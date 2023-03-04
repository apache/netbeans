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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ExploreFromHereAction;
import org.netbeans.jellytools.actions.FindAction;

/** Node representing Source packages node under project node.
 * @author Jiri.Skrivanek@sun.com
 */
public class SourcePackagesNode extends Node {

    private static final String SOURCE_PACKAGES_LABEL = Bundle.getString(
                                "org.netbeans.modules.java.j2seproject.Bundle",
                                "NAME_src.dir");
    static final ExploreFromHereAction exploreFromHereAction = new ExploreFromHereAction();
    static final FindAction findAction = new FindAction();
    
    /** Finds Source Packages node under project with given name
     * @param projectName display name of project
     */
    public SourcePackagesNode(String projectName) {
        super(new ProjectsTabOperator().getProjectRootNode(projectName), SOURCE_PACKAGES_LABEL);
    }

    /** Finds Source Packages node under given project node.
     * @param projectNode project node in the Projects view
     */
    public SourcePackagesNode(Node projectNode) {
        super(projectNode, SOURCE_PACKAGES_LABEL);
    }

    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            exploreFromHereAction,
            findAction,
        });
    }
    
    /** performs ExploreFromHereAction with this node */    
    public void exploreFromHere() {
        exploreFromHereAction.perform(this);
    }
    
    /** performs FindAction with this node */    
    public void find() {
        findAction.perform(this);
    }
}
