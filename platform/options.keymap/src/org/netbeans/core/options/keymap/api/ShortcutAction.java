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


package org.netbeans.core.options.keymap.api;


/**
 * A special object for representing the action to which the shortcut
 * is bound. As we are able to represent different kinds of actions (
 * (e.g. represented by javax.swing.Action or javax.swing.text.TextAction)
 * the instances of this interface wrap the original action and provide
 * methods contained in this interface.
 * 
 * @author David Strupl
 */
public interface ShortcutAction {

    /**
     * The display name is what the user can see when the shortcut
     * is displayed in the configuration dialogs.
     * @return the display name, or {@code ""} if this action should not be displayed
     */
    public String getDisplayName ();
    
    /**
     * The ID of the shortcut action. It is the action class name or some
     * other unique identification of the action ("cut-to-clipboard" or
     * "org.openide.actions.CutAction").
     * @return 
     */
    public String getId ();
    
    /**
     * If the same action is supplied by more KeymapManagers they can "know"
     * about each other. If the action "knows" what the ID of the action
     * is coming from the other provider it can supply it by returning a non-null
     * value from this method. An example: actions coming from the editor
     * can supply the class name of the corresponding openide action, e.g.
     * org.openide.actions.CutAction.
     * @return 
     */
    public String getDelegatingActionId ();
    
    /**
     * If the action is "compound" action (delegating to different actions
     * for different keymapManagers) this method returns the instance registered
     * in the given manager. If the action is not composed of more actions
     * this method should simply return <code>this</code>.
     * 
     * @param keymapManagerName 
     * @return 
     */
    public ShortcutAction getKeymapManagerInstance(String keymapManagerName);
}

