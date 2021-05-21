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
package org.netbeans.modules.html.angular.index;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class AngularJsIndex {

    private static final Logger LOGGER = Logger.getLogger(AngularJsIndex.class.getSimpleName());

    private static final Map<Project, AngularJsIndex> INDEXES = new WeakHashMap<>();
    private final QuerySupport querySupport;
    private static boolean areProjectsOpen = false;

    public static AngularJsIndex get(Project project) throws IOException {
        if (project == null) {
            return null;
        }
        synchronized (INDEXES) {
            AngularJsIndex index = INDEXES.get(project);
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
                QuerySupport querrySupport = QuerySupport.forRoots(AngularJsIndexer.Factory.NAME, AngularJsIndexer.Factory.VERSION, sourceRoots.toArray(new FileObject[]{}));
                index = new AngularJsIndex(querrySupport);
                if (sourceRoots.size() > 0) {
                    INDEXES.put(project, index);
                }
            }
            return index;
        }
    }

    private AngularJsIndex(QuerySupport querrySupport) throws IOException {        
        this.querySupport = querrySupport;
    }

    public Collection<AngularJsController> getControllers(final String name, final boolean exact) {
        Collection<? extends IndexResult> result = null;
        try {
            result = querySupport.query(AngularJsIndexer.FIELD_CONTROLLER, name, QuerySupport.Kind.PREFIX, AngularJsIndexer.FIELD_CONTROLLER);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            Collection<AngularJsController> controllers = new ArrayList<>();
            for (IndexResult indexResult : result) {
                Collection<AngularJsController> possibleControllers = createControllers(indexResult);
                for (AngularJsController controller : possibleControllers) {
                    if (exact && (controller.getName().equals(name)) || (!exact && controller.getName().startsWith(name))) {
                        controllers.add(controller);
                    }
                }
            }
            return controllers;
        }
        return Collections.emptyList();
    }

    public Collection<AngularJsController.ModuleConfigRegistration> getControllersForTemplate(@NonNull final URI uri) {
        Collection<? extends IndexResult> result = null;

        try {
            result = querySupport.query(AngularJsIndexer.FIELD_TEMPLATE_CONTROLLER, "", QuerySupport.Kind.PREFIX, AngularJsIndexer.FIELD_TEMPLATE_CONTROLLER);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            String path = uri.toString();
            Collection<AngularJsController.ModuleConfigRegistration> controllers = new HashSet<>();
            for (IndexResult indexResult : result) {
                String[] values = indexResult.getValues(AngularJsIndexer.FIELD_TEMPLATE_CONTROLLER);
                for (String value : values) {
                    int index = value.indexOf(':');
                    String fileNamePart = value.substring(0, index);
                    if (path.endsWith(fileNamePart)) {
                        String controllerPart = value.substring(index + 1);
                        if (controllerPart.contains(":")) { //NOI18N
                            String[] controllerNames = controllerPart.split(":"); //NOI18N
                            controllers.add(new AngularJsController.ModuleConfigRegistration(controllerNames[0], controllerNames[1]));
                        } else {
                            controllers.add(new AngularJsController.ModuleConfigRegistration(controllerPart));
                        }
                    }
                }
            }
            return controllers;
        }
        return Collections.emptyList();
    }

    public Collection<String> getComponents(final String name, final boolean exact) {
        Collection<? extends IndexResult> result = null;
        try {
            result = querySupport.query(AngularJsIndexer.FIELD_COMPONENT, name, QuerySupport.Kind.PREFIX, AngularJsIndexer.FIELD_COMPONENT);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            Collection<String> components = new ArrayList<>();
            for (IndexResult indexResult : result) {
                String[] possibleComponents = indexResult.getValues(AngularJsIndexer.FIELD_COMPONENT);
                for (String component : possibleComponents) {
                    if (exact && (component.equals(name)) || (!exact && component.startsWith(name))) {
                        components.add(component);
                    }
                }
            }
            return components;
        }
        return Collections.emptyList();
    }

    private Collection<AngularJsController> createControllers(final IndexResult indexResult) {
        String[] values = indexResult.getValues(AngularJsIndexer.FIELD_CONTROLLER);
        Collection<AngularJsController> result = new ArrayList<>(values.length);
        for (String value : values) {
            if (value != null && !value.isEmpty() && value.indexOf(':') > 0) {
                String[] split = value.split(":");
                int offset = Integer.parseInt(split[2]);
                if (indexResult.getFile() != null) {
                    result.add(new AngularJsController(split[0], split[1], indexResult.getFile().toURL(), offset));
                }
            }
        }
        return result;
    }
}
