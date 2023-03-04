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

package org.netbeans.modules.tomcat5.util;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.tomcat5.util.LogSupport.LineInfo;
import org.netbeans.modules.tomcat5.util.ServerLog.ServerLogSupport;

/**
 *
 * @author sherold
 */
public class ServerLogTest extends NbTestCase {

    public ServerLogTest(String testName) {
        super(testName);
    }

    public void testAnalyzeLine() {

        String log[] = new String[] {
            "Jan 5, 2006 6:46:45 PM org.apache.catalina.core.StandardWrapperValve invoke",
            "SEVERE: Servlet.service() for servlet HyperlinkTest threw exception",
            "java.lang.IllegalStateException",
            "       at t.HyperlinkTest$1.run(HyperlinkTest.java:24)",
            "       at t.HyperlinkTest.processRequest(HyperlinkTest.java:27)",
            "       at foo.bar",
        };

        String files[] = new String[] {
            null,
            null,
            null,
            "t/HyperlinkTest.java",
            "t/HyperlinkTest.java",
            null,
        };

        int lines[] = new int[] {
            -1,
            -1,
            -1,
            24,
            27,
            -1,
        };

        String message[] = new String[] {
            null,
            null,
            null,
            "java.lang.IllegalStateException",
            "java.lang.IllegalStateException",
            null,
        };

        ServerLogSupport sup = new ServerLogSupport();
        for (int i = 0; i < log.length; i++) {
            LineInfo nfo = sup.analyzeLine(log[i]);
            System.out.println(nfo);
            assertEquals("Path \"" + nfo.path() + "\" incorrectly recognized from: " + log[i],
                         files[i], nfo.path());
            assertEquals("Line \"" + nfo.line() + "\" incorrectly recognized from: " + log[i],
                         lines[i], nfo.line());
            assertEquals("Message \"" + nfo.message() + "\" incorrectly recognized from: " + log[i],
                         message[i], nfo.message());
        }
    }

    public void testAnalyzeLine191810() {
        String log[] = new String[] {
            "java.lang.StringIndexOutOfBoundsException: String index out of range: -1",
            "       at java.lang.String.substring(String.java:1949) ~[na:1.6.0_18]"
        };

        String files[] = new String[] {
            null,
            "java/lang/String.java",
        };

        int lines[] = new int[] {
            -1,
            1949
        };

        String message[] = new String[] {
            null,
            "java.lang.StringIndexOutOfBoundsException: String index out of range: -1"
        };

        ServerLogSupport sup = new ServerLogSupport();
        for (int i = 0; i < log.length; i++) {
            LineInfo nfo = sup.analyzeLine(log[i]);
            System.out.println(nfo);
            assertEquals("Path \"" + nfo.path() + "\" incorrectly recognized from: " + log[i],
                         files[i], nfo.path());
            assertEquals("Line \"" + nfo.line() + "\" incorrectly recognized from: " + log[i],
                         lines[i], nfo.line());
            assertEquals("Message \"" + nfo.message() + "\" incorrectly recognized from: " + log[i],
                         message[i], nfo.message());
        }
    }
}
