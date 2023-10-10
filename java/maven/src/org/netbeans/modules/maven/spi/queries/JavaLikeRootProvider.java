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

package org.netbeans.modules.maven.spi.queries;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 * Extension point for contributing source roots compilable in parallel with Java sources.
 * Necessary since the {@link ClassPath#SOURCE} for {@code src/main/java/} must include {@code src/main/KIND/} too.
 * Generally these roots (under {@code src/test/KIND/} also) will get the same classpath information as Java.
 * Creation of <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/SourceGroup.html">SourceGroup</a>s (or their nodes) is not automatic, as this requires display labels
 * and might merit further customization.
 * Related/similar to {@link org.netbeans.modules.maven.spi.nodes.OtherSourcesExclude}
 * 
 * Note: the api is a bit simplistic for performance reasons, eg. if user reconfigures the src/main/groovy content to a different location in pom.xml we silently assume that
 * src/main/groovy doesn't exist then.
 * @see ProjectServiceProvider
 * @since 2.36
 */
public interface JavaLikeRootProvider {

    /**
     * Obtains the kind of language supported.
     * @return e.g. {@code "groovy"}
     */
    String kind();

}
