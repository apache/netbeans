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
