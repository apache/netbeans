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
