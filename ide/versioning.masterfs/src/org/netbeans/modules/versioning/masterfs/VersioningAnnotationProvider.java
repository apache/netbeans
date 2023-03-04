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
package org.netbeans.modules.versioning.masterfs;

import java.awt.Image;
import java.util.*;
import javax.swing.*;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor.VCSAnnotationEvent;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Plugs into IDE filesystem and delegates annotation work to registered versioning systems.
 * 
 * @author Maros Sandor
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.masterfs.providers.AnnotationProvider.class)
public final class VersioningAnnotationProvider extends AnnotationProvider {
    private FilesystemInterceptor interceptor;

    public VersioningAnnotationProvider() {
    }
    
    @Override
    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        return VCSFilesystemInterceptor.annotateIcon(icon, iconType, files);
    }

    @Override
    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
        return VCSFilesystemInterceptor.annotateNameHtml(name, files);
    }

    @Override
    public Lookup findExtrasFor(Set<? extends FileObject> files) {
        Action[] arr = VCSFilesystemInterceptor.actions(files);
        return arr == null ? null : Lookups.fixed((Object[]) arr);
    }
    
    @Override
    public synchronized InterceptionListener getInterceptionListener() {
        if (interceptor == null) {
            interceptor = new FilesystemInterceptor(this);
        }
        return interceptor;
    }

    @Override
    public String annotateName(String name, Set files) {
        return name;    // do not support 'plain' annotations
    }
    
    final void deliverStatusEvent(FileSystem fs, VCSAnnotationEvent ev) {
        fireFileStatusChanged(new FileStatusEvent(fs, ev.getFiles(), ev.isIconChange(), ev.isNameChange()));
    }
}
