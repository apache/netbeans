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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
