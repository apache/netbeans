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

package org.netbeans.modules.web.jsf;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.xml.sax.*;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.xml.cookies.*;
import org.openide.loaders.DataNode;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 *
 * @author Petr Pisl
 */
@MIMEResolver.Registration(
    displayName="org.netbeans.modules.web.jsf.resources.Bundle#JSFResolver",
    resource="resources/web-faces-mime-resolver.xml",
    position=405
)
public class JSFConfigDataObject extends MultiDataObject implements org.openide.nodes.CookieSet.Factory  {

    private static final long serialVersionUID = 1L;
    private static JSFCatalog jsfCatalog = new JSFCatalog();
    private boolean documentDirty = true;
    private boolean documentValid = true;
    protected boolean nodeDirty = false;

    private transient InputStream inputStream;
    /** Editor support for text data object. */
    private transient JSFConfigEditorSupport editorSupport;
    private transient SAXParseError error;
    private transient FacesConfig lastGoodFacesConfig = null;

    /** Property name for property documentValid */
    public static final String PROP_DOC_VALID = "documentValid"; // NOI18N


    public JSFConfigDataObject(FileObject pf, JSFConfigLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        init();
    }

    private void init() {
        CookieSet cookies = getCookieSet();

        cookies.add(JSFConfigEditorSupport.class, this);

        //Lookup JSFConfigEditorContext for Page Flow Editor multiview
        cookies.assign(JSFConfigEditorContext.class, new JSFConfigEditorContextImpl(this));

        // Creates Check XML and Validate XML context actions
        InputSource in = DataObjectAdapters.inputSource(this);
        cookies.add(new CheckXMLSupport(in));
        cookies.add(new ValidateXMLSupport(in));
        cookies.assign(FileEncodingQueryImplementation.class, XmlFileEncodingQueryImpl.singleton());
    }

    @MultiViewElement.Registration(
            mimeType=JSFConfigLoader.MIME_TYPE,
            iconBase=JSFConfigNode.ICON_BASE,
            persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID="faces.config.xml",
            displayName="#CTL_SourceTabCaption",
            position=100
    )
    @Messages("CTL_SourceTabCaption=Source")
    public static MultiViewEditorElement createXmlMultiViewElement(Lookup context) {
        return new JSFConfigMultiViewEditorElement(context);
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
	return new JSFConfigNode(this);
    }

    /** Implements <code>CookieSet.Factory</code> interface. */
    @Override
    public Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(JSFConfigEditorSupport.class))
            return getEditorSupport();
        else
            return null;
    }

    /** Gets editor support for this data object. */
    public synchronized JSFConfigEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            editorSupport = new JSFConfigEditorSupport(this);
        }

        return editorSupport;
    }



    public FacesConfig getFacesConfig() throws java.io.IOException {
        if (lastGoodFacesConfig == null)
            parsingDocument();
        return lastGoodFacesConfig;
    }

    /** This method is used for obtaining the current source of xml document.
    * First try if document is in the memory. If not, provide the input from
    * underlayed file object.
    * @return The input source from memory or from file
    * @exception IOException if some problem occurs
    */
    protected InputStream prepareInputSource() throws java.io.IOException {
        if ((getEditorSupport() != null) && (getEditorSupport().isDocumentLoaded())) {
            // loading from the memory (Document)
            return getEditorSupport().getInputStream();
        }
        else {
            return getPrimaryFile().getInputStream();
        }
    }

    /** This method has to be called everytime after prepareInputSource calling.
     * It is used for closing the stream, because it is not possible to access the
     * underlayed stream hidden in InputSource.
     * It is save to call this method without opening.
     */
    protected void closeInputSource() {
        InputStream is = inputStream;
        if (is != null) {
            try {
                is.close();
            }
            catch (IOException e) {
                // nothing to do, if exception occurs during saving.
            }
            if (is == inputStream) {
                inputStream = null;
            }
        }
    }

    /** This method parses XML document and calls updateNode method which
    * updates corresponding Node.
    */
    public void parsingDocument(){
        error = null;
        try {
            error = updateNode(prepareInputSource());
        }
        catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
            setDocumentValid(false);
            return;
        }
        finally {
            closeInputSource();
            documentDirty=false;
        }
        if (error == null){
            setDocumentValid(true);
        }else {
            setDocumentValid(false);
        }
        setNodeDirty(false);
    }

    public void setDocumentValid (boolean valid){
        if (documentValid!=valid) {
            if (valid)
                repairNode();
            documentValid=valid;
            firePropertyChange (PROP_DOC_VALID, !documentValid ? Boolean.TRUE : Boolean.FALSE, documentValid ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    /** This method repairs Node Delegate (usually after changing document by property editor)
    */
    protected void repairNode(){
        // PENDING: set the icon in Node
        // ((DataNode)getNodeDelegate()).setIconBase (getIconBaseForValidDocument());
        org.openide.awt.StatusDisplayer.getDefault().setStatusText("");  // NOI18N
    /*    if (inOut!=null) {
            inOut.closeInputOutput();
            errorAnnotation.detach();
        }*/
    }

    private org.w3c.dom.Document getDomDocument(InputStream inputSource) throws SAXParseException {
        try {
            // creating w3c document
            org.w3c.dom.Document doc = org.netbeans.modules.schema2beans.GraphManager.
                createXmlDocument(new org.xml.sax.InputSource(inputSource), false, jsfCatalog,
                new J2eeErrorHandler(this));
            return doc;
        } catch(Exception e) {
            //    XXX Change that
            throw new SAXParseException(e.getMessage(), new org.xml.sax.helpers.LocatorImpl());
        }
    }


    /** Update the node from document. This method is called after document is changed.
    * @param is Input source for the document
    * @return number of the line with error (document is invalid), 0 (xml document is valid)
    */
    // TODO is prepared for handling arrors, but not time to finish it.
    protected SAXParseError updateNode(InputStream is) throws java.io.IOException{
        return null;
    }

    public boolean isDocumentValid(){
        return documentValid;
    }
    /** setter for property documentDirty. Method updateDocument() usually setsDocumentDirty to false
    */
    public void setDocumentDirty(boolean dirty){
        documentDirty=dirty;
    }

    /** Getter for property documentDirty.
    * @return Value of property documentDirty.
    */
    public boolean isDocumentDirty(){
        return documentDirty;
    }

    /** Getter for property nodeDirty.
    * @return Value of property nodeDirty.
    */
    public boolean isNodeDirty(){
        return nodeDirty;
    }

    /** Setter for property nodeDirty.
     * @param dirty New value of property nodeDirty.
     */
    public void setNodeDirty(boolean dirty){
        nodeDirty=dirty;
    }
    org.openide.nodes.CookieSet getCookieSet0() {
        return getCookieSet();
    }

    @Override
    protected int associateLookup() {
        return 1;
    }


    public static class J2eeErrorHandler implements ErrorHandler {

        private JSFConfigDataObject dataObject;

        public J2eeErrorHandler(JSFConfigDataObject obj) {
             dataObject=obj;
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            dataObject.createSAXParseError(exception);
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            dataObject.createSAXParseError(exception);
            throw exception;
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            dataObject.createSAXParseError(exception);
            throw exception;
        }
    }

    private void createSAXParseError(SAXParseException error){
        this.error = new SAXParseError(error);
    }


}
