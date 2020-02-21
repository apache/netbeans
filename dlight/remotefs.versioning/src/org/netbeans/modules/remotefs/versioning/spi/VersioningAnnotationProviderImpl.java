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
package org.netbeans.modules.remotefs.versioning.spi;

import java.awt.Image;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.remote.impl.fileoperations.spi.AnnotationProvider;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor;
import org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor.VCSAnnotationEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileSystem;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service = AnnotationProvider.class, position = 1000)
public class VersioningAnnotationProviderImpl extends AnnotationProvider {

    public VersioningAnnotationProviderImpl() {
    }

    @Override
    public String annotateName(String name, Set<? extends FileObject> files) {
        return name;    // do not support 'plain' annotations
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
    public Action[] actions(Set<? extends FileObject> files) {
        return VCSFilesystemInterceptor.actions(files);
    }

    final void deliverStatusEvent(FileSystem fs, VCSAnnotationEvent ev) {
        fireFileStatusChanged(new FileStatusEvent(fs, ev.getFiles(), ev.isIconChange(), ev.isNameChange()));
    }
}
