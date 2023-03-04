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
package org.netbeans.modules.xsl.cookies;

import org.xml.sax.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;


import org.netbeans.api.xml.cookies.*;
import org.netbeans.spi.xml.cookies.*;
import org.openide.util.NbBundle;

/**
 * Validates XSL transformation
 * @author asgeir@dimonsoftware.com
 */
public class ValidateXSLSupport implements ValidateXMLCookie {

    // associated input source
    private final InputSource inputSource;

    // it will viasualize our results
    private CookieObserver console;

    // fatal error counter
    private int fatalErrors;
    
    // error counter
    private int errors;

    /** Creates a new instance of ValidateXSLSupport */
    public ValidateXSLSupport(InputSource inputSource) {
        this.inputSource = inputSource;
    }
    
    // inherit JavaDoc
    public boolean validateXML(CookieObserver l) {
       try {
            console = l;

            int fatalErrors = 0;
            int errors = 0;

            String checkedFile = inputSource.getSystemId();
            sendMessage(NbBundle.getMessage(ValidateXSLSupport.class, "MSG_checking", checkedFile));

            ErrorListener errorListener = new XslErrorListener();
            try {
                SAXTransformerFactory factory = (SAXTransformerFactory)TransformerFactory.newInstance();
                factory.setErrorListener(errorListener);
                TransformerHandler transformerHandler = factory.newTransformerHandler(new SAXSource(inputSource));
            } catch (TransformerException ex) {
                CookieMessage message = new CookieMessage(
                    ex.getLocalizedMessage(), 
                    CookieMessage.FATAL_ERROR_LEVEL,
                    new DefaultXMLProcessorDetail(ex)
                );
                sendMessage (message);
            }
            
            return errors == 0 && fatalErrors == 0;
        } finally {
            console = null;
        }
    }

    private void sendMessage(String message) {
        if (console != null) {
            console.receive(new CookieMessage(message));
        }
    }

    private void sendMessage (CookieMessage message) {
        if (console != null) {
            console.receive (message);
        }
    }


    //
    // class XslErrorListener
    //
    private class XslErrorListener implements ErrorListener {
        public void error(TransformerException ex) throws TransformerException{
            if (errors++ == getMaxErrorCount()) {
                String msg = NbBundle.getMessage(ValidateXSLSupport.class, "MSG_too_many_errs");
                sendMessage(msg);
                throw ex; // stop the parser                
            } else {
                CookieMessage message = new CookieMessage(
                    ex.getLocalizedMessage(), 
                    CookieMessage.ERROR_LEVEL,
                    new DefaultXMLProcessorDetail(ex)
                );
                sendMessage (message);
            }
        }
    
        public void fatalError(TransformerException ex) throws TransformerException{
            fatalErrors++;
            CookieMessage message = new CookieMessage(
                ex.getLocalizedMessage(), 
                CookieMessage.FATAL_ERROR_LEVEL,
                new DefaultXMLProcessorDetail(ex)
            );
            sendMessage (message);
        }
        
        public void warning(TransformerException ex) throws TransformerException{
            CookieMessage message = new CookieMessage(
                ex.getLocalizedMessage(), 
                CookieMessage.WARNING_LEVEL,
                new DefaultXMLProcessorDetail(ex)
            );
            sendMessage (message);
        }
    
        private int getMaxErrorCount() {
            return 20;  //??? load from option
        }    
    } // class XslErrorListener
  
}
