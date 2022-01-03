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

package org.netbeans.modules.cnd.apt.impl.structure;

import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;
import org.openide.util.Parameters;

/**
 * implementation of APTFile
 */
public final class APTFileNode extends APTContainerNode 
                                implements APTFile, Serializable {
    private static final long serialVersionUID = -6182803432699849825L;
    private final Kind kind;
    transient private FileSystem fileSystem;
    private final CharSequence path;
    private volatile CharSequence guard = CharSequences.empty();
    transient private boolean tokenized;
    
    /** Copy constructor */
    /**package*/ APTFileNode(APTFileNode orig) {
        super(orig);
        this.kind = orig.kind;
        this.fileSystem = orig.fileSystem;
        this.path = orig.path;
        this.tokenized = false;
        this.guard = orig.guard;
    }
    
    /** Creates a new instance of APTFileNode */
    public APTFileNode(FileSystem fileSystem, CharSequence path, Kind kind) {
        Parameters.notNull("null fileSystem", fileSystem); //NOI18N
        this.kind = kind;
        this.fileSystem = fileSystem;
        this.path = FilePathCache.getManager().getString(path);
        this.guard = TextCache.getManager().getString(guard);
        tokenized = true;
    }

    /**package*/void setGuard(CharSequence guard) {
        this.guard = TextCache.getManager().getString(guard);
    }
    
    @Override
    public final int getType() {
        return APT.Type.FILE;
    }    
    

    @Override
    public int getOffset() {
        return -1;
    }

    @Override
    public int getEndOffset() {
        return -1;
    }
    
    @Override
    public APT getNextSibling() {
        return null;
    }              

    @Override
    public String getText() {
        return "FILE: " + kind + " {" + getPath() + "}"; // NOI18N
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public CharSequence getPath() {
        return path;
    }

    @Override
    public boolean isTokenized() {
        return tokenized;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof APTFileNode)) {
            return false;
        }
        final APTFileNode other = (APTFileNode) obj;
        if (!this.path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + this.path.hashCode();
        return hash;
    }

    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    @Override
    public final void setNextSibling(APT next) {
        assert(false):"Illegal to add siblings to file node"; // NOI18N
    }

    @Override
    public CharSequence getGuardMacro() {
        return guard;
    }

    @Override
    public String toString() {
        return "APTFileNode{" + "kind=" + kind + // NOI18N
                ", fileSystem=" + fileSystem + ", path=" + path + // NOI18N
                ", guard=" + guard + ", tokenized=" + tokenized + '}'; // NOI18N
    }
}
