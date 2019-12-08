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
package org.netbeans.modules.maven.grammar.effpom;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.model.ActivationOS;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.Developer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Extension;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Notifier;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Prerequisites;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Scm;
import org.apache.maven.model.Site;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;
import org.apache.maven.model.InputLocationTracker;

@SuppressWarnings( "all")
public class LocationAwareMavenXpp3Writer {

    private static final String NAMESPACE = null;
    private List<Location> locations;


    public List<Location> write(StringWriter writer, Model model)
            throws java.io.IOException {
        locations = new ArrayList<Location>();
        XmlSerializer serializer = new MXSerializer();
        serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
        serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n");
        serializer.setOutput(writer);
        serializer.startDocument(model.getModelEncoding(), null);
        writeModel(model, "project", serializer);
        serializer.endDocument();
        return locations;
    } //-- void write( Writer, Model )

    private StringBuffer b(XmlSerializer serializer) {
        return sw(serializer).getBuffer();
    }
    private StringWriter sw(XmlSerializer serializer) {
        MXSerializer ser = (MXSerializer) serializer;
        return  (StringWriter) ser.getWriter();
    }

    private void logLocation(InputLocationTracker tracker, Object value, int startOffset, int endOffset) {
        if (tracker != null) {
            InputLocation loc = tracker.getLocation(value);
            if (loc != null) {
                locations.add(new Location(loc, startOffset, endOffset));
            }
        }
    }

    private void flush(XmlSerializer serializer) {
        sw(serializer).flush();
    }

    private void writeValue(XmlSerializer serializer, String tag, String value, InputLocationTracker parent) throws IOException {
        writeValue(serializer, tag, value, parent, tag);
    }

    private void writeValue(XmlSerializer serializer, String tag, String value, InputLocationTracker parent, Object trackerId) throws IOException {
        StringBuffer b = b(serializer);
        serializer.startTag(NAMESPACE, tag);
        flush(serializer);
        int start = b.length() - tag.length() - 2;
        serializer.text(value).endTag(NAMESPACE, tag);
        flush(serializer);
        //TODO sometimes like when dependency scope is compile, which is the default value, there is no location for it, but it still gets printed.
        logLocation(parent, trackerId, start, b.length());
    }
    
    private void writeXpp3DOM(XmlSerializer serializer, Xpp3Dom root, InputLocationTracker rootTracker) throws IOException {
        StringBuffer b = b(serializer);
        serializer.startTag(NAMESPACE, root.getName());
        //need to flush the inner writer, flush on serializer closes tag
        flush(serializer);
        int start = b.length() - root.getName().length() - 2;
        
        String[] attributeNames = root.getAttributeNames();
        for ( int i = 0; i < attributeNames.length; i++ )
        {
            String attributeName = attributeNames[i];
            serializer.attribute(NAMESPACE, attributeName, root.getAttribute( attributeName ));
        }
        
        boolean config = rootTracker != null ? rootTracker.getLocation(root.getName()) != null : true;
        
        Xpp3Dom[] children = root.getChildren();
        for ( int i = 0; i < children.length; i++ )
        {
            writeXpp3DOM(serializer, children[i], rootTracker != null ? rootTracker.getLocation(config ? root.getName() : root) : null);
        }

        String value = root.getValue();
        if ( value != null )
        {
            serializer.text( value );
        }

        serializer.endTag(NAMESPACE, root.getName()).flush();
        logLocation(rootTracker, config ? root.getName() : root, start, b.length());
        
    }

    public static class Location {

        public final InputLocation loc;
        public final int startOffset;
        public final int endOffset;
        
        public Location(InputLocation loc, int startOffset, int endOffset) {
            this.loc = loc;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
    }


    private void writeActivation(Activation activation, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (activation.isActiveByDefault() != false) {
            writeValue(serializer, "activeByDefault", String.valueOf(activation.isActiveByDefault()), activation);
        }
        if (activation.getJdk() != null) {
            writeValue(serializer, "jdk", activation.getJdk(), activation);
        }
        if (activation.getOs() != null) {
            writeActivationOS(activation.getOs(), "os", serializer);
        }
        if (activation.getProperty() != null) {
            writeActivationProperty(activation.getProperty(), "property", serializer);
        }
        if (activation.getFile() != null) {
            writeActivationFile(activation.getFile(), "file", serializer);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(activation, "", start, b.length());
    } 
    
    private void writeActivationFile(ActivationFile activationFile, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (activationFile.getMissing() != null) {
            writeValue(serializer, "missing", activationFile.getMissing(), activationFile);
        }
        if (activationFile.getExists() != null) {
            writeValue(serializer, "exists", activationFile.getExists(), activationFile);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(activationFile, "", start, b.length());
    } 
    
    private void writeActivationOS(ActivationOS activationOS, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (activationOS.getName() != null) {
            writeValue(serializer, "name", activationOS.getName(), activationOS);
        }
        if (activationOS.getFamily() != null) {
            writeValue(serializer, "family", activationOS.getFamily(), activationOS);
        }
        if (activationOS.getArch() != null) {
            writeValue(serializer, "arch", activationOS.getArch(), activationOS);
        }
        if (activationOS.getVersion() != null) {
            writeValue(serializer, "version", activationOS.getVersion(), activationOS);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(activationOS, "", start, b.length());
    } 
    
    private void writeActivationProperty(ActivationProperty activationProperty, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (activationProperty.getName() != null) {
            writeValue(serializer, "name", activationProperty.getName(), activationProperty);
        }
        if (activationProperty.getValue() != null) {
            writeValue(serializer, "value", activationProperty.getValue(), activationProperty);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(activationProperty, "", start, b.length());
    } 
    
    private void writeBuild(Build build, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (build.getSourceDirectory() != null) {
            writeValue(serializer, "sourceDirectory", build.getSourceDirectory(), build);
        }
        if (build.getScriptSourceDirectory() != null) {
            writeValue(serializer, "scriptSourceDirectory", build.getScriptSourceDirectory(), build);
        }
        if (build.getTestSourceDirectory() != null) {
            writeValue(serializer, "testSourceDirectory", build.getTestSourceDirectory(), build);
        }
        if (build.getOutputDirectory() != null) {
            writeValue(serializer, "outputDirectory", build.getOutputDirectory(), build);
        }
        if (build.getTestOutputDirectory() != null) {
            writeValue(serializer, "testOutputDirectory", build.getTestOutputDirectory(), build);
        }
        if ((build.getExtensions() != null) && (build.getExtensions().size() > 0)) {
            serializer.startTag(NAMESPACE, "extensions");
            for (Iterator iter = build.getExtensions().iterator(); iter.hasNext();) {
                Extension o = (Extension) iter.next();
                writeExtension(o, "extension", serializer);
            }
            serializer.endTag(NAMESPACE, "extensions");
        }
        if (build.getDefaultGoal() != null) {
            writeValue(serializer, "defaultGoal", build.getDefaultGoal(), build);
        }
        if ((build.getResources() != null) && (build.getResources().size() > 0)) {
            serializer.startTag(NAMESPACE, "resources");
            for (Iterator iter = build.getResources().iterator(); iter.hasNext();) {
                Resource o = (Resource) iter.next();
                writeResource(o, "resource", serializer);
            }
            serializer.endTag(NAMESPACE, "resources");
        }
        if ((build.getTestResources() != null) && (build.getTestResources().size() > 0)) {
            serializer.startTag(NAMESPACE, "testResources");
            for (Iterator iter = build.getTestResources().iterator(); iter.hasNext();) {
                Resource o = (Resource) iter.next();
                writeResource(o, "testResource", serializer);
            }
            serializer.endTag(NAMESPACE, "testResources");
        }
        if (build.getDirectory() != null) {
            writeValue(serializer, "directory", build.getDirectory(), build);
        }
        if (build.getFinalName() != null) {
            writeValue(serializer, "finalName", build.getFinalName(), build);
        }
        if ((build.getFilters() != null) && (build.getFilters().size() > 0)) {
            serializer.startTag(NAMESPACE, "filters");
            flush(serializer);
            int start2 = b.length();
            InputLocationTracker filtersTracker = build.getLocation("filters");
            int index = 0;
            for (Iterator iter = build.getFilters().iterator(); iter.hasNext();) {
                String filter = (String) iter.next();
                writeValue(serializer, "filter", filter, filtersTracker, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "filters").flush();
            logLocation(build, "filters", start2, b.length());
        }
        if (build.getPluginManagement() != null) {
            writePluginManagement(build.getPluginManagement(), "pluginManagement", serializer);
        }
        if ((build.getPlugins() != null) && (build.getPlugins().size() > 0)) {
            serializer.startTag(NAMESPACE, "plugins");
            for (Iterator iter = build.getPlugins().iterator(); iter.hasNext();) {
                Plugin o = (Plugin) iter.next();
                writePlugin(o, "plugin", serializer);
            }
            serializer.endTag(NAMESPACE, "plugins");
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(build, "", start, b.length());
    } 
    
    private void writeBuildBase(BuildBase buildBase, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (buildBase.getDefaultGoal() != null) {
            writeValue(serializer, "defaultGoal", buildBase.getDefaultGoal(), buildBase);
        }
        if ((buildBase.getResources() != null) && (buildBase.getResources().size() > 0)) {
            serializer.startTag(NAMESPACE, "resources");
            for (Iterator iter = buildBase.getResources().iterator(); iter.hasNext();) {
                Resource o = (Resource) iter.next();
                writeResource(o, "resource", serializer);
            }
            serializer.endTag(NAMESPACE, "resources");
        }
        if ((buildBase.getTestResources() != null) && (buildBase.getTestResources().size() > 0)) {
            serializer.startTag(NAMESPACE, "testResources");
            for (Iterator iter = buildBase.getTestResources().iterator(); iter.hasNext();) {
                Resource o = (Resource) iter.next();
                writeResource(o, "testResource", serializer);
            }
            serializer.endTag(NAMESPACE, "testResources");
        }
        if (buildBase.getDirectory() != null) {
            writeValue(serializer, "directory", buildBase.getDirectory(), buildBase);
        }
        if (buildBase.getFinalName() != null) {
            writeValue(serializer, "finalName", buildBase.getFinalName(), buildBase);
        }
        if ((buildBase.getFilters() != null) && (buildBase.getFilters().size() > 0)) {
            serializer.startTag(NAMESPACE, "filters");
            flush(serializer);
            int start2 = b.length();
            InputLocationTracker filtersTracker = buildBase.getLocation("filters");
            int index = 0;
            for (Iterator iter = buildBase.getFilters().iterator(); iter.hasNext();) {
                String filter = (String) iter.next();
                writeValue(serializer, "filter", filter, filtersTracker, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "filters").flush();
            logLocation(buildBase, "filters", start2, b.length());
        }
        if (buildBase.getPluginManagement() != null) {
            writePluginManagement(buildBase.getPluginManagement(), "pluginManagement", serializer);
        }
        if ((buildBase.getPlugins() != null) && (buildBase.getPlugins().size() > 0)) {
            serializer.startTag(NAMESPACE, "plugins");
            for (Iterator iter = buildBase.getPlugins().iterator(); iter.hasNext();) {
                Plugin o = (Plugin) iter.next();
                writePlugin(o, "plugin", serializer);
            }
            serializer.endTag(NAMESPACE, "plugins");
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(buildBase, "", start, b.length());
    } 
    
    private void writeCiManagement(CiManagement ciManagement, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (ciManagement.getSystem() != null) {
            writeValue(serializer, "system", ciManagement.getSystem(), ciManagement);
        }
        if (ciManagement.getUrl() != null) {
            writeValue(serializer, "url", ciManagement.getUrl(), ciManagement);
        }
        if ((ciManagement.getNotifiers() != null) && (ciManagement.getNotifiers().size() > 0)) {
            serializer.startTag(NAMESPACE, "notifiers");
            for (Iterator iter = ciManagement.getNotifiers().iterator(); iter.hasNext();) {
                Notifier o = (Notifier) iter.next();
                writeNotifier(o, "notifier", serializer);
            }
            serializer.endTag(NAMESPACE, "notifiers");
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(ciManagement, "", start, b.length());
    } 
    
    private void writeContributor(Contributor contributor, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (contributor.getName() != null) {
            writeValue(serializer, "name", contributor.getName(), contributor);
        }
        if (contributor.getEmail() != null) {
            writeValue(serializer, "email", contributor.getEmail(), contributor);
        }
        if (contributor.getUrl() != null) {
            writeValue(serializer, "url", contributor.getUrl(), contributor);
        }
        if (contributor.getOrganization() != null) {
            writeValue(serializer, "organization", contributor.getOrganization(), contributor);
        }
        if (contributor.getOrganizationUrl() != null) {
            writeValue(serializer, "organizationUrl", contributor.getOrganizationUrl(), contributor);
        }
        if ((contributor.getRoles() != null) && (contributor.getRoles().size() > 0)) {
            serializer.startTag(NAMESPACE, "roles");
            flush(serializer);
            int start2 = b.length();
            InputLocationTracker rolesTracker = contributor.getLocation("roles");
            int index = 0;
            for (Iterator iter = contributor.getRoles().iterator(); iter.hasNext();) {
                String role = (String) iter.next();
                writeValue(serializer, "role", role, rolesTracker, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "roles").flush();
            logLocation(contributor, "roles", start2, b.length());
        }
        if (contributor.getTimezone() != null) {
            writeValue(serializer, "timezone", contributor.getTimezone(), contributor);
            serializer.startTag(NAMESPACE, "timezone").text(contributor.getTimezone()).endTag(NAMESPACE, "timezone");
        }
        if ((contributor.getProperties() != null) && (contributor.getProperties().size() > 0)) {
            serializer.startTag(NAMESPACE, "properties");
            flush(serializer);
            int start2 = b.length();
            InputLocationTracker propTracker = contributor.getLocation("properties");
            for (Iterator iter = contributor.getProperties().keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String value = (String) contributor.getProperties().get(key);
                writeValue(serializer, key, value, propTracker);
            }
            serializer.endTag(NAMESPACE, "properties").flush();
            logLocation(contributor, "properties", start2, b.length());
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(contributor, "", start, b.length());
    } 
    
    private void writeDependency(Dependency dependency, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (dependency.getGroupId() != null) {
            writeValue(serializer, "groupId", dependency.getGroupId(), dependency);
        }
        if (dependency.getArtifactId() != null) {
            writeValue(serializer, "artifactId", dependency.getArtifactId(), dependency);
        }
        if (dependency.getVersion() != null) {
            writeValue(serializer, "version", dependency.getVersion(), dependency);
        }
        if ((dependency.getType() != null) && !dependency.getType().equals("jar")) {
            writeValue(serializer, "type", dependency.getType(), dependency);
        }
        if (dependency.getClassifier() != null) {
            writeValue(serializer, "classifier", dependency.getClassifier(), dependency);
        }
        if (dependency.getScope() != null) {
            writeValue(serializer, "scope", dependency.getScope(), dependency);
        }
        if (dependency.getSystemPath() != null) {
            writeValue(serializer, "systemPath", dependency.getSystemPath(), dependency);
        }
        if ((dependency.getExclusions() != null) && (dependency.getExclusions().size() > 0)) {
            serializer.startTag(NAMESPACE, "exclusions");
            for (Iterator iter = dependency.getExclusions().iterator(); iter.hasNext();) {
                Exclusion o = (Exclusion) iter.next();
                writeExclusion(o, "exclusion", serializer);
            }
            serializer.endTag(NAMESPACE, "exclusions");
        }
        if (dependency.getOptional() != null) {
            writeValue(serializer, "optional", dependency.getOptional(), dependency);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(dependency, "", start, b.length());
    } 
    
    private void writeDependencyManagement(DependencyManagement dependencyManagement, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        if ((dependencyManagement.getDependencies() != null) && (dependencyManagement.getDependencies().size() > 0)) {
            serializer.startTag(NAMESPACE, "dependencies");
            for (Iterator iter = dependencyManagement.getDependencies().iterator(); iter.hasNext();) {
                Dependency o = (Dependency) iter.next();
                writeDependency(o, "dependency", serializer);
            }
            serializer.endTag(NAMESPACE, "dependencies");
        }
        serializer.endTag(NAMESPACE, tagName);
    } 
    
    private void writeDeploymentRepository(DeploymentRepository deploymentRepository, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (deploymentRepository.isUniqueVersion() != true) {
            writeValue(serializer, "uniqueVersion", String.valueOf(deploymentRepository.isUniqueVersion()), deploymentRepository);
        }
        if (deploymentRepository.getReleases() != null) {
            writeRepositoryPolicy((RepositoryPolicy) deploymentRepository.getReleases(), "releases", serializer);
        }
        if (deploymentRepository.getSnapshots() != null) {
            writeRepositoryPolicy((RepositoryPolicy) deploymentRepository.getSnapshots(), "snapshots", serializer);
        }
        if (deploymentRepository.getId() != null) {
            writeValue(serializer, "id", deploymentRepository.getId(), deploymentRepository);
        }
        if (deploymentRepository.getName() != null) {
            writeValue(serializer, "name", deploymentRepository.getName(), deploymentRepository);
        }
        if (deploymentRepository.getUrl() != null) {
            writeValue(serializer, "url", deploymentRepository.getUrl(), deploymentRepository);
        }
        if ((deploymentRepository.getLayout() != null) && !deploymentRepository.getLayout().equals("default")) {
            writeValue(serializer, "layout", deploymentRepository.getLayout(), deploymentRepository);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(deploymentRepository, "", start, b.length());
    } 
    
    private void writeDeveloper(Developer developer, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (developer.getId() != null) {
            writeValue(serializer, "id", developer.getId(), developer);
        }
        if (developer.getName() != null) {
            writeValue(serializer, "name", developer.getName(), developer);
        }
        if (developer.getEmail() != null) {
            writeValue(serializer, "email", developer.getEmail(), developer);
        }
        if (developer.getUrl() != null) {
            writeValue(serializer, "url", developer.getUrl(), developer);
        }
        if (developer.getOrganization() != null) {
            writeValue(serializer, "organization", developer.getOrganization(), developer);
        }
        if (developer.getOrganizationUrl() != null) {
            writeValue(serializer, "organizationUrl", developer.getOrganizationUrl(), developer);
        }
        if ((developer.getRoles() != null) && (developer.getRoles().size() > 0)) {
            serializer.startTag(NAMESPACE, "roles");
            flush(serializer);
            int start2 = b.length();
            InputLocationTracker rolesTracker = developer.getLocation("roles");
            int index = 0;
            for (Iterator iter = developer.getRoles().iterator(); iter.hasNext();) {
                String role = (String) iter.next();
                writeValue(serializer, "role", role, rolesTracker, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "roles").flush();
            logLocation(developer, "roles", start2, b.length());
        }
        if (developer.getTimezone() != null) {
            writeValue(serializer, "timezone", developer.getTimezone(), developer);
        }
        if ((developer.getProperties() != null) && (developer.getProperties().size() > 0)) {
            serializer.startTag(NAMESPACE, "properties");
            flush(serializer);
            int start2 = b.length();
            InputLocationTracker propTracker = developer.getLocation("properties");
            for (Iterator iter = developer.getProperties().keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String value = (String) developer.getProperties().get(key);
                writeValue(serializer, key, value, propTracker);
            }
            serializer.endTag(NAMESPACE, "properties").flush();
            logLocation(developer, "properties", start2, b.length());
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(developer, "", start, b.length());
    } 
    
    private void writeDistributionManagement(DistributionManagement distributionManagement, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();

        if (distributionManagement.getRepository() != null) {
            writeDeploymentRepository((DeploymentRepository) distributionManagement.getRepository(), "repository", serializer);
        }
        if (distributionManagement.getSnapshotRepository() != null) {
            writeDeploymentRepository((DeploymentRepository) distributionManagement.getSnapshotRepository(), "snapshotRepository", serializer);
        }
        if (distributionManagement.getSite() != null) {
            writeSite((Site) distributionManagement.getSite(), "site", serializer);
        }
        if (distributionManagement.getDownloadUrl() != null) {
            writeValue(serializer, "downloadUrl", distributionManagement.getDownloadUrl(), distributionManagement);
        }
        if (distributionManagement.getRelocation() != null) {
            writeRelocation((Relocation) distributionManagement.getRelocation(), "relocation", serializer);
        }
        if (distributionManagement.getStatus() != null) {
            writeValue(serializer, "status", distributionManagement.getStatus(), distributionManagement);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(distributionManagement, "", start, b.length());
    } 
    
    private void writeExclusion(Exclusion exclusion, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (exclusion.getGroupId() != null) {
            writeValue(serializer, "groupId", exclusion.getGroupId(), exclusion);
        }
        if (exclusion.getArtifactId() != null) {
            writeValue(serializer, "artifactId", exclusion.getArtifactId(), exclusion);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(exclusion, "", start, b.length());
    } 
    
    private void writeExtension(Extension extension, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (extension.getGroupId() != null) {
            writeValue(serializer, "groupId", extension.getGroupId(), extension);
        }
        if (extension.getArtifactId() != null) {
            writeValue(serializer, "artifactId", extension.getArtifactId(), extension);
        }
        if (extension.getVersion() != null) {
            writeValue(serializer, "version", extension.getVersion(), extension);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(extension, "", start, b.length());
    } 
    
    private void writeIssueManagement(IssueManagement issueManagement, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (issueManagement.getSystem() != null) {
            writeValue(serializer, "system", issueManagement.getSystem(), issueManagement);
        }
        if (issueManagement.getUrl() != null) {
            writeValue(serializer, "url", issueManagement.getUrl(), issueManagement);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(issueManagement, "", start, b.length());
    } 
    
    private void writeLicense(License license, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (license.getName() != null) {
            writeValue(serializer, "name", license.getName(), license);
        }
        if (license.getUrl() != null) {
            writeValue(serializer, "url", license.getUrl(), license);
        }
        if (license.getDistribution() != null) {
            writeValue(serializer, "distribution", license.getDistribution(), license);
        }
        if (license.getComments() != null) {
            writeValue(serializer, "comments", license.getComments(), license);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(license, "", start, b.length());
    } 
    
    private void writeMailingList(MailingList mailingList, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();

        if (mailingList.getName() != null) {
            writeValue(serializer, "name", mailingList.getName(), mailingList);
        }
        if (mailingList.getSubscribe() != null) {
            writeValue(serializer, "subscribe", mailingList.getSubscribe(), mailingList);
        }
        if (mailingList.getUnsubscribe() != null) {
            writeValue(serializer, "unsubscribe", mailingList.getUnsubscribe(), mailingList);
        }
        if (mailingList.getPost() != null) {
            writeValue(serializer, "post", mailingList.getPost(), mailingList);
        }
        if (mailingList.getArchive() != null) {
            writeValue(serializer, "archive", mailingList.getArchive(), mailingList);
        }
        if ((mailingList.getOtherArchives() != null) && (mailingList.getOtherArchives().size() > 0)) {
            serializer.startTag(NAMESPACE, "otherArchives");
            flush(serializer);
            InputLocation otherLoc = mailingList.getLocation("otherArchives");
            int index = 0;
            for (Iterator iter = mailingList.getOtherArchives().iterator(); iter.hasNext();) {
                String otherArchive = (String) iter.next();
                writeValue(serializer, "otherArchive", otherArchive, otherLoc, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "otherArchives");
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(mailingList, "", start, b.length());

    } 
    
    private void writeModel(Model model, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.setPrefix("", "http://maven.apache.org/POM/4.0.0");
        serializer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        serializer.startTag(NAMESPACE, tagName);
        serializer.attribute("", "xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
        StringBuffer b = b(serializer);
        if (model.getModelVersion() != null) {
            writeValue(serializer, "modelVersion", model.getModelVersion(), model);
        }
        if (model.getParent() != null) {
            writeParent((Parent) model.getParent(), "parent", serializer);
        }
        if (model.getGroupId() != null) {
            writeValue(serializer, "groupId", model.getGroupId(), model);
        }
        if (model.getArtifactId() != null) {
            writeValue(serializer, "artifactId", model.getArtifactId(), model);
        }
        if (model.getVersion() != null) {
            writeValue(serializer, "version", model.getVersion(), model);
        }
        if ((model.getPackaging() != null) && !model.getPackaging().equals("jar")) {
            writeValue(serializer, "packaging", model.getPackaging(), model);
        }
        if (model.getName() != null) {
            writeValue(serializer, "name", model.getName(), model);
        }
        if (model.getDescription() != null) {
            writeValue(serializer, "description", model.getDescription(), model);
        }
        if (model.getUrl() != null) {
            writeValue(serializer, "url", model.getUrl(), model);
        }
        if (model.getInceptionYear() != null) {
            writeValue(serializer, "inceptionYear", model.getInceptionYear(), model);
        }
        if (model.getOrganization() != null) {
            writeOrganization(model.getOrganization(), "organization", serializer);
        }
        if ((model.getLicenses() != null) && (model.getLicenses().size() > 0)) {
            serializer.startTag(NAMESPACE, "licenses");
            for (Iterator iter = model.getLicenses().iterator(); iter.hasNext();) {
                License o = (License) iter.next();
                writeLicense(o, "license", serializer);
            }
            serializer.endTag(NAMESPACE, "licenses");
        }
        if ((model.getDevelopers() != null) && (model.getDevelopers().size() > 0)) {
            serializer.startTag(NAMESPACE, "developers");
            for (Iterator iter = model.getDevelopers().iterator(); iter.hasNext();) {
                Developer o = (Developer) iter.next();
                writeDeveloper(o, "developer", serializer);
            }
            serializer.endTag(NAMESPACE, "developers");
        }
        if ((model.getContributors() != null) && (model.getContributors().size() > 0)) {
            serializer.startTag(NAMESPACE, "contributors");
            for (Iterator iter = model.getContributors().iterator(); iter.hasNext();) {
                Contributor o = (Contributor) iter.next();
                writeContributor(o, "contributor", serializer);
            }
            serializer.endTag(NAMESPACE, "contributors");
        }
        if ((model.getMailingLists() != null) && (model.getMailingLists().size() > 0)) {
            serializer.startTag(NAMESPACE, "mailingLists");
            for (Iterator iter = model.getMailingLists().iterator(); iter.hasNext();) {
                MailingList o = (MailingList) iter.next();
                writeMailingList(o, "mailingList", serializer);
            }
            serializer.endTag(NAMESPACE, "mailingLists");
        }
        if (model.getPrerequisites() != null) {
            writePrerequisites((Prerequisites) model.getPrerequisites(), "prerequisites", serializer);
        }
        if ((model.getModules() != null) && (model.getModules().size() > 0)) {
            serializer.startTag(NAMESPACE, "modules");
            flush(serializer);
            int start2 = b.length();
            int index = 0;
            InputLocation tracker = model.getLocation("modules");
            for (Iterator iter = model.getModules().iterator(); iter.hasNext();) {
                String module = (String) iter.next();
                writeValue(serializer, "module", module, tracker, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "modules").flush();
            logLocation(model, "modules", start2, b.length());
        }
        if (model.getScm() != null) {
            writeScm((Scm) model.getScm(), "scm", serializer);
        }
        if (model.getIssueManagement() != null) {
            writeIssueManagement((IssueManagement) model.getIssueManagement(), "issueManagement", serializer);
        }
        if (model.getCiManagement() != null) {
            writeCiManagement((CiManagement) model.getCiManagement(), "ciManagement", serializer);
        }
        if (model.getDistributionManagement() != null) {
            writeDistributionManagement((DistributionManagement) model.getDistributionManagement(), "distributionManagement", serializer);
        }
        if ((model.getProperties() != null) && (model.getProperties().size() > 0)) {
            serializer.startTag(NAMESPACE, "properties");
            flush(serializer);
            int start2 = b.length();
            InputLocation tracker = model.getLocation("properties");
            for (Iterator iter = model.getProperties().keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String value = (String) model.getProperties().get(key);
                writeValue(serializer, key, value, tracker);
            }
            serializer.endTag(NAMESPACE, "properties").flush();
            logLocation(model, "properties", start2, b.length());
        }
        if (model.getDependencyManagement() != null) {
            writeDependencyManagement((DependencyManagement) model.getDependencyManagement(), "dependencyManagement", serializer);
        }
        if ((model.getDependencies() != null) && (model.getDependencies().size() > 0)) {
            serializer.startTag(NAMESPACE, "dependencies");
            for (Iterator iter = model.getDependencies().iterator(); iter.hasNext();) {
                Dependency o = (Dependency) iter.next();
                writeDependency(o, "dependency", serializer);
            }
            serializer.endTag(NAMESPACE, "dependencies");
        }
        if ((model.getRepositories() != null) && (model.getRepositories().size() > 0)) {
            serializer.startTag(NAMESPACE, "repositories");
            for (Iterator iter = model.getRepositories().iterator(); iter.hasNext();) {
                Repository o = (Repository) iter.next();
                writeRepository(o, "repository", serializer);
            }
            serializer.endTag(NAMESPACE, "repositories");
        }
        if ((model.getPluginRepositories() != null) && (model.getPluginRepositories().size() > 0)) {
            serializer.startTag(NAMESPACE, "pluginRepositories");
            for (Iterator iter = model.getPluginRepositories().iterator(); iter.hasNext();) {
                Repository o = (Repository) iter.next();
                writeRepository(o, "pluginRepository", serializer);
            }
            serializer.endTag(NAMESPACE, "pluginRepositories");
        }
        if (model.getBuild() != null) {
            writeBuild((Build) model.getBuild(), "build", serializer);
        }
        if (model.getReports() != null) {
             writeXpp3DOM(serializer, (Xpp3Dom)model.getReports(), model);
        }
        if (model.getReporting() != null) {
            writeReporting((Reporting) model.getReporting(), "reporting", serializer);
        }
        if ((model.getProfiles() != null) && (model.getProfiles().size() > 0)) {
            serializer.startTag(NAMESPACE, "profiles");
            for (Iterator iter = model.getProfiles().iterator(); iter.hasNext();) {
                Profile o = (Profile) iter.next();
                writeProfile(o, "profile", serializer);
            }
            serializer.endTag(NAMESPACE, "profiles");
        }
        serializer.endTag(NAMESPACE, tagName);
    } 
    
    private void writeNotifier(Notifier notifier, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if ((notifier.getType() != null) && !notifier.getType().equals("mail")) {
            writeValue(serializer, "type", notifier.getType(), notifier);
        }
        if (notifier.isSendOnError() != true) {
            writeValue(serializer, "sendOnError", String.valueOf(notifier.isSendOnError()), notifier);
        }
        if (notifier.isSendOnFailure() != true) {
            writeValue(serializer, "sendOnFailure", String.valueOf(notifier.isSendOnFailure()), notifier);
        }
        if (notifier.isSendOnSuccess() != true) {
            writeValue(serializer, "sendOnSuccess", String.valueOf(notifier.isSendOnSuccess()), notifier);
        }
        if (notifier.isSendOnWarning() != true) {
            writeValue(serializer, "sendOnWarning", String.valueOf(notifier.isSendOnWarning()), notifier);
        }
        if (notifier.getAddress() != null) {
            writeValue(serializer, "address", notifier.getAddress(), notifier);
        }
        if ((notifier.getConfiguration() != null) && (notifier.getConfiguration().size() > 0)) {
            serializer.startTag(NAMESPACE, "configuration");
            for (Iterator iter = notifier.getConfiguration().keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String value = (String) notifier.getConfiguration().get(key);
                serializer.startTag(NAMESPACE, "" + key + "").text(value).endTag(NAMESPACE, "" + key + "");
            }
            serializer.endTag(NAMESPACE, "configuration");
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(notifier, "", start, b.length());
    } 
    
    private void writeOrganization(Organization organization, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (organization.getName() != null) {
            writeValue(serializer, "name", organization.getName(), organization);
        }
        if (organization.getUrl() != null) {
            writeValue(serializer, "url", organization.getUrl(), organization);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(organization, "", start, b.length());
    } 
    
    private void writeParent(Parent parent, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (parent.getGroupId() != null) {
            writeValue(serializer, "groupId", parent.getGroupId(), parent);
        }
        if (parent.getArtifactId() != null) {
            writeValue(serializer, "artifactId", parent.getArtifactId(), parent);
        }
        if (parent.getVersion() != null) {
            writeValue(serializer, "version", parent.getVersion(), parent);
        }
        if ((parent.getRelativePath() != null) && !parent.getRelativePath().equals("../pom.xml")) {
            writeValue(serializer, "relativePath", parent.getRelativePath(), parent);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(parent, "", start, b.length());
    } 
    
    private void writePlugin(Plugin plugin, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if ((plugin.getGroupId() != null) && !plugin.getGroupId().equals("org.apache.maven.plugins")) {
            writeValue(serializer, "groupId", plugin.getGroupId(), plugin);
        }
        if (plugin.getArtifactId() != null) {
            writeValue(serializer, "artifactId", plugin.getArtifactId(), plugin);
        }
        if (plugin.getVersion() != null) {
            writeValue(serializer, "version", plugin.getVersion(), plugin);
        }
        if (plugin.getExtensions() != null) {
            writeValue(serializer, "extensions", plugin.getExtensions(), plugin);
        }
        if ((plugin.getExecutions() != null) && (plugin.getExecutions().size() > 0)) {
            serializer.startTag(NAMESPACE, "executions");
            for (Iterator iter = plugin.getExecutions().iterator(); iter.hasNext();) {
                PluginExecution o = (PluginExecution) iter.next();
                writePluginExecution(o, "execution", serializer);
            }
            serializer.endTag(NAMESPACE, "executions");
        }
        if ((plugin.getDependencies() != null) && (plugin.getDependencies().size() > 0)) {
            serializer.startTag(NAMESPACE, "dependencies");
            for (Iterator iter = plugin.getDependencies().iterator(); iter.hasNext();) {
                Dependency o = (Dependency) iter.next();
                writeDependency(o, "dependency", serializer);
            }
            serializer.endTag(NAMESPACE, "dependencies");
        }
        if (plugin.getGoals() != null) {
            writeXpp3DOM(serializer, (Xpp3Dom)plugin.getGoals(), plugin);
        }
        if (plugin.getInherited() != null) {
            writeValue(serializer, "inherited", plugin.getInherited(), plugin);
        }
        if (plugin.getConfiguration() != null) {
            writeXpp3DOM(serializer, (Xpp3Dom)plugin.getConfiguration(), plugin);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(plugin, "", start, b.length());
    } 
    
    private void writePluginExecution(PluginExecution pluginExecution, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if ((pluginExecution.getId() != null) && !pluginExecution.getId().equals("default")) {
            writeValue(serializer, "id", pluginExecution.getId(), pluginExecution);
        }
        if (pluginExecution.getPhase() != null) {
            writeValue(serializer, "phase", pluginExecution.getPhase(), pluginExecution);
        }
        if ((pluginExecution.getGoals() != null) && (pluginExecution.getGoals().size() > 0)) {
            serializer.startTag(NAMESPACE, "goals");
            flush(serializer);
            int start2 = b.length();
            int index = 0;
            InputLocation tracker = pluginExecution.getLocation("goals");

            for (Iterator iter = pluginExecution.getGoals().iterator(); iter.hasNext();) {
                String goal = (String) iter.next();
                writeValue(serializer, "goal", goal, tracker, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "goals").flush();
            logLocation(pluginExecution, "goals", start2, b.length());
        }
        if (pluginExecution.getInherited() != null) {
            writeValue(serializer, "inherited", pluginExecution.getInherited(), pluginExecution);
        }
        if (pluginExecution.getConfiguration() != null) {
            writeXpp3DOM(serializer, (Xpp3Dom)pluginExecution.getConfiguration(), pluginExecution);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(pluginExecution, "", start, b.length());
    } 
    
    private void writePluginManagement(PluginManagement pluginManagement, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if ((pluginManagement.getPlugins() != null) && (pluginManagement.getPlugins().size() > 0)) {
            serializer.startTag(NAMESPACE, "plugins");
            for (Iterator iter = pluginManagement.getPlugins().iterator(); iter.hasNext();) {
                Plugin o = (Plugin) iter.next();
                writePlugin(o, "plugin", serializer);
            }
            serializer.endTag(NAMESPACE, "plugins");
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(pluginManagement, "", start, b.length());
    }
    
    private void writePrerequisites(Prerequisites prerequisites, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if ((prerequisites.getMaven() != null) && !prerequisites.getMaven().equals("2.0")) {
            writeValue(serializer, "maven", prerequisites.getMaven(), prerequisites);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(prerequisites, "", start, b.length());
    }
    
    private void writeProfile(Profile profile, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if ((profile.getId() != null) && !profile.getId().equals("default")) {
            writeValue(serializer, "id", profile.getId(), profile);
        }
        if (profile.getActivation() != null) {
            writeActivation((Activation) profile.getActivation(), "activation", serializer);
        }
        if (profile.getBuild() != null) {
            writeBuildBase((BuildBase) profile.getBuild(), "build", serializer);
        }
        if ((profile.getModules() != null) && (profile.getModules().size() > 0)) {
            serializer.startTag(NAMESPACE, "modules");
            flush(serializer);
            int start2 = b.length();
            int index = 0;
            InputLocation tracker = profile.getLocation("modules");
            for (Iterator iter = profile.getModules().iterator(); iter.hasNext();) {
                String module = (String) iter.next();
                writeValue(serializer, "module", module, tracker, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "modules").flush();
            logLocation(profile, "modules", start2, b.length());
        }
        if (profile.getDistributionManagement() != null) {
            writeDistributionManagement((DistributionManagement) profile.getDistributionManagement(), "distributionManagement", serializer);
        }
        if ((profile.getProperties() != null) && (profile.getProperties().size() > 0)) {
            serializer.startTag(NAMESPACE, "properties");
            flush(serializer);
            int start2 = b.length();
            InputLocation tracker = profile.getLocation("properties");
            for (Iterator iter = profile.getProperties().keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String value = (String) profile.getProperties().get(key);
                writeValue(serializer, key, value, tracker);
            }
            serializer.endTag(NAMESPACE, "properties").flush();
            logLocation(profile, "properties", start2, b.length());
        }
        if (profile.getDependencyManagement() != null) {
            writeDependencyManagement((DependencyManagement) profile.getDependencyManagement(), "dependencyManagement", serializer);
        }
        if ((profile.getDependencies() != null) && (profile.getDependencies().size() > 0)) {
            serializer.startTag(NAMESPACE, "dependencies");
            for (Iterator iter = profile.getDependencies().iterator(); iter.hasNext();) {
                Dependency o = (Dependency) iter.next();
                writeDependency(o, "dependency", serializer);
            }
            serializer.endTag(NAMESPACE, "dependencies");
        }
        if ((profile.getRepositories() != null) && (profile.getRepositories().size() > 0)) {
            serializer.startTag(NAMESPACE, "repositories");
            for (Iterator iter = profile.getRepositories().iterator(); iter.hasNext();) {
                Repository o = (Repository) iter.next();
                writeRepository(o, "repository", serializer);
            }
            serializer.endTag(NAMESPACE, "repositories");
        }
        if ((profile.getPluginRepositories() != null) && (profile.getPluginRepositories().size() > 0)) {
            serializer.startTag(NAMESPACE, "pluginRepositories");
            for (Iterator iter = profile.getPluginRepositories().iterator(); iter.hasNext();) {
                Repository o = (Repository) iter.next();
                writeRepository(o, "pluginRepository", serializer);
            }
            serializer.endTag(NAMESPACE, "pluginRepositories");
        }
        if (profile.getReports() != null) {
            writeXpp3DOM(serializer, (Xpp3Dom)profile.getReports(), profile);
        }
        if (profile.getReporting() != null) {
            writeReporting((Reporting) profile.getReporting(), "reporting", serializer);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(profile, "", start, b.length());
    }
    
    private void writeRelocation(Relocation relocation, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (relocation.getGroupId() != null) {
            writeValue(serializer, "groupId", relocation.getGroupId(), relocation);
        }
        if (relocation.getArtifactId() != null) {
            writeValue(serializer, "artifactId", relocation.getArtifactId(), relocation);
        }
        if (relocation.getVersion() != null) {
            writeValue(serializer, "version", relocation.getVersion(), relocation);
        }
        if (relocation.getMessage() != null) {
            writeValue(serializer, "message", relocation.getMessage(), relocation);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(relocation, "", start, b.length());
    }
    
    private void writeReportPlugin(ReportPlugin reportPlugin, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if ((reportPlugin.getGroupId() != null) && !reportPlugin.getGroupId().equals("org.apache.maven.plugins")) {
            writeValue(serializer, "groupId", reportPlugin.getGroupId(), reportPlugin);
        }
        if (reportPlugin.getArtifactId() != null) {
            writeValue(serializer, "artifactId", reportPlugin.getArtifactId(), reportPlugin);
        }
        if (reportPlugin.getVersion() != null) {
            writeValue(serializer, "version", reportPlugin.getVersion(), reportPlugin);
        }
        if ((reportPlugin.getReportSets() != null) && (reportPlugin.getReportSets().size() > 0)) {
            serializer.startTag(NAMESPACE, "reportSets");
            for (Iterator iter = reportPlugin.getReportSets().iterator(); iter.hasNext();) {
                ReportSet o = (ReportSet) iter.next();
                writeReportSet(o, "reportSet", serializer);
            }
            serializer.endTag(NAMESPACE, "reportSets");
        }
        if (reportPlugin.getInherited() != null) {
            writeValue(serializer, "inherited", reportPlugin.getInherited(), reportPlugin);
        }
        if (reportPlugin.getConfiguration() != null) {
            writeXpp3DOM(serializer, (Xpp3Dom)reportPlugin.getConfiguration(), reportPlugin);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(reportPlugin, "", start, b.length());
    } 
    
    private void writeReportSet(ReportSet reportSet, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if ((reportSet.getId() != null) && !reportSet.getId().equals("default")) {
            writeValue(serializer, "id", reportSet.getId(), reportSet);
        }
        if ((reportSet.getReports() != null) && (reportSet.getReports().size() > 0)) {
            serializer.startTag(NAMESPACE, "reports");
            flush(serializer);
            int start2 = b.length();
            InputLocation tracker = reportSet.getLocation("reports");
            int index = 0;
            for (Iterator iter = reportSet.getReports().iterator(); iter.hasNext();) {
                String report = (String) iter.next();
                writeValue(serializer, "report", report, tracker, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "reports").flush();
            logLocation(reportSet, "reports", start2, b.length());
        }
        if (reportSet.getInherited() != null) {
            writeValue(serializer, "inherited", reportSet.getInherited(), reportSet);
        }
        if (reportSet.getConfiguration() != null) {
            writeXpp3DOM(serializer, (Xpp3Dom)reportSet.getConfiguration(), reportSet);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(reportSet, "", start, b.length());
    } 
    
    private void writeReporting(Reporting reporting, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (reporting.getExcludeDefaults() != null) {
            writeValue(serializer, "excludeDefaults", reporting.getExcludeDefaults(), reporting);
        }
        if (reporting.getOutputDirectory() != null) {
            writeValue(serializer, "outputDirectory", reporting.getOutputDirectory(), reporting);
        }
        if ((reporting.getPlugins() != null) && (reporting.getPlugins().size() > 0)) {
            serializer.startTag(NAMESPACE, "plugins");
            for (Iterator iter = reporting.getPlugins().iterator(); iter.hasNext();) {
                ReportPlugin o = (ReportPlugin) iter.next();
                writeReportPlugin(o, "plugin", serializer);
            }
            serializer.endTag(NAMESPACE, "plugins");
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(reporting, "", start, b.length());
    } 
    
    private void writeRepository(Repository repository, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (repository.getReleases() != null) {
            writeRepositoryPolicy((RepositoryPolicy) repository.getReleases(), "releases", serializer);
        }
        if (repository.getSnapshots() != null) {
            writeRepositoryPolicy((RepositoryPolicy) repository.getSnapshots(), "snapshots", serializer);
        }
        if (repository.getId() != null) {
            writeValue(serializer, "id", repository.getId(), repository);
        }
        if (repository.getName() != null) {
            writeValue(serializer, "name", repository.getName(), repository);
        }
        if (repository.getUrl() != null) {
            writeValue(serializer, "url", repository.getUrl(), repository);
        }
        if ((repository.getLayout() != null) && !repository.getLayout().equals("default")) {
            writeValue(serializer, "layout", repository.getLayout(), repository);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(repository, "", start, b.length());
    } 
    
    private void writeRepositoryPolicy(RepositoryPolicy repositoryPolicy, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (repositoryPolicy.getEnabled() != null) {
            writeValue(serializer, "enabled", repositoryPolicy.getEnabled(), repositoryPolicy);
        }
        if (repositoryPolicy.getUpdatePolicy() != null) {
            writeValue(serializer, "updatePolicy", repositoryPolicy.getUpdatePolicy(), repositoryPolicy);
        }
        if (repositoryPolicy.getChecksumPolicy() != null) {
            writeValue(serializer, "checksumPolicy", repositoryPolicy.getChecksumPolicy(), repositoryPolicy);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(repositoryPolicy, "", start, b.length());
    } 
    
    private void writeResource(Resource resource, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (resource.getTargetPath() != null) {
            writeValue(serializer, "targetPath", resource.getTargetPath(), resource);
        }
        if (resource.getFiltering() != null) {
            writeValue(serializer, "filtering", resource.getFiltering(), resource);
        }
        if (resource.getDirectory() != null) {
            writeValue(serializer, "directory", resource.getDirectory(), resource);
        }
        if ((resource.getIncludes() != null) && (resource.getIncludes().size() > 0)) {
            serializer.startTag(NAMESPACE, "includes");
            flush(serializer);
            int start2 = b.length();
            InputLocation inclTracker = resource.getLocation("includes");
            int index = 0;
            for (Iterator iter = resource.getIncludes().iterator(); iter.hasNext();) {
                String include = (String) iter.next();
                writeValue(serializer, "include", include, inclTracker, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "includes").flush();
            logLocation(resource, "includes", start2, b.length());
        }
        if ((resource.getExcludes() != null) && (resource.getExcludes().size() > 0)) {
            serializer.startTag(NAMESPACE, "excludes");
            flush(serializer);
            int start2 = b.length();
            InputLocation inclTracker = resource.getLocation("excludes");
            int index = 0;
            for (Iterator iter = resource.getExcludes().iterator(); iter.hasNext();) {
                String exclude = (String) iter.next();
                writeValue(serializer, "exclude", exclude, inclTracker, index);
                index = index + 1;
            }
            serializer.endTag(NAMESPACE, "excludes").flush();
            logLocation(resource, "excludes", start2, b.length());
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(resource, "", start, b.length());
    } 
    
    private void writeScm(Scm scm, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (scm.getConnection() != null) {
            writeValue(serializer, "connection", scm.getConnection(), scm);
        }
        if (scm.getDeveloperConnection() != null) {
            writeValue(serializer, "developerConnection", scm.getDeveloperConnection(), scm);
        }
        if ((scm.getTag() != null) && !scm.getTag().equals("HEAD")) {
            writeValue(serializer, "tag", scm.getTag(), scm);
        }
        if (scm.getUrl() != null) {
            writeValue(serializer, "url", scm.getUrl(), scm);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(scm, "", start, b.length());
    } 

    private void writeSite(Site site, String tagName, XmlSerializer serializer)
            throws java.io.IOException {
        serializer.startTag(NAMESPACE, tagName);
        flush(serializer);
        StringBuffer b = b(serializer);
        int start = b.length();
        if (site.getId() != null) {
            writeValue(serializer, "id", site.getId(), site);
        }
        if (site.getName() != null) {
            writeValue(serializer, "name", site.getName(), site);
        }
        if (site.getUrl() != null) {
            writeValue(serializer, "url", site.getUrl(), site);
        }
        serializer.endTag(NAMESPACE, tagName).flush();
        logLocation(site, "", start, b.length());
    }
}
