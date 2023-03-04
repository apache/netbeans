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

import javax.swing.Action;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.form.EditorSupport;
import org.netbeans.modules.form.FormDataObject;
import org.openide.actions.EditAction;
import org.openide.actions.OpenAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

import org.openide.util.actions.SystemAction;

/** The DataNode for Forms.
 *
 * @author Ian Formanek
 */
public class FormDataNode extends FilterNode {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 1795549004166402392L;

    /** Icon base for form data objects. */
    private static final String FORM_ICON_BASE = "org/netbeans/modules/form/resources/form.gif"; // NOI18N

    /** Constructs a new FormDataObject for specified primary file
     * 
     * @param fdo form data object
     */
    public FormDataNode(FormDataObject fdo) {
        this(JavaDataSupport.createJavaNode(fdo.getPrimaryFile()));
    }
    
    private FormDataNode(Node orig) {
        super(orig);
        ((AbstractNode) orig).setIconBaseWithExtension(FORM_ICON_BASE);
    }
    
    @Override
    public Action getPreferredAction() {
        // issue 56351
        return new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                FormEditorSupport supp = (FormEditorSupport)getCookie(EditorSupport.class);
                supp.openFormEditor(false);
            }
        };
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action[] javaActions = super.getActions(context);
        Action[] formActions = new Action[javaActions.length+2];
        formActions[0] = SystemAction.get(OpenAction.class);
        formActions[1] = SystemAction.get(EditAction.class);
        formActions[2] = null;
        // Skipping the first (e.g. Open) action
        System.arraycopy(javaActions, 1, formActions, 3, javaActions.length-1);
        return formActions;
    }

}
