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
    
    
    /**
     * @supports rule
     */
    , supportsAtRule
    , supportsCondition
    , supportsInParens
    , supportsFeature
    , supportsDecl
    , supportsWithOperator
    , supportsConjunction
    , supportsDisjunction

    /**
     * @use rule
     */
    , sass_use
    , sass_use_as
    , sass_use_with
    , sass_use_with_declaration
    /**
     * @forward rule
     */
    , sass_forward
    , sass_forward_as
    , sass_forward_with
    , sass_forward_with_declaration
    , sass_forward_hide
    , sass_forward_show
    // definitions to handle variable syntax
    , preservedToken
    , preservedTokenTopLevel
    , braceBlock
    , bracketBlock
    , parenBlock
    , componentValue
    , componentValueOuter
    // @layer rule
    , layerAtRule
    , importLayer
    , layerName
    , layerBody
    , layerStatement
    , layerBlock
    // @container
    , containerAtRule
    , containerCondition
    , containerQueryWithOperator
    , containerQueryConjunction
    , containerQueryDisjunction
    , containerQueryInParens
    , containerName
    , styleQuery
    , styleCondition
    , styleConditionWithOperator
    , styleQueryConjunction
    , styleQueryDisjunction
    , styleInParens
    , sizeFeature
    , sizeFeatureName
    , sizeFeatureValue
    , styleFeature
    , sizeFeatureFixedValue
    , sizeFeatureRangeSingle
    , sizeFeatureRangeBetweenLt
    , sizeFeatureRangeBetweenGt
    ;
}
