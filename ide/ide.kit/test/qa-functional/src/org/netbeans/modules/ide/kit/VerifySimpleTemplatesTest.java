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

package org.netbeans.modules.ide.kit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public class VerifySimpleTemplatesTest extends NbTestCase {
    private static final Logger LOG = Logger.getLogger(VerifySimpleTemplatesTest.class.getName());

    private static final Set<String> templateProblemList = new HashSet<>(Arrays.asList(
            "Templates/Other/HintSample.test" // TODO degrades test stability "FSException: Cannot get shared access"
    ));

    public VerifySimpleTemplatesTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.emptyConfiguration().
            clusters("(extide|java).*").
            enableModules(".*", ".*").
            honorAutoloadEager(true).
            failOnException(Level.INFO).
            addTest(VerifySimpleTemplatesTest.class).
            suite();
    }

    public void testAllTemplates() throws IOException {
        clearWorkDir();

        DataFolder scratch = DataFolder.findFolder(FileUtil.toFileObject(getWorkDir()));

        DataFolder templates = DataFolder.findFolder(FileUtil.getConfigFile("Templates"));
        List<DataObject> groups = new ArrayList<>();
        quickPickTemplates(templates, groups);

        int cnt = 0;
        for (DataObject g : groups) {
            assertTrue("It is a folder: " + g, g instanceof DataFolder);
            DataFolder f = (DataFolder) g;

            List<DataObject> simpleTemplates = new ArrayList<>();
            quickPickTemplates(f, simpleTemplates);

            for (DataObject t : simpleTemplates) {
                LOG.log(Level.WARNING, "Processing {0}", t.getPrimaryFile().getPath());
                if (templateProblemList.contains(t.getPrimaryFile().getPath())) {
                    LOG.log(Level.WARNING, "template in ignore set, skipping.");
                    continue;
                }
                DataObject generated = t.createFromTemplate(scratch, "Test" + ++cnt);
                Editable edit = generated.getLookup().lookup(Editable.class);
                if (edit != null) {
                    LOG.log(Level.WARNING, "Editing {0}", generated.getPrimaryFile().getPath());
                    edit.edit();
                } else {
                    Openable open = generated.getLookup().lookup(Openable.class);
                    if (open != null) {
                        LOG.log(Level.WARNING, "Opening {0}", generated.getPrimaryFile().getPath());
                        open.open();
                    } else {
                        LOG.log(Level.WARNING, "Cannot open {0}", generated.getPrimaryFile().getPath());
                    }
                }
            }
        }
    }

    private void quickPickTemplates(DataFolder f, List<DataObject> collect) {
        for (DataObject obj : f.getChildren()) {
            if (obj instanceof DataFolder) {
                Object o = obj.getPrimaryFile().getAttribute("simple"); // NOI18N
                if (o == null || Boolean.TRUE.equals(o)) {
                    collect.add(obj);
                }
                continue;
            }
            if (obj.isTemplate()) {
                collect.add(obj);
            }
        }
    }
}
