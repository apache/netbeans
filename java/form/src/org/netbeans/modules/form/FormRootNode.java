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

package org.netbeans.modules.form;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.form.actions.*;
import org.openide.actions.PasteAction;
import org.openide.actions.ReorderAction;
import org.openide.util.datatransfer.PasteType;

/**
 * This class represents the root node of the form (displayed as root in
 * Component Inspector).
 *
 * @author Tomas Pavek
 */

class FormRootNode extends FormNode {
    private Node.Property[] codeGenProperties;
    private Node.Property[] resourceProperties;
    private Node.Property[] allProperties;

    public FormRootNode(FormModel formModel) {
        super(new RootChildren(formModel), formModel);
        setName("Form Root Node"); // NOI18N
        setIconBaseWithExtension("org/netbeans/modules/form/resources/formDesigner.gif"); // NOI18N
        updateName(formModel.getName());
    }

    // TODO: icons for visual and non-visual forms
//    public Image getIcon(int iconType) {
//    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (actions == null) { // from AbstractNode
            List<Action> l = new ArrayList<Action>();
            if (isModifiableContainer()) {
                l.add(SystemAction.get(AddAction.class));
                l.add(null);
                l.add(SystemAction.get(PasteAction.class));
                l.add(null);
                l.add(SystemAction.get(ReorderAction.class));
                l.add(null);
            }
            l.add(SystemAction.get(ReloadAction.class));
            l.add(null);
            for (Action a : super.getActions(context)) {
                l.add(a);
            }
            actions = l.toArray(new Action[0]);
        }
        return actions;
    }

    void updateName(String name) {
        setDisplayName(FormUtils.getFormattedBundleString("FMT_FormNodeName", // NOI18N
                                                          new Object[] { name }));
    }

    FormOthersNode getOthersNode() {
        return ((RootChildren)getChildren()).othersNode;
    }
    
    @Override
    public Node.PropertySet[] getPropertySets() {
        Node.PropertySet codeSet = new Node.PropertySet(
                "codeGeneration", // NOI18N
                FormUtils.getBundleString("CTL_SyntheticTab"), // NOI18N
                FormUtils.getBundleString("CTL_SyntheticTabHint")) // NOI18N
        {
            @Override
            public Node.Property[] getProperties() {
                return getCodeGenProperties();
            }
        };
        Node.PropertySet resourceSet = new Node.PropertySet(
                "resources", // NOI18N
                FormUtils.getBundleString("CTL_ResourceTab"), // NOI18N
                FormUtils.getBundleString("CTL_ResourceTabHint")) // NOI18N
        {
            @Override
            public Node.Property[] getProperties() {
                return getResourceProperties();
            }
        };
        return new Node.PropertySet[] { codeSet, resourceSet };
    }

    Node.Property[] getCodeGenProperties() {
        if (codeGenProperties == null)
            codeGenProperties = createCodeGenProperties();
        return codeGenProperties;
    }
    
    private Node.Property[] createCodeGenProperties() {
        return FormEditor.getCodeGenerator(getFormModel()).getSyntheticProperties(null);
    }

    Node.Property[] getResourceProperties() {
        if (resourceProperties == null)
            resourceProperties = createResourceProperties();
        return resourceProperties;
    }
    
    private Node.Property[] createResourceProperties() {
        return FormEditor.getResourceSupport(getFormModel()).createFormProperties();
    }

    Node.Property[] getAllProperties() {
        if (allProperties == null) {
            int codeGenCount = getCodeGenProperties().length;
            int resCount = getResourceProperties().length;
            allProperties = new Node.Property[codeGenCount + resCount];
            System.arraycopy(codeGenProperties, 0, allProperties, 0, codeGenCount);
            System.arraycopy(resourceProperties, 0, allProperties, codeGenCount, resCount);
        }
        return allProperties;
    }

    @Override
    protected void createPasteTypes(Transferable t, java.util.List<PasteType> s) {
        if (isModifiableContainer()) {
            CopySupport.createPasteTypes(t, s, getFormModel(), null);
        }
    }

    /**
     * Returns whether "other components" can be added under this node (i.e.
     * there is no Other Components node, the components appear directly under
     * root node).
     */
    private boolean isModifiableContainer() {
        return !getFormModel().isReadOnly() && !shouldHaveOthersNode(getFormModel());
    }

    /**
     * Returns true if the Other Components node should be used, or false if all
     * the "other" components should be shown directly under the root node. The
     * latter is the case when the root component either does not exists (the
     * form class extends Object) or if it is not a visual container. Here all
     * the components can be presented on the same level. OTOH if the root
     * component is a visual container (e.g. extends JPanel or JFrame), then it
     * has its hierarchy (the node can be expanded) and it seems better to have
     * the other components presented separately under Other Components node.
     */
    private static boolean shouldHaveOthersNode(FormModel formModel) {
        return formModel.getTopRADComponent() instanceof RADVisualContainer;
    }

    // ----------------

    /**
     * The children nodes of the root node can have 3 variants:
     */
    static class RootChildren extends FormNodeChildren {

        static final Object OTHERS_ROOT = new Object();

        private FormModel formModel;
        private FormOthersNode othersNode;

        protected RootChildren(FormModel formModel) {
            this.formModel = formModel;
            updateKeys();
        }

        // FormNodeChildren implementation
        @Override
        protected void updateKeys() {
            othersNode = null;

            List<Object> keys = new LinkedList<Object>();
            boolean otherComps = shouldHaveOthersNode(formModel);
            if (otherComps) {
                keys.add(OTHERS_ROOT);
            }
            RADComponent rootComp = formModel.getTopRADComponent();
            if (rootComp != null) {
                keys.add(rootComp);
            }
            if (!otherComps) {
                keys.addAll(formModel.getOtherComponents());
            }
            setKeys(keys.toArray());
        }

        @Override
        protected Node[] createNodes(Object key) {
            Node node;
            if (key == OTHERS_ROOT) {
                node = othersNode = new FormOthersNode(formModel);
            } else {
                node = new RADComponentNode((RADComponent)key);
            }
            node.getChildren().getNodes(); // enforce subnodes creation
            return new Node[] { node };
        }

        protected final FormModel getFormModel() {
            return formModel;
        }
    }
    
}
