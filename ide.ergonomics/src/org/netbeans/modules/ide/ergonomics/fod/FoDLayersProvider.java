/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
    final static Logger LOG = Logger.getLogger (FoDLayersProvider.class.getPackage().getName());
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
