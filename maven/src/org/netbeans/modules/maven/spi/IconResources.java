/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
