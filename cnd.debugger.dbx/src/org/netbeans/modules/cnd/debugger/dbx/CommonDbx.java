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

package org.netbeans.modules.cnd.debugger.dbx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.EnumSet;
import java.text.MessageFormat;
import java.io.IOException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.lib.terminalemulator.LogicalLineVisitor;
import org.netbeans.lib.terminalemulator.Coord;

import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;

import com.sun.tools.swdev.glue.GStr;
import com.sun.tools.swdev.glue.Glue;
import com.sun.tools.swdev.glue.Master;
import com.sun.tools.swdev.glue.NetAddr;
import com.sun.tools.swdev.glue.Notifier;
import com.sun.tools.swdev.glue.NotifierThread;
import com.sun.tools.swdev.glue.Surrogate;
import com.sun.tools.swdev.glue.HostInfo;

import com.sun.tools.swdev.glue.dbx.*;

import org.netbeans.modules.cnd.debugger.common2.utils.Executor;
import org.netbeans.modules.cnd.debugger.common2.utils.PhasedProgress;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.ProgressManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.io.IOPack;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionLayers;

import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcProgressManager;
import org.openide.windows.InputOutput;


/**
 * Quiet dbx surrogate.
 * CommonDbx is the basic dbx connectivity workhorse and is not attached to 
 * any specific UI debugger functionality.
 *
 * To create a dbx ...
 * - Create a CommonDbx.Factory.Listener
 * - Create a CommonDbx.Factory (or rather, a subclass of it)
 * - Issue CommonDbx.Factory.start(Executor, ..., Listener).
 *   start will, in no particular order, call
 *   - Factory.getIOPack()
 *   - Factory.getDbx()
 *   ... and then ...
 *   - Listener.assignIOPack()
 *   - Listener.assignDbx() | Listener.connectFailed
 */

public abstract class CommonDbx extends GPDbxSurrogate {

    protected class StartProgressManager extends ProgressManager {
	private final String[] levelLabels = new String[] {
	    "",
	    "",
	};

	public StartProgressManager() {
	    super();
	}

	protected String[] levelLabels() {
	    return levelLabels;
	}

	void setCancelListener() {
	    super.setCancelListener(cancelListener);
	}

	void clearCancelListener() {
	    super.setCancelListener(null);
	}

	public void startProgress(final boolean shortNames, final String hostname) {
            if (SwingUtilities.isEventDispatchThread()) {
                if (StartProgressManager.super.startProgress(cancelListener, shortNames)) {
                        phasedProgress().setCancelMsg(Catalog.get("CancelNoted"));// NOI18N
                        String msg;
                        if (hostname != null) {
                            msg = MessageFormat.format(Catalog.get("StartingDbgOn"),
                                                       hostname);
                        } else {
                        msg = Catalog.get("StartingDbg");
                    }
                    phasedProgress().setMessageFor(0, msg, 0);
                    phasedProgress().setVisible(true);
                }
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        startProgress(shortNames, hostname);
                    }
                });
            }
	}

	@Override
	public void finishProgress() {
	    super.finishProgress();
	    StatusDisplayer.getDefault().setStatusText("");	// NOI18N
	    rtcProgressManager().finishProgress();
	}

	@Override
	public void updateProgress(char beginEnd, int level,
					String message, int count, int total) {
	    super.updateProgress(beginEnd, level, message, count, total);
	    StatusDisplayer.getDefault().setStatusText(message);
	}
    }

    protected final PhasedProgress.CancelListener cancelListener =
	new PhasedProgress.CancelListener() {
	    public void cancelled() {
		interrupt();
	    }
	};

    private final StartProgressManager startProgressManager =
	new StartProgressManager();

    protected StartProgressManager startProgressManager() {
	return startProgressManager;
    }


    private final RtcProgressManager rtcProgressManager =
	new RtcProgressManager(cancelListener);

    protected RtcProgressManager rtcProgressManager() {
	return rtcProgressManager;
    }



    /**
     * Base class for creating Dbx surrogates.
     *
     * This factory is not intended to be a singleton. Use one Factory instance
     * per Dbx instance you want to create.
     */

    public abstract static class Factory {

	// startup parameters
	private final Executor executor;
	private final String additionalArgv[];
	private final Factory.Listener listener;
	private final boolean exec32;
	private final boolean shortNames;
	private final String dbxInitFile;
	private final Host host;
	private final boolean connectExisting;

	// additional startup parameters built up and maintained during startup
	private CommonDbx tentativeDbx;
	private String dbxname;
	private String[] dbx_argv;
	private Map<String, String> additionalEnv;
	private IOPack ioPack;
	private boolean remote;
        private final InputOutput io;
        private final NativeDebuggerInfo ndi;	// TMP
        private final OptionLayers optionLayers;

	// Start out connecting to dbx in master mode. If we can't we turn
	// 'beMaster' off and fall back on the old CONNECT_CHILD mode.

	private static boolean beMaster = true;

	private static Master master;

	private ConnectTimer timer;


	/**
	 * Base class for creating Dbx surrogates.
	 *
	 * @param executor Executor to use to start dbx excutable.
	 * @param additionalArgv Additional arguments to pass to dbx. (This
	 * typically happens on cloning, where dbx will create arguments for
	 * the "child" dbx being started to debug the child process.
	 * @param listener Factory.Listener to send notifications to. 
	 * Typically a NativeDebugegrEngine.
	 * @param exec32 Start dbx with -xexec32 (i.e. force a 32-bit dbx
	 * even on a 64-bit system)
	 */
	protected Factory(Executor executor, 
			   String additionalArgv[],
			   Factory.Listener listener,
			   boolean exec32,
			   boolean shortNames,
			   String dbxInitFile,
			   Host host,
			   boolean connectExisting,
                           String dbxName,
                           InputOutput io,
                           NativeDebuggerInfo ndi,
                           OptionLayers optionLayers) {
	    this.executor = executor;
	    this.additionalArgv = additionalArgv;
	    this.listener = listener;
	    this.exec32 = exec32;
	    this.shortNames = shortNames;
	    this.dbxInitFile = dbxInitFile;
	    this.host = host;
	    this.connectExisting = connectExisting;
            this.dbxname = dbxName;
            this.io = io;
            this.ndi = ndi;
            this.optionLayers = optionLayers;
	}

	protected abstract CommonDbx getDbx(Factory fatory,
					    Notifier n,
					    int flags,
					    boolean connectExisting,
					    Master master);


	public boolean connectExisting() {
	    return connectExisting;
	}

	public static interface Listener {
	    public void connectFailed(String toWhom, String why, IOPack ioPack);
	    public void assignDbx(CommonDbx tentativeDbx);
	    public void assignIOPack(IOPack ioPack);
	}


	/**
	 * Timer to help manage timout of async connects().
	 *
	 * Once started it will run indefinitely while polling every second 
	 * for one of the following:
	 * - A completed connection
	 *   Stop timer. we're done.
	 * - Some output from dbx in Term.
	 *   If first time, stop repeating the timeout and wait an additional
	 *   3 seconds.
	 *   If N'th time (after the 3 seconds) stop time and
	 *   recoverFromFailedConnect(false);
	 * - User pressing Cancel button on progress bar.
	 *   Stop timer, recoverFromFailedConnect(true).
	 */

	private class ConnectTimer extends Timer implements ActionListener {

	    private void log(String fmt, Object... args) {
		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
		    System.out.printf(fmt, args);
	    }

	    public ConnectTimer() {
		super(1 * 1000, null);
		tentativeDbx.startProgressManager().clearCancelListener();
		addActionListener(this);
	    }

	    public void actionPerformed(ActionEvent evt) {
		if (tentativeDbx.connected()) {
		    log("Timer: connected\n"); // NOI18N
		    tentativeDbx.startProgressManager().setCancelListener();
		    stop();
		    return;
		} else if (tentativeDbx.sawOutput()) {
		    if (isRepeats()) {
			log("Timer: saw output. one more round\n"); // NOI18N
			setRepeats(false);
			setDelay(3 * 1000);
		    } else {
			log("Timer: saw output. giving up & recovering\n"); // NOI18N
			recoverFromFailedConnect(false);
		    }
		} else if (tentativeDbx.startProgressManager().isCancelled()) {
		    log("Timer: cancel by user\n"); // NOI18N
		    stop();
		    recoverFromFailedConnect(true);
		} else {
		    log("Timer: waiting for connect, output or cancel\n"); // NOI18N
		}
	    }
	}


	/**
	 * Search for tell-tale error messages in dbx output.
	 */

	private static class Searcher implements LogicalLineVisitor {
	    public enum Failure {
		NONE,
		PRE_MASTER	// old dbx; doesn't know about master/slave
	    };

	    private Failure failure = Failure.NONE;

	    public Failure failure() {
		return failure;
	    }

	    public boolean visit(int line, Coord begin, Coord end, String text){
		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		    System.out.printf("%2d: %s %s %s\n", // NOI18N
			line, begin, end, text);
		}

		// Older dbx's will emit something like this if started in
		// master mode:
		//	dbx: unrecognized option argument\
		//	'master=UN:/tmp/glue.ivan.djomolungma:0/glue.26...'
		//       ^^^^^^^^^
		// Most of the error is translated so we latch on to this part
		// which isn't translated.

		if (text.matches(".*master=UN.*")) { // NOI18N
		    failure = Failure.PRE_MASTER;
		    return false;
		}
		return true;
	    }
	}


	private String failMsg() {
	    String failMsg;
	    if (tentativeDbx.connect_fail() == ConnectFail_NOTUNNEL) {
		failMsg = Catalog.format("FMT_notunnel",
					host.getHostName(),
					host.getPortNum(),
					tentativeDbx.connect_fail_str());
	    }  else{
		failMsg = tentativeDbx.connect_fail_str();
	    }
	    return failMsg;
	}

	/**
	 * Called if a connect() fails, times out or gets cancelled.
	 * Should always be called on the AWT eventQ.
	 * See 6675497 and 4909491.
	 */

	private void recoverFromFailedConnect(boolean cancelled) {

	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		System.out.printf
		    ("CommonDbx.Factory.recoverFromFailedConnect()\n"); // NOI18N
	    }

	    assert SwingUtilities.isEventDispatchThread();

	    String failMsg;
	    Searcher searcher = new Searcher();
	    IOPack tentativeIoPack = tentativeDbx.getIOPack();

	    if (cancelled) {
		failMsg = Catalog.get("MSG_DbxCancelled");

	    } else {
		failMsg = failMsg();

		// See if we can discern the cause of failure from dbx's output

		if (tentativeIoPack != null) {
		    Term term = tentativeIoPack.console().getTerm();
		    if (term != null)
			term.visitLogicalLines(null, null, searcher);
		}
	    }


	    if ((tentativeDbx.fl_async() && !cancelled) ||
		tentativeDbx.connectionTimedOut()) {
		failMsg += "  (" + Catalog.get("MSG_DbxTimedOut") + ")";
	    }

	    // kill dbx with a SIGTERM
	    try {
		tentativeDbx.executor.terminate();
	    } catch (Exception x) {
	    }


	    switch (searcher.failure()) {
		case NONE:
		    break;
		case PRE_MASTER:
		    if (beMaster) {
			// retry in connect-child mode
			if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
			    System.out.printf
				("CommonDbx.Factory.recoverFromFailedConnect() -- retrying with connect-child mode\n"); // NOI18N
			}
			close();	// ... the master 
			beMaster = false;

			tentativeDbx.ioPack.bringDown();

			// This isn't a recursive call ... start() has returned 
			// and we have been called from an invokeLater.

			start();
			return;
		    }
		    break;
	    }


	    tentativeDbx.startProgressManager().finishProgress();

	    if (cancelled)
		listener.connectFailed("dbx", failMsg, null); // NOI18N
	    else
		listener.connectFailed("dbx", failMsg, tentativeIoPack); // NOI18N
	    tentativeIoPack.bringDown();
	}

        private void updateDbxPath(Host host) {
            String newPath = DbxPathProvider.getDbxPath(host);
            if (newPath != null) {
                dbxname = newPath;
            }
        }

	/**
	 * Start a dbx and connect to it.
	 *
	 * We start out using master mode.
	 * If perchance we're interacting with an older dbx which doesn't
	 * know about master mode the connect will time out and
	 * recoverFromFailedConnect() will look for a telltale warning
	 * and fall back on CONNECT_CHILD mode and retry.
	 *
	 * In both cases a genuine hang will hang until the user cancels 
	 * from the progress dialog in which case the dbx process is terminated
	 * and the user gets an error dialog.
	 *
	 */
	public void start() {

	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		System.out.printf("CommonDbx.Factory.start() on thread %s\n", // NOI18N
		    java.lang.Thread.currentThread());
	    }

	    // IMPORTANT
	    // start() should be re-callable in case a connection failed.
	    // Various resources it creates are:
	    // - master is reset through recoverFromFailedConnect()
	    // - ioPack is brought down through disconnected()
	    // - tentativeDbx is local and doesn't escape
	    // - executor is re-usable



	    //
	    // Figure if we're in remote mode
	    //
	    /* TMP
	    This fails under ATD RTC runs
	    if (executor != null)
		assert executor.host() == host;
	    */
	    remote = host.isRemote();

	    if (!connectExisting) {
		//
		// Update dbx path if needed
		//
                updateDbxPath(host);

		if (dbxname == null) {
                    listener.connectFailed("dbx", Catalog.format("MSG_CantFindDbx",""), null); // NOI18N
		    return;
		}


		//
		// create a Master if we don't have one yet
		//
		if (beMaster && master == null) {

		    Master.SyncStyle syncStyle = Master.SyncStyle.ASYNC;

		    Master.KeyStyle keyStyle = Master.KeyStyle.KEY;

		    EnumSet<Master.ListenOn> listenOn;
		    if (Glue.supportsUnixSockets())
			listenOn = EnumSet.of(Master.ListenOn.LOCAL, 
					      Master.ListenOn.REMOTE);
		    else
			listenOn = EnumSet.of(Master.ListenOn.REMOTE);

		    master = new Master(NotifierThread.notifier(),
					syncStyle,
					keyStyle,
					listenOn,
					RemoteSupport.securityStyle(host.getSecuritySettings()));
		    master.start_up();
		}
	    }


	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		if (connectExisting)
		    System.out.printf("CommonDbx.Factory.start(): " + // NOI18N
		                      "CONNECT_CLIENT mode\n"); // NOI18N
		else if (beMaster)
		    System.out.printf("CommonDbx.Factory.start(): " + // NOI18N
		                      "CONNECT_MASTER mode\n"); // NOI18N
		else
		    System.out.printf("CommonDbx.Factory.start(): " + // NOI18N
		                      "CONNECT_CHILD\n"); // NOI18N
	    }

	    //
	    // Setup glue connect flags
	    //
	    int flags = 0;
	    if (!connectExisting) {
		if (!remote) {
		    if (beMaster)
			flags += Surrogate.CONNECT_MASTER;
		    else
			flags += Surrogate.CONNECT_CHILD;
		} else {
		    if (beMaster)
			flags += Surrogate.CONNECT_MASTER;
		    else
			flags += Surrogate.CONNECT_WAIT;
		}
	    }

	    flags += Surrogate.CONNECT_ASYNC;


	    // Get a dbx but don't associate it with the Listener
	    // (DebuggerEngine) until we have a solid connection.

	    tentativeDbx = getDbx(this,
				  NotifierThread.notifier(),
				  flags,
				  connectExisting,
				  connectExisting? null: master);

	    /* OLD
	    // Start out w/o a cancelListener because the ConnectTimer will
	    // be polling cancellation.
	    // As soon as the connectTimer is stopped due to a successful
	    // connection it registers the interrupting CancelListener.
	    */

	    String hostName = null;
	    if (remote)
		hostName = host.getHostName();

	    final HostInfo hostInfo;

	    tentativeDbx.startProgressManager().startProgress(shortNames, hostName);
	    tentativeDbx.startProgressManager().setCancelListener();

	    tentativeDbx.startProgressManager().updateProgress('>', 1, 
		Catalog.get("StartingDbx"), 0, 0);

	    //
	    // setup the IOPack
	    //

            if (DebuggerOption.RUN_IO.getCurrValue(optionLayers).equals("stdio")) { //NOI18N
                ndi.setInputOutput(null);
            }
	    ioPack = IOPack.create(remote, ndi, executor);
	    tentativeDbx.setIOPack(ioPack);
	    listener.assignIOPack(ioPack);

	    if (!connectExisting) {
		//
		// Build up startup arguments to dbx
		//

		List<String> avec = new ArrayList<String>();

		boolean use_ss_attach = false;
		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.capture_engine_startup)
		    use_ss_attach = true;
		if (use_ss_attach) {
		    avec.add("ss_attach"); // NOI18N
		    // for debugging ss_attach purpose 
		    // avec.add("-v");
		    avec.add(dbxname);
		    dbxname = "ss_attach"; // NOI18N
		} else {
		    avec.add(dbxname);
		}

		// flags to get dbx going as a glue service
		String glue_opts = "sync";		// NOI18N
		glue_opts += ",stdio";		// NOI18N
		if (beMaster) {
		    NetAddr na;
		    if (remote) {
			hostInfo = new HostInfo(host.getHostName(),
						host.getHostLogin(),
						RemoteSupport.securityStyle(host.getSecuritySettings()),
						host.getRemoteStudioLocation());

			na = tentativeDbx.callback_remote_addr(hostInfo);
			if (na == null) {
			    String message = failMsg();
			    DialogDisplayer.getDefault().
				notify(new NotifyDescriptor.Message(message));
			    cancelStartup();
			    tentativeDbx.startProgressManager().finishProgress();
			    return;
			}
		    } else {
			hostInfo = null;
			na = tentativeDbx.callback_local_addr();
		    }

		    glue_opts += ",master=" + na.toString();	// NOI18N
		    glue_opts += ",callback_key=" +			// NOI18N
				 tentativeDbx.callback_key();
		} else {
		    hostInfo = null;
		}

		avec.add("-g");	                 // NOI18N
		avec.add(glue_opts);                 // NOI18N

		if (exec32)
		    avec.add("-xexec32"); // NOI18N

		if (dbxInitFile != null) {
		    avec.add("-s"); // NOI18N
		    avec.add(dbxInitFile);
		}

		if (additionalArgv != null) {
		    for (int i = 1; i < additionalArgv.length; i++) {
			// NOTE: we're skipping argv[0]!
			avec.add(additionalArgv[i]);
		    }
		}

		dbx_argv = avec.toArray(new String[avec.size()]);

		// echo arguments
		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		    System.out.printf("CommonDbx.Factory.start():\n"); // NOI18N
		    System.out.printf("\texecutable: %s\n", dbxname); // NOI18N
		    for (int cx = 0; cx < dbx_argv.length; cx++)
			System.out.printf("\t  argv[%2d]: %s\n", cx, dbx_argv[cx]); // NOI18N
		}
	    } else {
		hostInfo = null;
	    }


	    /* OLD
	    //
	    // setup the IOPack
	    //

	    ioPack = getIOPack();
	    ioPack.setup(remote);
	    tentativeDbx.setIOPack(ioPack);
	    listener.assignIOPack(ioPack);
	    */

	    additionalEnv = new HashMap<String, String>();

	    boolean preload_rtc = false;
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.preload_rtc)
		preload_rtc = true;

	    if (preload_rtc) {
		String rtc_path_32 = System.getenv("SPRO_RTC_AUDIT_32"); // NOI18N
		String rtc_path_64 = System.getenv("SPRO_RTC_AUDIT_64"); // NOI18N
		if (rtc_path_32 != null)  {
		    additionalEnv.put("LD_AUDIT_32", rtc_path_32); // NOI18N
		    // OLD System.setProperty("LD_AUDIT_32", rtc_path_32);
		}
		if (rtc_path_64 != null) {
		    additionalEnv.put("LD_AUDIT_64", rtc_path_64); // NOI18N
		    // OLD System.setProperty("LD_AUDIT_64", rtc_path_64);
		}
	    }

	    /* OLD
	    Commenting out as fix for 6823380.
	    // Fix for 5016389 (and again for 6227968)
	    if (tentativeDbx.callback_path() != null) {
		additionalEnv.setValueOf("_ST_GLUE_CALLBACK_PATH",// NOI18N
		                         tentativeDbx.callback_path());
	    }
	    */


	    //
	    // pass on control to start2 ...
	    //

	    if (NativeDebuggerManager.isAsyncStart()) {
		NativeDebuggerManager.getRequestProcessor().post(new Runnable() {
		    public void run() {
			start2(hostInfo);
		    }
		} );

	    } else {
		start2(hostInfo);
	    }
	}


	private void start2(HostInfo hostInfo) {

	    int pid = 0;

	    //
	    // Start dbx
	    //
	    if (!connectExisting) {
		pid = executor.startEngine(dbxname, dbx_argv, null, null,
					   ioPack.console(), true, false);
		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		    System.out.printf("CommonDbx.Factory.start(): " + // NOI18N
				      "startEngine -> pid %d\n", pid); // NOI18N
		}
		if (pid == 0) {
		    tentativeDbx.startProgressManager().finishProgress();
		    listener.connectFailed("dbx", executor.getStartError(), null); // NOI18N
		    return;
		}

		tentativeDbx.setExecutor(executor);
	    }


	    boolean havePio = false;
	    if (!connectExisting)
		havePio = ioPack.start();
	    if (!havePio) {
		// SHOULD do something
	    }

	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		System.out.printf("CommonDbx.Factory.start(): remote %s\n", // NOI18N
		    remote);
	    }

	    tentativeDbx.startProgressManager().updateProgress('<', 1, null, 0, 0);

	    tentativeDbx.startProgressManager().updateProgress('>', 1,
					       Catalog.get("ConnectDbx"),
					       0, 0);

	    //
	    // do the glue connect
	    //

	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
		System.out.printf("CommonDbx.Factory.start(): connecting ...\n"); // NOI18N

	    if (!tentativeDbx.connect(hostInfo, pid)) {
		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		    System.out.printf("CommonDbx.Factory.start(): " + // NOI18N
				      "it failed\n"); // NOI18N
		}

		tentativeDbx.disconnected();

		// Use a timer instead if plain SwingUtilities.invokeLater()
		// so StreamTerm's OutputMonitor gets a chance to copy
		// dbx output to the Term

		javax.swing.Timer waitForOutputTimer;
		waitForOutputTimer = new javax.swing.Timer(3 * 1000,
		    new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
			    recoverFromFailedConnect(false);
			}
		    });
		waitForOutputTimer.setRepeats(false);
		waitForOutputTimer.start();

		return;

	    } else {
		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		    System.out.printf("CommonDbx.Factory.start(): " + // NOI18N
				      "it suceeded\n"); // NOI18N
		}
	    }

	    //
	    // point of no return
	    //

	    if (tentativeDbx.fl_async()) {

		if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
		    System.out.printf("CommonDbx.Factory.start2(): " + // NOI18N
		                      "runNotifier\n"); // NOI18N
		}

		NotifierThread.runNotifier();


		timer = new ConnectTimer();
		timer.start();

		// we will ultimately end up in connectionAvailable()

	    } else {
		// initializeDbx will send glue messages and since we've
		// started the thread we need to send stuff on the awt-eventq
		// to serialise surrogate access

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			connectionAvailable(true);
		    }
		});
	    }
	}

	private void cancelStartup() {
	    if (timer != null) {
		timer.stop();
		timer = null;
	    }
	}

	private void connectionAvailable(boolean success) {
	    tentativeDbx.startProgressManager().updateProgress('<', 1, null, 0, 0);
	    cancelStartup();
	    if (success) {
		listener.assignDbx(tentativeDbx);
		tentativeDbx.initializeDbx();
	    }
	}

	/**
	 * Cleanup Masters sockets.
	 * Called from DbxGuiModule.close().
	 * Master uses Runtime.addShutdownHook() to close itself but that
	 * doesn't seem to work under NB.
	 */
	public static void close() {
	    if (master != null) {
		master.close_down();
		master = null;
	    }
	}
    }

    private IOPack ioPack;
    private Executor executor;
    private final Factory factory;	// backpointer to creating factory
    private final boolean connectExisting;

    /**
     * Set defaults which don't match dbx defaults
     */

    private void overrideOptions() {

	prop_set("DBX_vdl_mode", "lisp");	// NOI18N
	prop_set("DBX_vdl_version", "4");	// NOI18N

	//
	// The following SHOULD really be set as a side-effect of option
	// application.
	//

	// IDE defaults which don't match dbx defaults
	prop_set("DBX_run_autostart", "on");	// NOI18N
	prop_set("DBX_scope_look_aside", "on"); // NOI18N
        prop_set("DBX_macro_expand", "on"); // NOI18N
        prop_set("DBX_macro_source", "skim_unless_compiler"); // NOI18N
        prop_set("DBX_output_pretty_print", DebuggerOption.OUTPUT_PRETTY_PRINT.getCurrValue(factory().optionLayers)); // NOI18N
	// prop_set("DBX_output_inherited_members", "off"); // NOI18N

	// Arrange for dbx victims to run under the Pio
	boolean ioInWindow = DebuggerOption.RUN_IO.getCurrValue(factory().optionLayers).equals("window"); //NOI18N

	String slaveName = null;
	if (!factory.connectExisting())
	    slaveName = getIOPack().getSlaveName();

	if (slaveName != null && ioInWindow) {
	    prop_set("DBX_run_io", "pty");	// NOI18N
	    prop_set("DBX_run_pty", slaveName); // NOI18N
	}
    }

    protected void initializeDbx() {
	overrideOptions();
    }

    protected CommonDbx(Factory factory,
			Notifier n,
			int flags,
			boolean connectExisting,
			Master master) {

	super(n, flags, master);
	this.factory = factory;
	this.connectExisting = connectExisting;
    }

    public Factory factory() {
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
	/* LATER ?
	if (startProgressManager().isCancelled()) 
	    interrupt();
	*/
    }

    protected Executor getExecutor() {
	return executor;
    }

    private int N_alive_checks;

    private boolean connectionTimedOut() {
	// glues sleeptime (in milliseconds) goes like this:
	//	N_alive_checks		cumulative
	// 10		1
	// 20		2
	// 40		3
	// 80		4
	// 160		5
	// 320	        6
	// 640		7		1270 msec
	// 1280		8		2550
	// ten more attempts at 1280 milliseconds

	return N_alive_checks > 8;
    }

    private static final Coord origin = new Coord();

    private boolean sawOutput() {
	// OLD IOPack ioPack = this.getIOPack();
	if (ioPack != null) {
	    Term term = ioPack.console().getTerm();
	    if (term != null) {
		return term.getCursorCoord().compareTo(origin) > 0;
	    }
	}
	return false;
    }

    // override Surrogate
    @Override
    protected boolean is_slave_alive() {
	// Not really used since we're not employing synchronous connects
	// anymore.
	// Executor.isAlive() isn't properly implemented yet, but again,
	// in the remote connect scenario we're not going to get called.
        if (executor == null) {
            // setExecutor() hasn't been called yet
            return true;
        } else if (executor.isAlive()) {
            N_alive_checks++;
            if (connectionTimedOut())
                return false;
            else
                return true;
        } else {
            return false;
        }
    }

    /**
     * Common connection failure dialog/panel.
     * It will display the Term so we can see the last breath of the dying
     * dbx or whatever.
     * Note that this Term in effect will be grabbed away from the OW window
     * which is OK since the session is going away anyway.
     *
     * was: DbxDebuggerImpl.connectionFailure()
     *
     * before that
     * was: Various copy/paste/edited variations of this existed in prior
     * releases:
     * - Dbx.rude_disconnect()
     * - Dbx.start() (after a failed 'connect()')
     */

    public static void dyingWords(String msg, IOPack ioPack) {

	JPanel panel = new JPanel();
	Catalog.setAccessibleDescription(panel, 
	    "ACSD_DbxsDyingWords");	// NOI18N
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



    //
    // Utilities for sending stuff to dbx

    //

    public void postEnvvars(String [] envvars, String[] removed) {
	if (removed != null)
	    for (int i = 0; i < removed.length; i++) {
		sendCommandHelp(0, 0, "unset " + removed[i].toString());//NOI18N
	    }
	if (envvars != null)
	    for (int i = 0; i < envvars.length; i++) {
		sendCommandHelp(0, 0, "export " + envvars[i].toString());//NOI18N
	    }
    }

    protected void sendCommandHelp(int routingToken, int flags, String cmd) {
//	assert SwingUtilities.isEventDispatchThread();
	ksh_cmd(routingToken, flags, cmd);
    }





    //
    // The below is strictly the glue protocol
    //

    @Override
    public void svc_available(boolean success) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
	    System.out.printf("CommonDbx.svc_available(%s)\n", success); // NOI18N

	if (success) {
	    if (fl_async())
		factory.connectionAvailable(success);
	} else {
	}

    }

    /**
     * Dbx has died
     */
    @Override
    public void rude_disconnect() {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
	    System.out.printf("CommonDbx.rude_disconnect()\n"); // NOI18N
	factory.cancelStartup();
	disconnected();
	startProgressManager().finishProgress();
    }

    @Override
    public void disconnected() {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
	    System.out.printf("CommonDbx.disconnected()\n"); // NOI18N
	if (ioPack != null)
	    ioPack.bringDown();
    }

    protected void interrupt() {
	if (executor == null) {
	    factory.cancelStartup();
	    disconnected();
	    startProgressManager().finishProgress();

	    return;
	}

	try {
	    executor.interruptGroup();
	} catch (IOException e) {
	    ErrorManager.getDefault().annotate(e, 
		"Sending kill signal to process group failed"); // NOI18N
	    ErrorManager.getDefault().notify(e);
	}
    }

    // override GPDbxSurrogate
    @Override
    protected void output(String str, boolean ready) {
    }

    // override GPDbxSurrogate
    @Override
    protected void dir_changed(String dir) {
    }

    // override GPDbxSurrogate
    @Override
    protected void ksh_notify(int argc, String argv[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void ksh_scmd_result(int rt, String result) {
    }

    // override GPDbxSurrogate
    @Override
    protected void jn_mode_update(int mode) {
    }

    // override GPDbxSurrogate
    @Override
    protected void prog_finished(String progname) {
    }

    // override GPDbxSurrogate
    @Override
    protected void prog_loading(String progname) {
    }

    // override GPDbxSurrogate
    @Override
    protected void prog_loaded(String progname, boolean success) {
    }

    // override GPDbxSurrogate
    @Override
    protected void prog_unloaded() {
    }

    // override GPDbxSurrogate
    @Override
    protected void prog_visit(GPDbxLocation vl) {
    }

    // override GPDbxSurrogate
    @Override
    protected void prog_runargs(int argc, String argv[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void prog_redir(String infile, String outfile,
				    boolean append) {
    }

    // override GPDbxSurrogate
    @Override
    protected void prog_datamodel(int bit_width) {
    }

    // override GPDbxSurrogate
    @Override
    protected int popup(int rt, String title, int nitems, String item[], boolean cancelable) {
	return 0;
    }

    // override GPDbxSurrogate
    @Override
    protected int popup2(int rt, String title, int nitems, String item[], boolean cancelable, boolean multiple_selection, String cookie) {
	return 0;
    }

    // override GPDbxSurrogate
    @Override
    protected void clone(int argc, String argv[],
			       boolean cloned_to_follow) {
    }

    // override GPDbxSurrogate
    @Override
    protected void busy(String with_what,
			      boolean on_off,
			      boolean block_input) {
    }

    // override GPDbxSurrogate
    @Override
    protected void capabilities(GPDbxCapabilities capabs) {
    }

    // override GPDbxSurrogate
    @Override
    protected void thread_capabilities(GPDbxThreadCapabilities thread_capabs) {
    }

    // override GPDbxSurrogate
    @Override
    protected void button(String label, String cmd, String option) {
    }

    // override GPDbxSurrogate
    @Override
    protected void unbutton(String label) {
    }

    // override GPDbxSurrogate
    @Override
    protected void env_changed(String name, String new_value) {
    }

    // override GPDbxSurrogate
    @Override
    protected boolean load_symbols(int rtRoutingToken) {
	return false;
    }

    // override GPDbxSurrogate
    @Override
    protected boolean rcmd(String hostString, int pid, String cmdString) {
	return false;
    }

    // override GPDbxSurrogate
    @Override
    protected void rconnect(com.sun.tools.swdev.glue.NetAddr addrNetAddr, String hostnameString) {
    }

    // override GPDbxSurrogate
    @Override
    protected void rgrab_attention() {
    }

    // override GPDbxSurrogate
    @Override
    protected void rlist(GPDbxRList list) {
    }

    // override GPDbxSurrogate
    @Override
    protected boolean rswitch(String host, int pid) {
	return false;
    }

    // override GPDbxSurrogate
    @Override
    protected void rmove(boolean backward) {
    }

    // override GPDbxSurrogate
    @Override
    protected void manifest_mark(String mark, GPDbxLocation location) {
    }

    // override GPDbxSurrogate
    @Override
    protected void bpt_set(int id, String filename, int line, GPDbxLocation loc) {
    }

    // override GPDbxSurrogate
    @Override
    protected void bpt_del(int id) {
    }

    // override GPDbxSurrogate
    @Override
    protected void handler_new(GPDbxHandler h, int rt) {
    }

    // override GPDbxSurrogate
    @Override
    protected void handler_replace(GPDbxHandler h, int rt) {
    }

    // override GPDbxSurrogate
    @Override
    protected void handler_delete(int id, int rt) {
    }

    // override GPDbxSurrogate
    @Override
    protected void handler_defunct(int id, int rt) {
    }

    // override GPDbxSurrogate
    @Override
    protected void handler_undo_defunct(int id, int rt) {
    }

    // override GPDbxSurrogate
    @Override
    protected void handler_enable(int id, boolean v) {
    }

    // override GPDbxSurrogate
    @Override
    protected void handler_count(int id, int current, int limit) {
    }

    // override GPDbxSurrogate
    @Override
    protected void handler_list(int count, GPDbxHandler list[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void handler_batch_begin() {
    }

    // override GPDbxSurrogate
    @Override
    protected void handler_batch_end() {
    }

    // override GPDbxSurrogate
    @Override
    protected void display_item_new(int id, String plain_lhs, int rt, String qualified_lhs, String static_type, int is_a_pointer, String reevaluable_lhs, boolean unrestricted) {
    }

    // override GPDbxSurrogate
    @Override
    protected void display_item_new2(int rt, GPDbxDisplaySpec spec) {
    }

    // override GPDbxSurrogate
    @Override
    protected void display_item_dup(int rt, int id) {
    }

    // override GPDbxSurrogate
    @Override
    protected void display_item_delete(int id, int rt) {
    }

    // override GPDbxSurrogate
    @Override
    protected void display_update_0(int nitems, GPDbxDisplayItem0 items[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void display_update(int nitems, GPDbxDisplayItem items[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void disassembly(GStr regs_str) {
    }

    // override GPDbxSurrogate
    @Override
    protected void registers(GStr regs_str) {
    }

    // override GPDbxSurrogate
    @Override
    protected void memorys(GStr mem_str) {
    }

    // override GPDbxSurrogate
    @Override
    protected void locals(int nitems, GPDbxLocalItem items[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void expanded_nodes(boolean is_local, int nitems, GPDbxLocalItem items[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void vitem_new(int rt, GPDbxVItemStatic sitem) {
    }

    // override GPDbxSurrogate
    @Override
    protected void vitem_replace(int rt, GPDbxVItemStatic sitem) {
    }

    // override GPDbxSurrogate
    @Override
    protected void vitem_add(GPDbxVItemStatic sitem, int id) {
    }

    /* vitem_delete now has a newer interface, i.e., 2 parameters
     * instead of 1.  The dbx.gp change will keep dbx backward
     * compatible with older version of the workshop.  
     *
     * However this new interface might become unnecessary when
     * we implement "glocal array view" because the proc_gone
     * is a hack to inform dbxgui whether this call is made
     * when the process is about to be terminated.
     *
     * With global array view, vitem_delete will not be called
     * simply at the end of the process.  It will only be called
     * when there is an explicit delete request.
     */

    // override GPDbxSurrogate
    @Override
    protected void vitem_delete(int id, boolean proc_gone) {
    }

    // override GPDbxSurrogate
    @Override
    protected void vitem_update(int nitems, GPDbxVItemDynamic items[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void vitem_update_mode(int id, int new_mode) {
    }

    // override GPDbxSurrogate
    @Override
    protected void vitem_timer(float seconds) {
    }

    // override GPDbxSurrogate
    @Override
    protected void prop_decl(int nprop, GPDbxPropDeclaration prop[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void prop_changed(String name, String new_value) {
    }

    // override GPDbxSurrogate
    @Override
    protected void signal_list(int count,
				     GPDbxSignalInfoInit initial_signal_list[]){
    }

    // override GPDbxSurrogate
    @Override
    protected void signal_list_state(GPDbxSignalInfo updated_signal){
    }

    // override GPDbxSurrogate
    @Override
    protected void pathmap_list(int count,
				      GPDbxPathMap updated_pathmap[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void intercept_list(boolean unhandled,
					boolean unexpected,
					int count, String typenames[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void intercept_except_list(int count, String typenames[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void loadobj_loading(String loadobj) {
    }

    // override GPDbxSurrogate
    @Override
    protected void loadobj_loaded(String loadobj, boolean success) {
    }

    // override GPDbxSurrogate
    @Override
    protected void proc_new_from_prog(int pid, long ttydev) {
    }

    // override GPDbxSurrogate
    @Override
    protected void proc_new_from_pid(int pid, long ttydev) {
    }

    // override GPDbxSurrogate
    @Override
    protected void proc_new_from_core(String corefilename) {
    }

    // override GPDbxSurrogate
    @Override
    protected void proc_visit(GPDbxLocation vl, int vframe) {
    }

    // override GPDbxSurrogate
    @Override
    protected void proc_go() {
    }

    // override GPDbxSurrogate
    @Override
    protected void proc_stopped(GPDbxLocation hl, int nevents, GPDbxEventRecord events[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void proc_modified(GPDbxLocation hl) {
    }

    // override GPDbxSurrogate
    @Override
    protected void proc_gone(String reason, int info) {
    }

    // override GPDbxSurrogate
    @Override
    protected void proc_thread(int tid, GPDbxLocation hl, GPDbxLocation vl, int htid) {
    }

    // override GPDbxSurrogate
    @Override
    protected void proc_about_to_fork(int tid, int htid, String str) {
    }

    // override GPDbxSurrogate
    @Override
    protected void expr_eval_result(int rt, String value) {
    }

    // override GPDbxSurrogate
    @Override
    protected void expr_qualify_result(int rt, String expr,
				       String qualified_expr,
				       String reevaluable_expr,
				       int error) {
    }

    // override GPDbxSurrogate
    @Override
    protected void expr_heval_result(int rt, GPDbxHEvalResult result) {
    }

    // override GPDbxSurrogate
    @Override
    protected void expr_type_result(int rt, String expr, String stype, String dtype) {
    }

    // override GPDbxSurrogate
    @Override
    protected void expr_set_result(int rt, String value) {
    }

    // override GPDbxSurrogate
    @Override
    protected void type_info_result(int rt, String def) {
    }

    // override GPDbxSurrogate
    @Override
    protected void expr_line_eval_result(int rt1, int rt2, int flags, String lhs, String rhs) {
    }

    // override GPDbxSurrogate
    @Override
    protected void expr_line_evalall_result(int rt1, int rt2, int flags, String lhs, String rhs, String type, String rhs_deref) {
    }

    // override GPDbxSurrogate
    @Override
    protected void stack(int nf, int vf, GPDbxFrame frame[], int flags) {
    }

    // override GPDbxSurrogate
    @Override
    protected void threads(int tot, int shown, GPDbxThread thread[], int flags) {
    }

    // override GPDbxSurrogate
    @Override
    protected void error(int rt, int nerr, GPDbxError errors[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected void perf_file(String dir, String file, String group) {
    }

    // override GPDbxSurrogate
    @Override
    protected void perf_options(GPDbxPerfOptions options) {
    }

    // override GPDbxSurrogate
    @Override
    protected void perf_events_status(GPDbxPerfEventsStatus status) {
    }

    // override GPDbxSurrogate
    @Override
    protected void perf_open() {
    }

    // override GPDbxSurrogate
    @Override
    protected void perf_close() {
    }

    // override GPDbxSurrogate
    @Override
    protected void rtc_state(GPDbxRtcState state) {
    }

    // override GPDbxSurrogate
    @Override
    protected void rtc_patching(byte beginEnd, String label, String message,
				int count, int total) {
    }

    // override GPDbxSurrogate
    @Override
    protected void mprof_state(GPDbxMprofState state) {
    }

    // override GPDbxSurrogate
    @Override
    protected void rtc_access_item(GPDbxRtcItem item) {
    }

    // override GPDbxSurrogate
    @Override
    protected void mprof_leak_report_begin(GPDbxMprofHeader header) {
    }

    // override GPDbxSurrogate
    @Override
    protected void mprof_leak_report_end() {
    }

    // override GPDbxSurrogate
    @Override
    protected void mprof_leak_report_stopped() {
    }

    // override GPDbxSurrogate
    @Override
    protected boolean mprof_leak_item(GPDbxMprofItem item) {
	return true;    // wasn't interrupted, keep going
    }

    // override GPDbxSurrogate
    @Override
    protected void mprof_use_report_begin(GPDbxMprofHeader header) {
    }

    // override GPDbxSurrogate
    @Override
    protected void mprof_use_report_end() {
    }

    // override GPDbxSurrogate
    @Override
    protected void mprof_use_report_stopped() {
    }

    // override GPDbxSurrogate
    @Override
    protected boolean mprof_use_item(GPDbxMprofItem item) {
	return true;    // wasn't interrupted, keep going
    }

    // override GPDbxSurrogate
    @Override
    protected void fix_start(String wd, String cmd, String file) {
    }

    // override GPDbxSurrogate
    @Override
    protected void fix_status(boolean succeeded, String errfile) {
    }

    // override GPDbxSurrogate
    @Override
    protected void fix_done(int attempted, int succeeded) {
    }

    // override GPDbxSurrogate
    @Override
    protected void fix_pending_build(String target,
					   int n, String file[]) {
    }

    // override GPDbxSurrogate
    @Override
    protected boolean save_file(String filename) {
	return false;
    }
}
