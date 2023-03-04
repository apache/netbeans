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

package org.netbeans.modules.debugger.importd;

import org.netbeans.modules.debugger.*;
import org.openide.modules.ModuleInstall;

/**
* Module installation class for ImportDebugger Module
*
* @author Jan Jancura
*/
public class ImportDebuggerModule extends ModuleInstall {

    static final long serialVersionUID = -2272025566936120988L;

    private static ImportDebuggerImpl idi;

    /** Module installed for the first time. */
    public void installed () {
        restored ();
    }

    /** Module installed again. */
    public void restored () {
        try {
            Register.registerDebuggerImpl (
                idi = new ImportDebuggerImpl ()
            );
        } catch (Exception e) {
        }
    }

    /** Module was uninstalled. */
    public void uninstalled () {
        try {
            Register.unregisterDebuggerImpl (
                idi
            );
            idi = null;
        } catch (RuntimeException e) {
        }
    }
    
    /**
     * Return type of debugger which should be used to debug this DebuggerInfo.
     *
     * @return type of debugger which should be used to debug this DebuggerInfo
     */
    public static DebuggerImpl getDebuggerImpl () {
        return idi;
    }
}
