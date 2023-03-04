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
package org.netbeans.modules.editor.tools.storage.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author lahvac
 */
public class ToolPreferencesTest extends NbTestCase {
    
    public ToolPreferencesTest(String testName) {
        super(testName);
    }
    
    public void testWriteToPreferences() throws Exception {
        clearWorkDir();
        File wd = getWorkDir();
        File settingsFile = new File(wd, "settings.xml");
        ToolPreferences prefs = ToolPreferences.from(settingsFile.toURI());
        assertNull(prefs.getPreferences("test", "text/x-test").get("test", null));
        prefs.getPreferences("test", "text/x-test").put("test", "testValue");
        assertEquals("testValue", ToolPreferences.from(settingsFile.toURI()).getPreferences("test", "text/x-test").get("test", null));
        prefs.save();
        assertEquals("testValue", ToolPreferences.from(settingsFile.toURI()).getPreferences("test", "text/x-test").get("test", null));
        
        Reference<ToolPreferences> ref = new WeakReference<>(prefs);
        prefs = null;
        assertGC("Must not hold onto the ToolPreferences", ref);
    }
    
    public void testEscaping() throws Exception {
        clearWorkDir();
        File wd = getWorkDir();
        File settingsFile = new File(wd, "settings.xml");
        ToolPreferences prefs = ToolPreferences.from(settingsFile.toURI());
        String key = "\"'<>&";
        String value = "&><'\"";
        assertNull(prefs.getPreferences("test", "text/x-test").get(key, null));
        prefs.getPreferences("test", "text/x-test").put(key, value);
        assertEquals(value, ToolPreferences.from(settingsFile.toURI()).getPreferences("test", "text/x-test").get(key, null));
        prefs.save();
    }
    
    public void testDontSaveEmptyNodes() throws Exception {
        clearWorkDir();
        File wd = getWorkDir();
        File settingsFile = new File(wd, "settings.xml");
        ToolPreferences prefs = ToolPreferences.from(settingsFile.toURI());
        Preferences p = prefs.getPreferences("test", "text/x-test");
        p.node("a/b/e/f");
        p.node("a/b/c/d").put("test", "test");
        prefs.save();
        StringBuilder content = new StringBuilder();
        try (Reader r = new InputStreamReader(new FileInputStream(settingsFile), StandardCharsets.UTF_8)) {
            int read;
            
            while ((read = r.read()) != (-1)) {
                content.append((char) read);
            }
        }
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<!DOCTYPE configuration PUBLIC \"-//NetBeans//DTD Tool Configuration 1.0//EN\" \"http://www.netbeans.org/dtds/ToolConfiguration-1_0.dtd\">\n" +
                     "<configuration>\n" +
                     "    <tool kind=\"test\" type=\"text/x-test\">\n" +
                     "        <node name=\"a\">\n" +
                     "            <node name=\"b\">\n" +
                     "                <node name=\"c\">\n" +
                     "                    <node name=\"d\">\n" +
                     "                        <attribute name=\"test\" value=\"test\"/>\n" +
                     "                    </node>\n" +
                     "                </node>\n" +
                     "            </node>\n" +
                     "        </node>\n" +
                     "    </tool>\n" +
                     "</configuration>\n",
                     content.toString().replace("\r", ""));
    }
    
}
