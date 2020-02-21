/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.security;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 */
public class FunctionsXmlService {
    private static final String LEVEL_TAG_NAME = "level"; // NOI18N
    private static final String[] LEVEL_TAG_ATTRIBUTES = {"value"}; // NOI18N
    private static final String CATEGORY_TAG_NAME = "category"; // NOI18N
    private static final String[] CATEGORY_TAG_ATTRIBUTES = {"name"}; // NOI18N
    private static final String FUNCTION_TAG_NAME = "function"; // NOI18N
    private static final String[] FUNCTION_TAG_ATTRIBUTES = {"name", "header"}; // NOI18N
    private static final String ALTERNATIVE_TAG_NAME = "alt"; // NOI18N
    
    public static final String ROOT_FOLDER = "CND"; // NOI18N
    public static final String CHECKS_FOLDER = "SecurityChecks"; // NOI18N
    
    private static volatile FunctionsXmlService instance;
    
    private final Map<Level, List<Category>> functions;
    private int counter = 0;
    
    public static FunctionsXmlService getInstance() {
        if (instance == null) {
            synchronized (FunctionsXmlService.class) {
                if (instance == null) {
                    instance = new FunctionsXmlService();
                }
            }            
        } else if (instance.getChecksCount() == 0) {
            synchronized (FunctionsXmlService.class) {
                if (instance.getChecksCount() == 0) {
                    instance.parseXml();
                }
            }
        }
        return instance;
    }
        
    public List<Category> getCategories(Level level) {
        return functions.get(level);
    }
    
    public int getChecksCount() {
        return counter;
    }
    
    private FunctionsXmlService() {
        functions = new HashMap<>();
        for (Level l : Level.values()) {
            functions.put(l, new LinkedList<Category>());
        }
        parseXml();
    }
    
    private void parseXml() {
        try {
            FileObject folder = FileUtil.getConfigFile(ROOT_FOLDER+"/"+CHECKS_FOLDER); // NOI18N
            if (folder != null && folder.isFolder()) {
                for (FileObject file : folder.getChildren()) {
                    Document doc = DocumentBuilderFactory.newInstance()
                                                .newDocumentBuilder()
                                                .parse(file.getInputStream());

                    NodeList levelNodes = doc.getElementsByTagName(LEVEL_TAG_NAME);

                    // iterate through all security levels
                    for (int i = 0, ilimit = levelNodes.getLength(); i < ilimit; i++) {
                        Node levelNode = levelNodes.item(i);
                        if (levelNode.getNodeType() == Node.ELEMENT_NODE) {
                            NodeList categoryNodes = levelNode.getChildNodes();

                            // iterate through all categories within level
                            for (int j = 0, jlimit = categoryNodes.getLength(); j < jlimit; j++) {
                                Node categoryNode = categoryNodes.item(j);
                                if (categoryNode.getNodeName().equals(CATEGORY_TAG_NAME) && categoryNode.getNodeType() == Node.ELEMENT_NODE) {
                                    String categoryName = ((Element) categoryNode).getAttribute(CATEGORY_TAG_ATTRIBUTES[0]);
                                    Category category = new Category(categoryName);
                                    NodeList funcNodes = categoryNode.getChildNodes();

                                    // iterate through all functions within category
                                    for (int k = 0, klimit = funcNodes.getLength(); k < klimit; k++) {
                                        Node funcNode = funcNodes.item(k);
                                        if (funcNode.getNodeName().equals(FUNCTION_TAG_NAME) && funcNode.getNodeType() == Node.ELEMENT_NODE) {
                                            String fnName = ((Element) funcNode).getAttribute(FUNCTION_TAG_ATTRIBUTES[0]);
                                            String header = ((Element) funcNode).getAttribute(FUNCTION_TAG_ATTRIBUTES[1]);
                                            RvsdFunction func = new RvsdFunction(fnName, header);
                                            counter++;

                                            NodeList alternatives = funcNode.getChildNodes();
                                            // iterate through alternatives
                                            for (int l = 0, limit = alternatives.getLength(); l < limit; l++) {
                                                Node altNode = alternatives.item(l);
                                                if (altNode.getNodeName().equals(ALTERNATIVE_TAG_NAME) && altNode.getNodeType() == Node.ELEMENT_NODE) {
                                                    if (altNode.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE) {
                                                        func.addAlternative(altNode.getFirstChild().getNodeValue().trim());
                                                    } else {
                                                        func.addAlternative(altNode.getFirstChild().getNodeValue());
                                                    }
                                                }
                                            }
                                            category.addFunction(func);
                                        }
                                    }

                                    Level value = toLevel(((Element) levelNode).getAttribute(LEVEL_TAG_ATTRIBUTES[0]));
                                    functions.get(value).add(category);
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Level toLevel(String level) {
        if (level.equals(Level.UNSAFE.name().toLowerCase(Locale.getDefault()))) {
            return Level.UNSAFE;
        } else if (level.equals(Level.AVOID.name().toLowerCase(Locale.getDefault()))) {
            return Level.AVOID;
        } else {
            return Level.CAUTION;
        }
    }
    
    public enum Level {
        UNSAFE(0),
        AVOID(1),
        CAUTION(2);
        
        private final int level;
        
        private Level(int level) {
            this.level = level;
        }
        
        public String getLevel() {
            return Integer.toString(level);
        }
    }
    
    public static class Category {
        private final String name;
        private final List<RvsdFunction> functions;
        
        private Category(String name) {
            this.name = name;
            functions = new LinkedList<>();
        }
        
        private void addFunction(RvsdFunction function) {
            functions.add(function);
        }
        
        public String getName() {
            return name;
        }
        
        public List<RvsdFunction> getFunctions() {
            return Collections.unmodifiableList(functions);
        }
    }
    
    public static class RvsdFunction {
        private final String name;
        private final String header;
        private final List<String> alternatives;
        private final StringBuilder text;
        
        private RvsdFunction(String name, String header) {
            this.name = name;
            this.header = header;
            alternatives = new LinkedList<>();
            text = new StringBuilder();
        }
        
        private void addAlternative(String alternative) {
            alternatives.add(alternative);
            if (text.length() != 0) {
                text.append(" "); // NOI18N
            }
            text.append(alternative).append(";"); // NOI18N
        }
        
        public String getName() {
            return name;
        }
        
        public String getHeader() {
            return header;
        }
        
        public String getAlternativesString() {
            return text.toString();
        }
    }
}
