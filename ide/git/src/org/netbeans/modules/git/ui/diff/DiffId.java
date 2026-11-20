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

package org.netbeans.modules.git.ui.diff;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 * An identity of a diff: the files the diff is computed for and the left
 * and right revisions.
 *
 * @param files the absolute paths of the files the diff is computed for
 * @param left the left revision
 * @param right the right revision
 * @author mbien
 */
record DiffId(Set<String> files, Revision left, Revision right) {

    static DiffId of(VCSContext context, Revision left, Revision right) {
        Set<String> files = HashSet.newHashSet(context.getRootFiles().size());
        for (File file : context.getRootFiles()) {
            files.add(file.getAbsolutePath());
        }
        return new DiffId(Set.copyOf(files), left, right);
    }

    static DiffId of(File file, Revision left, Revision right) {
        return new DiffId(Set.of(file.getAbsolutePath()), left, right);
    }
}
