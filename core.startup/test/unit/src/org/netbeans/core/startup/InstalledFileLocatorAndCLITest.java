/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import junit.framework.Test;
import org.netbeans.CLIHandler;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InstalledFileLocatorAndCLITest extends NbTestCase {
    public InstalledFileLocatorAndCLITest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.createConfiguration(InstalledFileLocatorAndCLITest.class)
            .addStartupArgument("--create", "$UD/modules/t.jar", "--find", "modules/t.jar")
            .honorAutoloadEager(true).gui(false).suite();
    }
    
    public void testTJarWasFound() {
        assertEquals("InstalledFileLocator failed to find the modules/t.jar file",
            "true", System.getProperty("found.modules/t.jar"));
    }

    
    @ServiceProvider(service = CLIHandler.class, position = -3)
    public static final class CreateFile extends CLIHandler {

        public CreateFile() {
            super(WHEN_EXTRA);
        }

        @Override
        protected int cli(Args args) {
            final PrintStream err =
                args.getErrorStream() != null ?
                new PrintStream(args.getErrorStream()) : System.err;
            boolean create = false;
            boolean seek = false;
            final String[] arr = args.getArguments();
            for (int i = 0; i < arr.length; i++) {
                String s = arr[i];
                try {
                    if ("--create".equals(s)) {
                        create = true;
                        arr[i] = null;
                        continue;
                    }
                    if (create) {
                        s = s.replace("$UD", System.getProperty("netbeans.user"))
                            .replace('/', File.separatorChar);
                        File f = new File(s);
                        f.getParentFile().mkdirs();
                        f.createNewFile();
                        arr[i] = null;
                    }
                    create = false;
                    
                    if ("--find".equals(s)) {
                        seek = true;
                        arr[i] = null;
                        continue;
                    }
                    if (seek) {
                        arr[i] = null;
                        File found = InstalledFileLocator.getDefault().locate(s, null, false);
                        if (found == null) {
                            err.println("Installed file locator cannot find " + s);
                            return 0;
                        }
                        System.setProperty("found." + s, "true");
                        return 0;
                    }
                    seek = false;
                } catch (IOException iOException) {
                    iOException.printStackTrace(err);
                }
            }
            return 0;
        }

        @Override
        protected void usage(PrintWriter w) {
        }
        
    }
}
