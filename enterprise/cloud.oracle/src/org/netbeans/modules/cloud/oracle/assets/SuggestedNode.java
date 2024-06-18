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
package org.netbeans.modules.cloud.oracle.assets;

import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.nodes.Children;

/**
 *
 * @author Jan Horvath
 */
public class SuggestedNode extends OCINode {
    
    private static final String SUGGEST_ICON = "org/netbeans/modules/cloud/oracle/resources/suggest.svg"; // NOI18N
    
    public SuggestedNode(OCIItem item) {
        super(item, Children.LEAF);
        setName(item.getName()); 
        setDisplayName(item.getName());
        setIconBaseWithExtension(SUGGEST_ICON);
        setShortDescription(item.getDescription());
    }
    
     public static NodeProvider<OCIItem> createNode() {
        return SuggestedNode::new;
    }
    
}
