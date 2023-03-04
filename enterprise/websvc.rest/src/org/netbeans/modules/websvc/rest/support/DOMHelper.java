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
package org.netbeans.modules.websvc.rest.support;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author PeterLiu
 */
public class DOMHelper {

    private static final int TIME_TO_WAIT = 300;
    private static final String IDENT = "    ";     //NOI18N

    private FileObject fobj;
    private Document document;

    public DOMHelper(FileObject fobj) {
        this.fobj = fobj;
        this.document = getDocument();
    }

    private Document getDocument() {
        if (document == null) {
            DocumentBuilder builder = getDocumentBuilder();

            if (builder == null) {
                Logger.getLogger(DOMHelper.class.getName()).log(Level.INFO, "Cannot get XML parser for "+fobj);
                return null;
            }
            FileLock lock = null;
            InputStream is = null;

            try {
                lock = fobj.lock();
                is = fobj.getInputStream();
                document = builder.parse(is);
            } catch (SAXParseException ex) {
                Logger.getLogger(DOMHelper.class.getName()).log(Level.INFO, "Cannot parse "+fobj, ex);
            } catch (SAXException ex) {
                Logger.getLogger(DOMHelper.class.getName()).log(Level.INFO, "Cannot parse "+fobj, ex);
            } catch (IOException ex) {
                Logger.getLogger(DOMHelper.class.getName()).log(Level.INFO, "Cannot parse "+fobj, ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
        return document;
    }

    public Element findElement(String tag, String value) {
        if (document != null) {
            NodeList nodeList = document.getElementsByTagName(tag);
            int len = nodeList.getLength();

            for (int i = 0; i < len; i++) {
                Element element = (Element) nodeList.item(i);

                if (containsValue(element, value)) {
                    return element;
                }
            }
        }
        return null;
    }

    public Element findElement(String tag) {
        if (document != null) {
            NodeList nodeList = document.getElementsByTagName(tag);

            if (nodeList.getLength() > 0) {
                return (Element) nodeList.item(0);
            }
        }
        return null;
    }
    
    public NodeList findElements(String tag) {
        if (document != null) {
            return document.getElementsByTagName(tag);
        }
        return null;
    }

    public Element findElementById(String id) {
        if (document != null) {
            return document.getElementById(id);
        }
        return null;
    }
    
    public Element findElement( String tag, String attrName , String attrValue ){
        if (document != null) {
            NodeList elements = document.getElementsByTagName(tag);
            for (int i=0; i<elements.getLength() ; i++) {
                Node item = elements.item(i);
                if ( item instanceof Element ){
                    String attribute = ((Element)item).getAttribute(attrName);
                    if ( attrValue.equals(attribute)){
                        return (Element)item;
                    }
                }
            }
        }
        return null;
    }

    public void appendChild(Element child) {
        if (document != null) {
            document.getDocumentElement().appendChild(child);
        }
    }

    public Element createElement(String tag) {
        if (document != null) {
            return document.createElement(tag);
        }
        return null;
    }

    public Element createElement(String tag, String value) {
        if (document != null) {
            Element element = document.createElement(tag);
            Text text = document.createTextNode(value);
            element.appendChild(text);

            return element;
        }
        return null;
    }

    public boolean containsValue(Element element, String value) {
        Node child = element.getFirstChild();

        if (child instanceof Text) {
            return (((Text) child).getWholeText().equals(value));
        }

        return false;
    }

    public String getValue(Element element) {
        Node child = element.getFirstChild();

        if (child instanceof Text) {
            return ((Text) child).getWholeText();
        }

        return "";      //NOI18N
    }

    public void setValue(Element element, String value) {
        Node child = element.getFirstChild();

        if (child instanceof Text) {
            ((Text) child).setData(value);
        }
    }

    public void save() {
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                FileLock lock = null;
                OutputStream os = null;

                try {
                    DocumentType docType = document.getDoctype();
                    TransformerFactory factory = TransformerFactory.newInstance();
                    Transformer transformer = factory.newTransformer();
                    DOMSource source = new DOMSource(document);

                    lock = fobj.lock();
                    os = fobj.getOutputStream(lock);
                    StreamResult result = new StreamResult(os);

                    //transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
                    //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");        //NOI18N

                    transformer.setOutputProperty(OutputKeys.METHOD, "xml");        //NOI18N

                    transformer.transform(source, result);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }, TIME_TO_WAIT);
    }

    private DocumentBuilder getDocumentBuilder() {
        DocumentBuilder builder = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(false);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setCoalescing(false);
        factory.setExpandEntityReferences(false);
        factory.setValidating(false);

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return builder;
    }

}
