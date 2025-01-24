/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.ide.ergonomics;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.spi.project.ProjectFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class VerifyFullIDETest extends NbTestCase {
    public VerifyFullIDETest(String n) {
        super(n);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(VerifyFullIDETest.class).
            addTest(ProjectTemplatesCheck.class).
            gui(false).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules(".*")
        );
    }

    public void testGetAllProjectFactories() throws Exception {
        StringBuilder sb = new StringBuilder();
        Map<String,String> all = FeatureManager.projectFiles();

        all.put("Fine", "org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton");

        iterateRegistrations(sb, ProjectFactory.class, null, all);

        if (!all.isEmpty()) {
            fail("Not all IDE projects are registered for ergonomics mode, see the list below.\n" +
                "This may mean that you are not using @AntBasedProjectRegistration to register\n" +
                "your projects, or that you need to hardcode the nature of your project into\n" +
                "ide.ergonomics/*.properties using XPath. For more information see\n" +
                "http://wiki.netbeans.org/FitnessForever" +
                "The list of differences follows:\n" + sb
            );
        }
    }

    public void testGetAllNbProjects() throws Exception {
        Map<String,String> all = FeatureManager.nbprojectTypes();
        StringBuilder sb = new StringBuilder();

        Class<?> ant = Class.forName(
            "org.netbeans.spi.project.support.ant.AntBasedProjectType",
            true,
            Thread.currentThread().getContextClassLoader()
        );
        iterateRegistrations(sb, ant, ant.getDeclaredMethod("getType"), all);

        if (!all.isEmpty()) {
            fail("Not all IDE projects are registered for ergonomics mode, see the list below.\n" +
                "This may mean that you are not using @AntBasedProjectRegistration to register\n" +
                "your projects. For more information see\n" +
                "http://wiki.netbeans.org/FitnessForever" +
                "The list of differences follows:\n" + sb
            );
        }
    }

    public void testGetAllProjectTemplates() throws Exception {
        List<XMLFileSystem> all = new ArrayList<XMLFileSystem>();

        for (FeatureInfo fi : FeatureManager.features()) {
            URL url = fi.getLayerURL();
            if (url != null) {
                XMLFileSystem xfs = new XMLFileSystem(url);
                all.add(xfs);
            }
        }

        MultiFileSystem mfs = new MultiFileSystem(all.toArray(new FileSystem[0]));
        FileObject orig = FileUtil.getConfigFile("Templates/Project");
        Enumeration<? extends FileObject> allTemplates = orig.getChildren(true);
        while (allTemplates.hasMoreElements()) {
            FileObject fo = allTemplates.nextElement();
            if (fo.getPath().equals("Templates/Project/Import")) {
                continue;
            }
            if (fo.getPath().equals("Templates/Project/Samples")) {
                continue;
            }

            FileObject clone = mfs.findResource(fo.getPath());

            assertNotNull("Both files exist: " + fo, clone);

            Enumeration<String> allAttributes = fo.getAttributes();
            while (allAttributes.hasMoreElements()) {
                String name = allAttributes.nextElement();
                if ("templateWizardIterator".equals(name)) {
                    name = "instantiatingIterator";
                }

                Object attr = clone.getAttribute(name);
                assertNotNull(
                    "Attribute " + name + " present in clone on " + fo, attr
                );

                if (attr instanceof URL) {
                    URL u = (URL)attr;
                    int read = u.openStream().read(new byte[4096]);
                    if (read <= 0) {
                        fail("Resource shall exist: " + fo + " attr: " + name + " value: " + attr);
                    }
                }
            }
        }
    }

    private void iterateRegistrations(
        StringBuilder sb, Class<?> what, Method info, Map<String,String> all
    ) throws Exception {
        for (Object f : Lookup.getDefault().lookupAll(what)) {
            if (f.getClass().getPackage().getName().equals("org.netbeans.modules.ide.ergonomics.fod")) {
                continue;
            }
            // defect #248615: ProjectConvertorFactory is exempt from this test, until
            // support is provided in the ergonomics
            if (f.getClass().getName().equals("org.netbeans.modules.project.ui.convertor.ProjectConvertorFactory")) {
                continue;
            }
            sb.append(f.getClass().getName());
            if (info != null) {
                Object more = info.invoke(f);
                sb.append(" info: ").append(more);
                Object value = all.get(more);
                if (f.getClass().getName().equals(value)) {
                    sb.append(" OK");
                    all.remove(more);
                } else {
                    sb.append(" not present");
                    all.put("FAIL", more.toString());
                }
            } else {
                if (all.values().remove(f.getClass().getName())) {
                    sb.append(" OK");
                } else {
                    all.put("FAIL", f.getClass().getName());
                    sb.append(" not present");
                }
            }
            sb.append('\n');
        }
    }

}
