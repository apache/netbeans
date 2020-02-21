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
public enum ATTR {
    DW_AT_sibling(0x01),
    DW_AT_location(0x02),
    DW_AT_name(0x03),
    DW_AT_ordering(0x09),
    DW_AT_subscr_data(0x0a),
    DW_AT_byte_size(0x0b),
    DW_AT_bit_offset(0x0c),
    DW_AT_bit_size(0x0d),
    DW_AT_element_list(0x0f),
    DW_AT_stmt_list(0x10),
    DW_AT_low_pc(0x11),
    DW_AT_high_pc(0x12),
    DW_AT_language(0x13),
    DW_AT_member(0x14),
    DW_AT_discr(0x15),
    DW_AT_discr_value(0x16),
    DW_AT_visibility(0x17),
    DW_AT_import(0x18),
    DW_AT_string_length(0x19),
    DW_AT_common_reference(0x1a),
    DW_AT_comp_dir(0x1b),
    DW_AT_const_value(0x1c),
    DW_AT_containing_type(0x1d),
    DW_AT_default_value(0x1e),
    DW_AT_inline(0x20),
    DW_AT_is_optional(0x21),
    DW_AT_lower_bound(0x22),
    DW_AT_producer(0x25),
    DW_AT_prototyped(0x27),
    DW_AT_return_addr(0x2a),
    DW_AT_start_scope(0x2c),
    DW_AT_stride_size(0x2e),
    DW_AT_upper_bound(0x2f),
    DW_AT_abstract_origin(0x31),
    DW_AT_accessibility(0x32),
    DW_AT_address_class(0x33),
    DW_AT_artificial(0x34),
    DW_AT_base_types(0x35),
    DW_AT_calling_convention(0x36),
    DW_AT_count(0x37),
    DW_AT_data_member_location(0x38),
    DW_AT_decl_column(0x39),
    DW_AT_decl_file(0x3a),
    DW_AT_decl_line(0x3b),
    DW_AT_declaration(0x3c),
    DW_AT_discr_list(0x3d),
    DW_AT_encoding(0x3e),
    DW_AT_external(0x3f),
    DW_AT_frame_base(0x40),
    DW_AT_friend(0x41),
    DW_AT_identifier_case(0x42),
    DW_AT_macro_info(0x43),
    DW_AT_namelist_items(0x44),
    DW_AT_priority(0x45),
    DW_AT_segment(0x46),
    DW_AT_specification(0x47),
    DW_AT_static_link(0x48),
    DW_AT_type(0x49),
    DW_AT_use_location(0x4a),
    DW_AT_variable_parameter(0x4b),
    DW_AT_virtuality(0x4c),
    DW_AT_vtable_elem_location(0x4d),
    DW_AT_allocated(0x4e),
    DW_AT_associated(0x4f),
    DW_AT_data_location(0x50),
    DW_AT_byte_stride(0x51),
    DW_AT_entry_pc(0x52),
    DW_AT_use_UTF8(0x53),
    DW_AT_extension(0x54),
    DW_AT_ranges(0x55),
    DW_AT_trampoline(0x56),
    DW_AT_call_column(0x57),
    DW_AT_call_file(0x58),
    DW_AT_call_line(0x59),
    DW_AT_description(0x5a),
    DW_AT_binary_scale(0x5b),
    DW_AT_decimal_scale(0x5c),
    DW_AT_small(0x5d),
    DW_AT_decimal_sign(0x5e),
    DW_AT_digit_count(0x5f),
    DW_AT_picture_string(0x60),
    DW_AT_mutable(0x61),
    DW_AT_threads_scaled(0x62),
    DW_AT_explicit(0x63),
    DW_AT_object_pointer(0x64),
    DW_AT_endianity(0x65),
    DW_AT_elemental(0x66),
    DW_AT_pure(0x67),
    DW_AT_recursive(0x68),
    
    DW_AT_MIPS_lo(0x2001),
    DW_AT_MIPS_fde(0x2001),
    DW_AT_MIPS_loop_begin(0x2002),
    DW_AT_MIPS_tail_loop_begin(0x2003),
    DW_AT_MIPS_epilog_begin(0x2004),
    DW_AT_MIPS_loop_unroll_factor(0x2005),
    DW_AT_MIPS_software_pipeline_depth(0x2006),
    DW_AT_MIPS_linkage_name(0x2007),
    DW_AT_MIPS_stride(0x2008),
    DW_AT_MIPS_abstract_name(0x2009),
    DW_AT_MIPS_clone_origin(0x200a),
    DW_AT_MIPS_has_inlines(0x200b),
    DW_AT_MIPS_stride_byte(0x200c),
    DW_AT_MIPS_stride_elem(0x200d),
    DW_AT_MIPS_ptr_dopetype(0x200e),
    DW_AT_MIPS_allocatable_dopetype(0x200f),
    DW_AT_MIPS_assumed_shape_dopetype(0x2010),
    DW_AT_MIPS_assumed_size(0x2011),
    DW_AT_MIPS_hi(0x2020),

    DW_AT_GNU_lo(0x2101),
    DW_AT_sf_names(0x2101),
    DW_AT_src_info(0x2102),
    DW_AT_mac_info(0x2103),
    DW_AT_src_coords(0x2104),
    DW_AT_body_begin(0x2105),
    DW_AT_body_end(0x2106),
    DW_AT_GNU_vector(0x2107),
    DW_AT_GNU_guarded_by(0x2108),
    DW_AT_GNU_pt_guarded_by(0x2109),
    DW_AT_GNU_guarded(0x210a),
    DW_AT_GNU_pt_guarded(0x210b),
    DW_AT_GNU_locks_excluded(0x210c),
    DW_AT_GNU_exclusive_locks_required(0x210d),
    DW_AT_GNU_shared_locks_required(0x210e),
    DW_AT_GNU_odr_signature(0x210f),
    DW_AT_GNU_template_name(0x2110),
    DW_AT_GNU_call_site_value(0x2111),
    DW_AT_GNU_call_site_data_value(0x2112),
    DW_AT_GNU_call_site_target(0x2113),
    DW_AT_GNU_call_site_target_clobbered(0x2114),
    DW_AT_GNU_tail_call(0x2115),
    DW_AT_GNU_all_tail_call_sites(0x2116),
    DW_AT_GNU_all_call_sites(0x2117),
    DW_AT_GNU_all_source_call_sites(0x2118),
    DW_AT_GNU_macros(0x2119),
    DW_AT_GNU_hi(0x21ff),
    
    DW_AT_SUN_lo(0x2201),
    DW_AT_SUN_template(0x2201),
    DW_AT_SUN_alignment(0x2202),
    DW_AT_SUN_vtable(0x2203),
    DW_AT_SUN_count_guarantee(0x2204),
    DW_AT_SUN_command_line(0x2205),
    DW_AT_SUN_vbase(0x2206),
    DW_AT_SUN_compile_options(0x2207),
    DW_AT_SUN_language(0x2208),
    DW_AT_SUN_browser_file(0x2209),
    DW_AT_SUN_vtable_abi(0x2210),
    DW_AT_SUN_func_offsets(0x2211),
    DW_AT_SUN_cf_kind(0x2212),
    DW_AT_SUN_vtable_index(0x2213),
    DW_AT_SUN_omp_tpriv_addr(0x2214),
    DW_AT_SUN_omp_child_func(0x2215),
    DW_AT_SUN_func_offset(0x2216),
    DW_AT_SUN_memop_type_ref(0x2217),
    DW_AT_SUN_profile_id(0x2218),
    DW_AT_SUN_memop_signature(0x2219),
    DW_AT_SUN_original_name(0x2222),
    DW_AT_SUN_amd64_parmdump(0x2224),
    DW_AT_SUN_part_link_name(0x2225),
    DW_AT_SUN_link_name(0x2226),
    DW_AT_SUN_pass_with_const(0x2227),
    DW_AT_SUN_return_with_const(0x2228),
    DW_AT_SUN_import_by_name(0x2229),
    DW_AT_SUN_f90_pointer(0x222a),
    DW_AT_SUN_pass_by_ref(0x222b),
    DW_AT_SUN_f90_allocatable(0x222c),
    DW_AT_SUN_f90_assumed_shape_array(0x222d),
    DW_AT_SUN_c_vla(0x222e),
    W_AT_SUN_return_value_ptr(0x2230),
    DW_AT_SUN_dtor_start(0x2231),
    DW_AT_SUN_dtor_length(0x2232),
    DW_AT_SUN_dtor_state_initial(0x2233),
    DW_AT_SUN_dtor_state_final(0x2234),
    DW_AT_SUN_dtor_state_deltas(0x2235),
    DW_AT_SUN_import_by_lname(0x2236),
    DW_AT_SUN_f90_use_only(0x2237),
    DW_AT_SUN_namelist_spec(0x2238),
    DW_AT_SUN_is_omp_child_func(0x2239),
    DW_AT_SUN_fortran_main_alias(0x223a),
    DW_AT_SUN_fortran_based(0x223b),
    DW_AT_SUN_hi(0x22ff),
    
    DW_AT_lo_user(0x2000),
    DW_AT_unknown(0x3ffe),
    DW_AT_hi_user(0x3fff),
    DW_AT_JDD_offset(0x3ffe);
    
    private final int value;
    private static final HashMap<Integer, ATTR> hashmap = new HashMap<Integer, ATTR>();
    
    static {
        for (ATTR elem : values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    ATTR(int value) {
        this.value = value;
    }
    
    public static ATTR get(int val) {
        ATTR attr = hashmap.get(val);
        //if (attr == null) {
        //    System.err.println("");
        //}
        return (attr != null) ? attr : DW_AT_unknown;
    }
    
    public int value() {
        return value;
    }
}
