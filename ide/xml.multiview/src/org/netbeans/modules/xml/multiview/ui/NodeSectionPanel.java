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

package org.netbeans.modules.xml.multiview.ui;

/**
 * NodeSectionPanel.java
 * This is the interface to section panels that correspond to specific node in explorer view.
 *
 * Created on October 17, 2004, 8:57 PM
 * @author mkuchtiak
 */
public interface NodeSectionPanel {

    /** Gets the corresponding node for the panel
     * @return Node Node that coresponds to this Section Panel
     */
    public org.openide.nodes.Node getNode();

    /** Sets this panel as the active panel in the section view
     * @param boolean active`tells if the panel should be active or passive
     * @param boolean active`tells if the panel should be active or passive
     */    
    public void setActive(boolean active);
    
    /** Tells whether the panel is active or not.
     * @return boolean true or false
     */    
    public boolean isActive();
    
    /** Opens (extends) the panel for editing.
     */       
    public void open();
    
    /** Scrolls the panel to be visibel in scrollPane.
     */        
    public void scroll();
    
    /** Sets panel index.
     */     
    void setIndex(int index);
    
    /** Gets panel index.
     */     
    int getIndex();

}
