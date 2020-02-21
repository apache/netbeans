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
package org.netbeans.modules.cnd.spi.toolchain;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.SPIAccessor;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * CompilerSetManagerEvents handles tasks which depends on CompilerSetManager activity and
 * have to survive CompilerSetManager.deepCopy()
 * 
 * There is only one (and I hope there would be only one) such event -- Code Model Readiness
 *
 */
public final class CompilerSetManagerEvents {

    static {
        SPIAccessor.register(new SPIAccessorImpl());
    }

    private static final Map<ExecutionEnvironment, CompilerSetManagerEvents> map =
            new HashMap<ExecutionEnvironment, CompilerSetManagerEvents>();

    private final ExecutionEnvironment executionEnvironment;
    private boolean isCodeModelInfoReady;
    private final CSMInitializationTaskRunner taskRunner;    
    

    public static synchronized CompilerSetManagerEvents get(ExecutionEnvironment env) {
        CompilerSetManagerEvents instance = map.get(env);
        if (instance == null) {
            instance = new CompilerSetManagerEvents(env);
            map.put(env, instance);
        }
        return instance;
    }

    public void runProjectReadiness(NamedRunnable task) {        
        taskRunner.runTask(executionEnvironment, isCodeModelInfoReady, task);
    }
    
    private CompilerSetManagerEvents(ExecutionEnvironment env) {
        this.executionEnvironment = env;
        this.isCodeModelInfoReady = CompilerSetManager.get(executionEnvironment).isComplete();
        taskRunner = CSMInitializationTaskRunner.getInstance();
    }

    private void runTasks() {
        isCodeModelInfoReady = true;
        taskRunner.runTasks();
    }

    private static final class SPIAccessorImpl extends SPIAccessor {

        @Override
        public void runTasks(CompilerSetManagerEvents event) {
            event.runTasks();
        }

        @Override
        public CompilerSetManagerEvents createEvent(ExecutionEnvironment env) {
            return new CompilerSetManagerEvents(env);
        }

        @Override
        public void add(ExecutionEnvironment env, CompilerSet cs) {
            ((CompilerSetManagerImpl)CompilerSetManagerImpl.get(env)).add(cs);
        }

        @Override
        public void remove(ExecutionEnvironment env, CompilerSet cs) {
            ((CompilerSetManagerImpl)CompilerSetManagerImpl.get(env)).remove(cs);
        }
    }
}
