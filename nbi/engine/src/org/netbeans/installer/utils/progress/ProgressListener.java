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

package org.netbeans.installer.utils.progress;

/**
 * The interface which needs to be implemented by classes which need to listen to
 * a progress' state change events. 
 * 
 * <p>
 * They will need to register themselves with the progress object by either 
 * constructing the progress with a specialized constructor or calling the 
 * {@link Progress#addProgressListener(ProgressListener)} method.
 * 
 * @see Progress#Progress(ProgressListener)
 * @see Progress#addProgressListener(ProgressListener)
 * 
 * @author Kirill Sorokin
 * 
 * @since 1.0
 */
public interface ProgressListener {
    /**
     * This method will be called when a {@link Progress} being listened changes 
     * its state.
     * 
     * <p>
     * The actual {@link Progress} which has changed will be passed in as the only 
     * parameter. A progress' state is considered changed when any of its core 
     * properties (<code>title</code>, <code>detail</code>, <code>percentage</code>, 
     * <code>canceled</code>) change.
     * 
     * @param progress The {@link Progress} whose state has changed. 
     */
    void progressUpdated(final Progress progress);
}
