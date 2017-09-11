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
import java.util.logging.Level;
import org.netbeans.junit.*;
import java.util.*;
import java.util.logging.Logger;
import org.openide.util.RequestProcessor;
import static org.netbeans.CLIHandlerTest.*;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class CLIHandlerReadingOfInputTest extends NbTestCase {

    final static ByteArrayInputStream nullInput = new ByteArrayInputStream(new byte[0]);
    final static ByteArrayOutputStream nullOutput = new ByteArrayOutputStream();
    
    private Logger LOG;

    public CLIHandlerReadingOfInputTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
        
        super.setUp();

        // all handlers shall be executed immediatelly
        CLIHandler.finishInitialization (false);
        
        // setups a temporary file
        String p = getWorkDirPath ();
        if (p == null) {
            p = System.getProperty("java.io.tmpdir");
        }
        String tmp = p;
        assertNotNull(tmp);
        System.getProperties().put("netbeans.user", tmp);
        
        File f = new File(tmp, "lock");
        if (f.exists()) {
            assertTrue("Clean up previous mess", f.delete());
            assertTrue(!f.exists());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        CLIHandler.stopServer();
    }
    
    protected @Override Level logLevel() {
        return Level.FINEST;
    }

    protected @Override int timeOut() {
        return 50000;
    }
    
    public void testReadingOfInputWorksInHandler() throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            private byte[] arr;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                try {
                    InputStream is = args.getInputStream();
                    arr = new byte[is.available() / 2];
                    if (arr.length > 0) {
                        assertEquals("Read amount is the same", arr.length, is.read(arr));
                    }
                    is.close();
                } catch (IOException ex) {
                    fail("There is an exception: " + ex);
                }
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        H h2 = new H();
        
        // why twice? first attempt is direct, second thru the socket server
        for (int i = 0; i < 2; i++) {
            CLIHandler.Status res = cliInitialize(
                new String[0], new H[] { h1, h2 }, new ByteArrayInputStream(template), nullOutput, nullOutput);
            
            assertNotNull("Attempt " + i + ": " + "Can be read", h1.arr);
            assertEquals("Attempt " + i + ": " + "Read two bytes", 2, h1.arr.length);
            assertEquals("Attempt " + i + ": " + "First is same", template[0], h1.arr[0]);
            assertEquals("Attempt " + i + ": " + "Second is same", template[1], h1.arr[1]);
            
            assertNotNull("Attempt " + i + ": " + "Can read as well", h2.arr);
            assertEquals("Attempt " + i + ": " + "Just one char", 1, h2.arr.length);
            assertEquals("Attempt " + i + ": " + "And is the right one", template[2], h2.arr[0]);
            
            h1.arr = null;
            h2.arr = null;
        }
    }
}
