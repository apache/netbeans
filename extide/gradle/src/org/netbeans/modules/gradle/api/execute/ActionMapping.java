/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.gradle.api.execute;

import java.io.Serializable;
import java.util.Set;

/**
 * Mapping for IDE actions to Gradle command line.
 * <p>
 * The mapping can cause the action to be <b>disabled</b>: if {@link #getReloadRule()} == {@link ReloadRule#NEVER} and
 * {@link #getArgs()} == {@code null}. Such mapping had no effect in previous versions. This can be checked by a
 * convenience method {@link ActionMapping#isDisabled}.
 * @since 1.0
 * @since 2.14 concept of empty/disabled action
 * @author Laszlo Kishalmi
 */
public interface ActionMapping extends Serializable, Comparable<ActionMapping> {

    /** Prefix fo custom, non-default IDE actions. */
    public static final String CUSTOM_PREFIX = "custom-"; //NOI18N

    /**
     * Rule to reload the project after the Gradle execution. The project reload
     * by default done, by setting the offline flag in Gradle in order to avoid
     * bumping into some time consuming remote interactions.
     */
    static enum ReloadRule {
        /** Never reload the project. */
        NEVER,
        /**
         * Reload only when the project information {@link org.netbeans.modules.gradle.api.NbGradleProject.Quality Quality}
         * is worse than {@link org.netbeans.modules.gradle.api.NbGradleProject.Quality#FULL FULL}.
         */
        DEFAULT,
        /** Always reload the project. */
        ALWAYS,
        /**
         * Always reload the project allowing Gradle to fetch things online.
         * You can usually expect the project info {@link org.netbeans.modules.gradle.api.NbGradleProject.Quality Quality}
         * to be set {@link org.netbeans.modules.gradle.api.NbGradleProject.Quality#FULL_ONLINE FULL_ONLINE},
         * however it is not guaranteed.
         */
        ALWAYS_ONLINE
    }

    /**
     * The name of the action, recognizable by the IDE. Custom action name
     * shall begin with {@link #CUSTOM_PREFIX}.
     *
     * @return the name of the action.
     */
    String getName();

    /**
     * The display name only used to present custom actions inside the IDE.
     *
     * @return the display name of the custom actions.
     */
    String getDisplayName();

    /**
     * The command line arguments passed to the Gradle process executed as the action.
     *
     * @return the command line arguments executed as the action.
     */
    String getArgs();

    /**
     * The reload rule of the action.
     * @return the reload rule of the action.
     */
    ReloadRule getReloadRule();

    /**
     * The command line arguments passed to the Gradle process executed
     * during the reload after the action execution.
     *
     * @return the command line arguments executed as the project reload.
     */
    String getReloadArgs();

    /**
     * Returns true if the action can be applicable regarding the provided
     * set of plugins.
     *
     * @param plugins the set of plugins check the action availability against.
     * @return true if the action can be used with the provided plugins.
     */
    boolean isApplicable(Set<String> plugins);

    /**
     * Returns true if the action can be repeated after execution.
     *
     * @return true if the action can be repeated after execution.
     */
    boolean isRepeatable();


    /**
     * Checks if the action is disabled. Use in preference to plain <code>actionMapping == null</code>
     * as it also handles a mapping that forcefully deconfigures an action.
     * 
     * @param am action mapping to check
     * @return true, if the action is <b>disabled</b>
     * @since 2.14
     */
    static boolean isDisabled(ActionMapping am) {
        return am == null || (
                    am.getReloadRule() == ActionMapping.ReloadRule.NEVER &&
                    (am.getArgs() == null || am.getArgs().isEmpty()) &&
                    (am.getReloadArgs() == null || am.getReloadArgs().isEmpty())
                );
    }
}
