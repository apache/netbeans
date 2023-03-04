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


/**
 *
 * @author mkleint
 */
public interface POMComponentVisitor {
    
    void visit(Project target);
    void visit(Parent target);
    void visit(Organization target);
    void visit(DistributionManagement target);
    void visit(Site target);
    void visit(DeploymentRepository target);
    void visit(Prerequisites target);
    void visit(Contributor target);
    void visit(Scm target);
    void visit(IssueManagement target);
    void visit(CiManagement target);
    void visit(Notifier target);
    void visit(Repository target);
    void visit(RepositoryPolicy target);
    void visit(Profile target);
    void visit(BuildBase target);
    void visit(Plugin target);
    void visit(Dependency target);
    void visit(Exclusion target);
    void visit(PluginExecution target);
    void visit(Resource target);
    void visit(PluginManagement target);
    void visit(Reporting target);
    void visit(ReportPlugin target);
    void visit(ReportSet target);
    void visit(Activation target);
    void visit(ActivationProperty target);
    void visit(ActivationOS target);
    void visit(ActivationFile target);
    void visit(ActivationCustom target);
    void visit(DependencyManagement target);
    void visit(Build target);
    void visit(Extension target);
    void visit(License target);
    void visit(MailingList target);
    void visit(Developer target);
    void visit(POMExtensibilityElement target);
    void visit(ModelList target);
    void visit(Configuration target);
    void visit(Properties target);
    void visit(StringList target);

}
