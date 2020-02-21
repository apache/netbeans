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
