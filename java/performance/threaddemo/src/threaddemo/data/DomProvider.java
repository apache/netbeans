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

package threaddemo.data;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.w3c.dom.Document;
import threaddemo.locking.RWLock;

/**
 * Cookie for an object with a DOM tree.
 * @author Jesse Glick
 */
public interface DomProvider {

    /**
     * Prepare for parsing. If the DOM tree is not already
     * available, parsing will be initiated. To receive notification
     * of completion, attach a listener.
     */
    void start();

    /**
     * Get the parsed document (blocking as needed).
     * @throws IOException if it cannot be read or parsed
     */
    Document getDocument() throws IOException;
    
    /**
     * Set the parsed document.
     * @throws IOException if it cannot be written
     */
    void setDocument(Document d) throws IOException;
    
    /**
     * True if the parse is finished and OK (does not block except for lock).
     */
    boolean isReady();
    
    /**
     * Listen for changes in status.
     */
    void addChangeListener(ChangeListener l);

    /**
     * Stop listening for changes in status.
     */
    void removeChangeListener(ChangeListener l);
    
    /**
     * Lock on which to lock while doing things.
     */
    RWLock lock();
    
    /**
     * Do an isolated block of operations to the document (must be in the write lock).
     * During this block you may not call any other methods of this interface which
     * require the lock (in read or write mode), or this method itself; you may
     * only adjust the document using DOM mutations.
     * Changes will be fired, and any underlying storage recreated, only when the
     * block is finished (possibly with an error). Does not roll back partial blocks.
     */
    void isolatingChange(Runnable r);

}
