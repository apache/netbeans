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
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class SuperglobalsHint extends HintRule {

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = new CheckVisitor(this, fileObject, context.doc);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                hints.addAll(checkVisitor.getHints());
            }
        }
    }

    @Override
    @NbBundle.Messages({
        "SuperglobalsHintDesc=Use some filtering functions instead (e.g. filter_input(), conditions with is_*() functions, etc.)."
    })
    public String getDescription() {
        return Bundle.SuperglobalsHintDesc();
    }

    @Override
    @NbBundle.Messages({
        "# {0} - Superglobal Array Name",
        "SuperglobalsHintDisp=Do not Access {0} Array Directly"
    })
    public String getDisplayName() {
        return Bundle.SuperglobalsHintDisp(getSuperglobalName());
    }

    abstract String getSuperglobalName();

    public static final class GetSuperglobalHint extends SuperglobalsHint {
        private static final String HINT_ID = "Get.Superglobal.Hint"; //NOI18N
        private static final String ARRAY_NAME = "$_GET"; //NOI18N

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        protected String getSuperglobalName() {
            return ARRAY_NAME;
        }

    }

    public static final class PostSuperglobalHint extends SuperglobalsHint {
        private static final String HINT_ID = "Post.Superglobal.Hint"; //NOI18N
        private static final String ARRAY_NAME = "$_POST"; //NOI18N

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        protected String getSuperglobalName() {
            return ARRAY_NAME;
        }

    }

    public static final class CookieSuperglobalHint extends SuperglobalsHint {
        private static final String HINT_ID = "Cookie.Superglobal.Hint"; //NOI18N
        private static final String ARRAY_NAME = "$_COOKIE"; //NOI18N

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        protected String getSuperglobalName() {
            return ARRAY_NAME;
        }

    }

    public static final class ServerSuperglobalHint extends SuperglobalsHint {
        private static final String HINT_ID = "Server.Superglobal.Hint"; //NOI18N
        private static final String ARRAY_NAME = "$_SERVER"; //NOI18N

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        protected String getSuperglobalName() {
            return ARRAY_NAME;
        }

    }

    public static final class EnvSuperglobalHint extends SuperglobalsHint {
        private static final String HINT_ID = "Env.Superglobal.Hint"; //NOI18N
        private static final String ARRAY_NAME = "$_ENV"; //NOI18N

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        protected String getSuperglobalName() {
            return ARRAY_NAME;
        }

    }

    public static final class RequestSuperglobalHint extends SuperglobalsHint {
        private static final String HINT_ID = "Request.Superglobal.Hint"; //NOI18N
        private static final String ARRAY_NAME = "$_REQUEST"; //NOI18N

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        protected String getSuperglobalName() {
            return ARRAY_NAME;
        }

    }

    private static final class CheckVisitor extends DefaultTreePathVisitor {
        private final SuperglobalsHint superglobalsHint;
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final List<Hint> hints;

        private CheckVisitor(SuperglobalsHint superglobalsHint, FileObject fileObject, BaseDocument baseDocument) {
            assert superglobalsHint != null;
            assert fileObject != null;
            this.superglobalsHint = superglobalsHint;
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.hints = new ArrayList<>();
        }

        public List<Hint> getHints() {
            return hints;
        }

        @Override
        public void visit(Variable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            String variableName = CodeUtils.extractVariableName(node);
            if (superglobalsHint.getSuperglobalName().equals(variableName) && !isValidAccess()) {
                addHint(node);
            }
        }

        @NbBundle.Messages({
            "# {0} - Superglobal Array Name",
            "SuperglobalHintText=Do not Access Superglobal {0} Array Directly."
                + "\n\nUse some filtering functions instead (e.g. filter_input(), conditions with is_*() functions, etc.)."
        })
        protected void addHint(ASTNode node) {
            assert node != null;
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (superglobalsHint.showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(superglobalsHint, Bundle.SuperglobalHintText(superglobalsHint.getSuperglobalName()), fileObject, offsetRange, null, 500));
            }
        }

        protected boolean isValidAccess() {
            return new AccessValidator(getPath(), superglobalsHint.getSuperglobalName()).isValidAccess();
        }

    }

    private static final class AccessValidator {
        private static final Collection<String> VALIDATOR_FUNCTIONS = new ArrayList<>();
        static {
            VALIDATOR_FUNCTIONS.add("is_integer"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_long"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_float"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_file"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_object"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_string"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_int"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_double"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_numeric"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_finite"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_infinite"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_null"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_nan"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_scalar"); //NOI18N
            VALIDATOR_FUNCTIONS.add("is_real"); //NOI18N
        }

        private static final Collection<String> FILTER_FUNCTIONS = new ArrayList<>();
        static {
            FILTER_FUNCTIONS.add("htmlspecialchars"); //NOI18N
        }

        private final List<ASTNode> path;
        private final String superglobalName;

        private AccessValidator(List<ASTNode> path, String superglobalName) {
            assert path != null;
            assert superglobalName != null;
            this.path = path;
            this.superglobalName = superglobalName;
        }

        public boolean isValidAccess() {
            return isInFilterFunction() || isInValidatorFunction() || isValidatedByCondition() || isOnLeftSideOfAssignment();
        }

        private boolean isOnLeftSideOfAssignment() {
            boolean result = false;
            ASTNode firstInPath = path.get(0);
            for (ASTNode aSTNode : path) {
                if (aSTNode instanceof Assignment) {
                    Assignment assignment = (Assignment) aSTNode;
                    if (assignment.getLeftHandSide().equals(firstInPath)) {
                        result = true;
                    }
                    break;
                }
            }
            return result;
        }

        private boolean isValidatedByCondition() {
            boolean result = false;
            for (ASTNode aSTNode : path) {
                Expression condition = null;
                if (aSTNode instanceof IfStatement) {
                    IfStatement ifStatement = (IfStatement) aSTNode;
                    condition = ifStatement.getCondition();
                } else if (aSTNode instanceof WhileStatement) {
                    WhileStatement whileStatement = (WhileStatement) aSTNode;
                    condition = whileStatement.getCondition();
                } else if (aSTNode instanceof ConditionalExpression) {
                    ConditionalExpression conditionalExpression = (ConditionalExpression) aSTNode;
                    condition = conditionalExpression.getCondition();
                }
                if (condition != null) {
                    ConditionVisitor conditionVisitor = new ConditionVisitor(superglobalName);
                    condition.accept(conditionVisitor);
                    if (conditionVisitor.isSuperglobalValidated()) {
                        result = true;
                        break;
                    }
                }
            }
            return result;
        }

        public boolean isInValidatorFunction() {
            boolean result = false;
            for (ASTNode aSTNode : path) {
                if (aSTNode instanceof FunctionInvocation) {
                    String functionName = CodeUtils.extractFunctionName((FunctionInvocation) aSTNode);
                    if (isValidatorFunction(functionName)) {
                        result = true;
                        break;
                    }
                }
            }
            return result;
        }

        private static boolean isValidatorFunction(String functionName) {
            return VALIDATOR_FUNCTIONS.contains(functionName);
        }

        private boolean isInFilterFunction() {
            boolean result = false;
            for (ASTNode aSTNode : path) {
                if (aSTNode instanceof FunctionInvocation) {
                    String functionName = CodeUtils.extractFunctionName((FunctionInvocation) aSTNode);
                    if (isFilterFunction(functionName)) {
                        result = true;
                        break;
                    }
                }
            }
            return result;
        }

        private static boolean isFilterFunction(String functionName) {
            return FILTER_FUNCTIONS.contains(functionName);
        }

    }

    private static final class ConditionVisitor extends DefaultTreePathVisitor {
        private final String superglobalName;
        private boolean isSuperglobalValidated;

        private ConditionVisitor(String superglobalName) {
            assert superglobalName != null;
            this.superglobalName = superglobalName;
            isSuperglobalValidated = false;
        }

        public boolean isSuperglobalValidated() {
            return isSuperglobalValidated;
        }

        @Override
        public void visit(Variable node) {
            super.visit(node);
            String variableName = CodeUtils.extractVariableName(node);
            if (superglobalName.equals(variableName) && new AccessValidator(getPath(), superglobalName).isInValidatorFunction()) {
                isSuperglobalValidated = true;
            }
        }

    }

}
