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

package org.netbeans.modules.websvc.jaxws.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.SAXException;

/** Utility
 *  This is the case when service element is missing
 *
 * @author mkuchtiak
 */
public class WsdlWrapperGenerator {
    
    public static WsdlWrapperHandler parse(String wsdlUrl) throws ParserConfigurationException, SAXException, IOException {
        WsdlWrapperHandler handler = new WsdlWrapperHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(wsdlUrl, handler);
        return handler;
    }
    
    public static WsdlWrapperHandler parse(File file) throws ParserConfigurationException, SAXException, IOException {
        WsdlWrapperHandler handler = new WsdlWrapperHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(file, handler);
        return handler;
    }
    
    private static final String TEMPLATE_BASE="/org/netbeans/modules/websvc/jaxws/resources/"; //NOI18N
    
    private static Transformer getTransformer() throws TransformerConfigurationException {
        InputStream is = new BufferedInputStream(WsdlWrapperGenerator.class.getResourceAsStream(TEMPLATE_BASE+"WsdlServiceGenerator.xsl")); //NOI18N
        TransformerFactory transFactory = TransformerFactory.newInstance();
        transFactory.setURIResolver(new URIResolver() {
            public Source resolve(String href, String base)
            throws TransformerException {
                InputStream is = getClass().getResourceAsStream(
                TEMPLATE_BASE + href.substring(href.lastIndexOf('/')+1));
                if (is == null) {
                    return null;
                }
                
                return new StreamSource(is);
            }
        });
        Templates t = transFactory.newTemplates(new StreamSource(is));
        return t.newTransformer();
    }
   
    public static void generateWrapperWSDLContent(File wrapperWsdlFile, StreamSource wsdlSource, String tnsPrefix, String wsdlLocation) throws IOException 
    {
        //File file = new File("/space/home/mkuchtiak/nb_projects/JavaApplication1/src/javaapplication1/resources/"+wsdlName);
        /*
        File wsdlFile = new File(System.getProperty("java.io.tmpdir"), wsdlName); //NOI18N
        
        if(!wsdlFile.exists()) {
            try {
                wsdlFile.createNewFile();
            } catch(IOException ex) {
                String mes = NbBundle.getMessage(WebServiceFromWSDLPanel.class, "ERR_UnableToCreateTempFile", wsdlFile.getPath()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return null;
            }
        }
        */
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(wrapperWsdlFile));
            Transformer transformer = getTransformer();
            transformer.setParameter("tns_prefix",tnsPrefix);
            transformer.setParameter("wsdl_location",wsdlLocation);
            transformer.transform(wsdlSource, new StreamResult(os));
            os.close();
        }
        catch(TransformerConfigurationException tce) {
            IOException ioe = new IOException();
            ioe.initCause(tce);
            throw ioe;
        }
        catch(TransformerException te) {
            IOException ioe = new IOException();
            ioe.initCause(te);
            throw ioe;
        }
        finally {
            if(os != null) {
                os.close();
            }
        }
    }
    
    public static String getWrapperName(URL wsdlURL) {
        String urlString = wsdlURL.toExternalForm();
        int start = urlString.lastIndexOf("/"); //NOI18N
        int end = urlString.lastIndexOf("."); //NOI18N
        if (start>=0) {
            if (start<end) return urlString.substring(start+1,end)+"Wrapper.wsdl"; //NOI18N
            else if (start+1<urlString.length()) return urlString.substring(start+1)+"Wrapper.wsdl"; //NOI18N
        } else if (end>0) return urlString.substring(0,end)+"Wrapper.wsdl"; //NOI18N
        return "WsdlWrapper.wsdl"; //NOI18N
    }
}
