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

package org.netbeans.modules.maven.spi.nodes;

import java.nio.file.Path;
import java.util.Set;

/**
 * Extension point to tell the Other sources project subnode to exclude the given folder name in
 * src/main and src/test. It is assumed that implementing this interface is associated 
 * with implementation of a NodeFactory providing special handling for such folder.
 * To be registered in project lookup.
 * Related to {@link org.netbeans.modules.maven.spi.queries.JavaLikeRootProvider} but only handling the Other sources node display.
 * 
 * Note: the api is a bit simplistic for performance reasons, eg. if user reconfigures the src/main/webapp content to a different location in pom.xml we silently assume that
 * src/main/webapp doesn't exist then.
 * @author mkleint
 * @since 2.88
 */
public interface OtherSourcesExclude {

    /**
     * Paths of sub-folders of src/main and src/test that should be excluded in Other
     * Sources node.
     *
     * @since 2.106
     *
     * @return {@link Path}s of sub-folders which should be excluded
     */
    Set<Path> excludedFolders();
}
