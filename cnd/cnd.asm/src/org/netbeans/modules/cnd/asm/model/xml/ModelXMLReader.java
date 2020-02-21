/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.cnd.asm.model.xml;

import java.lang.reflect.InvocationTargetException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.Reader;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class ModelXMLReader {
       
    private final static String START_ELEMENT_SUFFIX = "Start"; // NOI18N
    private final static String END_ELEMENT_SUFFIX = "End"; // NOI18N
    
    public void readModelXml(Reader src, XMLReaderContext ctx)  {
        XMLReader xmlReader = null;
        
        try {
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            
            saxFactory.setValidating(false);
            SAXParser parser = saxFactory.newSAXParser();
            
            xmlReader = parser.getXMLReader();
            xmlReader.setContentHandler(new ModelXMLContentHandler(ctx));
            xmlReader.parse(new InputSource(src));
        } catch (ModelXMLReaderException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new ModelXMLReaderException(ex);
        }                       
    }       
    
    
    private static class ModelXMLContentHandler extends DefaultHandler {
        
        private final List<XMLReaderContext> ctxStack;
        private XMLReaderContext ctxCur;
        
        public ModelXMLContentHandler(XMLReaderContext startCtx) {
            ctxCur = startCtx;
            ctxStack = new LinkedList<XMLReaderContext>();
        }
        
        @Override
        public void startElement (String uri, String localName,
			          String qName, Attributes attributes)
	      throws SAXException {
        
             ctxStack.add(ctxCur);            
             Object result = invokeCtxMethod(qName + START_ELEMENT_SUFFIX,
                                             attributes, Attributes.class );
                
             if (result instanceof XMLReaderContext) {
                 ctxCur = (XMLReaderContext) result;
             }                                                                             
        }

        @Override
        public void endElement (String uri, String localName, String qName)
               throws SAXException {
            
            Object backCtx = ctxCur;
            ctxCur = ctxStack.remove(ctxStack.size() - 1);
            
            invokeCtxMethod(qName + END_ELEMENT_SUFFIX,
                            backCtx, backCtx.getClass()); 
            
        }
        
        private Object invokeCtxMethod(String name, Object arg, Class argClass)  {
            Object result = null;
            Method method = null;
            
            if (ctxCur == null || arg == null) 
                return result;
            
            try {
                method = ctxCur.getClass().getMethod(name, argClass);                       
            } catch (Exception ex) {  
                 return null;
            }
            
            try {
                result = method.invoke(ctxCur, arg);
            } catch (InvocationTargetException ex) {
                throw new ModelXMLReaderException(ex.getTargetException().toString());
            } 
            catch (Exception ex) {
                throw new ModelXMLReaderException(ex);
            } 
            
            return result;
        }              
    }
}
