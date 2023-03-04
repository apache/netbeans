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

package org.netbeans.modules.java.preprocessorbridge.spi;

import com.sun.source.tree.ModuleTree;
import java.io.IOException;
import javax.lang.model.element.TypeElement;
import java.util.Map;
import javax.lang.model.element.ModuleElement;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.preprocessorbridge.JavaSourceUtilImplAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 * SPI interface provided by java.source to java.preprocessorbridge, used by JavaSourceUtil
 * @author Tomas Zezula
 * @since 1.5
 */
public abstract class JavaSourceUtilImpl {

    static {
        JavaSourceUtilImplAccessor.setInstance(new MyAccessor());
    }

    private static final String EXPECTED_PACKAGE = "org.netbeans.modules.java.source";  //NOI18N

    protected JavaSourceUtilImpl () {
        super ();
        final String implPackage = this.getClass().getPackage().getName();
        if (!EXPECTED_PACKAGE.equals(implPackage)) {
            throw new IllegalArgumentException ();
        }
    }
    
    protected long createTaggedCompilationController (FileObject file, int position, long currenTag, Object[] out) throws IOException {
        if (position == -1) {
            return createTaggedCompilationController(file, currenTag, out);
        }
        throw new UnsupportedOperationException("Not supported in the registered implementation: " + getClass().getName()); //NOI18N
    }
    

    protected abstract long createTaggedCompilationController (FileObject file, long currenTag, Object[] out) throws IOException;

    @NonNull
    protected Map<String, byte[]> generate(@NonNull FileObject root, @NonNull FileObject file, @NullAllowed CharSequence content, @NullAllowed final DiagnosticListener<? super JavaFileObject> diagnostics) throws IOException {
        throw new UnsupportedOperationException("Not supported in the registered implementation: " + getClass().getName()); //NOI18N
    }

    @CheckForNull
    protected abstract ModuleInfoHandle getModuleInfoHandle(@NonNull Object javaSource) throws IOException;

    public abstract static class ModuleInfoHandle {
        
        @CheckForNull
        public abstract String parseModuleName() throws IOException;

        @CheckForNull
        public abstract ModuleTree parseModule() throws IOException;

        @CheckForNull
        public abstract ModuleElement resolveModule(@NonNull ModuleTree moduleTree) throws IOException;

        @CheckForNull
        public abstract ModuleElement resolveModule(@NonNull String moduleName) throws IOException;
        
        @CheckForNull
        public abstract TypeElement readClassFile() throws IOException;
    }

    private static class MyAccessor extends JavaSourceUtilImplAccessor {

        @Override
        public long createTaggedCompilationController(JavaSourceUtilImpl spi, FileObject fo, int position, long currentTag, Object[] out) throws IOException {
            assert spi != null;
            return spi.createTaggedCompilationController(fo, currentTag, out);
        }

        @Override
        @NonNull
        public Map<String, byte[]> generate(
                @NonNull final JavaSourceUtilImpl spi,
                @NonNull final FileObject srcRoot,
                @NonNull final FileObject file,
                @NullAllowed final CharSequence content,
                @NullAllowed final DiagnosticListener<? super JavaFileObject> diagnostics) throws IOException {
            assert spi != null;
            return spi.generate(srcRoot, file, content, diagnostics);
        }

        @Override
        @CheckForNull
        public ModuleInfoHandle getModuleInfoHandle(
                @NonNull final JavaSourceUtilImpl spi,
                @NonNull final Object javaSource) throws IOException {
            assert spi != null;
            return spi.getModuleInfoHandle(javaSource);
        }
    }
}
