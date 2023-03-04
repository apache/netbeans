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

package org.netbeans.installer.utils.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 *
 * @author Danila_Dugurov
 */
public class DomUtil {
    
    private static final DocumentBuilderFactory BUILDER_FACTORY;
    private static final TransformerFactory TRANSFORMER_FACTORY;
    
    static {
        BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    }
    
    public static Document parseXmlFile(File xmlFile) throws IOException, ParseException {
        return parseXmlFile(xmlFile, null);
    }
    
    public static Document parseXmlFile(File xmlFile, Charset charset) throws IOException, ParseException {
        final InputStream in = new BufferedInputStream(
            new FileInputStream(xmlFile));
        try {
            return parseXmlFile(in, charset);
        } finally {
            try {
                in.close();
            } catch (IOException ignord) {//skip
            }
        }
    }
    
    public static Document parseXmlFile(CharSequence xmlFile) throws ParseException {
        try {
            final InputStream in = new ByteArrayInputStream(
                xmlFile.toString().getBytes(StringUtils.ENCODING_UTF8));
            return parseXmlFile(in);
        } catch (UnsupportedEncodingException worntHappend) {
            throw new ParseException("utf-8 not supported!", worntHappend);
        } catch (IOException worntHappend) {
            throw new ParseException("fatal error: I/O mustn't happen here.", worntHappend);
        }
    }
    
    public static Document parseXmlFile(InputStream xmlStream, Charset charset) throws IOException, ParseException {
        try {
            final DocumentBuilder builder = BUILDER_FACTORY.newDocumentBuilder();
            InputSource inputSource;
            if (charset != null) {
                final Reader reader = new InputStreamReader(xmlStream, charset);
                inputSource = new InputSource(reader);
            } else {
                inputSource = new InputSource(xmlStream);
            }
            return builder.parse(inputSource);
        } catch (ParserConfigurationException worntHappend) {
            throw new ParseException("parse configuration error.", worntHappend);
        } catch (SAXException ex) {
            throw new ParseException("parsing error occuers!", ex);
        }
    }
    
    public static Document parseXmlFile(InputStream xmlStream) throws IOException, ParseException {
        return parseXmlFile(xmlStream, null);
    }
    
    public static void writeXmlFile(Document document, OutputStream outputStream, Charset charset) throws IOException {
        try {
            final Source domSource = new DOMSource(document);
            Result output;
            if (charset != null) {
                final Writer writer = new PrintWriter(new OutputStreamWriter(outputStream, charset));
                output = new StreamResult(writer);
            } else {
                output = new StreamResult(outputStream);
            }
            final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.transform(domSource, output);
        //think :check. is it really flushed here or some action should be done
        } catch(TransformerConfigurationException worntHappend) {
            throw new IOException(worntHappend.getMessage());
        } catch(TransformerException ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
    public static void writeXmlFile(Document document, OutputStream outputStream) throws IOException {
        writeXmlFile(document, outputStream, null);
    }
    
    public static void writeXmlFile(Document document, File file) throws IOException {
        writeXmlFile(document, file, null);
    }
    
    public static void writeXmlFile(Document document, File file, Charset charset) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            writeXmlFile(document, out, charset);
        } finally {
            try {
                out.close();
            } catch (IOException ignord) {//skip
            }
        }
    }
    
    public static <T extends DomExternalizable> void addChild(Element parent, T object) {
        parent.appendChild(object.writeXML(parent.getOwnerDocument()));
    }
    
    public static void addElement(Element parent, String elemName, String elemText) {
        final Element child = parent.getOwnerDocument().createElement(elemName);
        child.setTextContent(elemText);
        parent.appendChild(child);
    }
}
