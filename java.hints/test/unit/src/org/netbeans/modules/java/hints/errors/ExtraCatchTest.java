/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;

/**
 *
 * @author lahvac
 */
public class ExtraCatchTest extends ErrorHintsTestBase {
    
    public ExtraCatchTest(String name) {
        super(name, ExtraCatch.class);
    }
    
    public void testAlreadyCaught() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        try {\n" +
                       "        } catch (RuntimeException ex) {" +
                       "        } catch (RuntimeException ex) {" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       Bundle.FIX_RemoveCatch(),
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test() {\n" +
                        "        try {\n" +
                        "        } catch (RuntimeException ex) {" +
                        "        }\n" +
                        "    }\n" +
                        "}\n").replaceAll("\\s+", " "));
    }
    
    public void testNotThrown1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        try {\n" +
                       "        } catch (RuntimeException ex) {" +
                       "        } catch (java.io.IOException ex) {" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       Bundle.FIX_RemoveCatch(),
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test() {\n" +
                        "        try {\n" +
                        "        } catch (RuntimeException ex) {" +
                        "        }\n" +
                        "    }\n" +
                        "}\n").replaceAll("\\s+", " "));
    }
    
    /**
     * Checks that alternative is correctly removed, but the multicatch remains
     */
    public void testNotThrownMulti3() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        try {\n" +
                       "            if (Boolean.getBoolean(\"\")) { throw new RuntimeException(); } else { throw new InvocationTargetException(); }\n" +
                       "        } catch (RuntimeException | java.io.IOException | InvocationTargetException ex) {" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       Bundle.FIX_RemoveCatchException("IOException"),
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test() {\n" +
                        "        try {\n" +
                        "            if (Boolean.getBoolean(\"\")) { throw new RuntimeException(); } else { throw new InvocationTargetException(); }\n" +
                        "        } catch (RuntimeException | InvocationTargetException ex) {" +
                        "        }\n" +
                        "    }\n" +
                        "}\n").replaceAll("\\s+", " "));
    }
    
    /**
     * Checks that in 2-alternative multicatch, the catch multicatch is removed, regular catch remains.
     */
    public void testNotThrownMulti2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        try {\n" +
                       "            if (Boolean.getBoolean(\"\")) { throw new RuntimeException(); } \n" +
                       "        } catch (RuntimeException | java.io.IOException ex) {" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       Bundle.FIX_RemoveCatchException("IOException"),
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test() {\n" +
                        "        try {\n" +
                        "            if (Boolean.getBoolean(\"\")) { throw new RuntimeException(); } \n" +
                        "        } catch (RuntimeException ex) {" +
                        "        }\n" +
                        "    }\n" +
                        "}\n").replaceAll("\\s+", " "));
    }

    public void testNotThrownRemoveTry() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        try {\n" +
                       "            System.err.println(1);\n" +
                       "        } catch (java.io.IOException ex) {" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       Bundle.FIX_RemoveCatch(),
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void test() {\n" +
                        "       System.err.println(1);\n" +
                        "    }\n" +
                        "}\n").replaceAll("\\s+", " "));
    }
    
}
