/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.nette2.codegen;

import java.util.Collection;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpType.Method;
import org.netbeans.modules.php.nette2.utils.Constants;
import org.netbeans.modules.php.nette2.utils.EditorUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ActionRenderMethodChecker {
    private FileObject presenterFile;
    private final EditorSupport editorSupport;

    public ActionRenderMethodChecker(FileObject presenterFile) {
        assert presenterFile != null;
        this.presenterFile = presenterFile;
        editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
    }

    public boolean existsActionMethod(String action) {
        return existsMethod(action, Constants.NETTE_ACTION_METHOD_PREFIX);
    }

    public boolean existsRenderMethod(String name) {
        return existsMethod(name, Constants.NETTE_RENDER_METHOD_PREFIX);
    }

    private boolean existsMethod(String name, String type) {
        boolean result = false;
        PhpClass properlyNamedPhpClass = getProperlyNamedPhpClass();
        if (properlyNamedPhpClass != null) {
            Collection<Method> classMethods = properlyNamedPhpClass.getMethods();
            if (classMethods != null) {
                for (Method method : classMethods) {
                    if (method.getName().equals(type + EditorUtils.firstLetterCapital(name))) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private PhpClass getProperlyNamedPhpClass() {
        PhpClass result = null;
        if (editorSupport != null) {
            Collection<PhpClass> classes = editorSupport.getClasses(presenterFile);
            for (PhpClass phpClazz : classes) {
                if (phpClazz.getName().contains(presenterFile.getName())) {
                    result = phpClazz;
                    break;
                }
            }
        }
        return result;
    }

}
