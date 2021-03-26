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
package org.netbeans.modules.micronaut;

import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;

/**
 *
 * @author Dusan Balek
 */
public final class MicronautConfigProperties {

    private static final String CONFIG_METADATA_JSON = "META-INF/spring-configuration-metadata.json";

    private MicronautConfigProperties() {
    }

    public static boolean hasConfigMetadata(Project project) {
        ClassPath cp = getExecuteClasspath(project);
        return cp != null && !cp.findAllResources(CONFIG_METADATA_JSON).isEmpty();
    }

    public static Map<String, ConfigurationMetadataProperty> getProperties(Project project) {
        Map<String, ConfigurationMetadataProperty> props = new LinkedHashMap<>();
        ClassPath cp = getExecuteClasspath(project);
        if (cp != null) {
            for (FileObject fo : cp.findAllResources(CONFIG_METADATA_JSON)) {
                try {
                    ConfigurationMetadataRepository repository = ConfigurationMetadataRepositoryJsonBuilder.create().withJsonResource(fo.getInputStream()).build();
                    props.putAll(repository.getAllProperties());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return props;
    }

    public static Map<String, ConfigurationMetadataGroup> getGroups(Project project) {
        Map<String, ConfigurationMetadataGroup> groups = new LinkedHashMap<>();
        ClassPath cp = getExecuteClasspath(project);
        if (cp != null) {
            for (FileObject fo : cp.findAllResources(CONFIG_METADATA_JSON)) {
                try {
                    ConfigurationMetadataRepository repository = ConfigurationMetadataRepositoryJsonBuilder.create().withJsonResource(fo.getInputStream()).build();
                    groups.putAll(repository.getAllGroups());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return groups;
    }

    private static ClassPath getExecuteClasspath(Project project) {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGroups.length > 0) {
            return ClassPath.getClassPath(srcGroups[0].getRootFolder(), ClassPath.EXECUTE);
        }
        return null;
    }
}
