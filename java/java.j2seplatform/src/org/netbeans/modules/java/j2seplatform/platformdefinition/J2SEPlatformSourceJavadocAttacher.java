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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceJavadocAttacher.AttachmentListener;
import org.netbeans.modules.java.j2seplatform.queries.SourceJavadocAttacherUtil;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=SourceJavadocAttacherImplementation.class,position=150)
public class J2SEPlatformSourceJavadocAttacher implements SourceJavadocAttacherImplementation {

    @Override
    public boolean attachSources(
            @NonNull final URL root,
            @NonNull AttachmentListener listener) throws IOException {
        return attach(root, listener, J2SEPlatformCustomizer.SOURCES);
    }

    @Override
    public boolean attachJavadoc(
            @NonNull final URL root,
            @NonNull AttachmentListener listener) throws IOException {
        return attach(root, listener, J2SEPlatformCustomizer.JAVADOC);
    }

    @NbBundle.Messages({
        "TXT_Title=Browse ZIP/Folder",
        "TXT_JavadocFilterName=Library Javadoc (folder, ZIP or JAR file)",
        "TXT_SourcesFilterName=Library Sources (folder, ZIP or JAR file)"
    })
    private boolean attach(
            @NonNull final URL root,
            @NonNull final AttachmentListener listener,
            final int mode) {
        final J2SEPlatformImpl platform = findOwner(root);
        if (platform == null) {
            return false;
        }
        final Runnable call = new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    final J2SEPlatformCustomizer.PathModel model = new J2SEPlatformCustomizer.PathModel(platform, mode);
                    final List<? extends URI> selected;
                    if (mode == J2SEPlatformCustomizer.SOURCES) {
                        selected = SourceJavadocAttacherUtil.selectSources(
                            root,
                            model.getRootURIs(),
                            SourceJavadocAttacherUtil.createDefaultBrowseCall(
                                Bundle.TXT_Title(),
                                Bundle.TXT_SourcesFilterName(),
                                new File[1]),
                            SourceJavadocAttacherUtil.createDefaultURIConvertor(true),
                            null);
                    } else if (mode == J2SEPlatformCustomizer.JAVADOC) {
                        selected = SourceJavadocAttacherUtil.selectJavadoc(
                            root,
                            model.getRootURIs(),
                            SourceJavadocAttacherUtil.createDefaultBrowseCall(
                                Bundle.TXT_Title(),
                                Bundle.TXT_JavadocFilterName(),
                                new File[1]),
                            SourceJavadocAttacherUtil.createDefaultURIConvertor(false),
                            null);
                    } else {
                        throw new IllegalStateException(Integer.toString(mode));
                    }
                    if (selected != null) {
                        model.update(toURLList(selected));
                        success = true;                        
                    }
                } finally {
                    SourceJavadocAttacherUtil.callListener(listener, success);
                }
            }
        };
        Mutex.EVENT.writeAccess(call);
        return true;
    }

    @NonNull
    private static List<? extends URL> toURLList(@NonNull final List<? extends URI> uris) {
        final List<URL> result = new ArrayList<>(uris.size());
        for (URI uri : uris) {
            try {
                result.add(uri.toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
    }

    private J2SEPlatformImpl findOwner(final URL root) {
        for (JavaPlatform p : JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2SEPlatformImpl.PLATFORM_J2SE, null))) {
            if (!(p instanceof J2SEPlatformImpl)) {
                //Cannot handle unknown platform
                continue;
            }
            final J2SEPlatformImpl j2sep = (J2SEPlatformImpl) p;
            if (!j2sep.isValid()) {
                continue;
            }
            for (ClassPath.Entry entry : j2sep.getBootstrapLibraries().entries()) {
                if (root.equals(entry.getURL())) {
                    return j2sep;
                }
            }
        }
        return null;
    }

}
