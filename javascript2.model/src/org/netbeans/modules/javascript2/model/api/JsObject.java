/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
