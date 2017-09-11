/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute.cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.options.MavenSettings;
import org.openide.util.Utilities;

/**
 * TODO candidate to merge back into MavenCommandLineExecutor
 * @author mkleint
 */
public class ShellConstructor implements Constructor {
    private final @NonNull File mavenHome;

    public ShellConstructor(@NonNull File mavenHome) {
        this.mavenHome = mavenHome;
    }

    @Override
    public List<String> construct() {
        //#164234
        //if maven.bat file is in space containing path, we need to quote with simple quotes.
        String quote = "\"";
        List<String> toRet = new ArrayList<String>();
        String ex = "mvn"; //NOI18N
        if (Utilities.isWindows()) {
            String version = MavenSettings.getCommandLineMavenVersion(mavenHome);
            if (null == version) {
                ex = "mvn.bat"; // NOI18N
            } else {
                String[] v = version.split("\\."); // NOI18N
                int major = Integer.parseInt(v[0]);
                int minor = Integer.parseInt(v[1]);
                // starting with 3.3.0 maven stop using .bat file
                if ((major < 3) || (major == 3 && minor < 3)) {
                    ex = "mvn.bat"; //NOI18N
                } else {
                    ex = "mvn.cmd"; //NOI18N
                }
            }
        }
        File bin = new File(mavenHome, "bin" + File.separator + ex);//NOI18N
        toRet.add(quoteSpaces(bin.getAbsolutePath(), quote));

        if (Utilities.isWindows()) { //#153101, since #228901 always on windows use cmd /c
            toRet.add(0, "/c"); //NOI18N
            toRet.add(0, "cmd"); //NOI18N
        }
        return toRet;
    }

    // we run the shell/bat script in the process, on windows we need to quote any spaces
    //once/if we get rid of shell/bat execution, we might need to remove this
    //#164234
    private static String quoteSpaces(String val, String quote) {
        if (Utilities.isWindows()) {
            //since #228901 always quote
            //#208065 not only space but a few other characters are to be quoted..
            //if (val.indexOf(' ') != -1 || val.indexOf('=') != -1 || val.indexOf(";") != -1 || val.indexOf(",") != -1) { //NOI18N
                return quote + val + quote;
            //}
        }
        return val;
    }


}
