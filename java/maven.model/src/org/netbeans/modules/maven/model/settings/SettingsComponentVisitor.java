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
package org.netbeans.modules.maven.model.settings;


/**
 *
 * @author mkleint
 */
public interface SettingsComponentVisitor {
    
    void visit(Settings target);
    void visit(Repository target);
    void visit(RepositoryPolicy target);
    void visit(Profile target);
    void visit(Activation target);
    void visit(ActivationProperty target);
    void visit(ActivationOS target);
    void visit(ActivationFile target);
    void visit(ActivationCustom target);
    void visit(SettingsExtensibilityElement target);
    void visit(ModelList target);
    void visit(Properties target);
    void visit(StringList target);
    void visit(Configuration target);
    void visit(Mirror target);
    void visit(Proxy target);
    void visit(Server target);

}
