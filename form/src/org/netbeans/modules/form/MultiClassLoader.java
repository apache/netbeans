/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.form;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Very simple class loader that delegates to several class loaders.
 * Note that no class returned by this class loader will claim that
 * it was loaded by this class loader. So, you probably don't want
 * to use this class unless you are sure that this is not a problem
 * in your context.
 *
 * @author Jan Stola
 */
public class MultiClassLoader extends ClassLoader {
    /** Class loader delegates. */
    private ClassLoader[] loaders;
    
    /**
     * Creates new {@code MultiClassLoader}.
     * 
     * @param loaders class loader delegates.
     */
    public MultiClassLoader(ClassLoader... loaders) {
        this.loaders = loaders;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader loader : loaders) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException cnfe) {}
        }
        return super.findClass(name);
    }

    @Override
    protected URL findResource(String name) {
        URL url = null;
        for (ClassLoader loader : loaders) {
            url = loader.getResource(name);
            if (url != null) {
                break;
            }
        }
        return url;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        Vector<URL> vector = new Vector<URL>();
        for (ClassLoader loader : loaders) {
            Enumeration<URL> enumeration = loader.getResources(name);
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                if (!vector.contains(url)) {
                    vector.add(url);
                }
            }
        }
        return vector.elements();
    }
    
}
