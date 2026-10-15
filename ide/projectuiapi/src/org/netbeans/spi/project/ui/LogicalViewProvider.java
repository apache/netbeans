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

package org.netbeans.spi.project.ui;

import org.netbeans.spi.project.ProjectIconAnnotator;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.nodes.Node;

/**
 * Ability for a {@link org.netbeans.api.project.Project} to supply
 * a logical view of itself.
 * @see org.netbeans.api.project.Project#getLookup
 * @see ProjectIconAnnotator
 * @author Jesse Glick
 */
public interface LogicalViewProvider extends PathFinder {

    /**
     * Create a logical view node.
     * Projects should not attempt to cache this node in any way;
     * this call should always create a fresh node with no parent.
     * The node's lookup should contain the project object.
     * <p>
     * For the root node; any files {@linkplain org.netbeans.api.project.Sources contained} in the project will
     * be considered as badging sources automatically. Other subnodes representing
     * various collections of files may still need explicit badging logic.
     * <em>As of <code>org.netbeans.modules.projectuiapi/1 1.31</code></em>
     * </p>
     * @return a node displaying the contents of the project in an intuitive way
     * @see CommonProjectActions#forType
     */
    Node createLogicalView();        
    
}
