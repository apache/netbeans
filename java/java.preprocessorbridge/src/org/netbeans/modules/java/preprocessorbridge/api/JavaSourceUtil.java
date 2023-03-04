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

package org.netbeans.modules.java.preprocessorbridge.api;

import java.io.IOException;
import java.util.Collection;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import java.util.Map;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.preprocessorbridge.JavaSourceUtilImplAccessor;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * @author Tomas Zezula
 * Contains utility method to create private copy of javac, used by debugger.jpda
 * @since 1.5
 */
public class JavaSourceUtil {

    /**
     * Helper method used by debugger.jpda to create its own private javac compiler.
     * The caller is responsible for freeing handles holding the compiler.
     * As the caller operates on the private copy of compiler the calls to
     * the JavaSource.runUserActionTask, JavaSource.runModificationTask may no work
     * correctly and shouldn't be used in combination with this method.
     * @since 1.5
     */
    private static final Lookup.Result<JavaSourceUtilImpl> result = Lookup.getDefault().lookupResult(JavaSourceUtilImpl.class);

    private JavaSourceUtil () {}


    public static class Handle {

        private final long id;
        private final Object compilationController;

        private Handle (Object compilaionController, long id) {
            this.compilationController = compilaionController;
            this.id = id;
        }

        public Object getCompilationController () {
            return compilationController;
        }
    }


    /**
     * Creates a handle for the compilation controller, or return an
     * existing one if still valid.
     * <p/>
     * This form attempts to locate controller that governs the specified
     * position in the source. If the position was not handled by java 
     * compilation, returns {@code null}. Use {@code -1} as position value
     * to locate <b>any</b> compilation controller processing the file.
     * <p/>
     * Use this form to locate suitable Controller in a file with several
     * possibly unrelated embeddings, or when working in a specific context, i.e.
     * at the editor's caret position.
     * 
     * @param position position to locate the correct embedding.
     * @param file the source file
     * @param handle existing handle, possibly {@code null}
     * @return handle to the compilation controller, or {@code null}
     * @throws IOException on error
     * @since 1.47
     */
    @NonNull
    public static Handle createControllerHandle (
            @NonNull final FileObject file,
            final int position,
            @NullAllowed final Handle handle) throws IOException {
        Parameters.notNull("file", file);   //NOI18N
        final JavaSourceUtilImpl impl = getSPI();
        assert impl != null;
        final long id = handle == null ? -1 : handle.id;
        final Object[] param = new Object[] {
          handle == null ? null : handle.compilationController
        };
        final long newId = JavaSourceUtilImplAccessor.getInstance().createTaggedCompilationController(impl, file, position, id, param);
        if (newId == id) {
            return handle;
        }
        else {
            return new Handle(param[0], newId);
        }
    }

    /**
     * Creates a handle for the compilation controller, or return an
     * existing one if still valid. If the source file is not a Java
     * MIME type, locates the first java embedding in the file
     * and returns its controller.
     * 
     * @param file the source file
     * @param handle existing handle, possibly {@code null}
     * @return handle to the compilation controller, or {@code null}
     * @throws IOException on error
     */
    @NonNull
    public static Handle createControllerHandle (
            @NonNull final FileObject file,
            @NullAllowed final Handle handle) throws IOException {
        return createControllerHandle(file, -1, handle);
    }
    
    /**
     * Generates a byte code for given class
     * @param srcRoot the source owning root
     * @param file the source file to generate
     * @param content the optional new content of the source file
     * @param diagnostics the optional {@link DiagnosticListener}
     * @return the {@link Map} of binary names to class file data.
     * @throws IOException in case of error
     * @since 1.42
     */
    @NonNull
    public static Map<String,byte[]> generate(
            @NonNull final FileObject srcRoot,
            @NonNull final FileObject file,
            @NullAllowed final CharSequence content,
            @NullAllowed final DiagnosticListener<? super JavaFileObject> diagnostics) throws IOException {
        final JavaSourceUtilImpl impl = getSPI();
        return JavaSourceUtilImplAccessor.getInstance().generate(impl, srcRoot, file, content, diagnostics);
    }

    private static JavaSourceUtilImpl getSPI () {
        Collection<? extends JavaSourceUtilImpl> instances = result.allInstances();
        int size = instances.size();
        assert  size < 2;
        return size == 0 ? null : instances.iterator().next();
    }

}
