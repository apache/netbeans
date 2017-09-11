/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
