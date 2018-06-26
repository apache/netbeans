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
package org.netbeans.modules.websvc.rest.codegen.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.wizard.AbstractPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author (changed by) ads
 *
 */
public class WadlModeler extends ResourceModel {

    public WadlModeler(FileObject fo) {
        this.fileObject = fo;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }

    public State validate() {
        InputStream is = null;
        try {
            is = fileObject.getInputStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);

            Node app = doc.getDocumentElement();
            if(app == null) {
                return State.APP_MISSING;
            }
            List<Node> resourcesList = getChildNodes(app, "resources");
            if(resourcesList == null || resourcesList.size() == 0) {
                return State.RESOURCES_MISSING;
            }
            Node resources = resourcesList.get(0);

            //App base
            String base = getAttributeValue(resources, "base");
            if(base == null || base.trim().equals(""))
                return State.BASE_URL_NULL;

            //Resources
            List<Node> resourceNodes = getChildNodes(resources, "resource");
            if(resourceNodes.isEmpty())
                return State.EMPTY_RESOURCES;
        } catch (Exception ex) {
            return State.INVALID;
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        }
        return State.VALID;
    }

    public void build() throws IOException {
        State state = validate();
        if(state != State.VALID) {
            throw new IOException(
                    NbBundle.getMessage(AbstractPanel.class,
                    "MSG_"+state.value()));
        }
        InputStream is = null;
        try {
            is = fileObject.getInputStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);

            Node app = doc.getDocumentElement();
            Node resources = getChildNodes(app, "resources").get(0);    // NOI18N
            
            //App base
            this.baseUrl = getAttributeValue(resources, "base");        // NOI18N

            //Resources
            List<Node> resourceNodes = getChildNodes(resources, "resource");// NOI18N
            for (Node node: resourceNodes) {
                Resource resource = createResource(doc, node, null);
                addResource(resource);
                Collection<Method> methods = buildResource(resource, doc, node);
                for (Method method : methods) {
                    resource.addMethod(method);
                }
            }

        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private Resource createResource(Document doc, Node node, Resource parent ) 
        throws IOException 
    {
        try {
            String path = getAttributeValue(node, "path");          // NOI18N
            String name = findResourceNameFromPath(path);
            if ( name!=null && name.length() >0 ){
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
            }
            String parentPath = "";
            if ( parent != null ){
                name = null;
            }
            if ( parent != null && parent.getName() == null ){
                parentPath = parent.getPath();
                if ( !parentPath.endsWith("/")) {       // NOI18N
                    parentPath = parentPath +"/";       // NOI18N
                }
                path = parentPath +path;
            }
            Resource resource = new Resource(name, path);
            return resource;
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
    private String findResourceNameFromPath(String path) {
        String name = path.replaceAll("/", "");
        name = name.replace("{", "");
        name = name.replace("}", "");
        return ClientStubModel.normalizeName(name);
    }
        
    private Collection<Method> buildResource(Resource resource, Document doc, Node n) 
        throws IOException 
    {
        try {
            Collection<Method> result = new LinkedList<Method>();
            //Methods
            NodeList methods = RestUtils.getNodeList(n, "method");      // NOI18N
            if (methods != null && methods.getLength() > 0) {
                for (int j = 0; j < methods.getLength(); j++) {
                    Method restMethod = null;
                    Node method = methods.item(j);
                    String methodName = getAttributeValue(method, "id");    // NOI18N
                    String httpMethod = getAttributeValue(method, "name");   // NOI18N
                    if (httpMethod == null) {
                        httpMethod = getAttributeValue(method, "href");      // NOI18N
                        if (httpMethod == null) {
                            throw new IOException("Method do not have name or " +
                                    "href attribute for resource: " + resource.getName()); // NOI18N
                        } 
                        else {
                            String ref = httpMethod;
                            if (ref.startsWith("#")) {                  // NOI18N
                                ref = ref.substring(1);
                            }
                            method = findMethodNodeByRef(doc, ref);
                            httpMethod = getAttributeValue(method, "name");      // NOI18N
                            if ( methodName == null ){
                                methodName = getAttributeValue(method, "id");    // NOI18N
                            }
                            restMethod = createMethod(resource, methodName, 
                                    httpMethod, method);
                        }
                    } 
                    else {
                        restMethod = createMethod(resource, methodName, 
                                httpMethod, method);
                    }
                    result.add( restMethod );
                }
            }
            NodeList resources = RestUtils.getNodeList(n, "resource");      // NOI18N
            for (int i=0; i<resources.getLength(); i++ ){
                Node item = resources.item(i);
                if ( item.getChildNodes().getLength() >0 ){
                    Resource subResource = createResource(doc, item, resource );
                    Collection<Method> resourceMethods = buildResource(subResource, 
                            doc, item);
                    result.addAll( resourceMethods );
                }
            }
            return result;
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
    private List<Node> getChildNodes(Node n, String name) {
        List<Node> childNodes = new ArrayList<Node>();
        NodeList childs = n.getChildNodes();
        if(childs != null) {
            for(int i=0;i<childs.getLength();i++) {
                Node child = childs.item(i);
                String cName = child.getNodeName();
                if(cName.indexOf(":")!=-1) {
                    cName = cName.substring(cName.indexOf(":")+1);
                }
                if(cName.equals(name) && (child.getNamespaceURI() == null || 
                        child.getNamespaceURI().equals(n.getNamespaceURI())))
                    childNodes.add(child);
            }
        }
        return childNodes;
    }
    
    private String getAttributeValue(Node attr, String name) {
        NamedNodeMap mAttrList = attr.getAttributes();
        Attr refAttr = (Attr) mAttrList.getNamedItem(name);
        if (refAttr != null) {
            return refAttr.getNodeValue();
        }
        return null;
    }

    private Node findMethodNodeByRef(Node doc, String ref) throws XPathExpressionException {
        Node method = null;
        NodeList methods = RestUtils.getNodeList(doc, "//application/method");
        if (methods != null && methods.getLength() > 0) {
            for (int j = 0; j < methods.getLength(); j++) {
                method = methods.item(j);
                NamedNodeMap mAttrList = method.getAttributes();
                Attr idAttr = (Attr) mAttrList.getNamedItem("id");
                if (idAttr != null) {
                    String mName = idAttr.getNodeValue();
                    if (mName.equals(ref)) {
                        return method;
                    }
                }
            }
        }
        return method;
    }

    private Method createMethod(Resource resource, String methodName, 
            String httpMethod, Node method) throws XPathExpressionException 
    {
        if (methodName != null) {
            Method restMethod = new Method(methodName.toLowerCase());
            restMethod.setType(HttpMethodType.valueOf(httpMethod));
            NodeList requests = RestUtils.getNodeList(method, "request");   // NOI18N
            boolean isVoid = true;
            if (requests != null && requests.getLength() > 0) {
                List<String> mimes = getMimes( requests.item(0) );
                restMethod.setRequestMimes(mimes);
                isVoid = mimes.isEmpty() ;
            }
            restMethod.setParamType( new RestEntity( isVoid ) );
            isVoid = true;
            NodeList responses = RestUtils.getNodeList(method, "response"); // NOI18N
            if (responses != null && responses.getLength() > 0) {
                List<String> mimes = getMimes( responses.item(0) );
                restMethod.setResponseMimes(mimes);
                isVoid = mimes.isEmpty();
            }
            restMethod.setReturnType( new RestEntity( isVoid ) );
            if ( resource.getName() == null ){
                restMethod.setPath( resource.getPath() );
            }
            return restMethod;
        }
        return null;
    }

    private List<String> getMimes( Node node ) throws XPathExpressionException {
        NodeList representation = RestUtils.getNodeList(node, "representation");// NOI18N
        List<String> mimes = new LinkedList<String>();
        if (representation != null && representation.getLength() > 0) {
            for (int l = 0; l < representation.getLength(); l++) {
                Node rep = representation.item(l);
                String media = null;
                Attr mediaAttr = (Attr) rep.getAttributes().
                    getNamedItem("mediaType");                          // NOI18N
                if (mediaAttr != null) {
                    media = mediaAttr.getNodeValue();
                }
                if ( media != null ){
                    mimes.add(media);
                }
            }
        }
        return mimes;
    }
    

    private FileObject fileObject;
    private String baseUrl;
}