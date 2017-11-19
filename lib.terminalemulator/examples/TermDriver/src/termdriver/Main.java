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
