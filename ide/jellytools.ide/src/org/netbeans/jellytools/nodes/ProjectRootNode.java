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
package org.netbeans.jellytools.nodes;

import org.netbeans.jellytools.actions.*;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Project root node class. It represents root node of a project in Projects
 * view.
 * @see org.netbeans.jellytools.ProjectsTabOperator
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class ProjectRootNode extends Node {

    static final FindAction findAction = new FindAction();    
    static final PropertiesAction propertiesAction = new PropertiesAction();
    static final DebugProjectAction debugProjectAction = new DebugProjectAction();
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            findAction,            
            propertiesAction,
            debugProjectAction
        });
    }
    
    /** creates new ProjectRootNode instance
     * @param treeOperator treeOperator JTreeOperator of tree with Filesystems repository 
     * @param projectName display name of project
     */
    public ProjectRootNode(JTreeOperator treeOperator, String projectName) {
        super(treeOperator, projectName);
    }
    
    /** opens Search Filesystems dialog */    
    public void find() {
        findAction.perform(this);
    }
       
    /** opens properties of project */    
    public void properties() {
        propertiesAction.perform(this);
    }

    /** debug project */
    public void debug()
    {
        debugProjectAction.perform(this);
    }
}
