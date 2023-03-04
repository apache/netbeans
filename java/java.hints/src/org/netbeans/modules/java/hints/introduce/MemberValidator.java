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
package org.netbeans.modules.java.hints.introduce;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;

/**
 *
 * @author sdedic
 */
public interface MemberValidator {
    /**
     * Validates whether that the entered name is correct, and that it does
     * not conflict with other members of the target.
     * <p/>
     * If the declaration overrides other accessible one, the method may return 
     * minimum access modifiers; UI will restrict the choice.
     * <p/>
     * The Validator should issue an information message in that case.
     * 
     * @param target the target type
     * @param n the requested name
     * @return minimum access modifiers
     */
    public MemberSearchResult validateName(TreePathHandle target, String n);
}
