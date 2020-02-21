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

package  org.netbeans.modules.cnd.makefile.wizard;

    /**
     *  We need to emit lots of make variables of the form "$(foo_bar)", where
     *  "foo" is the related to the variable we want to creat and "bar" is
     *  related to the current target. This class is a helper class which
     *  creates these names for us. To cut down on object creation its intended
     *  to be reused with different variables and targets.
     */

public class MakeVarName {
	private String targetName;		// this gets appended to name
	private StringBuffer lastName;		// save the last name created
	private StringBuffer lastRef;		// save the last ref created
	private StringBuffer lastSuffix;	// check if same as last call

	private StringBuffer buffer = new StringBuffer(80);

	public MakeVarName() {
	    targetName = null;
	    lastName = new StringBuffer(80);
	    lastRef = new StringBuffer(80);
	    lastSuffix = new StringBuffer(20);
	}


	/**
	 *  Change the targetName so we can reuse this same object with another
	 *  target.
	 */
	public void setTargetName(String targetName) {
	    this.targetName = targetName;

	    lastName.delete(0, lastName.length());
	    lastRef.delete(0, lastRef.length());
	    lastSuffix.delete(0, lastSuffix.length());
	}


	/**
	 *  Return a string with the desired name. Cache the last suffix and
	 *  returned string so we don't need to recreate it if we match the
	 *  last call. This should happen fairly often.
	 */
	public String makeName(String suffix) {

	    if (suffix.equals(lastSuffix.toString())) {
		return lastName.toString();
	    } else {
		buffer.replace(0, buffer.length(), suffix);
		buffer.append(targetName);
		lastName.replace(0, lastName.length(), buffer.toString());
		return buffer.toString();
	    }
	}


	/**
	 *  Return a string with the desired name. This flavor allows an extra
	 *  string to be appended to the name.
	 */
	public String makeName(String suffix, String extra) {

	    if (suffix.equals(lastSuffix.toString())) {
		return lastName.toString();
	    } else {
		buffer.replace(0, buffer.length(), suffix);
		buffer.append(targetName);
		buffer.append("_");					// NOI18N
		buffer.append(extra);
		lastName.replace(0, lastName.length(), buffer.toString());
		return buffer.toString();
	    }
	}


	/**
	 *  Return a string with the desired variable reference. Cache the last
	 *  suffix and returned string so we don't need to recreate it if we
	 *  match the last call. This should happen fairly often.
	 */
	public String makeRef(String suffix) {

	    if (suffix.equals(lastSuffix.toString())) {
		return lastRef.toString();
	    } else {
		buffer.replace(0, buffer.length(), "$(");		// NOI18N
		buffer.append(suffix);
		buffer.append(targetName);
		buffer.append(")");					// NOI18N
		lastRef.replace(0, lastRef.length(), buffer.toString());
		return buffer.toString();
	    }
	}


	/**
	 *  Return a string with the desired variable reference. This flavor
	 *  allows an extra string to be appended to the name.
	 */
	public String makeRef(String suffix, String extra) {

	    if (suffix.equals(lastSuffix.toString())) {
		return lastRef.toString();
	    } else {
		buffer.replace(0, buffer.length(), "$(");		// NOI18N
		buffer.append(suffix);
		buffer.append(targetName);
		buffer.append("_");					// NOI18N
		buffer.append(extra);
		buffer.append(")");					// NOI18N
		lastRef.replace(0, lastRef.length(), buffer.toString());
		return buffer.toString();
	    }
	}


	/** Return the last name we created */
	public String lastName() {
	    return lastName.toString();
	}


	/** Return the last variable reference we created */
	public String lastRef() {
	    return lastRef.toString();
	}
    }
