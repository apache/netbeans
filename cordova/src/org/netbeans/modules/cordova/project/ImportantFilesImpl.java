/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.ImportantFilesImplementation;
import org.netbeans.modules.web.common.spi.ImportantFilesSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Roman Svitanic
 */
@NbBundle.Messages("LBL_CordovaPlugins=Cordova Plugins")
@ProjectServiceProvider(service = ImportantFilesImplementation.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
public class ImportantFilesImpl implements ImportantFilesImplementation {

    private final ImportantFilesSupport support;
    private final ImportantFilesSupport support2;
    
    private final ImportantFilesSupport.FileInfoCreator fileInfoCreator = new ImportantFilesSupport.FileInfoCreator() {
        @Override
        public FileInfo create(FileObject fileObject) {
            return new FileInfo(
                    fileObject,
                    fileObject.getName().equals("plugins") ? Bundle.LBL_CordovaPlugins(): fileObject.getName(), //NOI18N
                    null);
        }
    };

    public ImportantFilesImpl(Project project) {
        assert project != null;
        support = ImportantFilesSupport.create(project.getProjectDirectory().getFileObject("nbproject"), "plugins.properties"); // NOI18N
        support2 = ImportantFilesSupport.create(project.getProjectDirectory(), "config.xml"); // NOI18N
    }

    @Override
    public Collection<FileInfo> getFiles() {
        List<FileInfo> ret = new ArrayList<>(support.getFiles(fileInfoCreator));
        ret.addAll(support2.getFiles(fileInfoCreator));
        return ret;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
        support2.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
        support2.removeChangeListener(listener);
    }
}
