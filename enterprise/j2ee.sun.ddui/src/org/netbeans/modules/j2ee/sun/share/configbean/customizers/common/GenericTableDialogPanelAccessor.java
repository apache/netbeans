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
/*
 * GenericTableDialogPanelAccessor.java
 *
 * Created on January 2, 2004, 2:07 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

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
	
	/** Initialization routine to handle any required building or initialization tasks.
	 *
     * @param asVersion The appserver version required by the current bean tree
	 * @param preferredWidth The calculated dynamic preferredWidth for the child dialog.
	 * @param entries List of field descriptions 
     * @param data Custom data in a format defined by the implementing class.
	 */
	public void init(ASDDVersion asVersion, int preferredWidth, List entries, Object data);
	
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
	public Collection getErrors(ValidationSupport validationSupport);
	
	/** Determines if the required fields have appropriate values.
	 *
	 * @return True if the required fields are filled appropriately, false otherwise.
	 */
	public boolean requiredFieldsFilled();
}
