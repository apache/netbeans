/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.embedder.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.maven.artifact.Artifact;

/**
 * A node of the resolved dependency tree. Replaces the node type of the
 * legacy Maven Dependency Tree library and keeps the subset of its API that NetBeans uses.
 * Nodes use identity for {@code equals} and {@code hashCode}.
 *
 * @since 2.90
 */
public final class DependencyNode {

    /** The artifact is part of the resolved dependencies. */
    public static final int INCLUDED = 0;

    /** The artifact was omitted because the same version is already in the tree. */
    public static final int OMITTED_FOR_DUPLICATE = 1;

    /** The artifact was omitted because another version of it won the conflict resolution. */
    public static final int OMITTED_FOR_CONFLICT = 2;

    /** The artifact was omitted because it is an ancestor of itself. */
    public static final int OMITTED_FOR_CYCLE = 3;

    private final Artifact artifact;
    private final int state;
    private final Artifact relatedArtifact;
    private final List<DependencyNode> children = new ArrayList<>();
    private DependencyNode parent;

    /**
     * Creates an included node.
     * @param artifact the artifact of the node
     */
    public DependencyNode(Artifact artifact) {
        this(artifact, INCLUDED, null);
    }

    /**
     * Creates a node.
     * @param artifact the artifact of the node
     * @param state one of the state constants
     * @param relatedArtifact the artifact that won the conflict or duplicates this one; {@code null} for {@link #INCLUDED}
     */
    public DependencyNode(Artifact artifact, int state, Artifact relatedArtifact) {
        this.artifact = artifact;
        this.state = state;
        this.relatedArtifact = relatedArtifact;
    }

    /**
     * Visits this node and, while the visitor returns {@code true}, its descendants.
     * @param visitor the visitor
     * @return the result of {@link DependencyNodeVisitor#endVisit}
     */
    public boolean accept(DependencyNodeVisitor visitor) {
        if (visitor.visit(this)) {
            for (DependencyNode child : children) {
                if (!child.accept(visitor)) {
                    break;
                }
            }
        }
        return visitor.endVisit(this);
    }

    public void addChild(DependencyNode child) {
        children.add(child);
        child.parent = this;
    }

    public void removeChild(DependencyNode child) {
        children.remove(child);
        child.parent = null;
    }

    public DependencyNode getParent() {
        return parent;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    /**
     * @return the number of ancestors, 0 for the root
     */
    public int getDepth() {
        int depth = 0;
        for (DependencyNode p = parent; p != null; p = p.parent) {
            depth++;
        }
        return depth;
    }

    /**
     * @return an unmodifiable view of the children
     */
    public List<DependencyNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public int getState() {
        return state;
    }

    /**
     * @return the artifact that won the conflict (for {@link #OMITTED_FOR_CONFLICT}) or the one this
     *         node duplicates, {@code null} for {@link #INCLUDED}
     */
    public Artifact getRelatedArtifact() {
        return relatedArtifact;
    }

    @Override
    public String toString() {
        return artifact + (state == INCLUDED ? "" : " (omitted, state " + state + ")");
    }
}
