/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.implspi;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SourceFactory {

    private static final SourceFactory DEFAULT_SOURCE_FACTORY = new DefaultSourceFactory();

    @NonNull
    public static SourceFactory getDefault() {
        SourceFactory sf = Lookup.getDefault().lookup(SourceFactory.class);
        if (sf == null) {
            sf = DEFAULT_SOURCE_FACTORY;
        }
        return sf;
    }

    @CheckForNull
    public abstract Source createSource(
            @NonNull FileObject fo,
            @NonNull String mimeType,
            @NonNull Lookup contents);

    @CheckForNull
    public abstract Source getSource(@NonNull FileObject fo);

    @CheckForNull
    public abstract Source removeSource(@NonNull FileObject fo);

    protected final Source newSource(
            @NonNull final FileObject fo,
            @NonNull final String mimeType,
            @NonNull final Lookup context) {
        return SourceAccessor.getINSTANCE().create(fo, mimeType, context);
    }

    private static class DefaultSourceFactory extends SourceFactory {

        private final Map<FileObject, Reference<Source>> instances = new WeakHashMap<>();

        @Override
        public Source createSource(
                @NonNull FileObject file,
                @NonNull String mimeType,
                @NonNull Lookup context) {
            Parameters.notNull("file", file);   //NOI18N
            Parameters.notNull("mimeType", mimeType);   //NOI18N
            Parameters.notNull("context", context);   //NOI18N
            final Reference<Source> sourceRef = instances.get(file);
            Source source = sourceRef == null ? null : sourceRef.get();
            if (source == null || !mimeType.equals(source.getMimeType())) {
                source = newSource(file, mimeType, context);
                instances.put(file, new WeakReference<>(source));
            }
            return source;
        }

        @Override
        public Source getSource(@NonNull final FileObject file) {
            Parameters.notNull("file", file);   //NOI18N
            final Reference<Source> ref = instances.get(file);
            return ref == null ? null : ref.get();
        }

        @Override
        public Source removeSource(@NonNull final FileObject file) {
            Parameters.notNull("file", file);   //NOI18N
            final Reference<Source> ref = instances.remove(file);
            return ref == null ? null : ref.get();
        }
    }
}
