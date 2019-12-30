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

package org.netbeans.modules.gradle;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.StatusDecorator;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableOpenSupport;

@Messages({
    "LBL_GradleFile_LOADER=Gradle Script",
    "CTL_SourceTabCaption=&Source"
})
@MIMEResolver.Registration(
        displayName="#LBL_GradleFile_LOADER",
        resource="gradle-mime-resolver.xml",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-gradle+x-kotlin/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-kotlin/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 400
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-kotlin/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 500,
            separatorAfter = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-kotlin/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 700
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-kotlin/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 800,
            separatorAfter = 900
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-kotlin/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 1000,
            separatorAfter = 1100
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-kotlin/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1200,
            separatorAfter = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-kotlin/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1400
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-kotlin/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1500
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-groovy/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-groovy/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 400
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-groovy/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 500,
            separatorAfter = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-groovy/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 700
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-groovy/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 800,
            separatorAfter = 900
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-groovy/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 1000,
            separatorAfter = 1100
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-groovy/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1200,
            separatorAfter = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-groovy/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1400
    ),
    @ActionReference(
            path = "Loaders/text/x-gradle+x-groovy/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1500
    )
})

@DataObject.Registrations({
        @DataObject.Registration(
        mimeType = GradleDataObject.MIME_TYPE,
        iconBase = "org/netbeans/modules/gradle/resources/gradle.png",
        displayName = "#LBL_GradleFile_LOADER",
        position = 300
    ),
    @DataObject.Registration(
        mimeType = GradleDataObject.KOTLIN_MIME_TYPE,
        iconBase = "org/netbeans/modules/gradle/resources/gradle.png",
        displayName = "#LBL_GradleFile_LOADER",
        position = 290
    )
})
public class GradleDataObject extends MultiDataObject {

    public static final String MIME_TYPE = "text/x-gradle+x-groovy"; //NOI18N
    public static final String KOTLIN_MIME_TYPE = "text/x-gradle+x-kotlin"; //NOI18N

    public GradleDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        getCookieSet().add(new GradleDataEditor());
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    private class GradleDataEditor extends DataEditorSupport implements EditorCookie.Observable, OpenCookie, EditCookie, PrintCookie, CloseCookie {

        private final SaveCookie save = new SaveCookie() {
            public @Override
            void save() throws IOException {
                saveDocument();
            }

            @Override
            public String toString() {
                return getPrimaryFile().getNameExt();
            }
        };

        GradleDataEditor() {
            super(GradleDataObject.this, null, new GradleEnv(GradleDataObject.this));
        }

        @Override
        protected CloneableEditorSupport.Pane createPane() {
            return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(MIME_TYPE, getDataObject());
        }

        @Override
        protected boolean notifyModified() {
            if (!super.notifyModified()) {
                return false;
            }
            if (getLookup().lookup(SaveCookie.class) == null) {
                getCookieSet().add(save);
                setModified(true);
            }
            return true;
        }

        @Override
        protected void notifyUnmodified() {
            super.notifyUnmodified();
            if (getLookup().lookup(SaveCookie.class) == save) {
                getCookieSet().remove(save);
                setModified(false);
            }
        }

        @Override
        protected String messageName() {
            String title = getFileOrProjectName(getPrimaryFile());
            try {
                StatusDecorator decorator = getPrimaryFile().getFileSystem().getDecorator();
                title = decorator.annotateName(title, Collections.singleton(getPrimaryFile()));
            } catch (FileStateInvalidException ex) {
                // Just fall through
            }
            return annotateName(title, false, isModified(), !getPrimaryFile().canWrite());
        }

        @Override
        protected String messageHtmlName() {
            String title = getFileOrProjectName(getPrimaryFile());
            try {
                StatusDecorator decorator = getPrimaryFile().getFileSystem().getDecorator();
                String annotateNameHtml = decorator.annotateNameHtml(title, Collections.singleton(getPrimaryFile()));
                if (annotateNameHtml != null && !title.equals(annotateNameHtml)) {
                    title = annotateNameHtml;
                }
            } catch (FileStateInvalidException ex) {
                // Just fall through
            }
            return annotateName(title, true, isModified(), !getPrimaryFile().canWrite());
        }


        @Override
        protected boolean asynchronousOpen() {
            return true;
        }

        // XXX override initializeCloneableEditor if needed; see AntProjectDataEditor
    }

    static String getFileOrProjectName(FileObject primaryFile) {
        String ret = primaryFile.getNameExt();

        if (GradleFiles.BUILD_FILE_NAME.equals(ret) || GradleFiles.BUILD_FILE_NAME_KTS.equals(ret)) {
            try {
                Project prj = ProjectManager.getDefault().findProject(primaryFile.getParent());
                if (prj != null) {
                    ret = ProjectUtils.getInformation(prj).getName();
                }
            } catch (IOException | IllegalArgumentException ex) {
                Logger.getLogger(GradleDataObject.class.getName()).log(Level.INFO, "Could not determine project and its name", ex);
            }
        }
        return ret;
    }

    private static class GradleEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = 1L;

        GradleEnv(MultiDataObject d) {
            super(d);
        }

        protected @Override
        FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected @Override
        FileLock takeLock() throws IOException {
            return ((MultiDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        public @Override
        CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getLookup().lookup(GradleDataEditor.class);
        }

    }
}