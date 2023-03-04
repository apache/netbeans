/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.ide.ergonomics;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LibrariesCheck extends NbTestCase {

    public LibrariesCheck(String name) {
        super(name);
    }

    public void testGetLibraries() throws Exception {
        FileObject fo = FileUtil.getConfigFile("org-netbeans-api-project-libraries/Libraries");
        assertNotNull("Libraries found", fo);
        StringBuilder sb = new StringBuilder();
        for (FileObject f : fo.getChildren()) {
            System.setProperty("nblibraries." + f.getNameExt(), f.asText());
            Object n = f.getAttribute("displayName");
            if (n instanceof String) {
                System.setProperty("displayName.nblibraries." + f.getNameExt(), (String)n);
            } else {
                sb.append("Missing displayName attribute: " + f).append("\n");
            }
        }
    }
    public void testCheckLibrariesPretest() throws Exception {
        testCheckLibrariesReal();
    }
    public void testCheckLibrariesReal() throws Exception {
        StringBuilder errors = new StringBuilder();
        FileObject fo = FileUtil.getConfigFile("org-netbeans-api-project-libraries/Libraries");
        for (Object o : System.getProperties().keySet()) {
            String f = (String)o;
            if (f.startsWith("nblibraries.")) {
                String contents = System.getProperties().getProperty(f);
                f = f.substring("nblibraries.".length());
                FileObject l = fo.getFileObject(f);
                if (l == null) {
                    if (f.endsWith("jaxb.xml") || f.endsWith("junit.xml") || f.endsWith("junit_4.xml") || f.endsWith("hamcrest.xml") || f.endsWith("junit_5.xml")) {
                        // this is a library defined in autoload module
                        // which is not enabled in ergonomic mode right now
                        continue;
                    }
                    errors.append("Missing library " + f + "\n");
                    continue;
                }
                if (l.asText().equals(contents)) {
                    Object n = l.getAttribute("displayName");
                    String origN = System.getProperty("displayName.nblibraries." + l.getNameExt());
                    if (origN == null || !origN.equals(n)) {
                        errors.append("Wrong name of " + l.getNameExt() + " old: " + origN + " new: " + n + "\n");
                    }
                    continue;
                }
                errors.append("Wrong library: "+ l.getNameExt() + "\n");
            }
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
    }
}
