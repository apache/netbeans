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

package org.netbeans.modules.websvc.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.text.MessageFormat;
import java.util.Iterator;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openide.util.NbBundle;


/** !PW FIXME This thread runs without a monitor thread ensuring a proper
 *      timeout and shutdown of the HttpConnection.  It should use the timeout
 *      feature of JDK 1.5.0 and the timeout global properties in JDK 1.4.2.
 *
 *      As is, it is probably possible for this thread to hang if it opens a
 *      connection to an HTTP server, sends a request, and that server never
 *      responds.
 *
 * @author Peter Williams
 */
public class WsdlRetriever implements Runnable {
    
    public static final int STATUS_START = 0;
    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_DOWNLOADING = 2;
    public static final int STATUS_COMPLETE = 3;
    public static final int STATUS_FAILED = 4;
    public static final int STATUS_TERMINATED = 5;
    public static final int STATUS_BAD_WSDL = 6;
    
    static final String [] STATUS_MESSAGE = {
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Ready"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Connecting"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Downloading"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Complete"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Exception"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Terminated"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_UnknownFileType") // NOI18N
    };
    
    // Thread plumbing
    private volatile boolean shutdown;
    private volatile int status;
    
    // Wsdl collection information
    private String wsdlUrlName;
    private byte [] wsdlContent;
    private List<SchemaInfo> schemas; // List of imported schema/wsdl files
    
    private String wsdlFileName;
    // Parent
    private MessageReceiver receiver;
    
    public WsdlRetriever(MessageReceiver r, String url) {
        this.shutdown = false;
        this.status = STATUS_START;
        this.wsdlUrlName = url;
        this.wsdlContent = null;
        this.wsdlFileName = null;
        this.schemas=null;
        this.receiver = r;
    }
    
    // Properties
    public byte [] getWsdl() {
        return wsdlContent;
    }
    
    public List<SchemaInfo> getSchemas() {
        return schemas;
    }
    
    public int getState() {
        return status;
    }
    
    public String getWsdlFileName() {
        return wsdlFileName;
    }
    
    public String getWsdlUrl() {
        return wsdlUrlName;
    }
    
    // not sure this is necessary -- for controller to signal shutdown in case
    // interrupted() doesn't work.
    public synchronized void stopRetrieval() {
        shutdown = true;
    }
    
    private URL wsdlUrl;
    private URLConnection connection;
    private InputStream in;
    
    public void run() {
       
        wsdlUrl = null;
        connection = null;
        in = null;
        
        try {
            // !PW FIXME if we wanted to add an option to turn beautification of
            // the URL off (because our algorithm conflicted with what the user
            // need to enter), this is the method that such option would need to
            // disable.
            wsdlUrlName = beautifyUrlName(wsdlUrlName);
            wsdlUrl = new URL(wsdlUrlName);
            setState(STATUS_CONNECTING);
            connection = wsdlUrl.openConnection();
            if (connection instanceof HttpsURLConnection) {
                SSLSocketFactory sf = getSSLSocketFactory();
                ((HttpsURLConnection)connection).setSSLSocketFactory(sf);
                ((HttpsURLConnection)connection).setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String string, SSLSession sSLSession) {
                        // accept all hosts
                        return true;
                    }
                });
            }
            in = connection.getInputStream();
            
            setState(STATUS_DOWNLOADING);
            
            // Download the wsdl file
            wsdlContent = downloadWsdlFileEncoded(new BufferedInputStream(in));
            
            WsdlInfo wsdlInfo=null;
            // Dowdload all imported schemas
            if(!shutdown) {
                wsdlInfo = getWsdlInfo();
                if (wsdlInfo != null) {
                    List /*String*/ schemaNames = wsdlInfo.getSchemaNames();
                    if (!schemaNames.isEmpty()) {
                        schemas = new ArrayList<SchemaInfo>();
                        Iterator it = schemaNames.iterator();
                        while (!shutdown && it.hasNext()) {
                            String schemaName = (String)it.next();
                            String schemaUrlName = getSchemaUrlName(wsdlUrlName,schemaName);
                            URL schemaUrl = new URL(schemaUrlName);
                            connection = schemaUrl.openConnection();
                            in = connection.getInputStream();
                            schemas.add(new SchemaInfo(schemaName, downloadWsdlFileEncoded(new BufferedInputStream(in))));
                        }
                    }
                } else {
                    throw new MalformedURLException();
                }
            }
            if (!shutdown) {
                // extract the (first) service name to use as a suggested filename.
                List serviceNames = wsdlInfo.getServiceNameList();
                if(serviceNames != null && serviceNames.size() > 0) {
                    wsdlFileName = wsdlUrl.getPath();
                    int slashIndex = wsdlFileName.lastIndexOf('/');
                    if(slashIndex != -1) {
                        wsdlFileName = wsdlFileName.substring(slashIndex+1);
                    }
                    
                    if(wsdlFileName.length() == 0) {
                        wsdlFileName = (String) serviceNames.get(0) + ".wsdl"; // NOI18N
                    } else if(wsdlFileName.length() < 5 || !".wsdl".equals(wsdlFileName.substring(wsdlFileName.length()-5))) { // NOI18N
                        wsdlFileName += ".wsdl"; // NOI18N
                    }
                    setState(STATUS_COMPLETE);
                } else {
                    // !PW FIXME bad wsdl file -- can we save and return the parser error message?
                    setState(STATUS_BAD_WSDL);
                }
            } else {
                setState(STATUS_TERMINATED);
            }
        } catch(ConnectException ex) {
            setState(STATUS_FAILED, NbBundle.getMessage(WsdlRetriever.class, "ERR_Connection"), ex); // NOI18N
            log(ex.getMessage());
        } catch(MalformedURLException ex) {
            setState(STATUS_FAILED, NbBundle.getMessage(WsdlRetriever.class, "ERR_BadUrl"), ex); // NOI18N
            log(ex.getMessage());
        } catch(IOException ex) {
            setState(STATUS_FAILED, NbBundle.getMessage(WsdlRetriever.class, "ERR_IOException"), ex); // NOI18N
            log(ex.getMessage());
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch(IOException ex) {
                }
            }
        }
    }
    
    /** Retrieve the wsdl file from the specified inputstream.  We don't know how big
     *  the file might be, and while many WSDL files are less than 30-40K, eBay's
     *  WSDL is over 1MB, so there is extra logic here to be very flexible on buffer
     *  space with minimal copying.
     *
     *  This routine could possibly be cleaned up a bit, but might lose in readability.
     *  For example, 'chunksize' is probably redundant and could be replaced by 'i',
     *  but the java optimizer will do that for us anyway and the usage is more clear
     *  this way.
     *
     */
    private byte [] downloadWsdlFileEncoded(InputStream in) throws IOException {
        ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        final int BUF = 65536;
        boolean eof = false;
        byte [] data = new byte [0];
        
        while(!shutdown && !eof) {
            byte [] b = new byte[BUF]; // New buffer for this block
            int i = 0; // index within this block we're writing at
            int l = 0; // number of bytes read during last call to read().
            int limit = b.length; // maximum number of bytes to read during call to read()
            int chunksize = 0; // number of bytes read into this block.  Should be always be BUF, except for last block of file.
            
            while(!shutdown && (l = in.read(b, i, limit)) != -1) {
                limit -= l;
                i += l;
                chunksize += l;
                
                if(limit == 0) {
                    break;
                }
            }
            
            // if we downloaded any data, add a chunk containing the data to our list of chunks.
            if(chunksize > 0) {
                chunks.add(new Chunk(b, chunksize));
            }
            
            eof = (l == -1);
        }
        
        if(!shutdown) {
            // calculate length for single byte array that contains the entire WSDL
            int bufLen = 0;
            Iterator<Chunk> iter = chunks.iterator();
            while(iter.hasNext()) {
                bufLen += iter.next().getLength();
            }
            
            // Now fill the single byte array with all the chunks we downloaded.
            data = new byte[bufLen];
            int index = 0;
            iter = chunks.iterator();
            while(iter.hasNext()) {
                Chunk c = iter.next();
                System.arraycopy(c.getData(), 0, data, index, c.getLength());
                index += c.getLength();
            }
        }
        
        return data;
    }
    
    
    public static String beautifyUrlName(String urlName) {
        // 1. verify protocol, use http if not specified.
        if(urlName.indexOf("://") == -1) { // NOI18N
            return "http://" + urlName; // NOI18N
        } else {
            return urlName;
        }
    }
    
    private String getSchemaUrlName(String wsdlUrl, String schemaName) {
        int index = wsdlUrl.lastIndexOf("/"); //NOI18N
        if (index>=0) return wsdlUrl.substring(0,index+1)+schemaName;
        else return null;
    }
    
    private void setState(int newState) {
        status = newState;
        log(STATUS_MESSAGE[newState]);
        SwingUtilities.invokeLater(new MessageSender(receiver, STATUS_MESSAGE[newState]));
    }
    
    private void setState(int newState, String msg, Exception ex) {
        status = newState;
        Object [] args = new Object [] { msg, ex.getMessage()};
        String message = MessageFormat.format(STATUS_MESSAGE[newState], args);
        log(message);
        SwingUtilities.invokeLater(new MessageSender(receiver, message));
    }
    
    private void log(String message) {
        // This method for debugging only.
//        System.out.println(message);
    }
    
    // private class used to cache a message and post to UI component on AWT Thread.
    private static class MessageSender implements Runnable {
        private MessageReceiver receiver;
        private String message;
        
        public MessageSender(MessageReceiver r, String m) {
            receiver = r;
            message = m;
        }
        
        public void run() {
            receiver.setWsdlDownloadMessage(message);
        }
    }
    
    public interface MessageReceiver {
        public void setWsdlDownloadMessage(String m);
    }
    
    /** Private method to sanity check the overall format of the WSDL file and
     *  determine the names of the one or more services defined therein.
     */
    private WsdlInfo getWsdlInfo() {
        WsdlInfo result = null;
        
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            ServiceNameParser handler= new ServiceNameParser();
            saxParser.parse(new InputSource(new ByteArrayInputStream(wsdlContent)), handler);
            result = new WsdlInfo(handler.getServiceNameList(),handler.getSchemaNames());
        } catch(ParserConfigurationException ex) {
            // Bogus WSDL, return null.
        } catch(SAXException ex) {
            // Bogus WSDL, return null.
        } catch(IOException ex) {
            // Bogus WSDL, return null.
        }
        
        return result;
    }
    
    private static final class ServiceNameParser extends DefaultHandler {
        
        private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl"; // NOI18N
        private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N
        
        private List<String> serviceNameList;
        private List<String> schemaNames;
        
        private boolean insideSchema;
        
        ServiceNameParser() {
            serviceNameList = new ArrayList<String>();
            schemaNames = new ArrayList<String>();
        }
        
        @Override
        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("service".equals(localname)) { // NOI18N
                    serviceNameList.add(attributes.getValue("name")); // NOI18N
                }
                if("types".equals(localname)) { // NOI18N
                    insideSchema=true;
                }
                if("import".equals(localname)) { // NOI18N
                    String wsdlLocation = attributes.getValue("location"); //NOI18N
                    if (wsdlLocation!=null && wsdlLocation.indexOf("/")<0 && wsdlLocation.endsWith(".wsdl")) { //NOI18N
                        schemaNames.add(wsdlLocation);
                    }
                }
            }
            if(insideSchema && "import".equals(localname)) { // NOI18N
                String schemaLocation = attributes.getValue("schemaLocation"); //NOI18N
                if (schemaLocation!=null && schemaLocation.indexOf("/")<0 && schemaLocation.endsWith(".xsd")) { //NOI18N
                    schemaNames.add(schemaLocation);
                }
            }
        }
        
        @Override
        public void endElement(String uri, String localname, String qname) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("types".equals(localname)) { // NOI18N
                    insideSchema=false;
                }
            }
        }
        
        public List<String> getServiceNameList() {
            return serviceNameList;
        }
        
        public List<String> getSchemaNames() {
            return schemaNames;
        }
    }
    
    /** Data chunk of downloaded WSDL.
     */
    private static class Chunk {
        private int length;
        private byte [] data;
        
        public Chunk(byte [] d, int l) {
            data = d;
            length = l;
        }
        
        public byte [] getData() {
            return data;
        }
        
        public int getLength() {
            return length;
        }
    }
    
    private static class WsdlInfo {
        private List<String> serviceNameList;
        private List<String> schemaNames;
        
        WsdlInfo(List<String> serviceNameList,  List<String> schemaNames) {
            this.serviceNameList=serviceNameList;
            this.schemaNames=schemaNames;
        }
        
        List<String> getSchemaNames() {
            return schemaNames;
        }
        List<String> getServiceNameList() {
            return serviceNameList;
        }
    }
    
    public static class SchemaInfo {
        private String schemaName;
        private byte[] schemaContent;
        
        SchemaInfo(String schemaName, byte[] schemaContent) {
            this.schemaName=schemaName;
            this.schemaContent=schemaContent;
        }
        
        public String getSchemaName() {
            return schemaName;
        }
        
        public byte[] getSchemaContent() {
            return schemaContent;
        }
    }
    
    // Install the trust manager for retriever
    private SSLSocketFactory getSSLSocketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
                    // ask user to accept the unknown certificate
                    if (certs!=null) {
                        for (int i=0;i<certs.length;i++) {
                            DialogDescriptor desc = new DialogDescriptor(Retriever.getCertificationPanel(certs[i]),
                                    NbBundle.getMessage(WsdlRetriever.class,"TTL_CertifiedWebSite"),
                                    true,
                                    DialogDescriptor.YES_NO_OPTION,
                                    DialogDescriptor.YES_OPTION,
                                    null);
                            DialogDisplayer.getDefault().notify(desc);
                            if (!DialogDescriptor.YES_OPTION.equals(desc.getValue())) {
                                throw new CertificateException(
                                        NbBundle.getMessage(WsdlRetriever.class,"ERR_NotTrustedCertificate"));
                            }
                        } // end for
                    }
                }
            }
        };
        
        
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL"); //NOI18N
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (java.security.GeneralSecurityException e) {
            Logger.getLogger(WsdlRetriever.class.getName()).log(Level.WARNING, "Can not init SSL Context", e);
            return null;
        }
    
    }
}
