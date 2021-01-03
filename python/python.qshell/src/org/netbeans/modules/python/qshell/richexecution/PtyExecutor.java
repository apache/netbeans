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

package org.netbeans.modules.python.qshell.richexecution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.python.qshell.richexecution.Pty.Mode;
import org.openide.util.Exceptions;

public class PtyExecutor {
    private static String setpgrpCmd = null;
    private Mode mode = Mode.REGULAR;

    /**
     * Locate executable 'bin' somewhere "near" us.
     *
     * "near" us is in a lib/ directory. That is because 'bin' is not meant
     * for execution by the user who has bin/ in the their $PATH.
     *
     * Where are we?
     * We figure this by using Class.getResource() and massaging the url.
     * But we can be loaded in a variety of contexts so there are a lot
     * of variations.
     *
     * @param bin
     * @return Full pathname to 'bin' or null.
     */
    private static String findBin(String bin) {
        final String myClass =
            "/org/netbeans/modules/python/qshell/richexecution/PtyExecutor.class";
        URL url = PtyExecutor.class.getResource(myClass);
        System.out.printf("findBin(): my resource is \"%s\"\n", url);
        String urlString = url.toString();

        boolean isJar = false;
        boolean isNbjcl = false;

        if (urlString.startsWith("jar:")) {
            isJar = true;
        } else if (urlString.startsWith("nbjcl:")) {
            isNbjcl = true;
        } else if (urlString.startsWith("file:")) {
            ;
        } else {
            System.out.printf("findBin(): " + "can only handle jar: nbjcl: or file: resources but got %s\n", urlString);
            return null;
        }


        if (isJar || isNbjcl) {
            // Extract the pathname to the jarfile and stuff it back into urlString.
            int colonx = urlString.indexOf(':');
            if (colonx == -1) {
                System.out.printf("findBin(): cannot find \':\' in %s\n", urlString);
                return null;
            }

            int bangx = urlString.indexOf('!');
            if (bangx == -1) {
                System.out.printf("findBin(): malformed jar url (no !): %s\n", urlString);
                return null;
            }
            String jarLocation = urlString.substring(colonx + 1, bangx);
            System.out.printf("findBin(): jarLocation \"%s\"\n", jarLocation);
            urlString = jarLocation;
        }

        if (urlString.startsWith("file:")) {
            // strip the "file:"
            urlString = urlString.substring(5);
        }
        try {
            urlString = URLDecoder.decode(urlString, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
        File binFile = null;
        if (isJar || isNbjcl) {
            File jarFile = new File(urlString);
            File installDir = jarFile.getParentFile();
            if (!installDir.getName().equals("lib")) {
                File libDir = new File(installDir, "lib");
                binFile = new File(libDir, bin);
            } else {
                binFile = new File(installDir, bin);
            }
        } else {
            final String classes = "/build/classes";

            // strip the pkg/class part
            if (!urlString.endsWith(myClass)) {
                System.out.printf("findBin(): urlString %s doesn\'t end with %s\n", urlString, myClass);
            }
            urlString = urlString.substring(0, urlString.length() - myClass.length());
            System.out.printf("findBin(): classpath \"%s\"\n", urlString);
            if (urlString.endsWith(classes)) {
                String projectDir = urlString.substring(0, urlString.length() - classes.length());
                System.out.printf("findBin(): projectDir \"%s\"\n", projectDir);
                File projectDirFile = new File(projectDir);
                binFile = new File(projectDirFile, bin);
            }
        }

        if (binFile != null) {
            if (binFile.exists()) {
                System.out.printf("findBin(): found: %s\n", binFile);
                return binFile.toString();
            } else {
                System.out.printf("findBin(): doesn\'t exist here: %s\n", binFile);
            }
        }
        return null;
    }

    /**
     * Find and cache one of /usr/bin/setpgrp or /usr/bin/setsid.
     * We usually get setsid on linux and setpgrp on solaris.
     */
    private static String setpgrpCmd() {
        if (false) {
            return null;
        }

        if (OS.get() == OS.MACOS) {
            return null;
        }
        if (setpgrpCmd == null) {
            File file;
            file = new File("/usr/bin/setpgrp");
            if (file.exists()) {
                setpgrpCmd = file.getPath();
                return setpgrpCmd;
            }
            file = new File("/usr/bin/setsid");
            if (file.exists()) {
                setpgrpCmd = file.getPath();
                return setpgrpCmd;
            }
            throw new MissingResourceException("Can\'t find setpgrp or setsid", null, null);
        }
        return setpgrpCmd;
    }

    /**
     * Ensure executable is executable.
     * @param executable
     */
    private static void fixExecution(String executable) {
        // The wrapper executables get nunzipped often, like when an NBM
        // is installed, and zip doesn't preserve execution permission bits.
        // So we force it each time.
        String[] chmodProgram = new String[3];
        switch (OS.get()) {
            case LINUX:
            case MACOS:
            case SOLARIS:
                chmodProgram[0] = "chmod";
                chmodProgram[1] = "u+x";
                chmodProgram[2] = executable;
                try {
                    Process p = Runtime.getRuntime().exec(chmodProgram);
                    p.waitFor();
                } catch (InterruptedException | IOException ex) {
                    Logger.getLogger(PtyExecutor.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case WINDOWS:
            case OTHER:
                break;
        }
    }

    private static String wrapper;
    private static String pgrp;     // set as side-effect of getWrapper()

    /**
     * Locate the process_start helper, either as process_start-<platform> or,
     * if not found, the fallback process_start.sh".
     */
    private static String getWrapper() {
        wrapper = null;
        if (wrapper == null) {
            if ((wrapper = findBin("process_start" + "-" + OS.platform())) != null) {
                pgrp = null;
            } else if ((wrapper = findBin("process_start.sh")) != null) {
                pgrp = setpgrpCmd();
            } else {
                throw new MissingResourceException("Can\'t find a wrapper", null, null);
            }
            fixExecution(wrapper);
        }
        return wrapper;
    }

    private static List<String> wrappedCmd(List<String> cmd, Pty pty) {
        if (pty == null) {
            return cmd;
        }
        List<String> wrapperCmd = new ArrayList<>();
        String wrapper = getWrapper();
        if (pgrp != null) {
            wrapperCmd.add(setpgrpCmd());
        }
        wrapperCmd.add(wrapper);
        wrapperCmd.add("-pty");
        wrapperCmd.add(pty.slaveName());
        wrapperCmd.addAll(cmd);


        return wrapperCmd;
    }

    public final PtyProcess start(Program program, Pty pty) {
        Process process;
        int pid = -1;
        try {
            List<String> wrappedCmd = wrappedCmd(program.command(), pty);
            program.processBuilder().command(wrappedCmd);
            process = program.processBuilder().start();
        } catch (IOException ex) {
            Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

	// On windows ...
	// skip this part, no PID
	// On unixy platforms ...
	// extract pid and other information from the wrapper, 'process_start'.
        if (pty != null) {
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                String line;
                try {
                    line = stdout.readLine();
                    if (line == null) {
                        break;
                    }
                    StringTokenizer tokens = new StringTokenizer(line);
                    String info = tokens.nextToken();
                    if ("PID".equals(info)) {
                        String pidString = tokens.nextToken();
                        pid = Integer.parseInt(pidString);
                        System.out.printf("pid is %d\n", pid);
                    } else if ("ARGS".equals(info)) {
                        System.out.printf("args is \'%s\'\n", line);
                    } else {
                        System.out.printf("%s\n", line);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return new PtyProcess(process, pid, pty);
    }

    /**
     * Set the Pty mode.
     * Should be called before start().
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }
}
