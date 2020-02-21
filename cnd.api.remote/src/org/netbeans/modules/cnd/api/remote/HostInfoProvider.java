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
package org.netbeans.modules.cnd.api.remote;

import java.util.Collection;
import java.util.Map;
//import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.spi.remote.HostInfoProviderFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 * Interface for a remote host information utility provider which can/will be implemented in another module.
 *
 */
public abstract class HostInfoProvider {

    /** Returns path mapper for the given host */
    public static PathMap getMapper(ExecutionEnvironment execEnv) {
        return getDefault(execEnv).getMapper();
    }

    /** Returns path mapper for the given host */
    protected abstract PathMap getMapper();

    /** Returns system environment for the given host */
    public static Map<String, String> getEnv(ExecutionEnvironment execEnv) {
        return getDefault(execEnv).getEnv();
    }

    /** Returns system environment for the given host */
    protected abstract Map<String, String> getEnv();

    /** Validates file existence */
    public static boolean fileExists(ExecutionEnvironment execEnv, String path) {
        return getDefault(execEnv).fileExists(path);
    }

    /** Validates file existence */
    protected abstract boolean fileExists(String path);

    /** Returns dir where libraries are located */
    public static String getLibDir(ExecutionEnvironment execEnv) {
        return getDefault(execEnv).getLibDir();
    }

    /** Returns dir where libraries are located */
    protected abstract String getLibDir();


    private static synchronized HostInfoProvider getDefault(ExecutionEnvironment execEnv) {
        final Collection<? extends HostInfoProviderFactory> factories =
                Lookup.getDefault().lookupAll(HostInfoProviderFactory.class);
        for (HostInfoProviderFactory factory : factories) {
            if (factory.canCreate(execEnv)) {
                final HostInfoProvider provider = factory.create(execEnv);
                assert provider != null;
                return provider;
            }
        }
        throw new IllegalStateException("No host info provider exists for " + execEnv); //NOI18N
    }
}
