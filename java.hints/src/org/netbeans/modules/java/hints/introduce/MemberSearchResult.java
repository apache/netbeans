/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;

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

    public MemberSearchResult(ElementHandle<? extends Element> conflicting) {
        this.conflicting = conflicting;
        this.shadowed = null;
        this.shadowedGate = null;
        this.gateSuper = false;
        this.requiredModifier = null;
    }

    public MemberSearchResult(ElementHandle<? extends Element> shadowed, ElementHandle<? extends TypeElement> shadowedGate) {
        this.shadowed = shadowed;
        this.shadowedGate = shadowedGate;
        this.gateSuper = false;
        this.conflicting = null;
        this.requiredModifier = null;
    }

    public MemberSearchResult(ElementHandle<? extends Element> shadowed, ElementHandle<? extends TypeElement> shadowedGate, Modifier requiredModifier) {
        this.shadowed = shadowed;
        this.shadowedGate = shadowedGate;
        this.requiredModifier = requiredModifier;
        this.gateSuper = true;
        this.conflicting = null;
    }
    
    public ElementHandle<? extends Element> getOverriden() {
        return requiredModifier != null ? shadowed : null;
    }

    public ElementHandle<? extends Element> getConflicting() {
        return conflicting;
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
