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
package org.netbeans.modules.refactoring.spi.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.Scope;
import org.openide.util.Lookup;

/**
 * ScopeProvider is used to add a scope to the {@link ScopePanel}.
 *
 * @see ScopePanel
 * @see ScopeDescription
 * @see ScopeReference
 *
 * @author Jan Lahoda
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 *
 * @since 1.30
 */
public abstract class ScopeProvider {

    /**
     * Initialize this scope provider with the supplied context.
     *
     * @param context a Lookup defining the context
     * @param cancel set to true if initialization should be canceled
     * @return true if this scope is usable for the supplied context, false
     * otherwise
     */
    public abstract boolean initialize(@NonNull Lookup context, @NonNull AtomicBoolean cancel);

    /**
     * Get this provider's scope. The scope can be null if this provider is not
     * yet initialized, or this provider is not usable for the supplied context.
     *
     * @see ScopeProvider#initialize(org.openide.util.Lookup,
     * java.util.concurrent.atomic.AtomicBoolean)
     * @return the Scope
     */
    @CheckForNull
    public abstract Scope getScope();

    /**
     * Override this method if the icon should be different depending on the
     * context.
     *
     * @see ScopeProvider#initialize(org.openide.util.Lookup,
     * java.util.concurrent.atomic.AtomicBoolean)
     * @see ScopeDescription
     * @return an icon for the Scope, or null if the default should be used.
     */
    @CheckForNull
    public Icon getIcon() {
        return null;
    }

    /**
     * Override this method if an explaining string should be displayed behind
     * the scope's name. This is used for scopes like "- Current File
     * (Filename.java)".
     *
     * @return an explaining string, or null of nothing should be displayed.
     */
    @CheckForNull
    public String getDetail() {
        return null;
    }
    
    /**
     * Override this method if a problem can occur when the user selects this
     * scope. This is used for scopes like "- Open Projects with Dependencies",
     * when searching for constants.
     *
     * @return a problem with an explaining string, or null of nothing should be displayed.
     * @since 1.44
     */
    @CheckForNull
    public Problem getProblem() {
        return null;
    }

    /**
     * The CustomScopeProvider extends the normal scope provider to support
     * scopes which can be customized by the user.
     *
     * @see ScopePanel
     * @see ScopeDescription
     * @see ScopeReference
     *
     * @author Jan Lahoda
     * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
     *
     * @since 1.30
     */
    public abstract static class CustomScopeProvider extends ScopeProvider {

        /**
         * Set the scope to be customized. This method will be called right
         * before the provider is asked to show the customizer, with the
         * currently selected scope.
         *
         * @param currentScope the Scope currently selected
         */
        public abstract void setScope(@NullAllowed Scope currentScope);

        /**
         * Show a Dialog to customize the scope.
         *
         * @return true if the user succesfully specified a scope, false
         * otherwise (ie. clicked cancel)
         */
        public abstract boolean showCustomizer();
    }
    
    /**
     * Declarative registration of a ScopeProvider implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation.
     * The class must be public and have a public constructor which takes
     * no arguments.
     * 
     * @author Jan Lahoda
     * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
     * @since 1.30
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public static @interface Registration {

        /**
         * The unique ID of the scope description.
         *
         * @return unique id
         */
        String id();

        /**
         * Position in the location. Negative numbers can be used for scopes
         * that should be higher in the list, but not selected by default.
         */
        int position() default Integer.MAX_VALUE;
        
        /**
         * Display name. Usually prefixed with '#' to reference value from a
         * <code>Bundle.properties</code> file in the same package.
         *
         * @return display name for the scope
         */
        String displayName();

        /**
         * Path to image representing the scope's icon.
         *
         * @return "org/myproject/mypkg/Icon.png"
         */
        String iconBase() default "";
    }
}
