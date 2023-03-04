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
