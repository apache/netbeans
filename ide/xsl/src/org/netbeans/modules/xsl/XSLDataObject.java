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
package org.netbeans.modules.xsl;

import java.io.IOException;
import org.xml.sax.InputSource;
import javax.xml.transform.Source;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.util.Lookup;
import org.netbeans.spi.xml.cookies.*;
import org.netbeans.modules.xml.XMLDataObjectLook;
import org.netbeans.modules.xml.text.TextEditorSupport;
import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.cookies.*;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.modules.xsl.cookies.ValidateXSLSupport;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * XSL owner.
 *
 * @author Libor Kramolis
 * @author asgeir@dimonsoftware.com
 */
@MIMEResolver.NamespaceRegistration(
    displayName="org.netbeans.modules.xsl.resources.Bundle#XSLTResolver",
    acceptedExtension={"xsl", "xslt"},
    mimeType="application/xslt+xml",
    position=450,
    elementNS="http://www.w3.org/1999/XSL/Transform"
)
public final class XSLDataObject extends MultiDataObject implements XMLDataObjectLook {
    /** Serial Version UID */
    private static final long serialVersionUID = -3523066651187749549L;
    /** XSLT Mime Type. */
    public static final String MIME_TYPE = "application/xslt+xml"; // NOI18N    
    private static final String XSL_ICON_BASE =
        "org/netbeans/modules/xsl/resources/xslObject"; // NOI18N    
    private final transient DataObjectCookieManager cookieManager;
    private transient Synchronizator synchronizator;    
    
    public XSLDataObject(final FileObject obj, final UniFileLoader loader) throws DataObjectExistsException {
        super (obj, loader);

        CookieSet set = getCookieSet();
        cookieManager = new DataObjectCookieManager (this, set);
        set.add (cookieManager);
    
        // add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource (this);
        set.add(new CheckXMLSupport (is));
        set.add(new ValidateXSLSupport (is));

        // add TransformableCookie
        Source source = DataObjectAdapters.source (this);
        set.add (new TransformableSupport (source));

        // editor support defines MIME type understood by EditorKits registry         
        final TextEditorSupport.TextEditorSupportFactory editorFactory =
            new TextEditorSupport.TextEditorSupportFactory (this, MIME_TYPE);
        editorFactory.registerCookies (set);

        set.assign(XmlFileEncodingQueryImpl.class, XmlFileEncodingQueryImpl.singleton());

        set.assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                editorFactory.createEditor().saveAs( folder, fileName );
            }
        });
    }

    @MultiViewElement.Registration(
        displayName="org.netbeans.modules.xsl.Bundle#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/xsl/resources/xslObject.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="xsl.text",
        mimeType=MIME_TYPE,
        position=1
    )
    public static MultiViewEditorElement createMultiViewDTDElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
    
    /**
     */
    protected Node createNodeDelegate () {
        return new XSLDataNode (this);
    }

    
    /**
     */
    public HelpCtx getHelpCtx() {
        //return new HelpCtx (XSLDataObject.class);
        return HelpCtx.DEFAULT_HELP;
    }
    
    // XMLDataObjectLook to be deprecated ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public DataObjectCookieManager getCookieManager() {
        return cookieManager;
    }

    public synchronized Synchronizator getSyncInterface() {
        if (synchronizator == null) {
            synchronizator = new DataObjectSyncSupport (XSLDataObject.this);
        }
        return synchronizator;
    }
    
    /**
     * Redefine icon and help.
     */
    private static class XSLDataNode extends DataNode {

        /** Create new XSLDataNode. */
        public XSLDataNode (XSLDataObject obj) {
            super (obj, Children.LEAF);
            setIconBase (XSL_ICON_BASE);
            setShortDescription(NbBundle.getMessage(XSLDataObject.class, "PROP_XSLDataNode_desc"));
        }

        /**
         */
        public HelpCtx getHelpCtx() {
            //return new HelpCtx (XSLDataObject.class);
            return HelpCtx.DEFAULT_HELP;
        }
        
    }

}
