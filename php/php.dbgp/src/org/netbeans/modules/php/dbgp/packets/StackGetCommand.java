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
package org.netbeans.modules.php.dbgp.packets;

/**
 * @author ads
 *
 */
public class StackGetCommand extends DbgpCommand {
    static final String STACK_GET = "stack_get"; // NOI18N
    private int myDepth;

    public StackGetCommand(String transactionId) {
        super(STACK_GET, transactionId);
        myDepth = -1;
    }

    public void setDepth(int depth) {
        myDepth = depth;
    }

    @Override
    protected String getArguments() {
        if (myDepth > 0) {
            return ContextNamesCommand.DEPTH_ARG + myDepth;
        }
        return super.getArguments();
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

}
