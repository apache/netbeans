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

import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public abstract class DbgpResponse extends DbgpMessage {
    static final String TRANSACTION_ID = "transaction_id"; // NOI18N
    static final String COMMAND = "command"; // NOI18N
    private static final String ERROR = "error "; // NOI18N

    DbgpResponse(Node node) {
        super(node);
    }

    public String getTransactionId() {
        return getAttribute(getNode(), TRANSACTION_ID);
    }

    public String getCommandName() {
        return getAttribute(getNode(), COMMAND);
    }

    public Error getError() {
        Node error = getChild(getNode(), ERROR);
        if (error == null) {
            return null;
        }
        return new Error(error);
    }

}
