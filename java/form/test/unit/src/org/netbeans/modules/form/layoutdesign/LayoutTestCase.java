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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.form.FormLAF;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.GandalfPersistenceManager;
import org.netbeans.modules.form.PersistenceException;
import org.openide.filesystems.FileObject;

public abstract class LayoutTestCase extends TestCase {

    private String testSwitch;

    protected LayoutModel lm = null;
    protected LayoutDesigner ld = null;
    
    protected URL url;
    
    protected FileObject startingFormFile;
    protected File expectedLayoutFile;

    /** False by default - interval attribute values not significant. */
    protected boolean checkAttributes;
    
    protected HashMap contInterior = new HashMap();
    protected HashMap baselinePosition = new HashMap();
    
    protected HashMap prefPaddingInParent = new HashMap();
    protected HashMap prefPadding = new HashMap();
    protected HashMap compBounds = new HashMap();
    protected HashMap compMinSize = new HashMap();
    protected HashMap compPrefSize = new HashMap();
    protected HashMap hasExplicitPrefSize = new HashMap();
    
    protected LayoutComponent lc = null;
    
    protected String goldenFilesPath = "../../../test/unit/data/goldenfiles/";

    protected String className;

    private static final String ATTR_PREFIX = "attributes=";

    public LayoutTestCase(String name) {
        super(name);
        String resName = LayoutTestCase.class.getName().replace('.', '/') + ".class";
        URL url = getClass().getClassLoader().getResource(resName);
        String urlStr = url.toExternalForm();
        try {
            this.url = new URL(urlStr.substring(0, urlStr.indexOf(resName)));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Tests the layout model by loading a form file, add/change some components there,
     * and then compare the results with golden files.
     * In case the dump does not match, it is saved into a file under
     * build/test/unit/results so it can be compared with the golden file manually.namename
     */
    public void testLayout() throws IOException {
        loadForm(startingFormFile);

        Method[] methods = this.getClass().getMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (int i=0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().startsWith("doChanges")) {
                try {
                    String name = getClass().getName();
                    String simpleName = name.substring(name.lastIndexOf('.')+1);
                    System.out.println("Invoking " + simpleName + "." + m.getName());
                    m.invoke(this, null);
                    
                    String methodCount = m.getName().substring(9); // "doChanges".length()
                    
                    String currentLayout = getCurrentLayoutDump();
                    String expectedLayout = getExpectedLayoutDump(methodCount);
                    String lineSep = System.getProperty("line.separator"); // NOI18N
                    if (lineSep.length() > 1) {
                        expectedLayout = expectedLayout.replace(lineSep, "\n"); // NOI18N
                    }

                    System.out.print("Comparing ... ");

                    boolean same = compare(currentLayout, expectedLayout);
                    if (!same) {
                        System.out.println("failed");
                        System.out.println("EXPECTED: ");
                        System.out.println(expectedLayout);
                        System.out.println("");
                        System.out.println("CURRENT: ");
                        System.out.println(currentLayout);
                        writeCurrentWrongLayout(methodCount, currentLayout);
                    }
                    else System.out.println("OK");
                    System.out.println("");

                    assertTrue("Model dump in step " + methodCount + " gives different result than expected", same);
                    
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    fail("Error while invoking method: " + m);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                    fail("Error while invoking method: " + m);
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                    fail("Error while invoking method: " + m);
                }
            }
        }
    }

    private boolean compare(String actual, String expected) {
        String[] lines1 = actual.split("\n");
        String[] lines2 = expected.split("\n");
        if (lines1.length != lines2.length) {
            return false;
        }
        for (int i=0; i < lines1.length; i++) {
            if (!lineContent(lines1[i]).equals(lineContent(lines2[i]))) {
                return false;
            }
        }
        return true;
    }

    private String lineContent(String line) {
        line = line.trim();
        if (!checkAttributes) { // extract attributes
            int i1 = line.indexOf(ATTR_PREFIX);
            if (i1 >= 0) {
                int i2 = i1 + ATTR_PREFIX.length();
                int n = line.length();
                if (i2 < n && line.charAt(i2) == '\"') { // first quotation mark
                    i2++;
                    while (i2 < n) {
                        if (line.charAt(i2) == '\"') { // second quotation mark
                            line = line.substring(0, i1) + line.substring(i2+1);
                            break;
                        }
                        i2++;
                    }
                }
            }
        }
        return line;
    }

    @Override
    protected void setUp() throws Exception {
        testSwitch = System.getProperty(LayoutDesigner.TEST_SWITCH);
        System.setProperty(LayoutDesigner.TEST_SWITCH, "true"); // NOI18N
        hackFormLAF(true);
    }

    @Override
    protected void tearDown() throws Exception {
        if (testSwitch != null)
            System.setProperty(LayoutDesigner.TEST_SWITCH, testSwitch);
        else
            System.getProperties().remove(LayoutDesigner.TEST_SWITCH);
        super.tearDown();
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

    private void loadForm(final FileObject file) {
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

        lm = fm[0].getLayoutModel();

        ld = new LayoutDesigner(lm, new FakeLayoutMapper(fm[0],
                                                         contInterior,
                                                         baselinePosition,
                                                         prefPaddingInParent,
                                                         compBounds,
                                                         compMinSize,
                                                         compPrefSize,
                                                         hasExplicitPrefSize,
                                                         prefPadding));
        ld.setActive(true);
    }
    
    private String getCurrentLayoutDump() {
        return lm.dump(null);
    }
    
    private String getExpectedLayoutDump(String methodCount) throws IOException {        
        expectedLayoutFile = new File(url.getFile() + goldenFilesPath + getExpectedResultFileName(methodCount) + ".txt").getCanonicalFile();
        int length = (int) expectedLayoutFile.length();
        FileReader fr = null;
        try {
            fr = new FileReader(expectedLayoutFile);
            char[] buf = new char[length];
            fr.read(buf);
            return new String(buf);
        } catch (IOException ioe) {
            fail(ioe.toString());
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException io) {
                    fail(io.toString());
                }
            }
        }
        return null;
    }

    private String getExpectedResultFileName(String methodCount) {
        return className + "-ExpectedEndModel" + methodCount;
    }

    private void writeCurrentWrongLayout(String methodCount, String dump) throws IOException {
        // will go to form/build/test/unit/results
        File file = new File(url.getFile() + "../results").getCanonicalFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(file, getExpectedResultFileName(methodCount)+".txt");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();

        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(dump);
        }
        finally {
            if (fw != null) {
                fw.close();
            }
        }
    }
    
}
