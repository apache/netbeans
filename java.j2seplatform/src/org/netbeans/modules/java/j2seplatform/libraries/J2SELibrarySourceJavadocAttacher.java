/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seplatform.libraries;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.queries.SourceJavadocAttacher.AttachmentListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.java.j2seplatform.queries.SourceJavadocAttacherUtil;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=SourceJavadocAttacherImplementation.class, position=151)
public class J2SELibrarySourceJavadocAttacher implements SourceJavadocAttacherImplementation {

    @Override
    public boolean attachSources(
            @NonNull final URL root,
            @NonNull final AttachmentListener listener) throws IOException {
        return attach(root, listener, J2SELibraryTypeProvider.VOLUME_TYPE_SRC);
    }

    @Override
    public boolean attachJavadoc(
            @NonNull final URL root,
            @NonNull final AttachmentListener listener) throws IOException {
        return attach(root, listener, J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC);
    }

    private boolean attach(
            @NonNull final URL root,
            @NonNull final AttachmentListener listener,
            @NonNull final String volume) {
        final Pair<LibraryManager,Library> pair = findOwner(root);
        if (pair == null) {
            return false;
        }
        final Runnable call = new Runnable() {
            @Override
            public void run() {
                final LibraryManager lm = pair.first;
                final Library lib = pair.second;
                assert lm != null;
                assert lib != null;
                boolean success = false;
                try {
                    final URL areaLocation = lm.getLocation();
                    final File baseFolder = areaLocation == null ? null : Utilities.toFile(areaLocation.toURI()).getParentFile();
                    final List<? extends URI> selected;
                    if (volume == J2SELibraryTypeProvider.VOLUME_TYPE_SRC) {
                        selected = SourceJavadocAttacherUtil.selectSources(
                            root,
                            lib.getURIContent(volume),
                            new SelectFolder(volume, lib.getName(), baseFolder),
                            new Convertor(volume, baseFolder),
                            null);
                    } else if (volume == J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC) {
                        selected = SourceJavadocAttacherUtil.selectJavadoc(
                            root,
                            lib.getURIContent(volume),
                            new SelectFolder(volume, lib.getName(), baseFolder),
                            new Convertor(volume, baseFolder),
                            null);
                    } else {
                        throw new IllegalStateException();
                    }
                    if (selected != null) {
                        final String name = lib.getName();
                        final String displayName = lib.getDisplayName();
                        final String desc = lib.getDescription();
                        final Map<String,List<URI>> volumes = new HashMap<String, List<URI>>();
                        for (String currentVolume : J2SELibraryTypeProvider.VOLUME_TYPES) {
                            List<URI> content = lib.getURIContent(currentVolume);
                            if (volume == currentVolume) {
                                final List<URI> newContent = new ArrayList<>(selected);
                                content = newContent;
                            }
                            volumes.put(currentVolume,content);
                        }
                        final Map<String,String> props = lib.getProperties();
                        lm.removeLibrary(lib);
                        lm.createURILibrary(
                            J2SELibraryTypeProvider.LIBRARY_TYPE,
                            name,
                            displayName,
                            desc,
                            volumes,
                            props);
                        success = true;
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                } catch (URISyntaxException use) {
                    Exceptions.printStackTrace(use);
                } finally {
                    SourceJavadocAttacherUtil.callListener(listener, success);
                }
            }
        };
        Mutex.EVENT.writeAccess(call);
        return true;
    }

    private Pair<LibraryManager,Library> findOwner(final URL root) {
        for (LibraryManager lm : LibraryManager.getOpenManagers()) {
            for (Library l : lm.getLibraries()) {
                if (!J2SELibraryTypeProvider.LIBRARY_TYPE.equals(l.getType())) {
                    continue;
                }
                final List<URL> cp = l.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
                if (cp.contains(root)) {
                    return Pair.<LibraryManager,Library>of(lm, l);
                }
            }
        }
        return null;
    }

    private static class Pair<F,S> {
        public final F first;
        public final S second;

        private Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public static <F,S> Pair<F,S> of(F first, S second) {
            return new Pair<F,S>(first,second);
        }
    }

    private static class SelectFolder implements Callable<List<? extends String>> {

        private final String volume;
        private final String libName;
        private final File baseFolder;

        private SelectFolder(
                @NonNull final String volume,
                @NonNull final String libName,
                @NullAllowed final File baseFolder) {
            this.volume = volume;
            this.libName = libName;
            this.baseFolder = baseFolder;
        }

        @Override
        public List<? extends String> call() throws Exception {
            final String[] paths = J2SEVolumeCustomizer.select(
                volume,
                libName,
                new File[1],
                null,
                baseFolder);
            return paths == null ? null : Arrays.<String>asList(paths);
        }
    }

    private static class Convertor implements SourceJavadocAttacherUtil.Function<String, Collection<? extends URI>> {

        private final String volume;
        private final File baseFolder;

        private Convertor(
                @NonNull final String volume,
                @NullAllowed final File baseFolder) {
            Parameters.notNull("volume", volume);   //NOI18N
            this.volume = volume;
            this.baseFolder = baseFolder;
        }

        @Override
        public Collection<? extends URI> call(String param) throws Exception {
            Collection<String> roots = Collections.singleton(param);
            final File paramFile = new File(param);
            if (paramFile.isAbsolute() && paramFile.isDirectory()) {
                FileObject paramFO = FileUtil.toFileObject(paramFile);
                if (paramFO != null) {
                    switch (volume) {
                        case J2SELibraryTypeProvider.VOLUME_TYPE_SRC:
                            final Collection<String> sourceFolders = new ArrayList<>();
                            for( FileObject fo : JavadocAndSourceRootDetection.findSourceRoots(paramFO, null)) {
                                final File f = FileUtil.toFile(fo);
                                if (f != null) {
                                    sourceFolders.add(f.getAbsolutePath());
                                }
                            }
                            if (!sourceFolders.isEmpty()) {
                                roots = sourceFolders;
                            }
                            break;
                        case J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC:
                            final Collection<String> javadocFolders = new ArrayList<>();
                            for (FileObject fo : JavadocAndSourceRootDetection.findJavadocRoots(paramFO, null)) {
                                final File f = FileUtil.toFile(fo);
                                if (f != null) {
                                    javadocFolders.add(f.getAbsolutePath());
                                }
                            }
                            if (!javadocFolders.isEmpty()) {
                                roots = javadocFolders;
                            }
                            break;
                    }
                }
            }
            final Collection<URI> result = new ArrayList<>(roots.size());
            for (String root : roots) {
                final URI uri = J2SEVolumeCustomizer.pathToURI(baseFolder, root, volume);
                if (uri != null) {
                    result.add(uri);
                }
            }
            return Collections.unmodifiableCollection(result);
        }
    }

}
