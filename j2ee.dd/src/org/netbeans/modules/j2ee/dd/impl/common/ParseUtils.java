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
            LOGGER.log(Level.FINE, "Parsing with ddResolver: " + ddResolver);
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
