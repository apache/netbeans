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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.api.picklist;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * DefaultPicklistElement
 */
public class DefaultPicklistElement implements PicklistElement, Serializable {
    private static final long serialVersionUID = -8893325364784938693L;
    private String elem;

    public DefaultPicklistElement(String elem) {
	this.elem = elem;
    }

    public String getString() {
	return elem;
    }

    /**
     * Compares two PicklistElement for equality. Returns true if equal,
     * otherwise false.
     */
    @Override
    public boolean equals(PicklistElement elem) {
	return ((DefaultPicklistElement)elem).getString().equals(this.elem);
    }

    /**
     * Returns a String representation of this element to be used
     * for displaying the element.
     */
    @Override
    public String displayName() {
	return elem;
    }

    /**
     * Return a clone (copy) of this element
     */
    @Override
    public PicklistElement cloneElement() {
	return new DefaultPicklistElement(elem);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	try {
	    out.writeObject(elem);
	}
	catch (IOException ioe) {
	    System.err.println("ExecutePicklistElement - writeObject - ioe " + ioe); // NOI18N
	    throw(ioe);
	}
    }
	                                                                                                         
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	try {
	    elem = (String)in.readObject();   
	}
	catch (IOException e) {
	    System.err.println("ExecutePicklistElement - readObject - e " + e); // NOI18N
	    throw(e);
	}
	catch (ClassNotFoundException e) {
	    System.err.println("ExecutePicklistElement - readObject - e " + e); // NOI18N
	    throw(e);
	}
    }
}
