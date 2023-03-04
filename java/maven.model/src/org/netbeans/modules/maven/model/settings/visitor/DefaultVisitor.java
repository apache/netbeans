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
package org.netbeans.modules.maven.model.settings.visitor;

import org.netbeans.modules.maven.model.settings.Activation;
import org.netbeans.modules.maven.model.settings.ActivationCustom;
import org.netbeans.modules.maven.model.settings.ActivationFile;
import org.netbeans.modules.maven.model.settings.ActivationOS;
import org.netbeans.modules.maven.model.settings.ActivationProperty;
import org.netbeans.modules.maven.model.settings.Configuration;
import org.netbeans.modules.maven.model.settings.Mirror;
import org.netbeans.modules.maven.model.settings.ModelList;
import org.netbeans.modules.maven.model.settings.Profile;
import org.netbeans.modules.maven.model.settings.Properties;
import org.netbeans.modules.maven.model.settings.Proxy;
import org.netbeans.modules.maven.model.settings.Repository;
import org.netbeans.modules.maven.model.settings.RepositoryPolicy;
import org.netbeans.modules.maven.model.settings.Server;
import org.netbeans.modules.maven.model.settings.Settings;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement;
import org.netbeans.modules.maven.model.settings.StringList;



/**
 * Default shallow visitor.
 *
 * @author mkleint
 */
public class DefaultVisitor implements SettingsComponentVisitor {
        
    @Override
    public void visit(Settings target) {
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
    public void visit(SettingsExtensibilityElement target) {
        visitComponent(target);
    }

    @Override
    public void visit(ModelList target) {
        visitComponent(target);
    }
    
    @Override
    public void visit(Properties target) {
        visitComponent(target);
    }

    protected void visitComponent(SettingsComponent target) {
    }

    @Override
    public void visit(StringList target) {
        visitComponent(target);
    }

    @Override
    public void visit(Configuration target) {
        visitComponent(target);
    }

    @Override
    public void visit(Mirror target) {
        visitComponent(target);
    }

    @Override
    public void visit(Proxy target) {
        visitComponent(target);
    }

    @Override
    public void visit(Server target) {
        visitComponent(target);
    }


}
