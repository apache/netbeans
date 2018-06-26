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
