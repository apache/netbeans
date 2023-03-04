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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    public static Action createSelectCamelCasePositionAction(Action originalAction, boolean next) {
        return next
                ? new SelectNextCamelCasePosition(originalAction)
                : new SelectPreviousCamelCasePosition(originalAction);
    }

}
