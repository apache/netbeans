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

import com.sun.jdi.ClassType;
import com.sun.jdi.ObjectReference;

import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.openide.util.Exceptions;


/**
 * @author   Jan Jancura
 */
class SuperVariable extends AbstractObjectVariable implements Super {

    // init ....................................................................
    private ClassType classType;

    SuperVariable (
        JPDADebuggerImpl debugger,
        ObjectReference value,
        ClassType classType,
        String parentID
    ) {
        super (
            debugger, 
            value,
            classType,
            parentID + ".super^"
        );
        this.classType = classType;
    }

    
    // Super impl ..............................................................
    
    public SuperVariable clone() {
        return new SuperVariable(getDebugger(), (ObjectReference) getJDIValue(), classType,
                getID().substring(0, getID().length() - ".super^".length()));
    }
    
    // other methods ...........................................................
        
    public String toString () {
        return "SuperVariable " + getType();
    }
    
}
