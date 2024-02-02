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

package org.netbeans.modules.web.taglib;

import java.io.IOException;
import org.openide.util.Lookup;
import org.xml.sax.InputSource;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.windows.TopComponent;

/** Object that provides main functionality for TLDLoader(data loader).
 * This class is final only for performance reasons,
 * can be unfinaled if desired.
 *
 */
public final class TLDDataObject extends MultiDataObject implements org.openide.nodes.CookieSet.Factory { 

    private static final boolean debug = false;
    /** Editor support for text data object. */
    private transient volatile TLDEditorSupport editorSupport;
    /** generated Serialized Version UID */
    private static final long serialVersionUID = -7581377241494497816L;
    
      @MultiViewElement.Registration(
            displayName="#LBL_TLDEditorTab",
            iconBase="org/netbeans/modules/web/taglib/resources/tag.gif",
            persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID="tld.source",
            mimeType=TLDLoader.TLD_MIMETYPE,
            position=1
        )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    public TLDDataObject ()
    	throws DataObjectExistsException, IOException {
	super(null, null);
	if (debug) System.out.println("====> TLDDataObject():constructor"); // NOI18N
    }

    public TLDDataObject (final FileObject obj, final MultiFileLoader loader)
	throws DataObjectExistsException, IOException {
	super (obj, loader);
        
        getCookieSet().add(TLDEditorSupport.class, this);
        
        // Creates Check XML and Validate XML context actions
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        getCookieSet().assign(FileEncodingQueryImplementation.class, XmlFileEncodingQueryImpl.singleton());
	if (debug) System.out.println("====> TLDDataObject(FileObject, loader):constructor()"); // NOI18N

	//
	// Sometimes the FileObject is not valid. This most usually
	// occurs when the tag library exists in a source controlled
	// filesystem such as a CVS or Teamware filesystem, has been
	// checked in and then deleted. The source control system
	// reports the existence of the FileObject, but the filesystem
	// does not.  In this case we throw an IOException, and the
	// data object does not get built.
	//
        /*
	if (!isValid(obj)) {
	    
	    MessageFormat msgFormat =
		new MessageFormat(resbundle.getString("TLDDataObject_FileDoesntExist"));    // NOI18N
	    Object[] arg0 = new Object[] {getPrimaryFile().getName()};
	    // PENDING: somehow we seem to be doing nothing here. 
	    //String msg = msgFormat.format(arg0);
	    //System.out.println(msg);
	    //throw new IOException(msg);
	}
        */
    }
    
    /**
     * Provides node that should represent this data object. When a node for
     * representation in a parent is requested by a call to getNode (parent)
     * it is the exact copy of this node
     * with only parent changed. This implementation creates instance
     * <CODE>DataNode</CODE>.
     * <P>
     * This method is called only once.
     *
     * @return the node representation for this data object
     * @see DataNode
     */
    @Override
    protected synchronized Node createNodeDelegate () {
	return new TLDNode(this);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

     // Accessibility from TXTEditorSupport:
    org.openide.nodes.CookieSet getCookieSet0() {
        return getCookieSet();
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    public Taglib getTaglib() throws java.io.IOException {
        java.io.InputStream is = getPrimaryFile().getInputStream();
        try {
            try {
                return Taglib.createGraph(is);
            } finally {
                is.close();
            }
        } catch (RuntimeException ex) {
            throw new java.io.IOException(ex.getMessage());
        }
    }
    
    public void write(Taglib taglib) throws java.io.IOException {
        java.io.File file = org.openide.filesystems.FileUtil.toFile(getPrimaryFile());
        org.openide.filesystems.FileObject tldFO = getPrimaryFile();
        try {
            org.openide.filesystems.FileLock lock = tldFO.lock();
            try {
                java.io.OutputStream os = tldFO.getOutputStream(lock);
                try {
                    String version=taglib.getAttributeValue("version"); //NOI18N
                    if (version==null) { //JSP1.2 version
                        taglib.changeDocType("-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN", //NOI18N
                                             "http://java.sun.com/dtd/web-jsptaglibrary_1_2.dtd"); //NOI18N
                        taglib.setAttributeValue("xmlns",null); //NOI18N
                    }
                    taglib.write(os);
                } finally {
                    os.close();
                }
            } 
            finally {
                lock.releaseLock();
            }
        } catch (org.openide.filesystems.FileAlreadyLockedException ex) {
            // PENDING should write a message
        }
    }
    
    /** Implements <code>CookieSet.Factory</code> interface. */
    public Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(TLDEditorSupport.class))
            return getEditorSupport();
        else
            return null;
    }
    
    /** Gets editor support for this data object. */
    private TLDEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            synchronized(this) {
                if(editorSupport == null)
                    editorSupport = new TLDEditorSupport(this);
            }
        }
        return editorSupport;
    }
    
}
