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

package org.netbeans.modules.profiler.oql.repository.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** 
 * This class provides an API to the OQL queries stored in the system<br/>
 * Currently, the API is read only
 * @author Jaroslav Bachorik
 * @version 0.1
 */
final public class OQLQueryRepository {
    final private static class Singleton {
        final private static OQLQueryRepository INSTANCE = new OQLQueryRepository();
    }

    final private static String MATCH_ALL = ".*"; // NOI18N
    final private static Logger LOGGER = Logger.getLogger(OQLQueryRepository.class.getName());

    private OQLQueryRepository() {}

    public static OQLQueryRepository getInstance() {
        return Singleton.INSTANCE;
    }

    @NonNull
    private FileObject getRepositoryRoot() {
        FileObject root = FileUtil.getConfigFile("NBProfiler/Config/OQL"); // NOI18N
        if (root == null) {
            throw new IllegalStateException("can not find OQL queries repository"); // NOI18N
        }
        return root;
    }

    @NonNull
    private String getDisplayName(@NonNull FileObject fo) {
        String dName = (String)fo.getAttribute("displayName"); // NOI18N
        return dName != null ? dName : fo.getName();
    }

    private String getDescription(FileObject fo) {
        return (String)fo.getAttribute("desc"); // NOI18N
    }

    @NonNull
    private List<? extends OQLQueryDefinition> getQueries(FileObject categoryFO, String pattern) {
        List<OQLQueryDefinition> defs = new ArrayList<OQLQueryDefinition>();
        try {
            Pattern p = Pattern.compile(pattern);
            List<FileObject> queries = sortedFOs(categoryFO.getData(false));
            for (FileObject query : queries) {
                String displayName = getDisplayName(query);
                if (p.matcher(displayName).matches()) {
                    defs.add(new OQLQueryDefinition(displayName, getDescription(query), query.asText())); // NOI18N
                }
            }
        } catch (IOException iOException) {
            LOGGER.log(Level.SEVERE, "error while retrieving query definitions", iOException); // NOI18N
        }
        return defs;
    }

    /**
     * Retrieves the list of all query categories registered in the system
     * @return Return the system of all query categories registered in the system
     */
    @NonNull
    public List<? extends OQLQueryCategory> listCategories() {
        return listCategories(MATCH_ALL);
    }

    @NonNull
    public List<? extends OQLQueryCategory> listCategories(@NonNull String pattern) {
        FileObject root = getRepositoryRoot();
        Pattern p = Pattern.compile(pattern);
        List<OQLQueryCategory> catList = new ArrayList<OQLQueryCategory>();
        List<FileObject> categories = sortedFOs(root.getFolders(false));
        for (FileObject categoryFO : categories) {
            String displayName = getDisplayName(categoryFO);
            if(p.matcher(displayName).matches()) {
                catList.add(new OQLQueryCategory(this, categoryFO.getName(),
                                                 displayName, getDescription(categoryFO)));
            }
        }
        
        return catList;
    }

    @NonNull
    public List<? extends OQLQueryDefinition> listQueries() {
        return listQueries(MATCH_ALL);
    }

    @NonNull
    public List<? extends OQLQueryDefinition> listQueries(@NonNull String pattern) {
        FileObject root = getRepositoryRoot();
        List<OQLQueryDefinition> qdefs = new ArrayList<OQLQueryDefinition>();
        for(OQLQueryCategory cat : listCategories()) {
            FileObject catFO = root.getFileObject(cat.getID());
            qdefs.addAll(getQueries(catFO, pattern));
        }
        return qdefs;
    }

    @NonNull
    public List<? extends OQLQueryDefinition> listQueries(@NonNull OQLQueryCategory category) {
        return listQueries(category, MATCH_ALL);
    }

    @NonNull
    public List<? extends OQLQueryDefinition> listQueries(@NonNull OQLQueryCategory category, @NonNull String pattern) {
        FileObject root = getRepositoryRoot();
        FileObject catFO = root.getFileObject(category.getID());

        return getQueries(catFO, pattern);
    }

    private static List<FileObject> sortedFOs(Enumeration<? extends FileObject> fos) {
        List<FileObject> list = new ArrayList<>();
        while(fos.hasMoreElements()) list.add(fos.nextElement());
        return FileUtil.getOrder(list, false);
    }
}
