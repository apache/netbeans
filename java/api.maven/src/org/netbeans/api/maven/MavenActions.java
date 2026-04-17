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
package org.netbeans.api.maven;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;

/**
 * APIs related to actions over the Maven project.
 * <p>
 * A API allows to <b>declaratively register actions</b> using project's Lookup. Action descriptions
 * must be provided in {@code nbactions.xml} format and referenced from the Layer XML using URL (i.e. nbres: protocol).
 * The {@code nbactions.xml} may contain descriptors of actions and even <b>profiles</b> which will be turned into
 * {@link org.netbeans.spi.project.ProjectConfiguration}s. Actions in profiles may override the default action mappings, or supply completely new
 * actions.
 * </p>
 * <div class="nonnormative">
 * This example shows how to declare actions in a modules' layer, and register actions when <code>org.netbeans.modules.maven:test.plugin</code>
 * plugin is used by the maven project:
 * {@snippet file="META-INF/generated-layer.xml" region="LayerActionsRegistration"}
 * The referenced resource may declare both default configuration actions, and actions overrides in a specific configuration:
 * {@snippet file="org/netbeans/api/maven/test-maven-actions.xml" region="ActionsExample"}
 * </div>
 * 
 * @author sdedic
 * @since 1.1
 */
public final class MavenActions {
    
    static LookupProvider forProjectLayer(FileObject fromLayer) {
        Object o = fromLayer.getAttribute("resource");
        URL resourceURL = null;
        
        if (o instanceof URL) {
            resourceURL = (URL)o;
        } else if (o instanceof String) {
            try {
                resourceURL = new URL(URLMapper.findURL(fromLayer, URLMapper.INTERNAL), (String)o);
            } catch (MalformedURLException ex) {
                Logger.getLogger(MavenActions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        final URL finURL = resourceURL;
        return new LookupProvider() {
            @Override
            public Lookup createAdditionalLookup(Lookup baseContext) {
                Project p = baseContext.lookup(Project.class);
                if (p == null || finURL == null) {
                    return Lookup.EMPTY;
                }
                return Lookups.fixed(AbstractMavenActionsProvider.fromNbActions(p, finURL)
                );
            }
        };
    }
    
}
