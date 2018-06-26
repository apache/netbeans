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
 * AuthorizationEntry.java
 *
 * Created on May 19, 2006, 6:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.openide.util.NbBundle;


/**
 *
 * @author  Peter Williams
 */
public class AuthorizationEntry extends GenericTableModel.TableEntry {

    private String childAttributeName;

    public AuthorizationEntry(String propName, String attrName, String resBase) {
        super(null, propName, NbBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.Bundle"),
                resBase, false, false);

        childAttributeName = attrName;
    }

    public Object getEntry(CommonDDBean parent) {
        Object result = null;

        if(parent.size(propertyName) > 0) {
            result = parent.getAttributeValue(propertyName, childAttributeName);
        }

        return result;
    }

    public void setEntry(CommonDDBean parent, Object value) {
        // Set blank strings to null.  This object also handles message-security-binding
        // though, so we have to check it out.        
        if(value instanceof String && ((String) value).length() == 0) {
            value = null;
        }

        if(parent.size(propertyName) == 0) {
            parent.setValue(propertyName, Boolean.TRUE);
        }

        parent.setAttributeValue(propertyName, childAttributeName, (String) value);
    }

    public Object getEntry(CommonDDBean parent, int row) {
        throw new UnsupportedOperationException();
    }	

    public void setEntry(CommonDDBean parent, int row, Object value) {
        throw new UnsupportedOperationException();
    }
}
