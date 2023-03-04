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

package org.netbeans.modules.java.preprocessorbridge;

import java.io.IOException;
import java.util.Map;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class JavaSourceUtilImplAccessor {

    private static volatile JavaSourceUtilImplAccessor impl;

    public static void setInstance (final JavaSourceUtilImplAccessor _impl) {
        assert _impl != null;
        impl = _impl;
    }

    public static synchronized JavaSourceUtilImplAccessor getInstance () {
        if (impl == null) {
            try {
                Class.forName(JavaSourceUtilImpl.class.getName(), true, JavaSourceUtilImpl.class.getClassLoader());
            } catch (ClassNotFoundException cnfe) {
                Exceptions.printStackTrace(cnfe);
            }
        }

        return impl;
    }

    public abstract long createTaggedCompilationController (JavaSourceUtilImpl spi, FileObject fo, int position, long currentTag, Object[] out) throws IOException;

    @NonNull
    public abstract Map<String,byte[]> generate(
            @NonNull JavaSourceUtilImpl spi,
            @NonNull final FileObject srcRoot,
            @NonNull final FileObject file,
            @NullAllowed CharSequence content,
            @NullAllowed final DiagnosticListener<? super JavaFileObject> diagnostics) throws IOException;

    @CheckForNull
    public abstract JavaSourceUtilImpl.ModuleInfoHandle getModuleInfoHandle(@NonNull JavaSourceUtilImpl spi, @NonNull Object javaSource) throws IOException;
}
