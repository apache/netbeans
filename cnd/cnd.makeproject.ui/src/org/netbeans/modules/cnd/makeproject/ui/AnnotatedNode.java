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
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.Image;
import java.util.Iterator;
import java.util.Set;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUIUtils;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

// XXX should have an API for this
class AnnotatedNode extends AbstractNode implements Runnable, FileStatusListener {

    private Set<FileObject> files;
    private RequestProcessor.Task task;
    private final RequestProcessor rp;
    private volatile boolean iconChange;
    private volatile boolean nameChange;
    private boolean forceAnnotation;
    private FileStatusListener fsl = null;
    private FileSystem fs = null;

    protected AnnotatedNode(Children children, Lookup lookup, RequestProcessor rp) {
        super(children, lookup);
        assert rp != null;
        this.rp = rp;
    }

    protected final void setFiles(final Set<FileObject> files) {
        if (fs != null && fsl != null) {
            fs.removeFileStatusListener(fsl);
        }

        this.files = files;
        if (files == null) {
            return;
        }
        if (files.isEmpty()) {
            return;
        }
        FileObject fo = files.iterator().next();
        if (fo == null) {
            // See IZ 125880
            return;
        }
        try {
            fs = fo.getFileSystem();
            fsl = FileUtil.weakFileStatusListener(this, fs);
            fs.addFileStatusListener(fsl);
        } catch (FileStateInvalidException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Cannot get " + fo + " filesystem, ignoring...");  // NOI18N
            err.notify(ErrorManager.INFORMATIONAL, e);
        }
    }

    protected final Set<FileObject> getFiles() {
        return files;
    }

    protected void setForceAnnotation(boolean forceAnnotation) {
        this.forceAnnotation = forceAnnotation;
    }

    protected final Image annotateIcon(final Image img, final int type) {
        Image annotatedImg = img;
        if (files != null && !files.isEmpty()) {
            Iterator<FileObject> it = files.iterator();
            try {
                FileObject fo = it.next();
                if (fo.isValid()) {
                    annotatedImg = FileUIUtils.getImageDecorator(fo.getFileSystem()).annotateIcon(img, type, files);
                }
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return annotatedImg;
    }

    protected final String annotateName(final String name) {
        String annotatedName = name;
        if (files != null && !files.isEmpty()) {
            Iterator<FileObject> it = files.iterator();
            try {
                FileObject fo = it.next();
                if (fo.isValid()) {
                    annotatedName = fo.getFileSystem().getDecorator().annotateName(name, files);
                }
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return annotatedName;
    }

    @Override
    public final void annotationChanged(FileStatusEvent event) {
        if (files == null) {
            return;
        }
        boolean changed = false;
        if (forceAnnotation || ((iconChange == false && event.isIconChange()) || (nameChange == false && event.isNameChange()))) {
            Iterator<FileObject> it = files.iterator();
            while (it.hasNext()) {
                FileObject fo = it.next();
                if (event.hasChanged(fo)) {
                    iconChange |= event.isIconChange();
                    nameChange |= event.isNameChange();
                    changed = true;
                }
            }
        }

        if (changed) {
            if (task == null) {
                task = this.rp.create(this);
            }
            task.schedule(BaseMakeViewChildren.WAIT_DELAY); // batch by 50 ms
        }
    }

    @Override
    public final void run() {
        if (forceAnnotation || iconChange) {
            fireIconChange();
            fireOpenedIconChange();
            iconChange = false;
        }
        if (forceAnnotation || nameChange) {
            fireDisplayNameChange(null, null);
            nameChange = false;
        }
    }
}
