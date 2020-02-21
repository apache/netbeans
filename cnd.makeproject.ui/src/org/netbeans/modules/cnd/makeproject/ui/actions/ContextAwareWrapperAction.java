/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
