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
package org.netbeans.jellytools;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Utility methods.
 *
 * @author Jiri Skrivanek
 */
public class Utils {

    public static Node getSourcePackagesNode(String projectName) {
        Node sourcePackagesNode = new Node(getProjectRootNode(projectName), "Source Packages");
        return sourcePackagesNode;
    }

    public static Node getSourcePackagesNode() {
        return getSourcePackagesNode("SampleProject");
    }

    public static Node getProjectRootNode(String projectName) {
        TopComponentOperator tcoProjects = new TopComponentOperator("Projects");
        tcoProjects.makeComponentVisible();
        return new Node(new JTreeOperator(tcoProjects), projectName);
    }
}