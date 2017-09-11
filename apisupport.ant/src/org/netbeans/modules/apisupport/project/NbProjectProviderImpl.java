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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.spi.NbProjectProvider;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkozeny
 */
public class NbProjectProviderImpl implements NbProjectProvider{
    
    private final Project prj;

    NbProjectProviderImpl(Project prj) {
        this.prj = prj;
    }

    @Override
    public boolean isNbPlatformApplication() {
        NbModuleProject target = prj.getLookup().lookup(NbModuleProject.class);
        if(target != null && target.getModuleType() == NbModuleType.SUITE_COMPONENT) {
            File suiteDirectory = target.getLookup().lookup(SuiteProvider.class).getSuiteDirectory();
            if(suiteDirectory!=null) {
                FileObject suiteDirectoryFO = FileUtil.toFileObject(suiteDirectory);
                if(suiteDirectoryFO != null) {
                    try {
                        Project suitePrj = ProjectManager.getDefault().findProject(suiteDirectoryFO);
                        if(suitePrj != null) {
                            SuiteProject suiteProject = suitePrj.getLookup().lookup(SuiteProject.class);
                            if(suiteProject != null && suiteProject.getSuiteType() == SuiteType.APPLICATION) {
                                return true;
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isSuiteComponent() {
        NbModuleProject target = prj.getLookup().lookup(NbModuleProject.class);
        if(target != null && target.getModuleType() == NbModuleType.SUITE_COMPONENT) {
            return true;
        }
        return false;
    }
    
}
