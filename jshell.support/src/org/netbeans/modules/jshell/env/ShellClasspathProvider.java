/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.env;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = ClassPathProvider.class, position=1200)
public class ShellClasspathProvider implements ClassPathProvider {

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        JShellEnvironment jshe = ShellRegistry.get().getOwnerEnvironment(file);
        if (jshe == null) {
            return null;
        }
        ShellSession ss = jshe.getSession();
        if (ss == null) {
            return null;
        }
        ClasspathInfo cpi = ss.getClasspathInfo();
        if (cpi == null) {
            return null;
        }
        switch (type) {
            case JavaClassPathConstants.MODULE_BOOT_PATH:
                return cpi.getClassPath(PathKind.MODULE_BOOT);
                
            case JavaClassPathConstants.MODULE_COMPILE_PATH:
                return cpi.getClassPath(PathKind.MODULE_COMPILE);
                
            case JavaClassPathConstants.MODULE_CLASS_PATH:
                return cpi.getClassPath(PathKind.MODULE_CLASS);
                
            case JavaClassPathConstants.MODULE_SOURCE_PATH:
                return cpi.getClassPath(PathKind.MODULE_SOURCE);
                
            case ClassPath.COMPILE:
                return cpi.getClassPath(PathKind.COMPILE);
            case ClassPath.SOURCE:
                return cpi.getClassPath(PathKind.SOURCE);
            case ClassPath.BOOT:
                return cpi.getClassPath(PathKind.BOOT);
        }
        return null;
    }
}
