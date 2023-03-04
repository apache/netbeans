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

package org.netbeans.modules.java.source.indexing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
final class JavaFileFilterListener implements ChangeListener {

    private static final String ATTR_FILTER_CFG = "filterCfg";  //NOI18N

    private static JavaFileFilterListener instance;

    private final Map<URL, JavaFileFilterImplementation> listensOn = Collections.synchronizedMap(new HashMap<URL, JavaFileFilterImplementation>());

    private JavaFileFilterListener() {
    }

    boolean startListeningOn (final FileObject root) {
        assert root != null;
        boolean result = true;
        try {
            final URL rootURL = root.toURL();
            synchronized (listensOn) {
                JavaFileFilterImplementation filter = listensOn.get(rootURL);
                if (filter == null) {
                    filter = JavaFileFilterQuery.getFilter(root);
                    if (filter != null) {
                        filter.addChangeListener(this);
                        listensOn.put(rootURL, filter);
                        result = verify(rootURL);
                    }
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (URISyntaxException use) {
            Exceptions.printStackTrace(use);
        }
        return result;
    }

    JavaFileFilterImplementation stopListeningOn (final URL rootURL) {
        assert rootURL != null;
        final JavaFileFilterImplementation filter = listensOn.remove(rootURL);
        if (filter != null) {
            filter.removeChangeListener(this);
        }
        return filter;
    }

    @Override
    public void stateChanged(final ChangeEvent event) {
        Map.Entry<URL,JavaFileFilterImplementation>[] entries;
        synchronized (this.listensOn) {
            entries = listensOn.entrySet().toArray(new Map.Entry[listensOn.size()]);
        }
        final Object source = event.getSource();
        for (Map.Entry<URL,JavaFileFilterImplementation> entry : entries) {
            if (entry.getValue().equals(source)) {
                final URL root = entry.getKey();
                try {
                    verify(root);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                } catch (URISyntaxException use) {
                    Exceptions.printStackTrace(use);
                } finally {
                    IndexingManager.getDefault().refreshIndex(root, null, true);
                }
            }
        }
    }

    private boolean verify(final URL root) throws IOException, URISyntaxException {
        boolean result = true;
        final Project p = FileOwnerQuery.getOwner(root.toURI());
        if (p != null) {
            final ProjectConfigurationProvider cp = p.getLookup().lookup(ProjectConfigurationProvider.class);
            if (cp != null) {
                final ProjectConfiguration cfg = cp.getActiveConfiguration();
                if (cfg != null) {
                    String name = cfg.getDisplayName();
                    result = !JavaIndex.ensureAttributeValue(root, ATTR_FILTER_CFG, name);
                }
            }
        }
        return result;
    }
    static synchronized JavaFileFilterListener getDefault() {
        if (instance == null) {
            instance = new JavaFileFilterListener();
        }
        return instance;
    }
}
