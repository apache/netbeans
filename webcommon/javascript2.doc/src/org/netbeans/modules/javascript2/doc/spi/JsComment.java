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
package org.netbeans.modules.javascript2.doc.spi;

import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationPrinter;
import org.netbeans.modules.javascript2.types.api.Type;

/**
 * Base class which represents JavaScript documentation comment.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class JsComment {

    private final OffsetRange offsetRange;

    /**
     * Creates new JavaScript comment block.
     * @param offsetRange offset of the comment block
     */
    public JsComment(OffsetRange offsetRange) {
        this.offsetRange = offsetRange;
    }

    /**
     * Gets offsets of this comment.
     * @return start and end offsets
     */
    public OffsetRange getOffsetRange() {
        return offsetRange;
    }

    /**
     * Gets formated documentation for the CC doc window.
     * @return formated documentation text
     */
    public final String getDocumentation() {
        return JsDocumentationPrinter.printDocumentation(this);
    }

    public abstract List<String> getSummary();

    public abstract List<String> getSyntax();

    public abstract DocParameter getReturnType();

    public abstract List<DocParameter> getParameters();

    /**
     * Gets the information about deprecation.
     *
     * @return {@code null} when the element is not deprecated, any String including the empty one when the element is
     * deprecated and the documentation can show additional description which was returned here.
     */
    public abstract String getDeprecated();

    /**
     * Gets all exceptions, errors which can throw the commented element.
     *
     * @return list of throws, empty list if no thrown, never {@code null}
     */
    public abstract List<DocParameter> getThrows();

    /**
     * Gets all extends of the elements. Informs about inheritance.
     *
     * @return list of extends, empty list if no extends, never {@code null}
     */
    public abstract List<Type> getExtends();

    /**
     * Gets all "see" information of the comment.
     *
     * @return list of sees, empty list if no one exists, never {@code null}
     */
    public abstract List<String> getSee();

    /**
     * Gets since information of the comment.
     *
     * @return since information if exists, {@code null} otherwise
     */
    public abstract String getSince();

//    /**
//     * Gets all author information of the comment.
//     *
//     * @return list of authors, empty list if no one exists, never {@code null}
//     */
//    public abstract List<String> getAuthor();
//
//    /**
//     * Gets version information of the comment.
//     *
//     * @return version information if exists, {@code null} otherwise
//     */
//    public abstract String getVersion();

    /**
     * Gets all example information of the comment.
     *
     * @return list of examples, empty list if no one exists, never {@code null}
     */
    public abstract List<String> getExamples();

    public abstract Set<JsModifier> getModifiers();

    public abstract boolean isClass();
    
    public abstract boolean isConstant();
    
    /**
     * Gets all defined properties in the comment
     * @return  list of properties, never {@code null}
     */
    public abstract List<DocParameter> getProperties();
    
    /**
     * If there is defined an object (type) inside the comment. Like @typedef in JsDoc
     * @return the type defined in the comment or null, if there is not defined any type.
     */
    public abstract DocParameter getDefinedType();
    
    /**
     * If there are defined types in the comment, like @type in jsdoc, then are returns by this method
     * @return types mentioned in the comment
     */
    public abstract List<Type> getTypes();
    
    /**
     * If there is defined an callback in the comment. Like @callback in JsDoc
     * @return the type of the callback or null, if there is not defined any callback in the comment.
     */
    public abstract Type getCallBack();
}
