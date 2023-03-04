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

package org.netbeans.modules.j2ee.dd.impl.common;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.xml.sax.*;


/** Class that collects XML parsing utility methods for web applications. It is 
 * implementation private for this module, however it is also intended to be used by 
 * the DDLoaders modules, which requires tighter coupling with ddapi and has an 
 * implementation dependency on it.
 *
 * @author Petr Jiricka
 */
public class ParseUtils {
  
    public static final String EXCEPTION_PREFIX="version:"; //NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(ParseUtils.class.getName());
    
    /** Parsing just for detecting the version  SAX parser used
     */
    public static String getVersion(java.io.InputStream is, org.xml.sax.helpers.DefaultHandler versionHandler,
        EntityResolver ddResolver) throws java.io.IOException, SAXException {

        XMLReader reader = XMLUtil.createXMLReader(false);
        reader.setContentHandler(versionHandler);
        reader.setEntityResolver(ddResolver);
        try {
            reader.parse(new InputSource(is));
        } catch (SAXException ex) {
            is.close();
            String message = ex.getMessage();
            if (message != null && message.startsWith(EXCEPTION_PREFIX)) {
                String versionStr = message.substring(EXCEPTION_PREFIX.length());
                if ("".equals(versionStr) || "null".equals(versionStr)) { // NOI18N
                    return null;
                } else {
                    return versionStr;
                }
            } else {
                throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotParse"), ex);
            }
        }
        is.close();
        throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotFindRoot"));
    }
    
    /** Parsing just for detecting the version  SAX parser used
    */
    public static String getVersion(InputSource is, org.xml.sax.helpers.DefaultHandler versionHandler, 
            EntityResolver ddResolver) throws IOException, SAXException {
        XMLReader reader = XMLUtil.createXMLReader(false);
        reader.setContentHandler(versionHandler);
        reader.setEntityResolver(ddResolver);
        try {
            reader.parse(is);
        } catch (SAXException ex) {
            String message = ex.getMessage();
            if (message != null && message.startsWith(EXCEPTION_PREFIX)) {
                return message.substring(EXCEPTION_PREFIX.length());
            } else {
                throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotParse"), ex);
            }
        }
        throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotFindRoot"));
    }
    
    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
        private int errorType=-1;
        SAXParseException error;
        
        public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            printMsg("warning", sAXParseException);
            if (errorType<0) {
                errorType=0;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        public void error(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            printMsg("error", sAXParseException);
            if (errorType<1) {
                errorType=1;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        public void fatalError(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            printMsg("fatal error", sAXParseException);
            errorType=2;
            throw sAXParseException;
        }

        private void printMsg(String type, org.xml.sax.SAXParseException e) {
            System.out.println("&&& SAX "+type+": ["+e.getLineNumber()+":"+e.getColumnNumber()+"] "+e.getPublicId()+" / "+e.getSystemId()+" / "+e);
        }

        public int getErrorType() {
            return errorType;
        }
        public SAXParseException getError() {
            return error;
        }
    }
    
    public static SAXParseException parseDD(InputSource is, EntityResolver ddResolver) 
            throws org.xml.sax.SAXException, java.io.IOException {
        // additional logging for #127276
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Parsing with ddResolver: {0}", ddResolver);
        }
        ErrorHandler errorHandler = new ErrorHandler();
        try {
            XMLReader reader = XMLUtil.createXMLReader();
            reader.setErrorHandler(errorHandler);
            reader.setEntityResolver(ddResolver);
            reader.setFeature("http://apache.org/xml/features/validation/schema", true); // NOI18N
            reader.setFeature("http://xml.org/sax/features/validation",  true); // NOI18N
            reader.setFeature("http://xml.org/sax/features/namespaces",  true); // NOI18N
            reader.parse(is);
            SAXParseException error = errorHandler.getError();
            if (error!=null) return error;
        } catch (IllegalArgumentException ex) {
            // yes, this may happen, see issue #71738
            SAXException sax = new SAXException(ex.getMessage(), ex);
            sax.initCause(ex);
            throw sax;
        }
        return null;
    }
  
}
