/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.util.concurrent.Callable;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class J2SEProjectTest extends NbTestCase {

    public J2SEProjectTest(String n) {
        super(n);
    }

    /**
     * Regarding projectimport.eclipse.core subpatch in #146582.
     */
    public void testGetLookup() throws Exception {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        Project p = ProjectManager.getDefault().findProject(J2SEProjectGenerator.createProject(
                getWorkDir(), "test", null, null, null, false).getProjectDirectory());
        assertNotNull(p.getLookup().lookup(AuxiliaryConfiguration.class));
        Callable<?> call = p.getLookup().lookup(Callable.class);
        assertNotNull(call);
        assertTrue(call.call() instanceof AuxiliaryConfiguration);
    }

    public void testJVMProperties() throws Exception {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        Project p = ProjectManager.getDefault().findProject(J2SEProjectGenerator.createProject(
                getWorkDir(), "test", null, null, null, false).getProjectDirectory());
        assertNotNull(p);
        final J2SEProject j2sep = p.getLookup().lookup(J2SEProject.class);
        assertNotNull(j2sep);
        final String javaHome = j2sep.evaluator().getProperty("java.home"); //NOI18N
        assertNotNull(javaHome);
        assertEquals(
            JavaPlatform.getDefault().getInstallFolders().iterator().next(),
            FileUtil.toFileObject(FileUtil.normalizeFile(new File(javaHome))));

    }

    @LookupProvider.Registration(projectType="org-netbeans-modules-java-j2seproject")
    public static class TestLookupProvider implements LookupProvider {

        public Lookup createAdditionalLookup(Lookup lkp) {
            Project p = lkp.lookup(Project.class);
            assertNotNull(p);
            Lookup base = p.getLookup();
            assertNotNull(base);
            final AuxiliaryConfiguration aux = base.lookup(AuxiliaryConfiguration.class);
            assertNotNull(aux);
            return Lookups.singleton(new Callable<AuxiliaryConfiguration>() {
                public AuxiliaryConfiguration call() throws Exception {
                    return aux;
                }
            });
        }

    }

}
