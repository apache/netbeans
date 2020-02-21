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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.util.Set;
import org.openide.text.Line;

import org.netbeans.modules.cnd.debugger.common2.utils.FileMapper;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionClient;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionLayers;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;

import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Disassembly;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.FormatOption;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.RegistersWindow;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.MemoryWindow;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointProvider;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.Context;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.spi.viewmodel.ModelListener;

public interface NativeDebugger extends BreakpointProvider {
    public interface QualifiedExprListener {
	public void qualifiedExpr(String qualifiedForm, boolean ok);
    };

    public String debuggerType();

    public NativeDebuggerManager manager();
    public boolean isCurrent();
    public DebuggerSettingsBridge profileBridge();
    public OptionClient getOptionClient();
    public Host getHost();

    public BreakpointManager bm();
    public Context context();

    public boolean watchError(int rt, Error error);
    public void runFailed();

    public void execute(String cmd);

    public void reuse(NativeDebuggerInfo ddi);
    public NativeDebuggerInfo getNDI();
    public NativeSession session();

    public void addStateListener(StateListener sl);
    public void removeStateListener(StateListener sl);
    public State state();

    public FileMapper fmap();
    public void setVisitedLocation(Location loc, boolean changeFocus);
    public void setVisitedLocation(Location visitingLocation);
    public Location getVisitedLocation();

    public void postRestoring(boolean restoring);
    public void	setSrcOODMessage(String msg);

    public void postKill();
    public void shutDown();
    
    boolean isConnected();

    public void postVarContinuation(VarContinuation vc);

    public void balloonEvaluate(Line.Part lp, String expr, boolean forceExtractExpression);
    public void evaluateInOutline(String expr);
    public void postExprQualify(String expr, QualifiedExprListener listener);
    
    public void registerLocalModel(LocalModel model);
    public int getLocalsCount();
    public Variable[] getLocals();

    public Set<String> requestAutos();
    public void setShowAutos(boolean showAutos);
    public int getAutosCount();
    public Variable[] getAutos();

    public void registerWatchModel(WatchModel model);
    public WatchVariable[] getWatches();
    public void postDeleteAllWatches();
    public void postDeleteWatch(WatchVariable variable, boolean spreading);
    public void replaceWatch(NativeWatch nw, String str);

    public String[] formatChoices();
    public boolean isDynamicType();
    public void setDynamicType(boolean v);
    public boolean isInheritedMembers();
    public void setInheritedMembers(boolean v);
    public boolean isStaticMembers();
    public void setStaticMembers(boolean v);
    public boolean isPrettyPrint();
    public void setPrettyPrint(boolean v);

    public void postPrettyPrint(boolean v);

    public void registerStackModel(StackModel model);
    public void postVerboseStack(boolean v);
    public void moreFrame();
    public void makeFrameCurrent(Frame f);
    public void copyStack();
    public boolean getVerboseStack();
    public Frame[] getStack();
    public Frame getCurrentFrame();
    
    public boolean isMultiThreading();
    public void registerThreadModel(ThreadModel model);
    public void makeThreadCurrent(Thread f );
    public Thread[] getThreads();
    
    public Thread[] getThreadsWithStacks();
    public void registerDebuggingViewModel(ModelListener model);

    public void rerun();


    public void terminate();
    public void detach();

    public void exprEval(FormatOption format, String expr);


    public void stepInto();
    public void stepOver();
    public void stepOut();
    public void stepTo(String function);
    public void go();
    public void pause();
    public void interrupt();
    public void resumeThread(Thread thread);
    public void runToCursor(String src, int line);
    public void contAt(String src, int line);
    public void makeCalleeCurrent();
    public void makeCallerCurrent();
    public void popTopmostCall();
    public void popLastDebuggerCall();
    public void popToCurrentFrame();
    public void popToHere(Frame frame);

    public String getDebuggingOption(String name); 
    public void setOption(String name, String value);
    public OptionLayers optionLayers();
    public OptionLayers optionLayersInit();
    public void optionLayersReset();

    public void invalidateSessionData();
    public void restoreWatches(WatchBag wb);
    public void spreadWatchCreation(NativeWatch w);

    //
    // support for disassemby stuff
    //
    public void requestDisassembly();
    Disassembly getDisassembly();

    public void InstBptEnabled(long addr, NativeBreakpoint bpt);
    public void InstBptDisabled(long addr, NativeBreakpoint bpt);
    public void InstBptAdded(long addr, NativeBreakpoint bpt);
    public void InstBptRemoved(long addr, NativeBreakpoint bpt);
    public void stepOutInst();
    public void stepOverInst();
    public void stepInst();
    public void runToCursorInst(String addr);
    public void contAtInst(String addr);

    public void registerDisassembly(Disassembly dis);
    public void setCurrentDisLine(Line l);
    public Line getCurrentDisLine();
    public void registerMemoryWindow(MemoryWindow w);
    public void requestMems(String start, String length, FormatOption format);
    FormatOption[] getMemoryFormats();
    public void registerEvaluationWindow(EvaluationWindow w);
    FormatOption[] getEvalFormats();
//    public void registerArrayBrowserWindow(ArrayBrowserWindow w);
    
    //
    // support for registers
    //
    public void registerRegistersWindow(RegistersWindow w);
    public void assignRegisterValue(String register, String value);

    public void activate(boolean redundant);
    public void deactivate(boolean redundant);

    public void notifyUnsavedFiles(String file[]);

    //
    // support for follow-fork
    //
    public void forkThisWay(NativeDebuggerManager.FollowForkInfo ffi);

    //
    // support for fix-and-continue
    //
    public void fix();

    /**
     * Convert a local pathname to a remote pathname.
     * Should be used to convert outgoing pathnames as close as possible
     * "to the wire".
     * @param who	info for debug/tracing purposes
     * @param path	/home/ivan/proj/src/t.c
     * @return		/home/ivan/.netbeans/remote/<platform>/home/ivan/proj/src/t.c
     */
    public String localToRemote(String who, String path);

    /**
     * Convert a remote pathname to a local pathname.
     * Should be used to convert incoming pathnames as close as possible
     * "to the wire".
     * @param who	info for debug/tracing purposes
     * @param path	/home/ivan/.netbeans/remote/<platform>/home/ivan/proj/src/t.c
     * @return		/home/ivan/proj/src/t.c
     */
    public String remoteToLocal(String who, String path);
    
    default public String getLogger(){
        return null;
    }
}
