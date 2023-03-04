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

package org.netbeans.modules.autoupdate.services;

import org.netbeans.api.autoupdate.*;
import java.util.Set;
import org.openide.modules.Dependency;

/** Callback interface which interacts with user when needs user's input.
 *
 * @author Jiri Rechtacek (jrechtacek@netbeans.org)
 */
public abstract class UpdateProblemHandler {
    
    /** Notifies an user that some dependency seems broken and allows
     * to decide if can ignore it or not.
     * 
     * @param dependency a dependency what seems as broken
     * @return true if can ignore it
     */
    public boolean ignoreBrokenDependency (Dependency dependency) {
        return true;
    }
    
    /** Notifies an user that some <code>UpdateElement</code> should be added
     * among elements what is being performed.
     * An user can decide if elements should be added or not.
     * 
     * @param elements list of <code>UpdateElement</code> what are required
     * @return true if an user agree to add them
     */
    public boolean addRequiredElements (Set<UpdateElement> elements) {
        return true;
    }
    
    /** Notifies an user the <code>UpdateElement</code> which is being performed
     * is not trusted but has a suspect state.
     * 
     * @param state suspect state
     * @param element <code>UpdateElement</code> already performed
     * @return true if allow untrusted element
     */
    public abstract boolean allowUntrustedUpdateElement (String state, UpdateElement element);
    
    /** Ask an user for approve the license agreement of handing <code>UpdateElement</code>.
     * 
     * @param license content of license
     * @return true if an user accpets the license agreement
     */
    public abstract boolean approveLicenseAgreement (String license);
    
    /** Sometimes completion of the proceeding operation needs restart of IDE. In this
     * case an user should decide if the IDE will restart now or later.
     *
     * 
     * @return true if IDE can be restart now
     */
    public boolean restartNow () {
        return true;
    }
    
}
