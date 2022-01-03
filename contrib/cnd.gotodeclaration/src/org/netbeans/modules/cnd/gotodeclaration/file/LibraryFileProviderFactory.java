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
package org.netbeans.modules.cnd.gotodeclaration.file;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.gotodeclaration.symbol.CppSymbolProvider;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.netbeans.spi.jumpto.file.FileProvider;
import org.netbeans.spi.jumpto.file.FileProviderFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;

/**
 *
 */
// we use position less than MakeProjectFileProviderFactory to be called by infrastructure
// and have a chance to contribute libraries even if MakeProjectFileProviderFactory consumes src root
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.spi.jumpto.file.FileProviderFactory.class, position = 900)
public final class LibraryFileProviderFactory implements FileProviderFactory {

    @Override
    public String name() {
        return "CND_LIBRARY_FILES"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return name();
    }

    @Override
    public FileProvider createFileProvider() {
        return new FileProviderImpl();
    }

    private final static class FileProviderImpl implements FileProvider {

        private final Map<CsmProject, Set<CsmUID<CsmFile>>> cache = new HashMap<CsmProject, Set<CsmUID<CsmFile>>>();
        private final AtomicBoolean cancel = new AtomicBoolean(false);
        private String cachedTextPrefix = null;
        private String lastText = null;
        private SearchType lastSearchType = null;
        private Context lastQueriedContext;

        @Override
        public boolean computeFiles(Context context, Result result) {
            cancel.set(false);
            if (lastQueriedContext == context) {
                // check if already provided info for this context
                if (context.getText().equals(lastText) && context.getSearchType().equals(lastSearchType)) {
                    return false;
                }
            }
            lastQueriedContext = context;
            boolean validCache = cachedTextPrefix != null && lastText.startsWith(cachedTextPrefix) && context.getSearchType().equals(lastSearchType);
            lastText = context.getText();
            lastSearchType = context.getSearchType();
            if (CsmModelAccessor.getModelState() == CsmModelState.ON) {
                CsmSelect.NameAcceptor nameAcceptor = CppSymbolProvider.createNameAcceptor(lastText, lastSearchType);
                if (nameAcceptor == null) {
                    if (CndUtils.isDebugMode()) {
                        Logger log = Logger.getLogger("org.netbeans.modules.cnd.gotodeclaration"); // NOI18N
                        log.log(Level.SEVERE, "Can not create File Matcher for ''{0}'' search type {1}", new Object[]{lastText, lastSearchType}); //NOI18N
                    }
                    return false;
                }
                if (!validCache) {
                    cache.clear();
                    Set<CsmProject> libs = new HashSet<CsmProject>();
                    for (CsmProject csmProject : CsmModelAccessor.getModel().projects()) {
                        if (cancel.get()) {
                            break;
                        }
                        // check only libraries
                        libs.addAll(csmProject.getLibraries());
                    }
                    for (CsmProject lib : libs) {
                        if (cancel.get()) {
                            break;
                        }
                        Set<CsmUID<CsmFile>> libFiles = cache.get(lib);
                        if (libFiles == null) {
                            cache.put(lib, libFiles = new HashSet<CsmUID<CsmFile>>());
                        }
                        Iterator<CsmUID<CsmFile>> fileUIDs = CsmSelect.getFileUIDs(lib, nameAcceptor);
                        while (fileUIDs.hasNext()) {
                            if (cancel.get()) {
                                break;
                            }
                            CsmUID<CsmFile> fileUID = fileUIDs.next();
                            libFiles.add(fileUID);
                        }
                    }
                }
                final CsmFileInfoQuery fiq = CsmFileInfoQuery.getDefault();
                for (Map.Entry<CsmProject, Set<CsmUID<CsmFile>>> entry : cache.entrySet()) {
                    for (CsmUID<CsmFile> fileUID : entry.getValue()) {
                        CharSequence name = fiq.getName(fileUID);
                        if (nameAcceptor.accept(name)) {
                            result.addFileDescriptor(new LibraryFileFD(fileUID, entry.getKey()));
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void cancel() {
            cancel.set(true);
        }
    }

    private static final class LibraryFileFD extends FileDescriptor {

        private final CsmUID<CsmFile> file;
        private final CsmProject project;

        public LibraryFileFD(CsmUID<CsmFile> file, CsmProject project) {
            this.file = file;
            this.project = project;
        }

        @Override
        public String getFileName() {
            return CsmFileInfoQuery.getDefault().getName(file).toString();
        }

        @Override
        public String getOwnerPath() {
            String ownerPath = CsmFileInfoQuery.getDefault().getAbsolutePath(file).toString();
            if (ownerPath.startsWith(project.getName().toString())) {
                // make relative path
                ownerPath = ownerPath.substring(project.getName().length() + 1);
            }
            int indx = ownerPath.lastIndexOf('/'); // NOI18N
            if (indx < 0) {
                indx = ownerPath.lastIndexOf('\\'); // NOI18N
            }
            if (indx > 0 && indx < ownerPath.length()) {
                ownerPath = ownerPath.substring(0, indx);
            } else {
                ownerPath = "";
            }
            return ownerPath;
        }

        @Override
        public Icon getIcon() {
            final CsmFile csmFile = file.getObject();
            if (csmFile != null) {
                return CsmImageLoader.getIcon(csmFile);
            }
            return null;
        }

        @Override
        public String getProjectName() {
            return project.getName().toString();
        }

        @Override
        public Icon getProjectIcon() {
            return CsmImageLoader.getProjectIcon(project, false);
        }

        @Override
        public void open() {
            final Runnable r = new Runnable() {

                @Override
                public void run() {
                    CsmUtilities.openSource(file.getObject());
                }
            };
            CsmModelAccessor.getModel().enqueue(r, "LibraryFileFD.open(" + getFileDisplayPath() + ")"); // NOI18N
        }

        @Override
        public FileObject getFileObject() {
            return CsmUtilities.getFileObject(file.getObject());
        }

        @Override
        public String getFileDisplayPath() {
            final CharSequence path = CsmFileInfoQuery.getDefault().getAbsolutePath(file);
            return path == null ? "" : path.toString(); // NOI18N
        }
    }
}
