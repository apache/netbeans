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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ThreadReference;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
 *
 * @author martin
 */
public class DeadlockDetectorImpl extends DeadlockDetector implements PropertyChangeListener {
    
    private final Set<JPDAThread> suspendedThreads = new WeakSet<JPDAThread>();
    private final RequestProcessor rp = new RequestProcessor("Deadlock Detector", 1); // NOI18N
    private final Set<Task> unfinishedTasks = new HashSet<Task>();
    
    private Map<Long, Node> monitorToNode;

    DeadlockDetectorImpl(JPDADebugger debugger) {
        debugger.addPropertyChangeListener(this);
        List<JPDAThread> threads = debugger.getThreadsCollector().getAllThreads();
        for (JPDAThread thread : threads) {
            ((Customizer) thread).addPropertyChangeListener(WeakListeners.propertyChange(this, thread));
            if (thread.isSuspended()) {
                synchronized (suspendedThreads) {
                    suspendedThreads.add(thread);
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (JPDADebugger.PROP_THREAD_STARTED.equals(propName)) {
            JPDAThread thread = (JPDAThread) evt.getNewValue();
            ((Customizer) thread).addPropertyChangeListener(WeakListeners.propertyChange(this, thread));
        } else if (JPDAThread.PROP_SUSPENDED.equals(propName)) {
            JPDAThread thread = (JPDAThread) evt.getSource();
            boolean suspended = (Boolean) evt.getNewValue();
            if (suspended) {
                final Set<JPDAThread> tempSuspThreads;
                synchronized(suspendedThreads) {
                    suspendedThreads.add(thread);
                    tempSuspThreads = new HashSet<JPDAThread>(suspendedThreads);
                }
                final Task[] taskPtr = new Task[] { null };
                synchronized (unfinishedTasks) {
                    Task task = rp.post(new Runnable() {
                        public void run() {
                            Set<Deadlock> deadlocks;
                            deadlocks = findDeadlockedThreads(tempSuspThreads);
                            if (deadlocks != null) {
                                setDeadlocks(deadlocks);
                            }
                            synchronized (unfinishedTasks) {
                                unfinishedTasks.remove(taskPtr[0]);
                            }
                        }
                    });
                    unfinishedTasks.add(task);
                    taskPtr[0] = task;
                }
            } else {
                synchronized (suspendedThreads) {
                    suspendedThreads.remove(thread);
                }
            }
        }
    }

    public void waitForUnfinishedTasks(long timeout) throws InterruptedException {
        Set<Task> tasks = new HashSet<Task>();
        synchronized (unfinishedTasks) {
            tasks.addAll(unfinishedTasks);
        }
        while (!tasks.isEmpty()) {
            Task task = tasks.iterator().next();
            task.waitFinished(timeout);
            tasks.remove(task);
        }
    }
    
    private Set<Deadlock> findDeadlockedThreads(Collection<JPDAThread> threads) {
        buildGraph(threads);
        Set<Node> simpleNodesSet = new HashSet<Node>();
        LinkedList<Node> simpleNodesList = new LinkedList<Node>();
        for (Entry<Long, Node> entry : monitorToNode.entrySet()) {
            Node node = entry.getValue();
            if (node.isSimple()) {
                simpleNodesSet.add(node);
                simpleNodesList.add(node);
            }
        } // for
        while (simpleNodesList.size() > 0) {
            Node currNode = simpleNodesList.removeFirst();
            simpleNodesSet.remove(currNode);

            if (currNode.outgoing != null) {
                currNode.outgoing.removeIncomming(currNode);
                if (currNode.outgoing.isSimple() && !simpleNodesSet.contains(currNode.outgoing)) {
                    simpleNodesSet.add(currNode.outgoing);
                    simpleNodesList.add(currNode.outgoing);
                }
            }
            for (Node node : currNode.incomming) {
                node.setOutgoing(null);
                if (node.isSimple() && !simpleNodesSet.contains(node)) {
                    simpleNodesSet.add(node);
                    simpleNodesList.add(node);
                }
            }
            monitorToNode.remove(currNode.ownedMonitor.getUniqueID());
        } // while

        Set<Deadlock> result = null;
        if (!monitorToNode.isEmpty()) {
            result = new HashSet<Deadlock>();
            Set<Node> nodesSet = new HashSet<Node>(monitorToNode.values());
            while (!nodesSet.isEmpty()) {
                Collection<JPDAThread> deadlockedThreads = new ArrayList<>(nodesSet.size());
                Node node = nodesSet.iterator().next();
                nodesSet.remove(node);
                do {
                    deadlockedThreads.add(node.thread);
                    node = node.outgoing;
                } while (nodesSet.remove(node));
                result.add(createDeadlock(deadlockedThreads));
            } // while
        } // if
        return result;
    }

    private void buildGraph(Collection<JPDAThread> threads) {
        monitorToNode = new HashMap<Long, Node>();
        for (JPDAThread thread : threads) {
            if (thread.getState() == JPDAThread.STATE_ZOMBIE) {
                continue;
            }
            ObjectVariable contendedMonitor = thread.getContendedMonitor();
            ObjectVariable[] ownedMonitors = thread.getOwnedMonitors();
            if (contendedMonitor != null && (((JDIVariable) contendedMonitor).getJDIValue() instanceof ThreadReference)) {
                ownedMonitors = checkForThreadJoin(thread, ownedMonitors);
            }
            if (contendedMonitor == null || ownedMonitors.length == 0) {
                continue;
            } // if
            Node contNode = monitorToNode.get(contendedMonitor.getUniqueID());
            if (contNode == null) {
                contNode = new Node(null, contendedMonitor);
                monitorToNode.put(contendedMonitor.getUniqueID(), contNode);
            }
            for (int x = 0; x < ownedMonitors.length; x++) {
                Node node = monitorToNode.get(ownedMonitors[x].getUniqueID());
                if (node == null) {
                    node = new Node(thread, ownedMonitors[x]);
                    monitorToNode.put(ownedMonitors[x].getUniqueID(), node);
                } else if (node.thread == null) {
                    node.thread = thread;
                } else {
                    continue;
                }
                node.setOutgoing(contNode);
                contNode.addIncomming(node);
            } // for
        } // for
    }

    private ObjectVariable[] checkForThreadJoin(JPDAThread thread, ObjectVariable[] ownedMonitors) {
        CallStackFrame[] callStack;
        try {
            callStack = thread.getCallStack(0, 2);
        } catch (AbsentInformationException ex) {
            return ownedMonitors;
        }
        if (callStack.length < 2) {
            return ownedMonitors;
        }
        if (Thread.class.getName().equals(callStack[1].getClassName()) && "join".equals(callStack[1].getMethodName())) {    // NOI18N
            // This current thread is the "owned" monitor, it's joning the contended monitor.
            ObjectVariable[] ownedMonitorsWithThread = Arrays.copyOf(ownedMonitors, ownedMonitors.length + 1);
            ownedMonitorsWithThread[ownedMonitors.length] = (ObjectVariable) ((JPDAThreadImpl) thread).getDebugger().getVariable(((JPDAThreadImpl) thread).getThreadReference());
            return ownedMonitorsWithThread;
        } else {
            return ownedMonitors;
        }
    }
    
    // **************************************************************************
    
    private static class Node {
        JPDAThread thread;
        ObjectVariable ownedMonitor;
        Collection<Node> incomming = new HashSet<Node>();
        Node outgoing = null;

        Node (JPDAThread thread, ObjectVariable ownedMonitor) {
            this.thread = thread;
            this.ownedMonitor = ownedMonitor;
        }

        void setOutgoing(Node node) {
            outgoing = node;
        }

        void addIncomming(Node node) {
            incomming.add(node);
        }

        void removeIncomming(Node node) {
            incomming.remove(node);
        }

        boolean isSimple() {
            return outgoing == null || incomming.size() == 0;
        }
    }

}
