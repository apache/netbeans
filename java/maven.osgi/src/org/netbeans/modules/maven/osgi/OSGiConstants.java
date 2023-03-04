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

package org.netbeans.modules.maven.osgi;

/**
 *
 * @author mkleint
 */
public interface OSGiConstants {
    public static final String ARTIFACTID_BUNDLE_PLUGIN = "maven-bundle-plugin"; //NOI18N
    public static final String GROUPID_FELIX = "org.apache.felix"; //NOI18N

    public static final String GOAL_MANIFEST = "manifest"; //NOI18N
    public static final String PARAM_INSTRUCTIONS = "instructions"; //NOI18N

    public static final String BUNDLE_ACTIVATOR = "Bundle-Activator"; //NOI18N
    public static final String PRIVATE_PACKAGE = "Private-Package"; //NOI18N
    public static final String EXPORT_PACKAGE = "Export-Package"; //NOI18N
    public static final String IMPORT_PACKAGE = "Import-Package"; //NOI18N
    public static final String INCLUDE_RESOURCE = "Include-Resource"; //NOI18N
    public static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName"; //NOI18N

    public static final String EMBED_DEPENDENCY = "Embed-Dependency"; //NOI18N
    public static final String EMBED_DIRECTORY = "Embed-Directory"; //NOI18N
    public static final String EMBED_STRIP_GROUP = "Embed-StripGroup"; //NOI18N
    public static final String EMBED_STRIP_VERSION = "Embed-StripVersion"; //NOI18N
    public static final String EMBED_TRANSITIVE = "Embed-Transitive"; //NOI18N
}
