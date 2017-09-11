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

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;

/**
 * This interface represents a choice outside a definition of a group.
 * @author Chris Webster
 */
public interface Choice extends ComplexExtensionDefinition,
ComplexTypeDefinition, SequenceDefinition, LocalGroupDefinition, SchemaComponent {
        public static final String MAX_OCCURS_PROPERTY  = "maxOccurs"; // NOI18N
	public static final String MIN_OCCURS_PROPERTY  = "minOccurs"; // NOI18N
	public static final String CHOICE_PROPERTY          = "choice"; // NOI18N
        public static final String GROUP_REF_PROPERTY       = "groupReference"; // NOI18N
        public static final String SEQUENCE_PROPERTY        = "sequence"; // NOI18N
        public static final String ANY_PROPERTY             = "any"; // NOI18N
        public static final String LOCAL_ELEMENT_PROPERTY   = "localElememnt"; // NOI18N
	public static final String ELEMENT_REFERENCE_PROPERTY = "elementReference"; // NOI18N
              
	Collection<Choice> getChoices();
	void addChoice(Choice choice);
	void removeChoice(Choice choice);
	
	Collection<GroupReference> getGroupReferences();
	void addGroupReference(GroupReference ref);
	void removeGroupReference(GroupReference ref);
	
	Collection<Sequence> getSequences();
	void addSequence(Sequence seq);
	void removeSequence(Sequence seq);
	
	Collection<AnyElement> getAnys();
	void addAny(AnyElement any);
	void removeAny(AnyElement any);
	
	Collection<LocalElement> getLocalElements();
	void addLocalElement(LocalElement element);
	void removeLocalElement(LocalElement element);
	
	Collection<ElementReference> getElementReferences();
	void addElementReference(ElementReference element);
	void removeElementReference(ElementReference element);
	
	/**
	 * return ability to set min and max occurs if appropriate, null 
	 * otherwise. This method
	 * should only be used after insertion into the model. 
	 */ 
	public Cardinality getCardinality();
    
}
