/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.test.ide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;

/**
 * Threads test
 * see details on http://wiki.netbeans.org/FitnessViaWhiteAndBlackList
 *
 * @author mrkam@netbeans.org
 */
public class ThreadsTest extends JellyTestCase {

    private Set<String> allowedThreads;

    public ThreadsTest(String name) throws IOException {
        super(name);
        allowedThreads = new HashSet<String>();
        InputStream is = ThreadsTest.class.getResourceAsStream("allowed-threads.txt");
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        for (;;) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("#")) {
                continue;
            }
            allowedThreads.add(line);
        }
        // System threads
        allowedThreads.add("Finalizer");
        allowedThreads.add("AWT-Windows");
    }
    
    public static Test suite() throws IOException {

        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
            ThreadsTest.class
        ).clusters(".*").enableModules(".*").gui(true).reuseUserDir(false)
        .honorAutoloadEager(true);

        conf = conf.addTest("testThreads");
        
        return conf.suite();
    }

    public void testThreads() throws Exception {
        try {
            assertThreads();
        } catch (Error e) {
            e.printStackTrace(getLog("threads-report.txt"));
            throw e;
        }
    }

    public void assertThreads() {
        Map<Thread, StackTraceElement[]> data = Thread.getAllStackTraces();
        StringWriter msgs = new StringWriter();
        boolean fail = false;
        msgs.append("assertThreads:\n");
        for (Thread t : data.keySet()) {
            if (!acceptThread(t, data.get(t))) {
                msgs.append("assertThread: ").append(t.getName()).append('\n');
                for (StackTraceElement s : data.get(t)) {
                    msgs.append("    ").append(s.toString()).append('\n');
                }
                fail = true;
            }
        }
        assertFalse(msgs.toString(), fail);
    }

    public boolean acceptThread(Thread t, StackTraceElement[] stack) {
        if (allowedThreads.contains(t.getName())) {
            return true;
        }
        for (StackTraceElement elem : stack) {
            if (elem.toString().startsWith("org.openide.util.RequestProcessor$Processor.run")) {
                return true;
            }
            if (elem.toString().startsWith("java.util.TimerThread.run")) {
                return true;
            }
            if (elem.toString().startsWith("java.util.prefs.AbstractPreferences$EventDispatchThread")) {
                return true;
            }
            if (elem.toString().startsWith("sun.awt.image.ImageFetcher.run")) {
                return true;
            }
            if (elem.toString().startsWith("sun.awt.X11.XToolkit")) {
                return true;
            }
            if (elem.toString().startsWith("sun.java2d.d3d.D3DScreenUpdateManager")) {
                return true;
            }
        }
        return false;
    }

}

