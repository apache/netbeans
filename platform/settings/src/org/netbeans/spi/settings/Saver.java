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

package org.netbeans.spi.settings;

/** The Saver should be used as a callback to the framework implementation
 * to handle setting object changes.
 *
 * @author  Jan Pokorsky
 */
public interface Saver {
    /** Notify the framework to be aware of the setting object is changed.
     */
    public void markDirty();

    /** Notify the framework the setting object is changed and can be written down
     * @exception java.io.IOException if the save cannot be performed
     */
    public void requestSave() throws java.io.IOException;
}
