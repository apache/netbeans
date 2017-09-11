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

package org.netbeans.modules.dbschema.nodes;

import java.beans.*;

import org.openide.nodes.*;

import org.netbeans.modules.dbschema.*;

/** Node representing a column pair.
 * @see ColumnElement
 */
public class ColumnPairElementNode extends DBMemberElementNode {
	/** Create a new column pair node.
	 * @param element column pair element to represent
	 * @param writeable <code>true</code> to be writable
	 */
	public ColumnPairElementNode(ColumnPairElement element,boolean writeable) {
		super(element, Children.LEAF, writeable);
	}

	/* Resolve the current icon base.
	 * @return icon base string.
	 */
	protected String resolveIconBase () {
        //PENDING - column pair element should be here
		return COLUMN;
	}

	/* Creates property set for this node */
	protected Sheet createSheet () {
		Sheet sheet = Sheet.createDefault();
		Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

		ps.put(createNameProperty(writeable));
		ps.put(createLocalColumnProperty(writeable));
		ps.put(createReferencedColumnProperty(writeable));

		return sheet;
	}
    
	/** Create a node property representing the element's name.
	 * @param canW if <code>false</code>, property will be read-only
	 * @return the property.
	 */
	protected Node.Property createNameProperty (boolean canW) {
		return new ElementProp(Node.PROP_NAME, String.class,canW) {
			/** Gets the value */
			public Object getValue () {
                return localColumnName() + ";" + referencedColumnName(); //NOI18N
			}
		};
	}

	/** Create a property for the column pair local column.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createLocalColumnProperty(boolean canW) {
		return new ElementProp(PROP_LOCAL_COLUMN, String.class, canW) {
			/** Gets the value */
			public Object getValue () {
                return localColumnName();
			}
		};
	}
    
	/** Create a property for the column pair referenced column.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createReferencedColumnProperty(boolean canW) {
		return new ElementProp(PROP_REFERENCED_COLUMN, String.class, canW) {
			/** Gets the value */
			public Object getValue () {
                return referencedColumnName();
			}
		};
	}
    
    private String localColumnName() {
        ColumnElement elm = ((ColumnPairElement) element).getLocalColumn();
        return elm.getDeclaringTable().getName().getName() + "." + elm.getName().getName(); //NOI18N
    }
    
    private String referencedColumnName() {
        ColumnElement elm = ((ColumnPairElement) element).getReferencedColumn();
        return elm.getDeclaringTable().getName().getName() + "." + elm.getName().getName(); //NOI18N
    }
    
}
