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

package org.netbeans.lib.editor.codetemplates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.ErrorManager;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 * Abbreviation detection detects typing of an abbreviation
 * in the document.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class AbbrevDetection implements DocumentListener, PropertyChangeListener, KeyListener, CaretListener, PreferenceChangeListener {

    private static final Logger LOG = Logger.getLogger(AbbrevDetection.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(AbbrevDetection.class.getName());
    
    /**
     * Document property which determines whether an ongoing document modification
     * should be completely ignored by the abbreviation framework.
     * <br/>
     * This is useful e.g. for code templates parameter replication.
     */
    private static final String ABBREV_IGNORE_MODIFICATION_DOC_PROPERTY
            = "abbrev-ignore-modification"; // NOI18N

    private static final String EDITING_TEMPLATE_DOC_PROPERTY = "code-template-insert-handler"; //NOI18N

    private static final String SURROUND_WITH = NbBundle.getMessage(SurroundWithFix.class, "TXT_SurroundWithHint_Label"); //NOI18N
    private static final int SURROUND_WITH_DELAY = 250;
    
    public static AbbrevDetection get(JTextComponent component) {
        AbbrevDetection ad = (AbbrevDetection)component.getClientProperty(AbbrevDetection.class);
        if (ad == null) {
            ad = new AbbrevDetection(component);
            component.putClientProperty(AbbrevDetection.class, ad);
        }
        return ad;
    }
    
    public static synchronized void remove(JTextComponent component) {
        AbbrevDetection ad = (AbbrevDetection)component.getClientProperty(AbbrevDetection.class);
        if (ad != null) {
            assert ad.component == component : "Wrong component: AbbrevDetection.component=" + ad.component + ", component=" + component;
            ad.uninstall();
            component.putClientProperty(AbbrevDetection.class, null);
        }
    }
    
    private JTextComponent component;
    
    /** Document for which this abbreviation detection was constructed. */
    private Document doc;
    private DocumentListener weakDocL;
    
    /**
     * Offset after the last typed character of the collected abbreviation.
     */
    private Position abbrevEndPosition;

    /**
     * Abbreviation characters captured from typing.
     */
    private final StringBuffer abbrevChars = new StringBuffer();

//    /** Chars on which to expand acceptor */
//    private Acceptor expandAcceptor;

    /** Which chars reset abbreviation accounting */
    private Acceptor resetAcceptor;
    
    private MimePath mimePath = null;
    private Preferences prefs = null;
    private PreferenceChangeListener weakPrefsListener = null;
    
    private ErrorDescription errorDescription = null;
    private List<Fix> surrounsWithFixes = null;
    private Timer surroundsWithTimer;
    
    private AbbrevDetection(JTextComponent component) {
        this.component = component;
        component.addCaretListener(this);
        doc = component.getDocument();
        if (doc != null) {
            listenOnDoc();
        }

        String mimeType = DocumentUtilities.getMimeType(component);
        if (mimeType != null) {
            mimePath = MimePath.parse(mimeType);
            prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
        }
        
        // Load the settings
        preferenceChange(null);
        
        component.addKeyListener(this);
        component.addPropertyChangeListener(this);
        
        surroundsWithTimer = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // #124515, give up when the document is locked otherwise we are likely
                // to cause a deadlock.
                if (!DocumentUtilities.isReadLocked(doc)) {
                    showSurroundWithHint();
                }
            }
        });
        surroundsWithTimer.setRepeats(false);
    }
    
    private void listenOnDoc() {
        weakDocL = WeakListeners.document(this, doc);
        doc.addDocumentListener(weakDocL);
    }

    private void uninstall() {
        assert component != null : "Can't call uninstall before the construction finished";
        component.removeCaretListener(this);
        if (doc != null) {
            listenOnDoc();
        }

        component.removeKeyListener(this);
        component.removePropertyChangeListener(this);
        surroundsWithTimer.stop();
    }
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        String settingName = evt == null ? null : evt.getKey();
        if (settingName == null || "abbrev-reset-acceptor".equals(settingName)) { //NOI18N
            resetAcceptor = getResetAcceptor(prefs, mimePath);
        }
    }
    
    public void insertUpdate(DocumentEvent evt) {
        if (!isIgnoreModification()) {
            if (DocumentUtilities.isTypingModification(evt.getDocument()) && !isAbbrevDisabled()) {
                int offset = evt.getOffset();
                int length = evt.getLength();
                appendTypedText(offset, length);
            } else { // not typing modification -> reset abbreviation collecting
                resetAbbrevChars();
            }
        }
    }

    public void removeUpdate(DocumentEvent evt) {
        if (!isIgnoreModification()) {
            if (DocumentUtilities.isTypingModification(evt.getDocument()) && !isAbbrevDisabled()) {
                int offset = evt.getOffset();
                int length = evt.getLength();
                removeAbbrevText(offset, length);
            } else { // not typing modification -> reset abbreviation collecting
                resetAbbrevChars();
            }
        }
    }

    public void changedUpdate(DocumentEvent evt) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("document".equals(evt.getPropertyName())) { //NOI18N
            if (doc != null && weakDocL != null) {
                doc.removeDocumentListener(weakDocL);
                weakDocL = null;
            }
            
            doc = component.getDocument();
            if (doc != null) {
                listenOnDoc();
            }

            // unregister and destroy the old preferences (if we have any)
            if (prefs != null) {
                prefs.removePreferenceChangeListener(weakPrefsListener);
                prefs = null;
                weakPrefsListener = null;
                mimePath = null;
            }
            
            // load and hook up to preferences for the new mime type
            String mimeType = DocumentUtilities.getMimeType(component);
            if (mimeType != null) {
                mimePath = MimePath.parse(mimeType);
                prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
                weakPrefsListener = WeakListeners.create(PreferenceChangeListener.class, this, prefs);
                prefs.addPreferenceChangeListener(weakPrefsListener);
            }
            
            // reload the settings
            preferenceChange(null);
        }
    }
    
    public void keyPressed(KeyEvent evt) {
        checkExpansionKeystroke(evt);
    }
    
    public void keyReleased(KeyEvent evt) {
        checkExpansionKeystroke(evt);
    }
    
    public void keyTyped(KeyEvent evt) {
        checkExpansionKeystroke(evt);
    }
    
    public void caretUpdate(CaretEvent evt) {
        if (evt.getDot() != evt.getMark()) {
            surroundsWithTimer.setInitialDelay(SURROUND_WITH_DELAY);
            surroundsWithTimer.restart();
        } else {
            surroundsWithTimer.stop();
            hideSurroundWithHint();
        }
    }

    private boolean isIgnoreModification() {
        return Boolean.TRUE.equals(doc.getProperty(ABBREV_IGNORE_MODIFICATION_DOC_PROPERTY));
    }
    
    private boolean isAbbrevDisabled() {
        return org.netbeans.editor.Abbrev.isAbbrevDisabled(component);
    }
    
    private void checkExpansionKeystroke(KeyEvent evt) {
        Position pos = null;
        Document d = null;
        synchronized (abbrevChars) {
            if (abbrevEndPosition != null && component != null && doc != null
                && component.getCaretPosition() == abbrevEndPosition.getOffset()
                && !isAbbrevDisabled()
                && doc.getProperty(EDITING_TEMPLATE_DOC_PROPERTY) == null
            ) {
                pos = abbrevEndPosition;
                d = component.getDocument();
            }
        }
        if (pos != null && d != null) {
            CodeTemplateManagerOperation operation = CodeTemplateManagerOperation.get(d, pos.getOffset());
            if (operation != null) {
                KeyStroke expandKeyStroke = operation.getExpansionKey();

                if (expandKeyStroke.equals(KeyStroke.getKeyStrokeForEvent(evt))) {
                    if (expand(operation)) {
                        evt.consume();
                    }
                }
            }
        }
    }

    /**
     * Get current abbreviation string.
     */
    private CharSequence getAbbrevText() {
        return abbrevChars;
    }

    /**
     * Reset abbreviation string collecting.
     */
    private void resetAbbrevChars() {
        synchronized(abbrevChars) {
            abbrevChars.setLength(0);
            abbrevEndPosition = null;
        }
    }
    
    private void appendTypedText(int offset, int insertLength) {
        if (abbrevEndPosition == null
            || offset + insertLength != abbrevEndPosition.getOffset()
        ) {
            // Does not follow previous insert
            resetAbbrevChars();
        }

        if (abbrevEndPosition == null) { // starting the new string
            try {
                // Start new accounting if previous char would reset abbrev
                // i.e. check that not start typing 'u' after existing 'p' which would
                // errorneously expand to 'public'
                if (offset == 0
                        || resetAcceptor.accept(DocumentUtilities.getText(doc, offset - 1, 1).charAt(0))
                ) {
                    abbrevEndPosition = doc.createPosition(offset + insertLength);
                }
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        if (abbrevEndPosition != null) {
            try {
                String typedText = doc.getText(offset, insertLength); // typically just one char
                boolean textAccepted = true;
                for (int i = typedText.length() - 1; i >= 0; i--) {
                    if (resetAcceptor.accept(typedText.charAt(i))) {
                        // In theory there could be more than one character in the typed text
                        // and the resetting could occur on the very first char
                        // the next chars would not be accumulated as the insert
                        // is treated as a batch.
                        textAccepted = false;
                        break;
                    }
                }
                
                if (textAccepted) {
                    abbrevChars.append(typedText);
                    // abbrevEndPosition should move appropriately
                } else {
                    resetAbbrevChars();
                }

            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
                resetAbbrevChars();
            }
        }
    }
    
    private void removeAbbrevText(int offset, int removeLength) {
        synchronized(abbrevChars) {
            if (abbrevEndPosition != null) {
                // Abbrev position should already move appropriately
                if (offset == abbrevEndPosition.getOffset()
                    && abbrevChars.length() >= removeLength
                ) { // removed at end
                    abbrevChars.setLength(abbrevChars.length() - removeLength);

                } else {
                    resetAbbrevChars();
                }
            }
        }
    }

    public boolean expand(CodeTemplateManagerOperation op) {
        CharSequence abbrevText = getAbbrevText();
        int abbrevEndOffset = abbrevEndPosition.getOffset();
        if (expand(op, component, abbrevEndOffset - abbrevText.length(), abbrevText)) {
            resetAbbrevChars();
            return true;
        } else {
            return false;
        }
    }
    
    private void showSurroundWithHint() {
        try {
            final Caret caret = component.getCaret();
            if (caret != null) {
                final Position pos = doc.createPosition(caret.getDot());
                RP.post(new Runnable() {
                    public void run() {
                        final List<Fix> fixes = SurroundWithFix.getFixes(component);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                if (!fixes.isEmpty()) {
                                    errorDescription = ErrorDescriptionFactory.createErrorDescription(
                                            Severity.HINT, SURROUND_WITH, surrounsWithFixes = fixes, doc, pos, pos);

                                    HintsController.setErrors(doc, SURROUND_WITH, Collections.singleton(errorDescription));
                                } else {
                                    hideSurroundWithHint();
                                }
                            }
                        });
                    }
                });
            }
        } catch (BadLocationException ble) {
            Logger.getLogger("global").log(Level.WARNING, ble.getMessage(), ble);
        }
    }

    private void hideSurroundWithHint() {
        if (surrounsWithFixes != null)
            surrounsWithFixes = null;
        if (errorDescription != null) {
            errorDescription = null;
            HintsController.setErrors(doc, SURROUND_WITH, Collections.<ErrorDescription>emptySet());
        }
    }

    public static Acceptor getResetAcceptor(Preferences prefs, MimePath mimePath) {
        return prefs != null ? (Acceptor) callFactory(prefs, mimePath, "abbrev-reset-acceptor", AcceptorFactory.WHITESPACE) : AcceptorFactory.WHITESPACE; //NOI18N
    }

    // copied from org.netbeans.modules.editor.lib.SettingsConversions
    private static Object callFactory(Preferences prefs, MimePath mimePath, String settingName, Object defaultValue) {
        String factoryRef = prefs.get(settingName, null);
        
        if (factoryRef != null) {
            int lastDot = factoryRef.lastIndexOf('.'); //NOI18N
            assert lastDot != -1 : "Need fully qualified name of class with the static setting factory method."; //NOI18N

            String classFqn = factoryRef.substring(0, lastDot);
            String methodName = factoryRef.substring(lastDot + 1);

            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            try {
                Class factoryClass = loader.loadClass(classFqn);
                Method factoryMethod;
                
                try {
                    // normally the method should accept mime path and the a setting name
                    factoryMethod = factoryClass.getDeclaredMethod(methodName, MimePath.class, String.class);
                } catch (NoSuchMethodException nsme) {
                    // but there might be methods that don't need those params
                    try {
                        factoryMethod = factoryClass.getDeclaredMethod(methodName);
                    } catch (NoSuchMethodException nsme2) {
                        // throw the first exception complaining about the full signature
                        throw nsme;
                    }
                }
                
                Object value;
                if (factoryMethod.getParameterTypes().length == 2) {
                    value = factoryMethod.invoke(null, mimePath, settingName);
                } else {
                    value = factoryMethod.invoke(null);
                }
                
                if (value != null) {
                    return value;
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }

        return defaultValue;
    }

    private static boolean expand(CodeTemplateManagerOperation op, JTextComponent component, int abbrevStartOffset, CharSequence abbrev) {
        op.waitLoaded();
        CodeTemplate ct = op.findByAbbreviation(abbrev.toString());
        if (ct != null) {
            if (accept(ct, CodeTemplateManagerOperation.getTemplateFilters(component.getDocument(), abbrevStartOffset, abbrevStartOffset))) {
                Document doc = component.getDocument();
                sendUndoableEdit(doc, CloneableEditorSupport.BEGIN_COMMIT_GROUP);
                try {
                    // Remove the abbrev text
                    doc.remove(abbrevStartOffset, abbrev.length());
                    ct.insert(component);
                } catch (BadLocationException ble) {
                } finally {
                    sendUndoableEdit(doc, CloneableEditorSupport.END_COMMIT_GROUP);
                }
                return true;
            }
        }
        return false;
    }

    private static boolean accept(CodeTemplate template, Collection<? extends CodeTemplateFilter> filters) {
        for(CodeTemplateFilter filter : filters) {
            if (!filter.accept(template)) {
                return false;
            }
        }
        return true;
    }

    private static void sendUndoableEdit(Document d, UndoableEdit ue) {
        if(d instanceof AbstractDocument) {
            UndoableEditListener[] uels = ((AbstractDocument)d).getUndoableEditListeners();
            UndoableEditEvent ev = new UndoableEditEvent(d, ue);
            for(UndoableEditListener uel : uels) {
                uel.undoableEditHappened(ev);
            }
        }
    }
}
