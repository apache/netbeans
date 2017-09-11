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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xml;

import java.beans.*;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.xml.transform.Source;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.xml.sax.*;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;
import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.windows.CloneableOpenSupport;
import org.netbeans.modules.xml.text.TextEditorSupport;
import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.cookies.*;
import org.netbeans.modules.xml.util.Util;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.spi.xml.cookies.*;
import org.openide.windows.TopComponent;

/** Object that provides main functionality for xml document.
 * Instance holds all synchronization related state information.
 * This class is final only for performance reasons,
 * can be unfinaled if desired.
 *
 * @author Libor Kramolis
 */
@MIMEResolver.Registration(
    displayName="org.netbeans.modules.xml.resources.Bundle#XMLFallbackResolver",
    position=999000,
    resource="resources/xml-fallback-resolver.xml"
)
public final class XMLDataObject extends org.openide.loaders.XMLDataObject
        implements XMLDataObjectLook, PropertyChangeListener {
    
    @MIMEResolver.Registration(
        displayName="org.netbeans.modules.xml.resources.Bundle#XMLFirstResolver",
        position=60001,
        resource="resources/xml-mime-resolver-basic.xml",
        showInFileChooser="#ResourceFiles"
    )
    /**
     * Special MIME type so that other XML data objects do not inherit our editor
     */
    public static final String MIME_PLAIN_XML = "text/plain+xml";
    
    @MIMEResolver.Registration(
        displayName = "org.netbeans.modules.xml.resources.Bundle#XMLFirstResolver",
    position = 60004,
    resource = "resources/xml-mime-resolver-other.xml")
    /**
     * XSD pseudo-MIME type
     */
    public static final String MIME_XSD_XML = "text/xsd+xml";

    /** Serial Version UID */
    private static final long serialVersionUID = 9153823984913876866L;
    
    /** Synchronization implementation delegate. */
    private Reference<XMLSyncSupport> refSync;
    
    /** Cookie Manager */
    private final DataObjectCookieManager cookieManager;
    
    /**
     * Factory for editor
     */
    private TextEditorSupport.TextEditorSupportFactory editorSupportFactory;

    /** Create new XMLDataObject
     *
     * @param fo the primary file object
     * @param loader loader of this data object
     */
    public XMLDataObject (final FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super (fo, loader, false);
        
        CookieSet set = getCookieSet();
        set.add (cookieManager = new DataObjectCookieManager (this, set));
        editorSupportFactory =
            TextEditorSupport.findEditorSupportFactory (this, null);
        
        editorSupportFactory.registerCookies (set);
        CookieSet.Factory viewCookieFactory = new ViewCookieFactory();
        set.add (ViewCookie.class, viewCookieFactory);
        InputSource is = DataObjectAdapters.inputSource (this);
        //enable "Save As"
        set.assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                editorSupportFactory.createEditor().saveAs( folder, fileName );
            }
        });
        
        // add check and validate cookies
        set.add (new CheckXMLSupport (is));
        set.add (new ValidateXMLSupport (is));        
        // add TransformableCookie
        Source source = DataObjectAdapters.source (this);
        set.add (new TransformableSupport (source));
        new CookieManager (this, set, XMLCookieFactoryCreator.class);
        this.addPropertyChangeListener (this);  //??? - strange be aware of firing cycles
    }
    
    @MultiViewElement.Registration(
        displayName="org.netbeans.modules.xml.Bundle#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/xml/resources/xmlObject.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="xml.text",
        mimeType=MIME_PLAIN_XML,
        position=1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }
        
    @MultiViewElement.Registration(
        displayName="org.netbeans.modules.xml.Bundle#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/xml/resources/xmlObject.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="xml.text",
        mimeType=MIME_XSD_XML,
        position=1
    )
    public static MultiViewEditorElement createMultiViewXSDEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override protected int associateLookup() {
        return 1;
    }


    /**
     */
    @Override
    protected Node createNodeDelegate () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("--> XMLDataObject.createNodeDelegate: this = " + this);

        DataNodeCreator dataNodeCreator = (DataNodeCreator) Lookup.getDefault().lookup (DataNodeCreator.class);
        Node dataNode = null;

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("-*- XMLD   O     .createNodeDelegate: dataNodeCreator = " + dataNodeCreator);

        if ( dataNodeCreator != null ) {
            dataNode = dataNodeCreator.createDataNode (this);
        } else {
            Lookup env = Environment.find(this);
            dataNode = env == null ? null : env.lookup(Node.class);
            if (dataNode == null) {
                dataNode = new XMLDataNode (this);
            }
        }

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("<-- XMLDataObject.createNodeDelegate: dataNode = " + dataNode);

        return dataNode;
    }

    /**
     * Get 'semantics' node delegate from superclass.
     */
    Node createDefaultNodeDelegate () {
        return super.createNodeDelegate();  //it is a FilterNode
    }

    /** Delegate to super with possible debug messages. */
    @Override
    public void setModified (boolean state) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("XMLDataObject:setModified: state = " + state); // NOI18N

        super.setModified (state);
    }


    /** Delegate to super with possible debug messages. */
    @Override
    public org.openide.nodes.Node.Cookie getCookie(Class klass) {                       
        Node.Cookie cake = null;

        if (SaveCookie.class.equals (klass) ) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("XMLDataObject::getCookie");//, new RuntimeException ("Save cookie check")); // NOI18N
        }

        // take lock to prevent deadlock on cookie set that can be called
        // from other thread during cookie removal
        synchronized (this) {
            cake = super.getCookie (klass);

            if ( ( cake == null ) &&
                 ( CloneableOpenSupport.class == klass ) ) { //!!! HACK -- backward compatibility
                cake = super.getCookie (OpenCookie.class);
            }
        }
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("XMLDataOObject::getCookie: class = " + klass + " => " + cake); // NOI18N
        
        return cake;
    }
        
        
    public DataObjectCookieManager getCookieManager() {
        return cookieManager;
    }
    
    /** TREE -> TEXT
     * Updates document by content of parsed tree based on the 
     * last document version.
     * Note: the tree is always maximum valid part of document.
     * It takes parsed tree as primary data model. IT MUST CHANGE
     * tree must contain an error element.
     */
    public synchronized void updateDocument () {

        //!!! to be implemented without dependency on tree
        Thread.dumpStack();
//        sync.representationChanged(TreeDocument.class); //!!!

    }            
    
    private synchronized Synchronizator getSyncIfAvailable() {
        if (refSync == null) {
            return null;
        }
        return refSync.get();
    }
    
    public synchronized Synchronizator getSyncInterface() {
        Synchronizator sync = null;
        if (refSync != null) {
            sync = refSync.get();
        }
        if (sync != null) {
            return sync;
        }
        sync = new XMLSyncSupport(this);
        refSync = new WeakReference(sync);
        return sync;
    }


    


/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * LISTENERS section
 *   handlers of various listeners attached by this DataObject
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

    /** 
     * File was externaly modified, detected by OpenIDE DataObject. 
     */
    public void propertyChange (PropertyChangeEvent e) {

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("event " + e.getPropertyName()); // NOI18N
        
        if (org.openide.loaders.XMLDataObject.PROP_DOCUMENT.equals (e.getPropertyName())) {

            // filter out uninteresting events
            if (e.getOldValue() == e.getNewValue()) return;  //e.g. null == null

            Synchronizator s = getSyncIfAvailable();
            if (s != null) {
                s.representationChanged(FileObject.class);
            }
        }
    }


    @Override
    public HelpCtx getHelpCtx() {
        //return new HelpCtx(XMLDataObject.class);
        return HelpCtx.DEFAULT_HELP;
    }
    
    //
    // class XMLDataNode
    //

    /**
     *
     */
    public static class XMLDataNode extends DataNode {

        /** Create new XMLDataNode. */
        public XMLDataNode (XMLDataObject obj) {
            super (obj, Children.LEAF);
            setIconBaseWithExtension ("org/netbeans/modules/xml/resources/xmlObject.gif"); // NOI18N
            setShortDescription (Util.THIS.getString (XMLDataObject.class, "PROP_XMLDataNode_description"));
        }

    } // end of class XMLDataNode


    //
    // class ViewCookieFactory
    //

    /**
     *
     */
    private class ViewCookieFactory implements CookieSet.Factory {

        /** Creates new Cookie */
        public Node.Cookie createCookie (Class klass) {
            if (klass == ViewCookie.class) {
                return new ViewSupport (XMLDataObject.this.getPrimaryEntry());
            } else {
                return null;
            }
        }
        
    } // end of class ViewCookieFactory
    

    //
    // class ViewSupport
    //

    /**
     *
     */
    private static final class ViewSupport implements ViewCookie {

        /** entry */
        private MultiDataObject.Entry primary;
        
        /** Constructs new ViewSupport */
        public ViewSupport (MultiDataObject.Entry primary) {
            this.primary = primary;
        }
        
        /**
         */
        public void view () {
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(primary.getFile().getURL());
            } catch (FileStateInvalidException e) {
            }
        }

    } // end of class ViewSupport
    


    //
    // interface DataNodeCreator
    //

    /**
     *
     */
    public static interface DataNodeCreator {

        /**
         */
        public DataNode createDataNode (XMLDataObject xmlDO);

    } // end of interface DataNodeCreator

    

    //
    // interface XMLCookieFactoryCreator
    //

    /**
     *
     */
    public static interface XMLCookieFactoryCreator extends CookieFactoryCreator {
        
    } // end: interface XMLCookieFactoryCreator

}
