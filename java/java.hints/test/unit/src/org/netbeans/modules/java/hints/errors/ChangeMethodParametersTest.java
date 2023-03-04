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
package org.netbeans.modules.java.hints.errors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;

/**
 *
 * @author Ralph Ruijs
 */
public class ChangeMethodParametersTest extends HintsTestBase {

    public ChangeMethodParametersTest(String name) {
        super(name);
    }
    
    public void testConstructor() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Constructor",
                    180,  new HashSet<String>(Arrays.asList(
                "Change constructor from Constructor(int i) to Constructor(int i, String hello_World)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.Constructor",
                    226,  new HashSet<String>(Arrays.asList(
                "Change constructor from Constructor(int i) to Constructor(String hello_World, int i)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.Constructor",
                    272,  new HashSet<String>(Arrays.asList(
                "Change constructor from Constructor(int i) to Constructor(String hello_World, int i, String string)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.Constructor",
                    323,  new HashSet<String>(Arrays.asList(
                "Change constructor from Constructor(int i) to Constructor(int i, int par1, int par2)"
        )));
    }
    
    public void testAddParameter() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.AddParameter",
                    180,  new HashSet<String>(Arrays.asList(
                "Change method from method(int i) to method(int i, String hello_World)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.AddParameter",
                    215,  new HashSet<String>(Arrays.asList(
                "Change method from method(int i) to method(String hello_World, int i)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.AddParameter",
                    250,  new HashSet<String>(Arrays.asList(
                "Change method from method(int i) to method(String hello_World, int i, String string)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.AddParameter",
                    290,  new HashSet<String>(Arrays.asList(
                "Change method from method(int i) to method(int i, int par1, int par2)"
        )));
    }
    
    public void test201360() throws Exception { // NullPointerException at org.netbeans.modules.java.hints.errors.ChangeMethodParameters.analyze
        performTestAnalysisTest("org.netbeans.test.java.hints.Test201360",
                    205,  new HashSet<String>(Arrays.asList(
                "Change method from getColor(int number) to getColor()", "Change method from getColor(int number, Color failedColor) to getColor()"
        )));
    }
    
    public void test204556() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Test204556",
                    130,  new HashSet<String>(Arrays.asList(
                "Change method from getColor(boolean b) to getColor(T b)"
        )));
    }
    
    public void testReorderParameter() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.ReorderParameter",
                    312,  new HashSet<String>(Arrays.asList(
                "Change method from method(int i, String a, Object o, boolean b) to "
                + "method(boolean b, String a, Object o, int i)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.ReorderParameter",
                    345,  new HashSet<String>(Arrays.asList(
                "Change method from method(int i, String a, Object o, boolean b) to "
                + "method(String a, Object o, boolean b, int i)"
        )));
    }
    
    public void testRemoveParameter() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.RemoveParameter",
                    312,  new HashSet<String>(Arrays.asList(
                "Change method from method(int i, String a, Object o, boolean b) to "
                + "method(int i, String a, Object o)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.RemoveParameter",
                    340,  new HashSet<String>(Arrays.asList(
                "Change method from method(int i, String a, Object o, boolean b) to "
                + "method(String a)"
        )));
    }
    
    public void test200235() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.AbstractMethod",
                    195,  new HashSet<String>(Arrays.asList(
                "Change method from method(int i) to method(int i, String hello_World)"
        )));
    }

    @Override
    protected boolean createCaches() {
        return false;
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/ChangeMethodParametersTest/";
    }
    
    protected void performTestAnalysisTest(String className, int offset, Set<String> golden) throws Exception {
        prepareTest(className);

        DataObject od = DataObject.find(info.getFileObject());
        EditorCookie ec = (EditorCookie) od.getLookup().lookup(EditorCookie.class);

        Document doc = ec.openDocument();

        ChangeMethodParameters cmp = new ChangeMethodParameters();
        List<Fix> fixes = cmp.run(info, null, offset, null, null);
        Set<String> real = new HashSet<String>();

        for (Fix f : fixes) {
            if (f instanceof ChangeParametersFix) {
                real.add(((ChangeParametersFix) f).getText());
                continue;
            }
        }

        assertEquals(golden, real);
    }
}
