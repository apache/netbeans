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

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 * @author girix
 */
public class LSResourceResolverImpl implements LSResourceResolver {
    
    /** Creates a new instance of LSResourceResolverImpl */
    public LSResourceResolverImpl() {
    }
    
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURIStr) {
        //check for sanity of the systemID
        if((systemId == null) || (systemId.trim().length() <=0 ))
            return null;
        URI systemIdURI = null;
        try {
            systemIdURI = new URI(systemId);
        } catch (URISyntaxException ex) {
            return null;
        }
        
        FileObject baseFO = null;
        //get the resolver object
        CatalogModel depRez = null;
        try {
            baseFO = getFileObject(baseURIStr);
            depRez = getResolver(baseFO);
        } catch (CatalogModelException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
        if(depRez == null)
            return null;
        ModelSource baseMS = null;
        try {
            baseMS = org.netbeans.modules.xml.retriever.catalog.Utilities.createModelSource(baseFO, false);
        } catch (CatalogModelException ex) {
        }
        //get the model source from it
        ModelSource resultMS = null;
        try {
            resultMS = depRez.getModelSource(systemIdURI, baseMS);
        } catch (CatalogModelException ex) {
            return null;
        }
        if(resultMS == null)
            return null;
        
        //get file object
        FileObject resultFob = (FileObject) resultMS.getLookup().lookup(FileObject.class);
        if(resultFob == null)
            return null;
        
        //get file
        File resultFile = FileUtil.toFile(resultFob);
        if(resultFile == null)
            return null;
        
        //get URI out of file
        URI resultURI = resultFile.toURI();
        
        
        //create LSInput object
        DOMImplementation domImpl = null;
        try {
            
            domImpl =  DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException ex) {
            return null;
        }
        DOMImplementationLS dols = (DOMImplementationLS) domImpl.getFeature("LS","3.0");
        LSInput lsi = dols.createLSInput();
        InputStream is = getFileStreamFromDocument(resultFile);
        if(is != null)
            lsi.setByteStream(is);
        lsi.setSystemId(resultURI.toString());
        return lsi;
    }
    
    
    private FileObject getFileObject(String baseURIStr) throws IOException{
        if(baseURIStr == null)
            return null;
        URI baseURI = null;
        try {
            baseURI = new URI(baseURIStr);
        } catch (URISyntaxException ex) {
            IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
        if(baseURI.isAbsolute()){
            if(baseURI.getScheme().equalsIgnoreCase("file")){ //NOI18N
                File baseFile = null;
                try{
                    baseFile = new File(baseURI);
                }catch(Exception e){
                    IOException ioe = new IOException();
                    ioe.initCause(e);
                    throw ioe;
                }
                baseFile = FileUtil.normalizeFile(baseFile);
                FileObject baseFileObject = null;
                try{
                    baseFileObject = FileUtil.toFileObject(baseFile);
                }catch(Exception e){
                    IOException ioe = new IOException();
                    ioe.initCause(e);
                    throw ioe;
                }
                return baseFileObject;
            }
        }
        return null;
    }
    
    
    private CatalogModel getResolver(FileObject baseFileObject) throws CatalogModelException{
        if(baseFileObject != null)
            return CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(baseFileObject);
        return null;
    }
    
    private InputStream getFileStreamFromDocument(File resultFile) {
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(resultFile));
        if(fo != null){
            DataObject dobj = null;
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                return null;
            }
            if(dobj.isModified()){
                EditorCookie thisDocumentEditorCookie = (EditorCookie)dobj.getCookie(EditorCookie.class);
                if(thisDocumentEditorCookie == null)
                    return null;
                StyledDocument sd = null;
                try {
                    sd = thisDocumentEditorCookie.openDocument();
                } catch (IOException ex) {
                    return null;
                }
                if(sd == null)
                    return null;
                String docContent = null;
                try {
                    docContent = sd.getText(0, sd.getLength());
                } catch (BadLocationException ex) {
                    return null;
                }
                if(docContent == null)
                    return null;
                BufferedInputStream bis = new BufferedInputStream(new
                        ByteArrayInputStream(docContent.getBytes()));
                return bis;
            }else{
                //return null so that the validator will use normal file path to access doc
                return null;
            }
        }
        return null;
    }
    
    
    
    
    
}
