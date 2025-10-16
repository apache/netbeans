/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.refactoring.java.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.RefactoringSession;

import static org.netbeans.modules.refactoring.java.ui.ContextAnalyzer.SHOW;

import org.netbeans.modules.refactoring.java.ui.InstantRefactoringUI;
import org.netbeans.modules.refactoring.java.ui.SyncDocumentRegion;
import org.netbeans.modules.refactoring.java.ui.UIUtilities;
import org.netbeans.modules.refactoring.java.ui.instant.CompletionLayout;
import org.netbeans.modules.refactoring.java.ui.instant.InstantOption;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.spi.editor.document.UndoableEditWrapper;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
@NbBundle.Messages({"# {0} - Shortcut text", "INFO_PressAgain=Press {0} Again for Refactoring Dialog"})
public final class InstantRefactoringPerformer implements DocumentListener, KeyListener, ProgressListener, PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(InstantRefactoringPerformer.class.getName());
    private static final Set<InstantRefactoringPerformer> registry = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private CompletionLayout compl;

    private SyncDocumentRegion region;
    private int span;
    private Document doc;
    private JTextComponent target;
    private InstantRefactoringUI ui;
    
    private AttributeSet attribs = null;
    private AttributeSet attribsLeft = null;
    private AttributeSet attribsRight = null;
    private AttributeSet attribsMiddle = null;
    private AttributeSet attribsAll = null;

    private AttributeSet attribsSlave = null;
    private AttributeSet attribsSlaveLeft = null;
    private AttributeSet attribsSlaveRight = null;
    private AttributeSet attribsSlaveMiddle = null;
    private AttributeSet attribsSlaveAll = null;

    public InstantRefactoringPerformer(final JTextComponent target, int caretOffset, InstantRefactoringUI ui) {
        releaseAll();
        this.target = target;
        this.ui = ui;
        doc = target.getDocument();

        MutablePositionRegion mainRegion = null;
        List<MutablePositionRegion> regions = new ArrayList<>(ui.getRegions().size());

        for (MutablePositionRegion current : ui.getRegions()) {
            // TODO: type parameter name is represented as ident -> ignore surrounding <> in rename
            if (isIn(current, caretOffset)) {
                mainRegion = current;
            } else {
                regions.add(current);
            }
        }

        if (mainRegion == null) {
            throw new IllegalArgumentException("No highlight contains the caret.");
        }

        regions.add(0, mainRegion);

        region = new SyncDocumentRegion(doc, regions);

        if (doc instanceof BaseDocument) {
            BaseDocument bdoc = ((BaseDocument) doc);
            bdoc.addPostModificationDocumentListener(this);
            
            UndoableWrapper wrapper = MimeLookup.getLookup("text/x-java").lookup(UndoableWrapper.class);
            if(wrapper != null) {
                wrapper.setActive(true, this);
            }

            UndoableEdit undo = new CancelInstantRenameUndoableEdit(this);
            for (UndoableEditListener l : bdoc.getUndoableEditListeners()) {
                l.undoableEditHappened(new UndoableEditEvent(doc, undo));
            }
        }

        target.addKeyListener(this);

        target.putClientProperty(InstantRefactoringPerformer.class, this);
        target.putClientProperty("NetBeansEditor.navigateBoundaries", mainRegion); // NOI18N
	
        requestRepaint();
        
        target.select(mainRegion.getStartOffset(), mainRegion.getEndOffset());
        
        span = region.getFirstRegionLength();
        compl = new CompletionLayout(this);
        compl.setEditorComponent(target);
        final KeyStroke OKKS = ui.getKeyStroke();
        target.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(OKKS, OKActionKey);
        Action OKAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if(registry.contains(InstantRefactoringPerformer.this)) {
                    doFullRefactoring();
                    target.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(OKKS);
                    target.getActionMap().remove(OKActionKey);
                } else {
                    target.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(OKKS);
                    target.getActionMap().remove(OKActionKey);
                }
            }
        };
        target.getActionMap().put(OKActionKey, OKAction);
        final KeyStroke keyStroke = ui.getKeyStroke();
        compl.showCompletion(ui.getOptions(), caretOffset, Bundle.INFO_PressAgain(getKeyStrokeAsText(keyStroke)));
        
        registry.add(this);
    }
    private static final String OKActionKey = "OK";
    
    private void doFullRefactoring() {
        releaseAll();
        final TopComponent activetc = TopComponent.getRegistry().getActivated();
        final Runnable task = new Runnable() {

            @Override
            public void run() {
                SHOW.show(ui.getRefactoringUI(), activetc);
            }
        };
        if(!EventQueue.isDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                    UIUtilities.runWhenScanFinished(task, "Instant Refactoring");
                }
            });
        } else {
            UIUtilities.runWhenScanFinished(task, "Instant Refactoring");
        }
    }

    private static void releaseAll() {
        Iterator<InstantRefactoringPerformer> it = registry.iterator();
        while(it.hasNext()) {
            it.next().release(true);
            it.remove();
        }
    }
    
    private static String getKeyStrokeAsText(KeyStroke keyStroke) {
        int modifiers = keyStroke.getModifiers();
        StringBuilder sb = new StringBuilder();
        sb.append('\'');
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) > 0) {
            sb.append("Ctrl+"); //NOI18N
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) > 0) {
            sb.append("Alt+"); //NOI18N
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) > 0) {
            sb.append("Shift+"); //NOI18N
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) > 0) {
            sb.append("Meta+"); //NOI18N
        }
        if (keyStroke.getKeyCode() != KeyEvent.VK_SHIFT
                && keyStroke.getKeyCode() != KeyEvent.VK_CONTROL
                && keyStroke.getKeyCode() != KeyEvent.VK_META
                && keyStroke.getKeyCode() != KeyEvent.VK_ALT
                && keyStroke.getKeyCode() != KeyEvent.VK_ALT_GRAPH) {
            sb.append(org.openide.util.Utilities.keyToString(
                    KeyStroke.getKeyStroke(keyStroke.getKeyCode(), 0)
            ));
        }
        sb.append('\'');
        return sb.toString();
    }
    
    private static boolean isIn(MutablePositionRegion region, int caretOffset) {
	return region.getStartOffset() <= caretOffset && caretOffset <= region.getEndOffset();
    }
    
    private volatile boolean inSync;
    
    @Override
    public synchronized void insertUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
        //check for modifications outside the first region:
        if (e.getOffset() < region.getFirstRegionStartOffset() || (e.getOffset() + e.getLength()) > region.getFirstRegionEndOffset()) {
            release(true);
            return;
        }
        
        inSync = true;
        region.sync();
        span = region.getFirstRegionLength();
        ui.updateInput(getRegionText(0));
        inSync = false;
        
        requestRepaint();
    }
    
    private String getRegionText(int regionIndex) {
        try {
            MutablePositionRegion r = region.getRegion(regionIndex);
            int offset = r.getStartOffset();
            int length = r.getEndOffset() - offset;
            return doc.getText(offset, length);
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    @Override
    public synchronized void removeUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
        if (e.getLength() == 1) {
            if ((e.getOffset() < region.getFirstRegionStartOffset() || e.getOffset() > region.getFirstRegionEndOffset())) {
                release(true);
                return;
            }

            if (e.getOffset() == region.getFirstRegionStartOffset() && region.getFirstRegionLength() > 0 && region.getFirstRegionLength() == span) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "e.getOffset()={0}", e.getOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionStartOffset()={0}", region.getFirstRegionStartOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionEndOffset()={0}", region.getFirstRegionEndOffset ());
                    LOG.log(Level.FINE, "span= {0}", span);
                }
                release(true);
                return;
            }
            
            if (e.getOffset() == region.getFirstRegionEndOffset() && region.getFirstRegionLength() > 0 && region.getFirstRegionLength() == span) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "e.getOffset()={0}", e.getOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionStartOffset()={0}", region.getFirstRegionStartOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionEndOffset()={0}", region.getFirstRegionEndOffset ());
                    LOG.log(Level.FINE, "span= {0}", span);
                }

                release(true);
                return;
            }
            if (e.getOffset() == region.getFirstRegionEndOffset() && e.getOffset() == region.getFirstRegionStartOffset() && region.getFirstRegionLength() == 0 && region.getFirstRegionLength() == span) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "e.getOffset()={0}", e.getOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionStartOffset()={0}", region.getFirstRegionStartOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionEndOffset()={0}", region.getFirstRegionEndOffset ());
                    LOG.log(Level.FINE, "span= {0}", span);
                }
                
               
                release(true);
                return;
            }
        } else {
            //selection/multiple characters removed:
            int removeSpan = e.getLength() + region.getFirstRegionLength();
            
            if (span < removeSpan) {
                release(true);
                return;
            }
        }
        
        //#89997: do not sync the regions for the "remove" part of replace selection,
        //as the consequent insert may use incorrect offset, and the regions will be synced
        //after the insert anyway.
        EditorUI editorUI = Utilities.getEditorUI(target);
        Boolean ovr = (Boolean) editorUI.getProperty(EditorUI.OVERWRITE_MODE_PROPERTY);
        if (doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY) != null || (ovr != null && ovr == true)) {
            return ;
        }
        
        inSync = true;
        region.sync();
        span = region.getFirstRegionLength();
        inSync = false;
        
        requestRepaint();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    public void caretUpdate(CaretEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        if (   (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiers() == 0) ||
               (e.getKeyCode() == KeyEvent.VK_ENTER  && e.getModifiers() == 0)
        ) {
            release(e.getKeyCode() == KeyEvent.VK_ESCAPE);
            e.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private synchronized void release(boolean cancel) {
        if(compl != null) {
            // should have been released
            compl.hideCompletion();
            compl = null;
        }
        if (target == null) {
            //already released
            return ;
        }
        
        UndoableWrapper wrapper = MimeLookup.getLookup("text/x-java").lookup(UndoableWrapper.class);
        if(wrapper != null) {
            wrapper.setActive(false, null);
            wrapper.close(true); // TODO: Simple changes do not need to go though full refactoring UI
        }
        target.putClientProperty("NetBeansEditor.navigateBoundaries", null); // NOI18N
        target.putClientProperty(InstantRefactoringPerformer.class, null);
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).removePostModificationDocumentListener(this);
        }
        target.removeKeyListener(this);
        target.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(ui.getKeyStroke());
        target.getActionMap().remove(OKActionKey);
        target = null;

        region = null;
        attribs = null;
        
        requestRepaint();

        doc = null;
        if (!cancel) {
            final DialogDescriptor descriptor = new DialogDescriptor(new JPanel(), ui.getName(), true, new Object[]{DialogDescriptor.CANCEL_OPTION}, null, 0, null, null);
            dialog = (JDialog) DialogDisplayer.getDefault().createDialog(descriptor);
            RP.post(new Runnable() {

                @Override
                public void run() {
                    dialog.setVisible(true);
                    if(ui != null) {
                        ui.getRefactoring().cancelRequest();
                    }
                    dialog.dispose();
                    dialog = null;
                }
            });
            RP.post(new Runnable() {
                @Override
                public void run() {
                    RefactoringSession session = RefactoringSession.create(ui.getName());
                    session.addProgressListener(InstantRefactoringPerformer.this);
                    ui.getRefactoring().addProgressListener(InstantRefactoringPerformer.this);
                    ui.getRefactoring().prepare(session);
                    if(dialog != null && dialog.isVisible()) {
                        dialog.getContentPane().setEnabled(false);
                        try {
                            session.doRefactoring(true);
                        } finally {
                            session.removeProgressListener(InstantRefactoringPerformer.this);
                            ui.getRefactoring().removeProgressListener(InstantRefactoringPerformer.this);
                            ui = null;
                            dialog.setVisible(false);
                        }
                    }
                }
            });
            
        }
    }

    private static final RequestProcessor RP = new RequestProcessor(InstantRefactoringPerformer.class.getName(), 2, false, false);
    
    private void requestRepaint() {
        if (region == null) {
            OffsetsBag bag = getHighlightsBag(doc);
            bag.clear();
        } else {
            // Compute attributes
            if (attribs == null) {
                // read the attributes for the master region
                attribs = getSyncedTextBlocksHighlight("synchronized-text-blocks-ext"); //NOI18N
                Color foreground = (Color) attribs.getAttribute(StyleConstants.Foreground);
                Color background = (Color) attribs.getAttribute(StyleConstants.Background);
                attribsLeft = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.LeftBorderLineColor, foreground, 
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsRight = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.RightBorderLineColor, foreground, 
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsMiddle = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsAll = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.LeftBorderLineColor, foreground, 
                        EditorStyleConstants.RightBorderLineColor, foreground,
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );

                // read the attributes for the slave regions
                attribsSlave = getSyncedTextBlocksHighlight("synchronized-text-blocks-ext-slave"); //NOI18N
                Color slaveForeground = (Color) attribsSlave.getAttribute(StyleConstants.Foreground);
                Color slaveBackground = (Color) attribsSlave.getAttribute(StyleConstants.Background);
                attribsSlaveLeft = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.LeftBorderLineColor, slaveForeground, 
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveRight = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.RightBorderLineColor, slaveForeground, 
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveMiddle = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveAll = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.LeftBorderLineColor, slaveForeground, 
                        EditorStyleConstants.RightBorderLineColor, slaveForeground,
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
            }
            
            OffsetsBag nue = new OffsetsBag(doc);
            for(int i = 0; i < region.getRegionCount(); i++) {
                int startOffset = region.getRegion(i).getStartOffset();
                int endOffset = region.getRegion(i).getEndOffset();
                int size = region.getRegion(i).getLength();
                if (size == 1) {
                    nue.addHighlight(startOffset, endOffset, i == 0 ? attribsAll : attribsSlaveAll);
                } else if (size > 1) {
                    nue.addHighlight(startOffset, startOffset + 1, i == 0 ? attribsLeft : attribsSlaveLeft);
                    nue.addHighlight(endOffset - 1, endOffset, i == 0 ? attribsRight : attribsSlaveRight);
                    if (size > 2) {
                        nue.addHighlight(startOffset + 1, endOffset - 1, i == 0 ? attribsMiddle : attribsSlaveMiddle);
                    }
                }
            }
            
            OffsetsBag bag = getHighlightsBag(doc);
            bag.setHighlights(nue);
        }
    }
    
//    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(138, 191, 236));
    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Foreground, Color.red);
    
    private static AttributeSet getSyncedTextBlocksHighlight(String name) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = fcs != null ? fcs.getFontColors(name) : null;
        return as == null ? defaultSyncedTextBlocksHighlight : as;
    }
    
    private static AttributeSet createAttribs(Object... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0 : "There must be even number of prameters. " +
            "They are key-value pairs of attributes that will be inserted into the set.";

        List<Object> list = new ArrayList<Object>();
        
        for(int i = keyValuePairs.length / 2 - 1; i >= 0 ; i--) {
            Object attrKey = keyValuePairs[2 * i];
            Object attrValue = keyValuePairs[2 * i + 1];

            if (attrKey != null && attrValue != null) {
                list.add(attrKey);
                list.add(attrValue);
            }
        }
        
        return AttributesUtilities.createImmutable(list.toArray());
    }
    
    public static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(InstantRefactoringPerformer.class);
        
        if (bag == null) {
            doc.putProperty(InstantRefactoringPerformer.class, bag = new OffsetsBag(doc));
            
            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            
            if (stream instanceof DataObject) {
                Logger.getLogger("TIMER").log(Level.FINE, "Instant Rename Highlights Bag", new Object[] {((DataObject) stream).getPrimaryFile(), bag}); //NOI18N
            }
        }
        
        return bag;
    }
    
    private JDialog dialog;
    private ProgressBar progressBar;
    private ProgressHandle progressHandle;
    private boolean isIndeterminate;

    /**
     * Implementation of ProgressListener.start method. Displays progress bar
     * and sets progress label and progress bar bounds.
     *
     * @param event Event object.
     */
    @Override
    @NbBundle.Messages({"LBL_ParametersCheck=Checking parameters...", "LBL_Usages=Usages", "LBL_Prepare=Prepare", "LBL_PreCheck=Initializing data..."})
    public void start(final ProgressEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                if (progressBar != null && progressBar.isVisible()) {
                    LOG.log(Level.INFO, "{0} called start multiple times", event.getSource());
                    stop(event);
                }

                progressBar = ProgressBar.create(progressHandle = ProgressHandle.createHandle("")); //NOI18N
                if (event.getCount() == -1) {
                    isIndeterminate = true;
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();
                } else {
                    isIndeterminate = false;
                    progressHandle.start(event.getCount());
                }
                String text;
                switch (event.getOperationType()) {
                    case AbstractRefactoring.PARAMETERS_CHECK:
                        text = NbBundle.getMessage(InstantRefactoringPerformer.class, "LBL_ParametersCheck");
                        break;
                    case AbstractRefactoring.PREPARE:
                        text = NbBundle.getMessage(InstantRefactoringPerformer.class, "LBL_Prepare");
                        break;
                    case AbstractRefactoring.PRE_CHECK:
                        text = NbBundle.getMessage(InstantRefactoringPerformer.class, "LBL_PreCheck");
                        break;
                    default:
                        text = NbBundle.getMessage(InstantRefactoringPerformer.class, "LBL_Usages");
                        break;
                }
                progressBar.setString(text);
                dialog.getContentPane().add(progressBar);
                dialog.pack();
            }
        });
    }

    /**
     * Implementation of ProgressListener.step method. Increments progress bar
     * value by 1.
     *
     * @param event Event object.
     */
    @Override
    public void step(final ProgressEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (progressHandle == null) {
                        return;
                    }

                    if (isIndeterminate && event.getCount() > 0) {
                        progressHandle.switchToDeterminate(event.getCount());
                        isIndeterminate = false;
                    } else {
                        progressHandle.progress(isIndeterminate ? -2 : event.getCount());
                    }
                } catch (Throwable e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }

    /**
     * Implementation of ProgressListener.stop method. Sets progress bar value
     * to its maximum.
     *
     * @param event Event object.
     */
    @Override
    public void stop(ProgressEvent event) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (progressHandle == null) {
                    return;
                }
                progressHandle.finish();
//                dialog.setVisible(false);
                //progressPanel.validate();
                //setButtonsEnabled(true); 
                //validate();
                progressHandle = null;
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(!evt.getOldValue().equals(evt.getNewValue()) && evt.getSource() instanceof InstantOption) {
            Set<MutablePositionRegion> newChangePoints = ui.optionChanged((InstantOption)evt.getSource());
            if(newChangePoints != null) {
                MutablePositionRegion mainRegion = region.getRegion(0);
                if(!newChangePoints.contains(mainRegion)) {
                    throw new IllegalArgumentException("MainRegion not part of the new change points.");
                }
                
                List<MutablePositionRegion> regions = new ArrayList<>(newChangePoints.size());

                for (MutablePositionRegion current : newChangePoints) {
                    if(current != mainRegion) {
                        regions.add(current);
                    }
                }

                regions.add(0, mainRegion);
                inSync = true;
                region.updateRegions(regions);
                inSync = false;
                requestRepaint();
            }
        }
    }
    
    private static class ProgressBar extends JPanel {

        private JLabel label;

        private static ProgressBar create(ProgressHandle handle) {
            ProgressBar instance = new ProgressBar();
            instance.setLayout(new BorderLayout());
            instance.label = new JLabel(" "); //NOI18N
            instance.label.setBorder(new EmptyBorder(0, 0, 2, 0));
            instance.add(instance.label, BorderLayout.NORTH);
            JComponent progress = ProgressHandleFactory.createProgressComponent(handle);
            instance.add(progress, BorderLayout.CENTER);
            return instance;
        }

        public void setString(String value) {
            label.setText(value);
        }

        private ProgressBar() {
        }
    }
    
    private static class CancelInstantRenameUndoableEdit extends AbstractUndoableEdit {

        private final Reference<InstantRefactoringPerformer> performer;

        public CancelInstantRenameUndoableEdit(InstantRefactoringPerformer performer) {
            this.performer = new WeakReference<InstantRefactoringPerformer>(performer);
        }

        @Override public boolean isSignificant() {
            return false;
        }

        @Override public void undo() throws CannotUndoException {
            InstantRefactoringPerformer perf = performer.get();

            if (perf != null) {
                perf.release(true);
            }
        }
    }
    
    @MimeRegistration(mimeType = "text/x-java", service = UndoableEditWrapper.class)
    public static class UndoableWrapper implements UndoableEditWrapper {

        private AtomicBoolean active = new AtomicBoolean(false);
        private Map<BaseDocument, UndoableEditDelegate> docToFirst = new HashMap<>();
        private InstantRefactoringPerformer performer;

        public UndoableWrapper() {
        }

        @Override
        public UndoableEdit wrap(UndoableEdit ed, Document doc) {
            if (!active.get()) {
                return ed;
            }
            if (doc.getProperty(BaseDocument.StreamDescriptionProperty) == null) {
                //no dataobject
                return ed;
            }
            UndoableEditDelegate current = new UndoableEditDelegate(ed, (BaseDocument) doc, performer);
            UndoableEditDelegate first = docToFirst.get(doc);
            if (first == null) {
                docToFirst.put((BaseDocument) doc, current);
            }
            return current;
        }

        public void setActive(boolean b, InstantRefactoringPerformer performer) {
            this.performer = performer;
            active.set(b);
        }

        public void close(boolean cancel) {
            for (UndoableEditDelegate first : docToFirst.values()) {
                first.end();
                if (cancel) {
                    first.undo();
                    first.die();
                }
            }
            docToFirst.clear();
        }

        public class UndoableEditDelegate implements UndoableEdit {

            private CloneableEditorSupport ces;
            private UndoableEdit delegate;
            private CompoundEdit inner;
            private InstantRefactoringPerformer performer;

            private UndoableEditDelegate(UndoableEdit ed, BaseDocument doc, InstantRefactoringPerformer performer) {
                DataObject dob = (DataObject) doc.getProperty(BaseDocument.StreamDescriptionProperty);
                this.ces = dob.getLookup().lookup(CloneableEditorSupport.class);
                this.inner = new CompoundEdit();
                this.inner.addEdit(ed);
                this.delegate = ed;
                this.performer = performer;
            }

            @Override
            public void undo() throws CannotUndoException {
//                JTextComponent focusedComponent = EditorRegistry.focusedComponent();
//                if (focusedComponent != null) {
//                    if (focusedComponent.getDocument() == ces.getDocument()) {
//                        //call global undo only for focused component
//                        undoManager.undo(session);
//                    }
//                }
                //delegate.undo();
                inner.undo();
            }

            @Override
            public boolean canUndo() {
//                return delegate.canUndo();
                return inner.canUndo();
            }

            @Override
            public void redo() throws CannotRedoException {
//                JTextComponent focusedComponent = EditorRegistry.focusedComponent();
//                if (focusedComponent != null) {
//                    if (focusedComponent.getDocument() == ces.getDocument()) {
//                        //call global undo only for focused component
//                        undoManager.redo(session);
//                    }
//                }
                //delegate.redo();
                inner.redo();
            }

            @Override
            public boolean canRedo() {
                //return delegate.canRedo();
                return inner.canRedo();
            }

            @Override
            public void die() {
                //delegate.die();
                inner.die();
            }

            @Override
            public boolean addEdit(UndoableEdit ue) {
                if (ue instanceof List) {
                    List<UndoableEdit> listEdit = (List<UndoableEdit>) ue;
                    UndoableEdit topEdit = listEdit.get(listEdit.size() - 1);
                    // Check that there's only original document's edit and the wrapping refactoring edit
                    boolean refatoringEditOnly = listEdit.size() == 2;
                    if (refatoringEditOnly && topEdit instanceof UndoableEditDelegate) {
                        inner.addEdit(listEdit.get(0));
                        return true;
                    }
                    return false;
                }
                return false;
            }

            public UndoableEdit unwrap() {
                return delegate;
            }

            @Override
            public boolean replaceEdit(UndoableEdit ue) {
                return inner.replaceEdit(ue);
                //return delegate.replaceEdit(ue);
            }

            @Override
            public boolean isSignificant() {
                return inner.isSignificant();
                //return delegate.isSignificant();
            }

            @Override
            public String getPresentationName() {
                return "Rename";
            }

            @Override
            public String getUndoPresentationName() {
                return "Undo Rename";
            }

            @Override
            public String getRedoPresentationName() {
                return "Redo Rename";
            }

            private void end() {
                inner.end();
            }
        }
    }
    
    @ServiceProvider(service=RefactoringPluginFactory.class, position=95)
    public static class AllRefactoringsPluginFactory implements RefactoringPluginFactory {

        @Override
        public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
            return new RefactoringPluginImpl();
        }

        private static final class RefactoringPluginImpl implements RefactoringPlugin {

            @Override
            public Problem preCheck() {
                return null;
            }

            @Override
            public Problem checkParameters() {
                return null;
            }

            @Override
            public Problem fastCheckParameters() {
                return null;
            }

            @Override
            public void cancelRequest() {}

            @Override
            public Problem prepare(RefactoringElementsBag refactoringElements) {
                refactoringElements.getSession().addProgressListener(new ProgressListener() {
                    @Override
                    public void start(ProgressEvent event) {
                        final InstantRefactoringPerformer[] performers = registry.toArray(new InstantRefactoringPerformer[0]);
                        for (InstantRefactoringPerformer p : performers) {
                            p.inSync = true;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                for (InstantRefactoringPerformer p : performers) {
                                    p.release(true);
                                }
                            }
                        });
                    }
                    @Override
                    public void step(ProgressEvent event) {}
                    @Override
                    public void stop(ProgressEvent event) {}
                });

                return null;
            }

        }

    }
    
    public static class RenameDeletedTextInterceptor implements DeletedTextInterceptor {
        
        @Override
        public boolean beforeRemove(DeletedTextInterceptor.Context context) throws BadLocationException {
            Object getObject = context.getComponent().getClientProperty(InstantRefactoringPerformer.class);
            if (getObject instanceof InstantRefactoringPerformer) {
                InstantRefactoringPerformer instantRenamePerformer = (InstantRefactoringPerformer)getObject;
                MutablePositionRegion region = instantRenamePerformer.region.getRegion(0);
                return ((context.isBackwardDelete() && region.getStartOffset() == context.getOffset()) || (!context.isBackwardDelete() && region.getEndOffset() == context.getOffset()));
            } else {
                return false;
            }
        }
        @Override
        public void remove(DeletedTextInterceptor.Context context) throws BadLocationException {            
        }

        @Override
        public void afterRemove(DeletedTextInterceptor.Context context) throws BadLocationException {
        }

        @Override
        public void cancelled(DeletedTextInterceptor.Context context) {
        }

        @MimeRegistrations({
            @MimeRegistration(mimeType = "text/x-java", service = DeletedTextInterceptor.Factory.class)
        })
        public static class Factory implements DeletedTextInterceptor.Factory {

            @Override
            public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
                return new RenameDeletedTextInterceptor();
            }
        }
    }
}
