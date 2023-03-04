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

package threaddemo.apps.index;

import java.util.Map;
import javax.swing.event.ChangeListener;
import threaddemo.locking.RWLock;
import threaddemo.model.Phadhail;

/**
 * Maintains a read-only index of elements in XML phadhail documents.
 * @author Jesse Glick
 */
interface Index {

    /**
     * Get the root of the tree being searched.
     */
    Phadhail getRoot();

    /**
     * Begin parsing, if it has not already been begun.
     */
    void start();

    /**
     * Stop any ongoing processing if there was any.
     * Results will not generally be valid after this.
     */
    void cancel();
    
    /**
     * Get the index data.
     * Keys are XML element names.
     * Values are occurrence counts.
     * <p>Must be called with lock held, and result may only be accessed with it held.
     */
    Map<String,Integer> getData();
    
    /**
     * Add a listener to changes in the data.
     */
    void addChangeListener(ChangeListener l);
    
    /**
     * Remove a listener.
     */
    void removeChangeListener(ChangeListener l);
    
    /**
     * Associated lock.
     */
    RWLock getLock();
    
}
