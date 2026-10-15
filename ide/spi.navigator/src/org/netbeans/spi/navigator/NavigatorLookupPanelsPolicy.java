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

package org.netbeans.spi.navigator;

/** Interface for SPI clients who want to affect searching mechanism for
 * available NavigatorPanel implementations.<p>
 * 
 * Navigator infrastructure searches for instance of this interface in
 * {@link org.openide.util.Utilities#actionsGlobalContext() Utilities.actionsGlobalContext()}
 * lookup and then applies found policy on set of available 
 * {@link org.netbeans.spi.navigator.NavigatorPanel NavigatorPanel}
 * implementations.<p>
 * 
 * Note that multiple instances of this interface are not supported in
 * Utilities.actionsGlobalContext() lookup, one instance is chosen randomly
 * in this case.<p>
 * 
 * Common Usage: 
 *  <ul>
 *      <li>Implement this interface, return kind of policy that suits you from
 *          <code>getPanelsPolicy()</code> method.</li>
 *      <li>Put implementation instance into your TopComponent's subclass lookup,
 *          see {@link org.openide.windows.TopComponent#getLookup()  TopComponent.getLookup()}
 *          for details.</li>
 *      <li>Now when your TopComponent becomes active in the system, found
 *          panels policy is used to limit/affect set of available NavigatorPanel
 *          implementations.</li>
 *  </ul>
 * 
 * @since 1.6
 *
 * @author Dafe Simonek
 */
public interface NavigatorLookupPanelsPolicy {
    
    /** Shows only NavigatorPanel implementations available through
     * {@link org.netbeans.spi.navigator.NavigatorLookupHint NavigatorLookupHint }
     * in Navigator window, hides NavigatorPanels
     * available from DataObject of active Node.<br>
     * 
     * Use when you want to remove NavigatorPanels of active Node from Navigator
     * window. 
     */
    public static final int LOOKUP_HINTS_ONLY = 1;
    
    /** Returns policy for available Navigator panels. Currently only 
     * LOOKUP_HINTS_ONLY policy is supported.
     * 
     * @return Navigator panels policy constant.
     */
    public int getPanelsPolicy ();
    
}
