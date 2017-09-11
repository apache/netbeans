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

package org.netbeans.spi.project.ui;

import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;

/**
 * A hook which can be run when a project is "opened" or "closed" in the GUI.
 * <p>
 * The meaning of these terms is intentionally left vague, but typically opening
 * a project signals that the user may wish to work with it, so it would be a good
 * idea to make sure caches are up to date, etc. It is perfectly possible to load
 * and use (even run) projects which are <em>not</em> open, so any project type
 * provider using this hook cannot rely on it for basic semantics.
 * </p>
 * <p>
 * XXX run with mutex read or write held?
 * </p>
 * <p>
 * {@link #projectOpened} and {@link #projectClosed} are always called in pairs,
 * e.g. a project cannot be opened twice in a row without being closed in between.
 * Also a project left open at the end of one VM session will receive
 * {@link #projectClosed} before shutdown and (if an open project list is persisted)
 * {@link #projectOpened} sometime during the next startup.
 * </p>
 * <p>
 * An instance should be placed into a project's lookup to register it. (That means either
 * directly placed in the {@link org.netbeans.api.project.Project}'s lookup or 
 * in the {@link org.netbeans.spi.project.LookupProvider} instance that extends the default
 * project's Lookup, if applicable to the specific project type.)
 * All instances found in the lookup will be notified on project open and close.
 * </p>
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public abstract class ProjectOpenedHook {
    

    static {
        ProjectOpenedTrampoline.DEFAULT = new ProjectOpenedTrampoline() {
            @Override
            public void projectOpened(ProjectOpenedHook hook) {
                hook.projectOpened();
            }
            @Override
            public void projectClosed(ProjectOpenedHook hook) {
                hook.projectClosed();
            }
        };
    }

    
    /**
     * Default constructor for use by subclasses.
     */
    protected ProjectOpenedHook() {}
    
    /**
     * Called when a project is opened in the GUI. If runtime exception is thrown during execution, project will not open as it might not be fully initialized.
     * <div class="nonnormative">
     * <p>Typical things to do here:</p>
     * <ul>
     * <li><p>
     * Update build scripts using
     * <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/GeneratedFilesHelper.html#refreshBuildScript"><code>GeneratedFilesHelper.refreshBuildScript(...)</code></a>.
     * </p></li>
     * <li><p>Call <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/GlobalPathRegistry.html#register"><code>GlobalPathRegistry.register(...)</code></a>
     * with source, compile, and boot paths known to the project.</p></li>
     * <li><p>Write property <code>user.properties.file</code> to <code>private.properties</code>
     * with absolute file path of the <code>build.properties</code> from 
     * the IDE's user directory. This makes it easier for the user to run headless 
     * builds in some cases. The IDE's user directory is defined in 
     * <code>netbeans.user</code> property of IDE's VM.</p></li>
     * </ul>
     * </div>
     */
    protected abstract void projectOpened();
    
    /**
     * Called when a project is closed in the GUI. If runtime exception is thrown during execution, project will still be closed.
     * <div class="nonnormative">
     * <p>Typical things to do here:</p>
     * <ul>
     * <li><p>
     * Call
     * {@link org.netbeans.api.project.ProjectManager#saveProject}
     * as a precaution in case the project was modified in an unusual
     * way (e.g. using
     * {@link org.netbeans.spi.project.AuxiliaryConfiguration}).
     * </p></li>
     * <li><p>Call <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/GlobalPathRegistry.html#unregister"><code>GlobalPathRegistry.unregister(...)</code></a>
     * with the same paths are were previously registered.</p></li>
     * </ul>
     * </div>
     */
    protected abstract void projectClosed();
    
}
