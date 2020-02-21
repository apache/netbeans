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
