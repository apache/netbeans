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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelui.switcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
/**
 *
 */
public final class SwitchProjectAction extends NodeAction {
    
    private final JCheckBoxMenuItem presenter;
    private final CsmModel model;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private enum State {
        Enabled, Disabled, Indeterminate, BeingCreated
    }
    
    public SwitchProjectAction() {
        presenter = new JCheckBoxMenuItem(getName());
        presenter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onActionPerformed();
            }
        });
        CsmModel aModel = CsmModelAccessor.getModel();
        if( CsmModelAccessor.getModelState() == CsmModelState.OFF ) {
            this.model = null;
        } else {
            this.model = aModel;
        }
    }
    
    @Override
    public String getName() {
	return NbBundle.getMessage(getClass(), ("CTL_SwitchProjectAction")); // NOI18N
    }
    
    @Override
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }    
    
    @Override
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }
    
    private JMenuItem getPresenter() {
        final Collection<NativeProject> projects = getNativeProjects(getActivatedNodes());
        if( projects == null ) {
            presenter.setEnabled(!running.get());
            presenter.setSelected(false);
        } else if (model == null) {
            presenter.setEnabled(false);
            presenter.setSelected(false);
        } else {
	    try {
		State state = getState(projects);
                switch (state) {
                    case Indeterminate:
                        presenter.setEnabled(!running.get());
                        presenter.setSelected(false);
                        break;
                    case BeingCreated:
                        presenter.setEnabled(false);
                        presenter.setSelected(true);
                        break;
                    case Enabled:
                        presenter.setEnabled(!running.get());
                        presenter.setSelected(true);
                        break;
                    case Disabled:
                        presenter.setEnabled(!running.get());
                        presenter.setSelected(false);
                        break;
                    default:
                        throw new IllegalArgumentException("" + state); //NOI18N
                }
	    }
	    catch( Throwable thr ) { 
		// we are in awt thread;
		// if exception occurs here, it doesn't allow even to close the project!
		thr.printStackTrace(System.err);
		presenter.setEnabled(false);
		presenter.setSelected(true);
	    }
        }
        return presenter;
    }

    /** 
     * Gets the collection of native projects that correspond the given nodes.
     * @return in the case all nodes correspond to native projects -
     * collection of native projects; otherwise null
     */
    private Collection<NativeProject> getNativeProjects(Node[] nodes) {
        Collection<NativeProject> projects = new ArrayList<NativeProject>();
        for (int i = 0; i < nodes.length; i++) {
            Object o = nodes[i].getValue("Project"); // NOI18N 
            if( ! (o instanceof  Project) ) {
                o = nodes[0].getLookup().lookup(Project.class);
                if (o == null) {
                    return null;
                }
            }
            NativeProject nativeProject = ((Project) o).getLookup().lookup(NativeProject.class);
            if( nativeProject == null ) {
                return null;
            }
            projects.add(nativeProject);
        }
        return projects;
    }
    
    private State getState(Collection<NativeProject> projects) {
        CndUtils.assertNotNull(model, "null model");//NOI18N
        State state = State.Indeterminate;
        for( NativeProject p : projects ) {
            State curr = getState(p);
            if( state == State.BeingCreated ) {
                return State.BeingCreated;
            } else if( state == State.Indeterminate ) {
                state = curr;
            } else {
                if( state != curr ) {
                    return State.Indeterminate;
                }
            }
        }
        return state;
    }
    
    private State getState(NativeProject p) {
        Boolean enabled = model.isProjectEnabled(p);
        if (enabled == null) {
            return State.BeingCreated;
        } else if (enabled.booleanValue()) {
            return State.Enabled;
        } else {
            return State.Disabled;
        }
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes)  {
        if( model == null ) {
            return false;
        }
	if( running.get() ) {
	    return false;
	}
        Collection<NativeProject> projects = getNativeProjects(getActivatedNodes());
        if( projects == null) {
            return false;
        }
        State state = getState(projects);
        switch (state) {
            case Enabled: 
                return true;
            case Disabled: 
                return true;
            case Indeterminate: 
                return false;
            case BeingCreated: 
                return false;
            default: 
                throw new IllegalArgumentException("" + state); //NOI18N
        }
    }

    private void onActionPerformed() {
        performAction(getActivatedNodes());
    }
    
    /** Actually nobody but us call this since we have a presenter. */
    @Override
    public void performAction(final Node[] activatedNodes) {
        if( model == null ) {
            return;
        }
	if (!running.compareAndSet(false, true)) {
            return;
        }
	model.enqueue(new Runnable() {
            @Override
	    public void run() {
                try {
                    performAction(getNativeProjects(getActivatedNodes()));
                } finally {
                    running.set(false);
                }
	    }
	}, "Switching code model ON/OFF"); //NOI18N
    }
    
    private void performAction(Collection<NativeProject> projects) {
        CndUtils.assertNotNull(model, "null model");//NOI18N
        if( projects != null ) {
            State state = getState(projects);
            switch( state ) {
                case Enabled:
                    for( NativeProject p : projects ) {
                        model.disableProject(p);
                    }
                    break;
                case Disabled:
                    for( NativeProject p : projects ) {
                        model.enableProject(p);
                    }
                    break;
            }
        }
    }
    
    @Override
    protected boolean asynchronous () {
        return false;
    }
}
