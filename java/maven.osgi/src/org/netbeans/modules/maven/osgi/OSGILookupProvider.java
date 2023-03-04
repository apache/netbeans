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


package org.netbeans.modules.maven.osgi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.ModelBuildingException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * intentional manual implementation of lookupprovider. for packagings other then bundle but with bundle-maven-plugin configuration
 * we want to have some osgi related impls in the lookup.
 * @author mkleint
 */
@LookupProvider.Registration(projectType="org-netbeans-modules-maven")
public class OSGILookupProvider implements LookupProvider {

    public OSGILookupProvider() {
    }
    
    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final InstanceContent ic = new InstanceContent();
        final Project prj = baseContext.lookup(Project.class);
        assert prj != null;
        final AccessQueryImpl access = new AccessQueryImpl(prj);
        final ForeignClassBundlerImpl bundler = new ForeignClassBundlerImpl(prj);
        final RecommendedTemplates templates = new RecommendedTemplates() {
            @Override
            public String[] getRecommendedTypes() {
                return new String[] {"osgi"};
            }
        };
        NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
        nbprj.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    checkContent(prj, ic, access, bundler, templates);
                }
            }

        });
        checkContent(prj, ic, access, bundler, templates);
        return new AbstractLookup(ic);
    }
    
    private void checkContent(Project prj, InstanceContent ic, AccessQueryImpl access, ForeignClassBundlerImpl bundler, RecommendedTemplates templates) {
        NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
        String effPackaging = nbprj.getPackagingType();
        
        boolean needToCheckFelixProjectTypes = true;
        if(!nbprj.isMavenProjectLoaded()) { 
            // issue #262646 
            // due to unfortunate ProjectManager.findPorjetc calls in awt, 
            // speed is essential during project init, so lets try to avoid
            // maven project loading if we can get the info faster from raw model.
            needToCheckFelixProjectTypes = false;
            Model model;
            try {
                model = nbprj.getRawModel();
            } catch (ModelBuildingException ex) {
                // whatever happend, we can't use the model, 
                // lets try to follow up with loading the maven project
                model = null;
                Logger.getLogger(OSGILookupProvider.class.getName()).log(Level.FINE, null, ex);
            }
            Build build = model != null ? model.getBuild() : null;
            List<Plugin> plugins = build != null ? build.getPlugins() : null;
            if(plugins != null) {
                for (Plugin plugin : plugins) {
                    if(OSGiConstants.GROUPID_FELIX.equals(plugin.getGroupId()) && OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN.equals(plugin.getArtifactId())) {
                        needToCheckFelixProjectTypes = true;
                        break;
                    }
                }
            } 
        }
        if(needToCheckFelixProjectTypes) {
            String[] types = PluginPropertyUtils.getPluginPropertyList(prj, OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN, "supportedProjectTypes", "supportedProjectType", /*"bundle" would not work for GlassFish parent POM*/null);
            if (types != null) {
                for (String type : types) {
                    if (effPackaging.equals(type)) {
                        effPackaging = NbMavenProject.TYPE_OSGI;
                    }
                }
            }
        }
        if (NbMavenProject.TYPE_OSGI.equals(effPackaging)) {
            ic.add(access);
            ic.add(bundler);
            ic.add(templates);
        } else {
            ic.remove(access);
            ic.remove(bundler);
            ic.remove(templates);
        }
    }

}
