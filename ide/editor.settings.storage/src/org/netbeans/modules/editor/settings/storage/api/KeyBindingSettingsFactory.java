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

package org.netbeans.modules.editor.settings.storage.api;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.editor.settings.MultiKeyBinding;


/**
 * Getters and setters for keymap editor profiles. Instances of this
 * class should be registerred in <code>MimeLookup</code> for particular mime types.
 *
 * @author Jan Jancura
 */
public abstract class KeyBindingSettingsFactory {

    /**
     * Gets the keybindings list, where items are instances of
     * {@link org.netbeans.api.editor.settings.MultiKeyBinding}.
     *
     * @return List of <code>MultiKeyBinding</code>s.
     */
    public abstract List<MultiKeyBinding> getKeyBindings ();
    
    /**
     * Gets the keybindings list for given keymap name, where items 
     * are instances of {@link org.netbeans.api.editor.settings.MultiKeyBinding}.
     *
     * @param profile a name of keymap
     * 
     * @return List of <code>MultiKeyBinding</code>s.
     */
    public abstract List<MultiKeyBinding> getKeyBindings (String profile);

    
    /**
     * Returns default keybindings list for given keymap name, where items 
     * are instances of {@link org.netbeans.api.editor.settings.MultiKeyBinding}.
     *
     * @return List of <code>MultiKeyBinding</code>s.
     */
    public abstract List<MultiKeyBinding> getKeyBindingDefaults (String profile);
    
    /**
     * Gets the keybindings list, where items are instances of 
     * {@link org.netbeans.api.editor.settings.MultiKeyBinding}.
     *
     * @param profile
     * @param keyBindings the list of <code>MultiKeyBindings</code>
     */
    public abstract void setKeyBindings (
        String profile, 
        List<MultiKeyBinding> keyBindings
    );
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be registered
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be unregistered
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
}
