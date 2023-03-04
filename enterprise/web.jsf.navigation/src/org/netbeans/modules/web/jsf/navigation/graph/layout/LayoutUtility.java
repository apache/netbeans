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
package org.netbeans.modules.web.jsf.navigation.graph.layout;

import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;

/**
 *
 * @author joelle
 */
public class LayoutUtility<N, E, P> {
    
    public static enum LayoutType  {
        GRID_GRAPH,  TREE_GRAPH, FREE_PLACES_NODES
    }
    
    public  LayoutUtility() {
    }
    
    //public static <N,E,P> void performLayout( GraphPinScene<N, E, P> graph, LayoutType type){
    @SuppressWarnings("unchecked") /* This is necessary because I can not specify N,E,P without a compilation error in jdk1.5.6 */
    public static void performLayout( GraphPinScene graph, LayoutType type){
        switch( type ) {
        case GRID_GRAPH:
            GridGraphLayoutUtility.performLayout(graph);
            break;
        case TREE_GRAPH:
            /* Tree Graph Layout Utility was taken from Tree Graph Layout as is incomplete.*/
            TreeGraphLayoutUtility.performLayout(graph);
            break;
        case FREE_PLACES_NODES:
           // TreeGraphLayoutUtility.performLayout(graph); 
            /* Dual Cast done to avoid bugs from 1.5.06 and earlier - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5066774*/
            if ( graph instanceof PageFlowScene)
                FreePlaceNodesLayouter.performLayout((PageFlowScene)graph);
            break;
        }
    }
}
