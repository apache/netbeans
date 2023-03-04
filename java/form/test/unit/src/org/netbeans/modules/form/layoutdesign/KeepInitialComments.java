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

package org.netbeans.modules.form.layoutdesign;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.form.FormLAF;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.GandalfPersistenceManager;
import org.netbeans.modules.form.PersistenceException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class KeepInitialComments extends TestCase {

    public KeepInitialComments(String name) {
        super(name);
    }

    public void testKeepInitialComment() throws IOException, PersistenceException {
        FileObject file = FileUtil.createMemoryFileSystem()
                                  .getRoot()
                                  .createData("test.form");
        String code = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                      "\n" +
                      "<!--Leading\n" +
                      "  - comment\n" +
                      "-->\n" +
                      "\n" +
                      "<Form version=\"1.3\" maxVersion=\"1.9\" type=\"org.netbeans.modules.form.forminfo.JPanelFormInfo\">\n" +
                      "  <AuxValues>\n" +
                      "    <AuxValue name=\"FormSettings_autoResourcing\" type=\"java.lang.Integer\" value=\"0\"/>\n" +
                      "    <AuxValue name=\"FormSettings_autoSetComponentName\" type=\"java.lang.Boolean\" value=\"false\"/>\n" +
                      "    <AuxValue name=\"FormSettings_generateFQN\" type=\"java.lang.Boolean\" value=\"true\"/>\n" +
                      "    <AuxValue name=\"FormSettings_generateMnemonicsCode\" type=\"java.lang.Boolean\" value=\"false\"/>\n" +
                      "    <AuxValue name=\"FormSettings_i18nAutoMode\" type=\"java.lang.Boolean\" value=\"false\"/>\n" +
                      "    <AuxValue name=\"FormSettings_layoutCodeTarget\" type=\"java.lang.Integer\" value=\"1\"/>\n" +
                      "    <AuxValue name=\"FormSettings_listenerGenerationStyle\" type=\"java.lang.Integer\" value=\"0\"/>\n" +
                      "    <AuxValue name=\"FormSettings_variablesLocal\" type=\"java.lang.Boolean\" value=\"false\"/>\n" +
                      "    <AuxValue name=\"FormSettings_variablesModifier\" type=\"java.lang.Integer\" value=\"2\"/>\n" +
                      "  </AuxValues>\n" +
                      "\n" +
                      "  <Layout>\n" +
                      "    <DimensionLayout dim=\"0\">\n" +
                      "      <Group type=\"103\" groupAlignment=\"0\" attributes=\"0\">\n" +
                      "          <EmptySpace min=\"0\" pref=\"400\" max=\"32767\" attributes=\"0\"/>\n" +
                      "      </Group>\n" +
                      "    </DimensionLayout>\n" +
                      "    <DimensionLayout dim=\"1\">\n" +
                      "      <Group type=\"103\" groupAlignment=\"0\" attributes=\"0\">\n" +
                      "          <EmptySpace min=\"0\" pref=\"300\" max=\"32767\" attributes=\"0\"/>\n" +
                      "      </Group>\n" +
                      "    </DimensionLayout>\n" +
                      "  </Layout>\n" +
                      "</Form>\n";
        try (OutputStream out = file.getOutputStream();
            Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            w.append(code);
        }

        FormModel form = loadForm(file);

        List<Throwable> errors = new ArrayList<Throwable>();

        new GandalfPersistenceManager().saveForm(file, form, errors);

        if (errors.size() > 0) {
            System.out.println("There were errors while loading the form: ");
            for (Throwable er : errors) {
                er.printStackTrace();
            }
        }

        String content = file.asText("UTF-8");

        assertEquals(code, content);
    }

    private void hackFormLAF(boolean b) {
        try {
            Field f1 = FormLAF.class.getDeclaredField("preview"); // NOI18N
            Field f2 = FormLAF.class.getDeclaredField("lafBlockEntered"); // NOI18N
            f1.setAccessible(true);
            f2.setAccessible(true);
            f1.setBoolean(null, b);
            f2.setBoolean(null, b);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private FormModel loadForm(final FileObject file) {
        final FormModel[] fm = new FormModel[1];
        final Exception[] failure = new Exception[1];
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        hackFormLAF(true);
                        List<Throwable> errors = new ArrayList<Throwable>();

                        fm[0] = new GandalfPersistenceManager().loadForm(file, file, null, errors);

                        if (errors.size() > 0) {
                            System.out.println("There were errors while loading the form: ");
                            for (Throwable er : errors) {
                                er.printStackTrace();
                            }
                        }
                    } catch (PersistenceException pe) {
                        failure[0] = pe;
                    } finally {
                        hackFormLAF(false);
                    }
                }
            });
        } catch (Exception ex) {
            fail(ex.toString());
        }
        if (failure[0] != null) {
            fail(failure[0].toString());
        }

        return fm[0];
    }

}
