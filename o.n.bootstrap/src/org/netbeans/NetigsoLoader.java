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
package org.netbeans;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NetigsoLoader extends ClassLoader {
    private final Module mi;

    public NetigsoLoader(Module mi) {
        this.mi = mi;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader del = getDelegate(10000);
        if (del == null) {
            Util.err.log(Level.WARNING, 
                "Time out waiting to enabled {0}. Cannot load {1}",
                new Object[]{mi.getCodeNameBase(), className}
            );
            throw new ClassNotFoundException(className);
        }
        return del.loadClass(className);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        ClassLoader d = getDelegate();
        if (d instanceof ProxyClassLoader) {
            return ((ProxyClassLoader)d).loadClass(name, resolve);
        } else {
            return d.loadClass(name);
        }
    }

    @Override
    public Enumeration<URL> getResources(String string) throws IOException {
        return getDelegate().getResources(string);
    }

    @Override
    public InputStream getResourceAsStream(String string) {
        return getDelegate().getResourceAsStream(string);
    }

    @Override
    public URL getResource(String string) {
        return getDelegate().getResource(string);
    }

    private ClassLoader getDelegate() {
        return getDelegate(0);
    }
    private ClassLoader getDelegate(long timeout) {
        if (!mi.isEnabled()) {
            Util.err.log(Level.INFO, 
                "OSGi is requesting adhoc start of {0}. This is inefficient. "
              + "It is suggested turn the module on by default", 
                mi.getCodeNameBase()
            );
            Mutex.Privileged p = mi.getManager().mutexPrivileged();
            if (!p.tryWriteAccess(timeout)) {
                return null;
            }
            try {
                mi.getManager().enable(mi, false);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvalidException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                p.exitWriteAccess();
            }
        }
        return mi.getClassLoader();
    }
    
} // end of DelegateLoader
