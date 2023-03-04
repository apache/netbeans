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
