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

package org.netbeans.modules.cnd.debugger.gdb2;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.ProgressManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.io.IOPack;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.utils.Executor;
import org.netbeans.modules.cnd.debugger.common2.utils.FileMapper;
import org.netbeans.modules.cnd.debugger.common2.utils.PhasedProgress;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommand;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommandInjector;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIProxy;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public class Gdb {
    private static final boolean GDBINIT = Boolean.getBoolean("gdb.init.enable"); // NOI18N

    protected class StartProgressManager extends ProgressManager {
        private final String[] levelLabels = new String[] {
            "",
            "",
        };

        public StartProgressManager() {
            super();
        }

        @Override
        protected String[] levelLabels() {
            return levelLabels;
        }

        void setCancelListener() {
            super.setCancelListener(cancelListener);
        }

        void clearCancelListener() {
            super.setCancelListener(null);
        }
        public void startProgress(final boolean shortNames,
                                  final String hostname) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (StartProgressManager.super.startProgress(cancelListener, shortNames)) {
                        phasedProgress().setCancelMsg(Catalog.get("CancelNoted"));// NOI18N
                        String msg;
                        if (hostname != null) {
                            msg = MessageFormat.format(Catalog.get("StartingDbgOn"), // NOI18N
                                                       hostname);
                        } else {
                            msg = Catalog.get("StartingDbg"); // NOI18N
                        }
                        phasedProgress().setMessageFor(0, msg, 0);
                        phasedProgress().setVisible(true);
                    }
                }
            });
        }

        @Override
        public void finishProgress() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    StartProgressManager.super.finishProgress();
                }
            });
            StatusDisplayer.getDefault().setStatusText("");     // NOI18N
        }

        @Override
        public void updateProgress(final char beginEnd,
                                   final int level,
                                   final String message,
                                   final int count,
                                   final int total) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    StartProgressManager.super.updateProgress(beginEnd, level, message, count, total);
                }
            });
            StatusDisplayer.getDefault().setStatusText(message);
        }
    }

    private final PhasedProgress.CancelListener cancelListener =
        new PhasedProgress.CancelListener() {
            @Override
            public void cancelled() {
                interrupt();
            }
        };

    private final StartProgressManager startProgressManager =
        new StartProgressManager();

    public StartProgressManager startProgressManager() {
        return startProgressManager;
    }

    static class Factory {
	// startup parameters
	private final Executor executor;
	private final String additionalArgv[];
	private final Factory.Listener listener;
	private final boolean exec32;
	private final boolean shortNames;
	private final String gdbInitFile;
	private final Host host;
	private final boolean connectExisting;

	// additional startup parameters built up and maintained during startup
	private Gdb tentativeGdb;
	private String gdbname;
	private String[] gdb_argv;
	private Map<String, String> additionalEnv;
	private IOPack ioPack;
	private boolean remote;
        private final String runDir;
	private final NativeDebuggerInfo ndi;	// TMP

	public Factory(Executor executor,
		       String additionalArgv[],
		       Gdb.Factory.Listener listener,
		       boolean exec32,
		       boolean shortNames,
		       String gdbInitFile,
		       Host host,
		       boolean connectExisting,
                       String runDir,
		       GdbDebuggerInfo gdi) {
	    this.executor = executor;
	    this.additionalArgv = additionalArgv;
	    this.listener = listener;
	    this.exec32 = exec32;
	    this.shortNames = shortNames;
	    this.gdbInitFile = gdbInitFile;
	    this.host = host;
	    this.connectExisting = connectExisting;
            this.runDir = runDir;
	    this.ndi = gdi;
	}

	private Gdb getGdb(Factory factory,
			   boolean connectExisting,
                           boolean remote) {
	    return new Gdb(factory, connectExisting, remote);
	}

	public boolean connectExisting() {
	    return connectExisting;
	}

        public static interface Listener {
            public void connectFailed(String toWhom, String why, IOPack ioPack);
            public void assignGdb(Gdb tentativeGdb);
            public void assignIOPack(IOPack ioPack);
        }

        private final RequestProcessor START_RP = new RequestProcessor("GDB start", 5); // NOI18N

	public void start() {
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		System.out.printf("Gdb.Factory.start() on thread %s\n", // NOI18N
		    java.lang.Thread.currentThread());
	    }

	    //
	    // Figure if we're in remote mode
	    //
	    if (executor != null)
		assert executor.host() == host;
	    remote = host.isRemote();

	    /* OLD
	    Host host = null;
	    boolean remote = false;
	    if (host == null) {
		remote = false;
	    } else if (host.getHostName() == null) {
		remote = false;
	    }

	    String overrideInstallDir = null;
	    if (remote) {
		overrideInstallDir = host.getRemoteStudioLocation();
	    }
	    */

	    // Get a gdb but don't associate it with the Listener
	    // (DebuggerEngine) until we have a solid connection.



	    tentativeGdb = getGdb(this, connectExisting, host.isRemote());

	    String hostName = null;
	    if (remote)
		hostName = host.getHostName();

	    tentativeGdb.startProgressManager().startProgress(shortNames,
							      hostName);
	    tentativeGdb.startProgressManager().setCancelListener();
	    tentativeGdb.startProgressManager().updateProgress('>', 1,
		Catalog.get("StartingGdb"), 0, 0); // NOI18N
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // setup the IOPack: should be invoked  in UI thread
                    //
                    ioPack = IOPack.create(remote, ndi, executor);
                    tentativeGdb.setIOPack(ioPack);
                    listener.assignIOPack(ioPack);

//                    if (Log.Startup.nopty) {
//                        // We only need a line discipline for pio because
//                        // the console has it's own "Tap".
//                        ioPack.pio().getTerm().pushStream(new LineDiscipline());
//                    }

                    //
                    // pass on control to startAsync ...
                    //
                    START_RP.post(new Runnable() {
                        @Override
                        public void run() {
                            startAsync();
                        }
                    });

                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                runnable.run();
            } else {
                SwingUtilities.invokeLater(runnable);
            }
	}

	private void startAsync() {
            if (!connectExisting) {
		//
		// Figure gdb'a exec path
		//
                gdbname = NativeDebuggerImpl.getDebuggerString(GdbEngineCapabilityProvider.getGdbEngineType().getDebuggerID(), (MakeConfiguration)ndi.getConfiguration());
                if (gdbname == null) {
                    listener.connectFailed("gdb", Catalog.get("MSG_NoGgb"), null); // NOI18N
                    return;
                }
	    }

            // We need the slave name ahead of time
	    boolean havePio = false;
	    if (!connectExisting) {
                havePio = ioPack.start();
	    }
	    if (!havePio) {
		// SHOULD do something
	    }

	    if (!connectExisting) {
		//
		// Build up startup arguments to gdb
		//
		List<String> avec = new ArrayList<String>();

		boolean use_ss_attach = false;
		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.capture_engine_startup)
		    use_ss_attach = true;
		if (use_ss_attach) {
		    avec.add("ss_attach"); // NOI18N
		    avec.add("-v"); // NOI18N
		    avec.add(gdbname);
		    gdbname = "ss_attach"; // NOI18N
		} else {
		    avec.add(gdbname);
		}

		if (gdbInitFile != null && !gdbInitFile.isEmpty()) {
		    avec.add("-x"); // NOI18N
		    avec.add(gdbInitFile);
		}

                // see IZ 207860 - disable sourceing gdbinit files other than the one specified
                if (!GDBINIT) {
                    avec.add("-nx"); // NOI18N
                }

		// flags to get gdb going as an MI service
		avec.add("--interpreter"); // NOI18N
		avec.add("mi"); // NOI18N

		if (gdbInitFile != null) {
		    // doesn't look like gdb has the equivalent of dbx' -s
		}

		// Arrange for gdb victims to run under the Pio
		boolean ioInWindow =
		    true;
		if (ioPack.getSlaveName() != null && ioInWindow) {
		    avec.add("-tty"); // NOI18N
		    avec.add(ioPack.getSlaveName());
		}

		if (additionalArgv != null) {
		    for (int i = 1; i < additionalArgv.length; i++) {
			// NOTE: we're skipping argv[0]!
			avec.add(additionalArgv[i]);
		    }
		}

		gdb_argv = new String[avec.size()];
		for (int vx = 0; vx < avec.size(); vx++) {
		    gdb_argv[vx] = avec.get(vx);
		}

		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		    System.out.printf("gdb being started with:\n"); // NOI18N
		    for (String arg : gdb_argv)
			System.out.printf("\t'%s'\n", arg); // NOI18N
		}
	    }

	    additionalEnv = new HashMap<String, String>();

	    int pid = 0;

	    //
	    // Start gdb
	    //

	    if (!connectExisting) {
                ioPack.console().getTerm().pushStream(KeyProcessing.createStream());
                ioPack.console().getTerm().pushStream(tentativeGdb.tap());
		ioPack.console().getTerm().setCustomColor(0,
		    Color.yellow.darker().darker());
		ioPack.console().getTerm().setCustomColor(1,
		    Color.green.darker());
		ioPack.console().getTerm().setCustomColor(2,
		    Color.red.darker());

                // On windows we need to run gdb itself with the correct path
                Map<String, String> env = null;
                if (!executor.isRemote() && Utilities.isWindows()) {
                    env = ndi.getProfile().getEnvironment().getenvAsMap();
                }

		pid = executor.startEngine(gdbname, gdb_argv, env, runDir,
		    ioPack.console(), false, false);
		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		    System.out.printf("CommonGdb.Factory.start(): " + // NOI18N
				      "startEngine -> pid %d\n", pid); // NOI18N
		}
		if (pid == 0) {
		    tentativeGdb.startProgressManager().finishProgress();
		    listener.connectFailed("gdb", executor.getStartError(), null); // NOI18N
		    return;
		}
		tentativeGdb.setExecutor(executor);
	    }

            tentativeGdb.myMIProxy.logInfo();

            /* OLD
	    Moved to start()

	    boolean havePio = false;
	    if (!connectExisting)
		havePio = ioPack.connectPio(executor);
	    if (!havePio) {
		// SHOULD do something
	    }
	    */

	    if (Log.Startup.nopty) {
		// Arrange to send characters typed in pio to gdb.
		//
		// If the the debuggee isn't running then these commands
		// will go to gdb so we SHOULD probably temper this with
		// a flag. However, there's no way such a flag can be reliable
		// given the asynchroous nature of things.

//		final PrintWriter pw =
//		    new PrintWriter(executor.getOutputStream());
//		ioPack.pio().getTerm().
//		    addInputListener(new TermInputListener() {
//			public void sendChars(char c[], int offset, int count) {
//			    pw.write(c, offset, count);
//			    pw.flush();
//			}
//			public void sendChar(char c) {
//			    pw.write(c);
//			    pw.flush();
//			}
//		    } );
	    }


	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		System.out.printf("CommonGdb.Factory.start(): remote %s\n", // NOI18N
		    remote);
	    }

	    tentativeGdb.startProgressManager().updateProgress('<', 1, null, 0, 0);

	    // unlike dbx there is no network connection phase
	    // and no connectionAvailable

	    //
	    // point of no return
	    //
	    // TMP connected = true;
	    // TMP connectionAvailable(true);

	    // wait til MyMIProxy.connectionEstablished() is called.
	    // SHOULD we not start a connection timer?

	}

	private void cancelStartup() {
	}

	private void connectionAvailable(boolean success, String version, FileMapper fmap) {
	    tentativeGdb.startProgressManager().updateProgress('<', 1, null, 0, 0);
	    cancelStartup();
	    if (success) {
		// OLD connected = true;
		// OLD debugger.connectionEstablished();
		listener.assignGdb(tentativeGdb);
		tentativeGdb.initializeGdb(version, fmap);
	    }
	}
    }


    private GdbDebuggerImpl debugger;
    private IOPack ioPack;
    private Executor executor;
    private final Factory factory;      // backpointer to creating factory

    private final Tap tap;
    private final MIProxy myMIProxy;

    private boolean connected;

    // set to true when sending signal to pause gdb
    private volatile boolean signalled = false;

    // set to true when silent stop is requested
    private volatile boolean silentStop = false;

    private void initializeGdb(String version, FileMapper fmap) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
	    System.out.printf("Gdb.initializeGdb()\n"); // NOI18N
	debugger.setGdbVersion(version);
	debugger.initializeGdb(fmap);
	if (!debugger.willBeLoading())
	    startProgressManager().finishProgress();
    }

    private Gdb(Factory factory, boolean connectExisting, boolean remote) {
	this.factory = factory;
        tap = new Tap();
        myMIProxy = new MyMIProxy(tap, getCharSetEncoding(remote));
        tap.setMiProxy(myMIProxy);
    }

    private static String getCharSetEncoding(boolean remote) {
        String encoding;
        if (remote) {
            encoding = ProcessUtils.getRemoteCharSet();
        } else {
            encoding = Charset.defaultCharset().name();
        }
        return encoding;
    }

    private Factory factory() {
	return factory;
    }

    protected void setIOPack(IOPack ioPack) {
	this.ioPack = ioPack;
    }

    protected IOPack getIOPack() {
	return ioPack;
    }

    protected void setExecutor(Executor executor) {
	this.executor = executor;
    }

    protected Executor getExecutor() {
	return executor;
    }

    boolean isSignalled() {
        return signalled;
    }

    void resetSignalled() {
        signalled = false;
    }

    boolean isSilentStop() {
        return silentStop;
    }

    void resetSilentStop() {
        silentStop = false;
    }

    Tap tap() {
        return tap;
    }

    public boolean connected() {
        return connected;
    }

    public final void setDebugger(GdbDebuggerImpl debugger) {
        this.debugger = debugger;
        tap().setDebugger(debugger);
	debugger.getNDI().setLoadSuccess(false);
    }

    /* OLD
    final void setProc(UnixChildProcess proc) {
        this.proc = proc;
    }
    */

    void interrupt() {
	if (executor == null) {
	    factory.cancelStartup();
	    startProgressManager().finishProgress();
	    return;
	}

	// SHOULD fix this racey test-and-set
	if (!executor.isAlive()) {
	    factory.cancelStartup();
	    startProgressManager().finishProgress();
	    return;
	}

        try {
            // OLD proc.killgrp(2);	// 2 == SIGINT
	    // OLD executor.interruptGroup();
	    executor.terminate();
        } catch (IOException e) {
            ErrorManager.getDefault().annotate(e,
                "Sending kill signal to process group failed"); // NOI18N
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * Interrupt the program (note that unlike almost every other debugging
     * action, we're NOT asking dbx to do it - this we're actually doing
     * ourselves!!
     */
    boolean pause(int pid, boolean silentStop, boolean interruptGdb) {
        // The following predicate is _not_ the same as isReceptive()
        if (debugger.state().isRunning && debugger.state().isProcess) {
	    try {
                signalled = true;
                this.silentStop = silentStop;
                if (interruptGdb) {
                    executor.interrupt();
                } else {
                    Executor signaller = Executor.getDefault("signaller", factory.host, 0); // NOI18N
                    signaller.interrupt(pid);
                }
	    } catch(java.io.IOException e) {
		ErrorManager.getDefault().annotate(e,
		    "Sending kill signal to process failed"); // NOI18N
		ErrorManager.getDefault().notify(e);
                return false;
	    }
        }
        return true;
    }

    public static void dyingWords(String msg, IOPack ioPack) {

	JPanel panel = new JPanel();
	Catalog.setAccessibleDescription(panel,
	    "ACSD_GdbsDyingWords");	// NOI18N
	panel.setLayout(new java.awt.BorderLayout(0, 12));
	panel.add(new JLabel(msg), java.awt.BorderLayout.NORTH);

	if (ioPack != null) {
	    Term term = ioPack.console().getTerm();
	    if (term != null) {
		panel.add(term, java.awt.BorderLayout.CENTER);
		panel.setSize(600,400);
		term.setSize(600,400);
	    }
	}

	DialogDisplayer.getDefault().
	    notify(new NotifyDescriptor.Message(panel));
    }

    static final String versionString = "GNU gdb";		// NOI18N

    void setGdbIdleHandler(Runnable handler) {
        myMIProxy.setIdleHandler(handler);
    }

    private class MyMIProxy extends MIProxy {

	// remember version between consoleStreamOutput's capture
	// and connectionEstablished().
	private String version;
        private FileMapper fmap = null;

        public MyMIProxy(MICommandInjector injector, String encoding) {
            super(injector, "(gdb)", encoding); // NOI18N
        }

        @Override
        protected void dispatch(MIRecord record) {
            if (debugger != null && !debugger.postedKillEngine()) {
                super.dispatch(record);
            }
        }

        private static final String SWITCHING_PREFIX = "[Switching to process "; //NOI18N

        @Override
        protected void consoleStreamOutput(MIRecord record) {
            if (record.isStream() && record.stream().startsWith(SWITCHING_PREFIX)) {
                String msg = record.stream();
                try {
                    int end = SWITCHING_PREFIX.length();
                    while (Character.isDigit(msg.charAt(end))) {
                        end++;
                    }
                    debugger.session().setSessionEngine(GdbEngineCapabilityProvider.getGdbEngineType());
                    debugger.session().setPid(Long.parseLong(msg.substring(SWITCHING_PREFIX.length(), end)));
                } catch (NumberFormatException ex) {
                }
            } else {
                super.consoleStreamOutput(record);

                if (record.isStream()) {
                    String stream = record.stream();
                    if (stream.contains("configured") && stream.contains("mingw")) { //NOI18N
                        fmap = FileMapper.getByType(FileMapper.Type.MSYS);
                    }
                }

                if (version == null &&
                    record.isStream() &&
                    record.stream().startsWith(versionString)) {

                    version = record.stream();
                    // OLD debugger.gdbVersionString(record.stream());
                    return;
                }
            }
        }

        @Override
        protected void execAsyncOutput(MIRecord record) {
            // dispatch async messages without a token here
            if (debugger == null) {
                /* Debugger can not be initialized before the readiness "(gdb)" prompt
                 * but some service output may appear. Debugger should not handle it.*/
                return;
            }
            if (record.token() == 0) {
                if (record.cls().equals("stopped")) { // NOI18N
                    debugger.genericStopped(record);
                    // LATER: should be inside manager somehow
                    // this is neccessary, otherwise we may have wrong console output in the next command
                    // IZ 193352
                    clearMessages();
                } else if (record.cls().equals("running")) { // NOI18N
                    debugger.genericRunning();
                    // LATER: should be inside manager somehow
                    // this is neccessary, otherwise we may have wrong console output in the next command
                    // IZ 193352
                    clearMessages();
                }
            } else {
                dispatch(record);
            }
        }

        @Override
        protected void notifyAsyncOutput(MIRecord record) {
            if (debugger == null || debugger.session() == null) {
                /* Debugger can not be initialized before the readiness "(gdb)" prompt
                 * but some service output may appear. Debugger should not handle it.*/
                return;
            }
            if (record.token() == 0) {
                if (record.cls().equals("thread-group-started")) { //NOI18N
                    debugger.session().setSessionEngine(GdbEngineCapabilityProvider.getGdbEngineType());
                    debugger.session().setPid(Long.parseLong(record.results().getConstValue("pid"))); //NOI18N
                } else if (record.cls().equals("breakpoint-created")) { //NOI18N
                    debugger.newAsyncBreakpoint(record);
                } else if (record.cls().equals("breakpoint-modified")) { //NOI18N
                } else if (record.cls().equals("breakpoint-deleted")) { //NOI18N
                    debugger.deleteAsyncBreakpoint(record);
                } else if (record.cls().equals("thread-group-added") || //NOI18N
                    record.cls().equals("thread-group-removed") || //NOI18N
                    record.cls().equals("thread-group-exited") || //NOI18N
                    record.cls().equals("thread-created") || //NOI18N
                    record.cls().equals("thread-exited") || //NOI18N
                    record.cls().equals("thread-selected") || //NOI18N
                    record.cls().equals("library-loaded") || //NOI18N
                    record.cls().equals("library-unloaded")) { //NOI18N
                        // just skip
                    }
            } else {
                dispatch(record);
            }
        }

        @Override
        protected void targetStreamOutput(MIRecord record) {
        }

        @Override
        protected void logStreamOutput(MIRecord record) {
	    super.logStreamOutput(record);
        }

        @Override
        protected void connectionEstablished() {
            connected = true;
	    /* OLD
            debugger.connectionEstablished();
	    */
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
		System.out.printf("MyMIProxy.connectionEstablished()\n"); // NOI18N
	    factory.connectionAvailable(true, version, fmap);
        }

        @Override
	protected void errorBadLine(String data) {
	    if (Log.Startup.nopty) {
//		Term term = ioPack.pio().getTerm();
//		term.putChars(data.toCharArray(), 0, data.length());
	    } else {
		super.errorBadLine(data);
	    }
	}
    }

    void sendCommand(MICommand cmd, boolean setRunning) {
        if (debugger.state().isRunning) {
            GdbDebuggerImpl.LOG.log(Level.FINE, "Sending {0} to gdb while program is running", cmd);
            // see IZ 200046, do not send commands while running
//            if (debugger.getHost().getPlatform() == Platform.MacOSX_x86) {
//                return;
//            }
        }
        if (setRunning) {
            debugger.state().isRunning = true;
        }
        myMIProxy.send(cmd);
    }

    void sendCommand(MICommand cmd) {
        sendCommand(cmd, false);
    }
    
    /*package*/ String getLogger() {
        return myMIProxy.getLogger();
    }
}
