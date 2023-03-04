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

package org.netbeans.modules.gradle.test.ui.nodes;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.output.Location;
import org.netbeans.modules.gradle.java.api.output.LocationOpener;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.netbeans.modules.java.testrunner.ui.api.NodeOpener;
import org.netbeans.modules.java.testrunner.ui.api.UIJavaUtils;
import org.netbeans.modules.junit.ui.api.JUnitTestMethodNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Laszlo Kishalmi
 */
@NodeOpener.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE, testingFramework = CommonUtils.JUNIT_TF)
public final class GradleJUnitNodeOpener extends NodeOpener {

    @Override
    public void openTestsuite(TestsuiteNode tn) {
        Children children = tn.getChildren();
        if (children != null) {
            Node first = children.getNodeAt(0);
            if (first instanceof GradleTestMethodNode) {
                GradleTestMethodNode node = (GradleTestMethodNode) first;
                Location loc = node.getTestLocation().withNoTarget();
                new LocationOpener(loc, node).open();
            }
        }
    }

    @Override
    public void openTestMethod(TestMethodNode tmn) {
        if (tmn instanceof GradleTestMethodNode) {
            GradleTestMethodNode node = (GradleTestMethodNode) tmn;
            Location loc = node.getTestLocation();
            new LocationOpener(loc, node).open();
        }
    }

    @Override
    public void openCallstackFrame(Node node, String frameInfo) {
        Location loc = null;
        if (frameInfo.isEmpty()) {
            if (!(node instanceof GradleTestMethodNode)) {
                return;
            } else {
                loc = ((GradleTestMethodNode) node).getTestLocation();
            }
        }
        JUnitTestMethodNode methodNode = (JUnitTestMethodNode) UIJavaUtils.getTestMethodNode(node);
        if (!(methodNode instanceof GradleTestMethodNode)) {
            return;
        }
        if (loc == null) {
            loc = Location.locationFromCallStackItem(frameInfo);
        }
        if (loc != null) {
            new LocationOpener(loc).open();
        }
    }
    
}
