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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.apt.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.APTMacroCache;
import org.netbeans.modules.cnd.apt.impl.support.SnapshotHolderCache;
import org.netbeans.modules.cnd.apt.impl.support.APTSystemMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankSystemMacroMap;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.FSPath;

/**
 *
 */
public final class APTSystemStorage {
    private final Map<String, PPMacroMap> allMacroMaps = new HashMap<String, PPMacroMap>();
    private final APTIncludePathStorage includesStorage;
    private final static String baseNewName = "#SYSTEM MACRO MAP# "; // NOI18N
    private final static APTSystemStorage instance = new APTSystemStorage();
    
    private APTSystemStorage() {
        includesStorage = new APTIncludePathStorage();
    }
    
    public static APTSystemStorage getInstance() {
        return instance;
    }
    
    public PPMacroMap getMacroMap(String configID, List<String> sysMacros) {
        synchronized (allMacroMaps) {
            PPMacroMap map = allMacroMaps.get(configID);
            if (map == null) {
                // create new one and put in map
                if (APTTraceFlags.USE_CLANK) {
                    map = new ClankSystemMacroMap(sysMacros);
                } else {
                    map = new APTSystemMacroMap(sysMacros);
                }
                allMacroMaps.put(configID, map);
                if (APTUtils.LOG.isLoggable(Level.FINE)) {
                    APTUtils.LOG.log(Level.FINE,
                            "new system macro map was added\n {0}", // NOI18N
                            new Object[] { map });
                }
            }
            return map;
        }
    }
    
//    // it's preferable to use getIncludes(String configID, List sysIncludes)
//    public List<CharSequence> getIncludes(List<CharSequence> sysIncludes) {
//        return includesStorage.get(sysIncludes);
//    }
    
    public List<IncludeDirEntry> getIncludes(CharSequence configID, List<IncludePath> sysIncludes) {
        return includesStorage.get(configID, sysIncludes);
    }   
    
    private void disposeImpl() {
        synchronized (allMacroMaps) {
            allMacroMaps.clear();
        }
        includesStorage.dispose();
    }
    
    public static void dispose() {
        instance.disposeImpl();
        APTMacroCache.getManager().dispose();
        SnapshotHolderCache.getManager().dispose();
        IncludeDirEntry.disposeCache();
    }
}
