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

package org.netbeans.installer.utils.system.launchers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Dmitry Lipin
 */
public class LauncherResource {

    public static enum Type {
        BUNDLED,
        ABSOLUTE,
        RELATIVE_JAVAHOME,
        RELATIVE_USERHOME,
        RELATIVE_LAUNCHER_PARENT,
        RELATIVE_LAUNCHER_TMPDIR;

        public long toLong() {
            switch (this) {
                case BUNDLED : return 0L;
                case ABSOLUTE: return 1L;
                case RELATIVE_JAVAHOME: return 2L;
                case RELATIVE_USERHOME: return 3L;
                case RELATIVE_LAUNCHER_PARENT: return 4L;
                case RELATIVE_LAUNCHER_TMPDIR: return 5L;
            }
            return 1L;
        }

        public String toString() {
            switch(this) {
                case BUNDLED:
                    return "nbi.launcher.tmp.dir";
                case ABSOLUTE:
                    return StringUtils.EMPTY_STRING;
                case RELATIVE_JAVAHOME:
                    return "nbi.launcher.java.home";
                case RELATIVE_USERHOME:
                    return "nbi.launcher.user.home";
                case RELATIVE_LAUNCHER_PARENT :
                    return "nbi.launcher.parent.dir";
                case RELATIVE_LAUNCHER_TMPDIR:
                    return "nbi.launcher.tmp.dir";
                default:
                    return null;

            }
        }
        public String getPathString(String path) {
            if(this == Type.ABSOLUTE) {
                return path;
            } else if (this == Type.BUNDLED) {
                return "$L{" + Type.BUNDLED.toString()+ "}/" +
                        new File(path).getName();
            } else {
                return "$L{" + this.toString() + "}/" + path;
            }
        }
    };

    private Type  type;
    private String path;
    private boolean resourceBased;
    /**
     * Bundled launcher file
     */
    public LauncherResource(File file) {
        this(true,file);
    }
    public LauncherResource(boolean bundled, File file) {
        this.type= (bundled) ? Type.BUNDLED : Type.ABSOLUTE;
        this.path= file.getPath();
    }
    /**
     * External or bundled launcher file
     */
    public LauncherResource(Type type, String path) {
        this.type=type;
        this.path=path;
    }
    /** Bundled launcher resource with NBI resource as a source */
    public LauncherResource(String resourceURI) {
        this.type = Type.BUNDLED;
        this.path = resourceURI;
        this.resourceBased = true;
    }
    public Type getPathType() {
        return type;
    }
    public boolean isBundled() {
        return type == Type.BUNDLED;
    }
    public String getPath() {
        return path;
    }
    public boolean isBasedOnResource() {
        return resourceBased;
    }
    public InputStream getInputStream()  {
        if(isBundled()) {
            if(resourceBased) {
                return ResourceUtils.getResource(path);
            } else {
                File f = new File(getPath());
                if(FileUtils.exists(f)) {
                    try {
                        return new FileInputStream(f);
                    } catch (FileNotFoundException ex) {
                        LogManager.log(ex);
                    }
                }
                return null;
            }
        } else {
            return null;
        }

    }
    public String getAbsolutePath() {
        return type.getPathString(path);
    }
    public long getSize() {
        if(isBundled()) {
            return isBasedOnResource() ?
                ResourceUtils.getResourceSize(path) :
                FileUtils.getSize(new File(path));
        } else {
            return 0;
        }
    }

    public String getMD5() {
        final InputStream is = getInputStream();
        String md5 = null;
        if (is != null) {
            try {
                md5 = FileUtils.getMd5(is);
            } catch (IOException e) {
                LogManager.log(e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    LogManager.log(e);
                }
            }
        }

        return md5;
    }
}
