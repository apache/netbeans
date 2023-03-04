/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
