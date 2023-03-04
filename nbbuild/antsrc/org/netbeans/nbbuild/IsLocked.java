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
package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;

/** Checks whether given file can be locked or not.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class IsLocked implements Condition {
    private File file;
    // defaults as for tryLock():
    private long position = 0L;
    private long size = Long.MAX_VALUE;
    private boolean shared = false;

    public void setShared(boolean shared) {
        this.shared = shared;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
    
    public void setPosition(long position) {
        this.position = position;
    }
    
    public void setSize(long size) {
        this.size = size;
    }

    public boolean eval() throws BuildException {
        if (file == null) {
            throw new BuildException("file needs to be specified");
        }
        if (!file.exists()) {
            return false;
        }
        RandomAccessFile raf = null;
        FileLock lock = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            try {
                lock = raf.getChannel().tryLock(position, size, shared);
                if (lock == null) {
                    return true;
                }
            } catch (RuntimeException ex) {
                throw new IOException(ex);
            }
            return false;
        } catch (IOException ex) {
            return true;
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException ex) {
                throw new BuildException("Cannot close " + file, ex);
            }
        }
        
    }
    
    
}
