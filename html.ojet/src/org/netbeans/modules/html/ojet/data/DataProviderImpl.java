/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.ojet.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.ojet.OJETUtils;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class DataProviderImpl extends DataProvider {

    private static DataProviderImpl instance = null;
    private static final String zipFolder = "docs";
    private static final String ZIP_PREFIX = "ojetdocs-";
    private static final String ZIP_EXTENSION = ".zip";
    protected static final String DEFAULT_VERSION = "2.0.0";
    private static final HashMap<String, DataItemImpl.DataItemComponent> data = new HashMap();
    private static DataItemImpl.DataItemModule moduleData = null;
    private static FileObject docRoot = null;
    private static String currentVersion;

    synchronized public static DataProvider getInstance() {
        if (instance == null) {
            instance = new DataProviderImpl();
            currentVersion = DEFAULT_VERSION;
        }
        if (data.isEmpty()) {
            File zipFile = InstalledFileLocator.getDefault().locate(zipFolder + "/" + codeFileNameFromVersion(currentVersion), "org.netbeans.modules.html.ojet", false); //NOI18N
            if (zipFile != null && zipFile.exists()) {
                docRoot = FileUtil.toFileObject(zipFile);
                docRoot = FileUtil.getArchiveRoot(docRoot);
                if (docRoot != null) {
                    FileObject folder = docRoot.getFileObject("docs"); // NOI18N
                    if (folder != null && folder.isValid()) {
                        for (FileObject child : folder.getChildren()) {
                            String name = child.getName();
                            if (name.startsWith("oj.oj")) {
                                name = name.substring(3);
                                data.put(name, new DataItemImpl.DataItemComponent(name, child.toURL().toString()));
                            } else if (OJETUtils.OJ_MODULE.equals(name)) {
                                moduleData = new DataItemImpl.DataItemModule(child.toURL().toString());
                            }
                        }
                    }
                }
            }
        }
        return instance;
    }

    @Override
    public Collection<DataItem> getBindingOptions() {
        List<DataItem> result = new ArrayList(1);
        result.add((new DataItemImpl(OJETUtils.OJ_COMPONENT, null, OJETUtils.OJ_COMPONENT + ": {component: }"))); // NOI18N
        result.add(new DataItemImpl(OJETUtils.OJ_MODULE, null, OJETUtils.OJ_MODULE + ": "));
        return result;
    }

    @Override
    public Collection<DataItem> getComponents() {
        List<DataItem> result = new ArrayList<>();
        for (DataItem component : data.values()) {
            result.add(component);
        }
        return result;
    }

    @Override
    public Collection<DataItem> getComponentOptions(String compName) {
        DataItemImpl.DataItemComponent component = data.get(compName);
        if (component != null) {
            return component.getOptions();
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<DataItem> getModuleProperties() {
        if (moduleData != null) {
            return moduleData.getProperies();
        }
        return Collections.emptyList(); 
    }

    
    @Override
    public Collection<DataItem> getComponentEvents(String compName) {
        DataItemImpl.DataItemComponent component = data.get(compName);
        if (component != null) {
            return component.getEvents();
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getAvailableVersions() {
        File folder = InstalledFileLocator.getDefault().locate(zipFolder, "org.netbeans.modules.html.ojet", false); //NOI18N
        List<String> versions = new ArrayList();
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.startsWith(ZIP_PREFIX) && fileName.endsWith(ZIP_EXTENSION)) {
                    versions.add(decodeVersionFromFileName(fileName));
                }
            }
        }
        return versions;
    }

    private static String decodeVersionFromFileName(String name) {
        String version = name.substring(ZIP_PREFIX.length());
        version = version.substring(0, version.length() - ZIP_EXTENSION.length());
        version = version.replace('_', '.');
        return version;
    }
    
    private static String codeFileNameFromVersion(String version) {
        StringBuilder sb = new StringBuilder();
        sb.append(ZIP_PREFIX);
        sb.append(version.replace('.', '_'));
        sb.append(ZIP_EXTENSION);
        return sb.toString();
    }
    
    @Override
    public String getCurrentVersion() {
        return currentVersion;
    }

    @Override
    public void setCurrentVersion(String version) {
        if (!getAvailableVersions().contains(version)) {
            throw new IllegalArgumentException(version + " is unknown version");
        } 
        if (!currentVersion.equals(version)) {
            currentVersion = version;
            // reset the cache
            data.clear();
        }
    }

    @Override
    @NbBundle.Messages("label_ojet=OJET")
    public Collection<JsObject> getGlobalObjects(ModelElementFactory factory) {
        JsFunction global = factory.newGlobalObject(null, 0);
        JsObject oj = factory.newObject(global, "oj", OffsetRange.NONE, true, Bundle.label_ojet()); //NOI18N
        factory.putGlobalProperty(global, oj);
        return Collections.singletonList((JsObject)global);
    }
}
