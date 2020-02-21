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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
