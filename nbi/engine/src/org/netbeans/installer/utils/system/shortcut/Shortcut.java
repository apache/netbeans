/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
