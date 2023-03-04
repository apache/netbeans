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
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class OptionsCheck extends NbTestCase {

    public OptionsCheck(String name) {
        super(name);
    }

    public void testGetKeywords() throws Exception {
        FileObject fo = FileUtil.getConfigFile("OptionsDialog");
        assertNotNull("Dialog data found", fo);
        StringBuilder sb = new StringBuilder();
        Enumeration<? extends FileObject> en = fo.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject f = en.nextElement();
            if (f.isFolder()) {
                continue;
            }
            Enumeration<String> attrs = f.getAttributes();
            while (attrs.hasMoreElements()) {
                String an = attrs.nextElement();
                if (an.startsWith("keywords")) {
                    assertFalse("Should not contain dot: " + an, an.contains("."));
                    final String kwds = getAttr(f, an);
                    System.setProperty("nbkeywords." + an + "." + f.getPath(), kwds);
                }
            }
            System.setProperty("category.nbkeywords." + f.getPath(), getAttr(f, "keywordsCategory"));
            System.setProperty("displayName.nbkeywords." + f.getPath(), getAttr(f, "displayName"));
        }
        if (sb.length() > 0) {
            fail(sb.toString());
        }
    }
    public void testCheckKeywordsPretest() throws Exception {
        verify(false);
    }
    public void testCheckKeywordsReal() throws Exception {
        verify(true);
    }
    
    private void verify(boolean errorOnExtra) throws Exception {
        StringBuilder errors = new StringBuilder();
        for (Object o : System.getProperties().keySet()) {
            String f = (String)o;
            if (f.startsWith("nbkeywords.")) {
                String contents = System.getProperties().getProperty(f);
                f = f.substring("nbkeywords.".length());
                int indx = f.indexOf('.');
                String attr = f.substring(0, indx);
                f = f.substring(indx + 1);
                
                FileObject optn = FileUtil.getConfigFile(f);
                if (f.startsWith("OptionsDialog/Editor")) {
                    // Editor is explicitly hidden in default.xml
                    if (optn != null && errorOnExtra) {
                        errors.append("Should not be visible ").append(optn).append("\n");
                    }
                    continue;
                }
                if (f.startsWith("OptionsDialog/FontsAndColors.instance")) {
                    // also explicitly hidden in default.xml
                    if (optn != null && errorOnExtra) {
                        errors.append("Should not be visible ").append(optn).append("\n");
                    }
                    continue;
                }
                if (optn == null) {
                    errors.append("Missing keywords ").append(f).
                        append(" again: ").
                        append(FileUtil.getConfigFile(f)).append("\n");
                    continue;
                }
                String kwds = getAttr(optn, attr);
                if (kwds != null && kwds.equals(contents)) {
                    String n = getAttr(optn, "displayName");
                    String origN = System.getProperty("displayName.nbkeywords." + optn.getPath());
                    if (origN == null || !origN.equals(n)) {
                        errors.append("Wrong display name of ").append(optn.getNameExt()).
                            append(" old: ").append(origN).
                            append(" new: ").append(n).append("\n");
                    }
                    Object c = getAttr(optn, "keywordsCategory");
                    String origC = System.getProperty("category.nbkeywords." + optn.getPath());
                    if (origC == null || !origC.equals(c)) {
                        errors.append("Wrong category of ").append(optn.getPath()).
                            append(" old: ").append(origC).
                            append(" new: ").append(c).append("\n");
                    }
                    continue;
                }
                errors.append("Wrong option: ").append(optn.getPath()).append("\n");
            }
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
    }

    private String getAttr(FileObject f, final String k) {
        CharSequence log = Log.enable("org.netbeans.core.startup.layers.BinaryFS", Level.INFO);
        String s = (String) f.getAttribute(k);
        if (log.length() > 0) {
            fail("Some errors gettting attribute " + f + "[" + k + "]\n" + log);
        }
        return s == null ? "" : s;
    }
}
