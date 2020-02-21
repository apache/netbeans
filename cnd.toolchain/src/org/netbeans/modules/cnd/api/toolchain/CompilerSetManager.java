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

package org.netbeans.modules.cnd.api.toolchain;

import java.io.Writer;
import java.util.List;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerAccessorImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerImpl;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public abstract class CompilerSetManager {
    /**
     * Find or create a default CompilerSetManager for the given key. A default
     * CSM is one which is active in the system. A non-default is one which gets
     * created but has no affect unless its made default.
     *
     * For instance, the Build Tools tab (on C/C++ Tools->Options) creates a non-Default
     * CSM and only makes it default if the OK button is pressed. If Cancel is pressed,
     * it never becomes default.
     *
     * @param env specifies execution environment
     * @return A default CompilerSetManager for the given key
     */
    public static CompilerSetManager get(ExecutionEnvironment env) {
        return CompilerSetManagerAccessorImpl.getDefault(env);
    }

    public abstract ExecutionEnvironment getExecutionEnvironment();
    
    public abstract CompilerSet getCompilerSet(String name);

    public abstract List<CompilerSet> getCompilerSets();

    public abstract CompilerSet getDefaultCompilerSet();

    public abstract boolean isDefaultCompilerSet(CompilerSet cs);

    public abstract int getPlatform();

    public abstract void setDefault(CompilerSet newDefault);

    /**
     * CAUTION: this is a slow method. It should NOT be called from the EDT thread
     */
    public abstract void initialize(boolean save, boolean runCompilerSetDataLoader, Writer reporter);

    public abstract boolean cancel();

    public abstract void finishInitialization();

    public abstract boolean isEmpty();

    public abstract boolean isPending();

    public abstract boolean isUninitialized();
    //add methods below as API/SPI should give the client the possibility to create own UI
    //in ODCS, for example
    
    public abstract boolean isComplete();
    
    public abstract void add(CompilerSet cs);
    
    public abstract void remove(CompilerSet cs);

    protected CompilerSetManager() {
        if (!getClass().equals(CompilerSetManagerImpl.class)) {
            throw new UnsupportedOperationException("this class can not be overriden by clients"); // NOI18N
        }
    }

}
