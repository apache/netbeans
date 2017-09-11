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

/** Describes an object which holds a list of column pairs.
 */
public interface ColumnPairElementHolder {
	/** Add a new column pair to the holder.
	 *  @param pair the pair to add
	 * @throws Exception if impossible
	 */
	public void addColumnPair (ColumnPairElement pair) throws Exception;

	/** Add some new column pairs to the holder.
	 *  @param pairs the column pairs to add
	 * @throws Exception if impossible
	 */
	public void addColumnPairs (ColumnPairElement[] pairs) throws Exception;

	/** Remove a column pair from the holder.
	 *  @param pair the column pair to remove
	 * @throws Exception if impossible
	 */
	public void removeColumnPair (ColumnPairElement pair) throws Exception;

	/** Remove some column pairs from the holder.
	 *  @param pairs the column pairs to remove
	 * @throws Exception if impossible
	 */
	public void removeColumnPairs (ColumnPairElement[] pairs) throws Exception;

	/** Set the column pairs for this holder.
	 * Previous column pairs are removed.
	 * @param pairs the new column pairs
	 * @throws Exception if impossible
	 */
	public void setColumnPairs (ColumnPairElement[] pairs) throws Exception;

	/** Get all column pairs in this holder.
	 * @return the column pairs
	 */
	public ColumnPairElement[] getColumnPairs ();

	/** Find a column pair by name.
	 * @param name the name of the column pair for which to look
	 * @return the column pair or <code>null</code> if not found
	 */
	public ColumnPairElement getColumnPair (DBIdentifier name);
}
