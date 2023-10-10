/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.maven.execute.cmd;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        // use mvnd if its the home of a daemon
        String ex = MavenSettings.isMavenDaemon(Paths.get(mavenHome.getPath())) ? "mvnd" : "mvn"; //NOI18N

        List<String> command = new ArrayList<>();

        if (Utilities.isWindows()) {

            //#153101, since #228901 always on windows use cmd /c
            command.add("cmd"); //NOI18N
            command.add("/c"); //NOI18N

            String version = MavenSettings.getCommandLineMavenVersion(mavenHome);
            if (null == version) {
                ex += ".bat"; // NOI18N
            } else {
                String[] v = version.split("\\."); // NOI18N
                int major = Integer.parseInt(v[0]);
                int minor = Integer.parseInt(v[1]);
                // starting with 3.3.0 maven stop using .bat file
                if ((major < 3) || (major == 3 && minor < 3)) {
                    ex += ".bat"; //NOI18N
                } else {
                    ex += ".cmd"; //NOI18N
                }
            }
        }

        //#164234
        //if maven.bat file is in space containing path, we need to quote with simple quotes.
        String quote = "\"";

        Path bin = Paths.get(mavenHome.getPath(), "bin", ex).toAbsolutePath();//NOI18N
        command.add(quoteSpaces(bin.toString(), quote));

        return command;
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
