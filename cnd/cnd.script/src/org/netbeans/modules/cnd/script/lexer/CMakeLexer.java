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

package org.netbeans.modules.cnd.script.lexer;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.cnd.api.script.CMakeTokenId;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 */
public class CMakeLexer implements Lexer<CMakeTokenId> {

    private static final Set<String> keywords = new HashSet<String> ();
    private static final Set<String> commands = new HashSet<String> ();

    static {
        keywords.add ("in_list"); // NOI18N
        keywords.add ("force"); // NOI18N
        keywords.add ("argn"); // NOI18N
        keywords.add ("output"); // NOI18N
        keywords.add ("copy"); // NOI18N
        
        keywords.add ("depends"); // NOI18N
        keywords.add ("byproducts"); // NOI18N
        keywords.add ("working_directory"); // NOI18N
        keywords.add ("main_dependency"); // NOI18N
        keywords.add ("implicit_depends"); // NOI18N
        keywords.add ("verbatim"); // NOI18N
        keywords.add ("uses_terminal"); // NOI18N
        keywords.add ("comment"); // NOI18N
        keywords.add ("pre_build"); // NOI18N
        keywords.add ("pre_link"); // NOI18N
        keywords.add ("post_build"); // NOI18N
        keywords.add ("configuration"); // NOI18N
        keywords.add ("configurations"); // NOI18N
        keywords.add ("name"); // NOI18N
        keywords.add ("project_name"); // NOI18N
        keywords.add ("version"); // NOI18N
        keywords.add ("old"); // NOI18N
        keywords.add ("new"); // NOI18N
        keywords.add ("push"); // NOI18N
        keywords.add ("pop"); // NOI18N
        
        keywords.add ("destination"); // NOI18N
        keywords.add ("files"); // NOI18N
        keywords.add ("pattern"); // NOI18N
        keywords.add ("regex"); // NOI18N
        keywords.add ("replace"); // NOI18N
        keywords.add ("exclude"); // NOI18N
        keywords.add ("permissions"); // NOI18N
        keywords.add ("file_permissions"); // NOI18N
        keywords.add ("directory_permissions"); // NOI18N
        keywords.add ("use_source_permissions"); // NOI18N
        keywords.add ("no_source_permissions"); // NOI18N
        keywords.add ("files_matching"); // NOI18N
        keywords.add ("optional"); // NOI18N
        
        keywords.add ("cache"); // NOI18N
        keywords.add ("internal"); // NOI18N
        keywords.add ("uninitialized"); // NOI18N
        keywords.add ("required"); // NOI18N
        keywords.add ("all"); // NOI18N

        keywords.add ("cmake_host_system_information"); // NOI18N
        keywords.add ("query"); // NOI18N
        keywords.add ("number_of_logical_cores"); // NOI18N
        keywords.add ("number_of_physical_cores"); // NOI18N
        keywords.add ("hostname"); // NOI18N
        keywords.add ("fqdn"); // NOI18N
        keywords.add ("total_virtual_memory"); // NOI18N
        keywords.add ("avaliable_virtual_memory"); // NOI18N
        keywords.add ("total_physical_memory"); // NOI18N
        keywords.add ("avaliable_physical_memory"); // NOI18N

        keywords.add ("configure_file"); // NOI18N
        keywords.add ("copyonly"); // NOI18N
        keywords.add ("escape_quotes"); // NOI18N
        keywords.add ("newline_style"); // NOI18N
        keywords.add ("unix"); // NOI18N
        keywords.add ("dos"); // NOI18N
        keywords.add ("win32"); // NOI18N
        keywords.add ("lf"); // NOI18N
        keywords.add ("crlf"); // NOI18N

        keywords.add ("create_test_sourcelist"); // NOI18N
        keywords.add ("extra_include"); // NOI18N

        keywords.add ("ctest_build"); // NOI18N
        keywords.add ("number_errors"); // NOI18N
        keywords.add ("number_warnings"); // NOI18N
        keywords.add ("build"); // NOI18N

        keywords.add ("ctest_coverage"); // NOI18N
        keywords.add ("labels"); // NOI18N

        keywords.add ("ctest_memcheck"); // NOI18N
        keywords.add ("start"); // NOI18N
        keywords.add ("stride"); // NOI18N
        keywords.add ("end"); // NOI18N
        keywords.add ("exclude_label"); // NOI18N
        keywords.add ("include_label"); // NOI18N
        keywords.add ("parallel_level"); // NOI18N

        keywords.add ("ctest_run_script"); // NOI18N
        keywords.add ("new_process"); // NOI18N

        keywords.add ("ctest_start"); // NOI18N
        keywords.add ("track"); // NOI18N

        keywords.add ("ctest_submit"); // NOI18N
        keywords.add ("retry_count"); // NOI18N
        keywords.add ("retry_delay"); // NOI18N
        keywords.add ("parts"); // NOI18N

        keywords.add ("ctest_test"); // NOI18N
        keywords.add ("schedule_random"); // NOI18N
        keywords.add ("stop_time"); // NOI18N
        keywords.add ("track"); // NOI18N
        keywords.add ("track"); // NOI18N
        keywords.add ("track"); // NOI18N

        keywords.add ("define_property"); // NOI18N
        keywords.add ("directory"); // NOI18N
        keywords.add ("source"); // NOI18N
        keywords.add ("test"); // NOI18N
        keywords.add ("variable"); // NOI18N
        keywords.add ("cached_variable"); // NOI18N
        keywords.add ("inherited"); // NOI18N
        keywords.add ("brief_docs"); // NOI18N
        keywords.add ("full_docs"); // NOI18N

        keywords.add ("enable_language"); // NOI18N
        keywords.add ("none"); // NOI18N
        keywords.add ("c"); // NOI18N
        keywords.add ("cxx"); // NOI18N
        keywords.add ("fortran"); // NOI18N
        
        keywords.add ("execute_process"); // NOI18N
        keywords.add ("timeout"); // NOI18N
        keywords.add ("output_variable"); // NOI18N
        keywords.add ("result_variable"); // NOI18N
        keywords.add ("error_variable"); // NOI18N
        keywords.add ("input_file"); // NOI18N
        keywords.add ("output_file"); // NOI18N
        keywords.add ("error_file"); // NOI18N
        keywords.add ("output_quiet"); // NOI18N
        keywords.add ("error_quiet"); // NOI18N
        keywords.add ("output_strip_trailing_whitespace"); // NOI18N
        keywords.add ("error_strip_trailing_whitespace"); // NOI18N

        keywords.add ("export"); // NOI18N
        keywords.add ("namespace"); // NOI18N
        keywords.add ("targets"); // NOI18N
        keywords.add ("export_link_interface_libraries"); // NOI18N
        keywords.add ("package"); // NOI18N

        keywords.add ("file"); // NOI18N
        keywords.add ("read"); // NOI18N
        keywords.add ("limit"); // NOI18N
        keywords.add ("offset"); // NOI18N
        keywords.add ("hex"); // NOI18N
        keywords.add ("write"); // NOI18N
        keywords.add ("strings"); // NOI18N
        keywords.add ("limit_count"); // NOI18N
        keywords.add ("limit_input"); // NOI18N
        keywords.add ("limit_output"); // NOI18N
        keywords.add ("length_minimum"); // NOI18N
        keywords.add ("length_maximum"); // NOI18N
        keywords.add ("newline_consume"); // NOI18N
        keywords.add ("no_hex_conversion"); // NOI18N
        keywords.add ("glob"); // NOI18N
        keywords.add ("relative"); // NOI18N
        keywords.add ("glob_recurse"); // NOI18N
        keywords.add ("follow_symlinks"); // NOI18N
        keywords.add ("rename"); // NOI18N
        keywords.add ("remove_recurse"); // NOI18N
        keywords.add ("relative_path"); // NOI18N
        keywords.add ("to_cmake_path"); // NOI18N
        keywords.add ("to_native_path"); // NOI18N
        keywords.add ("download"); // NOI18N
        keywords.add ("inactivity_timeout"); // NOI18N
        keywords.add ("expected_hash"); // NOI18N
        keywords.add ("algo"); // NOI18N
        keywords.add ("expected_MD5"); // NOI18N
        keywords.add ("tls_verify"); // NOI18N
        keywords.add ("tls_cainfo"); // NOI18N
        keywords.add ("upload"); // NOI18N
        keywords.add ("log"); // NOI18N
        keywords.add ("show_progress"); // NOI18N
        keywords.add ("timestamp"); // NOI18N
        keywords.add ("utc"); // NOI18N
        keywords.add ("generate"); // NOI18N
        keywords.add ("content"); // NOI18N
        keywords.add ("condition"); // NOI18N
        keywords.add ("MD5"); // NOI18N
        keywords.add ("SHA1"); // NOI18N
        keywords.add ("SHA224"); // NOI18N
        keywords.add ("SHA256"); // NOI18N
        keywords.add ("SHA384"); // NOI18N
        keywords.add ("SHA512"); // NOI18N

        keywords.add ("find_file"); // NOI18N
        keywords.add ("find_path"); // NOI18N
        keywords.add ("find_program"); // NOI18N
        keywords.add ("names"); // NOI18N
        keywords.add ("hints"); // NOI18N
        keywords.add ("env"); // NOI18N
        keywords.add ("paths"); // NOI18N
        keywords.add ("path_suffixes"); // NOI18N
        keywords.add ("doc"); // NOI18N
        keywords.add ("no_default_path"); // NOI18N
        keywords.add ("no_cmake_environment_path"); // NOI18N
        keywords.add ("no_cmake_path"); // NOI18N
        keywords.add ("no_system_environment_path"); // NOI18N
        keywords.add ("no_cmake_system_path"); // NOI18N
        keywords.add ("cmake_find_root_path_both"); // NOI18N
        keywords.add ("only_cmake_find_root_path"); // NOI18N
        keywords.add ("no_cmake_find_root_path"); // NOI18N

        keywords.add ("find_library"); // NOI18N
        keywords.add ("names_per_dir"); // NOI18N

        keywords.add ("find_package"); // NOI18N
        keywords.add ("exact"); // NOI18N
        keywords.add ("components"); // NOI18N
        keywords.add ("optional_components"); // NOI18N
        keywords.add ("no_policy_scope"); // NOI18N
             
        keywords.add ("foreach"); // NOI18N
        keywords.add ("range"); // NOI18N
        keywords.add ("in"); // NOI18N
        keywords.add ("items"); // NOI18N

        keywords.add ("get_directory_property"); // NOI18N
        keywords.add ("definition"); // NOI18N

        keywords.add ("get_filename_component"); // NOI18N
        keywords.add ("name_we"); // NOI18N
        keywords.add ("ext"); // NOI18N
        keywords.add ("absolute"); // NOI18N
        keywords.add ("realpath"); // NOI18N
        keywords.add ("program"); // NOI18N
        keywords.add ("program_args"); // NOI18N

        keywords.add ("get_property"); // NOI18N
        keywords.add ("property"); // NOI18N
        
        keywords.add ("if"); // NOI18N
        keywords.add ("on"); // NOI18N
        keywords.add ("off"); // NOI18N
        keywords.add ("yes"); // NOI18N
        keywords.add ("no"); // NOI18N
        keywords.add ("false"); // NOI18N
        keywords.add ("true"); // NOI18N
        keywords.add ("ignore"); // NOI18N
        keywords.add ("notfound"); // NOI18N
        keywords.add ("not"); // NOI18N
        keywords.add ("and"); // NOI18N
        keywords.add ("or"); // NOI18N
        keywords.add ("command"); // NOI18N
        keywords.add ("policy"); // NOI18N
        keywords.add ("target"); // NOI18N
        keywords.add ("exists"); // NOI18N
        keywords.add ("is_newer_than"); // NOI18N
        keywords.add ("is_directory"); // NOI18N
        keywords.add ("is_symlink"); // NOI18N
        keywords.add ("is_absolute"); // NOI18N
        keywords.add ("matches"); // NOI18N
        keywords.add ("less"); // NOI18N
        keywords.add ("greater"); // NOI18N
        keywords.add ("equal"); // NOI18N
        keywords.add ("strequal"); // NOI18N
        keywords.add ("strgreater"); // NOI18N
        keywords.add ("strless"); // NOI18N
        keywords.add ("version_less"); // NOI18N
        keywords.add ("version_equal"); // NOI18N
        keywords.add ("version_greater"); // NOI18N
        keywords.add ("defined"); // NOI18N

        keywords.add ("include_directories"); // NOI18N
        keywords.add ("after"); // NOI18N
        keywords.add ("before"); // NOI18N
        keywords.add ("system"); // NOI18N

        keywords.add ("include_external_msproject"); // NOI18N
        keywords.add ("type"); // NOI18N
        keywords.add ("guid"); // NOI18N
        keywords.add ("platform"); // NOI18N

        keywords.add ("install"); // NOI18N
        keywords.add ("archive"); // NOI18N
        keywords.add ("library"); // NOI18N
        keywords.add ("runtime"); // NOI18N
        keywords.add ("framework"); // NOI18N
        keywords.add ("bundle"); // NOI18N
        keywords.add ("private_header"); // NOI18N
        keywords.add ("public_header"); // NOI18N
        keywords.add ("resource"); // NOI18N
        keywords.add ("includes"); // NOI18N
        keywords.add ("component"); // NOI18N
        keywords.add ("namelink_only"); // NOI18N
        keywords.add ("namelink_skip"); // NOI18N
        keywords.add ("script"); // NOI18N
        keywords.add ("code"); // NOI18N

        keywords.add ("list"); // NOI18N
        keywords.add ("length"); // NOI18N
        keywords.add ("append"); // NOI18N
        keywords.add ("get"); // NOI18N
        keywords.add ("find"); // NOI18N
        keywords.add ("insert"); // NOI18N
        keywords.add ("remove_item"); // NOI18N
        keywords.add ("remove_at"); // NOI18N
        keywords.add ("remove_duplicates"); // NOI18N
        keywords.add ("reverse"); // NOI18N
        keywords.add ("sort"); // NOI18N
        
        keywords.add ("load_cache"); // NOI18N
        keywords.add ("read_with_prefix"); // NOI18N
        keywords.add ("include_internals"); // NOI18N

        keywords.add ("mark_as_advanced"); // NOI18N
        keywords.add ("clear"); // NOI18N

        keywords.add ("math"); // NOI18N
        keywords.add ("expr"); // NOI18N

        keywords.add ("message"); // NOI18N
        keywords.add ("status"); // NOI18N
        keywords.add ("send_error"); // NOI18N
        keywords.add ("fatal_error"); // NOI18N
        keywords.add ("warning"); // NOI18N
        keywords.add ("author_warning"); // NOI18N
        keywords.add ("deprecation"); // NOI18N

        keywords.add ("set_directory_properties"); // NOI18N
        keywords.add ("properties"); // NOI18N

        keywords.add ("set_property"); // NOI18N
        keywords.add ("append_string"); // NOI18N

        keywords.add ("set"); // NOI18N
        keywords.add ("filepath"); // NOI18N
        keywords.add ("parent_scope"); // NOI18N
        keywords.add ("string"); // NOI18N
        keywords.add ("bool"); // NOI18N
        keywords.add ("path"); // NOI18N

        keywords.add ("source_group"); // NOI18N
        keywords.add ("regular_expression"); // NOI18N

        keywords.add ("string"); // NOI18N
        keywords.add ("match"); // NOI18N
        keywords.add ("matchall"); // NOI18N
        keywords.add ("concat"); // NOI18N
        keywords.add ("compare"); // NOI18N
        keywords.add ("notequal"); // NOI18N
        keywords.add ("ascii"); // NOI18N
        keywords.add ("configure"); // NOI18N
        keywords.add ("tolower"); // NOI18N
        keywords.add ("toupper"); // NOI18N
        keywords.add ("substring"); // NOI18N
        keywords.add ("strip"); // NOI18N
        keywords.add ("random"); // NOI18N
        keywords.add ("alphabet"); // NOI18N
        keywords.add ("random_seed"); // NOI18N
        keywords.add ("make_c_identifier"); // NOI18N

        keywords.add ("target_compile_definitions"); // NOI18N
        keywords.add ("interface"); // NOI18N
        keywords.add ("public"); // NOI18N
        keywords.add ("private"); // NOI18N

        keywords.add ("target_link_libraries"); // NOI18N
        keywords.add ("link_interface_libraries"); // NOI18N
        keywords.add ("link_private"); // NOI18N
        keywords.add ("link_public"); // NOI18N

        keywords.add ("try_compile"); // NOI18N
        keywords.add ("result_var"); // NOI18N
        keywords.add ("compile_definitions"); // NOI18N
        keywords.add ("copy_file"); // NOI18N
        keywords.add ("copy_file_error"); // NOI18N
        keywords.add ("sources"); // NOI18N
        keywords.add ("cmake_flags"); // NOI18N

        keywords.add ("try_run"); // NOI18N
        keywords.add ("run_result_var"); // NOI18N
        keywords.add ("compile_result_var"); // NOI18N
        keywords.add ("compile_output_variable"); // NOI18N
        keywords.add ("run_output_variable"); // NOI18N
        keywords.add ("args"); // NOI18N

        keywords.add ("add_compile_options"); // NOI18N
        keywords.add ("add_custom_command"); // NOI18N
        keywords.add ("add_custom_target"); // NOI18N
        keywords.add ("add_definitions"); // NOI18N
        keywords.add ("add_dependencies"); // NOI18N
        keywords.add ("add_executable"); // NOI18N
        keywords.add ("add_library"); // NOI18N
        keywords.add ("add_rest"); // NOI18N
        keywords.add ("add_subdirectory"); // NOI18N
        keywords.add ("add_test"); // NOI18N
        keywords.add ("aux_source_directory"); // NOI18N
        keywords.add ("break"); // NOI18N
        keywords.add ("build_command"); // NOI18N
        keywords.add ("build_name"); // NOI18N
        keywords.add ("cmake_minimum_required"); // NOI18N
        keywords.add ("cmake_policy"); // NOI18N
        keywords.add ("continue"); // NOI18N
        keywords.add ("ctest_configure"); // NOI18N
        keywords.add ("ctest_empty_binary_directory"); // NOI18N
        keywords.add ("ctest_read_custom_files"); // NOI18N
        keywords.add ("ctest_sleep"); // NOI18N
        keywords.add ("ctest_update"); // NOI18N
        keywords.add ("ctest_upload"); // NOI18N
        keywords.add ("else"); // NOI18N
        keywords.add ("elseif"); // NOI18N
        keywords.add ("enable_testing"); // NOI18N
        keywords.add ("endforeach"); // NOI18N
        keywords.add ("endfunction"); // NOI18N
        keywords.add ("endif"); // NOI18N
        keywords.add ("endmacro"); // NOI18N
        keywords.add ("endwhile"); // NOI18N
        keywords.add ("exec_program"); // NOI18N
        keywords.add ("export_library_dependencies"); // NOI18N
        keywords.add ("fltk_wrap_ui"); // NOI18N
        keywords.add ("function"); // NOI18N
        keywords.add ("get_cmake_property"); // NOI18N
        keywords.add ("get_source_file_property"); // NOI18N
        keywords.add ("get_target_property"); // NOI18N
        keywords.add ("get_test_property"); // NOI18N
        keywords.add ("include"); // NOI18N
        keywords.add ("include_regular_expression"); // NOI18N
        keywords.add ("install_files"); // NOI18N
        keywords.add ("install_programs"); // NOI18N
        keywords.add ("install_targets"); // NOI18N
        keywords.add ("link_directories"); // NOI18N
        keywords.add ("link_libraries"); // NOI18N
        keywords.add ("load_command"); // NOI18N
        keywords.add ("macro"); // NOI18N
        keywords.add ("make_directory"); // NOI18N
        keywords.add ("option"); // NOI18N
        keywords.add ("output_required_files"); // NOI18N
        keywords.add ("project"); // NOI18N
        keywords.add ("qt_wrap_cpp"); // NOI18N
        keywords.add ("qt_wrap_ui"); // NOI18N
        keywords.add ("remove"); // NOI18N
        keywords.add ("remove_definitions"); // NOI18N
        keywords.add ("return"); // NOI18N
        keywords.add ("separate_arguments"); // NOI18N
        keywords.add ("set_source_files_properties"); // NOI18N
        keywords.add ("set_target_properties"); // NOI18N
        keywords.add ("set_tests_properties"); // NOI18N
        keywords.add ("site_name"); // NOI18N
        keywords.add ("subdir_depends"); // NOI18N
        keywords.add ("subdirs"); // NOI18N
        keywords.add ("target_compile_features"); // NOI18N
        keywords.add ("target_compile_options"); // NOI18N
        keywords.add ("target_include_directories"); // NOI18N
        keywords.add ("target_sources"); // NOI18N
        keywords.add ("unset"); // NOI18N
        keywords.add ("use_mangled_mesa"); // NOI18N
        keywords.add ("utility_source"); // NOI18N
        keywords.add ("variable_requires"); // NOI18N
        keywords.add ("variable_watch"); // NOI18N
        keywords.add ("while"); // NOI18N
        keywords.add ("write_file"); // NOI18N
        
        keywords.add ("aliased_target"); // NOI18N
        keywords.add ("unknown"); // NOI18N
        keywords.add ("imported"); // NOI18N
        keywords.add ("exclude_from_all"); // NOI18N 
        keywords.add ("global"); // NOI18N 
        keywords.add ("module"); // NOI18N
        keywords.add ("alias"); // NOI18N
        keywords.add ("shared"); // NOI18N
        keywords.add ("static"); // NOI18N
        keywords.add ("object"); // NOI18N
        keywords.add ("return_value"); // NOI18N

        commands.add ("rm"); // NOI18N
        commands.add ("mv"); // NOI18N
        commands.add ("mkdir"); // NOI18N
        commands.add ("echo"); // NOI18N
        commands.add ("exit"); // NOI18N
        commands.add ("scp"); // NOI18N
        commands.add ("cd"); // NOI18N
        commands.add ("tar"); // NOI18N
        commands.add ("patch"); // NOI18N
}

    private final LexerRestartInfo<CMakeTokenId> info;

    private static enum State {
        OTHER,
        AFTER_SEPARATOR
    }
    
    State state;

    CMakeLexer(LexerRestartInfo<CMakeTokenId> info) {
        this.info = info;
        state = info.state() == null ? State.AFTER_SEPARATOR : State.values()[(Integer) info.state()];
    }

    @Override
    public Token<CMakeTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case '+':
            case '<':
            case '>':
            case '!':
            case '@':
            case '=':
            case ';':
            case ',':
            case '{':
            case '}':
            case '[':
            case ']':
            case '-':
            case '*':
            case '/':
            case '?':
            case '^':
            case '.':
            case '`':
            case '%':
            case '$':
                state = (i == ';' || ((state == State.AFTER_SEPARATOR) && (i == '@' || i == '+' || i == '-'))) ? State.AFTER_SEPARATOR : State.OTHER;
                return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
            case ':':
            case '(':
            case ')':
                state = State.AFTER_SEPARATOR;
                return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
            case '&':
                i = input.read();
                if(i == '&') {
                    state = State.AFTER_SEPARATOR;
                    return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
                } else {
                    state = State.OTHER;
                    input.backup(1);
                    return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
                }
            case '|':
                i = input.read();
                if(i == '|') {
                    state = State.AFTER_SEPARATOR;
                    return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
                } else {
                    state = State.OTHER;
                    input.backup(1);
                    return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
                }
            case '\\':
                i = input.read();
                if(i != '\n') {
                    state = State.OTHER;
                }
                return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                state = State.AFTER_SEPARATOR;
                do {
                    i = input.read ();
                } while (
                    i == ' ' ||
                    i == '\n' ||
                    i == '\r' ||
                    i == '\t'
                );
                if (i != LexerInput.EOF) {
                    input.backup(1);
                }
                return info.tokenFactory ().createToken (CMakeTokenId.WHITESPACE);
            case '#':
                do {
                    i = input.read ();
                } while (
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                state = State.AFTER_SEPARATOR;
                return info.tokenFactory ().createToken (CMakeTokenId.COMMENT);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                do {
                    i = input.read ();
                } while (
                    i >= '0' &&
                    i <= '9'
                );
                if (i == '.') {
                    do {
                        i = input.read ();
                    } while (
                        i >= '0' &&
                        i <= '9'
                    );
                }
                input.backup (1);
                state = State.OTHER;
                return info.tokenFactory ().createToken (CMakeTokenId.NUMBER);
            case '"':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '"' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                state = State.OTHER;
                return info.tokenFactory ().createToken (CMakeTokenId.STRING);
            case '\'':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '\'' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                state = State.OTHER;
                return info.tokenFactory ().createToken (CMakeTokenId.STRING);
            default:
                if (
                    (i >= 'a' && i <= 'z') ||
                    (i >= 'A' && i <= 'Z') ||
                    i == '_' ||
                    i == '~'
                ) {
                    do {
                        i = input.read ();
                    } while (
                        (i >= 'a' && i <= 'z') ||
                        (i >= 'A' && i <= 'Z') ||
                        (i >= '0' && i <= '9') ||
                        i == '_' ||
                        i == '~'
                    );
                    input.backup (1);
                    String idstr = input.readText().toString();
                    if (state == State.AFTER_SEPARATOR) {
                        state = State.OTHER;
                        if (keywords.contains(idstr.toLowerCase())) {
                            return info.tokenFactory().createToken(CMakeTokenId.KEYWORD);
                        } else if (commands.contains(idstr.toLowerCase())) {
                            return info.tokenFactory().createToken(CMakeTokenId.COMMAND);
                        }
                    } else {
                        state = State.OTHER;
                    }
                    return info.tokenFactory().createToken(CMakeTokenId.IDENTIFIER);
                }
                return info.tokenFactory ().createToken (CMakeTokenId.ERROR);
        }
    }

    @Override
    public Object state() {
        return state == State.AFTER_SEPARATOR ? null : state.ordinal();
    }

    @Override
    public void release() {
    }
}
