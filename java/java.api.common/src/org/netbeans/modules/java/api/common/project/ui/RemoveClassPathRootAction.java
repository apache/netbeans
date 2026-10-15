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

package org.netbeans.modules.java.api.common.project.ui;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.netbeans.api.project.Project;


import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.project.ProjectManager;
import org.openide.util.Exceptions;

/**
 * Action for removing an ClassPathRoot. The action looks up
 * the {@link RemoveClassPathRootAction.Removable} in the
 * activated node's Lookups and delegates to it.
 * @author Tomas Zezula
 */
final class RemoveClassPathRootAction extends NodeAction {

    /**
     * Implementation of this interfaces has to be placed
     * into the node's Lookup to allow {@link RemoveClassPathRootAction}
     * on the node.
     */
    static interface Removable {
        /**
         * Checks if the classpath root can be removed
         * @return returns true if the action should be enabled
         */
        public boolean canRemove ();
        
        /**
         * <p>Removes the classpath root. The caller has write access to
         * ProjectManager. The implementation should <strong>not</strong> save the changed project.
         * Instead, it should return the changed Project. The caller ensures
         * that all the changed projects are saved.
         * 
         * <p>The reason why the implementation shouldn't save the project is that
         * changed made to the project may cause the build-impl.xml file to be 
         * recreated upon saving, which is slow. There will be performance issues (see #54160) if
         * multiple references are removed and the project is saved after 
         * each removal.</p>
         * 
         * <p>Threading: Called under the {@link ProjectManager#mutex} write access.</p>
         *
         * @return the changed project or null if no project has been changed.
         */
        public abstract Project remove ();
        
        /**
         * Called before the {@link Removable#remove} is called.
         * The implementation can perform operations which should not be done
         * under the {@link ProjectManager#mutex} write access.
         * Threading: Called outside the {@link ProjectManager#mutex} write access.
         */
        public default void beforeRemove() {}
        
        /**
         * Called after the {@link Removable#remove} is called.
         * The implementation can perform operations which should not be done
         * under the {@link ProjectManager#mutex} write access.
         * Threading: Called outside the {@link ProjectManager#mutex} write access.
         */
        public default void afterRemove() {}
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        assert !ProjectManager.mutex().isReadAccess();   //Prevent to deadlock
        final AtomicBoolean cancel = new AtomicBoolean();

        final Runnable action = () -> {
            final Set<Removable> removables = Arrays.stream(activatedNodes)
                    .map((n) -> n.getLookup().lookup(Removable.class))
                    .filter((r) -> r != null)
                    .collect(Collectors.toSet());
            removables.forEach(Removable::beforeRemove);
            try {
                ProjectManager.mutex().writeAccess(() -> {
                    if (cancel.get()) {
                        return;
                    }
                    final Set<Project> changedProjectsSet = new HashSet<>();
                    for (Removable removable : removables) {
                        if (cancel.get()) {
                            break;
                        }
                        Project p = removable.remove();
                        if (p != null)
                            changedProjectsSet.add(p);
                    }
                    for (Project p : changedProjectsSet) {
                        try {
                            ProjectManager.getDefault().saveProject(p);
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                });
            } finally {
                removables.forEach(Removable::afterRemove);
            }
        };
        BaseProgressUtils.runOffEventDispatchThread(
                action,
                NbBundle.getMessage(RemoveClassPathRootAction.class, "TXT_RemovingClassPathRoots"),
                cancel,
                false);
    }

    protected boolean enable(Node[] activatedNodes) {
        for (int i=0; i<activatedNodes.length; i++) {
            Removable removable = activatedNodes[i].getLookup().lookup(Removable.class);
            if (removable==null) {
                return false;
            }
            if (!removable.canRemove()) {
                return false;
            }
        }
        return true;
    }

    public String getName() {
        return NbBundle.getMessage (RemoveClassPathRootAction.class,"CTL_RemoveProject");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (RemoveClassPathRootAction.class);
    }

    protected boolean asynchronous() {
        return false;
    }

}
