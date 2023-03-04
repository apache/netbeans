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
import threaddemo.locking.Locks;
import threaddemo.locking.PrivilegedLock;

/**
 * A phadhail in which all model methods are locked with a plain lock.
 * In this variant, the impl acquires locks automatically, though another
 * style would be to require the client to do this.
 * @author Jesse Glick
 */
final class LockedPhadhail extends AbstractPhadhail {

    private static final PrivilegedLock PLOCK = new PrivilegedLock();
    static {
        Locks.readWrite(PLOCK);
    }
    
    private static final AbstractPhadhail.Factory FACTORY = new AbstractPhadhail.Factory() {
        public AbstractPhadhail create(File f) {
            return new LockedPhadhail(f);
        }
    };
    
    public static Phadhail create(File f) {
        return forFile(f, FACTORY);
    }
    
    private LockedPhadhail(File f) {
        super(f);
    }
    
    protected Factory factory() {
        return FACTORY;
    }
    
    public List<Phadhail> getChildren() {
        PLOCK.enterRead();
        try {
            return super.getChildren();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public String getName() {
        PLOCK.enterRead();
        try {
            return super.getName();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public String getPath() {
        PLOCK.enterRead();
        try {
            return super.getPath();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public boolean hasChildren() {
        PLOCK.enterRead();
        try {
            return super.hasChildren();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public void rename(String nue) throws IOException {
        PLOCK.enterWrite();
        try {
            super.rename(nue);
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        PLOCK.enterWrite();
        try {
            return super.createContainerPhadhail(name);
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        PLOCK.enterWrite();
        try {
            return super.createLeafPhadhail(name);
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public void delete() throws IOException {
        PLOCK.enterWrite();
        try {
            super.delete();
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public InputStream getInputStream() throws IOException {
        PLOCK.enterRead();
        try {
            return super.getInputStream();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        // See comment in AbstractPhadhail re. use of read access.
        PLOCK.enterRead();
        try {
            return super.getOutputStream();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public RWLock lock() {
        return PLOCK.getLock();
    }
    
}
