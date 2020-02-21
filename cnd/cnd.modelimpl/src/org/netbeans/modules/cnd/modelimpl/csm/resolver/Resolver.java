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

package org.netbeans.modules.cnd.modelimpl.csm.resolver;

import org.netbeans.modules.cnd.api.model.*;
import java.util.*;

/**
 */
public interface Resolver {
    public static final boolean TRACE_RECURSION = false;
    public static final int INFINITE_RECURSION = 200;
    public static final int LIMITED_RECURSION = 5;

    public static final int NAMESPACE = 1 << 0;
    public static final int CLASSIFIER = 1 << 1;
    public static final int CLASS = 1 << 2;
    public static final int TEMPLATE_CLASS = 1 << 3;
    public static final int CLASS_FORWARD = 1 << 4;
    public static final int ALL = NAMESPACE | CLASSIFIER | CLASS | TEMPLATE_CLASS | CLASS_FORWARD;

    public Collection<CsmProject> getLibraries();

    public CsmFile getStartFile();

    /**
     * Resolves classifier (class/enum/typedef) or namespace name.
     * Why classifier or namespace? Because in the case org::vk::test
     * you don't know which is class and which is namespace name
     *
     * @param nameTokens tokenized name to resolve
     * (for example, for std::vector it is new CharSequence[] { "std", "vector" })
     *
     * @return object of the following class:
     *  CsmClassifier (CsmClass, CsmEnum, CsmTypedef)
     *  CsmNamespace
     */
    public CsmObject resolve(CharSequence[] nameTokens, int interestedKind);
    
    /**
     * Check infinite recursion in resolving
     */
    public boolean isRecursionOnResolving(int maxRecursion);

    public CsmClassifier getOriginalClassifier(CsmClassifier orig);

    public interface SafeTemplateBasedProvider {
        boolean isTemplateBased(Set<CsmType> visited);
    }    
}
