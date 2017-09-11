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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class MainCLITest extends NbTestCase {
    public MainCLITest (String name) {
        super(name);
    }

    public void testHandlersCanBeInUserDir () throws Exception {
        clearWorkDir ();

        class H extends CLIHandler {
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                String[] arr = args.getArguments ();
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].equals("--userdir")) {
                        System.setProperty ("netbeans.user", arr[i + 1]);
                        return 0;
                    }
                }
                fail ("One of the arguments should be --userdir: " + Arrays.asList (arr));
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        
        File dir = super.getWorkDir ();
        File lib = new File (dir, "core"); 
        lib.mkdirs ();
        File jar = new File (lib, "sample.jar");
        JarOutputStream os = new JarOutputStream (new FileOutputStream (jar));
        os.putNextEntry(new ZipEntry("META-INF/services/org.netbeans.CLIHandler"));
        os.write (TestHandler.class.getName ().getBytes ());
        String res = "/" + TestHandler.class.getName ().replace ('.', '/') + ".class";
        os.putNextEntry(new ZipEntry(res));
        FileUtil.copy(getClass().getResourceAsStream(res), os);
        os.close();
        
        TestHandler.called = false;

        String[] args = {"--userdir", dir.toString()};
        assertFalse ("User dir is not correct. Will be set by org.netbeans.core.CLIOptions", dir.toString ().equals (System.getProperty ("netbeans.user")));
        MainImpl.execute (args, null, null, null, null);
        Main.finishInitialization ();
        assertEquals ("User set", dir.toString (), System.getProperty ("netbeans.user"));
        assertTrue ("CLI Handler from user dir was called", TestHandler.called);
    }

    /** Sample handler
     */
    public static final class TestHandler extends CLIHandler {
        public static boolean called;
        
        public TestHandler () {
            super (CLIHandler.WHEN_INIT);
        }
        
        protected int cli(CLIHandler.Args args) {
            called = true;
            return 0;
        }
        
        protected void usage (PrintWriter w) {
        }
        
    }
}
