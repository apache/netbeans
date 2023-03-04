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
package org.netbeans.modules.web.jsf.editor.refactoring.actions;

import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.jsf.editor.InjectCompositeComponent;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author marekfukala
 */
public class ConvertToCCAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
	JTextComponent tc = EditorRegistry.lastFocusedComponent();
	InjectCompositeComponent.inject(tc.getDocument(), tc.getSelectionStart(), tc.getSelectionEnd());
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
	//enables the action only if the first activated node
	//contains DataObject instance which is currently associated
	//with edited document
	if (activatedNodes.length > 0) {
	    Node node = activatedNodes[0]; //multiselection doesn't make sense
	    DataObject dobj = node.getLookup().lookup(DataObject.class);
	    if (dobj != null) {
		JTextComponent tc = EditorRegistry.lastFocusedComponent();
		if (tc != null && tc.getSelectedText() != null) { //disable w/o editor selection
		    DataObject editedDobj = NbEditorUtilities.getDataObject(tc.getDocument());
		    if (editedDobj != null && editedDobj.equals(dobj)) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    @Override
    public String getName() {
	return NbBundle.getMessage(ConvertToCCAction.class, "MSG_ConvertToCompositeComponents"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
	return null;
    }
}
