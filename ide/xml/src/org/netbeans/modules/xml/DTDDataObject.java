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
package org.netbeans.modules.xml;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.actions.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.openide.windows.CloneableOpenSupport;
import org.netbeans.modules.xml.text.TextEditorSupport;
import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.cookies.*;
import org.netbeans.modules.xml.util.Util;
import org.netbeans.modules.xml.text.syntax.DTDKit;
import org.netbeans.spi.xml.cookies.*;
import org.openide.windows.TopComponent;
import org.xml.sax.InputSource;

/** 
 * Implementation that provides main functionality for DTD data object.
 *
 * @author Libor Kramolis
 * @author Petr Kuzel
 */
@MIMEResolver.ExtensionRegistration(
    displayName="org.netbeans.modules.xml.resources.Bundle#DTDResolver",
    extension="dtd",
    mimeType="text/x-dtd",
    position=60002
)
public final class DTDDataObject extends MultiDataObject implements XMLDataObjectLook {
    public static final String DTD_MIME_TYPE = "text/x-dtd";

    /** generated Serialized Version UID */
    private static final long serialVersionUID = 2890472952957502631L;

    /** Synchronization implementation delegate. */
    private Reference<XMLSyncSupport> refSync;
    
    /** Cookie Manager */
    private final DataObjectCookieManager cookieManager;

    public DTDDataObject (final FileObject obj, final UniFileLoader loader) throws DataObjectExistsException {
        super (obj, loader);
                
        CookieSet set = getCookieSet();
        set.add (cookieManager = new DataObjectCookieManager (this, set));
        
         
        final TextEditorSupport.TextEditorSupportFactory editorFactory =
            TextEditorSupport.findEditorSupportFactory (this, DTDKit.MIME_TYPE);
        editorFactory.registerCookies (set);

        InputSource in = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(in, CheckXMLSupport.CHECK_PARAMETER_ENTITY_MODE));
        //??? This strange line registers updater of mu cookie set
        new CookieManager (this, set, DTDCookieFactoryCreator.class);
        //enable "Save As"
        set.assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                editorFactory.createEditor().saveAs( folder, fileName );
            }
        });
    }

    @MultiViewElement.Registration(
        displayName="org.netbeans.modules.xml.Bundle#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/xml/resources/dtdObject.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="dtd.text",
        mimeType=DTDKit.MIME_TYPE,
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
    @Override
    protected Node createNodeDelegate () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("--> DTDDataObject.createNodeDelegate: this = " + this);

        DataNodeCreator dataNodeCreator = Lookup.getDefault().lookup (DataNodeCreator.class);
        DataNode dataNode = null;

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("-*- DTDD   O     .createNodeDelegate: dataNodeCreator = " + dataNodeCreator);

        if ( dataNodeCreator != null ) {
            dataNode = dataNodeCreator.createDataNode (this);
        } else {
            dataNode = new DTDDataNode (this);
        }

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("<-- DTDDataObject.createNodeDelegate: dataNode = " + dataNode);

        return dataNode;
    }

    private synchronized Synchronizator getSyncIfAvailable() {
        if (refSync == null) {
            return null;
        }
        return refSync.get();
    }
    
    /** @return provider of sync interface.  */
    public synchronized Synchronizator getSyncInterface() {
        Synchronizator sync = null;
        if (refSync != null) {
            sync = refSync.get();
        }
        if (sync != null) {
            return sync;
        }
        sync = new DTDSyncSupport(this);       
        refSync = new WeakReference(sync);
        return sync;
    }

    // ~~~~~~~~~~~~~~~~~ COOKIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`


    /** Synchronize and delegate to super. */
    @Override
    public Node.Cookie getCookie(Class klass) {

        Node.Cookie cake = null;
        boolean change = false;

        // take lock to prevent deadlock on cookie set that can be called
        // from other thred during cookie removal
        synchronized (this) {
            cake = super.getCookie (klass);

            if ( ( cake == null ) &&
                 ( CloneableOpenSupport.class == klass ) ) { //!!! HACK -- backward compatibility
                cake = super.getCookie (OpenCookie.class);
            }
        }
                
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("DTD cookie query " + klass + " => " + cake); // NOI18N

        return cake;
    }
    
    
    public DataObjectCookieManager getCookieManager () {
        return cookieManager;
    }
    

    @Override
    public HelpCtx getHelpCtx() {
        //return new HelpCtx(DTDDataObject.class);
        return HelpCtx.DEFAULT_HELP;
    }
        

    //
    // class DTDDataNode
    //

    /**
     *
     */
    private static class DTDDataNode extends DataNode {

        /** Create new DTDDataNode. */
        public DTDDataNode (DTDDataObject obj) {
            super (obj, Children.LEAF);

            setDefaultAction (SystemAction.get (EditAction.class));
            setIconBase ("org/netbeans/modules/xml/resources/dtdObject"); // NOI18N
            setShortDescription (Util.THIS.getString (DTDDataObject.class,
                    "PROP_DTDDataNode_description"));
        }

        @Override
        public HelpCtx getHelpCtx() {
            //return new HelpCtx(DTDDataObject.class);
            return HelpCtx.DEFAULT_HELP;
        }
        
    } // end of class DTDDataNode



    //
    // interface DataNodeCreator
    //

    /**
     *
     */
    public static interface DataNodeCreator {

        /**
         */
        public DataNode createDataNode (DTDDataObject dtdDO);

    } // end of interface DataNodeCreator



    //
    // interface DTDCookieFactoryCreator
    //

    /**
     *
     */
    public static interface DTDCookieFactoryCreator extends CookieFactoryCreator {
        
    } // end: interface DTDCookieFactoryCreator

}
