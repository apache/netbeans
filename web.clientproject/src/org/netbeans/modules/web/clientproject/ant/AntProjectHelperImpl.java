/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.ant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.env.Values;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakSet;
import org.w3c.dom.Element;

/**
 */
final class AntProjectHelperImpl extends CommonProjectHelper {
    final org.netbeans.spi.project.support.ant.AntProjectHelper delegate;
    private final L listener;
    private final Collection<Callback> antListeners;

    public AntProjectHelperImpl(org.netbeans.spi.project.support.ant.AntProjectHelper delegate) {
        this.delegate = delegate;
        this.listener = new L();
        this.delegate.addAntProjectListener(listener);
        this.antListeners = Collections.synchronizedSet(new WeakSet<Callback>());
    }

    @Override
    public void registerCallback(Callback listener) {
        antListeners.add(listener);
    }

    @Override
    public FileObject getProjectDirectory() {
        return delegate.getProjectDirectory();
    }

    @Override
    public void notifyDeleted() {
        delegate.notifyDeleted();
    }

    @Override
    public Element getPrimaryConfigurationData(boolean shared) {
        return delegate.getPrimaryConfigurationData(shared);
    }

    @Override
    public void putPrimaryConfigurationData(Element data, boolean shared) throws IllegalArgumentException {
        delegate.putPrimaryConfigurationData(data, shared);
    }

    @Override
    public AuxiliaryConfiguration createAuxiliaryConfiguration() {
        return delegate.createAuxiliaryConfiguration();
    }

    @Override
    public CacheDirectoryProvider createCacheDirectoryProvider() {
        return delegate.createCacheDirectoryProvider();
    }

    @Override
    public AuxiliaryProperties createAuxiliaryProperties() {
        return delegate.createAuxiliaryProperties();
    }

    @Override
    public Values getStandardPropertyEvaluator() {
        return new PropertyEvaluatorImpl(delegate.getStandardPropertyEvaluator());
    }

    @Override
    public Object getXmlSavedHook() {
        return listener;
    }

    @Override
    public File resolveFile(String path) {
        return delegate.resolveFile(path);
    }

    @Override
    public FileObject resolveFileObject(String path) {
        return delegate.resolveFileObject(path);
    }

    @Override
    public void putProperties(Object path, org.openide.util.EditableProperties props) {
        EditableProperties copy = new EditableProperties(true);
        copy.putAll(props);
        delegate.putProperties(mapPath(path), copy);
    }
    
    @Override
    public org.openide.util.EditableProperties getProperties(Object path) {
        return extract(delegate.getProperties(mapPath(path)));
    }


    @Override
    public SharabilityQueryImplementation2 createSharabilityQuery2(
        org.netbeans.modules.web.clientproject.env.Values e,
        String[] roots, String[] dirs
    ) {
        PropertyEvaluatorImpl ip = (PropertyEvaluatorImpl) e;
        return delegate.createSharabilityQuery2(ip.delegate, roots, dirs);
    }

    private final class L extends ProjectXmlSavedHook
    implements AntProjectListener {
        @Override
        public void configurationXmlChanged(AntProjectEvent ev) {
            for (CommonProjectHelper.Callback l : antListeners) {
                l.configurationXmlChanged();
            }
        }

        @Override
        public void propertiesChanged(AntProjectEvent ev) {
            for (CommonProjectHelper.Callback l : antListeners) {
                l.propertiesChanged();
            }
        }

        @Override
        protected void projectXmlSaved() throws IOException {
            for (CommonProjectHelper.Callback l : antListeners) {
                l.projectXmlSaved();
            }
        }
    }

    private static final Field DELEGATE;
    static {
        try {
            DELEGATE = org.netbeans.spi.project.support.ant.EditableProperties.class.getDeclaredField("delegate");
            DELEGATE.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            throw new SecurityException(ex);
        }
    }
    
    private static org.openide.util.EditableProperties extract(
        org.netbeans.spi.project.support.ant.EditableProperties p
    ) {
        try {
            return (org.openide.util.EditableProperties) DELEGATE.get(p);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String mapPath(Object path) {
        if (path == CommonProjectHelper.PRIVATE_PROPERTIES_PATH) {
            return org.netbeans.spi.project.support.ant.AntProjectHelper.PRIVATE_PROPERTIES_PATH;
        } else if (path == CommonProjectHelper.PROJECT_PROPERTIES_PATH) {
            return org.netbeans.spi.project.support.ant.AntProjectHelper.PROJECT_PROPERTIES_PATH;
        }
        return path.toString();
    }
}
