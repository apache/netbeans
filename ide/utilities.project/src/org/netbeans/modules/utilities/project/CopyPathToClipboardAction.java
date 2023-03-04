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
package org.netbeans.modules.utilities.project;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.datatransfer.ExClipboard;

/**
 * Copies the absolute path of selected {@link DataObject}s or {@link Project}s
 * to the clipboard. Action is shown in context menu of tab. It support
 * DataShadows, so the path of items in the favorites window can be copied
 * properly. It support items within JAR-files.
 *
 * @author markiewb
 */
@ActionID(
    category = "Edit",
    id = "org.netbeans.modules.utilities.CopyPathToClipboard")
@ActionRegistration(
    displayName = "#CTL_CopyPaths",
    popupText = "#CTL_CopyPath")
@ActionReferences({
    @ActionReference(path = "Editors/TabActions", position=400),
    @ActionReference(path = "Shortcuts", name="SO-L")
})
@Messages({
    "CTL_CopyPaths=Copy File Path(s)",
    "CTL_CopyPath=Copy File Path"
})
public final class CopyPathToClipboardAction implements ActionListener,
        ClipboardOwner {

    private final List<Lookup.Provider> context;

    public CopyPathToClipboardAction(List<Lookup.Provider> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {

        Collection<String> paths=new TreeSet<String>();
        paths.addAll(getSelectedPathsForDataObjects());
        paths.addAll(getSelectedPathsForProjects());

        StringBuilder sb = new StringBuilder();
        int items = 0;
        for (String path : paths) {
            if (items > 0) {
                sb.append(" ");                                         //NOI18N
            }
            sb.append(quoteIfNeeded(path));
            items++;
        }
        if (items > 0) {
            String pathListString = sb.toString();
            if (!pathListString.isEmpty()) {
                setClipboardContents(pathListString, items);
            }
        }
    }

    /**
     * Get paths of selected DataObjects. Prevent duplicates, see #219014.
     *
     * @return Sorted collection of unique paths.
     */
    Collection<String> getSelectedPathsForDataObjects() {
        Set<String> paths = new TreeSet<String>();
        for (Lookup.Provider lookupProvider : context) {
            if (lookupProvider instanceof DataObject) {
                paths.add(getAbsolutePath((DataObject) lookupProvider));
            }
        }
        return paths;
    }

    /**
     * Quote a path if it contains space characters.
     */
    private String quoteIfNeeded(String s) {
        if (s.contains(" ")) { //NOI18N
            return "\"" + s + "\""; //NOI18N
        } else {
            return s;
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // do nothing
    }

    public String getAbsolutePath(DataObject dataObject) {
        String fileName = null;
        if (null != dataObject) {
            final FileObject primaryFile = getFileObjectWithShadowSupport(
                    dataObject);
            fileName = getAbsolutePath(primaryFile);
        }
        return fileName;
    }

    public String getAbsolutePath(FileObject fileObject) {
        String fileName = getNativePath(fileObject);
        //support selected items in jars
        if (null != FileUtil.getArchiveFile(fileObject)) {
            String fullJARPath
                    = getNativePath(FileUtil.getArchiveFile(fileObject));
            String archiveFileName = fileObject.getPath();
            if (!archiveFileName.isEmpty()) {
                fileName = fullJARPath + File.pathSeparator
                        + archiveFileName;
            } else {
                fileName = fullJARPath;
            }
        }
        return fileName;
    }

    /**
     * Get native path of a FileObject. If it does not exists, return
     * {@link FileObject#getPath()}
     */
    private String getNativePath(FileObject fo) {
        File f = FileUtil.toFile(fo);
        return f != null ? f.getAbsolutePath() : fo.getPath();
    }

    /**
     * Gets the primary FileObject or the original FileObject when it is a
     * DataShadow (f.e. an entry within favorites)
     *
     * @param dataObject
     * @return
     */
    private FileObject getFileObjectWithShadowSupport(DataObject dataObject) {
        if (dataObject instanceof DataShadow) {
            DataShadow dataShadow = (DataShadow) dataObject;
            return dataShadow.getOriginal().getPrimaryFile();
        }
        return dataObject.getPrimaryFile();
    }

    /**
     * Get the project directory of the given project.
     *
     * @param project
     * @return
     */
    private String getProjectDirectory(final Project project) {
        try {
            FileObject projectDirectory = project.getProjectDirectory();
            return getNativePath(projectDirectory);
        } catch (Exception e) {
            Logger.getLogger(CopyPathToClipboardAction.class.getName()).log(
                    Level.FINE, null, e);
            return null;
        }
    }

    /**
     * Get paths of selected projects. Prevent duplicates, see #219014.
     *
     * @return Sorted collection of unique paths.
     */
    Collection<String> getSelectedPathsForProjects() {

        Set<String> paths = new TreeSet<String>();
        for (Lookup.Provider object : context) {
            if (object instanceof Project) {
                String projectDir = getProjectDirectory((Project) object);
                if (null != projectDir) {
                    paths.add(projectDir);
                }
            }
        }
        return paths;
    }

    /**
     * Sets the clipboard context in textual-format.
     *
     * @param content
     */
    @Messages({
        "# {0} - copied file path",
        "CTL_Status_CopyToClipboardSingle=Copy to Clipboard: {0}",
        "# {0} - number of copied paths",
        "CTL_Status_CopyToClipboardMulti={0} paths were copied to clipboard"
    })
    private void setClipboardContents(String content, int items) {
        Clipboard clipboard = Lookup.getDefault().lookup(ExClipboard.class);
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        if (clipboard != null) {
            String statusText = items > 1
                    ? Bundle.CTL_Status_CopyToClipboardMulti(items)
                    : Bundle.CTL_Status_CopyToClipboardSingle(content);
            StatusDisplayer.getDefault().setStatusText(statusText);
            clipboard.setContents(new StringSelection(content), null);
        }
    }
}
