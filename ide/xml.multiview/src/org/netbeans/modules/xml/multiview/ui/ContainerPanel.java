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

/*
 * ContainerPanel.java
 *
 * Created on November 26, 2004, 6:35 PM
 */

package org.netbeans.modules.xml.multiview.ui;

/** ContainerPanel.java
 *  Interface for panel containing sections.
 *
 * Created on November 26, 2004, 6:35 PM
 * @author mkuchtiak
 */
public interface ContainerPanel {
    /** Gets section for specific explorer node
     * @param Node key explorer node
     * @return NodeSectionPanel JPanel corresponding to given node
     */
    public NodeSectionPanel getSection(org.openide.nodes.Node key);

    /** Adds new section
     * @param section component(JPanel) to be added to container
     */ 
    public void addSection(NodeSectionPanel section);
    
    /** Removes section
     * @param section component(JPanel) to be removed from container
     */ 
    public void removeSection(NodeSectionPanel section);
    
    /** Gets node corresponding to this container panel
     * @return Node corresponding node
     */  
    public org.openide.nodes.Node getRoot();
}
