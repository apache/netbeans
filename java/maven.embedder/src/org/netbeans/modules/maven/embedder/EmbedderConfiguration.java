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

package org.netbeans.modules.maven.embedder;

import java.io.File;
import java.util.Properties;
import org.codehaus.plexus.PlexusContainer;

/**
 *
 * @author mkleint
 */
class EmbedderConfiguration {
    private final PlexusContainer cont;
    private final Properties props;
    private final boolean offline;
    private final File settingsXml;
    private final Properties userprops;

    EmbedderConfiguration(PlexusContainer cont, Properties props, Properties userprops, boolean offline, File settingsXml) {
        this.cont = cont;
        this.props = props;
        this.offline = offline;
        this.settingsXml = settingsXml;
        this.userprops = userprops;
    }

    Properties getSystemProperties() {
        return props;
    }
    
    Properties getUserProperties() {
        return userprops;
    }

    PlexusContainer getContainer() {
        return cont;
    }

    public boolean isOffline() {
        return offline;
    }

    File getSettingsXml() {
        return settingsXml;
    }

}
