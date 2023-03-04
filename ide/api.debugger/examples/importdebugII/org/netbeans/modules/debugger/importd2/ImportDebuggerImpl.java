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

package org.netbeans.modules.debugger.importd2;

import org.openide.debugger.Debugger;

import org.netbeans.modules.debugger.*;


/**
 * This class represents JPDA Debugger Implementation.
 *
 * @author Jan Jancura
 */
public class ImportDebuggerImpl extends DebuggerImpl {

    static ImportDebugger impl;


    /**
     * Returns displayable name of JPDA debugger.
     *
     * @return displayable name of JPDA debugger
     */
    public  String getDisplayName () {
        return ImportDebugger.getLocString ("CTL_Import_Debugger");
    }

    /**
     * Returns a new instance of Debugger.
     */
    public AbstractDebugger createDebugger () {
        if (impl == null) impl = new ImportDebugger ();
        return new org.netbeans.modules.debugger.support.DelegatingDebugger (impl);
    }
}

