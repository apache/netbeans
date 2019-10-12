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
package org.netbeans.modules.payara.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.payara.common.PayaraLogger;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Payara server utilities.
 * <p/>
 * @author Tomas Kraus
 */
public class ServerUtils {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(ServerUtils.class);
    /** Domains folder prefix. */
    private static final String DOMAINS_FOLDER_PREFIX = "GF_";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get the file attribute with the specified name as {@see String}.
     * <p/>
     * @param fo       File objects on a file system.
     * @param attrName Name of the attribute.
     * @return Value of the file attribute with the specified name
     *         or <code>null</code> if no file attribute was found.
     */
    public static String getStringAttribute(FileObject fo, String attrName) {
        return getStringAttribute(fo, attrName, null);
    }

    /**
     * Get the file attribute with the specified name as {@see String}.
     * <p/>
     * @param fo       File objects on a file system.
     * @param attrName Name of the attribute.
     * @param defValue Default value of the attribute if no value
     *                 is stored in file object.
     * @return Value of the file attribute with the specified name or default
     *         value if no file attribute was found.
     */
    public static String getStringAttribute(FileObject fo, String attrName,
            String defValue) {
        String result = defValue;
        Object attr = fo.getAttribute(attrName);
        if(attr instanceof String) {
            result = (String) attr;
        }
        return result;
    }

    /**
     * Set file attribute of given file object.
     * <p/>
     * @param fo    File object.
     * @param key   Attribute key.
     * @param value Attribute value.
     */
    public static void setStringAttribute(FileObject fo, String key,
            String value) {
        try {
            fo.setAttribute(key, value);
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING,
                    "Cannot update file object value: {0} -> {1} in {2}",
                    new Object[]{key, value, fo.getPath()});
        }
    }
        
    /**
     * Get NetBeans repository directory under default configuration file system
     * root with specified sub path.
     * <p/>
     * @param path   Path to be appended to default configuration file system
     *               root.
     * @param create Create path under default configuration file system root
     *               if it does not exist.
     * @return NetBeans repository directory under default configuration
     *         file system.
     */
    public static FileObject getRepositoryDir(String path, boolean create) {
        FileObject dir = FileUtil.getConfigFile(path);
        if(dir == null && create) {
            try {
                dir = FileUtil.createFolder(FileUtil.getConfigRoot(), path);
            } catch(IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return dir;
    }

    /**
     * Verify that provided String represents valid readable directory
     * in file system.
     * <p/>
     * @param folderName Path of directory in file system to be verified.
     */
    public static boolean isValidFolder(String folderName) {
        if (folderName == null) {
            return false;
        }
        File f = new File(folderName);
        return f.isDirectory() && f.canRead();
    }

    /**
     * Build domains folder for Payara server using Payara version.
     * <p/>
     * Domains folder name is build using all versions number parts
     * (<code>major</code>, <code>minor</code>, <code>update</code>
     * and <code>build</code>). But <code>update</code> and <code>build</code>
     * <p/>
     * values are used only when they are not part of zero only sequence.<br/>
     * Folder names for individual Payara server versions will be:<ul>
     * <li><code>PF_4.1.144</code> for Payara 4.1.144</li>
     * <li><code>PF_4.1.151</code> for Payara 4.1.151</li>
     * <p/>
     * @param instance Payara server to build domains folder for.
     * @return Domains folder.
     */
    public static String getDomainsFolder(@NonNull PayaraInstance instance) {
        PayaraVersion version = instance.getVersion();
        if (version == null) {
            throw new IllegalStateException(NbBundle.getMessage(PayaraInstance.class,
                    "PayaraInstance.getDomainsFolder.versionIsNull",
                    instance.getDisplayName()));
        }
        boolean useBuild = version.getBuild() > 0;
        boolean useUpdate = useBuild || version.getUpdate() > 0;
        // Allocate 2 characters per version number part and 1 character
        // per separator.
        StringBuilder sb = new StringBuilder(DOMAINS_FOLDER_PREFIX.length() + 5
                + (useUpdate ? (useBuild ? 6 : 3) : 0));
        sb.append(DOMAINS_FOLDER_PREFIX);
        sb.append(Short.toString(version.getMajor()));
        sb.append(PayaraVersion.SEPARATOR);
        sb.append(Short.toString(version.getMinor()));
        if (useUpdate) {
            sb.append(PayaraVersion.SEPARATOR);
            sb.append(Short.toString(version.getUpdate()));
            if (useBuild) {
                sb.append(PayaraVersion.SEPARATOR);
                sb.append(Short.toString(version.getBuild()));
            }
        }
        return sb.toString();
    }

    /**
     * Check if Payara server process is still running.
     * <p/>
     * @param process Payara server process. May not be <code>null</code>.
     */
    public static boolean isProcessRunning(final Process process) {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException itse) {
            return true;
        }
    }

}
