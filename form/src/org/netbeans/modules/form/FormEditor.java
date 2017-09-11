/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.form;

import java.awt.EventQueue;
import java.beans.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.*;
import javax.swing.text.Document;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.form.actions.CustomizeEmptySpaceAction;
import org.netbeans.modules.form.actions.EditContainerAction;
import org.netbeans.modules.form.actions.EditFormAction;
import org.netbeans.modules.form.assistant.AssistantModel;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.netbeans.spi.palette.PaletteController;

import org.openide.*;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.WeakSet;

/**
 * Form editor.
 *
 * @author Jan Stola
 */
public class FormEditor {
    /** The FormModel instance holding the form itself */
    private FormModel formModel;

    /** The root node of form hierarchy presented in Component Inspector */
    private FormRootNode formRootNode;

    /** The designer component - the last active designer of the form
     * (there can be more clones). May happen to be null if the active designer
     * was closed and no other designer of given form was activated since then. */
    private FormDesigner formDesigner;

    /** Integration into the editor framework environment. */
    private EditorSupport editorSupport;

    /** The code generator for the form */
    private CodeGenerator codeGenerator;

    /** The FormJavaSource for the form */
    private FormJavaSource formJavaSource;
    
    /** ResourceSupport instance for the form */
    private ResourceSupport resourceSupport;

    /** Instance of binding support for the form.*/
    private boolean bindingSupportInitialized;
    private BindingDesignSupport bindingSupport;

    /** List of exceptions occurred during the last persistence operation */
    private List<Throwable> persistenceErrors;
    
    /** Persistence manager responsible for saving the form */
    private PersistenceManager persistenceManager;
    private String prefetchedSuperclassName;

    /** An indicator whether the form has been loaded (from the .form file) */
    private boolean formLoaded = false;
    
    /** Table of opened FormModel instances (FormModel to FormEditor map) */
    private static Map<FormModel,FormEditor> openForms = new Hashtable<FormModel,FormEditor>();

    /* Maps form model to assistant model. */
    private static Map<FormModel,AssistantModel> formModelToAssistant = new WeakHashMap<FormModel,AssistantModel>();
    
    /** List of floating windows - must be closed when the form is closed. */
    private List<java.awt.Window> floatingWindows;

    /** Set of nodes for which a standalone Properties window was opened.
     * The windows must be closed when the form is closed. */
    private Set<FormNode> nodesWithPropertiesWindows;

    /** The DataObject of the form */
    private FormDataObject formDataObject;
    private PropertyChangeListener dataObjectListener;
    private static PreferenceChangeListener settingsListener;
    private PropertyChangeListener paletteListener;
    
    // listeners
    private FormModelListener formListener;

    /** List of actions that are tried when a component is double-clicked. */
    private List<Action> defaultActions;

    /** Indicates that a task has been posted to ask the user about format
     * upgrade - not to show the confirmation dialog multiple times.
     */
    private boolean upgradeCheckPosted;

    // -----

    public FormEditor(FormDataObject formDataObject, EditorSupport sourceEditor) {
        this.formDataObject = formDataObject;
        this.editorSupport = sourceEditor;
    }

    /** @return root node representing the form (in pair with the class node) */
    public final FormNode getFormRootNode() {
        return formRootNode;
    }

    public final FormNode getOthersContainerNode() {
        FormNode othersNode = formRootNode.getOthersNode();
        return othersNode != null ? othersNode : formRootNode;
    }

    /** @return the FormModel of this form, null if the form is not loaded */
    public final FormModel getFormModel() {
        return formModel;
    }
    
    public final FormDataObject getFormDataObject() {
        return formDataObject;
    }

    public final EditorSupport getEditorSupport() {
        return editorSupport;
    }

    Document getSourcesDocument() {
        return getEditorSupport().getDocument();
    }

    GuardedSectionManager getGuardedSectionManager() {
        return getEditorSupport().getGuardedSectionManager();
    }

    SimpleSection getVariablesSection() {
        return getGuardedSectionManager().findSimpleSection("variables"); // NOI18N
    }
    
    SimpleSection getInitComponentSection() {
        return getGuardedSectionManager().findSimpleSection("initComponents"); // NOI18N
    }

    FormJavaSource getFormJavaSource() {
        return getFormJavaSource(false);
    }

    private FormJavaSource getFormJavaSource(boolean create) {
        if (formJavaSource == null && create) {
            formJavaSource = new FormJavaSource(formDataObject, editorSupport.getJavaContext());
        }
        return formJavaSource;
    }

    CodeGenerator getCodeGenerator() {
        if (!formLoaded)
            return null;
        if (codeGenerator == null)
            codeGenerator = new JavaCodeGenerator();
        return codeGenerator;
    }

    public void regenerateCodeIfNeeded() {
        JavaCodeGenerator codeGen = (JavaCodeGenerator) getCodeGenerator();
        if (codeGen != null) {
            codeGen.regenerateCode();
        }
    }

    ResourceSupport getResourceSupport() {
        if (resourceSupport == null && formModel != null) {
            resourceSupport = new ResourceSupport(formModel);
            resourceSupport.init();
        }
        return resourceSupport;
    }

    BindingDesignSupport getBindingSupport() {
        if (!bindingSupportInitialized && formModel != null) {
            BindingDesignSupportProvider provider = Lookup.getDefault().lookup(BindingDesignSupportProvider.class);
            if (provider != null) {
                bindingSupport = provider.create(formModel);
            }
            bindingSupportInitialized = true;
        }
        return bindingSupport;
    }

    public boolean isFormLoaded() {
        return formLoaded;
    }
    
    public boolean loadForm() {
        if (!formLoaded) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    loadFormData();
                }
            };
            if (java.awt.EventQueue.isDispatchThread()) {
                r.run();
            } else { // loading must be done in AWT event dispatch thread
                try {
                    java.awt.EventQueue.invokeAndWait(r);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return formLoaded;
    }

    /**
     * Called in EDT to prepare loading of the form.
     * @return true on success (so loading can proceed), false if some error happened
     */
    public boolean prepareLoading() {
        resetPersistenceErrorLog();
        if (persistenceManager == null) {
            try {
                persistenceManager = recognizeForm(formDataObject);
            } catch (PersistenceException ex) {
                logPersistenceError(ex, 0);
                return false;
            }
        }
        getFormJavaSource(true);
        prefetchedSuperclassName = null;
        return true;
    }

    /**
     * Optional part of loading, called in a background thread.
     */
    public void loadOnBackground() {
        FormJavaSource javaSource = getFormJavaSource();
        if (javaSource != null) { // i.e. form not closed meanwhile
            prefetchedSuperclassName = javaSource.getSuperClassName();
        }
    }

    /** 
     * Called in EDT to perform the actual form data loading.
     */
    public boolean loadFormData() {
        assert !formLoaded;

        // bug #234032: Beans.setDesignTime is ThreadGroupContext sensitive since JDK 7
        Beans.setDesignTime(true);

        if (persistenceManager == null && !prepareLoading()) {
            return false;
        }

        // Issue 151166 - hack
        Project p = FileOwnerQuery.getOwner(formDataObject.getFormFile());
        // FileOwnerQuery.getOwner() on form opened using Template editor returns null
        if (p != null) {
            FileObject projectDirectory = p.getProjectDirectory();
            FileObject nbprojectFolder = projectDirectory.getFileObject("nbproject", null); // NOI18N
            if ((nbprojectFolder == null) && (projectDirectory.getFileObject("pom", "xml") != null)) { // NOI18N
                // Maven project
                ClassPathUtils.resetFormClassLoader(p);
            }
        }

        if (persistenceManager instanceof GandalfPersistenceManager) {
            ((GandalfPersistenceManager)persistenceManager).setPrefetchedSuperclassName(prefetchedSuperclassName);
            prefetchedSuperclassName = null;
        }

        // create and register new FormModel instance
        formModel = new FormModel();
        formModel.setName(formDataObject.getName());        
        formModel.setReadOnly(formDataObject.isReadOnly());		
	formModel.getCodeStructure().setFormJavaSource(getFormJavaSource(true));

        openForms.put(formModel, this);

        Logger.getLogger("TIMER").log(Level.FINE, "FormModel", new Object[] { formDataObject.getPrimaryFile(), formModel}); // NOI18N

        // Force initialization of Auto Set Component Name.
        // It cannot be initialized in constructor of FormModel,
        // because it may call getResourceSupport() which
        // requires formModel/FormEditor pair to be in openForms.
        formModel.getSettings().getAutoSetComponentName();

        // load the form data (FormModel) and report errors
        try {
            FormLAF.executeWithLookAndFeel(formModel, new Mutex.ExceptionAction() {
                @Override
                public Object run() throws Exception {
                    persistenceManager.loadForm(formDataObject,
                                                formModel,
                                                persistenceErrors);
                    return null;
                }
            });
        }
        catch (PersistenceException ex) { // some fatal error occurred
            openForms.remove(formModel);
            closeForm();
            logPersistenceError(ex, 0);
            return false;
        }
        catch (Exception ex) { // should not happen, but for sure...
            Logger.getLogger(FormEditor.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            openForms.remove(formModel);
            closeForm();
            return false;
        }

        // form is successfully loaded...
        formLoaded = true;

        getCodeGenerator().initialize(formModel);
        ResourceSupport resupport = getResourceSupport(); // make sure ResourceSupport is created and initialized
        if (resupport.getDesignLocale() != null && !"".equals(resupport.getDesignLocale())) { // NOI18N
            resupport.updateDesignLocale();
        }

        getBindingSupport();
        formModel.fireFormLoaded();
        if (formModel.wasCorrected()) // model repaired or upgraded
            formModel.fireFormChanged(false);

        // create form nodes hierarchy
        formRootNode = new FormRootNode(formModel);
        formRootNode.getChildren().getNodes();
        
        attachFormListener();
        attachDataObjectListener();
        attachSettingsListener();
        attachPaletteListener();

        return true;
    }
    
    /** Public method for saving form data to file. Does not save the
     * source code (document), does not report errors and does not throw
     * any exceptions.
     * @return whether there was not any fatal error during saving (true means
     *         everything was ok); returns true even if nothing was saved
     *         because form was not loaded or read-only, etc.
     */
    public boolean saveForm() {
        try {
            saveFormData();
            return true;
        }
        catch (PersistenceException ex) {
            logPersistenceError(ex, 0);
            return false;
        }
    }
    
    public void saveFormData() throws PersistenceException {
        if (formLoaded && !formDataObject.formFileReadOnly() && !formModel.isReadOnly()) {
            formModel.fireFormToBeSaved();

            resetPersistenceErrorLog();

            persistenceManager.saveForm(formDataObject, formModel, persistenceErrors);
        }
    }
    
    private void resetPersistenceErrorLog() {
        if (persistenceErrors != null)
            persistenceErrors.clear();
        else
            persistenceErrors = new ArrayList<Throwable>();
    }
    
    void logPersistenceError(Throwable t, int index) {
        if (persistenceErrors == null)
            persistenceErrors = new ArrayList<Throwable>();

        if (index < 0)
            persistenceErrors.add(t);
        else
            persistenceErrors.add(index, t);
    }
    
    /** Finds PersistenceManager that can load and save the form.
     */
    private PersistenceManager recognizeForm(FormDataObject formDO)
        throws PersistenceException
    {
        Iterator<PersistenceManager> it = PersistenceManager.getManagers();
        if (!it.hasNext()) { // there's no PersistenceManager available
            PersistenceException ex = new PersistenceException(
                    FormUtils.getBundleString("MSG_ERR_NoPersistenceManager")); // NOI18N
            throw ex;
        }

        do {
            PersistenceManager pm = it.next();
            synchronized(pm) {
                try {
                    if (pm.canLoadForm(formDO)) {
                        resetPersistenceErrorLog();
                        return pm;
                    }
                }
                catch (PersistenceException ex) {
                    logPersistenceError(ex);
                    // [continue on exception?]
                }
            }
        }
        while (it.hasNext());

        // no PersistenceManager is able to load the form
        PersistenceException ex;
        if (!anyPersistenceError()) {
            // no error occurred, the format is just unknown
            ex = new PersistenceException(FormUtils.getBundleString("MSG_ERR_NotRecognizedForm")); // NOI18N
        }
        else { // some errors occurred when recognizing the form file format
            Throwable annotateT = null;
            int n = persistenceErrors.size();
            if (n == 1) { // just one exception occurred
                ex = (PersistenceException) persistenceErrors.get(0);
                Throwable t = ex.getOriginalException();
                annotateT = t != null ? t : ex;
                n = 0;
            }
            else { // there were more exceptions
                ex = new PersistenceException("Form file cannot be loaded"); // NOI18N
                annotateT = ex;
            }
            ErrorManager.getDefault().annotate(
                annotateT,
                FormUtils.getBundleString("MSG_ERR_LoadingErrors") // NOI18N
            );
            for (int i=0; i < n; i++) {
                PersistenceException pe = (PersistenceException)
                                          persistenceErrors.get(i);
                Throwable t = pe.getOriginalException();
                ErrorManager.getDefault().annotate(ex, (t != null ? t : pe));
            }
            // all the exceptions were attached to the main exception to
            // be thrown, so the log can be cleared
            resetPersistenceErrorLog();
        }
        throw ex;
    }

    private void logPersistenceError(Throwable t) {
        logPersistenceError(t, -1);
    }

    public boolean anyPersistenceError() {
        return persistenceErrors != null && !persistenceErrors.isEmpty();
    }

    public void reportSavingErrors() { // TODO can get rid of this? (throw exc on failed saving)
        reportErrors(false, null);
    }

    // TODO should not be needed, kept temporarily for compatibility
    public String reportLoadingErrors() {
        DialogDescriptor dd = reportLoadingErrors(null);
        Object message = dd != null ? dd.getMessage() : null;
        return message instanceof String ? (String)message : null;
    }

    /**
     * Helper methods for reporting errors that happend during form loading.
     * (1) If some fatal error happened (i.e. the form could not be loaded) then
     * it is reported right away by this method in a modal dialog.
     * (2) If only some non-fatal errors happened causing some components or
     * properties not loaded, then a DialogDescriptor is created and returned to
     * be used to report the errors at a suitable moment (i.e. after the loading
     * sequence is completed so the GUI form becomes fully visible incl. broken
     * components). In this case the parameter 'options' is used, representing
     * how the user can proceed (e.g. view only, edit anyway, cancel opening).
     * @return DialogDescriptor to use to report errors to the user, or null if
     *         nothing needs to be reported
     */
    public DialogDescriptor reportLoadingErrors(Object[] options) {
        // The options for DialogDescriptor must be provided upfront so the first
        // option can be set as initial (default). This cannot be set later, only
        // in constructor of DialogDescriptor.
        return reportErrors(formLoaded, options);
    }

    private DialogDescriptor reportErrors(boolean checkNonFatalLoadingErrors, Object[] options) {
        if (!anyPersistenceError()) {
            return null; // no errors or warnings logged
        }

        final ErrorManager errorManager = ErrorManager.getDefault();
        final GandalfPersistenceManager persistManager = 
                    (GandalfPersistenceManager) persistenceManager;

        boolean dataLossError = false;

        StringBuilder userErrorMsgs = new StringBuilder();

        for (Iterator it=persistenceErrors.iterator(); it.hasNext(); ) {
            Throwable t  = (Throwable) it.next();
            String originalMessage = null;
            if (t instanceof PersistenceException) {
                Throwable th = ((PersistenceException)t).getOriginalException();
                if (th != null) {
                    // log the original exception so the user has a chance to find it in the log
                    Logger.getLogger("").log(Level.INFO, null, th); // NOI18N
                    if (checkNonFatalLoadingErrors) {
                        t = th;
                    } else {
                        originalMessage = th.getLocalizedMessage();
                    }
                }
            }

            if (checkNonFatalLoadingErrors && !dataLossError) {
                // Loaded, but wasn't there an error causing some data missing?
                ErrorManager.Annotation[] annotations = 
                        errorManager.findAnnotations(t);
                int severity = 0;
                if ((annotations != null) && (annotations.length != 0)) {
                    for (int i=0; i < annotations.length; i++) {
                        int s = annotations[i].getSeverity();
                        if (s == ErrorManager.UNKNOWN)
                            s = ErrorManager.EXCEPTION;
                        if (s > severity)
                            severity = s;
                    }
                }
                else severity = ErrorManager.EXCEPTION;

                if (severity > ErrorManager.WARNING)
                    dataLossError = true;
            }

            if (dataLossError && !(t instanceof PersistenceException)) {
                Logger.getLogger("").log(Level.INFO, t.getLocalizedMessage(), t); // NOI18N
            }

            if (checkNonFatalLoadingErrors && persistManager != null) {
                // creating report about problems while loading components, 
                // setting props of components, ...
                userErrorMsgs.append(persistManager.getExceptionAnnotation(t));
                userErrorMsgs.append("\n\n");  // NOI18N
            } else { // fatal error
                String message = t.getLocalizedMessage();
                if (originalMessage != null && originalMessage.length() > 0) {
                    message = message != null && message.length() > 0 ?
                              (message + "\n\n" + originalMessage) : originalMessage; // NOI18N
                }
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        message, NotifyDescriptor.ERROR_MESSAGE));
            }
        }

        DialogDescriptor dd = null;
        if (checkNonFatalLoadingErrors && dataLossError) {
            // the form was loaded with some non-fatal errors - some data
            // was not loaded - show a warning about possible data loss
            userErrorMsgs.append(FormUtils.getBundleString("MSG_FormLoadedWithErrors"));
            dd = FormUtils.createErrorDialogWithExceptions(
                     FormUtils.getBundleString("CTL_FormLoadedWithErrors"), // NOI18N
                     userErrorMsgs.toString(),
                     DialogDescriptor.WARNING_MESSAGE,
                     options,
                     persistenceErrors.toArray(new Throwable[persistenceErrors.size()]));
        }
        resetPersistenceErrorLog();
        return dd;
    }    
    
    /**
     * Destroys all components from {@link #formModel} taged as invalid
     */
    public void destroyInvalidComponents() {
        Collection<RADComponent> allComps = formModel.getAllComponents();
        List<RADComponent> invalidComponents = new ArrayList<RADComponent>(allComps.size());
        // collect all invalid components
        for (RADComponent comp : allComps) {
            if(!comp.isValid()) {
                invalidComponents.add(comp);
            }
        }              
        // destroy all invalid components
        for (RADComponent comp : invalidComponents) {
            try {
                RADComponentNode node = comp.getNodeReference();
                if (node != null) {
                    node.destroy();
                }
            }
            catch (java.io.IOException ex) { // should not happen
                ex.printStackTrace();
            }                    
        }           
    }
    
    /**
     * Sets the FormEditor to read-only mode.
     */
    public void setFormReadOnly() {
        formModel.setReadOnly(true);
        FormDesigner designer = getFormDesigner();
        if (designer != null) {
            HandleLayer handleLayer = designer.getHandleLayer();
            if (handleLayer != null) {
                handleLayer.setViewOnly(true);
            }
        }
        detachFormListener();
    }

    boolean needPostCreationUpdate() {
        return Boolean.TRUE.equals(formDataObject.getPrimaryFile().getAttribute("justCreatedByNewWizard")); // NOI18N
        // see o.n.m.f.w.TemplateWizardIterator.instantiate()
    }

    /**
     * Form just created by the user via the New wizard may need some additional
     * setup that can't be ensured by the static template. For example the type
     * of layout code generation needs to be honored, or properties
     * internationalized or converted to resources.
     */
    public boolean postCreationUpdate() {
        if (formLoaded && formModel != null && !formModel.isReadOnly()
                && !anyPersistenceError() && needPostCreationUpdate()) {
            // detect settings, update the form, regenerate code, save
            // make sure no upgrade warning is shown
            formModel.setMaxVersionLevel(FormModel.LATEST_VERSION);
            // switch to resources if needed
            FormLAF.executeWithLookAndFeel(formModel, new Runnable() {
                @Override
                public void run() {
                    getResourceSupport().prepareNewForm();
                }
            });
            // make sure layout code generation type is detected
            formModel.getSettings().getLayoutCodeTarget();
            // hack: regenerate code immediately
            // - needs to be forced since there might be no change fired
            // - don't wait for the next round, we want to save now
            formModel.fireFormChanged(true);
            try {
                formDataObject.getPrimaryFile().setAttribute("justCreatedByNewWizard", null); // NOI18N
                formDataObject.getPrimaryFile().setAttribute("nonEditableTemplate", null); // NOI18N
            } catch (IOException ex) {
                Logger.getLogger(FormEditor.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
            return true;
        }
        return false;
    }

    /** @return the last activated FormDesigner for this form */
    FormDesigner getFormDesigner() {
        if (!formLoaded)
            return null;

        return formDesigner;
    }

    /** Called by FormDesigner when activated. */
    void setFormDesigner(FormDesigner designer) {
        formDesigner = designer;
    }

    /** Closes the form. Used when closing the form editor or reloading
     * the form. */
    public void closeForm() {
        if (formLoaded) {
            formModel.fireFormToBeClosed();

            openForms.remove(formModel);
            formModelToAssistant.remove(formModel);
            formLoaded = false;
            
            // remove listeners
            detachFormListener();
            detachDataObjectListener();
            detachPaletteListener();

            if (openForms.isEmpty()) {
                detachSettingsListener();
            }

            // close the floating windows
            if (floatingWindows != null) {
                if (floatingWindows.size() > 0) {
                    List<java.awt.Window> tempList = new LinkedList<java.awt.Window>(floatingWindows);
                    Iterator it = tempList.iterator();
                    while (it.hasNext()) {
                        java.awt.Window window = (java.awt.Window) it.next();
                        if (window.isVisible())
                            window.setVisible(false);
                    }
                }
                floatingWindows = null;
            }

            // close standalone properties window invoked explicitly on selected nodes via the Properties action
            if (nodesWithPropertiesWindows != null) {
                for (FormNode n : nodesWithPropertiesWindows) {
                    n.fireNodeDestroyedHelper();
                }
            }
        }
        ClassPathUtils.releaseFormClassLoader(formDataObject.getPrimaryFile());
        ClassPathUtils.releaseFormClassLoader(formDataObject.getFormFile());
        // cleanup just for sure
        formRootNode = null;
        formDesigner = null;
        persistenceManager = null;
        formModel = null;
        codeGenerator = null;
        formJavaSource = null;
        prefetchedSuperclassName = null;
        resourceSupport = null;
        bindingSupportInitialized = false;
        bindingSupport = null;
    }

    /**
     * Changes the DataObject of the form. Should be used only in situations when
     * the original java and form files are renamed or moved elsewhere and the
     * form editor cannot be reloaded (recreated) with the new FormDataObject
     * (e.g. because it is opened with unsaved changes). This may happen in JDev.
     */
    public void relocate(FormDataObject newDataObject) {
        detachDataObjectListener();
        detachPaletteListener();
        FormDataObject oldDataObject = formDataObject;
        formDataObject = newDataObject;
        formJavaSource = null;
        if (formLoaded) {
            String name = formDataObject.getName();
            formModel.setName(name);
            formRootNode.updateName(name);
            formModel.getCodeStructure().setFormJavaSource(getFormJavaSource(true));
            ClassPathUtils.getProjectClassLoader(newDataObject.getPrimaryFile());
            ClassPathUtils.releaseFormClassLoader(oldDataObject.getPrimaryFile());
            ClassPathUtils.releaseFormClassLoader(oldDataObject.getFormFile());
            attachDataObjectListener();
            attachPaletteListener();
            formModel.fireFormChanged(false);
        }
    }

    private void attachFormListener() {
        if (formListener != null || formDataObject.isReadOnly() || formModel.isReadOnly())
            return;

        // this listener ensures necessary updates of nodes according to
        // changes in containers in form
        formListener = new FormModelListener() {
            @Override
            public void formChanged(FormModelEvent[] events) {
                if (events == null)
                    return;

                boolean justAfterLoading = false;
                boolean modifying = false;
                Set<ComponentContainer> changedContainers = events.length > 0 ?
                                          new HashSet<ComponentContainer>() : null;
                Set<RADComponent> compsToSelect = null;
                FormNode nodeToSelect = null;

                for (int i=0; i < events.length; i++) {
                    FormModelEvent ev = events[i];

                    if (ev.isModifying())
                        modifying = true;

                    int type = ev.getChangeType();
                    if (type == FormModelEvent.CONTAINER_LAYOUT_EXCHANGED
                        || type == FormModelEvent.CONTAINER_LAYOUT_CHANGED
                        || type == FormModelEvent.COMPONENT_ADDED
                        || type == FormModelEvent.COMPONENT_REMOVED
                        || type == FormModelEvent.COMPONENTS_REORDERED)
                    {
                        ComponentContainer cont = ev.getContainer();
                        if (changedContainers == null
                            || !changedContainers.contains(cont))
                        {
                            updateNodeChildren(cont);
                            if (changedContainers != null)
                                changedContainers.add(cont);
                        }

                        if (type == FormModelEvent.COMPONENT_REMOVED) {
                            FormNode select;
                            if (cont instanceof RADComponent) {
                                select = ((RADComponent)cont).getNodeReference();
                             } else {
                                select = getOthersContainerNode();
                             }

                            if (!(nodeToSelect instanceof RADComponentNode)) {
                                if (nodeToSelect != formRootNode)
                                    nodeToSelect = select;
                            }
                            else if (nodeToSelect != select)
                                nodeToSelect = formRootNode;
                        }
                        else if (type == FormModelEvent.CONTAINER_LAYOUT_EXCHANGED) {
                            nodeToSelect = ((RADVisualContainer)cont)
                                                .getLayoutNodeReference();
                        }
                        else if (type == FormModelEvent.COMPONENT_ADDED
                                 && ev.getComponent().isInModel())
                        {
                            if (compsToSelect == null)
                                compsToSelect = new HashSet<RADComponent>();

                            compsToSelect.add(ev.getComponent());
                            compsToSelect.remove(ev.getContainer());
                        }
                    } else if (type == FormModelEvent.FORM_LOADED) {
                        justAfterLoading = true;
                    }
                }

                FormDesigner designer = getFormDesigner();
                if (designer != null) {
                    if (compsToSelect != null) {
                        RADComponent[] comps = new RADComponent[compsToSelect.size()];
                        compsToSelect.toArray(comps);
                        designer.setSelectedComponents(comps);
                    }
                    else if (nodeToSelect != null) {
                        designer.setSelectedNodes(nodeToSelect);
                    }
                }

                if (modifying && (!justAfterLoading || needPostCreationUpdate() || formModel.isCompoundEditInProgress())) {
                    // mark the form document modified explicitly, but not if modified
                    // as a result of correction during form loading (not to open as modified)
                    getEditorSupport().markModified();
                    checkFormVersionUpgrade();
                }
            }
        };

        formModel.addFormModelListener(formListener);
    }

    private void detachFormListener() {
        if (formListener != null) {
            formModel.removeFormModelListener(formListener);
            formListener = null;
        }
    }

    /** Updates (sub)nodes of a container (in Component Inspector) after
     * a change has been made (like component added or removed). */
    void updateNodeChildren(ComponentContainer metacont) {
        FormNode node = null;

        if (metacont == null || metacont == formModel.getModelContainer()) {
            node = (formRootNode != null ? getOthersContainerNode() : null);
        } else if (metacont instanceof RADComponent) {
            node = ((RADComponent)metacont).getNodeReference();
        }

        if (node != null) {
            node.updateChildren();
        }
    }

    /**
     * After a round of changes check whether they did not require to upgrade
     * the form version. If the required version is higher than the version of
     * the IDE in which the form was created, ask the user for confirmation - to
     * let them know the form will not open in the older IDE anymore. If the
     * user refuses the upgrade, undo is performed (for that all the fired
     * changes must be already processed).
     */
    private void checkFormVersionUpgrade() {
        FormModel.FormVersion currentVersion = formModel.getCurrentVersionLevel();
        FormModel.FormVersion maxVersion = formModel.getMaxVersionLevel();
        if (currentVersion.ordinal() > maxVersion.ordinal()) {
            if (EventQueue.isDispatchThread()) {
                processVersionUpgrade(true);
            } else { // not a result of a user action, or some forgotten upgrade...
                confirmVersionUpgrade();
            }
        }
    }

    private void processVersionUpgrade(boolean processingEvents) {
        if (!processingEvents && formModel.hasPendingEvents()) {
            processingEvents = true;
        }
        if (processingEvents) { // post a task for later, if not already posted
            if (!upgradeCheckPosted) {
                upgradeCheckPosted = true;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        upgradeCheckPosted = false;
                        if (formModel != null) {
                            processVersionUpgrade(false);
                        }
                    }
                });
            }
        } else { // all events processed
            String upgradeOption = FormUtils.getBundleString("CTL_UpgradeOption"); // NOI18N
            String undoOption = FormUtils.getBundleString("CTL_CancelOption"); // NOI18N
            NotifyDescriptor d = new NotifyDescriptor(
                    FormUtils.getBundleString("MSG_UpgradeQuestion"), // NOI18N
                    FormUtils.getBundleString("TITLE_FormatUpgrade"), // NOI18N
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new String[] { upgradeOption, undoOption},
                    upgradeOption);
            if (DialogDisplayer.getDefault().notify(d) == upgradeOption) {
                confirmVersionUpgrade();
            } else { // upgrade refused
                revertVersionUpgrade();
            }
        }
    }

    private void confirmVersionUpgrade() {
        if (formModel != null) {
            formModel.confirmVersionLevel();
            formModel.setMaxVersionLevel(FormModel.LATEST_VERSION);
        }
    }

    private void revertVersionUpgrade() {
        if (formModel != null) {
            formModel.getUndoRedoManager().undo();
            formModel.revertVersionLevel();
        }
    }

    private void attachDataObjectListener() {
        if (dataObjectListener != null)
            return;

        dataObjectListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                if (DataObject.PROP_NAME.equals(ev.getPropertyName())) {
                    // FormDataObject's name has changed
                    String name = formDataObject.getName();
                    formModel.setName(name);
                    formRootNode.updateName(name);
                    // multiview updated by FormEditorSupport
                    // code regenerated by FormRefactoringUpdate
                }
                else if (DataObject.PROP_COOKIE.equals(ev.getPropertyName())
                         && formDesigner != null) {
                    for (Node node : formDesigner.getSelectedNodes()) {
                        if (node instanceof FormNode) { // Issue 181709
                            ((FormNode)node).updateCookies();
                        }
                    }
                }
            }
        };

        formDataObject.addPropertyChangeListener(dataObjectListener);
    }

    private void detachDataObjectListener() {
        if (dataObjectListener != null) {
            formDataObject.removePropertyChangeListener(dataObjectListener);
            dataObjectListener = null;
        }
    }
    
    private static void attachSettingsListener() {
        if (settingsListener != null)
            return;

        settingsListener = new PreferenceChangeListener() {
            @Override
            public void preferenceChange(final PreferenceChangeEvent evt) {
                if (EventQueue.isDispatchThread()) {
                    updateSettings(evt);
                } else {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateSettings(evt);
                        }
                    });
                }
            }
        };

        FormLoaderSettings.getPreferences().addPreferenceChangeListener(settingsListener);
    }

    private static void updateSettings(PreferenceChangeEvent evt) {
        Iterator iter = openForms.keySet().iterator();
        while (iter.hasNext()) {
            FormModel formModel = (FormModel) iter.next();
            String propName = evt.getKey();

            if (FormLoaderSettings.PROP_USE_INDENT_ENGINE.equals(propName)) {
                formModel.fireSyntheticPropertyChanged(null, propName,
                                                       null, evt.getNewValue());
            } else if (FormLoaderSettings.PROP_SELECTION_BORDER_SIZE.equals(propName)                    
                  || FormLoaderSettings.PROP_SELECTION_BORDER_COLOR.equals(propName)
                  || FormLoaderSettings.PROP_CONNECTION_BORDER_COLOR.equals(propName)
                  || FormLoaderSettings.PROP_FORMDESIGNER_BACKGROUND_COLOR.equals(propName)
                  || FormLoaderSettings.PROP_FORMDESIGNER_BORDER_COLOR.equals(propName)
                  || FormLoaderSettings.PROP_GUIDING_LINE_COLOR.equals(propName))
            {
                FormDesigner designer = getFormDesigner(formModel);
                if (designer != null) {
                    designer.updateVisualSettings();
                }
            } else if (FormLoaderSettings.PROP_PALETTE_IN_TOOLBAR.equals(propName)) {
                FormDesigner designer = getFormDesigner(formModel);
                if (designer != null) {
                    designer.getFormToolBar().showPaletteButton(
                        FormLoaderSettings.getInstance().isPaletteInToolBar());
                }
            } else if (FormLoaderSettings.PROP_PAINT_ADVANCED_LAYOUT.equals(propName)) {
                FormDesigner designer = getFormDesigner(formModel);
                if (designer != null) {
                    LayoutDesigner layoutDesigner = designer.getLayoutDesigner();
                    if (layoutDesigner != null) {
                        int paintLayout = FormLoaderSettings.getInstance().getPaintAdvancedLayoutInfo();
                        layoutDesigner.setPaintAlignment((paintLayout&1) != 0);
                        layoutDesigner.setPaintGaps((paintLayout&2) != 0);
                        designer.getHandleLayer().repaint();
                    }
                }
            }
        }
    }

    private static void detachSettingsListener() {
        if (settingsListener != null) {
            FormLoaderSettings.getPreferences().removePreferenceChangeListener(settingsListener);
            settingsListener = null;
        }
    }

    private void attachPaletteListener() {
        if (paletteListener != null)
            return;

        paletteListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (PaletteController.PROP_SELECTED_ITEM.equals(evt.getPropertyName())) {
                    FormModel formModel = getFormModel();
                    if (isFormLoaded() && formModel != null && !formModel.isReadOnly()) {
                        FormDesigner designer = getFormDesigner(formModel);
                        if (designer != null) {
                            // PENDING should be done for all cloned designers
                            if (evt.getNewValue() == null) {
                                if (designer.getDesignerMode() == FormDesigner.MODE_ADD)
                                    designer.setDesignerMode(FormDesigner.MODE_SELECT);
                            } else {
                                if (designer.getDesignerMode() == FormDesigner.MODE_ADD) {
                                    // Change in the selected palette item means unselection
                                    // of the old item and selection of the new one
                                    designer.setDesignerMode(FormDesigner.MODE_SELECT);
                                }
                                designer.setDesignerMode(FormDesigner.MODE_ADD);
                            }
                            // TODO activate current designer?
                        }
                    }
                }
            }
        };

        PaletteUtils.addPaletteListener(paletteListener, formDataObject.getPrimaryFile());
    }

    private void detachPaletteListener() {
        if (paletteListener != null) {
            PaletteUtils.removePaletteListener(paletteListener, formDataObject.getPrimaryFile());
            paletteListener = null;
        }
    }

    void reinstallListener() {
        if (formListener != null) {
            formModel.removeFormModelListener(formListener);
            formModel.addFormModelListener(formListener);
        }
    }

    /**
     * Returns code editor pane for the specified form.
     * 
     * @param formModel form model.
     * @return JEditorPane set up with the actuall forms java source*/
    public static JEditorPane createCodeEditorPane(FormModel formModel) {                        
        FormEditor formEditor = FormEditor.getFormEditor(formModel);
        JavaCodeGenerator codeGen = (JavaCodeGenerator) formEditor.getCodeGenerator();
        codeGen.regenerateCode();

        JEditorPane codePane = new JEditorPane();
        SimpleSection sec = formEditor.getInitComponentSection();
        int pos = sec.getText().indexOf('{') + 2 + sec.getStartPosition().getOffset();
        FormUtils.setupEditorPane(codePane, formEditor.getFormDataObject().getPrimaryFile(), pos);
        return codePane;
    }

    public static synchronized AssistantModel getAssistantModel(FormModel formModel) {
        assert (formModel != null);
        AssistantModel assistant = formModelToAssistant.get(formModel);
        if (assistant == null) {
            assistant = new AssistantModel();
            formModelToAssistant.put(formModel, assistant);
        }
        return assistant;
    }

    /**
     * @param formModel form model.
     * @return FormDesigner for given form */
    public static FormDesigner getFormDesigner(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getFormDesigner() : null;
    }

    /**
     * Returns code generator for the specified form.
     * 
     * @param formModel form model.
     * @return CodeGenerator for given form */
    public static CodeGenerator getCodeGenerator(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getCodeGenerator() : null;
    }

    /**
     * Returns form data object for the specified form.
     * 
     * @param formModel form model.
     * @return FormDataObject of given form */
    public static FormDataObject getFormDataObject(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getFormDataObject() : null;
    }

    /**
     * Returns <code>FormJavaSource</code> for the specified form.
     * 
     * @param formModel form model.
     * @return FormJavaSource of given form */
    public static FormJavaSource getFormJavaSource(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getFormJavaSource() : null;
    }

    /**
     * Returns <code>ResourceSupport</code> for the specified form.
     * 
     * @param formModel form model.
     * @return ResourceSupport of given form */
    static ResourceSupport getResourceSupport(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getResourceSupport() : null;
    }

    /**
     * Returns <code>BindingDesignSupport</code> for the specified form.
     * 
     * @param formModel form model.
     * @return BindingDesignSupport of given form */
    public static BindingDesignSupport getBindingSupport(FormModel formModel) {
        FormEditor formEditor = openForms.get(formModel);
        return formEditor != null ? formEditor.getBindingSupport() : null;
    }

    /**
     * Returns form editor for the specified form.
     * 
     * @param formModel form model.
     * @return FormEditor instance for given form */
    public static FormEditor getFormEditor(FormModel formModel) {
        return openForms.get(formModel);
    }
    
    UndoRedo.Manager getFormUndoRedoManager() {
        return formModel != null ? formModel.getUndoRedoManager() : null;
    }
    
    public void registerFloatingWindow(java.awt.Window window) {
        if (floatingWindows == null)
            floatingWindows = new ArrayList<java.awt.Window>();
        else
            floatingWindows.remove(window);
        floatingWindows.add(window);
    }

    public void unregisterFloatingWindow(java.awt.Window window) {
        if (floatingWindows != null)
            floatingWindows.remove(window);
    }

    void registerNodeWithPropertiesWindow(FormNode node) {
        if (nodesWithPropertiesWindows == null) {
            nodesWithPropertiesWindows = new WeakSet<FormNode>();
        }
        nodesWithPropertiesWindows.add(node);
    }

    public void registerDefaultComponentAction(Action action) {
        if (defaultActions == null) {
            createDefaultComponentActionsList();
        } else {
            defaultActions.remove(action);
        }
        defaultActions.add(0, action);
    }

    public void unregisterDefaultComponentAction(Action action) {
        if (defaultActions != null) {
            defaultActions.remove(action);
        }
    }

    private void createDefaultComponentActionsList() {
        defaultActions = new LinkedList<Action>();
        defaultActions.add(SystemAction.get(CustomizeEmptySpaceAction.EditSingleGapAction.class));
        defaultActions.add(SystemAction.get(EditContainerAction.class));
        defaultActions.add(SystemAction.get(EditFormAction.class));
        defaultActions.add(SystemAction.get(DefaultRADAction.class));
    }

    Collection<Action> getDefaultComponentActions() {
        if (defaultActions == null) {
            createDefaultComponentActionsList();
        }
        return Collections.unmodifiableList(defaultActions);
    }

    /**
     * Updates project classpath with the layout extensions library.
     * 
     * @param formModel form model.
     * @return <code>true</code> if the project was updated.
     */
    public static boolean updateProjectForNaturalLayout(FormModel formModel) {
        FormEditor formEditor = getFormEditor(formModel);
        if (formEditor != null
                && formModel.getSettings().getLayoutCodeTarget() != JavaCodeGenerator.LAYOUT_CODE_JDK6
                && !ClassPathUtils.isOnClassPath(formEditor.getFormDataObject().getFormFile(), "org.jdesktop.layout.GroupLayout")) { // NOI18N
            try {
                ClassSource cs = new ClassSource("", // class name is not needed // NOI18N
                        ClassSource.unpickle("library", "swing-layout")); // NOI18N // Hack
                return Boolean.TRUE == ClassPathUtils.updateProject(formEditor.getFormDataObject().getFormFile(), cs, true);
            }
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            catch (RuntimeException ex) { // e.g. UnsupportedOperationException
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return false;
    }

    /**
     * Updates project classpath with the beans binding library.
     * 
     * @param formModel form model.
     * @return <code>true</code> if the project was updated.
     */
    public static boolean updateProjectForBeansBinding(FormModel formModel) {
        FormEditor formEditor = getFormEditor(formModel);
        if (formEditor != null) {
            return formEditor.getBindingSupport().updateProjectForBeansBinding();
        }
        return false;
    }

    public static boolean isNonVisualTrayEnabled() {
        return Boolean.getBoolean("netbeans.form.non_visual_tray"); // NOI18N
    }

}
