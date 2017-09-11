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
package org.netbeans.core.netigso;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.ProxyClassLoader;
import org.openide.modules.ModuleInfo;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;
import org.osgi.framework.Bundle;

final class NetigsoLoader extends ProxyClassLoader {
    private static final Logger LOG = Logger.getLogger(NetigsoLoader.class.getName());
    final Bundle bundle;

    NetigsoLoader(Bundle b, ModuleInfo m, File jar) {
        super(new ClassLoader[0], true);
        this.bundle = b;
    }

    @Override
    public URL findResource(String name) {
        //Netigso.start();
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load resource before initialization finished {0}", name);
            return null;
        }
        return b.getResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) {
        //Netigso.start();
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load resource before initialization finished {0}", name);
            return Enumerations.empty();
        }
        Enumeration ret = null;
        try {
            if (b.getState() != Bundle.UNINSTALLED) {
                ret = b.getResources(name);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ret == null ? Enumerations.<URL>empty() : NbCollections.checkedEnumerationByFilter(ret, URL.class, true);
    }

    @Override
    protected Class<?> doLoadClass(String pkg, String name) {
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load class before initialization finished {0}", pkg + '.' + name);
            return null;
        }
        try {
            return b.loadClass(name);
        } catch (ClassNotFoundException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "No class found in " + this, ex);
            }
            return null;
        }
    }

    @Override
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load class before initialization finished {0}", new Object[] { name });
            return null;
        }
        try {
            c = b.loadClass(name);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        } catch (ClassNotFoundException x) {
        }
        return super.loadClass(name, resolve);
    }


    @Override
    public String toString() {
        Bundle b = bundle;
        if (b == null) {
            return "uninitialized";
        }
        return b.getLocation();
    }
}
