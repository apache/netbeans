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
package org.netbeans.modules.cnd.debugger.gdb2;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerCommand;

/**
 *
 */
public class GdbHandlerCommand extends HandlerCommand {
    public enum Type {CHANGE, REPLACE};
    
    private final Type type;
    private GdbHandlerCommand next = null;
    
    GdbHandlerCommand(Type type, String changeCommand) {
        super(changeCommand, false);
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }

    public GdbHandlerCommand getNext() {
        return next;
    }

    public void setNext(GdbHandlerCommand next) {
        this.next = next;
    }
    
    void onDone(){};
}
