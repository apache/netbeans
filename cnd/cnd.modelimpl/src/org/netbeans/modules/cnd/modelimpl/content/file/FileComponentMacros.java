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

package org.netbeans.modules.cnd.modelimpl.content.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.repository.FileMacrosKey;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.openide.util.CharSequences;

/**
 *
 */
public class FileComponentMacros extends FileComponent {

    private final TreeMap<NameSortedKey, CsmUID<CsmMacro>> macros;
    private final ReadWriteLock macrosLock = new ReentrantReadWriteLock();

    // empty stub
    private static final FileComponentMacros EMPTY = new FileComponentMacros() {

        @Override
        public void appendFrom(FileComponentMacros other) {
        }

        @Override
        public void addMacro(CsmMacro macro) {
        }

        @Override
        void put() {
        }
    };

    public static FileComponentMacros empty() {
        return EMPTY;
    }

    FileComponentMacros(FileComponentMacros other, boolean empty) {
        super(other);
        macros = createMacros(empty ? null : other.macros);
    }

    public FileComponentMacros(FileImpl file) {
        super(new FileMacrosKey(file));
        macros = createMacros(null);
    }

    public FileComponentMacros(RepositoryDataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.macros = factory.readNameSortedToUIDMap(input, DefaultCache.getManager());
    }

    // only for EMPTY static field
    private FileComponentMacros() {
        super((org.netbeans.modules.cnd.repository.spi.Key)null);
        macros = createMacros(null);
    }

    void clean() {
        _clearMacros();
        // PUT should be done by FileContent
//        put();
    }

    private void _clearMacros() {
        Collection<CsmUID<CsmMacro>> copy;
        try {
            macrosLock.writeLock().lock();
            copy = new ArrayList<>(macros.values());
            macros.clear();
        } finally {
            macrosLock.writeLock().unlock();
        }
        RepositoryUtils.remove(copy);
    }

    public void addMacro(CsmMacro macro) {
        CsmUID<CsmMacro> macroUID = RepositoryUtils.put(macro);
        NameSortedKey key = new NameSortedKey(macro);
        assert macroUID != null;
        try {
            macrosLock.writeLock().lock();
            macros.put(key, macroUID);
        } finally {
            macrosLock.writeLock().unlock();
        }
    }

    public Collection<CsmMacro> getMacros() {
        Collection<CsmMacro> out;
        try {
            macrosLock.readLock().lock();
            out = UIDCsmConverter.UIDsToMacros(macros.values());
        } finally {
            macrosLock.readLock().unlock();
        }
        return out;
    }

    public Iterator<CsmMacro> getMacros(CsmFilter filter) {
        Iterator<CsmMacro> out;
        try {
            macrosLock.readLock().lock();
            out = UIDCsmConverter.UIDsToMacros(macros.values(), filter);
        } finally {
            macrosLock.readLock().unlock();
        }
        return out;
    }

    public Collection<CsmUID<CsmMacro>> findMacroUids(CharSequence name) {
        Collection<CsmUID<CsmMacro>> uids = new ArrayList<>(2);
        NameSortedKey from = NameSortedKey.getStartKey(name);
        NameSortedKey to = NameSortedKey.getEndKey(name);
        try {
            macrosLock.readLock().lock();
            for (Map.Entry<NameSortedKey, CsmUID<CsmMacro>> entry : macros.subMap(from, to).entrySet()) {
                uids.add(entry.getValue());
            }
        } finally {
            macrosLock.readLock().unlock();
        }
        return uids;
    }

    private TreeMap<NameSortedKey, CsmUID<CsmMacro>> createMacros(TreeMap<NameSortedKey, CsmUID<CsmMacro>> other) {
        if (other != null) {
            return new TreeMap<>(other);
        } else {
            return new TreeMap<>();
        }
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        try {
            macrosLock.readLock().lock();
            factory.writeNameSortedToUIDMap(macros, output, false);
        } finally {
            macrosLock.readLock().unlock();
        }
    }

    public void appendFrom(FileComponentMacros other) {
        try {
            macrosLock.writeLock().lock();
            macros.putAll(other.macros);
        } finally {
            macrosLock.writeLock().unlock();
        }
        // PUT should be done by FileContent
//        put();
    }

    public static final class NameSortedKey implements Comparable<NameSortedKey>, Persistent, SelfPersistent {

        private int start = 0;
        private CharSequence name;

        private NameSortedKey(CsmMacro macro) {
            this(macro.getName(), macro.getStartOffset());
        }

        private NameSortedKey(CharSequence name, int start) {
            this.start = start;
            this.name = NameCache.getManager().getString(name);
        }

        @Override
        public int compareTo(NameSortedKey o) {
            int res = CharSequences.comparator().compare(name, o.name);
            if (res == 0) {
                res = start - o.start;
            }
            return res;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NameSortedKey) {
                NameSortedKey key = (NameSortedKey) obj;
                return compareTo(key)==0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.start;
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "NameSortedKey: " + this.name + "[" + this.start; // NOI18N
        }

        public static NameSortedKey getStartKey(CharSequence name) {
            return new NameSortedKey(name, 0);
        }

        public static NameSortedKey getEndKey(CharSequence name) {
            return new NameSortedKey(name, Integer.MAX_VALUE);
        }

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            output.writeInt(start);
            PersistentUtils.writeUTF(name, output);
        }

        public NameSortedKey(RepositoryDataInput input) throws IOException {
            start = input.readInt();
            name = PersistentUtils.readUTF(input, NameCache.getManager());
        }
    }

}
