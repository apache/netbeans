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
package org.netbeans.modules.java.hints.jdk;

import javax.lang.model.SourceVersion;
import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

/* TODO to make this test work:
   - to ensure that the newest Java language features supported by the IDE are available,
     regardless of which JDK you build the module with:
   -- for Ant-based modules, add "requires.nb.javac=true" into nbproject/project.properties
   -- for Maven-based modules, use dependency:copy in validate phase to create
      target/endorsed/org-netbeans-libs-javacapi-*.jar and add to endorseddirs
      in maven-compiler-plugin and maven-surefire-plugin configuration
      See: http://wiki.netbeans.org/JavaHintsTestMaven
 */
public class AddSealedForClassTest {

    @Test
    public void testWarningProduced() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_15");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return;
        }
        HintTest.create()
                .input("class parentClass {\n"
                        + "    \n"
                        + "}\n"
                        + "final class b extends parentClass{}")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(AddSealedForClass.class)
                .assertWarnings("");
    }

    @Test
    public void testFixWorking() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_15");
        } catch (IllegalArgumentException ex) {
            //OK, skip test
            return;
        }
        HintTest.create()
                .input("package com.sun.aks.pack1;\n"
                        + "\n"
                        + "class parentClass {\n"
                        + "    \n"
                        + "}\n"
                        + "final class b extends parentClass{}")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(AddSealedForClass.class)
                .findWarning("2:2-2:10:verifier:" + Bundle.ERR_AddSealedForClass())
                .applyFix()
                .assertCompilable()
                //TODO: change to match expected output
                .assertOutput("package com.sun.aks.pack1;\n"
                        + "\n"
                        + "sealed class parentClass permits com.sun.aks.pack1.b{\n"
                        + "    \n"
                        + "}\n"
                        + "final class b extends parentClass{}");
    }
}
