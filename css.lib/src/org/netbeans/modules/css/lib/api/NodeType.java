/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only (GPL) or the Common
 * Development and Distribution License(CDDL) (collectively, the
 * License). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the Classpath exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * Portions Copyrighted [year] [name of copyright owner]
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api;

/**
 * Node types for all the CSS3 ANTLR grammar elements.
 * 
 * This enum must keep in sync with the ANTLR grammar set of rules!!!
 *
 * @author mfukala@netbeans.org
 */
public enum NodeType {

    /**
     * Identification of an @-rule.
     * 
     * For example represents the 'bounce' text in following code: 
     * <pre>
     * @-webkit-keyframes 'bounce' { ...
     * </pre>
     * 
     * IDENT | STRING 
     */
    atRuleId,
    
    /**
     * A list of bodyItem-s
     */
    body,
    
    /**
     * rule | media | page | counterStyle | fontFace | moz_document
     */
    bodyItem,
    
    combinator,
    /**
     * @counter-style <counter-name> { ... }
     */
    counterStyle,
    cssId,
    cssClass,
    declaration,
    declarations,
    elementSubsequent,
    elementName,
    esPred,
    expression,
    /**
     * @font-face { ... }
     */
    fontFace,
    /**
     * Attribute name - value pair in a css function: h1 { -moz-xxx:
     * draw(shape="rect") }
     */
    fnAttribute,
    /**
     * Attribute name in a css function: h1 { -moz-xxx: draw(shape="rect") }
     */
    fnAttributeName,
    /**
     * Attribute value in a css function: h1 { -moz-xxx: draw(shape="rect") }
     */
    fnAttributeValue,
    function,
    functionName,
     /**
         * any @xxxx { ... } generic at-rule
         */
     generic_at_rule,

    /**
     * @charset "..." rule
     */
    charSet,
    /**
     * The charset string in the @charset rule
     */
    charSetValue,
    invalidRule,
    hexColor,
    margin,
    margin_sym,
    media,
    mediaExpression,
    mediaFeature,
    mediaQueryList,
    mediaQuery,
    
    /**
     * NOT | ONLY prefix of the media query
     */
    mediaQueryOperator,
    
    
    mediaType,
    
    mediaBody,
    mediaFeatureValue,
    
    /**
     * Mozilla specific at rule @-moz-document See
     * https://developer.mozilla.org/en/CSS/@-moz-document
     */
    moz_document,
    /**
     * Mozilla @-moz-document at rule argument See
     * https://developer.mozilla.org/en/CSS/@-moz-document
     */
    moz_document_function,
    /**
     * Namespace declaration: @namespace prefix "http://myns";
     */
    namespace,
    
    /**
     * List of namespace-s
     */
    namespaces,
    /**
     * Namespace prefix or wildcard prefix of the css qualified name: myns|E {}
     * || *|E {}
     */
    namespacePrefix,
    /**
     * The namespace prefix
     */
    namespacePrefixName,
    operator,
    page,
    pseudoPage,
    property,
    prio,
    pseudo,
    /**
     * String or URI, used in namespace and import rules
     */
    resourceIdentifier,
    /**
     * @import ... rule
     */
    importItem,
    
    /**
     * list of importItem-s
     */
    imports,
    
    /**
     * Value of the css property - color : "red";
     */
    propertyValue,
    
    rule,
    /**
     * a CSS rule: div { ... }
     */
    
    selector,
    simpleSelectorSequence,
    /**
     * Attribute name - value pair in selectors h1[attr=value] {}
     */
    slAttribute,
    /**
     * Attribute name in selectors h1[attr=value] {}
     */
    slAttributeName,
    /**
     * Attribute value in selectors h1[attr=value] {}
     */
    slAttributeValue,
    /**
     * error recovery rule
     */
    syncToFollow,
    /**
     * error recovery rule
     */
    syncToDeclarationsRule,
    
    syncTo_RBRACE,
    syncTo_SEMI,
    
    synpred2_Css3,
    selectorsGroup,
    styleSheet,
    /**
     * syntactic predicate
     */
    synpred1_Css3,
    /**
     * syntactic predicate
     */
    synpred3_Css3,
    term,
    typeSelector,

    /**
     * an artificial root node of each parse tree
     * 
     * The node types doesn't correspond to any of the grammar rules (do not remove it!)
     */
    root,
    /**
     * an error node
     * 
     * The node types doesn't correspond to any of the grammar rules (do not remove it!)
     */
    error,
    /**
     * an error node, but for errors recovered - skipped (resynced) content by
     * syncToBitSet(...)
     * 
     * The node types doesn't correspond to any of the grammar rules (do not remove it!)
     */
    recovery,
    /**
     * a token node (each lexer token has its node in the parse tree)
     * 
     * The node types doesn't correspond to any of the grammar rules (do not remove it!)
     */
    token,
    unaryOperator,
    
    /**
     * A vendor specific @-rule.
     * 
     * Example: @-moz-document rule
     */
    vendorAtRule,
    
    /**
     * @-webkit-keyframes vendor specific rule.
     * 
     * Example:
     * <pre>
     * @-webkit-keyframes 'bounce' {
     * 
     *    from, 20% {
     *      top: 100px;
     *      -webkit-animation-timing-function: ease-out;
     *    }
     * 
     * }
     * </pre>
     * 
     */
    webkitKeyframes,
    
    /**
     * @-webkit-keyframes content block
     * 
     * Example:
     * <pre>
     *    from, 20% {
     *      top: 100px;
     *      -webkit-animation-timing-function: ease-out;
     *    }
     * </pre>
     */
    webkitKeyframesBlock,
    
    /**
     * @-webkit-keyframes content block selectors
     * 
     * Example:
     * <pre>
     * from, 20%
     * </pre>
     */
    webkitKeyframeSelectors,
    
    /**
     * whitespace, new line or comment tokens
     */
    ws,
    
    //*** LESS/SCSS syntax ***
    
    /**
     * color: "@color";
     */
    cp_variable,
    
    /**
     * "@color: #4D926F;"
     */
    cp_variable_declaration,
   
    /**
     * Same as expression, but allows more operators.
     * 
     * color: ("@base-color * 3");
     */
    cp_math_expression,
    cp_math_expressions,
    cp_math_expression_atom,
    
    /**
     * ".box-shadow (@x: 0, @y: 0, @blur: 1px, @color: #000)"
     */
    cp_mixin_declaration,
    
    cp_mixin_call,
    /**
     * .box-shadow ("@x: 0, @y: 0, @blur: 1px, @color: #000")
     */
    cp_args_list,
    
    /**
     * .box-shadow ("@x: 0", @y: 0, @blur: 1px, @color: #000)
     */
    cp_arg,
    
    less_mixin_guarded,
    less_condition,
    less_condition_operator,
    less_function_in_condition,
    less_fn_name,
    
    cp_mixin_call_args,
    cp_mixin_name,
    
    
    sass_interpolation_expression_var,
    
    sass_nested_properties,
    sass_extend,
    sass_extend_only_selector,
    sass_debug,
    
    sass_control,
    sass_if,
    sass_else,
    sass_for,
    sass_each,
    
    /**
     * List of variables in the SASS for each loop:
     * 
     * @each "$animal, $color, $cursor" in $animals {
     *      .#{$animal}-icon {
     *          background-image: url('/images/#{$animal}.png');
     *          border: 2px solid $color;
     *          cursor: $cursor;
     *      }
     * }
     */
    sass_each_variables,
    sass_while,
    sass_control_block,
    sass_control_expression,
    
    sass_function_declaration,
    sass_function_name,
    sass_function_return,
    
    sass_content,
    
    cp_mixin_call_arg,
    
    cp_expression_atom,
    cp_expression,
    cp_expression_operator,
    cp_expression_list,
    
    cp_propertyValue,
            
    at_rule,
    
    propertyDeclaration,

    fnAttributes,
    
    cp_mixin_block,
    
    mediaBodyItem,
    
    cp_term_symbol,
    
    less_selector_interpolation,
    
    /**
     * SASS values map
     * 
     * "$colors: (
     *     header: #b06,
     *     text: #334,
     *     footer: #666777,
     * )"
     */
    sass_map,
    
     /**
     * SASS values map -- the name of the map variable (including the dollar sign)
     * 
     * "$colors": (
     *     header: #b06,
     *     text: #334,
     *     footer: #666777,
     * )
     */
    sass_map_name,
    
    
    /**
     * SASS values map -- content of the parenthesis
     * 
     * $colors: ("
     *     header: #b06,
     *     text: #334,
     *     footer: #666777,
     * ")
     */
    sass_map_pairs,
    
    /**
     * SASS values map -- the key: value pair
     * 
     * $colors: (
     *     "header: #b06",
     *     "text: #334",
     *     "footer: #666777",
     * )
     */
    sass_map_pair
    
    , less_import_types
    , less_when
    , key_and
    , key_or
    , key_only
    
    , sass_selector_interpolation_exp
    , sass_error
    , less_selector_interpolation_exp
    ;
    
    
}
