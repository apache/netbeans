/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
