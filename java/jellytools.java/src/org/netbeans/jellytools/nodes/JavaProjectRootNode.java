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

/**
 * Java-specific extension to ProjectRootNode - adds "Build Project" and "Clean Project"
 * actions.
 *  
 * @author Vojtech.Sigler@sun.com
 */
public class JavaProjectRootNode extends ProjectRootNode {

    static final BuildJavaProjectAction buildProjectAction = new BuildJavaProjectAction();
    static final CleanJavaProjectAction cleanProjectAction = new CleanJavaProjectAction();
    

    /** tests popup menu items for presence */
    public void verifyPopup() {
        super.verifyPopup();
        verifyPopup(new Action[]{
            cleanProjectAction,
            buildProjectAction            
        });
    }

    /** creates new ProjectRootNode instance
     * @param treeOperator treeOperator JTreeOperator of tree with Filesystems repository
     * @param projectName display name of project
     */
    public JavaProjectRootNode(JTreeOperator treeOperator, String projectName) {
        super(treeOperator, projectName);
    }

    /** build project */
    public void buildProject() {
        buildProjectAction.perform(this);
    }

    /** Clean project */
    public void cleanProject() {
        cleanProjectAction.perform(this);
    }

}
