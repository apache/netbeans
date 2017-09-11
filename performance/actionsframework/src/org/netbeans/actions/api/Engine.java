/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
