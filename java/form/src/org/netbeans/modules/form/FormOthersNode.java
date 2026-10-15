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

import java.util.ArrayList;
import java.awt.datatransfer.*;
import java.util.List;
import javax.swing.Action;

import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.form.actions.AddAction;
import org.openide.util.datatransfer.PasteType;

/**
 * This class represents the root node of "Other Components".
 *
 * @author Tomas Pavek
 */

class FormOthersNode extends FormNode {

    public FormOthersNode(FormModel formModel) {
        super(new OthersChildren(formModel), formModel);

        getCookieSet().add(new OthersIndex((OthersChildren)getChildren()));
        setIconBaseWithExtension("org/netbeans/modules/form/resources/formNonVisual.gif"); // NOI18N
        setName("Others Node"); // NOI18N
        setName(FormUtils.getBundleString("CTL_NonVisualComponents")); // NOI18N
    }

    @Override
    public Action[] getActions(boolean context) {
        if (actions == null) { // from AbstractNode
            List<Action> l = new ArrayList<Action>();
            if (!getFormModel().isReadOnly()) {
                l.add(SystemAction.get(AddAction.class));
                l.add(null);
                l.add(SystemAction.get(PasteAction.class));
                l.add(null);
                l.add(SystemAction.get(ReorderAction.class));
                l.add(null);
            }
            for (Action a : super.getActions(context)) {
                l.add(a);
            }
            actions = l.toArray(new Action[0]);
        }

        return actions;
    }

    @Override
    protected void createPasteTypes(Transferable t, java.util.List<PasteType> s) {
        CopySupport.createPasteTypes(t, s, getFormModel(), null);
    }

    // -------------

    static class OthersChildren extends FormNodeChildren {

        private FormModel formModel;

        protected OthersChildren(FormModel formModel) {
            this.formModel = formModel;
            updateKeys();
        }

        // FormNodeChildren implementation
        @Override
        protected void updateKeys() {
            setKeys(formModel.getOtherComponents().toArray());
        }

        @Override
        protected Node[] createNodes(Object key) {
            Node node = new RADComponentNode((RADComponent)key);
            node.getChildren().getNodes(); // enforce subnodes creation
            return new Node[] { node };
        }

        protected final FormModel getFormModel() {
            return formModel;
        }
    }

    // -------------

    static final class OthersIndex extends org.openide.nodes.Index.Support {
        private OthersChildren children;

        public OthersIndex(OthersChildren children) {
            this.children = children;
        }

        @Override
        public Node[] getNodes() {
            return children.getNodes();
        }

        @Override
        public int getNodesCount() {
            return getNodes().length;
        }

        @Override
        public void reorder(int[] perm) {
            ComponentContainer cont = children.getFormModel().getModelContainer();
            cont.reorderSubComponents(perm);
            children.getFormModel().fireComponentsReordered(cont, perm);
//            children.updateKeys();
        }
    }
}
