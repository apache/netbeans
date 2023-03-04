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
package org.netbeans.modules.groovy.editor.api.completion;

/**
 *
 * @author schmidtm
 */
public enum GroovyKeyword {
    // Java keywords:                      groovy above  ouside inside code
    KEYWORD_as           ("as"           , false, true , false, false, false, KeywordCategory.KEYWORD),
    KEYWORD_assert       ("assert"       , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_abstract     ("abstract"     , false, true , true , false, false, KeywordCategory.KEYWORD),
    KEYWORD_break        ("break"        , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_case         ("case"         , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_catch        ("catch"        , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_class        ("class"        , false, true , true , false, false, KeywordCategory.KEYWORD),
    KEYWORD_continue     ("continue"     , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_default      ("default"      , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_do           ("do"           , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_else         ("else"         , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_enum         ("enum"         , false, true , true , false, false, KeywordCategory.KEYWORD),
    KEYWORD_extends      ("extends"      , false, false, true , false, true , KeywordCategory.KEYWORD),
    KEYWORD_finally      ("finally"      , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_for          ("for"          , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_if           ("if"           , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_implements   ("implements"   , false, false, true , false, false, KeywordCategory.KEYWORD),
    KEYWORD_import       ("import"       , false, true , false, false, false, KeywordCategory.KEYWORD),
    KEYWORD_instanceof   ("instanceof"   , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_interface    ("interface"    , false, true , true , false, false, KeywordCategory.KEYWORD),
    KEYWORD_new          ("new"          , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_package      ("package"      , false, true , false, false, false, KeywordCategory.KEYWORD),
    KEYWORD_return       ("return"       , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_super        ("super"        , false, false, false, true , true , KeywordCategory.KEYWORD),
    KEYWORD_switch       ("switch"       , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_this         ("this"         , false, false, false, true , true , KeywordCategory.KEYWORD),
    KEYWORD_throw        ("throw"        , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_throws       ("throws"       , false, false, false, false, false, KeywordCategory.KEYWORD),
    KEYWORD_trait        ("trait"        , false ,true , true , true , false, KeywordCategory.KEYWORD),
    KEYWORD_try          ("try"          , false, false, false, false, true , KeywordCategory.KEYWORD),
    KEYWORD_while        ("while"        , false, false, false, false, true , KeywordCategory.KEYWORD),
    // Uniq Groovy keywords:
    KEYWORD_def          ("def"          , true , false, true , true , true , KeywordCategory.KEYWORD),
    KEYWORD_in           ("in"           , true , false, true , false, true , KeywordCategory.KEYWORD),
    KEYWORD_property     ("property"     , true , false, true , true , true , KeywordCategory.KEYWORD),
    // Java primitive types:
    KEYWORD_boolean      ("boolean"      , false, false, false, true , true , KeywordCategory.PRIMITIVE),
    KEYWORD_byte         ("byte"         , false, false, false, true , true , KeywordCategory.PRIMITIVE),
    KEYWORD_char         ("char"         , false, false, false, true , true , KeywordCategory.PRIMITIVE),
    KEYWORD_double       ("double"       , false, false, false, true , true , KeywordCategory.PRIMITIVE),
    KEYWORD_float        ("float"        , false, false, false, true , true , KeywordCategory.PRIMITIVE),
    KEYWORD_int          ("int"          , false, false, false, true , true , KeywordCategory.PRIMITIVE),
    KEYWORD_long         ("long"         , false, false, false, true , true , KeywordCategory.PRIMITIVE),
    KEYWORD_short        ("short"        , false, false, false, true , true , KeywordCategory.PRIMITIVE),
    KEYWORD_void         ("void"         , false, false, false, true , true , KeywordCategory.PRIMITIVE),
    // Java modifiers
    KEYWORD_final        ("final"        , false, true , true , true , true , KeywordCategory.MODIFIER),
    KEYWORD_native       ("native"       , false, false, false, true , true , KeywordCategory.MODIFIER),
    KEYWORD_private      ("private"      , false, true , true , true , true , KeywordCategory.MODIFIER),
    KEYWORD_protected    ("protected"    , false, false, false, true , true , KeywordCategory.MODIFIER),
    KEYWORD_public       ("public"       , false, true , true , true , true , KeywordCategory.MODIFIER),
    KEYWORD_static       ("static"       , false, true , true , true , true , KeywordCategory.MODIFIER),
    KEYWORD_strictfp     ("strictfp"     , false, false, false, true , true , KeywordCategory.MODIFIER),
    KEYWORD_synchronized ("synchronized" , false, false, false, true , true , KeywordCategory.MODIFIER),
    KEYWORD_transient    ("transient"    , false, false, false, true , true , KeywordCategory.MODIFIER),
    KEYWORD_volatile     ("volatile"     , false, false, false, true , true , KeywordCategory.MODIFIER),
    
    KEYWORD_undefined    ("undefined"    , false, false, false, false, false, KeywordCategory.NONE);
    
    private String name;
    private boolean isGroovy;
    
    // This flag               maps to in CaretLocation:
    private boolean aboveFistClass;     // ABOVE_FIRST_CLASS
    private boolean outsideClasses;     // OUTSIDE_CLASSES
    private boolean insideClass;        // INSIDE_CLASS
    private boolean insideCode;         // INSIDE_METHOD || INSIDE_CLOSURE
    private KeywordCategory category;   // Keyword Category: keyword, primitive, modifier
    

    GroovyKeyword(String name,  boolean isGroovy, boolean aboveFistClass, 
                                boolean outsideClasses, boolean insideClass, boolean insideCode, KeywordCategory category) {
        this.name = name;
        this.isGroovy = isGroovy;
        this.aboveFistClass = aboveFistClass;
        this.outsideClasses = outsideClasses;
        this.insideClass = insideClass;
        this.insideCode = insideCode;
        this.category = category;
    }

    public boolean isAboveFistClass() {
        return aboveFistClass;
    }

    public KeywordCategory getCategory() {
        return category;
    }

    public boolean isInsideClass() {
        return insideClass;
    }

    public boolean isInsideCode() {
        return insideCode;
    }

    public boolean isGroovyKeyword() {
        return isGroovy;
    }

    public String getName() {
        return name;
    }

    public boolean isOutsideClasses() {
        return outsideClasses;
    }
}
