/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.navigation.graph;

import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
//import org.netbeans.modules.web.jsf.navigation.PageFlowToolbarUtilities;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneData.PageData;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem;

/**
 * @author David Kaspar
 */
public class SceneSerializer {

    private static final String SCENE_ELEMENT = "Scene"; // NOI18N
    private static final String SCENE_LAST_USED_SCOPE_ATTR = "Scope"; // NOI18N
    private static final String SCENE_SCOPE_ATTR = "Scope"; // NOI18N
    private static final String SCENE_SCOPE_ELEMENT = "Scope"; // NOI18N
    private static final String VERSION_ATTR = "version"; // NOI18NC

//    private static final String SCENE_FACES_SCOPE = PageFlowToolbarUtilities.getScopeLabel(PageFlowToolbarUtilities.Scope.SCOPE_FACESCONFIG); //NOI18N
//    private static final String SCENE_PROJECT_SCOPE = PageFlowToolbarUtilities.getScopeLabel(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT);

//    private static final String SCENE_NODE_COUNTER_ATTR = "nodeIDcounter"; // NOI18N
//    private static final String SCENE_EDGE_COUNTER_ATTR = "edgeIDcounter"; // NOI18N

    private static final String NODE_ELEMENT = "Node"; // NOI18N
    private static final String NODE_ID_ATTR = "id"; // NOI18N
    private static final String NODE_X_ATTR = "x"; // NOI18N
    private static final String NODE_Y_ATTR = "y"; // NOI18N
    private static final String NODE_ZOOM_ATTR = "zoom"; // NOI18N

//    private static final String EDGE_ELEMENT = "Edge"; // NOI18N
//    private static final String EDGE_ID_ATTR = "id"; // NOI18N
//    private static final String EDGE_SOURCE_ATTR = "source"; // NOI18N
//    private static final String EDGE_TARGET_ATTR = "target"; // NOI18N

    private static final String VERSION_VALUE_1 = "1"; // NOI18N
    private static final String VERSION_VALUE_2 = "2"; // NOI18N

    private SceneSerializer() {
    }

    // call in AWT to serialize scene
    //    public static void serialize(PageFlowScene scene, File file) {
    //        Document document = XMLUtil.createDocument(SCENE_ELEMENT, null, null, null);
    //
    //        Node sceneElement = document.getFirstChild();
    //        setAttribute(document, sceneElement, VERSION_ATTR, VERSION_VALUE_1);
    //
    //
    //
    //        //        setAttribute (document, sceneElement, SCENE_NODE_COUNTER_ATTR, Long.toString (scene.nodeIDcounter));
    //        //        setAttribute (document, sceneElement, SCENE_EDGE_COUNTER_ATTR, Long.toString (scene.edgeIDcounter));
    //
    //        for (Page page : scene.getNodes()) {
    //            Element nodeElement = document.createElement(NODE_ELEMENT);
    //            setAttribute(document, nodeElement, NODE_ID_ATTR, page.getDisplayName());
    //            Widget widget = scene.findWidget(page);
    //            Point location = widget.getPreferredLocation();
    //            if( location == null ) {
    //                location = widget.getLocation();
    //            }
    //            setAttribute(document, nodeElement, NODE_X_ATTR, Integer.toString(location.x));
    //            setAttribute(document, nodeElement, NODE_Y_ATTR, Integer.toString(location.y));
    //            sceneElement.appendChild(nodeElement);
    //        }
    //        //        for (String edge : scene.getEdges ()) {
    //        //            Element edgeElement = document.createElement (EDGE_ELEMENT);
    //        //            setAttribute (document, edgeElement, EDGE_ID_ATTR, edge);
    //        //            String sourceNode = scene.getEdgeSource (edge);
    //        //            if (sourceNode != null)
    //        //                setAttribute (document, edgeElement, EDGE_SOURCE_ATTR, sourceNode);
    //        //            String targetNode = scene.getEdgeTarget (edge);
    //        //            if (targetNode != null)
    //        //                setAttribute (document, edgeElement, EDGE_TARGET_ATTR, targetNode);
    //        //            sceneElement.appendChild (edgeElement);
    //        //        }
    //
    //        FileOutputStream fos = null;
    //        try {
    //            fos = new FileOutputStream(file);
    //            XMLUtil.write(document, fos, "UTF-8"); // NOI18N
    //        } catch (Exception e) {
    //            Exceptions.printStackTrace(e);
    //        } finally {
    //            try {
    //                if (fos != null) {
    //                    fos.close();
    //                }
    //            } catch (Exception e) {
    //                Exceptions.printStackTrace(e);
    //            }
    //        }
    //    }
    //


    public static void serialize(PageFlowSceneData sceneData, FileObject file) {
        if( file == null || !file.isValid()){
            LOG.warning("Can not serialize locations because file is null.");
            return;
        }
        LOG.entering("SceneSerializer", "serialize");
        Document document = XMLUtil.createDocument(SCENE_ELEMENT, null, null, null);

        Node sceneElement = document.getFirstChild();
        setAttribute(document, sceneElement, VERSION_ATTR, VERSION_VALUE_2);
        setAttribute(document, sceneElement, SCENE_LAST_USED_SCOPE_ATTR, XmlScope.getInstance(sceneData.getCurrentScopeStr() ).toString());
        Node scopeFacesElement = createScopeElement(document, sceneData, XmlScope.SCOPE_FACES);
        if( scopeFacesElement != null ) {
            sceneElement.appendChild( scopeFacesElement );
        }
        Node scopeProjectElement = createScopeElement(document, sceneData, XmlScope.SCOPE_PROJECT);
        if( scopeProjectElement != null ) {
            sceneElement.appendChild( scopeProjectElement );
        }
        Node scopeAllElement = createScopeElement(document, sceneData, XmlScope.SCOPE_ALL);
        if( scopeAllElement != null ) {
            sceneElement.appendChild( scopeAllElement );
        }

        writeToFile(document, file);
        LOG.finest("Serializing to the follwoing file: " + file.toString());

        LOG.exiting("SceneSerializer", "serialize");
    }
    /**
     * @param Should be either SCENE_PROJECT_SCOPR or SCENE_FACES_SCOPE
     **/
    private final static Node createScopeElement( Document document, PageFlowSceneData sceneData, XmlScope scopeXml ){
        Node sceneScopeElement =  null;
        Map<String,PageFlowSceneData.PageData> facesConfigScopeMap = sceneData.getScopeData(scopeXml.getScope());
        if( facesConfigScopeMap != null ){
            sceneScopeElement = document.createElement(SCENE_SCOPE_ELEMENT);
            setAttribute(document, sceneScopeElement, SCENE_SCOPE_ATTR, scopeXml.toString());

            for( String key : facesConfigScopeMap.keySet()){
                PageFlowSceneData.PageData data = facesConfigScopeMap.get(key);
                if ( data != null ) {
                    Element nodeElement = document.createElement(NODE_ELEMENT);
                    setAttribute(document, nodeElement, NODE_ID_ATTR, key);
                    setAttribute(document, nodeElement, NODE_X_ATTR, Integer.toString(data.getPoint().x));
                    setAttribute(document, nodeElement, NODE_Y_ATTR, Integer.toString(data.getPoint().y));
                    setAttribute(document, nodeElement, NODE_ZOOM_ATTR, Boolean.toString(data.isMinimized()));
                    sceneScopeElement.appendChild(nodeElement);
                }
            }
        }
        return sceneScopeElement;

    }

    private static synchronized void writeToFile(final Document document, final FileObject file) {
        try {
            FileSystem fs = file.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {

                @Override
                public void run() throws IOException {
                    final FileLock lock = file.lock();
                    try {
                        OutputStream fos = file.getOutputStream(lock);
                        try {
                            XMLUtil.write(document, fos, "UTF-8"); // NOI18N
                        } finally {
                            fos.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }


    private final static Logger LOG = Logger.getLogger("org.netbeans.modules.web.jsf.navigation");
    // call in AWT to deserialize scene
    public static void deserializeV1(PageFlowSceneData sceneData, FileObject file) {
        LOG.entering("SceneSerializer", "deserializeV1(PageFlowSceneData sceneData, File file)");
        Node sceneElement = getRootNode(file);


        //        scene.nodeIDcounter = Long.parseLong (getAttributeValue (sceneElement, SCENE_NODE_COUNTER_ATTR));
        //        scene.edgeIDcounter = Long.parseLong (getAttributeValue (sceneElement, SCENE_EDGE_COUNTER_ATTR));

        Map<String,PageData> sceneInfo = new HashMap<String,PageData>();
        for (Node element : getChildNode(sceneElement)) {
            if (NODE_ELEMENT.equals(element.getNodeName())) {
                String pageId = getAttributeValue(element, NODE_ID_ATTR);
                int x = Integer.parseInt(getAttributeValue(element, NODE_X_ATTR));
                int y = Integer.parseInt(getAttributeValue(element, NODE_Y_ATTR));


            }
        }
        sceneData.setScopeData(XmlScope.SCOPE_PROJECT.getScope(), sceneInfo);
        LOG.exiting("SceneSerializer", "deserialize");
    }


    public static void deserialize(PageFlowSceneData sceneData, FileObject file) {
        LOG.entering("SceneSerializer", "deserialize(PageFlowSceneData sceneData, File file)");
        Node sceneElement = getRootNode(file);
        if ( VERSION_VALUE_1.equals(getAttributeValue(sceneElement, VERSION_ATTR) )) {
            deserializeV1(sceneData, file);
        } else if ( VERSION_VALUE_2.equals(getAttributeValue(sceneElement, VERSION_ATTR))) {

            String lastUsedScopeXML = getAttributeValue(sceneElement, SCENE_LAST_USED_SCOPE_ATTR);
            XmlScope lastUsedScope = XmlScope.getInstance(lastUsedScopeXML);
            sceneData.setCurrentScope(lastUsedScope.getScope());
            LOG.fine("Last Used Scope: " + lastUsedScope);
            // TODO: Save the Last Used Scope


            NodeList scopeNodes = sceneElement.getChildNodes();
            for( int i = 0; i < scopeNodes.getLength(); i++ ){
                Node scopeElement = scopeNodes.item(i);
                if( scopeElement.getNodeName().equals(SCENE_SCOPE_ELEMENT) ){
                    String scopeXMLStr = getAttributeValue(scopeElement, SCENE_SCOPE_ATTR);
                    NodeList pageNodes = scopeElement.getChildNodes();
                    Map<String,PageData> sceneInfo = new HashMap<String,PageData>();
                    for( int j = 0; j < pageNodes.getLength(); j++ ){
                        Node pageNode = pageNodes.item(j);
                        if( pageNode.getNodeName().equals(NODE_ELEMENT)){
                            String pageDisplayName = getAttributeValue(pageNode, NODE_ID_ATTR);
                            int x = Integer.parseInt(getAttributeValue(pageNode, NODE_X_ATTR));
                            int y = Integer.parseInt(getAttributeValue(pageNode, NODE_Y_ATTR));
                            boolean isMinimized = false;
                            String zoom = getAttributeValue(pageNode, NODE_ZOOM_ATTR);
                            if(zoom != null ) {
                                isMinimized = Boolean.parseBoolean(zoom);
                            }
                            PageData data = PageFlowSceneData.createPageData(new Point(x,y), isMinimized);
                            sceneInfo.put(pageDisplayName, data);
                        }
                    }
                    sceneData.setScopeData(XmlScope.getInstance(scopeXMLStr).getScope(), sceneInfo);
                }
            }
        }

        LOG.exiting("SceneSerializer", "deserialize(PageFlowSceneData sceneData, File file)");
    }



    private static void setAttribute(Document xml, Node node, String name, String value) {
        NamedNodeMap map = node.getAttributes();
        Attr attribute = xml.createAttribute(name);
        attribute.setValue(value);
        map.setNamedItem(attribute);
    }

    private static synchronized Node getRootNode(FileObject file) {
        InputStream is = null;
        try {
            is = file.getInputStream();
            Document doc = XMLUtil.parse(new InputSource(is), false, false, new ErrorHandler() {
                public void error(SAXParseException e) throws SAXException {
                    throw new SAXException(e);
                }

                public void fatalError(SAXParseException e) throws SAXException {
                    throw new SAXException(e);
                }

                public void warning(SAXParseException e) {
                    Exceptions.printStackTrace(e);
                }
            }, null);
            return doc.getFirstChild();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }

    private static String getAttributeValue(Node node, String attr) {
        try {
            if (node != null) {
                NamedNodeMap map = node.getAttributes();
                if (map != null) {
                    Node mynode = map.getNamedItem(attr);
                    if (mynode != null) {
                        return mynode.getNodeValue();
                    }
                }
            }
        } catch (DOMException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    private static Node[] getChildNode(Node node) {
        NodeList childNodes = node.getChildNodes();
        Node[] nodes = new Node[childNodes != null ? childNodes.getLength() : 0];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = childNodes.item(i);
        }
        return nodes;
    }

}
