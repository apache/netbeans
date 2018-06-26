/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.doc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.lookup.Lookups;

/**
 * This class should resolve JavaScript documentation types of files.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationResolver {

    private static final Logger LOG = Logger.getLogger(JsDocumentationResolver.class.getName());
    private static JsDocumentationResolver instance;

    private static final List<? extends JsDocumentationProvider> PROVIDERS = new ArrayList<JsDocumentationProvider>(
            Lookups.forPath(JsDocumentationSupport.DOCUMENTATION_PROVIDER_PATH).lookupResult(JsDocumentationProvider.class).allInstances());

    public static synchronized JsDocumentationResolver getDefault() {
        if (instance == null) {
            instance = new JsDocumentationResolver();
        }
        return instance;
    }

    /**
     * Finds and gets JsDocumentationProvider for given snapshot.
     * @param snapshot snapshot to be examined
     * @return provider, never {@code null}; as a fallback can return {@link JsDocumentationFallbackProvider}
     */
    public JsDocumentationProvider getDocumentationProvider(Snapshot snapshot) {
        return findBestMatchingProvider(snapshot);
    }

    private JsDocumentationProvider findBestMatchingProvider(Snapshot snapshot) {
        Set<String> allTags = JsDocumentationReader.getAllTags(snapshot);
        float max = -1.0f;
        JsDocumentationProvider bestProvider = null;
        for (JsDocumentationProvider jsDocumentationProvider : PROVIDERS) {
            float coverage = countTagsCoverageRatio(allTags, jsDocumentationProvider);
            if (coverage == 1.0) {
                return jsDocumentationProvider;
            } else {
                if (coverage > max) {
                    max = coverage;
                    bestProvider = jsDocumentationProvider;
                }
            }
        }
        return bestProvider != null ? bestProvider : new JsDocumentationFallbackProvider();
    }

    private float countTagsCoverageRatio(Set<String> tags, JsDocumentationProvider provider) {
        Set<String> unsupportedTags = new HashSet<String>(tags);
        unsupportedTags.removeAll(provider.getSupportedTags());
        if (unsupportedTags.isEmpty()) {
            return 1.0f;
        } else {
            float coverage = 1.0f - (1.0f / tags.size() * unsupportedTags.size());
            return coverage;
        }
    }
}
