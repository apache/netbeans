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


package org.netbeans.modules.form;

import org.openide.nodes.Node;

/**
 *
 * @author Ian Formanek
 */
public abstract class CodeGenerator {

    /**
     * initializes a CodeGenerator for a given FormModel
     * @param formModel a FormModel object
     */
    public abstract void initialize(FormModel formModel);

    /**
     * Alows the code generator to provide synthetic properties for specified
     * component which are specific to the code generation method.  E.g. a
     * JavaCodeGenerator will return variableName property, as it generates
     * global Java variable for every component
     * @param component The RADComponent for which the properties are to be
     * obtained
     * @return synthetic properties for the given component.
     */
    public Node.Property[] getSyntheticProperties(RADComponent component) {
        return new Node.Property[0];
    }

    public abstract void regenerateCode();

    /**
     * Generates the specified event handler, if it does not exist yet.
     * @param handlerName The name of the event handler
     * @param paramTypes the list of event handler parameter types
     * @param bodyText the body text of the event handler or null for default
     *(empty) one
     * @return true if the event handler have not existed yet and was creaated,
     * false otherwise
     */

//    public abstract boolean generateEventHandler(String handlerName,
//                                                 String[] paramTypes,
//                                                 String[] exceptTypes,
//                                                 String bodyText);

    /**
     * Changes the text of the specified event handler, if it already exists.
     * @param handlerName The name of the event handler
     * @param paramTypes the list of event handler parameter types
     * @param bodyText the new body text of the event handler or null for default
     *(empty) one
     * @return true if the event handler existed and was modified, false
     * otherwise
     */

//    public abstract boolean changeEventHandler(final String handlerName,
//                                               final String[] paramTypes,
//                                               final String[] exceptTypes,
//                                               final String bodyText);

    /**
     * Removes the specified event handler - removes the whole method together
     * with the user code!
     * @param handlerName The name of the event handler
     */

//    public abstract boolean deleteEventHandler(String handlerName);

    /**
     * Renames the specified event handler to the given new name.
     * @param oldHandlerName The old name of the event handler
     * @param newHandlerName The new name of the event handler
     * @param paramTypes the list of event handler parameter types
     */

//    public abstract boolean renameEventHandler(String oldHandlerName,
//                                               String newHandlerName,
//                                               String[] exceptTypes,
//                                               String[] paramTypes);

    /** 
     * Gets the body (text) of event handler of given name.
     * @param handlerName name of the event handler
     * @return text of the event handler body
     */

//    public abstract String getEventHandlerText(String handlerName);

    /** Focuses the specified event handler in the editor. */

//    public abstract void gotoEventHandler(String handlerName);

    /** 
     * Returns whether the specified event handler is empty (with no user
     * code). Empty handlers can be deleted without user confirmation.
     * @return true if the event handler exists and is empty
     */
//    public boolean isEventHandlerEmpty(String handlerName) {
//        return false;
//    }
}
