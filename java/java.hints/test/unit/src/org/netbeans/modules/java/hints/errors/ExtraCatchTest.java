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
                       "import java.lang.reflect.InvocationTargetException;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        try {\n" +
                       "            if (Boolean.getBoolean(\"\")) { throw new RuntimeException(); } else { throw new InvocationTargetException(null); }\n" +
                       "        } catch (RuntimeException | java.io.IOException | InvocationTargetException ex) {" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       -1,
                       Bundle.FIX_RemoveCatchException("IOException"),
                       ("package test;\n" +
                        "import java.lang.reflect.InvocationTargetException;\n" +
                        "public class Test {\n" +
                        "    public void test() {\n" +
                        "        try {\n" +
                        "            if (Boolean.getBoolean(\"\")) { throw new RuntimeException(); } else { throw new InvocationTargetException(null); }\n" +
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
