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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.content.project.GraphContainer;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableIdentifiableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.FileNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * Implements CsmInclude
 */
public final class IncludeImpl extends OffsetableIdentifiableBase<CsmInclude> implements CsmInclude {
    private final CharSequence name;
    private final boolean system;
    private final boolean recursive;
    private final short includeDirectiveIndex;
    
    private CsmUID<CsmFile> includeFileUID;
    
    private IncludeImpl(String name, boolean system, boolean recursive, 
            CsmFile includeFile, CsmFile containingFile, 
            int startOffset, int endOffset, short includeDirectiveIndex) {
        super(containingFile, startOffset, endOffset);
        this.name = FileNameCache.getManager().getString(name);
        this.system = system;
        this.recursive = recursive;
        this.includeDirectiveIndex = includeDirectiveIndex;
        this.includeFileUID = UIDCsmConverter.fileToUID(includeFile);
        assert (includeFileUID != null || includeFile == null) : "got " + includeFileUID + " for " + includeFile;
    }

    public static IncludeImpl create(String name, boolean system, boolean recursive, 
            CsmFile includeFile, CsmFile containingFile, 
            int startOffset, int endOffset, int includeDirectiveIndex) {
        assert includeDirectiveIndex <= Short.MAX_VALUE;
        return new IncludeImpl(name, system, recursive, includeFile, containingFile, startOffset, endOffset, (short)includeDirectiveIndex);
    }
    
    @Override
    public CsmFile getIncludeFile() {
        return _getIncludeFile();
    }

    @Override
    public IncludeState getIncludeState() {
        if (recursive) {
            return IncludeState.Recursive;
        } if (getIncludeFile() == null) {
            return IncludeState.Fail;
        }
        return IncludeState.Success;
    }

    @Override
    public CharSequence getIncludeName() {
        return name;
    }

    @Override
    public boolean isSystem() {
        return system;
    }
    
    @Override
    public String toString() {
        char beg = isSystem() ? '<' : '"';
        char end = isSystem() ? '>' : '"';
        String error = "";
        if (getContainingFile() == null) {
            error = "<NO CONTAINER INFO> "; // NOI18N
        }
        IncludeState includeState = getIncludeState();
        String state = "";
        if (includeState == IncludeState.Recursive) {
            state = " <RECURSIVE inclusion>";// NOI18N
        } else if (includeState == IncludeState.Fail) {
            state = " <FAILED inclusion>";// NOI18N
        }
        return error + beg + getIncludeName() + end + state + 
                " [" + getStartPosition() + "-" + getEndPosition() + "]"; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof IncludeImpl)) {
            retValue = false;
        } else {
            IncludeImpl other = (IncludeImpl)obj;
            retValue = IncludeImpl.equals(this, other);
        }
        return retValue;
    }
    
    private static boolean equals(IncludeImpl one, IncludeImpl other) {
        // compare only name, type and start offset
        return (CharSequences.comparator().compare(one.getIncludeName(),other.getIncludeName()) == 0) &&
                (one.system == other.system) && 
                (one.getStartOffset() == other.getStartOffset());
    }
    
    @Override
    public int hashCode() {
        int retValue = 17*(isSystem() ? 1 : -1);
        retValue = 31*retValue + getStartOffset();
        retValue = 31*retValue + getIncludeName().hashCode();
        return retValue;
    }

    private CsmFile _getIncludeFile() {
        CsmFile file = UIDCsmConverter.UIDtoFile(includeFileUID);
        if (file == null && includeFileUID != null) {
            // include file was removed
            includeFileUID = null;
        }
        if (TraceFlags.NEED_TO_TRACE_UNRESOLVED_INCLUDE) {
            if (file == null && "yes".equals(System.getProperty("cnd.modelimpl.trace.trace_now"))){ //NOI18N
                CsmFile container = getContainingFile();
                if (container != null){
                    CsmProject prj = container.getProject();
                    if (prj instanceof ProjectImpl){
                        System.out.println("File "+container.getAbsolutePath()); // NOI18N
                        ProjectImpl impl = (ProjectImpl) prj;
                        boolean find = false;
                        GraphContainer graph = CsmCorePackageAccessor.get().getGraph(impl);
                        for(CsmFile top : graph.getTopParentFiles(container).getCompilationUnits()){
                            if (container != top) {
                                System.out.println("  icluded from "+top.getAbsolutePath()); //NOI18N
                                find = true;
                            }
                        }
                        if (!find){
                            System.out.println("  there are no files included the file"); //NOI18N
                        }
                    }
                }
            }
        }
        return file;
    }

    @Override
    protected CsmUID<CsmInclude> createUID() {
        return UIDUtilities.createIncludeUID(this);
    }
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        output.writeBoolean(this.system);
        output.writeBoolean(this.recursive);
        output.writeShort(includeDirectiveIndex);
        UIDObjectFactory.getDefaultFactory().writeUID(this.includeFileUID, output);
    }

    public IncludeImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, FileNameCache.getManager());
        assert this.name != null;
        this.system = input.readBoolean();
        this.recursive = input.readBoolean();
        this.includeDirectiveIndex = input.readShort();
        this.includeFileUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }

    public int getIncludeDirectiveIndex() {
        return this.includeDirectiveIndex;
    }
}
