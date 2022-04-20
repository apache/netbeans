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
package org.netbeans.lib.profiler.results.jdbc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.classfile.ClassInfo;
import org.netbeans.lib.profiler.classfile.ClassRepository;
import org.netbeans.lib.profiler.classfile.DynamicClassInfo;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.filters.InstrumentationFilter;
import org.netbeans.lib.profiler.filters.TextFilter;
import org.netbeans.lib.profiler.results.BaseCallGraphBuilder;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor;
import org.netbeans.lib.profiler.results.cpu.CPUProfilingResultListener;
import static org.netbeans.lib.profiler.results.cpu.CPUProfilingResultListener.METHODTYPE_MARKER;
import org.netbeans.lib.profiler.results.cpu.FlatProfileContainer;
import org.netbeans.lib.profiler.results.cpu.FlatProfileContainerFree;
import org.netbeans.lib.profiler.results.cpu.ThreadInfo;
import org.netbeans.lib.profiler.results.cpu.ThreadInfos;
import org.netbeans.lib.profiler.results.cpu.cct.CCTFlattener;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.SimpleCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.ThreadCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode;
import org.netbeans.lib.profiler.results.memory.JMethodIdTable;
import org.netbeans.lib.profiler.results.memory.JMethodIdTable.JMethodIdTableEntry;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode;
import org.netbeans.lib.profiler.results.memory.RuntimeObjAllocTermCCTNode;

/**
 *
 * @author Tomas Hurka
 */
public class JdbcGraphBuilder extends BaseCallGraphBuilder implements CPUProfilingResultListener, JdbcCCTProvider {

    static final Logger JDBC_LOGGER = Logger.getLogger(JdbcGraphBuilder.class.getName());

    private Map statements;
    private Map connections;
    private Map<Select,Integer> selectsToId;
    private Map<Integer, Select> idsToSelect;
    private Map<ThreadInfo, SQLStatement> currentObject;
    private Map<ThreadInfo, Integer> currentSqlLevel;
    private int lastSelectId;
    private RuntimeMemoryCCTNode[] stacksForSelects; // [1- maxSelectId] selectId -> root of its allocation traces tree
    private final ThreadInfos threadInfos = new ThreadInfos();
    private final SQLParser sqlParser = new SQLParser();
    private TextFilter filter;

    @Override
    protected RuntimeCCTNode getAppRootNode() {
        if (threadInfos.isEmpty()) {
            return null;
        }

        RuntimeCPUCCTNode appNode = null;

        beginTrans(false);

        try {
            ProfilerClient client = getClient();
            if (client != null) {
                appNode = new SimpleCPUCCTNode(lastSelectId + 1);
            } else {
                appNode = new SimpleCPUCCTNode(true);
            }

            int len = (threadInfos.getThreadNames() != null) ? threadInfos.getThreadNames().length : 0;

            for (int i = 0; i < len; i++) {
                ThreadInfo ti = threadInfos.threadInfos[i];

                if ((ti == null) || (ti.stack[0] == null)) {
                    continue;
                }

                appNode.attachNodeAsChild(ti.stack[0]);
            }
        } finally {
            endTrans();
        }

        return appNode;
    }

    public RuntimeMemoryCCTNode[] getStacksForSelects() {
        beginTrans(false);

        try {
            return stacksForSelects;
        } finally {
            endTrans();
        }
    }

    @Override
    public FlatProfileContainer createFlatProfile() {
        beginTrans(false);

        try {
            RuntimeCCTNode appNode = getAppRootNode();

            if (appNode != null) {
                CCTFlattener flattener = new JdbcCCTFlattener(getClient());

                RuntimeCCTNodeProcessor.process(appNode, flattener);
                return flattener.getFlatProfile();
            }
            return null;
        } finally {
            endTrans();
        }
    }

    @Override
    protected void doBatchStart() {
        beginTrans(true);
    }

    @Override
    protected void doBatchStop() {
        endTrans();
    }

    @Override
    protected void doReset() {
        beginTrans(true);
        try {
            threadInfos.reset();
            selectsToId.clear();
            idsToSelect.clear();
            currentObject.clear();
            currentSqlLevel.clear();
            lastSelectId = 0;
            if (stacksForSelects != null) {
                Arrays.fill(stacksForSelects, null);
            }
        } finally {
            endTrans();
        }
    }

    @Override
    protected void doShutdown() {
        statements = null;
        connections = null;
        selectsToId = null;
        idsToSelect = null;
        currentObject = null;
        currentSqlLevel = null;
        stacksForSelects = null;
    }

    @Override
    protected void doStartup(ProfilerClient profilerClient) {
        statements = new HashMap();
        connections = new HashMap();
        selectsToId = new HashMap();
        idsToSelect = new HashMap();
        currentObject = new HashMap();
        currentSqlLevel = new HashMap();
        threadInfos.reset();
        stacksForSelects = null;
        lastSelectId = 0;
        profilerClient.registerJdbcCCTProvider(this);
        InstrumentationFilter f = profilerClient.getSettings().getInstrumentationFilter();
        filter = new TextFilter(f.getValue(), f.getType(), false);
    }

    @Override
    public void profilingPoint(int threadId, int ppId, long timeStamp) {
    }

    @Override
    public void methodEntry(int methodId, int threadId, int methodType, long timeStamp0, long timeStamp1, List parameters, int[] methoIds) {
        if (methodType == METHODTYPE_MARKER) {
            if (threadInfos.threadInfos == null) {
                return;
            }
            ThreadInfo ti = threadInfos.threadInfos[threadId];
            if (ti == null) {
                return;
            }
            int sqlCallLevel = incrementSqlLevel(ti); 
            if (JDBC_LOGGER.isLoggable(Level.FINEST)) {
                String className = status.getInstrMethodClasses()[methodId];
                String methodName = status.getInstrMethodNames()[methodId];
                JDBC_LOGGER.log(Level.FINEST, "markerMethodEntry, mId = {0}, {1}.{2}{3}", new Object[]{methodId, className, methodName, parameters.toString()});
            }
            if (sqlCallLevel > 1) {
                if (ti.peek() instanceof MethodCPUCCTNode) {
                    markerMethodEntry(-1, ti, timeStamp0, timeStamp1, true);
                    batchNotEmpty = true;
                }
                return;
            }
            if (JDBC_LOGGER.isLoggable(Level.FINEST) && methoIds != null) {
                debugStackTrace(methoIds);
            }
            if (hasThis(methodId)) {
                String thisString = (String) parameters.get(0);
                int index = thisString.indexOf('@');
                String thisClass = thisString.substring(0, index);
                String thisHash = thisString.substring(index + 1);

                if (implementsInterface(thisClass, STATEMENT_INTERFACE)) {
                    SQLStatement statement = (SQLStatement) statements.get(thisHash);
                    String select;

                    if (statement == null) {
                        statement = new SQLStatement(SQL_STATEMENT_UNKNOWN);
                        statements.put(thisHash, statement);
                    }
                    select = statement.invoke(status.getInstrMethodNames()[methodId], status.getInstrMethodSignatures()[methodId], parameters);
                    if (select != null && filter.passes(select)) {
                        int selectId = getSelectId(statement.getType(), select);
                        markerMethodEntry(selectId, ti, timeStamp0, timeStamp1, true);
                        RuntimeObjAllocTermCCTNode term = (RuntimeObjAllocTermCCTNode) processStackTrace(selectId, methoIds);
                        if (term != null) {
                            term.updateForNewObject(0);
                        }
                        batchNotEmpty = true;
                    }
                }
                if (implementsInterface(thisClass, CONNECTION_INTERFACE)) {
                    SQLConnection connection = (SQLConnection) connections.get(thisHash);
                    if (connection == null) {
                        connection = new SQLConnection();
                        connections.put(thisHash, connection);
                    }
                    connection.invoke(status.getInstrMethodNames()[methodId], status.getInstrMethodSignatures()[methodId], parameters);
                    assert currentObject.get(ti) == null;
                    currentObject.put(ti, connection.useCurrentStatement());
                }
            }
        }
    }

    @Override
    public void methodEntryUnstamped(int methodId, int threadId, int methodType, List parameters, int[] methoIds) {
    }

    @Override
    public void methodExit(int methodId, int threadId, int methodType, long timeStamp0, long timeStamp1, Object retVal) {
        if (methodType == METHODTYPE_MARKER) {
            if (status == null || (threadInfos.threadInfos == null)) {
                return;
            }

            ThreadInfo ti = threadInfos.threadInfos[threadId];

            if (ti == null) {
                return;
            }
            int sqlCallLevel = decrementSqlLevel(ti);

            plainMethodExit(methodId, ti, timeStamp0, timeStamp1, true);
            if (sqlCallLevel == 0) {
                SQLStatement st = currentObject.get(ti);
                
                if (st != null && retVal instanceof String) {
                    String thisString = (String) retVal;
                    int index = thisString.indexOf('@');
                    String thisClass = thisString.substring(0, index);
                    String thisHash = thisString.substring(index + 1);
                    if (implementsInterface(thisClass, STATEMENT_INTERFACE)) {
                        assert st != null;
                        statements.put(thisHash, st);
                    }
                }
                currentObject.remove(ti);
            }
            batchNotEmpty = true;
        }
    }

    @Override
    public void methodExitUnstamped(int methodId, int threadId, int methodType) {
    }

    @Override
    public void servletRequest(int threadId, int requestType, String servletPath, int sessionId) {
    }

    @Override
    public void sleepEntry(int threadId, long timeStamp0, long timeStamp1) {
    }

    @Override
    public void sleepExit(int threadId, long timeStamp0, long timeStamp1) {
    }

    @Override
    public void threadsResume(long timeStamp0, long timeStamp1) {
    }

    @Override
    public void threadsSuspend(long timeStamp0, long timeStamp1) {
    }

    @Override
    public void waitEntry(int threadId, long timeStamp0, long timeStamp1) {
    }

    @Override
    public void waitExit(int threadId, long timeStamp0, long timeStamp1) {
    }

    @Override
    public void parkEntry(int threadId, long timeStamp0, long timeStamp1) {
    }

    @Override
    public void parkExit(int threadId, long timeStamp0, long timeStamp1) {
    }

    @Override
    public void monitorEntry(int threadId, long timeStamp0, long timeStamp1, int monitorId, int ownerThreadId) {
    }

    @Override
    public void monitorExit(int threadId, long timeStamp0, long timeStamp1, int monitorId) {
    }

    @Override
    public void newThread(int threadId, String threadName, String threadClassName) {
        if (status == null) {
            return;
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "New thread creation for thread id = {0}, name = {1}", new Object[]{threadId, threadName});
        }

        threadInfos.newThreadInfo(threadId, threadName, threadClassName);
    }

    @Override
    public void newMonitor(int hash, String className) {
    }

    @Override
    /**
     * Called when the TA is suspended waiting for the tool to process the
     * buffer
     */
    public void timeAdjust(final int threadId, final long timeDiff0, final long timeDiff1) {
        if (status == null || (threadInfos.threadInfos == null)) {
            return;
        }

        ThreadInfo ti = threadInfos.threadInfos[threadId];

        // In this case, time stamps are actually time adjustments.
        // timeStamp0 is always abosolute and timeStamp1 is always thread CPU.
        ti.rootMethodEntryTimeAbs += timeDiff0;
        ti.rootMethodEntryTimeThreadCPU += timeDiff1;
        ti.topMethodEntryTime0 += timeDiff0;

        if (isCollectingTwoTimeStamps()) {
            ti.topMethodEntryTime1 += timeDiff1;
        }
    }

    private boolean hasThis(int methodId) {
        String methodName = status.getInstrMethodNames()[methodId];
        if (methodName.equals("<init>")) {
            return false;
        }
        String className = status.getInstrMethodClasses()[methodId];
        String signature = status.getInstrMethodSignatures()[methodId];
        int classLoaderId = status.getClassLoaderIds()[methodId];

        try {
            ClassInfo clazz = ClassRepository.lookupClass(className, classLoaderId);
            int methodIdx = clazz.getMethodIndex(methodName, signature);
            return !clazz.isMethodStatic(methodIdx);
        } catch (IOException ex) {
            Logger.getLogger(JdbcGraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassFormatError ex) {
            Logger.getLogger(JdbcGraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean implementsInterface(String thisClass, String STATEMENT_INTERFACE) {
        try {
            String slashedClass = thisClass.replace('.', '/');  // NOI18N
            List<DynamicClassInfo> clazzes = ClassRepository.getAllClassVersions(STATEMENT_INTERFACE);
            if (null != clazzes) {
                for (DynamicClassInfo dynamicClassInfo : clazzes) {
                    List subclasses = dynamicClassInfo.getSubclasses();

                    if (subclasses != null) {
                        for (int i = 0; i < subclasses.size(); i++) {
                            DynamicClassInfo subclass = (DynamicClassInfo) subclasses.get(i);

                            if (subclass.getName().equals(slashedClass)) {
                                return true;
                            }
                        }
                    }                
                }
            }
            return false;
        } catch (ClassFormatError ex) {
            Logger.getLogger(JdbcGraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private int getSelectId(int type, String select) {
        Select sel = new Select(type, select);
        
        Integer selectId = selectsToId.get(sel);
        if (selectId == null) {
            selectId = Integer.valueOf(++lastSelectId);
            sel.setCommandType(extractSQLCommandType(select));
            sel.setTables(extractTables(select));
            selectsToId.put(sel, selectId);
            idsToSelect.put(selectId, sel);
            updateNumberOfSelects();
        }
        return selectId.intValue();
    }

    private boolean isCollectingTwoTimeStamps() {
        return status.collectingTwoTimeStamps();
    }

    private TimedCPUCCTNode markerMethodEntry(final int selectId, final ThreadInfo ti, long timeStamp0, long timeStamp1,
            boolean stamped) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "MarkerMEntry{0} for tId = {1}, time: {2}, method:  {3}, inRoot: {4}, rootEntryTimeThread: {5}", new Object[]{(!stamped) ? "(unstamped)" : "", (int) ti.threadId, timeStamp0, debugSelect(selectId), ti.rootMethodEntryTimeAbs, ti.rootMethodEntryTimeThreadCPU});
        }

        TimedCPUCCTNode curNode = ti.peek();

        if (curNode == null) {
            TimedCPUCCTNode rootNode = new ThreadCPUCCTNode(ti.threadId);
            ti.totalNNodes++;
            ti.push(rootNode);
            ti.totalNInv--;

            curNode = new MethodCPUCCTNode(selectId);
            rootNode.attachNodeAsChild(curNode);
            ti.totalNNodes++;
            ti.push(curNode);

            ti.topMethodEntryTime0 = timeStamp0;

            if (isCollectingTwoTimeStamps()) {
                ti.topMethodEntryTime1 = timeStamp1;
            }
        } else {
            if (stamped) {
                long diff = timeStamp0 - ti.topMethodEntryTime0;

                if (diff > 0) {
                    curNode.addNetTime0(diff);
                } else {
                    timeStamp0 = ti.topMethodEntryTime0;
                }

                ti.topMethodEntryTime0 = timeStamp0;

                if (isCollectingTwoTimeStamps()) {
                    diff = timeStamp1 - ti.topMethodEntryTime1;

                    if (diff > 0) {
                        curNode.addNetTime1(diff);
                    } else {
                        timeStamp1 = ti.topMethodEntryTime1;
                    }

                    ti.topMethodEntryTime1 = timeStamp1;
                }
            }

            TimedCPUCCTNode calleeNode;

            // Now find the appropriate callee in this node or create one
            calleeNode = MethodCPUCCTNode.Locator.locate(selectId, curNode.getChildren());

            if (calleeNode == null) {
                calleeNode = new MethodCPUCCTNode(selectId);
                curNode.attachNodeAsChild(calleeNode);
                ti.totalNNodes++;
            }

            ti.push(calleeNode);
            curNode = calleeNode; // make the callee node be the current one
        }

        if (selectId == -1) {
            curNode.setFilteredStatus(TimedCPUCCTNode.FILTERED_YES);
        }

        if (stamped) { // stamped marker method called before any root method; must set rootMethodEntryTimes
            // Be careful - with MARKER_ENTRY and MARKER_EXIT events, unlike with others, timeStamp0 is *always* absolute time,
            // and timeStamp1 is *always* thread CPU time
            ti.rootMethodEntryTimeAbs = timeStamp0;
            ti.rootMethodEntryTimeThreadCPU = timeStamp1;
            ti.topMethodEntryTime0 = timeStamp0;

            if (isCollectingTwoTimeStamps()) {
                ti.topMethodEntryTime1 = timeStamp1;
            }
        }
        return curNode;
    }

    private TimedCPUCCTNode plainMethodExit(final int methodId, final ThreadInfo ti, long timeStamp0, long timeStamp1,
            boolean stamped) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "MethodExit  {0}: for tId = {1}, time: {2}, method:  {3}", new Object[]{(!stamped) ? "(unstamped)" : "", ti.threadId, timeStamp0, methodId});
        }

        TimedCPUCCTNode curNode = ti.peek();

        if (!(curNode instanceof MethodCPUCCTNode)) {
            return null;
        }

        // Timer's coarse granularities etc. may occasionally cause this issue. FIXME: maybe need a warning, though not
        // every time this happens, but probably rather in the end of run, so that the problem could then be investigated.
        if (stamped) {
            long diff = timeStamp0 - ti.topMethodEntryTime0;

            if (diff > 0) {
                curNode.addNetTime0(diff);
            } else {
                timeStamp0 = ti.topMethodEntryTime0;
            }

            if (isCollectingTwoTimeStamps()) {
                diff = timeStamp1 - ti.topMethodEntryTime1;

                if (diff > 0) {
                    curNode.addNetTime1(diff);
                } else {
                    timeStamp1 = ti.topMethodEntryTime1;
                }
            }
        }

        TimedCPUCCTNode oldNode = ti.pop();

        //    if ((ti.stackTopIdx < 0 || ti.stack[ti.stackTopIdx].getMethodId() == 0) && checkStack) {
        //      System.err.println(ENGINE_WARNING + "critical: stack state on methodExit is like at rootMethodExit"); // NOI18N
        //      System.err.println(PLEASE_REPORT_PROBLEM);
        //    }
        // Resume the net time for the caller
        if (stamped) {
            ti.topMethodEntryTime0 = timeStamp0;

            if (isCollectingTwoTimeStamps()) {
                ti.topMethodEntryTime1 = timeStamp1;
            }
        }

        return oldNode;
    }

    /**
     * Given the selectId and the array of methodIds of the stack trace for the
     * newly invoked select, update the reverse Calling Context Tree for this
     * select. Adds new nodes and/or increases allocated object counters/size in
     * nodes. Returns the terminating node in the resulting CCT branch.
     */
    private RuntimeMemoryCCTNode processStackTrace(int selectId, int[] methodIds) {
        if (selectId >= stacksForSelects.length) {
            LOGGER.severe("Received stack for non existent select Id: " + (int) selectId + ", current length: " + stacksForSelects.length); // NOI18N
            return null;
        }

        RuntimeMemoryCCTNode curNode = stacksForSelects[selectId];
        RuntimeMemoryCCTNode parentNode = null;

        if (curNode == null) {
            curNode = new RuntimeMemoryCCTNode(0);
            stacksForSelects[selectId] = curNode;
        }

        int depth = methodIds.length;
        int depthMinusOne = depth - 1;

        for (int i = 0; i < depth; i++) {
            int methodId = methodIds[i];
            parentNode = curNode;

            Object children = curNode.children;

            boolean found = false;

            if (children != null) {
                if (children instanceof RuntimeMemoryCCTNode) {
                    if (((RuntimeMemoryCCTNode) children).methodId == methodId) {
                        curNode = (RuntimeMemoryCCTNode) children;
                        found = true;
                    }
                } else {
                    RuntimeMemoryCCTNode[] ar = (RuntimeMemoryCCTNode[]) children;

                    for (int j = 0; j < ar.length; j++) {
                        if (ar[j].methodId == methodId) {
                            curNode = ar[j];
                            found = true;

                            break;
                        }
                    }
                }
            }

            if (!found) {
                // Appropriate subnode not found or there are no subnodes yet - create one.
                if (i < depthMinusOne) {
                    curNode = curNode.addNewChild(methodId); // Non-terminal node
                } else { // Terminal node - need to create a specialized one depending on the profiling type (obj alloc or obj liveness)

                    RuntimeMemoryCCTNode newNode = new RuntimeObjAllocTermCCTNode(methodId);
                    curNode.attachNodeAsChild(newNode);
                    curNode = newNode;
                }
            }
        }

        // Now check if the curNode that we are going to return is actually not an instance of one of classes representing
        // "terminal nodes", like RuntimeObjAllocTermCCTNode or RuntimeObjLivenessTermCCTNode. Such nodes contain information
        // that is normally the same for the whole call chain - such as total number/size of objects of the given type allocated
        // by this call chain. However, it looks like in some cases (different threads?) it may happen that one complete call
        // chain may become a fragment of another, longer call chain. In that case we will neeed to have a "terminal" node in the middle
        // of the chain. Here we are checking for the case when first a longer chain is created, and then a shorter one that
        // matches a part of the longer one is found, and taking measures.
        if (curNode.getClass() == RuntimeMemoryCCTNode.class) {
            RuntimeMemoryCCTNode newNode = new RuntimeObjAllocTermCCTNode(curNode.methodId);
            newNode.children = curNode.children;

            if (parentNode != null) {
                Object parChildren = parentNode.children;
                assert (parChildren != null); // parent will always have chilren

                if (parChildren instanceof RuntimeMemoryCCTNode) {
                    if (parChildren == curNode) {
                        parentNode.children = newNode;
                    }
                } else {
                    RuntimeMemoryCCTNode[] ar = (RuntimeMemoryCCTNode[]) parChildren;

                    for (int i = 0; i < ar.length; i++) {
                        if (ar[i] == curNode) {
                            ar[i] = newNode;

                            break;
                        }
                    }
                }
            } else {
                stacksForSelects[selectId] = newNode;
            }

            curNode = newNode;
        }

        return curNode;
    }

    private void updateNumberOfSelects() {
        int nProfiledSelects = lastSelectId + 1;

        if ((stacksForSelects == null) || (stacksForSelects.length <= nProfiledSelects)) {
            int newSize = (nProfiledSelects * 3) / 2;
            RuntimeMemoryCCTNode[] newStacks = new RuntimeMemoryCCTNode[newSize];

            if (stacksForSelects != null) {
                System.arraycopy(stacksForSelects, 0, newStacks, 0, stacksForSelects.length);
            }
            stacksForSelects = newStacks;
        }
    }

    private String debugSelect(int selectId) {
        if (selectId == -1) {
            return "-1";
        }
        Select sel = idsToSelect.get(Integer.valueOf(selectId));
        if (sel == null) {
            return "Null select for SelectId " + selectId;
        }
        return sel.getSelect();
    }

    private void debugStackTrace(int[] methoIds) {
        JMethodIdTable table = JMethodIdTable.getDefault();
        for (int i = 0; i < methoIds.length; i++) {
            table.checkMethodId(methoIds[i]);
        }
        try {
            table.getNamesForMethodIds(getClient());
            for (int i = 0; i < methoIds.length; i++) {
                JMethodIdTableEntry en = table.getEntry(methoIds[i]);
                JDBC_LOGGER.log(Level.FINEST, "{0}.{1}", new Object[]{en.className, en.methodName});
            }
        } catch (ClientUtils.TargetAppOrVMTerminated ex) {
            Logger.getLogger(JdbcGraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    int extractSQLCommandType(String sql) {
        return sqlParser.extractSQLCommandType(sql);
    }
    
    String[] extractTables(String sql) {
        return sqlParser.extractTables(sql);
    }
    
    @Override
    public void beginTrans(boolean mutable) {
        threadInfos.beginTrans(mutable);
    }

    @Override
    public void endTrans() {
        threadInfos.endTrans();
    }

    @Override
    public void updateInternals() {
        final ProfilerClient client = getClient();
        if (client != null) {
            synchronized (client) {
                beginTrans(false);

                try {
                    PresoObjAllocCCTNode.getNamesForMethodIdsFromVM(client, stacksForSelects);
                } catch (ClientUtils.TargetAppOrVMTerminated ex) {
                    ProfilerLogger.log(ex.getMessage());
                    /* No longer ignore silently */
                } finally {
                    endTrans();
                }
            }
        }
    }

    @Override
    public int getCommandType(int selectId) {
        Select sel = idsToSelect.get(Integer.valueOf(selectId));
        if (sel != null) {
            return sel.getType();
        }
        return JdbcCCTProvider.SQL_STATEMENT_UNKNOWN;
    }

    @Override
    public int getSQLCommand(int selectId) {
        Select sel = idsToSelect.get(Integer.valueOf(selectId));
        if (sel != null) {
            return sel.getCommandType();
        }
        return JdbcCCTProvider.SQL_COMMAND_OTHER;
    }

    @Override
    public String[] getTables(int selectId) {
        Select sel = idsToSelect.get(Integer.valueOf(selectId));
        if (sel != null) {
            return sel.getTables();
        }
        return new String[0];
    }

    private int incrementSqlLevel(ThreadInfo ti) {
        Integer sqlLevel = currentSqlLevel.get(ti);
        if (sqlLevel == null) {
            sqlLevel = Integer.valueOf(1);
        } else {
            sqlLevel = Integer.valueOf(sqlLevel.intValue()+1);
        }
        currentSqlLevel.put(ti, sqlLevel);
        return sqlLevel.intValue();
    }

    private int decrementSqlLevel(ThreadInfo ti) {
        Integer sqlLevel = currentSqlLevel.get(ti);

        assert sqlLevel != null;
        sqlLevel = Integer.valueOf(sqlLevel.intValue()-1);
        currentSqlLevel.put(ti, sqlLevel);
        return sqlLevel.intValue();
    }


    private class JdbcCCTFlattener extends CCTFlattener {

        public JdbcCCTFlattener(ProfilerClient client) {
            super(client, null);
        }

        @Override
        protected int getMaxMethodId() {
            return lastSelectId + 1;
        }

        @Override
        protected String getInstrMethodClass(int selectId) {
            Select sel = idsToSelect.get(Integer.valueOf(selectId));
            if (sel == null) {
                return "Unknown select for selectId " + selectId;
            }
            return sel.getSelect();
        }

        @Override
        protected String getInstrMethodName(int nodeMethodId) {
            return "";
        }

        @Override
        protected FlatProfileContainer createContainer(long[] timeInMcs0, long[] timeInMcs1, long[] totalTimeInMcs0, long[] totalTimeInMcs1, int[] nInvocations, double wholeGraphNetTime0, double wholeGraphNetTime1) {
            return new JdbcFlatProfileContainer(timeInMcs0, timeInMcs1, totalTimeInMcs0, totalTimeInMcs1, nInvocations, wholeGraphNetTime0, wholeGraphNetTime1);
        }
    }
    
    private class JdbcFlatProfileContainer extends FlatProfileContainerFree {
        
        private JdbcFlatProfileContainer(long[] timeInMcs0, long[] timeInMcs1, long[] totalTimeInMcs0, long[] totalTimeInMcs1, int[] nInvocations, double wholeGraphNetTime0, double wholeGraphNetTime1) {
            super(getClient().getStatus(), timeInMcs0, timeInMcs1, totalTimeInMcs0, totalTimeInMcs1, nInvocations, new char[0], wholeGraphNetTime0, wholeGraphNetTime1, nInvocations.length);
        }

        @Override
        public String getMethodNameAtRow(int row) {
            int selectId = getMethodIdAtRow(row);
            return idsToSelect.get(Integer.valueOf(selectId)).getSelect();
        }
        
    }
    
    private static class Select {
        private final int type;
        private  int commandType;
        private final String select;
        private String[] tables;
        
        Select(int t, String s) {
            type = t;
            select = s;
        }

        private int getType() {
            return type;
        }

        private void setCommandType(int ct) {
            commandType = ct;
        }

        private void setTables(String[] t) {
            tables = t;
        }

        private int getCommandType() {
            return commandType;
        }

        public String[] getTables() {
            return tables;
        }

        private String getSelect() {
            return select;
        }

        @Override
        public int hashCode() {
            return type ^ select.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Select other = (Select) obj;
            if (this.type != other.type) {
                return false;
            }
            if (!Objects.equals(this.select, other.select)) {
                return false;
            }
            return true;
        }
        
    }
}
