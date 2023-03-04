/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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
                        writer = new OutputStreamWriter(fileOS, StandardCharsets.UTF_8);
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

        BufferedReader reader1 = new BufferedReader(new InputStreamReader(stream1, StandardCharsets.UTF_8));
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(stream2, StandardCharsets.UTF_8));

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
