/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
            final URL rootURL = root.getURL();
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
    synchronized static JavaFileFilterListener getDefault() {
        if (instance == null) {
            instance = new JavaFileFilterListener();
        }
        return instance;
    }
}
