/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package termdriver;

import com.sun.jna.Native;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.richexecution.CLibrary;
import org.netbeans.lib.richexecution.Util;

/**
 * An application to be run under terminals being tested by "Term Tester".
 * It takes a pty slave name as a side channel and shuttles from it to
 * stdout.
 * @author ivan
 */
public class Main {

    private static void error(String fmt, Object... args) {
        System.out.printf("termdriver: ");
        System.out.printf(fmt, args);
        System.out.printf("\r\n");
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(-1);
    }

    private static String strerror(int errno) {
        return CLibrary.INSTANCE.strerror(errno);
    }

    /**
     * Make out own i/o be raw.
     */
    private static void makeRaw() {
        String ttyName = "/dev/tty";
        int ofd = Util.getFd(FileDescriptor.out);

        CLibrary.LinuxTermios termios = new CLibrary.LinuxTermios();

        // check existing settings
        // If we don't do this tcssetattr() will return EINVAL.
        if (CLibrary.INSTANCE.tcgetattr(ofd, termios) == -1) {
            error("tcgetattr(\"" + ttyName + "\", <termios>) failed -- " + strerror(Native.getLastError()));
        }

        // System.out.printf("tcgetattr() gives %s\r\n", termios);

        // initialize values relevant for raw mode
        CLibrary.INSTANCE.cfmakeraw(termios);

        // System.out.printf("cfmakeraw() gives %s\r\n", termios);

        // apply them
        if (CLibrary.INSTANCE.tcsetattr(ofd, CLibrary.INSTANCE.TCSANOW(), termios) == -1) {
            error("tcsetattr(\"" + ttyName + "\", TCSANOW, <termios>) failed -- " + strerror(Native.getLastError()));
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        makeRaw();
        System.out.printf("termdriver: hello\r\n");

        String slave = null;

        for (String arg : args) {
            if (arg.startsWith("-")) {
                error("Unrecognized option '%s'", arg);
            } else {
                if (slave != null)
                    error("Extra argument '%s'", arg);
                else
                    slave = arg;
            }
        }

        if (slave == null) {
            error("Missing slave argument");
        }

        //
        // Open the input pty
        //
        File file = new File(slave);
        FileReader fr = null;
        OutputStreamWriter fw = null;
        if (!file.exists())
            error("No such file '%s'", slave);

        try {
            fr = new FileReader(file);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            fw = new FileWriter(file);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        final OutputStreamWriter ffw = fw;

        //
        // Shuttle what we see in stdin to pty
        //
        Thread shuttle = new Thread() {
            @Override
            public void run() {
                while (true) {
                    char c = '\n';
                    try {
                        c = (char) System.in.read();
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        return;
                    }
                    try {
                        ffw.write(c);
                        ffw.flush();
                        // System.out.printf("ECHO '%c'\r\n", c);
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        shuttle.start();

        //
        // Shuttle input from pty to stdout ...
        //
        try{
            int c;
            while ((c = fr.read()) != -1) {
                System.out.write(c);
                System.out.flush();
            }
        } catch (IOException x) {
            // pty closure often causes unwarranted versions of this
            // error("read failed");

        }

        System.out.printf("termdriver: goodbye\r\n");
    }
}
