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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.api.model.xref;

import org.netbeans.modules.cnd.api.model.*;

/**
 * reference object in file
 * i.e.
 * A* B::foo() {
 * }
 * => there are 3 reference objects:
 * 1) "A" pointed to class A
 * 2) "B" pointed to class B
 * 3) "foo" pointed to declaration of method foo in class B
 *
 * reference object could have owner. 
 * Owner reference is the connection between model objects and references.
 * Could be used for instance for searching the scope of reference.
 * in the example above:
 * - reference "1" has as owner return type of method definition
 * - reference "2" has owner method definition
 * - reference "3" has owner method definition as well
 *
 *TODOD: think about example
 * #define MACRO(x) #x
 * #include MACRO(file.h)
 * what are the references and owners?
 *
 */
public interface CsmReference extends CsmOffsetable {
    /**
     * do not use this method, use CsmReferenceResolver.isKindOf instead
     * @return kind of object
     */
    CsmReferenceKind getKind();
    
    /**
     * returns referenced object
     * this could be long operation of resolving, do not call in EQ
     */
    CsmObject getReferencedObject();
    
    CsmObject getOwner();
    
    /**
     * return the closest top level container of this reference
     * @return object which is the closest top level container of this reference
     */
    CsmObject getClosestTopLevelObject();
}
