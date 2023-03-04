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

package org.netbeans.jellytools.modules.web.nodes;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.Node;


/** Node representing "Web Pages" folder */
public class WebPagesNode extends Node {

    static final NewFileAction newFileAction = new NewFileAction();
    static final FindAction findAction = new FindAction();
    static final PasteAction pasteAction = new PasteAction();
    static final PropertiesAction propertiesAction = new PropertiesAction();
    private static final String treePath = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.project.ui.Bundle",
            "LBL_Node_DocBase");
    
    /**
     * creates new WebPagesNode
     * @param projectName project name
     */
    public WebPagesNode(String projectName) {
        super(new ProjectsTabOperator().getProjectRootNode(projectName), treePath);
    }
    
    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[] { newFileAction, findAction, propertiesAction});
    }
        
    /** performs FindAction with this node */
    public void find() {
        findAction.perform(this);
    }
    
    /** performs PasteAction with this node */
    public void paste() {
        pasteAction.perform(this);
    }
    
    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
    
    /** performs NewFileAction with this node */
    public void newFile() {
        newFileAction.perform(this);
    }
    
    /** performs NewFileAction with this node
     * @param templateName template name from sub menu
     */
    public void newFile(String templateName) {
        new NewFileAction(templateName).perform(this);
    }
}
