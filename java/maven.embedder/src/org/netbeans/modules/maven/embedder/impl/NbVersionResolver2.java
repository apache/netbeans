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
package org.netbeans.modules.maven.embedder.impl;

import javax.inject.Inject;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.resolution.VersionRequest;
import org.eclipse.aether.resolution.VersionResolutionException;
import org.eclipse.aether.resolution.VersionResult;

/**
 * This resolver intercepts requests to resolve artifact versions and artifact descriptor. The NbWorkspaceReader may 
 * then discover which artifact is actually missing and pass that information to ArtifactFixer.
 * <p>
 * Maven attempts to resolve the artifact and its POM - as another artifact, but the knowledge that the POM is actually
 * implied by the real artifact is lost in Maven's DependencyResolver. This interceptor saves the info so that
 * the Fixer has some context to report missing project dependencies.
 * 
 * @author sdedic
 */
public final class NbVersionResolver2 implements VersionResolver, VersionRangeResolver, ArtifactDescriptorReader {
    private static final ThreadLocal<Artifact> resolvingArtifact = new ThreadLocal<>();
    private static final ThreadLocal<Artifact> resolvingPom = new ThreadLocal<>();
    
    private final DefaultVersionResolver resolverDelegate;
    private final DefaultVersionRangeResolver rangeResolverDelegate;
    private final DefaultArtifactDescriptorReader descriptorReader;
    
    @Inject
    public NbVersionResolver2(DefaultVersionResolver r, DefaultVersionRangeResolver r2, DefaultArtifactDescriptorReader reader) {
        this.resolverDelegate = r;
        this.rangeResolverDelegate = r2;
        this.descriptorReader = reader;
        reader.setVersionResolver(this);
        reader.setVersionRangeResolver(this);
    }

    @Override
    public ArtifactDescriptorResult readArtifactDescriptor(RepositorySystemSession rss, ArtifactDescriptorRequest adr) throws ArtifactDescriptorException {
        Artifact save = resolvingPom.get();
        try {
            resolvingPom.set(adr.getArtifact());
            return descriptorReader.readArtifactDescriptor(rss, adr);
        } finally {
            resolvingPom.set(save);
        }
    }
    
    @Override
    public VersionResult resolveVersion(RepositorySystemSession session, VersionRequest request) throws VersionResolutionException {
        Artifact a = request.getArtifact();
        Artifact rq = null;
        // TODO: also can record the artifact SCOPE. It is not present directly, but
        // one can traverse through request.getTrace(), seeking for e.g. CollectStep that contains Dependency, which have a scope. Leaving for
        // future improvement.
        if (request.getTrace().getData() instanceof ArtifactDescriptorRequest) {
            rq = ((ArtifactDescriptorRequest)request.getTrace().getData()).getArtifact();
            if (rq.getArtifactId().equals(a.getArtifactId()) && rq.getGroupId().equals(a.getGroupId()) && rq.getVersion().equals(a.getVersion())) {
                // replace POM artifacts with their original ones
                a = rq;
            }
        } else if ("pom".equals(a.getExtension()) && 
            (request.getTrace().getData() instanceof ArtifactRequest) && request.getTrace().getParent() != null && 
            (request.getTrace().getParent().getData() instanceof ArtifactDescriptorRequest)) {
            rq = ((ArtifactDescriptorRequest)request.getTrace().getParent().getData()).getArtifact();
        }
        if (rq != null && 
            rq.getArtifactId().equals(a.getArtifactId()) && rq.getGroupId().equals(a.getGroupId()) && rq.getVersion().equals(a.getVersion())) {
            // replace POM artifacts with their original ones
            a = rq;
        }
        Artifact save = resolvingArtifact.get();
        resolvingArtifact.set(a);
        try {
            return resolverDelegate.resolveVersion(session, request);
        } finally {
            resolvingArtifact.set(save);
        }
    }

    @Override
    public VersionRangeResult resolveVersionRange(RepositorySystemSession rss, VersionRangeRequest vrr) throws VersionRangeResolutionException {
        Artifact a = vrr.getArtifact();
        if (vrr.getTrace().getData() instanceof ArtifactDescriptorRequest) {
            Artifact rq = ((ArtifactDescriptorRequest)vrr.getTrace().getData()).getArtifact();
            if (rq.getArtifactId().equals(a.getArtifactId()) && rq.getGroupId().equals(a.getGroupId()) && rq.getVersion().equals(a.getVersion())) {
                // replace POM artifacts with their original ones
                a = rq;
            }
        }
        Artifact save = resolvingArtifact.get();
        resolvingArtifact.set(a);
        try {
            return rangeResolverDelegate.resolveVersionRange(rss, vrr);
        } finally {
            resolvingArtifact.set(save);
        }
    }
    
    public Artifact getResolvingArtifact() {
        Artifact pomOrigin = resolvingPom.get();
        Artifact resolving = resolvingArtifact.get();
        if (resolving == null) {
            if (pomOrigin != null) {
                resolving = pomOrigin;
            }
        } else if (pomOrigin != null) {
            if (resolving.getGroupId().equals(pomOrigin.getGroupId()) && 
                resolving.getArtifactId().equals(pomOrigin.getArtifactId()) &&
                resolving.getVersion().equals(pomOrigin.getVersion())) {
                resolving = pomOrigin;
            }
        }
        return resolving;
    }
}
