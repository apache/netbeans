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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.*;

import org.openide.awt.UndoRedo;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.CodeStructure;
import org.netbeans.modules.form.layoutdesign.*;

/**
 * Holds all data of a form.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class FormModel
{
    // name of the form (name of the DataObject)
    private String formName;

    private boolean readOnly = false;

    public enum FormVersion {
        BASIC, // form file version up to 1.2
        NB50, // form file verson 1.3
        NB60_PRE, // until NB 6.0 beta 1 (incl. 5.5 with 6.0 update), form file version 1.4
        NB60, // since NB 6.0 beta1, form file version 1.5
        NB61, // since NB 6.1 milestone 2, form file version 1.6
        NB65, // since NB 6.5 milestone 1, form file version 1.7
        NB71, // since NB 7.1, form file version 1.8
        NB74 // since NB 7.4, form file version 1.9
    }
    final static FormVersion LATEST_VERSION = FormVersion.NB74;

    private FormVersion currentVersionLevel;
    private FormVersion lastConfirmedVersionLevel;
    private FormVersion maxVersionLevel; // max version to upgrade to without user confirmation

    // the class on which the form is based (which is extended in the java file)
    private Class<?> formBaseClass;

    // the top metacomponent of the form (null if form is based on Object)
    private RADComponent topRADComponent;

    // other components - out of the main hierarchy under topRADComponent
    private List<RADComponent> otherComponents = new ArrayList<RADComponent>(10);

    // holds both topRADComponent and otherComponents
    private ComponentContainer modelContainer;

    private LayoutModel layoutModel;

    private Map<String,RADComponent> idToComponents = new HashMap<String,RADComponent>();

    private boolean formLoaded = false;

    private UndoRedo.Manager undoRedoManager;
    private boolean undoRedoRecording = false;
    private CompoundEdit compoundEdit;
    private boolean undoCompoundEdit = false;

    private FormEvents formEvents;

    // list of listeners registered on FormModel
    private ArrayList<FormModelListener> listeners;
    private List<FormModelEvent> eventList;
    private boolean firing;

    private MetaComponentCreator metaCreator;

    private CodeStructure codeStructure = new CodeStructure(false);
    
    private FormSettings settings = new FormSettings(this);
    
    private boolean freeDesignDefaultLayout = false;

    // -------------
    // initialization

    FormModel() {
    }

    /** This methods sets the form base class (which is in fact the superclass
     * of the form class in source java file). It is used for initializing
     * the top meta component, and is also presented as the top component
     * in designer and inspector.
     * 
     * @param formClass form base class.
     * @throws java.lang.Exception if anything goes wrong.
     */
    public void setFormBaseClass(Class<?> formClass) throws Exception {
        if (formBaseClass != null)
            throw new IllegalStateException("Form type already initialized."); // NOI18N

        RADComponent topComp;
        if (FormUtils.isVisualizableClass(formClass)) {
            if (FormUtils.isContainer(formClass)) {
                topComp = new RADVisualFormContainer();
            }
            else {
                topComp = new RADVisualComponent() {
                    // top-level component does not have a variable
                    @Override
                    public String getName() {
                        return FormUtils.getBundleString("CTL_FormTopContainerName"); // NOI18N
                    }
                    @Override
                    public void setName(String value) {}
                };
            }
        }
        else if (java.lang.Object.class != formClass)
            topComp = new RADFormContainer();
        else topComp = null;

        if (topComp != null) {
            topRADComponent = topComp;
            topComp.initialize(this);
            topComp.initInstance(formClass);
            topComp.setInModel(true);
        }

        formBaseClass = formClass;
//        topRADComponent = topComp;
        layoutModel = new LayoutModel();
        layoutModel.setChangeRecording(false);
    }

    public Class<?> getFormBaseClass() {
        return formBaseClass;
    }

    void setName(String name) {
        formName = name;
    }

    /**
     * Requires the form version to be at least 'minVersion'. If the actual
     * version is lower, it is upgraded to 'upgradeTo'. If the upgrade exceeds
     * the maximum version level set for this form (roughly corresponding
     * to the NB version in which the form was created) a confirmation message
     * is shown to the user later (see FormEditor.checkFormVersionUpgrade).
     * @param minVersion the minimum version level required
     * @param upgradeTo version level to upgrade to if the minimum version is not met
     */
    public void raiseVersionLevel(FormVersion minVersion, FormVersion upgradeTo) {
        if (minVersion.ordinal() > currentVersionLevel.ordinal()
                && (undoRedoRecording || !formLoaded)) {
            assert upgradeTo.ordinal() >= minVersion.ordinal();
            setCurrentVersionLevel(upgradeTo);
        }
    }

    void setCurrentVersionLevel(FormVersion version) {
        if (lastConfirmedVersionLevel == null) {
            lastConfirmedVersionLevel = currentVersionLevel;
        }
        currentVersionLevel = version;
    }

    FormVersion getCurrentVersionLevel() {
        return currentVersionLevel;
    }

    void revertVersionLevel() {
        currentVersionLevel = lastConfirmedVersionLevel;
    }

    void confirmVersionLevel() {
        lastConfirmedVersionLevel = currentVersionLevel;
    }

    void setMaxVersionLevel(FormVersion version) {
        maxVersionLevel = version;
    }

    FormVersion getMaxVersionLevel() {
        return maxVersionLevel;
    }

    void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    // -----------
    // getters

    public final String getName() {
        return formName;
    }

    public final boolean isReadOnly() {
        return readOnly;
    }

    public final boolean isFormLoaded() {
        return formLoaded;
    }

    public final boolean wasCorrected() {
        return formLoaded && layoutModel != null && layoutModel.wasCorrected();
    }

//    public final FormDesigner getFormDesigner() {
//        return FormEditorSupport.getFormDesigner(this);
//    }
//
//    // for compatibility with previous version
//    public final FormDataObject getFormDataObject() {
//        return FormEditorSupport.getFormDataObject(this);
//    }

    public final RADComponent getTopRADComponent() {
        return topRADComponent;
    }

    public ComponentContainer getModelContainer() {
        if (modelContainer == null)
            modelContainer = new ModelContainer();
        return modelContainer;
    }

    public Collection<RADComponent> getOtherComponents() {
        return Collections.unmodifiableCollection(otherComponents);
    }

    public final LayoutModel getLayoutModel() {
        return layoutModel;
    }

    public final RADComponent getMetaComponent(String id) {
        return idToComponents.get(id);
    }

    public RADComponent findRADComponent(String name) {
        Iterator allComps = idToComponents.values().iterator(); // getMetaComponents().iterator();
        while (allComps.hasNext()) {
            RADComponent comp = (RADComponent) allComps.next();
            if (name.equals(comp.getName()))
                return comp;
        }
        return null;
    }

    /**
     * Returns list of all components in the model. A new List instance is
     * created. The order of the components is random.
     * 
     * @return list of components in the model.
     */
    public java.util.List<RADComponent> getComponentList() {
        return new ArrayList<RADComponent>(idToComponents.values());
    }

    /**
     * Returns list of all components in the model. A new instance of list is
     * created and the components are added to the list in the traversal order
     * (used e.g. by code generator or persistence manager).
     * 
     * @return list of components in the model.
     */
    public java.util.List<RADComponent> getOrderedComponentList() {
        java.util.List<RADComponent> list = new ArrayList<RADComponent>(idToComponents.size());
        collectMetaComponents(getModelContainer(), list);
        return list;
    }

    /**
     * Returns an unmodifiable collection of all components in the model
     * in random order.
     * 
     * @return list of components in the model.
     */
    public Collection<RADComponent> getAllComponents() {
        return Collections.unmodifiableCollection(idToComponents.values());
    }

    public List<RADComponent> getNonVisualComponents() {
        List<RADComponent> list = new ArrayList<RADComponent>(otherComponents.size());
        for (RADComponent metacomp : otherComponents) {
            if (!(metacomp instanceof RADVisualComponent)) {
                list.add(metacomp);
            }
        }
        return list;
    }

    public List<RADComponent> getVisualComponents() {
        List<RADComponent> list = new ArrayList<RADComponent>(idToComponents.size());
        for (Map.Entry<String,RADComponent> e : idToComponents.entrySet()) {
            RADComponent metacomp = e.getValue();
            if (metacomp instanceof RADVisualComponent) {
                list.add(metacomp);
            }
        }
        return list;
    }

    public FormEvents getFormEvents() {
        if (formEvents == null)
            formEvents = new FormEvents(this);
        return formEvents;
    }

    private static void collectMetaComponents(ComponentContainer cont,
                                              java.util.List<RADComponent> list) {
        RADComponent[] comps = cont.getSubBeans();
        for (int i = 0; i < comps.length; i++) {
            RADComponent comp = comps[i];
            list.add(comp);
            if (comp instanceof ComponentContainer)
                collectMetaComponents((ComponentContainer) comp, list);
        }
    }

    private static void collectVisualMetaComponents(RADVisualContainer cont,
                                                    java.util.List<RADComponent> list) {
        RADVisualComponent[] comps = cont.getSubComponents();
        for (int i = 0; i < comps.length; i++) {
            RADComponent comp = comps[i];
            list.add(comp);
            if (comp instanceof RADVisualContainer)
                collectVisualMetaComponents((RADVisualContainer) comp, list);
        }
    }

    public FormSettings getSettings() {
        return settings;
    }

    // -----------
    // adding/deleting components, setting layout, etc

    /**
     * @return MetaComponentCreator responsible for creating new components and
     *         adding them to the model.
     */
    public MetaComponentCreator getComponentCreator() {
        if (metaCreator == null)
            metaCreator = new MetaComponentCreator(this);
        return metaCreator;
    }

    /** Adds a new component to given (non-visual) container in the model. If
     * the container is not specified, the component is added to the
     * "other components".
     * 
     * @param metacomp component to add.
     * @param parentContainer parent of the added component.
     * @param newlyAdded is newly added?
     */
    public void addComponent(RADComponent metacomp,
                             ComponentContainer parentContainer,
                             boolean newlyAdded)
    {
        if (newlyAdded || !metacomp.isInModel()) {
            setInModelRecursively(metacomp, true);
            newlyAdded = true;
        }

        if (parentContainer != null) {
            parentContainer.add(metacomp);
        }
        else {
            metacomp.setParentComponent(null);
            otherComponents.add(metacomp);
        }
        if (!newlyAdded && (metacomp instanceof RADVisualComponent)) {
            ((RADVisualComponent)metacomp).resetConstraintsProperties();
        }

        fireComponentAdded(metacomp, newlyAdded);
    }

    /** Adds a new visual component to given container managed by the old
     * layout support.
     * 
     * @param metacomp component to add.
     * @param parentContainer parent of the added component.
     * @param constraints layout constraints.
     * @param newlyAdded is newly added?
     */
    public void addVisualComponent(RADVisualComponent metacomp,
                                   RADVisualContainer parentContainer,
                                   Object constraints,
                                   boolean newlyAdded)
    {
        LayoutSupportManager layoutSupport = parentContainer.getLayoutSupport();
        if (layoutSupport != null) {
            RADVisualComponent[] compArray = new RADVisualComponent[] { metacomp };
            LayoutConstraints c = constraints instanceof LayoutConstraints ?
                                  (LayoutConstraints) constraints : null;
            LayoutConstraints[] constrArray = new LayoutConstraints[] { c };
            int index = constraints instanceof Integer ? ((Integer)constraints).intValue() : -1;

            // component needs to be "in model" (have code expression) before added to layout
            if (newlyAdded || !metacomp.isInModel()) {
                setInModelRecursively(metacomp, true);
                newlyAdded = true;
            }

            try {
                layoutSupport.acceptNewComponents(compArray, constrArray, index);
            }
            catch (RuntimeException ex) {
                // LayoutSupportDelegate may not accept the component
                if (newlyAdded)
                    setInModelRecursively(metacomp, false);
                throw ex;
            }

            parentContainer.add(metacomp, index);

            if (parentContainer.isLayoutSubcomponent(metacomp)) {
                layoutSupport.addComponents(compArray, constrArray, index);
            }

            fireComponentAdded(metacomp, newlyAdded);
        }
        else {
            addComponent(metacomp, parentContainer, newlyAdded);
        }
    }

    void setContainerLayoutImpl(RADVisualContainer metacont,
                                LayoutSupportDelegate layoutDelegate)
        throws Exception
    {
        LayoutSupportManager currentLS = metacont.getLayoutSupport();
        LayoutSupportDelegate currentDel =
            currentLS != null ? currentLS.getLayoutDelegate() : null;

        if (currentLS == null) { // switching to old layout support
            metacont.setOldLayoutSupport(true);
        }
        try {
            metacont.setLayoutSupportDelegate(layoutDelegate);
        } catch (Exception ex) {
            if (currentLS == null) {
                // failure might leave the layout delegate null (#115431)
                metacont.setOldLayoutSupport(false);
            }
            throw ex;
        }

        fireContainerLayoutExchanged(metacont, currentDel, layoutDelegate);
    }

    public void setContainerLayout(RADVisualContainer metacont,
                                   LayoutSupportDelegate layoutDelegate)
        throws Exception {
        LayoutSupportManager currentLS = metacont.getLayoutSupport();
        setContainerLayoutImpl(metacont, layoutDelegate);
        if (currentLS == null) { // switching to old layout support
            Object layoutStartMark = layoutModel.getChangeMark();
            UndoableEdit ue = layoutModel.getUndoableEdit();
            boolean autoUndo = true;
            try {
                layoutModel.changeContainerToComponent(metacont.getId());
                autoUndo = false;
            } finally {
                if (layoutStartMark != null && !layoutStartMark.equals(layoutModel.getChangeMark())) {
                    addUndoableEdit(ue);
                }
                if (autoUndo) {
                    forceUndoOfCompoundEdit();
                }
            }
        }
    }
    
    void setNaturalContainerLayoutImpl(RADVisualContainer metacont) {
        LayoutSupportDelegate currentDel = metacont.getLayoutSupport().getLayoutDelegate();
        metacont.setOldLayoutSupport(false);
        fireContainerLayoutExchanged(metacont, currentDel, null);
        for (RADVisualComponent metacomp : metacont.getSubComponents()) {
            metacomp.resetConstraintsProperties();
        }
    }

    public void setNaturalContainerLayout(RADVisualContainer metacont) {
        LayoutSupportManager currentLS = metacont.getLayoutSupport();
        if (currentLS == null)
            return; // already set (no old layout support)
        
        setNaturalContainerLayoutImpl(metacont);
        Object layoutStartMark = layoutModel.getChangeMark();
        UndoableEdit ue = layoutModel.getUndoableEdit();
        boolean autoUndo = true;
        try {
            if (!layoutModel.changeComponentToContainer(metacont.getId())) {
                layoutModel.addRootComponent(
                        new LayoutComponent(metacont.getId(), true));
            }
            autoUndo = false;
        } finally {
            if (layoutStartMark != null && !layoutStartMark.equals(layoutModel.getChangeMark())) {
                addUndoableEdit(ue);
            }
            if (autoUndo) {
                forceUndoOfCompoundEdit();
            }
        }
    }

    public void removeComponent(RADComponent metacomp, boolean fromModel) {
        Object layoutStartMark = null;
        UndoableEdit ue = null;
        boolean autoUndo = true;
        try {
            if (fromModel && (layoutModel != null)) {
                layoutStartMark = layoutModel.getChangeMark();
                ue = layoutModel.getUndoableEdit();
                layoutModel.removeComponent(metacomp.getId(), true);
                removeLayoutComponentsRecursively(metacomp);
            }

            // [TODO need effective multi-component remove from LayoutModel (start in ComponentInspector.DeleteActionPerformer)]
            autoUndo = false;
        } finally {
            removeComponentImpl(metacomp, fromModel);
            if (layoutStartMark != null && !layoutStartMark.equals(layoutModel.getChangeMark())) {
                addUndoableEdit(ue); // is added to a compound edit
            }
            if (autoUndo) {
                forceUndoOfCompoundEdit();
            }
        }
    }

    void removeComponentImpl(RADComponent metacomp, boolean fromModel) {
        if (fromModel && formEvents != null) {
            removeEventHandlersRecursively(metacomp);
        }

        RADComponent parent = metacomp.getParentComponent();
        ComponentContainer parentContainer =
            parent instanceof ComponentContainer ?
                (ComponentContainer) parent : getModelContainer();

        int index = parentContainer.getIndexOf(metacomp);
        parentContainer.remove(metacomp);

        if (fromModel) {
            setInModelRecursively(metacomp, false);
        }

        FormModelEvent ev = fireComponentRemoved(metacomp, parentContainer, index, fromModel);
    }

    // needed for the case of mixed hierarchy of new/old layout support
    private void removeLayoutComponentsRecursively(RADComponent metacomp) {
        if (metacomp instanceof ComponentContainer) {
            RADComponent[] comps = ((ComponentContainer)metacomp).getSubBeans();
            for (int i=0; i<comps.length; i++) {
                removeLayoutComponentsRecursively(comps[i]);
            }
        }
        LayoutComponent layoutComp = layoutModel == null ? null : layoutModel.getLayoutComponent(metacomp.getId());
        if (layoutComp != null && layoutComp.getParent() == null) {
            // remove only root components
            layoutModel.removeComponent(layoutComp.getId(), true);
        }
    }

    void updateMapping(RADComponent metacomp, boolean register) {
        if (register)
            idToComponents.put(metacomp.getId(), metacomp);
        else
            idToComponents.remove(metacomp.getId());
    }

    // removes all event handlers attached to given component and all
    // its subcomponents
    private void removeEventHandlersRecursively(RADComponent comp) {
        if (comp instanceof ComponentContainer) {
            RADComponent[] subcomps = ((ComponentContainer)comp).getSubBeans();
            for (int i=0; i<subcomps.length; i++)
                removeEventHandlersRecursively(subcomps[i]);
        }

        Event[] events = comp.getKnownEvents();
        for (int i=0; i < events.length; i++)
            if (events[i].hasEventHandlers())
                getFormEvents().detachEvent(events[i]);
    }

    static void setInModelRecursively(RADComponent metacomp, boolean inModel) {
        if (metacomp instanceof ComponentContainer) {
            RADComponent[] comps = ((ComponentContainer)metacomp).getSubBeans();
            for (int i=0; i < comps.length; i++)
                setInModelRecursively(comps[i], inModel);
        }
        metacomp.setInModel(inModel);
    }

    // ----------
    // undo and redo

    public void setUndoRedoRecording(boolean record) {
        t("turning undo/redo recording "+(record?"on":"off")); // NOI18N
        undoRedoRecording = record;
    }

    public boolean isUndoRedoRecording() {
        return undoRedoRecording;
    }

    private void startCompoundEdit() {
        if (compoundEdit == null) {
            t("starting compound edit"); // NOI18N
            compoundEdit = new CompoundEdit();
        }
    }

    private static boolean formModifiedLogged = false;
    public CompoundEdit endCompoundEdit(boolean commit) {
        if (compoundEdit != null) {
            t("ending compound edit: "+commit); // NOI18N
            compoundEdit.end();
            if (commit && undoRedoRecording && compoundEdit.isSignificant()) {
                if (!formModifiedLogged) {
                    Logger logger = Logger.getLogger("org.netbeans.ui.metrics.form"); // NOI18N
                    LogRecord rec = new LogRecord(Level.INFO, "USG_FORM_MODIFIED"); // NOI18N
                    rec.setLoggerName(logger.getName());
                    logger.log(rec);
                    formModifiedLogged = true;
                }
                getUndoRedoManager().undoableEditHappened(
                    new UndoableEditEvent(this, compoundEdit));
            }
            CompoundEdit edit = compoundEdit;
            compoundEdit = null;
            return edit;
        }
        return null;
    }
    
    public void forceUndoOfCompoundEdit() {
        if (compoundEdit != null) {
            undoCompoundEdit = true;
        }
    }

    public boolean isCompoundEditInProgress() {
        return compoundEdit != null; // && compoundEdit.isInProgress();
    }

    public void addUndoableEdit(UndoableEdit edit) {
        t("adding undoable edit"); // NOI18N
        if (!isCompoundEditInProgress()) {
            startCompoundEdit();
        }
        compoundEdit.addEdit(edit);
    }

    public UndoRedo.Manager getUndoRedoManager() {
//        if (undoRedoManager == null) {
//            undoRedoManager = new UndoRedoManager();
//            undoRedoManager.setLimit(50);
//        }
        return undoRedoManager;
    }

    // [Undo manager performing undo/redo in AWT event thread should not be
    //  probably implemented here - in FormModel - but seperately.]
    public class UndoRedoManager extends UndoRedo.Manager {
        private Mutex.ExceptionAction<Object> runUndo = new Mutex.ExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
                superUndo();
                return null;
            }
        };
        private Mutex.ExceptionAction<Object> runRedo = new Mutex.ExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
                superRedo();
                return null;
            }
        };

        private boolean undoInProgress;
        private boolean redoInProgress;
        public boolean isUndoInProgress() {
            return undoInProgress;
        }
        public boolean isRedoInProgress() {
            return redoInProgress;
        }

        public void superUndo() throws CannotUndoException {
            undoInProgress = true;
            try {
                super.undo();
            } finally {
                undoInProgress = false;
            }
        }
        public void superRedo() throws CannotRedoException {
            redoInProgress = true;
            try {
                super.redo();
            } finally {
                redoInProgress = false;
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            if (java.awt.EventQueue.isDispatchThread()) {
                superUndo();
            }
            else {
                try {
                    Mutex.EVENT.readAccess(runUndo);
                }
                catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof CannotUndoException)
                        throw (CannotUndoException) e;
                    else // should not happen, ignore
                        e.printStackTrace();
                }
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            if (java.awt.EventQueue.isDispatchThread()) {
                superRedo();
            }
            else {
                try {
                    Mutex.EVENT.readAccess(runRedo);
                }
                catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof CannotRedoException)
                        throw (CannotRedoException) e;
                    else // should not happen, ignore
                        e.printStackTrace();
                }
            }
        }
    }

    // ----------
    // listeners registration, firing methods

    public synchronized void addFormModelListener(FormModelListener l) {
        if (listeners == null)
            listeners = new ArrayList<FormModelListener>();
        listeners.add(l);
    }

    public synchronized void removeFormModelListener(FormModelListener l) {
        if (listeners != null)
            listeners.remove(l);
    }

    /** Fires an event informing about that the form has been just loaded. */
    public void fireFormLoaded() {
        t("firing form loaded"); // NOI18N

        formLoaded = true;
//        if (undoRedoManager != null)
//            undoRedoManager.discardAllEdits();
        if (!readOnly && !Boolean.getBoolean("netbeans.form.no_undo")) { // NOI18N
            undoRedoManager = new UndoRedoManager();
            undoRedoManager.setLimit(50);
            setUndoRedoRecording(true);
            if (layoutModel != null)
                layoutModel.setChangeRecording(true);
        }
//        initializeCodeGenerator(); // [should go away]

        sendEventLater(new FormModelEvent(this, FormModelEvent.FORM_LOADED));
    }

    /** Fires an event informing about that the form is just about to be saved. */
    public void fireFormToBeSaved() {
        t("firing form to be saved"); // NOI18N

        sendEventImmediately(
            new FormModelEvent(this, FormModelEvent.FORM_TO_BE_SAVED));
    }

    /** Fires an event informing about that the form is just about to be closed. */
    public void fireFormToBeClosed() {
        t("firing form to be closed"); // NOI18N

        if (undoRedoManager != null)
            undoRedoManager.discardAllEdits();

        sendEventImmediately(
            new FormModelEvent(this, FormModelEvent.FORM_TO_BE_CLOSED));
    }

    /** Fires an event informing about changing layout manager of a container.
     * An undoable edit is created and registered automatically.
     * 
     * @param metacont container whose layout has been changed.
     * @param oldLayout old layout.
     * @param newLayout new layout.
     * @return event that has been fired.
     */
    public FormModelEvent fireContainerLayoutExchanged(
                              RADVisualContainer metacont,
                              LayoutSupportDelegate oldLayout,
                              LayoutSupportDelegate newLayout)
    {
        t("firing container layout exchange, container: " // NOI18N
          + (metacont != null ? metacont.getName() : "null")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.CONTAINER_LAYOUT_EXCHANGED);
        ev.setLayout(metacont, oldLayout, newLayout);
        sendEvent(ev);

        if (undoRedoRecording && metacont != null && oldLayout != newLayout)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about changing a property of container layout.
     * An undoable edit is created and registered automatically.
     * 
     * @param metacont container whose layout has been changed.
     * @param propName name of the layout property.
     * @param oldValue old value of the property.
     * @param newValue new value of the property.
     * @return event that has been fired.
     */
    public FormModelEvent fireContainerLayoutChanged(
                              RADVisualContainer metacont,
                              String propName,
                              Object oldValue,
                              Object newValue)
    {
        t("firing container layout change, container: " // NOI18N
          + (metacont != null ? metacont.getName() : "null") // NOI18N
          + ", property: " + propName); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.CONTAINER_LAYOUT_CHANGED);
        ev.setComponentAndContainer(metacont, metacont);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording
            && metacont != null && (propName == null || oldValue != newValue))
        {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /** Fires an event informing about changing a property of component layout
     * constraints. An undoable edit is created and registered automatically.
     * 
     * @param metacomp component whose layout property has been changed.
     * @param propName name of the layout property.
     * @param oldValue old value of the property.
     * @param newValue new value of the property.
     * @return event that has been fired.
     */
    public FormModelEvent fireComponentLayoutChanged(
                              RADVisualComponent metacomp,
                              String propName,
                              Object oldValue,
                              Object newValue)
    {
        t("firing component layout change: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENT_LAYOUT_CHANGED);
        ev.setComponentAndContainer(metacomp, null);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording
            && metacomp != null && propName != null && oldValue != newValue)
        {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /** Fires an event informing about adding a component to the form.
     * An undoable edit is created and registered automatically.
     * 
     * @param metacomp component that has been added.
     * @param addedNew is newly added?
     * @return event that has been fired.
     */
    public FormModelEvent fireComponentAdded(RADComponent metacomp,
                                             boolean addedNew)
    {
        t("firing component added: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENT_ADDED);
        ev.setAddData(metacomp, null, addedNew);
        sendEvent(ev);

        if (undoRedoRecording && metacomp != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about removing a component from the form.
     * An undoable edit is created and registered automatically.
     * 
     * @param metacomp component that has been removed.
     * @param metacont container from which the component was removed.
     * @param index index of the component in the container.
     * @param removedFromModel determines whether the component has been
     * removed from the model.
     * @return event that has been fired.
     */
    public FormModelEvent fireComponentRemoved(RADComponent metacomp,
                                               ComponentContainer metacont,
                                               int index,
                                               boolean removedFromModel)
    {
        t("firing component removed: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENT_REMOVED);
        ev.setRemoveData(metacomp, metacont, index, removedFromModel);
        sendEvent(ev);

        if (undoRedoRecording && metacomp != null && metacont != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about reordering components in a container.
     * An undoable edit is created and registered automatically.
     * 
     * @param metacont container whose subcomponents has been reordered.
     * @param perm permutation describing the change in order.
     * @return event that has been fired.
     */
    public FormModelEvent fireComponentsReordered(ComponentContainer metacont,
                                                  int[] perm)
    {
        t("firing components reorder in container: " // NOI18N
          + (metacont instanceof RADComponent ?
             ((RADComponent)metacont).getName() : "<top>")); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENTS_REORDERED);
        ev.setComponentAndContainer(null, metacont);
        ev.setReordering(perm);
        sendEvent(ev);

        if (undoRedoRecording && metacont != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about changing a property of a component.
     * An undoable edit is created and registered automatically.
     * @param metacomp component whose property has been changed.
     * @param propName name of the changed property.
     * @param oldValue old value of the property.
     * @param newValue new value of the property.
     * @return event that has been fired.
     */
    public FormModelEvent fireComponentPropertyChanged(RADComponent metacomp,
                                                       String propName,
                                                       Object oldValue,
                                                       Object newValue)
    {
        t("firing component property change, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "<null component>") // NOI18N
          + ", property: " + propName); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.COMPONENT_PROPERTY_CHANGED);
        ev.setComponentAndContainer(metacomp, null);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording
            && metacomp != null && propName != null && oldValue != newValue)
        {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    private static boolean bindingModifiedLogged = false;
    public FormModelEvent fireBindingChanged(RADComponent metacomp,
                                             String path,
                                             String subProperty,
                                             Object oldValue,
                                             Object newValue)
    {
        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.BINDING_PROPERTY_CHANGED);
        ev.setComponentAndContainer(metacomp, null);
        ev.setProperty(path, oldValue, newValue);
        ev.setSubProperty(subProperty);
        sendEvent(ev);

        if (undoRedoRecording && oldValue != newValue) {
            if (!bindingModifiedLogged) {
                Logger logger = Logger.getLogger("org.netbeans.ui.metrics.form"); // NOI18N
                LogRecord rec = new LogRecord(Level.INFO, "USG_FORM_BINDING_MODIFIED"); // NOI18N
                rec.setLoggerName(logger.getName());
                logger.log(rec);
                bindingModifiedLogged = true;
            }
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /** Fires an event informing about changing a synthetic property of
     * a component. An undoable edit is created and registered automatically.
     * 
     * @param metacomp component whose synthetic property has been changed.
     * @param propName name of the synthetic property that has been changed.
     * @param oldValue old value of the property.
     * @param newValue new value of the property.
     * @return event that has been fired.
     */
    public FormModelEvent fireSyntheticPropertyChanged(RADComponent metacomp,
                                                       String propName,
                                                       Object oldValue,
                                                       Object newValue)
    {
        t("firing synthetic property change, component: " // NOI18N
          + (metacomp != null ? metacomp.getName() : "null") // NOI18N
          + ", property: " + propName); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.SYNTHETIC_PROPERTY_CHANGED);
        ev.setComponentAndContainer(metacomp, null);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording && propName != null && oldValue != newValue)
        {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /** Fires an event informing about attaching a new event to an event handler
     * (createdNew parameter indicates whether the event handler was created
     * first). An undoable edit is created and registered automatically.
     * 
     * @param event event for which the handler was created.
     * @param handler name of the event handler.
     * @param bodyText body of the event handler.
     * @param createdNew newly created event handler?
     * @return event that has been fired.
     */
    public FormModelEvent fireEventHandlerAdded(Event event,
                                                String handler,
                                                String bodyText,
                                                String annotationText,
                                                boolean createdNew)
    {
        t("event handler added: "+handler); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.EVENT_HANDLER_ADDED);
        ev.setEvent(event, handler, bodyText, annotationText, createdNew);
        sendEvent(ev);

        if (undoRedoRecording && event != null && handler != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about detaching an event from event handler
     * (handlerDeleted parameter indicates whether the handler was deleted as
     * the last event was detached). An undoable edit is created and registered
     * automatically.
     * 
     * @param event event for which the handler was removed.
     * @param handler removed event handler.
     * @param handlerDeleted was deleted?
     * @return event that has been fired.
     */
    public FormModelEvent fireEventHandlerRemoved(Event event,
                                                  String handler,
                                                  boolean handlerDeleted)
    {
        t("firing event handler removed: "+handler); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.EVENT_HANDLER_REMOVED);
        ev.setEvent(event, handler, null, null, handlerDeleted);
        sendEvent(ev);

        if (undoRedoRecording && event != null && handler != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about renaming an event handler. An undoable
     * edit is created and registered automatically.
     * 
     * @param oldHandlerName old name of the event handler.
     * @param newHandlerName new name of the event handler.
     * @return event that has been fired.
     */
    public FormModelEvent fireEventHandlerRenamed(String oldHandlerName,
                                                  String newHandlerName)
    {
        t("event handler renamed: "+oldHandlerName+" to "+newHandlerName); // NOI18N

        FormModelEvent ev =
            new FormModelEvent(this, FormModelEvent.EVENT_HANDLER_RENAMED);
        ev.setEvent(oldHandlerName, newHandlerName);
        sendEvent(ev);

        if (undoRedoRecording && oldHandlerName != null && newHandlerName != null)
            addUndoableEdit(ev.getUndoableEdit());

        return ev;
    }

    /** Fires an event informing about general form change.
     * 
     * @param immediately determines whether the change should be fire immediately.
     * @return event that has been fired.
     */
    public FormModelEvent fireFormChanged(boolean immediately) {
        t("firing form change"); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.OTHER_CHANGE);
        if (immediately)
            sendEventImmediately(ev);
        else
            sendEvent(ev);

        return ev;
    }

    // ---------
    // firing methods for batch event processing

    private void sendEvent(FormModelEvent ev) {
        if (formLoaded) {
            if (eventList != null || ev.isModifying()) {
                sendEventLater(ev);
            } else {
                sendEventImmediately(ev);
            }
        } else {
            fireEvents(ev);
        }
    }

    private void sendEventLater(FormModelEvent ev) {
        // works properly only if called from AWT event dispatch thread
        if (!java.awt.EventQueue.isDispatchThread()) {
            sendEventImmediately(ev);
            return;
        }

        synchronized (this) {
            if (eventList == null) {
                eventList = new ArrayList<FormModelEvent>();
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        firePendingEvents();
                    }
                });
            }
            eventList.add(ev);
        }
    }

    private void sendEventImmediately(FormModelEvent ev) {
        synchronized (this) {
            if (eventList == null) {
                eventList = new ArrayList<FormModelEvent>();
            }
            eventList.add(ev);
        }
        firePendingEvents();
    }

    private void firePendingEvents() {
        List<FormModelEvent> list = pickUpEvents();
        if (list != null && !list.isEmpty()) {
            FormModelEvent[] events = new FormModelEvent[list.size()];
            list.toArray(events);
            fireEventBatch(events);
        }
    }

    private synchronized List<FormModelEvent> pickUpEvents() {
        List<FormModelEvent> list = eventList;
        eventList = null;
        return list;
    }

    boolean hasPendingEvents() {
        return eventList != null;
    }

     /**
     * This method fires events collected from all changes done during the last
     * round of AWT event queue. After all fired successfully (no error occurred),
     * all the changes are placed as one UndoableEdit into undo/redo queue. When
     * the fired events are being processed, some more changes may happen (they
     * are included in the same UndoableEdit). These changes are typically fired
     * immediately causing this method is re-entered while previous firing is not
     * finished yet.
     * Additionally - for robustness, if some unhandled error happens before or
     * during firing the events, all the changes done so far are undone:
     * If an operation failed before firing, the undoCompoundEdit field is set
     * and then no events are fired at all (the changes were defective), and the
     * changes done before the failure are undone. All the changes are undone
     * also if the failure happens during processing the events (e.g. the layout
     * can't be built).
     */
    private void fireEventBatch(FormModelEvent ... events) {
        if (!firing) {
            boolean firingFailed = false;
            try {
                firing = true;
                if (!undoCompoundEdit) {
                    firingFailed = true;
                    fireEvents(events);
                    firingFailed = false;
                }
            } finally {
                firing = false;
                boolean revert = undoCompoundEdit || firingFailed;
                undoCompoundEdit = false;
                CompoundEdit edit = endCompoundEdit(!revert);
                if (edit != null && revert) {
                    edit.undo();
                }
            }
        } else { // re-entrant call
            fireEvents(events);
        }
    }

    void fireEvents(FormModelEvent ... events) {
        java.util.List targets;
        synchronized(this) {
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            targets = (ArrayList) listeners.clone();
        }
        for (int i=0; i < targets.size(); i++) {
            FormModelListener l = (FormModelListener) targets.get(i);
            l.formChanged(events);
        }
    }

    // -------------

    public CodeStructure getCodeStructure() {
        return codeStructure;
    }
    
    public boolean isFreeDesignDefaultLayout() {
        return freeDesignDefaultLayout;
    }
    
    void setFreeDesignDefaultLayout(boolean freeDesignDefaultLayout) {
        this.freeDesignDefaultLayout = freeDesignDefaultLayout;
    }

//    CodeGenerator getCodeGenerator() {
////        return FormEditorSupport.getCodeGenerator(this);
//        if (codeGenerator == null)
//            codeGenerator = new JavaCodeGenerator();
//        return codeGenerator;
//    }
//
//    void initializeCodeGenerator() {
//        getCodeGenerator().initialize(this);
//    }

    // ---------------
    // ModelContainer innerclass

    final class ModelContainer implements ComponentContainer {
        @Override
        public RADComponent[] getSubBeans() {
            int n = otherComponents.size();
            if (topRADComponent != null)
                n++;
            RADComponent[] comps = new RADComponent[n];
            otherComponents.toArray(comps);
            if (topRADComponent != null)
                comps[n-1] = topRADComponent;
            return comps;
        }

        @Override
        public void initSubComponents(RADComponent[] initComponents) {
            otherComponents.clear();
            for (int i = 0; i < initComponents.length; i++)
                if (initComponents[i] != topRADComponent)
                    otherComponents.add(initComponents[i]);
        }

        @Override
        public void reorderSubComponents(int[] perm) {
            RADComponent[] components = new RADComponent[otherComponents.size()];
            for (int i=0; i < perm.length; i++)
                components[perm[i]] = otherComponents.get(i);

            otherComponents.clear();
            otherComponents.addAll(Arrays.asList(components));
        }

        @Override
        public void add(RADComponent comp) {
            comp.setParentComponent(null);
            otherComponents.add(comp);
        }

        @Override
        public void remove(RADComponent comp) {
            if (otherComponents.remove(comp))
                comp.setParentComponent(null);
        }

        @Override
        public int getIndexOf(RADComponent comp) {
            int index = otherComponents.indexOf(comp);
            if (index < 0 && comp == topRADComponent)
                index = otherComponents.size();
            return index;
        }
    }

    // ---------------

    /** For debugging purposes only. */
    static private int traceCount = 0;
    /** For debugging purposes only. */
    static private final boolean TRACE = false;
    /** For debugging purposes only. */
    static void t(String str) {
        if (TRACE)
            if (str != null)
                System.out.println("FormModel "+(++traceCount)+": "+str); // NOI18N
            else
                System.out.println(""); // NOI18N
    }
}
