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
     * @param j2eeProfile Java EE/Jakarta EE profile to specify which version of web.xml should be created
     * @param dir Directory where web.xml should be created
     * @return web.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebXml(Profile j2eeProfile, FileObject dir) throws IOException {
        return createWebXml(j2eeProfile, true, dir);
    }

    /**
     * Creates web.xml deployment descriptor.
     * @param j2eeProfile Java EE/Jakarta EE profile to specify which version of web.xml should be created
     * @param webXmlRequired true if web.xml should be created also for profiles where it is not required
     * @param dir Directory where web.xml should be created
     * @return web.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebXml(Profile j2eeProfile, boolean webXmlRequired, FileObject dir) throws IOException {
        String template = null;
        if ((Profile.JAKARTA_EE_11_FULL == j2eeProfile || Profile.JAKARTA_EE_11_WEB == j2eeProfile) && webXmlRequired) {
            template = "web-6.1.xml"; //NOI18N
        } else if ((Profile.JAKARTA_EE_10_FULL == j2eeProfile || Profile.JAKARTA_EE_10_WEB == j2eeProfile) && webXmlRequired) {
            template = "web-6.0.xml"; //NOI18N
        } else if ((Profile.JAKARTA_EE_9_1_FULL == j2eeProfile || Profile.JAKARTA_EE_9_1_WEB == j2eeProfile ||
                Profile.JAKARTA_EE_9_FULL == j2eeProfile || Profile.JAKARTA_EE_9_WEB == j2eeProfile) && webXmlRequired) {
            template = "web-5.0.xml"; //NOI18N
        } else if ((Profile.JAKARTA_EE_8_FULL == j2eeProfile || Profile.JAKARTA_EE_8_WEB == j2eeProfile ||
                Profile.JAVA_EE_8_FULL == j2eeProfile || Profile.JAVA_EE_8_WEB == j2eeProfile) && webXmlRequired) {
            template = "web-4.0.xml"; //NOI18N
        } else if ((Profile.JAVA_EE_7_FULL == j2eeProfile || Profile.JAVA_EE_7_WEB == j2eeProfile) && webXmlRequired) {
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
     * @param j2eeProfile Java EE/Jakarta EE profile to specify which version of web-fragment.xml should be created
     * @param dir Directory where web-fragment.xml should be created
     * @return web-fragment.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebFragmentXml(Profile j2eeProfile, FileObject dir) throws IOException {
        String template = null;
        if (Profile.JAKARTA_EE_11_FULL == j2eeProfile || Profile.JAKARTA_EE_11_WEB == j2eeProfile) {
            template = "web-fragment-6.1.xml"; //NOI18N
        } else if (Profile.JAKARTA_EE_10_FULL == j2eeProfile || Profile.JAKARTA_EE_10_WEB == j2eeProfile) {
            template = "web-fragment-6.0.xml"; //NOI18N
        } else if (Profile.JAKARTA_EE_9_1_FULL == j2eeProfile || Profile.JAKARTA_EE_9_1_WEB == j2eeProfile ||
                Profile.JAKARTA_EE_9_FULL == j2eeProfile || Profile.JAKARTA_EE_9_WEB == j2eeProfile) {
            template = "web-fragment-5.0.xml"; //NOI18N
        } else if (Profile.JAKARTA_EE_8_FULL == j2eeProfile || Profile.JAKARTA_EE_8_WEB == j2eeProfile ||
                Profile.JAVA_EE_8_FULL == j2eeProfile || Profile.JAVA_EE_8_WEB == j2eeProfile) {
            template = "web-fragment-4.0.xml"; //NOI18N
        } else if (Profile.JAVA_EE_7_FULL == j2eeProfile || Profile.JAVA_EE_7_WEB == j2eeProfile) {
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
     * @param j2eeProfile Java EE/Jakarta EE profile to specify which version of beans.xml should be created
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
     * @param j2eeProfile Java EE/Jakarta EE profile to specify which version of beans.xml should be created
     * @param dir Directory where beans.xml should be created
     * @param name name of configuration file to create; should be always "beans" for now
     * @return beans.xml file as FileObject
     * @throws java.io.IOException
     * @since 1.49
     */
    public static FileObject createBeansXml(Profile j2eeProfile, FileObject dir, String name) throws IOException {
        String template = null;       
        if (Profile.JAKARTA_EE_11_FULL == j2eeProfile || Profile.JAKARTA_EE_11_WEB == j2eeProfile) {
            template = "beans-4.1.xml"; //NOI18N
        } else if (Profile.JAKARTA_EE_10_FULL == j2eeProfile || Profile.JAKARTA_EE_10_WEB == j2eeProfile) {
            template = "beans-4.0.xml"; //NOI18N
        } else if (Profile.JAKARTA_EE_9_1_FULL == j2eeProfile || Profile.JAKARTA_EE_9_1_WEB == j2eeProfile ||
                Profile.JAKARTA_EE_9_FULL == j2eeProfile || Profile.JAKARTA_EE_9_WEB == j2eeProfile) {
            template = "beans-3.0.xml"; //NOI18N
        } else if (Profile.JAKARTA_EE_8_FULL == j2eeProfile || Profile.JAKARTA_EE_8_WEB == j2eeProfile ||
                Profile.JAVA_EE_8_FULL == j2eeProfile || Profile.JAVA_EE_8_WEB == j2eeProfile) {
            template = "beans-2.0.xml"; //NOI18N
        } else if (Profile.JAVA_EE_7_FULL == j2eeProfile || Profile.JAVA_EE_7_WEB == j2eeProfile) {
            template = "beans-1.1.xml"; //NOI18N
        } else if (Profile.JAVA_EE_6_FULL == j2eeProfile || Profile.JAVA_EE_6_WEB == j2eeProfile) {
            template = "beans-1.0.xml"; //NOI18N
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
     * @param j2eeProfile Java EE/Jakarta EE profile
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
     * @param j2eeProfile Java EE/Jakarta EE profile
     * @param dir Directory where validation.xml should be created
     * @param name name of configuration file to create;
     * @return validation.xml file as FileObject
     * @throws IOException
     * @since 1.52
     */
    public static FileObject createValidationXml(Profile j2eeProfile, FileObject dir, String name) throws IOException {
        String template = null;
        if (Profile.JAVA_EE_6_FULL == j2eeProfile || Profile.JAVA_EE_6_WEB == j2eeProfile) {
            template = "validation.xml"; //NOI18N
        } else if (Profile.JAVA_EE_7_FULL == j2eeProfile || Profile.JAVA_EE_7_WEB == j2eeProfile) {
            template = "validation-1.1.xml"; //NOI18N
        } else if (Profile.JAVA_EE_8_FULL == j2eeProfile || Profile.JAVA_EE_8_WEB == j2eeProfile
                || Profile.JAKARTA_EE_8_FULL == j2eeProfile || Profile.JAKARTA_EE_8_WEB == j2eeProfile) {
            template = "validation-2.0.xml"; //NOI18N
        } else if (Profile.JAKARTA_EE_9_FULL == j2eeProfile || Profile.JAKARTA_EE_9_WEB == j2eeProfile
                || Profile.JAKARTA_EE_9_1_FULL == j2eeProfile || Profile.JAKARTA_EE_9_1_WEB == j2eeProfile
                || Profile.JAKARTA_EE_10_FULL == j2eeProfile || Profile.JAKARTA_EE_10_WEB == j2eeProfile) {
            template = "validation-3.0.xml"; //NOI18N
        } else if (Profile.JAKARTA_EE_11_FULL == j2eeProfile || Profile.JAKARTA_EE_11_WEB == j2eeProfile) {
            template = "validation-3.1.xml"; //NOI18N
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
     * @param j2eeProfile Java EE/Jakarta EE profile
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
     * @param j2eeProfile Java EE/Jakarta EE profile
     * @param dir Directory where constraint.xml should be created
     * @param name name of configuration file to create;
     * @return validation.xml file as FileObject
     * @throws IOException
     * @since 1.52
     */
    public static FileObject createConstraintXml(Profile j2eeProfile, FileObject dir, String name) throws IOException {
        String template = null;
        if (Profile.JAVA_EE_6_FULL == j2eeProfile || Profile.JAVA_EE_6_WEB == j2eeProfile) {
            template = "constraint.xml"; //NOI18N
        } else if (Profile.JAVA_EE_7_FULL == j2eeProfile || Profile.JAVA_EE_7_WEB == j2eeProfile) {
            template = "constraint-1.1.xml"; //NOI18N
        } else if (Profile.JAVA_EE_8_FULL == j2eeProfile || Profile.JAVA_EE_8_WEB == j2eeProfile
                || Profile.JAKARTA_EE_8_FULL == j2eeProfile || Profile.JAKARTA_EE_8_WEB == j2eeProfile) {
            template = "constraint-2.0.xml"; //NOI18N
        } else if (Profile.JAKARTA_EE_9_FULL == j2eeProfile || Profile.JAKARTA_EE_9_WEB == j2eeProfile
                || Profile.JAKARTA_EE_9_1_FULL == j2eeProfile || Profile.JAKARTA_EE_9_1_WEB == j2eeProfile
                || Profile.JAKARTA_EE_10_FULL == j2eeProfile || Profile.JAKARTA_EE_10_WEB == j2eeProfile) {
            template = "constraint-3.0.xml"; //NOI18N
        } else if (Profile.JAKARTA_EE_11_FULL == j2eeProfile || Profile.JAKARTA_EE_11_WEB == j2eeProfile) {
            template = "constraint-3.1.xml"; //NOI18N
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
        
        if (profile != null && profile.equals(Profile.JAKARTA_EE_11_FULL) && forceCreation) {
            template = "ear-11.xml"; // NOI18N
        } else if (profile != null && profile.equals(Profile.JAKARTA_EE_10_FULL) && forceCreation) {
            template = "ear-10.xml"; // NOI18N
        } else if (profile != null && (profile.equals(Profile.JAKARTA_EE_9_FULL) || profile.equals(Profile.JAKARTA_EE_9_1_FULL)) && forceCreation) {
            template = "ear-9.xml"; // NOI18N
        } else if (profile != null && (profile.equals(Profile.JAKARTA_EE_8_FULL) || profile.equals(Profile.JAVA_EE_8_FULL)) && forceCreation) {
            template = "ear-8.xml"; // NOI18N
        } else if (profile != null && profile.equals(Profile.JAVA_EE_7_FULL) && forceCreation) {
            template = "ear-7.xml"; // NOI18N
        } else if (profile != null && profile.equals(Profile.JAVA_EE_6_FULL) && forceCreation) {
            template = "ear-6.xml"; // NOI18N
        } else if (Profile.JAVA_EE_5.equals(profile) && forceCreation) {
            template = "ear-5.xml"; // NOI18N
        } else if (Profile.J2EE_14.equals(profile) || Profile.J2EE_13.equals(profile)) {
            template = "ear-1.4.xml"; // NOI18N
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
        return ear != null && Profile.J2EE_14.equals(ear.getJ2eeProfile());
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

        @Override
        public void run() {
            try {
                // PENDING : should be easier to define in layer and copy related FileObject (doesn't require systemClassLoader)
                if (toDir.getFileObject(toFile) != null) {
                    return; // #229533, #189768: The file already exists in the file system --> Simply do nothing
                }
                FileObject xml = FileUtil.createData(toDir, toFile);
                String content = readResource(DDHelper.class.getResourceAsStream(fromFile));
                if (content != null) {
                    try (FileLock lock = xml.lock();
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(xml.getOutputStream(lock)))) {
                        bw.write(content);
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
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append(lineSep);
                    line = br.readLine();
                }
            }
            return sb.toString();
        }
    }

}
