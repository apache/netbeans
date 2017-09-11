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
            actions = l.toArray(new Action[l.size()]);
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
