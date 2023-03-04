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

package org.netbeans.modules.nbcode.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public class VerifySimpleTemplatesTest extends NbTestCase {
    private static Logger LOG = Logger.getLogger(VerifySimpleTemplatesTest.class.getName());

    public VerifySimpleTemplatesTest(String name) {
        super(name);
    }

    public static Test suite() {
        String v = System.getProperty("java.version");
        if (v == null || !v.startsWith("1.8")) {
            return NbModuleSuite.emptyConfiguration().suite();
        }

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

        List<DataObject> errorneus = new ArrayList<>();
        int cnt = 0;
        for (DataObject g : groups) {
            assertTrue("It is a folder: " + g, g instanceof DataFolder);
            DataFolder f = (DataFolder) g;

            List<DataObject> simpleTemplates = new ArrayList<>();
            quickPickTemplates(f, simpleTemplates);

            for (DataObject t : simpleTemplates) {
                LOG.log(Level.WARNING, "Processing {0}", t.getPrimaryFile().getPath());
                final DataObject generated = t.createFromTemplate(scratch, "Test" + ++cnt);
                final FileObject pf = generated.getPrimaryFile();
                final Editable edit = generated.getLookup().lookup(Editable.class);
                if (edit != null) {
                    LOG.log(Level.WARNING, "Editing {0}", pf.getPath());
                    edit.edit();
                } else {
                    Openable open = generated.getLookup().lookup(Openable.class);
                    if (open != null) {
                        LOG.log(Level.WARNING, "Opening {0}", pf.getPath());
                        open.open();
                    } else {
                        LOG.log(Level.WARNING, "Cannot open {0}", pf.getPath());
                    }
                }
                if (!pf.isFolder()) {
                    try {
                        if (pf.asText().isEmpty()) {
                            LOG.log(Level.WARNING, "The file is empty {0}", pf.getPath());
                            errorneus.add(t);
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Cannot read file " + pf.getPath(), ex);
                        errorneus.add(t);
                    }
                }
            }
        }
        if (errorneus.size() != 0) {
            fail("Failed to instantiate " + errorneus.size() + " of "+ cnt + " templates:\n" + errorneus);
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
