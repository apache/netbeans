/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.angular.model;

/**
 *
 * @author marekfukala
 */
public enum DirectiveType {

    /**
     * Directive has no value.
     *
     * For example ngChange.
     */
    noValue,
    /**
     * {expression} – Expression to evaluate.
     *
     * Used for example by nbBind.
     */
    expression,
    /**
     * {string} – template of form {{ expression }} to eval.
     *
     * Used for example by ngBindTemplate.
     */
    string,
    /**
     * {angular.Module} – an optional application module name to load.
     *
     * Used by ngApp. http://docs.angularjs.org/api/ng.directive:ngApp
     */
    angularModule,
    /**
     * {template} – any string which can contain {{}} markup.
     *
     * Used for example by ngHref.
     */
    template,
    /**
     * ngRepeat – {repeat_expression} – The expression indicating how to
     * enumerate a collection. 
     * 
     * Two formats are currently supported: variable in
     * expression – where variable is the user defined loop variable and
     * expression is a scope expression giving the collection to enumerate.
     *
     * For example: track in cd.tracks.
     *
     * (key, value) in expression – where key and value can be any user defined
     * identifiers, and expression is the scope expression giving the collection
     * to enumerate.
     *
     * For example: (name, age) in {'adam':10, 'amalie':12}.
     */
    repeatExpression,
     /**
     * {object} - any JavaScript object.
     * Used for example by ngModelOptions
     */
    object,
    /**
     * ngOptions - {comprehension_expression} - The expression used to
     * dynamically generate a list of <option> elements for the <select>
     * element.
     *
     * For example: "label disable when condition for value in array track by
     * trackexpr"
     */
    comprehensionExpression
}
