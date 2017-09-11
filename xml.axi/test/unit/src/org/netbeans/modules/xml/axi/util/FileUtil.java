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
package org.netbeans.modules.xml.axi.util;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponentFactory;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Samaresh
 */
public class FileUtil {
    
    private FileReader fileReader;
    
    /**
     * Creates a new instance of FileUtil
     */
    private FileUtil() {
    }
    
    public static FileUtil getInstance() {
        return new FileUtil();
    }
    
    InputSource openFile(URL url) throws Exception {
        File file = new File(url.toURI());
        if(file == null || !file.exists()) {
            return null;
        }
        fileReader = new FileReader(file);
        InputSource inputSource = new InputSource(fileReader);
        if(inputSource == null) {
            return null;
        }
        
        return inputSource;
    }
    
    void closeFile() {
        try {
            if(fileReader != null)
            fileReader.close();          
        } catch(Exception ex) {
            ex.printStackTrace();
        }        
    }

    /**
     * Uses SAX parser to parse the input XML file and
     * creates an AXI tree.
     */
    public static void parseXMLAndPopulateAXIModel(URL url, AXIModel model) {
        FileUtil util = FileUtil.getInstance();
        MyContentHandler handler = new MyContentHandler(model);
        try {
            model.startTransaction();
            InputSource inputSource = util.openFile(url);
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();            
            saxParser.parse(inputSource, handler);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                model.endTransaction();
                util.closeFile();
            } catch (Exception ex) {
                //TODO: model.endTransaction() throws an NPE
            }
        }        
    }

    public static class MyContentHandler extends DefaultHandler {
        private AXIComponent root;
        private java.util.Stack<AXIComponent> stack;
        private AXIModel model;
        private AXIComponentFactory factory;
        
        MyContentHandler(AXIModel model) {
            this.model = model;
            this.factory = model.getComponentFactory();
            this.stack = new java.util.Stack<AXIComponent>();
        }
                
        public void startElement(String uri, String localName,
                String qName, Attributes atts) {
            
            AXIComponent newChild = createCompositor(qName);
            if(newChild == null) {
                Element element = factory.createElement();
                element.setName(qName);
                for(int i=0; i<atts.getLength(); i++) {
                    Attribute attr = factory.createAttribute();
                    attr.setName(atts.getQName(i));
                    element.addAttribute(attr);
                }                
                newChild = element;
            }
            addChild(newChild);
        }
        
        public void endElement(String uri, String localName,
                String qName) {            
            AXIComponent component = stack.peek();            
            if(component instanceof Compositor) {
                Compositor c = (Compositor)component;
                if(c.toString().equals(qName)) {
                    stack.pop();
                }
            }
            if(component instanceof Element) {
                Element e = (Element)component;
                if(e.getName().equals(qName)) {
                    stack.pop();
                }
            }
        }
        
        private Compositor createCompositor(String qName) {
            if(qName.equals("Sequence")) {
                return factory.createSequence();
            }
            if(qName.equals("Choice")) {
                return factory.createChoice();
            }
            if(qName.equals("All")) {
                return factory.createAll();
            }
            
            return null;
        }
        
        private void addChild(AXIComponent child) {
            AXIComponent parent = null;
            if(stack.empty()) {
                parent = model.getRoot();
                addChild(parent, child);
                stack.push(parent);
            } else {
                parent = stack.peek();
                addChild(parent, child);
            }
            stack.push(child);
        }

        private void addChild(AXIComponent parent, AXIComponent child) {
            if(parent instanceof AXIDocument) {
                AXIDocument doc = (AXIDocument)parent;
                if(child instanceof Element) doc.addElement((Element)child);                    
            }
            
            if(parent instanceof Element) {
                Element element = (Element)parent;
                if(child instanceof Attribute) element.addAttribute((Attribute)child);
                if(child instanceof Compositor) element.addCompositor((Compositor)child);
                if(child instanceof Element) element.addElement((Element)child);
            }

            if(parent instanceof Compositor) {
                Compositor compositor = (Compositor)parent;
                if(child instanceof Compositor) compositor.addCompositor((Compositor)child);
                if(child instanceof Element) compositor.addElement((Element)child);
            }
        }
        
    }
            
}
