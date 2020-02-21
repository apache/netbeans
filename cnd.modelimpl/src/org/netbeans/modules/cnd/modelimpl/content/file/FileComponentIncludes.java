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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.content.file;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.repository.FileIncludesKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public class FileComponentIncludes extends FileComponent {
    private Set<CsmUID<CsmInclude>> includes = createIncludes();
    private final Set<CsmUID<CsmInclude>> brokenIncludes = new LinkedHashSet<>(0);
    private final ReadWriteLock includesLock = new ReentrantReadWriteLock();

    // empty stub
    private static final FileComponentIncludes EMPTY = new FileComponentIncludes() {

        @Override
        public void appendFrom(FileComponentIncludes fileIncludes) {
        }

        @Override
        boolean addInclude(IncludeImpl includeImpl, boolean broken) {
            return false;
        }

        @Override
        void put() {
        }
    };

    public static FileComponentIncludes empty() {
        return EMPTY;
    }

    FileComponentIncludes(FileComponentIncludes other, boolean empty) {
        super(other);
        if (!empty) {
            try {
                other.includesLock.readLock().lock();
                includes.addAll(other.includes);
                brokenIncludes.addAll(other.brokenIncludes);
            } finally {
                other.includesLock.readLock().unlock();
            }
        }
    }

    public FileComponentIncludes(FileImpl file) {
        super(new FileIncludesKey(file));
    }

    public FileComponentIncludes(RepositoryDataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.readUIDCollection(this.includes, input);
        factory.readUIDCollection(this.brokenIncludes, input);
    }

    // only for EMPTY static field
    private FileComponentIncludes() {
        super((org.netbeans.modules.cnd.repository.spi.Key)null);
    }

    void clean() {
        _clearIncludes();
        // PUT have to be done by owner
//        put();
    }

    private void _clearIncludes() {
        try {
            includesLock.writeLock().lock();
            RepositoryUtils.remove(includes);
            brokenIncludes.clear();
            includes = createIncludes();
        } finally {
            includesLock.writeLock().unlock();
        }
    }

    boolean addInclude(IncludeImpl includeImpl, boolean broken) {
        CsmUID<CsmInclude> inclUID = RepositoryUtils.put((CsmInclude)includeImpl);
        assert inclUID != null;
        try {
            includesLock.writeLock().lock();
            includes.add(inclUID);
            if (broken) {
                brokenIncludes.add(inclUID);
            } else {
                brokenIncludes.remove(inclUID);
            }
        } finally {
            includesLock.writeLock().unlock();
        }
        // PUT have to be done by owner
//        put();
        return !brokenIncludes.isEmpty();
    }

    public Collection<CsmInclude> getIncludes() {
        Collection<CsmInclude> out;
        try {
            includesLock.readLock().lock();
            out = UIDCsmConverter.UIDsToIncludes(includes);
        } finally {
            includesLock.readLock().unlock();
        }
        return out;
    }

    public Iterator<CsmInclude> getIncludes(CsmFilter filter) {
        Iterator<CsmInclude> out;
        try {
            includesLock.readLock().lock();
            out = UIDCsmConverter.UIDsToIncludes(includes, filter);

        } finally {
            includesLock.readLock().unlock();
        }
        return out;
    }

    public Collection<CsmInclude> getBrokenIncludes() {
        Collection<CsmInclude> out;
        try {
            includesLock.readLock().lock();
            out = UIDCsmConverter.UIDsToIncludes(brokenIncludes);
        } finally {
            includesLock.readLock().unlock();
        }
        return out;
    }

    private Set<CsmUID<CsmInclude>> createIncludes() {
        return new TreeSet<>(UID_START_OFFSET_COMPARATOR);
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        try {
            includesLock.readLock().lock();
            factory.writeUIDCollection(includes, output, false);
            factory.writeUIDCollection(brokenIncludes, output, false);
        } finally {
            includesLock.readLock().unlock();
        }
    }

    private static final Comparator<CsmUID<?>> UID_START_OFFSET_COMPARATOR = new Comparator<CsmUID<?>>() {

        @SuppressWarnings("unchecked")
        @Override
        public int compare(CsmUID<?> o1, CsmUID<?> o2) {
            if (o1 == o2) {
                return 0;
            }
            Comparable<CsmUID> i1 = (Comparable<CsmUID>) o1;
            assert i1 != null;
            return i1.compareTo(o2);
        }
    };

    public void appendFrom(FileComponentIncludes other) {
        try {
            includesLock.writeLock().lock();
            try {
                other.includesLock.readLock().lock();
                for (CsmUID<CsmInclude> csmUID : other.includes) {
                    includes.add(csmUID);
                    brokenIncludes.remove(csmUID);
                }
                for (CsmUID<CsmInclude> csmUID : other.brokenIncludes) {
                    if (!includes.contains(csmUID)) {
                        brokenIncludes.add(csmUID);
                    }
                }
            } finally {
                other.includesLock.readLock().unlock();
            }
        } finally {
            includesLock.writeLock().unlock();
        }
        // PUT have to be done by owner
//        put();
    }

}
