/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.viewmodel;

import java.beans.BeanInfo;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public class AsynchronousTest extends NbTestCase {

    private static final Set<String> SYNCHRONOUS_METHODS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList( "isLeaf",    // TreeModel
                           "getIconBase", "canCopy", "canCut", "canRename", // NodeModel
                           "clipboardCopy", "clipboardCut", "getPasteTypes", "getIconBaseWithExtension", // ExtendedNodeModel
                           "isReadOnly" )));    // TableModel

    public AsynchronousTest(String s) {
        super(s);
    }

    public void testDefaultThreadingAccess() throws Exception {
        Map<CALL, Executor> defaultRPs = new HashMap<CALL, Executor>();
        defaultRPs.put(CALL.CHILDREN, AsynchronousModelFilter.DEFAULT);
        defaultRPs.put(CALL.VALUE, AsynchronousModelFilter.DEFAULT);

        Map<String, ThreadChecker> defaultMethodThreads = new HashMap<String, ThreadChecker>();
        AWTChecker awtc = new AWTChecker();
        for (String methodName : SYNCHRONOUS_METHODS) {
            defaultMethodThreads.put(methodName, awtc);
        }
        defaultMethodThreads.put("getDisplayName", awtc);
        defaultMethodThreads.put("getShortDescription", awtc);
        defaultMethodThreads.put("getShortDescription (called on property values)", awtc);
        RPChecker rpc = new RPChecker((RequestProcessor) AsynchronousModelFilter.DEFAULT);
        defaultMethodThreads.put("getChildren", rpc);
        defaultMethodThreads.put("getChildrenCount", rpc);
        defaultMethodThreads.put("getValueAt", rpc);
        CheckCallingThreadModel cm = new CheckCallingThreadModel(new String[] { "a", "b", "c" }, 2, defaultMethodThreads);
        ArrayList l = new ArrayList ();
        l.add(cm);
        l.addAll(Arrays.asList(cm.createColumns()));
        final Models.CompoundModel mcm = Models.createCompoundModel(l);
        SwingUtilities.invokeAndWait(new GUIQuerier(mcm));
        Map<String, Thread> failedMethods = cm.getFailedMethods();
        assertEquals(failedMethods.toString(), 0, failedMethods.size());
    }

    public void testSynchronousAccess() throws Exception {
        AWTChecker awtc = new AWTChecker();
        CheckCallingThreadModel cm = new CheckCallingThreadModel(new String[] { "a", "b", "c" }, 2, new ConstantCheckersMap(awtc));
        SynchronousModelImpl sm = new SynchronousModelImpl();
        ArrayList l = new ArrayList ();
        l.add(cm);
        l.addAll(Arrays.asList(cm.createColumns()));
        l.add(sm);
        final Models.CompoundModel mcm = Models.createCompoundModel(l);
        SwingUtilities.invokeAndWait(new GUIQuerier(mcm));
        Map<String, Thread> failedMethods = cm.getFailedMethods();
        assertEquals(failedMethods.toString(), 0, failedMethods.size());
    }

    public void testDefaultRPAccess() throws Exception {
        RPChecker rpc = new RPChecker((RequestProcessor) AsynchronousModelFilter.DEFAULT);
        CheckCallingThreadModel cm = new CheckCallingThreadModel(new String[] { "a", "b", "c" }, 2, new ConstantCheckersMap(rpc));
        DefaultRPModelImpl drpm = new DefaultRPModelImpl();
        ArrayList l = new ArrayList ();
        l.add(cm);
        l.addAll(Arrays.asList(cm.createColumns()));
        l.add(drpm);
        final Models.CompoundModel mcm = Models.createCompoundModel(l);
        SwingUtilities.invokeAndWait(new GUIQuerier(mcm));
        Map<String, Thread> failedMethods = cm.getFailedMethods();
        assertEquals(failedMethods.toString(), 0, failedMethods.size());
    }

    private static final class GUIQuerier implements Runnable {

        private final Models.CompoundModel mcm;

        public GUIQuerier(Models.CompoundModel mcm) {
            this.mcm = mcm;
        }

        public void run() {
            OutlineTable tt = (OutlineTable) Models.createView(mcm);
            Node root = tt.getExplorerManager ().getRootContext ();

            root.getChildren().getNodes();
            root.getHtmlDisplayName();
            root.getShortDescription();
            for (Node n : root.getChildren().getNodes()) {
                inspectNode(n);
                for (Node nn : n.getChildren().getNodes()) {
                    inspectNode(nn);
                }
            }
        }

        private void inspectNode(Node n) {
            n.getDisplayName();
            n.getHtmlDisplayName();
            n.getShortDescription();
            n.getIcon(BeanInfo.ICON_COLOR_16x16);
            n.canCopy();
            n.canCut();
            n.canRename();
            n.getNewTypes();
            n.getActions(true);
            n.getPreferredAction();
            inspectProperties(n);
        }

        private void inspectProperties(Node n) {
            PropertySet[] propertySets = n.getPropertySets();
            for (PropertySet ps : propertySets) {
                for (Property<?> p : ps.getProperties()) {
                    try {
                        p.getValue();
                    } catch (IllegalAccessException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    p.canRead();
                    p.canWrite();
                    p.getName();
                    p.getDisplayName();
                    p.getHtmlDisplayName();
                    p.getShortDescription();
                }
            }
        }
    }

    private static interface ThreadChecker {

        boolean isInCorrectThread();

    }

    private static final class AWTChecker implements ThreadChecker {

        public boolean isInCorrectThread() {
            return SwingUtilities.isEventDispatchThread();
        }

    }

    private static final class RPChecker implements ThreadChecker {

        private RequestProcessor rp;

        public RPChecker(RequestProcessor rp) {
            this.rp = rp;
        }

        public boolean isInCorrectThread() {
            return rp.isRequestProcessorThread();
        }

    }

    private final class ConstantCheckersMap implements Map {

        private final ThreadChecker tc;

        public ConstantCheckersMap(ThreadChecker tc) {
            this.tc = tc;
        }

        public int size() { return Integer.MAX_VALUE; }

        public boolean isEmpty() { return false; }

        public boolean containsKey(Object key) { return true; }

        public boolean containsValue(Object value) { return value == tc; }

        public Object get(Object key) {
            if (SYNCHRONOUS_METHODS.contains(key)) {
                return new AWTChecker();
            }
            return tc;
        }

        public Object put(Object key, Object value) { throw new UnsupportedOperationException("N/A"); }

        public Object remove(Object key) { throw new UnsupportedOperationException("N/A"); }

        public void putAll(Map t) { throw new UnsupportedOperationException("N/A"); }

        public void clear() { throw new UnsupportedOperationException("N/A"); }

        public Set keySet() { throw new UnsupportedOperationException("N/A"); }

        public Collection values() { return Collections.singleton(tc); }

        public Set entrySet() { throw new UnsupportedOperationException("N/A"); }

    }

    private static class CheckCallingThreadModel extends CountedModel {

        private final Map<String, ThreadChecker> threadCheckers;
        private final Map<String, Thread> failedMethods = new HashMap<String, Thread>();

        public CheckCallingThreadModel(String[] children, int depth,
                                       Map<String, ThreadChecker> threadCheckers) {
            super(children, depth);
            this.threadCheckers = threadCheckers;
        }

        @Override
        protected void countCall(String methodName, Object... params) {
            if ("getValueAt".equals(methodName) && params[0] instanceof javax.swing.JToolTip) {
                methodName = "getShortDescription (called on property values)";
            }
            ThreadChecker tc = threadCheckers.get(methodName);
            if (!tc.isInCorrectThread()) {
                failedMethods.put(methodName, Thread.currentThread());
                //Thread.dumpStack();
            }
            super.countCall(methodName, params);
        }

        Map<String, Thread> getFailedMethods() {
            return failedMethods;
        }

    }

    private static class SynchronousModelImpl implements AsynchronousModelFilter {

        public Executor asynchronous(Executor original, CALL asynchCall, Object node) throws UnknownTypeException {
            return AsynchronousModelFilter.CURRENT_THREAD;
        }
        
    }

    private static class DefaultRPModelImpl implements AsynchronousModelFilter {

        public Executor asynchronous(Executor original, CALL asynchCall, Object node) throws UnknownTypeException {
            return AsynchronousModelFilter.DEFAULT;
        }

    }

    private static class CustomRPModelImpl implements AsynchronousModelFilter {

        private Map<CALL, Executor> rps;
        
        public CustomRPModelImpl(Map<CALL, Executor> rps) {
            this.rps = rps;
        }

        public Executor asynchronous(Executor original, CALL asynchCall, Object node) throws UnknownTypeException {
            RequestProcessor rp = (RequestProcessor) rps.get(asynchCall);
            if (rp != null) {
                return rp;
            } else {
                return AsynchronousModelFilter.CURRENT_THREAD;
            }
        }

    }

}
