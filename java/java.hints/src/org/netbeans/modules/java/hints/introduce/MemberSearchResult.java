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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;

/**
 *
 * @author sdedic
 */
public class MemberSearchResult {
    
    /**
     * A conflicting element
     */
    private final ElementHandle<? extends Element> conflicting;
    /**
     * Shadowed element, if any
     */
    private final ElementHandle<? extends Element> shadowed;
    /**
     * Type which introduced the to-be-shadowed element; useful for
     * refactoring to proper references.
     */
    private final ElementHandle<? extends TypeElement> shadowedGate;
    /**
     * Inherited element is shadowed; use super qualifier.
     */
    private final boolean gateSuper;
    /**
     * The required access modifier. Note that {@link Modifier#DEFAULT} is (mis)used
     * to indicate package-level access.
     */
    private final Modifier requiredModifier;
    
    private final TreePathHandle conflictingPath;
    
    private final ElementKind kind;

    public MemberSearchResult(ElementHandle<? extends Element> conflicting) {
        this.conflicting = conflicting;
        this.shadowed = null;
        this.shadowedGate = null;
        this.gateSuper = false;
        this.requiredModifier = null;
        this.conflictingPath = null;
        this.kind = conflicting.getKind();
    }
    
    public MemberSearchResult(ElementKind kind) {
        this.conflicting = null;
        this.shadowed = null;
        this.shadowedGate = null;
        this.gateSuper = false;
        this.requiredModifier = null;
        this.conflictingPath = null;
        this.kind = kind;
    }

    public MemberSearchResult(TreePathHandle conflicting, ElementKind kind) {
        this.conflicting = null;
        this.shadowed = null;
        this.shadowedGate = null;
        this.gateSuper = false;
        this.requiredModifier = null;
        this.conflictingPath = conflicting;
        this.kind = kind;
    }

    public MemberSearchResult(ElementHandle<? extends Element> shadowed, ElementHandle<? extends TypeElement> shadowedGate) {
        this.shadowed = shadowed;
        this.shadowedGate = shadowedGate;
        this.gateSuper = false;
        this.conflicting = null;
        this.requiredModifier = null;
        this.conflictingPath = null;
        this.kind = null;
    }

    public MemberSearchResult(ElementHandle<? extends Element> shadowed, ElementHandle<? extends TypeElement> shadowedGate, Modifier requiredModifier) {
        this.shadowed = shadowed;
        this.shadowedGate = shadowedGate;
        this.requiredModifier = requiredModifier;
        this.gateSuper = true;
        this.conflicting = null;
        this.conflictingPath = null;
        this.kind = null;
    }
    
    public ElementHandle<? extends Element> getOverriden() {
        return requiredModifier != null ? shadowed : null;
    }

    public ElementHandle<? extends Element> getConflicting() {
        return conflicting;
    }

    public TreePathHandle getConflictingPath() {
        return conflictingPath;
    }
    
    public ElementKind getConflictingKind() {
        return kind;
    }
    
    public Element resolveConflict(CompilationInfo info) {
        if (conflicting != null) {
            return conflicting.resolve(info);
        } else if (conflictingPath != null) {
            return conflictingPath.resolveElement(info);
        }
        return null;
    }
    
    public boolean isConflicting() {
        return conflicting != null || conflictingPath != null;
    }

    public ElementHandle<? extends Element> getShadowed() {
        return requiredModifier == null ? shadowed : null;
    }

    public ElementHandle<? extends TypeElement> getShadowedGate() {
        return shadowedGate;
    }

    public boolean isGateSuper() {
        return gateSuper;
    }

    public Modifier getRequiredModifier() {
        return requiredModifier;
    }
    
}
