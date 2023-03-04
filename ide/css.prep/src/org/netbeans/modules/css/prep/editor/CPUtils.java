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
package org.netbeans.modules.css.prep.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;
import org.netbeans.modules.css.prep.editor.model.CPElementType;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.DependencyType;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class CPUtils {

    public static String SCSS_FILE_EXTENSION = "scss"; //NOI18N
    public static String SASS_FILE_EXTENSION = "sass"; //NOI18N
    public static String LESS_FILE_EXTENSION = "less"; //NOI18N
    public static String SCSS_FILE_MIMETYPE = "text/scss"; //NOI18N
    public static String LESS_FILE_MIMETYPE = "text/less"; //NOI18N

    public static boolean isCPFile(FileObject file) {
        String mt = file.getMIMEType();
        return SCSS_FILE_MIMETYPE.equals(mt) || LESS_FILE_MIMETYPE.equals(mt);
    }

    /**
     * Gets {@link CPCssIndexModel}s for all referred or related files (transitionally)
     *
     * @param allRelatedFiles if true the map will takes into account file relations regardless the direction of the imports. 
     * if false then also referred files are taken into account.
     * @param file the base file
     * @param excludeTheBaseFile if true, model for the file passed as argument is not added to the result map.
     * @return
     */
    public static Map<FileObject, CPCssIndexModel> getIndexModels(FileObject file, DependencyType dependencyType, boolean excludeTheBaseFile) throws IOException {
        Map<FileObject, CPCssIndexModel> models = new LinkedHashMap<>();
        Project project = FileOwnerQuery.getOwner(file);
        if (project != null) {
            CssIndex index = CssIndex.get(project);
        DependenciesGraph dependencies = index.getDependencies(file);
        Collection<FileObject> referred = dependencies.getFiles(dependencyType);
        for (FileObject reff : referred) {
            if (excludeTheBaseFile && reff.equals(file)) {
                //skip current file (it is included to the referred files list)
                continue;
            }
            if (file.isSymbolicLink() && reff.getPath().equals(file.readSymbolicLinkPath())) {
                continue;
            }
            if (!reff.getPath().contains(project.getProjectDirectory().getPath())) {
                continue;
            }
            
            CPCssIndexModel cpIndexModel = (CPCssIndexModel) index.getIndexModel(CPCssIndexModel.Factory.class, reff);
            if (cpIndexModel != null) {
                models.put(reff, cpIndexModel);
            }

        }
        }
        return models;
    }

    /**
     * Gets a new collection of {@link CPElementHandle}s containing just handles of the specified types.
     *
     * @param handles handles to filter
     * @param type required handle types
     * @return non null collection of filtered handles.
     */
    public static Collection<CPElementHandle> filter(Collection<CPElementHandle> handles, CPElementType... types) {
        Set<CPElementType> typesSet = EnumSet.copyOf(Arrays.asList(types));
        Collection<CPElementHandle> filtered = new ArrayList<>();
        for (CPElementHandle handle : handles) {
            if (typesSet.contains(handle.getType())) {
                filtered.add(handle);
            }
        }
        return filtered;
    }
}
