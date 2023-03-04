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

package org.netbeans.modules.settings;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.*;

/** Provides support for async storing of a setting objects.
 * @author  Jan Pokorsky
 */
public final class ScheduledRequest implements Runnable {
    private static final RequestProcessor PROCESSOR =
        new RequestProcessor("Settings Processor"); //NOI18N
    private static final int DELAY = 2000;
    
    private Object inst;
    private Task task;
    private FileLock lock;
    private final FileObject fobj;
    private final FileSystem.AtomicAction run;
    private boolean running = false;
    /** counter of scheduled tasks */
    private int counter = 0;
    
    /** Creates a new instance of ScheduledRequest
     * @param fobj file containing a persistent setting object
     * @param run impl performing the storing task
     */
    public ScheduledRequest(FileObject fobj, FileSystem.AtomicAction run) {
        this.fobj = fobj;
        this.run = run;
    }
    
    /** Is the request being executed? */
    public boolean isRunning() {
        return running;
    }
    
    /** Provide a file lock; can be null if it is not invoked from the impl of
     * run.run() (see {@link #ScheduledRequest}). Do not release the lock,
     * it is the ScheduledRequest responsibility!
     */
    public FileLock getFileLock() {
        return lock;
    }
    
    /** Schedule the request, lock the file and keep the obj reference.
     * @param obj a setting object. ScheduledRequest prevents object to be GCed
     * before running the request task
     */
    public synchronized void schedule(Object obj) {
        schedule(obj, DELAY);
    }
    
    private void schedule(Object obj, int delay) {
        if (obj == null) return;
        if (inst != null && inst != obj)
            throw new IllegalStateException("Inconsistant state! File: " + fobj); //NOI18N
        
        try {
            if (lock == null) lock = fobj.lock();
        } catch (IOException ex) {
            Logger.getLogger(ScheduledRequest.class.getName()).log(Level.WARNING, null, ex);
            return;
        }
        
        counter++;
        if (task == null) {
            task = PROCESSOR.post(this, delay);
        } else {
            task.schedule(delay);
        }
        
        // prevent object to be GCed before running the task
        this.inst = obj;
    }
    
    /** Try to cancel the scheduled request if exists and perform the request task.
     * @exception IOException if the storing fails
     */
    public synchronized void runAndWait() throws IOException {
        if (task != null) {
            // if anything was scheduled cancel it
            task.cancel();
            counter = 0;
            releaseResources();
        }
        lock = fobj.lock();
        performRequest();
    }
    
    /** force to finish the scheduled request */
    public void forceToFinish() {
        Task t = null;
        synchronized (this) {
            if (task != null) {
                t = task;
                task.schedule(0);
            }
        }
        if (t != null) t.waitFinished();
    }
    
    /** cancel the scheduled request task */
    public void cancel() {
        RequestProcessor.Task t = task;
        if (t == null) return ;
        
        if (isRunning()) {
            t.waitFinished();
        } else {
            synchronized (this) {
                t.cancel();
                counter = 0;
                releaseResources();
            }
        }
    }
    
    public void run() {
        synchronized (this) {
            counter = 0;
        }
        
        try {
            performRequest();
        } catch (IOException ex) {
            Exceptions.attachLocalizedMessage(ex, fobj.toString());
            Logger.getLogger(ScheduledRequest.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    /** perform the storing task, release the file lock and reset
     * ScheduledRequest */
    private void performRequest() throws IOException {
        boolean isAlreadyRunning = false;
        Task runningTask;
        
        synchronized (this) {
            isAlreadyRunning = running;
            runningTask = task;
            running = true;
        }
        
        if (isAlreadyRunning) {
            runningTask.waitFinished();
            return;
        }
        
        try {
            run.run();
        } finally {
            synchronized (this) {
                running = false;
                if (task == null || counter == 0) {
                    releaseResources();
                }
            }
        }
    }
    
    private void releaseResources() {
        if (lock != null) {
            lock.releaseLock();
            lock = null;
        }
        inst = null;
        task = null;
    }
    
}
