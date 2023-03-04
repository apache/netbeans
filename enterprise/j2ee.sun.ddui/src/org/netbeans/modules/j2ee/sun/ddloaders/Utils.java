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
package org.netbeans.modules.j2ee.sun.ddloaders;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.openide.ErrorManager;

/**
 *
 * @author Peter Williams
 */
public class Utils {

    public static final String ICON_BASE_DD_VALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDValidIcon"; // NOI18N
    public static final String ICON_BASE_DD_INVALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDInvalidIcon"; // NOI18N
    public static final String ICON_BASE_ERROR_BADGE = 
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ErrorBadge"; // NOI18N
    
    
    /** No instances of this class should be created.
     */
    private Utils() {
    }

    
	private static final String [] booleanStrings = {
		"0", "1",			// NOI18N
		"false", "true",	// NOI18N
		"no", "yes",		// NOI18N
		"off", "on"			// NOI18N
	};

	public static boolean booleanValueOf(String val) {
		boolean result = false;
		int valueIndex = -1;

		if(val != null && val.length() > 0) {
            val = val.trim();
			for(int i = 0; i < booleanStrings.length; i++) {
				if(val.compareToIgnoreCase(booleanStrings[i]) == 0) {
					valueIndex = i;
					break;
				}
			}
		}

		if(valueIndex >= 0) {
			if(valueIndex%2 == 1) {
				result = true;
			}
		}

		return result;
	}
    
    public static boolean notEmpty(String testedString) {
        return (testedString != null) && (testedString.length() > 0);
    }
    
    public static boolean strEmpty(String testedString) {
        return testedString == null || testedString.length() == 0;
    }
    
    public static boolean strEquals(String one, String two) {
        boolean result = false;
        
        if(one == null) {
            result = (two == null);
        } else {
            if(two == null) {
                result = false;
            } else {
                result = one.equals(two);
            }
        }
        return result;
    }
    
    public static boolean strEquivalent(String one, String two) {
        boolean result = false;
        
        if(strEmpty(one) && strEmpty(two)) {
            result = true;
        } else if(one != null && two != null) {
            result = one.equals(two);
        }
        
        return result;
    }
    
    public static int strCompareTo(String one, String two) {
        int result;
        
        if(one == null) {
            if(two == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else {
            if(two == null) {
                result = 1;
            } else {
                result = one.compareTo(two);
            }
        }
        
        return result;
    }
    
    public static String getBeanDisplayName(CommonDDBean bean, String nameProperty) {
        String name = null;
        try {
            name = (String) bean.getValue(nameProperty);
        } catch(IllegalArgumentException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(Schema2BeansRuntimeException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return name != null ? name : "unknown"; // NOI18N
    }
    
}
