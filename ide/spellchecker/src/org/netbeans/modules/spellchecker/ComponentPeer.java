/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.spellchecker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JLayeredPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spellchecker.spi.dictionary.Dictionary;
import org.netbeans.modules.spellchecker.api.LocaleQuery;
import org.netbeans.modules.spellchecker.hints.AddToDictionaryHint;
import org.netbeans.modules.spellchecker.hints.DictionaryBasedHint;
import org.netbeans.modules.spellchecker.spi.dictionary.DictionaryProvider;
import org.netbeans.modules.spellchecker.spi.dictionary.ValidityType;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.netbeans.modules.spellchecker.spi.language.TokenListProvider;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
 *
 * @author Jan Lahoda
 */
public class ComponentPeer implements PropertyChangeListener, DocumentListener, ChangeListener, CaretListener, AncestorListener {

    private static final Logger LOG = Logger.getLogger(ComponentPeer.class.getName());
    
    private DocumentListener weakDocL;
    
    public static void assureInstalled(JTextComponent pane) {
        if (pane.getClientProperty(ComponentPeer.class) == null) {
            pane.putClientProperty(ComponentPeer.class, new ComponentPeer(pane));
        }
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (document != pane.getDocument()) {
            if (document != null) {
                document.removeDocumentListener(weakDocL);
                weakDocL = null;
            }
            document = pane.getDocument();
            weakDocL = WeakListeners.document(this, document);
            document.addDocumentListener(weakDocL);
            document = pane.getDocument();
            synchronized (tokenListLock) {
                tokenList = null;
            }
            doUpdateCurrentVisibleSpan();
        }
    }

    private final JTextComponent pane;
    private Document document;

    private static final RequestProcessor WORKER = new RequestProcessor("Spellchecker", 1, false, false);
    
    private final RequestProcessor.Task checker = WORKER.create(new Runnable() {
        public void run() {
            try {
                process();
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            }
        }
    });

    private final RequestProcessor.Task updateVisibleSpans = WORKER.create(new Runnable() {
        public void run() {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        updateCurrentVisibleSpan();
                        reschedule();
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    });

    private final RequestProcessor.Task computeHint = WORKER.create(new Runnable() {
        public void run() {
            computeHint();
        }
    });
    
    public void reschedule() {
        cancel();
        checker.schedule(100);
    }
    
    private synchronized Document getDocument() {
        return document;
    }

    /** Creates a new instance of ComponentPeer */
    private ComponentPeer(JTextComponent pane) {
        this.pane = pane;
//        reschedule();
        pane.addPropertyChangeListener(this);
        pane.addCaretListener(this);
        pane.addAncestorListener(this);
        document = pane.getDocument();
        weakDocL = WeakListeners.document(this, document);
        document.addDocumentListener(weakDocL);

        ancestorAdded(null);
    }
    
    private Component parentWithListener;

    private int[] computeVisibleSpan() {
        Component parent = pane.getParent();
        
        if (parent instanceof JLayeredPane) {
            parent = parent.getParent();
        }

        if (parent instanceof JViewport) {
            JViewport vp = (JViewport) parent;

            Point start = vp.getViewPosition();
            Dimension size = vp.getExtentSize();
            Point end = new Point((int) (start.getX() + size.getWidth()), (int) (start.getY() + size.getHeight()));

            int startPosition = pane.viewToModel(start);
            int endPosition = pane.viewToModel(end);

            if (parentWithListener != vp) {
                vp.addChangeListener(WeakListeners.change(this, vp));
                parentWithListener = vp;
            }
            return new int[] {startPosition, endPosition};
        }

        return new int[] {0, pane.getDocument().getLength()};
    }

    private void updateCurrentVisibleSpan() {
        //check possible change in visible rect:
        int[] newSpan = computeVisibleSpan();
        
        synchronized (this) {
            if (currentVisibleRange == null || currentVisibleRange[0] != newSpan[0] || currentVisibleRange[1] != newSpan[1]) {
                currentVisibleRange = newSpan;
                reschedule();
            }
        }
    }

    private int[] currentVisibleRange;

    private synchronized int[] getCurrentVisibleSpan() {
        return currentVisibleRange;
    }

    private final Object tokenListLock = new Object();
    private TokenList tokenList;
    
    private TokenList getTokenList(Document doc) {
        synchronized(tokenListLock) {
            if (tokenList == null) {
                tokenList = ACCESSOR.lookupTokenList(doc);

                if (tokenList != null)
                    tokenList.addChangeListener(this);
            }

            return tokenList;
        }
    }
    
    private void process() throws BadLocationException {
        final Document _document = getDocument();
        
        if (_document.getLength() == 0)
            return ;
        
        final List<int[]> localHighlights = new LinkedList<int[]>();
        
        long startTime = System.currentTimeMillis();
        
        try {
            resume();
            
            final TokenList _tokenList = getTokenList(_document);
            
            if (_tokenList == null) {
                //nothing to do:
                return ;
            }

            Dictionary d = getDictionary(_document);

            if (d == null)
                return ;
            
            final int[] span = getCurrentVisibleSpan();

            if (span == null) {
                //not initialized yet:
                doUpdateCurrentVisibleSpan();
                return ;
            }
            
            if (span[0] == (-1)) {
                return ;
            }

            final boolean[] cont = new boolean [1];
            
            _document.render(new Runnable() {
                public void run() {
                    if (isCanceled()) {
                        cont[0] = false;
                        return;
                    } else {
                        _tokenList.setStartOffset(span[0]);
                        cont[0] = true;
                    }
                }
            });
            
            if (!cont[0]) {
                return ;
            }

            final CharSequence[] word = new CharSequence[1];
            
            while (!isCanceled()) {
                _document.render(new Runnable() {
                    public void run() {
                        if (isCanceled()) {
                            cont[0] = false;
                            return ;
                        }
                        
                        if (cont[0] = _tokenList.nextWord()) {
                            if (_tokenList.getCurrentWordStartOffset() > span[1]) {
                                cont[0] = false;
                                return ;
                            }
                            
                            word[0] = _tokenList.getCurrentWordText();
                        }
                    }
                });
                
                if (!cont[0])
                    break;
                
                LOG.log(Level.FINER, "going to test word: {0}", word[0]);
                
                if (word[0].length() < 2) {
                    //ignore single letter words
                    LOG.log(Level.FINER, "too short");
                    continue;
                }
                
                ValidityType validity = d.validateWord(word[0]);
                
                LOG.log(Level.FINER, "validity: {0}", validity);

                switch (validity) {
                    case PREFIX_OF_VALID:
                    case BLACKLISTED:
                    case INVALID:
                        _document.render(new Runnable() {
                            public void run() {
                                if (!isCanceled()) {
                                    localHighlights.add(new int[] {_tokenList.getCurrentWordStartOffset(), _tokenList.getCurrentWordStartOffset() + word[0].length()});
                                }
                            }
                        });
                }
            }
        } finally {
            if (!isCanceled()) {
                if (!(pane instanceof JEditorPane)) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            _document.render(new Runnable() {
                                public void run() {
                                    if (isCanceled()) {
                                        return;
                                    }
                                    try {
                                        Highlighter h = pane.getHighlighter();

                                        if (h != null) {
                                            List<Object> oldTags = (List<Object>) pane.getClientProperty(ErrorHighlightPainter.class);

                                            if (oldTags != null) {
                                                for (Object tag : oldTags) {
                                                    h.removeHighlight(tag);
                                                }
                                            }

                                            List<Object> newTags = new LinkedList<Object>();
                                            for (int[] current : localHighlights) {
                                                newTags.add(h.addHighlight(current[0], current[1], new ErrorHighlightPainter()));
                                            }

                                            pane.putClientProperty(ErrorHighlightPainter.class, newTags);
                                        }
                                    } catch (BadLocationException e) {
                                        Exceptions.printStackTrace(e);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    final OffsetsBag paneBag = SpellcheckerHighlightLayerFactory.getBag(pane);
                    _document.render(new Runnable() {
                        public void run() {
                            if (isCanceled()) {
                                return;
                            }
                            OffsetsBag localHighlightsBag = new OffsetsBag(_document);

                            for (int[] current : localHighlights) {
                                localHighlightsBag.addHighlight(current[0], current[1], ERROR);
                            }
                            paneBag.setHighlights(localHighlightsBag);
                        }
                    });
                }
                
                FileObject file = NbEditorUtilities.getFileObject(_document);

                if (file != null) {
                    Logger.getLogger("TIMER").log(Level.FINE, "Spellchecker",
                            new Object[] {file, System.currentTimeMillis() - startTime});
                }
            }
        }
    }

    private static Set<Document> knownDocuments = new WeakSet<Document>();
    private static Map<Locale, DictionaryImpl> locale2UsersLocalDictionary = new HashMap<Locale, DictionaryImpl>();
    private static Map<Project, Reference<DictionaryImpl>> project2Reference = new WeakHashMap<Project, Reference<DictionaryImpl>>();
    
    public static synchronized DictionaryImpl getUsersLocalDictionary(Locale locale) {
        DictionaryImpl d = locale2UsersLocalDictionary.get(locale);
        
        if (d != null)
            return d;
        
        File cache = new File(Places.getUserDirectory(), "private-dictionary-" + locale.toString());
        
        locale2UsersLocalDictionary.put(locale, d = new DictionaryImpl(cache, locale));
        
        return d;
    }
    
    public static synchronized DictionaryImpl getProjectDictionary(Project p, Locale locale) {
        Reference<DictionaryImpl> r = project2Reference.get(p);
        DictionaryImpl d = r != null ? r.get() : null;
        
        if (d == null) {
            AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration(p);
            project2Reference.put(p, new WeakReference<DictionaryImpl>(d = new DictionaryImpl(p, ac, locale)));
        }
        
        return d;
    }
    
    public static synchronized Dictionary getDictionary(Document doc) {
        Dictionary result = (Dictionary) doc.getProperty(CompoundDictionary.class);
        
        if (result != null) {
            return result;
        }
        
        Locale locale;
        
        FileObject file = NbEditorUtilities.getFileObject(doc);

        if (file != null) {
            locale = LocaleQuery.findLocale(file);
        } else {
            locale = DefaultLocaleQueryImplementation.getDefaultLocale();
        }
        
        if (locale == null) {
            locale = Locale.getDefault();
        }
        
        Dictionary d = ACCESSOR.lookupDictionary(locale);
        
        if (d == null)
            return null; //XXX
        
        List<Dictionary> dictionaries = new LinkedList<Dictionary>();
        
        dictionaries.add(getUsersLocalDictionary(locale));
        
        if (file != null) {
            Project p = FileOwnerQuery.getOwner(file);

            if (p != null) {
                Dictionary projectDictionary = getProjectDictionary(p, locale);

                if (projectDictionary != null) {
                    dictionaries.add(projectDictionary);
                }
            }
        }
        
        dictionaries.add(d);
        
        result = CompoundDictionary.create(dictionaries.toArray(new Dictionary[0]));

        doc.putProperty(CompoundDictionary.class, result);
        knownDocuments.add(doc);
        
        return result;
    }

    static synchronized void clearDoc2DictionaryCache() {
        for (Document d : knownDocuments) {
            d.putProperty(CompoundDictionary.class, null);
        }
        knownDocuments.clear();
    }

    private boolean isCanceled() {
        return cancel.get();
    }

    private void cancel() {
        cancel.set(true);
    }

    private void resume() {
        cancel.set(false);
    }

    private final AtomicBoolean cancel = new AtomicBoolean();

    private static final AttributeSet ERROR = AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, Color.RED, EditorStyleConstants.Tooltip, NbBundle.getMessage(ComponentPeer.class, "TP_MisspelledWord"));

    public void insertUpdate(DocumentEvent e) {
        documentUpdate();
    }

    public void removeUpdate(DocumentEvent e) {
        documentUpdate();
    }

    public void changedUpdate(DocumentEvent e) {
    }

    private void documentUpdate() {
        doUpdateCurrentVisibleSpan();
        cancel();
    }
    
    private void doUpdateCurrentVisibleSpan() {
        //#156490: updateCurrentVisibleSpan invokes viewToModel, which may throw StateInvariantError
        //if the starting position of view disappeared from the document in the current change (before the views are adjusted)
        //reschedule to later, when the views are adjusted to the new state
        updateVisibleSpans.schedule(250);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == tokenList) {
            reschedule();
        } else {
            doUpdateCurrentVisibleSpan();
        }
    }
    
    public void caretUpdate(CaretEvent e) {
        synchronized (this) {
            lastCaretPosition = e.getDot();
        }
        
        LOG.fine("scheduling hints computation");
        
        computeHint.schedule(100);
    }
    
    private int lastCaretPosition = -1;
            
    private void computeHint() {
        LOG.entering(ComponentPeer.class.getName(), "computeHint");

        final Document _document = getDocument();
        final TokenList l = getTokenList(_document);

        if (l == null) {
            //nothing to do:
            LOG.fine("token list == null");
            LOG.exiting(ComponentPeer.class.getName(), "computeHint");
            return ;
        }

        final Dictionary d = ComponentPeer.getDictionary(_document);
        
        if (d == null) {
            LOG.fine("dictionary == null");
            LOG.exiting(ComponentPeer.class.getName(), "computeHint");
            return ;
        }
        
        final int[] lastCaretPositionCopy = new int[1];
        final Position[] span = new Position[2];
        final CharSequence[]   word = new CharSequence[1];
        
        synchronized (this) {
            lastCaretPositionCopy[0] = lastCaretPosition;
        }
        
        _document.render(new Runnable() {
            public void run() {
                LOG.log(Level.FINE, "lastCaretPosition={0}", lastCaretPositionCopy[0]);
                l.setStartOffset(lastCaretPositionCopy[0]);
                
                if (!l.nextWord()) {
                    LOG.log(Level.FINE, "l.nextWord() == false");
                    return ;
                }
                
                int currentWSO = l.getCurrentWordStartOffset();
                CharSequence w = l.getCurrentWordText();
                int length     = w.length();
                
                LOG.log(Level.FINE, "currentWSO={0}, w={1}, length={2}", new Object[] {currentWSO, w, length});
                
                if (currentWSO <= lastCaretPositionCopy[0] && (currentWSO + length) >= lastCaretPositionCopy[0]) {
                    try {
                        span[0] = _document.createPosition(currentWSO);
                        span[1] = _document.createPosition(currentWSO + length);
                        word[0] = w;
                    } catch (BadLocationException e) {
                        LOG.log(Level.INFO, null, e);
                    }
                }
            }
        });

        if (span[0] != null && span[1] != null) {
            ValidityType validity = d.validateWord(word[0]);

            if (validity == ValidityType.VALID) {
                span[0] = span[1] = null;
            }
        }
        
        List<Fix> result = new ArrayList<Fix>();
        
        LOG.log(Level.FINE, "word={0}", word[0]);
        
        if (span[0] != null && span[1] != null) {
            String currentWord = word[0].toString();
            
            for (String proposal : d.findProposals(currentWord)) {
                result.add(new DictionaryBasedHint(currentWord, proposal, _document, span, "0" + currentWord));
            }
            
            FileObject file = NbEditorUtilities.getFileObject(_document);

            if (file != null) {
                Project p = FileOwnerQuery.getOwner(file);
                Locale locale = LocaleQuery.findLocale(file);
                
                if (p != null) {
                    DictionaryImpl projectDictionary = getProjectDictionary(p, locale);
                    
                    if (projectDictionary != null) {
                        String displayName = NbBundle.getMessage(ComponentPeer.class, "FIX_ToProjectDictionary");
                        result.add(new AddToDictionaryHint(this, projectDictionary, currentWord, displayName, "1" + currentWord));
                    }
                }
            
                String displayName = NbBundle.getMessage(ComponentPeer.class, "FIX_ToPrivateDictionary");

                result.add(new AddToDictionaryHint(this, getUsersLocalDictionary(locale), currentWord, displayName, "2" + currentWord));
            }
            
            if (!result.isEmpty()) {
                String displayName = NbBundle.getMessage(ComponentPeer.class, "ERR_MisspelledWord");
                HintsController.setErrors(_document, ComponentPeer.class.getName(), Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(Severity.HINT, displayName, result, _document, span[0], span[1])));
            } else {
                HintsController.setErrors(_document, ComponentPeer.class.getName(), Collections.<ErrorDescription>emptyList());
            }
        } else {
            HintsController.setErrors(_document, ComponentPeer.class.getName(), Collections.<ErrorDescription>emptyList());
        }
    }
    
    public static LookupAccessor ACCESSOR = new LookupAccessor() {
        public Dictionary lookupDictionary(Locale locale) {
            for (DictionaryProvider p : Lookup.getDefault().lookupAll(DictionaryProvider.class)) {
                Dictionary d = p.getDictionary(locale);
                
                if (d != null)
                    return d;
            }
            
            return null;
        }
        public TokenList lookupTokenList(Document doc) {
            Object mimeTypeObj = doc.getProperty("mimeType");
            String mimeType = "text/plain";
            
            if (mimeTypeObj instanceof String) {
                mimeType = (String) mimeTypeObj;
            }
            
            for (TokenListProvider p : MimeLookup.getLookup(MimePath.get(mimeType)).lookupAll(TokenListProvider.class)) {
                TokenList l = p.findTokenList(doc);
                
                if (l != null)
                    return l;
            }
            
            return null;
            
        }
    };

    public void ancestorAdded(AncestorEvent event) {
        if (pane.getParent() != null)
            doUpdateCurrentVisibleSpan();
    }

    public void ancestorRemoved(AncestorEvent event) {}

    public void ancestorMoved(AncestorEvent event) {}
    
    private class ErrorHighlightPainter implements HighlightPainter {
        private ErrorHighlightPainter() {
        }

        public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
            g.setColor(Color.RED);
            
            try {
                Rectangle start = pane.modelToView(p0);
                Rectangle end = pane.modelToView(p1);

                if (start.x < 0) {
                    LOG.log(Level.INFO, "#182545: negative view position: {0} for: {1}", new Object[] {start, p0});
                    return;
                }

                int waveLength = end.x + end.width - start.x;
                if (waveLength > 0) {
                    int[] wf = {0, 0, -1, -1};
                    int[] xArray = new int[waveLength + 1];
                    int[] yArray = new int[waveLength + 1];

                    int yBase = (int) (start.y + start.height - 2);
                    for (int i = 0; i <= waveLength; i++) {
                        xArray[i] = start.x + i;
                        yArray[i] = yBase + wf[xArray[i] % 4];
                    }
                    g.drawPolyline(xArray, yArray, waveLength);
                }
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

}
