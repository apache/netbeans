/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
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
