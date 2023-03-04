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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import threaddemo.locking.RWLock;

/**
 * Really, a file.
 * Mutator methods (rename, delete, create*Phadhail) cannot be called
 * from within a listener callback, or generally without read access.
 * You *can* add/remove listeners from within a listener callback however,
 * or in fact at any other time (without even a lock).
 * Methods from java.lang.Object (toString, hashCode, equals) can be called at any time.
 * @author Jesse Glick
 */
public interface Phadhail {
    
    /** will be simple file name */
    String getName();
    
    /** will be full path */
    String getPath();
    
    /** rename (within parent) */
    void rename(String nue) throws IOException;
    
    /** will be true if a directory */
    boolean hasChildren();
    
    /**
     * Get a list of child files.
     * caller cannot mutate list, and it might not be thread-safe
     * implementor cannot change list after creation (i.e. size & identity of elements)
     * it is expected that once the list is obtained, asking for elements is fast and nonblocking
     * (and then the read lock is not required)
     */
    List<Phadhail> getChildren();
    
    /** delete this phadhail (must not have children) */
    void delete() throws IOException;
    
    /** make a new phadhail without children */
    Phadhail createLeafPhadhail(String name) throws IOException;
    
    /** make a new phadhail with children */
    Phadhail createContainerPhadhail(String name) throws IOException;
    
    /** read */
    InputStream getInputStream() throws IOException;
    
    /** write (note: in this simple model, no locks here) */
    OutputStream getOutputStream() throws IOException;
    
    /** add a listener */
    void addPhadhailListener(PhadhailListener l);
    
    /** remove a listener */
    void removePhadhailListener(PhadhailListener l);
    
    /**
     * Get a lock appropriate for locking operations from another thread.
     * Should be a single lock for a whole tree of phadhails.
     * Model methods should automatically acquire the relevant lock for you;
     * the view need not bother, unless it needs to do an atomic operation.
     */
    RWLock lock();
    
}
