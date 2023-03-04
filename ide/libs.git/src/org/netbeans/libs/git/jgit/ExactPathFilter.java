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
package org.netbeans.libs.git.jgit;

import java.io.IOException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 *
 * @author ondra
 */
public class ExactPathFilter extends TreeFilter {

    public static ExactPathFilter create (PathFilter filter) {
        return new ExactPathFilter(filter);
    }
    final String pathStr;
    final byte[] pathRaw;
    private final PathFilter filter;

    private ExactPathFilter (final PathFilter filter) {
        pathStr = filter.getPath();
        pathRaw = Constants.encode(pathStr);
        this.filter = filter;
    }

    @Override
    public TreeFilter clone() {
        return this;
    }

    @Override
    public boolean include(TreeWalk walker) throws MissingObjectException, IncorrectObjectTypeException, IOException {
        return filter.include(walker) && walker.isPathSuffix(pathRaw, pathRaw.length);
    }

    @Override
    public boolean shouldBeRecursive() {
        return true;
    }
}
