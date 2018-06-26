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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ide.ergonomics;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.modules.ide.ergonomics.fod.FindComponentModules;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class PerClusterEnablementCheck extends NbTestCase {
    public PerClusterEnablementCheck(String n) {
        super(n);
    }
    
    public void testAPISupportTriggersAlsoJavaKit() throws Exception {
        FeatureInfo apisupport = null;
        FeatureInfo java = null;
        for (FeatureInfo f : FeatureManager.features()) {
            if (f.getCodeNames().contains("org.netbeans.modules.apisupport.kit")) {
                apisupport = f;
            }
            if (f.getCodeNames().contains("org.netbeans.modules.java.kit")) {
                java = f;
            }
        }
        assertNotNull("apisupport feature found", apisupport);
        assertNotNull("java feature found", java);

        FindComponentModules find = new FindComponentModules(apisupport);
        Set<String> expectedNames = new HashSet<String>(java.getCodeNames());
        for (UpdateElement updateElement : find.getModulesForEnable()) {
            expectedNames.remove(updateElement.getCodeName());
        }
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (isEager(mi) || isAutoload(mi)) {
                expectedNames.remove(mi.getCodeNameBase());
            }
        }
        if (!expectedNames.isEmpty()) {
            fail(
                "java cluster shall be fully enabled, but this was missing:\n" +
                expectedNames.toString().replace(',', '\n')
            );
        }
    }

    private boolean isEager(ModuleInfo mi) throws Exception {
        Method m = mi.getClass().getMethod("isEager");
        return (Boolean)m.invoke(mi);
    }
    private boolean isAutoload(ModuleInfo mi) throws Exception {
        Method m = mi.getClass().getMethod("isAutoload");
        return (Boolean)m.invoke(mi);
    }
}
