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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.math.BigInteger;

/**
 * Utility to work with 64-bit addresses.
 * Java doesn't have unsigned so tackling unsigned 64-bit quantities 
 * with bit 31 turned on has to be done with care.
 *
 * Modelled after class Long.
 */

public final class Address {

    public static long parseAddr(String s) throws NumberFormatException {
	BigInteger b;
	if (s.startsWith("0x")) {		// NOI18N
	    b = new BigInteger(s.substring(2), 16);
	} else {
	    b = new BigInteger(s, 10);
	}
	return b.longValue();
    }

    public static String toHexString(long addr, boolean LP64) {
	// toHexString is already in unsigned representation
	if (LP64)
	    return Long.toHexString(addr);
	else
	    return Integer.toHexString((int) addr);
    }

    public static String toHexString0x(long addr, boolean LP64) {
	// toHexString is already in unsigned representation
	if (LP64)
	    return "0x" + Long.toHexString(addr);		// NOI18N
	else
	    return "0x" + Integer.toHexString((int) addr);	// NOI18N
    }
    
    public static String toOctalString(long addr, boolean LP64) {
	// toOctxString is already in unsigned representation
	if (LP64)
	    return Long.toOctalString(addr);
	else
	    return Integer.toOctalString((int) addr);        
    }
    
    public static String toOctalString0x(long addr, boolean LP64) {
	// toOctxString is already in unsigned representation
	if (LP64)
	    return  "0x" +Long.toOctalString(addr);//NOI18N
	else
	    return  "0x" +Integer.toOctalString((int) addr);  //NOI18N      
    }  
    
    public static String toBinaryString(long addr, boolean LP64) {
	// toOctxString is already in unsigned representation
	if (LP64)
	    return Long.toBinaryString(addr);
	else
	    return Integer.toBinaryString((int) addr);        
    }
    
    public static String toBinaryString0x(long addr, boolean LP64) {
	// toOctxString is already in unsigned representation
	if (LP64)
	    return  "0x" +Long.toBinaryString(addr);//NOI18N
	else
	    return  "0x" +Integer.toBinaryString((int) addr);  //NOI18N      
    }     
}
