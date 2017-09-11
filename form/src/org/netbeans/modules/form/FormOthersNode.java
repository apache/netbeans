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
            actions = l.toArray(new Action[l.size()]);
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
