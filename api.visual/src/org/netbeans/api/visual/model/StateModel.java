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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.visual.model;

import java.util.ArrayList;

/**
 * This class represents a model with a single state which can be boolean or non-negative integer value.
 * It supports stateChanged event listening.
 *
 * @author David Kaspar
 */
public final class StateModel {

    private final ArrayList<Listener> listeners = new ArrayList<Listener> ();
    private int state;
    private int maxStates;

    /**
     * Creates a boolean (2-states) model.
     */
    public StateModel () {
        this (2);
    }

    /**
     * Creates a model with a specified maximal states.
     * @param maxStates the maximal states
     */
    public StateModel (int maxStates) {
        assert maxStates > 0;
        this.maxStates = maxStates;
        state = 0;
    }

    private void fireChanged () {
        Listener[] ls;
        synchronized (listeners) {
            ls = this.listeners.toArray (new Listener[this.listeners.size ()]);
        }
        for (Listener listener : ls)
            listener.stateChanged ();
    }

    /**
     * Returns a boolean representation of a state.
     * @return the boolean value (true for non-zero value, false for zero value)
     */
    public boolean getBooleanState () {
        return state > 0;
    }

    /**
     * Sets a boolean state.
     * @param state the boolean state; if true, then 1 value is used; if false, then 0 value is used
     */
    public void setBooleanState (boolean state) {
        setState (state ? 1 : 0);
    }

    /**
     * Toggles boolean state.
     */
    public void toggleBooleanState () {
        setState (state > 0 ? 0 : 1);
    }

    /**
     * Returns a integer representation of the state.
     * @return the integer value of the state.
     */
    public int getState () {
        return state;
    }

    /**
     * Decreases the state value. If new value would have to be negative, then the maxStates-1 value is used instead.
     */
    public void decrease () {
        if (-- state < 0)
            state = maxStates - 1;
        fireChanged ();
    }

    /**
     * Increases the state value. If new value would have to be maxStates or higher, then the 0 value is used instead.
     */
    public void increase () {
        if (++ state >= maxStates)
            state = 0;
        fireChanged ();
    }

    /**
     * Sets a new state value.
     * @param state the new state value
     */
    public void setState (int state) {
        this.state = state;
        fireChanged ();
    }

    /**
     * Returns a maximal states.
     * @return the maximal states; 2 for boolean model
     */
    public int getMaxStates () {
        return maxStates;
    }

    /**
     * Adds a listener of a state value changed.
     * @param listener the listener
     */
    public void addListener (Listener listener) {
        synchronized (listeners) {
            listeners.add (listener);
        }
    }

    /**
     * Removes a listener of a state value changed.
     * @param listener the listener
     */
    public void removeListener (Listener listener) {
        synchronized (listeners) {
            listeners.remove (listener);
        }
    }

    /**
     * The listener for listening on state changed event on a state model.
     */
    public interface Listener {

        /**
         * Called when a state value is changed.
         */
        void stateChanged ();

    }

}
