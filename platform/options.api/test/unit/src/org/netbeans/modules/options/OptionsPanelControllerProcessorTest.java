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

package org.netbeans.modules.options;

import java.io.ByteArrayOutputStream;
import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.openide.util.test.TestFileUtils;

public class OptionsPanelControllerProcessorTest extends NbTestCase {

    public OptionsPanelControllerProcessorTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testBadIconBase() throws Exception {
        File src = new File(getWorkDir(), "src");
        File dest = new File(getWorkDir(), "classes");
        AnnotationProcessorTestUtils.makeSource(src, "p.C",
                "@org.netbeans.spi.options.OptionsPanelController.TopLevelRegistration(iconBase=\"no/such/icon\", categoryName=\"x\", keywords=\"x\", keywordsCategory=\"x\")",
                "public class C extends org.netbeans.spi.options.OptionsPanelController {",
                "    public void update() {}",
                "    public void applyChanges() {}",
                "    public void cancel() {}",
                "    public boolean isValid() {return false;}",
                "    public boolean isChanged() {return false;}",
                "    public org.openide.util.HelpCtx getHelpCtx() {return null;}",
                "    public javax.swing.JComponent getComponent(org.openide.util.Lookup l) {return null;}",
                "    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {}",
                "    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {}",
                "}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("no/such/icon"));
        TestFileUtils.writeFile(new File(src, "no/such/icon"), "whatever");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
    }

}
