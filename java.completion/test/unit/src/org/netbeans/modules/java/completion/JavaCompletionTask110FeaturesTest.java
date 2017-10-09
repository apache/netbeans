/**
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
package org.netbeans.modules.java.completion;

import javax.lang.model.SourceVersion;
import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionTask110FeaturesTest extends CompletionTestBase {

    public JavaCompletionTask110FeaturesTest(String testName) {
        super(testName);
    }

    // Java 1.10 var tests -------------------------------------------

    public void testVar() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_10");
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_10, skip test:
            return ;
        }
        //TODO: should not propose "null"!
        performTest("Method", 125, "var v =", "emptyVar.pass", "1.10");
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
