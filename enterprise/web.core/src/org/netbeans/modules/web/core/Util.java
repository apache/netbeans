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

package org.netbeans.modules.web.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.xml.sax.*;

/** Utility class
* @author  Petr Jiricka
* @version 1.00, Jun 03, 1999
*/
public class Util {

    private static final Logger LOG = Logger.getLogger(Util.class.getName());
    
    /** Waits for startup of a server, waits until the connection has
     * been established. */ 
    public static boolean waitForURLConnection(URL url, int timeout, int retryTime) { 
        Connect connect = new Connect(url, retryTime); 
        Thread t = new Thread(connect);
        t.start();
        try {
            t.join(timeout);
        } catch(InterruptedException ie) {
            LOG.log(Level.FINE, "error", ie);
        }
        if (t.isAlive()) {
            connect.finishLoop();
            t.interrupt();//for thread deadlock
        }
        return connect.getStatus();
    }

    public static String issueGetRequest(URL url) {
        BufferedReader in = null;
        StringBuffer input = new StringBuffer();
        try {
            in = new BufferedReader(new InputStreamReader(
                                        url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                input.append(inputLine);
                input.append("\n"); // NOI18N
            }  
            return input.toString();
        }
        catch (Exception e) {
            return null;
        }
        finally {
            if (in != null)
                try {
                    in.close();
                }
                catch(IOException e) {
                    LOG.log(Level.FINE, "error", e);
                }
        }
    }

    private static class Connect implements Runnable  {

        URL url = null;
        int retryTime;
        boolean status = false;
        boolean loop = true;

        public Connect(URL url, int retryTime) {
            this.url = url;
            this.retryTime = retryTime; 
        } 

        public void finishLoop() {
            loop = false;
        }

        public void run() {
            try {
                InetAddress.getByName(url.getHost());
            } catch (UnknownHostException e) {
                LOG.log(Level.FINE, "error", e);
                return;
            }
            while (loop) {
                try {
                    Socket socket = new Socket(url.getHost(), url.getPort());
                    socket.close();
                    status = true;
                    break;
                } catch (UnknownHostException e) {
                    LOG.log(Level.FINE, "error", e);
                    //nothing to do
                } catch (IOException e) {
                    LOG.log(Level.FINE, "error", e);
                    //nothing to do
                }
                try {
                    Thread.currentThread().sleep(retryTime);
                } catch(InterruptedException ie) {
                    LOG.log(Level.FINE, "error", ie);
                }
            }
        }

        boolean getStatus() {
            return status;
        }
    }

    // FIXME: consider removing of duplicate/similar code
    // following block is copy/pasted code from 
    // org.netbeans.modules.j2ee.common.Util
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    public static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<SourceGroup> testGroups = getTestSourceGroups(project, sourceGroups);
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        for (SourceGroup sourceGroup : sourceGroups) {
            if (!testGroups.contains(sourceGroup)) {
                result.add(sourceGroup);
            }
        }
        return result.toArray(new SourceGroup[0]);
    }

    private static Set<SourceGroup> getTestSourceGroups(Project project, SourceGroup[] sourceGroups) {
        Map<FileObject,SourceGroup> foldersToSourceGroupsMap =
                createFoldersToSourceGroupsMap(sourceGroups);
        Set<SourceGroup> testGroups = new HashSet<SourceGroup>();
        for (SourceGroup sourceGroup : sourceGroups) {
            testGroups.addAll(getTestTargets(sourceGroup, foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static Map<FileObject,SourceGroup> createFoldersToSourceGroupsMap(
            final SourceGroup[] sourceGroups)
    {
        Map<FileObject,SourceGroup> result;
        if (sourceGroups.length == 0) {
            result = Collections.emptyMap();
        } else {
            result = new HashMap<FileObject,SourceGroup>(2 * sourceGroups.length, .5f);
            for (SourceGroup sourceGroup : sourceGroups) {
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }

    private static List<FileObject> getFileObjects(URL[] urls) {
        List<FileObject> result = new ArrayList<FileObject>();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                if (LOG.isLoggable(Level.FINE)) {  //NOI18N
                    LOG.log(Level.FINE, null, new IllegalStateException("No FileObject found for the following URL: " + urls[i]));  //NOI18N
                }
            }
        }
        return result;
    }
    
    private static List<SourceGroup> getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return new ArrayList<SourceGroup>();
        }
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        List<FileObject> sourceRoots = getFileObjects(rootURLs);
        for (FileObject sourceRoot : sourceRoots) {
            SourceGroup srcGroup = (SourceGroup) foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }
    
    /** Parsing to get Set of Strings that correpond to tagName valeus inside elName, e.g.:
     *  to get all <servlet-name> values inside the <servlet> elements (in web.xml)
    */    
    public static Set getTagValues (java.io.InputStream is, String elName, String tagName) throws java.io.IOException, SAXException {
        return getTagValues(is,new String[]{elName},tagName);
    }
    /** Parsing to get Set of Strings that correpond to tagName valeus inside elNames, e.g.:
     *  to get all <name> values inside the <tag> and <tag-file> elements (in TLD)
    */
    public static Set getTagValues (java.io.InputStream is, String[] elNames, String tagName) throws java.io.IOException, SAXException {
        javax.xml.parsers.SAXParserFactory fact = javax.xml.parsers.SAXParserFactory.newInstance();
        fact.setValidating(false);
        try {
            javax.xml.parsers.SAXParser parser = fact.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            TLDVersionHandler handler = new TLDVersionHandler(elNames,tagName);
            reader.setContentHandler(handler);
            try {
                reader.parse(new InputSource(is));
            } catch (SAXException ex) {
                LOG.log(Level.FINE, "error", ex);
            }
            return handler.getValues();
        } catch(javax.xml.parsers.ParserConfigurationException ex) {
            return new java.util.HashSet();
        }
    }
    
    private static class TLDVersionHandler extends org.xml.sax.helpers.DefaultHandler {
        private String tagName;
        private Set<String> elNames;
        private Set<String> values;
        private boolean insideEl, insideTag;

        TLDVersionHandler(String[] elNames, String tagName) {
            this.elNames=new HashSet<String>();
            for (int i=0;i<elNames.length;i++) {
                this.elNames.add(elNames[i]);
            }
            this.tagName=tagName;
            values = new HashSet<String>();
        }
        @Override
        public void startElement(String uri, String localName, String rawName, Attributes atts) throws SAXException {
            if (elNames.contains(rawName)) insideEl=true;
            else if (tagName.equals(rawName) && insideEl) {
                insideTag=true;
            }
        }
        @Override
        public void endElement(String uri, String localName, String rawName) throws SAXException {
            if (elNames.contains(rawName)) insideEl=false;
            else if (tagName.equals(rawName) && insideEl) {
                insideTag=false;
            }
        }
        
        @Override
        public void characters(char[] ch,int start,int length) throws SAXException {
            if (insideTag) {
                values.add(String.valueOf(ch,start,length).trim());
            }
        }
        public Set getValues() {
            return values;
        }
    }
    
}
