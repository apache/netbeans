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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MenuProfileActionsCheck extends NbTestCase {

    public MenuProfileActionsCheck(String name) {
        super(name);
    }

    public void testGetAll() throws Exception {
        clearWorkDir();
       
        FileObject orig = FileUtil.getConfigFile("Menu/Profile");
        assertNotNull("actions folder is there", orig);

        Enumeration<? extends FileObject> nodes = Enumerations.singleton(orig);
        class VisitOriginalFile implements Enumerations.Processor<FileObject,FileObject> {
            public FileObject process(FileObject fo, Collection<FileObject> toAdd) {
                Object attr = fo.getAttribute("originalFile");
                if (fo.isFolder()) {
                    toAdd.addAll(Arrays.asList(fo.getChildren()));
                    return fo;
                }
                if (fo.getPath().startsWith("Menu/Profile/") && !Boolean.TRUE.equals(fo.getAttribute("ergonomics"))) {
                    return null;
                }
                if (attr instanceof String) {
                    FileObject originalFile = FileUtil.getConfigFile((String)attr);
                    assertNotNull("Original file for " + attr + " found", originalFile);
                    toAdd.add(originalFile);
                }
                return fo;
            }
        }


        StringBuilder errors = new StringBuilder();
        Enumeration<? extends FileObject> en = Enumerations.removeNulls(
            Enumerations.queue(nodes, new VisitOriginalFile())
        );
        int all = 0;
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            if (fo.isFolder()) {
                if (all > 0) {
                    continue;
                }
                // check the root folder for its attributes
            }

            all++;
            int cnt = 0;
            Enumeration<String> allAttributes = fo.getAttributes();
            while (allAttributes.hasMoreElements()) {
                String name = allAttributes.nextElement();
                Object attr = fo.getAttribute(name);
                if (attr == null) {
                    fail("fo: " + fo + " has null " + name + " attribute");
                }
                System.setProperty(dynAttr + fo.getPath() + "@" + name, attr.toString());
                cnt++;

                if (attr instanceof URL) {
                    URL u = (URL) attr;
                    int read = -1;
                    try {
                        read = u.openStream().read(new byte[4096]);
                    } catch (IOException ex) {
                        errors.append(ex.getMessage()).append('\n');
                    }
                    if (read <= 0) {
                        errors.append("Resource shall exist: " + fo + " attr: " + name + " value: " + attr + "\n");
                    }
                }
            }
            System.setProperty(dynVery + fo.getPath(), String.valueOf(cnt));
            String locName = getDisplayName(fo);
            System.setProperty(dynName + fo.getPath(), locName);
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
        if (all < 3) {
            fail("Not enough suitable files found under profiler menu: " + all);
        }
    }
    private static final String dynName = "menuprofiledynamic/name/";
    private static final String dynVery = "menuprofiledynamic/verify/";
    private static final String dynAttr = "menuprofiledynamic/attr/";

    public void testCheckAllPretest() throws Exception {
        testCheckAllReal();
    }
    public void testCheckAllReal() throws Exception {
        Map<String, List<String>> filesAndAttribs = new TreeMap<String, List<String>>();
        for (Object o : System.getProperties().keySet()) {
            String f = (String)o;
            if (f.startsWith(dynVery)) {
                int cnt = Integer.parseInt(System.getProperties().getProperty(f));
                f = f.substring(dynVery.length());
                ArrayList<String> arr = new ArrayList<String>();
                String pref = dynAttr + f + "@";
                for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                    if (entry.getKey() instanceof String) {
                        String a = (String) entry.getKey();
                        if (a.startsWith(pref)) {
                            a = a.substring(pref.length());
                            arr.add(a);
                        }
                    }
                }
                if (arr.size() != cnt) {
                    fail("There shall be " + cnt + " attributes for " + f);
                }
                filesAndAttribs.put(f, arr);
            }
        }
        if (filesAndAttribs.size() == 0) {
            fail("Some properties shall be set: " + System.getProperties());
        }

        Iterator<? extends String> allTemplates = filesAndAttribs.keySet().iterator();
        StringBuilder errors = new StringBuilder();
        while (allTemplates.hasNext()) {
            String fo = allTemplates.next();
            FileObject clone = FileUtil.getConfigFile(fo);

            if (clone == null) {
                errors.append("Both files exist: " + fo + "\n");
                continue;
            }
            Enumeration<String> allAttributes = Collections.enumeration(filesAndAttribs.get(fo));
            while (allAttributes.hasMoreElements()) {
                String name = allAttributes.nextElement();
                Object attr = clone.getAttribute(name);
                if (attr == null && "templateWizardIterator".equals(name)) {
                    attr = clone.getAttribute("instantiatingIterator");
                }

                if (attr == null) {
                    errors.append("Attribute " + name + " present in orig on " + fo + " but null in clone\n");
                    continue;
                }

                if (name.equals("iconResource") || name.equals("iconBase")) {
                    Object val = Thread.currentThread().getContextClassLoader().getResource((String)attr);
                    if (!(val instanceof URL)) {
                        errors.append(name + " attr for " + fo + " shall exist "  + attr + "\n");
                    }
                    attr = val;
                }
                
                if (attr instanceof URL) {
                    URL u = (URL) attr;
                    int read = -1;
                    try {
                        read = u.openStream().read(new byte[4096]);
                    } catch (IOException ex) {
                        errors.append(ex.getMessage()).append('\n');
                    }
                    if (read <= 0) {
                        errors.append("Resource shall exist: " + fo + " attr: " + name + " value: " + attr + "\n");
                    }
                }
            }
            allTemplates.remove();
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }

        if (!filesAndAttribs.isEmpty()) {
            fail("All should be empty: " + filesAndAttribs);
        }
    }

    private static String getDisplayName(FileObject f) throws FileStateInvalidException {
        return f.getFileSystem().getDecorator().annotateName(
            f.getNameExt(), Collections.<FileObject>singleton(f)
        );
    }
}
