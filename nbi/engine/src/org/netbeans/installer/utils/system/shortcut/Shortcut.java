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

package org.netbeans.installer.utils.system.shortcut;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.netbeans.installer.utils.StringUtils;

public abstract class Shortcut {
    private Map<Locale, String> names;
    private Map<Locale, String> descriptions;
    
    private String relativePath;
    private String fileName;
    
    
    private File workingDirectory;
    private File icon;
    private int iconIndex;
    
    private String[] categories;
    
    private String path;

    private Properties properties;
    
   
    protected Shortcut(final String name) {
        setNames(new HashMap<Locale, String>());
        setDescriptions(new HashMap<Locale, String>());
        setName(name);
        setCategories(new String [] {});
        setAdditionalProperties(new Properties());
    }
    public Map<Locale, String> getNames() {
        return names;
    }
    
    public void setNames(final Map<Locale, String> names) {
        this.names = names;
    }
    
    public String getName() {
        return getName(Locale.getDefault());
    }
    
    public void setName(final String name) {
        setName(name, Locale.getDefault());
    }
    
    public String getName(final Locale locale) {
        return StringUtils.getLocalizedString(names,locale);
    }
    
    public void setName(final String name, final Locale locale) {
        if (name != null) {
            names.put(locale, name);
        } else {
            names.remove(locale);
        }
    }
    
    public Map<Locale, String> getDescriptions() {
        return descriptions;
    }
    
    public void setDescriptions(final Map<Locale, String> comments) {
        this.descriptions = comments;
    }
    
    public String getDescription() {
        return getDescription(Locale.getDefault());
    }
    
    public void setDescription(final String description) {
        setDescription(description, Locale.getDefault());
    }
    
    public String getDescription(final Locale locale) {
        return StringUtils.getLocalizedString(descriptions,locale);
    }
    
    public void setDescription(final String description, final Locale locale) {
        descriptions.put(locale, description);
    }
    
    public String getRelativePath() {
        return relativePath;
    }
    
    public void setRelativePath(final String relativePath) {
        this.relativePath = relativePath;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }    
    public abstract String getTargetPath();
    
    public File getWorkingDirectory() {
        return workingDirectory;
    }
    
    public String getWorkingDirectoryPath() {
        if (workingDirectory != null) {
            return workingDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }
    
    public void setWorkingDirectory(final File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    
    public File getIcon() {
        return icon;
    }
    
    public String getIconPath() {
        if (icon != null) {
            return icon.getAbsolutePath();
        } else {
            return null;
        }
    }
    
    public void setIcon(final File icon) {
        this.icon = icon;
    }
    
    public void setIcon(final File icon, int index) {
        setIcon(icon);
        setIconIndex(index);
    }
    
    
    public String[] getCategories() {
        return categories;
    }
    
    public void setCategories(final String[] categories) {
        this.categories = categories;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(final String path) {
        this.path = path;
    }

    public int getIconIndex() {
        return iconIndex;
    }

    public void setIconIndex(int iconIndex) {
        this.iconIndex = iconIndex;
    }
    public Properties getAdditionalProperties() {
        return properties;
    }

    public void setAdditionalProperties(final Properties props) {
        this.properties = props;
    }

    public void setAdditionalProperty(final String propName, final String propValue) {
        properties.put(propName, propValue);
    }
}
