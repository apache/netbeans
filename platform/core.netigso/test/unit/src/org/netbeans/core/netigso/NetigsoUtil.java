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
package org.netbeans.core.netigso;

import java.lang.reflect.Method;
import org.junit.Assert;
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
