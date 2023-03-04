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

package org.netbeans.modules.hudson.ui.impl;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.hudson.ui.impl.PlainLogger.PlainLoggerLogic;

public class PlainLoggerTest extends NbTestCase {

    public PlainLoggerTest(String n) {
        super(n);
    }

    public void testPlainLogger() throws Exception {
        PlainLoggerLogic logger = new PlainLoggerLogic(null, "myprj");
        assertEquals("null", String.valueOf(logger.findHyperlink("some random text...")));
        assertEquals("pom.xml:4:-1:stupid error", String.valueOf(logger.findHyperlink("/hudson/work/jobs/myprj/workspace/pom.xml:5: stupid error")));
        assertEquals("src/X.java:-1:-1:uncompilable", String.valueOf(logger.findHyperlink("[javac] /w/jobs/myprj/workspace/src/X.java: warning: uncompilable")));
        assertEquals("src/X.java:-1:-1:uncompilable", String.valueOf(logger.findHyperlink(
                "  [javac] /w/jobs/myprj/workspace/src/X.java: warning: uncompilable")));
        assertEquals("src/main/java/p/C.java:17:19:[deprecation] toURL() in java.io.File has been deprecated",
                String.valueOf(logger.findHyperlink("[WARNING] /w/jobs/myprj/workspace/src/main/java/p/C.java:[18,20] " +
                "[deprecation] toURL() in java.io.File has been deprecated")));
        assertEquals("http://nowhere.net/", String.valueOf(logger.findHyperlink("http://nowhere.net/")));
        assertEquals("null", String.valueOf(logger.findHyperlink("see http://nowhere.net/ for more")));
        assertEquals("pom.xml:4:-1:stupid error", String.valueOf(logger.findHyperlink("/hudson/workspace/myprj/pom.xml:5: stupid error"))); // slave WS
        assertEquals("myprj/src/X.java:-1:-1:uncompilable", String.valueOf(logger.findHyperlink("[javac] /w/jobs/myprj/workspace/myprj/src/X.java: warning: uncompilable")));
    }

}
