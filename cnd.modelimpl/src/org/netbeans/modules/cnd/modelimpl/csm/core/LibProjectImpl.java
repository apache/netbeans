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

import java.io.*;
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 */
public final class LibProjectImpl extends ProjectBaseWithEditing {

    private final CharSequence includePath;
    private final SourceRootContainer projectRoots = new SourceRootContainer(true);

    private static void cleanLibraryRepository(FileSystem fs, CharSequence platformProject, boolean articicial, int storageID) {
        Key key = createLibraryProjectKey(fs, platformProject, storageID);
        RepositoryUtils.closeUnit(key, null, true);
    }

    private static Key createLibraryProjectKey(FileSystem fs, CharSequence  platfProj, int storageID) {
        return KeyUtilities.createLibraryProjectKey(KeyUtilities.createUnitDescriptor(platfProj, fs), storageID);
    }

    private static ProjectBase readLibraryInstance(ModelImpl model, FileSystem fs, CharSequence platformProject, CharSequence name, int storageID) {
        ProjectBase instance = readInstance(model, createLibraryProjectKey(fs, platformProject, storageID), platformProject, name);
        return instance;
    }

    
    private LibProjectImpl(ModelImpl model, FileSystem fs, CharSequence includePathName, int storageID) {
        super(model, fs, (Object) includePathName, includePathName, createLibraryProjectKey(fs, includePathName, storageID));
        this.includePath = FilePathCache.getManager().getString(includePathName);
        this.projectRoots.fixFolder(includePathName);
        assert this.includePath != null;
    }

    public static LibProjectImpl createInstance(ModelImpl model, FileSystem fs, CharSequence includePathName, int storageID) {
        ProjectBase instance = null;
        assert includePathName != null;
        if (TraceFlags.PERSISTENT_REPOSITORY) {
            try {
                instance = readLibraryInstance(model, fs, includePathName, includePathName, storageID);
            } catch (Exception e) {
                // just report to console;
                // the code below will create project "from scratch"
                cleanLibraryRepository(fs, includePathName, true, storageID);
                DiagnosticExceptoins.register(e);
            }
        }
        if (instance != null && !(instance instanceof LibProjectImpl)) {
            CndUtils.assertTrueInConsole(false, "cannot correctly deserialize library for " + includePathName);
            cleanLibraryRepository(fs, includePathName, true, storageID);
            instance = null;
        }
        if (instance == null) {
            instance = new LibProjectImpl(model, fs, includePathName, storageID);
        }
        if (CndUtils.isDebugMode()) {
            CndUtils.assertTrue(CharSequences.comparator().compare(includePathName, ((LibProjectImpl) instance).includePath) == 0, includePathName + " vs. " + ((LibProjectImpl) instance).includePath);
            CndUtils.assertTrue(instance.getFileSystem() == fs, instance.getFileSystem() + " vs. " + fs);
        }
        return (LibProjectImpl) instance;
    }

    protected CharSequence getPath() {
        return includePath;
    }

    @Override
    protected void ensureFilesCreated() {
    }

    protected boolean isStableStatus() {
        return true;
    }

    @Override
    public Collection<ProjectBase> getDependentProjects() {
        final LibraryManager instance = LibraryManager.getInstance(getUnitId());
        // TODO: looks like not very safe way to get dependencies
        // see issue #211061
        return instance == null ? Collections.<ProjectBase>emptyList() : instance.getProjectsByLibrary(this);
    }

    @Override
    protected Collection<Key> getLibrariesKeys() {
        return Collections.<Key>emptySet();
    }

    /** override parent to avoid infinite recursion */
    @Override
    public List<CsmProject> getLibraries() {
        return Collections.<CsmProject>emptyList();
    }

    @Override
    protected final ParserQueue.Position getIncludedFileParserQueuePosition() {
        return ParserQueue.Position.TAIL;
    }

    @Override
    public boolean isArtificial() {
        return true;
    }

    @Override
    public NativeFileItem getNativeFileItem(CsmUID<CsmFile> file) {
        return null;
    }

    @Override
    protected void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem) {
    }

    @Override
    protected NativeFileItem removeNativeFileItem(CsmUID<CsmFile> file) {
        return null;
    }

    @Override
    protected void clearNativeFileContainer() {
    }

    @Override
    public boolean isStable(CsmFile skipFile) {
        if (!isDisposing()) {
            return !ParserQueue.instance().hasPendingProjectRelatedWork(this, (FileImpl) skipFile);
        }
        return false;
    }

    @Override
    protected SourceRootContainer getProjectRoots() {
        return projectRoots;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        assert this.includePath != null;
        aStream.writeFilePathForFileSystem(getFileSystem(), includePath);
    }

    public LibProjectImpl(RepositoryDataInput aStream) throws IOException {
        super(aStream);
        this.includePath = aStream.readFilePathForFileSystem(getFileSystem());
        assert this.includePath != null;
        setPlatformProject(this.includePath);
    }
}
