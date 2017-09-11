/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.dependencies;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;

/**
 *
 * @author mkleint
 */
public class DependencyExcludeNodeVisitor implements DependencyNodeVisitor {
    private DependencyNode root;
    private Set<DependencyNode> directs;
    private Stack<DependencyNode> path;
    private Set<Stack<DependencyNode>> allPaths;
    private String key;

    public DependencyExcludeNodeVisitor(String groupId, String artifactId, String type) {
        assert groupId != null;
        assert artifactId != null;
        key = groupId + ":" + artifactId + ":" + type;
    }

    public Set<DependencyNode> getDirectDependencies() {
        return directs;
    }

    public Set<Stack<DependencyNode>> getAllPaths() {
        return allPaths;
    }

    @Override
    public boolean visit(DependencyNode node) {
        if (root == null) {
            root = node;
            directs = new HashSet<DependencyNode>();
            path = new Stack<DependencyNode>();
            allPaths = new HashSet<Stack<DependencyNode>>();
            return true;
        }
        path.push(node);
        Artifact artifact = node.getArtifact();
        if (key.equals(artifact.getDependencyConflictId())) {
            if (!path.isEmpty()) {
                directs.add(path.firstElement());
                Stack<DependencyNode> copy = new Stack<DependencyNode>();
                copy.addAll(path);
                allPaths.add(copy);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean endVisit(DependencyNode node) {
        if (root == node) {
            root = null;
            path = null;
            return true;
        }
        path.pop();
        return true;
    }

}
