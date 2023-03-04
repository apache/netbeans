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

package org.netbeans.modules.groovy.support.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

/**
 * Constants useful for Groovy-based projects.
 *
 * @author Martin Adamek
 */
public class GroovySources {

    /**
     * Location of Groovy file icon (16x16)
     */
    public static final String GROOVY_FILE_ICON_16x16 = "org/netbeans/modules/groovy/support/resources/GroovyFile16x16.png"; // NOI18N

    /**
     * Groovy package root sources type.
     * @see org.netbeans.api.project.Sources
     */
    public static final String SOURCES_TYPE_GROOVY = "groovy"; // NOI18N

    /**
     * Groovy sources in standard Grails folders, e.g. domain classes or controllers
     */
    public static final String SOURCES_TYPE_GRAILS = "grails"; // NOI18N

    /**
     * Groovy spources in non-standard Grails folders, e.g. jobs dir added by Quartz plugin
     */
    public static final String SOURCES_TYPE_GRAILS_UNKNOWN = "grails_unknown"; // NOI18N

    /**
     * Searches for all source groups that can contain Groovy sources, including Grails
     * default folders and also folders added to Grails by plugins etc...
     */
    public static List<SourceGroup> getGroovySourceGroups(Sources sources) {
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        result.addAll(Arrays.asList(sources.getSourceGroups(GroovySources.SOURCES_TYPE_GROOVY)));
        result.addAll(Arrays.asList(sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)));
        result.addAll(Arrays.asList(sources.getSourceGroups(GroovySources.SOURCES_TYPE_GRAILS)));
        result.addAll(Arrays.asList(sources.getSourceGroups(GroovySources.SOURCES_TYPE_GRAILS_UNKNOWN)));
        return result;
    }

}
