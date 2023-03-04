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

package org.netbeans.modules.hudson.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonFolder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.modules.hudson.spi.BuilderConnector;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.spi.RemoteFileSystem;
import org.openide.util.Lookup;
import org.openide.windows.OutputListener;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import static org.netbeans.modules.hudson.constants.HudsonXmlApiConstants.*;

/**
 * Hudson Server Connector
 *
 * @author Michal Mocnak
 */
public class HudsonConnector extends BuilderConnector {
    private static final Logger LOG = Logger.getLogger(HudsonConnector.class.getName());
    public static final HudsonFailureDataProvider HUDSON_FAILURE_DISPLAYER =
            new HudsonFailureDataProvider();
    public static final HudsonConsoleDataProvider HUDSON_CONSOLE_DISPLAYER =
            new HudsonConsoleDataProvider();
    
    private HudsonVersion version;
    private boolean connected = false;
    /** #182689: true if have no anon access and need to log in just to see job list */
    boolean forbidden;
    
    private Map<String, ViewData> cache = new HashMap<String, ViewData>();
    private String instanceUrl;

    public HudsonConnector(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }
    
    private boolean canUseTree(boolean authentication) {
        HudsonVersion v = getHudsonVersion(authentication);
        return v != null && v.compareTo(new HudsonVersion("1.367")) >= 0; // NOI18N
    }
    
    @Override
    public synchronized InstanceData getInstanceData(boolean authentication) {
        Document docInstance = getDocument(instanceUrl + XML_API_URL + (canUseTree(authentication) ?
                "?tree=primaryView[name],views[name,url,jobs[name]]," +
                "jobs[name,url,color,displayName,buildable,inQueue," +
                "primaryView," + // #215135: marker for folders
                "lastBuild[number],lastFailedBuild[number],lastStableBuild[number],lastSuccessfulBuild[number],lastCompletedBuild[number]," +
                "modules[name,displayName,url,color]]," +
                "securedJobs[name,url]" : // HUDSON-3924
                "?depth=1&xpath=/&exclude=//assignedLabel&exclude=//primaryView/job" +
                "&exclude=//view/job/url&exclude=//view/job/color&exclude=//description&exclude=//job/build&exclude=//healthReport" +
                "&exclude=//firstBuild&exclude=//keepDependencies&exclude=//nextBuildNumber&exclude=//property&exclude=//action" +
                "&exclude=//upstreamProject&exclude=//downstreamProject&exclude=//queueItem&exclude=//scm&exclude=//concurrentBuild" +
                "&exclude=//job/lastUnstableBuild&exclude=//job/lastUnsuccessfulBuild"), authentication); // NOI18N
        
        if (null == docInstance) {
            return new InstanceData(
                    Collections.<JobData>emptyList(),
                    Collections.<ViewData>emptyList(),
                    Collections.<FolderData>emptyList());
        }
        // Clear cache
        cache.clear();
        // Parse jobs and return them
        Collection<ViewData> viewsData = getViewData(docInstance, instanceUrl);
        Collection<FolderData> foldersData = new ArrayList<FolderData>();
        Collection<JobData> jobsData = getJobsData(docInstance, instanceUrl, viewsData, foldersData, null);
        return new InstanceData(jobsData, viewsData, foldersData);
    }

    @Override public InstanceData getInstanceData(HudsonFolder parentFolder, boolean authentication) {
        Document docInstance = getDocument(parentFolder.getUrl() + XML_API_URL + "?tree=jobs[name,url,color,displayName,buildable,inQueue,primaryView," +
                "lastBuild[number],lastFailedBuild[number],lastStableBuild[number],lastSuccessfulBuild[number],lastCompletedBuild[number]," +
                "modules[name,displayName,url,color]]," +
                "securedJobs[name,url]", authentication); // NOI18N

        if (null == docInstance) {
            return new InstanceData(
                    Collections.<JobData>emptyList(),
                    Collections.<ViewData>emptyList(),
                    Collections.<FolderData>emptyList());
        }
        // Clear cache
        cache.clear();
        Collection<FolderData> foldersData = new ArrayList<FolderData>();
        Collection<JobData> jobsData = getJobsData(docInstance, parentFolder.getUrl(), Collections.<ViewData>emptySet(), foldersData, parentFolder);
        return new InstanceData(jobsData, Collections.<ViewData>emptySet(), foldersData);
    }

    @Override
    public synchronized void startJob(final HudsonJob job) {
        try {
            new ConnectionBuilder().homeURL(instanceUrl).
                    url(job.getUrl() + "build"). //NOI18N
                    postData("delay=0sec".getBytes(StandardCharsets.UTF_8)). //NOI18N
                    followRedirects(false).connection(); // NOI18N
        } catch (MalformedURLException mue) {
            LOG.log(Level.INFO, "Malformed URL " + instanceUrl, mue);
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not start {0}: {1}", new Object[] {job, e});
        }
    }

    /**
     * Gets general information about a build.
     * The changelog ({@code <changeSet>}) can be interpreted separately by {@link HudsonJobBuild#getChanges}.
     */
    @Override
    public Collection<BuildData> getJobBuildsData(HudsonJob job) {
        Document docBuild = getDocument(job.getUrl() + XML_API_URL + (canUseTree(true) ?
            "?tree=builds[number,result,building]" :
            "?xpath=/*/build&wrapper=root&exclude=//url"), true);
        if (docBuild == null) {
            return Collections.emptySet();
        }
        List<BuildData> builds = new ArrayList<BuildData>();
        NodeList buildNodes = docBuild.getElementsByTagName("build"); // NOI18N // HUDSON-3267: might be root elt
        for (int i = 0; i < buildNodes.getLength(); i++) {
            Node build = buildNodes.item(i);
            int number = 0;
            boolean building = false;
            Result result = null;
            NodeList details = build.getChildNodes();
            for (int j = 0; j < details.getLength(); j++) {
                Node detail = details.item(j);
                if (detail.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String nodeName = detail.getNodeName();
                Node firstChild = detail.getFirstChild();
                if (firstChild == null) {
                    LOG.log(Level.WARNING, "#170267: unexpected empty <build> child: {0}", nodeName);
                    continue;
                }
                String text = firstChild.getTextContent();
                if (nodeName.equals("number")) { // NOI18N
                    number = Integer.parseInt(text);
                } else if (nodeName.equals("building")) { // NOI18N
                    building = Boolean.valueOf(text);
                } else if (nodeName.equals("result")) { // NOI18N
                    result = Result.valueOf(text);
                } else {
                    LOG.log(Level.WARNING, "unexpected <build> child: {0}", nodeName);
                }
            }
            builds.add(new BuildData(number, result, building));
        }
        return builds;
    }

    @Override
    public void getJobBuildResult(HudsonJobBuild build, AtomicBoolean building, AtomicReference<Result> result) {
        Document doc = getDocument(build.getUrl() + XML_API_URL +
                "?xpath=/*/*[name()='result'%20or%20name()='building']&wrapper=root", true);
        if (doc == null) {
            return;
        }
        Element docEl = doc.getDocumentElement();
        Element resultEl = XMLUtil.findElement(docEl, "result", null);
        if (resultEl != null) {
            result.set(Result.valueOf(XMLUtil.findText(resultEl)));
        }
        Element buildingEl = XMLUtil.findElement(docEl, "building", null);
        if (buildingEl != null) {
            building.set(Boolean.parseBoolean(XMLUtil.findText(buildingEl)));
        }
    }
    
    public synchronized @CheckForNull
    @Override
    HudsonVersion getHudsonVersion(boolean authentication) {
        if (version == null) {
            version = retrieveHudsonVersion(authentication);
        }
        return version;
    }
    
    @Override
    public boolean isConnected() {
        return connected;
        
    }
    
    private Collection<ViewData> getViewData(Document doc, String baseUrl) {
        String primaryViewName = null;
        Element primaryViewEl = XMLUtil.findElement(doc.getDocumentElement(), "primaryView", null); // NOI18N
        if (primaryViewEl != null) {
            Element nameEl = XMLUtil.findElement(primaryViewEl, "name", null); // NOI18N
            if (nameEl != null) {
                primaryViewName = XMLUtil.findText(nameEl);
            }
        }
        
        ArrayList<ViewData> views = new ArrayList<ViewData>();

        NodeList nodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (!n.getNodeName().equals(XML_API_VIEW_ELEMENT)) {
                continue;
            }
            
            String name = null;
            String url = null;
            boolean isPrimary = false;
            
            for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                Node o = n.getChildNodes().item(j);
                if (o.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                if (o.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                    name = o.getFirstChild().getTextContent();
                    isPrimary = name.equals(primaryViewName);
                } else if (o.getNodeName().equals(XML_API_URL_ELEMENT)) {
                    url = normalizeUrl(baseUrl, o.getFirstChild().getTextContent(), isPrimary ? "" : "view/[^/]+/"); // NOI18N
                }
            }
            
            if (null != name && null != url) {
                Element docView = (Element) n;
                
                ViewData viewData = new ViewData(name, url, isPrimary);
                
                NodeList jobsList = docView.getElementsByTagName(XML_API_JOB_ELEMENT);
                for (int k = 0; k < jobsList.getLength(); k++) {
                    Node d = jobsList.item(k);
                    for (int l = 0; l < d.getChildNodes().getLength(); l++) {
                        Node e = d.getChildNodes().item(l);
                        if (e.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        String nodeName = e.getNodeName();
                        if (nodeName.equals(XML_API_NAME_ELEMENT)) {
                            cache.put(viewData.getName() + "/" + e.getFirstChild().getTextContent(), viewData); // NOI18N
                        } else {
                            LOG.log(Level.FINE, "unexpected view <job> child: {0}", nodeName);
                        }
                    }
                }
                views.add(viewData);
            }
            
        }
        
        return views;
    }
    
    private Collection<JobData> getJobsData(Document doc, String baseUrl,
            Collection<ViewData> viewsData, Collection<FolderData> foldersData,
            HudsonFolder parentFolder) {
        Map<String, JobData> jobs = new HashMap<String, JobData>();
        
        NodeList nodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            boolean secured = n.getNodeName().equals(XML_API_SECURED_JOB_ELEMENT);
            if (!n.getNodeName().equals(XML_API_JOB_ELEMENT) && !secured) {
                continue;
            }
            
            JobData jd = new JobData();
            jd.setSecured(secured);
            FolderData fd = new FolderData();
            boolean isFolder = false;
            
            NodeList jobDetails = n.getChildNodes();
            for (int k = 0; k < jobDetails.getLength(); k++) {
                Node d = jobDetails.item(k);
                if (d.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String nodeName = d.getNodeName();
                if (nodeName.equals(XML_API_NAME_ELEMENT)) {
                    String folder = parentFolder == null
                            ? "" : parentFolder.getName() + "/";        //NOI18N
                    String name = folder + d.getFirstChild().getTextContent();
                    jd.setJobName(name);
                    fd.setName(name);
                } else if (nodeName.equals(XML_API_URL_ELEMENT)) {
                    String u = normalizeUrl(baseUrl, d.getFirstChild().getTextContent(), "job/[^/]+/"); // NOI18N
                    jd.setJobUrl(u);
                    fd.setUrl(u);
                } else if (nodeName.equals("primaryView")) { // NOI18N
                    isFolder = true;
                } else if (nodeName.equals(XML_API_COLOR_ELEMENT)) {
                    jd.setColor(Color.find(d.getFirstChild().getTextContent().trim()));
                } else if (nodeName.equals(XML_API_DISPLAY_NAME_ELEMENT)) {
                    jd.setDisplayName(d.getFirstChild().getTextContent());
                } else if (nodeName.equals(XML_API_BUILDABLE_ELEMENT)) {
                    jd.setBuildable(Boolean.valueOf(d.getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_INQUEUE_ELEMENT)) {
                    jd.setInQueue(Boolean.valueOf(d.getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_LAST_BUILD_ELEMENT)) {
                    jd.setLastBuild(Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_LAST_FAILED_BUILD_ELEMENT)) {
                    jd.setLastFailedBuild(Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_LAST_STABLE_BUILD_ELEMENT)) {
                    jd.setLastStableBuild(Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_LAST_SUCCESSFUL_BUILD_ELEMENT)) {
                    jd.setLastSuccessfulBuild(Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_LAST_COMPLETED_BUILD_ELEMENT)) {
                    jd.setLastCompletedBuild(Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                } else if (nodeName.equals("module")) { // NOI18N
                    String name = null, displayName = null, url = null;
                    Color color = null;
                    NodeList moduleDetails = d.getChildNodes();
                    for (int j = 0; j < moduleDetails.getLength(); j++) {
                        Node n2 = moduleDetails.item(j);
                        if (n2.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        String nodeName2 = n2.getNodeName();
                        Node firstChild = n2.getFirstChild();
                        if (firstChild != null) {
                            String text = firstChild.getTextContent();
                            if (nodeName2.equals("name")) { // NOI18N
                                name = text;
                            } else if (nodeName2.equals("displayName")) { // NOI18N
                                displayName = text;
                            } else if (nodeName2.equals("url")) { // NOI18N
                                url = normalizeUrl(baseUrl, text, "job/[^/]+/[^/]+/"); // NOI18N
                            } else if (nodeName2.equals("color")) { // NOI18N
                                color = Color.find(text);
                            } else {
                                LOG.log(Level.FINE, "unexpected <module> child: {0}", nodeName);
                            }
                        } else {
                            LOG.log(Level.FINE, "#178360: unexpected empty <module> child: {0}", nodeName);
                        }
                    }
                    if (name != null && url != null && color != null) {
                        if (displayName == null) {
                            LOG.log(Level.FINE, "#202671: missing displayName in {0}", jd.getJobUrl());
                            displayName = name;
                        }
                        jd.addModule(name, displayName, color, url);
                    } else {
                        LOG.log(Level.FINE, "#202671: missing name/url/color in {0}", jd.getJobUrl());
                    }
                } else {
                    LOG.log(Level.FINE, "unexpected global <job> child: {0}", nodeName);
                }
            }
            for (ViewData v : viewsData) {
                if (/* https://github.com/hudson/hudson/commit/105f2b09cf1376f9fe4dbf80c5bdb7a0d30ba1c1#commitcomment-447142 */secured ||
                        null != cache.get(v.getName() + "/" + jd.getJobName())) {
                    jd.addView(v.getName());
                }
            }
            if (isFolder) {
                foldersData.add(fd);
            } else {
                addJobToMap(jobs, jd);
            }
        }
        return jobs.values();
    }

    /**
     * Add a JobData instance to a map and handle duplicates.
     *
     * Some servers (e.g. ODCS) may return duplicate jobs, so we add a job to
     * the map only if no better job with the same name is already included. If
     * the new job is better than the already included job, it will replace the
     * included job. For our purposes, a standard job is better than a secured
     * job.
     *
     * @param jobs Map with jobs, where keys are job names, and values are
     * JobData instances.
     * @param jd JobData instance to add.
     */
    private void addJobToMap(Map<String, JobData> jobs, JobData jd) {
        if (jd != null && jd.getJobName() != null
                && !jd.getJobName().isEmpty()) {
            JobData existingJob = jobs.get(jd.getJobName());
            if (existingJob == null
                    || (existingJob.isSecured() && !jd.isSecured())) {
                jobs.put(jd.getJobName(), jd);
            }
        }
    }

    /**
     * Try to fix up a URL as returned by Hudson's XML API.
     * @param suggested whatever {@code .../api/xml#//url} offered, e.g. {@code http://localhost:9999/job/My%20Job/}
     * @param relativePattern regex for the expected portion of the URL relative to server root, e.g. {@code job/[^/]+/}
     * @return analogous URL constructed from instance root, e.g. {@code https://my.facade/hudson/job/My%20Job/}
     * @see "#165735"
     */
    private static String normalizeUrl(String instanceUrl, String suggested, String relativePattern) {
        Pattern tailPattern;
        synchronized (tailPatterns) {
            tailPattern = tailPatterns.get(relativePattern);
            if (tailPattern == null) {
                tailPatterns.put(relativePattern, tailPattern = Pattern.compile(".+/(" + relativePattern + ")"));
            }
        }
        Matcher m = tailPattern.matcher(suggested);
        if (m.matches()) {
            String result = instanceUrl + m.group(1);
            if (!result.equals(suggested)) {
                LOG.log(Level.FINER, "Normalizing {0} -> {1}", new Object[] {suggested, result});
            }
            return result;
        } else {
            LOG.log(Level.WARNING, "Anomalous URL {0} not ending with {1} from {2}", new Object[] {suggested, relativePattern, instanceUrl});
            return suggested;
        }
    }
    private static final Map<String,Pattern> tailPatterns = new HashMap<String,Pattern>();
    
    private synchronized @CheckForNull HudsonVersion retrieveHudsonVersion(boolean authentication) {
        HudsonVersion v = null;
        
        try {
            String sVersion = new ConnectionBuilder().homeURL(instanceUrl).
                    url(instanceUrl).authentication(authentication).
                    httpConnection().getHeaderField("X-Hudson"); // NOI18N
            if (sVersion != null) {
                v = new HudsonVersion(sVersion);
            }
        } catch (MalformedURLException mue) {
            LOG.log(Level.INFO, "Malformed URL " + instanceUrl, mue);   //NOI18N
        } catch (IOException e) {
            // Nothing
        }
        
        return v;
    }

    Document getDocument(String url, boolean authentication) {
        forbidden = false;
        Document doc = null;
        
        try {
            HttpURLConnection conn = new ConnectionBuilder().
                    homeURL(instanceUrl).url(url).
                    authentication(authentication).httpConnection();
            // Connected successfully
            if (!isConnected()) {
                connected = true;
                version = retrieveHudsonVersion(authentication);
            }
            
            // Get input stream
            InputStream stream = conn.getInputStream();
            
            // Parse document
            InputSource source = new InputSource(stream);
            source.setSystemId(url);
            doc = XMLUtil.parse(source, false, false, XMLUtil.defaultErrorHandler(), null);
            
            // Check for right version
            if (!Utilities.isSupportedVersion(getHudsonVersion(authentication))) {
                HudsonVersion v = retrieveHudsonVersion(authentication);
                
                if (!Utilities.isSupportedVersion(v)) {
                    return null;
                }
                
                version = v;
            }
            
            conn.disconnect();
        } catch (SAXParseException x) {
            // already reported
            LOG.log(Level.INFO, null, x);
        } catch (MalformedURLException mue) {
            LOG.log(Level.INFO, "Invalid URL " + instanceUrl, mue);     //NOI18N
        } catch (Exception x) {
            LOG.log(Level.FINE, url, x);
            if (!authentication && x instanceof HttpRetryException && ((HttpRetryException) x).responseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                forbidden = true;
            }
        }
        
        return doc;
    }

    @Override
    public RemoteFileSystem getArtifacts(HudsonJobBuild build) {
        try {
            return new HudsonRemoteFileSystem(build);
        } catch (MalformedURLException ex) {
            LOG.log(Level.INFO, null, ex);
            return null;
        }
    }

    @Override
    public RemoteFileSystem getArtifacts(HudsonMavenModuleBuild build) {
        try {
            return new HudsonRemoteFileSystem(build);
        } catch (MalformedURLException ex) {
            LOG.log(Level.INFO, null, ex);
            return null;
        }
    }

    @Override
    public RemoteFileSystem getWorkspace(HudsonJob job) {
        try {
            return new HudsonRemoteFileSystem(job);
        } catch (MalformedURLException ex) {
           LOG.log(Level.INFO, null, ex);
            return null;
        }
    }

    @Override
    public boolean isForbidden() {
        return forbidden;
    }

    @Override
    public ConsoleDataProvider getConsoleDataProvider() {
        return HUDSON_CONSOLE_DISPLAYER;
    }

    @Override
    public FailureDataProvider getFailureDataProvider() {
        return HUDSON_FAILURE_DISPLAYER;
    }

    @Override
    public Collection<? extends HudsonJobChangeItem> getJobBuildChanges(HudsonJobBuild build) {
        Collection<? extends HudsonJobChangeItem> changes = null;
        for (HudsonSCM scm : Lookup.getDefault().lookupAll(HudsonSCM.class)) {
            changes = scm.parseChangeSet(build);
            if (changes != null) {
                break;
            }
        }
        if (changes == null) {
            changes = parseChangeSetGeneric(build);
        }
        return changes;
    }

    private Collection<? extends HudsonJobChangeItem> parseChangeSetGeneric(HudsonJobBuild build) {
        final Element changeSet;
        try {
            changeSet = XMLUtil.findElement(new ConnectionBuilder().job(build.getJob()).url(build.getUrl() + "api/xml?tree=changeSet[items[author[fullName],msg,affectedFile[path,editType]]]").parseXML().getDocumentElement(), "changeSet", null);
        } catch (IOException x) {
            LOG.log(Level.WARNING, "could not parse changelog for {0}: {1}", new Object[]{this, x});
            return Collections.emptyList();
        }
        class Item implements HudsonJobChangeItem {

            final Element itemXML;

            Item(Element itemXML) {
                this.itemXML = itemXML;
            }

            @Override
            public String getUser() {
                return Utilities.xpath("author/fullName", itemXML);
            }

            @Override
            public String getMessage() {
                return Utilities.xpath("msg", itemXML);
            }

            @Override
            public Collection<? extends HudsonJobChangeItem.HudsonJobChangeFile> getFiles() {
                class AffectedFile implements HudsonJobChangeItem.HudsonJobChangeFile {

                    final Element fileXML;

                    AffectedFile(Element fileXML) {
                        this.fileXML = fileXML;
                    }

                    @Override
                    public String getName() {
                        return Utilities.xpath("path", fileXML);
                    }

                    @Override
                    public HudsonJobChangeItem.HudsonJobChangeFile.EditType getEditType() {
                        return HudsonJobChangeItem.HudsonJobChangeFile.EditType.valueOf(Utilities.xpath("editType", fileXML));
                    }

                    @Override
                    public OutputListener hyperlink() {
                        return null;
                    }
                }
                List<AffectedFile> files = new ArrayList<AffectedFile>();
                // XXX this is not typically @Exported, in which case no file changes will be shown
                NodeList nl = itemXML.getElementsByTagName("affectedFile");
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new AffectedFile((Element) nl.item(i)));
                }
                return files;
            }
        }
        List<Item> items = new ArrayList<Item>();
        NodeList nl = changeSet.getElementsByTagName("item");
        for (int i = 0; i < nl.getLength(); i++) {
            items.add(new Item(((Element) nl.item(i))));
        }
        return items;
    }

}
