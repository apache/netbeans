/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
