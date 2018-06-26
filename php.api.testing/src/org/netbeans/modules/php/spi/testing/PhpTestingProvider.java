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

package org.netbeans.modules.php.spi.testing;

import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.testing.create.CreateTestsResult;
import org.netbeans.modules.php.spi.testing.locate.TestLocator;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;

/**
 * Encapsulates a PHP testing provider. Provider might be interested in storing
 * and/or reading provider specific values (for example, a bootstrap, custom test suite etc.)
 * from {@link PhpModule#getPreferences(Class, boolean) preferences)}.
 *
 * <p>This class allows providing support for creating and running PHP tests.</p>
 *
 * <p>Instances of this class are registered in the <code>{@value org.netbeans.modules.php.api.testing.PhpTesting#TESTING_PATH}</code>
 * in the module layer.</p>
 */
public interface PhpTestingProvider {

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this PHP testing provider.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    String getIdentifier();

    /**
     * Returns the display name of this PHP testing provider. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    String getDisplayName();

    /**
     * Checks whether the given file is a test file.
     * @param phpModule the PHP module; never {@code null}
     * @param fileObj file to be checked; never {@code null}
     * @return {@code true} if the file is a test file
     */
    boolean isTestFile(@NonNull PhpModule phpModule, @NonNull FileObject fileObj);

    /**
     * Checks whether the given test is a test case (test method).
     * @param phpModule the PHP module; never {@code null}
     * @param method test case (test method) to be checked; never {@code null}
     * @return {@code true} if the test is a test case (test method)
     * @since 0.3
     */
    boolean isTestCase(@NonNull PhpModule phpModule, @NonNull PhpType.Method method);

    /**
     * Run tests, <b>synchronously</b>, for the given {@link TestRunInfo info} and use
     * the given {@link TestSession test session} for providing results.
     * <p>
     * <b>This method must be blocking, in other words, it should not return before test run finish.</b>
     * <p>
     * This method is always called in a background thread.
     * @param phpModule the PHP module; never {@code null}
     * @param runInfo info about the test run; never {@code null}
     * @param testSession  test session to be updated with the test results
     * @throws TestRunException if any error occurs during the test run, e.g. some resource is not available
     * @since 0.2
     */
    void runTests(@NonNull PhpModule phpModule, @NonNull TestRunInfo runInfo, @NonNull TestSession testSession) throws TestRunException;

    /**
     * Gets test locator for this provider.
     * @param phpModule the PHP module; never {@code null}
     * @return the test locator, never {@code null}
     */
    TestLocator getTestLocator(@NonNull PhpModule phpModule);

    /**
     * Creates tests for the given files and returns info about test creating.
     * <p>
     * This method is always called in a background thread.
     * @param phpModule the PHP module; never {@code null}
     * @param files source file the tests should be created for
     * @param configurationPanelProperties properties from {@link org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration configuration panel} (if provided any)
     * @return info about test creating
     * @since 0.19
     */
    CreateTestsResult createTests(@NonNull PhpModule phpModule, @NonNull List<FileObject> files, @NonNull Map<String, Object> configurationPanelProperties);

    /**
     * Checks whether this provider supports code coverage.
     * @param phpModule the PHP module; never {@code null}
     * @return {@code true} if this provider supports code coverage, {@code false} otherwise
     */
    boolean isCoverageSupported(@NonNull PhpModule phpModule);

    /**
     * Tries to parse the given input and finds a file with a line in it.
     * @param line input text to be parsed
     * @return info about the file and line if the parsing was successful, {@code null} otherwise
     */
    @CheckForNull
    Locations.Line parseFileFromOutput(String line);

    /**
     * Create project customizer for the given PHP module.
     * @param phpModule the PHP module; never {@code null}
     * @return project customizer, can be {@code null} if not supported
     * @since 0.8
     */
    @CheckForNull
    ProjectCustomizer.CompositeCategoryProvider createCustomizer(@NonNull PhpModule phpModule);

    //~ Inner classes

    /**
     * Declarative registration of a singleton PHP testing provider.
     * By marking an implementation class or a factory method with this annotation,
     * you automatically register that implementation, normally in {@link org.netbeans.modules.php.api.testing.PhpTesting#TESTING_PATH}.
     * The class must be public and have:
     * <ul>
     *  <li>a public no-argument constructor, or</li>
     *  <li>a public static factory method.</li>
     * </ul>
     *
     * <p>Example of usage:
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
     * &#64;PhpTestingProvider.Registration(position=100)
     * public class MyTests extends PhpTestingProvider {...}
     * </pre>
     * <pre>
     * package my.module;
     * import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
     * public class MyTests extends PhpTestingProvider {
     *     &#64;PhpTestingProvider.Registration(position=100)
     *     public static PhpTestingProvider getInstance() {...}
     * }
     * </pre>
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {
        /**
         * An optional position in which to register this testing provider relative to others.
         * Lower-numbered services are returned in the lookup result first.
         * Providers with no specified position are returned last.
         */
        int position() default Integer.MAX_VALUE;
    }

}
