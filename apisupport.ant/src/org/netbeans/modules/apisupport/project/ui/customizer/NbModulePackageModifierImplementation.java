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
package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.java.api.common.ant.PackageModifierImplementation;
import org.openide.ErrorManager;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author mkozeny
 */

//@ProjectServiceProvider(service = PackageModifierImplementation.class)
public final class NbModulePackageModifierImplementation implements PackageModifierImplementation {

    private final NbModuleProject project;
    
    /**
     * Constructor
     * @param project 
     */
    public NbModulePackageModifierImplementation(NbModuleProject project) {
        this.project = project;
    }

    @Override
    public void exportPackageAction(final Collection<String> packagesToExport, final boolean export) {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    ProjectXMLManager projectXMLManager = ProjectXMLManager.getInstance(project.getProjectDirectoryFile());
                    ManifestManager.PackageExport[] previousPubs = projectXMLManager.getPublicPackages();

                    String[] friends = projectXMLManager.getFriends();
                    Set<String> newPacks = new TreeSet<String>();
                    for (ManifestManager.PackageExport exp : previousPubs) {
                        newPacks.add(exp.getPackage() + (exp.isRecursive() ? ".*" : ""));
                    }
                    for (String newP : packagesToExport) {
                        if (export) {
                            newPacks.add(newP);
                        } else {
                            boolean removed = newPacks.remove(newP);
                            //TODO if not removed we have a problem, the package falls under "subpackages" element.
                            //but I suppose subpackages is rare these days.
                        }
                    }
                    if (friends == null) {
                        projectXMLManager.replacePublicPackages(newPacks);
                    } else {
                        projectXMLManager.replaceFriends(new TreeSet<String>(Arrays.asList(friends)), newPacks);
                    }
                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        }
    }

}
