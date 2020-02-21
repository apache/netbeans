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

package org.netbeans.modules.cnd.makeproject.ui;

import java.lang.reflect.Method;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;

/*
 * org.netbeans.modules.project.ui.ProjectTab is not public!. Use reflection....
 * See IZ 7551
 * ProjectTabBridge reflection bridge
 */
public class ProjectTabBridge {
    private static ProjectTabBridge instance = null;
    private static final String className = "org.netbeans.modules.project.ui.ProjectTab"; // NOI18N

    private Class<?> refClass = null;

    public ProjectTabBridge() throws ClassNotFoundException {
	ClassLoader c = Lookup.getDefault().lookup(ClassLoader.class);
	// Find the class
	if (c == null) {
	    refClass = Class.forName(className);
	}
	else {
	    refClass = Class.forName(className, true, c);
	}
    }

    public static ProjectTabBridge getInstance() {
	if (instance == null) {
	    try {
		instance = new ProjectTabBridge();
	    }
	    catch (java.lang.ClassNotFoundException e) {
		// FIXUP...
	    }
	}
	return instance;
    }
    

    public Object findDefault(String tcID) {
	String methodName = "findDefault"; // NOI18N
	Method method = null;
	Object ret = null;

	if (refClass == null) {
	    return null;
        }

	try {
	    method = refClass.getMethod(methodName, String.class);
	    ret = method.invoke(null, new Object[] {tcID});
	} catch(Exception e) {
	    System.err.println("ProjectTabBridge " + methodName + e); // NOI18N
	}

	return ret;
    }

    public ExplorerManager getExplorerManager() {
	Object projectTab = findDefault("projectTabLogical_tc"); // NOI18N

	String methodName = "getExplorerManager"; // NOI18N
	Method method = null;
	Object ret = null;

	if (refClass == null) {
	    return null;
        }

	try {
	    method = refClass.getMethod(methodName);
	    ret = method.invoke(projectTab, new Object[0]);
	} catch(Exception e) {
	    System.err.println("ProjectTabBridge " + methodName + e); // NOI18N
	}

	return (ExplorerManager)ret;
    }
}
