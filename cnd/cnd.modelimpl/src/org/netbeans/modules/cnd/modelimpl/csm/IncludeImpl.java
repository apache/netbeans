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
