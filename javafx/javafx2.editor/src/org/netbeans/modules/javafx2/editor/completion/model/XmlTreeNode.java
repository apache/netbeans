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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.Enumeration;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;

/**
 *
 * @author sdedic
 */
@Deprecated
public interface XmlTreeNode {
    /**
     * @return True, if the node is an attribute.
     */
    public boolean isAttribute();
    
    /**
     * Provides attribute nodes. Returns null for attributes, empty list
     * for Elements without attributes.
     * @return 
     */
    @NullUnknown
    public List<FxNode> getAttributes();
    
    /**
     * Provides child nodes. Returns {@code null} for attributes, empty
     * List for elements without children.
     * 
     * @return 
     */
    @NullUnknown
    public List<FxNode> getChildren();
    
    /**
     * Returns all enclosed nodes, regardless of whether the Node is a child element,
     * or attribute
     * 
     * @return 
     */
    @NonNull
    public Enumeration<FxNode>  getEnclosedNodes();
}
