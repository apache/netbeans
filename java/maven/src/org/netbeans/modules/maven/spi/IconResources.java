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

package org.netbeans.modules.maven.spi;

import org.netbeans.api.annotations.common.StaticResource;

/**
 * static resources pointing to maven specific icons worth reusing (file an issue if you need some others)
 * @author mkleint
 * @since 2.93
 */
public class IconResources {
    public static final @StaticResource String JAVADOC_BADGE_ICON = "org/netbeans/modules/maven/DependencyJavadocIncluded.png"; //NOI18N
    public static final @StaticResource String SOURCE_BADGE_ICON = "org/netbeans/modules/maven/DependencySrcIncluded.png"; //NOI18N
    public static final @StaticResource String MAVEN_ICON = "org/netbeans/modules/maven/resources/Maven2Icon.gif";
    public static final @StaticResource String MANAGED_BADGE_ICON = "org/netbeans/modules/maven/DependencyManaged.png"; //NOI18N
    public static final @StaticResource String ARTIFACT_ICON = "org/netbeans/modules/maven/ArtifactIcon.png";
    public static final @StaticResource String DEPENDENCY_ICON = "org/netbeans/modules/maven/DependencyIcon.png";
    public static final @StaticResource String BROKEN_PROJECT_BADGE_ICON = "org/netbeans/modules/maven/brokenProjectBadge.png"; //NOI18N
    /**
     * Icon for a dependency JAR file.
     */
    public static final @StaticResource String ICON_DEPENDENCY_JAR = "org/netbeans/modules/maven/spi/nodes/DependencyJar.gif";
    public static final @StaticResource String TRANSITIVE_ARTIFACT_ICON = "org/netbeans/modules/maven/TransitiveArtifactIcon.png";
    public static final @StaticResource String TRANSITIVE_DEPENDENCY_ICON = "org/netbeans/modules/maven/TransitiveDependencyIcon.png";
    public static final @StaticResource String TRANSITIVE_MAVEN_ICON = "org/netbeans/modules/maven/TransitiveMaven2Icon.png";
    public static final @StaticResource String MOJO_ICON = "org/netbeans/modules/maven/execute/ui/mojo.png";

    private IconResources() {
    }

}
