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

package org.netbeans.modules.cnd.toolchain.compilerset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;

/**
 * Recognized (and prioritized) types of compiler sets
 */
public final class CompilerFlavorImpl extends CompilerFlavor {

    private static final List<CompilerFlavorImpl> flavors = new ArrayList<CompilerFlavorImpl>();
    private static final Map<CompilerFlavorImpl, String> productNames = new ConcurrentHashMap<CompilerFlavorImpl, String>();
    private static final Map<Integer, CompilerFlavorImpl> unknown = new HashMap<Integer, CompilerFlavorImpl>();
    static {
        for (ToolchainDescriptor descriptor : ToolchainManagerImpl.getImpl().getAllToolchains()) {
            flavors.add(new CompilerFlavorImpl(descriptor));
        }
    }
    private final String sval;
    private final ToolchainDescriptor descriptor;

    CompilerFlavorImpl(ToolchainDescriptor descriptor) {
        this.sval = descriptor.getName();
        this.descriptor = descriptor;
    }

    @Override
    public ToolchainDescriptor getToolchainDescriptor() {
        return descriptor;
    }

    @Override
    public boolean isGnuCompiler() {
        ToolchainDescriptor d = getToolchainDescriptor();
        if (d != null) {
            for (String f : d.getFamily()) {
                if ("GNU".equals(f)) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSunStudioCompiler() {
        ToolchainDescriptor d = getToolchainDescriptor();
        if (d != null) {
            for (String f : d.getFamily()) {
                if ("SunStudio".equals(f)) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isMinGWCompiler() {
        return sval != null && sval.toLowerCase().startsWith("mingw"); // NOI18N
    }

    @Override
    public boolean isCygwinCompiler() {
        return "Cygwin".equals(sval); // NOI18N
    }

    public static CompilerFlavor getUnknown(int platform) {
        CompilerFlavor unknownFlavor = unknown.get(platform);
        if (unknownFlavor == null) {
            unknownFlavor = _getUnknown(platform);
        }
        return unknownFlavor;
    }

    private static CompilerFlavor _getUnknown(int platform) {
        CompilerFlavorImpl unknownFlavor = null;
        synchronized (unknown) {
            unknownFlavor = unknown.get(platform);
            if (unknownFlavor == null) {
                ToolchainDescriptor d = ToolchainManagerImpl.getImpl().getToolchain("GNU", platform); // NOI18N
                if (d == null) {
                    List<ToolchainDescriptor> list = ToolchainManagerImpl.getImpl().getToolchains(platform);
                    if (list.size() > 0) {
                        d = list.get(0);
                    }
                }
                d = new CompilerSetImpl.UnknownToolchainDescriptor(d);
                unknownFlavor = new CompilerFlavorImpl(d);
                unknown.put(platform, unknownFlavor);
            }
        }
        return unknownFlavor;
    }

    public static CompilerFlavor toFlavor(String name, int platform) {
        if (CompilerSetImpl.UNKNOWN.equals(name)) {
            return getUnknown(platform);
        }
        for (CompilerFlavorImpl flavor : flavors) {
            if (name.equals(flavor.sval) && ToolUtils.isPlatforSupported(platform, flavor.getToolchainDescriptor())) {
                return flavor;
            }
        }
        return null;
    }

    public static String mapOldToNew(String flavor, int version) {
        if (version <= 43) {
            if (flavor.equals("Sun")) { // NOI18N
                return "SunStudio"; // NOI18N
            } else if (flavor.equals("SunExpress")) { // NOI18N
                return "SunStudioExpress"; // NOI18N
            } else if (flavor.equals("Sun12")) { // NOI18N
                return "SunStudio_12"; // NOI18N
            } else if (flavor.equals("Sun11")) { // NOI18N
                return "SunStudio_11"; // NOI18N
            } else if (flavor.equals("Sun10")) { // NOI18N
                return "SunStudio_10"; // NOI18N
            } else if (flavor.equals("Sun9")) { // NOI18N
                return "SunStudio_9"; // NOI18N
            } else if (flavor.equals("Sun8")) { // NOI18N
                return "SunStudio_8"; // NOI18N
            } else if (flavor.equals("DJGPP")) { // NOI18N
                return "GNU"; // NOI18N
            } else if (flavor.equals("Interix")) { // NOI18N
                return "GNU"; // NOI18N
            } else if (flavor.equals(CompilerSetImpl.UNKNOWN)) {
                return "GNU"; // NOI18N
            }
        }
        return flavor;
    }

    private static boolean isPlatforSupported(CompilerFlavor flavor, int platform) {
        ToolchainDescriptor d = flavor.getToolchainDescriptor();
        if (d != null) {
            return ToolUtils.isPlatforSupported(platform, d);
        }
        return true;
    }

    public static List<CompilerFlavor> getFlavors(int platform) {
        ArrayList<CompilerFlavor> list = new ArrayList<CompilerFlavor>();
        for (CompilerFlavor flavor : flavors) {
            if (isPlatforSupported(flavor, platform)) {
                list.add(flavor);
            }
        }
        return list;
    }

    public static void putProductName(CompilerFlavorImpl flavor, String productName)  {
        productNames.put(flavor, productName);
    }
    
    public String getDisplayName() {
        return CompilerSetImpl.extractDisplayName(this, productNames.get(this));
    }
    
    @Override
    public String toString() {
        return CompilerSetImpl.extractDisplayName(this, productNames.get(this));
    }
}
