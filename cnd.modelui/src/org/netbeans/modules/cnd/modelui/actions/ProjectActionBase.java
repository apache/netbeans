/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelui.actions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * A common abstract parent for  test actions on projects
 */
public abstract class ProjectActionBase extends NodeAction {

    private final boolean enabledAction;
    protected enum State {
        NoProjects, Enabled, Disabled, Indeterminate
    }

    private boolean running;
    private JMenuItem presenter;
    private boolean inited = false;

    public ProjectActionBase(boolean enabledAction) {
        this.enabledAction = enabledAction;
    }

    protected abstract void performAction(Collection<CsmProject> projects);

    @Override
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }

    private JMenuItem getPresenter() {
        if (!inited) {
            presenter = new JMenuItem();
            org.openide.awt.Actions.connect(presenter, (Action) this, true);
            inited = true;
        }
        final Collection<CsmProject> projects = getCsmProjects(getActivatedNodes());
        if (enabledAction) {
            if (projects.isEmpty()) {
                setEnabled(!running);
                presenter.setVisible(false);
            } else {
                try {
                    presenter.setVisible(true);
                    setEnabled(!running);
                } catch (Throwable thr) {
                    // we are in awt thread;
                    // if exception occurs here, it doesn't allow even to close the project!
                    thr.printStackTrace(System.err);
                    setEnabled(false);
                }
            }
        } else {
            presenter.setVisible(false);
        }

        return presenter;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (!enabledAction) {
            return false;
        }
        if (running) {
            return false;
        }
        Collection<CsmProject> projects = getCsmProjects(activatedNodes);
        return isEnabledEx(activatedNodes, projects);
    }

    protected boolean isEnabledEx(Node[] activatedNodes, Collection<CsmProject> projects) {
         State state = getState(projects);
         return state != State.Indeterminate;
    }

    @Override
    public void performAction(final Node[] activatedNodes) {
        running = true;
        CsmModelAccessor.getModel().enqueue(new Runnable() {

            @Override
            public void run() {
                try {
                    performAction(getCsmProjects(activatedNodes));
                } finally {
                    running = false;
                }
            }
        }, "csmproject action " + this.getName()); //NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * Gets the collection of native projects that correspond the given nodes.
     * @return in the case all nodes correspond to native projects -
     * collection of native projects; otherwise null
     */
    protected Collection<CsmProject> getCsmProjects(Node[] nodes) {
        Set<CsmProject> csmProjects = new HashSet<CsmProject>();
        for (int i = 0; i < nodes.length; i++) {
            CsmProject csm = nodes[i].getLookup().lookup(CsmProject.class);
            if (csm == null) {
                NativeProject nativeProject = nodes[i].getLookup().lookup(NativeProject.class);
                if (nativeProject == null) {
                    Object o = nodes[i].getValue("Project"); // NOI18N
                    if (o instanceof Project) {
                        nativeProject = ((Project) o).getLookup().lookup(NativeProject.class);
                    }
                }
                if (nativeProject != null) {
                    CsmModel model = CsmModelAccessor.getModel();
                    csm = model.getProject(nativeProject);
                }
            }
            if (csm != null) {
                csmProjects.add(csm);
            }
        }
        return csmProjects;
    }

    protected State getState(Collection<CsmProject> projects) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model == null || model.getState() != CsmModelState.ON) {
            return State.Indeterminate;
        }
        State state = State.NoProjects;
        for (CsmProject p : projects) {
            State curr = getState(p);
            if (state == State.NoProjects) {
                state = curr;
            } else {
                if (state != curr) {
                    return State.Indeterminate;
                }
            }
        }
        return state;
    }

    private State getState(CsmProject csmPrj) {
        return csmPrj != null && csmPrj.isStable(null) ? State.Enabled : State.Disabled;
    }

}
