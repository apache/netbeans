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
package org.netbeans.modules.web.jsfapi.spi;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Someone should provide an implementation of this class in the global lookup.
 *
 * @author marekfukala
 */
public abstract class JsfSupportProvider {

    private static final Logger LOGGER = Logger.getLogger(JsfSupportProvider.class.getName());
    //note: JsfSupport DOES hold a strong reference to the Project (which is not really 
    //necessary, may be got from FEQ all the time) so I cannot hold the JsfSupport strongly
    //in the WeakHashMap - it would never GC due to the strong ref in this GC root.
    static final Map<Project, Reference<JsfSupport>> CACHE = new WeakHashMap<Project, Reference<JsfSupport>>();

    public static JsfSupport get(Source source) {
        FileObject fo = source.getFileObject();
        if (fo == null) {
            return null;
        } else {
            return get(fo);
        }
    }

    public static JsfSupport get(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }
        return get(project);

    }

    public static synchronized JsfSupport get(Project project) {
        Reference<JsfSupport> support_ref = CACHE.get(project);
        if (support_ref != null) {
            JsfSupport support = support_ref.get();
            if (support != null) {
                //use the cached one...
                return support;
            }
        }

        //...or create new one and put to the cache
        JsfSupportProvider provider = Lookup.getDefault().lookup(JsfSupportProvider.class);
        if (provider == null) {
            LOGGER.warning("There's no instance of JsfSupportProvider registered in the global lookup!"); //NOI18N
            return null;
        }
        JsfSupport support = provider.getSupport(project);
        if (support == null) {
            LOGGER.fine(
                    String.format("The implementation %s of JsfSupportProvider returned no JsfSupport instance for project %s", //NOI18N
                    provider.getClass().getName(),
                    getProjectDisplayName(project)));
            return null;
        }
        CACHE.put(project, new WeakReference<JsfSupport>(support));
        
        return support;

    }

    private static String getProjectDisplayName(Project project) {
        return FileUtil.getFileDisplayName(project.getProjectDirectory());
    }

    public abstract JsfSupport getSupport(Project project);
}
