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
package org.netbeans.modules.refactoring.java.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.MoveRefactoring;

/**
 * This class is just holder for properties of the Java Move Members
 * Refactoring. Refactoring itself is implemented in plugins.
 *
 * @see org.netbeans.modules.refactoring.api.MoveRefactoring
 * @see org.netbeans.modules.refactoring.api.Context
 * @author Ralph Ruijs
 * @since 1.31
 */
public final class JavaMoveMembersProperties {

    private final TreePathHandle[] preSelectedMembers;
    private Visibility visibility;
    private boolean delegate;
    private boolean addDeprecated;
    private boolean updateJavaDoc;

    /**
     * Constructs a new JavaMoveMembersProperties object.
     */
    public JavaMoveMembersProperties(@NonNull TreePathHandle... preSelectedMembers) {
        this.preSelectedMembers = preSelectedMembers;
        visibility = Visibility.ESCALATE;
        delegate = false;
        addDeprecated = false;
        updateJavaDoc = false;
    }

    /**
     * The members that are selected by the user when starting the refactoring.
     * @return TreePathHandle array with the size of 1 or more.
     */
    public @NonNull TreePathHandle[] getPreSelectedMembers() {
        return preSelectedMembers;
    }

    /**
     * The new visibility of the members.
     *
     * @return visibility
     */
    public @NonNull Visibility getVisibility() {
        return visibility;
    }

    /**
     * The new visibility of the members.
     *
     * @param visibility the visibility to use
     */
    public void setVisibility(@NonNull Visibility visibility) {
        this.visibility = visibility;
    }

    /**
     * Leave the old method in place and let it delegate to the new location.
     * All references to the method will not be touched.
     *
     * @return true if the old method will delegate to the new location, false
     * otherwise
     */
    public boolean isDelegate() {
        return delegate;
    }

    /**
     * Leave the old method in place and let it delegate to the new location.
     * All references to the method will not be touched.
     *
     * @param delegate true if the old method needs delegate to the new location
     */
    public void setDelegate(boolean delegate) {
        this.delegate = delegate;
    }

    /**
     * Add a deprecated tag to the JavaDoc of the delegating method.
     *
     * @return true if a deprecated tag needs to be added, false otherwise
     */
    public boolean isAddDeprecated() {
        return addDeprecated;
    }

    /**
     * Add a deprecated tag to the JavaDoc of the delegating method.
     *
     * @param addDeprecated true if a deprecated tag has to be added to the
     * delegate method
     */
    public void setAddDeprecated(boolean addDeprecated) {
        this.addDeprecated = addDeprecated;
    }

    /**
     * Update or create the JavaDoc for moved methods.
     *
     * @return true if JavaDoc will be created or the existing will be updated,
     * false otherwise
     */
    public boolean isUpdateJavaDoc() {
        return updateJavaDoc;
    }

    /**
     * Add a deprecated tag to the javadoc of the delegating method.
     *
     * @param updateJavaDoc true if the method's JavaDoc needs to be created or
     * updated.
     */
    public void setUpdateJavaDoc(boolean updateJavaDoc) {
        this.updateJavaDoc = updateJavaDoc;
    }

    /**
     * Used to specify visibility level. It can either be set explicitly, or set
     * to Escalate to automatically raise it to a necessary level.
     */
    public static enum Visibility {

        /**
         * Escalate, automatically raise the visibility to a necessary level,
         * based on usages.
         */
        ESCALATE,
        /**
         * As is, keep the current visibility level of the member.
         */
        ASIS,
        /**
         * Change to, or keep, the visibility public.
         */
        PUBLIC,
        /**
         * Change to, or keep, the visibility protected.
         */
        PROTECTED,
        /**
         * Change to, or keep, the visibility the default level.
         */
        DEFAULT,
        /**
         * Change to, or keep, the visibility private.
         */
        PRIVATE
    }
}
