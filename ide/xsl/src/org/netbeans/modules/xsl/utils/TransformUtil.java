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
package org.netbeans.modules.xsl.utils;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.execution.*;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.spi.xml.cookies.*;


import org.netbeans.modules.xsl.XSLDataObject;
import org.openide.util.NbBundle;

/**
 * Transformation utilities.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TransformUtil {
    
    private static final String SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces"; // NOI18N

    public static final String DEFAULT_OUTPUT_EXT = "html";
    private static TransformerFactory transformerFactory;
    private static SAXParserFactory saxParserFactory;


    public static boolean isXSLTransformation (DataObject dataObject) {
      //  return ( dataObject instanceof XSLDataObject );
         return dataObject.getPrimaryFile().getMIMEType().equals (XSLDataObject.MIME_TYPE);
    }

    public static String getURLName (FileObject fileObject) throws MalformedURLException, FileStateInvalidException {
        URL fileURL = null;
        File file = FileUtil.toFile (fileObject);

        if ( file != null ) {
//            if ( Util.THIS.isLoggable() ) /* then */ {
//                try {
//                    Util.THIS.debug ("[TransformUtil.getURLName]");
//                    Util.THIS.debug ("    file = " + file);
//                    Util.THIS.debug ("    file.getCanonicalPath = " + file.getCanonicalPath());
//                    Util.THIS.debug ("    file.getAbsolutePath  = " + file.getAbsolutePath());
//                    Util.THIS.debug ("    file.toString  = " + file.toString());
//                    Util.THIS.debug ("    file.toURL  = " + file.toURL());
//                } catch (Exception exc) {
//                    Util.THIS.debug ("DEBUG Exception", exc);
//                }
//            }

            fileURL = file.toURL();
        } else {
            fileURL = fileObject.toURL();
        }

        return fileURL.toExternalForm();
    }

    public static URL createURL (URL baseURL, String fileName) throws MalformedURLException, FileStateInvalidException {
//        if ( Util.THIS.isLoggable() ) /* then */ {
//            Util.THIS.debug ("TransformUtil.createURL:");
//            Util.THIS.debug ("    baseURL = " + baseURL);
//            Util.THIS.debug ("    fileName = " + fileName);
//        }

        URL url = new URL (baseURL, fileName);

//        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    return URL = " + url);

        return url;
    }

    public static Source createSource (URL baseURL, String fileName) throws IOException, MalformedURLException, FileStateInvalidException, ParserConfigurationException, SAXException {
        URL url = createURL (baseURL, fileName);
        // test right url
        InputStream is = url.openStream();
        is.close();

        XMLReader reader = TransformUtil.newXMLReader();

//        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TransformUtil.createSource: XMLReader (http://xml.org/sax/features/namespaces) : "
//                                                                  +  reader.getFeature (SAX_FEATURES_NAMESPACES));

        reader.setEntityResolver (TransformUtil.getEntityResolver());
        Source source = new SAXSource (reader, new InputSource (url.toExternalForm()));

        return source;
    }

    public static URIResolver getURIResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        URIResolver res = (catalog == null ? null : catalog.getURIResolver());
        return res;
    }

    public static EntityResolver getEntityResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());
        return res;
    }
    

    private static TransformerFactory getTransformerFactory () {
        if ( transformerFactory == null ) {
            transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver (getURIResolver()); //!!! maybe that it should be set every call if UsersCatalog instances are dynamic
        }
        return transformerFactory;
    }

    private static SAXParserFactory getSAXParserFactory () throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        if ( saxParserFactory == null ) {
            saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setFeature (SAX_FEATURES_NAMESPACES, true);
        }
        return saxParserFactory;
    }


    public static Transformer newTransformer (Source xsl) throws TransformerConfigurationException {
        Transformer transformer = getTransformerFactory().newTransformer (xsl);

//        if ( Util.THIS.isLoggable() ) /* then */ transformer.setParameter ("transformer", xsl); // debug

        return transformer;
    }

    public static XMLReader newXMLReader () throws ParserConfigurationException, SAXException {
        SAXParser parser = getSAXParserFactory().newSAXParser();
        return parser.getXMLReader();
    }


    /*  @return associated stylesheet or <code>null</code>.
     */
    public static Source getAssociatedStylesheet (URL baseURL) {
//        if ( Util.THIS.isLoggable() ) /* then */ {
//            Util.THIS.debug ("TransformUtil.getAssociatedStylesheet:");
//            Util.THIS.debug ("    baseURL = " + baseURL);
//        }

        Source xml_stylesheet = null;

        try {
            XMLReader reader = newXMLReader();
            reader.setEntityResolver (getEntityResolver());
            SAXSource source = new SAXSource (reader, new InputSource (baseURL.toExternalForm()));
            
            xml_stylesheet = getTransformerFactory().getAssociatedStylesheet (source, null, null, null);

//            if ( Util.THIS.isLoggable() ) /* then */ {
//                Util.THIS.debug ("    source = " + source.getSystemId());
//                Util.THIS.debug ("    xml_stylesheet = " + xml_stylesheet);
//            }
        } catch (Exception exc) { // ParserConfigurationException, SAXException, TransformerConfigurationException
            // ignore it
//            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TransformUtil.getAssociatedStylesheet: !!!", exc); // debug
        }

        return xml_stylesheet;
    }

    public static String guessOutputExt (Source source) {
        String ext = DEFAULT_OUTPUT_EXT;

        try {
            Transformer transformer = newTransformer (source);
            String method = transformer.getOutputProperty (OutputKeys.METHOD);
            
//            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[TransformUtil] guessOutputExt: method = " + method);
            
            if ( "text".equals (method) ) { // NOI18N
                ext = "txt"; // NOI18N
            } else if ( method != null ) {
                ext = method;
            }
        } catch (Exception exc) {
            // ignore it
            
//            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (exc);
        }

//        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[TransformUtil] guessOutputExt: extension = " + ext);

        return ext;
    }

    /**
     * @throws TransformerException it indicates 
     */
    public static void transform (Source xml, TransformableCookie transformable, Source xsl, Result output, CookieObserver notifier) throws TransformerException {
//        if ( Util.THIS.isLoggable() ) /* then */ {
//            Util.THIS.debug ("TransformUtil.transform");
//            Util.THIS.debug ("    XML source = " + xml.getSystemId());
//            Util.THIS.debug ("    TransformableCookie = " + transformable);
//            Util.THIS.debug ("    XSL source = " + xsl.getSystemId());
//            Util.THIS.debug ("    Output Result = " + output.getSystemId());
//            Util.THIS.debug ("    CookieObserver = " + notifier);
//        }

        if ( transformable != null ) {

            transformable.transform (xsl, output, notifier);

        } else {

            try {
                Transformer transformer = TransformUtil.newTransformer (xsl);

                if (notifier != null) {

                    // inform user about used implementation

                    ProtectionDomain domain = transformer.getClass().getProtectionDomain();
                    CodeSource codeSource = domain.getCodeSource();
                    if (codeSource == null) {
                        notifier.receive(new CookieMessage(NbBundle.getMessage(TransformUtil.class, "BK000", transformer.getClass().getName())));
                    } else {
                        URL location = codeSource.getLocation();
                        notifier.receive(new CookieMessage(NbBundle.getMessage(TransformUtil.class, "BK001", location, transformer.getClass().getName())));
                    }

                    Proxy proxy = new Proxy (notifier);
                    transformer.setErrorListener (proxy);
                }
            
                //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\n==> transform: param [transformer] = " + transformer.getParameter ("transformer")); // debug

                transformer.transform (xml, output);

            } catch (Exception exc) { // TransformerException, ParserConfigurationException, SAXException, FileStateInvalidException
//                if ( Util.THIS.isLoggable() ) /* then */ {
//                    Util.THIS.debug ("    EXCEPTION during transformation: " + exc.getClass().getName(), exc);
//                    Util.THIS.debug ("    exception's message = " + exc.getLocalizedMessage());
//                    
//                    Throwable tempExc = unwrapException (exc);
//                    Util.THIS.debug ("    wrapped exception = " + tempExc.getLocalizedMessage());
//                }
                    
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
                        (unwrapExceptionMessage(exc), 
                         CookieMessage.FATAL_ERROR_LEVEL,
                         detail);
                    notifier.receive (message);
                }

//                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("--> throw transExcept: " + transExcept);

                throw transExcept;
            }
        }
    }
    
    public static String unwrapExceptionMessage(Throwable exc) {
        Throwable msgHolder = unwrapException(exc);
        String m = msgHolder.getMessage();
        if (m != null) {
            return m;
        }
        // no messag present, at least use the original exception's classname
        return msgHolder.getClass().getSimpleName();
    }

    /** Unwrap wrapped cause exception.
     */
    public static Throwable unwrapException (Throwable exc) {
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
//            if ( Util.THIS.isLoggable() ) /* then */ {
//                Util.THIS.debug ("[TransformableSupport::Proxy]: report [" + level + "]: ", tex);
//                Util.THIS.debug ("    exception's message = " + tex.getLocalizedMessage());
//
//                Throwable tempExc = unwrapException (tex);
//                Util.THIS.debug ("    wrapped exception = " + tempExc.getLocalizedMessage());
//            }

            CookieMessage message = new CookieMessage (
                unwrapExceptionMessage(tex), 
                level,
                new DefaultXMLProcessorDetail (tex)
            );
            peer.receive (message);
        }
        
    } // class Proxy
    
}
