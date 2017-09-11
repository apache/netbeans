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
