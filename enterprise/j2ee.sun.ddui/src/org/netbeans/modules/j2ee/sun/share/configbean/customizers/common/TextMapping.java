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
 * TextMapping.java
 *
 * Created on October 27, 2003, 8:39 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import org.netbeans.modules.j2ee.sun.share.configbean.Utils;

/** Class that associates two strings with each other, presumably one to be
 *  written in code contexts, such as XML, and the other for display purposes,
 *  allowing it to be localized independently of the code.  This was originally
 *  designed to make localized comboboxes and listboxes easier to program but
 *  there are probably other UI elements that could make use of it.
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class TextMapping {
	
	private final String xmlText;
	private final String displayText;

	public TextMapping(final String xml, final String display) {
		xmlText = xml;
		displayText = display;
	}

	@Override
    public String toString() {
		return displayText;
	}

	public String getXMLString() {
		return xmlText;
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final TextMapping other = (TextMapping) obj;
        if(!Utils.strEquals(xmlText, other.xmlText) || !Utils.strEquals(displayText, other.displayText)) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.xmlText != null ? this.xmlText.hashCode() : 0;
        hash = 17 * hash + this.displayText != null ? this.displayText.hashCode() : 0;
        return hash;
    }

}
