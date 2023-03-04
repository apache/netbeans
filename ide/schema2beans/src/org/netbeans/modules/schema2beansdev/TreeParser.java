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

package org.netbeans.modules.schema2beansdev;

import java.util.*;
import java.io.*;


/**
 *
 *  Methods used by the bean builder to access the DTD object graph nodes.
 *  This interface is implemented by the TreeBuilder and used by the class
 *  BeanBuilder.
 *
 */
public interface TreeParser {
    /**
     *	Return the list of all the GraphNode objects of the DTD object graph.
     */
    public GraphNode[] getNodes();

    /**
     *	Return a specific GraphNode.
     */
    public GraphNode getNode(String name);

    /**
     *	Return the root of the DTD object graph.
     */
    public GraphNode getRoot();

    /**
     * Get the namespace that will be used by default in the documents.
     */
    public String getDefaultNamespace();
}
