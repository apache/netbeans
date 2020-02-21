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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.repository.FileInstantiationsKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Instantiations are not created during parsing process.
 * And they do not have container that will dispose then on reparse action.
 * So this container is created to solve this issue.
 *
 */
public class FileComponentInstantiations extends FileComponent {

    private Set<CsmUID<CsmInstantiation>> instantiations = createInstantiations();
    private final ReadWriteLock instantiationsLock = new ReentrantReadWriteLock();

    // empty stub
    private static final FileComponentInstantiations EMPTY = new FileComponentInstantiations() {

        @Override
        public void addInstantiation(CsmInstantiation inst) {
        }

        @Override
        void put() {
        }
    };

    public static FileComponentInstantiations empty() {
        return EMPTY;
    }

    FileComponentInstantiations(FileComponentInstantiations other, boolean empty) {
        super(other);
        if (!empty) {
            try {
                other.instantiationsLock.readLock().lock();
                instantiations.addAll(other.instantiations);
            } finally {
                other.instantiationsLock.readLock().unlock();
            }
        }
    }

    public FileComponentInstantiations(FileImpl file) {
        super(new FileInstantiationsKey(file));
    }

    public FileComponentInstantiations(RepositoryDataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.readUIDCollection(this.instantiations, input);
    }

    // only for EMPTY static field
    private FileComponentInstantiations() {
        super((org.netbeans.modules.cnd.repository.spi.Key)null);
    }

    void clean() {
        _clearInstantiations();
        // PUT should be done by FileContent
//        put();
    }

    private void _clearInstantiations() {
        try {
            instantiationsLock.writeLock().lock();
            RepositoryUtils.remove(instantiations);
            instantiations = createInstantiations();
        } finally {
            instantiationsLock.writeLock().unlock();
        }
    }

    public void addInstantiation(CsmInstantiation inst) {
        // TODO: is it safe to put smth into repository directly?
        CsmUID<CsmInstantiation> instUID = RepositoryUtils.put(inst);
        assert instUID != null;
        try {
            instantiationsLock.writeLock().lock();
            instantiations.add(instUID);
        } finally {
            instantiationsLock.writeLock().unlock();
        }
        // TODO: PUT should be done by FileContent?
        put();
    }

    private Set<CsmUID<CsmInstantiation>> createInstantiations() {
        return new HashSet<>();
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        try {
            instantiationsLock.readLock().lock();
            factory.writeUIDCollection(instantiations, output, false);
        } finally {
            instantiationsLock.readLock().unlock();
        }
    }
}
