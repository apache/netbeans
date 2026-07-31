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
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 * @author nbalyamm
 */
public class MutableMethodsOnImmutableCollectionsTest extends NbTestCase {

    public MutableMethodsOnImmutableCollectionsTest(String name) {
        super(name);
    }

    public void testCaseWithMutlipleVariablesAndNoAssigmentChange() throws Exception {

        HintTest
                .create()
                .input("""
                        package test;
                                               
                        import java.util.*;
                                               
                        public class Test {
                        		private void test () {
                                   var l=List.of("foo","bar");
                                   var l2=List.of("fool2","barl2");
                                   l.add("bar2");
                                   l.clear();  
                                   l2.clear();                                                          
                                }
                                               
                        }
                        """)
                .sourceLevel(10)
                .run(MutableMethodsOnImmutableCollections.class)
                .assertWarnings(
                        "8:13-8:16:warning:Attempting to modify an immutable List created via List.of()",
                        "9:13-9:18:warning:Attempting to modify an immutable List created via List.of()",
                        "10:14-10:19:warning:Attempting to modify an immutable List created via List.of()");
    }

    public void testCaseWithAssignmentChange() throws Exception {

        HintTest
                .create()
                .input("""
                        package test;
                                               
                        import java.util.*;
                                               
                        public class Test {
                        		private void test () {
                                   var l=List.of("foo","bar");
                                   var l2=List.of("foo2","bar2");
                                   l.add("bar2");
                                   l.clear();                           
                                   l2.clear();                                                          
                                   l2 = new ArrayList();
                                   l2.clear();
                                   l2 = List.of("foo3","bar3");
                                   l2.clear();
                                   l2 = l;
                                   l2.clear();
                                   if(true){
                                     l.clear();
                                   }
                                   List<String> l3 = new ArrayList<String>();
                                   l3 = l2;
                                   l3.clear();
                                   List<String> l4 = new ArrayList<String>();
                                   l4 = l3;
                                   l4.clear();
                                   var s1 = Set.of("sfoo1","sbar1");
                                   s1.clear();
                                }
                        }
                        """)
                .sourceLevel(10)
                .run(MutableMethodsOnImmutableCollections.class)
                .assertWarnings(
                        "8:13-8:16:warning:Attempting to modify an immutable List created via List.of()",
                        "9:13-9:18:warning:Attempting to modify an immutable List created via List.of()",
                        "10:14-10:19:warning:Attempting to modify an immutable List created via List.of()",
                        "14:14-14:19:warning:Attempting to modify an immutable List created via List.of()",
                        "16:14-16:19:warning:Attempting to modify an immutable List created via List.of()",
                        "18:15-18:20:warning:Attempting to modify an immutable List created via List.of()",
                        "22:14-22:19:warning:Attempting to modify an immutable List created via List.of()",
                        "25:14-25:19:warning:Attempting to modify an immutable List created via List.of()",
                        "27:14-27:19:warning:Attempting to modify an immutable Set created via Set.of()"
                );
    }
}
