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

package org.netbeans.junit;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.netbeans.junit.internal.NbModuleLogHandler;

/** Is logging working OK?
 */
public class LogTest extends NbTestCase {
    private Logger LOG = Logger.getLogger("my.log.for.test");
    
    public LogTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.OFF;
    }
    
    public void testLogEnable() throws Exception {
        CharSequence seq = Log.enable(LOG.getName(), Level.FINE);

        LOG.setLevel(Level.FINEST);
        LOG.finest("Too finest message to be seen");
        assertEquals(seq.toString(), 0, seq.length());
    }


    public void testLogSurviveRemoval() throws Exception {
        CharSequence seq = Log.enable(LOG.getName(), Level.FINE);

        LogManager.getLogManager().readConfiguration();

        LOG.warning("Look msg");
        if (seq.toString().indexOf("Look msg") == -1) {
            fail(seq.toString());
        }
    }

    public void testNormalize() throws Exception {
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        pw.println("some stuff");
        pw.println((String) null);
        pw.println("found " + new File(getWorkDir(), "some/thing") + " great");
        Object o0 = new Object();
        Object o1 = new Object();
        pw.println("o0=" + o0 + " o1=" + o1);
        pw.println("o0=" + o0);
        class Group {
            @Override public String toString() {
                return String.format("Group@%h", this);
            }
            class Item {
                @Override public String toString() {
                    return String.format("Item@%h:%H", this, Group.this);
                }
            }
        }
        Group g2 = new Group();
        Object i3 = g2.new Item();
        Object i4 = g2.new Item();
        Group g5 = new Group();
        Object i6 = g5.new Item();
        pw.println("g2=" + g2 + " i3=" + i3 + " i4=" + i4);
        pw.println("g5=" + g5 + " i6=" + i6);
        pw.println("i4=" + i4 + " o1=" + o1);
        pw.flush();
        String expect = NbModuleLogHandler.normalize(w.getBuffer(), getWorkDirPath()).replace('\\', '/').replace("\r\n", "\n");
        assertEquals("Original text:\n" + w.getBuffer(), "some stuff\n"
                + "null\n"
                + "found WORKDIR/some/thing great\n"
                + "o0=java.lang.Object@0 o1=java.lang.Object@1\n"
                + "o0=java.lang.Object@0\n"
                + "g2=Group@2 i3=Item@3:2 i4=Item@4:2\n"
                + "g5=Group@5 i6=Item@6:5\n"
                + "i4=Item@4:2 o1=java.lang.Object@1\n", expect);
    }

}
