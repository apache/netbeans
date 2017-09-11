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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Able to load and fill up an <code>EclipseWorkspace</code> from an Eclipse
 * workspace directory using a .workspace and .classpath file and eventually
 * passed workspace. It is also able to load a basic information from workspace.
 *
 * @author mkrauskopf
 */
public final class WorkspaceFactory {

    private static Map<File, WeakReference<Workspace>> cache = new HashMap<File, WeakReference<Workspace>>();
            
    /** singleton */
    private static WorkspaceFactory instance = new WorkspaceFactory();
    
    private WorkspaceFactory() {/*empty constructor*/}
    
    public static WorkspaceFactory getInstance() {
        return instance;
    }
    
    public void resetCache() {
        cache = new HashMap<File, WeakReference<Workspace>>();
    }
    /**
     * Loads a workspace contained in the given <code>workspaceDir</code>.
     *
     * @throws InvalidWorkspaceException if workspace in the given
     *     <code>workspaceDir</code> is not a valid Eclipse workspace.
     */
    public Workspace load(File workspaceDir) throws ProjectImporterException {
        WeakReference<Workspace> wr = cache.get(workspaceDir);
        Workspace w = wr != null ? wr.get() : null;
        if (w == null) {
            Workspace workspace = Workspace.createWorkspace(workspaceDir);
            if (workspace != null) {
                WorkspaceParser parser = new WorkspaceParser(workspace);
                parser.parse();
                cache.put(workspaceDir, new WeakReference<Workspace>(workspace));
                w = workspace;
            }
        }
        return w;
    }
}
