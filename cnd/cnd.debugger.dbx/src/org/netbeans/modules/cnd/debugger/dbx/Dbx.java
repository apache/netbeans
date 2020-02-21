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

import java.io.IOException;
import java.util.Vector;

import org.openide.ErrorManager;


import com.sun.tools.swdev.glue.GStr;
import com.sun.tools.swdev.glue.Master;
import com.sun.tools.swdev.glue.Notifier;

import com.sun.tools.swdev.glue.dbx.*;

import org.netbeans.modules.cnd.debugger.common2.utils.Executor;
import org.netbeans.modules.cnd.debugger.common2.utils.ItemSelectorResult;

import org.netbeans.modules.cnd.debugger.common2.values.EditUndo;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeSession;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.Location;
import org.netbeans.modules.cnd.debugger.common2.debugger.RoutingToken;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.Handler;

import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcModel;
import org.netbeans.modules.cnd.debugger.dbx.rtc.GpRtcUtil;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionLayers;
import org.openide.windows.InputOutput;


/**
 * dbx surrogate
 *
 * was: Dbx, but the older Dbx contained a whole lot of semantics and state
 *      which has all moved to DbxDebugger now .
 *      This one is just a simple glue bridge now.
 */

public final class Dbx extends CommonDbx {

    static class DbxFactory extends Factory {
	public DbxFactory(Executor executor, 
			  String additionalArgv[],
			  Listener listener,
			  boolean exec32,
			  boolean shortNames,
			  String dbxInit,
			  Host host,
			  boolean connectExisting,
                          String dbxName,
                          InputOutput io,
                          NativeDebuggerInfo ndi,
                          OptionLayers optionLayers) {
	    super(executor, additionalArgv, listener, exec32, shortNames,
		    dbxInit, host, connectExisting, dbxName, io, ndi, optionLayers);
	}

	protected Dbx getDbx(Factory factory, Notifier n, int flags,
			      boolean connectExisting, Master master) {
	    return new Dbx(factory, n, flags, connectExisting, master);
	}
    }

    private DbxDebuggerImpl debugger;

    private Location visitLoc;
    private Location homeLoc;		// was currentLoc

    public Location getVisitLoc() {
	return visitLoc;
    }

    public Location getHomeLoc() {
	return homeLoc;
    }

    /** Should we block input? */
    private boolean blockInput = false;

    /** Is dbx blocking input? */
    public boolean isDbxBusy() {
        return blockInput;
    }

    /** Timer used to suppress cursor updates for a short interval
        in case dbx is about to unblock */
    private javax.swing.Timer cursorTimer; // make sure we get the right one

    /** Whether or not the debugger is already thought to be blocked */
    private boolean guiBlocked = false;

    // True while dbx is processing commands in the user's .dbxrc file etc.
    private boolean dbxInitializing = false;

    /** The business of dbx has changed */
    public void dbxBusyChanged() {
	debugger.state().isBusy = isDbxBusy();
	debugger.stateChanged();
    }

    private static NativeDebuggerManager manager() {
	return NativeDebuggerManager.get();
    }

    private DbxDebuggerSettingsBridge dbxProfileBridge() {
	return (DbxDebuggerSettingsBridge) debugger.profileBridge();
    }
    
    @Override
    public void initializeDbx() {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug)
	    System.out.printf("Dbx.initializeDbx()\n"); // NOI18N
	super.initializeDbx();
	debugger.initializeDbx();
	if (! debugger.willBeLoading())
	    startProgressManager().finishProgress();
    }

    boolean dbxInitializing() {
	return dbxInitializing;
    } 

    // True when we're in the process of finishing a program.
    // For example, handler_delete messages received while finishing
    // do not represent user deletions of handlers, it means get rid
    // of the handlers in the editor since we're closing the program
    private boolean isFinishing = false;

    boolean isFinishing() {
	return isFinishing;
    }


    /**
     * Utility class to help us deal with GPDbxLocation
     */

    private static class GlueLocation extends Location {

	public static Location make(NativeDebugger debugger, GPDbxLocation dl) {
	    int flags = UPDATE;
	    if ((dl.flags & GPDbxLocation.NO_UPDATE) != 0)
		flags &= ~ UPDATE;
	    if ((dl.flags & GPDbxLocation.CALLED) != 0)
		flags |= CALLED;
	    if ((dl.flags & GPDbxLocation.TOPFRAME) != 0)
		flags |= TOPFRAME;
	    if ((dl.flags & GPDbxLocation.BOTTOMFRAME) != 0)
		flags |= BOTTOMFRAME;
	    if ((dl.flags & GPDbxLocation.SRC_OOD) != 0)
		flags |= SRC_OOD;

	    // dbx sometimes sends a null src but doesn't set NO_SRC.
	    // SHOULD fix in dbx!
	    if ((dl.flags & GPDbxLocation.NO_SRC) != 0)
		dl.src = null;

	    dl.src = debugger.remoteToLocal("GlueLocation", dl.src); // NOI18N

	    return new GlueLocation(dl.src, dl.line, dl.func, dl.pc, flags);
	}

	private GlueLocation(String src, int line, String func, long pc,
			     int flags) {
	    super(src, line, func, pc, flags, null);
	}

    }

    public Dbx(Factory factory, Notifier n, int flags, boolean connectExisting,
	    						Master master) {
	super(factory, n, flags, connectExisting, master);
    }

    public final void setDebugger(DbxDebuggerImpl debugger) {
	this.debugger = debugger;
	debugger.getNDI().setLoadSuccess(false);
    }


    /**
     * Interrupt the program (note that unlike almost every other debugging
     * action, we're NOT asking dbx to do it - this we're actually doing
     * ourselves!!
     *
     * was: Dbx.interrupt()
     */
    public void pause() {
	// The following predicate is _not_ the same as isReceptive()
	if (debugger.state().isRunning && debugger.state().isProcess) {
	    interrupt();
	}
    }

    /**
     * Return true if dbx is receptive to commands.
     * Dbx is receptive is
     * - there is no debuggee running
     * LATER:
     * - dbx is not in the middle of processing some user command or a long
     *   ksh command execution (see busy(), block_input);
     *
     * See also DbxDebuggerImpl.notePileup().
     */

    public boolean isReceptive() {
	return ! debugger.state().isRunning;
    }


    /**
     * If needed ...
     * Interrupt the program quietly (meaning: don't send stack updates,
     * display updates, etc.
     *
     * Return whether the program was running.
     *
     * was: private
     * The only external use of this was from the __Collector__ panel.
     * I'd rather it use sendcommandInt or something similarly structured 
     * instead of calling this function directly.
     */

    private boolean interruptQuietly() {
	boolean wasRunning = debugger.state().isRunning;

	if (!this.isReceptive()) {
	    // Get dbx to listen to us.
	    try {
		// The first argument is the signal: 3 is SIGQUIT
		// the second argument is a value understood by dbx
		// to mean "quiet, please".
		getExecutor().sigqueue(3, 2);

	    } catch (IOException e) {
		ErrorManager.getDefault().annotate (e,
		    "Sending kill signal to process group failed"); // NOI18N
		ErrorManager.getDefault().notify(e);
	    }
	}
	return wasRunning;
    }


    /**
     * Common dbx command sender.
     */
    @Override
    protected void sendCommandHelp(int routingToken, int flags, String cmd) {
	EditUndo.advance();
	super.sendCommandHelp(routingToken, flags, cmd);
    }


    /**
     * Send a command to dbx.
     *
     * If the process is running, i.e. dbx isn't listening, put up
     * a waring to the suer and abort the command.
     */
    public void sendCommand(int routingToken, int flags, String cmd) {

	if (!isReceptive()) {

	    // Actions that require that dbx be "receptive" should be disabled
	    // and not be able to reach here. But we put up this warning
	    // message in order to catch mismatches tat fall through the
	    // cracks. In the past sendCommand() would blindly forward
	    // commands to dbx which was bad for these reasons:
	    // - user would see no side-effect
	    // - the side-effects would bunch up and all happen at the next
	    //   stoppage in an unexpected way.
	    // - too many bunched up actions and the glue socket might fill up
	    //   increasing the potential for deadlocks.

	    NativeDebuggerManager.warning(Catalog.get("ActionIgnored")); // NOI18N
	    return;
	}

	sendCommandHelp(routingToken, flags, cmd);
    }

    /**
     * Execute a bunch of code which is likely to interact with dbx
     * possibly interrupting dbx if there is a running process and resuming
     * it thereafter.
     *
     * The origin of this technique goes back to 
     * 4048756: Easy way to set a breakpoint in a running program
     *
     * was: ... inlined all over Dbx
     */

    public void runCommandInt(Runnable runnable) {
	boolean wasRunning = interruptQuietly();

	runnable.run();

	if (wasRunning) {
	    /* OLD

            // Restart the program. Set the flag to nonrunning first
            // since it may not have been informed by dbx of the interruption
            // and might therefore ignore the go instruction.
            //
            // I'm assuming here that when I interrupted dbx, it
            // was running (not stepping). This seems like a safe
            // assumption since it would be difficult to hit the
            // stop-at icon immediately after hitting step.
            // XXX tor, don't we have a last-run command for this?
            // used for the follow-fork scheme?
            state.setRunning(false);
            go();
            // go() doesn't set the running flag (it's based on the timer)
            // so correct that now...
            state.setRunning(true);
	    OLD */

	    // go() calls runProgram() which checks isRunning which explains
	    // the mucking with 'setRunning' above.
	    //
	    // Maybe we can be simpler? and bypass go() and runProgram()?

	    ksh_cmd(0, 0, "cont");	// NOI18N
	}
    }

    /**
     * Send a command to dbx possibly interrupting it if there is a
     * running process and resuming it thereafter.
     */

    public void sendCommandInt(final int routingToken, final int flags,
			       final String cmd) {

	runCommandInt(new Runnable() {
	    public void run() {
		sendCommandHelp(routingToken, flags, cmd);
	    }
	} );
    }


    /**
     * Send a command to dbx possibly interrupting it if there is a
     * running process but not resuming it after.
     *
     * Relevant for commands that destroy the process, so there's
     * nothing to resume.
     */

    public void sendCommandIntNoresume(int routingToken, int flags,String cmd) {
	interruptQuietly();
	sendCommandHelp(routingToken, flags, cmd);
    }


    /**
     * Get the value of the given "event variable" like $vfunc, $scope etc.
     * 
     * was: Dbx.getCurrentClass()
     */

    public String getVar(String varName) {

	if (!isReceptive()) {
	    // See comment in sendCommand()
	    NativeDebuggerManager.warning(Catalog.get("ActionIgnored")); // NOI18N
	    return null;
	}

	GStr out = new GStr();
	ksh_scmd(0, 0, out, "kprint $" + varName); // NOI18N
	String value = out.value();

	// OLD:
	// we used to strip newlines  ... that should be fixed in dbx, not here

	if (value.length() == 0)
	    return null;
	else
	    return value;
    }
    
    /**
     * Dbx has died
     */
    @Override
    public final void rude_disconnect() {
	super.rude_disconnect();
	debugger.rudeDisconnect();
    }

    @Override
    public final void disconnected() {
	super.disconnected();
	if (debugger != null)
	    debugger.kill();
    }

    @Override
    protected final void output(String str, boolean ready) {
    }

    @Override
    protected final void dir_changed(String dir) {
	debugger.profileBridge().noteRunDir(dir);
    }

    @Override
    protected final void ksh_notify(int argc, String argv[]) {
    }

    @Override
    protected final void ksh_scmd_result(int rt, String result) {
	debugger.handleVarContinuation(rt, result);
    }

    @Override
    protected final void jn_mode_update(int mode) {
	debugger.setJavaMode(mode);
    }

    @Override
    protected final void prog_finished(String progname) {
	isFinishing = true;     // See 4529941
    }

    @Override
    protected final void prog_loading(String progname) {
	isFinishing = false;

	debugger.state().isLoading = true;
	debugger.stateChanged();

	debugger.session().setSessionState(debugger.state());

	// We might get a prog_loading on an existing session like
	// when user issues 'debug' or follows and exec. So make 
	// sure we have a progress dialog up.

	// SHOULD we not pass a hostname here?
	startProgressManager().startProgress(cancelListener,
					     debugger.isShortName());

	startProgressManager().updateProgress('>', 0,
	    Catalog.get("LoadProg") + debugger.shortname(progname), 0, 0);
    }

    @Override
    protected final void prog_loaded(String progname, boolean success) {
	debugger.state().isLoading = false;
	debugger.state().isLoaded = success;
	debugger.stateSetRunning(false);
	debugger.stateChanged();

	debugger.session().setSessionState(debugger.state());
	debugger.session().setTarget(progname);
	debugger.session().update();
	debugger.session().setSessionEngine(
		DbxEngineCapabilityProvider.getDbxEngineType());

	if (!factory().connectExisting())
	    startProgressManager().updateProgress('<', 0, null, 0, 0);

	if (!success) {
	    // progress indicator will be cancelled by error?
	    if (NativeDebuggerManager.isStartModel())
	        debugger.postKill();
	    debugger.noteProgLoadFailed();
	    return;
	} else {
	    // OLD moved to DbxDebuggerImpl.noteProgLoaded
	    // OLD manager().cancelProgress();
	}

	if (debugger.noteProgLoaded(progname) ) {
	    debugger.getNDI().setLoadSuccess(true);
	    startProgressManager().finishProgress();

	    // record loadobjs
	    String[] loadobjs_array = new String[loadobjs.size()];
	    loadobjs_array = loadobjs.toArray(loadobjs_array);
	    debugger.rtcModel().getProfile().setLoadobjs(loadobjs_array);
	    loadobjs.removeAllElements();

	    // Threads tab on if MT program loaded
	    if (debugger.isMultiThreading()) {
		NativeDebuggerManager.openComponent("threadsView", false); // NOI18N
	    }
	    // Already done in noteProgLoaded
	    // OLD manager().enableConsoleWindow();
	}
    }

    @Override
    protected final void prog_unloaded() {
        /* This triggered bug 4663131
           jdbx: Debugging toolbars are disabled after running first debugging step.
           in dbx. They may not get it fixed for Krakatoa so let's just
           ignore this message for now - no great harm done since it's
           not really possible (for a user) to "unload" a program from dbx;
           all they can do is load a new program, where temporarily the
           first program is unloaded (the below state), but since it's just
           a transition we can ignore it. (It's possible that loading the
           new program fails, in which case we'd have problems - but that
           scenario is less problematic than the current broken behavior.

           XXX Remove this when dbx is fixed!

	debugger.state.isLoaded = false;
	debugger.state.isRunning = false;
	debugger.stateChanged();

	debugger.session().setSessionState(debugger.state); 
     */
	if (debugger.session() != null) {
	    debugger.session().setTarget(null);
	    debugger.session().setSessionEngine(null);
	    debugger.session().update();
	}

	debugger.noteProgUnloaded();
    }

    @Override
    protected final void prog_visit(GPDbxLocation vl) {
	Location loc = GlueLocation.make(debugger, vl);
	loc.setVisited(! loc.equals(homeLoc));
	debugger.state().isLoaded = true; // 6588235
	//loc.warnAboutNoSource = true;
	if (loc.update()) {
	    debugger.setVisitedLocation(loc);
	}
	visitLoc = loc;

	// The up/down buttons should be insensitive since I'm not
	// showing a location on the stack. This is the case when
	// a frame is both on the top and on the bottom of the stack.
	debugger.state().isDebuggerCall = loc.called();
	debugger.state().isUpAllowed = false;
	debugger.state().isDownAllowed = false;
	debugger.stateChanged();
    }

    @Override
    protected final void prog_runargs(int argc, String argv[]) {
	debugger.profileBridge().noteRunArgs(argv);
    }

    @Override
    protected final void prog_redir(String infile, String outfile,
				    boolean append) {
	debugger.profileBridge().noteRedir(infile, outfile, append);
    }

    @Override
    protected final void prog_datamodel(int bit_width) {
	debugger.state().is64bit = (bit_width == 64);
	debugger.stateChanged();
    }

    @Override
    protected final int popup(int rt, String title, int nitems, String item[],
	    						boolean cancelable) {
	/*
	 * This is for really old dbx's there's nothing to do now.
	 */
	return 0;
    }

    @Override
    protected final int popup2(int rt, String title, int nitems, String item[],
	    	boolean cancelable, boolean multiple_selection, String cookie) {

	ItemSelectorResult result;
        if ("eventspec".equals(cookie)) { // NOI18N
	    // Special handling for breakpoint overloading
	    /* LATER
            if (Log.Bpt.pathway) {
                System.out.printf("DebuggerManager.popup()" +
                    "cookie '%s' items %d\n",
                    cookie, nitems);
            }
	    */

            assert rt == 0 || // spontaneous from cmdline
                RoutingToken.BREAKPOINTS.isSameSubsystem(rt);
            result = debugger.bm().noteMultipleBreakpoints(rt, title, nitems, item);
        } else {
	    result = manager().popup(rt, cookie, debugger,
						      title,
						      nitems, item,
						      cancelable,
						      multiple_selection);
	}


	// All this rigamarole is to pass an 'int' back by reference
	// GLIC doesn't allow 'out' parameters for scalars.
	// That in-turn is because Java doesn't handle them gracefully.

	int newRT[] = new int[1];
	newRT[0] = result.getRoutingToken();

	if (result.isCancelled()) {
	    popup2_selection(0, null, 1, newRT);

	} else {
	    int nselections = result.nSelected();
	    popup2_selection(nselections, result.selections(), 1, newRT);
	}
	return 0;
    }

    @Override
    protected final void clone(int argc, String argv[],
			       boolean cloned_to_follow) {

        debugger.getNDI().setTarget("");
	debugger.clone(argv, cloned_to_follow);
    }

    @Override
    protected final void busy(String with_what,
			      boolean on_off,
			      boolean block_input) {

	if ("initialization".equals(with_what)) {	// NOI18N
	    dbxInitializing = on_off;
	    if (!dbxInitializing) {
		debugger.profileBridge().noteInitializationDone();
	    }
	}

        /*
        // Use a timer based scheme since we get LOTS of little messages
        // from dbx about block switches (3 or 4 for a single single-line
        // command!)
        */
        if (blockInput != block_input) {
            blockInput = block_input;
            if (blockInput) {
                if (!guiBlocked) {
                    if (cursorTimer == null) {
                        cursorTimer = new javax.swing.Timer(250,
                                         new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     if (guiBlocked) {
                         dbxBusyChanged();
                     }
                 }});
                        cursorTimer.setRepeats(false);
                        cursorTimer.setCoalesce(true);
                        cursorTimer.start();
                    } else {
                        cursorTimer.restart();
                    }
                    guiBlocked = true;
                }
            } else {
                if (cursorTimer != null) {
                    cursorTimer.stop();
                    // XXX cursorTimer = null; ????
                }
                if (guiBlocked) {
                    dbxBusyChanged();
                }
                guiBlocked = false;
            } // end of if (blockInput)
        }

    }

    @Override
    protected final void capabilities(GPDbxCapabilities capabs) {
	debugger.state().multi_threading = capabs.multi_threading;
	debugger.state().capabAccess = capabs.rtc;
	debugger.state().capabMprof = capabs.mprof;
	debugger.state().capabCollector = capabs.mt_collector;
	debugger.state().capabilities = capabs.capabilities;
	debugger.stateChanged();

	if (debugger.isMultiThreading() &&
		!NativeDebuggerManager.isComponentOpened("threadsView")) //NOI18N
	    NativeDebuggerManager.openComponent("threadsView", false); // NOI18N

    }

    @Override
    protected final void thread_capabilities(GPDbxThreadCapabilities thread_capabs) {
    }

    @Override
    protected final void button(String label, String cmd, String option) {
	// SHOULD provide better (popup) error message
	manager().formatStatusText("ButtonIgnored",		// NOI18N
				   null);
    }

    @Override
    protected final void unbutton(String label) {
    }

    @Override
    protected final void env_changed(String name, String new_value) {
	// we don't care about environment variables set through the .dbxrc
	if (dbxInitializing)
	    return;
	debugger.profileBridge().noteEnvVar(name, new_value);
    }

    @Override
    protected final boolean load_symbols(int rtRoutingToken) {
	return true;
    }

    @Override
    protected final boolean rcmd(String hostString, int pid, String cmdString) {
	return false;
    }

    @Override
    protected final void rconnect(com.sun.tools.swdev.glue.NetAddr addrNetAddr,
						    String hostnameString) {
    }

    @Override
    protected final void rgrab_attention() {
    }

    @Override
    protected final void rlist(GPDbxRList list) {
    }

    @Override
    protected final boolean rswitch(String host, int pid) {
	return false;
    }

    @Override
    protected final void rmove(boolean backward) {
    }

    @Override
    protected final void manifest_mark(String mark, GPDbxLocation location) {
    }

    @Override
    protected final void bpt_set(int id, String filename, int line,
						GPDbxLocation loc) {
	// see dbx bug 1232137 for a history of handlers vs bpts
	Handler h = debugger.bm().findHandler(id);
	if (h != null) {
	    filename = debugger.remoteToLocal("bpt_set", filename); // NOI18N
	    h.breakpoint().addAnnotation(filename, line, loc.pc);
        }
    }

    @Override
    protected final void bpt_del(int id) {
	Handler h = debugger.bm().findHandler(id);
	if (h != null) {
	    h.breakpoint().removeAnnotations();
        }
    }


    @Override
    protected final void handler_new(GPDbxHandler h, int rt) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Bpt.pathway)
	    System.out.printf("handler_new(, rt %d)\n", rt); // NOI18N
	debugger.newHandler(rt, h);
	NativeDebuggerManager.get().bringDownDialog();
    }

    @Override
    protected final void handler_replace(GPDbxHandler h, int rt) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Bpt.pathway)
	    System.out.printf("handler_replace(, rt %d)\n", rt); // NOI18N
	Handler handler = debugger.bm().findHandler(h.id);
	if (handler != null) {
	    debugger.replaceHandler(rt, handler, h);
	    NativeDebuggerManager.get().bringDownDialog();
	}
    }

    @Override
    protected final void handler_delete(int id, int rt) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Bpt.pathway)
	    System.out.printf("handler_delete(id %d, rt %d)\n", id, rt); // NOI18N
	debugger.bm().deleteHandlerById(rt, id);
    }

    @Override
    protected final void handler_defunct(int id, int rt) {
	Handler handler = debugger.bm().findHandler(id);
	if (handler != null) {
	    debugger.setHandlerDefunct(handler, rt, true);
	}
    }

    @Override
    protected void handler_undo_defunct(int id, int rt) {
	Handler handler = debugger.bm().findHandler(id);
	if (handler != null) {
	    debugger.setHandlerDefunct(handler, rt, false);
	}
    }


    @Override
    protected final void handler_enable(int id, boolean v) {
	Handler handler = debugger.bm().findHandler(id);
	if (handler != null) {
            if (debugger.areBreakpointsActivated()) {
                handler.setEnabled(v);
            } else {
                handler.breakpoint().update();
            }
        }
    }

    @Override
    protected final void handler_count(int id, int current, int limit) {
	Handler handler = debugger.bm().findHandler(id);
	if (handler != null) {
		handler.setCount(current);
		handler.setCountLimit(limit, true);
	}
    }

    @Override
    protected final void handler_list(int count, GPDbxHandler list[]) {
    }

    @Override
    protected final void handler_batch_begin() {
	debugger.bm().breakpointUpdater().batchOn();
    }

    @Override
    protected final void handler_batch_end() {
	debugger.bm().breakpointUpdater().batchOff();
    }

    @Override
    protected final void display_item_new(int id, String plain_lhs, int rt, 
	    String qualified_lhs, String static_type, int is_a_pointer,
	    String reevaluable_lhs, boolean unrestricted) {
	/* OLD
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Watch.pathway)
	    System.out.println("display_item_new(, rt %d)\n", rt);
	boolean restricted = ! unrestricted;
	debugger.newWatch(rt, id, plain_lhs, static_type, is_a_pointer, restricted);
	DebuggerManager.get().bringDownDialog();
	*/
    }

    @Override
    protected final void display_item_new2(int rt, GPDbxDisplaySpec spec) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Watch.pathway) {
	    System.out.printf("display_item_new2('%s', rt %d)\n", // NOI18N
		spec.qualified_lhs, rt);
	}
	debugger.newWatch(rt, spec);
	NativeDebuggerManager.get().bringDownDialog();
    }

    @Override
    protected final void display_item_dup(int rt, int id) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Watch.pathway)
	    System.out.printf("display_item_dup(rt %d, id %d)\n", rt, id); // NOI18N
	debugger.dupWatch(rt, id);
	NativeDebuggerManager.get().bringDownDialog();
    }

    @Override
    protected final void display_item_delete(int id, int rt) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Watch.pathway)
	    System.out.printf("display_item_delete(, rt %d)\n", rt); // NOI18N
	debugger.deleteWatchById(rt, id);
    }

    @Override
    protected final void display_update_0(int nitems, GPDbxDisplayItem0 items[]) {
	// Empty stub - this is when we're connecting to Nozomi dbx (or older)
	// which we don't support.
    }

    @Override
    protected final void display_update(int nitems, GPDbxDisplayItem items[]) {
	debugger.updateWatches(items);
    }

    @Override
    protected final void disassembly(GStr dis_str) {
	debugger.setDis(dis_str.value());
    /* LATER
	Location vloc = new Location(vl);
	vloc.visited = !vloc.equals(homeLoc);
    */
    }

    @Override
    protected final void memorys(GStr mem_str) {
	debugger.setMems(mem_str.value());
    }

    @Override
    protected final void registers(GStr regs_str) {
	debugger.setRegs(regs_str.value());
    }

    @Override
    protected final void locals(int nitems, GPDbxLocalItem items[]) {
	debugger.setLocals(items);
    }

    @Override
    protected final void expanded_nodes(boolean is_local, int nitems,
					    GPDbxLocalItem items[]) {
	debugger.setExpandedNodes(is_local, items);
    }

    @Override
    protected final void vitem_new(int rt, GPDbxVItemStatic sitem) {
	debugger.addNewVItem(rt, sitem);
    }

    @Override
    protected final void vitem_replace(int rt, GPDbxVItemStatic sitem) {
    }

    @Override
    protected final void vitem_add(GPDbxVItemStatic sitem, int id) {
    }

    @Override
    protected final void vitem_delete(int id, boolean proc_gone) {
	debugger.deleteVItem(proc_gone, id);
    }

    @Override
    protected final void vitem_update(int nitems, GPDbxVItemDynamic items[]) {
	debugger.updateVItem(nitems, items);
    }

    @Override
    protected final void vitem_update_mode(int id, int new_mode) {
    }

    @Override
    protected final void vitem_timer(float seconds) {
    }

    @Override
    protected final void prop_decl(int nprop, GPDbxPropDeclaration prop[]) {
    }

    @Override
    protected final void prop_changed(String name, String new_value) {
	// strip "DBX_" prefix if any
	if (name.startsWith("DBX_")) { // NOI18N
	    name = name.substring(4);
	}

	debugger.debuggingOptionChanged(name, new_value);
    }

    @Override
    protected final void signal_list(int count,
				     GPDbxSignalInfoInit initial_signal_list[]){
	dbxProfileBridge().noteSignalList(initial_signal_list);
    }

    @Override
    protected final void signal_list_state(GPDbxSignalInfo updated_signal){
	dbxProfileBridge().noteSignalState(updated_signal);
    }

    @Override
    protected final void pathmap_list(int count,
				      GPDbxPathMap updated_pathmap[]) {
	dbxProfileBridge().notePathmap(updated_pathmap);
    }

    @Override
    protected final void intercept_list(boolean unhandled,
					boolean unexpected,
					int count, String typenames[]) {
	dbxProfileBridge().
	    noteInterceptList(unhandled, unexpected, typenames);
    }

    @Override
    protected final void intercept_except_list(int count, String typenames[]) {
	dbxProfileBridge().noteInterceptExceptList(typenames);
    }

    @Override
    protected final void loadobj_loading(String loadobj) {
	if (! debugger.state().isLoading) {
	    // Don't do a progress bar for loads in the middle of a
	    // run (due to dlopens)
	    return;
	}
	startProgressManager().updateProgress('>', 1,
	    Catalog.get("LoadingLib") + debugger.shortname(loadobj), 0, 0);
    }

    private final Vector<String> loadobjs = new Vector<String>();

    @Override
    protected final void loadobj_loaded(String loadobj, boolean success) {
	if (success) {
	    if (!loadobj.equals("/lib/libc.so.1")) // filter out libc.so // NOI18N
		loadobjs.add(loadobj);

	    manager().formatStatusText("Loaded",	// NOI18N
				     new String[] { loadobj });
	}
	if (! debugger.state().isLoading) {
	    // Don't do a progress bar for loads in the middle of a
	    // run (due to dlopens)
	    return;
	}
	startProgressManager().updateProgress('<', 1, null, 0, 0);
    }

    @Override
    protected final void proc_new_from_prog(int pid, long ttydev) {
	debugger.state().isProcess = true;
	debugger.state().isCore = false;
	debugger.state().isAttach = false;
	debugger.stateChanged();

	debugger.session().setSessionState(debugger.state());
	debugger.session().setPid(pid);
	debugger.session().setSessionEngine(
		DbxEngineCapabilityProvider.getDbxEngineType());
	debugger.session().update();
	debugger.pidChanged();

	NativeSession session = debugger.session();
	String path = session.getTarget();
	debugger.rtcModel().runBegin(path, pid, debugger.state().is64bit);

	manager().formatStatusText("ProgStarted",	// NOI18N
				    new String[] { Integer.toString(pid) });
    }

    @Override
    protected final void proc_new_from_pid(int pid, long ttydev) {
	debugger.state().isProcess = true;
	debugger.state().isCore = false;
	debugger.state().isAttach = true;
	debugger.stateChanged();

	debugger.session().setSessionState(debugger.state());
	debugger.session().setPid(pid);
	debugger.session().setSessionEngine(
		DbxEngineCapabilityProvider.getDbxEngineType());
	debugger.session().update();
	debugger.pidChanged();

	NativeSession session = debugger.session();
	String path = session.getTarget();
	debugger.rtcModel().runBegin(path, pid, debugger.state().is64bit);

	manager().formatStatusText("AttachedToProc",	// NOI18N
				    new String[] { Integer.toString(pid) });

	debugger.noteProcNewFromPid(pid);
    }

    @Override
    protected final void proc_new_from_core(String corefilename) {
	debugger.state().isProcess = true;
	debugger.state().isCore = true;
	debugger.state().isAttach = false;
	debugger.stateChanged();

	debugger.session().setSessionState(debugger.state());
	debugger.session().setSessionEngine(
		DbxEngineCapabilityProvider.getDbxEngineType());
	debugger.session().setCorefile(corefilename);
	debugger.session().update();

	manager().formatStatusText("LoadedCore",	// NOI18N
				    new String[] { corefilename });
    }

    @Override
    protected final void proc_visit(GPDbxLocation vl, int vframe) {
	Location vloc = GlueLocation.make(debugger, vl);
	vloc.setVisited( !vloc.equals(homeLoc));
	if (vloc.update()) {
	    debugger.setVisitedLocation(vloc);
	}
	visitLoc = vloc;

	debugger.setCurrentFrame(vframe);

	debugger.state().isLoaded = true; // 6588235
	debugger.state().isDebuggerCall = vloc.called();
	debugger.state().isDownAllowed = !vloc.topframe();
	debugger.state().isUpAllowed = !vloc.bottomframe();
	debugger.stateSetRunning(false); // 6781922
	debugger.stateChanged();
    }


    @Override
    protected final void proc_go() {
	debugger.stateSetRunning(true); 
	debugger.stateChanged();

	debugger.resumed();


        manager().setStatusText(Catalog.get("Running"));	// NOI18N
    }

    @Override
    protected final void proc_stopped(GPDbxLocation hl, int nevents,
					GPDbxEventRecord events[]) {

	Location hloc = GlueLocation.make(debugger, hl);

	debugger.stateSetRunning(false);

	if (!hloc.update()) {
	    // On the fly breakpoints - temporary/quiet stop; don't update
	    // the GUI (it should be pretty much transparent to the user that
	    // we did this, except for the I/O in the Dbx Window tracing the
	    // activity)
	    debugger.stateChanged();
	    return;
	}

	GPDbxEventRecord[] fixedEvents = events;
	debugger.updateFiredEvents(fixedEvents);
	debugger.explainStop(fixedEvents);

	homeLoc = hloc;

	// CR 6701251
	// only applied when corefile loaded or attach pid , because
	// dbx does not send proc_visit when loading corefile, this is
	// for showing src when loading corefile
	// if (!debugger.stateFromProg()) {
        //
        /* IZ 182977
         * dbx77 now sending proc_visit again when loading corefile
         * this is redundent and cause problems described in IZ 182977
	if (debugger.state().isCore) {
	    debugger.setVisitedLocation(hloc);
	    debugger.state().isDebuggerCall = hloc.called();
	    debugger.state().isUpAllowed = hloc.bottomframe();
	    debugger.state().isDownAllowed = hloc.topframe();
	    debugger.stateSetRunning(false); 
	    debugger.stateChanged();
	}
         * 
         */
    }

    @Override
    protected final void proc_modified(GPDbxLocation hl) {
	Location hloc = GlueLocation.make(debugger, hl);
	if (hloc.update()) {
	    debugger.setVisitedLocation(hloc);
	}
	homeLoc = hloc;

	debugger.setStatusText(Catalog.get("ProcessModified"));

	debugger.state().isDebuggerCall = hloc.called();
	debugger.state().isDownAllowed = !hloc.topframe();
	debugger.state().isUpAllowed = !hloc.bottomframe();
	debugger.stateSetRunning(false); // CR 6781922
	debugger.stateChanged();
    }

    @Override
    protected final void proc_gone(String reason, int info) {
	homeLoc = null;

	debugger.state().isProcess = false;
	debugger.state().isCore = false;
	// this fix for 6588235 cause problem described in 6649412
	// fixes for 6588235 should be somewhere else
	// debugger.state.isLoaded = false; // 6588235
	debugger.stateSetRunning(false);
	debugger.stateChanged();

	debugger.session().setSessionState(debugger.state());
	debugger.session().setPid(-1);
	debugger.session().setCorefile(null);
	debugger.session().setSessionEngine(null);
	debugger.session().update();
	debugger.pidChanged();

	debugger.noteProcGone(reason, info);
    }

    @Override
    protected final void proc_thread(int tid, GPDbxLocation hl, GPDbxLocation vl,
								int htid) {

	if (hl != null) {
	    Location hloc = GlueLocation.make(debugger, hl);
	    homeLoc = hloc;
	}

	if (vl != null) {
	    Location vloc = GlueLocation.make(debugger, vl);
	    vloc.setVisited( !vloc.equals(homeLoc));
	    if (vloc.update()) {
		if (vloc.hasSource()) {
		    debugger.setVisitedLocation(vloc);
		} else {
		}
	    }

	    debugger.state().isDebuggerCall = vloc.called();
	    debugger.state().isDownAllowed = !vloc.topframe();
	    debugger.state().isUpAllowed = !vloc.bottomframe();
	    debugger.stateChanged();

	    visitLoc = vloc;
	}

	long ltid = htid ;
	ltid <<= 32;
	ltid |= (0x00000000ffffffffL & tid) ;
	debugger.setCurrentThread(ltid);
    }

    @Override
    protected final void proc_about_to_fork(int tid, int htid, String str) {
        // non-null str means a process is about to spawn
	debugger.stateSetRunning(false);
	debugger.stateChanged();

	debugger.aboutToFork();
    }

    @Override
    protected final void expr_eval_result(int rt, String value) {
	debugger.evalResult(rt, value);
    }

    @Override
    protected final void expr_qualify_result(int rt, String expr,
					     String qualified_expr,
					     String reevaluable_expr,
					     int error) {
	debugger.qualifiedExpr(rt, qualified_expr, error);
    }

    @Override
    protected final void expr_heval_result(int rt, GPDbxHEvalResult result) {
	debugger.setChasedPointer(rt, result);
    }

    @Override
    protected final void expr_type_result(int rt, String expr, String stype,
	    						String dtype) {
    }

    @Override
    protected final void expr_set_result(int rt, String value) {
    }

    @Override
    protected final void type_info_result(int rt, String def) {
    }

    @Override
    protected final void expr_line_eval_result(int rt1, int rt2, int flags,
	    					String lhs, String rhs) {
	expr_line_evalall_result(rt1, rt2, flags, lhs, rhs, null, null);
    }

    @Override
    protected final void expr_line_evalall_result(int rt1, int rt2, int flags,
	    		String lhs, String rhs, String type, String rhs_deref) {
	debugger.balloonResult(rt1, rt2, flags, lhs, rhs, type, rhs_deref);
    }

    @Override
    protected final void stack(int nf, int vf, GPDbxFrame frame[], int flags) {
	GPDbxFrame fixedFrames[] = frame;
	for (GPDbxFrame f : fixedFrames) {
	    f.source = debugger.remoteToLocal("stack", f.source); // NOI18N
	    // Perhaps SHOULD do loadobj and loadobj_base as well?
	}
	debugger.setStack(fixedFrames);
	debugger.setCurrentFrame(vf);
    }

    @Override
    protected final void threads(int tot, int shown, GPDbxThread thread[],
	    							int flags) {
	debugger.setThreads(thread);
    }

    @Override
    protected final void error(int rt, int nerr, GPDbxError errors[]) {
	startProgressManager().finishProgress();

	// Get rid of any pending property edits
	EditUndo.undo();
        
        //suppress register tooltip errors
        if (rt != DbxDebuggerImpl.RT_EVAL_REGISTER) {
            manager().error(rt, new DbxError(errors), debugger);
        }
    }

    @Override
    protected final void perf_file(String dir, String file, String group) {
//	debugger.collectorBridge().setPerfFiles(dir, file, group);
    }

    @Override
    protected final void perf_options(GPDbxPerfOptions options) {
//	debugger.collectorBridge().setPerfOptions(options);
    }

    @Override
    protected final void perf_events_status(GPDbxPerfEventsStatus status) {
	/* OLD
	if (debugger.collector != null)
	    debugger.collector.set_events_state(status);
	*/
    }

    @Override
    protected final void perf_open() {
	/* OLD
	if (debugger.collector != null)
	    debugger.collector.running(true);
	*/
    }

    @Override
    protected final void perf_close() {
	/* OLD
	if (debugger.collector != null)
	    debugger.collector.running(false);
	*/
    }




    @Override
    protected final void rtc_state(GPDbxRtcState state) {
	if (dbxInitializing())
	    return;
	debugger.state().accessOn = state.ck_access;
	debugger.RTCStateChanged();
    }

    @Override
    protected final void rtc_patching(byte beginEnd, String label, String message,
				      int count, int total) {
	rtcProgressManager().rtc_patching(beginEnd,
					  label,
					  message,
					  count,
					  total);
    }

    @Override
    protected final void mprof_state(GPDbxMprofState state) {
	if (dbxInitializing())
	    return;
	debugger.state().memuseOn = state.gather_stacks;
	debugger.state().leaksOn = state.gather_stacks;
	debugger.RTCStateChanged();

	/* LATER

	At startup we don't have our optionsLayers established yet

	debugger.debuggingOptionChanged(
	    Option.RTC_CUSTOM_STACK_FRAMES_VALUE,
	    "" + state.nframes_max);
	debugger.debuggingOptionChanged(
	    Option.RTC_CUSTOM_STACK_MATCH_VALUE,
	    "" + state.nframes_match);
	*/
    }



    @Override
    protected final void rtc_access_item(GPDbxRtcItem item) {
	RtcModel.AccessError err = GpRtcUtil.accessError(debugger.rtcModel(),
							 item);
	debugger.rtcModel().accessItem(err);
    }

    @Override
    protected final void mprof_leak_report_begin(GPDbxMprofHeader header) {
	RtcModel.MemoryReportHeader rheader = GpRtcUtil.reportHeader(header);
	debugger.rtcModel().leaksBegin(rheader);
    }

    @Override
    protected final void mprof_leak_report_end() {
	debugger.rtcModel().leaksEnd();
    }

    @Override
    protected final void mprof_leak_report_stopped() {
	debugger.rtcModel().leaksInterrupted();
    }

    @Override
    protected final boolean mprof_leak_item(GPDbxMprofItem item) {
	RtcModel.MemoryReportItem ritem = GpRtcUtil.leakItem(debugger.rtcModel(),
							     item);
	debugger.rtcModel().leakItem(ritem);
	return true;	// wasn't interrupted, keep going
    }



    @Override
    protected final void mprof_use_report_begin(GPDbxMprofHeader header) {
	RtcModel.MemoryReportHeader rheader = GpRtcUtil.reportHeader(header);
	debugger.rtcModel().memuseBegin(rheader);
    }

    @Override
    protected final void mprof_use_report_end() {
	debugger.rtcModel().memuseEnd();
    }

    @Override
    protected final void mprof_use_report_stopped() {
	debugger.rtcModel().memuseInterrupted();
    }

    @Override
    protected final boolean mprof_use_item(GPDbxMprofItem item) {
	RtcModel.MemoryReportItem ritem = GpRtcUtil.useItem(debugger.rtcModel(),
							    item);
	debugger.rtcModel().memuseItem(ritem);
	return true;	// wasn't interrupted, keep going
    }



    /**
     * On-demand created (in fix_start()) and cached FixExecutor.
     */

    private FixExecutor fixExecutor;

    @Override
    protected final void fix_start(String wd, String cmd, String file) {
	if (fixExecutor == null)
	    fixExecutor = new FixExecutor();
	file = debugger.remoteToLocal("fix_start", file); // NOI18N
	fixExecutor.setFile(file);
	manager().formatStatusText("FixBuildingFile",		// NOI18N
				   new String[] { file });
    }

    @Override
    protected final void fix_status(boolean succeeded, String errfile) {
	    // TODO fixExecutor.compile(errfile);
	if (succeeded) {
	    manager().setStatusText(Catalog.get("FixSuccess"));	// NOI18N
	} else {
	    manager().setStatusText(Catalog.get("FixFailed"));	// NOI18N
	}
    }

    @Override
    protected final void fix_done(int attempted, int succeeded) {
	String msg;
	msg = manager().formatStatusText("FixDone",		// NOI18N
				         new String[] { "" + succeeded,
						        "" + attempted });
	if (fixExecutor != null)
	    fixExecutor.done(msg);
	manager().setFixStatus(false);
    }

    @Override
    protected final void fix_pending_build(String target,
					   int n, String file[]) {
	// 6569426
	// Arrange so on the next debug the project is rebuilt or the user
	// get a reminder.
	debugger.rebuildOnNextDebug(target, file);
    }

    @Override
    protected final boolean save_file(String filename) {
	filename = debugger.localToRemote("save_file", filename); // NOI18N
	return EditorBridge.saveFile(filename, debugger);
    }
}
