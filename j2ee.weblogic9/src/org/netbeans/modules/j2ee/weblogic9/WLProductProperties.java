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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public final class WLProductProperties {

    private static final Logger LOGGER = Logger.getLogger(WLProductProperties.class.getName());

    private final WLDeploymentManager dm;

    /**
     * <i>GuardedBy("this")</i>
     */
    private Properties props;

    private String rootProp;

    private File file;

    private FileChangeListener listener;

    public WLProductProperties(WLDeploymentManager dm) {
        this.dm = dm;
    }

    @CheckForNull
    public String getMiddlewareHome() {
//        String wlHome = System.getenv("MW_HOME"); //NOI18N
//        if (wlHome != null) {
//            return wlHome;
//        }
        synchronized(this) {
            checkProperty();

            if (props == null) {
                props = parse();
            }
            return props.getProperty("MW_HOME"); // NOI18N
        }
    }

    /**
     * Use this method only when DeploymentManager is not available (ie. from
     * registration wizard). It is slow and does not cache anything.
     * 
     * @param serverRoot
     * @return 
     */
    @CheckForNull
    public static String getMiddlewareHome(File serverRoot) {
        Properties ret = new Properties();
        File productProps = new File(serverRoot, ".product.properties"); // NOI18N

        if (!productProps.exists() || !productProps.canRead()) {
            return null;
        }
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(productProps));
            try {
                ret.load(is);
            } finally {
                is.close();
            }
            return ret.getProperty("MW_HOME");
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }        
    }
    
    private synchronized Properties parse() {
        Properties ret = new Properties();
        InstanceProperties ip = dm.getInstanceProperties();
        rootProp = ip.getProperty(WLPluginProperties.SERVER_ROOT_ATTR);

        if (listener != null) {
            FileUtil.removeFileChangeListener(listener, file);
        }

        file = null;
        if (rootProp != null) {
            file = new File(rootProp, ".product.properties"); // NOI18N
            listener = new PropertiesChangeListener();
            FileUtil.addFileChangeListener(listener, file);
        }

        if (file == null || !file.exists() || !file.canRead()) {
            return ret;
        }
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            try {
                ret.load(is);
            } finally {
                is.close();
            }
            return ret;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return new Properties();
        }
    }

    private void checkProperty() {
        InstanceProperties ip = dm.getInstanceProperties();
        String root = ip.getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        if ((root != null && !root.equals(rootProp)) || (root == null && rootProp != null)) {
            reset();
        }
    }

    private synchronized void reset() {
        props = null;
    }

    private class PropertiesChangeListener implements FileChangeListener {

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileChanged(FileEvent fe) {
            reset();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            reset();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reset();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset();
        }

    }
}
