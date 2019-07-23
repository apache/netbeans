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

package org.netbeans.modules.gradle.spi.actions;

import java.util.Map;
import java.util.Set;
import org.openide.util.Lookup;

/**
 * Plugins which would like to add more replace tokens inside the Gradle
 * command line evaluation process shall register implementations of this
 * interface into the project Lookup.
 *
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public interface ReplaceTokenProvider {

    /**
     * The list of the tokens this class implements.
     * @return the list of the supported tokens
     */
    Set<String> getSupportedTokens();

    /**
     * The implementation shall provide values for the tokens evaluating them
     * in the given context.
     * 
     * @param action the id (name) of the action
     * @param context the context where the action is being called.
     * @return map of tokens and values evaluated in the given context.
     */
    Map<String, String> createReplacements(String action, Lookup context);

}
