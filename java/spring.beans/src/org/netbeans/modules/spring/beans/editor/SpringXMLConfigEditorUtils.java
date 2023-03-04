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
package org.netbeans.modules.spring.beans.editor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeanProperty;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.BeansAttributes;
import org.netbeans.modules.spring.beans.BeansElements;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.TagElement;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Utility methods for Spring XML configuration file editor
 *
 * Inspired by BeansEditorUtils class from SpringIDE
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class SpringXMLConfigEditorUtils {

    private static final Logger LOGGER = Logger.getLogger(SpringXMLConfigEditorUtils.class.getName());
    public static final String BEAN_NAME_DELIMITERS = ",; "; // NOI18N

    private SpringXMLConfigEditorUtils() {
    }

    public static String getPNamespacePrefix(Document document, int offset) {
        DocumentContext context = DocumentContext.create(document, offset);
        if(context == null) {
            return null;
        }

        return context.getNamespacePrefix(ContextUtilities.P_NAMESPACE);
    }

    public static Map<String, String> getTagAttributes(Node node) {
        NamedNodeMap namedNodeMap = node.getAttributes();
        if(namedNodeMap == null || namedNodeMap.getLength() == 0) {
            return Collections.<String, String>emptyMap();
        }

        Map<String, String> attribs = new HashMap<String, String>();
        for(int i = 0; i < namedNodeMap.getLength(); i++) {
            Node attribNode = namedNodeMap.item(i);
            attribs.put(attribNode.getNodeName(), attribNode.getNodeValue());
        }

        return Collections.unmodifiableMap(attribs);
    }

    public static String getBeanPropertySetterName(String property) {
        char[] buffer = property.toCharArray();
        buffer[0] = Character.toUpperCase(buffer[0]);

        return "set" + String.valueOf(buffer); // NOI18N
    }

    public static String getBeanFactoryMethod(Node tag) {
        Node bean = getBean(tag);
        if (bean != null) {
            NamedNodeMap attribs = bean.getAttributes();
            if (attribs != null && attribs.getNamedItem(BeansAttributes.FACTORY_METHOD) != null) {
                return attribs.getNamedItem(BeansAttributes.FACTORY_METHOD).getNodeValue();
            }
        }

        return null;
    }

    public static Node getBean(Node tag) {
        if (tag == null) {
            return null;
        }

        if (tag.getNodeName().equals(BeansElements.BEAN)) {
            return tag;
        }

        if (tag.getNodeName().equals(BeansElements.LOOKUP_METHOD)
                || tag.getNodeName().equals(BeansElements.REPLACED_METHOD)
                || tag.getNodeName().equals(BeansElements.PROPERTY)) {
            Node parent = tag.getParentNode();

            if (parent != null // to prevent NPE in case the node has just been created and not yet added to the tree
                    && parent.getNodeName() != null // in cases that parent node is without node name (see #202319)
                    && parent.getNodeName().equals(BeansElements.BEAN)) { // NOI18N
                return parent;
            } else {
                return null;
            }
        }

        return null;

    }

    public static String getBeanClassName(Node tag) {
        Node bean = getBean(tag);
        if (bean != null) {
            NamedNodeMap attribs = bean.getAttributes();
            if (attribs != null && attribs.getNamedItem(BeansAttributes.CLASS) != null) { // NOI18N
                return attribs.getNamedItem(BeansAttributes.CLASS).getNodeValue(); // NOI18N
            }
        }

        return null;
    }

    public static final Node getDocumentRoot(Document doc) {
        Node retTag = null;
        //  Temporary fix for IZ#155008 until Lexer migration
        XMLSyntaxSupport syntaxSupport = null;
        try {
            syntaxSupport = XMLSyntaxSupport.getSyntaxSupport(doc);
        } catch (ClassCastException cce) {
            LOGGER.log(Level.FINE, cce.getMessage());
            syntaxSupport = XMLSyntaxSupport.createSyntaxSupport(doc);
        }
        if (syntaxSupport == null) return retTag;
        try {
            Token<XMLTokenId> tok = syntaxSupport.getNextToken(1);
            if(tok != null) {
                int off = syntaxSupport.runWithSequence(0, (TokenSequence s) -> {
                    s.move(0);
                    while (s.moveNext()) {
                        Token<XMLTokenId> t = s.token();
                        if (ContextUtilities.isTagToken(t)) {
                            return s.offset() + t.length();
                        }
                    }
                    return -1;
                });
                if (off == -1) {
                    return null;
                }
                SyntaxElement element = syntaxSupport.getElementChain(off);
                if (element != null && element.getType() == Node.ELEMENT_NODE) {
                    TagElement te = (TagElement)element;
                    if (te.isStart() || te.isSelfClosing()) {
                        if(te.getNode().getParentNode() instanceof org.w3c.dom.Document) {
                            return te.getNode();
                        }
                    }
                }
            }
        } catch (BadLocationException ex) {
            // No context support available in this case
        }

        return retTag;
    }

    public static final boolean hasAttribute(Node node, String attributeName) {
        return (node != null && node.getAttributes() != null && node.getAttributes().getNamedItem(attributeName) != null);
    }

    public static final String getAttribute(Node node, String attributeName) {
        if (hasAttribute(node, attributeName)) {
            return node.getAttributes().getNamedItem(attributeName).getNodeValue();
        }
        return null;
    }

    public static boolean openFile(File file, int offset) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            return openFile(fo, offset);
        }
        return false;
    }

    public static boolean openFile(FileObject fo, int offset) {
        DataObject dataObject;
        boolean opened = false;
        try {
            dataObject = DataObject.find(fo);
            if (offset > 0) {
                opened = openFileAtOffset(dataObject, offset);
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return false;
        }
        if (opened) {
            return true;
        } else {
            OpenCookie oc = dataObject.getCookie(org.openide.cookies.OpenCookie.class);
            if (oc != null) {
                oc.open();
                return true;
            }
        }
        return false;
    }

    private static boolean openFileAtOffset(DataObject dataObject, int offset) throws IOException {
        EditorCookie ec = dataObject.getCookie(EditorCookie.class);
        LineCookie lc = dataObject.getCookie(LineCookie.class);
        if (ec != null && lc != null) {
            StyledDocument doc = ec.openDocument();
            if (doc != null) {
                int lineNumber = NbDocument.findLineNumber(doc, offset);
                if (lineNumber != -1) {
                    Line line = lc.getLineSet().getCurrent(lineNumber);
                    if (line != null) {
                        int lineOffset = NbDocument.findLineOffset(doc, lineNumber);
                        int column = offset - lineOffset;
                        line.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static SpringBean getMergedBean(SpringBean origBean, FileObject fileObject) {
        if(origBean == null) {
            return null;
        }

        if(origBean.getParent() == null) {
            return origBean;
        }

        ModelBasedSpringBean logicalBean = new ModelBasedSpringBean(origBean, fileObject);
        return getMergedBean(logicalBean, fileObject);
    }

    public static SpringBean getMergedBean(Map<String, String> beanAttribs, FileObject fileObject) {

        NodeBasedSpringBean logicalBean = new NodeBasedSpringBean(beanAttribs);
        if (!StringUtils.hasText(logicalBean.getParent())) {
            return logicalBean;
        }

        SpringBean mergedBean = getMergedBean(logicalBean, fileObject);
        return mergedBean != null ? mergedBean : logicalBean;
    }

    private static SpringBean getMergedBean(MutableSpringBean startBean, FileObject fileObject) {
        final MutableSpringBean[] logicalBean = { startBean };
        SpringConfigModel model = SpringConfigModel.forFileObject(fileObject);
        if (model == null) {
            return null;
        }

        try {
            model.runReadAction(new Action<SpringBeans>() {

                public void run(SpringBeans springBeans) {
                    String currParent = logicalBean[0].getParent();
                    Set<SpringBean> walkedBeans = new HashSet<SpringBean>();
                    while (currParent != null && (logicalBean[0].getClassName() == null
                            || logicalBean[0].getFactoryBean() == null || logicalBean[0].getFactoryMethod() == null)) {
                        SpringBean currBean = springBeans.findBean(currParent);
                        if (currBean == null || walkedBeans.contains(currBean)) {
                            // circular dep. nullify everything
                            logicalBean[0] = null;
                            break;
                        }

                        if (logicalBean[0].getClassName() == null) {
                            logicalBean[0].setClassName(currBean.getClassName());
                        }
                        if (logicalBean[0].getFactoryBean() == null) {
                            logicalBean[0].setFactoryBean(currBean.getFactoryBean());
                        }
                        if (logicalBean[0].getFactoryMethod() == null) {
                            logicalBean[0].setFactoryMethod(currBean.getFactoryMethod());
                        }

                        walkedBeans.add(currBean);
                        currParent = currBean.getParent();
                    }
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            logicalBean[0] = null;
        }

        return logicalBean[0];
    }

    private static interface MutableSpringBean extends SpringBean {
        void setClassName(String className);
        void setFactoryBean(String factoryBean);
        void setFactoryMethod(String factoryMethod);
    }

    private static class ModelBasedSpringBean implements MutableSpringBean {
        private String className;
        private String factoryBean;
        private String factoryMethod;
        private String parent;
        private String id;
        private List<String> names;
        private Location location;

        public ModelBasedSpringBean(SpringBean springBean, FileObject fileObject) {
            this.className = springBean.getClassName();
            this.factoryBean = springBean.getFactoryBean();
            this.factoryMethod = springBean.getFactoryMethod();
            this.parent = springBean.getParent();
            this.id = springBean.getId();
            this.location = springBean.getLocation();
            this.names = springBean.getNames();
        }

        public String getId() {
            return id;
        }

        public List<String> getNames() {
            return names;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getParent() {
            return parent;
        }

        public String getFactoryBean() {
            return factoryBean;
        }

        public void setFactoryBean(String factoryBean) {
            this.factoryBean = factoryBean;
        }

        public String getFactoryMethod() {
            return factoryMethod;
        }

        public void setFactoryMethod(String factoryMethod) {
            this.factoryMethod = factoryMethod;
        }

        public Location getLocation() {
            return location;
        }

        public Set<SpringBeanProperty> getProperties() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static class NodeBasedSpringBean implements MutableSpringBean {

        private String className;
        private String factoryBean;
        private String factoryMethod;
        private String parent;
        private String id;
        private List<String> names;

        public NodeBasedSpringBean(Map<String, String> beanAttribs) {
            this.className = beanAttribs.get(BeansAttributes.CLASS);
            this.factoryBean = beanAttribs.get(BeansAttributes.FACTORY_BEAN);
            this.factoryMethod = beanAttribs.get(BeansAttributes.FACTORY_METHOD);
            this.parent = beanAttribs.get(BeansAttributes.PARENT);
            this.id = beanAttribs.get(BeansAttributes.ID);

            if(beanAttribs.get(BeansAttributes.NAME) == null) { // NOI18N
                this.names = Collections.<String>emptyList();
            }
            this.names = StringUtils.tokenize(beanAttribs.get(BeansAttributes.NAME), BEAN_NAME_DELIMITERS); // NOI18N
        }

        public String getId() {
            return this.id;
        }

        public List<String> getNames() {
            return names;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getParent() {
            return this.parent;
        }

        public String getFactoryBean() {
            return this.factoryBean;
        }

        public void setFactoryBean(String factoryBean) {
            this.factoryBean = factoryBean;
        }

        public String getFactoryMethod() {
            return this.factoryMethod;
        }

        public void setFactoryMethod(String factoryMethod) {
            this.factoryMethod = factoryMethod;
        }

        public Location getLocation() {
            // Logical bean cannot have a location
            throw new UnsupportedOperationException();
        }

        public Set<SpringBeanProperty> getProperties() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
