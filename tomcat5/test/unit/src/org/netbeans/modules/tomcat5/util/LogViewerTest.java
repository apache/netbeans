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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.tomcat5.util;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.tomcat5.util.LogSupport.LineInfo;
import org.netbeans.modules.tomcat5.util.LogViewer.ContextLogSupport;

/**
 *
 * @author sherold
 */
public class LogViewerTest extends NbTestCase {
    
    private File datadir;
    
    public LogViewerTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new LogViewerTest("testAnalyzeLine"));
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp ();
        datadir = getDataDir();
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
        
        ContextLogSupport sup = new ContextLogSupport("foo", "bar");
        for (int i = 0; i < log.length; i++) {
            LineInfo nfo = sup.analyzeLine(log[i]);
            assertEquals("Path \"" + nfo.path() + "\" incorrectly recognized from: " + log[i],
                         files[i], nfo.path());
            assertEquals("Line \"" + nfo.line() + "\" incorrectly recognized from: " + log[i],
                         lines[i], nfo.line());
            assertEquals("Message \"" + nfo.message() + "\" incorrectly recognized from: " + log[i],
                         message[i], nfo.message());
        }
    }
    
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
}
