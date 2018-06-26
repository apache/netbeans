/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.editor.api.completion;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 * Base class for all groovy CC tests, providing various helper methods.
 * 
 * @author Martin Janicek
 */
public abstract class GroovyCCTestBase extends GroovyTestBase {

    protected final String BASE;

    
    protected GroovyCCTestBase(String testName) {
        super(testName);
        Logger.getLogger(CompletionHandler.class.getName()).setLevel(Level.FINEST);

        BASE = getExpandedSourcePath() + "/"; //NOI18N
    }

    /**
     * This method should return concrete test type which will be used for ClassPath initialization. 
     * For example if method CC tests are located under completion/method this method should return
     * "method" for MethodCCTest.java test case
     *
     * @return concrete test type
     */
    protected abstract String getTestType();

    @Override
    protected Set<String> additionalSourceClassPath() {
        HashSet<String> sourceClassPath = new HashSet<String>();
        sourceClassPath.add(getExpandedSourcePath());

        return sourceClassPath;
    }

    private String getExpandedSourcePath() {
        return getBasicSourcePath() + "/" + firstLetterToLowerCase(getClassName()); //NOI18N
    }

    private String firstLetterToLowerCase(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1, className.length());
    }

    protected String getBasicSourcePath() {
        return "testfiles/completion/" + getTestType(); //NOI18N
    }

    protected String getTestPath() {
        return getExpandedSourcePath() + "/" + getClassName() + ".groovy"; //NOI18N
    }

    /*
     * This method returns simple test class name. For example when test method uses SomeTestMethod.groovy as a
     * class for code completion test, this test method is typically named testSomeNameMethod_1 (and if there
     * is more tests for the same SomeTestMethod.groovy class then the number behind '_' is typically incremented).
     * In this case simple test class name is SomeTestMethod.
     */
    private String getClassName() {
        String name = getName();
        String nameWithoutPrefix = name.substring(4); // Removing 'test' prefix

        int indexOf = nameWithoutPrefix.indexOf("_");
        if (indexOf != -1) {
            nameWithoutPrefix = nameWithoutPrefix.substring(0, indexOf); // Removing _someNumber sufix
        }
        return nameWithoutPrefix;
    }
}
