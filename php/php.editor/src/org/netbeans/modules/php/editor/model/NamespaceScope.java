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

package org.netbeans.modules.php.editor.model;

import java.util.Collection;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
/**
 * @author Radek Matous
 */
public interface NamespaceScope extends VariableScope, FullyQualifiedElement {

    QualifiedName getQualifiedName();
    /**
     * Gets all 'use's, included the ones from 'group use's.
     * <p>
     * The 'use's from 'group use's return complete namespace name
     * (including the prefix, the common part). It means that this
     * method is suitable for most of the situations.
     * @return all 'use's, included the ones from 'group use's
     */
    Collection<? extends UseScope> getAllDeclaredSingleUses();
    /**
     * @return all single 'use's (not 'group use's)
     */
    Collection<? extends UseScope> getDeclaredSingleUses();
    /**
     * Basically no need to use this method since registration of 'group use' registers
     * individual parts as single uses.
     * @return all 'group use's
     * @see #getAllDeclaredSingleUses()
     */
    Collection<? extends GroupUseScope> getDeclaredGroupUses();
    Collection<? extends TypeScope> getDeclaredTypes();
    Collection<? extends ClassScope> getDeclaredClasses();
    Collection<? extends InterfaceScope> getDeclaredInterfaces();
    Collection<? extends TraitScope> getDeclaredTraits();
    Collection<? extends EnumScope> getDeclaredEnums();
    Collection<? extends ConstantElement> getDeclaredConstants();
    Collection<? extends FunctionScope> getDeclaredFunctions();
    boolean isDefaultNamespace();
    FileScope getFileScope();

}
