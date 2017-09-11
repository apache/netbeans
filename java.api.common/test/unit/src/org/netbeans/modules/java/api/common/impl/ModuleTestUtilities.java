/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.api.common.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ModuleRoots;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Tomas Zezula
 */
public final class ModuleTestUtilities {
    private final TestProject tp;

    private ModuleTestUtilities(@NonNull final TestProject tp) {
        Parameters.notNull("tp",tp);
        this.tp = tp;
    }

    public boolean updateModuleRoots(
            final boolean tests,
            @NonNull final FileObject... folders) {
        return updateModuleRoots(tests, tests ? "tests" : "classes", folders);  //NOI18N
    }

    public boolean updateModuleRoots(
            final boolean tests,
            @NonNull final String modulePath,
            @NonNull final FileObject... folders) {
        return updateModuleRoots(tests, modulePath, true, folders);
    }

    public boolean updateModuleRoots(
            final boolean tests,
            @NonNull final String modulePath,
            final boolean cleanExisting,
            @NonNull final FileObject... folders) {
        final boolean[] res = new boolean[1];
        ProjectManager.mutex().writeAccess(() -> {
            String[] cfg = tests ?
                    new String[] {
                        "test-roots", //NOI18N
                        "test.dir_%d"    //NOI18N
                    } :
                    new String[] {
                        "source-roots", //NOI18N
                        "src.dir_%d"    //NOI18N
                    };
            final Element root = tp.getUpdateHelper().getPrimaryConfigurationData(true);
            final Element sources = XMLUtil.findElement(root, cfg[0], null);    //NOI18N
            if (sources != null) {
                final NodeList ch = sources.getChildNodes();
                if (cleanExisting) {
                    while (ch.getLength() > 0) {
                        sources.removeChild(ch.item(0));
                    }
                }
                final Map<Pair<String,String>,FileObject> rbn = new HashMap<>();
                int base = ch.getLength() + 1;
                for (int i=0; i<folders.length; i++) {
                    final Element src = root.getOwnerDocument().createElementNS(TestProject.PROJECT_CONFIGURATION_NAMESPACE, "root");  //NOI18N
                    final String name = String.format(cfg[1],base + i);                //NOI18N
                    final String path = String.format("%s.path",name);                  //NOI18N
                    src.setAttribute("id", name);                                       //NOI18N
                    src.setAttribute("pathref", path);
                    rbn.put(Pair.of(name,path), folders[i]);
                    sources.appendChild(src);
                }
                tp.getUpdateHelper().putPrimaryConfigurationData(root, true);
                final EditableProperties ep = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                for (Map.Entry<Pair<String,String>,FileObject> e : rbn.entrySet()) {
                    final Pair<String,String> p = e.getKey();
                    ep.put(p.first(), e.getValue().getNameExt());
                    ep.put(p.second(), modulePath); //NOI18N
                }
                tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                try {
                    ProjectManager.getDefault().saveProject(tp);
                    res[0] = true;
                } catch (IOException ioe) {
                    //keep res[0] false
                }
            }
        });
        return res[0];
    }

    @NonNull
    public SourceRoots newModuleRoots(
            final boolean tests) {
        return ModuleRoots.create(
                tp.getUpdateHelper(),
                tp.getEvaluator(),
                tp.getReferenceHelper(),
                TestProject.PROJECT_CONFIGURATION_NAMESPACE,
                tests ? "test-roots" : "source-roots", //NOI18N
                false,
                "src.{0}{1}.dir"); //NOI18N
    }

    @NonNull
    public SourceRoots newSourceRoots(
            final boolean tests) {
        return SourceRoots.create(
                tp.getUpdateHelper(),
                tp.getEvaluator(),
                tp.getReferenceHelper(),
                TestProject.PROJECT_CONFIGURATION_NAMESPACE,
                tests ? "test-roots" : "source-roots", //NOI18N
                false,
                "src.{0}{1}.dir"); //NOI18N
    }

    @NonNull
    public URL distFor(@NonNull final String moduleName) {
        final File dist = tp.getUpdateHelper().getAntProjectHelper().resolveFile(
                tp.getEvaluator().getProperty(ProjectProperties.DIST_DIR));
        final File jarFile = new File(dist, moduleName+".jar");
        final URL url = FileUtil.urlForArchiveOrDir(jarFile);
        assertNotNull(jarFile.getAbsolutePath(), url);
        return url;
    }

    @NonNull
    public URL buildFor(@NonNull final String moduleName) {
        return buildForImpl(moduleName, ProjectProperties.BUILD_CLASSES_DIR);
    }

    @NonNull
    public URL testBuildFor(@NonNull final String moduleName) {
        return buildForImpl(moduleName, ProjectProperties.BUILD_TEST_MODULES_DIR);
    }

    @NonNull
    private URL buildForImpl(
            @NonNull final String moduleName,
            @NonNull final String targetProp) {
        final File dist = tp.getUpdateHelper().getAntProjectHelper().resolveFile(
                tp.getEvaluator().getProperty(targetProp));
        final File builtModule = new File(dist, moduleName);
        try {
            URL url = BaseUtilities.toURI(builtModule).toURL();
            if (!url.toExternalForm().endsWith("/")) {      //NOI18N
                url = new URL(url.toExternalForm() + '/');  //NOI18N
            }
            return url;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public static ModuleTestUtilities newInstance(@NonNull final TestProject tp) {
        return new ModuleTestUtilities(tp);
    }
}
