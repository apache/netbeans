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

import java.util.Collection;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 *
 * @author lahvac
 * @since 2.3
 */
public interface OverridingMethods {

    /**Return references to methods that the <code>handle</code> method/class overrides
     *
     * @param info
     * @param handle
     * @return null is equivalent to empty collection
     */
    public @CheckForNull Collection<? extends AlternativeLocation> overrides(@NonNull ParserResult info, @NonNull ElementHandle handle);

    /**Whether the {@link OverridingMethods#overriddenBy(org.netbeans.modules.csl.spi.ParserResult, org.netbeans.modules.csl.api.ElementHandle)}
     * method may return non-empty result.
     *
     * @param info
     * @param handle
     * @return false is the {@link OverridingMethods#overriddenBy(org.netbeans.modules.csl.spi.ParserResult, org.netbeans.modules.csl.api.ElementHandle)}
     *               cannot return non-empty result for the given <code>handle</code>.
     */
    public boolean isOverriddenBySupported(@NonNull ParserResult info, @NonNull ElementHandle handle);

    /**Return references to methods that override <code>handle</code>.
     *
     * @param info
     * @param handle
     * @return null is equivalent to empty collection
     */
    public @CheckForNull Collection<? extends AlternativeLocation> overriddenBy(@NonNull ParserResult info, @NonNull ElementHandle handle);
    
}
