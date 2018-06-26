/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.beans.api.model;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;


/**
 * Represent eligible for injection element search result. 
 * 
 * @author ads
 *
 */
public interface DependencyInjectionResult {
    
    enum ResultKind {
        /**
         * This kind correspond to Error result only
         */
        DEFINITION_ERROR,
        /**
         * This kind represents at least InjectableResult and ResolutionResult.
         * Also there could be additional hints with set of eligible for injection
         * elements ( which are disabled , turned off alternatives, .... )
         * represented by ApplicableResult.
         */
        INJECTABLE_RESOLVED,
        /**
         * - No eligible for injection element found at all 
         * - Only disabled beans are found
         * - Ambiguous dependencies result   
         * It could be represented by Error only ( nothing found
         * at all ) or Error, ResolutionResult and ApplicableResult with 
         * information about probable eligible for injection elements. 
         */
        RESOLUTION_ERROR,
        /**
         * This kind is like INJECTABLE_RESOLVED but it can contain 
         * several eligible for injection elements from very beginning.
         * It is used when multiple eligible for injection elements are
         * valid result. F.e. it is normal to find a number elements 
         * via programmatic lookup .   
         * It is represented at least by ApplicableResult and ResolutionResult.
         */
        INJECTABLES_RESOLVED,
    }
    
    /**
     * @return element injection point which is used for injectable search
     */
    VariableElement getVariable();
    
    TypeMirror getVariableType();
    
    ResultKind getKind();
    
    interface Error extends DependencyInjectionResult {
        
        String getMessage();
    }
    
    interface ResolutionResult extends DependencyInjectionResult, Result {
        
        /**
         * Check whether <code>element</code> is alternative.
         * <code>element</code> could be eligible for injection element
         * ( which is found as result here ) or stereotype. 
         * @param element checked element 
         * @return true if <code>element</code> is alternative
         */
        boolean isAlternative( Element element );
        
        boolean hasAlternative( Element element );
    }
    
    interface InjectableResult extends DependencyInjectionResult {
        /**
         * <code>null</code> is returned if there is no eligible element for injection
         * ( no element which could be a pretender).
         * 
         * it could be a result of unsatisfied or ambiguous dependency.
         * F.e. unsatisfied dependency : there is a pretender satisfy typesafe 
         * resolution but something incorrect ( parameterized type is not valid , etc. ). 
         * Ambiguous dependency : there are a number of appropriate elements.
         *
         * 
         * @return element ( type definition, production field/method) 
         * that is used in injected point identified by {@link #getVariable()}
         */
        Element getElement();
    }
    
    interface ApplicableResult extends DependencyInjectionResult, BeansResult {
        public Set<TypeElement> getTypeElements();
        
        public Set<Element> getProductions();
    }
}
