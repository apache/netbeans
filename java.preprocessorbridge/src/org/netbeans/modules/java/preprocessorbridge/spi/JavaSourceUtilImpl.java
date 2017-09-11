/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

    public static abstract class ModuleInfoHandle {
        
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
