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

/*
 * TomcatInstallUtil.java
 *
 * Created on December 9, 2003, 11:14 AM
 */

package org.netbeans.modules.tomcat5.util;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.tomcat5.config.gen.Engine;
import org.netbeans.modules.tomcat5.config.gen.Host;
import org.netbeans.modules.tomcat5.config.gen.Server;
import org.netbeans.modules.tomcat5.config.gen.Service;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;


import org.w3c.dom.Document;
import org.netbeans.editor.BaseDocument;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 *
 * @author Martin Grebac
 */
public class TomcatInstallUtil {
    
    /** default value of bundled tomcat' http connector uri encoding */
    private static final String BUNDLED_DEFAULT_URI_ENCODING = "utf-8"; // NOI18N
    /** default value of bundled tomcat's host autoDeploy attribute */
    private static final Boolean BUNDLED_DEFAULT_AUTO_DEPLOY = Boolean.FALSE;
    
    private static final String ATTR_URI_ENCODING = "URIEncoding"; // NOI18N
    private static final String ATTR_PORT = "port"; // NOI18N
    private static final String ATTR_PROTOCOL = "protocol"; // NOI18N
    private static final String ATTR_AUTO_DEPLOY = "autoDeploy";    // NOI18N
    private static final String ATTR_SCHEME = "scheme";             // NOI18N
    private static final String ATTR_SECURE = "secure";             // NOI18N
    private static final String ATTR_SERVER = "server"; // NOI18N
    
    private static final String PROP_CONNECTOR = "Connector"; // NOI18N
    
    private static final String HTTP    = "http";   // NOI18N
    private static final String HTTPS   = "https";  // NOI18N
    private static final String TRUE    = "true";   // NOI18N
    
    /** Creates a new instance of TomcatInstallUtil */
    private TomcatInstallUtil() {
    }
    
    public static String getShutdownPort(Server server) {
        String port = server.getAttributeValue("port");
        return port != null ? port : String.valueOf(TomcatProperties.DEF_VALUE_SHUTDOWN_PORT);
    }
    
    public static String getPort(Server server) {

        Service service = server.getService(0);

        int defCon = -1;
        String port;
        for (int i=0; i<service.sizeConnector(); i++) {
            String protocol = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_PROTOCOL);
            String scheme = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_SCHEME);
            String secure = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_SECURE);
            if (isHttpConnector(protocol, scheme, secure)) {
                defCon = i;
                break;
            }
        }
        
        if (defCon==-1 && service.sizeConnector() > 0) {
            defCon=0;
        }
        
        port = service.getAttributeValue(PROP_CONNECTOR, defCon, ATTR_PORT);
        return port;
    }

    public static String getServerHeader(Server server) {

        Service service = server.getService(0);

        int defCon = -1;
        String serverHeader;
        for (int i=0; i<service.sizeConnector(); i++) {
            String protocol = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_PROTOCOL);
            String scheme = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_SCHEME);
            String secure = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_SECURE);
            if (isHttpConnector(protocol, scheme, secure)) {
                defCon = i;
                break;
            }
        }

        if (defCon==-1 && service.sizeConnector() > 0) {
            defCon=0;
        }

        serverHeader = service.getAttributeValue(PROP_CONNECTOR, defCon, ATTR_SERVER);
        return serverHeader;
    }
    
    public static String getHost(Server server) {
        String host = null;
        Service service = server.getService(0);
        if (service != null) {
            host = service.getAttributeValue("Engine",0,"defaultHost");
        }
        return host;
    }
    
    /**
     * Return the CATALINA_HOME directory of the bundled Tomcat
     *
     * @return the CATALINA_HOME directory of the bundled Tomcat, <code>null</code>
     *         if the CATALINA_HOME directory does not exist which should never happen.
     */
    public static File getBundledHome() {
        FileObject fo = FileUtil.getConfigFile(TomcatProperties.BUNDLED_TOMCAT_SETTING);
        if (fo != null) {
            InstalledFileLocator ifl = InstalledFileLocator.getDefault();
            return ifl.locate(fo.getAttribute("bundled_home").toString(), null, false); // NOI18N
        }
        return null;
    }
    
    /** @return text (suitable for printing to XML file) for a given XML document.
     * this method uses org.apache.xml.serialize.XMLSerializer class for printing XML file
     */
    public static String getDocumentText(Document doc) {
        OutputFormat format = new OutputFormat ();
        format.setPreserveSpace (true);
        StringWriter sw = new StringWriter();
        org.w3c.dom.Element rootElement = doc.getDocumentElement();
        if (rootElement==null) {
            return null;
        }
        try {
            XMLSerializer ser = new XMLSerializer (sw, format);
            ser.serialize (rootElement);
            // Apache serializer also fails to include trailing newline, sigh.
            sw.write('\n');
            return sw.toString();
        }catch(IOException ex) {
            System.out.println("ex="+ex);
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return rootElement.toString();
        }
        finally {
            try {
                sw.close();
            } catch(IOException ex) {
                System.out.println("ex="+ex);
                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    
    public static void updateDocument(final javax.swing.text.Document doc,
            final String newDocInput, final String prefixMark) throws javax.swing.text.BadLocationException {

        Runnable update = new Runnable() {
            @Override
            public void run() {
                try {
                    String newDoc = newDocInput;
                    int origLen = doc.getLength();
                    String origDoc = doc.getText(0, origLen);
                    int prefixInd=0;
                    if (prefixMark!=null) {
                        prefixInd = origDoc.indexOf(prefixMark);
                        if (prefixInd>0) {
                            origDoc=doc.getText(prefixInd,origLen-prefixInd);
                        }
                        else {
                            prefixInd=0;
                        }
                        int prefixIndNewDoc = newDoc.indexOf(prefixMark);
                        if (prefixIndNewDoc > 0) {
                            newDoc = newDoc.substring(prefixIndNewDoc);
                        }
                    }

                    if (origDoc.equals(newDoc)) {
                        // no change in document
                        return;
                    }

                    doc.remove(prefixInd, origLen - prefixInd);
                    doc.insertString(prefixInd, newDoc, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).runAtomic(update);
        } else {
            update.run();
        }
    }
    
    private static boolean isHttpConnector(String protocol, String scheme, String secure) {
        return (protocol == null || protocol.length() == 0 || protocol.toLowerCase().startsWith(HTTP))
                && (scheme == null || !scheme.toLowerCase().equals(HTTPS))
                && (secure == null || !secure.toLowerCase().equals(TRUE));
    }
    
    public static boolean setServerPort(int port, File tomcatConf) {
        FileObject fo = FileUtil.toFileObject(tomcatConf);
        if (fo != null) {
            try {
                XMLDataObject dobj = (XMLDataObject)DataObject.find(fo);
                org.w3c.dom.Document doc = dobj.getDocument();
                org.w3c.dom.Element root = doc.getDocumentElement();
                org.w3c.dom.NodeList list = root.getElementsByTagName("Service"); //NOI18N
                int size=list.getLength();
                if (size>0) {
                    org.w3c.dom.Element service=(org.w3c.dom.Element)list.item(0);
                    org.w3c.dom.NodeList cons = service.getElementsByTagName(PROP_CONNECTOR);
                    for (int i=0;i<cons.getLength();i++) {
                        org.w3c.dom.Element con=(org.w3c.dom.Element)cons.item(i);
                        String protocol = con.getAttribute(ATTR_PROTOCOL);
                        String scheme = con.getAttribute(ATTR_SCHEME);
                        String secure = con.getAttribute(ATTR_SECURE);
                        if (isHttpConnector(protocol, scheme, secure)) {
                            con.setAttribute(ATTR_PORT, String.valueOf(port));
                            updateDocument(dobj,doc);
                            return true;
                        }
                    }
                }
            } catch(org.xml.sax.SAXException ex){
                Exceptions.printStackTrace(ex);
            } catch(org.openide.loaders.DataObjectNotFoundException ex){
                Exceptions.printStackTrace(ex);
            } catch(javax.swing.text.BadLocationException ex){
                Exceptions.printStackTrace(ex);
            } catch(java.io.IOException ex){
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
    
    private static void setServerAttributeValue(Server server, String attribute, String value) {
        server.setAttributeValue(attribute, value);
    }
    
    private static void setHttpConnectorAttributeValue(Server server, String attribute, String value) {
        Service services[] = server.getService();
        if (services != null && services.length > 0) {
            Service service = services[0];
            int sizeConnector = service.sizeConnector();
            for(int i = 0; i < sizeConnector; i++) {
                String protocol = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_PROTOCOL);
                String scheme   = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_SCHEME);
                String secure   = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_SECURE);
                if (isHttpConnector(protocol, scheme, secure)) {
                    service.setAttributeValue(PROP_CONNECTOR, i, attribute, value);
                    return;
                }
            }
        }
    }
    
    private static void setHostAttributeValue(Server server, String attribute, String value) {
        Service service[] = server.getService();
        if (service != null) {
            for(int i = 0; i < service.length; i++) {
                Engine engine = service[i].getEngine();
                if (engine != null) {
                    Host host[] = engine.getHost();
                    if (host != null && host.length > 0) {
                        host[0].setAttributeValue(attribute, value);
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Make some Bundled Tomcat specific changes in server.xml.
     */
    public static void patchBundledServerXml(File serverXml) {
        try {
            Server server = Server.createGraph(serverXml);
            setServerAttributeValue(server, ATTR_PORT, String.valueOf(TomcatProperties.DEF_VALUE_BUNDLED_SHUTDOWN_PORT));
            setHttpConnectorAttributeValue(server, ATTR_PORT, String.valueOf(TomcatProperties.DEF_VALUE_BUNDLED_SERVER_PORT));
            setHttpConnectorAttributeValue(server, ATTR_URI_ENCODING, BUNDLED_DEFAULT_URI_ENCODING);
            setHostAttributeValue(server, ATTR_AUTO_DEPLOY, BUNDLED_DEFAULT_AUTO_DEPLOY.toString());
            server.write(serverXml);
        } catch (IOException e) {
            Logger.getLogger(TomcatInstallUtil.class.getName()).log(Level.INFO, null, e);
        } catch (RuntimeException e) {
            Logger.getLogger(TomcatInstallUtil.class.getName()).log(Level.INFO, null, e);
        }
    }
    
    /**
     * Patches catalina.properties file in a way that the Common class loader 
     * sees all the jar files in the ${catalina.base}/nblib folder.
     * <p/>
     * HTTP Monitor jars will be placed to this folder.
     * 
     * @param catalinaProperties catalina properties file.
     * 
     * @throws IOException if something goes wrong
     */
    public static void patchCatalinaProperties(File catalinaProperties) throws IOException {
        EditableProperties props = new EditableProperties(false);
        InputStream is = new BufferedInputStream(new FileInputStream(catalinaProperties));
        try {
            props.load(is);
            String COMMON_LOADER = "common.loader"; // NOI18N
            String commonLoader = props.getProperty(COMMON_LOADER);
            if (commonLoader != null) {
                commonLoader = commonLoader.trim();
                String NB_LIB = "${catalina.base}/nblib/*.jar"; // NOI18N
                if (commonLoader.contains(NB_LIB)) {
                    return;
                }
                StringBuilder commonLoaderValue = new StringBuilder(commonLoader);
                if (!commonLoader.endsWith(",")) { // NOI18N
                    commonLoaderValue.append(","); // NOI18N
                }
                commonLoaderValue.append(NB_LIB);
                props.setProperty(COMMON_LOADER, commonLoaderValue.toString());
            }
        } finally {
            is.close();
        }
        // store changes
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(catalinaProperties));
        try {
            props.store(out);
        } finally {
            out.close();
        }
    }
    
    /**
     * Creates CATALINA_BASE/nblib directory along with a CATALINA_BASE/nblib/README
     * file.
     * <p/>
     * HTTP Monitor jars will be placed to this folder.
     * 
     * @param catalinaBase CATALINA_BASE directory
     * 
     * @throws IOException if something goes wrong
     */
    public static void createNBLibDirectory(File catalinaBase) throws IOException {
        // create a README file
        new File(catalinaBase, "nblib").mkdir(); // NOI18N
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(catalinaBase, "nblib/README"))); // NOI18N
        try {
            for (String line : NbBundle.getMessage(TomcatInstallUtil.class, "MSG_NBLibReadmeContent").split("\n")) { // NOI18N
                // fix the new line sequence
                writer.write(line);
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }
    
    public static boolean setShutdownPort(int port, File tomcatConf) {
        FileObject fo = FileUtil.toFileObject(tomcatConf);
        if (fo != null) {
            try {
                XMLDataObject dobj = (XMLDataObject)DataObject.find(fo);
                org.w3c.dom.Document doc = dobj.getDocument();
                org.w3c.dom.Element root = doc.getDocumentElement();
                root.setAttribute("port", String.valueOf(port)); //NOI18N
                updateDocument(dobj,doc);
                return true;
            } catch(org.xml.sax.SAXException ex){
                Exceptions.printStackTrace(ex);
            } catch(org.openide.loaders.DataObjectNotFoundException ex){
                Exceptions.printStackTrace(ex);
            } catch(javax.swing.text.BadLocationException ex){
                Exceptions.printStackTrace(ex);
            } catch(java.io.IOException ex){
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
    
    public static void updateDocument(DataObject dobj, org.w3c.dom.Document doc)
        throws javax.swing.text.BadLocationException, java.io.IOException {
        org.openide.cookies.EditorCookie editor = (EditorCookie)dobj.getCookie(EditorCookie.class);
        javax.swing.text.Document textDoc = editor.getDocument();
        if (textDoc == null) {
            textDoc = editor.openDocument();
        }
        TomcatInstallUtil.updateDocument(textDoc,TomcatInstallUtil.getDocumentText(doc),"<Server"); //NOI18N
        SaveCookie savec = (SaveCookie) dobj.getCookie(SaveCookie.class);
        if (savec!=null) savec.save();
    }

}
