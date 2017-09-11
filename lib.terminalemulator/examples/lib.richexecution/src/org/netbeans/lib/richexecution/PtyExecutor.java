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

package org.netbeans.lib.richexecution;

import org.netbeans.lib.richexecution.program.Program;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.richexecution.Pty.Mode;

/**
 * Execute a {@link Program}, {@link Shell} or {@link Command} connected to a Pty.
 * <p>
 * If the pty is null, as the case might be on Windows, executes the program
 * as {@link java.lang.ProcessBuilder} would.
 * @author ivan
 */
public final class PtyExecutor {
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
            "/org/netbeans/lib/richexecution/PtyExecutor.class";
        URL url = PtyExecutor.class.getResource(myClass);
        System.out.printf("findBin(): my resource is \"%s\"\n", url);
        String urlString = url.toString();

        // We usually get something like this:
        // jar:file:/home/ivan/work/pty/share/Pty/dist/Pty.jar!/pty/TermProgram.class
        //     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        // Sometimes instead of "jar:" we get "nbjcl:" ... these occur for
        // example if Pty.jar is loaded via a NB library wrapper.
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

        // Now urlString has something like these in it:
        // file:/home/ivan/work/pty/share/Pty/dist/Pty.jar
        // file:/home/ivan/work/pty/share/TermSuite/build/cluster/modules/ext/Pty.jar
        if (urlString.startsWith("file:")) {
            // strip the "file:"
            urlString = urlString.substring(5);
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
                } catch (InterruptedException ex) {
                    Logger.getLogger(PtyExecutor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
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
        if (wrapper == null) {
            if ((wrapper = findBin("process_start" + "-" + Platform.get().platform())) != null) {
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
        List<String> wrapperCmd = new ArrayList<String>();
        if (pgrp != null) {
            wrapperCmd.add(setpgrpCmd());
        }
        wrapperCmd.add(getWrapper());
        wrapperCmd.add("-pty");
        wrapperCmd.add(pty.slaveName());
        wrapperCmd.addAll(cmd);


        return wrapperCmd;
    }

    public final PtyProcess start(Program program, Pty pty) {
        Process process;
        int pid = -1;
        try {
	    ProcessBuilder pb = new ProcessBuilder(wrappedCmd(program.command(), pty));
	    pb.directory(program.directory());
	    pb.environment().putAll(program.environment());
	    // LATER
            pb.redirectErrorStream(true /* LATER program.redirectErrorStream() */);
            System.out.printf("\nExecuting %s\n", program.command());
	    process = pb.start();
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
                        System.err.printf("%s\n", line);
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
