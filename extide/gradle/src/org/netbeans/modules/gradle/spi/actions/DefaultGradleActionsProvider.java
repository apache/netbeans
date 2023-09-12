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

package org.netbeans.modules.gradle.spi.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.actions.ActionMappingScanner;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.xml.sax.SAXException;

/**
 *
 * @author Laszlo Kishalmi
 */
public abstract class DefaultGradleActionsProvider implements GradleActionsProvider {

    final Set<String> supportedActions;

    public DefaultGradleActionsProvider(String... actions) {
        Set<String> actionSet = new HashSet<>(Arrays.asList(actions));
        supportedActions = Collections.unmodifiableSet(actionSet);
    }
    
    @Override
    public final Set<String> getSupportedActions() {
        return supportedActions;
    }

    @Override
    public boolean isActionEnabled(String action, Project project, Lookup context) {
        return supportedActions.contains(action);
    }

    @Override
    public final InputStream defaultActionMapConfig() {
        return getClass().getResourceAsStream("action-mapping.xml"); //NOI18N
    }

    /**
     * Defines a {@link GradleActionsProvider} for a project based on layer information.
     * It should be invoked for a {@link FileObject} from the XML layer, that has the
     * following attributes:
     * <ul>
     * <li>{@code resource} : String - URL of the resource that will be streamed from
     * {@link GradleActionsProvider#defaultActionMapConfig()}
     * </ul>
     * <div class="nonnormative">
     * An example of how an action or configuration contribution can be declared in the
     * action definition stream:
     * {@snippet file="org/netbeans/modules/gradle/actions/declarative-actions.xml" region="configuration-declaration-xml"}
     * It can be then included in either Gradle Project's generic Lookup (folder
     * {@code Projects/org-netbeans-modules-gradle/Lookup} or in a plugin-specific folder:
     * {@snippet file="META-INF/generated-layer.xml" region="configprovider-declaration-xml"}
     * </div>
     * 
     * @param fo the layer file
     * @return LookupProvider instance that can be inserted into project's Lookup.
     * @throws IOException in case of an error.
     * @since 2.13
     */
    public static LookupProvider forProjectLayer(FileObject fo) throws IOException {
        Object r = fo.getAttribute("resource");
        if (!(r instanceof String)) {
            throw new IllegalArgumentException("Resource URL not found: " + fo);
        }
        URL u = new URL(r.toString());
        return new LookupProvider() {
            @Override
            public Lookup createAdditionalLookup(Lookup baseContext) {
                return Lookups.fixed(new ResourceActionsProvider(u));
            }
        };
    }
    
    private static class ResourceActionsProvider implements GradleActionsProvider {
        private final URL resourceURL;
        
        // @GuardedBy(this)
        private Set<String> actions;

        public ResourceActionsProvider(URL resourceURL) {
            this.resourceURL = resourceURL;
        }
        
        @Override
        public boolean isActionEnabled(String action, Project project, Lookup context) {
            return getSupportedActions().contains(action);
        }

        @Override
        public Set<String> getSupportedActions() {
            Set<String> ids;
            if (actions != null) {
                return actions;
            }
            
            try {
                Map<GradleExecConfiguration, Set<ActionMapping>> mapp = new HashMap<>();
                Set<ActionMapping> def = ActionMappingScanner.loadMappings(defaultActionMapConfig(), mapp);
                Set<String> nids = new HashSet<>();
                def.forEach(a -> nids.add(a.getName()));
                for (Set<ActionMapping> m : mapp.values()) {
                    m.forEach(a -> nids.add(a.getName()));
                }
                ids = nids;
            } catch (SAXException | IOException | ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
                ids = new HashSet<>();
            }
            synchronized (this) {
                actions = ids;
                return ids;
            }
        }

        @Override
        public InputStream defaultActionMapConfig() {
            try {
                return resourceURL.openStream();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return new ByteArrayInputStream(new byte[0]);
            }
        }
    }
}
