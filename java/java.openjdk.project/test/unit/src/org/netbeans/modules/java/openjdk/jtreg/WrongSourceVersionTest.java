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
package org.netbeans.modules.java.openjdk.jtreg;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.HintContext;

/**
 *
 * @author jlahoda
 */
public class WrongSourceVersionTest {
    
    public WrongSourceVersionTest() {
    }
    
    @Test
    public void testNoRefOutput() throws Exception {
        HintTest.create()
                .input("/*@test\n" +
                       " *@compile --enable-preview -source 14 Test.java\n" +
                       " */\n" +
                       "class Test {\n" +
                       "}\n")
                .run(WrongSourceVersion.class)
                .findWarning("1:36-1:38:verifier:" + Bundle.ERR_HardcodedSource())
                .applyFix()
                .assertVerbatimOutput("/*@test\n" +
                                      " *@compile --enable-preview -source ${jdk.version} Test.java\n" +
                                      " */\n" +
                                      "class Test {\n" +
                                      "}\n");
    }
}
