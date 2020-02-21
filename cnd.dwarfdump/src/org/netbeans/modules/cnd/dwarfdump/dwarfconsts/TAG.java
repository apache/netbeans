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

package org.netbeans.modules.cnd.dwarfdump.dwarfconsts;

import java.util.HashMap;

/**
 *
 */
public enum TAG {
    DW_TAG_array_type(0x01),
    DW_TAG_class_type(0x02),
    DW_TAG_entry_point(0x03),
    DW_TAG_enumeration_type(0x04),
    DW_TAG_formal_parameter(0x05),
    DW_TAG_imported_declaration(0x08),
    DW_TAG_label(0x0a),
    DW_TAG_lexical_block(0x0b),
    DW_TAG_member(0x0d),
    DW_TAG_pointer_type(0x0f),
    DW_TAG_reference_type(0x10),
    DW_TAG_compile_unit(0x11),
    DW_TAG_string_type(0x12),
    DW_TAG_structure_type(0x13),
    DW_TAG_subroutine_type(0x15),
    DW_TAG_typedef(0x16),
    DW_TAG_union_type(0x17),
    DW_TAG_unspecified_parameters(0x18),
    DW_TAG_variant(0x19),
    DW_TAG_common_block(0x1a),
    DW_TAG_common_inclusion(0x1b),
    DW_TAG_inheritance(0x1c),
    DW_TAG_inlined_subroutine(0x1d),
    DW_TAG_module(0x1e),
    DW_TAG_ptr_to_member_type(0x1f),
    DW_TAG_set_type(0x20),
    DW_TAG_subrange_type(0x21),
    DW_TAG_with_stmt(0x22),
    DW_TAG_access_declaration(0x23),
    DW_TAG_base_type(0x24),
    DW_TAG_catch_block(0x25),
    DW_TAG_const_type(0x26),
    DW_TAG_constant(0x27),
    DW_TAG_enumerator(0x28),
    DW_TAG_file_type(0x29),
    DW_TAG_friend(0x2a),
    DW_TAG_namelist(0x2b),
    DW_TAG_namelist_item(0x2c),
    DW_TAG_packed_type(0x2d),
    DW_TAG_subprogram(0x2e),
    DW_TAG_template_type_param(0x2f),
    DW_TAG_template_value_param(0x30),
    DW_TAG_thrown_type(0x31),
    DW_TAG_try_block(0x32),
    DW_TAG_variant_part(0x33),
    DW_TAG_variable(0x34),
    DW_TAG_volatile_type(0x35),
    DW_TAG_dwarf_procedure(0x36),
    DW_TAG_restrict_type(0x37),
    DW_TAG_interface_type(0x38),
    DW_TAG_namespace(0x39),
    DW_TAG_imported_module(0x3a),
    DW_TAG_unspecified_type(0x3b),
    DW_TAG_partial_unit(0x3c),
    DW_TAG_imported_unit(0x3d),
    DW_TAG_condition(0x3f),
    DW_TAG_shared_type(0x40),
    
    DW_TAG_lo_user(0x4080),

    DW_TAG_MIPS_lo(0x4081),
    DW_TAG_MIPS_loop(0x4081),
    DW_TAG_MIPS_hi(0x4090),

    /* GNU extensions.  */
    DW_TAG_format_label(0x4101),	/* For FORTRAN 77 and Fortran 90.  */
    DW_TAG_function_template(0x4102),	/* For C++.  */
    DW_TAG_class_template(0x4103),	/* For C++.  */
    DW_TAG_GNU_BINCL(0x4104),
    DW_TAG_GNU_EINCL(0x4105),
    
    DW_TAG_SUN_lo(0x4201),
    DW_TAG_SUN_function_template(0x4201),
    DW_TAG_SUN_class_template(0x4202),
    DW_TAG_SUN_struct_template(0x4203),
    DW_TAG_SUN_union_template(0x4204),
    DW_TAG_SUN_indirect_inheritance(0x4205),
    DW_TAG_SUN_codeflags(0x4206),
    DW_TAG_SUN_memop_info(0x4207),
    DW_TAG_SUN_omp_child_func(0x4208),
    DW_TAG_SUN_rtti_descriptor(0x4209),
    DW_TAG_SUN_dtor_info(0x420a),
    DW_TAG_SUN_dtor(0x420b),
    DW_TAG_SUN_f90_interface(0x420c),
    DW_TAG_SUN_fortran_vax_structure(0x420d),
    DW_TAG_SUN_hi(0x42ff),

    DW_TAG_upc_shared_type(0x8765),
    DW_TAG_upc_strict_type(0x8766),
    DW_TAG_upc_relaxed_type(0x8767),
    
    DW_TAG_unknown(0xfffe),
    DW_TAG_hi_user(0xffff);
    
    private final int value;
    private static final HashMap<Integer, TAG> hashmap = new HashMap<Integer, TAG>();
 
    static {
        for (TAG elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }
 
    TAG(int value) {
        this.value = value;
    }
    
    public static TAG get(int val) {
        TAG kind = hashmap.get(val);
        return (kind != null) ? kind : DW_TAG_unknown;
    }
    
    public int value() {
        return value;
    }
}
