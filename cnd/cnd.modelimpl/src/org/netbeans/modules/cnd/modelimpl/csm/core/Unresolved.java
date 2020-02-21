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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.CharSequences;


/**
 * Container for all unresolved stuff in the project
 *
 */
public final class Unresolved implements Disposable {

    private static final CharSequence UNRESOLVED = CharSequences.create("$unresolved file$"); // NOI18N)
    private static class IllegalCallException extends RuntimeException {
	IllegalCallException() {
	    super("This method should never be called for Unresolved"); // NOI18N
	}
    }

    public static boolean isUnresolved(Object obj) {
        return obj instanceof UnresolvedClass || obj instanceof UnresolvedFile || obj instanceof UnresolvedNamespace;
    }

    public final class UnresolvedClass extends ClassEnumBase<CsmClass> implements CsmClass {
        private UnresolvedClass(CharSequence name) {
            super(NameHolder.createName(name), unresolvedFile, null);
            initScope(unresolvedNamespace);
        }

	public void register() {
	    // we don't need registering in project here.
	    // so we just register in namespace and in repository
            if (unresolvedNamespace != null) {
                unresolvedNamespace.addDeclaration(this);
            }
	}

        public boolean isTemplate() {
            return false;
        }
        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return Collections.<CsmScopeElement>emptyList();
        }

        @Override
        public Collection<CsmMember> getMembers() {
            return Collections.<CsmMember>emptyList();
        }

        @Override
        public List<CsmFriend> getFriends() {
            return Collections.<CsmFriend>emptyList();
        }

        @Override
        public int getLeftBracketOffset() {
            return 0;
        }

        @Override
        public List<CsmInheritance> getBaseClasses() {
            return Collections.<CsmInheritance>emptyList();
        }

        @Override
        public boolean isValid() {
            return false; // false for dummy class, to allow reconstruct in usage place
        }

        @Override
        public CsmDeclaration.Kind getKind() {
            return CsmClass.Kind.CLASS;
        }

	@Override
	protected CsmUID<CsmClass> createUID() {
	    return UIDUtilities.createUnresolvedClassUID(getName().toString(), getProject());
	}

        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            throw new IllegalCallException();
        }
    }

    private final static class UnresolvedNamespace extends NamespaceImpl {

        private UnresolvedNamespace(ProjectBase project) {
            super(project, null, "$unresolved$","$unresolved$"); // NOI18N
        }

        @Override
        protected void notify(CsmObject obj, NotifyEvent kind) {
            // skip
        }

	@Override
	protected CsmUID<CsmNamespace> createUID() {
	    return UIDUtilities.createUnresolvedNamespaceUID(getProject());
	}


	@Override
	public void write(RepositoryDataOutput output) throws IOException {
	    throw new IllegalCallException();
	}
    }

    private static final String UNRESOLVED_FILE_FAKE_PATH = FileUtil.normalizeFile(CndFileUtils.createLocalFile((System.getProperty("java.io.tmpdir")), "$_UNRESOLVED_CND_MODEL_FILE_5858$")).getAbsolutePath(); // NOI18N
    public final class UnresolvedFile implements CsmFile, CsmIdentifiable, Disposable {

        private UnresolvedFile() {
        }

        @Override
        public String getText(int start, int end) {
            return "";
        }
        @Override
        public String getText() {
            return "";
        }
        @Override
        public List<CsmScopeElement> getScopeElements() {
            return Collections.<CsmScopeElement>emptyList();
        }

        @Override
        public CsmProject getProject() {
            return _getProject();
        }

        private synchronized ProjectBase _getProject() {
            if( projectRef == null ) {
                assert projectUID != null;
                return (ProjectBase)UIDCsmConverter.UIDtoProject(projectUID);
            }
            else {
                return projectRef;
            }
        }

        @Override
        public CharSequence getName() {
            return UNRESOLVED; // NOI18N
        }
        @Override
        public List<CsmInclude> getIncludes() {
            return Collections.<CsmInclude>emptyList();
        }
        @Override
        public List<CsmOffsetableDeclaration> getDeclarations() {
            return Collections.<CsmOffsetableDeclaration>emptyList();
        }
        @Override
        public String getAbsolutePath() {
            return UNRESOLVED_FILE_FAKE_PATH; // NOI18N
        }

        @Override
        public FileObject getFileObject() {
            ProjectBase csmProject = _getProject();
            FileSystem fs = (csmProject == null) ? CndFileUtils.getLocalFileSystem() : csmProject.getFileSystem();
            return InvalidFileObjectSupport.getInvalidFileObject(fs, getAbsolutePath());
        }
        
        @Override
        public boolean isValid() {
            return getProject().isValid();
        }
        @Override
        public void scheduleParsing(boolean wait) {
        }
        @Override
        public boolean isParsed() {
            return true;
        }
        @Override
        public List<CsmMacro> getMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        public Iterator<CsmMacro> getMacros(CsmFilter filter) {
            return getMacros().iterator();
        }

        @Override
        public CsmUID<CsmFile> getUID() {
            if (uid == null) {
                uid = UIDUtilities.createUnresolvedFileUID(this.getProject());
            }
            return uid;
        }

        private CsmUID<CsmFile> uid = null;

        @Override
        public boolean isSourceFile() {
            return false;
        }

        @Override
        public boolean isHeaderFile() {
            return true;
        }

        @Override
        public FileType getFileType() {
            return FileType.UNDEFINED_FILE;
        }

        @Override
        public Collection<CsmErrorDirective> getErrors() {
            return Collections.<CsmErrorDirective>emptyList();
        }

        @Override
        public void dispose() {
            UIDUtilities.disposeUnresolved(uid);
        }
    };

    // only one of projectRef/projectUID must be used (based on USE_UID_TO_CONTAINER)
    private /*final*/ ProjectBase projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;

    // doesn't need Repository Keys
    private final UnresolvedFile unresolvedFile;
    // doesn't need Repository Keys
    private final UnresolvedNamespace unresolvedNamespace;
    // doesn't need Repository Keys
    private final Map<CharSequence, Reference<UnresolvedClass>> dummiesForUnresolved = new ConcurrentHashMap<>();

    public Unresolved(ProjectBase project) {
        this.projectUID = UIDCsmConverter.projectToUID(project);
        this.projectRef = null;
        unresolvedFile = new UnresolvedFile();
        unresolvedNamespace = new UnresolvedNamespace(project);
    }

    @Override
    public void dispose() {
        disposeAll();
        onDispose();
    }

    private synchronized void onDispose() {
        if (this.projectRef == null) {
            // restore container from it's UID
            this.projectRef = (ProjectBase)UIDCsmConverter.UIDtoProject(this.projectUID);
            assert (this.projectRef != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
        }
    }

    private void disposeAll() {
        this.unresolvedFile.dispose();
    }

    public CsmClass getDummyForUnresolved(CharSequence[] nameTokens) {
        return getDummyForUnresolved(getName(nameTokens));
    }
    
    public CsmClass getDummyForUnresolved(CharSequence name) {
        name = NameCache.getManager().getString(name);
        Reference<UnresolvedClass> ref = dummiesForUnresolved.get(name);
        UnresolvedClass cls = ref == null ? null : ref.get();
        if( cls == null ) {
            cls = new UnresolvedClass(name);
            dummiesForUnresolved.put(name, new SoftReference<>(cls));
	    cls.register();
        }
        return cls;
    }

    public CsmNamespace getUnresolvedNamespace() {
        return unresolvedNamespace;
    }

    public CsmFile getUnresolvedFile() {
	return unresolvedFile;
    }

    private CharSequence getName(CharSequence[] nameTokens) {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < nameTokens.length; i++ ) {
            if( i > 0 ) {
                sb.append("::"); // NOI18N
            }
            sb.append(nameTokens[i]);
        }
        return sb;
    }
}
