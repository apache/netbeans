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

package org.netbeans.modules.projectimport.eclipse.core.spi;

import java.util.Map;
import java.util.Set;

/**
 * Represents one classpath entry of an Eclipse project's .classpath file.
 * Basically set of attributes (key/value pairs) and couple of helper methods for
 * some important attributes like 'kind' or 'path'.
 * 
 */
public final class DotClassPathEntry {

    public static final String ATTRIBUTE_KIND = "kind"; //NOI18N
    public static final String ATTRIBUTE_PATH = "path"; //NOI18N
    public static final String ATTRIBUTE_EXPORTED = "exported"; //NOI18N
    public static final String ATTRIBUTE_SOURCEPATH = "sourcepath"; //NOI18N
    public static final String ATTRIBUTE_JAVADOC = "javadoc_location"; //NOI18N
    public static final String ATTRIBUTE_SOURCE_EXCLUDES = "excluding"; //NOI18N
    public static final String ATTRIBUTE_SOURCE_INCLUDES = "including"; //NOI18N

    public static enum Kind {
        CONTAINER,
        LIBRARY,
        PROJECT,
        SOURCE,
        VARIABLE,
        OUTPUT
    };
    
    private Map<String,String> properties;
    
    private String absolutePath;
    
    private String containerMapping;
    
    private String linkName;
    
    private Boolean importSuccessful;
    
    public DotClassPathEntry(Map<String, String> properties, String linkName) {
        this.properties = properties;
        this.linkName = linkName;
    }
    
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }
    
    public Kind getKind() {
        String value = getProperty(ATTRIBUTE_KIND);
        if ("con".equals(value)) { // NOI18N
            return Kind.CONTAINER;
        } else if ("lib".equals(value)) { // NOI18N
            return Kind.LIBRARY;
        } else if ("src".equals(value)) { // NOI18N
            // resolved SOURCE link has absolute URL:
            if (getRawPath().startsWith("/") && getLinkName() == null) { //NOI18N
                return Kind.PROJECT;
            } else {
                return Kind.SOURCE;
            }
        } else if ("var".equals(value)) { // NOI18N
            return Kind.VARIABLE;
        } else if ("output".equals(value)) { // NOI18N
            return Kind.OUTPUT;
        }
        throw new IllegalStateException("unknown kind: "+value); //NOI18N
    }
    /**
     * Value of path attribute. If path attribute was link then resolved link 
     * value is returned instead and link name can be retrieve via {@link #getLinkName}.
     */
    public String getRawPath() {
        return getProperty(ATTRIBUTE_PATH);
    }

    /**
     * Normalized path.
     */
    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * Returns link name. 
     * @return null if entry was not a link
     */
    /*public*/ String getLinkName() {
        return linkName;
    }

    public boolean isExported() {
        return Boolean.parseBoolean(getProperty(ATTRIBUTE_EXPORTED));
    }
    
    /**
     * Despite being public this method should not be called outside of 
     * eclipse.core module.
     */
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
    
    @Override
    public String toString() {
        return "DotClassPathEntry: "+properties; //NOI18N
    }

    /**
     * Returns Ant-like classpath where entries are separated by ":" or ";" and
     * entry can be or contain:
     *  <li>absolute path to a folder or jar
     *  <li>netbeans project relative paths to a folders or jars
     *  <li>references to existing variables from project properties, 
     *     private properties or build.properties.
     *  <li>perhaps more
     * 
     * <p>Valid only for entry of CONTAINER type.
     */
    public String getContainerMapping() {
        return containerMapping;
    }
    
    /**
     * Despite being public this method should not be called outside of 
     * eclipse.core module.
     */
    public void setContainerMapping(String containerMapping) {
        this.containerMapping = containerMapping;
    }

    public Boolean getImportSuccessful() {
        return importSuccessful;
    }

    /**
     * Despite being public this method should not be called outside of 
     * eclipse.core module.
     */
    public void setImportSuccessful(Boolean importSuccessful) {
        this.importSuccessful = importSuccessful;
    }
    
    /**
     * Despite being public this method should not be called outside of 
     * eclipse.core module.
     */
    public void updateVariableValue(String value) {
        this.properties.put(ATTRIBUTE_PATH, value);
    }
    
    /**
     * Despite being public this method should not be called outside of 
     * eclipse.core module.
     */
    public void updateSourcePath(String value) {
        this.properties.put(ATTRIBUTE_SOURCEPATH, value);
    }
    
    /**
     * Despite being public this method should not be called outside of 
     * eclipse.core module.
     */
    public void updateJavadoc(String value) {
        if (value == null) {
            this.properties.remove(ATTRIBUTE_JAVADOC);
        } else {
            this.properties.put(ATTRIBUTE_JAVADOC, value);
        }
    }
    
}
