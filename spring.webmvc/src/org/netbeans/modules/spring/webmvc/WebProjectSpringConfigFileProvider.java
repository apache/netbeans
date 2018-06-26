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

package org.netbeans.modules.spring.webmvc;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileLocationProvider;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbCollections;

/**
 *
 * @author Andrei Badea
 */
@ProjectServiceProvider(service={SpringConfigFileProvider.class, SpringConfigFileLocationProvider.class},
projectType="org-netbeans-modules-web-project")
public class WebProjectSpringConfigFileProvider implements SpringConfigFileProvider, SpringConfigFileLocationProvider {

    private final Project project;

    public WebProjectSpringConfigFileProvider(Project project) {
        this.project = project;
    }

    public Set<File> getConfigFiles() {
        FileObject webInf = getWebInf();
        if (webInf == null) {
            return Collections.emptySet();
        }
        Set<File> result = new HashSet<File>();
        addFilesInWebInf(webInf, result);
        return Collections.unmodifiableSet(result);
    }

    private static void addFilesInWebInf(FileObject webInf, Set<File> result) {
        for (FileObject fo : NbCollections.iterable(webInf.getChildren(true))) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (!SpringConstants.CONFIG_MIME_TYPE.equals(fo.getMIMEType())) {
                continue;
            }
            File file = FileUtil.toFile(fo);
            if (file == null) {
                continue;
            }
            result.add(file);
        }
    }

    public FileObject getLocation() {
        return getWebInf();
    }

    private FileObject getWebInf() {
        WebModuleProvider provider = project.getLookup().lookup(WebModuleProvider.class);
        if (provider == null) {
            return null;
        }
        WebModule webModule = provider.findWebModule(project.getProjectDirectory());
        if (webModule == null) {
            return null;
        }
        return webModule.getWebInf();
    }
}
