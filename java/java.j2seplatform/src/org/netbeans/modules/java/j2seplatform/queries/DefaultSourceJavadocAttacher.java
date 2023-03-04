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
package org.netbeans.modules.java.j2seplatform.queries;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceJavadocAttacher.AttachmentListener;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=SourceJavadocAttacherImplementation.class) //position=last
public class DefaultSourceJavadocAttacher implements SourceJavadocAttacherImplementation {

    @Override
    public boolean attachSources(
            @NonNull final URL root,
            @NonNull final AttachmentListener listener) throws IOException {
        return attach(root, listener, 0);
    }

    @Override
    public boolean attachJavadoc(
            @NonNull final URL root,
            @NonNull final AttachmentListener listener) throws IOException {
        return attach(root, listener, 1);
    }

    private boolean attach (
            @NonNull final URL root,
            @NonNull final AttachmentListener listener,
            final int mode) throws IOException {
        final Runnable call = new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    final QueriesCache<?> cache;
                    switch (mode) {
                        case 0:
                            cache = QueriesCache.getSources();
                            break;
                        case 1:
                            cache = QueriesCache.getJavadoc();
                            break;
                        default:
                            throw new IllegalArgumentException(Integer.toString(mode));
                    }
                    final QueriesCache.ResultBase rb = cache.getRoots().get(root);
                    final List<URI> currentRoots;
                    if (rb == null) {
                        currentRoots = Collections.emptyList();
                    } else {
                        currentRoots = new ArrayList<>();
                        currentRoots.addAll(rb.getRootURIs());
                    }
                    final URL[] toAttach = selectRoots(root, currentRoots, mode);
                    if (toAttach != null) {
                        // verify the added roots are not discoverable by normal infrastructure:
                        FileObject[] queryRoots = SourceForBinaryQuery.findSourceRoots(root).getRoots();
                        List<FileObject> filesToAttach = new ArrayList<>();

                        boolean allMatches = true;
                        for (URL u : toAttach) {
                            FileObject f = URLMapper.findFileObject(u);
                            if (f != null) {
                                filesToAttach.add(f);
                            } else {
                                // URL cannot be mapped to a file, can't be represented by SFBQ
                                allMatches = false;
                                break;
                            }
                        }
                        if (allMatches) {
                            allMatches = Arrays.asList(queryRoots).containsAll(filesToAttach);
                        }
                        if (!allMatches) {
                            cache.updateRoot(root, toAttach);
                        }
                        success = true;
                    }
                } catch (MalformedURLException | FileStateInvalidException e) {
                    Exceptions.printStackTrace(e);
                } finally {
                    SourceJavadocAttacherUtil.callListener(listener,success);
                }
            }
        };
        Mutex.EVENT.writeAccess(call);
        return true;
    }

    @NbBundle.Messages({
        "TXT_Title=Browse ZIP/Folder",
        "TXT_Javadoc=Library Javadoc (folder, ZIP or JAR file)",
        "TXT_Sources=Library Sources (folder, ZIP or JAR file)"
    })
    private static URL[] selectRoots(
            @NonNull final URL root,
            @NonNull final List<? extends URI> attachedRoots,
            final int mode) throws MalformedURLException, FileStateInvalidException {
        final File[] cfh = new File[]{currentFolder};
        final List<? extends URI> selected;
        if (mode == 0) {
            selected = SourceJavadocAttacherUtil.selectSources(
                root,
                attachedRoots,
                SourceJavadocAttacherUtil.createDefaultBrowseCall(
                    Bundle.TXT_Title(),
                    Bundle.TXT_Sources(),
                    cfh),
                SourceJavadocAttacherUtil.createDefaultURIConvertor(true),
                Lookup.getDefault().lookupAll(SourceJavadocAttacherImplementation.Definer.class));
        } else if (mode == 1) {
            selected = SourceJavadocAttacherUtil.selectJavadoc(
                root,
                attachedRoots,
                SourceJavadocAttacherUtil.createDefaultBrowseCall(
                    Bundle.TXT_Title(),
                    Bundle.TXT_Javadoc(),
                    cfh),
                SourceJavadocAttacherUtil.createDefaultURIConvertor(false),
                Lookup.getDefault().lookupAll(SourceJavadocAttacherImplementation.Definer.class));
        } else {
            throw new IllegalStateException(Integer.toString(mode));
        }

        if (selected == null) {
            return null;
        }
        currentFolder = cfh[0];
        final URL[] result = new URL[selected.size()];
        for (int i=0; i< result.length; i++) {
            result[i] = selected.get(i).toURL();
        }
        return result;
    }

    private static File currentFolder;
}
