/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.spi.debugger;

import java.util.HashSet;
import java.util.Vector;


/**
 * Support for {@link ActionsProvider} implementation. You should implement
 * {@link #doAction} and {@link #getActions} only, and call {@link #setEnabled}
 * when the action state is changed.
 *
 * @author   Jan Jancura
 */
public abstract class ActionsProviderSupport extends ActionsProvider {

    private HashSet enabled = new HashSet ();
    private Vector listeners = new Vector ();


    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public abstract void doAction (Object action);
    
    /**
     * Returns a state of given action defined by {@link #setEnabled} 
     * method call.
     *
     * Do not override. Should be final - the enabled state is cached,
     * therefore this method is not consulted unless the state change
     * is fired.
     *
     * @param action action
     */
    public boolean isEnabled (Object action) {
        return enabled.contains (action);
    }
    
    /**
     * Sets state of enabled property.
     *
     * @param action action whose state should be changed
     * @param enabled the new state
     */
    protected final void setEnabled (Object action, boolean enabled) {
        boolean fire = false;
        if (enabled)
            fire = this.enabled.add (action);
        else
            fire = this.enabled.remove (action);
        if (fire)
            fireActionStateChanged (action, enabled);
    }
    
    /**
     * Fires a change of action state.
     *
     * @param action action whose state has been changed
     * @param enabled the new state
     */
    protected void fireActionStateChanged (Object action, boolean enabled) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ActionsProviderListener) v.elementAt (i)).actionStateChange (
                action, enabled
            );
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public final void addActionsProviderListener (ActionsProviderListener l) {
        listeners.addElement (l);
    }
    
    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public final void removeActionsProviderListener (ActionsProviderListener l) {
        listeners.removeElement (l);
    }
}

