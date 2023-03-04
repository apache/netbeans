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

package org.netbeans.swing.tabcontrol;

import java.awt.Component;

/**
 * Interface that provides external information (provided by window system)
 * that TabbedContainers need to know in order to work fully.<p>
 *
 * Tab control uses info to provide for example tab buttons reacting on 
 * the position of the container or on maximization state.
 *
 * @see TabbedContainer#TabbedContainer
 *
 * @author Dafe Simonek
 */
public interface WinsysInfoForTabbed {

    /** Returns global orientation of given component.
     *
     * @return Orientation of component, as defined in
     * TabDisplayer.ORIENTATION_XXX constants
     */
    public Object getOrientation (Component comp);
    
    /** Finds out in what state is window system mode containing given component.
     * 
     * @return true if given component is inside mode which is in maximized state,
     * false otherwise 
     */
    public boolean inMaximizedMode (Component comp);
}
