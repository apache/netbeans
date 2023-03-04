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
package org.netbeans.modules.xml;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.actions.EditAction;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;

import org.netbeans.modules.xml.text.TextEditorSupport;
import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.cookies.*;

import org.netbeans.modules.xml.util.Util;
import org.netbeans.spi.xml.cookies.*;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.xml.sax.InputSource;

/** 
 * Object that provides main functionality for XML Entity data object.
 *
 * @author Libor Kramolis
 * @version 0.1
 */
@MIMEResolver.ExtensionRegistration(
    displayName="org.netbeans.modules.xml.resources.Bundle#ENTResolver",
    extension="ent",
    mimeType="text/xml-external-parsed-entity",
    position=60003
)
public final class EntityDataObject extends MultiDataObject implements XMLDataObjectLook {
    /** Serial Version UID */
    private static final long serialVersionUID = 2909112365229995364L;
    
    /** Default XML Entity MIME type. */
    public static final String MIME_TYPE = "text/xml-external-parsed-entity"; // NOI18N

    /** Delegate sync support */
    private transient Synchronizator synchronizator;

    /** Cookie Manager */
    private final transient DataObjectCookieManager cookieManager;

    
    public EntityDataObject (final FileObject obj, final UniFileLoader loader) throws DataObjectExistsException {
        super (obj, loader);

        CookieSet set = getCookieSet();
        set.add (cookieManager = new DataObjectCookieManager (this, set));
        
        final TextEditorSupport.TextEditorSupportFactory editorFactory =
            new TextEditorSupport.TextEditorSupportFactory (this, MIME_TYPE);
        editorFactory.registerCookies (set);

//         CookieSet.Factory treeEditorFactory = new TreeEditorCookieImpl.CookieFactoryImpl (this);
//         set.add (TreeEditorCookie.class, treeEditorFactory);

        // add check cookie
        InputSource in = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(in, CheckXMLSupport.CHECK_ENTITY_MODE));
        
//         new CookieManager (this, set, EntityCookieFactoryCreator.class);
        //enable "Save As"
        set.assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                editorFactory.createEditor().saveAs( folder, fileName );
            }
        });
    }

    @MultiViewElement.Registration(
        displayName="org.netbeans.modules.xml.Bundle#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/xml/resources/entObject.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="entity.text",
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
    

    @Override
    public final Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    /**
     */
    @Override
    protected Node createNodeDelegate () {
        return new EntityDataNode (this);
    }


    /** @return provider of sync interface.  */
    public synchronized Synchronizator getSyncInterface() {
        if (synchronizator == null) {
            synchronizator = new EntitySyncSupport (this);
        
        }
        return synchronizator;
    }

    public DataObjectCookieManager getCookieManager() {
        return cookieManager;
    }

    
    /**
     */
    @Override
    public HelpCtx getHelpCtx() {
        //return new HelpCtx (EntityDataObject.class);
        return HelpCtx.DEFAULT_HELP;
    }
        

    /**
     *
     */
    private static class EntityDataNode extends DataNode {

        /** Create new EntityDataNode. */
        public EntityDataNode (EntityDataObject obj) {
            super (obj, Children.LEAF);

            setDefaultAction (SystemAction.get (EditAction.class));
            setIconBase ("org/netbeans/modules/xml/resources/entObject"); // NOI18N
            setShortDescription(Util.THIS.getString(
                    EntityDataObject.class, "PROP_EntityDataNode_desc"));
        }

        /**
         */
        @Override
        public HelpCtx getHelpCtx() {
            //return new HelpCtx (EntityDataObject.class);
            return HelpCtx.DEFAULT_HELP;
        }
        
    } // end of class EntityDataNode

}
