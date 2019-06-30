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
package org.netbeans.modules.payara.tooling.admin.response;

import java.util.List;
import java.util.Properties;

/**
 * Class represents one part of REST server message.
 * <p>
 * This part can be repeated in server response.
 * It includes string message and can have other properties.
 * It can be nesting also other message parts.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class MessagePart {

    /** Message properties.*/
    Properties props;

    /** Message.*/
    String message;

    /** Nested messages.*/
    List<MessagePart> children;

    public List<MessagePart> getChildren() {
        return children;
    }

    public String getMessage() {
        return message;
    }

    public Properties getProperties() {
        return props;
    }

    public void setProperties(Properties props) {
        this.props = props;
    }

    void setMessage(String message) {
        this.message = message;
    }

}
