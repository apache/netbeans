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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
class ThisVariable extends AbstractObjectVariable implements This {

    private int cloneNumber = 1;

    ThisVariable (
        JPDADebuggerImpl debugger,
        Value value,
        String parentID
    ) {
        super (
            debugger,
            value,
            parentID + ".this^"
        );
    }


    // This impl................................................................

    @Override
    public ThisVariable clone() {
        return new ThisVariable(getDebugger(), getJDIValue(),
                getID().substring(0, getID().length() - ".this^".length()) + "_clone" + (cloneNumber++));
    }

    // other methods ...........................................................
    
    @Override
    public String toString () {
        return "ThisVariable this";
    }
}
