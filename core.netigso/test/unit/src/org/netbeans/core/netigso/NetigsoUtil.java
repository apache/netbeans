/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.netigso;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
import junit.framework.Assert;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.launch.Framework;

/** Some useful utilities to work with Netigso framework.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class NetigsoUtil {
    private NetigsoUtil() {
    }
    
    static void downgradeJDK() {
        final Pattern JDK9 = Pattern.compile("(1\\.)?9");   //NOI18N
        if (JDK9.matcher(System.getProperty("java.specification.version")).matches()) {
            System.setProperty("java.specification.version", "1.8");
        }
    }

    public static Framework framework(ModuleManager mgr) throws Exception {
        final Method nm = mgr.getClass().getDeclaredMethod("netigso");
        nm.setAccessible(true);
        final Netigso netigso = (Netigso) nm.invoke(mgr);
        Method m = Netigso.class.getDeclaredMethod("getFramework");
        m.setAccessible(true);
        Framework f = (Framework) m.invoke(netigso);
        return f;
    }
    
    static Bundle bundle(Module module) throws Exception {
        Framework f = framework(module.getManager());
        for (Bundle b : f.getBundleContext().getBundles()) {
            if (b.getSymbolicName().equals(module.getCodeNameBase())) {
                return b;
            }
        }
        Assert.fail("no bundle found for " + module);
        return null;
    }
    
}
