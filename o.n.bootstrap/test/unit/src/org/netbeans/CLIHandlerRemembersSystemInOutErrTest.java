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

package org.netbeans;

import java.io.*;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class CLIHandlerRemembersSystemInOutErrTest extends NbTestCase {

    private static ByteArrayInputStream in = new ByteArrayInputStream("Ahoj".getBytes());
    private static PrintStream out = new PrintStream(new ByteArrayOutputStream());
    private static PrintStream err = new PrintStream(new ByteArrayOutputStream());
    private static String[] args = { "AnArg" };
    private static String curDir = "curDir";
    
    private Logger LOG;

    static {
        System.setIn(in);
        System.setErr(err);
        System.setOut(out);
    }
    
    public CLIHandlerRemembersSystemInOutErrTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        System.setProperty ("netbeans.user", getWorkDirPath());
        LOG = Logger.getLogger("TEST-" + getName());
    }

    protected @Override Level logLevel() {
        return Level.ALL;
    }
    
    public void testFileExistsButItCannotBeRead() throws Exception {
        // just initialize the CLIHandler
        CLIHandler.Args a = new CLIHandler.Args(args, in, out, err, curDir);

        ArrayList<CLIHandler> arr = new ArrayList<CLIHandler>();
        arr.add(new H(H.WHEN_BOOT));
        arr.add(new H(H.WHEN_INIT));

        // now change the System values
        ByteArrayInputStream in2 = new ByteArrayInputStream("NeverBeSeen".getBytes());
        PrintStream out2 = new PrintStream(new ByteArrayOutputStream());
        PrintStream err2 = new PrintStream(new ByteArrayOutputStream());

        System.setIn(in2);
        System.setErr(err2);
        System.setOut(out2);

        LOG.info("before initialized");
        CLIHandler.initialize(a, null, arr, false, true, null);
        LOG.info("after initialize");
        assertEquals("One H called", 1, H.cnt);
        LOG.info("before finishInitialization");
        CLIHandler.finishInitialization(false);
        LOG.info("after finishInitialization");
        assertEquals("Both Hs called", 2, H.cnt);
    }

    private static final class H extends CLIHandler {
        static int cnt;

        public H(int w) {
            super(w);
        }

        protected int cli(CLIHandler.Args a) {
            cnt++;

            assertEquals("Same arg", Arrays.asList(args), Arrays.asList(a.getArguments()));
            assertEquals("same dir", curDir, a.getCurrentDirectory().toString());
            assertEquals("same in", in, a.getInputStream());
            assertEquals("same out", out, a.getOutputStream());
            assertEquals("same err", err, a.getErrorStream());

            return 0;
        }

        protected void usage(PrintWriter w) {
        }
    }
}
