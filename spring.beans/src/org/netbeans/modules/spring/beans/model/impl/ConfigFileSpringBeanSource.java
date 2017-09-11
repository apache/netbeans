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

package org.netbeans.modules.spring.beans.model.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeanProperty;
import org.netbeans.modules.spring.beans.BeansAttributes;
import org.netbeans.modules.spring.beans.BeansElements;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.model.SpringBeanSource;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An implementation of {@link SpringBeanSource} delegating to
 * a file or a its document in the editor.
 *
 * @author Andrei Badea
 */
public class ConfigFileSpringBeanSource implements SpringBeanSource {

    private static final Logger LOGGER = Logger.getLogger(ConfigFileSpringBeanSource.class.getName());

    private final Map<String, ConfigFileSpringBean> id2Bean = new HashMap<String, ConfigFileSpringBean>();
    private final Map<String, ConfigFileSpringBean> name2Bean = new HashMap<String, ConfigFileSpringBean>();
    private final Map<String, String> alias2Name = new HashMap<String, String>();
    private final List<ConfigFileSpringBean> beans = new ArrayList<ConfigFileSpringBean>();

    /**
     * Parses the given document.
     * Currently the implementation expects it to be a {@link BaseDocument} or null.
     *
     * @param  document the document to parse.
     */
    public void parse(BaseDocument document) throws IOException {
        FileObject fo = NbEditorUtilities.getFileObject(document);
        if (fo == null) {
            LOGGER.log(Level.WARNING, "Could not get a FileObject for document {0}", document);
            return;
        }
        LOGGER.log(Level.FINE, "Parsing {0}", fo);
        File file = FileUtil.toFile(fo);
        if (file == null) {
            LOGGER.log(Level.WARNING, "{0} resolves to a null File, aborting", fo);
            return;
        }
        new DocumentParser(file, document).run();
        LOGGER.log(Level.FINE, "Parsed {0}", fo);
    }

    public List<SpringBean> getBeans() {
        return Collections.<SpringBean>unmodifiableList(beans);
    }

    public SpringBean findBeanByID(String id) {
        return id2Bean.get(id);
    }

    public SpringBean findBean(String name) {
        SpringBean bean = findBeanByID(name);
        if (bean == null) {
            bean = name2Bean.get(name);
        }
        return bean;
    }

    public String findAliasName(String alias) {
        return alias2Name.get(alias);
    }
    
    public Set<String> getAliases() {
        return alias2Name.keySet();
    }

    /**
     * This is the actual document parser.
     */
    private final class DocumentParser implements Runnable {

        private final File file;
        private final Document document;
        
        private static final String REF_SUFFIX = "-ref"; // NOI18N
        private static final String NULL_PREFIX = "null_prefix";
        
        private XMLSyntaxSupport support;

        public DocumentParser(File file, Document document) {
            this.file = file;
            this.document = document;
        }

        public void run() {
            id2Bean.clear();
            name2Bean.clear();
            beans.clear();
            alias2Name.clear();
            Node rootNode = SpringXMLConfigEditorUtils.getDocumentRoot(document);
            if (rootNode == null) {
                return;
            }
            support = XMLSyntaxSupport.getSyntaxSupport(document);
            NodeList childNodes = rootNode.getChildNodes();

            // prefixesMap caches the prefixes for tag nemes
            // see the issue 154518
            Map<String, String> prefixesMap = new HashMap<String, String>();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                String nodeName = node.getNodeName();
                if(BeansElements.ALIAS.equals(nodeName)) {
                    parseAlias(node);
                } else if (BeansElements.BEAN.equals(nodeName)) { 
                    parseBean(node, prefixesMap);
                }
            }
        }
        
        private void parseAlias(Node node) {
            String name = getTrimmedAttr(node, BeansAttributes.NAME);
            String alias = getTrimmedAttr(node, BeansAttributes.ALIAS);
            if(StringUtils.hasText(name) && StringUtils.hasText(alias)) {
                alias2Name.put(alias, name);
            }
        }

        private void parseBean(Node node, Map<String, String> prefixesMap) {
            String id = getTrimmedAttr(node, BeansAttributes.ID); 
            String name = getTrimmedAttr(node, BeansAttributes.NAME);
            List<String> names;
            if (name != null) {
                names = Collections.unmodifiableList(StringUtils.tokenize(name, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS));
            } else {
                names = Collections.<String>emptyList();
            }
            String clazz = getTrimmedAttr(node, BeansAttributes.CLASS); 
            String parent = getTrimmedAttr(node, BeansAttributes.PARENT); 
            String factoryBean = getTrimmedAttr(node, BeansAttributes.FACTORY_BEAN); 
            String factoryMethod = getTrimmedAttr(node, BeansAttributes.FACTORY_METHOD); 
            Location location = new ConfigFileLocation(FileUtil.toFileObject(file), 
                    support.getNodeOffset(node));
            Set<SpringBeanProperty> properties = parseBeanProperties(node, prefixesMap);
            ConfigFileSpringBean bean = new ConfigFileSpringBean(id, names, clazz, parent, factoryBean, factoryMethod, properties, location);
            if (id != null) {
                addBeanID(id, bean);
            }
            for (String each : names) {
                addBeanName(each, bean);
            }
            if (clazz != null) {
                beans.add(bean);
            }
        }
        
        private Set<SpringBeanProperty> parseBeanProperties(Node node, Map<String, String> prefixesMap) {
            Map<String, SpringBeanProperty> name2Properties = new HashMap<String, SpringBeanProperty>();
            NodeList nl = node.getChildNodes();
            for(int i=0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if(BeansElements.PROPERTY.equals(n.getNodeName())) {
                    String name = getTrimmedAttr(n, BeansAttributes.NAME);
                    if(StringUtils.hasText(name) && !name2Properties.containsKey(name)) {
                        name2Properties.put(name, new ConfigFileSpringBeanProperty(name));
                    }
                }
            }
            
            // P Namespace items
            String tagName = node.getNodeName();
            String prefix = prefixesMap.get(tagName);
            if (prefix == null) {
                prefix = SpringXMLConfigEditorUtils.getPNamespacePrefix(document, 
                        support.getNodeOffset(node));
                if (prefix == null) {
                    // this is caching the case when prefix declaration is missing
                    prefixesMap.put(tagName, NULL_PREFIX);
                } else {
                    prefixesMap.put(tagName, prefix);
                }
            } else if (NULL_PREFIX.equals(prefix)) {
                prefix = null;
            }
            if(prefix != null) {
                NamedNodeMap attribs = node.getAttributes();
                for(int i = 0; i < attribs.getLength(); i++) {
                    Node attribNode = attribs.item(i);
                    String attribName = attribNode.getNodeName();
                    if(attribName.length() > prefix.length() + 1 && prefix.equals(ContextUtilities.getPrefixFromNodeName(attribName))) {
                        int endIndex = attribName.endsWith(REF_SUFFIX) ? attribName.lastIndexOf(REF_SUFFIX) : attribName.length(); 
                        String name = attribName.substring(prefix.length() + 1, endIndex);
                        if(StringUtils.hasText(name) && !name2Properties.containsKey(name)) {
                            name2Properties.put(name, new ConfigFileSpringBeanProperty(name));
                        }
                    }
                }
            }
            
            return Collections.unmodifiableSet(new HashSet<SpringBeanProperty>(name2Properties.values()));
        }

        private void addBeanID(String id, ConfigFileSpringBean bean) {
            if (id2Bean.get(id) == null) {
                id2Bean.put(id, bean);
            }
        }

        private void addBeanName(String name, ConfigFileSpringBean bean) {
            if (name2Bean.get(name) == null) {
                name2Bean.put(name, bean);
            }
        }

        private String getTrimmedAttr(Node node, String attrName) {
            String attrValue = SpringXMLConfigEditorUtils.getAttribute(node, attrName);
            if (attrValue != null) {
                attrValue = attrValue.trim();
            }
            return attrValue;
        }
    }
}
