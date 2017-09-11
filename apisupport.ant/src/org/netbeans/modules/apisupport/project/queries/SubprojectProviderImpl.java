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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Enumerates subprojects, defined as other modules on which this one
 * has build-time dependencies.
 * @author Jesse Glick
 */
public final class SubprojectProviderImpl implements SubprojectProvider {
    
    private final NbModuleProject project;
    
    public SubprojectProviderImpl(NbModuleProject project) {
        this.project = project;
    }
    
    @NbBundle.Messages({
        "WRN_problem_getting_module_list=Problem getting module list.",
        "TITLE_getting_module_list=Getting module list"
    })
    public @Override Set<? extends Project> getSubprojects() {
        // XXX could use a special set w/ lazy isEmpty() - cf. #58639 for freeform
        Set<Project> s = new HashSet<Project>();
        ModuleList ml;
        try {
            ml = project.getModuleList();
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor(Bundle.WRN_problem_getting_module_list(), Bundle.TITLE_getting_module_list(), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE, new Object[] {NotifyDescriptor.OK_OPTION}, null));
            return Collections.emptySet();
        }
        Element data = project.getPrimaryConfigurationData();
        Element moduleDependencies = XMLUtil.findElement(data,
            "module-dependencies", NbModuleProject.NAMESPACE_SHARED); // NOI18N
        assert moduleDependencies != null : "Malformed metadata in " + project;
        for (Element dep : XMLUtil.findSubElements(moduleDependencies)) {
            /* Probably better to open runtime deps too. TBD.
            if (XMLUtil.findElement(dep, "build-prerequisite", // NOI18N
                    NbModuleProject.NAMESPACE_SHARED) == null) {
                continue;
            }
             */
            Element cnbEl = XMLUtil.findElement(dep, "code-name-base", // NOI18N
                NbModuleProject.NAMESPACE_SHARED);
            String cnb = XMLUtil.findText(cnbEl);
            ModuleEntry module = ml.getEntry(cnb);
            if (module == null) {
                Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + project);
                continue;
            }
            File moduleProjectDirF = module.getSourceLocation();
            if (moduleProjectDirF == null) {
                // Do not log, this is pretty normal.
                continue;
            }
            FileObject moduleProjectDir = FileUtil.toFileObject(moduleProjectDirF);
            if (moduleProjectDir == null) {
                Util.err.log(ErrorManager.WARNING, "Warning - could not load sources for dependent module " + cnb + " for " + project);
                continue;
            }
            try {
                Project moduleProject = ProjectManager.getDefault().findProject(moduleProjectDir);
                if (moduleProject == null) {
                    Util.err.log(ErrorManager.WARNING, "Warning - dependent module " + cnb + " for " + project + " is not projectized");
                    continue;
                }
                s.add(moduleProject);
            } catch (IOException e) {
                Logger.getLogger(SubprojectProviderImpl.class.getName()).log(Level.INFO, "Could not load dependent module " + cnb + " for " + project, e);
            }
        }
        // #63824: consider also artifacts found in ${cp.extra} and/or <class-path-extension>s
        for (Element cpext : XMLUtil.findSubElements(data)) {
            if (!cpext.getTagName().equals("class-path-extension")) { // NOI18N
                continue;
            }
            Element binorig = XMLUtil.findElement(cpext, "binary-origin", NbModuleProject.NAMESPACE_SHARED); // NOI18N
            if (binorig == null) {
                continue;
            }
            String text = XMLUtil.findText(binorig);
            String eval = project.evaluator().evaluate(text);
            if (eval == null) {
                continue;
            }
            File jar = project.getHelper().resolveFile(eval);
            AntArtifact aa = AntArtifactQuery.findArtifactFromFile(jar);
            if (aa != null) {
                Project owner = aa.getProject();
                if (owner != null) {
                    s.add(owner);
                }
            }
        }
        String eval = project.evaluator().getProperty("cp.extra"); // NOI18N
        if (eval != null) {
            for (String piece : PropertyUtils.tokenizePath(eval)) {
                if (piece.contains("${")) {
                    //Unresolved property, ignore.
                    continue;
                }
                File jar = project.getHelper().resolveFile(piece);
                Project owner = FileOwnerQuery.getOwner(Utilities.toURI(jar));
                if (owner != null) {
                    s.add(owner);
                }
            }
        }
        return s;
    }
    
    public @Override void addChangeListener(ChangeListener listener) {
        // XXX no impl yet
    }
    
    public @Override void removeChangeListener(ChangeListener listener) {
        // XXX
    }
    
}
