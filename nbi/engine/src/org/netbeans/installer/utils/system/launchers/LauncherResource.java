/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
            if(this.equals(Type.ABSOLUTE)) {
                return path;
            } else if (this.equals(Type.BUNDLED)) {
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
        return type.equals(Type.BUNDLED);
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
