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

package org.netbeans.modules.cnd.dwarfdump.dwarfconsts;

import java.util.HashMap;

/**
 *
 */
public enum LANG {
    DW_LANG_C89(0x0001, "C 89"), // NOI18N
    DW_LANG_C(0x0002, "C"), // NOI18N
    DW_LANG_Ada83(0x0003, "Ada 83"), // NOI18N
    DW_LANG_C_plus_plus(0x0004, "C++"), // NOI18N
    DW_LANG_Cobol74(0x0005, "Cobol 74"), // NOI18N
    DW_LANG_Cobol85(0x0006, "Cobol 85"), // NOI18N
    DW_LANG_Fortran77(0x0007, "Fortran 77"), // NOI18N
    DW_LANG_Fortran90(0x0008, "Fortran 90"), // NOI18N
    DW_LANG_Pascal83(0x0009, "Pascal 83"), // NOI18N
    DW_LANG_Modula2(0x000a, "Modula 2"), // NOI18N
    DW_LANG_Java(0x000b, "Java"), // NOI18N
    DW_LANG_C99(0x000c, "C 99"), // NOI18N
    DW_LANG_Ada95(0x000d, "Ada"), // NOI18N
    DW_LANG_Fortran95(0x000e, "Fortran 95"), // NOI18N
    DW_LANG_PLI(0x000f, "PL/I"), // NOI18N
    DW_LANG_ObjC(0x0010, "Objective-C"), // NOI18N
    DW_LANG_ObjC_plus_plus(0x0011, "Objective-C++"), // NOI18N
    DW_LANG_UPC(0x0012, "Unified Parallel C"), // NOI18N
    DW_LANG_D(0x0013, "D"), // NOI18N
    DW_LANG_lo_user(0x8000, null), // NOI18N
    DW_LANG_SUN_Assembler(0x9001, "Assembler"), // NOI18N
    DW_LANG_hi_user(0xffff, null); // NOI18N
    
/* What about dwarf 3.0 ?
    See http://dwarf.freestandards.org/Dwarf3Std.php
*/    
    
    private final int value;
    private final String name;
    static private final HashMap<Integer, LANG> hashmap = new HashMap<Integer, LANG>();
    
    static {
        for (LANG elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    LANG(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static LANG get(int val) {
        return hashmap.get(val);
    }
    
    public int value() {
        return value;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
