/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
