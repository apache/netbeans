/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.payara.eecommon.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DomainEditor {

    private static final Logger LOGGER = Logger.getLogger("payara-eecommon");
    private static final String HTTP_PROXY_HOST = "-Dhttp.proxyHost=";
    private static final String HTTP_PROXY_PORT = "-Dhttp.proxyPort=";
    private static final String HTTPS_PROXY_HOST = "-Dhttps.proxyHost=";
    private static final String HTTPS_PROXY_PORT = "-Dhttps.proxyPort=";
    private static final String HTTP_PROXY_NO_HOST = "-Dhttp.nonProxyHosts=";
    
    private static String SAMPLE_DATASOURCE = "jdbc/__default"; //NOI18N
    private static String SAMPLE_CONNPOOL = "H2Pool"; //NOI18N
    
    private static String NBPROFILERNAME = "NetBeansProfiler"; //NOI18N
    
    private static String CONST_USER = "User"; // NOI18N
    private static String CONST_PASSWORD = "Password"; // NOI18N
    private static String CONST_URL = "URL"; // NOI18N
    private static String CONST_LOWER_DATABASE_NAME = "databaseName"; // NOI18N
    private static String CONST_LOWER_PORT_NUMBER = "portNumber"; // NOI18N
    private static String CONST_DATABASE_NAME = "DatabaseName"; // NOI18N
    private static String CONST_PORT_NUMBER = "PortNumber"; // NOI18N
    private static String CONST_SID = "SID"; // NOI18N
    private static String CONST_SERVER_NAME = "serverName"; // NOI18N
    private static String CONST_DRIVER_CLASS = "driverClass"; // NOI18N
    private static String CONST_NAME = "name"; // NOI18N
    private static String CONST_VALUE = "value"; // NOI18N
    private static String CONST_DS_CLASS = "datasource-classname"; // NOI18N
    private static String CONST_RES_TYPE = "res-type"; // NOI18N
    private static String CONST_JVM_OPTIONS = "jvm-options"; // NOI18N
    private static String CONST_JNDINAME = "jndi-name"; // NOI18N
    private static String CONST_PROP = "property"; // NOI18N
    private static String CONST_POOLNAME = "pool-name"; // NOI18N
    private static String CONST_ENABLED = "enabled"; // NOI18N
    private static String CONST_OBJTYPE = "object-type"; // NOI18N
    private static String CONST_JDBC = "jdbc-resource"; // NOI18N
    private static String CONST_CP = "jdbc-connection-pool"; // NOI18N
    private static String CONST_AO = "admin-object-resource"; // NOI18N
    private static String XML_ENTITY = "<?xml version=\"1.0\" encoding=\"{0}\"?>";
    
    private String dmLoc;
    private String dmName;

    /**
     * Creates a new instance of DomainEditor
     * @param dm Deployment Manager of Target Server
     */
    public DomainEditor(String domainLocation, String domainName) {
        this.dmLoc = domainLocation;
        this.dmName = domainName;
    }
    
    /**
     * Get the location of the server's domain.xml
     * @return String representing path to domain.xml
     */
    public String getDomainLocation(){
        String domainScriptFilePath = dmLoc+"/" + dmName + "/config/domain.xml"; //NOI18N
        return domainScriptFilePath;
    }
    
    /**
     * Get Document Object representing the domain.xml    
     * @return Document object representing given domain.xml
     */
    public Document getDomainDocument(){
        String domainLoc = getDomainLocation();
        
        // Load domain.xml
        Document domainScriptDocument = getDomainDocument(domainLoc);
        return domainScriptDocument;
    }
    
    /**
     * Get Document Object representing the domain.xml
     * @param domainLoc Location of domain.xml
     * @return Document object representing given domain.xml
     */
    public Document getDomainDocument(String domainLoc){
        // Load domain.xml
        Document domainScriptDocument = loadDomainScriptFile(domainLoc);
        return domainScriptDocument;
    }
    
    /**
     * Perform server instrumentation for profiling
     * @param domainDoc Document object representing domain.xml
     * @param nativeLibraryPath Native Library Path
     * @param jvmOptions Values for jvm-options to enable profiling
     * @return returns true if server is ready for profiling
     */
    public boolean addProfilerElements(Document domainDoc, String nativeLibraryPath, String[] jvmOptions){
        String domainPath = getDomainLocation();
        
        // Remove any previously defined 'profiler' element(s)
        removeProfiler(domainDoc);
        
        // If no 'profiler' element needs to be defined, the existing one is simply removed (by the code above)
        // (This won't happen for NetBeans Profiler, but is a valid scenario)
        // Otherwise new 'profiler' element is inserted according to provided parameters
        if (nativeLibraryPath != null || jvmOptions != null) {
            
            // Create "profiler" element
            Element profilerElement = domainDoc.createElement("profiler");//NOI18N
            profilerElement.setAttribute("enabled", "true");//NOI18N
            profilerElement.setAttribute(CONST_NAME, NBPROFILERNAME);//NOI18N
            if (nativeLibraryPath != null) {
                profilerElement.setAttribute("native-library-path", nativeLibraryPath);//NOI18N
            }
            
            // Create "jvm-options" element
            if (jvmOptions != null) {
                for (int i = 0; i < jvmOptions.length; i++) {
                    Element jvmOptionsElement = domainDoc.createElement(CONST_JVM_OPTIONS);
                    Text tt = domainDoc.createTextNode(formatJvmOption(jvmOptions[i]));
                    jvmOptionsElement.appendChild(tt);
                    profilerElement.appendChild(jvmOptionsElement);
                }
            }
            
            // Find the "java-config" element
            NodeList javaConfigNodeList = domainDoc.getElementsByTagName("java-config");
            if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
                LOGGER.log(Level.INFO,
                        "Cannot find 'java-config' section in domain config file {0}",
                        domainPath);
                return false;
            }
            
            // Insert the "profiler" element as a first child of "java-config" element
            Node javaConfigNode = javaConfigNodeList.item(0);
            if (javaConfigNode.getFirstChild() != null) 
                javaConfigNode.insertBefore(profilerElement, javaConfigNode.getFirstChild());
            else 
                javaConfigNode.appendChild(profilerElement);
            
        }
        // Save domain.xml
        return saveDomainScriptFile(domainDoc, domainPath);
    }
    
    /**
     * Remove server instrumentation to disable profiling
     * @param domainDoc Document object representing domain.xml
     * @return true if profiling support has been removed
     */
    public boolean removeProfilerElements(Document domainDoc){
        boolean eleRemoved = removeProfiler(domainDoc);
        if(eleRemoved){
            // Save domain.xml
            return saveDomainScriptFile(domainDoc, getDomainLocation());
        }else{
            //no need to save.
            return true;
        }    
    }
    
    private boolean removeProfiler(Document domainDoc){
        // Remove any previously defined 'profiler' element(s)
        NodeList profilerElementNodeList = domainDoc.getElementsByTagName("profiler");//NOI18N
        if (profilerElementNodeList != null && profilerElementNodeList.getLength() > 0){
            Vector<Node> nodes = new Vector<Node>(); //temp storage for the nodes to delete
            //we only want to delete the NBPROFILERNAME nodes.
            // otherwise, see bug # 77026
            for (int i = 0; i < profilerElementNodeList.getLength(); i++) {
                Node n= profilerElementNodeList.item(i);                
                Node a= n.getAttributes().getNamedItem(CONST_NAME);//NOI18N
                if ((a!=null)&&(a.getNodeValue().equals(NBPROFILERNAME))){//NOI18N
                    nodes.add(n);
                }                              
            }
            for(int i=0; i<nodes.size(); i++){
                Node nd = nodes.get(i);
                nd.getParentNode().removeChild(nd);
            }
            return true;
        }
            
        return false;
    }
       
    public String[] getHttpProxyOptions(){
        List<String> httpProxyOptions = new ArrayList<String>();
        Document domainDoc = getDomainDocument();
        NodeList javaConfigNodeList = domainDoc.getElementsByTagName("java-config");
        if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
            return httpProxyOptions.toArray(new String[0]);
        }
        
        NodeList jvmOptionNodeList = domainDoc.getElementsByTagName(CONST_JVM_OPTIONS);
        for(int i=0; i<jvmOptionNodeList.getLength(); i++){
            Node nd = jvmOptionNodeList.item(i);
            if(nd.hasChildNodes())  {
                Node childNode = nd.getFirstChild();
                String childValue = childNode.getNodeValue();
                if(childValue.indexOf(HTTP_PROXY_HOST) != -1
                        || childValue.indexOf(HTTP_PROXY_PORT) != -1
                        || childValue.indexOf(HTTPS_PROXY_HOST) != -1
                        || childValue.indexOf(HTTPS_PROXY_PORT) != -1
                        || childValue.indexOf(HTTP_PROXY_NO_HOST) != -1){
                    httpProxyOptions.add(childValue);
                }
            }
        }

        String[] opts = new String[httpProxyOptions.size()];
        return httpProxyOptions.toArray(opts);
        
    }
    
    public boolean setHttpProxyOptions(String[] httpProxyOptions){
        Document domainDoc = getDomainDocument();
        NodeList javaConfigNodeList = domainDoc.getElementsByTagName("java-config");
        if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
            return false;
        }
        
        //Iterates through the existing proxy attributes and deletes them
        removeProxyOptions(domainDoc, javaConfigNodeList.item(0));
                
        //Add new set of proxy options
        for(int j=0; j<httpProxyOptions.length; j++){
            String option = httpProxyOptions[j];
            Element jvmOptionsElement = domainDoc.createElement(CONST_JVM_OPTIONS);
            Text proxyOption = domainDoc.createTextNode(option);
            jvmOptionsElement.appendChild(proxyOption);
            javaConfigNodeList.item(0).appendChild(jvmOptionsElement);
        }
        
        return saveDomainScriptFile(domainDoc, getDomainLocation(), false);
    }
      
    private boolean removeProxyOptions(Document domainDoc, Node javaConfigNode){
        NodeList jvmOptionNodeList = domainDoc.getElementsByTagName(CONST_JVM_OPTIONS);
        
        Vector<Node> nodes = new Vector<Node>();
        for(int i=0; i<jvmOptionNodeList.getLength(); i++){
            Node nd = jvmOptionNodeList.item(i);
            if(nd.hasChildNodes())  {
                Node childNode = nd.getFirstChild();
                String childValue = childNode.getNodeValue();
                if(childValue.indexOf(HTTP_PROXY_HOST) != -1
                        || childValue.indexOf(HTTP_PROXY_PORT) != -1
                        || childValue.indexOf(HTTPS_PROXY_HOST) != -1
                        || childValue.indexOf(HTTPS_PROXY_PORT) != -1
                        || childValue.indexOf(HTTP_PROXY_NO_HOST) != -1){
                   nodes.add(nd);
                }
            }
        }
        for(int i=0; i<nodes.size(); i++){
            javaConfigNode.removeChild(nodes.get(i));
        }
        
        return saveDomainScriptFile(domainDoc, getDomainLocation(), false);
    }
    
    /*
     * Creates Document instance from domain.xml
     * @param domainScriptFilePath Path to domain.xml
     */
    private Document loadDomainScriptFile(String domainScriptFilePath) {
        InputStreamReader reader = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dBuilder.setEntityResolver(new InnerResolver());
            reader = new FileReader(new File(domainScriptFilePath));
            InputSource source = new InputSource(reader);
            return dBuilder.parse(source);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "Unable to parse domain config file {0}",
                    domainScriptFilePath);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, "Cannot close reader for {0}: {1}",
                            new String[] {domainScriptFilePath, ex.getLocalizedMessage()});
                }
            }
        }
    }

    static class InnerResolver implements EntityResolver {
        private final Charset charset;
        InnerResolver() {
            charset = Charset.defaultCharset();
        }
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            String xmlEntity = MessageFormat.format(XML_ENTITY, charset.name());
            StringReader reader = new StringReader(xmlEntity);
            InputSource source = new InputSource(reader);
            source.setPublicId(publicId);
            source.setSystemId(systemId);
            return source;
        }
    }
    private boolean saveDomainScriptFile(Document domainScriptDocument, String domainScriptFilePath) {
        return saveDomainScriptFile(domainScriptDocument, domainScriptFilePath, true);
    }
    /*
     * Saves Document instance to domain.xml
     * @param domainScriptDocument Document representing the xml
     * @param domainScriptFilePath Path to domain.xml
     */
    private boolean saveDomainScriptFile(Document domainScriptDocument, String domainScriptFilePath, boolean indent) {
        boolean result = false;
        OutputStreamWriter domainXmlWriter = null;
        final Charset charset = Charset.defaultCharset();
        try {
            domainXmlWriter = new OutputStreamWriter(new FileOutputStream(domainScriptFilePath), charset.name());
            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                if(indent) {
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                }

                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
                if(domainScriptDocument.getDoctype() != null) {
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, domainScriptDocument.getDoctype().getPublicId());
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, domainScriptDocument.getDoctype().getSystemId());
                }
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                
                DOMSource domSource = new DOMSource(domainScriptDocument);
                StreamResult streamResult = new StreamResult(domainXmlWriter);
                
                transformer.transform(domSource, streamResult);
                result = true;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "Unable to save domain config file {0}",
                        domainScriptFilePath);
                result = false;
            }
        } catch (IOException ioex) {
            LOGGER.log(Level.INFO,
                    "Cannot create output stream for domain config file {0}",
                    domainScriptFilePath);
            result = false;
        } finally {
            try { 
                if (domainXmlWriter != null)  {
                    domainXmlWriter.close(); 
                }
            } catch (IOException ioex2) {
                LOGGER.log(Level.INFO,
                        "Cannot close output stream for {0}",
                        domainScriptFilePath);
            }
        }
        
        return result;
    }
    
    // Converts -agentpath:"C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\"",5140
    // to -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140 (AS 8.1 and AS 8.2)
    // or to  "-agentpath:C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\",5140" (Payara or AS 9.0)
    private String formatJvmOption(String jvmOption) {
        // only jvmOption containing \" needs to be formatted
        if (jvmOption.indexOf("\"") != -1) { // NOI18N
            // special handling for -agentpath
            if (jvmOption.indexOf("\\\"") != -1 && jvmOption.indexOf("-agentpath") != -1 ){ // NOI18N
            // Modification for AS 8.1, 8.2, initial modification for AS 9.0, Payara
            // Converts -agentpath:"C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\"",5140
            // to -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140
            String modifiedOption = jvmOption.replaceAll("\\\\\"", "#"); // replace every \" by #
            modifiedOption = modifiedOption.replaceAll("\\\"", ""); // delete all "
            modifiedOption = modifiedOption.replaceAll("#", "\""); // replace every # by "

            // Modification for AS 9.0, Payara should be done only if native launcher isn't used,
            // otherwise will cause server startup failure. It seems that currently native launcher is used
            // for starting the servers from the IDE.
   //         boolean usingNativeLauncher = false;
            String osType=System.getProperty("os.name");//NOI18N
            if ((osType.startsWith("Mac OS"))){//no native for mac of glassfish
  //          if (!usingNativeLauncher) {

                // Modification for AS 9.0, Payara
                // Converts -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140
                // "-agentpath:C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\",5140"

                    modifiedOption = "\"" + modifiedOption.replaceAll("\\\"", "\\\\\"") + "\"";

            }

            // return correctly formatted jvmOption
            return modifiedOption;
            } else {
                return jvmOption.replace('"', ' ');
        }
        }
        // return original jvmOption
        return jvmOption;
     }
    
    static final String[] sysDatasources = {"jdbc/__TimerPool", "jdbc/__CallFlowPool"}; //NOI18N
    

    public HashMap<String,Map> getSunDatasourcesFromXml(){
        HashMap<String,Map> dSources = new HashMap<>();
        Document domainDoc = getDomainDocument();
        if (domainDoc != null) {
            HashMap<String,NamedNodeMap> dsMap = getDataSourcesAttrMap(domainDoc);
            HashMap<String,Node> cpMap = getConnPoolsNodeMap(domainDoc);
            dsMap.keySet().removeAll(Arrays.asList(sysDatasources));
            String[] ds = dsMap.keySet().toArray(new String[dsMap.size()]);

            for (int i = 0; i < ds.length; i++) {
                String jndiName = ds[i];
                NamedNodeMap dsAttrMap = dsMap.get(jndiName);

                String poolName = dsAttrMap.getNamedItem(CONST_POOLNAME).getNodeValue();
                dSources.put(jndiName, getPoolValues(cpMap, poolName));
            } // for each jdbc-resource
        }
        return dSources;
    }

    private Map<String,String> getPoolValues(Map<String, Node> cpMap, String poolName) {
        Map<String,String> pValues = new HashMap<>();
        Node cpNode = (Node) cpMap.get(poolName);
        NamedNodeMap cpAttrMap = cpNode.getAttributes();
        Node dsClassName = cpAttrMap.getNamedItem(CONST_DS_CLASS);
        Node resType = cpAttrMap.getNamedItem(CONST_RES_TYPE);

        //Get property values
        Element cpElement = (Element) cpNode;
        NodeList propsNodeList = cpElement.getElementsByTagName(CONST_PROP);

        //Cycle through each property element
        Map<String, String> map = new HashMap<>();
        for (int j = 0; j < propsNodeList.getLength(); j++) {
            Node propNode = propsNodeList.item(j);
            NamedNodeMap propsMap = propNode.getAttributes();
            String mkey = propsMap.getNamedItem(CONST_NAME).getNodeValue();
            String mkeyValue = propsMap.getNamedItem(CONST_VALUE).getNodeValue();
            if (mkey.equalsIgnoreCase(CONST_USER)) {
                pValues.put(CONST_USER, mkeyValue);
            } else if (mkey.equalsIgnoreCase(CONST_PASSWORD)) {
                pValues.put(CONST_PASSWORD, mkeyValue);
            } else if (mkey.equalsIgnoreCase(CONST_URL)) {
                pValues.put(CONST_URL, mkeyValue);
            } else if (mkey.equalsIgnoreCase(CONST_SERVER_NAME)) {
                pValues.put(CONST_SERVER_NAME, mkeyValue);
            } else {
                map.put(mkey, mkeyValue);
            }
        } // connection-pool properties

        pValues.put(CONST_LOWER_DATABASE_NAME, map.get(CONST_LOWER_DATABASE_NAME));
        pValues.put(CONST_PORT_NUMBER, map.get(CONST_PORT_NUMBER));
        pValues.put(CONST_LOWER_PORT_NUMBER, map.get(CONST_LOWER_PORT_NUMBER));
        pValues.put(CONST_DATABASE_NAME, map.get(CONST_DATABASE_NAME));
        pValues.put(CONST_SID, map.get(CONST_SID));
        pValues.put(CONST_DRIVER_CLASS, map.get(CONST_DRIVER_CLASS));
        if (dsClassName != null) {
            pValues.put("dsClassName", dsClassName.getNodeValue());
        }
        if (resType != null) {
            pValues.put("resType", resType.getNodeValue());
        }
        return pValues;
    }

    public HashMap<String,Map> getConnPoolsFromXml(){
        HashMap<String,Map> pools = new HashMap<>();
        Document domainDoc = getDomainDocument();
        if (domainDoc != null) {
            HashMap<String,Node> cpMap = getConnPoolsNodeMap(domainDoc);

            String[] cp = cpMap.keySet().toArray(new String[cpMap.size()]);
            for (int i = 0; i < cp.length; i++) {
                String name = cp[i];
                pools.put(name, getPoolValues(cpMap, name));
            }
        }
        return pools;
    }
    
    private HashMap<String,NamedNodeMap> getDataSourcesAttrMap(Document domainDoc){
        HashMap<String,NamedNodeMap> dataSourceMap = new HashMap<String,NamedNodeMap>();
        updateWithSampleDataSource(domainDoc);
        NodeList dataSourceNodeList = domainDoc.getElementsByTagName(CONST_JDBC);
        for(int i=0; i<dataSourceNodeList.getLength(); i++){
            Node dsNode = dataSourceNodeList.item(i);
            NamedNodeMap dsAttrMap = dsNode.getAttributes();
            String jndiName = dsAttrMap.getNamedItem(CONST_JNDINAME).getNodeValue();
            dataSourceMap.put(jndiName, dsAttrMap);
        }
        return dataSourceMap;
    }
    
    public void createSampleDatasource(){
        Document domainDoc = getDomainDocument();
        if (domainDoc != null) {
            updateWithSampleDataSource(domainDoc);
        }
    }
    
    private boolean updateWithSampleDataSource(Document domainDoc){
        boolean sampleExists = false;
        NodeList dataSourceNodeList = domainDoc.getElementsByTagName(CONST_JDBC);
        for(int i=0; i<dataSourceNodeList.getLength(); i++){
            Node dsNode = dataSourceNodeList.item(i);
            NamedNodeMap dsAttrMap = dsNode.getAttributes();
            String jndiName = dsAttrMap.getNamedItem(CONST_JNDINAME).getNodeValue();
            if(jndiName.equals(SAMPLE_DATASOURCE)) {
                sampleExists = true;
            }
        }
        if(!sampleExists) {
            return createSampleDatasource(domainDoc);
        }
        return true;
    }
    
    private boolean createSampleDatasource(Document domainDoc){
        NodeList resourcesNodeList = domainDoc.getElementsByTagName("resources");
        NodeList serverNodeList = domainDoc.getElementsByTagName("server");
        if (resourcesNodeList == null || resourcesNodeList.getLength() == 0 ||
                serverNodeList == null || serverNodeList.getLength() == 0) {
            return true;
        }
        Node resourcesNode = resourcesNodeList.item(0);

        Map<String,Node> cpMap = getConnPoolsNodeMap(domainDoc);
        if(! cpMap.containsKey(SAMPLE_CONNPOOL)){
            if (cpMap.isEmpty()) {
                LOGGER.log(Level.INFO,
                        "Cannot create sample datasource {0}",
                        SAMPLE_DATASOURCE);
                return false;
            }
            Node oldNode = cpMap.values().iterator().next();
            Node cpNode = oldNode.cloneNode(false);
            NamedNodeMap cpAttrMap = cpNode.getAttributes();
            if(cpAttrMap.getNamedItem(CONST_NAME) != null) {
                cpAttrMap.getNamedItem(CONST_NAME).setNodeValue(SAMPLE_CONNPOOL);
            }
            if(cpAttrMap.getNamedItem(CONST_DS_CLASS) != null) {
                cpAttrMap.getNamedItem(CONST_DS_CLASS).setNodeValue("org.h2.jdbcx.JdbcDataSource"); //N0I18N
            }
            if(cpAttrMap.getNamedItem(CONST_RES_TYPE) != null) {
                cpAttrMap.getNamedItem(CONST_RES_TYPE).setNodeValue("javax.sql.DataSource"); //N0I18N
            }
            HashMap<String, String> poolProps = new HashMap<>();
            poolProps.put(CONST_URL, "jdbc:h2:${com.sun.aas.instanceRoot}/lib/databases/embedded_default;AUTO_SERVER=TRUE"); //N0I18N

            Object[] propNames = poolProps.keySet().toArray();
            for(int i=0; i<propNames.length; i++){
                String keyName = (String)propNames[i];
                Element propElement = domainDoc.createElement(CONST_PROP); //N0I18N
                propElement.setAttribute(CONST_NAME, keyName);
                propElement.setAttribute(CONST_VALUE, poolProps.get(keyName)); //N0I18N
                cpNode.appendChild(propElement);
            }
            resourcesNode.appendChild(cpNode);
        }

        Element dsElement = domainDoc.createElement(CONST_JDBC); //N0I18N
        dsElement.setAttribute(CONST_JNDINAME, SAMPLE_DATASOURCE); //N0I18N
        dsElement.setAttribute(CONST_POOLNAME, SAMPLE_CONNPOOL); //N0I18N
        dsElement.setAttribute(CONST_OBJTYPE, "user"); //N0I18N
        dsElement.setAttribute(CONST_ENABLED, "true"); //N0I18N

        // Insert the ds __Sample as a first child of "resources" element
        if (resourcesNode.getFirstChild() != null)
            resourcesNode.insertBefore(dsElement, resourcesNode.getFirstChild());
        else
            resourcesNode.appendChild(dsElement);

        //<resource-ref enabled="true" ref="jdbc/__default"/>
        Element dsResRefElement = domainDoc.createElement("resource-ref"); //N0I18N
        dsResRefElement.setAttribute("ref", SAMPLE_DATASOURCE); //N0I18N
        dsResRefElement.setAttribute(CONST_ENABLED, "true"); //N0I18N
        // Insert the ds reference __Sample as last child of "server" element
        Node serverNode = serverNodeList.item(0);
        if (serverNode.getLastChild() != null)
            serverNode.insertBefore(dsResRefElement, serverNode.getLastChild());
        else
            serverNode.appendChild(dsResRefElement);

        return saveDomainScriptFile(domainDoc, getDomainLocation());
    }

    private HashMap<String,Node> getConnPoolsNodeMap(Document domainDoc){
        HashMap<String,Node> connPoolMap = new HashMap<>();
        NodeList connPoolNodeList = domainDoc.getElementsByTagName(CONST_CP);
        for(int i=0; i<connPoolNodeList.getLength(); i++){
            Node cpNode = connPoolNodeList.item(i);
            NamedNodeMap cpAttrMap = cpNode.getAttributes();
            String cpName = cpAttrMap.getNamedItem(CONST_NAME).getNodeValue();
            connPoolMap.put(cpName, cpNode);
        }
        return connPoolMap;
    }

    public HashMap<String,String> getAdminObjectResourcesFromXml(){
        HashMap<String,String> aoResources = new HashMap<>();
        Document domainDoc = getDomainDocument();
        if (domainDoc != null) {
            NodeList adminObjectNodeList = domainDoc.getElementsByTagName(CONST_AO);
            for (int i = 0; i < adminObjectNodeList.getLength(); i++) {
                Node aoNode = adminObjectNodeList.item(i);
                NamedNodeMap aoAttrMap = aoNode.getAttributes();
                String jndiName = aoAttrMap.getNamedItem(CONST_JNDINAME).getNodeValue();

                Node type = aoAttrMap.getNamedItem(CONST_RES_TYPE);
                if (type != null){
                    aoResources.put(jndiName, type.getNodeValue());
                }
            }
        }
        return aoResources;
    }
       
}
