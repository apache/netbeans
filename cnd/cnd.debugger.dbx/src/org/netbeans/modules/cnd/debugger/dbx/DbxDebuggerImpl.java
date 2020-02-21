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

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionClient;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionValue;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionLayers;
import org.netbeans.modules.cnd.debugger.common2.utils.options.Option;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.openide.text.Line;

import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;

import org.netbeans.api.debugger.Watch;

import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.ContextProvider;


import com.sun.tools.swdev.glue.dbx.*;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JEditorPane;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;

import org.netbeans.modules.cnd.debugger.common2.utils.Executor;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;

import org.netbeans.modules.cnd.debugger.common2.debugger.Address;
import org.netbeans.modules.cnd.debugger.common2.debugger.Thread;
import org.netbeans.modules.cnd.debugger.common2.debugger.Frame;
import org.netbeans.modules.cnd.debugger.common2.debugger.Variable;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeWatch;
import org.netbeans.modules.cnd.debugger.common2.debugger.WatchVariable;
import org.netbeans.modules.cnd.debugger.common2.debugger.WatchModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.LocalModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.StackModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.ThreadModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.VarContinuation;
import org.netbeans.modules.cnd.debugger.common2.debugger.EvaluationWindow;
import org.netbeans.modules.cnd.debugger.common2.debugger.Error;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.EvalAnnotation;
import org.netbeans.modules.cnd.debugger.common2.debugger.SignalDialog;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointManager.BreakpointMsg;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointManager.BreakpointOp;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointManager.BreakpointPlan;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.Handler;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerCommand;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerExpert;

import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Controller;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.DisFragModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.MemoryWindow;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.RegistersWindow;

import org.netbeans.modules.cnd.debugger.dbx.arraybrowser.ArrayBrowserController;
import org.netbeans.modules.cnd.debugger.dbx.arraybrowser.ArrayBrowserWindow;

import org.netbeans.modules.cnd.debugger.dbx.actions.DbxStartActionProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.io.IOPack;

import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Signals;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;

import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcOption;
import org.netbeans.modules.cnd.debugger.dbx.rtc.Loadobjs;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcProfile;

import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcState;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcController;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcView;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcModel;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcMarker;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;

import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStartManager;
import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStart;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettingsBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.ToolTipView;
import org.netbeans.modules.cnd.debugger.common2.debugger.ToolTipView.VariableNode;
import org.netbeans.modules.cnd.debugger.common2.debugger.ToolTipView.VariableNodeChildren;

// for rebuildOnNextDebug
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Disassembly;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.FormatOption;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RTCWindowAction;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcTopComponent;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CndRemote;
import org.netbeans.modules.cnd.debugger.common2.utils.FileMapper;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.debugger.ui.ToolTipUI;
import org.netbeans.spi.debugger.ui.ViewFactory;
import org.netbeans.spi.viewmodel.ModelListener;
import org.openide.nodes.Node;

/**
 * A "service" for DebuggerEngine
 * I.e. our parallel object to cores DebuggerEngine.
 * was: DbxDebugger
 */
public final class DbxDebuggerImpl extends NativeDebuggerImpl
        implements BreakpointProvider, CommonDbx.Factory.Listener {

    private DbxEngineProvider engineProvider;
    private final DebuggerSettingsBridge profileBridge;
    private Dbx dbx;				// corresponding glue surrogate
    private final DbxHandlerExpert handlerExpert;
    private final RtcState rtcState = new RtcState();

    private final DisModel disModel = new DisModel();
    private final DisController disController = new DisController();
    private final DbxDisassembly disassembly;
    private boolean update_dis = true;
    
    /**
     * The last run/step/next/step out command sent to dbx.
     * This is so that we can do something reasonable during fork.
     */
    private String lastRunCmd = null;

    // routing tokens passed to various calls to expr_heval and
    // checked for in setChasedPointer().
    private final static int RT_CHASE_WATCH = 1;
    private final static int RT_CHASE_LOCAL = 2;
    private final static int RT_EVAL_AUTO = 3;
    private final static int RT_EVAL_AUTO_LAST = 4;
    private final static int RT_CHASE_AUTO = 5;
    private final static int RT_EVAL_TOOLTIP = 6;
    final static int RT_EVAL_REGISTER = 7;

    public DbxDebuggerImpl(ContextProvider ctxProvider) {
        super(ctxProvider);

        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("DbxDebuggerImpl.<init>()\n"); // NOI18N
        }

        // cache a pointer to our provider.
        // We only need it to call the destructor so far.

        List<? extends DebuggerEngineProvider> l = debuggerEngine.lookup(null, DebuggerEngineProvider.class);
        for (DebuggerEngineProvider e : l) {
            if (e instanceof DbxEngineProvider) {
                engineProvider = (DbxEngineProvider) e;
            }
        }
        if (engineProvider == null) {
            throw new IllegalArgumentException("DbxDebuggerImpl not started via DbxEngineProvider"); // NOI18N
        }

        //
        // enhance State
        //

        // Actually SHOULD control this by prop sets
        state().capabAutoRun = true;

        profileBridge = new DbxDebuggerSettingsBridge(this);
        handlerExpert = new DbxHandlerExpert(this);
        disassembly = new DbxDisassembly(this, breakpointModel());
    }

    // interface NativeDebugger
    @Override
    public String debuggerType() {
        return "dbx"; // NOI18N
    }

    @Override
    public DebuggerSettingsBridge profileBridge() {
        return profileBridge;
    }

    public Dbx dbx() {
        return dbx;
    }

    /**
     * Return true if it's OK to send messages to dbx
     */
    public boolean isConnected() {
        // See "README.startup"
        if (dbx == null || !dbx.connected() || postedKillEngine) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Called when this session has been switched to
     * 'redundant' is true when we double-click on the same session.
     */
    @Override
    public void activate(boolean redundant) {

        if (isConnected()) {

            super.activate(redundant);
            
//            disassemblerWindow().setDebugger(this);
//            disassemblerWindow().getView().setModelController(disModel(),
//							  disController(),
//							  disStateModel(),
//							  breakpointModel());

	    rtcView.switchTo();

            /* disable the ABW creation for now */
            if (org.netbeans.modules.cnd.debugger.dbx.Log.ArrayBrowser.enabled) {
                ArrayBrowserWindow.getDefault().setDebugger(this);
                ArrayBrowserWindow.getDefault().setArrayBrowserController(arrayBrowserController);
            }
        } else {

            // We really don't want to enable the actions until we have
            // a proper connection.
            // However ... if the connection goes bad we want to be
            // able to clean up the session (in connectfailed())
            // but Session.kill() goes through DebuggerEngine.ACTION_KILL)
            // which is disabled!

            // If the user presses buttons while progress is being made
            // chaos will ensue.
            // We could special case this and disable all buttons but
            // Finish but even pressing that when in the middle of startup
            // can lead to unpredictable behaviour
            //
            // How about enabling the action right before killing
            // the session? LATER ...

            updateActions();
        }


        if (redundant) {
            return;
        }
    }

    /**
     * Called when this session has been switched away from
     * 'redundant' is true when we double-click on the same session.
     */
    @Override
    public void deactivate(boolean redundant) {
        super.deactivate(redundant);
        if (redundant) {
            return;
        }
        RtcMarker.getDefaultError().relinquish(this);
        RtcMarker.getDefaultFrame().relinquish(this);
    }

    // was: Dbx.rude_disconnect
    public void rudeDisconnect() {

        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("DbxDebuggerImpl.rudeDisconnect()\n"); // NOI18N
        }
	// clean up Profile pcs stuff
	// CR 7003638
        profileBridge().noteProgUnloaded();

        if (executor.destroyedByHand()) {
            getIOPack().bringDown();

        } else {
            String msg = Catalog.get("DbxDiedInfo");
            CommonDbx.dyingWords(msg, getIOPack());
        }
    }

    /**
     * Record chosen target in pick list (if not core file)
     *
     * was: beginning of startDbx()
     */
    private void rememberTarget(DbxDebuggerInfo ddi) {
    }


    /**
     * It all starts here
     *
     * Fork a dbx and establish a glue connection to it.
     * On a successful connect call initializeDbx().
     *
     * was: A glomming/mutation of rainier DbxDebugger.startDbx() & Dbx.start()
     * But also consider DbxDebugger.start(), startDebugger() and
     * continueStartup().
     * The order used to be:
     * 	startDebugger()
     *	continueStartup()
     *	startDbx()
     */
    private DbxDebuggerInfo ddi;

    public void rememberDDI(DbxDebuggerInfo d) {
        this.ddi = d;
    }

    public NativeDebuggerInfo getNDI() {
        return ddi;
    }

    boolean isShortName() {
        DebuggerOption option = DebuggerOption.OUTPUT_SHORT_FILE_NAME;
        return option.isEnabled(optionLayers());
    }

    /**
     * Utility to convert pathnames to short form based on
     * 'output_short_file_name'.
     */
    String shortname(String path) {
        if (isShortName()) {
            return CndPathUtilities.getBaseName(path);
        } else {
            return path;
        }
    }

    // interface NativeDebugger
    @Override
    public boolean isDynamicType() {
	// CR 6502043
	// CR 6879383
        DebuggerOption option = DebuggerOption.OUTPUT_DYNAMIC_TYPE;
        return option.isEnabled(optionLayers());
    }

    // interface NativeDebugger
    @Override
    public void setDynamicType(boolean b) {
	setDbxOption("DBX_output_dynamic_type", b);		// NOI18N
    }

    // interface NativeDebugger
    @Override
    public boolean isInheritedMembers() {
	// CR 6502043
	// CR 6879383
        DebuggerOption option = DebuggerOption.OUTPUT_INHERITED_MEMBERS;
        return option.isEnabled(optionLayers());
    }

    // interface NativeDebugger
    @Override
    public void setInheritedMembers(boolean b) {
	setDbxOption("DBX_output_inherited_members", b);	// NOI18N
    }

    // interface NativeDebugger
    @Override
    public boolean isStaticMembers() {
        DebuggerOption option = DebuggerOption.SHOW_STATIC_MEMBERS;
        return option.isEnabled(optionLayers());
    }

    // interface NativeDebugger
    @Override
    public void setStaticMembers(boolean b) {
	setDbxOption("DBX_show_static_members", b);	// NOI18N
    }
    
    // interface NativeDebugger
    @Override
    public boolean isPrettyPrint() {
        DebuggerOption option = DebuggerOption.OUTPUT_PRETTY_PRINT;
        return option.isEnabled(optionLayers());
    }

    // interface NativeDebugger
    @Override
    public void setPrettyPrint(boolean b) {
	setDbxOption("DBX_output_pretty_print", b);	// NOI18N
    }

    // interface NativeDebugger
    @Override
    public String[] formatChoices() {
	return new String[] {"8", "10", "16", "automatic"};	// NOI18N
    }

    public void start(final DbxDebuggerInfo ddi) {
	// SHOULD factor with GdbDebuggerImpl

        //
        // The following is what used to be in startDebugger():
        //

        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            int act = ddi.getAction();
            System.out.printf("START ==========\n\t"); // NOI18N
            if ((act & NativeDebuggerManager.RUN) != 0) {
                System.out.printf("RUN "); // NOI18N
            }
            if ((act & NativeDebuggerManager.STEP) != 0) {
                System.out.printf("STEP "); // NOI18N
            }
            if ((act & NativeDebuggerManager.ATTACH) != 0) {
                System.out.printf("ATTACH "); // NOI18N
            }
            if ((act & NativeDebuggerManager.CORE) != 0) {
                System.out.printf("CORE "); // NOI18N
            }
            if ((act & NativeDebuggerManager.LOAD) != 0) {
                System.out.printf("LOAD "); // NOI18N
            }
            if ((act & NativeDebuggerManager.CONNECT) != 0) {
                System.out.printf("CONNECT "); // NOI18N
            }
            System.out.printf("\n"); // NOI18N
        }

        rememberDDI(ddi);
        session().setSessionHost(ddi.getHostName());

        final boolean connectExisting;
        if ((ddi.getAction() & NativeDebuggerManager.CONNECT) != 0) {
            connectExisting = true;
        } else {
            connectExisting = false;
        }

        // used to be in debug():
        // TMP_PM
        profileBridge.setup(ddi);
        if (!connectExisting) {
	    executor = Executor.getDefault(Catalog.get("Dbx"), getHost(), 0);
        }

        final String additionalArgv[] = ddi.getAdditionalArgv();

        if (ddi.isCaptured()) {
            ExternalStart xstart = ExternalStartManager.getXstart(getHost());
            if (xstart != null) {
		xstart.debuggerStarted();
	    }
        }

        // See "README.startup"
        if (NativeDebuggerManager.isAsyncStart()) {

            // May not be neccessary in the future.
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    start2(executor, additionalArgv, DbxDebuggerImpl.this, connectExisting);
                }
            });

        } else {
            CndRemote.validate(ddi.getHostName(), new Runnable() {
                public void run() {
                    start2(executor, additionalArgv, DbxDebuggerImpl.this, connectExisting);
                }
            });
        }
    }


    // LATER ... make ExecutorUnix be package private but make 
    // ExecutorJava and Unix both have remote capability.
    // LATER private Executor executor =Executor.getDefault(Catalog.get("Dbx"));

    private volatile CommonDbx.Factory factory;

    /**
     * @param additionalArgv Additional arguments to pass to dbx. (This
     * typically happens on cloning, where dbx will create arguments for
     * the "child" dbx being started to debug the child process
     */
    private boolean exec32 = false;

    private void start2(Executor executor,
            String additionalArgv[],
            Dbx.Factory.Listener listener,
            boolean connectExisting) {

        String dbxInitFile = DebuggerOption.DBX_INIT_FILE.getCurrValue(optionLayers());

        if (dbxInitFile.equals(DebuggerOption.DBX_INIT_FILE.getDefaultValue())) {
            dbxInitFile = null;
        } else {
            boolean preventInitPathConvertion = dbxInitFile.startsWith("///"); // NOI18N
            if (!preventInitPathConvertion) {
                dbxInitFile = localToRemote("dbxInitFile", dbxInitFile); // NOI18N
            }
        }

        // figure out 32-bit or 64-bit debuggee, only on Linux and not user pre-chosen 
        Host host = getHost();
        if (host != null && host.isLinux() && !ddi.is32bitEngine()) {
            boolean is64 = ddi.is64bitDebuggee(host);
            if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
                System.out.println("DbxDebuggerImpl.is64 " + is64); // NOI18N
            }

            if (is64) {
                DebuggerOption.OPTION_EXEC32.setCurrValue(optionLayers(), "off"); // NOI18N
            } else {
                DebuggerOption.OPTION_EXEC32.setCurrValue(optionLayers(), "on"); // NOI18N
            }
        }

        if (DebuggerOption.OPTION_EXEC32.isEnabled(optionLayers()) || ddi.is32bitEngine()) {
            exec32 = true;
        }

        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("DbxDebuggerImpl.start2() exec32 = %s\n", exec32); // NOI18N
        }

        // Figure out dbx command for IDE
        String dbxPath = null;
        if (!NativeDebuggerManager.isStandalone() /*&& !NativeDebuggerManager.isPL()*/) {
            dbxPath = getDebuggerString(DbxEngineCapabilityProvider.getDbxEngineType().getDebuggerID(), (MakeConfiguration)ddi.getConfiguration());
        } else {
            
        }
        factory = new Dbx.DbxFactory(executor, additionalArgv,
                                     listener, exec32, isShortName(),
                                     dbxInitFile, host, connectExisting, dbxPath,
                                     ddi.getInputOutput(), ddi, optionLayers());

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                factory.start();
            }
        });
    }

    // interface Dbx.Factory.Listener
    @Override
    public void assignDbx(CommonDbx tentativeDbx) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("DbxDebuggerImpl.assignDbx()\n"); // NOI18N
        }
        dbx = (Dbx) tentativeDbx;
        dbx.setDebugger(this);
        DbxStartActionProvider.succeeded();
        NativeDebuggerManager.get().setCurrentDebugger(this);
	stackEnabler.setConnected(true);
	localEnabler.setConnected(true);
	watchEnabler.setConnected(true);
	threadEnabler.setConnected(true);

    }

    // interface Dbx.Factory.Listener
    @Override
    public void assignIOPack(IOPack ioPack) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("DbxDebuggerImpl.assignIOPack()\n"); // NOI18N
        }
        setIOPack(ioPack);
    }

    // interface Dbx.Factory.Listener
    @Override
    public void connectFailed(String toWhom, String why, IOPack ioPack) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("DbxDebuggerImpl.connectFailed()\n"); // NOI18N
        }
        String msg = Catalog.format("ConnectionFailed", toWhom, why);
        CommonDbx.dyingWords(msg, ioPack);

        // kill() doesn't work unless ACTION_KILL is enabled.
        session.kill();
    }

    /**
     * Send any initial commands (like 'run' for Debug, or 'next' for
     * StepInto) after all initialization is done
     */
    private void initialAction() {

        if (ddi == null) {
            return;
        }

        if (NativeDebuggerManager.isStartModel() && (getJavaMode() == 0)) {
            // For load and run
            if ((ddi.getAction() & NativeDebuggerManager.RUN) != 0) {
                // if we're in attach - just cont
                if (ddi.getPid() != -1) {
                    go();
                } else {
                    rerun();
                }
                ddi.removeAction(NativeDebuggerManager.RUN);
                manager().removeAction(NativeDebuggerManager.RUN);
            } else // For load and step
            if ((ddi.getAction() & NativeDebuggerManager.STEP) != 0) {
                dbx.sendCommand(0, 0, "next"); // NOI18N
                ddi.removeAction(NativeDebuggerManager.STEP);
                manager().removeAction(NativeDebuggerManager.STEP);
            }
        // do nothing for load
        }

        if (NativeDebuggerManager.isStandalone()) {
            if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
                System.out.printf("DbxDebuggerImpl.initialAction(): dbxtool\n"); // NOI18N
            }
            // dbxtool -r
            if ((ddi.getAction() & NativeDebuggerManager.RUN) != 0) {
                if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
                    System.out.printf("DbxDebuggerImpl.initialAction(): -r\n"); // NOI18N
                }
                rerun();
                ddi.removeAction(NativeDebuggerManager.RUN);
                manager().removeAction(NativeDebuggerManager.RUN);
            }
        }
    }

    boolean noteProgLoaded(String progname) {

        if (getCaptureState() == CaptureState.INITIAL) {
            // Dbx has succesfully managed to attach to ss_attach
            // loaded ss_attach
            setCaptureState(CaptureState.FINAL);

            // If the exec fails ss_attach will exit with 127, so
            // arrange to quit the useless session.
            // If the exec succeeds dbx wil destroy all handlers on a
            // successful follow-exec so don't have to worry about removing
            // this one.

            // NOTE: dbx doesn't send proc_gone to use because $booting
            // is true in this scenario and dbx skips proc_gone.
            dbx.sendCommand(0, 0, "when exit 127 -hidden { quit; }");// NOI18N

            // DEBUG manager().warning("Attach to dbx");
            dbx.sendCommand(0, 0, "cont");                          // NOI18N
            return false;

        } else if (getCaptureState() == CaptureState.FINAL) {
            // followed exec
            setCaptureState(CaptureState.NONE);
            startUpdates();
        } else {
            setCaptureState(CaptureState.NONE);
        }

        profileBridge().noteProgLoaded(progname);
        if (getNDI().getAction() == NativeDebuggerManager.ATTACH) {
            profileBridge().noteAttached();
        }

        // In case we switched to another program within a session
        // (follow-exec, debug command issued by hand) unbind all
        // existing bpts associated with this engine.
        // See CR 6691195.

        // This code is executed in the eventQ after noteProgLoaded
        // so we aren't touching any new bpts yet.

	bm().removeHandlers();

        rtcModel.setProfile(currentRtcProfile());
        manager().formatStatusText("ReadyToRun", null);	// NOI18N

        manager().addRecentDebugTarget(progname, false);

        // Show console only in tool (see convergence discussions) and CR 7032948
        // in the IDE we show program output
        if (NativeDebuggerManager.isStandalone()) {
            manager().enableConsoleWindow();
        }
	// CR 6998041
	// DebuggerManager.openComponent("callstackView", true); // NOI18N

        if (Log.Bpt.fix6810534) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    initialAction();
                }
            });
        } else {
            initialAction();
        }

        return true;
    }

    void noteProgLoadFailed() {
        setCaptureState(CaptureState.NONE);
    }

    void noteProgUnloaded() {
        profileBridge().noteProgUnloaded();
        // need to do this when unload, because configuration may be changed
        // while debugging, debugtarget list needs to be updated as well
        if (getNDI().loadSucceed()) {
            manager().addRecentDebugTarget(getNDI().getTarget(), true);
        }
        
        if (MemoryWindow.getDefault().isShowing()) {
            MemoryWindow.getDefault().setDebugger(null);
            // CR 6660966
            //currentMemoryWindow = null;
            //MemoryWindow.getDefault().setControlPanelData("main", "80", null); // NOI18N
        }
    }

    void noteProcNewFromPid(int pid) {
        // let external start manager know
        Host host = getHost();
        ExternalStart xstart = ExternalStartManager.getXstart(host);

        if (xstart != null && xstart.attached(pid)) {
            assert getCaptureState() == CaptureState.INITIAL;
            dbx.prop_set("DBX_run_quick", "off"); // NOI18N
        }
    }

    void noteProcGone(String reason, int info) {
        updateFiredEvents(null);

        rtcModel().runEnd();

        boolean skipkill = false;
        if (isRtcEnabled()) {
            skipkill = true;
        }

        // the following ...
        // was: portions of DbxDebugger.processTerminated

        String msg = ""; // NOI18N

        if (reason == null) {
            msg = Catalog.get("ProgCompletedUnknown");
        } else if (reason.equals("kill")) { // NOI18N
            msg = Catalog.get("ProgTerminated");

            // TMP:
            // a process will get killed quite often in normal dbx
            // operation:
            // - user entered 'kill'
            // - 'run' w/o killing first
            // - 'debug' killing existing process and then killing the
            //   booted process
            // Finishing the session on all of these is a bad idea.

            skipkill = true;

        } else if (reason.equals("detach")) { // NOI18N
            msg = Catalog.get("ProgDetached");
            skipkill = true;

        } else if (reason.equals("signal")) { // NOI18N
            // Core file was loaded
            msg = Catalog.format("ProgAborted", info);
            skipkill = true;
        } else if (reason.equals("exit")) { // NOI18N
            // DbxDebuggerInfo ddi = this.getDDI();
            if (!DebuggerOption.FINISH_SESSION.isEnabled(optionLayers()) ||
                    ((ddi.getAction() & NativeDebuggerManager.LOAD) != 0)) {
                skipkill = true;
            }
            msg = Catalog.format("ProgCompletedExit", info);
        } else {
            msg = Catalog.format("ProgCompleted", reason, info);
        }

        setStatusText(msg);

        if (getCaptureState() == CaptureState.FINAL) {
            skipkill = false;
            captureFailed();
        }

        if (!skipkill && NativeDebuggerManager.isStartModel()) {
            postKill();
        }

	autos.clear();
	localUpdater.batchOffForce();	// cause a pull to clear view

        setVisitedLocation(null);
        resetCurrentLine();
    }
    private RtcController rtcController;

    public RtcController rtcController() {
        if (rtcController != null) {
            return rtcController;
        }

        rtcController = new RtcController() {

            public OptionSet optionSet() {
                return optionLayers();
            }

            public boolean isInteractive() {
                return true;
            }

            public void setChecking(boolean access, boolean memuse) {
                DbxDebuggerImpl.this.setChecking(access, memuse);
            }

            public void setAccessChecking(boolean enable) {
                DbxDebuggerImpl.this.setAccessChecking(enable);
            }

            public boolean isAccessCheckingEnabled() {
                // return DbxDebuggerImpl.this.isAccessCheckingEnabled();
                return state().accessOn;
            }

            public void setMemuseChecking(boolean enable) {
                DbxDebuggerImpl.this.setMemuseChecking(enable);
            }

            public boolean isMemuseEnabled() {
                return DbxDebuggerImpl.this.isMemuseEnabled();
            }

            public void setLeaksChecking(boolean enable) {
                DbxDebuggerImpl.this.setLeaksChecking(enable);
            }

            public boolean isLeaksEnabled() {
                return DbxDebuggerImpl.this.isLeaksEnabled();
            }

            public void suppressLastError() {
                DbxDebuggerImpl.this.suppressLastError();
            }

            public void showLeaks(boolean all, boolean detailed) {
                DbxDebuggerImpl.this.showLeaks(all, detailed);
            }

            public void showBlocks(boolean all, boolean detailed) {
                DbxDebuggerImpl.this.showBlocks(all, detailed);
            }

            public void showErrorInEditor(String fileName, int lineNumber) {
                Line line = EditorBridge.getLine(fileName, lineNumber, NativeDebuggerManager.get().currentDebugger());

                if (line != null) {
                    EditorBridge.showInEditor(line);
                    RtcMarker.getDefaultError().setLine(DbxDebuggerImpl.this, line);
                } else {
                    RtcMarker.getDefaultError().clearLine(DbxDebuggerImpl.this);
                    RtcMarker.getDefaultFrame().clearLine(DbxDebuggerImpl.this);
                }
            }

            public void showFrameInEditor(String fileName, int lineNumber) {
                Line line = EditorBridge.getLine(fileName, lineNumber, NativeDebuggerManager.get().currentDebugger());

                if (line != null) {
                    EditorBridge.showInEditor(line);
                    RtcMarker.getDefaultFrame().setLine(DbxDebuggerImpl.this, line);
                } else {
                    RtcMarker.getDefaultError().clearLine(DbxDebuggerImpl.this);
                    RtcMarker.getDefaultFrame().clearLine(DbxDebuggerImpl.this);
                }
            }

            public void skipLoadobjs(Loadobjs loadobjs) {
                String cmd = loadobjs.toString();
                if (!cmd.equals("")) {
                    cmd = "rtc skippatch " + cmd; // NOI18N
                    dbx.sendCommand(0, 0, cmd);
                }
            }
        };
        return rtcController;
    }

    public FileMapper fmap() {
        return FileMapper.getByType(FileMapper.Type.NULL);
    }

    /**
     * We just got a dbx connection, send all kinds of interesting stuff to it.
     *
     * was: latter portion of Dbx.svc_available() as well as
     * DbxDebugger.finishStartDebugger
     */
    void initializeDbx() {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("DbxDebuggerImpl.initializeDbx()\n"); // NOI18N
        }

        assert isConnected() : "initializeDbx() called when dbx wasn't ready";
        
        manager().initialUnsavedFiles(this);

        if (ddi.isCaptured()) {
            setCaptureState(CaptureState.INITIAL);
            setCaptureInfo(ddi.getCaptureInfo());
        } else {
            assert getCaptureState() == CaptureState.NONE;
        }

        // Adjust dbx's context
        // LATER TMP profileBridge.sendRundir();

        if (rtcView == null) {
            rtcView = new RtcView(RtcTopComponent.getDefault(),
                    DebuggerOption.FRONT_ACCESS,
                    DebuggerOption.FRONT_MEMUSE);
            rtcView.setModelController(rtcModel(), rtcController());
            rtcView.componentOpened();
	    RtcTopComponent.getDefault().ioContainer().add(rtcView, null);
        }

        if (!ddi.isClone()) {
            // Tell dbx what to debug
            debug(ddi);
        }

        // Make us be the current session
        // We flip-flop to force the posting of another PROP_CURRENT_SESSION
        manager().setCurrentSession(null);
        manager().setCurrentSession(session.coreSession());
    }

    /**
     * Resue this session with a new debug target
     */
    public void reuse(NativeDebuggerInfo di) {
        // Tell dbx what to debug
        debug((DbxDebuggerInfo) di);
    }

    /**
     * Ask dbx to debug the given program 
     * Common to both new dbx and reuse dbx pathways.
     *
     * Was: Dbx.debug
     */
    private void debug(DbxDebuggerInfo ddi) {

        // we used to get target name as config.getProgram().getExecutableName()
        // but I think depending on ddi is better

        String program = ddi.getTarget();
        long pid = ddi.getPid();
        String corefile = ddi.getCorefile();
        String image = "";	// NOI18N

        profileBridge.setup(ddi);

        rememberDDI(ddi);

        if (corefile != null) {
            // debug corefile
            image = corefile;
            if (program == null || program.equals("")) {
                program = "-";				// NOI18N
            }
        } else if (pid != -1) {
            // attach
            image = Long.toString(pid);
            if (program == null || program.isEmpty()) {
                program = "-";				// NOI18N
            }
            // If we're capturing, we'll be attaching to ss_attach and even
            // if we have a program name it won't match ss_attach and dbx will
            // complain.
            if (getCaptureState() == CaptureState.INITIAL) {
                program = "-";				// NOI18N
            }
        } else {
            // raw dbx session, no need to send 'debug' cmd.
            if (program == null) {
                return;
            }

        // load program
        }

        if (getCaptureState() == CaptureState.INITIAL) {
            stopUpdates();

            /*
            if (Utilities.getOperatingSystem() == Utilities.OS_LINUX)
            dbx.prop_set("DBX_run_quick", "off"); // NOI18N
            else
            dbx.prop_set("DBX_run_quick", "on"); // NOI18N
             */

            Host host = getHost();
            if (host != null) {
                if (host.isLinux()) {
                    dbx.prop_set("DBX_run_quick", "off"); // NOI18N
                } else {
                    dbx.prop_set("DBX_run_quick", "on"); // NOI18N
                }
            } else {
                dbx.prop_set("DBX_run_quick", "on"); // NOI18N
            }
        }

        // Fix for bug #172494 and CRs 4983422, 7105028
        ((DbxDebuggerSettingsBridge)profileBridge()).noteReady();

	if (program != null && !program.isEmpty()) {
	    program = "\"" + program; //NOI18N
	    program += "\""; //NOI18N
	}
        String cmd = "debug " + program + " " + image;	// NOI18N
        dbx.sendCommand(0, 0, cmd);
    }

    /**
     * Only called via glue when dbx goes away.
     * (Or on ACTION_KILL if there is no good dbx connection)
     *
     * was: sessionExited() and if(cleanup) portion of finishDebugger()
     */
    public final void kill() {

        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("DbxDebuggerImpl.kill()\n"); // NOI18N
        }

        captureFailed();

        try {
            super.preKill();

            optionLayers().save();
	    DebuggerOption.STACK_MAX_SIZE.setCurrValue(optionLayers(), "40"); // reset to default // NOI18N

            if (rtcView != null) {
		rtcView.bringDown();
                RtcMarker.getDefaultError().relinquish(this);
                RtcMarker.getDefaultFrame().relinquish(this);
            }
        } catch (Exception x) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, x);
        }

        state().isLoaded = false;
        stateChanged();

        if (getIOPack() != null) {
            getIOPack().close();
        }

        // remember that we're shutting down.
        // we use this flag to defeat the sending of glue messages to an
        // already disconnected dbx.
        // Q: Why not just use dbx.connected()?
        // A: Because connected also transitions during startup and I want
        //    to consider messages being sent before we're connected as
        //    a fatal error.

        postedKillEngine = true;

        // tell debuggercore that we're going away
        engineProvider.getDestructor().killEngine();
        
	// It all ends here
    }

    /**
     * was: Dbx.restart()
     */
    public void rerun() {
        runProgram("run");	// NOI18N
    }

    /**
     * was: Dbx.terminate()
     */
    public void terminate() {
        dbx.sendCommandIntNoresume(0, 0, "kill");	// NOI18N
    }

    /**
     * was: Dbx.detach()
     */
    public void detach() {
        dbx.sendCommandIntNoresume(0, 0, "detach");	// NOI18N
    }

    public final void stepOverInst() {
        dbx.sendCommand(0, 0, "nexti");	// NOI18N
    }

    public final void stepOutInst() {
        dbx.sendCommand(0, 0, "stepi up");	// NOI18N
    }

    public final void stepInst() {
        dbx.sendCommand(0, 0, "stepi");	// NOI18N
    }

    public final void stepInto() {
        dbx.sendCommand(0, 0, "step");	// NOI18N
    }

    public final void stepOver() {
        dbx.sendCommand(0, 0, "next");	// NOI18N
    }

    public final void stepOut() {
        dbx.sendCommand(0, 0, "step up");	// NOI18N
    }

    public final void fix() {
        // LATER:
        // older dbxGUI provided for cookies and a filename argument,
        // although not sure from whence it was used.
	Host host = getHost();
	if (host != null && !host.isSolaris())
	    NativeDebuggerManager.warning(Catalog.get("FIX_AD"));
	else
	    dbx.sendCommand(0, 0, "fix -a");	// NOI18N
    }

    /*
     * was: DbxDebugger.goToCalledMethod() & Dbx.downStack()
     */
    public void makeCalleeCurrent() {
        dbx.sendCommand(0, 0, "down");				// NOI18N
    }

    /**
     * was: DbxDebugger.goToCallingMethod() & Dbx.upStack()
     */
    public void makeCallerCurrent() {
        dbx.sendCommand(0, 0, "up");				// NOI18N
    }

    /**
     * was: DbxDebugger.popTopmostFrame() & Dbx.pop()
     */
    public void popTopmostCall() {
        dbx.sendCommand(0, 0, "pop");				// NOI18N
    }

    /**
     * was: Dbx.popFromCall()
     */
    public void popLastDebuggerCall() {
        dbx.sendCommand(0, 0, "pop -c");			// NOI18N
    }

    public void popToHere(Frame frame) {
        String cmd = "pop -f " + frame.getNumber();		// NOI18N
        dbx.sendCommand(0, 0, cmd);	// NOI18N
    }

    /**
     * was: Dbx.pop2cur()
     */
    public void popToCurrentFrame() {
        dbx.sendCommand(0, 0, "pop -f $(builtin frame)");	// NOI18N
    }

    public final void stepTo(String function) {
        if (function != null) {
            String cmd =  "stop in " + function + " -temp -hidden";	// NOI18N
            runProgram(cmd, "cont");	// NOI18N
        } else {
            dbx.sendCommand(0, 0, "step to");	// NOI18N
        }
    }

    public void contAt(String src, int line) {
	src = localToRemote("contAt", src); // NOI18N
        String cmd = "cont at " + src + ":" + line;	// NOI18N
        runProgram(cmd);				// NOI18N
    }

    public void runToCursor(String src, int line) {
	src = localToRemote("runToCursor", src); // NOI18N
        String cmd = "stop at " + src + ":" + line + " -temp -hidden";	// NOI18N
        runProgram(cmd, "cont");				// NOI18N
    }

    public void runToCursorInst(String addr) {
        String cmd = "stopi at " + addr + " -temp -hidden";	// NOI18N
        runProgram(cmd, "cont");				// NOI18N
    }

    /**
     * Pause. This should stop executing user code
     *
     * was: DbxDebugger.pause()
     */
    public void pause() {
        dbx.pause();
    }

    public void interrupt() {
        dbx.interrupt();
    }

    /**
     * Ask dbx to continue running this program.
     * Note - it may fail (for example, if the program has terminated,
     * and cannot be restarted because you originally attached to a
     * process). We handle this through the asynchronous error() call.
     *
     * was: DbxDebugger.go() & Dbx.go()
     */
    public final void go() {
        runProgram("cont");		// NOI18N
    }

    /**
     * was: Dbx.proc_go()
     */
    public final void resumed() {

        // SHOULD do all the following on a timer?

        // SHOULD we clear the bpt fired bits?
        // workshop didn't do it and neither did the NB-based variations
        clearFiredEvents();

        deleteMarkLocations();
        deliverSignal = -1;
    }

    private void runProgram(String cmd) {
        runProgram(null, cmd);
    }
    
    /**
     * Common resumption action used for stepping, cont, run etc
     * Runs:
     * cmd1 ; cmd2
     * meaning that cmd2 will not run if cmd1 fails
     */
    private void runProgram(String cmd1, String cmd2) {
        lastRunCmd = cmd2;

        // We SHOULD only need to do this on a proc_go note from dbx.
        // The following seems redundant with resumed()
        deleteMarkLocations();

        if (!state().isRunning) {
            if (deliverSignal != -1) {
                if (cmd2.startsWith("cont") || // NOI18N
                        cmd2.startsWith("step") || // NOI18N
                        cmd2.equals("next")) {		// NOI18N
                    cmd2 = cmd2 + " -sig " + deliverSignal; // NOI18N
                    deliverSignal = -1;
                }
            }
            
            String cmd = cmd2;
            if (cmd1 != null) {
                cmd = cmd1 + "; " + cmd2; //NOI18N
            }
            dbx.sendCommand(0, 0, cmd);
        }


    // LATER. The following was all commented out in prior releases

    // Start timer. The debugger state won't commit to being
    // run yet - only once the timer expires will we realize the
    // program is fully running. This is done to avoid lots of GUI
    // flashing for short, interactive stepping.
    //      debugger.disableStoppedActions(this);
    }

    /**
     * Baby steps ...
     * This is factored from send*Updates() with the hope of making the
     * concept more general later.
     *
     * A "pileup" is when we send messages to an unreceptive dbx.
     * dbx is usually not receptive when it has resumed a process
     * (state.isRunning). In such cases messages will pile up in the 
     * socket between us and dbx. There are two consequences of this.
     * 1) User sees no immediate effect. Hence the status update by this
     *    routine.
     * 2) If the pileup gets big enough the socket will fill and we'll 
     *    lock up.
     *
     * In general when dbx has resumed()/proc_go we disable all actions which
     * we can (see ActionEnabler). But not everything is disableable so
     * some stuff might sneak through; Even some disable actions might sneak
     * through in a ship-crossing-in-the-night scenario.
     * In some cases we ignore the action (see Dbx.sendCommand()).
     * In other cases we're banking on this slippage to be small enough
     * that the pileup will not fill the socket.
     *
     * But there are still some problem areas ... one specific example:
     * If you start with many breakpoints (~40) IDE will send that many
     * commands to dbx and dbx will echo them all back. Sockets in both
     * directions will fill up.
     *
     * Solutions to "pileup":
     *
     * - Be more diligent about contrlling action enabledness.
     *
     * - Dbx.runCommandInt() which interrupts dbx to make it receptive, sends
     *   a command and quitely resumes dbx. We started using it as far back as 
     *   workshop for the ability to create bpts at any time.
     *   I don't like relying on interrupts too much.
     *
     * - One genral solution is to explicitly queue stuff instead of depending
     *   on the socket buffers for queueing. GdbDebugger does better in
     *   that respect.
     *
     * - Eliminate redundancies. For example, if the user madly enables and
     *   disables the stack view, they will fill the pipe. A cleverer scheme
     *   would keep track of the (requested) state.
     */
    private void notePileup(boolean enable, String msg) {

        if (enable && !dbx.isReceptive()) {

            // Status area message is not neccessarily in your face
            // Instead SHOULD put some info into the actual views.
            // We had an rfe for the ability to put a message n NB 3.5 but I
            // think it went away in debuggercore.

            setStatusText(msg);
        }
    }

    /**
     * was: Dbx.runFailed
     * Called from DebuggerManager error processing
     */
    public void runFailed() {
        setStatusText(Catalog.get("RunFailed"));
        stateSetRunning(false);
        stateChanged();
    }

    /** Execute a dbx "assign" command */
    public void execute(String cmd) {
        dbx.sendCommand(0, 0, cmd);
    }


    /*
     * General variable stuff
     */
    /**
     * Convert expr_heval results of opened ptr to DbxVariable
     * and call treeChange to cause a pull on either local view or watch view
     */
    public final void setExpandedNodes(boolean locals, GPDbxLocalItem items[]) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.traffic) {
            System.out.printf("setExpandedNodes(%s)\n", // NOI18N
                    locals ? "locals" : "watches"); // NOI18N
        }
        if (items == null) {
            return;
        }

        // disable firing treeChanged or treeNodeChanged
        if (locals) {
            localUpdater.batchOn();
        } else {
            watchUpdater().batchOn();
        }

        for (GPDbxLocalItem item : items) {
            DbxVariable v;
            if (locals) {
                v = searchLocalNodes(item.qualified_lhs);
            } else {
                v = searchWatchNodes(item.qualified_lhs);
            }
            if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.traffic) {
                System.out.printf("'%s' -> %s\n", // NOI18N
                        item.qualified_lhs, v == null ? "miss" : "hit"); // NOI18N
            }
            if (v != null) {
                // The fact that we set the children here creates a
                // hard requirement that update information be applied
                // in sorted order, parents before children.
                //
                // In the past we got around this problem by calling
                // removeAllDescendantFromOpenList() when a Variable was
                // collapsed which ensured that no sequence of user
                // collpase/expand actions would created an unsorted list.
                // However, IZ 97706 makes that unworkable.

                v.setExpanded(true);
		if (item.flags == 0) {
		    v.setChildren(item.rhs_vdl, null, !locals);
		} else {
		    DbxVariable var = new DbxVariable(this, localUpdater, null,
			   item.plain_lhs,
			   item.plain_lhs,
			   null,
			   null,
			   variableErrorCode(item.flags),
			   !locals);
		    v.setChildren(item.rhs_vdl, var, !locals);
		}
            }
        }

        // enable firing treeChanged or treeNodeChanged
        if (locals) {
            localUpdater.batchOff();	// causes a pull for local nodes
        } else {
            watchUpdater().batchOff();	// causes a pull for watch nodes
        }
    }

    public FormatOption[] getEvalFormats() {
        return DbxEvalFormat.values();
    }

    public void exprEval(FormatOption format, String expr) {
        // CR 6574620
        if (currentEvaluationWindow == null) {
            currentEvaluationWindow = EvaluationWindow.getDefault();
        }
        currentEvaluationWindow.open();
        currentEvaluationWindow.requestActive();
        currentEvaluationWindow.componentShowing();
        dbx.expr_eval("EvaluationWindow".hashCode(), format.getOption() + ' ' + expr); // NOI18N
    }

    public void evalResult(int rt, String value) {
        if (rt == RT_EVAL_REGISTER) {
            EvalAnnotation.postResult(value.trim());
            return;
        }
        // CR 6770439, we can not guarantee there is currentEvaluationWindow
        // when expression result come up, this checking is a safety net
        if (currentEvaluationWindow == null) {
            currentEvaluationWindow = EvaluationWindow.getDefault();
        }
        currentEvaluationWindow.open();
        currentEvaluationWindow.requestActive();
        currentEvaluationWindow.componentShowing();
        currentEvaluationWindow.evalResult(value);
    }

    void chaseWatchPointer(DbxVariable var, String expr) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.traffic) {
            System.out.printf("chaseWatchPointer: %s\n", expr); // NOI18N
        }

        if (expr == null) {
            return;
        }

        // send reguest to dbx only when not in batch mode
        // NOTE: dbx tests the routing token!
        if (!watchUpdater().batchMode()) {
	    // CR 6879383
	    if (isInheritedMembers() && isDynamicType())
		dbx.expr_heval(RT_CHASE_WATCH, "-r -d " + expr); // NOI18N
	    else if (isInheritedMembers())
		dbx.expr_heval(RT_CHASE_WATCH, "-r " + expr); // NOI18N
	    else if (isDynamicType())
		dbx.expr_heval(RT_CHASE_WATCH, "-d " + expr); // NOI18N
	    else
		dbx.expr_heval(RT_CHASE_WATCH, expr);
        }
    }

    void chaseLocalPointer(DbxVariable var, String expr) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.traffic) {
            System.out.printf("chaseLocalPointer: %s\n", expr); // NOI18N
        }

        if (expr == null) {
            return;
        }

        assert SwingUtilities.isEventDispatchThread();

        // send reguest to dbx only when not in batch mode
        // NOTE: dbx tests the routing token!
        if (!localUpdater.batchMode()) {
	    int rt = isShowAutos()? RT_CHASE_AUTO: RT_CHASE_LOCAL;
	    // CR 6879383
	    if (isInheritedMembers() && isDynamicType())
		dbx.expr_heval(rt, "-r -d " + expr);		// NOI18N
	    else if (isInheritedMembers())
		dbx.expr_heval(rt, "-r " + expr);		// NOI18N
	    else if (isDynamicType())
		dbx.expr_heval(rt, "-d " + expr);		// NOI18N
	    else
		dbx.expr_heval(rt, expr);
        }
    }

    /**
     * We had asked dbx to evaluate an expression for us. Now it has
     * arrived back.
     *
     * was: Dbx.expr_heval(DbxVariable, String)
     */
    public void setChasedPointer(int rt, GPDbxHEvalResult result) {
        if (rt == RT_CHASE_WATCH) {
            watchUpdater().batchOn();
            setWatchChasedPointer(rt, result);
            watchUpdater().batchOff();
        } else if (rt == RT_CHASE_LOCAL || rt == RT_CHASE_AUTO) {
            localUpdater.batchOn();
            setLocalChasedPointer(rt, result);
            localUpdater.batchOff();
        } else if (rt == RT_EVAL_AUTO || rt == RT_EVAL_AUTO_LAST) {
	    DbxVariable v;
	    if (result.flags == 0) {
		v = new DbxVariable(this, localUpdater, null,
			result.plain_lhs,
			result.plain_lhs,
			null,
			null,
			result.rhs,
			rt == RT_CHASE_WATCH);
		// this will generate a open node list for this var
		v.setRHS(result.rhs, result.rhs_vdl, rt == RT_CHASE_WATCH);
	    } else {
		v = new DbxVariable(this, localUpdater, null,
			result.plain_lhs,
			result.plain_lhs,
			result.static_type,
			result.static_type,
			variableErrorCode(result.flags),
			rt == RT_CHASE_WATCH);
	    }

	    autos.add(v);
	    if (rt == RT_EVAL_AUTO_LAST) {
		localUpdater.batchOffForce();
            }
        } else if (rt == RT_EVAL_TOOLTIP) {
            final DbxVariable v;
            if (result.flags == 0) {
                v = new DbxVariable(this, localUpdater, null,
                        result.plain_lhs,
                        result.plain_lhs,
                        null,
                        null,
                        result.rhs,
                        rt == RT_CHASE_WATCH);
                // this will generate a open node list for this var
                v.setRHS(result.rhs, result.rhs_vdl, rt == RT_CHASE_WATCH);
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ToolTipView.getDefault().setRootElement(new VariableNode(v, new DbxVariableNodeChildren(v))).showTooltip();
                        VariableNode.propertyChanged(v);
                    }

                });
            }
        }
    }
    
    /*
     * debugging view stuff 
     */
    private boolean get_debugging = false; // indicates Debugging View open/close
    
    @Override
    public void registerDebuggingViewModel(ModelListener model) {
        super.registerDebuggingViewModel(model);
        
        get_debugging = model != null;
        
        if (get_frames || get_debugging) {
	    stackEnabler.setRegistered(true);
        } else {
            if (dbx != null && dbx.connected()) {   // Debugging View unregister its model during the shutdown
                stackEnabler.setRegistered(false);
            }
        }
        
        if (get_threads || get_debugging) {
            threadEnabler.setRegistered(true);
        } else {
            if (dbx != null && dbx.connected()) {   // Debugging View unregister its model during the shutdown
                threadEnabler.setRegistered(false);
            }
        }
    }

    public Thread[] getThreadsWithStacks() {
        Thread[] threadsWithStack = getThreads();
        for (Thread thread : threadsWithStack) {
            if (thread.isCurrent()) {
                Frame[] stack = getStack();
                for (Frame frame : stack) {
                    ((DbxFrame) frame).setThread(thread);
                }
                thread.setStack(stack);
            } else{
                thread.setStack(null);
            }
        }
        return threadsWithStack;
    }

    public void resumeThread(final Thread thread) {
        runProgram("cont " + ((DbxThread) thread).getName()); // NOI18N
    }
    
    private static final class DbxVariableNodeChildren extends VariableNodeChildren {
        public DbxVariableNodeChildren(Variable v) {
            super(v);
            setKeys(v.getChildren());
        }

        @Override
        protected Node[] createNodes(Variable key) {
            return new Node[]{new VariableNode(key, new DbxVariableNodeChildren(key))};
        }
    }

    /**
     * Dbx's answer to chaseWatchPointer().
     */
    private void setWatchChasedPointer(int rt, GPDbxHEvalResult result) {
        DbxVariable var = searchWatchNodes(result.qualified_lhs);
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.traffic) {
            System.out.printf("setWatchChasedPointer: %s -> %s\n", // NOI18N
                    result.qualified_lhs, var == null ? "miss" : "hit"); // NOI18N
        }
        if (var != null) {
	    if (result.flags == 0) {
		var.setChildren(result.rhs_vdl, null, rt == RT_CHASE_WATCH);
	    } else {
	        DbxVariable v = new DbxVariable(this, localUpdater, null,
		       result.plain_lhs,
		       result.plain_lhs,
		       result.static_type,
		       result.static_type,
		       variableErrorCode(result.flags),
		       rt == RT_CHASE_WATCH);
	        Variable vars[] = new Variable[1];
	        vars[0] = v;
	        var.setChildren(vars, true);
	    }
        }
    }

    /**
     * Dbx's answer to chaseLocalPointer().
     */
    private void setLocalChasedPointer(int rt, GPDbxHEvalResult result) {
        DbxVariable var;
	if (isShowAutos())
	    var = variableByKey(getAutos(), result.qualified_lhs);
	else
	    var = searchLocalNodes(result.qualified_lhs);
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.traffic) {
            System.out.printf("setLocalChasedPointer: %s -> %s\n", // NOI18N
                    result.qualified_lhs, var == null ? "miss" : "hit"); // NOI18N
        }
        if (var != null) {
	    if (result.flags == 0) {
		var.setChildren(result.rhs_vdl, null, false);
	    } else {
	        DbxVariable v = new DbxVariable(this, localUpdater, null,
		       result.plain_lhs,
		       result.plain_lhs,
		       result.static_type,
		       result.static_type,
		       variableErrorCode(result.flags),
		       false);
	        Variable vars[] = new Variable[1];
	        vars[0] = v;
	        var.setChildren(vars, true);
	    }
        }
    }

    /**
     * Tell dbx about node getting expanded or unexpanded..
     */
    void updateOpenNodes(DbxVariable v, String expr,
            boolean expanded, boolean isLocal) {

	// dbx sends updates of expanded nodes before we get a chance to
	// requestAutos() so remembering chased nodes for auto's won't
	// work very well. So we forbid it.
	// There's still a scenario where the forbidding fails and that is
	// if the ndoe was opened while in locals view, and then we switch to
	// auto view. It will start arriving but too early.
	if (isShowAutos())
	    return;

        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.traffic) {
            System.out.printf("updateOpenNodes: %s %s\n", // NOI18N
                    expanded ? "+" : "-", expr); // NOI18N
        }
        assert v != null;

        if (isLocal) {
            dbx.node_expanded(true, expanded, v.isPtr(), expr);
        } else {
            dbx.node_expanded(false, expanded, v.isPtr(), expr);
        }
    }

    public void setDbxOption(String option, boolean b) {
        if (b) {
            dbx.prop_set(option, "on"); // NOI18N
        } else {
            dbx.prop_set(option, "off"); // NOI18N
        }
    }


    //
    // Utilities for searching Varibales used by locals and watches
    //
    private DbxVariable variableByKey(Variable children[], String key) {
        if (children == null) {
            return null;
        }
        for (Variable child : children) {
            if (child == null) {
                continue;
            }
            DbxVariable candidate = variableByKey((DbxVariable) child, key);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private DbxVariable variableByKey(DbxVariable var, String key) {
        if (var.matchesNodeKey(key)) {
            return var;
        }
        return variableByKey(var.getChildren(), key);
    }

    /*
     * Watches stuff
     */

    // interface NativeDebuggerImpl
    @Override
    protected void restoreWatch(NativeWatch template) {
        // LATER super.restoreWatch(template);

        // We still need this so manager().error() routes bpt errors
        // correctly.
        int rt = template.getRoutingToken();
        noteRestoredWatch(template, rt);
        dbx.sendCommandInt(rt, 0, watchCommand(template));

	// We'll come back either via newWatch() or watchError().
    }

    // interface NativeDebuggerImpl
    @Override
    public void replaceWatch(NativeWatch original, String replacedwith) {
	String capab = state().capabilities;
	int i = capab.indexOf("watchreplace"); // NOI18N
	if (i == -1) { // old dbx that does not support watch -replace
	    // remove the original
	    original.postDelete(false);
	    // create a new one base on replacewith
	    manager().createWatch(replacedwith.trim());
	} else { // new dbx78 that support watch -replace
	    int rt = original.getRoutingToken();
	    original.replacedWith(replacedwith);
	    dbx.sendCommandInt(rt, 0, watchCommand(original));
	}
    }

    /**
     * Return command string to be sent to debugger.
     * SHOULD really have a WatchExpert analogous to HandlerExpert.
     */

    private String watchCommand(NativeWatch watch) {
	String watch_cmd;
	if (watch.watch() != null) {
            WatchVariable wv = watch.findByDebugger(this);
	    watch_cmd = "watch";				// NOI18N
	    if (watch.isReplaced())
		watch_cmd += " -replace " + wv.getKey() + " " + watch.getReplaced();	// NOI18N
	    else
		watch_cmd += " " + watch.getExpression();		// NOI18N
	} else {
	    if (watch.isRestricted()) {
		watch_cmd = "display";				// NOI18N
		if (watch.isReplaced())
		    watch_cmd += " -replace " + watch.getReplaced();	// NOI18N
		else {
		    if (watch.getQualifiedExpression() != null)
			watch_cmd += " " + watch.getQualifiedExpression();// NOI18N
		    else
			watch_cmd += " " + watch.getExpression();		// NOI18N
		}
	    } else {
		watch_cmd = "watch";				// NOI18N
		if (watch.isReplaced())
		    watch_cmd += " -replace " + watch.getReplaced();	// NOI18N
		else
		    watch_cmd += " " + watch.getExpression();		// NOI18N
	    }
	}
	return watch_cmd;
    }
    
    public void newWatch(int rt, GPDbxDisplaySpec spec) {
        DbxWatch dbxWatch = null;
        NativeWatch nativeWatch = null;
        WatchJob wj = getWatchJob(rt);
        switch (wj.kind()) {
            case RESTORE:
                dbxWatch = new DbxWatch(this, watchUpdater(),
                        spec.id, spec.restricted,
                        spec.plain_lhs, spec.static_type);
                nativeWatch = wj.template();
                nativeWatch.setSubWatchFor(dbxWatch, this);
                if (nativeWatch.watch() == null) {
                    nativeWatch.setQualifiedExpression(spec.reevaluable_lhs);
                }
                watches.add(dbxWatch);
                break;
            case NEW:
            case SPONTANEOUS:
                dbxWatch = new DbxWatch(this, watchUpdater(),
                        spec.id, spec.restricted,
                        spec.plain_lhs, spec.static_type);
                if (wj.kind() == WatchJob.Kind.SPONTANEOUS) {
                    // spontaneous creation from dbx cmdline
                    // need to create both a Watch and a sub-watch

		    Watch coreWatch = manager().createWatch(null);
		    nativeWatch = new NativeWatch(coreWatch);
		    // mimic what we do in watchAdded(). Can't do this
		    // there because we can't create a NativeWatch before
		    // we have a coreWatch.
		    manager().watchMap(nativeWatch);

                    nativeWatch.setExpression(spec.plain_lhs);
                    if (nativeWatch.watch() == null) {
                        nativeWatch.setQualifiedExpression(spec.reevaluable_lhs);
                        nativeWatch.setScope(spec.scope);
                        nativeWatch.setRestricted(spec.restricted);
                    }
                    watchBag().add(nativeWatch);
                } else {
		    // Always shows up as RESTORE because we spread from
		    // the outset.
		    assert false;
                }
                nativeWatch.setSubWatchFor(dbxWatch, this);
                watches.add(dbxWatch);

                manager().bringDownDialog();

                manager().spreadWatchCreation(this, nativeWatch);

                break;
        }
        
        watchEnabler.update();

    /* CR 6520370
    if (is_a_pointer != 0)
    dbxWatch.setPtr(true);
    else
    dbxWatch.setPtr(false);
     */
    }

    private void newErrorWatch(NativeWatch template,
            String msg, boolean restored) {

        NativeWatch nativeWatch = template;

        DbxWatch dbxWatch = new DbxWatch(this, watchUpdater(),
                0,
                nativeWatch.isRestricted(),
                nativeWatch.getExpression(), "<unknown type>"); // NOI18N
        dbxWatch.setAsText(msg);

        if (!restored) {
            watchBag().add(nativeWatch);
        }
        nativeWatch.setSubWatchFor(dbxWatch, this);
        watches.add(dbxWatch);
        dbxWatch.update();
        watchEnabler.update();
    }

    public void dupWatch(int rt, int id) {
        WatchJob wj = getWatchJob(rt);
        NativeWatch nativeWatch = wj.template();

        if (wj.kind() == WatchJob.Kind.SPONTANEOUS) {
            Watch coreWatch = manager().createWatch(null);
            nativeWatch = new NativeWatch(coreWatch);
            // See newWatch() case SPONTANEOUS.
            manager().watchMap(nativeWatch);

            WatchVariable original = watches.byKey(id);
            nativeWatch.setExpression(original.getNativeWatch().getExpression());
        }

        DbxWatch dbxWatch = new DbxWatch(this, watchUpdater(),
                0,
                nativeWatch.isRestricted(),
                nativeWatch.getExpression(), "<unknown type>", id); // NOI18N

        // set "value"
        final String msg = String.format("Duplicate of %d", id); // NOI18N
        dbxWatch.setAsText(msg);

        if (wj.kind() == WatchJob.Kind.SPONTANEOUS) {
            watchBag().add(nativeWatch);
        }

        nativeWatch.setSubWatchFor(dbxWatch, this);
        watches.add(dbxWatch);
        dbxWatch.update();
        watchEnabler.update();
    }

    /**
     * Deal with watch errors.
     * We either create an "error" watch and silence the error by
     * returning true, or return false and let the user see the error.
     *
     * We get errors in the following situations:
     * - During restoration.
     *   'restoredWatches' has a list of watches being restored.
     *   we convert them to error watches.
     * - During watch creation.
     *   'newBreakpoint' is set.
     *   We make this one be an error as well because the watch might have been 
     *   created in anticipation of a new session which has nothing to do
     *   with us.
    LATER:
     * - During watch editing.
     *   If the bpt is broken 'repairedBreakpoint' is set.
     *   For now we present an error to the user for these.
     */
    public boolean watchError(int rt, Error error) {

        boolean beSilent;
        boolean convert;
        boolean restored;

        WatchJob wj = getWatchJob(rt);
        NativeWatch template = wj.template();

        switch (wj.kind()) {
            default:
            case SPONTANEOUS:
                // should never happen
		assert false : "spontaneous breakpointError";
                beSilent = false;
                restored = false;
                convert = false;
		break;

            /* LATER
            case CHANGE:
            case REPAIR:
            if (bj.isPrimaryChange()) {
            // validation of initial change failed; complain
            beSilent = false;
            restored = false;
            convert = false;
            } else {
            // validation of spread of change failed;
            // silently convert to broken
            beSilent = true;
            restored = false;
            convert = true;
            }
            break;
             */

            case RESTORE:
                // restoration failed;
                beSilent = true;
                restored = true;
                convert = true;
                break;

            case NEW:
                // new breakpoint,
                // convert to broken and complain (really?)
                restored = false;
                beSilent = false;
                convert = false;
                break;
        }


        if (convert) {
            newErrorWatch(template, error.first(), restored);
        }

        return beSilent;
    }

    /**
     * New values for watches have arrived. Incorporate them into the 
     * existing watches.
     *
     * was: Dbx.display_update
     */
    public void updateWatches(GPDbxDisplayItem items[]) {
        watchUpdater().batchOn();
        for (int wx = 0; wx < items.length; wx++) {
            GPDbxDisplayItem i = items[wx];
            List<DbxWatch> watchesToUpdate = new ArrayList<DbxWatch>();
//            WatchVariable wv = watches.byKey(i.id);
            //find all dups that has pin and set values for them and for original one
            //see bz#270230
            for (Iterator<WatchVariable> iterator = watches.iterator();iterator.hasNext();) {
                DbxWatch next = (DbxWatch)iterator.next();
                if ((next.hasKey() && next.getId() == i.id) || 
                        (!next.hasKey() && next.getOriginalId() == i.id && next.getNativeWatch().watch().getPin() != null)) {
                    watchesToUpdate.add(next);
                }
            }
            for (DbxWatch w : watchesToUpdate) {
                // SHOULD do the following in something analogous to
                // HandlerExpert.newHandler ...

                //DbxWatch w = (DbxWatch) wv;
                if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.traffic) {
                    System.out.printf("updateWatches() item %d -> %s\n", // NOI18N
                            i.id, w == null ? "miss" : w.getVariableName()); // NOI18N
                }
                if (w == null) {
                    continue;
                }

                if (i.rhs_vdl != null && i.flags == 0) {
                    w.setRHS(i.rhs, i.rhs_vdl, true); // should give accurate Leaf setting
                } else {
                    w.setChildren(null, null, true);
                    w.setLeaf(true);
                    w.setAsText(variableErrorCode(items[wx].flags));
                }

                if (i.plain_lhs != null) {
                    w.setVariableName(i.plain_lhs); // could be watch replace
                    // also update the one in debuggercore
                    NativeWatch nativeWatch = w.getNativeWatch();
                    nativeWatch.setExpression(i.plain_lhs);
                    nativeWatch.replacedWith(null);
                }

                final NativeWatch nativeWatch = w.getNativeWatch();
                if (nativeWatch != null && nativeWatch.watch().getPin() != null) {
                    NativeDebuggerManager.get().firePinnedWatchChange(this, nativeWatch.watch());
                }
            }
        }

        watchUpdater().batchOffForce();
    }
    /* OLD
    private static final String watch_cmd = "display";
    // LATER private static final String watch_cmd = "watch";
    private static final String unwatch_cmd = "un" + watch_cmd;

    public void postDynamicWatch(Variable watch) {
        String exp = watch.getVariableName();
        dbx.sendCommandInt(0, 0, unwatch_cmd + " " + ((DbxWatch) watch).getId()); // NOI18N
        dbx.sendCommand(0, 0, watch_cmd + " -d " + exp); // NOI18N
    }

    public void postInheritedWatch(Variable watch) {
        String exp = watch.getVariableName();
        dbx.sendCommandInt(0, 0, unwatch_cmd + " " + ((DbxWatch) watch).getId()); // NOI18N
        dbx.sendCommand(0, 0, watch_cmd + " -r " + exp); // NOI18N
    }
     */

    public void postDeleteAllWatches() {
        dbx.sendCommandInt(0, 0, "undisplay 0");	// NOI18N
        dbx.sendCommandInt(0, 0, "unwatch 0");		// NOI18N
    }

    public void postDeleteWatch(WatchVariable variable, boolean spreading) {
        if (!(variable instanceof DbxWatch)) {
            return;
        }
        DbxWatch watch = (DbxWatch) variable;

        if (watch.getId() == 0) {
            deleteWatch(watch, spreading);
        } else {
            noteDeletedWatch(watch, spreading);
            String unwatch_cmd;
            if (watch.isRestricted()) {
                unwatch_cmd = "undisplay";	// NOI18N
            } else {
                unwatch_cmd = "unwatch";	// NOI18N
            }
            unwatch_cmd += " " + watch.getId();	// NOI18N
            dbx.sendCommandInt(watch.getRoutingToken(), 0, unwatch_cmd);
	    // Will come back via deleteWatchById and call deleteWatch
        }
    }

    private DbxVariable searchWatchNodes(String lhs) {
        // Go through this rigamarole because can't directly
        // cast WatchVariable[] to DbxWatch[]

        WatchVariable[] watchesCopy = getWatches();
        Variable[] variables = new Variable[watchesCopy.length];
        for (int wx = 0; wx < watchesCopy.length; wx++) {
            variables[wx] = (Variable) watchesCopy[wx];
        }
        return variableByKey(variables, lhs);
    }

    public void contAtInst(String addr) {
        throw new UnsupportedOperationException("Not supported yet.");// NOI18N
    }

    class WatchesEnableLatch extends EnableLatch {
	protected void setEnabled(boolean enable) {
	    dbx.display_notify(0, enable || watchBag().hasPinnedWatches());
	    notePileup(enable, Catalog.get("NoDisplayWhileRunning"));// NOI18N
	}
    }

    // control notification to dbx engine to send watch variable data
    WatchesEnableLatch watchEnabler = new WatchesEnableLatch();

    // interface NativeDebugger
    @Override
    public void registerWatchModel(WatchModel model) {
	watchUpdater().addListener(model);

	if (postedKill)
	    return;

	if (model == null)
	    watchEnabler.setRegistered(false);
	else
	    watchEnabler.setRegistered(true);
    }

    /*
     * Locals stuff
     */
    private DbxVariable[] locals = new DbxVariable[0];

    private DbxVariable searchLocalNodes(String lhs) {
        return variableByKey(locals, lhs);
    }

    /**
     * Convert flags in a GPDbxDisplayItem or GPDbxLocalItem to a string.
     * Return null if no errors.
     */
    private String variableErrorCode(int flags) {
        if (flags == 0) {
            return null;
        } else if ((flags & GPDbxDisplayItem.OOSCOPE) != 0) {
            return "<OUT_OF_SCOPE>"; // NOI18N
        } else if ((flags & GPDbxDisplayItem.NOTALLOC) != 0) {
            return "<NOT_ALLOC>"; // NOI18N
        } else if ((flags & GPDbxDisplayItem.TOOBIG) != 0) {
            return "<TOO_BIG - Use \"Set Max Object Size\" to see expression value>"; // NOI18N
        } else if ((flags & GPDbxDisplayItem.NOT_AVAILABLE) != 0) {
            return "<NOT_AVAILABLE>"; // NOI18N
        } else {
            // Also if (flags & GPDbxDisplayItem.ERROR) != 0
            // We don't test for ERROR explicitly becaus eit might be set
            // in conjunction with the above
            return "<ERROR>"; // NOI18N
        }
    }

    /**
     * New set of locals arriving from dbx
     */
    public final void setLocals(GPDbxLocalItem[] gp_locals) {

        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.traffic) {
            System.out.printf("setLocals()\n"); // NOI18N
        }

	if (isShowAutos())
	    return;

        localUpdater.batchOn();

        if (gp_locals == null) {
            locals = new DbxVariable[0];

        } else {
            List<DbxVariable> locals_vector = new ArrayList<DbxVariable>();
            for (int lx = 0; lx < gp_locals.length; lx++) {
                DbxVariable v = null;
                if (gp_locals[lx].plain_lhs == null) {
                    // CR 6536351
                    // CR 6548972, don't create local node if variable is not allocated
                } else if (gp_locals[lx].flags == 0) {
                    v = new DbxVariable(this, localUpdater, null,
                            gp_locals[lx].plain_lhs,
                            gp_locals[lx].plain_lhs,
                            null,
                            null,
                            gp_locals[lx].rhs,
			    false);
                    // this will generate a open node list for this var
                    v.setRHS(gp_locals[lx].rhs, gp_locals[lx].rhs_vdl, false);
                } else {
                    v = new DbxVariable(this, localUpdater, null,
                            gp_locals[lx].plain_lhs,
                            gp_locals[lx].plain_lhs,
                            null,
                            null,
                            variableErrorCode(gp_locals[lx].flags),
			    false);
                }
                if (v != null) {
                    locals_vector.add(v);
                }
            }
            locals = new DbxVariable[locals_vector.size()];
            locals = locals_vector.toArray(locals);
        }

	localUpdater.batchOffForce();
    }

    public int getLocalsCount() {
        if (locals == null) {
            return 0;
        } else {
            return locals.length;
        }
    }

    public Variable[] getLocals() {
        return locals;
    }

    @Override
    public void setShowAutos(boolean showAutos) {
	super.setShowAutos(showAutos);
	if (dbx != null && dbx.connected()) {
	    if (showAutos)
		requestAutos();
	    else
		localEnabler.update();
	}
    }

    /**
     * - Ask the Autos service for a list of relevant variables
     * - get dbx to evaluate them by sending a series of expr_heval()s.
     *   The last call has a sentinel routing token.
     * - we'll get callbacks in setChasedPointer() which will assemble
     *   variables in 'autos'.
     *   When it detects the reply with the sentinel routing token it will
     *   call batchOffForce() to cause a pull of LocalModel.
     */
    @Override
    public Set<String> requestAutos() {
        Set<String> autoNames = super.requestAutos();
        
        if (autoNames != null) {
            if (autoNames.isEmpty()) {
                localUpdater.batchOffForce();	// cause a pull to clear view
                return autoNames;
            }

	// Corresponding batchOff() will happen in setChasedPointer() when
            // it detects the sentinel routing token.
            localUpdater.batchOn();

            int count = 0;
            for (String autoName : autoNames) {
                int rt = RT_EVAL_AUTO;
                if (count++ >= autoNames.size()-1) {
                    // Last one. Use sentinel routing token of 4
                    rt = RT_EVAL_AUTO_LAST;
                }
                // expr_heval's will "continue" in setChasedPointer()
                dbx.expr_heval(rt, "-r " + autoName);	// NOI18N
            }
        } else {
            autos.add(null);    // in order to let the debugger show the special warning message
        }
        
        return autoNames;
    }

    private class LocalEnableLatch extends EnableLatch {
	public void setEnabled(boolean enable) {
	    dbx.locals_notify(0, enable);
	    notePileup(enable, Catalog.get("NoLocalsWhileRunning"));// NOI18N
	}
    }

    // control notification to dbx engine to send local variable data
    LocalEnableLatch localEnabler = new LocalEnableLatch(); 

    public void registerLocalModel(LocalModel model) {
	localUpdater.addListener(model);

	if (postedKill)
	    return;
	
	if (model == null)
	    localEnabler.setRegistered(false);
	else
	    localEnabler.setRegistered(true);
    }

    /*
     * Stack stuff
     */
    /* package */ final void setStack(GPDbxFrame[] stack) {
        int nf = stack.length;
        if (nf <= 0) {
            return;
        }

        int actualFrames = 0;
	boolean bottomframe_seen = false;

        for (int i = 0; i < nf; i++) {
            if (stack[i].attr_user_call) {
                actualFrames++;
            } else if (stack[i].attr_sig != 0 &&
                    stack[i].attr_sig != -1) {
                actualFrames++;
            }
	    actualFrames++;

            if (stack[i].bottomframe)
		bottomframe_seen = true;

        }
	if (!bottomframe_seen)
	    actualFrames++; // for entry "More..."

        guiStackFrames = new DbxFrame[actualFrames];

        int fx = 0;
        for (int i = 0; i < nf; i++) {
            GPDbxFrame f = stack[i];
            // Turn off 'attr_user_call' in original object but
            // remember the value in order to create new placeholder obj
            boolean attr_user_call = f.attr_user_call;
            f.attr_user_call = false;
            int attr_sig = f.attr_sig;
            String attr_signame = f.attr_signame;
            f.attr_sig = 0;
            f.attr_signame = null;

            guiStackFrames[fx] = new DbxFrame(this, f, null);
            fx++;
            if (attr_user_call) {
                GPDbxFrame dummy = new GPDbxFrame();
                dummy.attr_user_call = true;
                guiStackFrames[fx] = new DbxFrame(this, dummy, null);
                fx++;
            } else if (attr_sig != 0 && attr_sig != -1) {
                GPDbxFrame dummy = new GPDbxFrame();
                dummy.attr_sig = attr_sig;
                dummy.attr_signame = attr_signame;
                guiStackFrames[fx] = new DbxFrame(this, dummy, null);
                fx++;
            }
        }
	// add "More entry" in the end
	if (!bottomframe_seen) {
	    GPDbxFrame dummy = new GPDbxFrame();
	    guiStackFrames[fx] = new DbxFrame(this, dummy, null);
	    guiStackFrames[fx].more = true;
	    guiStackFrames[fx].setFunc(Catalog.get("MoreFrames"));
	}


        stackUpdater.treeChanged();	// causes a pull
        debuggingViewUpdater.treeChanged();
        disassembly.stateUpdated();
    }

    public Frame[] getStack() {
        if (guiStackFrames == null) {
            return new DbxFrame[0];
        } else {
            return guiStackFrames;
        }
    }

    private class StackEnableLatch extends EnableLatch {

	public void setEnabled(boolean enable) {
	    // default max_frames is 40
	    int max_frames = 40;
	    try {
		Integer.parseInt(DebuggerOption.STACK_MAX_SIZE.getCurrValue(optionLayers()));
	    } catch (NumberFormatException x) {
		// fix for the symptoms (but not cause) of CR 6564951
	    }

	    dbx.stack_notify(0, enable, max_frames, 0);
	    notePileup(enable, Catalog.get("NoStackWhileRunning"));// NOI18N
	}
    }

    // control notification to dbx engine to send stack data
    StackEnableLatch stackEnabler = new StackEnableLatch(); 
    private boolean get_frames = false; // indicate Stack View open/close

    public void registerStackModel(StackModel model) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
	    System.out.printf("\t registerModel.model_registered = true \n"); // NOI18N
	stackUpdater.addListener(model);

	if (postedKill)
	    return;

        get_frames = model != null;
        
        if (get_frames || get_debugging) {
	    stackEnabler.setRegistered(true);
        } else {
	    stackEnabler.setRegistered(false);
        }
    }

    public Frame getCurrentFrame() {
        if (guiStackFrames == null) {
            return null;
        }

        return currentFrame;
    }

    public void setCurrentFrame(int frameno) {
        if (guiStackFrames == null) {
            return;
        }

        // Sort of the slow way. SHOULD just record the current frame and
        // trigger treeNodeChanged() on the old and new frames

        boolean changed = false;
        for (int fx = 0; fx < guiStackFrames.length; fx++) {
            if (((DbxFrame) guiStackFrames[fx]).getFrameNo() == frameno) {
                if (guiStackFrames[fx].isCurrent()) {
                    currentFrame = guiStackFrames[fx];
                    break;		// no change in state
                }
                changed = true;
                guiStackFrames[fx].setCurrent(true);
            // don't break cuz need to reset 'current' in rest of frames
            } else {
                guiStackFrames[fx].setCurrent(false);
            }
        }

        if (changed) {
            stackUpdater.treeChanged();	// causes a pull
            debuggingViewUpdater.treeChanged();
            disassembly.stateUpdated();
        }
    }
    public void moreFrame() {
        int max_frame = Integer.parseInt(DebuggerOption.STACK_MAX_SIZE.getCurrValue(optionLayers()));
        max_frame +=40;
        setOption("DBX_stack_max_size", Integer.toString(max_frame)); // NOI18N
    }

    public void makeFrameCurrent(Frame frame) {
        if (frame.isSpecial()) {
            return;
        }
        String cmd = "frame " + frame.getNumber();		// NOI18N
        dbx.sendCommand(0, 0, cmd);
    }

    public void postVerboseStack(boolean v) {
        String cmd = "dbxenv stack_verbose " + (v ? "on" : "off");// NOI18N
        dbx.sendCommand(0, 0, cmd);
    }

    public void postPrettyPrint(boolean v) {
        String cmd = "dbxenv output_pretty_print " + (v ? "on" : "off");// NOI18N
        dbx.sendCommand(0, 0, cmd);
    }

    public void postOutputBase(String output_base) {
        String cmd = "dbxenv output_base " + output_base;// NOI18N
        dbx.sendCommand(0, 0, cmd);
    }

    /*
     * Thread stuff
     */
    private DbxThread[] threads = new DbxThread[0];
    // private ModelChangeDelegator threadUpdater = new ModelChangeDelegator();

    public final void setThreads(GPDbxThread[] dbxThreads) {
        threads = new DbxThread[dbxThreads.length];

        for (int tx = 0; tx < dbxThreads.length; tx++) {
            threads[tx] = new DbxThread(this, threadUpdater, dbxThreads[tx]);
        }

        threadUpdater.treeChanged();	// causes a pull
        debuggingViewUpdater.treeChanged();
    }

    public Thread[] getThreads() {
        return threads;
    }

    private class ThreadEnableLatch extends EnableLatch {
	public void setEnabled(boolean enable) {
	    dbx.threads_notify(0, enable , 0);
	    notePileup(enable, Catalog.get("NoThreadsWhileRunning"));// NOI18N
	}
    }

    // control notification to dbx engine to send threads info
    ThreadEnableLatch threadEnabler = new ThreadEnableLatch();
    private boolean get_threads = false; // indicate Thread View open/close

    public void registerThreadModel(ThreadModel model) {
	threadUpdater.addListener(model);

	if (postedKill)
	    return;

        get_threads = model != null;
        
        if (get_threads || get_debugging) {
	    threadEnabler.setRegistered(true);
        } else {
	    threadEnabler.setRegistered(false);
        }
    }

    public void setCurrentThread(long tid) {

        // Not as slow as it seems.
        // setCurrent() will not cause a refresh if currency hasn't changed.
        // As a result at most we'll get two calls to 'threadChanged' one
        // for the outgoing current and one for the incoming current.
        //
        // A faster way would be to have a 'currentThread' pointer but
        // not sure where is the best place to keep it. LATER.

        boolean found = false;
        for (int tx = 0; tx < threads.length; tx++) {
            if (threads[tx].getId() == tid) {
                threads[tx].setCurrent(true);
                found = true;
            } else {
                threads[tx].setCurrent(false);
            }
        }
        if (!found) {
            // work around when tid does not have htid
            // Remove it when glue or/and dbx will be fixed
            int res = 0;
            for (int tx = 0; tx < threads.length; tx++) {
                long ltid = threads[tx].getId() & 0xFFFFFFFFL;
                if (ltid == tid) {
                    threads[tx].setCurrent(true);
                    found = true;
                    res = tx;
                    break;
                }
            }
            if (found) {
                CndUtils.assertTrueInConsole(false, "DBX asks to set current thread for unexisting TID = "+tid+". Thread found by ltid of thread "+threads[res].getName());
            } else {
                CndUtils.assertTrueInConsole(false, "DBX asks to set current thread for unexisting TID = "+tid);
            }
        }

        if (RegistersWindow.getDefault().isShowing()) {
            dbx.register_notify(0, true);
        }

    }

    public void makeThreadCurrent(Thread thread) {
        // was: Dbx.selectThread
        dbx.sendCommand(0, 0, "thread " + ( (DbxThread)thread).getName()); // NOI18N
    }

    public boolean isMultiThreading() {
        return state().multi_threading;
    }

    @Override
    protected void setCurrentLine(Line l, boolean visited, boolean srcOOD, ShowMode showMode, boolean focus) {
        super.setCurrentLine(l, visited, srcOOD, showMode, focus);
        RtcMarker.getDefaultError().clearLine(this);
        RtcMarker.getDefaultFrame().clearLine(this);
    }

    public void postKill() {
        // was: finishDebugger()
        // We get here when ...
        // - Finish action on session node
        // - When IDE is exiting

        // The quit to dbx will come back to us as kill()
        // which will call killEngine()
        // debuggercore itself never calls killEngine()!

        postedKill = true;

        if (dbx != null && dbx.connected()) {
            // Ask dbx to quit (shutdown)
            dbx.sendCommandIntNoresume(0, 0, "quit");	// NOI18N
        } else {
            // since there's no dbx connection (e.g. failed to start)
            // call kill directly
            kill();
        }
    }

    public void shutDown() {
        if (!postedKill) {
            postKill();
        }
    }


    /*
     * Breakpoint, Handler and Event stuff
     */

    // interface NativeDebugger
    // interface NativeDebuggerImpl
    @Override
    public HandlerExpert handlerExpert() {
        return handlerExpert;
    }

    // interface BreakpointProvider
    @Override
    public void postRestoreHandler(final int rt, final HandlerCommand hc) {

        // CR 6810534
        if (Log.Bpt.fix6810534) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    dbx().sendCommand(rt, 0, hc.getData());
                }
            });
        } else {
            dbx().sendCommand(rt, 0, hc.getData());
        }

	// We'll come back either via newHandler() or noteBreakpointError().
    }


    /**
     * Toggling a bpt off will map to a dbx "clear" command.
     * "clear" will delete all bpts on that line and any kind not just
     * line bpts.
     */
    public void removeBreakpointByLine(String src, int line) {
        String cmd = "clear " + src + ":" + line;	// NOI18N
        dbx.sendCommandInt(0, 0, cmd);
    }


    // newBrokenHandler() defined in super
    public void newHandler(int rt, GPDbxHandler h) {
        // SHOULD be factorable with noteBreakpointError/newBrokenHandler

        Handler handler = null;
	BreakpointPlan bp = bm().getBreakpointPlan(rt, BreakpointMsg.NEW);

	switch (bp.op()) {
	    case NEW:
		handler = handlerExpert.newHandler(this,
					           h,
					           null,
					           bp.isFallback());
		break;
	    case RESTORE:
		handler = handlerExpert.newHandler(this,
			                           h,
			                           bp.restored(),
			                           false);
		assert handler.breakpoint() == bp.restored();
		break;
	    case MODIFY:
		handler = bp.originalHandler();
		handlerExpert.replaceHandler(handler, h);
		break;
	}
	bm().noteNewHandler(rt, bp, handler);
    }

    void replaceHandler(int rt, Handler targetHandler, GPDbxHandler newData) {

	BreakpointPlan bp = bm().getBreakpointPlan(rt, BreakpointMsg.REPLACE);
        assert bp.op() == BreakpointOp.MODIFY :
                "replaceHandler(): bpt plan not CHANGE for rt " + rt; // NOI18N

        assert targetHandler == bp.originalHandler();

        handlerExpert.replaceHandler(targetHandler, newData);

	bm().noteReplacedHandler(bp, null);
    }

    public void setHandlerDefunct(Handler handler, int rt, boolean on) {
        handler.setDefunct(on);
        if (on) {
            // Create a DEFUNCT BJ for noteBreakpointError to handle
            bm().noteDefunctBreakpoint(handler.breakpoint(), rt);
        }
    }

    public void setHandlerCountLimit(int hid, long countLimit) {
        dbx.sendCommand(0, 0, "handler -count " + hid + " " + // NOI18N
                ((countLimit == -1) ? "infinity" : Long.toString(countLimit))); // NOI18N
    }

    // interface BreakpointProvider
    @Override
    public void postEnableHandler(int rt, int hid, boolean enable) {
        if (enable) {
            dbx.sendCommandInt(rt, 0, "handler -enable " + hid); // NOI18N
        } else {
            dbx.sendCommandInt(rt, 0, "handler -disable " + hid); // NOI18N
        }
    }

    // interface BreakpointProvider
    @Override
    public void postEnableAllHandlersImpl(boolean enable) {
        if (enable) {
            dbx.sendCommandInt(0, 0, "handler -enable all"); // NOI18N
        } else {
            dbx.sendCommandInt(0, 0, "handler -disable all"); // NOI18N
        }
    }

    // interface BreakpointProvider
    @Override
    public void postActivateBreakpoints() {
        final Handler[] handlers = bm().getHandlers();

        // no need to enable/disable if there is no handlers
        if (handlers.length == 0) {
            return;
        }
        
        StringBuilder command = new StringBuilder();
        command.append("handler -enable"); // NOI18N
        
        for (Handler h : handlers) {
            if (h.breakpoint().isEnabled()) {
                command.append(' ');
                command.append(h.getId());
            }
        }
        
        breakpointsActivated = true;
        dbx.sendCommandInt(0, 0, command.toString());
    }

    // interface BreakpointProvider
    @Override
    public void postDeactivateBreakpoints() {
        final Handler[] handlers = bm().getHandlers();

        // no need to enable/disable if there is no handlers
        if (handlers.length == 0) {
            return;
        }
        
        StringBuilder command = new StringBuilder();
        command.append("handler -disable"); // NOI18N

        for (Handler h : handlers) {
            if (h.breakpoint().isEnabled()) {
                command.append(' ');
                command.append(h.getId());
            }
        }
        
        breakpointsActivated = false;
        dbx.sendCommandInt(0, 0, command.toString());
    }
    
    // interface BreakpointProvider
    @Override
    public void postDeleteAllHandlersImpl() {
        dbx.sendCommandInt(0, 0, "delete all"); // NOI18N
    }

    // interface BreakpointProvider
    @Override
    public void postDeleteHandlerImpl(int rt, int hid) {
	dbx.sendCommandInt(rt, 0, "delete " + hid); // NOI18N
	// Will come back via deleteHandlerById
    }

    // interface BreakpointProvider
    @Override
    public void postCreateHandlerImpl(int routingToken, HandlerCommand hc) {
	dbx.sendCommandInt(routingToken, 0, hc.getData());
	// We'll come back either via newHandler() or noteBreakpointError().
    }


    // interface BreakpointProvider
    @Override
    public void postChangeHandlerImpl(int rt, HandlerCommand hc) {

        // this will show up as a handler_replace or error
        dbx.sendCommandInt(rt, 0, hc.getData());
    }

    // interface BreakpointProvider
    @Override
    public void postRepairHandlerImpl(int rt, HandlerCommand cmd) {
        // this will show up as a handler_new or error
        dbx.sendCommandInt(rt, 0, cmd.getData());
    }

    protected void postVarContinuation(int rt, VarContinuation vc) {
        dbx.ksh_scmd_async(rt, 0, "kprint $" + vc.getName()); // NOI18N
    }

    /**
     * The program has hit a signal; produce a popup to ask the user
     * how to handle it.
     */
    private void showSignalPopup(String description) {

        // Extract signal info.
        // A sample description from dbx is
        //  "signal 11 SIGSEGV sigcode 0 SI_USER sigsender 19957"

        String signame = "?"; // NOI18N
        String signum = "?"; // NOI18N
        String usercodename = "?"; // NOI18N
        String usercodenum = "?"; // NOI18N
        String senderpid = "?"; // NOI18N

        StringTokenizer st = new StringTokenizer(description);
        String dummy = st.nextToken(); // e.g. "signal"

        assert dummy.equals("signal") || dummy.equals("Signal");	// NOI18N
        signum = st.nextToken(); // e.g. "11"
        signame = st.nextToken(); // e.g. "SIGSEGV"
        dummy = st.nextToken(); // e.g. "sigcode"
        assert dummy.equals("sigcode"); // NOI18N
        usercodenum = st.nextToken(); // e.g. "0"
        usercodename = st.nextToken(); // e.g. "SI_USER"
        dummy = st.nextToken(); // e.g. "sigsender"
        assert dummy.equals("sigsender"); // NOI18N
        senderpid = st.nextToken(); // e.g. "19957"

        assert !st.hasMoreTokens();

        SignalDialog sd = new SignalDialog();

        String signalInfo;
        signalInfo = Catalog.format("FMT_SignalInfo",
                signame, signum, usercodename, usercodenum);
        sd.setSignalInfo(signalInfo);


        sd.setSenderInfo(senderpid);

        if (session != null) {
            sd.setReceiverInfo(session.getShortName(), session.getPid());
        } else {
            sd.setReceiverInfo("", 0);
        }

        Signals.InitialSignalInfo dsii = null;
        int signo = 0;
        int index = 0;

        try {
            signo = Integer.parseInt(signum);
            index = signo - 1;
            DbgProfile dbxprofile = ddi.getDbgProfile();
            dsii = dbxprofile.signals().getSignal(index);
        } catch (Exception x) {
        }

        boolean wasIgnored = false;
        if (dsii != null) {
            wasIgnored = ! dsii.isCaught();
            sd.setIgnore(true, wasIgnored);
        } else {
            sd.setIgnore(true, false); // default
        }

        sd.show();

        if (dsii != null && sd.isIgnore() != wasIgnored) {
            if (sd.isIgnore()) {
                dbx.sendCommand(0, 0, "ignore " + dsii.signo());	// NOI18N
            } else {
                dbx.sendCommand(0, 0, "catch " + dsii.signo());	// NOI18N
            }
        }

        boolean signalDiscarded = sd.discardSignal();
        if (signalDiscarded) {
            deliverSignal = -1;
        } else {
            deliverSignal = signo;
        }

        if (sd.shouldContinue()) {
            go();
        }
    }

    /**
     * was: Dbx.updateFiredEvents()
     */
    public void updateFiredEvents(GPDbxEventRecord[] events) {

        if (events == null) {
            return;
        }

        for (Handler h : bm().getHandlers()) {
            boolean match = false;
            for (int ex = 0; ex < events.length; ex++) {
                if (h.getId() == events[ex].hid) {
                    match = true;
                    break;
                }
            }

            h.setFired(match);
        }
    }

    public void explainStop(GPDbxEventRecord[] events) {
        if (session.coreSession() != DebuggerManager.getDebuggerManager().getCurrentSession()) {
            DebuggerManager.getDebuggerManager().setCurrentSession(session.coreSession());
        }

        if (events.length == 0) {
            return;
        }

        /*
        if (warnOnlyOnce1) {
        System.out.println("Dbx.explainStop:  Ignoring background events code");
        warnOnlyOnce1 = false;
        }
         */

        /* reinstate later!
        if (eventListeners.size() == 0) {
        // Nobody is listening ... preserve events.
        backgroundEvents = events;
        backgroundEventCount = nevents;
        }
         */

        // Translate from dbx "enum" strings to WorkShop parlance.
        // For now, we'll stick with dbx's names

        String desc = events[0].description;
        boolean builtin = "builtin".equals(events[0].origin);	// NOI18N
        boolean internal = "internal".equals(events[0].origin);	// NOI18N

        String stateMsg = desc;

        if (internal) {
            // 5034232
            stateMsg = null;

        } else if ("stepped".equals(desc)) { // NOI18N

            // Perhaps "stepped" is uninteresting.
            //stateMsg = Catalog.get("Dbx_stepped"); // NOI18N
            // Let's just set it to null instead to suppress these
            stateMsg = null;

        } else if ("function returned".equals(desc)) { // NOI18N
            stateMsg = Catalog.get("Dbx_function_returned"); // NOI18N

        } else if (desc.startsWith("throw unhandled ")) { // NOI18N
            stateMsg = Catalog.get("Dbx_throw_unhandled") + // NOI18N
                    desc.substring(15);

        } else if (desc.startsWith("throw unexpected ")) { // NOI18N
            stateMsg = Catalog.get("Dbx_throw_unexpected") + // NOI18N
                    desc.substring(16); // include space

        } else if (desc.startsWith("throw caught ")) { // NOI18N
            stateMsg = Catalog.get("Dbx_throw_caught") + // NOI18N
                    desc.substring(12); // include space

        } else if (desc.startsWith("signal ")) { // NOI18N
            stateMsg = Catalog.get("Dbx_signal") + // NOI18N
                    desc.substring(6); // include space
        }

        // Else: some user-defined handler event. In that case, don't
        // translate the state since the handler description will make
        // sense to the user

        if (stateMsg != null) {
            setStatusText(stateMsg);
        }

        if (builtin && // NOI18N
                events[0].description.startsWith("signal ")) { // NOI18N
            // suppress SIGINT popup if remote dbx
            if ( /* LATER (getPioTerm() != null) || */(!events[0].description.startsWith("signal 2 SIGINT"))) { // NOI18N
                // String begins with "signal 11 SIGSEGV " ...
                // we want to pull out the "11"

                /* a typical stateMsg that comes from dbx look like this:
                 * "signal 11 SIGSEGV sigcode 0 SI_USER sigsender 19957"
                 * stateMsg also has signal number, move string->integer
                 * conversion of signal number to showSignalPopup().
                int signalNo = Integer.parseInt(events[0].description.
                substring(7, 9));
                showSignalPopup(stateMsg, signalNo);
                 */
                showSignalPopup(stateMsg);
            }
        } else if (builtin && // NOI18N
                (events[0].description.startsWith("throw unhandled ") || //NOI18N
                events[0].description.startsWith("throw unexpected "))) { //NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    events[0].description));
        }
    }

    /**
     * Synchronous function to return the value of $scope.
     */
    public String getCurrentScope() {
        return dbx.getVar("vscope");	// NOI18N
    }
    private final boolean optionDebugTried = false;
    private boolean optionDebug = false;

    private boolean getOptionDebug() {
        if (!optionDebugTried) {
            String value = System.getProperty("spro.optiondebug"); // NOI18N
            if ("on".equals(value)) // NOI18N
            {
                optionDebug = true;
            }
        }
        return optionDebug;
    }

    public void balloonEvaluate(final Line.Part lp, final String expr, final boolean forceExtractExpression) {
        if (!DebuggerOption.BALLOON_EVAL.isEnabled(manager().globalOptions())) {
            return;
        }


        if (!SwingUtilities.isEventDispatchThread()) {
            // Transfer flow from from RP to eventQ
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    balloonEvaluate(lp, expr, forceExtractExpression);
                }
            });
            return;
        }

        // balloonEvaluate() requests come from the editor completely
        // independently of debugger startup and shutdown.

        if (!isConnected()) {
            return;
        }

	// 7098557 IDE may hang due to balloon evaluation if debuggee is running.
        if (!state().isLoaded || state().isLoading ||
		!state().isProcess || state().isRunning) {
	    return;
	}

        
        String text = expr;
        if (forceExtractExpression) {
            text = EvalAnnotation.extractExpr(lp, expr);
        }
        if (Disassembly.isInDisasm()) {
            // probably a register - append $ at the beginning
            if (text != null && !text.isEmpty()) {
                if (Character.isLetter(text.charAt(0))) {
                    text = '$' + text;
                }
                dbx.expr_eval(RT_EVAL_REGISTER, DbxEvalFormat.HEXADECIMAL8.getOption() + ' ' + text);
            }
        } else {
            // remember to pathmap if file ever becomes non-null
            dbx.expr_line_eval(lp.getLine().getLineNumber(), 0, expr, lp.getColumn(), null, 0, GPDbxLineEval.COMBO_ALL);
        }

        // result will be sent to us asynchronously via expr_line_eval_result()
        // which will call balloonResult() below.
    }
    private QualifiedExprListener qeListener;

    public void postExprQualify(String expr, QualifiedExprListener qeListener) {
        this.qeListener = qeListener;
        dbx.expr_qualify(0, expr);
    }

    void qualifiedExpr(int rt, String qualifiedForm, int error) {
        if (qeListener != null) {

            if (error == GPDbxExprError.AMBIG) {
                qualifiedForm = "<ambiguous>"; // NOI18N
            } else if (error == GPDbxExprError.PARSE) {
                qualifiedForm = "<parse error>"; // NOI18N
            } else if (error != 0) {
                qualifiedForm = "<error>"; // NOI18N
            }

            qeListener.qualifiedExpr(qualifiedForm, error == 0);
            qeListener = null;
        }
    }

    public void balloonResult(final int lineNumber, int rt2, int flags,
            final String lhs, final String rhs,
        String rhs2, String rhs3) {
//        EvalAnnotation.postResult(rt1, rt2, flags, lhs, rhs, rhs2, rhs3);
        //final int lineNumber = 0;//= lp.getLine();
//        DataObject dob = DataEditorSupport.findDataObject(line);
//        if (dob == null) {
//            return;
//        }
//        final EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
//        if (ec == null) {
//            return;
//            // Only for editable dataobjects
//        }
//        StyledDocument doc;
//        try {
//            doc = ec.openDocument();
//        } catch (IOException ex) {
//            return;
//        }
        final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
        if (ep == null){// || ep.getDocument() != doc) {
            return ;
        }      
        final DbxWatch watch = new DbxWatch(this, localUpdater, 0, false, lhs, rhs);
        final String toolTip = lhs + "=" + rhs;//NOI18N
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
//                final ToolTipView.ExpandableTooltip expTooltip = ToolTipView.getExpTooltipForText(DbxDebuggerImpl.this, lhs, rhs);
//                expTooltip.showTooltip();
                EditorUI eui = Utilities.getEditorUI(ep);
                if (eui == null) {
                    //firePropertyChange(PROP_SHORT_DESCRIPTION, null, toolTip);
                    return;
                }
                ToolTipUI.Expandable expandable = !(watch.isLeaf())
                        ? new ToolTipUI.Expandable(lhs, watch)
                        : null;
                ToolTipUI.Pinnable pinnable = new ToolTipUI.Pinnable(
                        lhs,
                        lineNumber,
                        "NativePinWatchValueProvider");   // NOI18N
                ToolTipUI toolTipUI = ViewFactory.getDefault().createToolTip(toolTip, expandable, pinnable);
                ToolTipSupport tts = toolTipUI.show(ep);
            }
        });
    }
    
    @Override
    public void evaluateInOutline(String expr) {
        dbx.expr_heval(RT_EVAL_TOOLTIP, expr);
    }

    // interface NativeDebugger
    @Override
    public void postRestoring(boolean restoring) {
        dbx.restoring(restoring);
    }

    /**
     * was: latter part Dbx.prop_changed()
     */
    public void debuggingOptionChanged(OptionValue o, String new_value) {

        // Special case: if we've set the run_io to "pty" and
        // the run_pty to the pty of the PIO window, we really want
        // the run_io option to be "window" (dbx doesn't support that
        // option but we're faking it up)
        //
        // This mirrors setOption()

        String slave = null;
        if (!factory.connectExisting()) {
            slave = getIOPack().getSlaveName();
        }

        OptionValue run_io = optionLayers().byType(DebuggerOption.RUN_IO);
        OptionValue run_pty = optionLayers().byType(DebuggerOption.RUN_PTY);

        if (o.type() == DebuggerOption.RUN_PTY && new_value.equals(slave) && !"stdio".equals(run_io.get())) { //NOI18N
            run_io.set("window"); // NOI18N
        } else if (o.type() == DebuggerOption.RUN_IO &&
                new_value.equals("pty") && // NOI18N
                run_pty.get().equals(slave)) {
            new_value = "window"; // NOI18N
        }

        // It's possible that the IO arrives in the wrong order



        // Another special case: rtc_biu_at_exit and rtc_mel_at_exit can
        // take on the value of "verbose" which we will convert to "on".
        // When reporting to the IDE dbx will send both reports so
        // the distinction vanishes.

        if (o.type() == RtcOption.RTC_BIU_AT_EXIT ||
                o.type() == RtcOption.RTC_MEL_AT_EXIT) {

            if (new_value.equals("verbose")) // NOI18N
            {
                new_value = "on";		// NOI18N
            }
        }


        o.set(new_value);
    }

    public OptionClient getOptionClient() {
        OptionClient option = new DbxDebuggerOptionClient(this);
        return option;
    }

    // CR 6502043
    // get dbx option value by dbxenv name e.g. output_dynamic_type
    public String getDebuggingOption(String name) {
        OptionValue o = optionLayers().byName(name);
        if (o != null) {
            return o.get();
        }
        return null;
    }

    public void debuggingOptionChanged(String name, String new_value) {

        /* OLD
         CR 6669451
        // ignore dbxenv's set in .dbxrc!
        if (dbx.dbxInitializing())
        return;
         */

        if (name == null) {
            return;
        }

        OptionValue o = optionLayers().byName(name);
        if (o != null) {
            debuggingOptionChanged(o, new_value);
        } else {
        }
    }

    public void debuggingOptionChanged(Option t, String new_value) {
        OptionValue o = optionLayers().byType(t);
        if (o != null) {
            debuggingOptionChanged(o, new_value);
        }
    }

    private void setOptionHelp(String name, String value) {
	// CR 6995039
        //if (getOptionDebug()) {
	if (name != null && name.startsWith("DBX_")) { // NOI18N
	    String dbxenv = name.substring(4); // strip "DBX_"
	    dbx.sendCommand(0, 0,
                    "### dbxenv " + dbxenv + " " + value );// NOI18N
	}
        //}
        dbx.prop_set(name, value);
    }

    // interface NativeDebugger
    // SHOULD be postSetOption()
    @Override
    public void setOption(String name, String value) {


        // convert io setting sentinels to actual values
        if ("DBX_run_io".equals(name) && "window".equals(value)) {// NOI18N
            if (factory.connectExisting()) {
                return;
            }
            String slave = getIOPack().getSlaveName();
            setOptionHelp("DBX_run_pty", slave);		// NOI18N
            value = "pty";					// NOI18N

        /* TMP
        } else if ("DBX_run_pty".equals(name) &&		// NOI18N
        DebuggerOption.RUN_IO.getCurrValue(optionLayers()).equals("window")) {// NOI18N

        value = termset.getDefault().getPty().getSlaveName();
        }
         */
        } else if ("DBX_run_pty".equals(name)) {		// NOI18N
            if (factory.connectExisting()) {
                return;
            }
            // Always send it for now ... since thp moved most options to
            // global trying to get at optionLayers() when syncing global ones
            // due to dbxDoneInitialization causes NPE's
            // SHOULD be ok once we move DBX_run_pty back to per-profile.
            String slave = getIOPack().getSlaveName();
            value = slave;

        } else if ("DBX_follow_fork_inherit".equals(name)) { // NOI18N
            // skip it since we set it above
            return;
        }

        setOptionHelp(name, value);
    }

    private RtcProfile currentRtcProfile() {
        DbxDebuggerSettings currentSettings = (DbxDebuggerSettings)profileBridge.getCurrentSettings();
        return currentSettings.rtcProfile();
    }

    @Override
    protected void addExtraOptions(OptionLayers optionLayers) {
        super.addExtraOptions(optionLayers);
        RtcProfile rtcProfile = currentRtcProfile();
        if (rtcProfile != null) {
            OptionSet rtcOptions = rtcProfile.getOptions();
            assert rtcOptions != null : "rtc profile must have options";
            optionLayers.push(rtcOptions);
        }
    }
    /*
     * RTC stuff
     */
    RtcView rtcView = null;
    RtcModel rtcModel = null;

    public RtcModel rtcModel() {
        if (rtcModel == null) {
            rtcModel = new RtcModel("");
        }
        return rtcModel;
    }

    public void rtcListen() {
        // Always listen to these notifications
        dbx.rtc_notify(0, true);
        dbx.mprof_notify(0, true);
    }

    private boolean isRtcEnabled() {
        return state().accessOn || state().leaksOn || state().memuseOn;
    }

    private void enableRtcWindow() {
        RTCWindowAction.enableRtcWindow();
    }

    void RTCStateChanged() {
        /* OLD
        Perhaps shouldn't do this as the RTC window indicates very little
        of the state.
        Enabling the window fronts it and that grabs focus away from the
        dbx cmdline.

        if (isRtcEnabled())
        enableRtcWindow();
         */

        rtcState.accessOn = state().accessOn;
        rtcState.memuseOn = state().memuseOn;
        rtcState.leaksOn = state().leaksOn;

        rtcModel().accessStateChanged(rtcState);
        rtcModel().memuseStateChanged(rtcState);

        // propagate state to options
        RtcOption.RTC_ACCESS_ENABLE.setEnabled(optionLayers(),
                rtcState.accessOn);
        RtcOption.RTC_LEAKS_MEMUSE_ENABLE.setEnabled(optionLayers(),
                rtcState.memuseOn ||
                rtcState.leaksOn);
        if (rtcState.accessOn || rtcState.memuseOn || rtcState.leaksOn) {
            RtcOption.RTC_ENABLE_AT_DEBUG.setEnabled(optionLayers(), true);
        } else if (!rtcState.accessOn && !rtcState.memuseOn && !rtcState.leaksOn) {
            RtcOption.RTC_ENABLE_AT_DEBUG.setEnabled(optionLayers(), false);
        }
    }

    private void setChecking(boolean access, boolean memuse) {

        // Allowing simultaneous turning on and off isn't so much a matter
        // of optimization as it is a matter of dbx bugs
        // CR 6697146 (separate checkk -access/check -memuse during attach
        // only do half the job)

        if (access == isAccessCheckingEnabled() &&
                memuse == isMemuseEnabled()) {
            return;	// nothing to do
        }

        if (!access && !memuse) {
            // Turn both off simultaneously
            dbx.sendCommand(0, 0, "uncheck -all");	// NOI18N

        } else if (access && memuse) {
            // Turn both on simultaneously
            dbx.sendCommand(0, 0, "check -all" + // NOI18N
                    rtcFrameOptions());

        } else {
            // Turn on/off individually
            setAccessChecking(access);
            setMemuseChecking(memuse);
        }
    }

    private void setMemuseChecking(boolean enable) {
        // note : if we call this to effect only changes in -frames and -match
        // it will not work.
        if (enable == isMemuseEnabled()) {
            return;	// nothing to do
        }
        if (enable) {
            dbx.sendCommand(0, 0, "check -memuse" + // NOI18N
                    rtcFrameOptions());
        } else {
            dbx.sendCommand(0, 0, "uncheck -memuse"); // NOI18N
        }
    }

    private boolean isMemuseEnabled() {
        return state().memuseOn;
    }

    /**
     * Construct the -frames and or -match options to the rtc commands
     * on the basis of the relevant Option's.
     */
    private String rtcFrameOptions() {
        String options = "";


        /* OLD
        String customStackFrames =
        RtcOption.RTC_CUSTOM_STACK_FRAMES.getCurrValue(optionLayers());

        if (customStackFrames.equals("custom")) {
        String customStackFramesValue =
        RtcOption.RTC_CUSTOM_STACK_FRAMES_VALUE.getCurrValue(optionLayers());
        options += " -frames " + customStackFramesValue;
        }
         */
        String customStackFrames =
                RtcOption.RTC_CUSTOM_STACK_FRAMES2.getCurrValue(optionLayers());
        options += " -frames " + customStackFrames; // NOI18N


        /* OLD
        String customStackMatch =
        RtcOption.RTC_CUSTOM_STACK_MATCH.getCurrValue(optionLayers());

        if (customStackMatch.equals("custom")) {
        String customStackMatchValue =
        RtcOption.RTC_CUSTOM_STACK_MATCH_VALUE.getCurrValue(optionLayers());
        options += " -match " + customStackMatchValue;
        }
         */
        String customStackMatch =
                RtcOption.RTC_CUSTOM_STACK_MATCH2.getCurrValue(optionLayers());
        options += " -match " + customStackMatch; // NOI18N

        return options;
    }

    private void setLeaksChecking(boolean enable) {

        /* LATER
        While dbx has a -leaks command the gui, the glue protocol and
        to a certain extent dbx itself have a hard time reflecting a
        state where only leaks checking is turned on.

        So for now we enable all memuse since it implicitly enables
        leak checking.

        if (enable) {
        dbx.sendCommand(0, 0, "check -leaks" +	// NOI18N
        rtcFrameOptions());
        } else {
        dbx.sendCommand(0, 0, "uncheck -leaks"); // NOI18N
        }
         */

        // note : if we call this to effect only changes in -frames and -match
        // it will not work.
        if (enable == isLeaksEnabled()) {
            return;	// nothing to do
        }
        if (enable) {
            dbx.sendCommand(0, 0, "check -memuse" + // NOI18N
                    rtcFrameOptions());
        } else {
            dbx.sendCommand(0, 0, "uncheck -memuse");	// NOI18N
        }
    }

    private boolean isLeaksEnabled() {
        return state().leaksOn;
    }

    private void setAccessChecking(boolean enable) {

        if (enable == isAccessCheckingEnabled()) {
            return;	// nothing to do
        }
        if (enable) {
            dbx.sendCommand(0, 0, "check -access"); // NOI18N
        } else {
            dbx.sendCommand(0, 0, "uncheck -access"); // NOI18N
        }
    }

    private boolean isAccessCheckingEnabled() {
        return state().accessOn;
    }

    private void suppressLastError() {
        dbx.sendCommand(0, 0, "suppress -last"); // NOI18N
    }

    private void showLeaks(boolean all, boolean detailed) {
        dbx.sendCommand(0, 0, "showleaks" + // NOI18N
                (all ? " -a" : "") + // NOI18N
                (detailed ? " -v" : ""));	// NOI18N

    }

    private void showBlocks(boolean all, boolean detailed) {
        dbx.sendCommand(0, 0, "showmemuse" + // NOI18N
                (all ? " -a" : "") + // NOI18N
                (detailed ? " -v" : ""));	// NOI18N
    }


    /*
     * Disassembler stuff
     *
     */

    // interface NativeDebugger
    @Override
    public void registerDisassembly(Disassembly dis) {

	//assert dis == null || dis == disassemblerWindow();

	boolean makeAsmVisible = (dis != null);
	if (makeAsmVisible == isAsmVisible())
	    return;

        if (postedKillEngine)
            return;

        if (!isConnected())
            return;

//	if (! viaShowLocation) {
//	    // I.e. user clicked on Disassembly tab or some other tab
//	    if (makeAsmVisible)
//		requestDisassembly();
//	    else
//		requestSource(false);
//	}

        if (makeAsmVisible) {
	    setAsmVisible(true);
	    dbx.dis_notify(0, true);
        } else {
	    setAsmVisible(false);
	    dbx.dis_notify(0, false);
        }
    }

    // implement NativeDebuggerImpl
    protected DisFragModel disModel() {
        return disModel;
    }

    // implement NativeDebuggerImpl
    public Controller disController() {
	return disController;
    }
    
    public DbxDisassembly getDisassembly() {
        return disassembly;
    }
    
    private void requestDisFromDbx(String cmd, String start) {
        if (postedKill || postedKillEngine || dbx == null) {
            return;
        }

	// Even though dis_info() is slated to get a count parameter
	// inpractice we've never used it. Instead we've used listi vs
	// dis with hacks like tacking on a "/100" to 'start'.

        dbx.dis_info(cmd, start, null);
    }

    public void setDis(String dis_codes) {
        disModel.parseData(dis_codes);
        disassembly.update(disModel);
	// CR 6582172
//        if (update_dis) {
//            disStateModel().updateStateModel(visitedLocation, false);
//        }
    }

    @Override
    public void registerRegistersWindow(RegistersWindow rw) {
        if (postedKillEngine) {
            return;
        }
        if (!isConnected() || !state().isProcess) {
            return;
        }
        if (rw == null) {
            dbx.register_notify(0, false);
        } else {
            dbx.register_notify(0, true);
        }
    }

    public void assignRegisterValue(String register, String value) {
        dbx.sendCommand(0, 0, "assign $" + register + " = " + value); // NOI18N
    }

    void setRegs(String regs) {
	if (RegistersWindow.getDefault().isShowing()) {
            LinkedList<String> res = new LinkedList<String>();
            int i, j, k, l, regnamelen;
            String s, regname, regvalue;
            l = 1;
            for (i = 0; i < regs.length(); i++, l++) {
                k = regs.indexOf('\n', i);
                if (k < i) break;
                s = regs.substring(i, k + 1);
                i = k;
                regname = null;
                regvalue = null;
                regnamelen = 0;
                for (j=0, k=0; j < s.length(); j++) {
                    if (s.charAt(j) != ' ') {
                        if (k == 0) {
                            k = s.indexOf(' ', j);
                            if (k < j) break;
                            regnamelen = k - j;
                            regname = s.substring(j, k);
                            j = k;
                        } else {
                            k = s.indexOf('\n', j);
                            if (k < j) break;
                            regvalue = s.substring(j, k);
                            break;
                        }
                    }
                }
                if (l != 1) {
                    // skip the first line (current frame || current thread)
                    // second line could be "current frame" 
                    // if first line is current thread
                    if ((regname != null) && (regvalue != null)) {
                        if (!regname.equals("current")) { // NOI18N
//                            if (regname.indexOf("g0") > 0) 		// NOI18N
//                                seen_sparc_regs = true;
//                            if (regname.indexOf("ax") > 0) 	// NOI18N
//                                seen_sparc_regs = false;
                            if (regnamelen < 6)  {
                                res.add("   " + // NOI18N
                                                 regname +
                                                 "  \t\t" + // NOI18N
                                                 regvalue +
                                                 "\n"); // NOI18N
                            } else if (regnamelen < 14) {
                                res.add("   " + // NOI18N
                                                 regname +
                                                 "  \t" + // NOI18N
                                                 regvalue +
                                                 "\n"); // NOI18N
                            } else {
                                res.add("   " + // NOI18N
                                                 regname +
                                                 regvalue +
                                                 "\n"); // NOI18N
                            }
                        }
                    } else {
                        res.add("   " + s); // NOI18N
                    }
                }
            }
	    RegistersWindow.getDefault().updateData(res);
        }
    }
    private EvaluationWindow currentEvaluationWindow = null;

    public void registerEvaluationWindow(EvaluationWindow mw) {
        currentEvaluationWindow = mw;
    /*
    if (postedKillEngine)
    return;
    if (mw == null)
    dbx.mem_notify(0, false);
    else
    dbx.mem_notify(0, true);
     */
    }
    private final ArrayBrowserController arrayBrowserController = new DbxArrayBrowserController();

//    private ArrayBrowserWindow currentArrayBrowserWindow = null;
//    public void registerArrayBrowserWindow(TopComponent aw) {
//        currentArrayBrowserWindow = (ArrayBrowserWindow) aw;
//    }

    private class DbxArrayBrowserController implements ArrayBrowserController {

        public void displayArray(String name, String indexing) {
            if (name != null) {
                dbx.sendCommand(0, 0, "vitem -new " + name); // NOI18N
            }
        }

        public void deleteArray(int id) {
            dbx.sendCommand(0, 0, "vitem -delete " + id); // NOI18N
        }

        public void sendArrayViewUpdates(boolean enable, int id) {
            dbx.vitem_item_notify(0, enable, id);
        }
    }

    public void addNewVItem(int rt, GPDbxVItemStatic sitem) {
        ArrayBrowserWindow.getDefault().addArrayView(sitem);
    }

    public void deleteVItem(boolean proc_gone, int id) {
        ArrayBrowserWindow abw = ArrayBrowserWindow.getDefault();
        abw.setProcGone(proc_gone);
        abw.deleteArrayView(id);
    }

    public void updateVItem(int nitems, GPDbxVItemDynamic items[]) {
        ArrayBrowserWindow.getDefault().updateArrayView(nitems, items);
    }

    @Override
    public void registerMemoryWindow(MemoryWindow mw) {
        super.registerMemoryWindow(mw);
        if (postedKillEngine) {
            return;
        }
        if (!isConnected()) {
            return;
        }
        if (mw == null) {
            dbx.mem_notify(0, false);
        } else {
            dbx.mem_notify(0, true);
        }
    }

    void setMems(String mems) {
	if (MemoryWindow.getDefault().isShowing()) {
            LinkedList<String> res = new LinkedList<String>();
            int i, j, k, l, memaddrlen;
            String s, memaddr, memvalue;
            l = 1;
            for (i = 0; i < mems.length(); i++, l++) {
                k = mems.indexOf('\n', i);
                if (k < i) break;
                s = mems.substring(i, k + 1);
                i = k;
                memaddr = null;
                memvalue = null;
                memaddrlen = 0;
                for (j=0, k=0; j < s.length(); j++) {
                    if (s.charAt(j) != ' ') {
                        if (k == 0) {
                            k = s.indexOf(' ', j);
                            if (k < j) break;
                            memaddrlen = k - j;
                            memaddr = s.substring(j, k);
                            j = k;
                        } else {
                            k = s.indexOf('\n', j);
                            if (k < j) break;
                            memvalue = s.substring(j, k);
                            memvalue = align_memvalue(memvalue);
                            break;
                        }
                    }
                }
                res.add("   " + memaddr + "  " + memvalue + "\n"); // NOI18N
            }
	    MemoryWindow.getDefault().updateData(res);
        }
    }
    
    private String align_memvalue(String memvalue) {
        int i, j, valuelen, maxvaluelen, total_len;
        String value, new_memvalue;
        char c;
        
        FormatOption memoryFormat = MemoryWindow.getDefault().getMemoryFormat();
        
        if (memoryFormat == DbxMemoryFormat.DECIMAL || 
                memoryFormat == DbxMemoryFormat.OCTAL) {
            total_len = memvalue.length();
            j = memvalue.indexOf(':', 0);
            new_memvalue = "";		// NOI18N
       	    if (j >= 0) {
       	        // Symbol information
       	        j++;
       	        new_memvalue = memvalue.substring(0, j);
       	    } else {
       	        j = 0;
       	    }
       	    for (i = 0 ; i < 4 ; i++) {
       	        for ( ; j < total_len ; j++) {
       	            c = memvalue.charAt(j);
       	            if ((c == ' ') || (c == '\t')) {
       	                new_memvalue = new_memvalue + " ";	// NOI18N
       	                continue;
       	            }
       	            break;
       	        }
       	        if (total_len <= j) break;
       	        value = "";	// NOI18N
       	        valuelen = 0;
       	        maxvaluelen = 12;
       	        if (memoryFormat == DbxMemoryFormat.DECIMAL) {
       	            if (memvalue.charAt(j) == '-') {
       	                value = "-"; // NOI18N
       	                j++;
       	            } else {
       	                value = "+"; // NOI18N
       	            }
       	            valuelen = 1;
       	            maxvaluelen = 11;
       	        }
       	        for ( ; j < total_len ; j++) {
       	            c = memvalue.charAt(j);
       	            if ((c >= '0') && (c <= '9')) {
       	                value = value + c;
       	                valuelen++;
       	                continue;
       	            }
       	            break;
       	        }
       	        if (valuelen > maxvaluelen) return memvalue; // something wrong
       	        for ( ; valuelen < maxvaluelen ; valuelen++) {
       	            value = " " + value; // NOI18N
       	        }
       	        new_memvalue = new_memvalue + value;
            }
            return new_memvalue;
        }
        return memvalue;
    }
    
    public FormatOption[] getMemoryFormats() {
        return DbxMemoryFormat.values();
    }

    public void requestMems(String start, String length, FormatOption format) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("DbxDebuggerImpl.requestMems() ready %s\n", // NOI18N
                    isConnected());
        }
        if (!isConnected() || !state().isProcess) {
            return;
        }
        dbx.mem_format(start, length, format.getOption());
    }

    /**
     * was: Dbx.proc_about_to_fork()
     */
    void aboutToFork() {
        manager().aboutToFork(this, session.getShortName());
    }

    public void forkThisWay(NativeDebuggerManager.FollowForkInfo ffi) {
        if (ffi.parent && ffi.child) {
            // Follow both
            dbx.prop_set("DBX_follow_fork_mode_inner", "both"); // NOI18N
        } else if (ffi.parent) {
            dbx.prop_set("DBX_follow_fork_mode_inner", "parent"); // NOI18N
        } else if (ffi.child) {
            dbx.prop_set("DBX_follow_fork_mode_inner", "child"); // NOI18N
        }

        if (ffi.parent || ffi.child) {
            if (ffi.stopAfterFork) {
                runProgram("step");	// NOI18N
            } else {
                // SHOULD use 'lastRunCmd' ?
                runProgram("cont");	// NOI18N
            }
        } else {
            // dialog was cancelled do nothing.
        }
    }

    /**
     * was: Dbx.clone()
     */
    void clone(String argv[], boolean cloned_to_follow) {

        // See CR 6573955 for various issues surrounding follow-fork both.

        // SHOULD refine cloning behavior for sessions here...
        // We've forked, and so we need to create a new configuration
        DbxDebuggerSettings dbxSettings  = (DbxDebuggerSettings) profileBridge.cloneMainSettings();
        RunProfile newProfile = (dbxSettings == null) ? null : dbxSettings.runProfile();
	DbgProfile newDbgProfile = (dbxSettings == null) ? null : dbxSettings.dbgProfile();
        String hostName = getNDI().getHostName();

        // CR 6814282 DbxDebuggerInfo ddi = DbxDebuggerInfo.create();
        ddi.setAdditionalArgv(argv);
        ddi.setHostName(hostName);
        ddi.setProfile(newProfile);
        ddi.setDbgProfile(newDbgProfile);
        ddi.setRtcProfile(new RtcProfile("./"));	// NOI18N
        ddi.setClone();		// CR 6814282

        profileBridge.setup(ddi);

        manager().debugNoAsk(ddi);
    }

    public void notifyUnsavedFiles(String file[]) {
        // perhaps SHOULD use interruptQuietly?
        if (!isConnected()) {
            return;
        }
	String mappedFile[] = new String[file.length];
	for (int sx = 0; sx < file.length; sx++) {
	    mappedFile[sx] = localToRemote("notifyUnsavedFiles", file[sx]); // NOI18N
	}
        dbx.unsaved_files(mappedFile.length, mappedFile);
    }
    private Line current_dis_line = null;

    // interface NativeDebugger
    @Override
    public void setCurrentDisLine(Line l) {
        current_dis_line = l;
    }

    public Line getCurrentDisLine() {
        return current_dis_line;
    }

    private class DisController extends ControllerSupport {

	protected void setBreakpointHelp(String address) {
	    dbx.sendCommandInt(0, 0, "stopi at" + " " + address); // NOI18N
	}

        // interface Controller
	@Override
        public void requestDis(boolean withSource) {
            if (visitedLocation == null)
                return;

            String start;
            String cmd;

            if (visitedLocation.hasSource()) {
		// request by line #
		final int linesAbove = 5;
		final int linesBelow = 35;

		// CR 6742661
                if (visitedLocation.line() <= 0)
		    visitedLocation = visitedLocation.line(1);

		int startLine = visitedLocation.line() - linesAbove;
		if (startLine <= 0)
		    startLine = 1;
		start = Integer.toString(startLine) + "," + // NOI18N
			Integer.toString(visitedLocation.line() + linesBelow);

                cmd = "listi"; // NOI18N

            } else {
		long pc = visitedLocation.pc();
		if (pc > 0) {
		    String addr = Address.toHexString0x(pc, true);
		    start = addr + "/" + 100; // NOI18N
		    cmd = "dis "; // NOI18N
		} else {
		    String function = visitedLocation.func();
		    if (function != null) {
			start = "-a " + function; // NOI18N
			cmd = "dis "; // NOI18N
		    } else {
			NativeDebuggerManager.warning(Catalog.get("Dis_MSG_NoSource"));
			return;
		    }
		}
            }
            requestDisHelp(cmd, start);
        }

        // interface Controller
	@Override
        public void requestDis(String start, int count, boolean withSource) {
	    start = start + "/" + count; // NOI18N
	    requestDisHelp("dis ", start); // NOI18N
        }

        private void requestDisHelp(String cmd, String start) {
            if (cmd.equals("listi")) { // NOI18N
                // CR 6582172
                update_dis = true;
                requestDisFromDbx(cmd, start);
            } else if (cmd.equals("dis ")) { // NOI18N
                // CR 6582172
                if (start.startsWith("-a")) { // NOI18N
                    update_dis = true;
                    if (start.equals("-a (unknown)") || start.equals("-a null")) // NOI18N
                    // CR 6733766 stepi into PLT
                    // replace "dis -a (unknown)" with "dis <address>/100" dbx command
                    {
                        start = Address.toHexString0x(visitedLocation.pc(), true);
			start += "/100";	// NOI18N
                    }
                } else {
                    update_dis = false;
                }
                requestDisFromDbx(cmd, start);
            }

        }
    }

    private final class DisModel extends DisModelSupport {

	/**
	 * Interpret disassembly in 'record', stuff it into this
	 * DisFragModel and update(), notifying the DisView.
	 */

        public void parseData(String mem) {
            if (mem == null) {
                return;
            }
            clear();
            List<Line> lines = new ArrayList<Line>();
            for (int i = 0; i < mem.length(); i++) {
                int k = mem.indexOf('\n', i);
                if (k < i) {
                    break;
                }
                String s = mem.substring(i, k + 1);
                i = k;
                
                String memaddr = null;
                String memvalue = null;
                for (int j = 0, m = 0; j < s.length(); j++) {
                    if (s.charAt(j) != ' ') {
                        if (m == 0) {
                            m = s.indexOf(' ', j);
                            if (m < j) {
                                break;
                            }
                            memaddr = s.substring(j, m);
                            j = m;
                            if (!memaddr.startsWith("0x")) { // NOI18N
                                int lineend = s.indexOf('\n', j);
                                if (lineend < j) {
                                    memvalue = "";
                                } else {
                                    memvalue = s.substring(j, lineend);
                                }
                                break;
                            }
                        } else {
                            m = s.indexOf('\n', j);
                            if (m < j) {
                                break;
                            }
                            memvalue = s.substring(j, m);
                            break;
                        }
                    }
                }
                // check for ':' at the end
                if (memaddr != null && memaddr.endsWith(":")) { //NOI18N
                    memaddr = memaddr.substring(0, memaddr.length()-1);
                    memvalue = ":  " + memvalue; //NOI18N
                } else {
                    memvalue = "  " + memvalue; //NOI18N
                }
                lines.add(new Line(memaddr, memvalue));
            }
            addAll(lines);
	    update();
        }
    }

    /**
     * @returns Whether it is a good idea to make the project rebuild on
     * the next debug.
     */
    private boolean safeToRebuild() {
        MakefileConfiguration makefileConfiguration =
                ddi.getMakefileConfiguration();

        if (makefileConfiguration == null) {
            return true;	// managed project, OK to rebuild
        }
        StringConfiguration buildCommandConf =
                makefileConfiguration.getBuildCommand();

        /* DEBUG
        String        buildCommand = buildCommandConf.getValue();
        String defaultBuildCommand = buildCommandConf.getDefault();

        System.out.printf("defaultBuildCommand: %s\n", defaultBuildCommand);
        System.out.printf("       buildCommand: %s\n", buildCommand);
        System.out.printf("           modified: %s\n", buildCommandConf.getModified());
         */

        if (buildCommandConf.getModified()) {
            // user customized their build command
            return true;

        } else {
            // user didn't customize it so it's unlikely to work well.
            // In fact it will infinite loop in SS12 (see IZ 106578).
            return false;
        }
    }

    /**
     * Some files were edited and fixed. 
     * Ensure that on the next debug we rebuild the application first.
     *
     * See CR's 6317809, 6322910 and IZ 74267.
     */
    void rebuildOnNextDebug(String target, String files[]) {
        RunProfile runProfile = ddi.getProfile();
        DbgProfile dbxProfile = ddi.getDbgProfile();

        if (!safeToRebuild()) {
            StringBuilder sb = new StringBuilder();
            sb.append(Catalog.format("FixPendingBuild", target));
            sb.append('\n');
            for (int i = 0; i < files.length; i++) {
                sb.append("   " + files[i] + "\n"); // NOI18N
            }
            sb.append('\n');
            sb.append(Catalog.get("FixPendingBuildWarn"));	// NOI18N
            NativeDebuggerManager.warning(sb.toString());

        } else {
            boolean oldBuildFirst = runProfile.getBuildFirst();
            runProfile.setBuildFirst(true);

            // Remember so we can restore it on the next debug
            // It gets restored in ProfileBridge.XsetProfile()
            dbxProfile.setBuildFirstOverriden(true);
            dbxProfile.setSavedBuildFirst(oldBuildFirst);
        }
    }

    // implement NativeDebuggerImpl
    protected void stopUpdates() {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
	    System.out.printf("\t stopUpdates \n"); // NOI18N

	stackEnabler.setMasked(true);
	localEnabler.setMasked(true);
	watchEnabler.setMasked(true);
	threadEnabler.setMasked(true);
    }

    // implement NativeDebuggerImpl
    protected void startUpdates() {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
	    System.out.printf("\t startUpdates \n"); // NOI18N

	stackEnabler.setMasked(false);
	localEnabler.setMasked(false);
	watchEnabler.setMasked(false);
	threadEnabler.setMasked(false);
    }

    private abstract class EnableLatch {

	private boolean connected = false;
	private boolean registered = false;
	private boolean masked = false;

	private boolean isEnabled = false;

	protected abstract void setEnabled(boolean enabled);

	public final void setConnected(boolean connected) {
	    this.connected = connected;
	    recalculate();
	}

	public final void setRegistered(boolean registered) {
	    this.registered = registered;
	    recalculate();
	}

	public final void setMasked(boolean masked) {
	    this.masked = masked;
	    recalculate();
	}

	/**
	 * Force setEnabled() to get called.
	 */
	public final void update() {
	    boolean needEnable = connected && registered && !masked;
	    setEnabled(needEnable);
	    isEnabled = needEnable;
	}

	private void recalculate() {
	    boolean needEnable = connected && registered && !masked;
	    if (needEnable != isEnabled)
		setEnabled(needEnable);
	    isEnabled = needEnable;
	}

    }

    /*
     * Which mode is this session in now? See DbxJN_mode for possible values,
     * which currently are DBX_PLAIN (normal native debugging),
     * DBX_JAVA (normal jdbx debugging) and DBX_JAVANATIVE (jdbx debugging
     * in joff mode; e.g. a native view onto a java program, possibly in
     * a JNI call but not necessarily)
     */
    private int javaMode = GPDbxJN_mode.DBX_PLAIN;

    public int getJavaMode() {
	return javaMode;
    }
    public void setJavaMode(int mode) {
	javaMode = mode;
    }

}
