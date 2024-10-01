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
package org.netbeans.modules.cloud.oracle.assets;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Petrovic
 */
@ServiceProvider(service = CommandProvider.class)
public class ConfigFileProvider implements CommandProvider {
    private static final String GET_CONFIG_FILE_PATH = "nbls.config.file.path"; //NOI18N

    private final TempFileGenerator applicationPropertiesFileGenerator;
    private final TempFileGenerator bootstrapPropertiesFileGenerator;

    public ConfigFileProvider() {
        this.applicationPropertiesFileGenerator = new TempFileGenerator("application-", ".properties", GET_CONFIG_FILE_PATH, false); // NOI18N
        this.bootstrapPropertiesFileGenerator = new TempFileGenerator("bootstrap-", ".properties", GET_CONFIG_FILE_PATH, false); // NOI18N
    }
    
    @Override
    public Set<String> getCommands() {
        return Collections.singleton(GET_CONFIG_FILE_PATH);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        CompletableFuture ret = new CompletableFuture();

        Properties applicationProps = new Properties();
        Properties bootstrapProps = new Properties();
        PropertiesGenerator propGen = new PropertiesGenerator(false);
        
        applicationProps.putAll(propGen.getApplication());
        bootstrapProps.putAll(propGen.getBootstrap());
        
        String applicationPropertiesPath = null;
        String bootstrapPropertiesPath = null;  
        try {
            if (!bootstrapProps.isEmpty()) {
                Path bootstrapProperties = bootstrapPropertiesFileGenerator.writePropertiesFile(bootstrapProps);
                bootstrapPropertiesPath = bootstrapProperties.toAbsolutePath().toString();
            }
            
            if (!applicationProps.isEmpty()) {
                Path applicationProperties = applicationPropertiesFileGenerator.writePropertiesFile(applicationProps);
                applicationPropertiesPath = applicationProperties.toAbsolutePath().toString();
            }
            ret.complete(new ConfigFilesResponse(applicationPropertiesPath, bootstrapPropertiesPath));
        } catch (IOException ex) {
            ret.completeExceptionally(ex);
            return ret;
        }
        
        return ret;
    }
    
    private class ConfigFilesResponse {
        
        final String applicationProperties;
        final String bootstrapProperties;

        public ConfigFilesResponse(String applicationProperties, String bootstrapProperties) {
            this.applicationProperties = applicationProperties;
            this.bootstrapProperties = bootstrapProperties;
        }
    }
}
