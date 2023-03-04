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

package threaddemo.locking;

// XXX need test

// XXX could track read vs. write state

/**
 * Simple lock that actually just uses a simple synchronization monitor.
 * @author Jesse Glick
 */
final class MonitorLock implements RWLock {

    private final Object monitor;

    MonitorLock(Object monitor) {
        this.monitor = monitor;
    }

    public boolean canRead() {
        return Thread.holdsLock(monitor);
    }

    public <T, E extends Exception> T read(LockExceptionAction<T,E> action) throws E {
        synchronized (monitor) {
            return action.run();
        }
    }
    
    public <T> T read(LockAction<T> action) {
        synchronized (monitor) {
            return action.run();
        }
    }
    
    public void read(Runnable action) {
        synchronized (monitor) {
            action.run();
        }
    }
    
    public void readLater(final Runnable action) {
        Worker.start(new Runnable() {
            public void run() {
                read(action);
            }
        });
    }
    
    public void write(Runnable action) {
        read(action);
    }
    
    public boolean canWrite() {
        return canRead();
    }
    
    public <T> T write(LockAction<T> action) {
        return read(action);
    }
    
    public <T, E extends Exception> T write(LockExceptionAction<T,E> action) throws E {
        return read(action);
    }
    
    public void writeLater(Runnable action) {
        readLater(action);
    }
    
    public String toString() {
        return "MonitorLock<monitor=" + monitor + ">";
    }
    
}
