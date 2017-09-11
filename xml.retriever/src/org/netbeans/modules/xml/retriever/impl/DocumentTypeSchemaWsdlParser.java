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

package org.netbeans.modules.xml.retriever.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.xml.retriever.*;
import org.netbeans.modules.xml.retriever.DocumentTypeParser;
import org.netbeans.modules.xml.retriever.impl.IConstants;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.netbeans.modules.xml.retriever.catalog.Utilities.HashNamespaceResolver;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author girix
 */
public class DocumentTypeSchemaWsdlParser implements DocumentTypeParser{
    
    /** Creates a new instance of DocumentTypeSchemaParser */
    public DocumentTypeSchemaWsdlParser() {
    }
    
    public boolean accept(String mimeType) {
        if(mimeType != null && mimeType.equalsIgnoreCase(DocumentTypesEnum.schema.toString()))  //noi18n
            return true;
        if(mimeType != null && mimeType.equalsIgnoreCase(DocumentTypesEnum.wsdl.toString()))  //noi18n
            return true;
        return false;
    }
    
    
    public List<String> getAllLocationOfReferencedEntities(FileObject fob) throws Exception{
        return getAllLocationOfReferencedEntities(FileUtil.toFile(fob));
    }
    
    
    private static XPath xpath = null;
    
    private void initXpath() throws Exception{
        if(xpath == null){
            xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(getNamespaceContext());
            xpath.compile(IConstants.XPATH_SCHEMA_IMPORT_LOCATION);
            xpath.compile(IConstants.XPATH_SCHEMA_INCLUDE_LOCATION);
            xpath.compile(IConstants.XPATH_SCHEMA_REDEFINE_LOCATION);
            xpath.compile(IConstants.XPATH_WSDL_IMPORT_LOCATION);
            xpath.compile(IConstants.XPATH_WSDL_TAG);
            xpath.compile(IConstants.XPATH_SCHEMA_TAG);
            
        }
    }
    
    private Map<String, String> namespaces = new HashMap<String,String>();
    private Map<String, String> prefixes = new HashMap<String,String>();
    
    private NamespaceContext getNamespaceContext() {
        namespaces.put("xsd","http://www.w3.org/2001/XMLSchema"); //NOI18N
        prefixes.put("http://www.w3.org/2001/XMLSchema", "xsd"); //NOI18N
        namespaces.put("wsdl", "http://schemas.xmlsoap.org/wsdl/"); //NOI18N
        prefixes.put("http://schemas.xmlsoap.org/wsdl/", "wsdl"); //NOI18N
        return new HashNamespaceResolver(namespaces, prefixes);
    }
    
    private Node getDOMTree(File parsedFile) throws Exception{
        DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
        dbfact.setNamespaceAware(true);
        FileObject parsedFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(parsedFile));
        Node node = dbfact.newDocumentBuilder().parse(parsedFileObject.getInputStream());
        return node;
    }
    
    
    public List<String> getAllLocationOfReferencedEntities(File parsedFile) throws Exception{
        List<String> result = new ArrayList<String>();
        initXpath();
        Node documentNode = getDOMTree(parsedFile);
        
        //scan for schema imports'
        String locationExpression = IConstants.XPATH_SCHEMA_IMPORT_LOCATION; //noi18n
        NodeList nodes = (NodeList) xpath.evaluate(locationExpression, documentNode, XPathConstants.NODESET);
        if(nodes == null) {
            locationExpression = IConstants.XPATH_SCHEMA_IMPORT_NAMESPACE; //noi18n
            nodes = (NodeList) xpath.evaluate(locationExpression, documentNode, XPathConstants.NODESET);
        }
        if((nodes != null) && (nodes.getLength() > 0)){
            for(int i=0; i<nodes.getLength();i++){
                Node node = nodes.item(i);
                result.add(node.getNodeValue());
            }
        }
        
        //scan for schema includes'
        locationExpression = IConstants.XPATH_SCHEMA_INCLUDE_LOCATION; //noi18n
        nodes = (NodeList) xpath.evaluate(locationExpression, documentNode, XPathConstants.NODESET);
        if((nodes != null) && (nodes.getLength() > 0)){
            for(int i=0; i<nodes.getLength();i++){
                result.add(nodes.item(i).getNodeValue());
            }
        }
        
        //scan for schema redefines'
        locationExpression = IConstants.XPATH_SCHEMA_REDEFINE_LOCATION; //noi18n
        nodes = (NodeList) xpath.evaluate(locationExpression, documentNode, XPathConstants.NODESET);
        if((nodes != null) && (nodes.getLength() > 0)){
            for(int i=0; i<nodes.getLength();i++){
                result.add(nodes.item(i).getNodeValue());
            }
        }
        
        //scan for wsdl imports'
        locationExpression = IConstants.XPATH_WSDL_IMPORT_LOCATION;
        nodes = (NodeList) xpath.evaluate(locationExpression, documentNode, XPathConstants.NODESET);
        if((nodes != null) && (nodes.getLength() > 0)){
            for(int i=0; i<nodes.getLength();i++){
                result.add(nodes.item(i).getNodeValue());
            }
        }
        
        return result;
    }
    
    
    public String getFileExtensionByParsing(File parsedFile) throws Exception{
        String result = null;
        initXpath();
        Node documentNode = getDOMTree(parsedFile);
        
        //scan for schema tag
        String locationExpression = IConstants.XPATH_SCHEMA_TAG;
        NodeList nodes = (NodeList) xpath.evaluate(locationExpression, documentNode, XPathConstants.NODESET);
        if((nodes != null) && (nodes.getLength() > 0)){
            return "xsd"; //noi18n
        }
        
        //scan for wsdl tag
        locationExpression = IConstants.XPATH_WSDL_TAG;
        nodes = (NodeList) xpath.evaluate(locationExpression, documentNode, XPathConstants.NODESET);
        if((nodes != null) && (nodes.getLength() > 0)){
            return "wsdl"; //noi18n
        }
        return "xml"; //noi18n
    }
    
}
