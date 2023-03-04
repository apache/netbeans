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
package org.netbeans.modules.xml.wsdl.model.extensions.soap12.validation;

import org.netbeans.modules.xml.wsdl.model.Message;

public class MessagePart {

    private final String messageName;
    private final String partName;
    private final int hashCode;

    public MessagePart(Message message, String partName) {
        if (message == null) {
            throw new NullPointerException("null messageName");
        }
        if (partName == null) {
            throw new NullPointerException("null partName");
        }
        if ("".equals(partName)) {
            throw new IllegalArgumentException("blank partName");
        }

        messageName = message.getName();
        this.partName = partName;
        hashCode = messageName.toString().concat(partName).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof MessagePart)) {
            return false;
        }

        MessagePart that = (MessagePart) other;
        if (!messageName.equals(that.messageName)) {
            return false;
        }

        return partName.equals(that.partName);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
