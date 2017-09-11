/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
                "Change Method Signature from method(int i) to method(int i, String hello_World)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.AddParameter",
                    215,  new HashSet<String>(Arrays.asList(
                "Change Method Signature from method(int i) to method(String hello_World, int i)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.AddParameter",
                    250,  new HashSet<String>(Arrays.asList(
                "Change Method Signature from method(int i) to method(String hello_World, int i, String string)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.AddParameter",
                    290,  new HashSet<String>(Arrays.asList(
                "Change Method Signature from method(int i) to method(int i, int par1, int par2)"
        )));
    }
    
    public void test201360() throws Exception { // NullPointerException at org.netbeans.modules.java.hints.errors.ChangeMethodParameters.analyze
        performTestAnalysisTest("org.netbeans.test.java.hints.Test201360",
                    205,  new HashSet<String>(Arrays.asList(
                "Change Method Signature from getColor(int number) to getColor()", "Change Method Signature from getColor(int number, Color failedColor) to getColor()"
        )));
    }
    
    public void test204556() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.Test204556",
                    130,  new HashSet<String>(Arrays.asList(
                "Change Method Signature from getColor(boolean b) to getColor(T b)"
        )));
    }
    
    public void testReorderParameter() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.ReorderParameter",
                    312,  new HashSet<String>(Arrays.asList(
                "Change Method Signature from method(int i, String a, Object o, boolean b) to "
                + "method(boolean b, String a, Object o, int i)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.ReorderParameter",
                    345,  new HashSet<String>(Arrays.asList(
                "Change Method Signature from method(int i, String a, Object o, boolean b) to "
                + "method(String a, Object o, boolean b, int i)"
        )));
    }
    
    public void testRemoveParameter() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.RemoveParameter",
                    312,  new HashSet<String>(Arrays.asList(
                "Change Method Signature from method(int i, String a, Object o, boolean b) to "
                + "method(int i, String a, Object o)"
        )));
        performTestAnalysisTest("org.netbeans.test.java.hints.RemoveParameter",
                    340,  new HashSet<String>(Arrays.asList(
                "Change Method Signature from method(int i, String a, Object o, boolean b) to "
                + "method(String a)"
        )));
    }
    
    public void test200235() throws Exception {
        performTestAnalysisTest("org.netbeans.test.java.hints.AbstractMethod",
                    195,  new HashSet<String>(Arrays.asList(
                "Change Method Signature from method(int i) to method(int i, String hello_World)"
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
