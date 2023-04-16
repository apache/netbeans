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
package org.netbeans.modules.rust.grammar.antlr4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.rust.grammar.antlr4.RustParser.CrateContext;

/**
 *
 * @author antonio
 */
public class RustParserTest extends NbTestCase {

    public RustParserTest(String testName) {
        super(testName);
    }

   @Test
    public void testShouldParse_shl_shr_rs() throws Exception {
        System.out.println("testShouldParse_shl_shr_rs");
        RustTestUtils.parseFile(getDataDir(), "shl_shr.rs", null);
    }

}
