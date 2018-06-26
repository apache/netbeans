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
package org.netbeans.modules.javascript2.knockout.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Roman Svitanic
 */
public class KnockoutIndex {

    private static final Logger LOGGER = Logger.getLogger(KnockoutIndex.class.getSimpleName());

    private static final Map<Project, KnockoutIndex> INDEXES = new WeakHashMap<>();
    private final QuerySupport querySupport;
    private static boolean areProjectsOpen = false;

    public static KnockoutIndex get(Project project) throws IOException {
        if (project == null) {
            return null;
        }
        synchronized (INDEXES) {
            KnockoutIndex index = INDEXES.get(project);
            if (index == null) {
                if (!areProjectsOpen) {
                    try {
                        // just be sure that the projects are open
                        OpenProjects.getDefault().openProjects().get();
                    } catch (InterruptedException | ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        areProjectsOpen = true;
                    }
                }
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
