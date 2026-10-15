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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.platform.*;

/**
 * Implementation of the "Default" platform. The information here is extracted
 * from the NetBeans' own runtime.
 *
 * @author Svata Dedic
 */
public class DefaultPlatformImpl extends J2SEPlatformImpl {


    public static final String DEFAULT_PLATFORM_ANT_NAME = "default_platform";           //NOI18N

    @SuppressWarnings("unchecked")  //Properties cast to Map<String,String>
    static JavaPlatform create(Map<String,String> properties, List<URL> sources, List<URL> javadoc) {
        if (properties == null) {
            properties = new HashMap<> ();
        }
        Map<Object,Object> p = System.getProperties();
        synchronized (p) {
            p = new HashMap<>(p);
        }
        String jdkHome = System.getProperty("jdk.home"); // NOI18N
        File javaHome;
        if (jdkHome == null || Files.isSymbolicLink(Paths.get(jdkHome, "bin", "java"))) {
            if (Util.getSpecificationVersion((String) p.get("java.specification.version")).compareTo(Util.JDK9) < 0) {
                javaHome = FileUtil.normalizeFile(new File(System.getProperty("java.home")).getParentFile()); // NOI18N
            } else {
                javaHome = FileUtil.normalizeFile(new File(System.getProperty("java.home"))); // NOI18N
            }
        } else {
            javaHome = FileUtil.normalizeFile(new File(jdkHome));
        }

        List<URL> installFolders = new ArrayList<> ();
        try {
            installFolders.add (Utilities.toURI(javaHome).toURL());
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        }
        final Map<String,String> systemProperties = new HashMap<>();
        for (Map.Entry<Object,Object> e : p.entrySet()) {
            final String key = (String) e.getKey();
            final String value = Util.fixSymLinks(
                    key,
                    Util.removeNBArtifacts(
                            key,
                            (String) e.getValue()),
                    Util.toFileObjects(installFolders));
            systemProperties.put(key, value);
        }
        return new DefaultPlatformImpl(installFolders, properties, systemProperties, sources, javadoc);
    }

    private DefaultPlatformImpl(List<URL> installFolders, Map<String,String> platformProperties,
        Map<String,String> systemProperties, List<URL> sources, List<URL> javadoc) {
        super(null,DEFAULT_PLATFORM_ANT_NAME,
              installFolders, platformProperties, systemProperties, sources, javadoc);
    }

    @Override
    public void setAntName(String antName) {
        throw new UnsupportedOperationException (); //Default platform ant name can not be changed
    }

    @Override
    public String getDisplayName () {
        String displayName = super.getDisplayName();
        if (displayName == null) {
            displayName = NbBundle.getMessage(DefaultPlatformImpl.class,"TXT_DefaultPlatform", getSpecification().getVersion().toString());
            this.internalSetDisplayName (displayName);
        }
        return displayName;
    }

    @Override
    public void setDisplayName(String name) {
        throw new UnsupportedOperationException (); //Default platform name can not be changed
    }

}
