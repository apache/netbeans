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
import org.netbeans.api.progress.ProgressUtils;
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
        ProgressUtils.runOffEventDispatchThread(
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
