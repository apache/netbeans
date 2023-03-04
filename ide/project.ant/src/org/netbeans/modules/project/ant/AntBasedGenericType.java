/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.project.ant;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.spi.intern.ProjectIDEServices;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Generic implementation of {@link AntBasedGenericType} that feeds itself from
 * an layer data.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class AntBasedGenericType implements AntBasedProjectType {

    private final String iconResource;
    private final String type;
    private final String className;
    private final String methodName;
    private final String[] configNames;
    private final String[] configNamespaces;

    public AntBasedGenericType(Map map) {
        
        // XXXX 
        iconResource = (String) map.get("iconResource"); // NOI18N
        type = (String) map.get("type"); // NOI18N
        className = (String) map.get("className"); // NOI18N
        methodName = (String) map.get("methodName"); // NOI18N
        configNames = new String[]{
            (String) map.get("sharedName"), // NOI18N
            (String) map.get("privateName"), // NOI18N
        };
        configNamespaces = new String[]{
            (String) map.get("sharedNamespace"), // NOI18N
            (String) map.get("privateNamespace"), // NOI18N
        };
        Parameters.notNull("iconResource", iconResource); // NOI18N
        Parameters.notNull("type", type); // NOI18N
        Parameters.notNull("className", className); // NOI18N
        Parameters.notNull("sharedName", configNames[0]); // NOI18N
        Parameters.notNull("privateName", configNames[1]); // NOI18N
        Parameters.notNull("sharedNamespace", configNamespaces[0]); // NOI18N
        Parameters.notNull("privateNamespace", configNamespaces[1]); // NOI18N
    }

    public Icon getIcon() {
        return ProjectIDEServices.loadImageIcon(iconResource, true);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? configNames[0] : configNames[1];
    }

    @Override
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? configNamespaces[0] : configNamespaces[1];
    }

    @Override
    public Project createProject(AntProjectHelper helper) throws IOException {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = AntBasedGenericType.class.getClassLoader();
        }
        try {
            Class<?> clazz = l.loadClass(className);
            if (methodName != null) {
                Method m = clazz.getDeclaredMethod(methodName, AntProjectHelper.class);
                return (Project) m.invoke(null, helper);
            } else {
                Constructor c = clazz.getConstructor(AntProjectHelper.class);
                return (Project) c.newInstance(helper);
            }
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof IOException) {
                throw (IOException) ex.getTargetException();
            }
            if (ex.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) ex.getTargetException();
            }
            throw new IllegalArgumentException(ex);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
