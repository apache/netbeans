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

package org.netbeans.modules.javaee.project.api.ant.ui.logicalview;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.LibrariesNode;
import org.netbeans.modules.java.api.common.project.ui.LibrariesNode.Key;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.nodes.Node;

public final class ExtraLibrariesTestNode implements LibrariesNode.Callback {

    private Project p;
    private PropertyEvaluator eval;
    private String j2eePlatformProperty;
    private ClassPathSupport cs;

    public ExtraLibrariesTestNode(Project p, PropertyEvaluator eval, String j2eePlatformProperty, ClassPathSupport cs) {
        this.p = p;
        this.eval = eval;
        this.j2eePlatformProperty = j2eePlatformProperty;
        this.cs = cs;
    }
    
    public List<Key> getExtraKeys() {
        J2eeProjectCapabilities projectCap = J2eeProjectCapabilities.forProject(p);
        if (projectCap.isEjb31LiteSupported()){
            return Collections.<Key>singletonList(new LibrariesNode.Key(ExtraLibrariesTestNode.class.getName()));
        } else {
            return Collections.<Key>emptyList();
        }
    }

    public Node[] createNodes(Key key) {
        assert key.getID().equals(ExtraLibrariesTestNode.class.getName());
        return new Node[]{J2eePlatformTestNode.create(p, eval, j2eePlatformProperty, cs)};
    }

}
