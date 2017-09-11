/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.api;

import javax.swing.Action;
import org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor;

/**
 * A factory class for creating actions provided by CSL.
 *
 * @author Erno Mononen
 * @since 2.10
 */
public final class CslActions {

    private CslActions() {
    }

    /**
     * Creates an action for navigating to element declaration
     * @return the action; never {@code null}.
     */
    public static Action createGoToDeclarationAction() {
        return new GoToDeclarationAction();
    }
    
    /**
     * Creates an action for navigating to next/previous occurrences.
     * @param nextOccurrence  - {@code true} for navigating to next occurrence; {@code false}
     *  for previous.
     * @return the action; never {@code null}.
     */
    public static Action createGoToMarkOccurrencesAction(boolean nextOccurrence) {
        return new GoToMarkOccurrencesAction(nextOccurrence);
    }

    /**
     * Creates an action for handling instant rename.
     *
     * @return the action; never {@code null}.
     */
    public static Action createInstantRenameAction() {
        return new InstantRenameAction();
    }

    /**
     * Creates an action that selects next/previous code elements
     * according to the language model.
     *
     * @param selectNext {@code true} if the next element should be selected.
     *  {@code false} if the previous element should be selected.
     * 
     * @return the action; never {@code null}.
     */
    public static Action createSelectCodeElementAction(boolean selectNext) {
        String name = selectNext
                ? SelectCodeElementAction.selectNextElementAction
                : SelectCodeElementAction.selectPreviousElementAction;

        return new SelectCodeElementAction(name, selectNext);
    }

    /**
     * Creates a general toggle comment action. This action will dynamically determine
     * the language of the document section, where it is invoked and use correct comments.
     *
     * <p class="nonnormative">
     * It uses {@code CommentHandler} implementations from {@code DefaultLanguageConfig.getCommentHandler}
     * for the section's language. If there is no {@code CommentHandler} the action
     * assumes that the language uses line comments and the action will use {@code DefaultLanguageConfig.getLineCommentPrefix}.
     *
     * @return the action; never {@code null}.
     */
    public static Action createToggleBlockCommentAction() {
        return new ToggleBlockCommentAction();
    }

    /**
     * Creates an action for navigating to next/previous camel case positions.
     * @param originalAction - the action to wrap around; may be {@code null}.
     * @param next - {@code true} for navigating to next; {@code false} for previous.
     * 
     * @deprecated use {@link CamelCaseInterceptor} instead
     * @return the action; never {@code null}.
     */
    public static Action createCamelCasePositionAction(Action originalAction, boolean next) {
        return next
                ? new NextCamelCasePosition(originalAction)
                : new PreviousCamelCasePosition(originalAction);
    }

    /**
     * Creates an action for deleting text to next/previous camel case position.
     * @param originalAction - the action to wrap around; may be {@code null}.
     * @param next - {@code true} for navigating to next; {@code false} for previous.
     *
     * @deprecated use {@link CamelCaseInterceptor} instead
     * @return the action; never {@code null}.
     */
    public static Action createDeleteToCamelCasePositionAction(Action originalAction, boolean next) {
        return next
                ? new DeleteToNextCamelCasePosition(originalAction)
                : new DeleteToPreviousCamelCasePosition(originalAction);
    }

    /**
     * Creates an action for selecting text to next/previous camel case position.
     * @param originalAction - the action to wrap around; may be {@code null}.
     * @param next - {@code true} for navigating to next; {@code false} for previous.
     *
     * @deprecated use {@link CamelCaseInterceptor} instead
     * @return the action; never {@code null}.
     */
    public static Action createSelectCamelCasePositionAction(Action originalAction, boolean next) {
        return next
                ? new SelectNextCamelCasePosition(originalAction)
                : new SelectPreviousCamelCasePosition(originalAction);
    }

}
