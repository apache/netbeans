/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
