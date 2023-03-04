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
package org.netbeans.modules.websvc.core.actions;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.project.api.WebServiceData;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Peter Williams
 */
public class WebServiceClientActionGroup extends NodeAction implements Presenter.Popup {
	
        @Override
	public String getName() {
            return NbBundle.getMessage(WebServiceClientActionGroup.class, "LBL_WebServiceClientActionGroup"); // NOI18N
	}

	/** List of system actions to be displayed within this one's toolbar or submenu. */
	private static SystemAction[] grouped() {
		return new SystemAction[] {
			SystemAction.get(InvokeOperationAction.class),
		};
	}

        @Override
	public JMenuItem getPopupPresenter() {
		Node[] activatedNodes = getActivatedNodes();
		if(activatedNodes.length == 1 && hasWebServiceClient()) {
			return new LazyMenu();
		}
		JMenuItem i = super.getPopupPresenter();
		i.setVisible(false);
		return i;
	}

        @Override
	public HelpCtx getHelpCtx() {
		// If you will provide context help then use:
		// return new HelpCtx(PromoteBusinessMethodAction.class);
		return HelpCtx.DEFAULT_HELP;
	}

        @Override
	protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
		return true;
	}

        @Override
	protected void performAction(org.openide.nodes.Node[] activatedNodes) {
		assert false : "Should never be called: ";
	}

	/**
	 * Returns true if this node is in a project that has any web service clients
	 * added to it.
	 */    
	private boolean hasWebServiceClient() {
		Node[] activatedNodes = getActivatedNodes();
		DataObject dobj = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
            if (dobj != null) {
                Project prj = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
                if (prj != null && WebServiceData.getWebServiceData(prj) != null) {
                    return true;
                }
                WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(dobj.getPrimaryFile());
                if(clientSupport != null) {
                    // !PW FIXME add code to confirm that the project actually has
                    // clients added to it.
                    return true;
                }
            }
            return false;
	}

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().<Node>toArray(new Node[0]));
        return enable ? this : null;
    }
    
    /**
     * Avoids constructing submenu until it will be needed.
     */
    private final class LazyMenu extends JMenu {
        
        public LazyMenu() {
            super(WebServiceClientActionGroup.this.getName());
        }
        
        @Override
        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                SystemAction[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    SystemAction action = grouped[i];
                    if (action == null) {
                        addSeparator();
                    } else if (action instanceof Presenter.Popup) {
                        add(((Presenter.Popup)action).getPopupPresenter());
                    } else {
                        assert false : "Action had no popup presenter: " + action;
                    }
                }
            }
            return super.getPopupMenu();
        }
 
    }
     
}
