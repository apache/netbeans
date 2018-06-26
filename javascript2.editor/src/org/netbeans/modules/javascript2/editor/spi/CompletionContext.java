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
package org.netbeans.modules.javascript2.editor.spi;

/**
 *
 * @author Petr Hejl
 */
public enum CompletionContext {
    NONE, // There shouldn't be any code completion
    EXPRESSION, // usually, we will offer everything what we know in the context
    OBJECT_PROPERTY, // object property that are visible outside the object
    OBJECT_MEMBERS, // usually after this.
    /**
     * This context is before ':' in an object literal definition, when a property
     * is defined. Typically 
     * var object_listeral = {
     *  property_name : value
     * }
     * 
     * This context can be used by frameworks to suggest the names of properties
     * to define for example various configuration objects.
     */
    OBJECT_PROPERTY_NAME, 
    DOCUMENTATION, // inside documentation blocks
    GLOBAL,
    IN_STRING,      // inside a string
    STRING_ELEMENTS_BY_ID, // should offers css elements by id from project
    STRING_ELEMENTS_BY_CLASS_NAME, // should offers css elements by class name from project
    CALL_ARGUMENT, // the position when the cc is called at position of an argument of a function call
    NUMBER, // cc should offer methods of Number objects
    STRING, // cc should offer methods of String objects
    REGEXP,  // cc should offer methods of RegEx objects
    IMPORT_EXPORT_MODULE, // the position where js modules names should be offered
    IMPORT_EXPORT_SPECIAL_TOKENS //the position where as,from keywords should be displayed in CC list 
}
