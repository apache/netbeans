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


package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Helps to generate Ant deployment build scripts.
 *
 * @author sherold
 *
 * @since 1.18
 */
public final class AntDeploymentHelper {
    
    /**
     * Generates the Ant deployment build script for the given module type for 
     * the specified server instance to the specified file. If the specified 
     * serverInstanceID is null or no server instance of the specified ID exists 
     * a default deployment build script will be generated.
     * <p>
     * The Ant deployment build script requires the following properties to be
     * defined.
     * <ul>
     * <li><code>deploy.ant.properties.file</code> - Path to the server instance 
     * specific deployment properties file, see {@link #getDeploymentPropertiesFile}.
     * <li><code>deploy.ant.archive</code> - The deployable archive.
     * <li><code>deploy.ant.resource.dir</code> - The server resources directory.
     * <li><code>deploy.ant.enabled</code> - The Ant deployment targets should be
     * executed only if this property has been set.
     * </ul>
     * <p>
     * The Ant deployment build script is bound to provide the following targets.
     * <ul>
     * <li><code>-deploy-ant</code> - Deploys the deployable archive defined by the 
     * <code>deploy.ant.archive</code> property. If the deployable archive is a web 
     * module or an enterprise application with a web module the 
     * <code>deploy.ant.client.url</code> property is set by this target.
     * <li><code>-undeploy-ant</code> - Undeploys the deployable archive defined 
     * by the <code>deploy.ant.archive</code> property.
     * </ul>
     *
     * @param file the file to which the deployment build script will be generated.
     *             If the file does not exist, it will be created.
     * @param moduleType the module type the build script should handle. Use the
     *                   constants defined in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}.
     * @param serverInstanceID the server instance for which the build script will
     *                         be generated.
     * @throws IOException if a problem during generating the build script occurs.
     */
    public static void writeDeploymentScript(File file, Object moduleType, String serverInstanceID) 
    throws IOException {
        AntDeploymentProvider provider = null;
        if (serverInstanceID != null) {
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(serverInstanceID);
            if (si != null) {
                provider = si.getAntDeploymentProvider();
            }
        }

        FileObject fo = FileUtil.createData(FileUtil.normalizeFile(file));
        FileLock lock = fo.lock();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            InputStream stream = null;
            try {
                if (provider == null) {
                    InputStream is = ServerInstance.class.getResourceAsStream("resources/default-ant-deploy.xml"); // NOI18N
                    try {
                        FileUtil.copy(is, os);
                    } finally {
                        is.close();
                    }
                } else {
                    provider.writeDeploymentScript(os, moduleType);
                }

                if(file.exists() == true)
                {
                    stream = fo.getInputStream();
//                    ByteArrayInputStream stringStream = new ByteArrayInputStream(os.toString().getBytes());
                    ByteArrayInputStream stringStream = new ByteArrayInputStream(os.toByteArray());
                    try
                    {
                        if(isEqual(stream, stringStream) == false)
                        {
                            stream.close();
                            stringStream.reset();
                            OutputStream fileStream = fo.getOutputStream(lock);
                            try
                            {
                                FileUtil.copy(stringStream, fileStream);
                            }
                            finally
                            {
                                fileStream.close();
                            }
                        }
                        else
                        {
                            stream.close();
                        }
                    }
                    finally
                    {
                        stream.close();
                        stringStream.close();
                    }

                }
                else
                {

                    OutputStreamWriter writer = null;
                    try
                    {
                        OutputStream fileOS = fo.getOutputStream(lock);
                        writer = new OutputStreamWriter(fileOS, "UTF-8"); // NOI18N
                        String outString = os.toString("UTF-8"); // NOI18N
                        writer.write(outString, 0, outString.length());
                    }
                    finally
                    {
                        if(writer != null)
                        {
                            writer.close();
                        }
                    }
                }

            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /**
     * Returns the server instance specific deployment properties file used by 
     * the deployment build script generated by the {@link #writeDeploymentScript(File,Object,String)}. 
     *
     * @param serverInstanceID specifies the server instance.
     *
     * @return the deployment properties file for the specified server instance, 
     *         if such instance exists and supports Ant deployment, null otherwise.
     *
     * @throws NullPointerException if the specified serverInstanceID is null.
     */
    public static File getDeploymentPropertiesFile(String serverInstanceID) {
        if (serverInstanceID == null) {
            throw new NullPointerException("The serverInstanceID must not be null"); // NOI18N
        }
        ServerInstance si = ServerRegistry.getInstance().getServerInstance(serverInstanceID);
        if (si == null) {
            return null;
        }
        AntDeploymentProvider sup = si.getAntDeploymentProvider();
        return sup == null ? null : sup.getDeploymentPropertiesFile();
    }

    private static boolean isEqual(InputStream stream1, InputStream stream2)
            throws IOException {
        boolean retVal = false;

        BufferedReader reader1 = new BufferedReader(new java.io.InputStreamReader(stream1, "UTF-8")); // NOI18N
        BufferedReader reader2 = new BufferedReader(new java.io.InputStreamReader(stream2, "UTF-8")); // NOI18N

        for (;;) {
            String line1 = reader1.readLine();
            String line2 = reader2.readLine();

            if ((line1 == null) && (line2 == null)) {
                // Both streams has ended, and at this point both stream are the
                // same;
                retVal = true;
                break;
            } else if ((line1 == null) || (line2 == null)) {
                // One stream has ended before the other.  Therefore they are
                // not equal
                break;
            } else if (!line1.equals(line2)) {
                break;
            }
        }

        return retVal;
    }
}
