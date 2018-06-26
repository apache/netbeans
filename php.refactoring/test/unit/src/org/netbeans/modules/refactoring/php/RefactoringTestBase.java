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
package org.netbeans.modules.refactoring.php;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.csl.PHPLanguage;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class RefactoringTestBase extends CslTestBase {

    private static void suppressUselessLogging() {
        for (Handler handler : Logger.getLogger("").getHandlers()) {
            handler.setFilter(new Filter() {

                @Override
                public boolean isLoggable(LogRecord record) {
                    boolean result = true;
                    if (record.getSourceClassName().startsWith("org.netbeans.modules.parsing.impl.indexing.LogContext")
                            || record.getSourceClassName().startsWith("org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater")
                            || record.getSourceClassName().startsWith("org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage")) { //NOI18N
                        result = false;
                    }
                    return result;
                }
            });
        }
    }

    public RefactoringTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        suppressUselessLogging();
        super.setUp();
        TestLanguageProvider.register(getPreferredLanguage().getLexerLanguage());
        TestLanguageProvider.register(HTMLTokenId.language());
    }

    protected String getTestPath() {
        return getTestFolderPath() + "/index.php";
    }

    protected String getTestFolderPath() {
        return "testfiles/" + getTestFolderPathSuffix();
    }

    protected abstract String getTestFolderPathSuffix();

    protected String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), getTestFolderPath()))
            })
        );
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new PHPLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return FileUtils.PHP_MIME_TYPE;
    }

}
