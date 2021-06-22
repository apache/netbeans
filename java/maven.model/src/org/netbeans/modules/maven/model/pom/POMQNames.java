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
package org.netbeans.modules.maven.model.pom;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author mkleint
 */
public final class POMQNames {
    
    public final POMQName PROJECT; // NOI18N
    public final POMQName PARENT; // NOI18N
    public final POMQName ORGANIZATION; // NOI18N
    public final POMQName DISTRIBUTIONMANAGEMENT; // NOI18N
    public final POMQName SITE; // NOI18N
    public final POMQName DIST_REPOSITORY; // NOI18N
    public final POMQName DIST_SNAPSHOTREPOSITORY; // NOI18N
    public final POMQName PREREQUISITES; // NOI18N
    public final POMQName CONTRIBUTOR; // NOI18N
    public final POMQName SCM; // NOI18N
    public final POMQName ISSUEMANAGEMENT; // NOI18N
    public final POMQName CIMANAGEMENT; // NOI18N
    public final POMQName NOTIFIER; // NOI18N
    public final POMQName REPOSITORY; // NOI18N
    public final POMQName PLUGINREPOSITORY; // NOI18N
    public final POMQName RELEASES; // NOI18N
    public final POMQName SNAPSHOTS; // NOI18N
    public final POMQName PROFILE; // NOI18N
    public final POMQName PLUGIN; // NOI18N
    public final POMQName DEPENDENCY; // NOI18N
    public final POMQName EXCLUSION; // NOI18N
    public final POMQName EXECUTION; // NOI18N
    public final POMQName RESOURCE; // NOI18N
    public final POMQName TESTRESOURCE; // NOI18N
    public final POMQName PLUGINMANAGEMENT; // NOI18N
    public final POMQName REPORTING; // NOI18N
    public final POMQName REPORTPLUGIN; // NOI18N
    public final POMQName REPORTSET; // NOI18N
    public final POMQName ACTIVATION; // NOI18N
    public final POMQName ACTIVATIONPROPERTY; // NOI18N
    public final POMQName ACTIVATIONOS; // NOI18N
    public final POMQName ACTIVATIONFILE; // NOI18N
    public final POMQName ACTIVATIONCUSTOM; // NOI18N
    public final POMQName DEPENDENCYMANAGEMENT; // NOI18N
    public final POMQName BUILD; // NOI18N
    public final POMQName EXTENSION; // NOI18N
    public final POMQName LICENSE; // NOI18N
    public final POMQName MAILINGLIST; // NOI18N
    public final POMQName DEVELOPER; // NOI18N

    public final POMQName MAILINGLISTS; // NOI18N
    public final POMQName DEPENDENCIES; // NOI18N
    public final POMQName DEVELOPERS; // NOI18N
    public final POMQName CONTRIBUTORS; // NOI18N
    public final POMQName LICENSES; // NOI18N
    public final POMQName PROFILES; // NOI18N
    public final POMQName REPOSITORIES; // NOI18N
    public final POMQName PLUGINREPOSITORIES; // NOI18N
    public final POMQName EXCLUSIONS; // NOI18N
    public final POMQName EXECUTIONS; // NOI18N
    public final POMQName PLUGINS; // NOI18N
    public final POMQName EXTENSIONS; // NOI18N
    public final POMQName RESOURCES; // NOI18N
    public final POMQName TESTRESOURCES; // NOI18N
    public final POMQName REPORTPLUGINS; // NOI18N
    public final POMQName REPORTSETS; // NOI18N


    public final POMQName ID; //NOI18N
    public final POMQName GROUPID; //NOI18N
    public final POMQName ARTIFACTID; //NOI18N
    public final POMQName VERSION; //NOI18N
    public final POMQName CONFIGURATION; //NOI18N
    public final POMQName PROPERTIES; //NOI18N

    public final POMQName RELATIVEPATH; //NOI18N

    public final POMQName MODELVERSION; //NOI18N
    public final POMQName PACKAGING; //NOI18N
    public final POMQName URL; //NOI18N
    public final POMQName NAME; //NOI18N
    public final POMQName DESCRIPTION; //NOI18N
    public final POMQName INCEPTIONYEAR; //NOI18N

    public final POMQName TYPE; //NOI18N
    public final POMQName CLASSIFIER; //NOI18N
    public final POMQName SCOPE; //NOI18N
    public final POMQName SYSTEMPATH; //NOI18N
    public final POMQName OPTIONAL; //NOI18N

    public final POMQName INHERITED; //NOI18N
    public final POMQName PHASE; //NOI18N

    public final POMQName CIMANAG_SYSTEM; //NOI18N

    public final POMQName DIRECTORY; //NOI18N
    public final POMQName DEFAULTGOAL; //NOI18N
    public final POMQName FINALNAME; //NOI18N

    public final POMQName SOURCEDIRECTORY; //NOI18N
    public final POMQName SCRIPTSOURCEDIRECTORY; //NOI18N
    public final POMQName TESTSOURCEDIRECTORY; //NOI18N
    public final POMQName OUTPUTDIRECTORY; //NOI18N
    public final POMQName TESTOUTPUTDIRECTORY; //NOI18N

    public final POMQName EXCLUDEDEFAULTS; //NOI18N

    public final POMQName VALUE; //NOI18N

    public final POMQName LAYOUT; //NOI18N

    public final POMQName GOALS; //NOI18N
    public final POMQName GOAL; //NOI18N

    public final POMQName MODULES; //NOI18N
    public final POMQName MODULE; //NOI18N

    public final POMQName EXISTS;
    public final POMQName MISSING;

    public final POMQName ARCH;
    public final POMQName FAMILY;

    public final POMQName TARGETPATH;
    public final POMQName FILTERING;
    public final POMQName INCLUDES;
    public final POMQName INCLUDE;
    public final POMQName EXCLUDES;
    public final POMQName EXCLUDE;

    public final POMQName TAG;
    public final POMQName CONNECTION;
    public final POMQName DEVELOPERCONNECTION;

    public final POMQName SYSTEM;

    public final POMQName EMAIL;
    public final POMQName ORGANIZATIONURL;
    public final POMQName TIMEZONE;

    public final POMQName SUBSCRIBE;
    public final POMQName UNSUBSCRIBE;
    public final POMQName POST;
    public final POMQName ARCHIVE;

    public final POMQName DOWNLOADURL;

    public final POMQName MAVEN;

    public final POMQName REPORTS;
    public final POMQName REPORT;

    public final POMQName ENABLED;
    public final POMQName UPDATEPOLICY;
    public final POMQName CHECKSUMPOLICY;
    public final POMQName COMMENTS;
    public final POMQName ROLES;
    public final POMQName ROLE;



    private final boolean ns;

    /**
     * @deprecated Use {@link POMQNames(boolean, boolean)}
     */
    @Deprecated
    public POMQNames(boolean ns) {
        this(ns, false);
    }

    public POMQNames(boolean ns, boolean secure) {
        this.ns = ns;
        PROJECT = new POMQName("project",ns,secure);
        PARENT = new POMQName("parent",ns,secure);
        ORGANIZATION = new POMQName("organization",ns,secure);
        DISTRIBUTIONMANAGEMENT = new POMQName("distributionManagement",ns,secure);
        SITE = new POMQName("site",ns,secure);
        DIST_REPOSITORY = new POMQName("repository",ns,secure);
        DIST_SNAPSHOTREPOSITORY = new POMQName("snapshotRepository",ns,secure);
        PREREQUISITES = new POMQName("prerequisites",ns,secure);
        CONTRIBUTOR = new POMQName("contributor",ns,secure);
        SCM = new POMQName("scm",ns,secure);
        ISSUEMANAGEMENT = new POMQName("issueManagement",ns,secure);
        CIMANAGEMENT = new POMQName("ciManagement",ns,secure);
        NOTIFIER = new POMQName("notifier",ns,secure);
        REPOSITORY = new POMQName("repository",ns,secure);
        PLUGINREPOSITORY = new POMQName("pluginRepository",ns,secure);
        RELEASES = new POMQName("releases",ns,secure);
        SNAPSHOTS = new POMQName("snapshots",ns,secure);
        PROFILE = new POMQName("profile",ns,secure);
        PLUGIN = new POMQName("plugin",ns,secure);
        DEPENDENCY = new POMQName("dependency",ns,secure);
        EXCLUSION = new POMQName("exclusion",ns,secure);
        EXECUTION = new POMQName("execution",ns,secure);
        RESOURCE = new POMQName("resource",ns,secure);
        TESTRESOURCE = new POMQName("testResource",ns,secure);
        PLUGINMANAGEMENT = new POMQName("pluginManagement",ns,secure);
        REPORTING = new POMQName("reporting",ns,secure);
        REPORTPLUGIN = new POMQName("plugin",ns,secure);
        REPORTSET = new POMQName("reportSet",ns,secure);
        ACTIVATION = new POMQName("activation",ns,secure);
        ACTIVATIONPROPERTY = new POMQName("property",ns,secure);
        ACTIVATIONOS = new POMQName("os",ns,secure);
        ACTIVATIONFILE = new POMQName("file",ns,secure);
        ACTIVATIONCUSTOM = new POMQName("custom",ns,secure);
        DEPENDENCYMANAGEMENT = new POMQName("dependencyManagement",ns,secure);
        BUILD = new POMQName("build",ns,secure);
        EXTENSION = new POMQName("extension",ns,secure);
        LICENSE = new POMQName("license",ns,secure);
        MAILINGLIST = new POMQName("mailingList",ns,secure);
        DEVELOPER = new POMQName("developer",ns,secure);

        MAILINGLISTS = new POMQName("mailingLists",ns,secure);
        DEPENDENCIES = new POMQName("dependencies",ns,secure);
        DEVELOPERS = new POMQName("developers",ns,secure);
        CONTRIBUTORS = new POMQName("contributors",ns,secure);
        LICENSES = new POMQName("licenses",ns,secure);
        PROFILES = new POMQName("profiles",ns,secure);
        REPOSITORIES = new POMQName("repositories",ns,secure);
        PLUGINREPOSITORIES = new POMQName("pluginRepositories",ns,secure);
        EXCLUSIONS = new POMQName("exclusions",ns,secure);
        EXECUTIONS = new POMQName("executions",ns,secure);
        PLUGINS = new POMQName("plugins",ns,secure);
        EXTENSIONS = new POMQName("extensions",ns,secure);
        RESOURCES = new POMQName("resources",ns,secure);
        TESTRESOURCES = new POMQName("testResources",ns,secure);
        REPORTPLUGINS = new POMQName("plugins",ns,secure);
        REPORTSETS = new POMQName("reportSets",ns,secure);


        ID = new POMQName("id",ns,secure);
        GROUPID = new POMQName("groupId",ns,secure);
        ARTIFACTID = new POMQName("artifactId",ns,secure);
        VERSION = new POMQName("version",ns,secure);
        CONFIGURATION = new POMQName("configuration",ns,secure);
        PROPERTIES = new POMQName("properties",ns,secure);

        RELATIVEPATH = new POMQName("relativePath",ns,secure);

        MODELVERSION = new POMQName("modelVersion",ns,secure);
        PACKAGING = new POMQName("packaging",ns,secure);
        URL = new POMQName("url",ns,secure);
        NAME = new POMQName("name",ns,secure);
        DESCRIPTION = new POMQName("description",ns,secure);
        INCEPTIONYEAR = new POMQName("inceptionYear",ns,secure);

        TYPE = new POMQName("type",ns,secure);
        CLASSIFIER = new POMQName("classifier",ns,secure);
        SCOPE = new POMQName("scope",ns,secure);
        SYSTEMPATH = new POMQName("systemPath",ns,secure);
        OPTIONAL = new POMQName("optional",ns,secure);

        INHERITED = new POMQName("inherited",ns,secure);
        PHASE = new POMQName("phase",ns,secure);

        CIMANAG_SYSTEM = new POMQName("system",ns,secure);

        DIRECTORY = new POMQName("directory",ns,secure);
        DEFAULTGOAL = new POMQName("defaultGoal",ns,secure);
        FINALNAME = new POMQName("finalName",ns,secure);

        SOURCEDIRECTORY = new POMQName("sourceDirectory",ns,secure);
        SCRIPTSOURCEDIRECTORY = new POMQName("scriptSourceDirectory",ns,secure);
        TESTSOURCEDIRECTORY = new POMQName("testSourceDirectory",ns,secure);
        OUTPUTDIRECTORY = new POMQName("outputDirectory",ns,secure);
        TESTOUTPUTDIRECTORY = new POMQName("testOutputDirectory",ns,secure);

        EXCLUDEDEFAULTS = new POMQName("excludeDefaults",ns,secure);

        VALUE = new POMQName("value",ns,secure);

        LAYOUT = new POMQName("layout",ns,secure);

        GOALS = new POMQName("goals",ns,secure);
        GOAL = new POMQName("goal",ns,secure);

        MODULES = new POMQName("modules",ns,secure);
        MODULE = new POMQName("module",ns,secure);

        EXISTS = new POMQName("exists",ns,secure);
        MISSING = new POMQName("missing",ns,secure);

        FAMILY = new POMQName("family",ns,secure);
        ARCH = new POMQName("arch",ns,secure);

        TARGETPATH = new POMQName("targetPath",ns,secure);
        FILTERING = new POMQName("filtering",ns,secure);
        INCLUDES = new POMQName("includes",ns,secure);
        INCLUDE = new POMQName("include",ns,secure);
        EXCLUDES = new POMQName("excludes",ns,secure);
        EXCLUDE = new POMQName("exclude",ns,secure);

        TAG = new POMQName("tag",ns,secure);
        CONNECTION = new POMQName("connection",ns,secure);
        DEVELOPERCONNECTION = new POMQName("developerConnection",ns,secure);

        SYSTEM = new POMQName("system",ns,secure);

        ORGANIZATIONURL = new POMQName("organizationUrl",ns,secure);
        EMAIL = new POMQName("email",ns,secure);
        TIMEZONE = new POMQName("timezone",ns,secure);
        //when adding items here, need to add them to the set below as well.

        SUBSCRIBE = new POMQName("subscribe",ns,secure);
        UNSUBSCRIBE = new POMQName("unsubscribe",ns,secure);
        POST = new POMQName("post",ns,secure);
        ARCHIVE = new POMQName("archive",ns,secure);

        DOWNLOADURL = new POMQName("downloadUrl",ns,secure);

        MAVEN = new POMQName("maven",ns,secure);

        REPORTS = new POMQName("reports",ns,secure);
        REPORT = new POMQName("report",ns,secure);

        ENABLED = new POMQName("enabled",ns,secure);
        UPDATEPOLICY = new POMQName("updatePolicy",ns,secure);
        CHECKSUMPOLICY = new POMQName("checksumPolicy",ns,secure);
        COMMENTS = new POMQName("comments",ns,secure);
        ROLES = new POMQName("roles",ns,secure);
        ROLE = new POMQName("role",ns,secure);
    }

    public boolean isNSAware() {
        return ns;
    }

    public Set<QName> getElementQNames() {
        QName[] names = new QName[] {
            PROJECT.getQName(),
            PARENT.getQName(),
            ORGANIZATION.getQName(),
            DISTRIBUTIONMANAGEMENT.getQName(),
            SITE.getQName(),
            DIST_REPOSITORY.getQName(),
            DIST_SNAPSHOTREPOSITORY.getQName(),
            PREREQUISITES.getQName(),
            CONTRIBUTOR.getQName(),
            SCM.getQName(),
            ISSUEMANAGEMENT.getQName(),
            CIMANAGEMENT.getQName(),
            NOTIFIER.getQName(),
            REPOSITORY.getQName(),
            PLUGINREPOSITORY.getQName(),
            RELEASES.getQName(),
            SNAPSHOTS.getQName(),
            PROFILE.getQName(),
            PLUGIN.getQName(),
            DEPENDENCY.getQName(),
            EXCLUSION.getQName(),
            EXECUTION.getQName(),
            RESOURCE.getQName(),
            TESTRESOURCE.getQName(),
            PLUGINMANAGEMENT.getQName(),
            REPORTING.getQName(),
            REPORTPLUGIN.getQName(),
            REPORTSET.getQName(),
            ACTIVATION.getQName(),
            ACTIVATIONPROPERTY.getQName(),
            ACTIVATIONOS.getQName(),
            ACTIVATIONFILE.getQName(),
            ACTIVATIONCUSTOM.getQName(),
            DEPENDENCYMANAGEMENT.getQName(),
            BUILD.getQName(),
            EXTENSION.getQName(),
            LICENSE.getQName(),
            MAILINGLIST.getQName(),
            DEVELOPER.getQName(),
            MAILINGLISTS.getQName(),
            DEPENDENCIES.getQName(),
            DEVELOPERS.getQName(),
            CONTRIBUTORS.getQName(),
            LICENSES.getQName(),
            PROFILES.getQName(),
            REPOSITORIES.getQName(),
            PLUGINREPOSITORIES.getQName(),
            EXCLUSIONS.getQName(),
            EXECUTIONS.getQName(),
            PLUGINS.getQName(),
            EXTENSIONS.getQName(),
            RESOURCES.getQName(),
            TESTRESOURCES.getQName(),
            REPORTPLUGINS.getQName(),
            REPORTSETS.getQName(),
            ID.getQName(),
            GROUPID.getQName(),
            ARTIFACTID.getQName(),
            VERSION.getQName(),
            CONFIGURATION.getQName(),
            PROPERTIES.getQName(),
            RELATIVEPATH.getQName(),
            MODELVERSION.getQName(),
            PACKAGING.getQName(),
            URL.getQName(),
            NAME.getQName(),
            DESCRIPTION.getQName(),
            INCEPTIONYEAR.getQName(),
            TYPE.getQName(),
            CLASSIFIER.getQName(),
            SCOPE.getQName(),
            SYSTEMPATH.getQName(),
            OPTIONAL.getQName(),
            INHERITED.getQName(),
            PHASE.getQName(),
            CIMANAG_SYSTEM.getQName(),
            DIRECTORY.getQName(),
            DEFAULTGOAL.getQName(),
            FINALNAME.getQName(),
            SOURCEDIRECTORY.getQName(),
            SCRIPTSOURCEDIRECTORY.getQName(),
            TESTSOURCEDIRECTORY.getQName(),
            OUTPUTDIRECTORY.getQName(),
            TESTOUTPUTDIRECTORY.getQName(),
            EXCLUDEDEFAULTS.getQName(),
            VALUE.getQName(),
            LAYOUT.getQName(),
            GOALS.getQName(),
            GOAL.getQName(),
            MODULES.getQName(),
            MODULE.getQName(),
            EXISTS.getQName(),
            MISSING.getQName(),
            ARCH.getQName(),
            FAMILY.getQName(),
            TARGETPATH.getQName(),
            FILTERING.getQName(),
            INCLUDES.getQName(),
            INCLUDE.getQName(),
            EXCLUDES.getQName(),
            EXCLUDE.getQName(),
            DEVELOPERCONNECTION.getQName(),
            CONNECTION.getQName(),
            TAG.getQName(),
            SYSTEMPATH.getQName(),
            ORGANIZATIONURL.getQName(),
            EMAIL.getQName(),
            TIMEZONE.getQName(),
            ARCHIVE.getQName(),
            SUBSCRIBE.getQName(),
            UNSUBSCRIBE.getQName(),
            POST.getQName(),
            DOWNLOADURL.getQName(),
            MAVEN.getQName(),
            REPORTS.getQName(),
            REPORT.getQName(),
            ENABLED.getQName(),
            UPDATEPOLICY.getQName(),
            CHECKSUMPOLICY.getQName(),
            COMMENTS.getQName(),
            ROLES.getQName(),
            ROLE.getQName()
        };
        List<QName> list = Arrays.asList(names);
        return new HashSet<QName>(list);
    }
    
}
