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
import java.util.EnumSet;
import java.util.function.Consumer;
import org.netbeans.api.j2ee.core.DeploymentDescriptors;
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

    static final String RESOURCE_FOLDER = "/org/netbeans/modules/j2ee/common/dd/resources/"; //NOI18N

    static Consumer<MakeFileCopy> atomicActionRunner = FileUtil::runAtomicAction;

    static MakeFileCopyFactory makeFileCopyFactory = MakeFileCopy::new;

    private static final EnumSet<Profile> WEBXML_REQUIREMENTS_INDEPENDANT_PROFILES = EnumSet.of(
            Profile.JAVA_EE_5,
            Profile.J2EE_14,
            Profile.J2EE_13
    );

    private DDHelper() {
    }

    /**
     * Creates web.xml deployment descriptor.
     * @param eeProfile Java EE/Jakarta EE profile to specify which version of web.xml should be created
     * @param dir Directory where web.xml should be created
     * @return web.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebXml(Profile eeProfile, FileObject dir) throws IOException {
        return createWebXml(eeProfile, true, dir);
    }

    /**
     * Creates web.xml deployment descriptor.
     * @param eeProfile Java EE/Jakarta EE profile to specify which version of web.xml should be created
     * @param webXmlRequired true if web.xml should be created also for profiles where it is not required
     * @param dir Directory where web.xml should be created
     * @return web.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebXml(Profile eeProfile, boolean webXmlRequired, FileObject dir) throws IOException {
        if (!webXmlRequired && !WEBXML_REQUIREMENTS_INDEPENDANT_PROFILES.contains(eeProfile)) {
            return null;
        }

        return createDeploymentDescriptorFromResource(eeProfile, DeploymentDescriptors.Type.WEB, "web-%s.xml", dir, "web.xml");
    }

    /**
     * Creates web-fragment.xml deployment descriptor.
     * @param eeProfile Java EE/Jakarta EE profile to specify which version of web-fragment.xml should be created
     * @param dir Directory where web-fragment.xml should be created
     * @return web-fragment.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebFragmentXml(Profile eeProfile, FileObject dir) throws IOException {
        return createDeploymentDescriptorFromResource(eeProfile, DeploymentDescriptors.Type.WEB_FRAGMENT, "web-fragment-%s.xml", dir, "web-fragment.xml");
    }

    /**
     * Creates beans.xml deployment descriptor.
     * @param eeProfile Java EE/Jakarta EE profile to specify which version of beans.xml should be created
     * @param dir Directory where beans.xml should be created
     * @return beans.xml file as FileObject
     * @throws java.io.IOException
     * @since 1.49
     */
    public static FileObject createBeansXml(Profile eeProfile, FileObject dir) throws IOException {
        return createBeansXml(eeProfile, dir, "beans");
    }

    /**
     * Creates beans.xml deployment descriptor.
     * @param eeProfile Java EE/Jakarta EE profile to specify which version of beans.xml should be created
     * @param dir Directory where beans.xml should be created
     * @param name name of configuration file to create; should be always "beans" for now
     * @return beans.xml file as FileObject
     * @throws java.io.IOException
     * @since 1.49
     */
    public static FileObject createBeansXml(Profile eeProfile, FileObject dir, String name) throws IOException {
        return createDeploymentDescriptorFromResource(eeProfile, DeploymentDescriptors.Type.BEANS, "beans-%s.xml", dir, name + ".xml");
    }

    /**
     * Created validation.xml deployment descriptor
     * @param eeProfile Java EE/Jakarta EE profile
     * @param dir Directory where validation.xml should be created
     * @return validation.xml file as FileObject
     * @throws IOException
     * @since 1.52
     */
    public static FileObject createValidationXml(Profile eeProfile, FileObject dir) throws IOException {
        return createValidationXml(eeProfile, dir, "validation");
    }

    /**
     * Created validation.xml deployment descriptor
     * @param eeProfile Java EE/Jakarta EE profile
     * @param dir Directory where validation.xml should be created
     * @param name name of configuration file to create;
     * @return validation.xml file as FileObject
     * @throws IOException
     * @since 1.52
     */
    public static FileObject createValidationXml(Profile eeProfile, FileObject dir, String name) throws IOException {
        return createDeploymentDescriptorFromResource(eeProfile, DeploymentDescriptors.Type.VALIDATION, "validation-%s.xml", dir, name + ".xml");
    }

    /**
     * Created Constraint declaration deployment descriptor
     * @param eeProfile Java EE/Jakarta EE profile
     * @param dir Directory where constraint.xml should be created
     * @return validation.xml file as FileObject
     * @throws IOException
     * @since 1.52
     */
    public static FileObject createConstraintXml(Profile eeProfile, FileObject dir) throws IOException {
        return createValidationXml(eeProfile, dir, "constraint");
    }

    /**
     * Created Constraint declaration deployment descriptor
     * @param eeProfile Java EE/Jakarta EE profile
     * @param dir Directory where constraint.xml should be created
     * @param name name of configuration file to create;
     * @return validation.xml file as FileObject
     * @throws IOException
     * @since 1.52
     */
    public static FileObject createConstraintXml(Profile eeProfile, FileObject dir, String name) throws IOException {
        return createDeploymentDescriptorFromResource(eeProfile, DeploymentDescriptors.Type.CONSTRAINT, "constraint-%s.xml", dir, name + ".xml");
    }

    /**
     * Generate EAR deployment descriptor (<i>application.xml</i>) if needed or forced (applies for JAVA EE 5).
     * <p>
     * For J2EE 1.4 or older the deployment descriptor is always generated if missing.
     * For JAVA EE 5 it is only generated if missing and forced as well.
     * @param eeProfile J2EE profile.
     * @param dir Configuration directory.
     * @param forceCreation if <code>true</code> <i>application.xml</i> is generated even if it's not needed
     * @return {@link FileObject} of the deployment descriptor or <code>null</code>.
     * @throws java.io.IOException if any error occurs.
     * @since 1.84
     */
    public static FileObject createApplicationXml(final Profile eeProfile, final FileObject dir,
            boolean forceCreation) throws IOException {
        if (!forceCreation && (eeProfile != Profile.J2EE_14 && eeProfile != Profile.J2EE_13)) {
            return null;
        }

        return createDeploymentDescriptorFromResource(eeProfile, DeploymentDescriptors.Type.EAR, "ear-%s.xml", dir, "application.xml");
    }
    
    private static FileObject createDeploymentDescriptorFromResource(Profile eeProfile, DeploymentDescriptors.Type type, String filenamePattern, FileObject toDir, String toFile) throws IOException {
        String deploymentDescriptorVersion = eeProfile.getDeploymentDescriptors().get(type);
        if (deploymentDescriptorVersion == null) {
            return null;
        }

        String template = String.format(filenamePattern, deploymentDescriptorVersion);
        MakeFileCopy action = makeFileCopyFactory.build(RESOURCE_FOLDER + template, toDir, toFile);
        atomicActionRunner.accept(action);
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

    @FunctionalInterface
    static interface MakeFileCopyFactory {

        MakeFileCopy build(String resource, FileObject toDir, String toFile);
    }

    // -------------------------------------------------------------------------
    static class MakeFileCopy implements Runnable {
        private final String fromFile;
        private final FileObject toDir;
        private final String toFile;

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
