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

package org.netbeans.modules.bugtracking.commons;

import org.netbeans.modules.bugtracking.commons.StackTraceSupport;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.bugtracking.commons.StackTraceSupport.StackTracePosition;

/**
 *
 * @author tomas
 */
public class StackTraceSupportTest extends TestCase {

    public void testStackTrace() {
        String prefix = "simply something \n";
        String st ="org.netbeans.ProxyClassLoader.printDefaultPackageWarning(ProxyClassLoader.java:539)";
        List<StackTracePosition> res = StackTraceSupport.find(prefix + st);
        assertEquals(1, res.size());
        assertEquals(prefix.length(), res.get(0).getStartOffset());
        assertEquals(prefix.length() + st.length(), res.get(0).getEndOffset());

        prefix = " got this bloody stacktrace\n";
        st ="   at org.netbeans.ProxyClassLoader.printDefaultPackageWarning(ProxyClassLoader.java:539)\n" +
            "   at org.netbeans.ProxyClassLoader.getResource(ProxyClassLoader.java:312)\n" +
            "   at java.lang.ClassLoader.getResourceAsStream(ClassLoader.java:1214)";
        res = StackTraceSupport.find(prefix + st);
        assertEquals(1, res.size());
        assertEquals(prefix.length(), res.get(0).getStartOffset());
        assertEquals(prefix.length() + st.length(), res.get(0).getEndOffset());

        String prefix1 = " got those 2 stacktraces\nthis one: \n";
        String st1 = "   at org.netbeans.ProxyClassLoader.printDefaultPackageWarning(ProxyClassLoader.java:539)\n" +
            "   at org.netbeans.ProxyClassLoader.getResource(ProxyClassLoader.java:312)\n" +
            "   at java.lang.ClassLoader.getResourceAsStream(ClassLoader.java:1214)\n";
        String prefix2 = "\n\nand this another one: \n";
        String st2 = "   at org.netbeans.ProxyClassLoader.printDefaultPackageWarning(ProxyClassLoader.java:539)\n" +
            "   at org.netbeans.ProxyClassLoader.getResource(ProxyClassLoader.java:312)\n" +
            "   at java.lang.ClassLoader.getResourceAsStream(ClassLoader.java:1214)";

        st = (prefix1 + st1 + prefix2 + st2);
        res = StackTraceSupport.find(st);
        assertEquals(2, res.size());
        assertEquals(prefix1.length(), res.get(0).getStartOffset());
        assertEquals(prefix1.length() + st1.length() - 1, res.get(0).getEndOffset()); // XXX -1 house numero?
        assertEquals(prefix1.length() + st1.length() + prefix2.length(), res.get(1).getStartOffset());
        assertEquals(prefix1.length() + st1.length() + prefix2.length() + st2.length(), res.get(1).getEndOffset());
    }

}
