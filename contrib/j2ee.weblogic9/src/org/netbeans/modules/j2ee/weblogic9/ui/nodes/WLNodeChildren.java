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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children.Keys;
import org.openide.nodes.Node;
import org.openide.nodes.Children.Keys;


/**
 * @author ads
 *
 */
class WLNodeChildren<T> extends Keys<T> {

    WLNodeChildren() {
        super();
    }

    @Override
    protected void addNotify() {
    }
    
    @Override
    protected void removeNotify() {
    }
    
    @Override
    protected org.openide.nodes.Node[] createNodes(T key) {
        if (key instanceof AbstractNode) {
            return new Node[] {(AbstractNode) key};
        }
        
        return null;
    }

}
