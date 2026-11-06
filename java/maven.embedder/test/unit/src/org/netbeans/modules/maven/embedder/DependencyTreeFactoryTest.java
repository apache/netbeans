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
package org.netbeans.modules.maven.embedder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.netbeans.modules.maven.embedder.tree.DependencyNode.INCLUDED;
import static org.netbeans.modules.maven.embedder.tree.DependencyNode.OMITTED_FOR_CONFLICT;
import static org.netbeans.modules.maven.embedder.tree.DependencyNode.OMITTED_FOR_CYCLE;
import static org.netbeans.modules.maven.embedder.tree.DependencyNode.OMITTED_FOR_DUPLICATE;

/**
 * Maps a graph that went through the conflict resolver configured by {@link DependencyTreeFactory}.
 */
public class DependencyTreeFactoryTest {

    private static final GenericVersionScheme SCHEME = new GenericVersionScheme();

    private static DependencyNode node(String artifactId, String version, DependencyNode... children) throws InvalidVersionSpecificationException {
        DefaultDependencyNode n = new DefaultDependencyNode(
                new Dependency(new DefaultArtifact("g", artifactId, "jar", version), "compile"));
        n.setVersion(SCHEME.parseVersion(version));
        n.setVersionConstraint(SCHEME.parseVersionConstraint(version));
        n.setChildren(new ArrayList<>(List.of(children)));
        return n;
    }

    private static org.netbeans.modules.maven.embedder.tree.DependencyNode tree(DependencyNode root) throws Exception {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
        DependencyTreeFactory.configureVerboseSession(session);
        Map<Object, Object> data = new HashMap<>();
        session.getDependencyGraphTransformer().transformGraph(root, new DependencyGraphTransformationContext() {
            @Override
            public RepositorySystemSession getSession() {
                return session;
            }
            @Override
            public Object get(Object key) {
                return data.get(key);
            }
            @Override
            public Object put(Object key, Object value) {
                return data.put(key, value);
            }
        });
        Artifact project = RepositoryUtils.toArtifact(root.getArtifact());
        return DependencyTreeFactory.createDependencyTree(root, project, null);
    }

    private static org.netbeans.modules.maven.embedder.tree.DependencyNode child(
            org.netbeans.modules.maven.embedder.tree.DependencyNode parent, String artifactId, int index) {
        List<org.netbeans.modules.maven.embedder.tree.DependencyNode> found = new ArrayList<>();
        for (org.netbeans.modules.maven.embedder.tree.DependencyNode c : parent.getChildren()) {
            if (c.getArtifact().getArtifactId().equals(artifactId)) {
                found.add(c);
            }
        }
        return found.get(index);
    }

    @Test
    public void conflictLoserIsOmittedForConflictWithWinnerAsRelatedArtifact() throws Exception {
        // c:1 is nearer than c:2, so c:2 loses
        DependencyNode root = node("root", "1.0", node("c", "1.0"), node("a", "1.0", node("c", "2.0")));
        org.netbeans.modules.maven.embedder.tree.DependencyNode tree = tree(root);

        org.netbeans.modules.maven.embedder.tree.DependencyNode winner = child(tree, "c", 0);
        assertEquals(INCLUDED, winner.getState());
        assertNull(winner.getRelatedArtifact());

        org.netbeans.modules.maven.embedder.tree.DependencyNode loser = child(child(tree, "a", 0), "c", 0);
        assertEquals("2.0", loser.getArtifact().getVersion());
        assertEquals(OMITTED_FOR_CONFLICT, loser.getState());
        assertNotNull(loser.getRelatedArtifact());
        assertEquals("1.0", loser.getRelatedArtifact().getVersion());
        assertEquals(loser.getArtifact().getDependencyConflictId(), loser.getRelatedArtifact().getDependencyConflictId());
        assertFalse(loser.hasChildren());
        assertEquals(2, loser.getDepth());
        assertEquals("compile", loser.getArtifact().getScope());
    }

    @Test
    public void sameVersionLoserIsOmittedForDuplicate() throws Exception {
        DependencyNode root = node("root", "1.0", node("c", "1.0"), node("a", "1.0", node("c", "1.0")));
        org.netbeans.modules.maven.embedder.tree.DependencyNode tree = tree(root);

        assertEquals(INCLUDED, child(tree, "c", 0).getState());
        org.netbeans.modules.maven.embedder.tree.DependencyNode dup = child(child(tree, "a", 0), "c", 0);
        assertEquals(OMITTED_FOR_DUPLICATE, dup.getState());
        assertEquals("1.0", dup.getRelatedArtifact().getVersion());
        assertFalse(dup.hasChildren());
    }

    @Test
    public void winnerKeepsItsChildren() throws Exception {
        DependencyNode root = node("root", "1.0", node("a", "1.0", node("d", "1.0")));
        org.netbeans.modules.maven.embedder.tree.DependencyNode tree = tree(root);
        org.netbeans.modules.maven.embedder.tree.DependencyNode d = child(child(tree, "a", 0), "d", 0);
        assertEquals(INCLUDED, d.getState());
        assertSame(tree, d.getParent().getParent());
    }

    @Test
    public void loserWhoseWinnerIsAnAncestorIsOmittedForCycle() throws Exception {
        // root -> a -> b -> a' where a' is a loser of a, as if the conflict resolver had kept it
        DependencyNode a = node("a", "1.0");
        DependencyNode b = node("b", "1.0");
        DefaultDependencyNode again = (DefaultDependencyNode) node("a", "1.0");
        again.setData(ConflictResolver.NODE_DATA_WINNER, a);
        b.setChildren(new ArrayList<>(List.of(again)));
        a.setChildren(new ArrayList<>(List.of(b)));
        DependencyNode root = node("root", "1.0", a);

        org.netbeans.modules.maven.embedder.tree.DependencyNode tree = DependencyTreeFactory.createDependencyTree(
                root, RepositoryUtils.toArtifact(root.getArtifact()), null);

        org.netbeans.modules.maven.embedder.tree.DependencyNode cycle = child(child(child(tree, "a", 0), "b", 0), "a", 0);
        assertEquals(OMITTED_FOR_CYCLE, cycle.getState());
        assertNull(cycle.getRelatedArtifact());
    }

    @Test
    public void conflictResolverCutsCyclesSoTheTreeEnds() throws Exception {
        DependencyNode a = node("a", "1.0");
        DependencyNode b = node("b", "1.0");
        DependencyNode again = node("a", "1.0");
        List<DependencyNode> aChildren = new ArrayList<>(List.of(b));
        a.setChildren(aChildren);
        b.setChildren(new ArrayList<>(List.of(again)));
        // like the collector, the repeated node shares the children of the ancestor
        again.setChildren(aChildren);

        org.netbeans.modules.maven.embedder.tree.DependencyNode tree = tree(node("root", "1.0", a));

        org.netbeans.modules.maven.embedder.tree.DependencyNode bNode = child(child(tree, "a", 0), "b", 0);
        assertEquals(INCLUDED, bNode.getState());
        assertFalse(bNode.hasChildren());
    }
}
