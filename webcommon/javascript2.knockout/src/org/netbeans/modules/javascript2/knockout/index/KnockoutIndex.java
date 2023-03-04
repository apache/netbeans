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
package org.netbeans.modules.javascript2.knockout.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Roman Svitanic
 */
public class KnockoutIndex {
    private static final Map<Project, KnockoutIndex> INDEXES = new WeakHashMap<>();
    private final QuerySupport querySupport;

    public static KnockoutIndex get(Project project) throws IOException {
        if (project == null) {
            return null;
        }
        synchronized (INDEXES) {
            KnockoutIndex index = INDEXES.get(project);
            if (index == null) {
                Collection<FileObject> sourceRoots = QuerySupport.findRoots(project,
                        null /* all source roots */,
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList());
                QuerySupport querrySupport = QuerySupport.forRoots(KnockoutIndexer.Factory.NAME, KnockoutIndexer.Factory.VERSION, sourceRoots.toArray(new FileObject[]{}));
                index = new KnockoutIndex(querrySupport);
                if (sourceRoots.size() > 0) {
                    INDEXES.put(project, index);
                }
            }
            return index;
        }
    }

    private KnockoutIndex(QuerySupport querrySupport) throws IOException {
        this.querySupport = querrySupport;
    }

    public Collection<KnockoutCustomElement> getCustomElements(final String name, final boolean exact) {
        Collection<? extends IndexResult> result = null;
        try {
            result = querySupport.query(KnockoutIndexer.CUSTOM_ELEMENT, name, QuerySupport.Kind.PREFIX, KnockoutIndexer.CUSTOM_ELEMENT);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            Collection<KnockoutCustomElement> customElements = new ArrayList<>();
            for (IndexResult indexResult : result) {
                Collection<KnockoutCustomElement> possibleCustomElements = createCustomElements(indexResult);
                for (KnockoutCustomElement customElement : possibleCustomElements) {
                    if (exact && (customElement.getName().equals(name)) || (!exact && customElement.getName().startsWith(name))) {
                        customElements.add(customElement);
                    }
                }
            }
            return customElements;
        }
        return Collections.emptyList();
    }

    public Collection<String> getCustomElementParameters(final String elementName) {
        Collection<? extends IndexResult> result = null;
        try {
            result = querySupport.query(KnockoutIndexer.CUSTOM_ELEMENT, elementName, QuerySupport.Kind.PREFIX, KnockoutIndexer.CUSTOM_ELEMENT);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != null && !result.isEmpty()) {
            List<String> parameters = new ArrayList<>();
            for (IndexResult indexResult : result) {
                Collection<KnockoutCustomElement> possibleCustomElements = createCustomElements(indexResult);
                for (KnockoutCustomElement customElement : possibleCustomElements) {
                    if (customElement.getName().equals(elementName)) {
                        parameters.addAll(customElement.getParameters());
                        break;
                    }
                }
            }
            return Collections.unmodifiableList(parameters);
        }
        return Collections.emptyList();
    }

    private Collection<KnockoutCustomElement> createCustomElements(final IndexResult indexResult) {
        String[] values = indexResult.getValues(KnockoutIndexer.CUSTOM_ELEMENT);
        if (indexResult.getFile() == null) {
            return Collections.emptyList();
        }
        Collection<KnockoutCustomElement> result = new ArrayList<>(values.length);
        for (String value : values) {
            if (value != null && !value.isEmpty() && value.indexOf(':') > 0) {
                String[] split = value.split(":");
                int offset = Integer.parseInt(split[2]);
                List<String> parameters = new ArrayList<>();
                if (split.length == 4) {
                    String[] params = split[3].split(";"); //NOI18N
                    if (params.length != 0) {
                        parameters.addAll(Arrays.<String>asList(params));
                    }
                }
                result.add(new KnockoutCustomElement(split[0], split[1], parameters, indexResult.getFile().toURL(), offset));
            }
        }
        return result;
    }
}
