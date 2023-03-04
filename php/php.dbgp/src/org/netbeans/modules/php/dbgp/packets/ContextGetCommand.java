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

import org.netbeans.modules.php.dbgp.packets.ContextNamesResponse.Context;

/**
 * @author ads
 *
 */
public class ContextGetCommand extends DbgpCommand {
    static final String CONTEXT_GET = "context_get"; // NOI18N
    private static final String CONTEXT_ARG = "-c "; // NOI18N
    private int myDepth;
    private Context myContext;

    public ContextGetCommand(String transactionId) {
        super(CONTEXT_GET, transactionId);
        myDepth = -1;
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

    public void setDepth(int depth) {
        myDepth = depth;
    }

    @Override
    protected String getArguments() {
        StringBuilder builder = new StringBuilder();
        if (myDepth > -1) {
            builder.append(ContextNamesCommand.DEPTH_ARG);
            builder.append(myDepth);
        }

        if (myContext != null) {
            if (builder.length() != 0) {
                builder.append(BrkpntSetCommand.SPACE);
            }
            builder.append(CONTEXT_ARG);
            builder.append(myContext.getId());
        }

        return builder.toString();
    }

    public void setContext(Context context) {
        myContext = context;
    }

    public Context getContext() {
        return myContext;
    }

}
