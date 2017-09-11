/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.form.project;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.openide.ErrorManager;

/**
 * A special classloader capable to combine system classpath (IDE modules) and
 * user project classpath into one. Classes loaded by this classloader can link
 * with module classes running in the IDE and access resources on project
 * classpath at the same time.
 *
 * @author Tomas Pavek
 */

final class FormClassLoader extends ClassLoader {

    private ClassLoader systemClassLoader;
    private ClassLoader projectClassLoader;

    FormClassLoader(ClassLoader projectClassLoader) {
        super(null);
        this.systemClassLoader = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
        this.projectClassLoader = projectClassLoader;
    }

    ClassLoader getProjectClassLoader() {
        return projectClassLoader;
    }

    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        ClassPathUtils.ClassLoadingType type = ClassPathUtils.getClassLoadingType(name);
        if (type == null) {
            if (projectClassLoader == null) {
                throw new ClassNotFoundException(ClassPathUtils.getBundleString("MSG_NullClassPath")); // NOI18N
            }
            return projectClassLoader.loadClass(name);
        }
        if (type == ClassPathUtils.SYSTEM_CLASS) {
            return systemClassLoader.loadClass(name);
        }
        // otherwise type == ClassPathUtils.SYSTEM_CLASS_WITH_PROJECT

        Class c = null;
        String filename = name.replace('.', '/').concat(".class"); // NOI18N
        URL url = systemClassLoader.getResource(filename);
        if (url == null && projectClassLoader != null)
            url = projectClassLoader.getResource(filename);
        if (url != null) {
            InputStream is = null;
            try {
                is = url.openStream();
                byte[] data = null;
                int first;
                int available = is.available();
                while ((first = is.read()) != -1) {
                    int length = is.available();
                    if (length != available) { // Workaround for issue 4401122
                        length++;
                    }
                    byte[] b = new byte[length];
                    b[0] = (byte) first;
                    int count = 1;
                    while (count < length) {
                        int read = is.read(b, count, length - count);
                        assert (read != -1);
                        count += read;
                    }
                    if (data == null) {
                        data = b;
                    }
                    else {
                        byte[] temp = new byte[data.length + count];
                        System.arraycopy(data, 0, temp, 0, data.length);
                        System.arraycopy(b, 0, temp, data.length, count);
                        data = temp;
                    }
                }
                int dot = name.lastIndexOf('.');
                if (dot != -1) { // Is there anything we should do for the default package?
                    String packageName = name.substring(0, dot);
                    Package pakcage = getPackage(packageName);
                    if (pakcage == null) {
                        // PENDING are we able to determine the attributes somehow?
                        definePackage(packageName, null, null, null, null, null, null, null);
                    }
                }
                c = defineClass(name, data, 0, data.length);
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioex) {
                        // ignore
                    }
                }
            }
        }
        if (c == null)
            throw new ClassNotFoundException(name);

        return c;
    }

    @Override
    public URL getResource(String name) {
        URL url = projectClassLoader != null ? projectClassLoader.getResource(name) : null;
        if (url == null)
            url = systemClassLoader.getResource(name);
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> e = projectClassLoader != null ? projectClassLoader.getResources(name) : null;
        if (e == null) {
            e = systemClassLoader.getResources(name);
        }
        return e;
    }

}
