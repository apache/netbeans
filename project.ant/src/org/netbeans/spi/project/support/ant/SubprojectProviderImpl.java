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

package org.netbeans.spi.project.support.ant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;

/**
 * Translates a list of subproject names into actual subprojects
 * for an Ant-based project.
 * @author Jesse Glick
 */
final class SubprojectProviderImpl implements SubprojectProvider {
    
    private final ReferenceHelper helper;
    
    SubprojectProviderImpl(ReferenceHelper helper) {
        this.helper = helper;
    }
    
    public Set<? extends Project> getSubprojects() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Set<? extends Project>>() {
            public Set<? extends Project> run() {
                // XXX could use a special set w/ lazy isEmpty() - cf. #58639 for freeform
                Set<String> foreignProjectNames = new HashSet<String>();
                for (ReferenceHelper.RawReference ref : helper.getRawReferences()) {
                    foreignProjectNames.add(ref.getForeignProjectName());
                }
                Set<Project> foreignProjects = new HashSet<Project>();
                for (String foreignProjectName : foreignProjectNames) {
                    String prop = "project." + foreignProjectName; // NOI18N
                    AntProjectHelper h = helper.getAntProjectHelper();
                    String foreignProjectDirS = helper.eval.getProperty(prop);
                    if (foreignProjectDirS == null) {
                        // Missing for some reason. Skip it.
                        continue;
                    }
                    FileObject foreignProjectDir = h.resolveFileObject(foreignProjectDirS);
                    if (foreignProjectDir == null) {
                        // Not present on disk, erroneous property, etc. Skip it.
                        continue;
                    }
                    try {
                        Project p = ProjectManager.getDefault().findProject(foreignProjectDir);
                        if (p != null) {
                            // OK, got a real project.
                            foreignProjects.add(p);
                        }
                    } catch (IOException e) {
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                        // skip it
                    }
                }
                return foreignProjects;
            }
        });
    }
    
    public void addChangeListener(ChangeListener listener) {
        // XXX implement - listen to references added and removed
    }    
    
    public void removeChangeListener(ChangeListener listener) {
        // XXX
    }
    
}
