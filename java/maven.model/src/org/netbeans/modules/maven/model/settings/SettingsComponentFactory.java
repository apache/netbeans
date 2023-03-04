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

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.ComponentFactory;

/**
 *
 * @author mkleint
 */
public interface SettingsComponentFactory extends ComponentFactory<SettingsComponent>  {

    /**
     * Creates a domain component generically.
     */
    SettingsComponent create(SettingsComponent context, QName qName);
    
    // The following are specific create method for each of the defined 
    // component interfaces

    Settings createSettings();
    Repository createRepository();
    Repository createPluginRepository();
    RepositoryPolicy createSnapshotRepositoryPolicy();
    RepositoryPolicy createReleaseRepositoryPolicy();
    Profile createProfile();
    Activation createActivation();
    ActivationProperty createActivationProperty();
    ActivationOS createActivationOS();
    ActivationFile createActivationFile();
    ActivationCustom createActivationCustom();
    Properties createProperties();
    Configuration createConfiguration();
    Mirror createMirror();
    Proxy createProxy();
    Server createServer();
    SettingsExtensibilityElement createSettingsExtensibilityElement(QName name);
}
