/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.actions;

import java.io.IOException;
import org.netbeans.modules.openide.awt.SavableRegistry;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.util.Lookup;

/** Context interface that represents ability to persist its state to long term storage. To get best
 * interaction with the system, it is preferable to use {@link AbstractSavable}
 * to create instances of this interface rather than implementing it 
 * directly.
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 * @since 7.33
 */
public interface Savable {
    /** Global registry of all {@link Savable}s that are modified in the
     * application and subject to save by <em>Save All</em> action. See 
     * {@link AbstractSavable} for description how to register your own
     * implementation into the registry.
     */
    public static final Lookup REGISTRY = SavableRegistry.getRegistry();
    
    /** Invoke the save operation.
     * @throws IOException if the object could not be saved
     */
    public void save() throws IOException;

    /** Human descriptive, localized name of the savable. It is advised that
     * all implementations of Savable override the toString method to provide
     * human readable name.
     * 
     * @return human readable name representing the savable
     */
    @Override
    public String toString();
}
