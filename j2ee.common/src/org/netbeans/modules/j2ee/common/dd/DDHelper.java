/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.common.dd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Support for creation of deployment descriptors
 * @author Petr Slechta
 */
public class DDHelper {

    private static final String RESOURCE_FOLDER = "/org/netbeans/modules/j2ee/common/dd/resources/"; //NOI18N

    private DDHelper() {
    }

    /**
     * Creates web.xml deployment descriptor.
     * @param j2eeProfile Java EE profile to specify which version of web.xml should be created
     * @param dir Directory where web.xml should be created
     * @return web.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebXml(Profile j2eeProfile, FileObject dir) throws IOException {
        return createWebXml(j2eeProfile, true, dir);
    }
    
    /**
     * Creates web.xml deployment descriptor.
     * @param j2eeProfile Java EE profile to specify which version of web.xml should be created
     * @param webXmlRequired true if web.xml should be created also for profiles where it is not required
     * @param dir Directory where web.xml should be created
     * @return web.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebXml(Profile j2eeProfile, boolean webXmlRequired, FileObject dir) throws IOException {
        String template = null;
        if ((Profile.JAVA_EE_7_FULL == j2eeProfile || Profile.JAVA_EE_7_WEB == j2eeProfile) && webXmlRequired) {
            template = "web-3.1.xml"; //NOI18N
        } else if ((Profile.JAVA_EE_6_FULL == j2eeProfile || Profile.JAVA_EE_6_WEB == j2eeProfile) && webXmlRequired) {
            template = "web-3.0.xml"; //NOI18N
        } else if (Profile.JAVA_EE_5 == j2eeProfile) {
            template = "web-2.5.xml"; //NOI18N
        } else if (Profile.J2EE_14 == j2eeProfile) {
            template = "web-2.4.xml"; //NOI18N
        } else if (Profile.J2EE_13 == j2eeProfile) {
            template = "web-2.3.xml"; //NOI18N
        }

        if (template == null)
            return null;

        MakeFileCopy action = new MakeFileCopy(RESOURCE_FOLDER + template, dir, "web.xml");
        FileUtil.runAtomicAction(action);
        if (action.getException() != null)
            throw action.getException();
        else
            return action.getResult();
    }

    /**
     * Creates web-fragment.xml deployment descriptor.
     * @param j2eeProfile Java EE profile to specify which version of web-fragment.xml should be created
     * @param dir Directory where web-fragment.xml should be created
     * @return web-fragment.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebFragmentXml(Profile j2eeProfile, FileObject dir) throws IOException {
        String template = null;
        if (Profile.JAVA_EE_7_FULL == j2eeProfile || Profile.JAVA_EE_7_WEB == j2eeProfile) {
            template = "web-fragment-3.1.xml"; //NOI18N
        } else if (Profile.JAVA_EE_6_FULL == j2eeProfile || Profile.JAVA_EE_6_WEB == j2eeProfile) {
            template = "web-fragment-3.0.xml"; //NOI18N
        }

        if (template == null)
            return null;

        MakeFileCopy action = new MakeFileCopy(RESOURCE_FOLDER + template, dir, "web-fragment.xml");
        FileUtil.runAtomicAction(action);
        if (action.getException() != null)
            throw action.getException();
        else
            return action.getResult();
    }
    
    /**
     * Creates beans.xml deployment descriptor.
     * @param j2eeProfile Java EE profile to specify which version of beans.xml should be created
     * @param dir Directory where beans.xml should be created
     * @return beans.xml file as FileObject
     * @throws java.io.IOException
     * @since 1.49
     */
    public static FileObject createBeansXml(Profile j2eeProfile, FileObject dir) throws IOException {
        return createBeansXml(j2eeProfile, dir, "beans");
    }

    /**
     * Creates beans.xml deployment descriptor.
     * @param j2eeProfile Java EE profile to specify which version of beans.xml should be created
     * @param dir Directory where beans.xml should be created
     * @param name name of configuration file to create; should be always "beans" for now
     * @return beans.xml file as FileObject
     * @throws java.io.IOException
     * @since 1.49
     */
    public static FileObject createBeansXml(Profile j2eeProfile, FileObject dir, String name) throws IOException {
        String template = null;
        if (Profile.JAVA_EE_6_FULL == j2eeProfile || Profile.JAVA_EE_6_WEB == j2eeProfile) {
            template = "beans-1.0.xml"; //NOI18N
        }
        if (Profile.JAVA_EE_7_FULL == j2eeProfile || Profile.JAVA_EE_7_WEB == j2eeProfile) {
            template = "beans-1.1.xml"; //NOI18N
        }

        if (template == null)
            return null;

        MakeFileCopy action = new MakeFileCopy(RESOURCE_FOLDER + template, dir, name+".xml");
        FileUtil.runAtomicAction(action);
        if (action.getException() != null)
            throw action.getException();
        else
            return action.getResult();
    }

    /**
     * Created validation.xml deployment descriptor
     * @param j2eeProfile Java EE profile
     * @param dir Directory where validation.xml should be created
     * @return validation.xml file as FileObject
     * @throws IOException
     * @since 1.52
     */
    public static FileObject createValidationXml(Profile j2eeProfile, FileObject dir) throws IOException {
        return createValidationXml(j2eeProfile, dir, "validation");
    }

    /**
     * Created validation.xml deployment descriptor
     * @param j2eeProfile Java EE profile
     * @param dir Directory where validation.xml should be created
     * @param name name of configuration file to create;
     * @return validation.xml file as FileObject
     * @throws IOException
     * @since 1.52
     */
    public static FileObject createValidationXml(Profile j2eeProfile, FileObject dir, String name) throws IOException {
        String template = null;
        if (Profile.JAVA_EE_6_FULL == j2eeProfile || Profile.JAVA_EE_6_WEB == j2eeProfile ||
                Profile.JAVA_EE_7_FULL == j2eeProfile || Profile.JAVA_EE_7_WEB == j2eeProfile) {
            template = "validation.xml"; //NOI18N
        }

        if (template == null)
            return null;

        MakeFileCopy action = new MakeFileCopy(RESOURCE_FOLDER + template, dir, name+".xml");
        FileUtil.runAtomicAction(action);
        if (action.getException() != null)
            throw action.getException();
        else
            return action.getResult();
    }

    /**
     * Created Constraint declaration deployment descriptor
     * @param j2eeProfile Java EE profile
     * @param dir Directory where constraint.xml should be created
     * @return validation.xml file as FileObject
     * @throws IOException
     * @since 1.52
     */
    public static FileObject createConstraintXml(Profile j2eeProfile, FileObject dir) throws IOException {
        return createValidationXml(j2eeProfile, dir, "constraint");
    }

    /**
     * Created Constraint declaration deployment descriptor
     * @param j2eeProfile Java EE profile
     * @param dir Directory where constraint.xml should be created
     * @param name name of configuration file to create;
     * @return validation.xml file as FileObject
     * @throws IOException
     * @since 1.52
     */
    public static FileObject createConstraintXml(Profile j2eeProfile, FileObject dir, String name) throws IOException {
        String template = null;
        if (Profile.JAVA_EE_6_FULL == j2eeProfile || Profile.JAVA_EE_6_WEB == j2eeProfile ||
                Profile.JAVA_EE_7_FULL == j2eeProfile || Profile.JAVA_EE_7_WEB == j2eeProfile) {
            template = "constraint.xml"; //NOI18N
        }

        if (template == null)
            return null;

        MakeFileCopy action = new MakeFileCopy(RESOURCE_FOLDER + template, dir, name+".xml");
        FileUtil.runAtomicAction(action);
        if (action.getException() != null)
            throw action.getException();
        else
            return action.getResult();
    }

    /**
     * Generate EAR deployment descriptor (<i>application.xml</i>) if needed or forced (applies for JAVA EE 5).
     * <p>
     * For J2EE 1.4 or older the deployment descriptor is always generated if missing.
     * For JAVA EE 5 it is only generated if missing and forced as well.
     * @param profile J2EE profile.
     * @param dir Configuration directory.
     * @param forceCreation if <code>true</code> <i>application.xml</i> is generated even if it's not needed
     * @return {@link FileObject} of the deployment descriptor or <code>null</code>.
     * @throws java.io.IOException if any error occurs.
     * @since 1.84
     */
    public static FileObject createApplicationXml(final Profile profile, final FileObject dir,
            boolean forceCreation) throws IOException {
        String template = null;
        if (Profile.J2EE_14.equals(profile) || Profile.J2EE_13.equals(profile)) {
            template = "ear-1.4.xml"; // NOI18N
        } else if (Profile.JAVA_EE_5.equals(profile) && forceCreation) {
            template = "ear-5.xml"; // NOI18N
        } else if (profile != null && profile.isAtLeast(Profile.JAVA_EE_7_WEB) && forceCreation) {
            template = "ear-7.xml"; // NOI18N
        } else if (profile != null && profile.isAtLeast(Profile.JAVA_EE_6_WEB) && forceCreation) {
            template = "ear-6.xml"; // NOI18N
        }

        if (template == null) {
            return null;
        }
        
        MakeFileCopy action = new MakeFileCopy(RESOURCE_FOLDER + template, dir, "application.xml");
        FileUtil.runAtomicAction(action);
        if (action.getException() != null) {
            throw action.getException();
        } else {
            return action.getResult();
        }
    }

    /**
     * Return <code>true</code> if deployment descriptor is compulsory for given EAR project.
     * @param project EAR project instance, shall include EarImplementation in it's lookup.
     * @return <code>true</code> if deployment descriptor is compulsory for given EAR project.
     * @since 1.84
     */
    public static boolean isApplicationXMLCompulsory(Project project) {
        assert project != null;
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null && provider.getJ2eeModule().getType() == J2eeModule.Type.EAR) {
            if (provider.getConfigSupport().isDescriptorRequired()) {
                return true;
            }
        }
        Ear ear = Ear.getEar(project.getProjectDirectory());
        if (ear != null && Profile.J2EE_14.equals(ear.getJ2eeProfile())) {
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    private static class MakeFileCopy implements Runnable {
        private String fromFile;
        private FileObject toDir;
        private String toFile;
        private IOException exception;
        private FileObject result;

        MakeFileCopy(String fromFile, FileObject toDir, String toFile) {
            this.fromFile = fromFile;
            this.toDir = toDir;
            this.toFile = toFile;
        }

        IOException getException() {
            return exception;
        }

        FileObject getResult() {
            return result;
        }

        public void run() {
            try {
                // PENDING : should be easier to define in layer and copy related FileObject (doesn't require systemClassLoader)
                if (toDir.getFileObject(toFile) != null) {
                    return; // #229533, #189768: The file already exists in the file system --> Simply do nothing
                }
                FileObject xml = FileUtil.createData(toDir, toFile);
                String content = readResource(DDHelper.class.getResourceAsStream(fromFile));
                if (content != null) {
                    FileLock lock = xml.lock();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(xml.getOutputStream(lock)));
                    try {
                        bw.write(content);
                    } finally {
                        bw.close();
                        lock.releaseLock();
                    }
                }
                result = xml;
            }
            catch (IOException e) {
                exception = e;
            }
        }

        private String readResource(InputStream is) throws IOException {
            StringBuilder sb = new StringBuilder();
            String lineSep = System.getProperty("line.separator"); // NOI18N
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append(lineSep);
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
            return sb.toString();
        }
    }

}
