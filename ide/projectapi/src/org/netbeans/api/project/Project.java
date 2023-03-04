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

package org.netbeans.api.project;

import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Represents one IDE project in memory.
 * <p>
 * <strong>Never cast a project instance</strong> to any subtype.
 * (Nor call {@link Object#getClass} on the <code>Project</code> instance.)
 * The project
 * manager is free to wrap any project in an unspecified proxy for its own
 * purposes. For extensibility, use {@link #getLookup}.
 * </p>
 * <div class="nonnormative">
 * <p>Note that this API is primarily of interest to project type provider
 * modules, and to infrastructure and generic GUI. Most other modules providing
 * tools or services should <em>not</em> need to explicitly model projects, and
 * should not be using this API much or at all.</p>
 * </div>
 * @see <a href="http://wiki.netbeans.org/BuildSystemHowTo">NetBeans 4.0 Project &amp; Build System How-To</a>
 * @author Jesse Glick
 */
public interface Project extends Lookup.Provider {
    
    /**
     * Gets an associated directory where the project metadata and possibly sources live.
     * In the case of a typical Ant project, this is the top directory, not the
     * project metadata subdirectory.
     * @return a directory
     */
    FileObject getProjectDirectory();
    
    /**
     * Get any optional abilities of this project.
     * <div class="nonnormative">
     * <p>If you are <em>providing</em> a project, there are a number of interfaces
     * which you should consider implementing and including in lookup, some of which
     * are described below. If you are <em>using</em> a project from another module,
     * there are some cases where you will want to ask a project for a particular
     * object in lookup (e.g. <code>ExtensibleMetadataProvider</code>) but in most
     * cases you should not; in the case of queries, always call the static query
     * API helper method, rather than looking for the query implementation objects
     * yourself. <strong>In the case of <code>ProjectInformation</code> and <code>Sources</code>,
     * use {@link ProjectUtils} rather than directly searching the project lookup.</strong>
     * </p>
     * <p>The following abilities are recommended:</p>
     * <ol>
     * <li>{@link org.netbeans.api.project.ProjectInformation}</li>
     * <li><a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/LogicalViewProvider.html"><code>LogicalViewProvider</code></a></li>
     * <li><a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/CustomizerProvider.html"><code>CustomizerProvider</code></a></li>
     * <li>{@link org.netbeans.api.project.Sources}</li>
     * <li>{@link org.netbeans.spi.project.ActionProvider}</li>
     * <li>{@link org.netbeans.spi.project.SubprojectProvider}</li>
     * <li>{@link org.netbeans.spi.project.AuxiliaryConfiguration}</li>
     * <li>{@link org.netbeans.spi.project.AuxiliaryProperties}</li>
     * <li>{@link org.netbeans.spi.project.CacheDirectoryProvider}</li>
     * </ol>
     * <p>You might also have e.g.:</p>
     * <ol>
     * <li>{@link org.netbeans.spi.project.ProjectConfigurationProvider}</li>
     * <li>{@link org.netbeans.spi.queries.FileBuiltQueryImplementation}</li>
     * <li>{@link org.netbeans.spi.queries.SharabilityQueryImplementation}</li>
     * <li>{@link org.netbeans.spi.queries.FileEncodingQueryImplementation}</li>
     * <li><a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/ProjectOpenedHook.html"><code>ProjectOpenedHook</code></a></li>
     * <li><a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/RecommendedTemplates.html"><code>RecommendedTemplates</code></a></li>
     * <li><a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/PrivilegedTemplates.html"><code>PrivilegedTemplates</code></a></li>
     * <li><a href="@org-netbeans-api-java-classpath@/org/netbeans/spi/java/classpath/ClassPathProvider.html"><code>ClassPathProvider</code></a></li>
     * <li><a href="@org-netbeans-api-java-classpath@/org/netbeans/spi/java/queries/SourceForBinaryQueryImplementation.html"><code>SourceForBinaryQueryImplementation</code></a></li>
     * <li><a href="@org-netbeans-api-java@/org/netbeans/spi/java/queries/SourceLevelQueryImplementation2.html"><code>SourceLevelQueryImplementation2</code></a></li>
     * <li><a href="@org-netbeans-api-java@/org/netbeans/spi/java/queries/JavadocForBinaryQueryImplementation.html"><code>JavadocForBinaryQueryImplementation</code></a></li>
     * <li><a href="@org-netbeans-api-java@/org/netbeans/spi/java/queries/AccessibilityQueryImplementation.html"><code>AccessibilityQueryImplementation</code></a></li>
     * <li><a href="@org-netbeans-api-java@/org/netbeans/spi/java/queries/MultipleRootsUnitTestForSourceQueryImplementation.html"><code>MultipleRootsUnitTestForSourceQueryImplementation</code></a></li>
     * <li><a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/ProjectXmlSavedHook.html"><code>ProjectXmlSavedHook</code></a></li>
     * <li><a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/ant/AntArtifactProvider.html"><code>AntArtifactProvider</code></a></li>
     * <li><a href="@org-openidex-util@/org/openidex/search/SearchInfo.html"><code>SearchInfo</code></a></li>
     * <li><a href="@org-netbeans-api-java-classpath@/org/netbeans/spi/java/queries/BinaryForSourceQueryImplementation.html"><code>BinaryForSourceQueryImplementation</code></a></li>
     * <li><a href="@org-netbeans-modules-project-ant@/org/netbeans/api/project/ant/AntBuildExtender.html"><code>AntBuildExtender</code></a></li>
     * <li><a href="@org-openide-loaders@/org/openide/loaders/CreateFromTemplateAttributesProvider.html"><code>CreateFromTemplateAttributesProvider</code></a></li>
     * </ol>
     * <p>Typical implementation:</p>
     * <pre>
     * private final Lookup lookup = Lookups.fixed(new Object[] {
     *     new MyAbility1(this),
     *     // ...
     * });
     * public Lookup getLookup() {
     *     return lookup;
     * }
     * </pre>
     * </div>
     * @return a set of abilities
     */
    Lookup getLookup();
    
}
