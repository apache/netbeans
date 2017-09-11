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
package org.netbeans.spi.xml.cookies;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.ProtectionDomain;
import java.security.CodeSource;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.api.xml.services.UserCatalog;

/**
 * Perform Transform action on XML document.
 * Default implementation of {@link TransformableCookie} cookie.
 *
 * @author     Libor Kramolis
 */
public final class TransformableSupport implements TransformableCookie {

    // associated source
    private final Source source;
    /** cached TransformerFactory instance. */
    private static TransformerFactory transformerFactory;
    
    
    /** 
     * Create new TransformableSupport for given data object.
     * @param source Supported <code>Source</code>.
     */    
    public TransformableSupport (Source source) {
        if (source == null) throw new NullPointerException();
        this.source = source;
    }

    /**
     * Transform this object by XSL Transformation.
     *
     * @param transformSource source of transformation.
     * @param outputResult result of transformation.
     * @param notifier optional listener (<code>null</code> allowed)
     *                 giving judgement details.
     * @throws TransformerException if an unrecoverable error occurs during the course of the transformation
     */
    public void transform (Source transformSource, Result outputResult, CookieObserver notifier) throws TransformerException {
        try {
            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug ("TransformableSupport.transform");
                Util.THIS.debug ("   transformSource = " + transformSource.getSystemId());
                Util.THIS.debug ("   outputResult = " + outputResult.getSystemId());
            }

            
            Source xmlSource = source;

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("   xmlSource = " + xmlSource.getSystemId());

            // prepare transformer == parse stylesheet, errors may occur
            Transformer transformer = newTransformer (transformSource);
            
            // transform
            if (notifier != null) {

                // inform user about used implementation

                ProtectionDomain domain = transformer.getClass().getProtectionDomain();
                CodeSource codeSource = domain.getCodeSource();
                if (codeSource == null) {
                    notifier.receive(new CookieMessage(Util.THIS.getString("BK000", transformer.getClass().getName())));
                } else {
                    URL location = codeSource.getLocation();
                    notifier.receive(new CookieMessage(Util.THIS.getString("BK001", location, transformer.getClass().getName())));
                }

                Proxy proxy = new Proxy (notifier);
                transformer.setErrorListener (proxy);
            }
            transformer.transform (xmlSource, outputResult);
            
        } catch (Exception exc) { // TransformerException, ParserConfigurationException, SAXException, FileStateInvalidException
            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug ("    EXCEPTION during transformation: " + exc.getClass().getName(), exc);
                Util.THIS.debug ("    exception's message = " + exc.getLocalizedMessage());

                Throwable tempExc = unwrapException (exc);
                Util.THIS.debug ("    wrapped exception = " + tempExc.getLocalizedMessage());
            }

            TransformerException transExcept = null;
            Object detail = null;
            
            if ( exc instanceof TransformerException ) {
                transExcept = (TransformerException)exc;                
                if ( ( notifier != null ) &&
                     ( exc instanceof TransformerConfigurationException ) ) {
                    detail = new DefaultXMLProcessorDetail (transExcept);
                }
            } else if ( exc instanceof SAXParseException ) {
                transExcept = new TransformerException (exc);
                if ( notifier != null ) {
                    detail = new DefaultXMLProcessorDetail ((SAXParseException)exc);
                }
            } else {
                transExcept = new TransformerException (exc);
                if ( notifier != null ) {
                    detail = new DefaultXMLProcessorDetail (transExcept);
                }
            }

            if ( ( notifier != null ) &&
                 ( detail != null ) ) {
                CookieMessage message = new CookieMessage
                    (message(exc), 
                     CookieMessage.FATAL_ERROR_LEVEL,
                     detail);
                notifier.receive (message);
            }

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("--> throw transExcept: " + transExcept);

            throw transExcept;
        } // catch (Exception exc)
    }


    //
    // utils
    //

    private static Throwable unwrapException (Throwable exc) {
        Throwable wrapped = null;
        if (exc instanceof TransformerException) {
            wrapped = ((TransformerException) exc).getException();
        } else if (exc instanceof SAXException) {
            wrapped = ((SAXException) exc).getException();
        } else {
            return exc;
        }

        if ( wrapped == null ) {
            return exc;
        }

        return unwrapException (wrapped);
    }

    private static URIResolver getURIResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        URIResolver res = (catalog == null ? null : catalog.getURIResolver());
        return res;
    }

    private static TransformerFactory getTransformerFactory () {
        if ( transformerFactory == null ) {
            transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver (getURIResolver()); //!!! maybe that it should be set every call if UsersCatalog instances are dynamic
        }
        return transformerFactory;
    }


    private static Transformer newTransformer (Source xsl) throws TransformerConfigurationException {
        return getTransformerFactory().newTransformer (xsl);
    }

    /**
     * Extract message from exception or use exception name.
     */
    private static String message(Throwable t) {
        String msg = t.getLocalizedMessage();
        return (msg!=null ? msg : new ExceptionWriter(t).toString());
    }

    /**
     * Print first four exception lines.
     */
    private static class ExceptionWriter extends PrintWriter {
        private int counter = 4;
        private Throwable t;
         
        public ExceptionWriter(Throwable t) {
            super(new StringWriter());
            this.t = t;
        }
        
        public void println(String s) {
            if (counter-- > 0) super.println(s);
        }

        public void println(Object o) {
            if (counter-- > 0) super.println(o);
        }
        
        public String toString() {
            t.printStackTrace(this);
            flush();
            return ((StringWriter)out).getBuffer().toString();
        }
    }

    //
    // class Proxy
    //

    private static class Proxy implements ErrorListener {
        
        private final CookieObserver peer;
        
        public Proxy (CookieObserver peer) {
            if (peer == null) {
                throw new NullPointerException();
            }
            this.peer = peer;
        }
        
        public void error (TransformerException tex) throws TransformerException {
            report (CookieMessage.ERROR_LEVEL, tex);
        }
        
        public void fatalError (TransformerException tex) throws TransformerException {
            report (CookieMessage.FATAL_ERROR_LEVEL, tex);

            throw tex;
        }
        

        public void warning (TransformerException tex) throws TransformerException {
            report (CookieMessage.WARNING_LEVEL, tex);
        }

        private void report (int level, TransformerException tex) throws TransformerException {
            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug ("[TransformableSupport::Proxy]: report [" + level + "]: ", tex);
                Util.THIS.debug ("    exception's message = " + tex.getLocalizedMessage());

                Throwable tempExc = unwrapException (tex);
                Util.THIS.debug ("    wrapped exception = " + tempExc.getLocalizedMessage());
            }

            Throwable unwrappedExc = unwrapException (tex);
            CookieMessage message = new CookieMessage (
                message(unwrappedExc), 
                level,
                new DefaultXMLProcessorDetail (tex)
            );

            peer.receive (message);
        }
        
    } // class Proxy
    
}
