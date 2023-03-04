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

    public POMQNames(boolean ns) {
        this.ns = ns;
        PROJECT = new POMQName("project",ns);
        PARENT = new POMQName("parent",ns);
        ORGANIZATION = new POMQName("organization",ns);
        DISTRIBUTIONMANAGEMENT = new POMQName("distributionManagement",ns);
        SITE = new POMQName("site",ns);
        DIST_REPOSITORY = new POMQName("repository",ns);
        DIST_SNAPSHOTREPOSITORY = new POMQName("snapshotRepository",ns);
        PREREQUISITES = new POMQName("prerequisites",ns);
        CONTRIBUTOR = new POMQName("contributor",ns);
        SCM = new POMQName("scm",ns);
        ISSUEMANAGEMENT = new POMQName("issueManagement",ns);
        CIMANAGEMENT = new POMQName("ciManagement",ns);
        NOTIFIER = new POMQName("notifier",ns);
        REPOSITORY = new POMQName("repository",ns);
        PLUGINREPOSITORY = new POMQName("pluginRepository",ns);
        RELEASES = new POMQName("releases",ns);
        SNAPSHOTS = new POMQName("snapshots",ns);
        PROFILE = new POMQName("profile",ns);
        PLUGIN = new POMQName("plugin",ns);
        DEPENDENCY = new POMQName("dependency",ns);
        EXCLUSION = new POMQName("exclusion",ns);
        EXECUTION = new POMQName("execution",ns);
        RESOURCE = new POMQName("resource",ns);
        TESTRESOURCE = new POMQName("testResource",ns);
        PLUGINMANAGEMENT = new POMQName("pluginManagement",ns);
        REPORTING = new POMQName("reporting",ns);
        REPORTPLUGIN = new POMQName("plugin",ns);
        REPORTSET = new POMQName("reportSet",ns);
        ACTIVATION = new POMQName("activation",ns);
        ACTIVATIONPROPERTY = new POMQName("property",ns);
        ACTIVATIONOS = new POMQName("os",ns);
        ACTIVATIONFILE = new POMQName("file",ns);
        ACTIVATIONCUSTOM = new POMQName("custom",ns);
        DEPENDENCYMANAGEMENT = new POMQName("dependencyManagement",ns);
        BUILD = new POMQName("build",ns);
        EXTENSION = new POMQName("extension",ns);
        LICENSE = new POMQName("license",ns);
        MAILINGLIST = new POMQName("mailingList",ns);
        DEVELOPER = new POMQName("developer",ns);

        MAILINGLISTS = new POMQName("mailingLists",ns);
        DEPENDENCIES = new POMQName("dependencies",ns);
        DEVELOPERS = new POMQName("developers",ns);
        CONTRIBUTORS = new POMQName("contributors",ns);
        LICENSES = new POMQName("licenses",ns);
        PROFILES = new POMQName("profiles",ns);
        REPOSITORIES = new POMQName("repositories",ns);
        PLUGINREPOSITORIES = new POMQName("pluginRepositories",ns);
        EXCLUSIONS = new POMQName("exclusions",ns);
        EXECUTIONS = new POMQName("executions",ns);
        PLUGINS = new POMQName("plugins",ns);
        EXTENSIONS = new POMQName("extensions",ns);
        RESOURCES = new POMQName("resources",ns);
        TESTRESOURCES = new POMQName("testResources",ns);
        REPORTPLUGINS = new POMQName("plugins",ns);
        REPORTSETS = new POMQName("reportSets",ns);


        ID = new POMQName("id",ns);
        GROUPID = new POMQName("groupId",ns);
        ARTIFACTID = new POMQName("artifactId",ns);
        VERSION = new POMQName("version",ns);
        CONFIGURATION = new POMQName("configuration",ns);
        PROPERTIES = new POMQName("properties",ns);

        RELATIVEPATH = new POMQName("relativePath",ns);

        MODELVERSION = new POMQName("modelVersion",ns);
        PACKAGING = new POMQName("packaging",ns);
        URL = new POMQName("url",ns);
        NAME = new POMQName("name",ns);
        DESCRIPTION = new POMQName("description",ns);
        INCEPTIONYEAR = new POMQName("inceptionYear",ns);

        TYPE = new POMQName("type",ns);
        CLASSIFIER = new POMQName("classifier",ns);
        SCOPE = new POMQName("scope",ns);
        SYSTEMPATH = new POMQName("systemPath",ns);
        OPTIONAL = new POMQName("optional",ns);

        INHERITED = new POMQName("inherited",ns);
        PHASE = new POMQName("phase",ns);

        CIMANAG_SYSTEM = new POMQName("system",ns);

        DIRECTORY = new POMQName("directory",ns);
        DEFAULTGOAL = new POMQName("defaultGoal",ns);
        FINALNAME = new POMQName("finalName",ns);

        SOURCEDIRECTORY = new POMQName("sourceDirectory",ns);
        SCRIPTSOURCEDIRECTORY = new POMQName("scriptSourceDirectory",ns);
        TESTSOURCEDIRECTORY = new POMQName("testSourceDirectory",ns);
        OUTPUTDIRECTORY = new POMQName("outputDirectory",ns);
        TESTOUTPUTDIRECTORY = new POMQName("testOutputDirectory",ns);

        EXCLUDEDEFAULTS = new POMQName("excludeDefaults",ns);

        VALUE = new POMQName("value",ns);

        LAYOUT = new POMQName("layout",ns);

        GOALS = new POMQName("goals",ns);
        GOAL = new POMQName("goal",ns);

        MODULES = new POMQName("modules",ns);
        MODULE = new POMQName("module",ns);

        EXISTS = new POMQName("exists",ns);
        MISSING = new POMQName("missing",ns);

        FAMILY = new POMQName("family",ns);
        ARCH = new POMQName("arch",ns);

        TARGETPATH = new POMQName("targetPath",ns);
        FILTERING = new POMQName("filtering",ns);
        INCLUDES = new POMQName("includes",ns);
        INCLUDE = new POMQName("include",ns);
        EXCLUDES = new POMQName("excludes",ns);
        EXCLUDE = new POMQName("exclude",ns);

        TAG = new POMQName("tag",ns);
        CONNECTION = new POMQName("connection",ns);
        DEVELOPERCONNECTION = new POMQName("developerConnection",ns);

        SYSTEM = new POMQName("system",ns);

        ORGANIZATIONURL = new POMQName("organizationUrl",ns);
        EMAIL = new POMQName("email",ns);
        TIMEZONE = new POMQName("timezone",ns);
        //when adding items here, need to add them to the set below as well.

        SUBSCRIBE = new POMQName("subscribe",ns);
        UNSUBSCRIBE = new POMQName("unsubscribe",ns);
        POST = new POMQName("post",ns);
        ARCHIVE = new POMQName("archive",ns);

        DOWNLOADURL = new POMQName("downloadUrl",ns);

        MAVEN = new POMQName("maven",ns);

        REPORTS = new POMQName("reports",ns);
        REPORT = new POMQName("report",ns);

        ENABLED = new POMQName("enabled",ns);
        UPDATEPOLICY = new POMQName("updatePolicy",ns);
        CHECKSUMPOLICY = new POMQName("checksumPolicy",ns);
        COMMENTS = new POMQName("comments",ns);
        ROLES = new POMQName("roles",ns);
        ROLE = new POMQName("role",ns);
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
