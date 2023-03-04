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

import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.newproject.EnableStep;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProjectTemplatesCheck extends NbTestCase {
    public ProjectTemplatesCheck(String n) {
        super(n);
    }
    
    public void testCanGetWizardIteratorsForAllProjects() {
        FileObject root = FileUtil.getConfigFile("Templates/Project");
        assertNotNull("project is available (need to run in NbModuleTest mode)", root);
        Enumeration<? extends FileObject> en = root.getChildren(true);
        StringBuilder sb = new StringBuilder();
        int error = 0;
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            if (Boolean.TRUE.equals(fo.getAttribute("template"))) {
                sb.append(fo);
                Object value = EnableStep.readWizard(fo);

                if (value == null) {
                    error++;
                    sb.append(" - failure\n");
                    Enumeration<String> names = fo.getAttributes();
                    while (names.hasMoreElements()) {
                        String n = names.nextElement();
                        sb.append("  name: " + n + " value: " + fo.getAttribute(n) + "\n");
                    }
                } else {
                    sb.append(" - OK\n");
                }
            }
        }
        if (error > 0) {
            fail(sb.toString());
        }
    }
}
