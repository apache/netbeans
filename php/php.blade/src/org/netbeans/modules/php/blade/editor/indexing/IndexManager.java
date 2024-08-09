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
package org.netbeans.modules.php.blade.editor.indexing;

import java.io.File;
import java.util.Enumeration;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.php.blade.project.BladeProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author bogdan
 */
public class IndexManager {

    public static void reindexProjectViews(Project project) {
        assert project != null;
        String[] views = BladeProjectProperties.getInstance(project).getViewsPathList();

        if (views.length > 0) {
            for (String view : views) {
                if (view.length() == 0) {
                    continue;
                }
                File viewPath = new File(view);
                if (viewPath.exists()) {
                    FileObject fileObj = FileUtil.toFileObject(viewPath);
                    Enumeration<? extends FileObject> children = fileObj.getChildren(true);
                    while (children.hasMoreElements()) {
                        FileObject file = children.nextElement();
                        if (file.isFolder()) {
                            continue;
                        }
                        IndexingManager.getDefault().refreshAllIndices(file);
                    }
                }
            }
        } else {
            //falback
            String projectDir = project.getProjectDirectory().getPath();
            File viewPath = new File(projectDir + "/views");
            if (viewPath.exists()) {
                FileObject fileObj = FileUtil.toFileObject(viewPath);
                Enumeration<? extends FileObject> children = fileObj.getChildren(true);
                while (children.hasMoreElements()) {
                    FileObject file = children.nextElement();
                    IndexingManager.getDefault().refreshAllIndices(file);
                }
            }
        }
    }

    public static void reindexFolder(File viewPath) {
        FileObject fileObj = FileUtil.toFileObject(viewPath);
        Enumeration<? extends FileObject> children = fileObj.getChildren(true);
        while (children.hasMoreElements()) {
            FileObject file = children.nextElement();
            if (file.isFolder()) {
                continue;
            }
            IndexingManager.getDefault().refreshAllIndices(file);
        }
    }

}
