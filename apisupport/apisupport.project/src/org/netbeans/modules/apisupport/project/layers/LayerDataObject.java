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

package org.netbeans.modules.apisupport.project.layers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

@MIMEResolver.NamespaceRegistration(
    displayName="org.netbeans.modules.apisupport.project.api.Bundle#LayerResolver.xml",
    position=320,
    doctypePublicId={
        "-//NetBeans//DTD Filesystem 1.0//EN",
        "-//NetBeans//DTD Filesystem 1.1//EN",
        "-//NetBeans//DTD Filesystem 1.2//EN"
    },
    mimeType="text/x-netbeans-layer+xml"
)
@MIMEResolver.Registration(
    displayName="org.netbeans.modules.apisupport.project.layers.Bundle#LBL_hidden_files",
    resource="../ui/resources/hidden-resolver.xml",
    position=97447
)
public class LayerDataObject extends MultiDataObject {

    private final Lookup lkp;

    public LayerDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        final CookieSet cookies = getCookieSet();
        final Lookup baseLookup = cookies.getLookup();
        lkp = new ProxyLookup(baseLookup) {
            final AtomicBoolean checked = new AtomicBoolean();
            protected @Override void beforeLookup(Template<?> template) {
                if (template.getType() == LayerHandle.class && checked.compareAndSet(false, true)) {
                    FileObject xml = getPrimaryFile();
                    Project p = FileOwnerQuery.getOwner(xml);
                    if (p != null) {
                        setLookups(baseLookup, Lookups.singleton(new LayerHandle(p, xml)));
                    }
                }
            }
        };
        registerEditor("text/x-netbeans-layer+xml", true);
        cookies.add(new ValidateXMLSupport(DataObjectAdapters.inputSource(this)));
    }
    
    protected @Override Node createNodeDelegate() {
        Node base = new DataNode(this, Children.LEAF, getLookup());
        LayerHandle handle = lkp.lookup(LayerHandle.class);
        return handle != null ? new LayerNode(base, handle, false) : base;
    }
    
    public @Override Lookup getLookup() {
        return lkp;
    }

    @Override protected int associateLookup() {
        return 1;
    }

    @Messages("Source=&Source")
    @MultiViewElement.Registration(
        displayName = "#Source",
        iconBase = LayerUtil.LAYER_ICON,
        mimeType = "text/x-netbeans-layer+xml",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "source",
        position = 1
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

}
