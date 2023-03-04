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

package org.netbeans.modules.csl.navigation.base;

/**
 * This file is originally from Retouche, the Java Support
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * Interface for navigator models to notify clients about their not-ready
 * state, during long computations.
 *
 * Note, this is temporary and will be deleted and replaced by simpler JComponent
 * navigator based API.
 *
 * @author Dafe Simonek
 */
public interface ModelBusyListener {

    /** Computation started.
     * Threading: Can be called on any thread
     */
    public void busyStart ();

    /** Computation finished.
     * Threading: Can be called on any thread
     */
    public void busyEnd ();

    /** Called when new content was loaded and is ready. It means that 
     * list data change events was already fired and so the Swing component
     * which contains the model already knows about new data.
     * Currently used only to keep selection in swing components after 
     * load of new data.
     *
     * Threading: Always called from EQT 
     */ 
    public void newContentReady ();
    
}
