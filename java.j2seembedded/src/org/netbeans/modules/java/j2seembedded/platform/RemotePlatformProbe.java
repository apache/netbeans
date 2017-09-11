/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seembedded.platform;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.WizardValidationException;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 * @author Roman Svitanic
 */
public final class RemotePlatformProbe {

    private static final String NB_PROP_PREFIX = "netbeans.";   //NOI18N
    private static final String PLATFROM_PROP_PREFIX = "platform.";  //NOI18N

    private RemotePlatformProbe() {
        throw new IllegalStateException();
    }
    
    public static File createBuildScript() {
        final String resourcesPath = "org/netbeans/modules/java/j2seembedded/resources/validateconnection.xml"; //NOI18N        
        File buildScript = null;
        try {
            buildScript = FileUtil.normalizeFile(File.createTempFile("antScript", ".xml")); //NOI18N
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        try (InputStream inputStream = RemotePlatformProbe.class.getClassLoader().getResourceAsStream(resourcesPath);
                OutputStream outputStream = new FileOutputStream(buildScript)) {
            FileUtil.copy(inputStream, outputStream);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return buildScript;
    }

    @NonNull
    public static Properties verifyPlatform(
        @NonNull final String jreLocation,
        @NullAllowed final String execDecorator,
        @NonNull final String workingDir,
        @NonNull final ConnectionMethod connectionMethod,
        @NullAllowed File buildScript) throws WizardValidationException {
        String[] antTargets = null;
            final Properties prop = new Properties();
            prop.setProperty("remote.host", connectionMethod.getHost()); //NOI18N
            prop.setProperty("remote.port", String.valueOf(connectionMethod.getPort())); //NOI18N
            prop.setProperty("remote.username", connectionMethod.getAuthentification().getUserName()); //NOI18N
            prop.setProperty("remote.platform.home", jreLocation); //NOI18N
            if (execDecorator != null) {
                prop.setProperty("remote.exec.decorator", execDecorator);   //NOI18N
            }
            prop.setProperty("remote.working.dir", workingDir.length() > 0 ? workingDir : "/home/" + connectionMethod.getAuthentification().getUserName() + "/NetBeansProjects/"); //NOI18N
            final File probe = InstalledFileLocator.getDefault().locate("modules/ext/org-netbeans-modules-java-j2seembedded-probe.jar", "org.netbeans.modules.java.j2seembedded", false);   //NOI18N
            if (probe == null) {
                throw new WizardValidationException(
                    null,
                    NbBundle.getMessage(RemotePlatformProbe.class, "MSG_MissingProbe"),
                    null);
            }
            prop.setProperty("probe.file", probe.getAbsolutePath());
            File platformProperties = null;            
            ExecutorTask executorTask = null;
            int antResult = -1;
            try {
                platformProperties = File.createTempFile("platform", ".properties");   //NOI18N
                prop.setProperty("platform.properties.file", platformProperties.getAbsolutePath()); //NOI18N
                final Set<String> concealedProps;
                if (connectionMethod.getAuthentification().getKind() == ConnectionMethod.Authentification.Kind.PASSWORD) {
                    antTargets = new String[]{"connect-ssh-password"}; //NOI18N
                    prop.setProperty("remote.password", ((ConnectionMethod.Authentification.Password)connectionMethod.getAuthentification()).getPassword()); //NOI18N
                    concealedProps = Collections.singleton("remote.password");  //NOI18N
                } else {
                    antTargets = new String[]{"connect-ssh-keyfile"}; //NOI18N
                    prop.setProperty("keystore.file", ((ConnectionMethod.Authentification.Key)connectionMethod.getAuthentification()).getKeyStore().getAbsolutePath()); //NOI18N
                    prop.setProperty("keystore.passphrase", ((ConnectionMethod.Authentification.Key)connectionMethod.getAuthentification()).getPassPhrase()); //NOI18N
                    concealedProps = Collections.singleton("keystore.passphrase");  //NOI18N
                }

                final FileObject antScript = FileUtil.toFileObject(buildScript != null && buildScript.exists() ? buildScript : createBuildScript());
                executorTask = ActionUtils.runTarget(antScript, antTargets, prop, concealedProps);
                antResult = executorTask.result();
                if (antResult != 0) {
                    throw new WizardValidationException(
                        null,
                        NbBundle.getMessage(RemotePlatformProbe.class, "LBL_ConnectionError"),
                        null);
                }
                final Properties props = new Properties();
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(platformProperties))) {
                    props.load(in);
                }
                return props;
            } catch (IllegalArgumentException | IOException ex) {
                Exceptions.printStackTrace(ex);
                throw new WizardValidationException(
                    null,
                    ex.getMessage(),
                    ex.getLocalizedMessage());
            } finally {
                if (antResult == 0 && executorTask != null) {
                    executorTask.getInputOutput().closeInputOutput();
                }
                if (buildScript != null) {
                    buildScript.delete();
                }
                if (platformProperties != null) {
                    platformProperties.delete();
                }
            }
    }

    @NonNull
    public static Pair<Map<String,String>,Map<String,String>> getSystemProperties(@NonNull final Properties p) {
        final Map<String,String> sysProps = new HashMap<>();
        final Map<String,String> properties = new HashMap<>();
        for (Map.Entry<Object,Object> e : p.entrySet()) {
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            if (key.startsWith(NB_PROP_PREFIX)) {
                properties.put(
                    String.format(
                        "%s%s", //NOI18N
                        PLATFROM_PROP_PREFIX,
                        key.substring(NB_PROP_PREFIX.length())),
                    value);
            } else {
                sysProps.put(key, value);
            }
        }
        return Pair.<Map<String,String>,Map<String,String>>of(properties,sysProps);
    }

    public static int uploadJRE(
            @NonNull final String localJreLocation,
            @NonNull final String remoteJreLocation,
            @NonNull final ConnectionMethod connectionMethod,
            @NullAllowed File buildScript) {
        String[] antTargets = null;
        final Properties prop = new Properties();
        prop.setProperty("remote.host", connectionMethod.getHost()); //NOI18N
        prop.setProperty("remote.port", String.valueOf(connectionMethod.getPort())); //NOI18N
        prop.setProperty("remote.username", connectionMethod.getAuthentification().getUserName()); //NOI18N
        prop.setProperty("remote.jre.dir", remoteJreLocation); //NOI18N
        prop.setProperty("jre.dir", localJreLocation); //NOI18N
        ExecutorTask executorTask = null;
        Set<String> concealedProps;
        if (connectionMethod.getAuthentification().getKind() == ConnectionMethod.Authentification.Kind.PASSWORD) {
            antTargets = new String[]{"upload-JRE-password"}; //NOI18N
            prop.setProperty("remote.password", ((ConnectionMethod.Authentification.Password) connectionMethod.getAuthentification()).getPassword()); //NOI18N
            concealedProps = Collections.singleton("remote.password");  //NOI18N
        } else {
            antTargets = new String[]{"upload-JRE-keyfile"}; //NOI18N
            prop.setProperty("keystore.file", ((ConnectionMethod.Authentification.Key) connectionMethod.getAuthentification()).getKeyStore().getAbsolutePath()); //NOI18N
            prop.setProperty("keystore.passphrase", ((ConnectionMethod.Authentification.Key) connectionMethod.getAuthentification()).getPassPhrase()); //NOI18N
            concealedProps = Collections.singleton("keystore.passphrase");  //NOI18N
        }
        final FileObject antScript = FileUtil.toFileObject(buildScript != null ? buildScript : createBuildScript());
        try {
            executorTask = ActionUtils.runTarget(antScript, antTargets, prop, concealedProps);
        } catch (IOException | IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (executorTask != null) {
                executorTask.getInputOutput().closeInputOutput();
            }
        }
        return executorTask != null ? executorTask.result() : -1;
    }
}
