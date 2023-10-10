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
package org.netbeans.modules.javascript2.model.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public interface JsObject extends JsElement {
    public Identifier getDeclarationName();
    public Map <String, ? extends JsObject> getProperties();
    public void addProperty(String name, JsObject property);
    public JsObject getProperty(String name);

    /**
     *
     * @return the object within this is declared
     */
    public JsObject getParent();
    List<Occurrence> getOccurrences();

    public void addOccurrence(OffsetRange offsetRange);

    public String getFullyQualifiedName();
    /**
     *
     * @param offset
     * @return
     */
    Collection<? extends TypeUsage> getAssignmentForOffset(int offset);

    Collection<? extends TypeUsage> getAssignments();

    int getAssignmentCount();

    public void addAssignment(TypeUsage typeName, int offset);
    public void clearAssignments();

    public boolean isAnonymous();
    public void setAnonymous(boolean value);
    public boolean isDeprecated();


    /**
     *
     * @return true if the element is virtual and shouldn't be visible to the user in structure scanner.
     */
    boolean isVirtual();

    /**
     *
     * @return true if the object/function is identified by a name.
     * False if the function is declared as an item in array or the name is an expression
     */
    public boolean hasExactName();

    public Documentation getDocumentation();
    public void setDocumentation(Documentation documentation);

    public boolean containsOffset(int offset);

    public boolean moveProperty(String name, JsObject newParent);
}
