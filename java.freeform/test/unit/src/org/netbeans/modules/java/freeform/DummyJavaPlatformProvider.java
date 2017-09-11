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

package org.netbeans.modules.java.freeform;

import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 * Testing Java platform provider, just enough to check ClassPath.BOOT usage.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.java.platform.implspi.JavaPlatformProvider.class)
public final class DummyJavaPlatformProvider implements JavaPlatformProvider {
    
    private final JavaPlatform jdk14 = new DummyJavaPlatform("1.4");
    private final JavaPlatform jdk15 = new DummyJavaPlatform("1.5");
    
    /** Default constructor for lookup. */
    public DummyJavaPlatformProvider() {}
    
    public JavaPlatform getDefaultPlatform() {
        return jdk15;
    }
    
    public JavaPlatform[] getInstalledPlatforms() {
        return new JavaPlatform[] {
            jdk14,
            jdk15,
        };
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {}
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {}
    
    private static final class DummyJavaPlatform extends JavaPlatform {
        
        private final String spec;
        private ClassPath bootcp;
        
        public DummyJavaPlatform(String spec) {
            this.spec = spec;
        }
        
        public FileObject findTool(String toolName) {
            return null;
        }
        
        public ClassPath getBootstrapLibraries() {
            if (bootcp == null) {
                try {
                    bootcp = ClassPathSupport.createClassPath(new URL[] {
                        // This file does not really have to exist - just needs to have a well-known location.
                        // Cf. ClasspathsTest.
                        new URL("jar:file:/c:/java/" + spec + "/jre/lib/rt.jar!/"),
                    });
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
            }
            return bootcp;
        }
        
        public String getDisplayName() {
            return "JDK " + spec;
        }
        
        public Collection getInstallFolders() {
            return Collections.EMPTY_SET;
        }
        
        public List getJavadocFolders() {
            return Collections.EMPTY_LIST;
        }
        
        public Map getProperties() {
            return Collections.EMPTY_MAP;
        }
        
        public ClassPath getSourceFolders() {
            return ClassPathSupport.createClassPath(new FileObject[0]);
        }
        
        public Specification getSpecification() {
            return new Specification("j2se", new SpecificationVersion(spec));
        }
        
        public ClassPath getStandardLibraries() {
            return ClassPathSupport.createClassPath(new FileObject[0]);
        }
        
        public String getVendor() {
            return "test";
        }
        
    }
    
}
