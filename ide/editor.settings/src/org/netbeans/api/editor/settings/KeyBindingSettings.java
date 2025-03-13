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

package org.netbeans.api.editor.settings;

/**
 * The list of available key bindings. Each key binding is represented by an
 * instance of the <code>MultiKeyBinding</code> class, which associates one
 * or more keyboard shortcuts with an <code>Action</code>.
 * 
 * <p>Instances of this class should be retrieved from <code>MimeLookup</code>.
 * 
 * <p><span style="color:red">This class must NOT be extended by any API clients.</span>
 *
 * @author Martin Roskanin
 */
public abstract class KeyBindingSettings {

    /**
     * Construction prohibited for API clients.
     */
    public KeyBindingSettings() {
        // Control instantiation of the allowed subclass only
        if (!getClass().getName().startsWith("org.netbeans.modules.editor.settings.storage")) { // NOI18N
            throw new IllegalStateException("Instantiation prohibited. " + getClass().getName()); // NOI18N
        }
    }
    
    /**
     * Gets the keybindings list, where items are instances of {@link MultiKeyBinding}
     *
     * @return List of {@link MultiKeyBinding}
     */
    public abstract java.util.List<MultiKeyBinding> getKeyBindings();

}
