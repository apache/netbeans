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
