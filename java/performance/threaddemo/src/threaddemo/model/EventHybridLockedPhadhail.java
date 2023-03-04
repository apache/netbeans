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

package threaddemo.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import threaddemo.locking.RWLock;
import threaddemo.locking.LockAction;
import threaddemo.locking.LockExceptionAction;
import threaddemo.locking.Locks;

/**
 * Similar to LockedPhadhail but using the "event hybrid" lock.
 * @author Jesse Glick
 */
final class EventHybridLockedPhadhail extends AbstractPhadhail {

    private static final AbstractPhadhail.Factory FACTORY = new AbstractPhadhail.Factory() {
        public AbstractPhadhail create(File f) {
            return new EventHybridLockedPhadhail(f);
        }
    };
    
    public static Phadhail create(File f) {
        return forFile(f, FACTORY);
    }
    
    private EventHybridLockedPhadhail(File f) {
        super(f);
    }
    
    protected Factory factory() {
        return FACTORY;
    }
    
    public List<Phadhail> getChildren() {
        return Locks.eventHybrid().read(new LockAction<List<Phadhail>>() {
            public List<Phadhail> run() {
                return EventHybridLockedPhadhail.super.getChildren();
            }
        });
    }
    
    public String getName() {
        return Locks.eventHybrid().read(new LockAction<String>() {
            public String run() {
                return EventHybridLockedPhadhail.super.getName();
            }
        });
    }
    
    public String getPath() {
        return Locks.eventHybrid().read(new LockAction<String>() {
            public String run() {
                return EventHybridLockedPhadhail.super.getPath();
            }
        });
    }
    
    public boolean hasChildren() {
        return Locks.eventHybrid().read(new LockAction<Boolean>() {
            public Boolean run() {
                return EventHybridLockedPhadhail.super.hasChildren();
            }
        });
    }
    
    public void rename(final String nue) throws IOException {
        Locks.eventHybrid().write(new LockExceptionAction<Void,IOException>() {
            public Void run() throws IOException {
                EventHybridLockedPhadhail.super.rename(nue);
                return null;
            }
        });
    }
    
    public Phadhail createContainerPhadhail(final String name) throws IOException {
        return Locks.eventHybrid().write(new LockExceptionAction<Phadhail,IOException>() {
            public Phadhail run() throws IOException {
                return EventHybridLockedPhadhail.super.createContainerPhadhail(name);
            }
        });
    }
    
    public Phadhail createLeafPhadhail(final String name) throws IOException {
        return Locks.eventHybrid().write(new LockExceptionAction<Phadhail,IOException>() {
            public Phadhail run() throws IOException {
                return EventHybridLockedPhadhail.super.createLeafPhadhail(name);
            }
        });
    }
    
    public void delete() throws IOException {
        Locks.eventHybrid().write(new LockExceptionAction<Void,IOException>() {
            public Void run() throws IOException {
                EventHybridLockedPhadhail.super.delete();
                return null;
            }
        });
    }
    
    public InputStream getInputStream() throws IOException {
        return Locks.eventHybrid().read(new LockExceptionAction<InputStream,IOException>() {
            public InputStream run() throws IOException {
                return EventHybridLockedPhadhail.super.getInputStream();
            }
        });
    }
    
    public OutputStream getOutputStream() throws IOException {
        // See comments in AbstractPhadhail.getOutputStream.
        return Locks.eventHybrid().read(new LockExceptionAction<OutputStream,IOException>() {
            public OutputStream run() throws IOException {
                return EventHybridLockedPhadhail.super.getOutputStream();
            }
        });
    }
    
    public RWLock lock() {
        return Locks.eventHybrid();
    }
    
}
