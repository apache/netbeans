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

package org.netbeans.modules.gradle.actions;

import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.spi.actions.GradleActionsProvider;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Lookup;
import org.xml.sax.SAXException;

/**
 *
 * @author Laszlo Kishalmi
 */
public class MappingContainer {

    private static MappingContainer defaultInstance;

    final MappingContainer parent;
    Set<ActionMapping> mappings = new HashSet<>();

    public MappingContainer(MappingContainer parent) {
        this.parent = parent;
    }

    public ActionMapping findMapping(String action, Set<String> plugins) {
        ActionMapping result = null;
        for (ActionMapping mapping : mappings) {
            if (mapping.getName().equals(action) && mapping.isApplicable(plugins)) {
                if (result == null || result.compareTo(mapping) < 0) {
                    result = mapping;
                }
            }
        }
        if ((result == null) && (parent != null)) {
            result = parent.findMapping(action, plugins);
        }
        return result;
    }

    public void addMappings(Collection<ActionMapping> mappings) {
        this.mappings.addAll(mappings);
    }

    public static MappingContainer getDefault() {
        if (defaultInstance == null) {
            DefaultContainer instance = new DefaultContainer();
            instance.initialize();
            defaultInstance = instance;
        }
        return defaultInstance;
    }

    private static class DefaultContainer extends MappingContainer {

        DefaultContainer() {
            super(null);
        }

        void initialize() {
            Collection<? extends GradleActionsProvider> all = Lookup.getDefault().lookupAll(GradleActionsProvider.class);
            for (GradleActionsProvider provider : all) {
                try (InputStream is = provider.defaultActionMapConfig()) {
                    if (is != null) {
                        Set<ActionMapping> load = ActionMappingScanner.loadMappings(is);
                        addMappings(load);
                    }
                } catch (IOException | ParserConfigurationException | SAXException ex) {}

            }
        }
    }
}
