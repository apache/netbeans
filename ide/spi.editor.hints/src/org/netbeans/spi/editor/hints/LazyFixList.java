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

package org.netbeans.spi.editor.hints;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * A list of fixes that allows lazy computation of the fixes for an error.
 *
 * @author Jan Lahoda
 */
public interface LazyFixList {

    /**
     * PropertyChangeEvent with this name is fired when the list
     * of the fixes is changed.
     */
    public static final String PROP_FIXES = "fixes";

    /**
     * PropertyChangeEvent with this name is fired when the list
     * of the fixes is computed - no more changes are to be expeted
     * after this.
     */
    public static final String PROP_COMPUTED = "computed";

    /**
     * The registered PropertyChangeListener will recieve events
     * with names PROP_COMPUTED and PROP_FIXES.
     * @param l the listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Allows to unregister a PropertyChangeListener.
     * @param l the listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Should return false if there will not be any fixes in the list for sure.
     * Should return true otherwise.
     * Should run very fast - should not try to actualy compute the fixes.
     * 
     * @return false if this list will never contain any fixes, true otherwise.
     */
    public boolean probablyContainsFixes();

    /**
     * Getter for the current list of fixes.
     * @return the list of fixes
     */
    public List<Fix> getFixes();
    
    /**
     * Returns true if the list of fixes will not changed anymore (it is computed).
     *
     * @return true if the list of fixes is computed.
     */
    public boolean isComputed();
    
}
