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
package org.netbeans.modules.cnd.refactoring.actions;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * perform instant rename action
 * 
 */
public class InstantRenamePerformer implements DocumentListener, KeyListener, FocusListener {
    private static final String POSITION_BAG = "CndInstantRenamePerformer"; // NOI18N

    private SyncDocumentRegion region;
    private Document doc;
    private JTextComponent target;
    private final PositionsBag bag;
    
    /** Creates a new instance of InstantRenamePerformer */
    /*package*/ InstantRenamePerformer(JTextComponent target,  Collection<CsmReference> highlights, int caretOffset) throws BadLocationException {
	this.target = target;
	this.doc = target.getDocument();
	
	MutablePositionRegion mainRegion = null;
	List<MutablePositionRegion> regions = new ArrayList<>();
        bag = new PositionsBag(doc);
        
	for (CsmReference h : highlights) {
	    Position start = NbDocument.createPosition(doc, h.getStartOffset(), Bias.Backward);
	    Position end = NbDocument.createPosition(doc, h.getEndOffset(), Bias.Forward);
	    MutablePositionRegion current = new MutablePositionRegion(start, end);
	    
            AttributeSet attribs;
	    if (isIn(current, caretOffset)) {
		mainRegion = current;
                attribs = getSyncedTextBlocksHighlight("synchronized-text-blocks-ext"); //NOI18N
	    } else {
		regions.add(current);
                attribs = getSyncedTextBlocksHighlight("synchronized-text-blocks-ext-slave"); //NOI18N
	    }
	    
            Color foreground = (Color) attribs.getAttribute(StyleConstants.Foreground);
            Color background = (Color) attribs.getAttribute(StyleConstants.Background);
            AttributeSet attribsAll = createAttributeSet(
                StyleConstants.Background, background,
                EditorStyleConstants.LeftBorderLineColor, foreground, 
                EditorStyleConstants.RightBorderLineColor, foreground,
                EditorStyleConstants.TopBorderLineColor, foreground, 
                EditorStyleConstants.BottomBorderLineColor, foreground
            );
            bag.addHighlight(start, end, attribsAll);
	}
	
        if (mainRegion == null) {
            Logger.getLogger(InstantRenamePerformer.class.getName()).log(Level.WARNING, "No highlight contains the caret ({0}; highlights={1})", new Object[]{caretOffset, highlights}); //NOI18N
            // Attempt to use another region - pick the one closest to the caret
            if (regions.size() > 0) {
                mainRegion = regions.get(0);
                int mainDistance = Integer.MAX_VALUE;
                for (MutablePositionRegion r : regions) {
                    int distance = caretOffset < r.getStartOffset() ? (r.getStartOffset() - caretOffset) : (caretOffset - r.getEndOffset());
                    if (distance < mainDistance) {
                        mainRegion = r;
                        mainDistance = distance;
                    }
                }
            } else {
                return;
            }
        }
	
	regions.add(0, mainRegion);
	
	this.region = new SyncDocumentRegion(doc, regions);
        
        if (doc instanceof BaseDocument) {
            final BaseDocument bdoc = (BaseDocument) doc;
            bdoc.addPostModificationDocumentListener(InstantRenamePerformer.this);
            
//            UndoableEdit undo = new CancelInstantRenameUndoableEdit(this);
//            for (UndoableEditListener l : bdoc.getUndoableEditListeners()) {
//                l.undoableEditHappened(new UndoableEditEvent(doc, undo));
//            }            
        }
        target.addFocusListener(InstantRenamePerformer.this);

        target.addKeyListener(InstantRenamePerformer.this);

        target.putClientProperty(InstantRenamePerformer.class, InstantRenamePerformer.this);
	
        getHighlightsBag(doc).setHighlights(bag);
        
        target.select(mainRegion.getStartOffset(), mainRegion.getEndOffset());
    }
    
//    private FileObject getFileObject() {
//	DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
//	
//	if (od == null)
//	    return null;
//	
//	return od.getPrimaryFile();
//    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(InstantRenamePerformer.class, key);
    }
    
    public static void invokeInstantRename(JTextComponent target) {
        try {
            final int caret = target.getCaretPosition();   
            Document doc = target.getDocument();
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            CsmFile file = CsmUtilities.getCsmFile(dobj, false, false);
            if (file == null) {
                Utilities.setStatusBoldText(target, getString("no-instant-rename")); // NOI18N
                return;
            }
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(file, doc, caret);
            if (ref == null) {
                Utilities.setStatusBoldText(target, getString("no-instant-rename")); // NOI18N
                return;
            }

            boolean doFullRename = true;
            if (allowInstantRename(ref, dobj.getPrimaryFile())) {
                Collection<CsmReference> changePoints = computeChangePoints(ref);
                if (!changePoints.isEmpty()) {
                    doFullRename = false;
                    doInstantRename(changePoints, target, caret);
                }
            }
            if (doFullRename) {
                doFullRename(dobj, target, ref);
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private static boolean allowInstantRename(CsmReference ref, FileObject fo) {
        if (CsmRefactoringUtils.isRefactorable(fo)) {
            return allowInstantRename(ref);
        } else {
            return false;
        }
    }
    
    /*package*/ static boolean allowInstantRename(CsmReference ref) {
        CsmReferenceResolver.Scope scope = CsmReferenceResolver.getDefault().fastCheckScope(ref);
        if (scope == CsmReferenceResolver.Scope.LOCAL) {
            return true;
        } else if (scope == CsmReferenceResolver.Scope.FILE_LOCAL) {
            // allow if file is not included anywhere
            return CsmIncludeHierarchyResolver.getDefault().getFiles(ref.getContainingFile()).isEmpty();
        } else {
            return false;
        }
    }
    
    private static void doFullRename(DataObject dobj, JTextComponent target, CsmReference ref) {
        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        Node n = dobj.getNodeDelegate();
        if (n == null) {
             Utilities.setStatusBoldText(target, getString("no-instant-rename")); // NOI18N
             return;
        }
        InstanceContent ic = new InstanceContent();
        if (ec != null) {
            ic.add(ec);
        }
        ic.add(n);
        ic.add(ref);
        Lookup actionContext = new AbstractLookup(ic);
        
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
        a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }
    
    private static void doInstantRename(Collection<CsmReference> changePoints, JTextComponent target, int caret) throws BadLocationException {
        performInstantRename(target, changePoints, caret);
    }
        
    static Collection<CsmReference> computeChangePoints(CsmReference ref) {
        CsmObject resolved = ref.getReferencedObject();
        if (resolved == null) {
            return Collections.<CsmReference>emptyList();
        }
        CsmFile file = ref.getContainingFile();
        Collection<CsmReference> out = CsmReferenceRepository.getDefault().getReferences(resolved, file, CsmReferenceKind.ALL, Interrupter.DUMMY);
        return out;
    }
    
    public static void invokeInstantRename(JTextComponent target, ChangeInfo changeInfo) throws BadLocationException {
        Collection<CsmReference> highlights = new ArrayList<>(changeInfo.size());
        int size = changeInfo.size();
        int caretOffset = -1;
        for (int i = 0; i < size; i++) {
            ChangeInfo.Change change = changeInfo.get(i);
            highlights.add(new RefImpl(change));
            if (caretOffset < 0) {
                caretOffset = change.getEnd().getOffset();
            }
        }
        performInstantRename(target, highlights, caretOffset);
    }
    
    public synchronized static void performInstantRename(JTextComponent target, Collection<CsmReference> highlights, int caretOffset) throws BadLocationException {
        if (instance != null) {
            // cancel previouse rename
            instance.release();
        }
        UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, "INSTANT_RENAME"); // NOI18N
        instance = new InstantRenamePerformer(target, highlights, caretOffset);
    }

    private boolean isIn(MutablePositionRegion region, int caretOffset) {
	return region.getStartOffset() <= caretOffset && caretOffset <= region.getEndOffset();
    }
    
    private boolean inSync;
    
    @Override
    public synchronized void insertUpdate(DocumentEvent e) {
        if (inSync) {
            return;
        }
        updateOnInsertRemove();
    }

    @Override
    public synchronized void removeUpdate(DocumentEvent e) {
        if (inSync) {
            return ;
        }
        //#89997: do not sync the regions for the "remove" part of replace selection,
        //as the consequent insert may use incorrect offset, and the regions will be synced
        //after the insert anyway.
        if (doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY) != null) {
            return ;
        }

        updateOnInsertRemove();
    }

    private void updateOnInsertRemove() {
      inSync = true;
      JTextComponent aTarget = target;
      if (region.sync(0)) {
          getHighlightsBag(doc).setHighlights(bag);
      } else {
          release();
      }
      inSync = false;
      aTarget.repaint();
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        release();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    public void caretUpdate(CaretEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (!CndLexerUtilities.isCppIdentifierPart(e.getKeyChar())) {
            JTextComponent aTarget = target;
            if (aTarget != null) {
                aTarget.getToolkit().beep();
                e.consume();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
	if (   (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiers() == 0) 
            || (e.getKeyCode() == KeyEvent.VK_ENTER  && e.getModifiers() == 0)) {
	    release();
	    e.consume();
	}
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private synchronized void release() {
        if (target == null) {
            //already released
            return;
        }
        
        target.putClientProperty(InstantRenamePerformer.class, null);
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).removePostModificationDocumentListener(this);
        }
        target.removeKeyListener(this);
        target.removeFocusListener(this);
        getHighlightsBag(doc).clear();

        region = null;
        doc = null;
        target = null;
        instance = null;
    }
    
    private static AttributeSet createAttributeSet(Object... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0 : "There must be even number of prameters. " +
            "They are key-value pairs of attributes that will be inserted into the set."; //NOI18N

        List<Object> list = new ArrayList<>();
        
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
    
    private static InstantRenamePerformer instance = null;
    //private static final AttributeSet COLORING = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(138, 191, 236));
    private static final AttributeSet COLORING = AttributesUtilities.createImmutable(StyleConstants.Foreground, Color.red);
    
    private static AttributeSet getSyncedTextBlocksHighlight(String name) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = fcs != null ? fcs.getFontColors(name) : null;
        return as == null ? COLORING : as;
    }
    
    public static PositionsBag getHighlightsBag(Document doc) {
        PositionsBag bag = (PositionsBag) doc.getProperty(POSITION_BAG);
        if (bag == null) {
            doc.putProperty(POSITION_BAG, bag = new PositionsBag(doc));
        }
        return bag;
    }

    private static class CancelInstantRenameUndoableEdit extends AbstractUndoableEdit {

        private final Reference<InstantRenamePerformer> performer;

        public CancelInstantRenameUndoableEdit(InstantRenamePerformer performer) {
            this.performer = new WeakReference<>(performer);
        }

        @Override
        public boolean isSignificant() {
            return false;
        }

        @Override
        public void undo() throws CannotUndoException {
            InstantRenamePerformer perf = performer.get();

            if (perf != null) {
                perf.release();
            }
        }
    }
    
    public static class RenameDeletedTextInterceptor implements DeletedTextInterceptor {
        
        @Override
        public boolean beforeRemove(DeletedTextInterceptor.Context context) throws BadLocationException {
            Object getObject = context.getComponent().getClientProperty(InstantRenamePerformer.class);
            if (getObject instanceof InstantRenamePerformer) {
                InstantRenamePerformer instantRenamePerformer = (InstantRenamePerformer)getObject;
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
            @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
            @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
            @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
            @MimeRegistration(mimeType = MIMENames.DOXYGEN_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
            @MimeRegistration(mimeType = MIMENames.STRING_DOUBLE_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
            @MimeRegistration(mimeType = MIMENames.STRING_SINGLE_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
            @MimeRegistration(mimeType = MIMENames.PREPROC_MIME_TYPE, service = DeletedTextInterceptor.Factory.class)
        })
        public static class Factory implements DeletedTextInterceptor.Factory {

            @Override
            public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
                return new RenameDeletedTextInterceptor();
            }
        }
    }

    private static class RefImpl implements CsmReference {
        private final ChangeInfo.Change change;

        public RefImpl(ChangeInfo.Change change) {
            this.change = change;
        }

        @Override
        public CsmReferenceKind getKind() {
            return CsmReferenceKind.UNKNOWN;
        }

        @Override
        public CsmObject getReferencedObject() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public CsmObject getOwner() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int getStartOffset() {
            return change.getStart().getOffset();
        }

        @Override
        public int getEndOffset() {
            return change.getEnd().getOffset();
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
    }
}
