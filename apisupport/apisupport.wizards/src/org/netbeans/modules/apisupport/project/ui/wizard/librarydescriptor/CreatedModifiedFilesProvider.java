/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
/*import org.openide.util.Exceptions;*/

/**
 *
 * @author Radek Matous
 */
final class CreatedModifiedFilesProvider {

    private CreatedModifiedFilesProvider() {
    }
    private static final String VOLUME_CLASS = "classpath";//NOI18N
    private static final String VOLUME_SRC = "src";//NOI18N
    private static final String VOLUME_JAVADOC = "javadoc";//NOI18N
    private static final String LIBRARY_LAYER_ENTRY = "org-netbeans-api-project-libraries/Libraries";//NOI18N

    static CreatedModifiedFiles createInstance(NewLibraryDescriptor.DataModel data) {

        CreatedModifiedFiles retval = new CreatedModifiedFiles(data.getProject());
        addOperations(retval, data);

        return retval;
    }

    private static void addOperations(CreatedModifiedFiles fileSupport, NewLibraryDescriptor.DataModel data) {
        FileObject template = CreatedModifiedFiles.getTemplate("libdescriptemplate.xml");//NOI18N
        Map<String, String> tokens = getTokens(fileSupport, data.getProject(), data);
        String layerEntry = getLibraryDescriptorEntryPath(data.getLibraryName());

        fileSupport.add(
                fileSupport.createLayerEntry(layerEntry, template, tokens, null, null));

        fileSupport.add(
                fileSupport.bundleKeyFromPackagePath(tokens.get("BUNDLE"),
                data.getLibraryName(), data.getLibraryDisplayName()));
    }

    private static String getPackagePlusBundle(Project project) {
        NbModuleProvider mod = project.getLookup().lookup(NbModuleProvider.class);
        if (mod != null) {
            FileObject mf = mod.getManifestFile();
            if (mf != null) {
                File mff = FileUtil.toFile(mf);
                if (mff != null) {
                    String bundle = ManifestManager.getInstance(mff, false).getLocalizingBundle();
                    if (bundle != null) {
                        bundle = bundle.replace('/', '.');
                        if (bundle.endsWith(".properties")) { // NOI18N
                            return bundle.substring(0, bundle.length() - 11);
                        }
                    } else {
                        String newBundleFilePath = ManifestManager.getInstance(mff, false).getCodeNameBase()
                                + "/Bundle.properties";

                        newBundleFilePath = newBundleFilePath.replace('/', '.');
                        return newBundleFilePath.substring(0, newBundleFilePath.length() - 11);

                    }
                }
            }
        }
        return null;
    }

    static String getLibraryDescriptorEntryPath(String libraryName) {
        return LIBRARY_LAYER_ENTRY + "/" + libraryName + ".xml"; // NOI18N
    }

    private static String transformURL(final String cnb, final String pathPrefix, final String archiveName) {
        return "jar:nbinst://" + cnb + "/" + pathPrefix + archiveName + "!/"; // NOI18N
    }

    private static Map<String, String> getTokens(CreatedModifiedFiles fileSupport, Project project, NewLibraryDescriptor.DataModel data) {
        Map<String, String> retval = new HashMap<String, String>();
        Library library = data.getLibrary();
        retval.put("NAME", data.getLibraryName());//NOI18N
        String packagePlusBundle = getPackagePlusBundle(project);
        if (packagePlusBundle != null) {
            retval.put("BUNDLE", packagePlusBundle.replace('/', '.'));//NOI18N
        }
        retval.put("CLASSPATH", getTokenSubstitution(library.getContent(VOLUME_CLASS), fileSupport, data, "libs/")); // NOI18N
        retval.put("SRC", getTokenSubstitution(library.getContent(VOLUME_SRC), fileSupport, data, "sources/")); // NOI18N
        retval.put("JAVADOC", getTokenSubstitution(library.getContent(VOLUME_JAVADOC), fileSupport, data, "docs/")); // NOI18N
        return retval;
    }

    private static String getTokenSubstitution(List<URL> urls, CreatedModifiedFiles fileSupport,
            NewLibraryDescriptor.DataModel data, String pathPrefix) {
        final NbModuleProvider nbmp = data.getModuleInfo();
        fileSupport.add(new CreatedModifiedFiles.AbstractOperation(data.getProject()) {
            // XXX abstract this into a standard operation; should also add pom.xml as a modified file when a Maven project
            public @Override
            void run() throws IOException {
                nbmp.getReleaseDirectory();
            }
        });
        StringBuilder sb = new StringBuilder();
        for (URL originalURL : urls) {
            String archiveName;
            archiveName = addArchiveToCopy(fileSupport, data, originalURL, nbmp.getReleaseDirectoryPath() + '/' + pathPrefix);
            if (archiveName != null) {
                String codeNameBase = nbmp.getCodeNameBase();
                String urlToString = transformURL(codeNameBase, pathPrefix, archiveName);//NOI18N
                sb.append("\n        <resource>"); // NOI18N
                sb.append(urlToString);
                sb.append("</resource>"); // NOI18N
            }
        }
        return sb.toString();
    }

    /**
     * returns archive name or temporarily null cause there is no zip support
     * for file protocol
     */
    private static String addArchiveToCopy(CreatedModifiedFiles fileSupport, NewLibraryDescriptor.DataModel data, URL originalURL, String pathPrefix) {
        String retval = null;

        URL archivURL = FileUtil.getArchiveFile(originalURL);
        if (archivURL != null && FileUtil.isArchiveFile(archivURL)) {
            FileObject archiv = URLMapper.findFileObject(archivURL);
            if (archiv == null) {
                // #129617: broken library entry, just skip it.
                return null;
            }
            retval = archiv.getNameExt();
            fileSupport.add(fileSupport.createFile(pathPrefix + retval, archiv));
        } else {
            if ("file".equals(originalURL.getProtocol())) {//NOI18N
                FileObject folderToZip;
                folderToZip = URLMapper.findFileObject(originalURL);
                if (folderToZip != null) {
                    retval = data.getLibraryName() + "-" + folderToZip.getName() + ".zip"; // NOI18N
                    pathPrefix += retval;
                    fileSupport.add(new ZipAndCopyOperation(data.getProject(),
                            folderToZip, pathPrefix));
                }
            }
        }
        return retval;
    }

    private static class ZipAndCopyOperation extends CreatedModifiedFiles.AbstractOperation {

        private FileObject folderToZip;
        private String relativePath;

        ZipAndCopyOperation(Project prj, FileObject folderToZip, String relativePath) {
            super(prj);
            this.folderToZip = folderToZip;
            this.relativePath = relativePath;
            addCreatedOrModifiedPath(relativePath, false);
        }

        public @Override
        void run() throws IOException {
            Collection<? extends FileObject> files = Collections.list(folderToZip.getChildren(true));
            if (files.isEmpty()) {
                return;
            }
            FileObject prjDir = getProject().getProjectDirectory();
            assert prjDir != null;

            FileObject zippedTarget = prjDir.getFileObject(relativePath);
            if (zippedTarget == null) {
                zippedTarget = FileUtil.createData(prjDir, relativePath);
            }

            assert zippedTarget != null;
            OutputStream os = zippedTarget.getOutputStream();
            try {
                createZipFile(os, folderToZip, files);
            } finally {
                os.close();
            }
        }

        private static void createZipFile(OutputStream target, FileObject root, Collection<? extends FileObject> files) throws IOException {
            ZipOutputStream str = null;
            try {
                str = new ZipOutputStream(target);
                for (FileObject fo : files) {
                    String relativePath = FileUtil.getRelativePath(root, fo);
                    if (fo.isFolder()) {
                        if (fo.getChildren().length > 0) {
                            continue;
                        } else if (!relativePath.endsWith("/")) {
                            relativePath += "/";
                        }
                    }
                    ZipEntry entry = new ZipEntry(relativePath);
                    str.putNextEntry(entry);
                    if (fo.isData()) {
                        InputStream in = null;
                        try {
                            in = fo.getInputStream();
                            FileUtil.copy(in, str);
                        } finally {
                            if (in != null) {
                                in.close();
                            }
                        }
                    }
                    str.closeEntry();
                }
            } finally {
                if (str != null) {
                    str.close();
                }
            }
        }
    }
}
