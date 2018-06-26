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
package org.netbeans.modules.php.spi.testing.run;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.openide.filesystems.FileObject;

/**
 * Interface for a test session.
 */
public interface TestSession {

    /**
     * Add new test suite to this test session.
     * @param name name of the test suite
     * @param location location of the test suite, can be {@code null}
     * @return new test suite
     * @see TestSuite#finish(long)
     * @since 0.2
     */
    TestSuite addTestSuite(@NonNull String name, @NullAllowed FileObject location);

    /**
     * Set line handler to use for printing while running tests.
     * <p>
     * This method should be called before first test suite is {@link #addTestSuite(String, FileObject) added}.
     * @param outputLineHandler line handler to use for printing while running tests
     * @since 0.2
     */
    void setOutputLineHandler(@NonNull OutputLineHandler outputLineHandler);

    /**
     * Print message.
     * @param message message that is print, can be empty but never {@code null}
     * @param error {@code true} if the given message is an error message
     * @since 0.2
     */
    void printMessage(@NonNull String message, boolean error);

    /**
     * Set code coverage data, compulsory to call if
     * {@link org.netbeans.modules.php.spi.testing.PhpTestingProvider#isCoverageSupported(org.netbeans.modules.php.api.phpmodule.PhpModule) supported} by this testing provider.
     * @param coverage code coverage data, can be {@code null} if any error occured
     * @see org.netbeans.modules.php.spi.testing.PhpTestingProvider#isCoverageSupported(org.netbeans.modules.php.api.phpmodule.PhpModule)
     * @since 0.2
     */
    void setCoverage(@NullAllowed Coverage coverage);

}
