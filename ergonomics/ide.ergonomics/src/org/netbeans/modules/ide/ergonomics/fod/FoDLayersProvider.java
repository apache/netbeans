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
package org.netbeans.modules.ide.ergonomics.fod;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Jirka Rechtacek
 */
@ServiceProviders({
    @ServiceProvider(service=Repository.LayerProvider.class),
    @ServiceProvider(service=FoDLayersProvider.class)
})
public final class FoDLayersProvider extends Repository.LayerProvider
implements LookupListener, Runnable {
    static final Logger LOG = Logger.getLogger (FoDLayersProvider.class.getPackage().getName());
    private RequestProcessor.Task refresh = FeatureManager.getInstance().create(this, true);
    private Lookup.Result<ProjectFactory> factories;
    private Lookup.Result<?> ants;

    public static FoDLayersProvider getInstance() {
        return Lookup.getDefault().lookup(FoDLayersProvider.class);
    }

    @Override
    protected void registerLayers(Collection<? super URL> context) {
        boolean empty = true;
        LOG.fine("collecting layers"); // NOI18N
        List<URL> urls = new ArrayList<URL>();
        urls.add(0, FoDLayersProvider.class.getResource("common.xml")); // NOI18N
        for (FeatureInfo info : FeatureManager.features()) {
            if (!info.isPresent()) {
                continue;
            }
            LOG.log(Level.FINEST, "adding feature {0}", info.clusterName); // NOI18N
            if (info.getLayerURL() != null) {
                urls.add(info.getLayerURL());
            }
            if (info.isEnabled()) {
                empty = false;
            }
        }
        if (empty && noAdditionalProjects() && !FoDEditorOpened.anEditorIsOpened) {
            LOG.fine("adding default layer"); // NOI18N
            urls.add(0, FoDLayersProvider.class.getResource("default.xml")); // NOI18N
        }
        LOG.log(Level.FINE, "delegating to {0} layers", urls.size()); // NOI18N
        context.addAll(urls);
        LOG.log(Level.FINEST, "{0}", urls); // NOI18N
        LOG.fine("done");
        FeatureManager.dumpModules();
    }

    public FeatureInfo whichProvides(FileObject template) {
        Set<URL> layers = new HashSet<URL>();
        Object obj = template.getAttribute("layers");
        if (obj instanceof URL[]) {
            layers.addAll(Arrays.asList((URL[])obj));
        }
        
        for (FeatureInfo info : FeatureManager.features()) {
            if (layers.contains(info.getLayerURL())) {
                return info;
            }
        }
        return null;
    }
    
    public URL getDelegateFileSystem(FileObject template) {
        Object obj = template.getAttribute("layers");
        if (obj instanceof URL[]) {
            return ((URL[])obj)[0];
        }
        return null;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        refresh.schedule(500);
    }
    public void refreshForce() {
        super.refresh();
    }
    @Override
    public void run() {
        super.refresh();
    }
    public void waitFinished() {
        refresh.waitFinished();
    }

    private boolean noAdditionalProjects() {
        if (factories == null) {
            factories = Lookup.getDefault().lookupResult(ProjectFactory.class);
            factories.addLookupListener(this);
            
            ants = Lookup.getDefault().lookupResult(AntBasedProjectType.class);
            ants.addLookupListener(this);
        }

        for (ProjectFactory pf : factories.allInstances()) {
            if (pf.getClass().getName().contains("AntBasedProjectFactorySingleton")) { // NOI18N
                continue;
            }
            if (pf.getClass().getName().startsWith("org.netbeans.modules.ide.ergonomics")) { // NOI18N
                continue;
            }
            if (pf.getClass().getName().startsWith("org.netbeans.modules.project.ui.convertor.ProjectConvertorFactory")) { // NOI18N
                continue;
            }
            return false;
        }
        return ants.allItems().isEmpty();
    }
}
