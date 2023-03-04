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

package org.netbeans.modules.nbform;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.form.CodeGenerator;
import org.netbeans.modules.form.EditorSupport;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormDesigner;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.PersistenceException;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.nbform.project.ClassSourceResolver;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.util.UserQuestionException;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Ian Formanek, Tomas Pavek
 */

public final class FormEditorSupport extends DataEditorSupport implements EditorSupport,
        EditorCookie.Observable, CloseCookie, PrintCookie {
    
    /** ID of the form designer (in the multiview) */
    static final String MV_FORM_ID = "form"; //NOI18N
    /** ID of the java editor (in the multiview) */
    private static final String MV_JAVA_ID = "java"; // NOI18N
    
    private static final int JAVA_ELEMENT_INDEX = 0;
    private static final int FORM_ELEMENT_INDEX = 2;
    private int elementToOpen; // default element index when multiview TC is created
    
    /** Icon for the form editor multiview window */
    static final String iconURL =
            "org/netbeans/modules/form/resources/form.gif"; // NOI18N
    
    /** The DataObject of the form */
    private FormDataObject formDataObject;
    
    /** The embracing multiview TopComponent (holds the form designer and
     * java editor) - we remember the last active TopComponent (not all clones) */
    private CloneableTopComponent multiviewTC;

    /**
     * Listener for node delegate's icon changes. It is responsible
     * for synchronization of node's and multiviewTC's icons.
     */
    private NodeListener nodeListener;

    private static PropertyChangeListener topcompsListener;

    private FoldHierarchyListener foldHierarchyListener;

    private UndoRedo.Manager editorUndoManager;
    
    private FormEditor formEditor;
    
    /** Set of opened FormEditorSupport instances (java or form opened) */
    private static final Set<FormEditorSupport> opened = Collections.synchronizedSet(new HashSet<FormEditorSupport>());
    
    private static Map<FileSystem,FileStatusListener> fsToStatusListener = new HashMap<FileSystem,FileStatusListener>();
    
    // --------------
    // constructor
    
    public FormEditorSupport(FormDataObject formDataObject) {
        super(formDataObject, formDataObject.getLookup(), new Environment(formDataObject));
        setMIMEType("text/x-java"); // NOI18N
        this.formDataObject = formDataObject;
        this.cookies = formDataObject.getCookies();
    }
    
    // ----------
    // opening & saving interface methods
    
    /** Main entry method. Called by OpenCookie implementation - opens the form.
     * 
     * @param forceFormElement determines whether we should force switch to form element.
     * @see OpenCookie#open
     */
    public void openFormEditor(boolean forceFormElement) {
        boolean alreadyOpened = opened.contains(this);
        boolean switchToForm = forceFormElement || !alreadyOpened;
        if (switchToForm) {
            elementToOpen = FORM_ELEMENT_INDEX;
        }
        long ms = System.currentTimeMillis();
        try {
            showOpeningStatus("FMT_PreparingForm"); // NOI18N

            multiviewTC = openCloneableTopComponent();
            multiviewTC.requestActive();

            registerNodeListener();

            if (switchToForm) {
                MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
                handler.requestActive(handler.getPerspectives()[FORM_ELEMENT_INDEX]);
            }
        } finally {
            hideOpeningStatus();
        }
        Logger.getLogger(FormEditor.class.getName()).log(Level.FINER, "Opening form time 1: {0}ms", (System.currentTimeMillis()-ms)); // NOI18N
    }

    private void registerNodeListener() {
        if (formDataObject.isValid()) {
            Node node = formDataObject.getNodeDelegate();
            multiviewTC.setIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
            if (nodeListener == null) {
                NodeListener listener = new NodeAdapter() {
                    @Override
                    public void propertyChange(final PropertyChangeEvent ev) {
                        Mutex.EVENT.writeAccess(new Runnable() {
                            @Override
                            public void run() {
                                if (Node.PROP_ICON.equals(ev.getPropertyName())) {
                                    if (formDataObject.isValid() && (multiviewTC != null)) {
                                        multiviewTC.setIcon(formDataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
                                    }
                                }
                            }
                        });
                    }
                };
                node.addNodeListener(org.openide.nodes.NodeOp.weakNodeListener(listener, node));
                nodeListener = listener;
            }
        }
    }

    void showOpeningStatus(String fmtMessage) {
        JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();

        // set wait cursor
        mainWin.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mainWin.getGlassPane().setVisible(true);

        // set status text like "Opening form: ..."
        StatusDisplayer.getDefault().setStatusText(
            FormUtils.getFormattedBundleString(
                fmtMessage, // NOI18N
                new Object[] { formDataObject.getFormFile().getName() }));
        javax.swing.RepaintManager.currentManager(mainWin).paintDirtyRegions();
    }

    void hideOpeningStatus() {
        // clear wait cursor
        JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();
        mainWin.getGlassPane().setVisible(false);
        mainWin.getGlassPane().setCursor(null);

        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
    }

    private void addStatusListener(FileSystem fs) {
        FileStatusListener fsl = fsToStatusListener.get(fs);
        if (fsl == null) {
            fsl = new FileStatusListener() {
                @Override
                public void annotationChanged(FileStatusEvent ev) {
                    synchronized (opened) {
                        Iterator<FormEditorSupport> iter = opened.iterator();
                        while (iter.hasNext()) {
                            FormEditorSupport fes = iter.next();
                            if (ev.hasChanged(fes.getFormDataObject().getPrimaryFile())
                                    || ev.hasChanged(fes.getFormDataObject().getFormFile())) {
                                fes.updateMVTCDisplayName();
                            }
                        }
                    }
                }
            };
            fs.addFileStatusListener(fsl);
            fsToStatusListener.put(fs, fsl);
        } // else do nothing - the listener is already added
    }
    
    private static void detachStatusListeners() {
        Iterator iter = fsToStatusListener.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            FileSystem fs = (FileSystem)entry.getKey();
            FileStatusListener fsl = (FileStatusListener)entry.getValue();
            fs.removeFileStatusListener(fsl);
        }
        fsToStatusListener.clear();
    }
    
    void selectJavaEditor(){
        MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
        if (handler != null) {
            handler.requestActive(handler.getPerspectives()[JAVA_ELEMENT_INDEX]);
        }
    }

    @Override
    protected boolean asynchronousOpen() {
        return false;
    }

    @Override
    public void openSource() {
        open();
    }

    @Override
    public void openDesign() {
        if (EventQueue.isDispatchThread()) {
            openFormEditor(true);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    openFormEditor(true);
                }
            });
        }
    }

    /** Overriden from JavaEditor - opens editor and ensures it is selected
     * in the multiview.
     */
    @Override
    public void open() {
        if (EventQueue.isDispatchThread()) {
            openInAWT();
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    openInAWT();
                }
            });
        }
    }
    
    private void openInAWT() {
        if (!formDataObject.isValid()) {
            return;
        }
        if (Boolean.TRUE.equals(formDataObject.getPrimaryFile().getAttribute("nonEditableTemplate"))) { // NOI18N
            String pattern = FormUtils.getBundleString("MSG_NonEditableTemplate"); // NOI18N
            String message = MessageFormat.format(pattern, new Object[] {formDataObject.getNodeDelegate().getName()});
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
            return;
        }
        elementToOpen = JAVA_ELEMENT_INDEX;
        super.open();
        
        // This method must be executed in AWT thread because
        // otherwise multiview is opened in AWT using invokeLater
        // and we don't have multiviewTC correctly set
        MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
        if (handler != null) {
            handler.requestActive(handler.getPerspectives()[JAVA_ELEMENT_INDEX]);
            // will continue in loadOpeningForm
        }
    }
    
    /** Overriden from JavaEditor - opens editor at given position and ensures
     * it is selected in the multiview.
     * 
     * @param pos position
     */
    public void openAt(PositionRef pos) {
        elementToOpen = JAVA_ELEMENT_INDEX;
        openCloneableTopComponent();
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(multiviewTC);
        handler.requestActive(handler.getPerspectives()[JAVA_ELEMENT_INDEX]);
        
        openAt(pos, -1).getComponent().requestActive();
    }
    
    @Override
    public void openAt(Position pos) {
        openAt(createPositionRef(pos.getOffset(), Position.Bias.Forward));
    }

    boolean startFormLoading() {
        if (!formEditor.prepareLoading()) {
            // report errors and switch to Source tab - later because now in the
            // middle of switching to Design (error dialog would run event queue
            // and process waiting tasks too soon, e.g. closing - bug 223487)
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (formEditor != null) { // not closed yet
                        reportErrors();
                        selectJavaEditor();
                    }
                }
            });
            return false;
        }
        return true;
    }

    /**
     * Callback from multiview (FormDesignerTC), requiring to load form whose
     * designer is just being opened.
     * @return true if successfully loaded
     */
    boolean loadOpeningForm() {
        showOpeningStatus("FMT_OpeningForm"); // NOI18N

        postCreationUpdate1();

        boolean success = formEditor.loadFormData();

        postCreationUpdate2();

        hideOpeningStatus();

        reportErrors(); // report errors during loading, fatal or non-fatal

        if (!success) { // loading failed - don't keep empty designer opened
            selectJavaEditor();
        }

        return success;
    }

    /** Public method for loading form data from file. Does not open the
     * source editor and designer, does not report errors and does not throw
     * any exceptions. Runs in AWT event dispatch thread, returns after the
     * form is loaded (even if not called from AWT thread).
     * @return whether the form is loaded (true also if it already was)
     */
    public boolean loadForm() {
        return getFormEditor(true).loadForm();
    }
    
    /** @return true if the form is opened, false otherwise */
    public boolean isOpened() {
        return (formEditor != null) && formEditor.isFormLoaded();
    }
    
    private boolean saving; // workaround for bug 75225
    
    /** Save the document in this thread and start reparsing it.
     * @exception IOException on I/O error
     */
    @Override
    public void saveDocument() throws IOException {
        IOException ioEx = null;
        try {
            if (formEditor != null) {
                formEditor.saveFormData();
            }
            saving = true; // workaround for bug 75225
            super.saveDocument();
        }
        catch (PersistenceException ex) {
            Throwable t = ex.getOriginalException();
            if (t instanceof IOException)
                ioEx = (IOException) t;
            else {
                ioEx = new IOException("Cannot save the form"); // NOI18N
                ErrorManager.getDefault().annotate(ioEx, t != null ? t : ex);
            }
        }
        finally {
            saving = false; // workaround for bug 75225
        }
        if (formEditor != null) {
            formEditor.reportSavingErrors(); // TODO can't just throw IOException?
        }
        
        if (ioEx != null)
            throw ioEx;
    }

    public void saveSourceOnly() throws IOException {
        try {
            saving = true; // workaround for bug 75225
            super.saveDocument();
        } finally {
            saving = false; // workaround for bug 75225
        }
    }

    /**
     * Reports errors occurred during loading or saving the form.
     */
    private void reportErrors() {
        if (!formEditor.isFormLoaded()) {
            formEditor.reportLoadingErrors(null); // fatal error, no options
        } else {
            // The form was loaded with some non-fatal errors - some data
            // was not loaded - show a warning about possible data loss.
            // The dialog is shown later to let the designer opening complete.
            JButton viewButton = new JButton();
            Mnemonics.setLocalizedText(viewButton, FormUtils.getBundleString("CTL_ViewOnly")); // NOI18N
            JButton editButton = new JButton();
            Mnemonics.setLocalizedText(editButton, FormUtils.getBundleString("CTL_AllowEditing")); // NOI18N
            final Object[] options = new Object[] { viewButton, editButton,
                                                    NotifyDescriptor.CANCEL_OPTION };
            final DialogDescriptor dd = formEditor.reportLoadingErrors(options);
            if (dd != null) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (formEditor != null && !formEditor.isFormLoaded()) {
                            return; // quite unlikely, but the form could be closed meanwhile (#164444)
                        }
                        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                        // hack: adjust focus so it is not on the Show Exceptions button
                        if (dialog instanceof JDialog) {
                            ((JDialog)dialog).getContentPane().requestFocus();
                        }
                        dialog.setVisible(true);
                        dialog.dispose();

                        Object ret = dd.getValue();
                        if (ret == options[0]) { // View Only
                            formEditor.setFormReadOnly();
                            updateMVTCDisplayName();
                        } else if (ret == options[1]) { // Allow Editing
                            formEditor.destroyInvalidComponents();
                        } else { // close form, switch to source editor
                            closeFormEditor();
                        }
                    }
                });
            }
        }
    }

    // ------------
    // other interface methods
    
    /** @return data object representing the form */
    public final FormDataObject getFormDataObject() {
        return formDataObject;
    }
    
    public FormModel getFormModel() {
        FormEditor fe = getFormEditor();
        return (fe == null) ? null : fe.getFormModel();
    }
    
    public FormEditor getFormEditor() {
        return getFormEditor(false);
    }
    
    FormEditor getFormEditor(boolean initialize) {
        if ((formEditor == null) && initialize) {
            formEditor = new FormEditor(formDataObject, this);
        }
        return formEditor;
    }

    private FormDesignerTC getFormDesignerTC() {
        if (multiviewTC == null) {
            return null;
        }
        return (FormDesignerTC) multiviewTC.getClientProperty("formDesigner"); // NOI18N
    }

    /** Marks the form as modified if it's not yet. Used if changes made
     * in form data don't affect the java source file (generated code). */
    @Override
    public void markModified() {
        if (formEditor != null && formEditor.isFormLoaded() && !formDataObject.isModified()) {
            notifyModified();
        }
    }
    
    @Override
    protected UndoRedo.Manager createUndoRedoManager() {
        editorUndoManager = super.createUndoRedoManager();
        return editorUndoManager;
    }

    @Override
    public void discardEditorUndoableEdits() {
        if (editorUndoManager != null)
            editorUndoManager.discardAllEdits();
    }
    
    // -----------
    // closing/reloading
    
    @Override
    public void reloadForm() {
        if (canClose())
            reloadDocument();
    }
    
    @Override
    protected org.openide.util.Task reloadDocument() {
        if (multiviewTC == null)
            return super.reloadDocument();
        
        org.openide.util.Task docLoadTask = super.reloadDocument();
        
        if (saving) // workaround for bug 75225
            return docLoadTask;
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FormDesignerTC designerTC = getFormDesignerTC();
                if (designerTC != null) {
                    formEditor.closeForm();
                    designerTC.resetDesigner(true); // will trigger loading
                }
            }
        });
        
        return docLoadTask;
    }

    /**
     * Reload the form if opened. It always loads the form, no matter whether on
     * Source or Design tabs. That's different from standard reload task that
     * won't load the form if currently not in Design.
     */
    public FormEditor reloadFormEditor() {
        if (!isOpened()) {
            return null;
        }
        FormDesignerTC designerTC = getFormDesignerTC();
        formEditor.closeForm();
        boolean success = formEditor.loadForm();
        if (designerTC != null) {
            if (success) {
                designerTC.resetDesigner(true);
            } else {
                closeFormEditor();
            }
        }
        reportErrors();
        return formEditor;
    }

    /**
     * Closes the form editor without closing the document in editor. The editor
     * is switched to Source tab, later clicking on Design tab will load the
     * form again.
     */
    public void closeFormEditor() {
        if (isOpened()) {
            final FormDesignerTC designerTC = getFormDesignerTC();
            formEditor.closeForm();
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    if (designerTC != null) {
                        designerTC.resetDesigner(false);
                    }
                    selectJavaEditor();
                }
            };
            if (EventQueue.isDispatchThread()) {
                run.run();
            } else {
                try             {
                    java.awt.EventQueue.invokeAndWait(run);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }

    @Override
    protected void notifyClosed() {
        opened.remove(this);
        if (opened.isEmpty()) {
            detachTopComponentsListener();
            detachStatusListeners();
        }
        
        super.notifyClosed(); // close java editor
        if (formEditor != null) {
            formEditor.closeForm();
            formEditor = null;
        }
        nodeListener = null;
        multiviewTC = null;
        guardedProvider = null;
        guardedEditor = null;
        elementToOpen = JAVA_ELEMENT_INDEX;
    }
    
    private void multiViewClosed(CloneableTopComponent mvtc) {
        Enumeration en = mvtc.getReference().getComponents();
        boolean isLast = !en.hasMoreElements();
        if (multiviewTC == mvtc) {
            multiviewTC = null;
            FormDesignerTC designerTC = null;
            // Find another multiviewTC, possibly with loaded formDesigner
            while (en.hasMoreElements()) {
                multiviewTC = (CloneableTopComponent)en.nextElement();
                designerTC = getFormDesignerTC();
                if (designerTC != null) {
                    break;
                }
            }
            if (!isLast && (designerTC == null)) {
                // Only Java elements are opened in the remaining clones
                if (formEditor != null) {
                    formEditor.closeForm();
                    formEditor = null;
                }
            }
        }
        
        if (isLast) // last view of this form closed
            notifyClosed();
    }
    
    @Override
    protected boolean notifyModified () {
        boolean alreadyModified = isModified();
        boolean retVal = super.notifyModified();
        
        if (retVal) { // java source modification
            addSaveCookie();
        }
        
        if (!alreadyModified) {
            FileObject formFile = formDataObject.getFormFile();
            if (!formFile.canWrite()) { // Issue 74092
                FileLock lock = null;
                try {
                    lock = formFile.lock();
                } catch (UserQuestionException uqex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                            uqex.getLocalizedMessage(),
                            FormUtils.getBundleString("TITLE_UserQuestion"), // NOI18N
                            NotifyDescriptor.YES_NO_OPTION);
                    DialogDisplayer.getDefault().notify(nd);
                    if (NotifyDescriptor.YES_OPTION.equals(nd.getValue())) {
                        try {
                            uqex.confirmed();
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run()  {
                                    reloadForm();
                                }
                            });
                        } catch (IOException ioex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioex);
                        }
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
            updateMVTCDisplayName();
        }
        return retVal;
    }
    
    @Override
    protected void notifyUnmodified () {
        super.notifyUnmodified();
         // java source modification
        removeSaveCookie();
        updateMVTCDisplayName();
    }
    
    private static void attachTopComponentsListener() {
        if (topcompsListener != null)
            return;
        
        topcompsListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                if (TopComponent.Registry.PROP_OPENED.equals(
                                                ev.getPropertyName()))
                {   // set of opened TopComponents has changed - hasn't some
                    // of our views been closed?
                    Set oldSet = (Set) ev.getOldValue();
                    Set newSet = (Set) ev.getNewValue();
                    if (newSet.size() < oldSet.size()) {
                        Iterator it = oldSet.iterator();
                        while (it.hasNext()) {
                            Object o = it.next();
                            if (!newSet.contains(o)) {
                                if (o instanceof CloneableTopComponent) {
                                    CloneableTopComponent closedTC = (CloneableTopComponent) o;
                                    if (getSelectedElementType(closedTC) != -1) { // it is our multiview
                                        FormEditorSupport fes = getFormEditor(closedTC);
                                        if (fes != null) {
                                            fes.multiViewClosed(closedTC);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    TopComponent active = TopComponent.getRegistry().getActivated();
                    if (active!=null && getSelectedElementType(active) != -1) { // it is our multiview
                        FormEditorSupport fes = getFormEditor(active);
                        if (fes != null) {
                            fes.updateMVTCDisplayName();
                        }
                    }
                }
            }
        };
        
        TopComponent.getRegistry().addPropertyChangeListener(topcompsListener);
    }
    
    private static void detachTopComponentsListener() {
        if (topcompsListener != null) {
            TopComponent.getRegistry()
                    .removePropertyChangeListener(topcompsListener);
            topcompsListener = null;
            
            TopComponentGroup group = WindowManager.getDefault()
                    .findTopComponentGroup("form"); // NOI18N
            if (group != null)
                group.close();
        }
    }
    
    /**
     * Additional updates for a newly created form before it gets loaded for the first time.
     */
    private void postCreationUpdate1() {
        FileObject fob = formDataObject.getPrimaryFile();
        Object libName = fob.getAttribute("requiredLibrary"); // NOI18N
        if (libName != null) {
            Object className = fob.getAttribute("requiredClass"); // NOI18N
            if ((className == null) || !ClassPathUtils.isOnClassPath(fob, className.toString())) {
                try {
                    Library lib = LibraryManager.getDefault().getLibrary((String)libName);
                    ClassPathUtils.updateProject(fob, new ClassSource(
                            (className == null) ? null : className.toString(),
                            new ClassSourceResolver.LibraryEntry(lib))
                    );
                } catch (IOException ioex) {
                    Logger.getLogger(FormEditorSupport.class.getName()).log(Level.INFO, ioex.getLocalizedMessage(), ioex);
                }
            }
        }
    }

    /**
     * Additional updates for a newly created form, just loaded for the first time.
     */
    private void postCreationUpdate2() {
        if (formEditor.postCreationUpdate()) {
            try {
                checkSuppressWarningsAnnotation();
                if (isModified()) {
                    saveDocument();
                }
            } catch (IOException ex) { // unlikely for just created form
                Logger.getLogger(FormEditorSupport.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
    }

    private void checkSuppressWarningsAnnotation() throws IOException {
        FileObject fo = getFormDataObject().getPrimaryFile();
        String sourceLevel = SourceLevelQuery.getSourceLevel(fo);
        boolean invalidSL = (sourceLevel != null) && ("1.5".compareTo(sourceLevel) > 0); // NOI18N
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.BOOT);
        if (invalidSL || cp.findResource("java/lang/SuppressWarnings.class") == null) { // NOI18N
            // The project's bootclasspath doesn't contain SuppressWarnings class.
            // So, remove this annotation from initComponents() method.
            final String foName = fo.getName();
            JavaSource js = JavaSource.forFileObject(fo);
            final int[] positions = new int[] {-1,-1};
            js.runModificationTask(new CancellableTask<WorkingCopy>() {
                @Override
                public void cancel() {
                }
                @Override
                public void run(WorkingCopy wcopy) throws Exception {
                    wcopy.toPhase(JavaSource.Phase.RESOLVED);

                    ClassTree clazz = null;
                    CompilationUnitTree cu = wcopy.getCompilationUnit();
                    for (Tree tree : cu.getTypeDecls()) {
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                            ClassTree cand = (ClassTree)tree;
                            if (foName.equals(cand.getSimpleName().toString())) {
                                clazz = cand;
                            }
                        }
                    }
                    if (clazz == null) return;

                    for (Tree tree : clazz.getMembers()) {
                        if (tree.getKind() == Tree.Kind.METHOD) {
                            MethodTree method = (MethodTree)tree;
                            if ("initComponents".equals(method.getName().toString()) // NOI18N
                                    && (method.getParameters().isEmpty())) {
                                ModifiersTree modifiers = method.getModifiers();
                                for (AnnotationTree annotation : modifiers.getAnnotations()) {
                                    if (annotation.getAnnotationType().toString().contains("SuppressWarnings")) { // NOI18N
                                        SourcePositions sp = wcopy.getTrees().getSourcePositions();
                                        positions[0] = (int)sp.getStartPosition(cu, annotation);
                                        positions[1] = (int)sp.getEndPosition(cu, annotation);
                                        // We cannot use the following code because
                                        // part of the modifier is in guarded block
                                        //ModifiersTree newModifiers = wcopy.getTreeMaker().removeModifiersAnnotation(method.getModifiers(), annotation);
                                        //wcopy.rewrite(modifiers, newModifiers);
                                    }
                                }
                            }
                        }
                    }

                }
            }).commit();
            if (positions[0] != -1) {
                try {
                    getFormDataObject().getFormEditorSupport().getDocument().remove(positions[0], positions[1]-positions[0]);
                } catch (BadLocationException blex) {
                    Logger.getLogger(FormEditor.class.getName()).log(Level.INFO, blex.getLocalizedMessage(), blex);
                }
            }
        }
    }

    // -------
    // multiview & java editor
    
    @Override
    protected CloneableEditorSupport.Pane createPane() {
        if (!formDataObject.isValid()) {
            return super.createPane(); // Issue 110249
        } 
        
        CloneableTopComponent mvtc = MultiViews.createCloneableMultiView("text/x-form", getDataObject());
        
        // #45665 - dock into editor mode if possible..
        Mode editorMode = WindowManager.getDefault().findMode(CloneableEditorSupport.EDITOR_MODE);
        if (editorMode != null) {
            editorMode.dockInto(mvtc);
        }
        return (CloneableEditorSupport.Pane)mvtc;
    }

    private static boolean readOnly(FormDataObject formDataObject) {
        if (!formDataObject.getPrimaryFile().canWrite()) {
            return true;
        }
        TopComponent active = TopComponent.getRegistry().getActivated();
        if (active != null && getSelectedElementType(active) == FORM_ELEMENT_INDEX) {
            FormEditorSupport fes = (FormEditorSupport)formDataObject.getFormEditorSupport();
            if (fes != null) {
                FormModel fm = fes.getFormModel();
                if (fm != null) {
                    return fm.isReadOnly();
                }
            }
        }
        return false;
    }
    
    private static String getMVTCToolTipText(FormDataObject formDataObject) {
        return DataEditorSupport.toolTip(formDataObject.getPrimaryFile(), formDataObject.isModified(), readOnly(formDataObject));
    }
    
    /**
     * Returns display name of the multiview top component.
     * The first item of the array is normal display name,
     * the second item of the array is HTML display name.
     *
     * @param formDataObject form data object representing the multiview tc.
     * @return display names of the MVTC. The second item can be <code>null</code>.
     */
    private static String[] getMVTCDisplayName(FormDataObject formDataObject) {
        Node node = formDataObject.getNodeDelegate();
        String title = node.getDisplayName();
        String htmlTitle = node.getHtmlDisplayName();
        if (htmlTitle == null) {
            try {
                htmlTitle = XMLUtil.toElementContent(title);
            } catch (CharConversionException x) {
                htmlTitle = "???";
            }
        }
        FormEditorSupport fes = (FormEditorSupport)formDataObject.getFormEditorSupport();
        if (fes != null) {
            FormDesignerTC designerTC = fes.getFormDesignerTC();
            if (designerTC != null && designerTC.isShowing()) {
                FormModel fm = fes.getFormModel();
                if (fm != null) {
                    FormDesigner fd = FormEditor.getFormDesigner(fes.getFormModel());
                    if (fd != null && fd.getFormModel() != null
                            && !fd.isTopRADComponent() && fd.getTopDesignComponent() != null) {
                        title = FormUtils.getFormattedBundleString(
                                "FMT_FormTitleWithContainerName", // NOI18N
                                new Object[] {title, fd.getTopDesignComponent().getName()});
                        htmlTitle = FormUtils.getFormattedBundleString(
                                "FMT_FormTitleWithContainerName", // NOI18N
                                new Object[] {htmlTitle, fd.getTopDesignComponent().getName()});
                    }
                }
            }
        }
        boolean modified = formDataObject.isModified();
        boolean readOnly = readOnly(formDataObject);
        return new String[] {
            DataEditorSupport.annotateName(title, false, modified, readOnly),
            DataEditorSupport.annotateName(htmlTitle, true, modified, readOnly)
        };
    }

    @Override
    protected String messageName() {
        String[] titles = getMVTCDisplayName(formDataObject);
        return titles[0];
    }

    @Override
    protected String messageHtmlName() {
        String[] titles = getMVTCDisplayName(formDataObject);
        return titles[1];
    }
    
    /** Updates title (display name) of all multiviews for given form. Replans
     * to event queue thread if necessary. */
    void updateMVTCDisplayName() {
        if (java.awt.EventQueue.isDispatchThread()) {
            updateMVTCDisplayNameInAWT();
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateMVTCDisplayNameInAWT();
                }
            });
        }
    }
    
    private void updateMVTCDisplayNameInAWT() {
        if ((multiviewTC == null) || (!formDataObject.isValid())) // Issue 67544
            return;
        
        String[] titles = getMVTCDisplayName(formDataObject);
        Enumeration en = multiviewTC.getReference().getComponents();
        while (en.hasMoreElements()) {
            TopComponent tc = (TopComponent) en.nextElement();
            tc.setDisplayName(titles[0]);
            tc.setHtmlDisplayName(titles[1]);
        }
    }
    
    /** Updates tooltip of all multiviews for given form. Replans to even queue
     * thread if necessary. */
    void updateMVTCToolTipText() {
        if (java.awt.EventQueue.isDispatchThread()) {
            if (multiviewTC == null)
                return;
            
            String tooltip = getMVTCToolTipText(formDataObject);
            Enumeration en = multiviewTC.getReference().getComponents();
            while (en.hasMoreElements()) {
                TopComponent tc = (TopComponent) en.nextElement();
                tc.setToolTipText(tooltip);
            }
        }
        else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (multiviewTC == null)
                        return;
                    
                    String tooltip = getMVTCToolTipText(formDataObject);
                    Enumeration en = multiviewTC.getReference().getComponents();
                    while (en.hasMoreElements()) {
                        TopComponent tc = (TopComponent) en.nextElement();
                        tc.setToolTipText(tooltip);
                    }
                }
            });
        }
    }

    @Messages({
        "MSG_MODIFIED=File {0} is modified. Save?"
    })
    final CloseOperationState canCloseElement(TopComponent tc) {
        // if this is not the last cloned java editor component, closing is OK
        if (!FormEditorSupport.isLastView(tc)) {
            return CloseOperationState.STATE_OK;
        }

        if (!isModified()) {
            return CloseOperationState.STATE_OK;
        }
        
        AbstractAction save = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveDocument();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_MODIFIED(
            getDataObject().getPrimaryFile().getNameExt()
        ));

        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                "ID_FORM_CLOSING", // NOI18N
                save,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }
    
    static boolean isLastView(TopComponent tc) {
        if (!(tc instanceof CloneableTopComponent))
            return false;
        
        boolean oneOrLess = true;
        Enumeration en = ((CloneableTopComponent)tc).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements())
                oneOrLess = false;
        }
        return oneOrLess;
    }
    
    /** This is called by the multiview elements whenever they are created
     * (and given a observer knowing their multiview TopComponent). It is
     * important during deserialization and cloning the multiview - i.e. during
     * the operations we have no control over. But anytime a multiview is
     * created, this method gets called.
     */
    void setTopComponent(TopComponent topComp) {
        multiviewTC = (CloneableTopComponent)topComp;
        String[] titles = getMVTCDisplayName(formDataObject);
        multiviewTC.setDisplayName(titles[0]);
        multiviewTC.setHtmlDisplayName(titles[1]);
        multiviewTC.setToolTipText(getMVTCToolTipText(formDataObject));
        opened.add(this);
        registerNodeListener();
        attachTopComponentsListener();
        try {
            addStatusListener(formDataObject.getPrimaryFile().getFileSystem());
        } catch (FileStateInvalidException fsiex) {
            fsiex.printStackTrace();
        }
    }
    
    public static FormEditorSupport getFormEditor(TopComponent tc) {
        Object dobj = tc.getLookup().lookup(DataObject.class);
        FormEditorSupport fes = null;
        if (dobj instanceof FormDataObject) {
            FormDataObject formDataObject = (FormDataObject)dobj;
            fes = (FormEditorSupport)formDataObject.getFormEditorSupport();
        }
        return fes;
    }

    @Override
    public boolean isJavaEditorDisplayed() {
        boolean showing = false;
        if (EventQueue.isDispatchThread()) { // issue 91715
            JEditorPane[] jeditPane = getOpenedPanes();
            if (jeditPane != null) {
                for (int i=0; i<jeditPane.length; i++) {
                    if (showing = jeditPane[i].isShowing()) {
                        break;
                    }
                }
            }
        }
        return showing;
    }

    /**
     * Called before regenerating initComponents guarded section to obtain the
     * actual state of the editor fold for the generated code. Needed for the case
     * the user expanded it manually to expand it again after recreating the fold
     * for the new content.
     * @param offset the start offset of the initComponents section
     * @return true if the fold is collapsed, false if expanded
     */
    @Override
    public Boolean getFoldState(int offset) {
        if (EventQueue.isDispatchThread()) {
            JEditorPane[] panes = getOpenedPanes();
            if (panes != null && panes.length > 0) {
                FoldHierarchy hierarchy = FoldHierarchy.get(panes[0]);
                if (hierarchy != null) {
                    try {
                        hierarchy.lock();
                        Fold fold = FoldUtilities.findNearestFold(hierarchy, offset);
                        if (fold != null) {
                            return fold.isCollapsed();
                        }
                    } finally {
                        hierarchy.unlock();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Called after setting new content to the initComponents section to restore
     * the remembered state of the fold. Setting the text creates a new fold that
     * is initially collapsed, we may want to expand it (if the user expanded it
     * manually in the editor before).
     * @param collapse
     * @param startOffset
     * @param endOffset 
     */
    @Override
    public void restoreFoldState(boolean collapse, int startOffset, int endOffset) {
        if (collapse) {
            return; // the fold will be initially collapsed
        }
        JEditorPane[] panes = getOpenedPanes();
        if (panes != null && panes.length > 0) {
            FoldHierarchy hierarchy = FoldHierarchy.get(panes[0]);
            if (hierarchy != null) {
                try {
                    hierarchy.lock();
                    Fold fold = FoldUtilities.findCollapsedFold(hierarchy, startOffset, endOffset);
                    if (fold != null) {
                        hierarchy.expand(fold);
                    } else {
                        // in fact we don't really know when the new fold will appear
                        // in the hierarchy, it happens somehow asynchronously
                        if (foldHierarchyListener == null) {
                            foldHierarchyListener = new FoldHierarchyListener();
                        } else {
                            hierarchy.removeFoldHierarchyListener(foldHierarchyListener);
                        }
                        hierarchy.addFoldHierarchyListener(foldHierarchyListener);
                        foldHierarchyListener.setOffsets(startOffset, endOffset);
                    }
                } finally {
                    hierarchy.unlock();
                }
            }
        }
    }

    private static class FoldHierarchyListener implements org.netbeans.api.editor.fold.FoldHierarchyListener {
        private int startOffset = -1;
        private int endOffset;

        private void setOffsets(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        @Override
        public void foldHierarchyChanged(FoldHierarchyEvent evt) {
            if (startOffset >= 0
                && evt.getAddedFoldCount() > 0
                && evt.getAffectedStartOffset() < endOffset && evt.getAffectedEndOffset() > startOffset
                && evt.getSource() instanceof FoldHierarchy) {
                // here we should have the fold for the new initComponents code added
                FoldHierarchy hierarchy = (FoldHierarchy) evt.getSource();
                Fold fold = FoldUtilities.findCollapsedFold(hierarchy, startOffset, endOffset);
                if (fold != null) {
                    startOffset = -1; // ignore any futher events
                    hierarchy.expand(fold);
                }
            }
        }
    }

    private static Boolean groupVisible = null;
    
    static void checkFormGroupVisibility() {
        // when active TopComponent changes, check if we should open or close
        // the form editor group of windows (Inspector, Palette, Properties)
        WindowManager wm = WindowManager.getDefault();
        final TopComponentGroup group = wm.findTopComponentGroup("form"); // NOI18N
        if (group == null)
            return; // group not found (should not happen)
        
        boolean designerSelected = false;
        Iterator it = wm.getModes().iterator();
        while (it.hasNext()) {
            Mode mode = (Mode) it.next();
            TopComponent selected = mode.getSelectedTopComponent();
            if (getSelectedElementType(selected) == FORM_ELEMENT_INDEX) {
                designerSelected = true;
                break;
            }
        }

        if (designerSelected && !Boolean.TRUE.equals(groupVisible)) {
            // Bug 116008: calling group.open() first time may cause hiding the
            // FormDesigner (some winsys multiview initialization mess), calling
            // this method again and hiding the group. By setting the groupVisible
            // to false we make the re-entrant call effectively do nothing.
            groupVisible = Boolean.FALSE;
            group.open();
            groupVisible = Boolean.TRUE;
            final TopComponentGroup paletteGroup = wm.findTopComponentGroup( "commonpalette" ); // NOI18N
            if( null != paletteGroup ) {
                paletteGroup.open();
            }
        }
        else if (!designerSelected && !Boolean.FALSE.equals(groupVisible)) {
            group.close();
            groupVisible = Boolean.FALSE;
        }
    }
    
    /** @return 0 if java editor in form editor multiview is selected
     *          1 if form designer in form editor multiview is selected
     *         -1 if the given TopComponent is not form editor multiview
     */
    static int getSelectedElementType(TopComponent tc) {
        if (tc != null) {
            MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
            if (handler != null) {
                String prefId = handler.getSelectedPerspective().preferredID();
                if (MV_JAVA_ID.equals(prefId))
                    return JAVA_ELEMENT_INDEX; // 0
                if (MV_FORM_ID.equals(prefId))
                    return FORM_ELEMENT_INDEX; // 1
            }
        }
        return -1;
    }

    @Override
    public Object getJavaContext() {
        return null; // nothing else than FileObject is needed in NB
    }

    @Override
    public int getCodeIndentSize() {
        CodeStyle cs = CodeStyle.getDefault(getFormDataObject().getPrimaryFile());
        return cs != null ? cs.getIndentSize() : 4;
    }

    @Override
    public boolean getCodeBraceOnNewLine() {
        CodeStyle cs = CodeStyle.getDefault(getFormDataObject().getPrimaryFile());
        return cs != null ? cs.getMethodDeclBracePlacement() != CodeStyle.BracePlacement.SAME_LINE : false;
    }

    public SimpleSection getVariablesSection() {
        return getGuardedSectionManager().findSimpleSection(SECTION_VARIABLES);
    }
    
    public SimpleSection getInitComponentSection() {
        return getGuardedSectionManager().findSimpleSection(SECTION_INIT_COMPONENTS);
    }

    @Override
    public GuardedSectionManager getGuardedSectionManager() {
        try {
            StyledDocument doc = null;
            try {
                doc = openDocument();
            } catch (UserQuestionException uqex) { // Issue 143655
                Object retVal = DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Confirmation(uqex.getLocalizedMessage(),
                            NotifyDescriptor.YES_NO_OPTION));
                if (NotifyDescriptor.YES_OPTION == retVal) {
                    uqex.confirmed();
                    doc = openDocument();
                }
            }
            if (doc == null) {
                // Issue 143655 - opening of big file canceled
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        close();
                    }
                });
                return null;
            } else {
                return GuardedSectionManager.getInstance(doc);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("cannot open document", ex); // NOI18N
        }
    }

    @Override
    public boolean canGenerateNBMnemonicsCode() {
        FileObject srcFile = getFormDataObject().getPrimaryFile();
        return isNBMProject(srcFile)
            || ClassPathUtils.checkUserClass("org.openide.awt.Mnemonics", srcFile); // NOI18N
    }

    private static boolean isNBMProject(FileObject srcFile) {
        // hack: checking project impl. class name, is there a better way?
        Project p = FileOwnerQuery.getOwner(srcFile);
        return p != null && p.getClass().getName().startsWith("org.netbeans.modules.apisupport.") // NOI18N
               && p.getClass().getName().endsWith("Project"); // NOI18N
    }

    private static final class FormGEditor implements GuardedEditorSupport {
        
        StyledDocument doc = null;
        
        @Override
        public StyledDocument getDocument() {
            return doc;
        }
    }
    
    private FormGEditor guardedEditor;
    private GuardedSectionsProvider guardedProvider;
    
    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        if (guardedEditor == null) {
            guardedEditor = new FormGEditor();
            GuardedSectionsFactory gFactory = GuardedSectionsFactory.find(((DataEditorSupport.Env) env).getMimeType());
            if (gFactory != null) {
                guardedProvider = gFactory.create(guardedEditor);
            }
        }
        
        if (guardedProvider != null) {
            guardedEditor.doc = doc;
            Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            Reader reader = guardedProvider.createGuardedReader(stream, c);
            try {
                kit.read(reader, doc, 0);
            } finally {
                reader.close();
            }
        } else {
            super.loadFromStreamToKit(doc, stream, kit);
        }
    }

    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        if (guardedProvider != null) {
            Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            Writer writer = guardedProvider.createGuardedWriter(stream, c);
            try {
                kit.write(writer, doc, 0, doc.getLength());
            } finally {
                writer.close();
            }
        } else {
            super.saveFromKitToStream(doc, kit, stream);
        }
    }
    
    @MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",
        iconBase=iconURL,
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID=MV_JAVA_ID,
        mimeType="text/x-form",
        position=1000
    )
    public static class JavaMultiViewEditorElement extends MultiViewEditorElement {
        private static final long serialVersionUID =-3126744316624172415L;
        
        private DataObject dataObject;
        private transient FormEditorSupport javaEditor;
        private transient MultiViewElementCallback multiViewObserver;

        public JavaMultiViewEditorElement(Lookup context) {
            super(context);
            dataObject = context.lookup(DataObject.class);
            javaEditor = context.lookup(FormEditorSupport.class);
            if (javaEditor != null) {
                javaEditor.prepareDocument();
            }
        }
        
        @Override
        public void setMultiViewCallback(MultiViewElementCallback callback) {
            multiViewObserver = callback;
            
            // needed for deserialization...
            if (dataObject instanceof FormDataObject) {
                // this is used (or misused?) to obtain the deserialized
                // multiview topcomponent and set it to FormEditorSupport
                FormDataObject formDataObject = (FormDataObject) dataObject;
                FormEditorSupport fes = (FormEditorSupport)formDataObject.getFormEditorSupport();
                if (javaEditor == null) {
                    javaEditor = fes;
                }
                fes.setTopComponent(callback.getTopComponent());
            }
            super.setMultiViewCallback(callback);
        }

        @Override
        public void componentShowing() {
            super.componentShowing();
            if (dataObject instanceof FormDataObject) {
                FormDataObject formDO = (FormDataObject) dataObject;
                FormEditorSupport fe = (FormEditorSupport)formDO.getFormEditorSupport();
                if (fe != null) {
                    FormModel model = fe.getFormModel();
                    if (model != null) {
                        CodeGenerator codeGen = FormEditor.getCodeGenerator(model);
                        if (codeGen != null) {
                            codeGen.regenerateCode();
                        }
                    }
                }
            }
        }
        
        @Override
        public CloseOperationState canCloseElement() {
            if (javaEditor == null) {
                return CloseOperationState.STATE_OK;
            }
            return javaEditor.canCloseElement(multiViewObserver.getTopComponent());
        }
    }
    
    private static final class Environment extends DataEditorSupport.Env {
        
        private static final long serialVersionUID = -1;
        
        public Environment(DataObject obj) {
            super(obj);
        }
        
        @Override
        protected FileObject getFile() {
            return this.getDataObject().getPrimaryFile();
        }
        
        @Override
        protected FileLock takeLock() throws java.io.IOException {            
            return ((FormDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }
        
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return this.getDataObject().getCookie(FormEditorSupport.class);
        }
        
    }
        
    private final SaveCookie saveCookie = new SaveCookie() {
        @Override
        public void save() throws java.io.IOException {
            if (formEditor == null) { // not saving form, only java
                doSave(false); // don't need to be in event dispatch thread (#102986)
            } else if (EventQueue.isDispatchThread()) {
                doSave(true);
            } else {
                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            doSave(true);
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(FormEditorSupport.class.getName()).log(Level.INFO, "", ex); // NOI18N
                } catch (InvocationTargetException ex) {
                    if (ex.getCause() instanceof RuntimeException) {
                        throw (RuntimeException) ex.getCause();
                    }
                    Logger.getLogger(FormEditorSupport.class.getName()).log(Level.INFO, "", ex); // NOI18N
                }
            }
        }

        private void doSave(boolean bothJavaAndForm) {
            try {
                if (bothJavaAndForm) {
                    saveDocument();
                } else {
                    saveSourceOnly();
                }
            } catch (IOException ex) {
                Logger.getLogger(FormEditorSupport.class.getName()).log(Level.INFO, "", ex); // NOI18N
            }
        }
    };

    private final CookieSet cookies;

    public void addSaveCookie() {
        DataObject javaData = this.getDataObject();
        if (javaData.getCookie(SaveCookie.class) == null) {
            cookies.add(saveCookie);
            javaData.setModified(true);
        }
    }

    public void removeSaveCookie() {
        DataObject javaData = this.getDataObject();
        if (javaData.getCookie(SaveCookie.class) != null) {
            cookies.remove(saveCookie);
            javaData.setModified(false);
        }
    }

}
