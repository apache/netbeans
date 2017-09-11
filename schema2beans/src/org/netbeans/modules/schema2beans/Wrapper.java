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

package org.netbeans.modules.schema2beans;

/**
 *  The Wrapper interface is one of the two ways a user can initialize a
 *  Wrapper object.
 *
 *  By default, when schema2beans generates the java classes, a #PCDATA DTD type
 *  is mapped to a java String. Sometime, the user might want to map an
 *  element to either a scalar or a specialized type (user defined class).
 *
 *  For example, a price DTD element specified as #PCDATA could be mapped
 *  to a float or integer data type. Or a date DTD element also specified
 *  as #PCDATA could be mapped to a specialized Date object, that the user might
 *  provide. schema2beans calls these specialized object 'wrappers'.
 *
 *  If the user specifies a wrapper object in the mdd file (see user
 *  documentation for mdd explanations), schema2beans uses the wrapper class
 *  instead of the String type. In this case, schema2beans needs to initialize
 *  the wrapper object using the String value from the XML document, and
 *  also needs to be able to get the String value from the wrapper 
 *  object (in order to write back the XML document).
 *
 *  This what this Wrapper interface provides. A wrapper class has
 *  has either to have a String constructor and toString() method,
 *  or implements the Wrapper interface. This is how schema2beans can set/get
 *  the String values of user wrapper/customized object.
 *
 */
public interface Wrapper {
    /**
     *	Method called by the schema2beans runtime to get the String value of the
     *	wrapper object. This String value is the value that has to appear 
     *	in the XML document.
     */
    public String 	getWrapperValue();
    
    /**
     *	Method called by the schema2beans runtime to set the value of the
     *	wrapper object. The String value is the value read in the XML document.
     */
    public void 	setWrapperValue(String value);
}
