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

package org.netbeans.modules.xml.spi.dom;

import java.util.List;

import org.w3c.dom.*;

/**
 * A simple implementation of NodeList wrapping Java <code>List</code>.
 *
 * @author  Petr Kuzel
 */
public final class NodeListImpl implements NodeList {

    public static final NodeList EMPTY = new NodeList() {
        public int getLength() { return 0; }
        public org.w3c.dom.Node item(int i) { return null; }
        public String toString() { return "NodeListImpl.EMPTY"; }
    };

    private final List peer;

    /**
     * Creates new NodeListImpl */
    public NodeListImpl(List l) {
        peer = l;
    }

    public int getLength() {
        return peer.size();
    }
    
    public org.w3c.dom.Node item(int i) {
        return (Node) peer.get(i);
    }
      
    public String toString() {
        return peer.toString();
    }
}
