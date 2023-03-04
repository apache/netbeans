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
package org.netbeans.modules.maven.model.pom.visitor;

import org.netbeans.modules.maven.model.pom.Activation;
import org.netbeans.modules.maven.model.pom.ActivationCustom;
import org.netbeans.modules.maven.model.pom.ActivationFile;
import org.netbeans.modules.maven.model.pom.ActivationOS;
import org.netbeans.modules.maven.model.pom.ActivationProperty;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.CiManagement;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Contributor;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.DeploymentRepository;
import org.netbeans.modules.maven.model.pom.Developer;
import org.netbeans.modules.maven.model.pom.DistributionManagement;
import org.netbeans.modules.maven.model.pom.Exclusion;
import org.netbeans.modules.maven.model.pom.Extension;
import org.netbeans.modules.maven.model.pom.IssueManagement;
import org.netbeans.modules.maven.model.pom.License;
import org.netbeans.modules.maven.model.pom.MailingList;
import org.netbeans.modules.maven.model.pom.ModelList;
import org.netbeans.modules.maven.model.pom.Notifier;
import org.netbeans.modules.maven.model.pom.Organization;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMComponentVisitor;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.Parent;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Prerequisites;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.ReportPlugin;
import org.netbeans.modules.maven.model.pom.ReportSet;
import org.netbeans.modules.maven.model.pom.Reporting;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.model.pom.RepositoryPolicy;
import org.netbeans.modules.maven.model.pom.Resource;
import org.netbeans.modules.maven.model.pom.Scm;
import org.netbeans.modules.maven.model.pom.Site;
import org.netbeans.modules.maven.model.pom.StringList;


/**
 * Default shallow visitor.
 *
 * @author mkleint
 */
public class DefaultVisitor implements POMComponentVisitor {
        
    @Override
    public void visit(Project target) {
        visitComponent(target);
    }

    @Override
    public void visit(Parent target) {
        visitComponent(target);
    }

    @Override
    public void visit(Organization target) {
        visitComponent(target);
    }

    @Override
    public void visit(DistributionManagement target) {
        visitComponent(target);
    }

    @Override
    public void visit(Site target) {
        visitComponent(target);
    }

    @Override
    public void visit(DeploymentRepository target) {
        visitComponent(target);
    }

    @Override
    public void visit(Prerequisites target) {
        visitComponent(target);
    }

    @Override
    public void visit(Contributor target) {
        visitComponent(target);
    }

    @Override
    public void visit(Scm target) {
        visitComponent(target);
    }

    @Override
    public void visit(IssueManagement target) {
        visitComponent(target);
    }

    @Override
    public void visit(CiManagement target) {
        visitComponent(target);
    }

    @Override
    public void visit(Notifier target) {
        visitComponent(target);
    }

    @Override
    public void visit(Repository target) {
        visitComponent(target);
    }

    @Override
    public void visit(RepositoryPolicy target) {
        visitComponent(target);
    }

    @Override
    public void visit(Profile target) {
        visitComponent(target);
    }

    @Override
    public void visit(BuildBase target) {
        visitComponent(target);
    }

    @Override
    public void visit(Plugin target) {
        visitComponent(target);
    }

    @Override
    public void visit(Dependency target) {
        visitComponent(target);
    }

    @Override
    public void visit(Exclusion target) {
        visitComponent(target);
    }

    @Override
    public void visit(PluginExecution target) {
        visitComponent(target);
    }

    @Override
    public void visit(Resource target) {
        visitComponent(target);
    }

    @Override
    public void visit(PluginManagement target) {
        visitComponent(target);
    }

    @Override
    public void visit(Reporting target) {
        visitComponent(target);
    }

    @Override
    public void visit(ReportPlugin target) {
        visitComponent(target);
    }

    @Override
    public void visit(ReportSet target) {
        visitComponent(target);
    }

    @Override
    public void visit(Activation target) {
        visitComponent(target);
    }

    @Override
    public void visit(ActivationProperty target) {
        visitComponent(target);
    }

    @Override
    public void visit(ActivationOS target) {
        visitComponent(target);
    }

    @Override
    public void visit(ActivationFile target) {
        visitComponent(target);
    }

    @Override
    public void visit(ActivationCustom target) {
        visitComponent(target);
    }

    @Override
    public void visit(DependencyManagement target) {
        visitComponent(target);
    }

    @Override
    public void visit(Build target) {
        visitComponent(target);
    }

    @Override
    public void visit(Extension target) {
        visitComponent(target);
    }

    @Override
    public void visit(License target) {
        visitComponent(target);
    }

    @Override
    public void visit(MailingList target) {
        visitComponent(target);
    }

    @Override
    public void visit(Developer target) {
        visitComponent(target);
    }

    @Override
    public void visit(POMExtensibilityElement target) {
        visitComponent(target);
    }

    @Override
    public void visit(ModelList target) {
        visitComponent(target);
    }
    
    @Override
    public void visit(Configuration target) {
        visitComponent(target);
    }

    @Override
    public void visit(Properties target) {
        visitComponent(target);
    }

    protected void visitComponent(POMComponent target) {
    }

    @Override
    public void visit(StringList target) {
        visitComponent(target);
    }

}
