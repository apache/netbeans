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

package org.openide.util.lookup.implspi;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;

public class ServiceLoaderLineTest extends NbTestCase {

    public ServiceLoaderLineTest(String n) {
        super(n);
    }

    public void testParseAndRewriteLines() throws Exception {
        assertEquals("", parseAndRewrite(""));
        assertEquals("a ", parseAndRewrite("a "));
        assertEquals("a b ", parseAndRewrite("b a "));
        assertEquals("a b ", parseAndRewrite("b a b "));
        assertEquals("a #position=1 c #position=2 b #position=3 ", parseAndRewrite("a #position=1 b #position=3 c #position=2 "));
        assertEquals("b #position=55 a c ", parseAndRewrite("a b #position=55 c "));
        assertEquals("a #position=1 b #position=1 ", parseAndRewrite("b #position=1 a #position=1 "));
        assertEquals("a #-b c d #-e #-f ", parseAndRewrite("d #-e #-f a #-b c "));
    }

    private static String parseAndRewrite(String input) throws IOException {
        SortedSet<ServiceLoaderLine> lines = new TreeSet<ServiceLoaderLine>();
        ServiceLoaderLine.parse(new StringReader(input.replace(' ', '\n')), lines);
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        for (ServiceLoaderLine line : lines) {
            line.write(pw);
        }
        pw.flush();
        return w.toString().replace('\n', ' ').replace("\r", "");
    }

    public static void clearLookupsForPath() {
        NamedServicesProvider.clearCache();
    }
}
