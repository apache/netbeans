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
public class ProjectConvertorCheck extends NbTestCase {

    public ProjectConvertorCheck(String name) {
        super(name);
    }

    public void testGetConvertors() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Services/ProjectConvertors");
        if (fo == null) {
            // basic IDE may not provide any convertors
            return;
        }
        assertNotNull("Convertor folder found found", fo);
        assertTrue("Convertor folder is not empty found", fo.getChildren().length > 0);
        StringBuilder sb = new StringBuilder();
        for (FileObject f : fo.getChildren()) {
            Object d = f.getAttribute("raw:delegate");
            if (d instanceof Class) {
                System.setProperty("project.convertor." + f.getNameExt(), ((Class)d).getName());
            } else {
                sb.append("delegate property is not set ").append(f).append("\n");
            }
            Object n = f.getAttribute("requiredPattern");
            if (n instanceof String) {
                System.setProperty("requiredPattern.project.convertor." + f.getNameExt(), (String)n);
            } else {
                sb.append("Missing requiredPattern attribute: ").append(f).append("\n");
            }
        }
    }
    public void testCheckConvertorsPretest() throws Exception {
        testCheckConvertorsReal();
    }
    public void testCheckConvertorsReal() throws Exception {
        StringBuilder errors = new StringBuilder();
        FileObject fo = FileUtil.getConfigFile("Services/ProjectConvertors");
        for (Object o : System.getProperties().keySet()) {
            String f = (String)o;
            if (f.startsWith("project.convertor.")) {
                f = f.substring("project.convertor.".length());
                FileObject l = fo.getFileObject(f);
                if (l == null) {
                    errors.append("Missing convertor " + f + "\n");
                    continue;
                }
                Object n = l.getAttribute("requiredPattern");
                String origN = System.getProperty("requiredPattern.project.convertor." + l.getNameExt());
                if (origN == null || !origN.equals(n)) {
                    errors.append("Wrong name of " + l.getNameExt() + " old: " + origN + " new: " + n + "\n");
                }
            }
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
    }
}
