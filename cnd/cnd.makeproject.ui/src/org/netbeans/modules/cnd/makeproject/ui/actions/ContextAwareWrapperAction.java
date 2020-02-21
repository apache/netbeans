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
package org.netbeans.modules.cnd.makeproject.ui.actions;

import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.Presenter;

/**
 *
 */
abstract public class ContextAwareWrapperAction extends MakeProjectContextAwareAction implements Presenter.Menu, Presenter.Popup{
    
    private Action delegateAction;
    
    abstract protected Action createDelegateAction(Project[] projects);        
    abstract protected boolean supportMultipleProjects();

    @Override
    protected void performAction(Node[] activatedNodes) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        //find projects
        Project[] projects = getProjects(activatedNodes);
        if (projects == null || projects.length == 0) {
            delegateAction = null;
            return false;
        } else if (projects.length > 1 && ! supportMultipleProjects()) {
            delegateAction = null;
            return false;
        }
        delegateAction = createDelegateAction(projects);
        return delegateAction != null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        if (delegateAction == null) {
            return null;
        }
        if (Presenter.Popup.class.isAssignableFrom(delegateAction.getClass())) {
            return ((Presenter.Popup)delegateAction).getPopupPresenter();
        }
        return null;
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        if (delegateAction == null) {
            return null;
        }        
        if (Presenter.Menu.class.isAssignableFrom(delegateAction.getClass())) {
            return ((Presenter.Menu)delegateAction).getMenuPresenter();
        }
        return null;
    }      
      
    
}
