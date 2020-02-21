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
public enum FORM {
    DW_FORM_addr(0x01),
    DW_FORM_block2(0x03),
    DW_FORM_block4(0x04),
    DW_FORM_data2(0x05),
    DW_FORM_data4(0x06),
    DW_FORM_data8(0x07),
    DW_FORM_string(0x08),
    DW_FORM_block(0x09),
    DW_FORM_block1(0x0a),
    DW_FORM_data1(0x0b),
    DW_FORM_flag(0x0c),
    DW_FORM_sdata(0x0d),
    DW_FORM_strp(0x0e),
    DW_FORM_udata(0x0f),
    DW_FORM_ref_addr(0x10),
    DW_FORM_ref1(0x11),
    DW_FORM_ref2(0x12),
    DW_FORM_ref4(0x13),
    DW_FORM_ref8(0x14),
    DW_FORM_ref_udata(0x15),
    DW_FORM_indirect(0x16),
    /* DWARF 4.  */
    DW_FORM_sec_offset(0x17),
    DW_FORM_exprloc(0x18),
    DW_FORM_flag_present(0x19),
    DW_FORM_sig8(0x20);
    
    private final int value;
    private static final HashMap<Integer, FORM> hashmap = new HashMap<Integer, FORM>();
    
    static {
        for (FORM elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    FORM(int value) {
        this.value = value;
    }
    
    public static FORM get(int val) {
        return hashmap.get(val);
    }
    
    
    public int value() {
        return value;
    }
}
