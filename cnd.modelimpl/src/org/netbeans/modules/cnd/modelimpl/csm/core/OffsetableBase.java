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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import static org.netbeans.modules.cnd.apt.utils.APTUtils.NOT_AN_EXPANDED_TOKEN;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.CsmObjectBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.OffsetableAST;
import org.netbeans.modules.cnd.modelimpl.parser.TokenBasedAST;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Base class for CsmOffsetable
 */
public abstract class OffsetableBase implements CsmOffsetable, Disposable, CsmValidable {
    private final CsmFile fileRef;// we keep ref to file, because it owns disposing flag from project
    private boolean isValid = true;
    
    private final int startPosition;
    private final int endPosition;
    
    protected OffsetableBase(CsmOffsetable pos) {
        this(pos.getContainingFile(), 
                pos.getStartOffset(),
                pos.getEndOffset());      
    }

    protected OffsetableBase(CsmFile file, int start, int end) {
        // Parameters.notNull("file can not be null", file); // NOI18N
        this.fileRef = file;
        if(end < start) {
            if(CndUtils.isDebugMode()) {            
                CndUtils.assertTrueInConsole(false, "end < start for " + ((file != null)?file.getAbsolutePath():"null file") + ":[" + start + "-" + end + "]"); // NOI18N
            }
            end = start;
        }
        CsmUID<CsmFile> fileUID = UIDCsmConverter.fileToUID(file);
        this.startPosition = PositionManager.createPositionID(fileUID, start, PositionManager.Position.Bias.FOWARD);
        this.endPosition = PositionManager.createPositionID(fileUID, end, PositionManager.Position.Bias.BACKWARD);
    }

    @Override
    final public int getStartOffset() {
        return PositionManager.getOffset(fileRef, startPosition);
    }
    
    @Override
    final public int getEndOffset() {
        return endPosition != 0 ? PositionManager.getOffset(fileRef, endPosition) : PositionManager.getOffset(fileRef, startPosition);
    }

    @Override
    public final Position getStartPosition() {
        return PositionManager.getPosition(fileRef, startPosition);
    }
    
    @Override
    public final Position getEndPosition() {
        return PositionManager.getPosition(fileRef, endPosition);
    }
    
    public static int getStartOffset(AST node) {
        if( node != null ) {
            OffsetableAST csmAst = AstUtil.getFirstOffsetableAST(node);
            if( csmAst != null ) {
                return csmAst.getOffset();
            }
        }
        return 0;
    }
    
    public static int getMacroStartMarker(AST node) {
        if (node != null) {
            TokenBasedAST csmAst = AstUtil.getFirstTokenBasedAST(node);
            if (csmAst != null && APTUtils.isMacroExpandedToken(csmAst.getToken())) {
                return APTUtils.getExpandedTokenMarker((APTToken) csmAst.getToken());
            }
        }
        return NOT_AN_EXPANDED_TOKEN;
    }

    public static int getEndOffset(AST node) {
        if( node != null ) {
            AST lastChild = AstUtil.getLastChildRecursively(node);
            if(lastChild.getType() != Token.EOF_TYPE && lastChild instanceof OffsetableAST) {
                return ((OffsetableAST) lastChild).getEndOffset();
            } else {
                // #error directive broke parsing
                // end offset should not be < start one
                lastChild = AstUtil.getLastNonEOFChildRecursively(node);
                if( lastChild instanceof OffsetableAST ) {
                    return ((OffsetableAST) lastChild).getEndOffset();
                }
            }
        }
        return 0;
    }
    
    @Override
    public CsmFile getContainingFile() {
        return this.fileRef;
    }

    @Override
    public boolean isValid() {
        return isValid && CsmBaseUtilities.isValid(this.fileRef);
    }
    
    @Override
    public CharSequence getText() {
        CsmFile containingFile = getContainingFile();
        if (containingFile != null) {
            return containingFile.getText(getStartOffset(), getEndOffset());
        } else {
            return ""; // NOI18N
        }
    }

    @Override
    public void dispose() {
        onDispose();
    }
    
    private synchronized void onDispose() {
        this.isValid = false;
    }    

    public void write(RepositoryDataOutput output) throws IOException {
        output.writeInt(startPosition);
        output.writeInt(endPosition);
        CsmUID<CsmFile> fileUID = UIDCsmConverter.fileToUID(fileRef);
        // not null UID
        assert fileUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(fileUID, output);
    }
    
    protected OffsetableBase(RepositoryDataInput input) throws IOException {
        startPosition = input.readInt();
        endPosition = input.readInt();

        CsmUID<CsmFile> fileUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert fileUID != null;
        this.fileRef = UIDCsmConverter.UIDtoFile(fileUID);
        this.isValid = (this.fileRef != null);
    }

    protected OffsetableBase(CsmFile containingFile, RepositoryDataInput input) throws IOException {
        // must be in sync with the above constructor
        startPosition = input.readInt();
        endPosition = input.readInt();

        CsmUID<CsmFile> fileUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert fileUID != null;
        this.fileRef = containingFile;
        this.isValid = (this.fileRef != null);
    }
    
    // test trace method
    protected String getOffsetString() {
        return  "[ " + getStartPosition() + " - " + getEndPosition() + " ]"; // NOI18N
    }

    protected CharSequence getPositionString() {
        StringBuilder sb = new StringBuilder(); 
        sb.append('[');
        CsmFile containingFile = this.fileRef;
        if (containingFile == null) {
            sb.append(" NO CONTAINER ");// NOI18N
        } else {
            sb.append(containingFile.getName());
        }
        sb.append(' ');
        Position pos;
        pos = getStartPosition();
        sb.append(pos.getLine());
        sb.append(':');
        sb.append(pos.getColumn());
        sb.append('-');
        pos = getEndPosition();
        sb.append(pos.getLine());
        sb.append(':');
        sb.append(pos.getColumn());
        sb.append(']');
        return sb;
    }

    @Override
    public String toString() {
        return getOffsetString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OffsetableBase other = (OffsetableBase) obj;
        if ((this.fileRef == null || !this.fileRef.equals(other.fileRef))) {
            return false;
        }
        if (this.startPosition != other.startPosition) {
            return false;
        }
        if (this.endPosition != other.endPosition) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.fileRef != null ? this.fileRef.hashCode() : 0);
        hash = 47 * hash + this.startPosition;
        hash = 47 * hash + this.endPosition;
        return hash;
    }
    
    
    public static abstract class OffsetableBuilder implements CsmObjectBuilder {
        
        private CsmFile file;
        private FileContent fileContent;
        private int startOffset = -1;
        private int endOffset = -1;

        public OffsetableBuilder() {
        }

        protected OffsetableBuilder(OffsetableBuilder builder) {
            file = builder.file;
            fileContent = builder.fileContent;
            startOffset = builder.startOffset;
            endOffset = builder.endOffset;
        }
        
        public void setFile(CsmFile file) {
            this.file = file;
            this.fileContent = ((FileImpl)file).getParsingFileContent();
        }
        
        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }
        
        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }

        public CsmFile getFile() {
            return file;
        }

        public FileContent getFileContent() {
            return fileContent;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }
        
        @Override
        public String toString() {
            return "[" + getStartOffset() + ":" + getEndOffset() + "]"; // NOI18N
        }
    }    
}
