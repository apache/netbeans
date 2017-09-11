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

package org.netbeans.modules.dbschema;

import java.io.*;

/** Superclass for containable database metadata members
 * (columns, indexes, and foreign keys). Provides support
 * for associating this element with a declaring table.
 */
public abstract class DBMemberElement extends DBElement implements Cloneable {
	/** the table to which this element belongs */
	private TableElement declaringTable;

	/** Default constructor
     */
    protected DBMemberElement() {
	}

	/** Creates a member element.
	 * @param impl the pluggable implementation
	 * @param declaringTable the table to which this element belongs, or 
	 * <code>null</code> if unattached
	 */
	protected DBMemberElement(Impl impl, TableElement declaringTable) {
		super(impl);
        this.declaringTable = declaringTable;
	}

    /** Returns the implementation of the element.
	 * @return the current implementation.
     */
	final Impl getMemberImpl() {
        return (Impl) getElementImpl();
    }

	/* This should be automatically synchronized
	* when a DBMemberElement is added to the table. */
    
	/** Gets the declaring table. 
	 * @return the table that owns this member element, or <code>null</code> 
	 * if the element is not attached to any table
	 */
	public TableElement getDeclaringTable() {
        return declaringTable;
    }

	/** Sets the declaring table. 
	 * @param te the table that owns this member element
	 */
	public void setDeclaringTable(TableElement te) {
        if (declaringTable == null)
            declaringTable = te;
    }

	/** Pluggable implementation of member elements.
	 * @see DBMemberElement
	 */
	public interface Impl extends DBElement.Impl {
    }

	/** Default implementation of the Impl interface.
	 * It just holds the property values.
	 */
	static abstract class Memory extends DBElement.Memory implements DBMemberElement.Impl {
		/** Default */
		public Memory() {
			super();
		}

		/** Copy */
		public Memory(DBMemberElement el) {
			super(el);
		}
	}
}
