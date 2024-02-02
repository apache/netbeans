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
package org.netbeans.modules.project.dependency.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.refactoring.spi.ModificationResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
public class CompoundModificationResult implements ModificationResult {
    private final List<ModificationResult> results;

    public CompoundModificationResult(List<ModificationResult> results) {
        this.results = results;
    }

    @Override
    public String getResultingSource(FileObject file) throws IOException, IllegalArgumentException {
        for (ModificationResult r : results) {
            // PENDING: assumes that there's no overlap between results. Computing difference across
            // multiple ModificationResults would require a shared thread-local copy of the file's contents. Doable, but
            // out of scope now.
            if (r.getModifiedFileObjects().contains(file)) {
                return r.getResultingSource(file);
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Collection<? extends FileObject> getModifiedFileObjects() {
        Set<FileObject> s = new LinkedHashSet<>();
        for (ModificationResult r : results) {
            s.addAll(r.getModifiedFileObjects());
        }
        return s;
    }

    @Override
    public Collection<? extends File> getNewFiles() {
        Set<File> files = new LinkedHashSet<>();
        for (ModificationResult r : results) {
            files.addAll(r.getNewFiles());
        }
        return files;
    }

    @Override
    public void commit() throws IOException {
        for (ModificationResult r : results) {
            r.commit();
        }
    }
}
