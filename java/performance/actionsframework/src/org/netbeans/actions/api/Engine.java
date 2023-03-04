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
/*
 * Engine.java
 *
 * Created on January 24, 2004, 1:31 AM
 */

package org.netbeans.actions.api;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.text.Keymap;

/** The Engine is responsible for the relationship between the user context
 * (selected object, window, whatever can influence action presence or
 * enablement).  By design it does not use a &quot;push&quot; model, but
 * rather a &quot;pull&quot; model.  That is, it, and only it, manages the
 * state of the action presenters in the system.  It is responsible for
 * deciding when something needs to be displayed/painted/updated.  The
 * system can provide hints that now would be a good time to update something,
 * but it will decide what to do.
 * <p>
 * For toolbar enablement, the intention is to implement handling enablement
 * issues using a polling model - rather than trying to push updates for
 * every change in context (lots of work and most of the time nothing changes),
 * it will perform such updates in a timely way, but based on its analysis of
 * the current circumstances. This may be influenced by such things as requests
 * for visibility on its presenters, idle time in the AWT thread, a timer or
 * the phase of the moon.  In practice the visual result should be no different
 * than that of a push model, except that it does far less work.
 * <p>
 * Note the complete absence of use of the listener pattern - this is by design.
 *
 * @author  Tim Boudreau
 */
public abstract class Engine {
    /** Sets the ContextProvider that the engine will ask for the current
     * user context (selected object, etc.) when it deems it necessary to
     * update or instantiate presenter components */
    public abstract void setContextProvider (ContextProvider ctx);
    
    /** Method by which the infrastructure can suggest that now is a good time
     * to update the state of toolbars, etc.  Calling this method amounts to
     * making a suggestion - like calling System.gc(), there is no guarantee
     * that an implementation will immediately update its presenters.
     */
    public abstract void recommendUpdate();
    
    public abstract JMenuBar createMenuBar();
    
    public abstract JToolBar[] createToolbars();
        
    
    /** Create an input map.  Will return an instance of ComponentInputMap
     * (needed so the resulting input map can be used
     * as a master input map for an application, i.e. set against 
     * WHEN_IN_FOCUSED_WINDOW). */
    public abstract InputMap createInputMap(JComponent jc);
    
    public abstract ActionMap createActionMap();
    
    public abstract JPopupMenu createPopupMenu();

}
