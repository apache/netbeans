/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven;

import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.test.MockLookup;

public class MavenProjectPropsImplTest extends NbTestCase {

    public MavenProjectPropsImplTest(String name) {
        super(name);
    }

    private FileObject d;
    private Project prj;
    private Preferences p;

    protected @Override void setUp() throws Exception {
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        MockLookup.setInstances(new Modules() {
            @Override public ModuleInfo ownerOf(Class<?> clazz) {
                return null;
            }
        });
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version></project>");
        prj = ProjectManager.getDefault().findProject(d);
        p = ProjectUtils.getPreferences(prj, MavenProjectPropsImplTest.class, true);
    }

    public void testBasicUsage() throws Exception {
        p.put("k", "v");
        assertEquals("v", p.get("k", null));
    }

    public void testInvalidNames() throws Exception { // #200901
        p.putBoolean("a<b", true);
        p.flush();
    }

    public void testHintPackaging() throws Exception {
        NbMavenProject nbmp = prj.getLookup().lookup(NbMavenProject.class);
        assertEquals("jar", nbmp.getPackagingType());
        TestFileUtils.writeFile(d, "nb-configuration.xml", "<project-shared-configuration><properties xmlns='http://www.netbeans.org/ns/maven-properties-data/1'><netbeans.hint.packaging>war</netbeans.hint.packaging></properties></project-shared-configuration>");
        assertEquals("war", nbmp.getPackagingType());
    }

}
