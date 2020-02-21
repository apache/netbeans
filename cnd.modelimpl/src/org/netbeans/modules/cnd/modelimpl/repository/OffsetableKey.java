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
package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.KeyDataPresentation;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * File and offset -based key
 */

/*package*/ abstract class OffsetableKey extends ProjectFileNameBasedKey implements Comparable<OffsetableKey> {

    private final int startOffset;
    private int endOffset = KeyUtilities.NON_INITIALIZED;
    private int hashCode;
    private final CharSequence name;

    protected OffsetableKey(CsmOffsetable obj, CharSequence name) {
        this((FileImpl) obj.getContainingFile(), obj.getStartOffset(), obj.getEndOffset(), name);
    }

    protected OffsetableKey(FileImpl containingFile, int startOffset, CharSequence name) {
        this(containingFile, startOffset, KeyUtilities.NON_INITIALIZED, name);
    }
    
    protected OffsetableKey(FileImpl containingFile, int startOffset, int endOffset, CharSequence name) {
        super(containingFile);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.name = NameCache.getManager().getString(name);
    }

    protected OffsetableKey(KeyDataPresentation presentation) {
        super(presentation);
        this.startOffset = presentation.getStartPresentation();
        this.endOffset = presentation.getEndPresentation();
        this.name = NameCache.getManager().getString(presentation.getNamePresentation());
    }

    abstract char getKind();
    
    /*package-local*/ CharSequence getName() {
        if (name != null && 0 < name.length() && isDigit(name.charAt(0))) {
            return CharSequences.empty();
        }
        return name;
    }

    // to improve performance of Character.isDigit(char)
    private boolean isDigit(char c) {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
        }
        return false;
    }

    /*package-local*/ int getStartOffset() {
        return startOffset;
    }

    /*package-local*/ final int getEndOffset() {
        return endOffset;
    }

    /*package-local*/ final void cacheEndOffset(int endOffset) {
        assert (this.endOffset == KeyUtilities.NON_INITIALIZED || this.endOffset == endOffset) : "end offset is set already to " + this.endOffset;
        this.endOffset = endOffset;
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        aStream.writeInt(this.startOffset);
        aStream.writeInt(this.endOffset);
        assert this.name != null;
        PersistentUtils.writeUTF(name, aStream);
    }

    protected OffsetableKey(RepositoryDataInput aStream) throws IOException {
        super(aStream);
        this.startOffset = aStream.readInt();
        this.endOffset = aStream.readInt();
        this.name = PersistentUtils.readUTF(aStream, NameCache.getManager());
        assert CharSequences.isCompact(name);
    }

    @Override
    public String toString() {
        return name + "[" + getKind() + " " + getStartOffset() + "-" + (getEndOffset() == KeyUtilities.NON_INITIALIZED ? "U" : getEndOffset()) + "] {" + getFileNameSafe() + "; " + getProjectName() + "}"; // NOI18N
    }

        @Override
    public boolean equals(int thisUnitID, Key object, int objectUnitID) {
        if (!super.equals(thisUnitID, object, objectUnitID)) {
            return false;
        }
        OffsetableKey other = (OffsetableKey) object;
        assert CharSequences.isCompact(name);
        assert CharSequences.isCompact(other.name);
        return this.startOffset == other.startOffset &&
                ((this.endOffset == other.endOffset) || 
                 (this.endOffset == KeyUtilities.NON_INITIALIZED || other.endOffset == KeyUtilities.NON_INITIALIZED)) &&
                this.getHandler() == other.getHandler() &&
                this.name.equals(other.name);
    }

    @Override
    public int hashCode(int unitID) {
        return 59*name.hashCode() + 19*startOffset + super.hashCode(unitID);
    }
    
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = hashCode(getUnitId());
        }
        return hashCode;
    }

    @Override
    public int compareTo(OffsetableKey other) {
        if (this == other) {
            return 0;
        }
        assert (getKind() == other.getKind());
        //FUXUP assertion: unit and file tables should be deserialized before files deserialization.
        //instead compare indexes.
        //assert (this.getFileName().equals(other.getFileName()));
        //assert (this.getProjectName().equals(other.getProjectName()));
        assert (this.getUnitId() == other.getUnitId());
        assert (this.fileNameIndex == other.fileNameIndex);
        int ofs1 = this.getStartOffset();
        int ofs2 = other.getStartOffset();
        if (ofs1 == ofs2) {
            return 0;
        } else {
            return (ofs1 - ofs2);
        }
    }

    @Override
    public int getDepth() {
        return super.getDepth() + 2;
    }

    @Override
    public CharSequence getAt(int level) {
        int superDepth = super.getDepth();
        if (level < superDepth) {
            return super.getAt(level);
        } else {
            switch (level - superDepth) {
                case 0:
                    return new String(new char[]{getKind()});
                case 1:
                    return this.name;
                default:

                    throw new IllegalArgumentException("not supported level" + level); // NOI18N
            }
        }
    }

    @Override
    public int getSecondaryDepth() {
        return 2;
    }

    @Override
    public int getSecondaryAt(int level) {
        switch (level) {
            case 0:

                return this.startOffset;
            case 1:

                return this.endOffset;
            default:

                throw new IllegalArgumentException("not supported level" + level); // NOI18N
        }
    }

    @Override
    public final int getStartPresentation() {
        return startOffset;
    }

    @Override
    public final int getEndPresentation() {
        return endOffset;
    }

    @Override
    public final CharSequence getNamePresentation() {
        return name;
    }
}
