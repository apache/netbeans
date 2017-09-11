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
