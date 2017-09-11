/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.platform.queries;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import static org.netbeans.api.java.platform.JavaPlatform.PROP_SOURCE_FOLDER;
import org.netbeans.api.java.platform.Specification;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Tomas Zezula
 */
class TestJavaPlatform extends JavaPlatform {

    private final String name;
    private final ClassPath boot;
    private volatile ClassPath sources;
    private volatile List<URL> javadoc;

    TestJavaPlatform(final String name, final ClassPath boot) {
        this.name = name;
        this.boot = boot;
        this.sources = ClassPath.EMPTY;
        this.javadoc = Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.<String,String>emptyMap();
    }

    @Override
    public ClassPath getBootstrapLibraries() {
        return boot;
    }

    @Override
    public ClassPath getStandardLibraries() {
        return ClassPath.EMPTY;
    }

    @Override
    public String getVendor() {
        return "Oracle";    //NOI18N
    }

    @Override
    public Specification getSpecification() {
        return new Specification("j2se", new SpecificationVersion("1.5"));
    }

    @Override
    public Collection<FileObject> getInstallFolders() {
        return Collections.<FileObject>emptySet();
    }

    @Override
    public FileObject findTool(String toolName) {
        return null;
    }

    @Override
    public ClassPath getSourceFolders() {
        return sources;
    }

    @Override
    public List<URL> getJavadocFolders() {
        return Collections.unmodifiableList(javadoc);
    }

    void setSources(final ClassPath cp) {
        sources = cp;
        firePropertyChange(PROP_SOURCE_FOLDER, null, null);
    }

    void setJavadoc(final List<URL> javadoc) {
        this.javadoc = javadoc;
        firePropertyChange(PROP_JAVADOC_FOLDER, null, null);
    }

}
