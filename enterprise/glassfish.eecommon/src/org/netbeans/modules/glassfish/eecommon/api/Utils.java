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

package org.netbeans.modules.glassfish.eecommon.api;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Utility class for common procedures
 *
 * @author Peter Williams
 */
public final class Utils {

    private Utils() {
    }

    public static final boolean notEmpty(String testedString) {
        return (testedString != null) && (testedString.length() > 0);
    }

    public static final boolean strEmpty(String testedString) {
        return testedString == null || testedString.length() == 0;
    }

    public static final boolean strEquals(String one, String two) {
        boolean result = false;

        if(one == null) {
            result = (two == null);
        } else {
            if(two == null) {
                result = false;
            } else {
                result = one.equals(two);
            }
        }
        return result;
    }

    public static final boolean strEquivalent(String one, String two) {
        boolean result = false;

        if(strEmpty(one) && strEmpty(two)) {
            result = true;
        } else if(one != null && two != null) {
            result = one.equals(two);
        }

        return result;
    }

    public final static int strCompareTo(String one, String two) {
        int result;

        if(one == null) {
            if(two == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else {
            if(two == null) {
                result = 1;
            } else {
                result = one.compareTo(two);
            }
        }

        return result;
    }

    public static String computeModuleID(J2eeModule module, File dir, String fallbackExt) {
        String moduleID = null;
        FileObject fo = null;
        try {
            fo = module.getContentDirectory();
            if (null != fo) {
                moduleID = ProjectUtils.getInformation(FileOwnerQuery.getOwner(fo)).getDisplayName();
            }
        } catch (IOException ex) {
            Logger.getLogger("glassfish-eecommon").log(Level.FINER, null, ex);
        }

        if (null == moduleID || moduleID.trim().length() < 1) {
            J2eeModuleHelper j2eeModuleHelper = J2eeModuleHelper.getSunDDModuleHelper(module.getType());
            if(j2eeModuleHelper != null) {
                RootInterface rootDD = j2eeModuleHelper.getStandardRootDD(module);
                if(rootDD != null) {
                    try {
                        moduleID = rootDD.getDisplayName(null);
                    } catch (VersionNotSupportedException ex) {
                        // ignore, handle as null below.
                    }
                }
            }
        }
        if (null != dir && null != fallbackExt) {
            if (null == moduleID || moduleID.trim().length() < 1) {
                moduleID = simplifyModuleID(dir.getParentFile().getParentFile().getName(), fallbackExt);
            } else {
                moduleID = simplifyModuleID(moduleID, fallbackExt);
            }
        }

        return moduleID;
    }
    
    private static String simplifyModuleID(String candidateID, String fallbackExt) {
        String moduleID = null;

        if (candidateID == null) {
            moduleID = "_default_" + fallbackExt;
        } else if (candidateID.equals("")) {
            moduleID = "_default_" + fallbackExt;
        }

        if (null == moduleID) {
            moduleID = candidateID.replace(' ', '_');
            if (moduleID.startsWith("/")) {
                moduleID = moduleID.substring(1);
            }

            // This moduleID will be later used to construct file path,
            // replace the illegal characters in file name
            //  \ / : * ? " < > | with _
            moduleID = moduleID.replace('\\', '_').replace('/', '_');
            moduleID = moduleID.replace('*', '_');
            moduleID = moduleID.replace('?', '_').replace('"', '_');
            moduleID = moduleID.replace('<', '_').replace('>', '_');
            moduleID = moduleID.replace('|', '_');

            // This moduleID will also be used to construct an ObjectName
            // to register the module, so replace additional special
            // characters , =  used in property parsing with -
            moduleID = moduleID.replace(',', '_').replace('=', '_');
        }
        
        return moduleID;
    }
    
    public static class JarFileFilter implements FileFilter {
        @Override
        public boolean accept(File f) {
            return ((! f.isDirectory()) && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".jar")); //NOI18N
        }
    }
    
    public static FileObject getSunDDFromProjectsModuleVersion(J2eeModule mod, String sunDDFileName) {
        FileObject retVal = null;
        String suffix = "-java_ee_5/";
        if (null != mod) {
            String modVer = mod.getModuleVersion();
            J2eeModule.Type t = mod.getType();
            // ejb 2.0, 2.1, 3.0, 3.1
            // web 2.3, 2.4, 2.5, 3.0
            // appcli 1.3, 1.4, 5.0, 6.0
            // ear 1.3, 1.4, 5, 6
            if (modVer.equals("6") || modVer.equals("6.0") || modVer.endsWith("1.6") || modVer.equals("3.1")) {
                suffix = "-java_ee/";
            } else if (modVer.equals("3.0")) {
                if (J2eeModule.Type.WAR.equals(t)) {
                    suffix = "-java_ee/";
                }
            } else if (modVer.equals("1.4") || modVer.equals("2.4") || modVer.equals("2.1")) {
                suffix = "-j2ee_1_4/";
            } else if (modVer.equals("2.0") || modVer.equals("2.3") || modVer.equals("1.3")) {
                suffix = "-j2ee_1_3/";
            }
        }
        String resource = "org-netbeans-modules-glassfish-eecommon-ddtemplates" + suffix + sunDDFileName; // NOI18N
        retVal = FileUtil.getConfigFile(resource);
        
        return retVal;
    }
    
    public static String getInstanceReleaseID(J2eeModuleProvider jmp) {
        String retVal = "bogusID";
        try {
            String sid = jmp.getServerInstanceID();

            if (null != sid) {
                retVal = sid.replaceFirst(".*\\]deployer:", "").replaceFirst("\\:.*$", ""); // NOI18N
            }
        } catch (NullPointerException npe) {
            Logger.getLogger("glassfish").log(Level.WARNING,
                    "could not get valid InstanceReleaseID from {0}", jmp.getServerInstanceID());
        }
        return retVal;
    }

}
