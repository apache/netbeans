/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.support;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.openide.util.Parameters;

/**
 * Provides an information about frequencies of type and package usages
 * in given source root or source path.
 * @since 0.97
 * @author Tomas Zezula
 */
public final class ReferencesCount {
    
    private static final Logger LOG = Logger.getLogger(ReferencesCount.class.getName());
    
    private final Object lck = new Object();
    private final Iterable<? extends URL> roots;
    //@GuardedBy("lck")
    private Map<String,Integer> typeFreqs;
    //@GuardedBy("lck")
    private Map<String,Integer> pkgFreqs;
    
    private ReferencesCount(@NonNull final Iterable<? extends URL> roots) {
        this.roots = roots;
    }
    
    /**
     * Returns an estimate of a number of classes on given source path (source root) which are
     * using given type.
     * @param type the type type to find the usage frequency for.
     * @return number of classes using the type.
     */
    public int getTypeReferenceCount(@NonNull final ElementHandle<? extends TypeElement> type) {
        Parameters.notNull("binaryName", type);   //NOI18N
        if (!type.getKind().isClass() &&
            !type.getKind().isInterface() &&
             type.getKind() != ElementKind.OTHER) {
            throw new IllegalArgumentException(type.toString());
        }
        try {
            init();
            final Integer count = typeFreqs.get(SourceUtils.getJVMSignature(type)[0]);
            return count == null ? 0 : count;
        } catch (InterruptedException ie) {
            return 0;
        }
    }
    
    /**
     * Returns an estimate of a number of classes on given source path (source root) which are
     * using given package.
     * @param pkg  the package to find the usage frequency for.
     * @return number of classes using types from given package.
     */
    public int getPackageReferenceCount(@NonNull final ElementHandle<? extends PackageElement> pkg) {
        Parameters.notNull("pkgName", pkg); //NOI18N
        if (pkg.getKind() != ElementKind.PACKAGE) {
            throw new IllegalArgumentException(pkg.toString());
        }
        try {
            init();
            final Integer count = pkgFreqs.get(SourceUtils.getJVMSignature(pkg)[0]);
            return count == null ? 0 : count;
        } catch (InterruptedException ie) {
            return 0;
        }
    }
    
    /**
     * Returns all types used by classes on given source path (source root).
     * @return the used classes
     */
    @NonNull
    public Iterable<? extends ElementHandle<? extends TypeElement>> getUsedTypes() {
        try {
            init();
            return new AsHandlesIterable<String, ElementHandle<TypeElement>>(
                typeFreqs.keySet(),
                new Convertor<String, ElementHandle<TypeElement>>() {
                    @NonNull
                    @Override
                    public ElementHandle<TypeElement> convert(@NonNull final String p) {
                        return ElementHandleAccessor.getInstance().create(ElementKind.OTHER, p);   //FIXME
                    }
                });
        } catch (InterruptedException ie) {
            return Collections.<ElementHandle<TypeElement>>emptySet();
        }
    }
    
    /**
     * Returns all packages used by classes on given source path (source root).
     * @return the used packages
     */
    @NonNull
    public Iterable<? extends ElementHandle<? extends PackageElement>> getUsedPackages() {
        try {
            init();
            return new AsHandlesIterable<String, ElementHandle<PackageElement>>(
                pkgFreqs.keySet(),
                new Convertor<String, ElementHandle<PackageElement>>() {
                    @NonNull
                    @Override
                    public ElementHandle<PackageElement> convert(@NonNull final String p) {
                        return ElementHandleAccessor.getInstance().create(ElementKind.PACKAGE, p);
                    }
                });
        } catch (InterruptedException ie) {
            return Collections.<ElementHandle<PackageElement>>emptySet();
        }
    }
    
    private void init() throws InterruptedException {
        synchronized (lck) {
            if (typeFreqs == null) {
                long st = System.currentTimeMillis();
                final ClassIndexManager cim = ClassIndexManager.getDefault();
                final Map<String,Integer> typef = new HashMap<String, Integer>();
                final Map<String,Integer> pkgf = new HashMap<String, Integer>();
                try {
                    for (URL root : roots) {
                        final ClassIndexImpl ci = cim.getUsagesQuery(root, true);
                        if (ci != null) {
                            ci.getReferencesFrequences(typef, pkgf);
                        } else if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(
                                Level.FINE,
                                "No ClasIndexImpl for root: {0} scan: {1}",  //NOI18N
                                new Object[]{
                                    root,
                                    SourceUtils.isScanInProgress()
                                });
                        }
                    }
                    typeFreqs = Collections.<String,Integer>unmodifiableMap(typef);
                    pkgFreqs = Collections.<String,Integer>unmodifiableMap(pkgf);
                } catch (IOException ioe) {
                    typeFreqs = Collections.<String,Integer>emptyMap();
                    pkgFreqs = Collections.<String,Integer>emptyMap();
                }
                long et = System.currentTimeMillis();
                LOG.log(
                    Level.FINE,
                    "Frequencies calculation time: {0}ms.", //NOI18N
                    (et-st));
            }
        }
        assert typeFreqs != null;
        assert pkgFreqs != null;
    }
    
    private static class AsHandlesIterable<P,R> implements Iterable<R> {
        
        private final Iterable<P> from;
        private final Convertor<P,R> fnc;
        
        private AsHandlesIterable(
            @NonNull Iterable<P> from,
            @NonNull Convertor<P,R> fnc) {
            assert from != null;
            assert fnc != null;
            this.from = from;
            this.fnc = fnc;
        }

        @Override
        public Iterator<R> iterator() {
            return new AsHandlesIterator<P, R>(from.iterator(),fnc);
        }
        
    }
    
    private static class AsHandlesIterator<P,R> implements Iterator<R> {
        
        private final Iterator<P> from;
        private final Convertor<P,R> fnc;
        
        private AsHandlesIterator(
            @NonNull Iterator<P> from,
            @NonNull Convertor<P,R> fnc) {
            assert from != null;
            assert fnc != null;
            this.from = from;
            this.fnc = fnc;
        }

        @Override
        public boolean hasNext() {
            return from.hasNext();
        }

        @Override
        public R next() {
            return fnc.convert(from.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Read only Collection.");   //NOI18N
        }
        
        
    }
    
    /**
     * Creates a {@link ReferencesCount} for source classpath represented by given
     * {@link ClasspathInfo}.
     * @param cpInfo the {@link ClasspathInfo} to create {@link ReferencesCount} for.
     * @return the {@link ReferencesCount}
     */
    @NonNull
    public static ReferencesCount get(@NonNull final ClasspathInfo cpInfo) {
        Parameters.notNull("cpInfo", cpInfo);   //NOI18N
        final List<? extends ClassPath.Entry> scp = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE).entries();
        final List<URL> roots = new ArrayList<URL>(scp.size());
        for (ClassPath.Entry e : scp) {
            roots.add(e.getURL());
        }
        return new ReferencesCount(roots);
    }
    
    
    /**
     * Creates a {@link ReferencesCount} for source root.
     * @param root the root to create {@link ReferencesCount} for.
     * @return the {@link ReferencesCount}
     */
    @NonNull
    public static ReferencesCount get(@NonNull final URL root) {
        Parameters.notNull("cpInfo", root);   //NOI18N
        return new ReferencesCount(Collections.<URL>singleton(root));
    }
}
