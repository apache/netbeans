/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.net.MalformedURLException;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.platform.*;
import org.netbeans.api.java.classpath.*;

/**
 * Implementation of the "Default" platform. The information here is extracted
 * from the NetBeans' own runtime.
 *
 * @author Svata Dedic
 */
public class DefaultPlatformImpl extends J2SEPlatformImpl {


    public static final String DEFAULT_PLATFORM_ANT_NAME = "default_platform";           //NOI18N

    private ClassPath standardLibs;

    @SuppressWarnings("unchecked")  //Properties cast to Map<String,String>
    static JavaPlatform create(Map<String,String> properties, List<URL> sources, List<URL> javadoc) {
        if (properties == null) {
            properties = new HashMap<> ();
        }
        String  jdkHome = System.getProperty("jdk.home"); // NOI18N
        File javaHome;
        if (jdkHome == null) {
            javaHome = FileUtil.normalizeFile(new File(System.getProperty("java.home")).getParentFile()); // NOI18N
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
        Map<Object,Object> p = System.getProperties();
        synchronized (p) {
            p = new HashMap<>(p);
        }
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
