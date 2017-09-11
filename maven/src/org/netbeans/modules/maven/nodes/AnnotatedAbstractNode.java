/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.nodes;

import java.awt.Image;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/** 
 * A node that shows filesystem annotations on icons.
 *
 * @author Milos Kleint
 */
public abstract class AnnotatedAbstractNode extends AbstractNode implements FileStatusListener, Runnable {

    private Set<FileObject> files;
    private Map<FileSystem, FileStatusListener> fileSystemListeners;
    private RequestProcessor.Task task;
    private final Object privateLock = new Object();
    private boolean iconChange;
    private boolean nameChange;
    private static final RequestProcessor RP = new RequestProcessor(AnnotatedAbstractNode.class);

    public AnnotatedAbstractNode(Children childs, Lookup lookup) {
        super(childs, lookup);
    }

    //----------------------------------------------------
// icon annotation change related, copied from j2se project.
// eventually this should end up in a sort of SPI and be shared across project types

    protected final void setFiles(Set<FileObject> files) {
        if (fileSystemListeners != null) {
            for (Map.Entry<FileSystem, FileStatusListener> e : fileSystemListeners.entrySet()) {
                e.getKey().removeFileStatusListener(e.getValue());
            }
        }

        fileSystemListeners = new HashMap<FileSystem, FileStatusListener>();
        synchronized (privateLock) {
            this.files = files;
            if (files == null) {
                return;
            }

            Set<FileSystem> hookedFileSystems = new HashSet<FileSystem>();
            for (FileObject fo : files) {
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (hookedFileSystems.contains(fs)) {
                        continue;
                    }
                    hookedFileSystems.add(fs);
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.put(fs, fsl);
                } catch (FileStateInvalidException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, ErrorManager.UNKNOWN, "Cannot get " + fo + " filesystem, ignoring...", null, null, null); // NO18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        fireIconChange();
        fireOpenedIconChange();
    }

    @Override
    public void run() {
        boolean fireIcon;
        boolean fireName;
        synchronized (privateLock) {
            fireIcon = iconChange;
            fireName = nameChange;
            iconChange = false;
            nameChange = false;
        }
        if (fireIcon) {
            fireIconChange();
            fireOpenedIconChange();
        }
        if (fireName) {
            fireDisplayNameChange(null, null);
        }
    }

    @Override
    public void annotationChanged(FileStatusEvent event) {
        if (task == null) {
            task = RP.create(this);
        }

        synchronized (privateLock) {
            if ((iconChange == false && event.isIconChange()) || (nameChange == false && event.isNameChange())) {
                for (FileObject fo : files) {
                    if (event.hasChanged(fo)) {
                        iconChange |= event.isIconChange();
                        nameChange |= event.isNameChange();
                    }
                }
            }
        }

        task.schedule(50); // batch by 50 ms
    }
    //----------------------------------------------------

    protected abstract Image getIconImpl(int param);

    protected abstract Image getOpenedIconImpl(int param);

    @Override
    public final Image getIcon(int param) {
        Image img = getIconImpl(param);
        return annotateImpl(img, param);
    }

    @Override
    public final Image getOpenedIcon(int param) {
        Image img = getOpenedIconImpl(param);
        return annotateImpl(img, param);
    }
    
    private Image annotateImpl(Image img, int param) {
        synchronized (privateLock) {
            if (files != null && files.size() > 0) {
                try {
                    Iterator<FileObject> it = files.iterator();
                    assert it.hasNext();
                    FileObject fo = it.next();
                    assert fo != null;
                    FileSystem fs = fo.getFileSystem();
                    assert fs != null;
                    return FileUIUtils.getImageDecorator(fs).annotateIcon(img, param, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        return img;
    }
}
