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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStart;
import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStartManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.*;
import org.netbeans.modules.cnd.debugger.common2.debugger.Error;
import org.netbeans.modules.cnd.debugger.common2.debugger.MacroSupport;
import org.netbeans.modules.cnd.debugger.common2.debugger.Thread;
import org.netbeans.modules.cnd.debugger.common2.debugger.ToolTipView.VariableNode;
import org.netbeans.modules.cnd.debugger.common2.debugger.ToolTipView.VariableNodeChildren;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Controller;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.DisFragModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Disassembly;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.FormatOption;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.MemoryWindow;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.RegistersWindow;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointManager.BreakpointMsg;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointManager.BreakpointOp;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointManager.BreakpointPlan;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointProvider;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.Handler;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerCommand;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerExpert;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.ExceptionBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.FunctionBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.InstructionBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.LineBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.SysCallBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.io.IOPack;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Signals;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CndRemote;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Platform;
import org.netbeans.modules.cnd.debugger.common2.utils.Executor;
import org.netbeans.modules.cnd.debugger.common2.utils.FileMapper;
import org.netbeans.modules.cnd.debugger.common2.utils.InfoPanel;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.ItemSelectorResult;
import org.netbeans.modules.cnd.debugger.common2.utils.StopWatch;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionClient;
import org.netbeans.modules.cnd.debugger.gdb2.actions.GdbStartActionProvider;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommand;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIConst;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITListItem;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIUserInteraction;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.debugger.ui.ToolTipUI;
import org.netbeans.spi.debugger.ui.ViewFactory;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.util.Exceptions;

public final class GdbDebuggerImpl extends NativeDebuggerImpl
        implements BreakpointProvider, Gdb.Factory.Listener {

    private AtomicBoolean breakpointsStateIsChangedAndEnabled = new AtomicBoolean(false);
    private AtomicBoolean breakpointsStateIsChangedAndDisabled = new AtomicBoolean(false);
    private GdbEngineProvider engineProvider;
    private volatile Gdb gdb;				// gdb proxy
    private GdbVersionPeculiarity peculiarity;  // gdb version differences
    private final DebuggerSettingsBridge profileBridge;

    static final Logger LOG = Logger.getLogger(GdbDebuggerImpl.class.toString());

    private final GdbHandlerExpert handlerExpert;
    private volatile MILocation homeLoc;
    private boolean dynamicType;
    //current stack frame number
    //bz#270229 - IDE doesn't update "Call Stack" tab if this tab is not a current
    private String currentStackFrameNo = "0";//NOI18N

    private DisModel disModel = new DisModel();
    private DisController disController = new DisController();
    private final GdbDisassembly disassembly;
    private boolean update_dis = true;

    private final VariableBag variableBag = new VariableBag();

    private FileMapper fmap = FileMapper.getDefault();

    private String currentThreadId;

    public static final String MI_BKPT = "bkpt";     //NOI18N
    public static final String MI_WPT = "wpt";       //NOI18N
    public static final String MI_EXP = "exp";       //NOI18N
    public static final String MI_NUMBER = "number"; //NOI18N
    public static final String MI_FRAME = "frame";   //NOI18N
    public static final String MI_THREADS = "threads";                  //NOI18N
    public static final String MI_CURRENT_THREAD = "current-thread-id"; //NOI18N
    public static final String MI_WATCHPOINT_TRIGGER = "watchpoint-trigger"; //NOI18N
    public static final String MI_WATCHPOINT_SCOPE = "watchpoint-scope"; //NOI18N
    public static final String MI_SYSCALL_ENTRY = "syscall-entry"; //NOI18N
    public static final String MI_SYSCALL_RETURN = "syscall-return"; //NOI18N
    public static final String MI_NUMCHILD = "numchild"; //NOI18N

    /**
     * Utility class to help us deal with 'frame' or 'source file'
     */
    private static class MILocation extends Location {

        /*
         * frameTuple : frame information, could be null
         *    addr, func, args, file (short), fullname, line  (stopped)
         *
         * srcTuple : source file information
         *    line, file, fullname
         *
         * use 'frameTuple' information first, if null, then use 'srcTuple'
         */
        public static MILocation make(NativeDebugger debugger,
                MITList frameTuple,
                MITList srcTuple,
                boolean visited,
                int stackSize,
                NativeBreakpoint breakpoint) {

            String src = "";
            int line = 0;
            String func = null;
            long pc = 0;
            int level = 0;
            //20^done,stack=[frame={level="0",addr="<unavailable>",func="??"}] - exception, see bz#247777
            try {
                if (frameTuple != null) {
                    func = frameTuple.getConstValue("func"); // NOI18N
                    src = frameTuple.getConstValue("fullname", srcTuple != null ? srcTuple.getConstValue("fullname", null) : null); //NOI18N
                    level = Integer.parseInt(frameTuple.getConstValue("level", "0")); // NOI18N
                    line = Integer.parseInt(frameTuple.getConstValue("line", "0")); //NOI18N
                    pc = Address.parseAddr(frameTuple.getConstValue("addr", "0")); // NOI18N
                } else if (srcTuple != null){
                    // use srcTuple
                    src = srcTuple.getConstValue("fullname", null); // NOI18N
                    line = Integer.parseInt(srcTuple.getConstValue("line", "0")); //NOI18N
                }
            }catch (NumberFormatException ex) {
                //log exception
                LOG.log(Level.WARNING, "NumberFormatException occurred while parsing  " + (frameTuple != null ? "frameTuple " + frameTuple : //NOI18N
                        (srcTuple != null ? "srcTuple " + srcTuple : "")));//NOI18N
            }

            src = debugger.remoteToLocal("MILocation", src); // NOI18N

            return new MILocation(src,
                    line,
                    func,
                    pc,
				  Location.UPDATE |
				  (visited ? Location.VISITED: 0) |
                                  (level == 0 ? Location.TOPFRAME : 0) |
                                  (level >= stackSize-1 ? Location.BOTTOMFRAME : 0),
                    breakpoint);
        }

        public static MILocation make(MILocation h, boolean visited) {
            return new MILocation(h.src(),
                    h.line(),
                    h.func(),
                    h.pc(),
				  Location.UPDATE |
				  (visited ? Location.VISITED: 0) |
                                  (h.topframe() ? Location.TOPFRAME: 0) |
                                  (h.bottomframe() ? Location.BOTTOMFRAME: 0),
                    h.getBreakpoint());
        }

        private MILocation(String src, int line, String func, long pc,
                int flags, NativeBreakpoint breakpoint) {
            super(src, line, func, pc, flags, breakpoint);
        }
    }

    public GdbDebuggerImpl(ContextProvider ctxProvider) {
        super(ctxProvider);
        final List<? extends DebuggerEngineProvider> l = debuggerEngine.lookup(null, DebuggerEngineProvider.class);
        for (int lx = 0; lx < l.size(); lx++) {
            if (l.get(lx) instanceof GdbEngineProvider) {
                engineProvider = (GdbEngineProvider) l.get(lx);
            }
        }
        if (engineProvider == null) {
            throw new IllegalArgumentException("GdbDebuggerImpl not started via GdbEngineProvider"); // NOI18N
        }

        //
        // enhance State
        //

        // Actually SHOULD control this by prop sets
        state().capabAutoRun = false;

        profileBridge = new GdbDebuggerSettingsBridge(this);
        handlerExpert = new GdbHandlerExpert(this);
        disassembly = new GdbDisassembly(this, breakpointModel());
        disStateModel().addListener(disassembly);
        //need to attach to EditorContext Bridge as we need to 
    }

    @Override
    public String debuggerType() {
        return "gdb"; // NOI18N
    }

    @Override
    public DebuggerSettingsBridge profileBridge() {
        return profileBridge;
    }

    public Gdb gdb() {
        return gdb;
    }

    /**
     *
     * @return true if it's OK to send messages to gdb
     */
    @Override
    public boolean isConnected() {
        // See "README.startup"
        if (gdb == null || !gdb.connected() || postedKillEngine) {
            return false;
        } else {
            return true;
        }
    }

    private GdbDebuggerInfo gdi;

    public void rememberDDI(GdbDebuggerInfo gdi) {
        this.gdi = gdi;
    }

    // interface NativeDebugger
    @Override
    public NativeDebuggerInfo getNDI() {
        return gdi;
    }

    boolean isShortName() {
        DebuggerOption option = DebuggerOption.OUTPUT_SHORT_FILE_NAME;
        return option.isEnabled(optionLayers());
    }

    public void start(final GdbDebuggerInfo gdi) {
        // SHOULD factor with DbxDebuggerImpl

        //
        // The following is what used to be in startDebugger():
        //

        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            int act = gdi.getAction();
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

        rememberDDI(gdi);
        session().setSessionHost(gdi.getHostName());
        session().setSessionEngine(GdbEngineCapabilityProvider.getGdbEngineType());

        // This might make sense for gdbserver for example
        final boolean connectExisting;
        if ((gdi.getAction() & NativeDebuggerManager.CONNECT) != 0) {
            connectExisting = true;
        } else {
            connectExisting = false;
        }

        profileBridge.setup(gdi);
        if (!connectExisting) {
            int flags = 0;
            if (Log.Startup.nopty) {
                flags |= Executor.NOPTY;
            }
            executor = Executor.getDefault(
                    Catalog.get("Gdb"),  // NOI18N
                    getHost(),
                    flags,
                    new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (e instanceof NativeProcessChangeEvent) {
                        if (((NativeProcessChangeEvent) e).state == NativeProcess.State.FINISHED) {
                            if (!postedKill) {
                                NativeDebuggerManager.warning(// In order to avoid catching the exception from exitValue()
                                        Catalog.format(
                                                "MSG_GdbUnexpectedlyStopped", // NOI18N
                                                executor.getExitValue()));
                                kill();
                            }
                        }
                    }
                }
            });
        }

        final String additionalArgv[] = null; // gdi.getAdditionalArgv();

        if (gdi.isCaptured()) {
            ExternalStart xstart = ExternalStartManager.getXstart(getHost());
            if (xstart != null) {
                xstart.debuggerStarted();
            }
        }

        // See "README.startup"
        if (NativeDebuggerManager.isAsyncStart()) {

            // May not be neccessary in the future.
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    start2(executor, additionalArgv, GdbDebuggerImpl.this, connectExisting);
                }
            });

        } else {
            CndRemote.validate(gdi.getHostName(), new Runnable() {
                @Override
                public void run() {
                    start2(executor, additionalArgv, GdbDebuggerImpl.this, connectExisting);
                }
            });
        }
    }

    private boolean directoryExists(ExecutionEnvironment env, String path)
            throws ConnectException, IOException, InterruptedException {

        // first try via file system
        FileObject fileObject = FileSystemProvider.getFileObject(env, path);
        if (fileObject != null) {
            return fileObject.isFolder();
        } else {
            // what if file system just wasn't refresh? try directly
            return HostInfoUtils.directoryExists(env, path);
        }
    }

    private Gdb.Factory factory;

    private void start2(final Executor executor,
            final String additionalArgv[],
            final Gdb.Factory.Listener listener,
            final boolean connectExisting) {

        String gdbInitFile = DebuggerOption.GDB_INIT_FILE.getCurrValue(optionLayers());
        boolean preventInitPathConvertion = gdbInitFile.startsWith("///"); // NOI18N
        if (!preventInitPathConvertion) {
            gdbInitFile = localToRemote("gdbInitFile", gdbInitFile); // NOI18N
        }

        // SHOULD process OPTION_EXEC32?
        String runDir = gdi.getRunDir();
        boolean preventRunPathConvertion = runDir.startsWith("///"); // NOI18N

        if (!preventRunPathConvertion) {
            runDir = localToRemote("gdbRunDirectory", runDir); // NOI18N
        }

        final String finalRunDir = runDir;
        final String finalGdbInitFile = gdbInitFile;

        NativeDebuggerManager.getRequestProcessor().post(new Runnable() {

            @Override
            public void run() {
                final AtomicBoolean exists = new AtomicBoolean();
                try {
                    exists.set(directoryExists(executor.getExecutionEnvironment(), finalRunDir));
                } catch (IOException ioe) {
                    exists.set(false); // broken link?
                } catch (InterruptedException ex) {
                    return; // cancelled?
                }
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (!exists.get()) {
                            NativeDebuggerManager.error(Catalog.format("MSG_NonExistentWorkDir", finalRunDir)); // NOI18N
                            kill();
                            return;
                        }
                        factory = new Gdb.Factory(executor, additionalArgv,
                                listener, false, isShortName(),
                                finalGdbInitFile,
                                getHost(),
                                connectExisting,
                                finalRunDir,
                                gdi);
                        factory.start();
                    }
                });
            }
        });
    }

    /* OLD

    Moved to Gdb.Factory et al

    private void start2(GdbDebuggerInfo gdi) {
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


        //
        // Get gdb and pio consoles
        //
        IOPack ioPack = new GdbIOPack();
        ioPack.setup(remote);
        setIOPack(ioPack);


        // We need the slave name ahead of time
        boolean havePio = executor.startIO(getIOPack().pio);
        if (!havePio) {
            ;   // SHOULD do something
        }


        String gdbname = "gdb";

        // Startup arguments to gdb:
        Vector avec = new Vector();

        avec.add(gdbname);

        // flags to get gdb going as an MI service
        avec.add("--interpreter");
        avec.add("mi");

	// attach or debug corefile
        String program = gdi.getTarget();
        long attach_pid = gdi.getPid();
        String corefile = gdi.getCorefile();

        if (corefile != null) {
            // debug corefile
            if (program == null) {
                program = " ";
            }
            avec.add(program);
            avec.add(corefile);

        } else if (attach_pid != -1) {
            // attach
            String image = Long.toString(attach_pid);
            if (program == null) {
                program = "-";
            }
        }

        // Arrange for gdb victims to run under the Pio
        boolean ioInWindow =
                true;
        if (executor.slaveName() != null && ioInWindow) {
            avec.add("-tty");
            avec.add(executor.slaveName());
        }

        String[] gdb_argv = new String[avec.size()];
        for (int vx = 0; vx < avec.size(); vx++) {
            gdb_argv[vx] = (String) avec.elementAt(vx);
        }


        gdb = new Gdb();

        // setup back- and convenience links from Gdb
        gdb.setDebugger(this);


        getIOPack().console().getTerm().pushStream(gdb.tap());
        getIOPack().console().getTerm().setCustomColor(0,
                Color.yellow.darker().darker());
        getIOPack().console().getTerm().setCustomColor(1,
	    Color.green.darker());
        getIOPack().console().getTerm().setCustomColor(2,
	    Color.blue.brighter());



        int pid = 0;
        pid = executor.startEngine(gdbname, gdb_argv, null,
                getIOPack().console());
        if (pid == 0) {
            return;
        }

        String hostName = null;
        if (remote) {
            hostName = host.getHostName();
        }

    }
     */

    // interface Gdb.Factory.Listener
    @Override
    public void assignGdb(Gdb tentativeGdb) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("GdbDebuggerImpl.assignGdb()\n"); // NOI18N
        }
        gdb = tentativeGdb;
        gdb.setDebugger(this);
        GdbStartActionProvider.succeeded();
        NativeDebuggerManager.get().setCurrentDebugger(this);
        // OLD initializeGdb(getGDI());
    }

    // interface Gdb.Factory.Listener
    @Override
    public void assignIOPack(IOPack ioPack) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("GdbDebuggerImpl.assignIOPack()\n"); // NOI18N
        }
        setIOPack(ioPack);
    }

    // interface gdb.Factory.Listener
    @Override
    public void connectFailed(String toWhom, String why, IOPack ioPack) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("GdbDebuggerImpl.connectFailed()\n"); // NOI18N
        }
        String msg = Catalog.format("ConnectionFailed", toWhom, why); // NOI18N
        Gdb.dyingWords(msg, ioPack);

        // kill() doesn't work unless ACTION_KILL is enabled.
        session.kill();
    }


    /* OLD
    void connectionEstablished() {

        GdbStartActionProvider.succeeded();

        // setup DebuggerManager currentDebugger
        DebuggerManager.get().setCurrentDebugger(this);

        initializeGdb(getGDI());
    }
     */

    private static boolean warnUnsupported = false;

    private void warnVersionUnsupported(String gdbVersion) {
        if (!warnUnsupported) {
            InfoPanel panel = new InfoPanel(
                    Catalog.format("ERR_UnsupportedVersion", gdbVersion), //NOI18N
                    Catalog.get("MSG_Do_Not_Show_Again_In_Session")); //NOI18N
            NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                    panel,
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(descriptor);
            warnUnsupported = panel.dontShowAgain();
        }
    }

    void setGdbVersion(String version) {
        GdbVersionPeculiarity.Version gdbVersion = new GdbVersionPeculiarity.Version(6, 8);
        try {
            gdbVersion = GdbUtils.parseVersionString(version);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to parse gdb version {0}", version); //NOI18N
        }
        peculiarity = GdbVersionPeculiarity.create(gdbVersion, getHost().getPlatform());
        if (!peculiarity.isSupported()) {
            warnVersionUnsupported(gdbVersion.toString());
        }
    }

    /**
     * Only called by proxy when gdb goes away.
     * (Or on ACTION_KILL if there is no good gdb connection)
     *
     * was: sessionExited() and if(cleanup) portion of finishDebugger()
     */
    public final void kill() {
        super.preKill();

        optionLayers().save();

        // System.out.println("kill.resetDebugWindowLayout();");
        // resetDebugWindowLayout();

        /* LATER
        setDisassemblerWindow(false);
        if (currentDisassemblerWindow != null) {
        currentDisassemblerWindow.close();
        }
        setMemoryWindow(false);
        if (currentMemoryWindow != null) {
        currentMemoryWindow.close();
        }
        setRegistersWindow(false);
        if (currentRegistersWindow != null) {
        currentRegistersWindow.close();
        }
         */

        IOPack ioPack = getIOPack();
        if (ioPack != null) {
            ioPack.bringDown();
            ioPack.close();
        }

        postedKillEngine = true;
        state().isLoaded = false;
        stateChanged();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (MemoryWindow.getDefault().isShowing()) {
                    MemoryWindow.getDefault().setDebugger(null);
                }
            }
        });

        // tell debuggercore that we're going away
        engineProvider.getDestructor().killEngine();

        // It all ends here
    }

    boolean postedKillEngine() {
        return postedKillEngine;
    }

    @Override
    public void postKill() {
        // was: finishDebugger()
        // We get here when ...
        // - Finish action on session node
        // - When IDE is exiting

        // DEBUG System.out.println("GdbDebuggerImpl.postKill()");

        // The quit to dbx will come back to us as kill()
        // which will call killEngine()
        // debuggercore itself never calls killEngine()!

        postedKill = true;

        //termset.finish();

        NativeDebuggerManager.getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                if (gdb != null && gdb.connected()) {
                    // see IZ 191508, need to pause before exit
                    // or kill gdb if process pid is unavailable
                    if (getHost().getPlatform() == Platform.Windows_x86 || !pause(true)) {
                        try {
                            executor.terminate();
                            kill();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        return;
                    }

                    // Ask gdb to quit (shutdown)
                    MICommand cmd = new MiCommandImpl("-gdb-exit") { // NOI18N

                        @Override
                        protected void onError(MIRecord record) {
                            finish();
                        }

                        @Override
                        protected void onExit(MIRecord record) {
                            kill();
                            finish();
                        }
                    };
                    gdb.sendCommand(cmd);
                } else {
                    // since there's no gdb connection (e.g. failed to start)
                    // call kill directly
                    kill();
                }
            }
        });
    }

    @Override
    public void shutDown() {
        postKill();
    }

    @Override
    public final void stepInto() {
        sendResumptive(peculiarity.execStepCommand(currentThreadId));
    }

    private static final String STEP_INTO_ID = "STEP_INTO"; //NOI18N

    private void stepIntoMain() {
        send("-break-insert -t main"); //NOI18N
        firstBreakpointId = STEP_INTO_ID; // to force pid request but avoid continue
        MICommand cmd = new MIResumptiveCommand("-exec-run") { // NOI18N

            @Override
            protected void onRunning(MIRecord record) {
                startRecordingIfNeeded();
                super.onRunning(record);
            }

        };
        gdb.sendCommand(cmd, true);
    }

    @Override
    public final void stepOver() {
        sendResumptive(peculiarity.execNextCommand(currentThreadId));
    }

    @Override
    public final void stepOut() {
        if (!peculiarity.isLldb()) {
            send("-stack-select-frame 0"); // NOI18N
        }
        execFinish();
    }

    private void execFinish() {
        sendResumptive(peculiarity.execFinishCommand(currentThreadId));
    }

    public final void pathmap(String pathmap) {
        send(pathmap);
    }

    private void notImplemented(String method) {
        System.out.printf("NOT IMPLEMENTED: GdbDebuggerImpl.%s()\n",// NOI18N
                method);
    }

    @Override
    public final void stepTo(final String selectedText) {
        if (selectedText == null || selectedText.trim().isEmpty()) {
            return;
        }
        String expandedExpr = MacroSupport.expandMacro(this, selectedText);
        String cmdString = "-data-evaluate-expression " + "\"" + expandedExpr + "\""; // NOI18N

        MiCommandImpl cmd = new MiCommandImpl(cmdString) {    //NOI18N

            @Override
            protected void onDone(MIRecord record) {
                MITList results = record.results();
                String value = results.getConstValue("value"); // NOI18N
                value = value.substring(value.indexOf('}') + 1); // NOI18N
                value = value.substring(0, value.indexOf('<')).trim(); // NOI18N

                if (value.isEmpty()) {
                    value = selectedText;       // fallback
                }

                MiCommandImpl cmd2 = new MiCommandImpl("-break-insert -t *" + value) {    //NOI18N

                    @Override
                    protected void onDone(MIRecord record) {
                        go();
                        finish();
                    }

                };
                gdb.sendCommand(cmd2);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    @Override
    public final void go() {
        if (state().isProcess) {
            sendResumptive("-exec-continue"); // NOI18N
        } else {
            sendResumptive("-exec-run"); // NOI18N
        }
    }

    @Override
    public void resumeThread(Thread thread) {
        sendResumptive("-exec-continue --thread " + ((GdbThread) thread).getId()); // NOI18N
    }

    private boolean targetAttach = false;
    private static final String ATTACH_ID = "ATTACH"; //NOI18N

    private void doMIAttach(GdbDebuggerInfo gdi) {
        String cmdString;
        String remoteTarget = gdi.getTargetCommand();
        if (remoteTarget != null) {
            cmdString = "target " + remoteTarget;  //NOI18N
            targetAttach = true;
        } else {
            long pid = gdi.getPid();
            // MI command "-target-attach pid | file" does not available in
            // gdb 6.1, 6.6, use CLI command "attach" instead.
            cmdString = "attach " + Long.toString(pid); //NOI18N
        }

        firstBreakpointId = ATTACH_ID;
        MICommand cmd = new MiCommandImpl(cmdString) {
            @Override
            protected void onDone(MIRecord record) {
                attachDone();
                startRecordingIfNeeded();
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    private void attachDone() {
        firstBreakpointId = null;
        if (state().isProcess) {
            return;
        }
        state().isProcess = true;
        stateChanged();
        session().setSessionState(state());
        long pid = getNDI().getPid();
        if (pid != -1) {
            session().setPid(pid);
        }
        //see IZ 197786, we set breakpoints here not on prog load
        profileBridge().noteAttached();

        // continue, see IZ 198495
        if (getNDI().isAutoStart() && DebuggerOption.RUN_AUTOSTART.isEnabled(optionLayers())) {
            go();
        } else {
            requestStack(null);
        }
    }

    private void doMICorefile(GdbDebuggerInfo gdi) {
        String corefile = gdi.getCorefile();
        String cmdString = "core " + corefile; // NOI18N
        /*
         * (gdb 6.2) core-file
         * ^done,frame={level="0",addr="0x080508ae",func="main",args=[],file="t.cc",line="4"},line="4",file="t.cc"
         */
        MICommand cmd =
            new MiCommandImpl(cmdString) {

            @Override
            protected void onDone(MIRecord record) {
                state().isCore = true;
                stateChanged();
                session().setSessionState(state());
                startRecordingIfNeeded();

                MICommand cmd2 = new MiCommandImpl("-thread-list-ids") { // NOI18N

                    @Override
                    protected void onDone(MIRecord record) {
                        currentThreadId = record.results().getConstValue(MI_CURRENT_THREAD);
                        requestStack(null);
                        finish();
                    }

                };

                gdb.sendCommand(cmd2);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    @Override
    public void contAt(String src, int line) {
        src = localToRemote("contAt", src); // NOI18N
        sendResumptive("-exec-jump \"" + src + ':' + line + '"'); // NOI18N
    }

    @Override
    public void contAtInst(String addr) {
        sendResumptive("-exec-jump *" + addr); // NOI18N
    }

    @Override
    public void runToCursor(String src, int line) {
        src = localToRemote("runToCursor", src); // NOI18N

        if (!state().isProcess) {
            send("-break-insert -t " + src + ":" + line); //NOI18N
            go();
        } else {
            sendResumptive("-exec-until \"" + src + ':' + line + '"'); // NOI18N
        }
    }

    // interface NativeDebugger
    @Override
    public void runToCursorInst(String addr) {
        sendResumptive("-exec-until *" + addr); //NOI18N
    }

    public GdbVersionPeculiarity getGdbVersionPeculiarity() {
        return peculiarity;
    }

    @Override
    public void pause() {
        pause(false);
    }

    private boolean pause(boolean silentStop) {
        /* LATER

        On unix, and probably in all non-embedded gdb scenarios,
        "-exec-interrupt" is not honored while running ...

        MICommand cmd =
        new MIResumpiveCommand("-exec-interrupt") {
	    protected void onRunning(MIRecord record) {
		unexpected("running", command());
	    }
        };
        gdb.sendCommand(cmd);
         */

        // ... so we interrupt
        int pid = (int) session().getPid();
        if (pid > 0) {
            return gdb.pause(pid, silentStop, targetAttach);
        }
        return false;
    }

    @Override
    public void interrupt() {
        gdb.interrupt();
    }

    // interface NativeDebugger
    @Override
    public void terminate() {
        int pid = (int) session().getPid();
        if (pid > 0) {
//            CommonTasksSupport.sendSignal(getHost().executionEnvironment(), pid, Signal.SIGKILL, null);
            MICommand command = new MiCommandImpl("kill") { // NOI18N

                @Override
                protected void onDone(MIRecord record) {
                    resetCurrentLine();

                    state().isRunning = false;
                    state().isProcess = false;
                    updateActions();

                    session().setSessionState(state());
                    session().setPid(-1);
                    session().update();

                    super.onDone(record);
                }

            };
            sendCommandInt(command);
        }
    }

    // interface NativeDebugger
    @Override
    public void detach() {
        sendCommandInt(new MiCommandImpl("detach")); //NOI18N
    }

    private class MiCommandImpl extends MICommand {
        private MICommand successChain = null;
        private MICommand failureChain = null;

        private boolean emptyDoneIsError = false;
        private boolean reportError = true;

        protected MiCommandImpl(String cmd) {
            super(0, cmd);
        }

        protected MiCommandImpl(int rt, String cmd) {
            super(rt, cmd);
        }

        public void chain(MICommand successChain, MICommand failureChain) {
            this.successChain = successChain;
            this.failureChain = failureChain;
        }

        public void setEmptyDoneIsError() {
            this.emptyDoneIsError = true;
        }

        public void dontReportError() {
            this.reportError = false;
        }

        @Override
        protected void onDone(MIRecord record) {
            if (emptyDoneIsError && record.isEmpty()) {
                // See comment for isEmpty
                onError(record);
            } else {
                finish();
                if (successChain != null) {
                    gdb.sendCommand(successChain);
                }
            }
        }

        @Override
        protected void onRunning(MIRecord record) {
            unexpected("running", command()); // NOI18N
        }

        @Override
        protected void onError(MIRecord record) {
            if (failureChain == null && reportError) {
                genericFailure(record);
            }
            finish();
            if (failureChain != null) {
                gdb.sendCommand(failureChain);
            }
        }

        @Override
        protected void onExit(MIRecord record) {
            unexpected("exit", command()); // NOI18N
            kill();
            finish();
        }

        @Override
        protected void onStopped(MIRecord record) {
            unexpected("stopped", command()); // NOI18N
        }

        @Override
        protected void onOther(MIRecord record) {
            unexpected("other", command()); // NOI18N
        }

        @Override
        protected void onUserInteraction(MIUserInteraction ui) {
            unexpected("userinteraction", command()); // NOI18N
        }
    }

    // command typed directly in terminal
    private final class TermCommandImpl extends MiCommandImpl {

        public TermCommandImpl(String cmd) {
            super(cmd);
        }

        @Override
        protected void onDone(MIRecord record) {
            // Logging output for user-typed commands
            gdb.tap().log(getConsoleStream().replaceAll("\\\\n","\r\n")); // NOI18N
        }

        @Override
        protected void onError(MIRecord record) {
            String errMsg = getErrMsg(record);
            if (!errMsg.isEmpty()) {
                gdb.tap().printError(errMsg);
            } else {
                super.onError(record);
            }
        }
    }

    /**
     * Handle the output of "info proc".
     */
    static int extractPid1(MIRecord record) {
	StringTokenizer st =
	    new StringTokenizer(record.command().
                        getConsoleStream());
        while (st.hasMoreTokens()) {
            String str = st.nextToken();
            if ("process".equals(str) && st.hasMoreTokens()) { //NOI18N
                String pidStr = st.nextToken();
                int pidEnd = 0;
                while (pidEnd < pidStr.length() && Character.isDigit(pidStr.charAt(pidEnd))) {
                    pidEnd++;
                }
                try {
                    return Integer.parseInt(pidStr.substring(0, pidEnd));
                } catch (Exception e) {
                    Exceptions.printStackTrace(new Exception("Pid parsing error: " + record.command().getConsoleStream(), e)); //NOI18N
                }
            }
        }
        return 0;
    }

    /**
     * handle output of the form
     *		~"[Switching to process 446 ...]\n"
     * which we get on Mac 10.4
     */
    private int extractPid2(String console) {
        int pid = 0;

	if (Log.Gdb.pid)
            System.out.printf("//////// '%s'\n", console);

        if (console != null) {
	    StringTokenizer st =
		new StringTokenizer(console);
            int ntokens = 0;
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
		if (Log.Gdb.pid)
                    System.out.printf("\t%d: '%s'\n", ntokens, token); // NOI18N
                if (ntokens == 3) {
                    String pidStr = token;
                    pid = Integer.parseInt(pidStr);
                    break;
                }
                ntokens++;
            }
        }

	if (Log.Gdb.pid)
            System.out.printf("\\\\\\\\ pid %d\n", pid); // NOI18N
        return pid;
    }

    private void sendPidCommand(boolean resume) {
        if (session().getPid() <= 0) {
            if (getHost().getPlatform() == Platform.Windows_x86) {
                MICommand findPidCmd = new InfoThreadsMICmd(resume);
                gdb.sendCommand(findPidCmd);
            } else if (getHost().getPlatform() != Platform.MacOSX_x86) {
                InfoProcMICmd findPidCmd = new InfoProcMICmd(resume);
                // if it fails - try "info threads"
                MICommand findPidCmd2 = new InfoThreadsMICmd(resume);
                findPidCmd.chain(null, findPidCmd2);
                gdb.sendCommand(findPidCmd);
            }
        } else if (resume) {
            go();
        }
    }

    private final class InfoThreadsMICmd extends MiCommandImpl {
        final boolean resume;

        public InfoThreadsMICmd(boolean resume) {
            super("info threads");// NOI18N
            this.resume = resume;
        }

        @Override
        protected void onDone(MIRecord record) {
            long pid = extractPidThreads(record);

            session().setSessionEngine(GdbEngineCapabilityProvider.getGdbEngineType());
            if (pid != 0) {
                session().setPid(pid);
            }

            if (resume) {
                go();	// resume
            }
            finish();
        }
    }

    static long extractPidThreads(MIRecord record) {
        String msg = record.command().getConsoleStream();
        Pattern pattern = Pattern.compile("[*]\\s+1\\s+[Tt]hread\\s+\\d+"); //NOI18N
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            String group = matcher.group();
            Pattern patternPid = Pattern.compile("\\d+$");  //NOI18N
            Matcher matcherPid = patternPid.matcher(group);
            if (matcherPid.find()) {
                try {
                    return Long.parseLong(matcherPid.group());
                } catch (NumberFormatException ex) {
                    //log.warning("Failed to get PID from \"info threads\""); // NOI18N
                }
            }
        }
        return 0;
    }

    private final class InfoProcMICmd extends MiCommandImpl {
        final boolean resume;

        public InfoProcMICmd(boolean resume) {
            super("info proc");// NOI18N
            this.resume = resume;
        }

        @Override
        protected void onDone(MIRecord record) {
            // We get something of the form
            //		process <pid> flags:

            if (Log.Gdb.pid) {
                System.out.printf("FindPidMICmd.onDone(): record: %s\n", // NOI18N
                        record);
                System.out.printf("                      command: %s\n", // NOI18N
                        record.command());
                System.out.printf("                      console: %s\n", // NOI18N
                        record.command().getConsoleStream());
            }

            int pid = extractPid1(record);

            session().setSessionEngine(GdbEngineCapabilityProvider.getGdbEngineType());
	    if (pid != 0)
                session().setPid(pid);

	    if (resume)
                go();	// resume
            finish();
        }
    }

    private volatile String firstBreakpointId = null;

    private void setFirstBreakpointId(MIRecord record) {
        MIValue bkptValue = record.results().valueOf(MI_BKPT);
        if (bkptValue != null) {
            MIValue numberValue = bkptValue.asTList().valueOf(MI_NUMBER);
            if (numberValue != null) {
                firstBreakpointId = numberValue.asConst().value();
            }
        }
    }

    @Override
    public void rerun() {
        if (true /* LATER !state.isRunning */) {
            //
            // setup to discover the processes pid
            // In effect we do this:
            // 		tbreak _start || tbreak main
            //		-exec-run
            //		# we hit the bpt
            //		info proc
            //		# -> "process 977144 flags:"
            // '_start' occurs much earlier than main, earlier than init
            // sections(?), so we try it first and if we can't find it we
            // try main. On linux stripped executables don't define
            // either so we're SOL.
            //
            // I also tried to use gdb command (analog of dbx when)
            // to issue "info proc" on the tbreak but the output of
            // "info proc" goes into the bit bucket!
            //
            // Note that the implicit resumption after a stoppage on main
            // will interfere with a normal "break main"!

	    MiCommandImpl breakStartCmd =
		new MiCommandImpl("-break-insert -t _start") { // NOI18N
                @Override
                protected void onDone(MIRecord record) {
                    setFirstBreakpointId(record);
                    super.onDone(record);
                }
            };
            breakStartCmd.setEmptyDoneIsError();

	    MiCommandImpl breakMainCmd =
		new MiCommandImpl("-break-insert -t main") { // NOI18N
                @Override
                protected void onDone(MIRecord record) {
                    setFirstBreakpointId(record);
                    super.onDone(record);
                }
            };
            breakMainCmd.setEmptyDoneIsError();

            //
            // The actual run command
            //
            MICommand runCmd =
                new MIResumptiveCommand("-exec-run") {		// NOI18N

                @Override
                protected void onRunning(MIRecord record) {
                    state().isProcess = true;
                    startRecordingIfNeeded();
                    super.onRunning(record);
                }
            };

            breakStartCmd.chain(runCmd, breakMainCmd);
            breakMainCmd.chain(runCmd, runCmd);

            // need to clear PID, see IZ 203916
            session().setPid(-1);

            // _start does not work on MacOS
            if (getHost().getPlatform() == Platform.MacOSX_x86) {
                gdb.sendCommand(breakMainCmd);
            } else {
                gdb.sendCommand(breakStartCmd);
            }
        }
    }

    @Override
    public void makeCalleeCurrent() {
        Frame frame = getCurrentFrame();
        if (frame != null) {
            int frameNo = Integer.parseInt(frame.getNumber());
            if (frameNo > 0) {
                makeFrameCurrent(getStack()[frameNo-1]);
            }
        }
    }

    @Override
    public void makeCallerCurrent() {
        Frame frame = getCurrentFrame();
        if (frame != null) {
            int newFrameNo = Integer.parseInt(frame.getNumber())+1;
            Frame[] stack = getStack();
            if (newFrameNo < stack.length) {
                makeFrameCurrent(stack[newFrameNo]);
            }
        }
    }

    @Override
    public void popToHere(Frame frame) {
        if (warnAboutReverseIfNeeded()) {
            return;
        }
        int frameNo = Integer.parseInt(frame.getNumber());
        // We have to pop the last frame several times
        // as there is no way to pop several frames at once in GDB
        while (frameNo > 0) {
            popTopmostCall();
            frameNo--;
        }
    }

    private boolean warnAboutReverseIfNeeded() {
        if (!DebuggerOption.GDB_REVERSE_DEBUGGING.isEnabled(optionLayers())) {
            NativeDebuggerManager.error(Catalog.get("MSG_Reverse_Debugging_Option"));	// NOI18N
            return true;
        }
        return false;
    }
    @Override
    public void popTopmostCall() {
        if (!warnAboutReverseIfNeeded()) {
            sendResumptive(peculiarity.execFinishCommand(currentThreadId) + " --reverse");	// NOI18N
        }
    }

    private void startRecordingIfNeeded() {
        if (DebuggerOption.GDB_REVERSE_DEBUGGING.isEnabled(optionLayers())) {
            send("-gdb-set record stop-at-limit off"); // NOI18N
            send("record"); // NOI18N
        }
    }

    @Override
    public void popLastDebuggerCall() {
    }

    @Override
    public void popToCurrentFrame() {
        popToHere(getCurrentFrame());
    }

    private static final int PRINT_REPEAT = Integer.getInteger("gdb.print.repeat", 0); //NOI18N
    private static final int STACK_MAX_DEPTH = Integer.getInteger("gdb.stack.maxdepth", 1024); // NOI18N
    private static final int PRINT_ELEMENTS = Integer.getInteger("gdb.print.elements", 400); // NOI18N
    private static final boolean ENABLE_PRETTY_PRINTING = !Boolean.getBoolean("gdb.pretty.disable"); //NOI18N

    @Override
    public FileMapper fmap() {
        return fmap;
    }

    private static String parseEnvDirFromOption(String src) {
        StringBuilder res = new StringBuilder();
        StringTokenizer st = new StringTokenizer(src, File.pathSeparator); // NOI18N
        while (st.hasMoreTokens()) {
            res.append(" \"");// NOI18N
            res.append(st.nextToken());
            res.append("\"");// NOI18N
        }
        return res.toString();
    }

    void initializeGdb(FileMapper fmap) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
            System.out.printf("GdbDebuggerImpl.initializeGdb()\n"); // NOI18N
        }

        assert isConnected() : "initializeGdb() called when gdb wasn't ready";

        // for remote always use NULL mapper
        if (getHost().isRemote()) {
            this.fmap = FileMapper.getByType(FileMapper.Type.NULL);
        } else if (fmap != null) {
            this.fmap = fmap;
        }

        // OLD overrideOptions();
        manager().initialUnsavedFiles(this);

        if (gdi.isCaptured()) {
            setCaptureState(CaptureState.INITIAL);
            setCaptureInfo(gdi.getCaptureInfo());
        } else {
            assert getCaptureState() == CaptureState.NONE;
        }

        // get supported features
        initFeatures();

        // init signals list
        if (!peculiarity.isLldb()) {
            initSignalsList();
        }

        //init global parameters
        send("-gdb-set print repeat " + PRINT_REPEAT); // NOI18N
        send("-gdb-set backtrace limit " + STACK_MAX_DEPTH); // NOI18N
        send("-gdb-set print elements " + PRINT_ELEMENTS); // NOI18N
        send("-gdb-set follow-fork-mode " + DebuggerOption.GDB_FOLLOW_FORK_MODE.getCurrValue(optionLayers())); // NOI18N
        send("-gdb-set detach-on-fork " + DebuggerOption.GDB_DETACH_ON_FORK.getCurrValue(optionLayers())); // NOI18N

        if (ENABLE_PRETTY_PRINTING) {
            sendSilent("-enable-pretty-printing"); // NOI18N
        }

        // set extra source folders
        String sourceFolders = DebuggerOption.GDB_SOURCE_DIRS.getCurrValue(optionLayers());
        if (sourceFolders != null && !sourceFolders.isEmpty()) {
            send("-environment-directory" + parseEnvDirFromOption(sourceFolders)); // NOI18N
        }

        // set terminal mode on windows, see IZ 193220
        if (getHost().getPlatform() == Platform.Windows_x86 && getIOPack().isExternal()) {
            send("set new-console"); //NOI18N
        }

        // Tell gdb what to debug
        debug(gdi);

        // Make us be the current session
        // We flip-flop to force the posting of another PROP_CURRENT_SESSION
        manager().setCurrentSession(null);
        manager().setCurrentSession(session.coreSession());
    }

    /**
     * Send any initial commands (like 'run' for Debug, or 'next' for
     * StepInto) after all initialization is done
     */
    private void initialAction() {
        if (NativeDebuggerManager.isStartModel()) {
            // OLD GdbDebuggerInfo gdi = this.getGDI();
            if (gdi != null) {
                // For load and run
                if ((gdi.getAction() & NativeDebuggerManager.RUN) != 0) {
                    rerun();
                    gdi.removeAction(NativeDebuggerManager.RUN);
                }
                // For attach
                if ((gdi.getAction() & NativeDebuggerManager.ATTACH) != 0) {

                    doMIAttach(gdi);
                    gdi.removeAction(NativeDebuggerManager.ATTACH);
                }

                // For debugging core file
                if ((gdi.getAction() & NativeDebuggerManager.CORE) != 0) {

                    doMICorefile(gdi);
                    gdi.removeAction(NativeDebuggerManager.CORE);
                }

                // For load and step
                if ((gdi.getAction() & NativeDebuggerManager.STEP) != 0) {
                    //stepOver(); // gdb 6.1
                    stepIntoMain(); // gdb 6.6
                    gdi.removeAction(NativeDebuggerManager.STEP);
                }
            }
        }
    }
    void noteProgLoaded(String progname) {

        // OLD manager().cancelProgress();
        // LATER manager().startProgressManager().cancelStartProgress();

        profileBridge().noteProgLoaded(progname);

        // SHOULD add Handler cleanup code from DbxDebuggerImpl

        // OLD overrideOptions();

        manager().formatStatusText("ReadyToRun"); // NOI18N

        NativeDebuggerManager.get().addRecentDebugTarget(progname, false);

        //need to wait until all commands go to gdb and back
        gdb.setGdbIdleHandler(new Runnable() {
            @Override
            public void run() {
                if (Log.Bpt.fix6810534) {
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            initialAction();
                        }
                    });
                } else {
                    initialAction();
                }
            }
        });
    }

    @Override
    public OptionClient getOptionClient() {
        return null;
    }

    @Override
    public String getDebuggingOption(String name) {
        notImplemented("getDebuggingOption");	// NOI18N
        return null;
    }

    @Override
    public void setOption(String name, String value) {
        notImplemented("setOption() " + name + " " + value );	// NOI18N
    }

    /*
     * RTC stuff
     * Not supported.
     */
    public void setAccessChecking(boolean enable) {
    }

    public void setMemuseChecking(boolean enable) {
    }

    public void setLeaksChecking(boolean enable) {
    }

    /**
     * Set defaults which don't match gdb defaults
     */
    private void overrideOptions() {

        //
        // The following SHOULD really be set as a side-effect of option
        // application.
        //

        /* LATER
        This seems to be available only in gdb 6.4
        Meanwhile we use the gdb cmdline option -tty
        // Arrange for gdb victims to run under the Pio
        boolean ioInWindow =
        true;

        if (executor.slaveName() != null && ioInWindow) {
        MICommand cmd =
        new MICommand("-inferior-tty-set", executor.slaveName()) {
        protected void onDone(MIRecord record) {
        finish();
        }
        protected void onRunning(MIRecord record) {
        unexpected("running", command);
        }
        protected void onError(MIRecord record) {
        finish();
        }
        protected void onStopped(MIRecord record) {
        unexpected("stopped", command);
        }
        protected void onOther(MIRecord record) {
        unexpected("other", command());
        }
        };
        gdb.sendCommand(cmd);
        }
         */
    }

    private String getErrMsg(MIRecord record) {
        String errMsg = null;

        if (record.isError()) {
            errMsg = record.error();

        } else if (!record.isEmpty()) {
            errMsg = record.results().getConstValue("msg");	// NOI18N

        } else {
            // See comment to MIRecord.isEmpty().
            if (record.command() != null) {
                errMsg = record.command().getLogStream();
            }
	    if (errMsg == null)
                errMsg = Catalog.get("MSG_UnknownFailure"); // NOI18N
            }
        return errMsg;
    }

    public String getLogger() {
        return gdb == null ? null : gdb.getLogger();
    }

    private void genericFailure(MIRecord record) {
        String errMsg = getErrMsg(record);
        manager().error(record.command().routingToken(), new GdbError(record.command().command(),errMsg), this);
    }

    private void unexpected(String what, String command) {
        System.out.println("Unexpcted callback '" + what + "' on '" + command + "'"); // NOI18N
    }

    void genericRunning() {
        final NativeSession s = session();

        clearFiredEvents();
        deleteMarkLocations();
        deliverSignal = -1;

        stateSetRunning(true);
        stateChanged();
        if (s != null) {
            s.setSessionState(state());
        }
        setStatusText(Catalog.get("MSG_running"));//NOI18N
    }

    private boolean dontKillOnExit() {
        // SHOULD factor with inline code in DbxDebuggerImpl.noteProcGone

	if (!DebuggerOption.FINISH_SESSION.isEnabled(optionLayers()) ||
	    ((gdi.getAction() & NativeDebuggerManager.LOAD) != 0)) {
            return true;
        } else {
            return false;
        }
    }

    private void noteProcGone(final String reason, final MITList results) {
        session().setPid(-1);
        session().setCorefile(null);
        session().update();
        session().setSessionEngine(null);

        state().isProcess = false;
        state().isCore = false;
        stateSetRunning(false);
        stateChanged();
        session().setSessionState(state());

        clearFiredEvents();

        String msg = "";	// NOI18N
        boolean skipkill = false;

        if ("exited-normally".equals(reason)) { // NOI18N
            final String exitcodeString = "0";		// NOI18N
            msg = Catalog.format("ProgCompletedExit", exitcodeString); // NOI18N
            skipkill = dontKillOnExit();

        } else if ("exited".equals(reason)) { // NOI18N
            final String exitcodeString = results.getConstValue("exit-code"); // NOI18N
            msg = Catalog.format("ProgCompletedExit", exitcodeString); // NOI18N
            skipkill = dontKillOnExit();

        } else if ("exited-signalled".equals(reason)) { // NOI18N
            final String signalnameString = results.getConstValue("signal-name"); // NOI18N
            msg = Catalog.format("ProgAborted", signalnameString); // NOI18N

        } else {
            msg = "Stopped for unrecognized reason: " + reason; // NOI18N
        }

        setStatusText(msg);

        if (!skipkill && NativeDebuggerManager.isStartModel()) {
            postKill();
        }

        setVisitedLocation(null);
        resetCurrentLine();
    }

    /*
     * thread stuff
     */
    private GdbThread[] threads = new GdbThread[0];
    private int current_thread_index;

    // SHOULD factor with DbxDebuggerImpl's threadsMasked
    private boolean get_threads = false; // indicate Thread View open/close

    // interface NativeDebugger
    @Override
    public boolean isMultiThreading() {
        return false;
    }

    @Override
    public void registerThreadModel(ThreadModel model) {
        if (Log.Variable.mi_threads) {
            System.out.println("registerThreadModel " + model); // NOI18N
        }
        threadUpdater.addListener(model);
        if (model != null) {
            get_threads = true;
            if (state().isProcess && !state().isRunning) {
                showThreads();
            }
        } else {
            get_threads = false;
        }
    }

    @Override
    public Thread[] getThreads() {
        return threads;
    }

    @Override
    public void makeThreadCurrent(Thread thread) {
        if (!thread.isCurrent()) {
            String tid = ((GdbThread) thread).getId();

            selectThread(-1, tid, true); // notify gdb to set current thread
        }
    }

//    private void getAllThreads(MIRecord thread) {
//        MITList threadresults = thread.results();
//        MITList thread_ids = (MITList) threadresults.valueOf("thread-ids"); // NOI18N
//
//        MIValue tid = threadresults.valueOf("number-of-threads"); // NOI18N
//
//	// assume this thread is current
//        String current_tid_no = tid.asConst().value();
//
//        if (Log.Variable.mi_threads) {
//            System.out.println("threads " + threadresults.toString()); // NOI18N
//            System.out.println("thread_ids " + thread_ids.toString()); // NOI18N
//        }
//        int size = thread_ids.size();
//
//        threads = new GdbThread[size];
//        for (int vx = 0; vx < size; vx++) {
//            MIResult thread_id = (MIResult) thread_ids.get(vx);
//            String id_no = thread_id.value().asConst().value();
//            if (Log.Variable.mi_threads) {
//                System.out.println("threads_id " + thread_id.toString()); // NOI18N
//                System.out.println("thread_ id " + id_no); // NOI18N
//            }
//	    if (id_no.equals(current_tid_no))
//		current_thread_index = vx;
//	    else
//		// collect detail thread info from gdb engine
//		selectThread(vx, id_no, false);
//        }
//
//	// do current thread the last, becuase selectThread will also set
//	// current thread
//	selectThread(current_thread_index, current_tid_no, true);
//    }

//    private void interpAttach(MIRecord threadframe) {
//        MITList threadresults = threadframe.results();
//        if (Log.Variable.mi_threads) {
//            System.out.println("threadframe " + threadresults.toString()); // NOI18N
//	}
//
//        /* no used for now, may be used for attaching multi-thread prog
//        MIValue tid = threadresults.valueOf("thread-id");
//        String tid_no = tid.asConst().value();
//        System.out.println("tid_no " + tid);
//         */
//
////        if (get_frames || get_locals) { // get new Frames for current thread
////            // would call getMILocals.
////            showStackFrames();
////        }
//
//        GdbFrame f = null;
//
//        if (threadresults.isEmpty()) {
//            f = getCurrentFrame();
//        } else {
//            MIValue frame = threadresults.valueOf(MI_FRAME); // frame entry
//            f = new GdbFrame(this, frame, null); // args data are included in frame
//        }
//
//        if (f != null && f.getLineNo() != null && !f.getLineNo().equals("")) {
//            // has source info,
//            // get full path for current frame from gdb,
//            // update source editor, and make frame as current
//            getFullPath(f);
//        }
//    }

    private void updateLocalsForSelectFrame() {
        if (get_locals) { // get local vars for current frame
            getMILocals(true);    // get local vars for current frame from gdb
        }
    }

//    private void interpCore(MIRecord threadframe) {
//        MITList threadresults = threadframe.results();
//        MIValue frame = threadresults.valueOf(MI_FRAME); // frame entry
//        GdbFrame f = new GdbFrame(this, frame, null); // args data are included in frame
//
////        if (get_frames || get_locals) {
////            // would call getMILocals.
////            showStackFrames();
////        }
//        if (!f.getLineNo().equals("")) {
//            // has source info,
//            // get full path for current frame from gdb,
//            // update source editor, and make frame as current
//            getFullPath(f);
//        }
//    }

    private void setCurrentThread(int index,
            MIRecord threadframe,
            boolean isCurrent) {

        MITList threadresults = threadframe.results();

        currentThreadId = threadresults.getConstValue("new-thread-id"); // NOI18N

        MIValue frame = threadresults.valueOf(MI_FRAME);// frame entry
        if (Log.Variable.mi_threads) {
            System.out.println("threadframe " + threadresults.toString()); // NOI18N
            System.out.println("tid_no " + currentThreadId); // NOI18N
            System.out.println("frame " + frame.toString()); // NOI18N
        }
        GdbFrame f = new GdbFrame(this, frame, null);

        //if (index != -1)
        //threads[index] = new GdbThread(this, threadUpdater, tid_no, f);
        if (isCurrent) {
//            selectFrame(f.getNumber());   // TODO check // notify gdb to change current frame
//            if (get_frames || get_locals) { // get Frames for current thread
//                // would call getMILocals.
//                showStackFrames();
//            }

            if (!f.getLineNo().equals("")) {
                // has source info,
                // get full path for current frame from gdb,
                // update source editor, and make frame as current
                getFullPath(f);
            }
            for (int tx = 0; tx < threads.length; tx++) {
                if (threads[tx].getId().equals(currentThreadId)) {
                    threads[tx].setCurrent(true);
                } else {
                    threads[tx].setCurrent(false);
                }
            }
        }
	if (isCurrent || index == threads.length-1)
            threadUpdater.treeChanged();     // causes a pull
        }

    // get detail thread info, thread id, frame, args, file, fullpath...etc.
    // also set current thread, so order of calling this method is critical
    // the last call will be the current thread.
    private void selectThread(final int index, final String id_no, final boolean isCurrent) {
        MICommand cmd =
            new MiCommandImpl("-thread-select " + id_no) { // NOI18N
            @Override
            protected void onDone(MIRecord record) {
                setCurrentThread(index, record, isCurrent);
                requestStack(record);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    private void initFeatures() {
        MiCommandImpl cmd = new MiCommandImpl("-list-features") { //NOI18N
            @Override
            protected void onDone(MIRecord record) {
                peculiarity.setFeatures(record);
                finish();
            }
        };
        cmd.dontReportError();
        gdb.sendCommand(cmd);
    }

    private void initSignalsList() {
        // Should be replaced with MI command when this functionality is supported by MI
        MiCommandImpl cmd = new MiCommandImpl("info signals") { //NOI18N
            @Override
            protected void onDone(MIRecord record) {
                String signalList = getConsoleStream();
                ((GdbDebuggerSettingsBridge)profileBridge()).noteSignalList(signalList);
                finish();
            }
        };
        cmd.dontReportError();
        gdb.sendCommand(cmd);
    }

    private void showThreads() {
        if (peculiarity.supports(GdbVersionPeculiarity.Feature.THREAD_INFO) || peculiarity.isLldb()) {
            MICommand cmd = new MiCommandImpl("-thread-info") { // NOI18N
                @Override
                protected void onDone(MIRecord record) {
                    List<GdbThread> res = new ArrayList<GdbThread>();
                    MITList results = record.results();
                    String threadId = results.getConstValue(MI_CURRENT_THREAD);
                    if (!threadId.isEmpty()) {
                        currentThreadId = threadId;
                    }
                    for (MITListItem thr : results.valueOf(MI_THREADS).asList()) {
                        MITList thrList = (MITList) thr;
                        String id = thrList.getConstValue("id"); //NOI18N
                        String name = thrList.getConstValue("target-id"); //NOI18N
                        MIValue frame = thrList.valueOf(MI_FRAME);// frame entry
                        GdbFrame f = new GdbFrame(GdbDebuggerImpl.this, frame, null);
                        f.setCurrent(true);     // in order to let Thread make some updates | GDB response contains only current frame
                        String state = thrList.getConstValue("state"); // NOI18N
                        GdbThread gdbThread = new GdbThread(GdbDebuggerImpl.this, threadUpdater, id, name, new Frame[]{f}, state);
                        if (id.equals(currentThreadId)) {
                            gdbThread.setCurrent(true);
                        }
                        res.add(gdbThread);
                    }
                    threads = res.toArray(new GdbThread[res.size()]);
                    threadUpdater.treeChanged();
                    finish();
                }
            };
            gdb.sendCommand(cmd);
        } else {
            // Workaround for older gdb versions that do not support -thread-info
            // use console "info threads" instead and put everything into name

            MICommand cmd = new MiCommandImpl("info threads") { // NOI18N
                @Override
                protected void onDone(final MIRecord record) {
                    if (peculiarity.isThreadsOutputUnusual()) {
                        MICommand cmd2 = new MiCommandImpl("info thread") { // NOI18N
                            @Override
                            protected void onDone(MIRecord record2) {
                                List<GdbThread> res = new ArrayList<GdbThread>();
                                String msg = record2.command().getConsoleStream();
                                System.out.println(msg);
                                currentThreadId = msg.substring(msg.indexOf(' ') + 1, msg.indexOf(' ', msg.indexOf(' ') + 1));  // NOI18N

                                MITList results = record.results();
                                int i = 0;
                                do {
                                    String id = "", name = null;
                                    GdbFrame f = null;

                                    MIResult result = (MIResult) results.get(i++);
                                    if (result.matches("threadno")) { //NOI18N
                                        id = IpeUtils.unquoteIfNecessary(result.value().toString());
                                    }

                                    result = (MIResult) results.get(i++);
                                    if (result.matches("target_thread_name")) { //NOI18N
                                        name = result.value().toString();

                                        result = (MIResult) results.get(i++);
                                        if (result.matches(MI_FRAME)) {
                                            MIValue frame = result.value();
                                            f = new GdbFrame(GdbDebuggerImpl.this, frame, null);
                                        }
                                    } else if (result.matches(MI_FRAME)) {
                                        name = "Thread ".concat(id); //NOI18N

                                        MIValue frame = result.value();
                                        f = new GdbFrame(GdbDebuggerImpl.this, frame, null);
                                        f.setCurrent(true);     // in order to let Thread make some updates | GDB response contains only current frame
                                    }

                                    GdbThread gdbThread = new GdbThread(GdbDebuggerImpl.this, threadUpdater, id, name, new Frame[]{f}, "");  // TODO investigate thread state for Mac OS
                                    if (id.equals(currentThreadId)) {
                                        gdbThread.setCurrent(true);
                                    }
                                    res.add(gdbThread);
                                } while (i < results.size());

                                finish();

                                threads = res.toArray(new GdbThread[res.size()]);
                                threadUpdater.treeChanged();
                                finish();
                            }
                        };

                        gdb.sendCommand(cmd2);


                    } else {
                        String msg = record.command().getConsoleStream();
                        if (msg.length() > 0) {
                            List<GdbThread> res = new ArrayList<GdbThread>();
                            StringBuilder sb = new StringBuilder();
                            boolean current = false;
                            for (String line : msg.split("\\\\n")) { // NOI18N
                                if (line.startsWith("    ")) { // NOI18N
                                    sb.append(" " + line.replace("\\n", "").trim()); // NOI18N
                                } else {
                                    if (sb.length() > 0) {
                                        GdbThread gdbThread = new GdbThread(GdbDebuggerImpl.this, threadUpdater, sb.toString());
                                        gdbThread.setCurrent(current);
                                        res.add(gdbThread);
                                        sb.delete(0, sb.length());
                                        current = false;
                                    }
                                    line = line.trim();
                                    char ch = line.charAt(0);
                                    if (ch == '*' || Character.isDigit(ch)) {
                                        current = (ch == '*');
                                        sb.append(line);
                                    }
                                }
                            }
                            if (sb.length() > 0) {
                                GdbThread gdbThread = new GdbThread(GdbDebuggerImpl.this, threadUpdater, sb.toString());
                                gdbThread.setCurrent(current);
                                res.add(gdbThread);
                            }
                            threads = res.toArray(new GdbThread[res.size()]);
                            threadUpdater.treeChanged();
                            finish();
                        }
                    }

                }
            };
            gdb.sendCommand(cmd);
        }
    }

    /*
     * debugging view stuff
     */
    private GdbThread[] threadsWithStacks = new GdbThread[0];
    private boolean get_debugging = false; // indicates Debugging View open/close

    @Override
    public void registerDebuggingViewModel(ModelListener model) {
        super.registerDebuggingViewModel(model);
        get_debugging = model != null;
        if (get_debugging && state().isProcess && !state().isRunning ) {
            requestThreadsWithStacks();
        }
    }

    private boolean showMessage = true;

    private void requestThreadsWithStacks() {
        if (peculiarity.supports(GdbVersionPeculiarity.Feature.THREAD_INFO) || peculiarity.isLldb()) {
            MICommand cmd = new MiCommandImpl("-thread-info") { // NOI18N
                @Override
                protected void onDone(final MIRecord threads) {
                    final List<GdbThread> res = new ArrayList<GdbThread>();
                    MITList results = threads.results();
                    String threadId = results.getConstValue(MI_CURRENT_THREAD);
                    Thread curThread = null;
                    if (!threadId.isEmpty()) {
                        // lldb-mi doesn't report an active thread for a single-threaded app
                        currentThreadId = threadId;
                    }

                    for (MITListItem thr : results.valueOf(MI_THREADS).asList()) {
                        MITList thrList = (MITList) thr;
                        final String id = thrList.getConstValue("id"); //NOI18N
                        final String name = thrList.getConstValue("target-id"); //NOI18N
                        final String state = thrList.getConstValue("state"); //NOI18N

                        final GdbThread gdbThread = new GdbThread(GdbDebuggerImpl.this, threadUpdater, id, name, null, state);
                        String frNo = "0"; // TODO As --thread activates a thread there will be the only value // NOI18N
                        if (id.equals(currentThreadId)) {
                            curThread = gdbThread;
                            //bz#270229 - IDE doesn't update "Call Stack" tab if this tab is not a current
                            frNo = currentStackFrameNo;
                        }
                        final String frameNo = frNo;
                        res.add(gdbThread);


                        if (peculiarity.isLldb()) {
                            // lldb-mi output for -thread-info contains frames
                            LinkedList<GdbFrame> frames = new LinkedList<GdbFrame>();
                            Iterator<MITListItem> iterator = thrList.iterator();
                            while (iterator.hasNext()) {
                                MIResult item = (MIResult) iterator.next();
                                if (item.matches(MI_FRAME)) {
                                    MIValue frame = item.value();

                                    GdbFrame gdbFrame = new GdbFrame(GdbDebuggerImpl.this, frame, gdbThread);
                                    if (gdbFrame.getNumber().equals(frameNo)) {
                                        gdbFrame.setCurrent(true);
                                    }
                                    frames.add(gdbFrame);
                                }
                            }

                            gdbThread.setStack(frames.toArray(new GdbFrame[0]));

                            if (gdbThread.isCurrent()) {    // TODO update stack in the appropriate method
                                guiStackFrames = gdbThread.getStack();
                            }

                            threadsWithStacks = res.toArray(new GdbThread[res.size()]);
                        } else {

                            if (gdbThread.isSuspended()) {

                            MICommand cmd = new MiCommandImpl(peculiarity.stackListFramesCommand(id)) {
                                @Override
                                protected void onDone(final MIRecord framesRecord) {
                                    //do not ask for arguments here
                                    //it is too time expensive and too earlier
                                    // "1" means get both arg's name and value
                                    String stack_list_args_param = 
                                            DebuggerOption.ARGS_VALUES_IN_STACK.isEnabled(NativeDebuggerManager.get().globalOptions()) ? "1" :"0";//NOI18N
                                    String args_command = "-stack-list-arguments " + stack_list_args_param + " --thread " + id; // NOI18N

                                    MICommand cmd3 = new MiCommandImpl(args_command) {

                                        @Override
                                        protected void onDone(MIRecord args) {
                                            MITList argsresults;
                                            MITList args_list = null;
                                            String stringframes;

                                            if (args != null) {
                                                argsresults = args.results();
                                                args_list = (MITList) argsresults.valueOf("stack-args"); // NOI18N
                                                stringframes = args_list.toString();
                                                if (Log.Variable.mi_frame) {
                                                    System.out.println("args_list " + stringframes); // NOI18N
                                                }
                                            }

                                            MITList results = framesRecord.results();
                                            MITList stack_list = (MITList) results.valueOf("stack"); // NOI18N
                                            int size = stack_list.size();

                                            // iterate through frame list
                                            // initialize before assign, see IZ 196318
                                            GdbFrame[] frames = new GdbFrame[size];
                                            for (int vx = 0; vx < size; vx++) {
                                                MIResult frame = (MIResult) stack_list.get(vx);

                                                // try to find frame arguments
                                                //no args here
                                                //do not ask it ever
                                                MIResult frameArgs = null;
                                                if (args_list != null && vx < args_list.size()) {
                                                    frameArgs = (MIResult) args_list.get(vx);
                                                }

                                                GdbFrame gdbFrame = 
                                                        new GdbFrame(GdbDebuggerImpl.this, frame.value(), frameArgs, gdbThread);    // TODO arguments request
                                                if (gdbFrame.getNumber().equals(frameNo)) {
                                                    gdbFrame.setCurrent(true);
                                                }
                                                frames[vx] = gdbFrame;
                                            }
                                            gdbThread.setStack(frames);

                                            threadsWithStacks = res.toArray(new GdbThread[res.size()]);
                                            debuggingViewUpdater.treeChanged(); // maybe unnecessary

                                            finish();
                                        }
                                        @Override
                                        protected void onError(MIRecord record) {
                                            onDone(null);
                                        }
                                    };
                                    gdb.sendCommand(cmd3);

                                    finish();
                                }
                            };
                            gdb.sendCommand(cmd);
                        }
                    }
                    }

                    if (curThread != null) {
                        makeThreadCurrent(curThread);
                    } else {
                        CndUtils.assertTrueInConsole(false, "curThread shouldn't be null!\n" + threads.toString()); // NOI18N
                    }

                    finish();
                }
            };
            gdb.sendCommand(cmd);
        } else {
            if (showMessage) {
            // TODO we should somehow show the message in the Debugging View itself
            NativeDebuggerManager.warning(Catalog.get("MSG_OldGdbVersion"));
            showMessage = false;
        }
    }
    }

    @Override
    public Thread[] getThreadsWithStacks() {
        return threadsWithStacks;
    }

    /*
     * stack stuff
     *
     */
    // SHOULD factor with DbxDebuggerImpl's stackMasked
    private boolean get_frames = false; // indicate Stack View open/close

    @Override
    public void registerStackModel(StackModel model) {
        if (Log.Variable.mi_frame) {
            System.out.println("registerStackModel " + model); // NOI18N
        }
        stackUpdater.addListener(model);
        get_frames = model != null;
        if (get_frames && state().isProcess && !state().isRunning ) {
            requestStack();
        }          
        
    }

    @Override
    public Frame[] getStack() {
        if (guiStackFrames == null) {
            return new GdbFrame[0];
        } else {
            return guiStackFrames;
        }
    }
    /*
    public boolean getVerboseStack() {
    return stack_verbose;
    }
     */

    @Override
    public void postPrettyPrint(boolean v) {
    }


    @Override
    public void postVerboseStack(boolean v) {
    }

    @Override
    public GdbFrame getCurrentFrame() {
        if (guiStackFrames != null) {
            for (Frame frame : guiStackFrames) {
                if (frame.isCurrent()) {
                    return (GdbFrame) frame;
                }
            }
            return (GdbFrame) guiStackFrames[0];
        }
        return null;
    }

    @Override
    public void moreFrame() {
        return;
    }

    @Override
    public void makeFrameCurrent(final Frame f) {
        final String fno = f.getNumber();
        //bz#270229 - IDE doesn't update "Call Stack" tab if this tab is not a current
        currentStackFrameNo = fno;
        boolean changed = false;
        if (guiStackFrames != null) {
            for (Frame frame : guiStackFrames) {
                if (frame.getNumber().equals(fno)) {
                    /* can't break, need it for bring back source
                    if (guiStackFrames[fx].isCurrent())
                    break;              // no change in state
                     */
                    changed = true;
                    frame.setCurrent(true);
                } else {
                    frame.setCurrent(false);
                }
            }
        }
        if (changed) {
            Runnable onDoneRunnable = new Runnable() {
                @Override
                public void run() {
                    // has source info,
                    // get full path for current frame from gdb,
                    // update source editor, and make frame as current
                    getFullPath((GdbFrame) f);

                    if (get_debugging) {    // TODO maybe better way
                        for (Thread thread : threadsWithStacks) {
                            if (thread.isCurrent()) {
                                for (Frame frame : thread.getStack()) {
                                    frame.setCurrent(frame.getNumber().equals(fno));
                                }
                            }
                        }
                        debuggingViewUpdater.treeChanged();
                    }
                    stackUpdater.treeChanged();     // causes a pull
                    disassembly.stateUpdated();
                    localUpdater.treeChanged();
                }
            };
            // selectFrame would update local vars too
            selectFrame(fno, onDoneRunnable); // notify gdb to change current frame
            return;

        }
        stackUpdater.treeChanged();     // causes a pull
        disassembly.stateUpdated();
    }

    private void visitCurrentSrc(GdbFrame f, MIRecord srcRecord) {
        MITList  srcTuple = srcRecord.results();
        if (f == null)
            f = new GdbFrame(this, null, null);

        // create a non-visited location because it may be assigned to
        // homeLoc

        MILocation l = MILocation.make(this, f.getMIframe(), srcTuple, false, getStack().length, null);

        // We really SHOULD not be setting homeLoc in a method called
        // visitBlahBlah
	if (homeLoc == null)
            homeLoc = l;		// attach scenario

        boolean visited;
        if (state().isProcess) {
            visited = ! l.equals(homeLoc);
        } else {
            visited = true;
        }
        setVisitedLocation(MILocation.make(l, visited), true);

        state().isUpAllowed = !l.bottomframe();
        state().isDownAllowed = !l.topframe();
        stateChanged();
    }

    private void getFullPath(final GdbFrame f) {
        MiCommandImpl cmd =
            new MiCommandImpl("-file-list-exec-source-file") { // NOI18N
            @Override
            protected void onDone(MIRecord record) {
                visitCurrentSrc(f, record);
                finish();
            }
        };
        cmd.dontReportError();
        gdb.sendCommand(cmd);
    }

    /*
     * notify gdb to switch current frame to fno
     * also get locals info for new current frame
     */
    private void selectFrame(final Object fno, final Runnable onDoneRunnable) {

        MICommand cmd =
            new MiCommandImpl("-stack-select-frame " + fno) { // NOI18N
            @Override
            protected void onDone(MIRecord record) {
                updateLocalsForSelectFrame();
                if (RegistersWindow.getDefault().isShowing()) {
                    requestRegisters();
                }
                finish();
                if (onDoneRunnable != null) {
                    onDoneRunnable.run();
                }
            }
        };
        gdb.sendCommand(cmd);
    }

    /*
     * framerecords: what we got from -stack-list-frames = stack
     * args: what we got from -stack-list-arguments 1 = stack-args
     */
    //mromashova_bz#269898: we will not do this call ever with the 1 (names and values)
    //it is too expensive to get all arguments for all frames even for one particular thread
    //and the only need is Debuggin View, but we will show arg names only without values
    private void setStackWithArgs(MIRecord framerecords
            , MIRecord args
    ) {
        MITList argsresults;
        MITList args_list = null;
        String stringframes;

        if (args != null) {
            argsresults = args.results();
            args_list = (MITList) argsresults.valueOf("stack-args"); // NOI18N
            stringframes = args_list.toString();
            if (Log.Variable.mi_frame) {
                System.out.println("args_list " + stringframes); // NOI18N
            }
        }


        MITList results = framerecords.results();
        MITList stack_list = (MITList) results.valueOf("stack"); // NOI18N
        int size = stack_list.size();

        // iterate through frame list
        // initialize before assign, see IZ 196318
        GdbFrame[] newGuiStackFrames = new GdbFrame[size];
        for (int vx = 0; vx < size; vx++) {
            MIResult frame = (MIResult) stack_list.get(vx);

            // try to find frame arguments
            MIResult frameArgs = null;
            if (args_list != null && vx < args_list.size()) {
                frameArgs = (MIResult) args_list.get(vx);
            }

            newGuiStackFrames[vx] = new GdbFrame(this, frame.value(), frameArgs, null);
            //bz#270229 - IDE doesn't update "Call Stack" tab if this tab is not a current
            //if (vx == 0) {
                newGuiStackFrames[vx].setCurrent((currentStackFrameNo + "").equals(newGuiStackFrames[vx].getNumber())); // make top frame current
            //}
        }
        //
        guiStackFrames = newGuiStackFrames;

        if (get_locals) {
            getMILocals(true); // "true" for gdb "var-update *" to get value update
        }

        stackUpdater.treeChanged();     // causes a pull
    }

    /*
     * get frame info: level, args
     * for whole stack
     * framerecords: what we got from -stack-list-frames
     */
    private void setStack(final MIRecord framerecords) {     
        //mromashova_bz#269898:
        // "1" means get both arg's name and value
        String stack_list_args_param = 
                get_frames && DebuggerOption.ARGS_VALUES_IN_STACK.isEnabled(NativeDebuggerManager.get().globalOptions()) 
                ? "1" :"0";//NOI18N
        
        String args_command = "-stack-list-arguments " + stack_list_args_param; // NOI18N

        MICommand cmd =
            new MiCommandImpl(args_command) {

            @Override
            protected void onDone(MIRecord record) {
                try {
                    try {
                        setStackWithArgs(framerecords, record);
                    } catch (RuntimeException x) {
                        // This can happenif we issue steps too quickly
                        // such that -stack-list-arguments comes
                        // after a -stack-list-frames but also after
                        // a resumption.
                        /*
				System.out.printf("framerecords; %s\n", framerecords);
				System.out.printf("record; %s\n", record);
                         */
                        throw x;
                    }
                } finally {
                    finish();
                }
            }

            @Override
            protected void onError(MIRecord record) {
                String errMsg = getErrMsg(record);
                if (errMsg.equals(corrupt_stack)) {
                    setStack(framerecords);
                } else {
                    setStackWithArgs(framerecords, null);
                }
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }
    static String corrupt_stack = "Previous frame identical to this frame (corrupt stack?)"; // NOI18N
    boolean try_one_more = false;

    /*
     * get frame info : level, addr, func, file
     */
//    private void showStackFrames() {
//        MICommand cmd =
//            new MiCommandImpl("-stack-list-frames") { // NOI18N
//
//            @Override
//                    protected void onDone(MIRecord record) {
//                        try_one_more = true; // success
//                        setStack(record);
//                        finish();
//                    }
//
//            @Override
//                    protected void onError(MIRecord record) {
//                        String errMsg = getErrMsg(record);
//                        // to work around gdb thread frame problem
//                        // get the real and correct output of "-stack-list-frames"
//                        // just try_one_more is not enough, so I try it for as long
//                        // as it fail, but watch out for infinite loop
//                        //if (try_one_more && errMsg.equals(corrupt_stack)) {
//                        if (errMsg.equals(corrupt_stack)) {
//                            // sometime we have timing issue that we need
//                            // to try several time, one more time is not
//                            // enough.
//                            // try_one_more = false;
//                            showStackFrames(); // try again, watch out for infinite loop
//                        } else {
//                            genericFailure(record);
//                        }
//			finish();
//                    }
//                };
//        gdb.sendCommand(cmd);
//    }

    /*
     * balloonEval stuff
     */
    @Override
    public void balloonEvaluate(Line.Part lp, String text, boolean forceExtractExpression) {
        // balloonEvaluate() requests come from the editor completely
        // independently of debugger startup and shutdown.
        if (gdb == null || !gdb.connected()) {
            return;
        }
        if (state().isProcess && state().isRunning) {
            return;
        }
        String expr;
        if (forceExtractExpression) {
            expr = EvalAnnotation.extractExpr(lp, text);
        } else {
            expr = text;
        }

        if (expr == null || expr.isEmpty()) {
            return;
        }

        final boolean dis = Disassembly.isInDisasm();
        if (dis) {
            // probably a register - append $ at the beginning
            if (Character.isLetter(expr.charAt(0))) {
                expr = '$' + expr;
            }
        }
        // disable breakpoints and signals
        modifyBreakpoints(false);
        Runnable postRunnable = new Runnable() {
            @Override
            public void run() {
                // enable breakpoints and signals
                modifyBreakpoints(true);                
            }
        };
        dataMIEval(lp, expr, dis, postRunnable, postRunnable);
    }

    private void modifyBreakpoints(boolean isEnabled) {
        //flags breakpointsStateIsChangedAndDisabled and breakpointsStateIsChangedAndEnabled
        //allows to avoid repeatable enabling/disabling  that make a little speed up for the step
        boolean wasChangedEndEnabledAlready = isEnabled && breakpointsStateIsChangedAndEnabled.getAndSet(isEnabled);
        if (isEnabled) {
            breakpointsStateIsChangedAndDisabled.set(false);
        }
        if (wasChangedEndEnabledAlready) {
            return;
        }
        boolean wasChangedEndDisabledAlready = !isEnabled && breakpointsStateIsChangedAndDisabled.getAndSet(!isEnabled);
        if (!isEnabled) {
            breakpointsStateIsChangedAndEnabled.set(false);
        }
        if (wasChangedEndDisabledAlready) { //nothing to change
            return;
        }
        send("-gdb-set unwindonsignal " + (isEnabled ? "off" : "on")); //NOI18N
        final Handler[] handlers = bm().getHandlers();
        if (handlers.length == 0) {
            return;
        }
        String breakCommand = isEnabled ? "-break-enable" : "-break-disable";//NOI18N
        if (handlers.length > 0) {
            StringBuilder command = new StringBuilder();
            command.append(breakCommand);
            for (Handler h : handlers) {
                if (h.breakpoint().isEnabled()) {
                    command.append(' ');
                    command.append(h.getId());
                }
            }
            send(command.toString());
        }
    }

    @Override
    public void evaluateInOutline(String expr) {
        // balloonEvaluate() requests come from the editor completely
        // independently of debugger startup and shutdown.
        /*if (gdb == null || !gdb.connected()) {
            return;
        }
        if (state().isProcess && state().isRunning) {
            return;
        }
        String expr;
        if (pos == -1) {
            expr = text;
        } else {
            expr = EvalAnnotation.extractExpr(pos, text);
        }

        if (expr == null || expr.isEmpty()) {
            return;
        }*/

        ModelChangeDelegator mcd = new ModelChangeDelegator();
        mcd.addListener(new ModelChangeListenerImpl());

        final GdbWatch watch = new GdbWatch(GdbDebuggerImpl.this, mcd, expr);
        try{
                        //see bz#269968 - debugger continues from unexpected breakpoint place after tooltip evaluation
                //should disable breakpoints            
            modifyBreakpoints(false);
            createMIVar(watch, false);
        } finally {
            modifyBreakpoints(true);
        }

        final Node node = new VariableNode(watch, new GdbVariableNodeChildren(watch, false));

        final ActionListener disposeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MiCommandImpl cmd = new DeleteMIVarCommand(watch);
                cmd.dontReportError();
                if (session() != null) {
                    sendCommandInt(cmd);
                }
            }
        };

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ToolTipView.getDefault().setRootElement(node).setOnDisposeListener(disposeListener).showTooltip();
            }
        });
    }

    private final class ModelChangeListenerTooltipImpl implements ModelListener {
        private AtomicBoolean isInitilaized = new AtomicBoolean(false);
        private JEditorPane editorPane;
        private  Line.Part lp;
        private ToolTipSupport tts;
        
        void registerToolTip(ToolTipSupport tts, JEditorPane editorPane, Line.Part lp) {
            this.tts = tts;
            this.editorPane = editorPane;
            this.lp = lp;
            isInitilaized.set(true);
        }
        
        @Override
        public void modelChanged(ModelEvent event) {
            //ignore
            if (!isInitilaized.get()) {
                return;
            }
            if (event instanceof ModelEvent.NodeChanged) {
                ModelEvent.NodeChanged nodeChanged = (ModelEvent.NodeChanged) event;
                if (nodeChanged.getChange() != ModelEvent.NodeChanged.DISPLAY_NAME_MASK) {
                    return;//need to update display name only
                }                
                if (!(nodeChanged.getNode() instanceof GdbVariable) ){
                    return;
                }
                final GdbVariable variable = ((GdbVariable) nodeChanged.getNode());
                if (!tts.isEnabled() || !tts.isToolTipVisible()) {
                    return;
                }
               if ( editorPane != null) {                   
                   SwingUtilities.invokeLater(new Runnable() {
                       public void run() {
                           EditorUI eui = Utilities.getEditorUI(editorPane);
                           if (eui == null) {
                               //firePropertyChange(PROP_SHORT_DESCRIPTION, null, toolTip);
                               return;
                           }
                           ToolTipUI.Expandable expandable = !(variable.isLeaf())
                                   ? new ToolTipUI.Expandable(variable.getAsText(), variable)
                                   : null;
                           ToolTipUI.Pinnable pinnable = new ToolTipUI.Pinnable(
                                   variable.getVariableName(),
                                   lp.getLine().getLineNumber(),
                                   "NativePinWatchValueProvider");   // NOI18N
                           final String toolTip = variable.getVariableName() + "=" + variable.getAsText();//NOI18N
                           ToolTipUI toolTipUI = ViewFactory.getDefault().createToolTip(toolTip, expandable, pinnable);
                           ToolTipSupport tts = toolTipUI.show(editorPane);
                           tts.addPropertyChangeListener(new ToolTipSupportPropertyChangeListener(variable));
                           variable.getUpdater().removeListener(ModelChangeListenerTooltipImpl.this);
                           //variable.getUpdater().setListener(null);
                       }
                   });
               }
            }
        }
    } 
    
    private final class ToolTipSupportPropertyChangeListener implements PropertyChangeListener {
        private final GdbVariable v;

        ToolTipSupportPropertyChangeListener(GdbVariable v) {
            this.v = v;
        }
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!ToolTipSupport.PROP_STATUS.equals(evt.getPropertyName())) {                
               return;//we are interested in status only 
            }
            if ((((Integer)evt.getNewValue()) == ToolTipSupport.STATUS_HIDDEN)){
                DeleteMIVarCommand cmd = new DeleteMIVarCommand(v);
                cmd.dontReportError();
                sendCommandInt(cmd);
                ((ToolTipSupport)evt.getSource()).removePropertyChangeListener(this);
            }
        }
        
    }
    
    private static final class ModelChangeListenerImpl implements ModelListener {
        @Override
        public void modelChanged(ModelEvent event) {
            if (event instanceof ModelEvent.NodeChanged) {
                ModelEvent.NodeChanged nodeChanged = (ModelEvent.NodeChanged) event;
                final Variable variable = ((Variable) nodeChanged.getNode());
                VariableNode.propertyChanged(variable);
            }
        }
    }

    private static final class GdbVariableNodeChildren extends VariableNodeChildren {

        public GdbVariableNodeChildren(Variable v, boolean requestChildren) {
            super(v);
            if (requestChildren) {
                ((GdbDebuggerImpl) v.getDebugger()).getMIChildren((GdbVariable) v, ((GdbVariable) v).getMIName(), 0);
            }
        }

        @Override
        protected Node[] createNodes(Variable key) {
            return new Node[]{new VariableNode(key, new GdbVariableNodeChildren(key, true))};
        }
    }

    @Override
    public void postExprQualify(String expr, QualifiedExprListener qeListener) {
    }
    
    private static String formatRegisterIfNeeded(GdbVariable v, String value_string){
        //for watches we should get format from the Output Base
        //for tooltips we should show for Disasm windows in hexadecimal format
        //but if the tooltip is pinned -> use as Output Base in Variables
        //see bz#270494 - Wrong variable values if user opens Disassembly tab 
        String format = v != null && (!v.isWatch() || 
                (v.isWatch() && (v instanceof GdbWatch) && ((GdbWatch)v).getNativeWatch() != null)) ?  v.getFormat() : 
                Disassembly.getCurrentDataRepresentationFormat().toString();
        String result = value_string;
        if (Disassembly.isInDisasm()) {
            // see #199557 we need to convert dis annotations to hex
            if (!value_string.startsWith("0x")) { //NOI18N
                try {
                    switch  (Disassembly.DATA_REPRESENTATION.valueOf(format)) {
                        case  decimal:
                            break;
                        case octal:
                            result = Address.toOctalString0x(Address.parseAddr(value_string), true);
                            break;
                        case binary:
                            result = Address.toBinaryString0x(Address.parseAddr(value_string), true);
                            break;
                        case hexadecimal:
                            result = Address.toHexString0x(Address.parseAddr(value_string), true);
                            break;
                        default:
                            result = value_string;
                    }
                    //value_string = Address.toHexString0x(Address.parseAddr(value_string), true);
                } catch (Exception e) {
                    // do nothing
                }
            }      
        }
        return result;
    }
    

    private void processMIDataEval(MIRecord record, boolean isError, final Line.Part lp,
            final String expr, final boolean dis, final Runnable postRunnable, final Runnable postRunnableOnError) {
        String value_string = "<OUT_OF_SCOPE>"; // NOI18N
        if (!isError) {
            MITList value = record.results();
            if (Log.Variable.mi_vars) {
                System.out.println("value " + value.toString()); // NOI18N
            }
            value_string = value.getConstValue("value"); // NOI18N

            if (dis) {
                value_string = formatRegisterIfNeeded(null, value_string);
            } else if (value_string.startsWith("@0x")) { //NOI18N
                try {
                    // See bug 206736 - tooltip for reference-based variable shows address instead of value
                    balloonEvaluate(lp, "*&" + expr, false); //NOI18N
                    return;
                } finally {
                    postRunnable.run();
                }
            } else {
                value_string = ValuePresenter.getValue(value_string);
            }
        } else {
            if (postRunnableOnError != null) {
                postRunnableOnError.run();
            }
            value_string = "<OUT_OF_SCOPE>"; // NOI18N
        }
        final String data_value_string = value_string;
        Line line = lp.getLine();
        DataObject dob = DataEditorSupport.findDataObject(line);
        if (dob == null) {
            postRunnable.run();
            return;
        }
        final EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
        if (ec == null) {
            postRunnable.run();
            return;
            // Only for editable dataobjects
        }
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            postRunnable.run();
            return;
        }
        final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
        if (ep == null || ep.getDocument() != doc) {
            postRunnable.run();
            return;
        }
        //Object var = null; 
        ModelChangeDelegator mcd = new ModelChangeDelegator();
        final ModelChangeListenerTooltipImpl mcLImpl = new ModelChangeListenerTooltipImpl();
        final ModelChangeDelegator watchUpdater = watchUpdater();
        watchUpdater.addListener(mcLImpl);
        final GdbWatch watch = new GdbWatch(GdbDebuggerImpl.this, watchUpdater(), expr);
        final Runnable onDoneRunnable = new Runnable() {
            @Override
            public void run() {
                postRunnable.run();
                //need to execute following code only after the value is update in onDone in createMIVar
                final Object objectVariable = watch;
                final String toolTip = expr + "=" + watch.getAsText();//NOI18N
                final String expression = expr;
                //get var
                //((GdbDebuggerImpl) watch.getDebugger()).getMIChildren((GdbVariable) watch, ((GdbVariable) watch).getMIName(), 0);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        EditorUI eui = Utilities.getEditorUI(ep);
                        if (eui == null) {
                            //firePropertyChange(PROP_SHORT_DESCRIPTION, null, toolTip);
                            return;
                        }

                        ToolTipUI.Expandable expandable = !(watch.isLeaf())
                                ? new ToolTipUI.Expandable(expression, objectVariable)
                                : null;
                        ToolTipUI.Pinnable pinnable = new ToolTipUI.Pinnable(
                                expression,
                                lp.getLine().getLineNumber(),
                                "NativePinWatchValueProvider");   // NOI18N
                        ToolTipUI toolTipUI = ViewFactory.getDefault().createToolTip(toolTip, expandable, pinnable);
                        final ToolTipSupport tts = toolTipUI.show(ep);
                        if (tts != null) {
                            tts.addPropertyChangeListener(new ToolTipSupportPropertyChangeListener(watch));
                            mcLImpl.registerToolTip(tts, ep, lp);
                        }

                    }
                });

            }
        };
        //this runnable for the case when it is not WATCH
        final Runnable onErrorRunnable = new Runnable() {
            @Override
            public void run() {
                postRunnable.run();
                if (postRunnableOnError != null) {
                    postRunnableOnError.run();
                }
                //need to execute following code only after the value is update in onDone in createMIVar
                final String toolTip = expr + "=" + data_value_string;//NOI18N
                final String expression = expr;
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        EditorUI eui = Utilities.getEditorUI(ep);
                        if (eui == null) {
                            //firePropertyChange(PROP_SHORT_DESCRIPTION, null, toolTip);
                            return;
                        }

                        ToolTipUI.Expandable expandable = null;
                        ToolTipUI.Pinnable pinnable = new ToolTipUI.Pinnable(
                                expression,
                                lp.getLine().getLineNumber(),
                                "NativePinWatchValueProvider");   // NOI18N
                        ToolTipUI toolTipUI = ViewFactory.getDefault().createToolTip(toolTip, expandable, pinnable);
                        final ToolTipSupport tts = toolTipUI.show(ep);
                        if (tts != null) {
                            tts.addPropertyChangeListener(new ToolTipSupportPropertyChangeListener(watch));
                            mcLImpl.registerToolTip(tts, ep, lp);
                        }

                    }
                });
            }
        };
        //the Runnable below will be executed for real watch only
        //but what if we want to get expression evaluation
        createMIVar(watch, false, onDoneRunnable, onErrorRunnable);
    }
    

    private void dataMIEval(final Line.Part lp, final String expr, final boolean dis, final Runnable postRunnable, final Runnable postRunnableOnError) {
        String expandedExpr = MacroSupport.expandMacro(this, expr);
        String cmdString = "-data-evaluate-expression " + "\"" + expandedExpr + "\""; // NOI18N

        MICommand cmd =
            new MiCommandImpl(cmdString) {
            @Override
            protected void onDone(MIRecord record) {
                processMIDataEval(record, false, lp, expr, dis, postRunnable, postRunnableOnError);
                finish();
            }

            @Override
            protected void onError(MIRecord record) {
                // Be silent on balloon eval failures
                processMIDataEval(record, true, lp, expr, dis, postRunnable, postRunnableOnError);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    /*
     * watch stuff
     */
    private boolean get_watches = false; // indicate Watch View open/close

    public void postCreateWatch(int routingToken, NativeWatch newWatch) {
    }

    @Override
    public boolean watchError(int rt, Error error) {
        return false;
    }

    // interface NativeDebugger
    @Override
    public void replaceWatch(NativeWatch original, String replacewith) {
        // remove the original
        original.postDelete(false);
        // create a new one base on replacewith
        manager().createWatch(replacewith.trim());
        return;
    }

    // interface NativeDebuggerImpl
    @Override
    protected void restoreWatch(NativeWatch template) {
        // We don't create watches on the gdb side so there's
        // nothing to post. Instead we use MI-based "var"s and scan
        // through them on our own.

        // See DbxDebuggerImpl.newWatch() and dupWatch() for comparison

        NativeWatch nativeWatch = template;

        // see IZ 194721
        // No need to check for duplicates - gdb will create different vars
        GdbWatch gdbWatch = new GdbWatch(this, watchUpdater(), nativeWatch.getExpression());
        if (get_watches || WatchBag.isPinOpened(nativeWatch)) {
            try{
                //see bz#269968 - debugger continues from unexpected breakpoint place after tooltip evaluation
                //should disable breakpoints
                modifyBreakpoints(false);
                createMIVar(gdbWatch, true);
                //do not call update if there is no editor opened or watches are opened
//            if (get_watches || WatchBag.isPinOpened(nativeWatch)) {
                updateMIVar();
                //}
            } finally {
                modifyBreakpoints(true);
            }
            
        }
        nativeWatch.setSubWatchFor(gdbWatch, this);
        watches.add(gdbWatch);
        manager().bringDownDialog();
        if (get_watches) {
            watchUpdater().treeChanged();     // causes a pull
    }       
    }

    @Override
    public void postDeleteAllWatches() {
        // no-op
    }

    @Override
    public void postDeleteWatch(final WatchVariable variable,
            final boolean spreading) {

        if (!(variable instanceof GdbWatch)) {
            return;
        }
        GdbWatch watch = (GdbWatch) variable;

        if (watch.getMIName() == null) {
            deleteWatch(variable, spreading);
        } else {
            MICommand cmd = new DeleteMIVarCommand(watch) {
                @Override
                protected void onDone(MIRecord record) {
                    super.onDone(record);
                    deleteWatch(variable, spreading);
                }

                @Override
                protected void onError(MIRecord record) {
                    super.onDone(record);
                    deleteWatch(variable, spreading);
                }
            };
            sendCommandInt(cmd);
        }
    }

    public void postDynamicWatch(Variable variable) {
        // TODO does gdb/MI support this ?
    }

    public void postInheritedWatch(Variable watch) {
        // TODO does gdb/MI support this ?
    }

    public void deleteVar(Variable var, MIRecord record) {
        variableBag.remove_count = 0;
        variableBag.remove(var);
        if (Log.Variable.mi_vars) {
            System.out.println("variableBag.remove_count " + // NOI18N
                    variableBag.remove_count);
        }
        variableBag.remove_count = 0;
    }

    @Override
    public void registerWatchModel(WatchModel model) {
        if (Log.Variable.mi_vars) {
            System.out.println("registerWatchModel " + model); // NOI18N
        }
        watchUpdater().addListener(model);
        if (model != null) {
            get_watches = true;
            if (state().isProcess && !state().isRunning) {
                updateWatches(true);
            }
        } else {
            get_watches = false;
        }
    }

    /**
     * Try and re-create vars for watches which don't have var's (mi_name's)
     * yet.
     */
    private void retryWatches(boolean forceVarCreate) {
        for (WatchVariable wv : watches) {
            GdbWatch w = (GdbWatch) wv;
            if (forceVarCreate || w.getNativeWatch().watch().getPin() != null) {
                // due to the fix of #197053 it looks safe not to create new vars
                if (w.getMIName() != null) {
                    continue;		// we already have a var for this one
                }

                createMIVar(w, true);
            }
        }
    }

    private void updateWatches(boolean forceVarCreate) {
        try{
            //see bz#269968 - debugger continues from unexpected breakpoint place after tooltip evaluation
            //should disable breakpoints
            modifyBreakpoints(false);
            retryWatches(forceVarCreate);
            if (forceVarCreate) {
                updateMIVar();
            }
        } finally {
           modifyBreakpoints(true);
        }            
    }

    private void updateVarAttr(GdbVariable v, MIRecord attr, boolean evalValue) {
        MITList attr_results = attr.results();
        String value = attr_results.getConstValue("attr"); // NOI18N
        v.setEditable(value);
        if (v.isEditable() && evalValue) {
            evalMIVar(v);
        }
    }

    private void updateStringValue(final GdbVariable v) {
        if (!ValuePresenter.acceptsType(v.getType())) { //NOI18N
            return;
        }
        MiCommandImpl cmd = new MiCommandImpl("-data-evaluate-expression \"" + v.getFullName() + '\"') { //NOI18N
            @Override
            protected void onDone(MIRecord record) {
                updateValue(v, record, false);
                super.onDone(record);
            }
        };
        cmd.dontReportError();
        gdb.sendCommand(cmd);
    }

    public static final String STRUCT_VALUE = "{...}"; // NOI18N

    private void updateValue(final GdbVariable v, MIRecord varvalue, boolean pretty) {
        MITList value_results = varvalue.results();
        MIValue miValue = value_results.valueOf("value"); //NOI18N
        updateValue(v, miValue, pretty);
    }

    private void updateValue(final GdbVariable v, MIValue miValue, boolean pretty) {
        boolean valueChanged = false;
        String value = null;
        if (miValue != null) {
            value = miValue.asConst().value();
        }
        value = processValue(value);
        if (!pretty) {
            value = ValuePresenter.getValue(value);
        }
        value = formatRegisterIfNeeded(v, value);
        valueChanged = !value.equals(v.getAsText());
        v.setAsText(value);

        // pretty printer for string type
        if (pretty) {
            updateStringValue(v);
        }        
        if (v.isWatch()) {
            watchUpdater().treeNodeChanged(v); // just update this node
            if (WatchVariable.class.isAssignableFrom(v.getClass())) {
                final NativeWatch nativeWatch = ((WatchVariable) v).getNativeWatch();
                if (nativeWatch != null && nativeWatch.watch().getPin() != null) {
                    NativeDebuggerManager.get().firePinnedWatchChange(this, nativeWatch.watch());
                }
            }
            if (valueChanged) {
                v.getUpdater().treeNodeChanged(v, ModelEvent.NodeChanged.DISPLAY_NAME_MASK);
            }
        } else {
            localUpdater.treeNodeChanged(v); // just update this node
        }
    }

    private static String processValue(String value) {
        if (value == null) {
            return STRUCT_VALUE;
        } else if (value.startsWith("[") && value.endsWith("]")) { //NOI18N
            // detect arrays, see IZ 192927
            return STRUCT_VALUE;
        }
        return value;
    }

    private void interpMIChildren(GdbVariable parent,
            MIRecord miRecord,
            int level) {
        MITList results = miRecord.results();

        parent.setNumChild(results.getConstValue(MI_NUMCHILD));
        parent.setHasMore(results.getConstValue(GdbVariable.HAS_MORE));

        // workaround for lldb-mi:
        // in case numchild==0 it responses like 'children="[]"' instead of 'children=[]'
        MITList children_list = null;
        MIValue childrenValue = results.valueOf("children"); // NOI18N
        if (childrenValue instanceof MITList) {
            children_list = (MITList) childrenValue;
        }
        int maxIndexLog = log10(parent.getNumChild());
        // iterate through children list
        List<GdbVariable> children = new ArrayList<GdbVariable>();
        if (children_list != null) {
            int childIdx = 0;
            for (MITListItem childresult : children_list) {
                final MITList childResList = ((MIResult)childresult).value().asTuple();

                // full qualified name,
                // e.g. "var31.public.proc.private.p_proc_heap"
                String qname = childResList.getConstValue("name"); // NOI18N
                // display name,
                // e.g. "p_proc_heap"
                String exp = childResList.getConstValue(MI_EXP);

                if (exp.equals("private") || exp.equals("public") || // NOI18N
                        exp.equals("protected")) { // NOI18N
                    getMIChildren(parent, qname, level+1);
                } else {
                    if (parent.isDynamic() && parent.getDisplayHint() == GdbVariable.DisplayHint.MAP) {
                        // in pretty maps even element is a key, odd is a value
                        exp = (childIdx % 2 == 0) ? Catalog.format("Map_Key", childIdx / 2) : Catalog.format("Map_Value", childIdx / 2); // NOI18N
                        childIdx++;
                    } else {
                        // Show array name and index instead of only index, IZ 192123
                        //substring first
                        String indexStr = exp;
                        if (exp.startsWith("[") && exp.endsWith("]")) { // NOI18N
                            indexStr = exp.substring(1, exp.length() - 1);
                        }
                        try {
                            int index = Integer.parseInt(indexStr);
                            //add space
                            indexStr = parent.getVariableName() + getIndexStr(maxIndexLog, index);
                        } catch (Exception e) {
                            // do nothing
                        }
                        exp = indexStr;
                    }
                    GdbVariable childvar = new GdbVariable(this, parent.getUpdater(),
                            parent, exp, null, null, parent.isWatch());

                    String value = childResList.getConstValue("value"); // NOI18N

                    value = processValue(value);
                    childvar.setAsText(value);

                    childvar.populateFields(childResList);

                    variableBag.add(childvar);
                    children.add(childvar);
                    attrMIVar(childvar, false);
                }
            }
        }

        // make a pull to update children's value
        GdbVariable[] vars = new GdbVariable[children.size()];
        if (level == 0) {
            parent.setChildren(children.toArray(vars), true);
        } else {
            parent.addChildren(children.toArray(vars), true);
        }

        // make a pull to update children's value
        // parent.setChildren(childrenvar, true);
    }

    private static String getIndexStr(int maxIndexLog, int index) {
        return getIndexStr(maxIndexLog, index, ""); // NOI18N
    }

    private static String getIndexStr(int maxIndexLog, int index, String postfix) {
        int num0 = maxIndexLog - log10(index);
        String data = index + postfix;
        if (num0 > 0) {
            data = zeros(num0) + data;
        }
        return "[" + data + "]"; // NOI18N
    }

    private static int log10(int n) {
        int l = 1;
        while ((n = n / 10) > 0) {
            l++;
        }
        return l;
    }

    private static final String ZEROS = "            "; // NOI18N

    static String zeros(int n) {
        if (n < ZEROS.length()) {
            return ZEROS.substring(0, n);
        } else {
            String z = ZEROS;
            while (z.length() < n) {
                z += " "; // NOI18N
            }
            return z;
        }
    }

    /**
     * process a -var-update command
     */
    private void interpUpdate(MIRecord var) {
        MITList varsresults = var.results();
        MITList update_list = (MITList) varsresults.valueOf("changelist"); // NOI18N
        if (Log.Variable.mi_vars) {
            System.out.println("update_list " + update_list.toString()); // NOI18N
        }

        // iterate through update list
        for (MITListItem item : update_list) {
            MIValue updatevar;

            // On the Mac a 'changelist' is a list of results not values
            if (update_list.isResultList()) {
		MIResult result = (MIResult)item;
                CndUtils.assertTrue(result.variable().equals("varobj"), "Erroneous response:" + var.toString()); // NOI18N
                updatevar = result.value();
            } else {
		updatevar = (MIValue)item;
            }
            //see bz#248868 as we can get segmentation faults while watches evaluation
            if (updatevar == null) {
                //ignore
                return;//
            }
            String mi_name = updatevar.asTuple().getConstValue("name"); // NOI18N
            String in_scope = updatevar.asTuple().getConstValue("in_scope"); // NOI18N
            if (Log.Variable.mi_vars) {
                System.out.println("update name " + mi_name + " in_scope " + in_scope); // NOI18N
            }
            /* not used
            MIValue type_changed_entry = updatevar.asTuple().valueOf("type_changed");
            String type_changed;
            if (type_changed_entry != null)
            type_changed = type_changed_entry.asConst().value();
             */
//            if (in_scope != null && in_scope.equals("true")) { // NOI18N
//                Variable wv = variableBag.get(mi_name, true, VariableBag.FROM_BOTH);
//                if (wv != null) {
//                    evalMIVar(wv);
//                }
//            }
            GdbVariable wv = variableBag.get(mi_name, true, VariableBag.FROM_BOTH);
            if (wv != null) {
                wv.populateUpdate(updatevar.asTuple(), variableBag);

                // update value
                if (updatevar.asTuple().valueOf("value") != null) { //NOI18N
                    updateValue(wv, updatevar.asTuple().valueOf("value"), true); //NOI18N
                } else if (in_scope == null || in_scope.equalsIgnoreCase("true")){  //NOI18N
                    evalMIVar(wv);
                }
            }
        }
    }


    /*
     * dynamic type is not supported in MI (base on gdb6.6)
     * this Gdb command won't take effected for MI output
     * e.g. if issues "-var-list-children --all-values var34.protected"
     * after turn on dynamic type, the children of var34.protected
     * won't get members of dynamic type.
     * Can be used for future when MI support dynamic type.
     */
    // interface NativeDebugger
    @Override
    public void setDynamicType(boolean b) {
        String cmdString;
        if (b) {
            cmdString = "-gdb-set print object on";			// NOI18N
        } else {
            cmdString = "-gdb-set print object off";			// NOI18N
        }
        send(cmdString);
        dynamicType = b;
    }

    // interface NativeDebugger
    @Override
    public boolean isDynamicType() {
        return dynamicType;
    }

    // interface NativeDebugger
    @Override
    public boolean isStaticMembers() {
        return true; // always show static members
    }

    // interface NativeDebugger
    @Override
    public void setStaticMembers(boolean b) {
        // no-op
        // GDB TODO
    }

    // interface NativeDebugger
    @Override
    public boolean isPrettyPrint() {
        return true; // always use pretty-print
    }

    // interface NativeDebugger
    @Override
    public void setPrettyPrint(boolean b) {
        // no-op
        // GDB TODO
    }

    // interface NativeDebugger
    @Override
    public boolean isInheritedMembers() {
        return true; // always show inherited members
    }

    // interface NativeDebugger
    @Override
    public void setInheritedMembers(boolean b) {
        // no-op
        // GDB TODO
    }

    // interface NativeDebugger
    @Override
    public String[] formatChoices() {
        List<String> formats = Stream.of(Disassembly.DATA_REPRESENTATION.values())
                .map(Enum::name)
                .collect(Collectors.toList());
	return formats.toArray(new String[0]);
    }
    
    private void interpVarFormat(GdbVariable v, MIRecord record) {
        MITList format_results = record.results();
        String format = format_results.getConstValue("format"); // NOI18N
        v.setFormat(format);
        evalMIVar(v);
    }

    void postVarFormat(final GdbVariable v, String format) {
        String expr = v.getMIName();
        // update variable output format
        String cmdString = "-var-set-format " + expr + " " + format; // NOI18N
        MICommand cmd =
                new MiCommandImpl(cmdString) {

            @Override
            protected void onDone(MIRecord record) {
                interpVarFormat(v, record);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    private void interpVar(GdbVariable v, MIRecord var) {
        v.populateFields(var.results());

        Variable wv = variableBag.get(v.getMIName(), true, VariableBag.FROM_BOTH);
        if (wv == null) {
            variableBag.add(v);
        }
        attrMIVar(v, true);
    }

    private void attrMIVar(final GdbVariable v, final boolean evalValue) {
        // see IZ 197562, on MacOSX -var-show-attributes on invalid watch breaks gdb
        if (v.getNumChild() == -1) {
            return;
        }
        String expr = v.getMIName();
        // editable ?
        String cmdString = peculiarity.showAttributesCommand(expr);
        MICommand cmd =
            new MiCommandImpl(cmdString) {
            @Override
            protected void onDone(MIRecord record) {
                updateVarAttr(v, record, evalValue);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    // common method for lldb-mi
    private void updateMIVar(GdbVariable var) {
        String cmdString = "-var-update --all-values " + var.getMIName(); // NOI18N
        MICommand cmd =
            new MiCommandImpl(cmdString) {

            @Override
            protected void onDone(MIRecord record) {
                interpUpdate(record);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    private void updateMIVar() {
        //enable/disable breakpoint earlier if neeed to avoid multiple enabling/disabling
        //ideally it would good idea to introduce something similar to transaction with start and end actions
        if (!peculiarity.isLldb()) {
            String cmdString = "-var-update --all-values * "; // NOI18N
            MICommand cmd =
                    new MiCommandImpl(cmdString) {

                @Override
                protected void onDone(MIRecord record) {
                    interpUpdate(record);
                    finish();
                }

                @Override
                protected void onError(MIRecord record) {
                    String errMsg = getErrMsg(record);

                    // to work around gdb "corrupt stack" problem
                    if (try_one_more && errMsg.equals(corrupt_stack)) {
                        try_one_more = true;
                        //updateMIVar();
                    }
                    // to work around gdb "out of scope" problem
                    String out_of_scope = "mi_cmd_var_assign: Could not assign expression to varible object"; // NOI18N
                    if (!errMsg.equals(out_of_scope)) {
                        genericFailure(record);
                        finish();
                    }
                }
            };

            gdb.sendCommand(cmd);
        }

        // update string values
        Variable[] list = isShowAutos() ? getAutos() : getLocals();
        for (Variable var : list) {
            if (var instanceof GdbVariable) {
                // TODO: MI name should be always available by this moment
                if (peculiarity.isLldb() && ((GdbVariable) var).getMIName() != null) {
                    updateMIVar((GdbVariable) var);
                }
                updateStringValue((GdbVariable)var);
            }
        }

        for (WatchVariable var : getWatches()) {
            if (var instanceof GdbVariable) {
                // TODO: MI name should be always available by this moment
                if (peculiarity.isLldb() && ((GdbVariable) var).getMIName() != null) {
                    updateMIVar((GdbVariable) var);
                }
                updateStringValue((GdbVariable)var);
            }
        }

        // TODO maybe better place
        if (peculiarity.isLldb()) {
            localUpdater.treeChanged();
        }
    }

    private void evalMIVar(final GdbVariable v) {
        final String mi_name = v.getMIName();
        // value of mi_name
        final String cmdString = "-var-evaluate-expression " + mi_name; // NOI18N
        final MICommand cmd =
            new MiCommandImpl(cmdString) {

            @Override
            protected void onDone(MIRecord record) {
                updateValue(v, record, true);
                finish();
            }

            @Override
            protected void onError(MIRecord record) {
                String errMsg = getErrMsg(record);

                // to work around gdb "out of scope" problem
                String out_of_scope = "mi_cmd_var_assign: Could not assign expression to varible object"; // NOI18N
                if (!errMsg.equals(out_of_scope)) {
                    //see bz#271342
                    if (errMsg.equals("Variable object not found") && !variableBag.hasVariable(mi_name)) {//NOI18N
                        //log it, nothing to evaluate
                        System.err.println("var=" + v.getVariableName() + " cmd=\"" + cmdString + "\"" + " err=\"" + errMsg + "\"");//NOI18N
                        return;
                    }
                    genericFailure(record);
                    finish();
                }
            }
        };
        gdb.sendCommand(cmd);
    }
    
    private class DeleteMIVarCommand extends MiCommandImpl {

        private final GdbVariable v;

        public DeleteMIVarCommand(GdbVariable v) {
            super("-var-delete " + v.getMIName()); // NOI18N
            this.v = v;
        }

        @Override
        protected void onDone(MIRecord record) {
            deleteVar(v, record);
            finish();
        }
    }

    /*
     * this MI call would create MI Vars for each child automatically by gdb
     */
    void getMoreMIChildren(final GdbVariable parent,
            String expr,
            final int level) {

        String cmdString = peculiarity.listChildrenCommand(expr, parent.getChildrenRequestedCount(), parent.incrementChildrenRequestedCount());
        MiCommandImpl cmd = new MiCommandImpl(cmdString) {
            @Override
            protected void onDone(MIRecord record) {
                interpMIChildren(parent, record, level);
                finish();
            }
        };
        cmd.dontReportError();
        gdb.sendCommand(cmd);
    }

    void getMIChildren(final GdbVariable parent,
            String expr,
            final int level) {
        parent.resetChildrenRequestedCount();
        getMoreMIChildren(parent, expr, level);
    }
    
    private void createMIVar(final GdbVariable v, boolean expandMacros) {
        createMIVar(v, expandMacros, null, null);
    }

    private void createMIVar(final GdbVariable v, boolean expandMacros, final Runnable onDoneRunnable,
            final Runnable onErrorRunnable) {
        String expr = v.getVariableName();
        if (expandMacros) {
            expr = MacroSupport.expandMacro(this, v.getVariableName());
        }       
        String cmdString = peculiarity.createVarCommand(expr, currentThreadId, currentStackFrameNo); // NOI18N //use current frame number
        MICommand cmd =
            new MiCommandImpl(cmdString) {

            @Override
            protected void onDone(MIRecord record) {
                v.setAsText("{...}");// clear any error messages // NOI18N
                v.setInScope(true);
                interpVar(v, record);
                updateValue(v, record, true);
                //when we create var if it is register(starts with $ and we are in disasm windows)
                //run var-set-format var11 hexadecimal (where hexadecimal is current format from Disassembly.getCurrentDataRepresentationFormat
                //if we are in disasm and the variable name starts with $ - consider it as a register - format
                if (Disassembly.isInDisasm() && v.getVariableName().startsWith("$")) {//NOI18N
                    postVarFormat(v, Disassembly.getCurrentDataRepresentationFormat().name());
                }
                finish();
                if (onDoneRunnable != null) {
                    onDoneRunnable.run();
                }
            }

            @Override
            protected void onError(MIRecord record) {
                // If var's eing created for watches cannot be parsed
                // we get an error.
                String errMsg = getErrMsg(record);
                v.setAsText(errMsg);
                v.setInScope(false);
                finish();
                if (onErrorRunnable != null) {
                    onErrorRunnable.run();
                }                
                watchUpdater().treeChanged();     // causes a pull

            }
        };
        gdb.sendCommand(cmd);
    }

    /*
     * local stuff
     */
    // SHOULD factor with DbxDebuggerImpl's localsMasked
    private boolean get_locals = false; // indicate Locals View open/close
    private GdbVariable[] local_vars = new GdbVariable[0];

    @Override
    public void registerLocalModel(LocalModel model) {
        if (Log.Variable.mi_vars) {
            System.out.println("registerLocalModel " + model); // NOI18N
        }
        localUpdater.addListener(model);
        if (model != null) {
            get_locals = true;
            if ((state().isProcess || state().isCore) && !state().isRunning) {
                // have frame args already
                getMILocals(true); // from current frame
            }
        } else {
            get_locals = false;
        }
    }

    @Override
    public Variable[] getLocals() {
        return local_vars;
    }

    @Override
    public int getLocalsCount() {
        return local_vars.length;
    }

    @Override
    public Set<String> requestAutos() {
        Set<String> autoNames = super.requestAutos();
        LinkedList<Variable> res = new LinkedList<Variable>();
        if (autoNames != null) {
            for (String auto : autoNames) {
                GdbVariable var = variableBag.get(auto, false, VariableBag.FROM_BOTH);
                if (var == null) {
                    var = new GdbVariable(this, localUpdater, null, auto, null, null, false);
                    createMIVar(var, true);
                }
                res.add(var);
            }
        } else {
            res = null;
        }

        synchronized (autos) {
            autos.clear();
            if (res == null) {
                autos.add(null);
            } else {
                autos.addAll(res);
            }
        }

        return autoNames;
    }

    @Override
    public void setShowAutos(boolean showAutos) {
        super.setShowAutos(showAutos);
        if (gdb != null && gdb.connected()) {
            if (showAutos) {
                requestAutos();
            }
        }
    }

    /*
     * update local vars, include paramaters
     *
     */
    private void setLocals(boolean update_var, MIRecord locals) {
        MITList localsresults = locals.results();
        MITList locals_list = (MITList) localsresults.valueOf("variables"); // NOI18N
        int size = locals_list.size();
        int local_count = size;

        List<GdbLocal> param_list = null;
        int params_count = 0;
        
        //mromashova_bz#269898: we will use "-stack-list-variables --no-values" not "-stack-list-locals --no-values"
        //and frame will never keep args
//        // paramaters
//        GdbFrame cf = getCurrentFrame();
//        if (cf != null) {
//            //ask for the args as we will not ask now
//            param_list = cf.getArgsList();
//            if (param_list != null) {
//                params_count = param_list.size();
//            }
//        }

        local_count += params_count;
        if (Log.Variable.mi_vars) {
            System.out.println("locals " + locals_list.toString()); // NOI18N
            System.out.println("args " + param_list.toString()); // NOI18N
            System.out.println("local_count " + local_count); // NOI18N
            System.out.println("update_var " + update_var); // NOI18N
        }
        try{
            modifyBreakpoints(false);
            // iterate through local list
            GdbVariable[] new_local_vars = new GdbVariable[local_count];
            for (int vx = 0; vx < size; vx++) {
                MITListItem localItem = locals_list.get(vx);
                if (peculiarity.isLocalsOutputUnusual()) {
                    localItem = ((MITList) localItem).get(0);
                }
                if (localItem instanceof MITList) {
                    localItem = ((MITList) localItem).get(0);
                }
                MIResult localvar = (MIResult) localItem;
                String var_name = localvar.value().asConst().value();
                GdbVariable gv = variableBag.get(var_name,
                        false, VariableBag.FROM_LOCALS);
                if (gv == null) {
                    new_local_vars[vx] = new GdbVariable(this, localUpdater, null,
                            var_name, null, null, false);
                    createMIVar(new_local_vars[vx], false);
                } else {
                    new_local_vars[vx] = gv;
                    evalMIVar(new_local_vars[vx]);
                }
            }

            // iterate through frame arguments list
            for (int vx = 0; vx < params_count; vx++) {
                GdbLocal loc = param_list.get(vx);
                String var_name = loc.getName();
                String var_value = loc.getValue();

                GdbVariable gv = variableBag.get(var_name, false, VariableBag.FROM_LOCALS);
                if (gv != null) {
                    gv.setValue(var_value); // update value
                    new_local_vars[size + vx] = gv;
                } else {
                    new_local_vars[size + vx] = new GdbVariable(this, localUpdater,
                            null, var_name, loc.getType(), loc.getValue(), false);
                    createMIVar(new_local_vars[size + vx], false);
                }
            }
            // need to update local_vars with fully filled array
            local_vars = new_local_vars;

            if (update_var) {
                updateMIVar(); // call var-update * , but results are not reliable
            }
            localUpdater.treeChanged();     // causes a pull
        } finally {
            modifyBreakpoints(true);
        }
    }

    private void getMILocals(final boolean update_var) {
        MICommand cmd =
            new MiCommandImpl(peculiarity.stackListLocalsCommand()) {
            @Override
            protected void onDone(MIRecord record) {
                setLocals(update_var, record);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    private void getMIDis(String command) {
        MICommand cmd =
            new MiCommandImpl(command) {
            @Override
            protected void onDone(MIRecord record) {
                setDis(record);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    /**
     * Continuation from genericStopped().
     *
     * We get here on a generic stop and after a success or failure of
     * This is not compitable with  "-file-list-exec-source-file" anymore
     * On failure srcRecord is null.
     * "-stack-info-frame". On failure srcRecord is null.
     *
     */
    private void genericStoppedWithSrc(MIRecord record, MIRecord srcRecord) {
        final MITList srcResults = (srcRecord == null) ? null : srcRecord.results();
        MITList results = (record == null) ? null : record.results();
        // make results null if empty to avoid later checks, IZ194272
        if (results != null && results.isEmpty()) {
            results = null;
        }
        final MIValue reasonValue = (results == null) ? null : results.valueOf("reason"); // NOI18N
        final String reason;
        if (reasonValue == null) {
            reason = "breakpoint-hit"; // temp bpt hit // NOI18N
        } else {
            reason = reasonValue.asConst().value();
        }

        if (reason.equals("exited-normally")) { // NOI18N
            noteProcGone(reason, results);
            return;

        } else if (reason.equals("exited")) { // NOI18N
            noteProcGone(reason, results);
            return;

        } else if (reason.equals("exited-signalled")) { // NOI18N
            noteProcGone(reason, results);
            return;

        } else if (reason.equals("breakpoint-hit") || // NOI18N
            reason.equals("end-stepping-range") || // NOI18N
            reason.equals("location-reached") || // NOI18N
            reason.equals("signal-received") || // NOI18N
            reason.equals(MI_WATCHPOINT_TRIGGER) || //NOI18N
            reason.equals(MI_WATCHPOINT_SCOPE) || //NOI18N
            reason.equals(MI_SYSCALL_ENTRY) || //NOI18N
            reason.equals(MI_SYSCALL_RETURN) || //NOI18N
                reason.equals("function-finished")) { // NOI18N
            //mark as not running once we are stopped
            state().isRunning = false;
            // update our views
            NativeBreakpoint breakpoint = null;
            MIValue bkptnoValue = (results != null) ? results.valueOf("bkptno") : null; // NOI18N
            if (bkptnoValue != null) {
                // It's a breakpoint event
                String bkptnoString = bkptnoValue.asConst().value();
                int bkptno = Integer.parseInt(bkptnoString);
                Handler handler = bm().findHandler(bkptno);
                if (handler != null) {
                    handler.setFired(true);
                    breakpoint = handler.breakpoint();
                }
                // updateFiredEvent will set status
            }

            // watchpoint if any
            MIValue wptValue = (results != null) ? results.valueOf(MI_WPT) : null; // NOI18N
            if (wptValue != null) {
                // It's a watchpoint event
                String bkptnoString = wptValue.asList().getConstValue(MI_NUMBER);
                int bkptno = Integer.parseInt(bkptnoString);
                Handler handler = bm().findHandler(bkptno);
                if (handler != null) {
                    handler.setFired(true);
                    breakpoint = handler.breakpoint();
                }
            }

            // watchpoint to disable
            MIValue wpnumValue = (results != null) ? results.valueOf("wpnum") : null; // NOI18N
            if (wpnumValue != null) {
                // It's a watchpoint scope event
                String bkptnoString = wpnumValue.asConst().value();
                int bkptno = Integer.parseInt(bkptnoString);
                Handler handler = bm().findHandler(bkptno);
                if (handler != null) {
                    handler.setFired(true);
                    handler.setEnabled(false);
                    breakpoint = handler.breakpoint();
                }
            }

            MIValue frameValue = (results != null) ? results.valueOf(MI_FRAME) : null;
            MITList frameTuple;
            MITList stack;
            // Mac 10.4 gdb provides no "frame" attribute

            // For the scenario that stack view is closed and local view
            // is open, we need frame params info from here.
            //mromashova_bz#269898: the above is incorrect anymore
            //when locals are requested we will get arguments (names and values, no need for this)
//            if (get_locals && frameValue != null) {
//                // needs to get args info
//                // frameValue include args  info
//                guiStackFrames = new GdbFrame[] {new GdbFrame(this, frameValue, null)};
//            }
        
            if (srcResults != null) {
                boolean visited = false;
                stack = srcResults.valueOf("stack").asList(); // NOI18N
                frameValue = ((MIResult) stack.asList().get(0)).value();
                frameTuple = frameValue.asTuple();

                // find the first frame with source info if dis was not requested
                for (MITListItem stf : stack.asList()) {
                    frameTuple = ((MIResult)stf).value().asTuple();
                    if (disRequested || frameTuple.valueOf("file") != null) { //NOI18N
                        break;
                    }
                    visited = true;
                }
                homeLoc = MILocation.make(this, frameTuple, srcResults, visited, stack.size(), breakpoint);
                state().isUpAllowed = !homeLoc.bottomframe();
                state().isDownAllowed = !homeLoc.topframe();
                setStack(srcRecord);
            } else {
                frameTuple = ( frameValue == null ? null : frameValue.asTuple() );
                stack = null;
                homeLoc = MILocation.make(this, frameTuple, null, false, 0, breakpoint);
            }

            setVisitedLocation(homeLoc);

//            if (get_frames || get_locals) {
//                showStackFrames();
//            }

            if (get_threads) {
                showThreads();
            }
            FileObject fileObj = homeLoc == null || !homeLoc.hasSource() ? null : 
                    EditorBridge.findFileObject(fmap().engineToWorld(homeLoc.src()), this);                    
            final boolean updateWatches = get_watches || watchBag().hasPinnedWatchesOpened(fileObj);
            if (updateWatches) {
                //do not update non pinned watches if watches window is closed 
                updateWatches(updateWatches);
            }

            if (get_debugging) {
                for (GdbThread debuggingViewThread : threadsWithStacks) {
                    debuggingViewThread.setCurrent(currentThreadId.equals(debuggingViewThread.getId()));
                }
                debuggingViewUpdater.treeChanged();
            }

            state().isProcess = true;

            if (RegistersWindow.getDefault().isShowing()) {
                requestRegisters();
            }
        }

        if (record != null) {
            explainStop(reason, record);
        }

        stateSetRunning(false);
        stateChanged();
        session().setSessionState(state());
    }

    private boolean haveCountingBreakpoints() {
        for (Handler h : bm().getHandlers()) {
            NativeBreakpoint b = h.breakpoint();
	    if (b != null && b.hasCountLimit())
                return true;
            }
        return false;
    }

    /**
     * Re-reset the ignore count.
     *
     * Once a bpts ignore count reaches 0 it stops getting ignored.
     * That would mean that if we re-run we'll hit the bpt on the first time.
     * This is different from the dbx-style semantics that we've adopted,
     * so if the bpts ignore count has been reset we re-reset it back
     * based on count limit.
     *
     * If instead of re-running we resume then the bpt will be hit
     * after countlimit more tries. For example, if count-limit is set to
     * 2, the bpt will be hit on the 2nd, 4th, 6th etc counts.
     *
     * Not that with gdb the actual count of the bpt will keep growing
     * whereas in dbx it gets reset and never exceeds the limit.
     */
    private void adjustIgnore(NativeBreakpoint b, MITList props) {
        assert b.hasCountLimit() :
                "adjustIgnore() called on a bpt w/o a count limit"; // NOI18N
        MIValue ignore = props.valueOf("ignore"); // NOI18N
	if (ignore != null)
            return;

        /*
	System.out.printf("Handler %d has count limit of %d but gdb reset it\n", hid, h.breakpoint().getCountLimit());
         */

        long limit = b.getCountLimit();
        int newIgnore;
	if (limit == -1)
            newIgnore = GdbHandlerExpert.infinity;
	else
            newIgnore = (int) limit - 1;
        send("-break-after " + b.getId() + ' ' + newIgnore); // NOI18N
    }

    /**
     * Process the reply from -break-list and update active counts.
     *
     * Also gdb (6.4 at least) has a bug where ignore counts get eliminated,
     * reset to 0, under MI. I can't even tell when they seem to get reset
     * since the get reset if they get hit or if they don't.
     * So we go through and re-reset the ignore counts for any counting
     * breakpoints.
     */
    private void updateCounts(MIRecord record) {
        MITList bptresults = record.results();
	MITList table =
	    bptresults.valueOf("BreakpointTable").asTuple();	// NOI18N
        MITList bpts = table.valueOf("body").asList();		// NOI18N
        System.out.printf("updateCounts: %d bpts\n", bpts.size()); // NOI18N

        for (int bx = 0; bx < bpts.size(); bx++) {
            MIResult b = (MIResult) bpts.get(bx);
            // System.out.printf("b %s\n", b.toString());

            MITList props = b.value().asTuple();
            // System.out.printf("props %s\n", props.toString());

            int hid = -1;
            try {
                hid = Integer.parseInt(props.getConstValue(MI_NUMBER));
            } catch (NumberFormatException ex) {
                /* Handling sub-breakpoints with <number>.<sub-number> IDs.
                   As hid was set to -1 there will be no handler for such a breakpoint.

                   This is a temporary solution until CND supports multiple breakpoints in full
                 */
            }
            Handler h = bm().findHandler(hid);

            if (h != null && h.breakpoint().hasCountLimit()) {
                int count = Integer.parseInt(props.getConstValue("times")); // NOI18N
                h.setCount(count);

                adjustIgnore(h.breakpoint(), props);
            }
        }
        System.out.printf("............................................\n"); // NOI18N
    }

    void genericStopped(final MIRecord stopRecord) {
        // Get as much info about the stopped src code location
        /* OLD
         * for gdb 6.3/6.4 ( can debug gcc-build dbx on linux )
         * 6.3/6.4 does not support "-stack-info-frame"
         * On the Mac -file-list-exec-source-file seems to return some
         * unrelated constant value

        MICommand cmd =
        new AbstractMICommand(0, "-file-list-exec-source-file") {
        protected void onDone(MIRecord record) {
        genericStoppedWithSrc(stopRecord, record);
        finish();
        }
        };
        gdb.sendCommand(cmd);
         */
        if (session.coreSession() != DebuggerManager.getDebuggerManager().getCurrentSession()) {
            DebuggerManager.getDebuggerManager().setCurrentSession(session.coreSession());
        }
        //bz#270229 - IDE doesn't update "Call Stack" tab if this tab is not a current
        //need to update current stack frame number, new generic stop
        currentStackFrameNo = "0";//NOI18N
        final MITList results = stopRecord.results();

        state().isRunning = false;

        // detect first stop (in _start or main)
        if (firstBreakpointId != null && results != null && "breakpoint-hit".equals(results.getConstValue("reason"))) { // NOI18N
            if (ATTACH_ID.equals(firstBreakpointId)) {
                attachDone();
                return;
            }
            MIValue bkptnoValue = results.valueOf("bkptno"); // NOI18N
            boolean cont = (bkptnoValue == null && !STEP_INTO_ID.equals(firstBreakpointId)) ||
               (bkptnoValue != null && (firstBreakpointId.equals(bkptnoValue.asConst().value())));
            firstBreakpointId = null;

            // see IZ 210468, init watches here
            ((GdbDebuggerSettingsBridge)profileBridge).noteFistStop();

            sendPidCommand(cont);
            if (cont) {
                return;
            }
        }

        //detect silent stop
        if (gdb.isSignalled()) {
            if (results != null && "signal-received".equals(results.getConstValue("reason"))) { //NOI18N
                MIValue signalValue = results.valueOf("signal-name"); //NOI18N
                if (signalValue != null) {
                    String signal = signalValue.asConst().value();
                    if ("SIGCONT".equals(signal)) { // NOI18N
                        // continue after silent stop
                        gdb.resetSignalled();
                        go();
                        return;
                    } else if ("SIGINT".equals(signal)) { // NOI18N
                        // silent stop
                        if (gdb.isSilentStop()) {
                            gdb.resetSilentStop();
                            state().isRunning = false;
                            return;
                        }
                    } else if ("SIGTRAP".equals(signal) && // NOI18N
                            (getHost().getPlatform() == Platform.Windows_x86 ||
                            getHost().getPlatform() == Platform.MacOSX_x86)) {
                        // see IZ 172855 (On windows we need to skip SIGTRAP)
                        if (gdb.isSilentStop()) {
                            gdb.resetSignalled();
                            // silent stop
                            state().isRunning = false;
                            return;
                        }
                    } else {
                        gdb.resetSignalled();
                    }
                }
            }
        }
        MIValue threadIdValue = (results == null) ? null : results.valueOf("thread-id"); // NOI18N
        if (threadIdValue != null) {    // exited case should be omitted
            currentThreadId = threadIdValue.asConst().value();
        }

        if (get_debugging) {
            requestThreadsWithStacks();
        }

        // Should be called after requestThreadsWithStacks to activate a necessary thread
        requestStack(stopRecord);

        if (MemoryWindow.getDefault().isShowing()) {
            MemoryWindow.getDefault().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "debugger stopped")); // NOI18N
        }

        // If we have any counting bpts poll the bpt list in order to
        // learn the current bpt counts.
        if (haveCountingBreakpoints()) {
            MICommand cmd = new MiCommandImpl("-break-list") { // NOI18N
                @Override
                protected void onDone(MIRecord record) {
                    updateCounts(record);
                    finish();
                }
            };
            gdb.sendCommand(cmd);
        }
    }
    
    //call this when Call Stack window is opened
    private void requestStack() {
        MICommand cmd =
            new MiCommandImpl(peculiarity.stackListFramesCommand(currentThreadId)) {
            @Override
            protected void onDone(MIRecord record) {
                setStack(record);
                finish();
            }
            @Override
            protected void onError(MIRecord record) {
//                if (get_frames) {
//                    setStack(record);
//                }
//should show some message here I think
                finish();
            }

           
        };
        gdb.sendCommand(cmd);        
    }    

    protected void requestStack(final MIRecord stopRecord) {
        MICommand cmd =
            new MiCommandImpl(peculiarity.stackListFramesCommand(currentThreadId)) {
            @Override
            protected void onDone(MIRecord record) {
                genericStoppedWithSrc(stopRecord, record);
                finish();
            }
            @Override
            protected void onError(MIRecord record) {
                genericStoppedWithSrc(stopRecord, null);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    /**
     * The program has hit a signal; produce a popup to ask the user
     * how to handle it.
     */
    private void showSignalPopup(MITList results, String sigName) {
        SignalDialog sd = new SignalDialog();

        // LATER SHOULD factor this info into a class

        String signum = results.getConstValue("signal-meaning", "?"); // NOI18N

        // gdb doesn't furnish any of the other info

        String signalInfo;
        signalInfo = Catalog.format("FMT_SignalInfo", // NOI18N
                sigName, signum);
        sd.setSignalInfo(signalInfo);

        if (session != null) {
            sd.setReceiverInfo(session.getShortName(), session.getPid());
        } else {
            sd.setReceiverInfo("", 0);
        }

        // get disposition of signal
        // LATER: use "info signal" to initialize gdb's signal disposition
        // LATER: lookup disposition by name?

        Signals.InitialSignalInfo dsii = null;
        int signo = 0;
        DbgProfile debugProfile = getNDI().getDbgProfile();
        dsii = debugProfile.signals().getSignalByName(sigName);

        boolean wasIgnored = false;
        if (dsii != null) {
	    wasIgnored = ! dsii.isCaught();
            sd.setIgnore(true, wasIgnored);
        } else {
            sd.setIgnore(true, false); // default
        }

        sd.show();

        if (dsii != null /*&& sd.isIgnore() != wasIgnored*/) {
            String cmd;
            if (sd.isIgnore()) {
                // gdb seems to not be able to ignore caught signals???
                cmd = "handle " + sigName + " noprint"; // NOI18N
            } else {
                cmd = "handle " + sigName + " stop"; // NOI18N
            }
            send(cmd);
        }

        boolean signalDiscarded = sd.discardSignal();
        if (signalDiscarded) {
            deliverSignal = -1;
        } else {
            deliverSignal = signo;
        }

        if (sd.discardSignal()) {
            send("handle " + sigName + " nopass"); //NOI18N
        } else {
            send("handle " + sigName + " pass"); //NOI18N
        }

        if (sd.shouldContinue()) {
            go();
        }
    }

    private void explainStop(String reason, MIRecord record) {
        final MITList results = record.results();

        String stateMsg = reason;
        String signalName = "<UNKNOWN>"; // NOI18N

        if (reason.equals("end-stepping-range")) {		// NOI18N
            stateMsg = Catalog.get("Dbx_program_stopped");	// NOI18N
        } else if (reason.equals("signal-received")) {		// NOI18N
            signalName = results.getConstValue("signal-name", signalName); // NOI18N
            stateMsg = Catalog.get("Dbx_signal") + // NOI18N
                    " " + signalName;			// NOI18N

        } else if (reason.equals("function-finished")) { // NOI18N
            stateMsg = Catalog.get("Dbx_program_stopped");	// NOI18N
        } else if (reason.equals("breakpoint-hit")) {		// NOI18N
            stateMsg = Catalog.get("Dbx_program_stopped");	// NOI18N
        } else if (reason.equals("location-reached")) {	// NOI18N
            stateMsg = Catalog.get("Dbx_program_stopped");	// NOI18N
        } else if (reason.equals(MI_WATCHPOINT_TRIGGER)) {
            String expValue = "";
            MIValue wptVal = results.valueOf(MI_WPT);
            if (wptVal != null) {
                expValue = wptVal.asList().getConstValue(MI_EXP);
            }
            stateMsg = Catalog.format("MSG_Watchpoint_Trigger", expValue); // NOI18N
        } else if (reason.equals(MI_WATCHPOINT_SCOPE)) {
            // LATER: show watchpoint name/expression
//            String expValue = "";
//            MIValue wptVal = results.valueOf(MI_WPT);
//            if (wptVal != null) {
//                expValue = wptVal.asList().getConstValue(MI_EXP);
//            }
            stateMsg = Catalog.format("MSG_Watchpoint_Scope"); // NOI18N
        } else if (reason.equals(MI_SYSCALL_ENTRY)) {
            stateMsg = Catalog.format("MSG_Syscall_Entry", //NOI18N
                    results.getConstValue("syscall-name", "?"), results.getConstValue("syscall-number", "?")); // NOI18N
        } else if (reason.equals(MI_SYSCALL_RETURN)) {
            stateMsg = Catalog.format("MSG_Syscall_Return", //NOI18N
                    results.getConstValue("syscall-name", "?"), results.getConstValue("syscall-number", "?")); // NOI18N
        } else {
            stateMsg = "Stopped for unrecognized reason: " + reason; // NOI18N
        }

	if (stateMsg != null)
            setStatusText(stateMsg);

        if (reason.equals("signal-received") && !gdb.isSignalled()) { //NOI18N
            showSignalPopup(results, signalName);
        }
    }

    /**
     * Convert a string into a C (C++,Java) string.
     *
     * For example, the following ... well I was going to write an example
     * using the backslash but being that it's a unicode escape character
     * 'javac' ends up complaining about it.
     *     XXX
     * becomes
     *     "c:Documents and Settings\\user\\Projects"
     */
    private static String toCString(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int sx = 0; sx < s.length(); sx++) {
            char c = s.charAt(sx);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Reuse this session with a new debug target
     * @param di
     */
    @Override
    public void reuse(NativeDebuggerInfo di) {
        // Tell gdb what to debug
        debug((GdbDebuggerInfo) di);
    }

    private void debug(GdbDebuggerInfo gdi) {
        String program = gdi.getTarget();
        long pid = gdi.getPid();
        String corefile = gdi.getCorefile();
        final boolean isCore = (corefile != null);

        profileBridge.setup(gdi);

        if (corefile != null) {
            // debug corefile
            if (program == null) {
                program = "-"; // NOI18N
            }

        } else if (pid != -1) {
            // attach
            if (program == null) {
                program = "-"; // NOI18N
            }

        } else {
            // raw gdb session, no need to send 'file' cmd.
            if (program == null) {
                return;
        }

        // load program
        }

        String outputFile = gdi.getSymbolFile();
        outputFile = localToRemote("symbol-file", outputFile); //NOI18N

        String tmp_cmd;
        if (peculiarity.isLldb()) {
            tmp_cmd = "-file-exec-and-symbols"; // NOI18N
        } else if (isCore || pid != -1) {
            tmp_cmd = "-file-symbol-file "; // NOI18N
        } else if (!CndPathUtilities.sameString(program, outputFile)) {
            // load symbol file separately, IZ 194531
            send("-file-symbol-file " + toCString(outputFile), false); // NOI18N
            tmp_cmd = "-file-exec-file "; // NOI18N
        } else {
            tmp_cmd = "-file-exec-and-symbols"; // NOI18N
        }

        final String mi_command = tmp_cmd;
        final String fprogram = program;

        // There is no way to determine correct file mapper here, see #191835
        //final String mprogram = toCString(fmap.worldToEngine(program));
        final String mprogram = toCString(program);

        // mainly load symbol table
        // -file-core-file is not implemented in gdb 6.1
        // use CLI command "core-file" instead
        MICommand cmd =
            new MiCommandImpl(mi_command + ' ' + mprogram) {

            @Override
            protected void onDone(MIRecord record) {
                if (isCore) {
                    state().isCore = true;
                }

                //if ((getNDI().getAction() & NativeDebuggerManager.LOAD) != 0) {
                getFullPath(null);
                //}

                gdb.startProgressManager().finishProgress();
                session().setTarget(fprogram);
                session().update();
                session().setSessionEngine(GdbEngineCapabilityProvider.getGdbEngineType());

                state().isLoaded = true;
                stateChanged();
                session().setSessionState(state());

                // Set session pid on attach
                // The UserAttachActionImpl.attach() creates target by binary+pid
                // The DebuggerCommonIntegrationUtils.registerDefaultListener().propertyChange() try to recreate
                // target by binary+pid. If pid is not set, the second target by binary is created.
                long aPid = getNDI().getPid();
                if (aPid != -1) {
                    session().setPid(aPid);
                }

                noteProgLoaded(fprogram);

                finish();
            }

            @Override
            protected void onError(MIRecord record) {
                gdb.startProgressManager().finishProgress();
                /* LATER
                session().setTarget(fprogram);
                session().update();
                 */

                state().isLoaded = false;
                stateChanged();
                session().setSessionState(state());

                genericFailure(record);
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    // interface BreakpointProvider
    @Override
    public HandlerExpert handlerExpert() {
        return handlerExpert;
    }

    // interface BreakpointProvider
    @Override
    public void postRestoreHandler(final int rt, HandlerCommand hc) {

        final MICommand cmd = new MIRestoreBreakCommand(rt, hc.getData());

        //
        // What's the inject("1\n") about?
        //
        // 1 is the "all" choice for overloaded function names so we
        // pre-issue it. If we don't do this then onUserInteraction() will
        // get called against the wrong command because it only works
        // with the current outstanding command.
        // If the break command has no overloaded methods and there is no menu
        // then there is no harm done either. gdb simply responds with
        // 	1^done
        // and we also ignore it.
        //
        // An alternative, more robust, solution is to chain all these commands
        // together and let the regular overload resolution handle everything.
        // Chaining a bunch of bpt commands in a loop is easy. It's the
        // trickiness of ensuring that the following commands are also
        // chained that makes me favor the pre-injection solution ... for now.

        sendCommandInt(cmd);

        // modern gdb does not ask user when in MI mode
        //gdb.tap().inject("1\n");	// TMP // NOI18N
    }

    private final class DisModel extends DisModelSupport {

        /*
	 * Parse list of instructions
	 * See gdb/mi/README.examples for examples.
         */
        private void parseDisasm(MITList inss, List<Line> lines) {
            for (int ix = 0; ix < inss.size(); ix++) {
                MITList ins = ((MIValue) inss.get(ix)).asTuple();
                String address = ins.getConstValue("address"); // NOI18N

                MIValue fnameValue = ins.valueOf("func-name"); // NOI18N
                String fname;
                String offset = null;
                if (fnameValue != null) {
                    fname = fnameValue.asConst().value();
                    offset = ins.getConstValue("offset"); // NOI18N
                } else {
                    fname = Catalog.get("MSG_UnknownFunction");	// NOI18N
                }

                String inst = ins.getConstValue("inst"); // NOI18N
                /*
		System.out.printf("\t%s: %s+%s: %s\n",
		    address, fname, offset, inst);
                 */

		if (offset != null)
                    lines.add(new Line(address + ":", fname + "+" + offset + ":\t" + inst)); // NOI18N
		else
                    lines.add(new Line(address + ":", fname + ":\t" + inst)); // NOI18N
                }
            }

        /**
	 * Interpret disassembly in 'record', stuff it into this
	 * DisFragModel and update(), notifying the DisView.
         */

        public void parseRecord(MIRecord record) {
            clear();

            StopWatch sw = new StopWatch("Parse MI instructions"); // NOI18N
            sw.start();

            MITList asm_insnsR = record.results();
            MITList lines = asm_insnsR.valueOf("asm_insns").asList(); // NOI18N

            List<Line> list = new ArrayList<Line>();
            if (lines.isValueList()) {
                // disassembly only
                parseDisasm(lines, list);

	    }  else {
                // src lines and disassembly
                for (int lx = 0; lx < lines.size(); lx++) {
                    MIResult src_and_asm_lineR = (MIResult) lines.get(lx);
		    MITList src_and_asm_line =
			src_and_asm_lineR.value().asTuple();

		    String line =
			src_and_asm_line.getConstValue("line"); // NOI18N
		    String file =
			src_and_asm_line.getConstValue("file"); // NOI18N
		    MITList inss =
			src_and_asm_line.valueOf("line_asm_insn").asList(); // NOI18N

                    /*
		    System.out.printf("%s:%s\n", file, line);
                     */
                    list.add(new Line(line, file));
                    parseDisasm(inss, list);
                }
            }
            addAll(list);
            sw.stop();
            // sw.dump();

            update();
        }
    }

    private class DisController extends ControllerSupport {

        @Override
        protected void setBreakpointHelp(String address) {
            // Similar to ToggleBreakpointActionProvider.doAction
            NativeBreakpoint b = NativeBreakpoint.newInstructionBreakpoint(address);
            if (b != null) {
		int routingToken =
		    RoutingToken.BREAKPOINTS.getUniqueRoutingTokenInt();
                Handler.postNewHandler(GdbDebuggerImpl.this, b, routingToken);
            }
            // We'll continue in newHandler() or ?error?
        }

        /*
	 * Get some disassembly around the current visiting location.
	 *
	 * gdb-mi syntax is one of
	 *	-data-disassemble -s <start> -e <end> -- <mode>
	 *	-data-disassemble -f <file> -l <line> [ -n <Nins> ]  -- <mode>
	 *
	 * <mode> if 0 provides only assembly, if 1 provides line info
	 * and assembly.
	 *
	 * NOTES:
	 *
	 * - gdb will automatically subtract from -l. As the docs say, it's
	 *   "line number to disassemble _around_".
	 *   This also means that subtracting our own lines as we do for dbx
	 *   is unneccessarty. Moreover if the number of lines we subtract
	 *   reaches back into the _previous_ function, gbd will only give us
	 *   disassembly for that one.
	 *
	 * - It seems gdb will return a reasonable "window" when -n isn't
	 *   specified.
	 *
	 * - Apparenty -n specifies the number of _instructions_ not lines as
	 *   implied by the gdb docs.
	 *
	 * - Unlike what the gdb docs for -n imply, you cannot mix and match
	 *   -e and -n
	 *
	 * - -n of -1 wil disassemble the whole function but we avoid that
	 *   since it will swamp us for very large functions.
	 *
	 * - We will always ask for src line info.
         */

        // interface Controller
        @Override
        public void requestDis(boolean withSource) {
            if (peculiarity.isLldb()) {
                MICommand cmd = new MiCommandImpl("-data-evaluate-expression $pc") { //NOI18N

                    @Override
                    protected void onDone(MIRecord record) {
                        if (!record.isError()) {
                            String start = record.results().getConstValue("value"); //NOI18N
                            long end = Long.parseLong(start) + 100;
                            requestDisFromGdb(
                                    "-data-disassemble -s " + start +   //NOI18N
                                    " -e " + end +                      //NOI18N
                                    " -- 0");                            //NOI18N
                        }
                    }

                };

                gdb.sendCommand(cmd);
                return;
            }
            GdbFrame currentFrame = getCurrentFrame();
            if (currentFrame == null) {
                return;
            }
            String file = currentFrame.getEngineFullName();
            String line = currentFrame.getLineNo();

            String cmd = "-data-disassemble"; // NOI18N
            int src = withSource ? 1 : 0;
            if (file != null && line != null && !line.isEmpty()) {
                // request by line #

                cmd += " -f \"" + file + '"'; // NOI18N
                cmd += " -l " + line; // NOI18N
                cmd += " -- " + src; // NOI18N

            } else {
                cmd += " -s $pc -e \"$pc+100\" -- " + src; //NOI18N
            }
            requestDisFromGdb(cmd);
        }

        // interface Controller
        @Override
        public void requestDis(String start, int count, boolean withSource) {
            /*
	    System.out.printf("DisController.requestDis(%s, %d)\n",
		start, count);
	    System.out.printf("%s\n", getVisitedLocation);
             */
	    if (start == null)
                return;

            int src = withSource ? 1 : 0;
            String cmd = "-data-disassemble"; // NOI18N
            cmd += " -s \"" + start + '"'; // NOI18N
            cmd += " -e \"" + start + '+' + count + '"'; // NOI18N
            cmd += " -- " + src; // NOI18N
            requestDisFromGdb(cmd);
        }
    }

    // interface NativeDebugger
    @Override
    public void registerDisassembly(Disassembly dis) {
        //assert w == null || w == disassemblerWindow();

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
        } else {
            setAsmVisible(false);
        }
    }

    // implement NativeDebuggerImpl
    @Override
    protected DisFragModel disModel() {
        return disModel;
    }

    // implement NativeDebuggerImpl
    @Override
    public Controller disController() {
        return disController;
    }

    @Override
    public GdbDisassembly getDisassembly() {
        return disassembly;
    }

    private void requestDisFromGdb(String cmd) {
        // DEBUG System.out.printf("requestDisFromGdb(%s)\n", cmd);
	if (postedKill || postedKillEngine || gdb == null || cmd == null)
            return;
        getMIDis(cmd);
    }

    private void setDis(MIRecord record) {
        disModel.parseRecord(record);
        disassembly.update(record.toString());

        // 6582172
//	if (update_dis)
//	    disStateModel().updateStateModel(visitedLocation, false);
    }

    @Override
    public FormatOption[] getMemoryFormats() {
        return GdbMemoryFormat.values();
    }

    static List<String> parseMem(MIRecord record) {
        LinkedList<String> res = new LinkedList<String>();

        int size[] = new int[MEMORY_READ_WIDTH];

        for (MITListItem elem : record.results().valueOf("memory").asList()) { //NOI18N
            MITList line = ((MITList)elem);
            MIValue dataValue = line.valueOf("data"); //NOI18N
            int count = 0;
            for (MITListItem dataElem : dataValue.asList()) {
                int len = ((MIConst)dataElem).value().length();
                size[count] = Math.max(size[count], len);
                count++;
            }
        }

        for (MITListItem elem : record.results().valueOf("memory").asList()) { //NOI18N
            StringBuilder sb = new StringBuilder();
            MITList line = ((MITList)elem);
            String addr = line.getConstValue("addr"); //NOI18N
            sb.append(addr).append(':'); //NOI18N
            MIValue dataValue = line.valueOf("data"); //NOI18N
            int count = 0;
            for (MITListItem dataElem : dataValue.asList()) {
                for(int i = 0; i < size[count]-((MIConst)dataElem).value().length()+1; i++) {
                    sb.append(' '); //NOI18N
                }
                sb.append(((MIConst)dataElem).value());
                count++;
            }
            String ascii = line.getConstValue("ascii"); //NOI18N
            sb.append(" \"").append(ascii).append("\""); //NOI18N
            res.add(sb.toString() + "\n"); //NOI18N
        }

        return res;
    }

    private static final int MEMORY_READ_WIDTH = 16;

    @Override
    public void requestMems(String start, String length, FormatOption format) {
        int lines;
        try {
            lines = (Integer.parseInt(length)-1)/MEMORY_READ_WIDTH+1;
        } catch (Exception e) {
            return;
        }
        MICommand cmd = new MiCommandImpl("-data-read-memory " + start + ' ' + format.getOption() + //NOI18N
                " 1 " + lines + ' ' + MEMORY_READ_WIDTH + " .") { // NOI18N
            @Override
            protected void onDone(MIRecord record) {
                if (MemoryWindow.getDefault().isShowing()) {

                    MemoryWindow.getDefault().updateData(parseMem(record));
                }
                finish();
            }

            @Override
            protected void onError(MIRecord record) {
                if (MemoryWindow.getDefault().isShowing()) {
                    MemoryWindow.getDefault().updateData(Collections.EMPTY_LIST);
                }
                finish();
            }
        };
        // LATER: sometimes it is sent too early, need to investigate
        if (gdb != null) {
            gdb.sendCommand(cmd);
        }
    }

    private Map<Integer, String> regNames = null;

    public void requestRegisters() {
        //check that we have regNames
        if (regNames == null) {
            MICommand cmd = new MiCommandImpl("-data-list-register-names") { // NOI18N
                @Override
                protected void onDone(MIRecord record) {
                    Map<Integer, String> res = new HashMap<Integer, String>();
                    int idx = 0;
                    for (MITListItem elem : record.results().valueOf("register-names").asList()) { //NOI18N
                        res.put(idx++, ((MIConst)elem).value());
                    }
                    regNames = res;
                    finish();
                }
            };
            // LATER: sometimes it is sent too early, need to investigate
            if (gdb != null) {
                gdb.sendCommand(cmd);
            }
        }

        if (state().isProcess) {
            MICommand cmd = new MiCommandImpl("-data-list-register-values x") { // NOI18N
                @Override
                protected void onDone(MIRecord record) {
                    if (RegistersWindow.getDefault().isShowing()) {
                        LinkedList<String> res = new LinkedList<String>();
                        for (MITListItem elem : record.results().valueOf("register-values").asList()) { //NOI18N
                            StringBuilder sb = new StringBuilder();
                            MITList line = ((MITList)elem);
                            String number = line.getConstValue(MI_NUMBER);
                            // try to get real name
                            try {
                                number = regNames.get(Integer.valueOf(number));
                            } catch (Exception e) {
                                Exceptions.printStackTrace(e);
                            }
                            sb.append(number).append(' ');
                            String value = line.getConstValue("value"); //NOI18N
                            sb.append(value);
                            res.add(sb.toString());
                        }
                        RegistersWindow.getDefault().updateData(res);
                    }
                    finish();
                }
            };
            // LATER: sometimes it is sent too early, need to investigate
            if (gdb != null) {
                gdb.sendCommand(cmd);
            }
        }
    }

    @Override
    public void registerEvaluationWindow(EvaluationWindow w) {
    }

//    public void registerArrayBrowserWindow(ArrayBrowserWindow w) {
//        notImplemented("registerArrayBrowserWindow()");	// NOI18N
//    }
    /*package*/ void newAsyncBreakpoint(MIRecord record) {
        Integer rt = cliBreakpointsRTs.poll();
        if (rt == null) {
            rt = 0;
        }
        BreakpointPlan bp = bm().getBreakpointPlan(rt, BreakpointMsg.NEW);
        if (bp.op() == BreakpointOp.NEW) {
            newHandlers(rt, record, bp);
        } else {
            MIResult result = (MIResult) record.results().get(0);
            replaceHandler(rt, result, bp);
        }
    }

    /*package*/ void deleteAsyncBreakpoint(MIRecord record) {
        String id = record.results().getConstValue("id");   // NOI18N
        int hid = Integer.parseInt(id);
        bm().deleteHandlerById(0, hid);
    }

    private void newHandlers(int rt, MIRecord record, BreakpointPlan bp) {
        MITList results = record.results();
        for (int tx = 0; tx < results.size(); tx++) {
            MIResult result = (MIResult) results.get(tx);
            if (result.matches(MI_BKPT) || result.matches(MI_WPT)) {
                newHandler(rt, result, bp);
                return;  // In order to avoid errors in multiple locations breakpoints
            }
        }
        if (results.size() > 0) {   // IN case of async BPs we get "^done" response
            newHandler(rt, (MIResult) results.get(0), bp);
        }
    }

    private void newHandler(int rt, MIResult result, BreakpointPlan bp) {
        if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Bpt.pathway) {
            System.out.printf("GdbDebuggerImpl.newHandler(%s)\n", result); // NOI18N
        }

        Handler handler = null;
        try {
            if (bp == null) {
                bp = bm().getBreakpointPlan(rt, BreakpointMsg.NEW);
            }

            /* LATER
	     See LATER below
	    // remember enable state before we process incoming bpt data
	    boolean disable = !bj.midlevel().isEnabled();
             */

            NativeBreakpoint template = bp.template();

            switch (bp.op()) {
                case NEW:
                    if (template == null) {
                        // prepare to create handlers for breakpoints created in console
                        template = prepareTemplate(result);
                    }
                    handler = handlerExpert.newHandler(template, result, null);
                    break;
                case RESTORE:
                    handler = handlerExpert.newHandler(template, result, bp.restored());
                    assert handler.breakpoint() == bp.restored();
                    break;
                case MODIFY:
                    handler = bp.originalHandler();
                    handler = handlerExpert.replaceHandler(template, handler, result);
                    break;
            }

            handlerExpert.addAnnotations(handler,
                    handler.breakpoint(),
                    template,
                    result);

            /* LATER
	     Not needed if we work with 6.8

	    boolean isLoadModel =
		! DebuggerOption.RUN_AUTOSTART.
		      isEnabled(manager().globalOptions());

	    if (DebuggerManager.isStandalone() || isLoadModel) {
		// Not until gdb 6.8 do we get the -d option which would
		// allow us to create the handler in initially disabled
		// form.
		// Until then we need to send gdb an explicit disable.
		//
		// However, in the start model, because of the
		// scheduling of outgoing commands this won't get
		// sent until -exec-run is sent and will
		// arrive too late :-(
		// So we only try this in the load model.

		if (disable)
		    setHandlerEnabled(0, handler.getId(), false);
	    }
             */

            if (!template.isEnabled() && handler.breakpoint().isEnabled()) {
                // manually switch off async breakpoint
                handler.postEnable(false, handler.breakpoint().getId());
            }

            bm().noteNewHandler(rt, bp, handler);
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
            /* LATER
            // something went wrong, create a "broken" breakpoint
            if (created != null) {
                newBrokenHandler(created, null, x.getMessage(), false);
            } else {
                ErrorManager.getDefault().notify(x);
            }
             */
        }
    }

    private NativeBreakpoint prepareTemplate(MIResult result) {
        NativeBreakpointType type;
        MIValue bkptValue = result.value();
        MITList props = bkptValue.asTuple();
        String typeVal = props.getConstValue("type"); //NOI18N
        if ("catchpoint".equals(typeVal)) { //NOI18N
            type = new SysCallBreakpointType();
        } else if ("breakpoint".equals(typeVal)) { //NOI18N
            type = new LineBreakpointType();
            String origLoc = props.getConstValue("original-location"); //NOI18N
            if (origLoc != null) {
                if (origLoc.startsWith("*")) { //NOI18N
                    type = new InstructionBreakpointType();
                } else if (!origLoc.contains(":")) { //NOI18N
                    type = new FunctionBreakpointType();
                }
            }
        } else {
            type = new ExceptionBreakpointType();
        }
        return type.newInstance(NativeBreakpoint.SUBBREAKPOINT);
    }

    private void deleteForReplace(int rt, Handler targetHandler){
        // Don't use
        //	postDeleteHandler(hid);
        // because it will come around and call handlerDeleted which
        // we don't want.

        final int hid = targetHandler.getId();
        MICommand deleteCmd = new MIBreakCommand(rt, "-break-delete " + hid) { // NOI18N

            @Override
            protected void onDone(MIRecord record) {
                // Don't use deleteHandlerById ... it ties back to
                // owning NB's ...
                // deleteHandlerById(b.getRoutingToken(), hid);
                Handler h = bm().findHandler(hid);

                // Don't cleanup either since it sets the bpts back pointer
                // to null.
                // OLD h.cleanup();

                // OLD handlers.remove(h);
                bm().simpleRemove(h);
                finish();
            }
        };
        gdb.sendCommand(deleteCmd);
    }


    private void replaceHandler(int rt, MIResult result, BreakpointPlan bp) {
        if (bp == null) {
            bp = bm().getBreakpointPlan(rt, BreakpointMsg.REPLACE);
        }
        assert bp.op() == BreakpointOp.MODIFY :
                "replaceHandler(): bpt plan not CHANGE for rt " + rt; // NOI18N


        NativeBreakpoint targetBreakpoint = bp.target();

        assert targetBreakpoint.isSubBreakpoint();
        assert !targetBreakpoint.isEditable() :
                "targetBreakpoint is editable"; // NOI18N
        Handler targetHandler = targetBreakpoint.getHandler();
        assert targetHandler == bp.originalHandler();

        Handler replacementHandler =
            handlerExpert.replaceHandler(targetBreakpoint, targetHandler, result, bp.template());

        handlerExpert.addAnnotations(replacementHandler, null, targetBreakpoint, result);

        deleteForReplace(rt, targetHandler);

        bm().noteReplacedHandler(bp, replacementHandler);
    }

    /**
     * Common behaviour for resumptive commands.
     * While these commands are mostly in the -exec family not all
     * -exec comamnds are resumptive (e.g. -exec-arguments)
     */
    private class MIResumptiveCommand extends MiCommandImpl {

        protected MIResumptiveCommand(String cmdString) {
            super(cmdString);
        }

        @Override
        protected void onRunning(MIRecord record) {
            // Actually we might get an error that will undo running
            // Perhaps we SHOULD note the receipt of running and commit to
            // it on a done?
            genericRunning();
        }

        @Override
        protected void onError(MIRecord record) {
            // gdb will send a "^running" even if step fails
            // cancel running state
            stateSetRunning(false);
            stateChanged();
            session().setSessionState(state());

            genericFailure(record);
            finish();
        }

        @Override
        protected void onStopped(MIRecord record) {
            genericStopped(record);
            finish();
        }
    };

    private final ConcurrentLinkedQueue<Integer> cliBreakpointsRTs = new ConcurrentLinkedQueue<Integer>();

    /**
     * Common behaviour for -break command.
     */
    abstract class MIBreakCommand extends MiCommandImpl {
        private final boolean wasRunning;

        protected MIBreakCommand(int rt, String cmdString) {
            super(rt, cmdString);
            this.wasRunning = state().isRunning;
            //remember catchpoints routing tokens
            if (isConsoleCommand()) {
                cliBreakpointsRTs.add(rt);
            }
        }

        @Override
        protected void onError(MIRecord record) {
            cliBreakpointsRTs.poll();   // removing routing tokens in case of error
            super.onError(record);
        }

        @Override
        protected void finish() {
            if (wasRunning) {
                go();
            }
            super.finish();
        }
    };

    @Override
    public void runFailed() {
        setStatusText(Catalog.get("RunFailed")); // NOI18N
        stateSetRunning(false);
        stateChanged();
        session().setSessionState(state());
    }

    private boolean userInteraction(int rt, MIUserInteraction ui, boolean isBreakpoint) {
        boolean overloadCancelled = false;

        ItemSelectorResult result;

        int nitems = ui.items().length;
        String item[] = ui.items();

        if (isBreakpoint) {
            // Special handling for breakpoint overloading
            assert rt == 0 || // spontaneous from cmdline
                    RoutingToken.BREAKPOINTS.isSameSubsystem(rt);
            String title = "Overloaded breakpoint"; // NOI18N
            result = this.bm().noteMultipleBreakpoints(rt, title, nitems, item);

        } else {

            //
            // convert to a form that popup() likes
            //
            String cookie = null; // OLD "eventspec";
            String title = "Ambiguous symbol"; // NOI18N
            boolean cancelable = ui.hasCancel();
            boolean multiple_selection = true;

            //
            // post the popup
            //
            result = manager().popup(rt, cookie,
                    GdbDebuggerImpl.this, title,
                    nitems, item, cancelable, multiple_selection);
        }

        //
        // convert popup results back to something to pass back to gdb
        //

        String returnValue;

        if (result.isCancelled()) {
            overloadCancelled = true;

            // "cancel" scenario
            returnValue = "" + ui.cancelChoice();

        } else if (result.nSelected() == nitems) {
            // "all" scenario;
            returnValue = "" + ui.allChoice();

        } else {
            returnValue = "";
	    for (int sx = 0; sx < result.nSelected(); sx++)
		returnValue += " " + (result.selections()[sx] + // NOI18N
                        ui.firstChoice());
            }

        gdb.tap().inject(returnValue + "\n"); // NOI18N

        // LATER return result.newRT;
        return overloadCancelled;
    }

    /**
     * Command to restore breakpoints.
     */
    private class MIRestoreBreakCommand extends MIBreakCommand {

        private boolean overloadCancelled = false;

        MIRestoreBreakCommand(int rt, String cmdString) {
            super(rt, cmdString);
        }

        @Override
        protected void onDone(MIRecord record) {
            if (record.isEmpty() && !isConsoleCommand()) {
                // See comment for isEmpty
                onError(record);
            } else {
                newHandlers(routingToken(), record, null);
            }
            finish();
        }

        // override MIBreakCommand
        @Override
        protected void onError(MIRecord record) {
            if (overloadCancelled) {
                // don't do anything
                finish();
            } else {
                super.onError(record);
            }
        }

        @Override
        protected void onUserInteraction(MIUserInteraction ui) {
	    if (ui == null || ui.isEmpty())
                return;
            overloadCancelled = userInteraction(this.routingToken(), ui, true);
        }
    };

    /**
     * Command to create line breakpoint.
     * Creates closure to retain original breakpoint data so we can recover
     * full pathname.
     */
    private class MIBreakLineCommand extends MIBreakCommand {

        // If we get an overload menu we might end up working with a new
        // non-0 rt:
        private final int newRT = 0;

        // If we cancel an overload menu we'll get something like this:
        //
        // which will come to us as an onError(). 'overloadCancelled' helps us
        // bypass creating a broken bpt.
        private boolean overloadCancelled = false;

        MIBreakLineCommand(int rt, String cmdString) {
            super(rt, cmdString);
        }

        @Override
        protected void onDone(MIRecord record) {
            if (record.isEmpty() && !isConsoleCommand()) {
                // See comment for isEmpty
                onError(record);
            } else {
 		newHandlers(newRT == 0? routingToken(): newRT, record, null);
                manager().bringDownDialog();
            }
            finish();
        }

        // override MIBreakCommand
        @Override
        protected void onError(MIRecord record) {
            if (overloadCancelled) {
                // don't do anything
                finish();
            } else {
                super.onError(record);
            }
        }

        @Override
        protected void onUserInteraction(MIUserInteraction ui) {
	    if (ui == null || ui.isEmpty())
                return;
            overloadCancelled = userInteraction(this.routingToken(), ui, true);
        }
    };

    private class MIChangeBreakCommand extends MIBreakCommand {
        private final GdbHandlerCommand ghc;

        MIChangeBreakCommand(int rt, GdbHandlerCommand ghc) {
            super(rt, ghc.getData());
            this.ghc = ghc;
        }

        @Override
        protected void onDone(MIRecord record) {
            ghc.onDone();
            manager().bringDownDialog();
            super.onDone(record);
        }
    }

    /**
     * Command to modify line breakpoint.
     */
    private class MIReplaceBreakLineCommand extends MIChangeBreakCommand {

        MIReplaceBreakLineCommand(int rt, GdbHandlerCommand ghc) {
            super(rt, ghc);
        }

        @Override
        protected void onDone(MIRecord record) {
            if (record.isEmpty() && !isConsoleCommand()) {
                // See comment for isEmpty
                onError(record);
            } else {
                MITList results = record.results();
//		MIValue bkptValue = results.valueOf("bkpt"); // NOI18N
                if (!results.isEmpty()) {
                    MIResult result = (MIResult) results.get(0);
                    replaceHandler(routingToken(), result, null);
                }
                manager().bringDownDialog();
            }
            finish();
        }
    };

    /**
     * Command to repair broken breakpoint.
     */
    private class MIRepairBreakLineCommand extends MIChangeBreakCommand {

        MIRepairBreakLineCommand(int rt, GdbHandlerCommand ghc) {
            super(rt, ghc);
        }

        @Override
        protected void onDone(MIRecord record) {
            if (record.isEmpty() && !isConsoleCommand()) {
                // See comment for isEmpty
                onError(record);
            } else {
                newHandlers(routingToken(), record, null);
                manager().bringDownDialog();
            }
            finish();
        }
    }

    @Override
    public void postEnableAllHandlersImpl(final boolean enable) {
        final Handler[] handlers = bm().getHandlers();

        // no need to enable/disable if there is no handlers
        if (handlers.length == 0) {
            return;
        }

        StringBuilder command = new StringBuilder();
        if (enable) {
            command.append("-break-enable"); // NOI18N
        } else {
            command.append("-break-disable"); // NOI18N
        }

        for (Handler h : handlers) {
            command.append(' ');
            command.append(h.getId());
        }

        MICommand cmd = new MIBreakCommand(0, command.toString()) {
            @Override
            protected void onDone(MIRecord record) {
                for (Handler h : handlers) {
                    h.setEnabled(enable);
                }
                finish();
            }
        };
        sendCommandInt(cmd);
    }

    @Override
    public void postActivateBreakpoints() {
        final Handler[] handlers = bm().getHandlers();

        // no need to enable/disable if there is no handlers
        if (handlers.length == 0) {
            return;
        }

        StringBuilder command = new StringBuilder();
        command.append("-break-enable"); // NOI18N

        for (Handler h : handlers) {
            if (h.breakpoint().isEnabled()) {
                command.append(' ');
                command.append(h.getId());
            }
        }

        MICommand cmd = new MIBreakCommand(0, command.toString()) {
            @Override
            protected void onDone(MIRecord record) {
                breakpointsActivated = true;
                for (Handler h : handlers) {
                    h.breakpoint().update();
                }
                finish();
            }
        };
        sendCommandInt(cmd);
    }

    @Override
    public void postDeactivateBreakpoints() {
        final Handler[] handlers = bm().getHandlers();

        // no need to enable/disable if there is no handlers
        if (handlers.length == 0) {
            return;
        }

        StringBuilder command = new StringBuilder();
        command.append("-break-disable"); // NOI18N

        for (Handler h : handlers) {
            if (h.breakpoint().isEnabled()) {
                command.append(' ');
                command.append(h.getId());
            }
        }

        MICommand cmd = new MIBreakCommand(0, command.toString()) {
            @Override
            protected void onDone(MIRecord record) {
                breakpointsActivated = false;
                for (Handler h : handlers) {
                    h.breakpoint().update();
                }
                finish();
            }
        };
        sendCommandInt(cmd);
    }

    @Override
    public void postDeleteAllHandlersImpl() {
        final Handler[] handlers = bm().getHandlers();

        // no need to enable/disable if there is no handlers
        if (handlers.length == 0) {
            return;
        }

        // To test error recovery:
        // gdb sent back a &"warning: ..." and a ^done and seemed to have
        // processed everything following junk.
        //
        // However I'm still wary that under some circumstances we'll get
        // a partial result, where only some of the breakpoint would have
        // truly been deleted.
        //
        // hids += " " + "junk";

        StringBuilder command = new StringBuilder("-break-delete"); //NOI18N

        for (Handler h : handlers) {
            command.append(' ');
            command.append(h.getId());
        }

        MICommand cmd = new MIBreakCommand(0, command.toString()) {
            @Override
            protected void onDone(MIRecord record) {
                for (Handler h : handlers) {
                    bm().deleteHandlerById(0, h.getId());
                }
                finish();
            }
        };
        sendCommandInt(cmd);
    }

    @Override
    public void postDeleteHandlerImpl(final int rt, final int hid) {
        MICommand cmd = new MIBreakCommand(rt, "-break-delete " + hid) { // NOI18N
            @Override
            protected void onDone(MIRecord record) {
                bm().deleteHandlerById(rt, hid);
                finish();
            }
        };
        sendCommandInt(cmd);
    }

    @Override
    public void postCreateHandlerImpl(int routingToken, HandlerCommand hc) {
        final MICommand cmd = new MIBreakLineCommand(routingToken, hc.getData());
        sendCommandInt(cmd);
    }

    @Override
    public void postChangeHandlerImpl(int rt, HandlerCommand hc) {
        assert hc instanceof GdbHandlerCommand;
        GdbHandlerCommand ghc = (GdbHandlerCommand)hc;

        // build a chain of gdb commands
        MiCommandImpl cmd = null;
        while (ghc != null) {
            switch (ghc.getType()) {
                case CHANGE:
                    MiCommandImpl changeCmd = new MIChangeBreakCommand(rt, ghc);
                    if (cmd != null) {
                        cmd.chain(changeCmd, null);
                    } else {
                        cmd = changeCmd;
                    }
                    break;
                case REPLACE:
                    MICommand old = cmd;
                    cmd = new MIReplaceBreakLineCommand(rt, ghc);
                    cmd.chain(old, null);
                    break;
            }
            ghc = ghc.getNext();
        }
        if (cmd != null) {
            sendCommandInt(cmd);
        }
    }

    @Override
    public void postRepairHandlerImpl(int rt, HandlerCommand hc) {
        assert hc instanceof GdbHandlerCommand;
        GdbHandlerCommand ghc = (GdbHandlerCommand)hc;
        final MICommand cmd = new MIRepairBreakLineCommand(rt, ghc);
        sendCommandInt(cmd);
    }

    public void setHandlerCountLimit(int hid, long countLimit) {
        notImplemented("setHandlerCountLimit()");	// NOI18N
    }

    @Override
    public void postEnableHandler(int rt, final int hid, final boolean enable) {
        String cmdString;
        if (enable) {
            cmdString = "-break-enable "; // NOI18N
        } else {
            cmdString = "-break-disable "; // NOI18N
        }

        MICommand cmd = new MIBreakCommand(rt, cmdString + hid) {
            @Override
            protected void onDone(MIRecord record) {
                // SHOULD factor with code in Dbx.java
                Handler handler = bm().findHandler(hid);
                if (handler != null) {
                    handler.setEnabled(enable);
                }
                finish();
            }
        };
        sendCommandInt(cmd);
    }

    // interface NativeDebugger
    @Override
    public void postVarContinuation(VarContinuation vc) {
        notImplemented("postVarContinuation");	// NOI18N
    }

    @Override
    protected void postVarContinuation(int rt, VarContinuation vc) {
        notImplemented("postVarContinuation");	// NOI18N
    }

    // interface GdbDebugger 
    public void runArgs(String args) {
        sendSilent("-exec-arguments " + args); // NOI18N
    }

    public void runDir(String dir) {
        dir = localToRemote("runDir", dir); // NOI18N
        String cmdString = peculiarity.environmentCdCommand() + " " + dir; // NOI18N
        sendSilent(cmdString);
    }

    void setEnv(String envVar) {
        sendSilent("set environment " + envVar); // NOI18N
    }

    void unSetEnv(String envVar) {
        sendSilent("unset environment " + envVar); // NOI18N
    }

    private static String quoteValue(String value) {
        int length = value.length();
        if (length > 1) {
            return value.replace("\"", "\\\""); //NOI18N
        }
        return value;
    }

    private static String unquoteValue(String value) {
        int length = value.length();
        if (length > 1) {
            return value.replace("\\\"", "\""); //NOI18N
        }
        return value;
    }

    void assignVar(final GdbVariable var, final String value, final boolean miVar) {
        String cmdString;
        String quotedValue = value;

        if ( (quotedValue.indexOf(' ') >= 0) && (quotedValue.indexOf(' ') < quotedValue.indexOf('"')) ) { // NOI18N
            quotedValue = quotedValue.substring(quotedValue.indexOf('"')); // NOI18N
        }

        quotedValue = quoteValue(quotedValue);

        if (miVar) {
            cmdString = "-var-assign " + var.getMIName() + " " + quotedValue; // NOI18N
        } else {
            cmdString = "-data-evaluate-expression \"" +  //NOI18N
                    var.getFullName() + '=' + quotedValue + '"'; // NOI18N
        }
        MICommand cmd =
            new MiCommandImpl(cmdString) {

            @Override
            protected void onDone(MIRecord record) {
//                GdbFrame currentFrame = getCurrentFrame();
//                if (currentFrame != null) {
//                    currentFrame.varUpdated(var.getFullName(), value);
//                }
                updateMIVar();
                finish();
            }

            @Override
            protected void onError(MIRecord record) {
                String errMsg = getErrMsg(record);

                // to work around gdb "corrupt stack" problem
                if (try_one_more && errMsg.equals(corrupt_stack)) {
                    try_one_more = true;
                    //updateMIVar();
                }
                // to work around gdb "out of scope" problem
                String out_of_scope = "mi_cmd_var_assign: Could not assign expression to varible object"; // NOI18N
                if (!errMsg.equals(out_of_scope)) {
                    genericFailure(record);
                    finish();
                }
            }
        };
        gdb.sendCommand(cmd);
    }

    /**
     * Called when this session has been switched to
     * 'redundant' is true when we double-click on the same session.
     * @param redundant
     */
    @Override
    public void activate(boolean redundant) {

        if (isConnected()) {

            super.activate(redundant);

        } else {
            // See big comment in dbx side
            updateActions();
        }

        if (redundant) {
            return;
        }
    }

    /**
     * Called when this session has been switched away from
     * 'redundant' is true when we double-click on the same session.
     * @param redundant
     */
    @Override
    public void deactivate(boolean redundant) {
        super.deactivate(redundant);
        if (redundant) {
            return;
        }
    }

    // interface NativeDebugger
    @Override
    public void setCurrentDisLine(Line l) {
        notImplemented("setCurrentDisLine");	// NOI18N
    }

    // interface NativeDebugger
    @Override
    public Line getCurrentDisLine() {
        return null;
    }

    @Override
    public void notifyUnsavedFiles(String file[]) {
    }

    // interface NativeDebugger
    @Override
    public void stepOutInst() {
        execFinish();
    }

    // interface NativeDebugger
    @Override
    public void stepOverInst() {
        sendResumptive(peculiarity.execNextInstCommand(currentThreadId));
    }

    // interface NativeDebugger
    @Override
    public void stepInst() {
        sendResumptive(peculiarity.execStepInstCommand(currentThreadId));
    }

    // interface NativeDebugger
    @Override
    public void postRestoring(boolean restoring) {
    }

    // interface NativeDebugger
    @Override
    public void forkThisWay(NativeDebuggerManager.FollowForkInfo ffi) {
        notImplemented("forkThisWay");	// NOI18N
    }

    // interface NativeDebugger
    @Override
    public void fix() {
        notImplemented("fix");	// NOI18N
    }

    @Override
    public FormatOption[] getEvalFormats() {
        return null; // gdb does not support eval formats
    }

    // interface NativeDebugger
    @Override
    public void exprEval(FormatOption format, final String expr) {
        String cmdString = "-data-evaluate-expression " + "\"" + quoteValue(expr) + "\""; // NOI18N
        MICommand cmd = new MiCommandImpl(cmdString) {
            @Override
            protected void onDone(MIRecord record) {
                final String res;
                if (!record.isError()) {
                    res = record.results().getConstValue("value"); //NOI18N
                } else {
                    res = record.error();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        EvaluationWindow evalWindow = EvaluationWindow.getDefault();
                        evalWindow.open();
                        evalWindow.requestActive();
                        evalWindow.componentShowing();
                        evalWindow.evalResult(expr + " = " + unquoteValue(res) + "\n"); //NOI18N
                    }
                });
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }

    // interface NativeDebugger
    @Override
    public void execute(String cmd) {
        notImplemented("execute");	// NOI18N
    }


    // implement NativeDebuggerImpl
    @Override
    protected void stopUpdates() {
        // no-op for now
    }

    // implement NativeDebuggerImpl
    @Override
    protected void startUpdates() {
        // no-op for now
    }

    /*package*/void sendTerminalTypedCommand(String termLine) {
        TermCommandImpl cmd = new TermCommandImpl(termLine);
        gdb.sendCommand(cmd);
    }

    private void send(String commandStr, boolean reportError) {
        MiCommandImpl cmd = new MiCommandImpl(commandStr);
        if (!reportError) {
            cmd.dontReportError();
        }
        gdb.sendCommand(cmd);
    }

    private void sendSilent(String commandStr) {
        send(commandStr, false);
    }

    private void send(String commandStr) {
        send(commandStr, true);
    }

    private void sendResumptive(String commandStr) {
        MICommand cmd = new MIResumptiveCommand(commandStr);
        gdb.sendCommand(cmd, true);
    }

    private void sendCommandInt(MICommand cmd) {
        if (postedKill || postedKillEngine || gdb == null || cmd == null) {
            LOG.log(Level.FINE, "sendCommandInt when session is finished for example, see values to get more information postedKill= {0} "//NOI18N
                    + "postedKillEngine = {1} gdb == {2} || cmd == {3}",  //NOI18N
                    new String[]{postedKill + "", "" + postedKillEngine, gdb == null ? "null" : "Not null",  //NOI18N
                        cmd == null ? "null" : "Not null"});//NOI18N
            return;
        }
        pause(true);
        gdb.sendCommand(cmd);
    }

    @Override
    public void registerRegistersWindow(RegistersWindow w) {
        if (w != null) {
            requestRegisters();
        }
    }

    @Override
    public void assignRegisterValue(String register, String value) {
        MICommand cmd = new MiCommandImpl("-gdb-set $" + register + " = " + value) { //NOI18N

            @Override
            protected void onDone(MIRecord record) {
                requestRegisters();
                finish();
            }
        };

        gdb.sendCommand(cmd);
    }

    void createWatchFromVariable(GdbVariable var) {
        MiCommandImpl cmd = new MiCommandImpl("-var-info-path-expression " + var.getMIName()) { //NOI18N
            @Override
            protected void onDone(MIRecord record) {
                if (!record.isEmpty()) {
                    String expr = record.results().getConstValue("path_expr", null); //NOI18N
                    if (expr != null) {
                        manager().createWatch(expr);
                    }
                }
                finish();
            }
        };
        gdb.sendCommand(cmd);
    }
}
