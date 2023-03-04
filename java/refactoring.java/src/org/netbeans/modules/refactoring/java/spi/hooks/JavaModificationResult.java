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
package org.netbeans.modules.refactoring.java.spi.hooks;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.refactoring.spi.ModificationResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class JavaModificationResult implements ModificationResult {

    public final org.netbeans.api.java.source.ModificationResult delegate;
    
    public JavaModificationResult(org.netbeans.api.java.source.ModificationResult r) {
        this.delegate = r;
    }

    @Override
    public Collection<? extends FileObject> getModifiedFileObjects() {
        return delegate.getModifiedFileObjects();
    }

    @Override
    public Collection<? extends File> getNewFiles() {
        return delegate.getNewFiles();
    }

    @Override
    public void commit() throws IOException {
        delegate.commit();
    }

    @Override
    public String getResultingSource(FileObject file) throws IOException, IllegalArgumentException {
        return delegate.getResultingSource(file);
    }
}
