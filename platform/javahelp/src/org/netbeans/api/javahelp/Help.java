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

package org.netbeans.api.javahelp;

import javax.swing.event.ChangeListener;


import org.openide.util.HelpCtx;

/** An implementation of the JavaHelp system.
* Get the concrete instance using lookup.
* @author Jaroslav Tulach, Jesse Glick
*/
public abstract class Help {

    /** constructor for subclasses
     */
    protected Help() {}

    /** Test whether a given ID is valid in some known helpset.
     * In lazy mode, should be a fast operation; if in doubt say you do not know.
     * @param id the ID to check for validity
     * @param force if false, do not do too much work (be lazy) and if necessary return null;
     *              if true, must return non-null (meaning the call may block loading helpsets)
     * @return whether it is valid, if this is known; else may be null (only permitted when force is false)
     */
    public abstract Boolean isValidID(String id, boolean force);
    
    /** Shows help.
     * <p>Note that for basic usage it may suffice to call {@link HelpCtx#display},
     * avoiding any direct dependency on this module.
     * @param ctx help context
     */
    public void showHelp(HelpCtx ctx) {
        showHelp(ctx, /* #15711 */true);
    }

    /** Shows help.
     * @param ctx help context
     * @param showmaster whether to force the master helpset
     * to be shown (full navigators) even
     * though the supplied ID only applies
     * to one subhelpset
     */
    public abstract void showHelp(HelpCtx ctx, boolean showmaster);

    /** Add a change listener for when help sets change.
     * @param l the listener to add
     */
    public abstract void addChangeListener(ChangeListener l);
    
    /** Remove a change listener.
     * @param l the listener to remove
     */
    public abstract void removeChangeListener(ChangeListener l);

}
