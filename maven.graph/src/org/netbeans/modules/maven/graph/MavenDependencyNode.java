/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.openide.util.NbBundle;
import org.netbeans.modules.java.graph.GraphNodeImplementation;

/**
 *
 * @author tomas
 */
public class MavenDependencyNode implements GraphNodeImplementation {

    static final Color PROVIDED = new Color(191, 255, 255);
    static final Color COMPILE = new Color(191, 191, 255);
    static final Color RUNTIME = new Color(191, 255, 191);
    static final Color TEST = new Color(202, 151, 151);
    private final org.apache.maven.shared.dependency.tree.DependencyNode dependencyNode;
    private DefaultArtifactVersion version;
    private List<String> scopes;
    private MavenDependencyNode parent;

    private final HashMap<org.apache.maven.shared.dependency.tree.DependencyNode, MavenDependencyNode> children = new HashMap<>();
    
    public MavenDependencyNode(org.apache.maven.shared.dependency.tree.DependencyNode dependencyNode) {
        this(null, dependencyNode);
    }
    
    private MavenDependencyNode(MavenDependencyNode parent, org.apache.maven.shared.dependency.tree.DependencyNode dependencyNode) {
        this.dependencyNode = dependencyNode;
        this.parent = parent;
    }

    public Artifact getArtifact() {
        return dependencyNode.getArtifact();
    }
    
    private String getScope() {
        return dependencyNode.getArtifact().getScope();
    }
    
    public Color getScopeColor() {
        String scope = getScope();
        if(scopes == null || !scopes.contains(scope)) {
            return null;
        }
        if (Artifact.SCOPE_COMPILE.equals(scope)) {
            return COMPILE;
        }
        if (Artifact.SCOPE_PROVIDED.equals(scope)) {
            return PROVIDED;
        }
        if (Artifact.SCOPE_RUNTIME.equals(scope)) {
            return RUNTIME;
        }
        if (Artifact.SCOPE_TEST.equals(scope)) {
            return TEST;
        }
        return Color.BLACK;
    }

    void hightlightScopes(List<String> scopes) {
        this.scopes = scopes;
    }
            
    @Override
    public String getQualifiedName() {
        return dependencyNode.getArtifact().getId();
    }

    @Override
    public String getName() {
        return dependencyNode.getArtifact().getArtifactId();
    }

    public String getVersion() {
        return dependencyNode.getArtifact().getVersion();
    }

    int compareVersions(MavenDependencyNode dependencyNode2) {
        if(version == null) {
            version = new DefaultArtifactVersion(getVersion());
        }
        return version.compareTo(new DefaultArtifactVersion(dependencyNode2.getVersion()));
    }
    
    @NbBundle.Messages({
        "TIP_Artifact=<i>GroupId:</i><b> {0}</b><br><i>ArtifactId:</i><b> {1} </b><br><i>Version:</i><b> {2}</b><br><i>Scope:</i><b> {3}</b><br><i>Type:</i><b> {4}</b>"
    })
    @Override
    public String getTooltipText() {
        final String scope = (getScope() != null ? getScope() : "");
        return Bundle.TIP_Artifact(dependencyNode.getArtifact().getGroupId(), dependencyNode.getArtifact().getArtifactId(), dependencyNode.getArtifact().getVersion(), scope, dependencyNode.getArtifact().getType());
    }

    @Override
    public MavenDependencyNode getParent() {
        org.apache.maven.shared.dependency.tree.DependencyNode p = dependencyNode.getParent();
        if(parent == null) {
            parent = p != null ? new MavenDependencyNode(dependencyNode.getParent()) : null;
        }
        return parent;
    }

    public int getState() {
        return dependencyNode.getState();
    }

    public String getDependencyConflictId() {
        return dependencyNode.getArtifact().getDependencyConflictId();
    }

    public String getRelatedDependencyConflictId() {
        return dependencyNode.getRelatedArtifact().getDependencyConflictId();
    }

    @Override
    public List<MavenDependencyNode> getChildren() {
        synchronized(children) {
            List<MavenDependencyNode> ret = new ArrayList<>();
            for (org.apache.maven.shared.dependency.tree.DependencyNode n : dependencyNode.getChildren()) {
                MavenDependencyNode mn = children.get(n);
                if(mn == null) {
                    mn = new MavenDependencyNode(this, n);
                    children.put(n, mn);
                }
                ret.add(mn);
            }
            return ret;
        }
    }

    public void addChild(MavenDependencyNode node) {
        synchronized(children) {
            children.put(node.dependencyNode, node);
            dependencyNode.addChild(node.dependencyNode);
        }
    }

    public synchronized void removeChild(MavenDependencyNode node) {
        synchronized(children) {
            children.remove(node.dependencyNode, node);
            dependencyNode.removeChild(node.dependencyNode);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.dependencyNode != null ? this.dependencyNode.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MavenDependencyNode other = (MavenDependencyNode) obj;
        if (this.dependencyNode != other.dependencyNode && (this.dependencyNode == null || !this.dependencyNode.equals(other.dependencyNode))) {
            return false;
        }
        return true;
    }

}
