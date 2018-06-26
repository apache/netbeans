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

package org.netbeans.modules.php.project.ui.customizer;

import java.util.List;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.classpath.BaseProjectPathSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.util.NbBundle;

public final class SeleniumTestDirectoriesPathSupport extends BaseProjectPathSupport {

    public SeleniumTestDirectoriesPathSupport(PropertyEvaluator evaluator, ReferenceHelper referenceHelper, AntProjectHelper antProjectHelper) {
        super(evaluator, referenceHelper, antProjectHelper);
    }

    @Override
    protected boolean isWellKnownPath(String p) {
        return false;
    }

    //~ Inner classes

    public static final class Validator {

        private final ValidationResult result = new ValidationResult();


        public ValidationResult getResult() {
            return new ValidationResult(result);
        }

        public Validator validatePaths(PhpProject project, List<BasePathSupport.Item> items) {
            for (Item item : items) {
                validatePath(project, item);
            }
            return this;
        }

        @NbBundle.Messages({
            "# {0} - file path",
            "# {1} - error message",
            "SeleniumTestDirectoriesPathSupport.Validator.error={0}: {1}",
        })
        public Validator validatePath(PhpProject project, BasePathSupport.Item item) {
            String error = Utils.validateTestSources(project, item.getAbsoluteFilePath(project.getProjectDirectory()));
            if (error != null) {
                String filePath = item.getFilePath();
                result.addError(new ValidationResult.Message(filePath, Bundle.SeleniumTestDirectoriesPathSupport_Validator_error(filePath, error)));
            }
            return this;
        }

    }

}
