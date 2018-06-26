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
package org.netbeans.modules.glassfish.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * GlassFish server utilities.
 * <p/>
 * @author Tomas Kraus
 */
public class ServerUtils {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(ServerUtils.class);
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
     * Build domains folder for GlassFish server using GlassFish version.
     * <p/>
     * Domains folder name is build using all versions number parts
     * (<code>major</code>, <code>minor</code>, <code>update</code>
     * and <code>build</code>). But <code>update</code> and <code>build</code>
     * <p/>
     * values are used only when they are not part of zero only sequence.<br/>
     * Folder names for individual GlassFish server versions will be:<ul>
     * <li><code>GF_3.0</code> for GlassFish 3</li>
     * <li><code>GF_3.1</code> for GlassFish 3.1</li>
     * <li><code>GF_3.0.1</code> for GlassFish 3.0.1</li>
     * <li><code>GF_3.1.2.2</code> for GlassFish 3.1.2.2</li><ul/>
     * <p/>
     * @param instance GlassFish server to build domains folder for.
     * @return Domains folder.
     */
    public static String getDomainsFolder(@NonNull GlassfishInstance instance) {
        GlassFishVersion version = instance.getVersion();
        if (version == null) {
            throw new IllegalStateException(NbBundle.getMessage(
                    GlassfishInstance.class,
                    "GlassfishInstance.getDomainsFolder.versionIsNull",
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
        sb.append(GlassFishVersion.SEPARATOR);
        sb.append(Short.toString(version.getMinor()));
        if (useUpdate) {
            sb.append(GlassFishVersion.SEPARATOR);
            sb.append(Short.toString(version.getUpdate()));
            if (useBuild) {
                sb.append(GlassFishVersion.SEPARATOR);
                sb.append(Short.toString(version.getBuild()));
            }
        }
        return sb.toString();
    }

    /**
     * Check if GlassFish server process is still running.
     * <p/>
     * @param process GlassFish server process. May not be <code>null</code>.
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
