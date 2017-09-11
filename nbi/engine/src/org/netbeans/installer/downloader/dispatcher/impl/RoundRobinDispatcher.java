/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.downloader.dispatcher.impl;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.netbeans.installer.downloader.dispatcher.LoadFactor;
import org.netbeans.installer.downloader.dispatcher.Process;
import org.netbeans.installer.downloader.dispatcher.ProcessDispatcher;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.MutualHashMap;
import org.netbeans.installer.utils.helper.MutualMap;

/**
 * @author Danila_Dugurov
 */
public class RoundRobinDispatcher implements ProcessDispatcher {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static final Map<LoadFactor, Byte> quantumToSkip = 
            new HashMap<LoadFactor, Byte>();
    
    static {
        quantumToSkip.put(LoadFactor.FULL, (byte) 0);
        quantumToSkip.put(LoadFactor.AVERAGE, (byte) 2);
        quantumToSkip.put(LoadFactor.LOW, (byte) 10);
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private final int timeQuantum;
    private final WorkersPool pool;
    private final BlockingQueue<Worker> workingQueue;
    private final Queue<Process> waitingQueue;
    private final MutualMap<Process, Worker> proc2Worker;
    private Set<Worker> makedToStop = new HashSet<Worker>();

    private Thread dispatcherThread;
    private Terminator terminator = new Terminator();
    private boolean isActive;
    private LoadFactor factor;

    public RoundRobinDispatcher(
            final int quantum, 
            final int poolSize) {
        if (quantum < 10 || poolSize < 1) {
            throw new IllegalArgumentException();
        }
        this.timeQuantum = quantum;
        this.pool = new WorkersPool(poolSize);
        workingQueue = new ArrayBlockingQueue<Worker>(poolSize);
        waitingQueue = new LinkedList<Process>();
        proc2Worker = new MutualHashMap<Process, Worker>();
        factor = LoadFactor.FULL;
    }

    public synchronized boolean schedule(final Process process) {
        synchronized (waitingQueue) {
            waitingQueue.offer(process);
            waitingQueue.notify();
        }
        return true;
    }

    public synchronized void terminate(final Process process) {
        synchronized (waitingQueue) {
            if (waitingQueue.remove(process)) {
                return;
            }
        }
        final Worker stoped = proc2Worker.get(process);
        makedToStop.add(stoped);
        terminateInternal(process);
    }

    public void setLoadFactor(final LoadFactor factor) {
        this.factor = factor;
    }

    public LoadFactor loadFactor() {
        return factor;
    }

    private void terminateInternal(final Process process) {
        final Worker worker = proc2Worker.get(process);
        if (worker == null) {
            return;
        }
        if (worker.isFree()) {
            return;
        }
        if (!terminator.isAlive()) {
            terminator.start();
        }
        terminator.terminate(process);
        SystemUtils.sleep(timeQuantum);
        if (terminator.isBusy()) {
            terminator.stop();
            terminator = new Terminator();
        }
        if (!worker.isFree()) {
            worker.stop();
        }
        proc2Worker.remove(process);
        pool.release(worker);
        workingQueue.remove(worker);
    }

    public synchronized boolean isActive() {
        return isActive;
    }

    // for tracknig perpose no synchronization so no sure of correctness
    public int activeCount() {
        return proc2Worker.size();
    }

    // for tracknig perpose no synchronization so no sure of correctness
    public int waitingCount() {
        return waitingQueue.size();
    }

    public synchronized void start() {
        if (isActive) {
            return;
        }
        dispatcherThread = new Thread(new DispatcherWorker());
        dispatcherThread.setDaemon(true);
        dispatcherThread.start();
        isActive = true;
    }

    public synchronized void stop() {
        if (!isActive) {
            return;
        }
        dispatcherThread.interrupt();
        try {
            dispatcherThread.join((timeQuantum) * (pool.capacity() + 3));
        } catch (InterruptedException exit) {
        } finally {
            //this condition mustn't happens to true
            if (dispatcherThread.isAlive()) {
                dispatcherThread.stop();
            }
        }
        waitingQueue.clear();
        isActive = false;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private class DispatcherWorker implements Runnable {
        Worker current;

        public void run() {
            while (true) {
                if (Thread.interrupted()) {
                    break;
                }
                try {
                    current = workingQueue.poll();
                    if (current == null || makedToStop.contains(current)) {
                        synchronized (waitingQueue) {
                            if (waitingQueue.isEmpty()) {
                                waitingQueue.wait();
                            }
                        }
                        filWorkingQueue();
                        continue;
                    }
                    invokeCurrent();
                    Thread.sleep(timeQuantum);
                    suspendCurrent();
                    if (factor != LoadFactor.FULL) {
                        Thread.sleep(quantumToSkip.get(factor) * timeQuantum);
                    }
                } catch (InterruptedException exit) {
                    suspendCurrent();
                    break;
                }
            }
            terminateAll();
        }

        private void terminateAll() {
            for (Worker worker : workingQueue.toArray(new Worker[0])) {
                terminateInternal(proc2Worker.reversedGet(worker));
            }
        }

        private void invokeCurrent() {
            switch (current.getState()) {
                case NEW:
                    current.start();
                    break;
                case RUNNABLE:
                    current.resume();
                    break;
                case TERMINATED:
                    break;
                default:
                    current.resume();
                    //temprorary while blocking queue not impl.
            }
        }

        private void suspendCurrent() {
            if (current == null) {
                return;
            }
            if (makedToStop.contains(current)) {
                return;
            }
            current.suspend();
            if (current.isAlive() && !current.isFree()) {
                workingQueue.offer(current);
            } else {
                proc2Worker.reversedRemove(current);
                pool.release(current);
            }
            filWorkingQueue();
        }

        private void filWorkingQueue() {
            if (waitingQueue.size() == 0 || pool.remaining() == 0) {
                return;
            }
            synchronized (waitingQueue) {
                while (workingQueue.remainingCapacity() > 0) {
                    if (waitingQueue.isEmpty()) {
                        return;
                    }
                    final Worker worker = pool.tryAcquire();
                    final Process process = waitingQueue.poll();
                    worker.setCurrent(process);
                    proc2Worker.put(process, worker);
                    makedToStop.remove(worker);
                    workingQueue.add(worker);
                }
            }
        }
    }
    
    private class Terminator extends Thread {
        private Process current;

        public Terminator() {
            setDaemon(true);
        }

        public synchronized void terminate(final Process process) {
            current = process;
            notifyAll();
        }
        
        @Override
        public void run() {
            while (true) {
                synchronized (this) {
                    try {
                        Thread.interrupted();
                        if (current == null) {
                            wait();
                            if (current == null) {
                                continue;
                            }
                        }
                        final Worker worker = proc2Worker.get(current);
                        worker.resume();
                        worker.interrupt();
                        try {
                            current.terminate();
                        } catch (Exception ignored) { //may be log?
                        }
                        current = null;
                    } catch (InterruptedException e) {
                        ErrorManager.notifyDebug(
                                "Terminator thread interrupted", e); // NOI18N
                        break;
                    }
                }
            }
        }

        public synchronized boolean isBusy() {
            return current == null;
        }
    }
}
