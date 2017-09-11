/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadGroupReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMOutOfMemoryExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ThreadDeathEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ThreadStartEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.openide.util.Exceptions;

/**
 *
 * @author martin
 */
public class ThreadsCache implements Executor {
    
    public static final String PROP_THREAD_STARTED = "threadStarted";   // NOI18N
    public static final String PROP_THREAD_DIED = "threadDied";         // NOI18N
    public static final String PROP_GROUP_ADDED = "groupAdded";         // NOI18N
    
    /** Threads containing this pattern in their names are filtered out (private threads, see visual debugger). */
    public static final String THREAD_NAME_FILTER_PATTERN = "org.netbeans.modules.debugger.jpda";  // NOI18N

    private static final Logger logger = Logger.getLogger(ThreadsCache.class.getName());
    
    private VirtualMachine vm;
    private JPDADebuggerImpl debugger;
    // Map of thread groups. Null when not initialized, otherwise contains
    // list of sub-groups for a group key.
    // uninitializedGroupList values for unknown sub-groups
    private Map<ThreadGroupReference, List<ThreadGroupReference>> groupMap;
    private final List<ThreadGroupReference> uninitializedGroupList = Collections.emptyList();
    // Map of threads hierarchy - list of threads for group keys.
    // Null when not initialized.
    private Map<ThreadGroupReference, List<ThreadReference>> threadMap;
    private List<ThreadReference> allThreads;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final boolean[] canFireChanges = new boolean[] { false };
    
    public ThreadsCache(JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        //groupMap = new HashMap<ThreadGroupReference, List<ThreadGroupReference>>();
        //threadMap = new HashMap<ThreadGroupReference, List<ThreadReference>>();
        allThreads = new ArrayList<ThreadReference>();
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm != null) {
            setVirtualMachine(vm);
        }
    }
    
    public void setVirtualMachine(VirtualMachine vm) {
        List<ThreadReference> _allThreads;
        List<ThreadGroupReference> _allGroups;
        synchronized (this) {
            if (this.vm == vm) return ;
            try {
                this.vm = vm;
                ThreadStartRequest tsr = EventRequestManagerWrapper.createThreadStartRequest(
                        VirtualMachineWrapper.eventRequestManager(vm));
                ThreadDeathRequest tdr = EventRequestManagerWrapper.createThreadDeathRequest(
                        VirtualMachineWrapper.eventRequestManager(vm));
                EventRequestWrapper.setSuspendPolicy(tsr, ThreadStartRequest.SUSPEND_NONE);
                EventRequestWrapper.setSuspendPolicy(tdr, ThreadStartRequest.SUSPEND_NONE);
                debugger.getOperator().register(tsr, this);
                debugger.getOperator().register(tdr, this);
                EventRequestWrapper.enable(tsr);
                EventRequestWrapper.enable(tdr);
                init();
            } catch (VMDisconnectedExceptionWrapper e) {
                this.vm = null;
            } catch (InternalExceptionWrapper e) {
                this.vm = null;
            } catch (ObjectCollectedExceptionWrapper e) {
                this.vm = null;
            } catch (InvalidRequestStateExceptionWrapper irse) {
                Exceptions.printStackTrace(irse);
                this.vm = null;
            }
            _allThreads = new ArrayList<ThreadReference>(allThreads);
            if (groupMap != null) {
                _allGroups = getAllGroups();
            } else {
                _allGroups = Collections.emptyList();
            }
            
        }
        for (ThreadReference t : _allThreads) {
            pcs.firePropertyChange(PROP_THREAD_STARTED, null, t);
        }
        for (ThreadGroupReference g : _allGroups) {
            pcs.firePropertyChange(PROP_GROUP_ADDED, null, g);
        }
        synchronized (canFireChanges) {
            canFireChanges[0] = true;
            canFireChanges.notifyAll();
        }
    }
    
    private synchronized void init() throws VMDisconnectedExceptionWrapper, InternalExceptionWrapper {
        allThreads = new ArrayList<ThreadReference>(VirtualMachineWrapper.allThreads(vm));
        filterThreads(allThreads);
    }
    
    private void filterThreads(List<ThreadReference> threads) {
        for (int i = 0; i < threads.size(); i++) {
            ThreadReference tr = threads.get(i);
            try {
                if (ThreadReferenceWrapper.name(tr).contains(THREAD_NAME_FILTER_PATTERN)) {
                    threads.remove(i);
                    i--;
                }
            } catch (Exception ex) {
                // continue
            }
        }
    }

    private void initGroups(ThreadGroupReference group) {
        try {
            List<ThreadGroupReference> groups = new ArrayList(ThreadGroupReferenceWrapper.threadGroups0(group));
            List<ThreadReference> threads = new ArrayList(ThreadGroupReferenceWrapper.threads0(group));
            filterThreads(threads);
            groupMap.put(group, groups);
            threadMap.put(group, threads);
            for (ThreadGroupReference g : groups) {
                initGroups(g);
            }
        } catch (ObjectCollectedException e) {
        }
    }

    private synchronized void initThreadGroups() throws VMDisconnectedExceptionWrapper, VMOutOfMemoryExceptionWrapper {
        threadMap = new HashMap<ThreadGroupReference, List<ThreadReference>>();
        if (groupMap == null) {
            groupMap = new HashMap<ThreadGroupReference, List<ThreadGroupReference>>();
        } else {
            // Remove unknown groups:
            Set<ThreadGroupReference> groups = new HashSet<ThreadGroupReference>(groupMap.keySet());
            for (ThreadGroupReference g : groups) {
                if (groupMap.get(g) == uninitializedGroupList) {
                    groupMap.remove(g);
                }
            }
        }
        List<ThreadGroupReference> groups;
        groups = groupMap.get(null);
        if (groups == null) {
            try {
                groups = new ArrayList(VirtualMachineWrapper.topLevelThreadGroups(vm));
            } catch (InternalExceptionWrapper ex) {
                return ;
            }
            groupMap.put(null, groups);
        }
        for (ThreadGroupReference group : groups) {
            initGroups(group);
        }
        List<ThreadReference> mainThreads = new ArrayList();
        threadMap.put(null, mainThreads);
        for (ThreadReference thread : allThreads) {
            try {
                if (ThreadReferenceWrapper.threadGroup(thread) == null) {
                    mainThreads.add(thread);
                }
            } catch (ObjectCollectedExceptionWrapper e) {
            } catch (IllegalThreadStateExceptionWrapper e) {
            } catch (InternalExceptionWrapper e) {
            }
        }
    }

    public synchronized List<ThreadReference> getAllThreads() {
        return Collections.unmodifiableList(new ArrayList(allThreads));
    }
    
    public synchronized List<ThreadGroupReference> getTopLevelThreadGroups() {
        boolean uninitialized;
        if (groupMap == null) {
            groupMap = new HashMap<ThreadGroupReference, List<ThreadGroupReference>>();
            uninitialized = true;
        } else {
            uninitialized = false;
        }
        List<ThreadGroupReference> topGroups = groupMap.get(null);
        if (topGroups == null) {
            if (vm == null) {
                return Collections.EMPTY_LIST;
            }
            topGroups = new ArrayList(VirtualMachineWrapper.topLevelThreadGroups0(vm));
            groupMap.put(null, topGroups);
            if (uninitialized) {
                for (ThreadGroupReference g : topGroups) {
                    groupMap.put(g, uninitializedGroupList);
                }
            }
        }
        return Collections.unmodifiableList(new ArrayList(topGroups));
    }
    
    public synchronized List<ThreadReference> getThreads(ThreadGroupReference group) {
        if (threadMap == null) {
            try {
                initThreadGroups();
            } catch (VMDisconnectedExceptionWrapper ex) {
                return Collections.emptyList();
            } catch (VMOutOfMemoryExceptionWrapper ex) {
                return Collections.emptyList();
            }
        }
        List<ThreadReference> threads = threadMap.get(group);
        if (threads == null) {
            threads = Collections.emptyList();
        } else {
            threads = Collections.unmodifiableList(new ArrayList(threads));
        }
        return threads;
    }

    public synchronized List<ThreadGroupReference> getGroups(ThreadGroupReference group) {
        if (groupMap == null) {
            try {
                initThreadGroups();
            } catch (VMDisconnectedExceptionWrapper ex) {
                return Collections.emptyList();
            } catch (VMOutOfMemoryExceptionWrapper ex) {
                return Collections.emptyList();
            }
        }
        List<ThreadGroupReference> groups = groupMap.get(group);
        if (groups == uninitializedGroupList) {
            groups = new ArrayList(ThreadGroupReferenceWrapper.threadGroups0(group));
            groupMap.put(group, groups);
        }
        if (groups == null) {
            groups = Collections.emptyList();
        } else {
            groups = Collections.unmodifiableList(new ArrayList(groups));
        }
        return groups;
    }

    private synchronized List<ThreadGroupReference> getAllGroups() {
        if (groupMap == null) {
            try {
                initThreadGroups();
            } catch (VMDisconnectedExceptionWrapper ex) {
                return Collections.emptyList();
            } catch (VMOutOfMemoryExceptionWrapper ex) {
                return Collections.emptyList();
            }
        }
        List<ThreadGroupReference> groups = new ArrayList<ThreadGroupReference>();
        fillAllGroups(groups, null);
        return groups;
    }

    private void fillAllGroups(List<ThreadGroupReference> groups, ThreadGroupReference g) {
        List<ThreadGroupReference> gs = groupMap.get(g);
        if (gs != null) {
            if (gs == uninitializedGroupList) {
                gs = new ArrayList(ThreadGroupReferenceWrapper.threadGroups0(g));
                groupMap.put(g, gs);
            }
            groups.addAll(gs);
            for (ThreadGroupReference gg : gs) {
                fillAllGroups(groups, gg);
            }
        }
    }
    
    private List<ThreadGroupReference> addGroups(ThreadGroupReference group) throws ObjectCollectedExceptionWrapper {
        if (threadMap != null && !threadMap.containsKey(group)) {
            List<ThreadReference> threads = new ArrayList();
            threadMap.put(group, threads);
        }
        if (groupMap == null) {
            return Collections.emptyList();
        }
        List<ThreadGroupReference> addedGroups = new ArrayList<ThreadGroupReference>();
        ThreadGroupReference parent;
        try {
            parent = ThreadGroupReferenceWrapper.parent(group);
        } catch (InternalExceptionWrapper ex) {
            return Collections.emptyList();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return Collections.emptyList();
        }
        if (groupMap.get(parent) == null) {
            if (parent != null) {
                addedGroups.addAll(addGroups(parent));
            } else {
                List<ThreadGroupReference> topGroups = new ArrayList(VirtualMachineWrapper.topLevelThreadGroups0(vm));
                groupMap.put(null, topGroups);
                addedGroups.addAll(topGroups);
            }
        }
        List<ThreadGroupReference> parentsGroups = groupMap.get(parent);
        if (parentsGroups != null && !parentsGroups.contains(group)) {
            if (parentsGroups == uninitializedGroupList) {
                parentsGroups = new ArrayList(ThreadGroupReferenceWrapper.threadGroups0(parent));
                groupMap.put(parent, parentsGroups);
                addedGroups.addAll(parentsGroups);
            } else {
                parentsGroups.add(group);
                addedGroups.add(group);
            }
            List<ThreadGroupReference> groups = new ArrayList();
            groupMap.put(group, groups);
        }
        return addedGroups;
    }

    public boolean exec(Event event) {
        if (event instanceof ThreadStartEvent) {
            ThreadReference thread;;
            boolean handleGroups = debugger.isInterestedInThreadGroups();
            ThreadGroupReference group = null;
            try {
                thread = ThreadStartEventWrapper.thread((ThreadStartEvent) event);
                String name = ThreadReferenceWrapper.name(thread);
                if (name.contains(THREAD_NAME_FILTER_PATTERN)) {
                    // Filtered
                    return true;
                }
                if (handleGroups) {
                    group = ThreadReferenceWrapper.threadGroup(thread);
                }
            } catch (InternalExceptionWrapper ex) {
                return true;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return true;
            } catch (VMOutOfMemoryExceptionWrapper ex) {
                return true;
            } catch (IllegalThreadStateExceptionWrapper ex) {
                return true;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                return true;
            }
            List<ThreadGroupReference> addedGroups = null;
            if (logger.isLoggable(Level.FINE)) {
                try {
                    logger.fine("ThreadStartEvent: "+thread+", group = "+group+", handleGroups = "+handleGroups);
                } catch (Exception ex) {
                    logger.log(Level.FINE, ex.getLocalizedMessage(), ex);
                }
            }
            synchronized (this) {
                if (group != null) {
                    try {
                        addedGroups = addGroups(group);
                    } catch (ObjectCollectedExceptionWrapper ex) {
                        try {
                            if (ObjectReferenceWrapper.isCollected(thread)) {
                                return true;
                            }
                        } catch (InternalExceptionWrapper ex1) {
                            return true;
                        } catch (VMDisconnectedExceptionWrapper ex1) {
                            return true;
                        } catch (ObjectCollectedExceptionWrapper ex1) {
                            return true;
                        }
                    }
                }
                if (!handleGroups) {
                    groupMap = null;
                    threadMap = null;
                } else {
                    if (threadMap != null) {
                        List<ThreadReference> threads = threadMap.get(group);
                        if (threads != null && !threads.contains(thread)) { // could be added by init()
                            threads.add(thread);
                        }
                    }
                }
                if (!allThreads.contains(thread)) { // could be added by init()
                    allThreads.add(thread);
                }
            }
            synchronized (canFireChanges) {
                if (!canFireChanges[0]) {
                    try {
                        canFireChanges.wait();
                    } catch (InterruptedException ex) {}
                }
            }
            if (addedGroups != null) {
                for (ThreadGroupReference g : addedGroups) {
                    pcs.firePropertyChange(PROP_GROUP_ADDED, null, g);
                }
            }
            pcs.firePropertyChange(PROP_THREAD_STARTED, null, thread);
        }
        if (event instanceof ThreadDeathEvent) {
            ThreadReference thread;
            ThreadGroupReference group;
            try {
                thread = ThreadDeathEventWrapper.thread((ThreadDeathEvent) event);
            } catch (InternalExceptionWrapper ex) {
                return true;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return true;
            }
            boolean getGroup;
            synchronized (this) {
                getGroup = groupMap != null || threadMap != null;
            }
            if (getGroup) {
                try {
                    group = ThreadReferenceWrapper.threadGroup(thread);
                } catch (InternalExceptionWrapper ex) {
                    group = null;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return true;
                } catch (VMOutOfMemoryExceptionWrapper ex) {
                    return true;
                } catch (IllegalThreadStateExceptionWrapper ex) {
                    group = null;
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    group = null;
                }
            } else {
                group = null;
            }
            if (logger.isLoggable(Level.FINE)) {
                try {
                    logger.fine("ThreadDeathEvent: "+thread+", group = "+group+", groupMap = "+groupMap);
                } catch (Exception ex) {
                    logger.log(Level.FINE, ex.getLocalizedMessage(), ex);
                }
            }
            boolean removed = false;
            synchronized (this) {
                if (threadMap != null) {
                    List<ThreadReference> threads;
                    if (group != null) {
                        threads = threadMap.get(group);
                    } else {
                        threads = null;
                        for (List<ThreadReference> testThreads : threadMap.values()) {
                            if (testThreads.contains(thread)) {
                                threads = testThreads;
                            }
                        }
                    }
                    if (threads != null) {
                        threads.remove(thread);
                    }
                }
                removed = allThreads.remove(thread);
            }
            if (removed) {
                synchronized (canFireChanges) {
                    if (!canFireChanges[0]) {
                        try {
                            canFireChanges.wait();
                        } catch (InterruptedException ex) {}
                    }
                }
                pcs.firePropertyChange(PROP_THREAD_DIED, thread, null);
            }
        }
        return true;
    }

    public void removed(EventRequest eventRequest) {
    }

    /**
     * Check if the given thread is in the threads cache and add it if it's not.
     * This should be called when we have a suspicion that we did not receive
     * some events or that thread IDs changed (see e.g.: http://bugs.sun.com/view_bug.do?bug_id=6862295).
     */
    public void assureThreadIsCached(ThreadReference tref) {
        boolean contains;
        synchronized (this) {
            contains = allThreads.contains(tref);
        }
        if (!contains) {
            String tname;
            try {
                tname = tref.toString();
                if (tname.contains(THREAD_NAME_FILTER_PATTERN)) {
                    return ;
                }
            } catch (Exception ex) {
                tname = ex.getLocalizedMessage();
            }
            logger.info("Must SYNCHRONIZE ThreadsCache, did not found "+tname);
            sync();
        }
    }

    /**
     * Synchronize the cached threads with VM threads.
     * This should be called when we have a suspicion that we did not receive
     * some events or that thread IDs changed (see e.g.: http://bugs.sun.com/view_bug.do?bug_id=6862295).
     */
    private void sync() {
        List<ThreadReference> newThreads;
        List<ThreadReference> oldThreads;
        List<ThreadGroupReference> addedGroups = null;
        synchronized (this) {
            // Synchronize soon so that we do not add back threads removed from events,
            // or remove threads added from events
            List<ThreadReference> allThreadsNew;
            try {
                allThreadsNew = new ArrayList<ThreadReference>(VirtualMachineWrapper.allThreads(vm));
            } catch (InternalExceptionWrapper iex) {
                return ;
            } catch (VMDisconnectedExceptionWrapper vmdex) {
                return ;
            }
            filterThreads(allThreadsNew);

            newThreads = new ArrayList<ThreadReference>(allThreadsNew);
            newThreads.removeAll(allThreads);
            oldThreads = new ArrayList<ThreadReference>(allThreads);
            oldThreads.removeAll(allThreadsNew);

            // Add new threads:
            for (ThreadReference thread : newThreads) {
                ThreadGroupReference group;
                try {
                    group = ThreadReferenceWrapper.threadGroup(thread);
                } catch (InternalExceptionWrapper ex) {
                    continue;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return ;
                } catch (VMOutOfMemoryExceptionWrapper ex) {
                    return ;
                } catch (IllegalThreadStateExceptionWrapper ex) {
                    continue;
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    continue;
                }
                if (group != null) {
                    try {
                        if (addedGroups == null) {
                            addedGroups = addGroups(group);
                        } else {
                            addedGroups.addAll(addGroups(group));
                        }
                    } catch (ObjectCollectedExceptionWrapper occex) {
                        try {
                            if (ObjectReferenceWrapper.isCollected(thread)) {
                                continue;
                            }
                        } catch (InternalExceptionWrapper ex1) {
                            continue;
                        } catch (VMDisconnectedExceptionWrapper ex1) {
                            return ;
                        } catch (ObjectCollectedExceptionWrapper ex1) {
                            continue;
                        }
                    }
                }
                if (threadMap != null) {
                    List<ThreadReference> threads = threadMap.get(group);
                    if (threads != null && !threads.contains(thread)) { // could be added by init()
                        threads.add(thread);
                    }
                }
            }
            allThreads.addAll(newThreads);

            // Remove old threads:
            if (threadMap != null) {
                for (ThreadReference thread : oldThreads) {
                    ThreadGroupReference group;
                    try {
                        group = ThreadReferenceWrapper.threadGroup(thread);
                    } catch (InternalExceptionWrapper ex) {
                        group = null;
                    } catch (VMDisconnectedExceptionWrapper ex) {
                        return ;
                    } catch (VMOutOfMemoryExceptionWrapper ex) {
                        return ;
                    } catch (IllegalThreadStateExceptionWrapper ex) {
                        group = null;
                    } catch (ObjectCollectedExceptionWrapper ocex) {
                        group = null;
                    }
                    List<ThreadReference> threads;
                    if (group != null) {
                        threads = threadMap.get(group);
                    } else {
                        threads = null;
                        for (List<ThreadReference> testThreads : threadMap.values()) {
                            if (testThreads.contains(thread)) {
                                threads = testThreads;
                            }
                        }
                    }
                    if (threads != null) {
                        threads.remove(thread);
                    }
                }
            }
            allThreads.removeAll(oldThreads);
        } // End synchronized

        synchronized (canFireChanges) {
            if (!canFireChanges[0]) {
                try {
                    canFireChanges.wait();
                } catch (InterruptedException ex) {}
            }
        }
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("SYNCHRONIZE of ThreadsCache discovered new threads: "+threadsListing(newThreads));
            logger.config("                      and removed obsolete threads: "+threadsListing(oldThreads));
        }
        if (addedGroups != null) {
            for (ThreadGroupReference g : addedGroups) {
                pcs.firePropertyChange(PROP_GROUP_ADDED, null, g);
            }
        }
        for (ThreadReference thread : newThreads) {
            pcs.firePropertyChange(PROP_THREAD_STARTED, null, thread);
        }
        for (ThreadReference thread : oldThreads) {
            pcs.firePropertyChange(PROP_THREAD_DIED, thread, null);
        }
    }

    private static String threadsListing(List<ThreadReference> threads) {
        StringBuilder nt = new StringBuilder("[\n");
        for (ThreadReference t : threads) {
            String s;
            try {
                s = t.toString();
            } catch (Exception ex) {
                s = ex.toString();
            }
            nt.append(s);
            nt.append(",\n");
        }
        int l = nt.length();
        if (l > 1) {
            nt.delete(l - 2, l);
        }
        nt.append(']');
        return nt.toString();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}
