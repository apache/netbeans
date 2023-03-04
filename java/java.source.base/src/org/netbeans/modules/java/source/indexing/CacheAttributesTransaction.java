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
package org.netbeans.modules.java.source.indexing;

import java.io.IOException;
import java.net.URL;
import java.util.function.Supplier;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class CacheAttributesTransaction extends TransactionContext.Service {

    private final URL root;
    private final boolean srcRoot;
    private final Supplier<Boolean> allFiles;
    private boolean closed;
    private boolean invalid;

    private CacheAttributesTransaction(
        @NonNull final URL root,
        final boolean srcRoot,
        final Supplier<Boolean> allFiles) {
        this.root = root;
        this.srcRoot = srcRoot;
        this.allFiles = allFiles;
    }    

    static CacheAttributesTransaction create(
            @NonNull final URL root,
            final boolean srcRoot,
            final Supplier<Boolean> allFiles) {
        Parameters.notNull("root", root);   //NOI18N
        return new CacheAttributesTransaction(root, srcRoot, allFiles);
    }

    @Override
    protected void commit() throws IOException {
        closeTx();
        final ClassIndexImpl uq = ClassIndexManager.getDefault().getUsagesQuery(root, false);
        if (uq == null) {
            //Closing
            return;
        }
        if (srcRoot) {
            if (uq.getState() == ClassIndexImpl.State.NEW && uq.getType() != ClassIndexImpl.Type.SOURCE) {
                JavaIndex.setAttribute(root, ClassIndexManager.PROP_SOURCE_ROOT, Boolean.TRUE.toString());
            }
        } else {            
            if (allFiles.get()) {
                JavaIndex.setAttribute(root, ClassIndexManager.PROP_SOURCE_ROOT, Boolean.FALSE.toString());
            }
        }
        if (!invalid) {
            uq.setState(ClassIndexImpl.State.INITIALIZED);
        }
    }

    @Override
    protected void rollBack() throws IOException {
        closeTx();
        //NOP - keep index state as NEW
    }

    void setInvalid(final boolean invalid) {
        this.invalid = invalid;
    }

    private void closeTx() {
        if (closed) {
            throw new IllegalStateException("Already commited or rolled back transaction.");    //NOI18N
        }
        closed = true;
    }
}
