/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.remotefiles;

import java.awt.EventQueue;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.RemoteFileCache;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

@NbBundle.Messages("LBL_RemoteFiles=Remote Files")
public class RemoteFilesNode extends AbstractNode {

    private final Project project;

    public RemoteFilesNode(Project project, RemoteFiles remoteFiles) {
        super(new RemoteFilesChildren(project, remoteFiles), Lookups.singleton(project));
        this.project = project;
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/web/clientproject/remotefiles/remotefiles.png"); //NOI18N
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getDisplayName() {
        return Bundle.LBL_RemoteFiles();
    }

    private static class RemoteFilesChildren extends Children.Keys<RemoteFile> implements ChangeListener {

        private final Project project;
        private final RemoteFiles remoteFiles;


        public RemoteFilesChildren(Project project, RemoteFiles remoteFiles) {
            this.project = project;
            this.remoteFiles = remoteFiles;
        }

        @Override
        protected Node[] createNodes(RemoteFile key) {
            try {
                FileObject fo = RemoteFileCache.getRemoteFile(key.getUrl());
                DataObject dobj = DataObject.find(fo);
                return new Node[] { new RemoteFileFilterNode(dobj.getNodeDelegate().cloneNode(), key, project) };
            } catch (DataObjectNotFoundException ex) {
                return new Node[] {};
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return new Node[] {};
            }
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            remoteFiles.addChangeListener(this);
            updateKeys();
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            remoteFiles.removeChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            updateKeys();
        }

        private void updateKeys() {
            List<RemoteFile> keys = new ArrayList<>();
            for (URL u : remoteFiles.getRemoteFiles()) {
                keys.add(new RemoteFile(u));
            }
            Collections.sort(keys, new Comparator<RemoteFile>() {
                    @Override
                    public int compare(RemoteFile o1, RemoteFile o2) {
                        // #232116
                        String name1 = o1.getName().toLowerCase();
                        String name2 = o2.getName().toLowerCase();
                        // XXX mime type should be used
                        boolean isJs1 = name1.endsWith(".js"); // NOI18N
                        boolean isJs2 = name2.endsWith(".js"); // NOI18N
                        if (isJs1 && !isJs2) {
                            return -1;
                        }
                        if (!isJs1 && isJs2) {
                            return 1;
                        }
                        return name1.compareTo(name2);
                    }
                });
            setKeys(keys);
        }

    }

    private static class RemoteFileFilterNode extends FilterNode {

        private final String desc;
        private final Node delegate;

        public RemoteFileFilterNode(Node original, RemoteFile remoteFile, Project p) {
            super(original, null, Lookups.fixed(p, new RemoteFileOpenable(remoteFile)));
            this.desc = remoteFile.getDescription();
            delegate = original;
        }

        @Override
        public String getShortDescription() {
            return desc;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return hash * delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RemoteFileFilterNode other = (RemoteFileFilterNode) obj;
            if (this.delegate != other.delegate && (this.delegate == null || !this.delegate.equals(other.delegate))) {
                return false;
            }
            return true;
        }

        @Override
        public Action getPreferredAction() {
            Action[] actions = super.getActions(false);
            if (actions.length > 0) {
                Action firstAction = actions[0];
                if (firstAction != null) {
                    return firstAction;
                }
            }
            return super.getPreferredAction();
        }

    }

    public static class RemoteFile {
        private final URL url;
        private final String name;
        private final String urlAsString;

        public RemoteFile(URL url) {
            this.url = url;
            urlAsString = url.toExternalForm();
            int index = urlAsString.lastIndexOf('/');
            if (index != -1) {
                name = urlAsString.substring(index+1);
            } else {
                name = null;
            }
        }

        public URL getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return urlAsString;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + (this.urlAsString != null ? this.urlAsString.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RemoteFile other = (RemoteFile) obj;
            if ((this.urlAsString == null) ? (other.urlAsString != null) : !this.urlAsString.equals(other.urlAsString)) {
                return false;
            }
            return true;
        }

    }

    private static final class RemoteFileOpenable implements Openable {

        private static final Logger LOGGER = Logger.getLogger(RemoteFileOpenable.class.getName());

        private static final RequestProcessor RP = new RequestProcessor(RemoteFileOpenable.class);

        private final RemoteFile remoteFile;


        public RemoteFileOpenable(RemoteFile remoteFile) {
            assert remoteFile != null;
            this.remoteFile = remoteFile;
        }

        @Override
        public void open() {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    openInBackground();
                }
            });
        }

        void openInBackground() {
            assert !EventQueue.isDispatchThread();
            try {
                FileObject fileObject = RemoteFileCache.getRemoteFile(remoteFile.getUrl());
                assert fileObject != null;
                DataObject dataObject = DataObject.find(fileObject);
                EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);
                editorCookie.open();
            } catch (IOException ex) {
                 LOGGER.log(Level.WARNING, null, ex);
            }
        }

    }
}
