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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.html.angular.AngularDoc;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import static org.netbeans.modules.html.angular.model.DirectiveType.*;

/**
 *
 * @author marekfukala
 */
public enum Directive {
    //      requir.|attr |class |element
    app     (false, true, true,  false, angularModule), 
    bind    (true,  true, true,  false, expression),
    bindHtml(true,  true, false, false, expression),
    bindHtmlUnsafe
            (true,  true, true,  false, expression), 
    bindTemplate
            (true,  true, true,  false, expression),
    blur    (true,  true, true,  false, expression),
    change  (true,  true, false, true,  expression), 
    checked (true,  true, false, false, expression),
    _class  (true,  true, true,  false, expression), //real name is "class"
    classEven
            (true,  true, true,  false, expression),
    classOdd(true,  true, true,  false, expression),
    click   (true,  true, true,  false, expression),
    cloak   (true,  true, true,  false, noValue),
    controller
            (true,  true, true,  false, expression),
    copy    (true,  true, true,  false, expression),
    csp     (false, true, true,  false, noValue),
    cut     (true,  true, true,  false, expression),
    dblclick(true,  true, true,  false, expression),
    disabled(true,  true, false, false, expression),
    focus   (true,  true, false, false, expression),
    form    (true,  true, true,  true,  string),
    hide    (true,  true, true,  false, expression),
    href    (true,  true, false, false, template),
    _if     (true,  true, true,  true,  expression), // real name is "if"
    include (true,  true, true,  true,  expression),
    init    (true,  true, true,  false, expression),
    jq      (false, true, false, false, string),
    keydown (true,  true, true,  false, expression),
    keypress(true,  true, true,  false, expression),
    keyup   (true,  true, true,  false, expression),
    link    (true,  true, false, false, object),
    list    (true,  true, true,  false, string),
    model   (true,  true, true,  false, expression),
    modelOptions
            (true,  true, false, false, object),
    mousedown
            (true,  true, true,  false, expression),
    mouseenter
            (true,  true, true,  false, expression),
    mouseleave
            (true,  true, true,  false, expression),
    mousemove
            (true,  true, true,  false, expression),
    mouseover
            (true,  true, true,  false, expression),
    mouseup (true,  true, true,  false, expression),
    multiple(true,  true, false, false, expression),
    nonBindable
            (true,  true, true,  false, noValue),
    open    (true,  true, true,  false, expression),
    options (true,  true, false, false, comprehensionExpression),
    paste   (true,  true, true,  false, expression),
    //TODO add sub directives
    pluralize
            (true,  true, false, true,  noValue),

    //TODO add sub directives
    readonly(false, true, false, false, expression),
    repeat  (true,  true, true,  false, repeatExpression),
    repeatStart
            (true,  true, false, false, repeatExpression),
    repeatEnd
            (false, true, false, false, noValue),
    selected(false, true, false, false, expression),
    show    (true,  true, true,  false, expression),
    src     (true,  true, false, false, template),
    srcset  (true,  true, false, false, template),
    style   (true,  true, true,  false, expression),
    submit  (true,  true, true,  false, expression),

    _switch (true,  true, false, true,  expression), //??? //real name is "switch"
    _switch_when
            (true,  true, false, true,  string),
    _switch_default
            (true,  true, false, true,  string),
    transclude
            (true,  true, true,  false, noValue),
    value   (true,  true, false, false, string),
    viewport(true,  true, true,  true,  string),
    view    (false, true, true,  true,  noValue);
    
    //ngdoc parser is here: https://github.com/angular/angular.js/blob/master/docs/src/ngdoc.js
    //the directives documentation in .ngdoc format is here: https://github.com/angular/angular.js/blob/master/src/ng/directive/ngController.js
    private static final String PARTIAL_DOC_URL_BASE = "https://code.angularjs.org/" + AngularDoc.DOC_VERSION + "/docs/partials/api/ng/directive/"; //NOI18N
    private static final String PARTIAL_SUFFIX = ".html"; //NOI18N
    
    private static final String DOC_URL_BASE = "http://docs.angularjs.org/api/ng.directive:"; //NOI18N
    
    public static final String NAME_PREFIX = "ng";
    
    public static boolean isAngularAttribute(Attribute attribute) {
        return DirectiveConvention.getConvention(attribute.unqualifiedName()) != null;
    }
    
    private static final Map<String, Directive> NAMES2DIRECTIVES = new HashMap<>();
    static {
        for(Directive d : values()) {
            for(DirectiveConvention dc : DirectiveConvention.values()) {
                NAMES2DIRECTIVES.put(d.getAttributeName(dc), d);
            }
        }
    }
    
    /**
     * Gets an instance of {@link Directive} for an angular attribute name.
     * 
     * Attribute names in all supported forms can be used.
     * 
     * @param attributeName
     */
    public static Directive getDirective(String attributeName) {
        return NAMES2DIRECTIVES.get(attributeName);
    }
    
    private boolean attributeValueTypicallyUsed;

    private boolean useAsAttribute, useAsClass, useAsElement;
    
    private DirectiveType type;
    
    private Directive(
            boolean attributeValueRequired, 
            boolean useAsAttribute, 
            boolean useAsClass, 
            boolean useAsElement,
            DirectiveType type) {
        this.attributeValueTypicallyUsed = attributeValueRequired;
        this.useAsAttribute = useAsAttribute;
        this.useAsClass = useAsClass;
        this.useAsElement = useAsElement;
        this.type = type;
    }

    public String getExternalDocumentationURL() {
        return new StringBuilder().append(DOC_URL_BASE)
                .append(NAME_PREFIX)
                .append(Character.toUpperCase(getCleanCoreName().charAt(0)))
                .append(getCleanCoreName().substring(1))
                .toString();
    }
    
    /**
     * Workaround for the "I'm a teapot" embedded browser issue:
     * https://netbeans.org/bugzilla/show_bug.cgi?id=229689
     */
    public String getExternalDocumentationURL_partial() {
        return new StringBuilder().append(PARTIAL_DOC_URL_BASE)
                .append(NAME_PREFIX)
                .append(Character.toUpperCase(getCleanCoreName().charAt(0)))
                .append(getCleanCoreName().substring(1))
                .append(PARTIAL_SUFFIX)
                .toString();
    }
    
    /**
     * Gets the directive name as html attribute using the given convention
     */
    @NonNull
    public String getAttributeName(DirectiveConvention convention) {
        return convention.createFQN(this);
    }

   /**
     * Resolves the attribute name to the word-by-dash-separated form.
     * "bind-html-unsafe", "class-even",...
     */
    String getAttributeCoreName(char delimiter) {
         StringBuilder sb = new StringBuilder();
        //class name workaround
        String name = getCleanCoreName();
        for(int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if(Character.isUpperCase(c)) {
                sb.append(delimiter);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

     /**
     * Name of the directive. Use this method instead of {@link #name()}.
     */
    private String getCleanCoreName() {
        String name = name().charAt(0) == '_' ? name().substring(1) : name();
        if (name.indexOf('_') > -1) {
            name = name.replace('_', '-');
        }
        return name;
    }
     
    public boolean isAttributeValueTypicallyUsed() {
        return attributeValueTypicallyUsed;
    }
    
    public boolean canUseAsAttribute() {
        return useAsAttribute;
    }
    
    public boolean canUseAsClass() {
        return useAsClass;
    }
    
    public boolean canUseAsElement() {
        return useAsElement;
    }

    public DirectiveType getType() {
        return type;
    }
    
}
