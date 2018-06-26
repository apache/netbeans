/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.requirejs.editor.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class RequireJsIndex {

    private static final Logger LOGGER = Logger.getLogger(RequireJsIndex.class.getSimpleName());

    private final QuerySupport querySupport;
    private static final Map<Project, RequireJsIndex> INDEXES = new WeakHashMap();
    private static boolean areProjectsOpen = false;

    public static RequireJsIndex get(Project project) throws IOException {
        if (project == null) {
            return null;
        }
        synchronized (INDEXES) {
            RequireJsIndex index = INDEXES.get(project);
            if (index == null) {
                if (!areProjectsOpen) {
                    try {
                        // just be sure that the projects are open
                        OpenProjects.getDefault().openProjects().get();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        areProjectsOpen = true;
                    }
                }
                Collection<FileObject> sourceRoots = QuerySupport.findRoots(project,
                        null /* all source roots */,
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList());
                QuerySupport querySupport = QuerySupport.forRoots(RequireJsIndexer.Factory.NAME, RequireJsIndexer.Factory.VERSION, sourceRoots.toArray(new FileObject[]{}));
                index = new RequireJsIndex(querySupport);
                if (sourceRoots.size() > 0) {
                    INDEXES.put(project, index);
                }
            }
            return index;
        }
    }

    private RequireJsIndex(QuerySupport querySupport) throws IOException {
        this.querySupport = querySupport;
    }
    
    
    public Collection<? extends TypeUsage> getExposedTypes(String module, ModelElementFactory factory) {
        Collection<? extends IndexResult> result = null;
        String moduleName = module;
        String[] parts = moduleName.split("/");
        if (parts.length > 0) {
            moduleName = parts[parts.length - 1];
        }
        try {
            result = querySupport.query(RequireJsIndexer.FIELD_MODULE_NAME, moduleName, QuerySupport.Kind.PREFIX, RequireJsIndexer.FIELD_MODULE_NAME, RequireJsIndexer.FIELD_EXPOSED_TYPES);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            Collection<TypeUsage> types = new ArrayList<TypeUsage>();
            for (IndexResult indexResult : result) {
                String[] values = indexResult.getValues(RequireJsIndexer.FIELD_EXPOSED_TYPES);
                String mn = indexResult.getValue(RequireJsIndexer.FIELD_MODULE_NAME);
                if(mn.equals(moduleName)) {
                    for (String stype : values) {
                        String[] typeParts = stype.split(":");
                        String typeName = typeParts[0];
                        int offset = Integer.parseInt(typeParts[1]);
                        boolean resolved = "1".equals(typeParts[2]);
                        types.add(factory.newType(typeName, offset, resolved));
                    }
                }
            }
            return types;
        }
        return Collections.emptyList();
    }
    
    public Map<String, String> getPathMappings(final String prefix) {
        Collection<? extends IndexResult> result = null;
        
        try {
            result = querySupport.query(RequireJsIndexer.FIELD_PATH_MAP, prefix, QuerySupport.Kind.PREFIX, RequireJsIndexer.FIELD_PATH_MAP);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            Map<String, String> mappings = new HashMap<String, String>();
            for (IndexResult indexResult : result) {
                for(String value : indexResult.getValues(RequireJsIndexer.FIELD_PATH_MAP)) {
                    String[] parts = value.split(";");
                    if (parts.length == 2 && parts[0].startsWith(prefix)) {
                        mappings.put(parts[0], parts[1]);
                    }
                }
            }
            return mappings;
        }
        return Collections.emptyMap();
    }
    
    public Collection<String> getBasePaths() {
        Collection<? extends IndexResult> result = null;
        
        try {
            result = querySupport.query(RequireJsIndexer.FIELD_BASE_PATH, "", QuerySupport.Kind.PREFIX, RequireJsIndexer.FIELD_BASE_PATH);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            List<String> paths = new ArrayList();
            for (IndexResult indexResult : result) {
                paths.addAll(Arrays.asList(indexResult.getValues(RequireJsIndexer.FIELD_BASE_PATH)));
            }
            return paths;
        }
        return Collections.emptyList();
    }
    
    public Collection<String> getUsedPlugins() {
        Collection<? extends IndexResult> result = null;
        
        try {
            result = querySupport.query(RequireJsIndexer.FIELD_USED_PLUGINS, "", QuerySupport.Kind.PREFIX, RequireJsIndexer.FIELD_USED_PLUGINS);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            HashSet<String> plugins = new HashSet<>();
            for (IndexResult indexResult : result) {
                String[] values = indexResult.getValues(RequireJsIndexer.FIELD_USED_PLUGINS);
                for (int i = 0; i < values.length; i++) {
                    String pluginNames = values[i];
                    String[] names = pluginNames.split(";");
                    for (int j = 0; j < names.length; j++) {
                        String name = names[j];
                        if (!plugins.contains(name)) {
                            plugins.add(name);
                        }
                    }
                }
            }
            return plugins;
        }
        return Collections.emptyList();
    }

    public Map<String, String> getPackages() {
        Collection<? extends IndexResult> result = null;

        try {
            result = querySupport.query(RequireJsIndexer.FIELD_PACKAGES, "", QuerySupport.Kind.PREFIX, RequireJsIndexer.FIELD_PACKAGES);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            Map<String, String> mappings = new HashMap<>();
            for (IndexResult indexResult : result) {
                for (String value : indexResult.getValues(RequireJsIndexer.FIELD_PACKAGES)) {
                    String[] parts = value.split(";");
                    if (parts.length == 2) {
                        mappings.put(parts[0], parts[1]);
                    }
                }
            }
            return mappings;
        }
        return Collections.emptyMap();
    }

    public Collection<String> getSourceRoots() {
        Collection<? extends IndexResult> result = null;

        try {
            result = querySupport.query(RequireJsIndexer.FIELD_SOURCE_ROOT, "", QuerySupport.Kind.PREFIX, RequireJsIndexer.FIELD_SOURCE_ROOT);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            Set<String> paths = new HashSet<>();
            for (IndexResult indexResult : result) {
                paths.addAll(Arrays.asList(indexResult.getValues(RequireJsIndexer.FIELD_SOURCE_ROOT)));
            }
            return paths;
        }
        return Collections.emptyList();
    }
}
