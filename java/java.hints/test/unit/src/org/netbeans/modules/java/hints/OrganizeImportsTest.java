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
package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class OrganizeImportsTest extends NbTestCase {
    
    public OrganizeImportsTest(String name) {
        super(name);
    }
    
    public void testSimple() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.List;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     List l = new ArrayList();\n" +
                       "}\n")
                .run(OrganizeImports.class)
                .findWarning("1:0-1:22:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("package test;\n" +
                              "import java.util.ArrayList;\n" +
                              "import java.util.List;\n" +
                              "public class Test {\n" +
                              "     List l = new ArrayList();\n" +
                              "}\n");
    }

    public void testClashing() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.awt.*;\n" +
                       "import java.util.List;\n" +
                       "import java.util.*;\n" +
                       "public class Test {\n" +
                       "     List l = new ArrayList();\n" +
                       "     Button b;\n" +
                       "}\n")
                .run(OrganizeImports.class)
                .findWarning("2:0-2:22:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("package test;\n" +
                              "import java.awt.*;\n" +
                              "import java.util.*;\n" +
                              "import java.util.List;\n" +
                              "public class Test {\n" +
                              "     List l = new ArrayList();\n" +
                              "     Button b;\n" +
                              "}\n");
    }
    
    public void testModuleSimple() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import module java.base;
                       import java.util.ArrayList;
                       public class Test {
                            List l = new ArrayList();
                       }
                       """)
                .run(OrganizeImports.class)
                .findWarning("2:0-2:27:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("""
                              package test;
                              import module java.base;
                              public class Test {
                                   List l = new ArrayList();
                              }
                              """);
    }
    
    public void testModuleSimpleNoUsage() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import module java.base;
                       import java.awt.*;
                       public class Test {
                            java.util.List l = java.util.Arrays.asList(1,2,3);
                            List ui;
                            Button b;
                       }
                       """)
                .run(OrganizeImports.class)
                .findWarning("1:0-1:24:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("""
                              package test;
                              import java.awt.*;
                              public class Test {
                                  java.util.List l = java.util.Arrays.asList(1,2,3);
                                  List ui;
                                  Button b;
                              }
                              """);
    }
    
    public void testModuleTransitiveSimple() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import java.lang.Thread;
                       import module java.desktop;
                       public class Test {
                           java.util.List l = java.util.List.of(XMLConstants.XML_NS_URI);
                           List ui;
                           Button b;
                           Clipboard cp;
                       }
                       """)
                .run(OrganizeImports.class)
                .findWarning("1:0-1:24:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("""
                              package test;
                              import module java.desktop;
                              public class Test {
                                  java.util.List l = java.util.List.of(XMLConstants.XML_NS_URI);
                                  List ui;
                                  Button b;
                                  Clipboard cp;
                              }
                              """);
    }
    
    public void testModuleTransitiveCommon() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import module java.sql;
                       import module java.desktop;
                       public class Test {
                           java.util.List l = java.util.List.of(XMLConstants.XML_NS_URI);
                           List ui;
                           Button b;
                           Clipboard cp;
                       }
                       """)
                .run(OrganizeImports.class)
                .findWarning("1:0-1:23:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("""
                              package test;
                              import module java.desktop;
                              import module java.sql;
                              public class Test {
                                  java.util.List l = java.util.List.of(XMLConstants.XML_NS_URI);
                                  List ui;
                                  Button b;
                                  Clipboard cp;
                              }
                              """);
    }
    
    public void testModuleSimpleAllNamed() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import java.util.List;
                       import module java.base;
                       import java.util.ArrayList;
                       public class Test {
                            List l = new ArrayList();
                       }
                       """)
                .run(OrganizeImports.class)
                .findWarning("1:0-1:22:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("""
                              package test;
                              import java.util.ArrayList;
                              import java.util.List;
                              public class Test {
                                   List l = new ArrayList();
                              }
                              """);
    }
    
    public void testModuleSimpleAllStar() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import java.util.List;
                       import module java.base;
                       import java.util.*;
                       public class Test {
                            List l = new ArrayList();
                       }
                       """)
                .run(OrganizeImports.class)
                .findWarning("1:0-1:22:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("""
                              package test;
                              import java.util.*;
                              public class Test {
                                   List l = new ArrayList();
                              }
                              """);
    }

    public void testModuleSimpleWithStar() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import java.util.List;
                       import module java.base;
                       import java.util.*;
                       public class Test {
                            Consumer<String> print = System.out::println;
                            List<String> l = new ArrayList<>();
                            private void printAll() { l.forEach(print); }
                       }
                       """)
                .run(OrganizeImports.class)
                .findWarning("1:0-1:22:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("""
                              package test;
                              import java.util.*;
                              import module java.base;
                              public class Test {
                                   Consumer<String> print = System.out::println;
                                   List<String> l = new ArrayList<>();
                                   private void printAll() { l.forEach(print); }
                              }
                              """);
    }

    public void testModuleSimpleWithStarAndStatic() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import java.util.List;
                       import module java.base;
                       import static java.util.List.of;
                       import java.util.*;
                       public class Test {
                            Consumer<String> print = System.out::println;
                            List<String> l = new ArrayList<>();
                            private void printAll() { of("abc","def").forEach(print); }
                       }
                       """)
                .run(OrganizeImports.class)
                .findWarning("1:0-1:22:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("""
                              package test;
                              import java.util.*;
                              import static java.util.List.of;
                              import module java.base;
                              public class Test {
                                   Consumer<String> print = System.out::println;
                                   List<String> l = new ArrayList<>();
                                   private void printAll() { of("abc","def").forEach(print); }
                              }
                              """);
    }

    public void testModuleClashing() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import module java.desktop;
                       import java.util.List;
                       import module java.base;
                       public class Test {
                            List l = new ArrayList();
                            Button b;
                       }
                       """)
                .run(OrganizeImports.class)
                .findWarning("1:0-1:27:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("""
                              package test;
                              import java.util.List;
                              import module java.base;
                              import module java.desktop;
                              public class Test {
                                   List l = new ArrayList();
                                   Button b;
                              }
                              """);
    }

    public void testModuleClashing2() throws Exception {
        HintTest.create()
                .sourceLevel(25)
                .input("""
                       package test;
                       import module java.desktop;
                       import java.util.List;
                       import static java.util.List.of;
                       import module java.base;
                       public class Test {
                           List<String> l = new ArrayList<>(of("abc", "xyz", "def"));
                           Button b;
                           public Test() {
                               l.sort(Comparator.naturalOrder());
                           }
                       }
                       """)
                .run(OrganizeImports.class)
                .findWarning("1:0-1:27:verifier:MSG_OragnizeImports")
                .applyFix()
                .assertOutput("""
                              package test;
                              import java.util.List;
                              import static java.util.List.of;
                              import module java.base;
                              import module java.desktop;
                              public class Test {
                                  List<String> l = new ArrayList<>(of("abc", "xyz", "def"));
                                  Button b;
                                  public Test() {
                                      l.sort(Comparator.naturalOrder());
                                  }
                              }
                              """);
    }
}
