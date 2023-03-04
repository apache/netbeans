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

import java.util.List;

import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
abstract class BaseMessageChildElement {
    private Node myNode;

    BaseMessageChildElement(Node node) {
        myNode = node;
    }

    protected Node getNode() {
        return myNode;
    }

    protected Node getChild(String nodeName) {
        return DbgpMessage.getChild(getNode(), nodeName);
    }

    protected List<Node> getChildren(String nodeName) {
        return DbgpMessage.getChildren(getNode(), nodeName);
    }

    protected String getAttribute(String attrName) {
        return DbgpMessage.getAttribute(getNode(), attrName);
    }

    protected int getInt(String attrName) {
        String number = getAttribute(attrName);
        if (number == null) {
            return 0;
        }
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            assert false;
            return -1;
        }
    }

}
