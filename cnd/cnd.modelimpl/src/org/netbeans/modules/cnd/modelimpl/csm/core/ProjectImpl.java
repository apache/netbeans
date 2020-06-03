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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Project implementation
 */
public final class ProjectImpl extends ProjectBaseWithEditing {

    private static void cleanProjectRepository(NativeProject platformProject) {
        Key key = createProjectKey(platformProject);
        RepositoryUtils.closeUnit(key, null, true);
    }

    private static Key createProjectKey(NativeProject platfProj) {
         return KeyUtilities.createProjectKey(platfProj);
    }

    private static ProjectBase readProjectInstance(ModelImpl model, NativeProject platformProject, String name) {
        return readInstance(model, createProjectKey(platformProject), platformProject, name);
    }

    private ProjectImpl(ModelImpl model, NativeProject platformProject, CharSequence name) {
        super(model, platformProject.getFileSystem(), (Object) platformProject, name, createProjectKey(platformProject));
    // RepositoryUtils.put(this);
    }

    public static ProjectImpl createInstance(ModelImpl model, NativeProject platformProject, String name) {
        ProjectBase instance = null;
        if (TraceFlags.PERSISTENT_REPOSITORY) {
            try {
                instance = readProjectInstance(model, platformProject, name);
            } catch (Exception e) {
                // just report to console;
                // the code below will create project "from scratch"
                DiagnosticExceptoins.register(e);
                cleanProjectRepository(platformProject);
            }
        }
        if (instance != null && !(instance instanceof ProjectImpl)) {
            DiagnosticExceptoins.register(new IllegalStateException(
                    "Expected " + ProjectImpl.class.getName() + //NOI18N
                    " but restored from repository " + instance.getClass().getName())); //NOI18N
            cleanProjectRepository(platformProject);
            instance = null;
        }
        if (instance == null) {
            if (CsmModelAccessor.isModelAlive()) {
                instance = new ProjectImpl(model, platformProject, name);
            }
        }
        return (ProjectImpl) instance;
    }

    @Override
    protected Collection<Key> getLibrariesKeys() {
        List<Key> res = new ArrayList<>();
        assert (getPlatformProject() instanceof NativeProject) : "Expected NativeProject, got " + getPlatformProject();
        for (NativeProject nativeLib : ((NativeProject) getPlatformProject()).getDependences()) {
            final Key key = createProjectKey(nativeLib);
            res.add(key);
        }
        // Last dependent project is common library.
        //final Key lib = KeyUtilities.createProjectKey("/usr/include"); // NOI18N
        //if (lib != null) {
        //    res.add(lib);
        //}
        for (CsmUID<CsmProject> library : getLibraryManager().getLirariesKeys(getUID())) {
            res.add(RepositoryUtils.UIDtoKey(library));
        }
        return res;
    }


    @Override
    protected final ParserQueue.Position getIncludedFileParserQueuePosition() {
        return ParserQueue.Position.HEAD;
    }

    public
    @Override
    ProjectBase findFileProject(CharSequence absPath, boolean waitFilesCreated) {
        ProjectBase retValue = super.findFileProject(absPath, waitFilesCreated);
        // trick for tracemodel. We should accept all not registered files as well, till it is not system one.
        if (retValue == null && ParserThreadManager.instance().isStandalone()) {
            retValue = absPath.toString().startsWith("/usr") ? retValue : this; // NOI18N
        }
        return retValue;
    }

    @Override
    public boolean isArtificial() {
        return false;
    }

    @Override
    public NativeFileItem getNativeFileItem(CsmUID<CsmFile> file) {
        return nativeFiles.getNativeFileItem(file);
    }

    @Override
    protected void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem) {
        nativeFiles.putNativeFileItem(file, nativeFileItem);
    }

    @Override
    protected NativeFileItem removeNativeFileItem(CsmUID<CsmFile> file) {
        return nativeFiles.removeNativeFileItem(file);
    }

    @Override
    protected void clearNativeFileContainer() {
        nativeFiles.clear();
    }
    private final NativeFileContainer nativeFiles = new NativeFileContainer();

    @Override
    protected void onDispose() {
        super.onDispose();
        nativeFiles.clear();
        projectRoots.clear();
    }

    private final SourceRootContainer projectRoots = new SourceRootContainer(false);
    @Override
    protected SourceRootContainer getProjectRoots() {
        return projectRoots;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    public
    @Override
    void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        // we don't need this since ProjectBase persists fqn
        //UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        //aFactory.writeUID(getUID(), aStream);
        getLibraryManager().writeProjectLibraries(getUID(), aStream);
    }

    public ProjectImpl(RepositoryDataInput input) throws IOException {
        super(input);
        // we don't need this since ProjectBase persists fqn
        //UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        //CsmUID uid = aFactory.readUID(input);
        //LibraryManager.getInsatnce().read(uid, input);
        getLibraryManager().readProjectLibraries(getUID(), input);
    //nativeFiles = new NativeFileContainer();
    }
}
