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
public class Error extends BaseMessageChildElement {
    private static final String CODE = "code"; // NOI18N
    private static final String MESSAGE = "message"; // NOI18N

    Error(Node node) {
        super(node);
    }

    public int getErrorCode() {
        return getInt(CODE);
    }

    public String getMessage() {
        Node node = getChild(MESSAGE);
        if (node == null) {
            return null;
        }
        return DbgpMessage.getNodeValue(node);
    }

}
