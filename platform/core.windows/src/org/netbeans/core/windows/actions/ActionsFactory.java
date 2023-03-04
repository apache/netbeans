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
package org.netbeans.core.windows.actions;

import javax.swing.Action;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

/**
 * When TopComponent's or Mode's popup menu is being constructed then the default
 * Lookup is searched for instances of this factory. When found the factory
 * instances are asked to amend the list of actions of TopComponent/Mode popup menu.
 * 
 * @author S. Aubrecht
 * @since 2.30
 */
public abstract class ActionsFactory {
   
    /**
     * Adjust the list of actions for TopComponent's popup menu.
     * @param tc 
     * @param defaultActions
     * @return 
     */
    public abstract Action[] createPopupActions( TopComponent tc, Action[] defaultActions );
    
    /**
     * Adjust the list of actions for Mode's popup menu (when user right-clicks
     * into Mode's header but not into a particular TopComponent tab).
     * @param mode
     * @param defaultActions
     * @return 
     */
    public abstract Action[] createPopupActions( Mode mode, Action[] defaultActions );
}
