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

package nbterm;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.richexecution.Pty.Mode;
import org.netbeans.lib.richexecution.program.Command;
import org.netbeans.lib.richexecution.program.Program;
import org.netbeans.lib.richexecution.program.Shell;

/**
 *
 * @author ivan
 */
public class Main {
    // defaults for unix
    static Boolean optLineDiscipline = null;  // line_discipline overriden by options
    static Mode mode = Mode.REGULAR;
    static String termType = "xterm-16color";
    static boolean debug = false;
    static boolean processErrors = false;
    static int rows = 24;
    static int cols = 80;
    static ArrayList<String> command = new ArrayList<String>();

    static private Terminal editorTerminal;
    static private Injector injector;
    static private String currentFile = "";

    private static void help() {
        System.out.printf("usage: term [ <option> ... ]\n");
        System.out.printf("\t-e <executable> [ <arg> ... ] (has to appear last)\n");
        System.out.printf("\t-geometry CCxRR\n");
        System.out.printf("\t-m pipe|pty_raw|pty|pty_packet (default = pty)\n");
        System.out.printf("\t-t dumb|xterm|ansi|dtterm (default = ansi)\n");
        System.out.printf("\t-l\tDon't use Term's own line discipline\n");
        System.out.printf("\t+l\tDo use Term's own line discipline\n");
        System.out.printf("\t-d\tTurn on term debugging\n");
        System.out.printf("\t-E\tProcess compilation Errors and turn them to hyperlinks\n");
        System.out.printf("\t-h\tHelp\n");
        System.out.printf("\t---------------------------------------------\n");
        System.out.printf("\t      pipe: Use raw i/o. Implies +l\n");
        System.out.printf("\t   pty_raw: Use raw pty's. Equivalent to 'pipe'\n");
        System.out.printf("\t       pty: Use standard pty's. Implies -l\n");
        System.out.printf("\tpty_packet: Enhanced pty functionality: track window size change\n");
    }

    private static void uerror(String fmt, Object...args) {
        System.out.printf(fmt + "\n", args);
        help();
        System.exit(1);
    }


    static TermExecutor executor() {
        TermExecutor executor = new TermExecutor();
        executor.setMode(mode);
        executor.setLineDiscipline(optLineDiscipline);
        executor.setDebug(debug);
        return executor;
    }

    static void start() {
        final Program program;
        if (command.isEmpty()) {
            program = new Shell();
        } else {
            program = new Program(command);
        }

        TermExecutor executor = executor();
        Terminal terminal = new Terminal(executor, termType, program, processErrors, rows, cols);
        executor.setTitledWindow(terminal);
        Thread thread = new Thread(terminal);
        thread.start();
    }

    /**
     * If there isn't one bring up a terminal running vi and have it
     * go to the given file and lineno.
     */
    static void showInEditor(String file, int lineno) {
        if (file == null)
            return;

        if (editorTerminal == null) {
            Program program = new Command("vi");
            editorTerminal = new Terminal(executor(), termType, program, false, rows, cols);

            editorTerminal.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    editorTerminal = null;
                    currentFile = "";
                }
            });

            injector = new Injector();
            editorTerminal.term().pushStream(injector);

            Thread thread = new Thread(editorTerminal);
            thread.start();

            // Give it some time to come up otherwise it's not receptive
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!file.equals(currentFile)) {
            injector.inject("" + (char) 27 + ":e " + file + "\r\n");
            currentFile = file;
        }
        injector.inject("" + (char) 27 + lineno + "G");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //
        // Process arguments
        //
        for (int cx = 0; cx < args.length; cx++) {
            if (args[cx].startsWith("-") || args[cx].startsWith("+")) {
                if (args[cx].equals("-m")) {
                    cx++;
                    if (cx >= args.length || args[cx].startsWith("-")) 
                        uerror("expected argument after -m");
                    if (args[cx].equals("pipe"))
                        mode = Mode.NONE;
                    else if (args[cx].equals("pty_raw"))
                        mode = Mode.RAW;
                    else if (args[cx].equals("pty"))
                        mode = Mode.REGULAR;
                    else if (args[cx].equals("pty_packet"))
                        mode = Mode.PACKET;
                    else
                        uerror("Unrecognized mode '%s'", args[cx]);
                } else if (args[cx].equals("-t")) {
                    cx++;
                    if (cx >= args.length || args[cx].startsWith("-")) 
                        uerror("expected argument after -t");
                    termType = args[cx];
                } else if (args[cx].equals("-l")) {
                    optLineDiscipline = Boolean.FALSE;
                } else if (args[cx].equals("+l")) {
                    optLineDiscipline = Boolean.TRUE;
                } else if (args[cx].equals("-E")) {
                    processErrors = true;
                } else if (args[cx].equals("-d")) {
                    debug = true;
                } else if (args[cx].equals("-h")) {
                    help();
                    System.exit(0);
                } else if (args[cx].equals("-e")) {
                    cx++;
                    if (cx >= args.length || args[cx].startsWith("-"))
                        uerror("expected argument after -e");
                    command.add(args[cx++]);
                    while (cx < args.length)
                        command.add(args[cx++]);
                } else if (args[cx].equals("-geometry")) {
                    cx++;
                    if (cx >= args.length || args[cx].startsWith("-"))
                        uerror("expected argument after -geometry");
                    String gs = args[cx];
                    int xx = gs.indexOf('x');
                    if (xx == -1)
                        uerror("Geometry contains no x");
                    String cc = gs.substring(0, xx);
                    String rr = gs.substring(xx+1);
                    System.out.printf("rr %s  cc %s\n", rr, cc);
                    rows = Integer.parseInt(rr);
                    cols = Integer.parseInt(cc);
                } else {
                    uerror("Unrecognized option '%s'", args[cx]);
                }
            } else {
                uerror("Unrecognized argument '%s'", args[cx]);
            }
        }
        start();
    }

}
