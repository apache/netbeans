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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.spi.J2SEPlatformDefaultSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(
        service =J2SEPlatformDefaultSources.class,
        position = 100,
        path = "org-netbeans-api-java/platform/j2seplatform/defaultSourcesProviders")
public class J2SEPlatformDefaultSourcesImpl implements J2SEPlatformDefaultSources {

    @Override
    @NonNull
    public List<URI> getDefaultSources(@NonNull final JavaPlatform platform) {
        final Collection<? extends FileObject> fos = platform.getInstallFolders();
        if (fos.isEmpty()) {
            return Collections.emptyList();
        }
        final File javaHome = FileUtil.toFile(fos.iterator().next());
        if (javaHome == null) {
            return Collections.emptyList();
        }
        return getSources(javaHome, platform.getSpecification().getVersion());
    }

    @NonNull
    private static List<URI> getSources (
            @NonNull final File javaHome,
            @NonNull final SpecificationVersion version) {
        try {
            File f;
            //On VMS, the root of the "src.zip" is "src", and this causes
            //problems with NetBeans 4.0. So use the modified "src.zip" shipped
            //with the OpenVMS NetBeans 4.0 kit.
            if (Utilities.getOperatingSystem() == Utilities.OS_VMS) {
                String srcHome =
                    System.getProperty("netbeans.openvms.j2seplatform.default.srcdir"); //NOI18N
                if (srcHome != null)
                    f = new File(srcHome, "src.zip");   //NOI18N
                else
                    f = new File (javaHome, "src.zip"); //NOI18N
            } else {
                //1st) ${java.home}/lib/src.zip - JDK 9
                final File lib = new File(javaHome, "lib");  //NOI18N
                f = new File (lib, "src.zip");          //NOI18N
                //2nd) ${java.home}/src.zip - older than JDK 9
                if (!f.exists()) {
                    f = new File (javaHome, "src.zip");    //NOI18N
                }
                //3rd) ${java.home}/src.jar - Apple JDK 1.6
                //If src.zip does not exist, try src.jar (it is on some platforms)
                if (!f.exists()) {
                    f = new File (javaHome, "src.jar");    //NOI18N
                }
            }
            if (f.exists() && f.canRead()) {
                URL url = FileUtil.getArchiveRoot(Utilities.toURI(f).toURL());
                List<URI> res = Collections.singletonList (url.toURI());
                if (Util.JDK9.compareTo(version)<=0) {
                    final FileObject fo = URLMapper.findFileObject(url);
                    if (fo.getFileObject("java.base") != null) {    //NOI18N
                        res = Arrays.stream(fo.getChildren())
                                .filter((m) -> m.isFolder())
                                .map((m) -> m.toURI())
                                .collect(Collectors.toList());
                    }
                } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                    //Test for src folder in the src.zip on Mac
                    FileObject fo = URLMapper.findFileObject(url);
                    if (fo != null) {
                        fo = fo.getFileObject("src");    //NOI18N
                        if (fo != null) {
                            url = fo.toURL();
                            res = Collections.singletonList (url.toURI());
                        }
                    }
                }
                return res;
            }
        } catch (MalformedURLException | URISyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        return Collections.emptyList();
    }

}
