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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.parsing.Archive;
import org.netbeans.modules.java.source.util.Iterators;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
abstract class ProxyArchive implements Archive {

    private final Archive[] delegates;

    private ProxyArchive(
            @NonNull final Archive[] delegates) {
        Parameters.notNull("delegates", delegates); //NOI18N
        this.delegates = delegates;
    }

    @Override
    public JavaFileObject getFile(String name) throws IOException {
        for (Archive delegate : delegates) {
            final JavaFileObject jfo = delegate.getFile(name);
            if (jfo != null) {
                return jfo;
            }
        }
        return null;
    }

    @Override
    public URI getDirectory(String dirName) throws IOException {
        for (Archive delegate : delegates) {
            final URI dirURI = delegate.getDirectory(dirName);
            if (dirURI != null) {
                return dirURI;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        for (Archive delegate : delegates) {
            delegate.clear();
        }
    }

    @Override
    public JavaFileObject create(String relativeName, JavaFileFilterImplementation filter) throws UnsupportedOperationException {
        for (Archive delegate : delegates) {
            try {
                return delegate.create(relativeName, filter);
            } catch (UnsupportedOperationException e) {
                //pass
            }
        }
        throw new UnsupportedOperationException("Create operation s not supported by delegates");   //NOI18N
    }

    @Override
    public boolean isMultiRelease() {
        for (Archive delegate : delegates) {
            if (delegate.isMultiRelease()) {
                return true;
            }
        }
        return false;
    }


    @NonNull
    static ProxyArchive createComposite(@NonNull final Archive... delegates) {
        return new Composite(delegates);
    }

    @NonNull
    static ProxyArchive createAdditionalPackages(@NonNull final Archive... delegates) {
        return new AddPkgs(delegates);
    }

    private static final class Composite extends ProxyArchive {

        Composite(@NonNull final Archive[] delegates) {
            super(delegates);
        }

        @NonNull
        @Override
        public Iterable<JavaFileObject> getFiles(
                @NonNull final String folderName,
                @NullAllowed final ClassPath.Entry entry,
                @NullAllowed final Set<JavaFileObject.Kind> kinds,
                @NullAllowed final JavaFileFilterImplementation filter,
                final boolean recursive) throws IOException {
            final Collection<Iterable<JavaFileObject>> collector = new ArrayList<>();
            for (Archive delegate : getDelegates(this)) {
                final Iterable<JavaFileObject> it = delegate.getFiles(folderName, entry, kinds, filter, recursive);
                if (!isEmpty(it)) {
                    collector.add(it);
                }
            }
            return Iterators.chained(collector);
        }
    }

    private static final class AddPkgs extends ProxyArchive {
        AddPkgs(@NonNull final Archive[] delegates) {
            super(delegates);
        }

        @NonNull
        @Override
        public Iterable<JavaFileObject> getFiles(
                @NonNull final String folderName,
                @NullAllowed final ClassPath.Entry entry,
                @NullAllowed final Set<JavaFileObject.Kind> kinds,
                @NullAllowed final JavaFileFilterImplementation filter,
                final boolean recursive) throws IOException {
            for (Archive delegate : getDelegates(this)) {
                final Iterable<JavaFileObject> it = delegate.getFiles(folderName, entry, kinds, filter, recursive);
                if (!isEmpty(it)) {
                    return it;
                }
            }
            return Collections.<JavaFileObject>emptyList();
        }
    }

    @NonNull
    private static Archive[] getDelegates(@NonNull final ProxyArchive pa) {
        return pa.delegates;
    }

    private static boolean isEmpty(@NonNull final Iterable<? extends JavaFileObject> it) {
        if (it instanceof Collection) {
            return ((Collection)it).isEmpty();
        } else {
            return !it.iterator().hasNext();
        }
    }

}
