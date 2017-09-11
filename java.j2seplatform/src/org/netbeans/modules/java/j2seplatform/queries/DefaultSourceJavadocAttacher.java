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
package org.netbeans.modules.java.j2seplatform.queries;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceJavadocAttacher.AttachmentListener;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation.Definer;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.filesystems.FileStateInvalidException;
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
                        cache.updateRoot(root, toAttach);
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
                Lookup.getDefault().lookup(SourceJavadocAttacherImplementation.Definer.class));
        } else if (mode == 1) {
            selected = SourceJavadocAttacherUtil.selectJavadoc(
                root,
                attachedRoots,
                SourceJavadocAttacherUtil.createDefaultBrowseCall(
                    Bundle.TXT_Title(),
                    Bundle.TXT_Javadoc(),
                    cfh),
                SourceJavadocAttacherUtil.createDefaultURIConvertor(false),
                Lookup.getDefault().lookup(SourceJavadocAttacherImplementation.Definer.class));
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
