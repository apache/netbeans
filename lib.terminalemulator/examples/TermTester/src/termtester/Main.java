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

package termtester;

import interp.CmdSet;
import interp.Interp;
import interp.Listener;
import org.netbeans.lib.richexecution.Pty;
import org.netbeans.lib.richexecution.PtyException;
import org.netbeans.lib.richexecution.program.Program;

/**
 *
 * @author ivan
 */
@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class Main {

    private static final String java =
            // "/opt/sun/jdk1.5.0_11/bin/java";
            "java";
    private static final String termDriver =
            "../TermDriver/dist/TermDriver.jar";

    private static class NBTermTestSubject extends InternalTestSubject {
        public NBTermTestSubject(Context context) throws PtyException {
            super(context, "NBTerm", 0, 0);
        }

	@Override
        protected Program makeProgram(Context context, Pty pty) {
            Program p = new Program();
            p.add(java);
            p.add("-jar");
            p.add(termDriver);
            p.add(pty.slaveName());
            return p;
        }
    }

    private static class XTermTestSubject extends ExternalTestSubject {
        public XTermTestSubject(Context context) throws PtyException {
            super(context,"xterm");
        }

	@Override
        protected Program makeProgram(Context context, Pty pty) {
            Program p = new Program();
            p.add("/bin/xterm");

            p.add("-geometry");
            p.add(String.format("%dx%d+0+350", context.width(), context.height()));

            p.add("-title");
            p.add(title());

            p.add("-sb");
            p.add("-rightbar");

	    p.add("-bg");
	    p.add("white");
	    p.add("-fg");
	    p.add("black");

            p.add("-e");
            p.add(java);
            p.add("-jar");
            p.add(termDriver);
            p.add(pty.slaveName());

            return p;
        }
    }

    private static class GnomeTestSubject extends ExternalTestSubject {
        public GnomeTestSubject(Context context) throws PtyException {
            super(context,"gnome-terminal");
        }

	@Override
        protected Program makeProgram(Context context, Pty pty) {
            Program p = new Program();
            p.add("/bin/gnome-terminal");

            p.add("--geometry");
            p.add(String.format("%dx%d+0+710", context.width(), context.height()));

	    // gnome-terminal no longer accepts --title:
	    // https://bugzilla.redhat.com/show_bug.cgi?id=1160184
            // https://bugzilla.gnome.org/show_bug.cgi?id=740188
            // What a bunch of hooey.
            // p.add("--title");
            // p.add(title());

            p.add("--hide-menubar");

            p.add("--profile=TermTest");

            // So we don't automatically run in background.
	    // Also no longer supported
	    // This means you have to close the gnome-terminal by hand after you
	    // exit TermTester.
	    // workaround: https://wiki.gnome.org/Apps/Terminal/Debugging
            // p.add("--disable-factory");

            p.add("-e");
            String cmd = java;
            cmd += " -jar";
            cmd += " " + termDriver;
            cmd += " " + pty.slaveName();
            p.add(cmd);

            return p;
        }
    }

    private static class KonsoleTestSubject extends ExternalTestSubject {
        public KonsoleTestSubject(Context context) throws PtyException {
            super(context, "konsole");
        }

	@Override
        protected Program makeProgram(Context context, Pty pty) {
            // Even this simplest version fails to start up a konsole
            Program p = new Program();
            // p.directory(new File("/home/ivan"));
            p.add("/usr/bin/konsole");
            p.add("--nofork");

            p.add("--profile");
            p.add("TermTester");

            p.add("--title");
            p.add(title());

            // Hopeless:
            // https://bugs.kde.org/show_bug.cgi?id=165355
            // https://bugs.kde.org/show_bug.cgi?id=147094
            // Since 2007!

            p.add("--geometry");
            p.add(String.format("%dx%d", context.width(), context.height()));

            p.add("-e");
            p.add("/opt/sun/jdk1.5.0_11/bin/java");
            p.add("-jar");
            p.add(termDriver);
            p.add(pty.slaveName());
            return p;
        }
    }

    private static void fatal(String fmt, Object... args) {
        System.out.printf("termtester: ");
        System.out.printf(fmt, args);
        System.out.printf("\n");
        System.exit(-1);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.printf("termtester: hello\n");


	Interp interp = Interp.getDefault();
        Context context = new Context(interp);
        Test.init(context);

        CmdSet mainCmdSet = new MainCmdSet(interp, context);

        try {
            TestSubject xterm = new XTermTestSubject(context);
            context.addTestSubject(xterm);

            TestSubject gnome = new GnomeTestSubject(context);
            context.addTestSubject(gnome);

            TestSubject nbterm = new NBTermTestSubject(context);
            context.addTestSubject(nbterm);

        } catch (PtyException x) {
            Main.fatal("exception creating pty:\n%s", x);
        }

        MainFrame frame = new MainFrame(context);
	Listener listener = new Listener(frame.term(), interp);
        frame.setLocation(550, 180);
        frame.setVisible(true);
        listener.start();

        System.out.printf("termtester: goodbye\n");
    }
}
