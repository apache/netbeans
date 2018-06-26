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
/*
 * GenericTableDialogPanelAccessor.java
 *
 * Created on January 2, 2004, 2:07 PM
 */

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;


/** Interface to be implemented by dialog subpanels that are to be used for data
 *  entry into a table managed by the GenericTablePanel/GenericTableModel pair.
 *
 *  Extend a JPanel that implements this interface to allow editing of complex
 *  fields.  (For simple text only input, the existing default panel should be
 *  sufficient.)
 *
 * @author Peter Williams
 */
public interface GenericTableDialogPanelAccessor {
	
    public static final String USER_DATA_CHANGED = "UserDataChanged";  // NOI18N
    
    
	/** Initialization routine to handle any required building or initialization tasks.
	 *
     * @param asVersion The appserver version required by the current bean tree
	 * @param preferredWidth The calculated dynamic preferredWidth for the child dialog.
	 * @param entries List of field descriptions 
     * @param data Custom data in a format defined by the implementing class.
	 */
	public void init(ASDDVersion asVersion, int preferredWidth, List<TableEntry> entries, Object data);
	
	/** Set the error manager for use by this panel.
	 */
//	public void setErrorManager(ErrorSupportManager errorMgr);
	
	/** Pass initial field values.
	 *
	 * @param values The list of initial values for the fields.
	 */
	public void setValues(Object [] values);
	
	/** Retrieve current field values.
	 *
	 * @return List of objects that represent the values entered by the user
	 */
	public Object [] getValues();
	
	/** Retrieve any errors in what the user has currently entered.
	 *
	 * @param validationSupport The validation support object
	 * @return Collection of error messages that tell the user why their current
	 *   input isn't acceptable.
	 */
//	public Collection getErrors(ValidationSupport validationSupport);
	
	/** Determines if the required fields have appropriate values.
	 *
	 * @return True if the required fields are filled appropriately, false otherwise.
	 */
	public boolean requiredFieldsFilled();
}
