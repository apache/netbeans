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

package org.netbeans.modules.form;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.*;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;
import org.openide.ErrorManager;

/**
 * Describes single change in FormModel. Provides UndoableEdit capable to
 * undo/redo the change.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class FormModelEvent extends EventObject
{
    // possible types of changes
    public static final int FORM_LOADED = 1;
    public static final int FORM_TO_BE_SAVED = 2;
    public static final int FORM_TO_BE_CLOSED = 3;
    public static final int CONTAINER_LAYOUT_EXCHANGED = 4;
    public static final int CONTAINER_LAYOUT_CHANGED = 5;
    public static final int COMPONENT_LAYOUT_CHANGED = 6;
    public static final int COMPONENT_ADDED = 7;
    public static final int COMPONENT_REMOVED = 8;
    public static final int COMPONENTS_REORDERED = 9;
    public static final int COMPONENT_PROPERTY_CHANGED = 10;
    public static final int BINDING_PROPERTY_CHANGED = 16;
    public static final int SYNTHETIC_PROPERTY_CHANGED = 11;
    public static final int EVENT_HANDLER_ADDED = 12;
    public static final int EVENT_HANDLER_REMOVED = 13;
    public static final int EVENT_HANDLER_RENAMED = 14;
    public static final int OTHER_CHANGE = 15;

    // data about the change
    private int changeType;

    private boolean createdDeleted;
    private RADComponent component;
    private ComponentContainer container;
    private LayoutConstraints constraints;
    private int componentIndex = -1;
    private int[] reordering;
    private String propertyName;
    private String subPropertyName;
    private Object oldPropertyValue;
    private Object newPropertyValue;
    private Event componentEvent;

    private UndoableEdit undoableEdit;

    // -----------

    private FormModelEvent additionalEvent; 
    private static List<FormModelEvent> interestList; // events interested in additional events

    // -----------

    FormModelEvent(FormModel source, int changeType) {
        super(source);
        this.changeType = changeType;
        informInterestedEvents(this);
    }

    void setProperty(String propName, Object oldValue, Object newValue) {
        propertyName = propName;
        oldPropertyValue = oldValue;
        newPropertyValue = newValue;
    }

    void setSubProperty(String subPropertyName) {
        this.subPropertyName = subPropertyName;
    }

    void setComponentAndContainer(RADComponent metacomp,
                                  ComponentContainer metacont)
    {
        component = metacomp;
        container = metacont != null ? metacont : deriveContainer(metacomp);
    }

    void setLayout(RADVisualContainer metacont,
                   LayoutSupportDelegate oldLayoutSupp,
                   LayoutSupportDelegate newLayoutSupp)
    {
        component = metacont;
        container = metacont;
        oldPropertyValue = oldLayoutSupp;
        newPropertyValue = newLayoutSupp;
    }

    void setReordering(int[] perm) {
        reordering = perm;
    }

    void setAddData(RADComponent metacomp,
                    ComponentContainer metacont,
                    boolean addedNew)
    {
        setComponentAndContainer(metacomp, metacont);
        createdDeleted = addedNew;

        if (component instanceof RADVisualComponent
            && container instanceof RADVisualContainer)
        {
            componentIndex = container.getIndexOf(component);
            if (componentIndex >= 0) {
                LayoutSupportManager laysup =
                    ((RADVisualContainer)container).getLayoutSupport();
                if (laysup != null)
                    constraints = laysup.getConstraints(componentIndex);
            }
        }
    }

    void setRemoveData(RADComponent metacomp,
                       ComponentContainer metacont,
                       int index,
                       boolean removedFromModel)
    {
        component = metacomp;
        container = metacont;
        componentIndex = index;
        createdDeleted = removedFromModel;

        if (metacomp instanceof RADVisualComponent
            && metacont instanceof RADVisualContainer)
        {
            LayoutSupportManager laysup =
                ((RADVisualContainer)metacont).getLayoutSupport();
            constraints = laysup == null ? null :
                laysup.getStoredConstraints((RADVisualComponent)metacomp);
        }
    }

    void setEvent(Event event, // may be null if the handler is just updated
                  String handler,
                  String bodyText,
                  String annotationText,
                  boolean createdNew)
    {
        if (event != null)
            component = event.getComponent();
        componentEvent = event;
        propertyName = handler;
        oldPropertyValue = bodyText;
        newPropertyValue = annotationText;
        createdDeleted = createdNew;
    }

    void setEvent(String oldHandlerName, String newHandlerName) {
        oldPropertyValue = oldHandlerName;
        propertyName = newHandlerName;
        newPropertyValue = newHandlerName;
    }

    void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    private static ComponentContainer deriveContainer(RADComponent comp) {
        if (comp == null)
            return null;
        if (comp.getParentComponent() instanceof ComponentContainer)
            return (ComponentContainer) comp.getParentComponent();
        else if (comp.getParentComponent() == null)
            return comp.getFormModel().getModelContainer();
        return null;
    }

    // -------

    public final FormModel getFormModel() {
        return (FormModel) getSource();
    }

    public final int getChangeType() {
        return changeType;
    }

    public final boolean isModifying() {
        return changeType != FORM_LOADED
               && changeType != FORM_TO_BE_SAVED
               && changeType != FORM_TO_BE_CLOSED
               && (changeType != EVENT_HANDLER_ADDED || componentEvent != null);
    }

    public final boolean getCreatedDeleted() {
        return createdDeleted;
    }

    public final ComponentContainer getContainer() {
        return container;
    }

    public final RADComponent getComponent() {
        return component;
    }

    public final LayoutConstraints getComponentLayoutConstraints() {
        return constraints;
    }

    public final int getComponentIndex() {
        return componentIndex;
    }

    public final String getPropertyName() {
        return propertyName;
    }

    public final String getSubPropertyName() {
        return subPropertyName;
    }

    public final RADProperty getComponentProperty() {
        return component != null && propertyName != null ?
            component.getBeanProperty(propertyName) : null;
    }

    public final Object getOldPropertyValue() {
        return oldPropertyValue;
    }

    public final Object getNewPropertyValue() {
        return newPropertyValue;
    }

    public final MetaBinding getOldBinding() {
        return (MetaBinding) oldPropertyValue;
    }

    public final MetaBinding getNewBinding() {
        return (MetaBinding) newPropertyValue;
    }

    public final LayoutSupportDelegate getOldLayoutSupport() {
        return (LayoutSupportDelegate) oldPropertyValue;
    }

    public final LayoutSupportDelegate getNewLayoutSupport() {
        return (LayoutSupportDelegate) newPropertyValue;
    }

    public final int[] getReordering() {
        return reordering;
    }

    public final Event getComponentEvent() {
        return componentEvent;
    }

    public final String getEventHandler() {
        return propertyName;
    }

    public final String getOldEventHandler() {
        return (String) oldPropertyValue;
    }

    public final String getNewEventHandler() {
        return (String) newPropertyValue;
    }

    public final String getEventHandlerContent() {
        if (changeType == EVENT_HANDLER_ADDED
            || changeType == EVENT_HANDLER_REMOVED)
        {
            if (additionalEvent != null) {
                if (additionalEvent.changeType == EVENT_HANDLER_REMOVED
                    || additionalEvent.changeType == EVENT_HANDLER_ADDED)
                {
                    oldPropertyValue = additionalEvent.oldPropertyValue;
                }
                additionalEvent = null;
            }
            return (String) oldPropertyValue;
        }
        return null;
    }

    public final void setEventHandlerContent(String text) {
        if (changeType == EVENT_HANDLER_ADDED
                || changeType == EVENT_HANDLER_REMOVED)
            oldPropertyValue = text;
    }

    public final String getEventHandlerAnnotation() {
        if (changeType == EVENT_HANDLER_ADDED || changeType == EVENT_HANDLER_REMOVED) {
            if (additionalEvent != null) {
                if (additionalEvent.changeType == EVENT_HANDLER_REMOVED || additionalEvent.changeType == EVENT_HANDLER_ADDED) {
                    newPropertyValue = additionalEvent.newPropertyValue;
                }
                additionalEvent = null;
            }
            return (String)newPropertyValue;
        }
        return null;
    }

    public final void setEventHandlerAnnotation(String text) {
        if (changeType == EVENT_HANDLER_ADDED || changeType == EVENT_HANDLER_REMOVED) {
            newPropertyValue = text;
        }
    }

    // ----------

    UndoableEdit getUndoableEdit() {
        if (undoableEdit == null)
            undoableEdit = new FormUndoableEdit();
        return undoableEdit;
    }

    // ----------
    // methods for events interested in additional events occured
    // (used for undo/redo processing of event handlers)

    private static void addToInterestList(FormModelEvent ev) {
        if (interestList == null) {
            interestList = new ArrayList<FormModelEvent>();
        } else {
            interestList.remove(ev);
        }

        interestList.add(ev);
    }

    private static void removeFromInterestList(FormModelEvent ev) {
        if (interestList != null)
            interestList.remove(ev);
    }

    private static void informInterestedEvents(FormModelEvent newEvent) {
        if (interestList != null)
            for (Iterator it=interestList.iterator(); it.hasNext(); )
                ((FormModelEvent)it.next()).newEventCreated(newEvent);
    }

    private void newEventCreated(FormModelEvent newEvent) {
        additionalEvent = newEvent;
    }

    // ----------

    private class FormUndoableEdit extends AbstractUndoableEdit {
        @Override
        public void undo() throws CannotUndoException {
            super.undo();

            // turn off undo/redo monitoring in FormModel while undoing!
            boolean undoRedoOn = getFormModel().isUndoRedoRecording();
            if (undoRedoOn)
                getFormModel().setUndoRedoRecording(false);

            switch(changeType) {
                case CONTAINER_LAYOUT_EXCHANGED:
                    FormModel.t("UNDO: container layout change"); // NOI18N
                    undoContainerLayoutExchange();
                    break;
                case CONTAINER_LAYOUT_CHANGED:
                    FormModel.t("UNDO: container layout property change"); // NOI18N
                    undoContainerLayoutChange();
                    break;
                case COMPONENT_LAYOUT_CHANGED:
                    FormModel.t("UNDO: component layout constraints change"); // NOI18N
                    undoComponentLayoutChange();
                    break;
                case COMPONENTS_REORDERED:
                    FormModel.t("UNDO: components reorder"); // NOI18N
                    undoComponentsReorder();
                    break;
                case COMPONENT_ADDED:
                    FormModel.t("UNDO: component addition"); // NOI18N
                    undoComponentAddition();
                    break;
                case COMPONENT_REMOVED:
                    FormModel.t("UNDO: component removal"); // NOI18N
                    undoComponentRemoval();
                    break;
                case COMPONENT_PROPERTY_CHANGED:
                    FormModel.t("UNDO: component property change"); // NOI18N
                    undoComponentPropertyChange();
                    break;
                case SYNTHETIC_PROPERTY_CHANGED:
                    FormModel.t("UNDO: synthetic property change"); // NOI18N
                    undoSyntheticPropertyChange();
                    break;
                case EVENT_HANDLER_ADDED:
                    FormModel.t("UNDO: event handler addition"); // NOI18N
                    undoEventHandlerAddition();
                    break;
                case EVENT_HANDLER_REMOVED:
                    FormModel.t("UNDO: event handler removal"); // NOI18N
                    undoEventHandlerRemoval();
                    break;
                case EVENT_HANDLER_RENAMED:
                    FormModel.t("UNDO: event handler renaming"); // NOI18N
                    undoEventHandlerRenaming();
                    break;
                case BINDING_PROPERTY_CHANGED:
                    FormModel.t("UNDO: binding property change"); // NOI18N
                    undoBindingPropertyChange();
                    break;                    

                default: FormModel.t("UNDO: "+changeType); // NOI18N
                         break;
            }

            if (undoRedoOn) // turn on undo/redo monitoring again
                getFormModel().setUndoRedoRecording(true);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();

            // turn off undo/redo monitoring in FormModel while redoing!
            boolean undoRedoOn = getFormModel().isUndoRedoRecording();
            if (undoRedoOn)
                getFormModel().setUndoRedoRecording(false);

            switch(changeType) {
                case CONTAINER_LAYOUT_EXCHANGED:
                    FormModel.t("REDO: container layout change"); // NOI18N
                    redoContainerLayoutExchange();
                    break;
                case CONTAINER_LAYOUT_CHANGED:
                    FormModel.t("REDO: container layout property change"); // NOI18N
                    redoContainerLayoutChange();
                    break;
                case COMPONENT_LAYOUT_CHANGED:
                    FormModel.t("REDO: component layout constraints change"); // NOI18N
                    redoComponentLayoutChange();
                    break;
                case COMPONENTS_REORDERED:
                    FormModel.t("REDO: components reorder"); // NOI18N
                    redoComponentsReorder();
                    break;
                case COMPONENT_ADDED:
                    FormModel.t("REDO: component addition"); // NOI18N
                    redoComponentAddition();
                    break;
                case COMPONENT_REMOVED:
                    FormModel.t("REDO: component removal"); // NOI18N
                    redoComponentRemoval();
                    break;
                case COMPONENT_PROPERTY_CHANGED:
                    FormModel.t("REDO: component property change"); // NOI18N
                    redoComponentPropertyChange();
                    break;
                case SYNTHETIC_PROPERTY_CHANGED:
                    FormModel.t("REDO: synthetic property change"); // NOI18N
                    redoSyntheticPropertyChange();
                    break;
                case EVENT_HANDLER_ADDED:
                    FormModel.t("REDO: event handler addition"); // NOI18N
                    redoEventHandlerAddition();
                    break;
                case EVENT_HANDLER_REMOVED:
                    FormModel.t("REDO: event handler removal"); // NOI18N
                    redoEventHandlerRemoval();
                    break;
                case EVENT_HANDLER_RENAMED:
                    FormModel.t("REDO: event handler renaming"); // NOI18N
                    redoEventHandlerRenaming();
                    break;
               case BINDING_PROPERTY_CHANGED:
                    FormModel.t("REDO: binding property change"); // NOI18N
                    redoBindingPropertyChange();
                    break;

                default: FormModel.t("REDO: "+changeType); // NOI18N
                         break;
            }

            if (undoRedoOn) // turn on undo/redo monitoring again
                getFormModel().setUndoRedoRecording(true);
        }

        @Override
        public String getUndoPresentationName() {
            return ""; // NOI18N
        }
        @Override
        public String getRedoPresentationName() {
            return ""; // NOI18N
        }

        // -------------

        private void undoContainerLayoutExchange() {
            try {
                LayoutSupportDelegate layoutDelegate = getOldLayoutSupport();
                if (layoutDelegate != null) {
                    getFormModel().setContainerLayoutImpl(
                            (RADVisualContainer)getContainer(), layoutDelegate);
                }
                else {
                    getFormModel().setNaturalContainerLayoutImpl(
                        (RADVisualContainer)getContainer());
                }
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        private void redoContainerLayoutExchange() {
            try {
                LayoutSupportDelegate layoutDelegate = getNewLayoutSupport();
                if (layoutDelegate != null) {
                    getFormModel().setContainerLayoutImpl(
                            (RADVisualContainer)getContainer(), layoutDelegate);
                }
                else {
                    getFormModel().setNaturalContainerLayoutImpl(
                            (RADVisualContainer)getContainer());
                }
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        private void undoContainerLayoutChange() {
            RADVisualContainer metacont = (RADVisualContainer) getComponent();
            LayoutSupportManager laysup = metacont.getLayoutSupport();
            if (laysup != null) {
                String propName = getPropertyName();
                if (propName != null) {
                    Node.Property prop = laysup.getLayoutProperty(propName);
                    if (prop != null) {
                        try {
                            prop.setValue(getOldPropertyValue());
                        }
                        catch (Exception ex) { // should not happen
                            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                        }
                    }
                }
            }
            else {
                getFormModel().fireContainerLayoutChanged(metacont, null, null, null);
            }
        }

        private void redoContainerLayoutChange() {
            RADVisualContainer metacont = (RADVisualContainer) getComponent();
            LayoutSupportManager laysup = metacont.getLayoutSupport();
            if (laysup != null) {
                String propName = getPropertyName();
                if (propName != null) {
                    Node.Property prop = laysup.getLayoutProperty(propName);
                    if (prop != null) {
                        try {
                            prop.setValue(getNewPropertyValue());
                        }
                        catch (Exception ex) { // should not happen
                            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                        }
                    }
                }
            }
            else {
                getFormModel().fireContainerLayoutChanged(metacont, null, null, null);
            }
        }

        private void undoComponentLayoutChange() {
            if (getComponent() instanceof RADVisualComponent) {
                ((RADVisualComponent)getComponent()).getConstraintsProperties();
                Node.Property prop =
                    getComponent().getPropertyByName(getPropertyName());
                if (prop != null)
                    try {
                        prop.setValue(getOldPropertyValue());
                    }
                    catch (Exception ex) { // should not happen
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
            }
        }

        private void redoComponentLayoutChange() {
            if (getComponent() instanceof RADVisualComponent) {
                ((RADVisualComponent)getComponent()).getConstraintsProperties();
                Node.Property prop =
                    getComponent().getPropertyByName(getPropertyName());
                if (prop != null)
                    try {
                        prop.setValue(getNewPropertyValue());
                    }
                    catch (Exception ex) { // should not happen
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
            }
        }

        private void undoComponentAddition() {
            removeComponent();
        }

        private void undoComponentRemoval() {
            addComponent();
        }

        private void redoComponentAddition() {
            addComponent();
        }

        private void redoComponentRemoval() {
            removeComponent();
        }

        private void addComponent() {
            RADComponent component = getComponent();
            ComponentContainer container = getContainer();
            RADComponent[] currentSubComps = container.getSubBeans();
            RADComponent[] undoneSubComps =
                new RADComponent[currentSubComps.length+1];

            if (componentIndex < 0)
                componentIndex = currentSubComps.length;

            for (int i=0,j=0; j < undoneSubComps.length; i++,j++) {
                if (i == componentIndex) {
                    undoneSubComps[j] = component;
                    if (i == currentSubComps.length)
                        break;
                    j++;
                }
                undoneSubComps[j] = currentSubComps[i];
            }

            if (getCreatedDeleted() || !component.isInModel()) {
                FormModel.setInModelRecursively(component, true);
            }

            container.initSubComponents(undoneSubComps);

            if (component instanceof RADVisualComponent) {
                if (container instanceof RADVisualContainer) {
                    LayoutSupportManager layoutSupport =
                        ((RADVisualContainer)container).getLayoutSupport();
                    if (layoutSupport != null)
                        layoutSupport.addComponents(
                            new RADVisualComponent[] { (RADVisualComponent)component },
                            new LayoutConstraints[] { getComponentLayoutConstraints() },
                            componentIndex);
                } else {
                    ((RADVisualComponent)component).resetConstraintsProperties();
                }
            }

            getFormModel().fireComponentAdded(component, getCreatedDeleted());
        }

        private void removeComponent() {
            getFormModel().removeComponentImpl(getComponent(), getCreatedDeleted());
        }

        private void undoComponentsReorder() {
            if (getContainer() != null && reordering != null) {
                int[] revPerm = new int[reordering.length];
                for (int i=0; i < reordering.length; i++)
                    revPerm[reordering[i]] = i;

                getContainer().reorderSubComponents(revPerm);
                getFormModel().fireComponentsReordered(getContainer(), revPerm);
            }
        }

        private void redoComponentsReorder() {
            if (getContainer() != null && reordering != null) {
                getContainer().reorderSubComponents(reordering);
                getFormModel().fireComponentsReordered(getContainer(),
                                                       reordering);
            }
        }

        private void undoComponentPropertyChange() {
            Node.Property prop =
                getComponent().getPropertyByName(getPropertyName());
            if (prop != null)
                try {
                    prop.setValue(getOldPropertyValue());
                }
                catch (Exception ex) { // should not happen
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
        }

        private void redoComponentPropertyChange() {
            Node.Property prop =
                getComponent().getPropertyByName(getPropertyName());
            if (prop != null)
                try {
                    prop.setValue(getNewPropertyValue());
                }
                catch (Exception ex) { // should not happen
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
        }

        private void undoBindingPropertyChange() {
            String subPropName = getSubPropertyName();
            BindingProperty prop = getComponent().getBindingProperty(getPropertyName());
            if (subPropName == null) {
                if (prop != null) {
                    try {
                        prop.setValue(getOldBinding());
                    } catch (Exception ex) { // should not happen
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                }
            } else {
                FormProperty subProp = prop.getSubProperty(subPropName);
                try {
                    subProp.setValue(getOldPropertyValue());
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }

        private void redoBindingPropertyChange() {
            String subPropName = getSubPropertyName();
            BindingProperty prop = getComponent().getBindingProperty(getPropertyName());
            if (subPropName == null) {
                if (prop != null) {
                    try {
                        prop.setValue(getNewBinding());
                    } catch (Exception ex) { // should not happen
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                }
            } else {
                FormProperty subProp = prop.getSubProperty(subPropName);
                try {
                    subProp.setValue(getNewPropertyValue());
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }

        private void undoSyntheticPropertyChange() {
            String propName = getPropertyName();
            RADComponent metacomp = getComponent();
            if (propName.startsWith(RADProperty.SYNTH_PREFIX)) {
                // special case - pre/post init code of a property
                if (propName.startsWith(RADProperty.SYNTH_PRE_CODE)) {
                    FormProperty prop = (FormProperty) metacomp.getPropertyByName(
                        propName.substring(RADProperty.SYNTH_PRE_CODE.length()));
                    prop.setPreCode((String)getOldPropertyValue());
                }
                else if (propName.startsWith(RADProperty.SYNTH_POST_CODE)) {
                    FormProperty prop = (FormProperty) metacomp.getPropertyByName(
                        propName.substring(RADProperty.SYNTH_POST_CODE.length()));
                    prop.setPostCode((String)getOldPropertyValue());
                }
            } else { 
                Node.Property[] props;
                if (metacomp == null) { // form synthetic property
                    FormEditor formEditor = FormEditor.getFormEditor(getFormModel());
                    FormRootNode rootNode = (FormRootNode)formEditor.getFormRootNode();
                    props = rootNode.getAllProperties();
                } else { // component synthetic property
                    props = metacomp.getSyntheticProperties();
                }
                boolean found = false;
                for (int i=0; i < props.length; i++) {
                    if (props[i].getName().equals(propName)) {
                        found = true;
                        try {
                            props[i].setValue(getOldPropertyValue());
                        }
                        catch (Exception ex) { // should not happen
                            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                        }
                        break;
                    }
                }
                if (!found) { // hack for FormDesigner.PROP_DESIGNER_SIZE property on subcontainers
                    getFormModel().fireSyntheticPropertyChanged(metacomp, propName,
                            getNewPropertyValue(), getOldPropertyValue());
                }
            }
        }

        private void redoSyntheticPropertyChange() {
            String propName = getPropertyName();
            RADComponent metacomp = getComponent();
            if (propName.startsWith(RADProperty.SYNTH_PREFIX)) {
                // special case - pre/post init code of a property
                if (propName.startsWith(RADProperty.SYNTH_PRE_CODE)) {
                    FormProperty prop = (FormProperty) metacomp.getPropertyByName(
                        propName.substring(RADProperty.SYNTH_PRE_CODE.length()));
                    prop.setPreCode((String)getNewPropertyValue());
                }
                else if (propName.startsWith(RADProperty.SYNTH_POST_CODE)) {
                    FormProperty prop = (FormProperty) metacomp.getPropertyByName(
                        propName.substring(RADProperty.SYNTH_POST_CODE.length()));
                    prop.setPostCode((String)getNewPropertyValue());
                }
            } else {
                Node.Property[] props;
                if (metacomp == null) { // form synthetic property
                    FormEditor formEditor = FormEditor.getFormEditor(getFormModel());
                    FormRootNode rootNode = (FormRootNode)formEditor.getFormRootNode();
                    props = rootNode.getAllProperties();
                } else { // component synthetic property
                    props = metacomp.getSyntheticProperties();
                }
                boolean found = false;
                for (int i=0; i < props.length; i++) {
                    if (props[i].getName().equals(propName)) {
                        found = true;
                        try {
                            props[i].setValue(getNewPropertyValue());
                        }
                        catch (Exception ex) { // should not happen
                            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                        }
                        break;
                    }
                }
                if (!found) { // hack for FormDesigner.PROP_DESIGNER_SIZE property on subcontainers
                    getFormModel().fireSyntheticPropertyChanged(metacomp, propName,
                            getOldPropertyValue(), getNewPropertyValue());
                }
            }
        }

        private void undoEventHandlerAddition() {
            Event event = getComponentEvent();
            if (event == null)
                return;

            addToInterestList(FormModelEvent.this);

            getFormModel().getFormEvents().detachEvent(event, getEventHandler());

            removeFromInterestList(FormModelEvent.this);

            // hack: reset the event property to update the property sheet
            Node.Property prop = getComponent().getPropertyByName(event.getId());
            if (prop != null) {
                try {
                    if (getEventHandler().equals(prop.getValue()))
                        prop.setValue(null);
                }
                catch (Exception ex) { // should not happen
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }

        private void redoEventHandlerAddition() {
            Event event = getComponentEvent();
            if (event == null)
                return;

            getFormModel().getFormEvents().attachEvent(
                event,
                getEventHandler(),
                getEventHandlerContent(),
                getEventHandlerAnnotation());

            // hack: set the event property to update the property sheet
            Node.Property prop = getComponent().getPropertyByName(event.getId());
            if (prop != null) {
                try {
                    prop.setValue(getEventHandler());
                }
                catch (Exception ex) { // should not happen
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }

        private void undoEventHandlerRemoval() {
            Event event = getComponentEvent();
            if (event == null)
                return;

            getFormModel().getFormEvents().attachEvent(
                event,
                getEventHandler(),
                getEventHandlerContent(),
                getEventHandlerAnnotation());

            // hack: set the event property to update the property sheet
            Node.Property prop = getComponent().getPropertyByName(event.getId());
            if (prop != null) {
                try {
                    prop.setValue(getEventHandler());
                }
                catch (Exception ex) { // should not happen
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }

        private void redoEventHandlerRemoval() {
            Event event = getComponentEvent();
            if (event == null)
                return;

            addToInterestList(FormModelEvent.this);

            getFormModel().getFormEvents().detachEvent(event, getEventHandler());

            removeFromInterestList(FormModelEvent.this);

            // hack: reset the event property to reflect the change in property sheet
            Node.Property prop = getComponent().getPropertyByName(event.getId());
            if (prop != null) {
                try {
                    if (getEventHandler().equals(prop.getValue()))
                        prop.setValue(null);
                }
                catch (Exception ex) { // should not happen
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }

        private void undoEventHandlerRenaming() {
            FormEvents formEvents = getFormModel().getFormEvents();

            formEvents.renameEventHandler(getNewEventHandler(),
                                          getOldEventHandler());

            Event[] events = formEvents.getEventsForHandler(
                                            getOldEventHandler());
            for (int i=0 ; i < events.length; i++) {
                Node.Property prop = events[i].getComponent()
                                       .getPropertyByName(events[i].getId());
                if (prop != null) {
                    try {
                        prop.setValue(getOldEventHandler());
                    }
                    catch (Exception ex) { // should not happen
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                }
            }
        }

        private void redoEventHandlerRenaming() {
            FormEvents formEvents = getFormModel().getFormEvents();

            formEvents.renameEventHandler(getOldEventHandler(),
                                          getNewEventHandler());

            Event[] events = formEvents.getEventsForHandler(
                                            getNewEventHandler());
            for (int i=0 ; i < events.length; i++) {
                Node.Property prop = events[i].getComponent()
                                       .getPropertyByName(events[i].getId());
                if (prop != null) {
                    try {
                        prop.setValue(getNewEventHandler());
                    }
                    catch (Exception ex) { // should not happen
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                }
            }
        }
    }
}
