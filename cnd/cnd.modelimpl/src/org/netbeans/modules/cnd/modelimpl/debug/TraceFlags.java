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

package org.netbeans.modules.cnd.modelimpl.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.debug.DebugUtils;

/**
 * A common place for tracing flags that are used by several classes
 */
public class TraceFlags {
    public static final boolean SORT_PARSED_FILES = DebugUtils.getBoolean("cnd.model.parse.sorted", false);
    public static final boolean PARSE_ENABLED = DebugUtils.getBoolean("cnd.model.parse.enable", true);
    public static final boolean MERGE_EVENTS = DebugUtils.getBoolean("cnd.model.merge.events", true);
    public static final boolean USE_PARSER_API = DebugUtils.getBoolean("cnd.model.parser.api", false);

    public static volatile boolean TRACE_182342_BUG = Boolean.getBoolean("cnd.modelimpl.trace182342"); // NOI18N
    public static final boolean TRACE_191307_BUG = Boolean.getBoolean("cnd.modelimpl.trace191307"); // NOI18N
    public static final boolean DEBUG_BROKEN_REPOSITORY = Boolean.getBoolean("cnd.corrupt.repository"); // NOI18N

    public static final boolean TRACE_CPU_CPP = false;
    public static final boolean TRACE_PARSER_QUEUE_DETAILS = Boolean.getBoolean("cnd.parser.queue.trace.details"); // NOI18N
    public static final boolean TRACE_PARSER_PROGRESS = Boolean.getBoolean("cnd.parser.progress.trace"); // NOI18N
    public static final boolean TRACE_PARSER_QUEUE = TRACE_PARSER_QUEUE_DETAILS || Boolean.getBoolean("cnd.parser.queue.trace"); // NOI18N
    public static final boolean TRACE_PARSER_QUEUE_POLL = TRACE_PARSER_QUEUE || Boolean.getBoolean("cnd.parser.queue.trace.poll"); // NOI18N
    public static final boolean TRACE_CLOSE_PROJECT = DebugUtils.getBoolean("cnd.trace.close.project", false); // NOI18N
    public static final boolean TIMING_PARSE_PER_FILE_DEEP = Boolean.getBoolean("cnd.modelimpl.timing.per.file.deep"); // NOI18N
    public static final boolean TIMING_PARSE_PER_FILE_FLAT = Boolean.getBoolean("cnd.modelimpl.timing.per.file.flat"); // NOI18N
    public static final boolean TIMING = Boolean.getBoolean("cnd.modelimpl.timing"); // NOI18N
    public static final boolean CHECK_CONSISTENCY = DebugUtils.getBoolean("cnd.modelimpl.checkConsistency", false); // NOI18N
    public static final int     SUSPEND_PARSE_TIME = Integer.getInteger("cnd.modelimpl.sleep", 0); // NOI18N
    public static final int     SUSPEND_PARSE_FILE_TIME = Integer.getInteger("cnd.modelimpl.parse.sleep", 0); // NOI18N
    public static final boolean REPORT_PARSING_ERRORS = Boolean.getBoolean("parser.report.errors"); // NOI18N
    public static final boolean DUMP_AST = Boolean.getBoolean("parser.collect.ast"); // NOI18N
    public static final boolean DUMP_PROJECT_ON_OPEN = DebugUtils.getBoolean("cnd.dump.project.on.open", false); // NOI18N
    public static final boolean DUMP_NATIVE_FILE_ITEM_USER_INCLUDE_PATHS = DebugUtils.getBoolean("cnd.dump.native.file.item.paths", false); // NOI18N

    public static final String TRACE_FILE_NAME = System.getProperty("cnd.modelimpl.trace.file");

    public static final boolean APT_CHECK_GET_STATE = DebugUtils.getBoolean("apt.check.get.state", false); // NOI18N
 
    public static final int     BUF_SIZE = APTTraceFlags.BUF_SIZE;
    
    /**
     * switches for cache
     */ 
    public static final boolean CACHE_AST = DebugUtils.getBoolean("cnd.cache.ast", false); // NOI18N
    public static final boolean TRACE_CACHE = DebugUtils.getBoolean("cnd.trace.cache", false); // NOI18N
    public static final boolean USE_AST_CACHE = DebugUtils.getBoolean("cnd.use.ast.cache", false); // NOI18N
    public static final boolean CACHE_SKIP_APT_VISIT = DebugUtils.getBoolean("cnd.cache.skip.apt.visit", false); // NOI18N
    public static final boolean CACHE_FILE_STATE = DebugUtils.getBoolean("cnd.cache.file.state", true); // NOI18N
    public static final boolean USE_WEAK_MEMORY_CACHE = DebugUtils.getBoolean("cnd.cache.key.object", true); // NOI18N
    public static final boolean APT_FILE_CACHE_ENTRY = DebugUtils.getBoolean("cnd.apt.cache.entry", true); //NOI18N

    public static final boolean CACHE_SKIP_SAVE = DebugUtils.getBoolean("cnd.cache.skip.save", true); // NOI18N
    
    public static final boolean TRACE_MODEL_STATE = Boolean.getBoolean("cnd.modelimpl.installer.trace"); // NOI18N

    public static final boolean USE_CANONICAL_PATH = DebugUtils.getBoolean("cnd.modelimpl.use.canonical.path", false); // NOI18N
    public static final boolean SYMLINK_AS_OWN_FILE = DebugUtils.getBoolean("cnd.modelimpl.symlink.as.file", true); // NOI18N
    
    public static final boolean CHECK_MEMORY = DebugUtils.getBoolean("cnd.check.memory", false); // NOI18N
    
    public static final boolean DUMP_PARSE_RESULTS = DebugUtils.getBoolean("cnd.dump.parse.results", false); // NOI18N
    public static final boolean DUMP_REPARSE_RESULTS = DebugUtils.getBoolean("cnd.dump.reparse.results", false); // NOI18N
    
    public static final boolean DEBUG = Boolean.getBoolean("org.netbeans.modules.cnd.modelimpl.trace")  || Boolean.getBoolean("cnd.modelimpl.trace"); // NOI18N
    
    //public static final boolean USE_REPOSITORY = DebugUtils.getBoolean("cnd.modelimpl.use.repository", true); // NOI18N
    public static final boolean PERSISTENT_REPOSITORY = DebugUtils.getBoolean("cnd.modelimpl.persistent", true); // NOI18N
    //public static final boolean RESTORE_CONTAINER_FROM_UID = DebugUtils.getBoolean("cnd.modelimpl.use.uid.container", true); // NOI18N
    //public static final boolean UID_CONTAINER_MARKER = true;

    public static final boolean CLEAN_MACROS_AFTER_PARSE = DebugUtils.getBoolean("cnd.clean.macros.after.parse", true); // NOI18N
    
    public static final boolean SET_UNNAMED_QUALIFIED_NAME = DebugUtils.getBoolean("cnd.modelimpl.fqn.unnamed", false); // NOI18N
    public static final boolean TRACE_UNNAMED_DECLARATIONS = DebugUtils.getBoolean("cnd.modelimpl.trace.unnamed", false); // NOI18N

    public static final boolean TRACE_REGISTRATION = DebugUtils.getBoolean("cnd.modelimpl.trace.registration", false); // NOI18N
    public static final boolean TRACE_DISPOSE = DebugUtils.getBoolean("cnd.modelimpl.trace.dispose", false); // NOI18N

    public static final boolean CLOSE_AFTER_PARSE = DebugUtils.getBoolean("cnd.close.ide.after.parse", false); // NOI18N
    public static final int     CLOSE_TIMEOUT = Integer.getInteger("cnd.close.ide.timeout",0); // in seconds // NOI18N

    public static final boolean USE_DEEP_REPARSING_TRACE = DebugUtils.getBoolean("cnd.modelimpl.use.deep.reparsing.trace", false); // NOI18N
    public static final boolean DEEP_REPARSING_OPTIMISTIC = DebugUtils.getBoolean("cnd.modelimpl.use.deep.reparsing.optimistic", false); // NOI18N
    
    public static final boolean SAFE_REPOSITORY_ACCESS = DebugUtils.getBoolean("cnd.modelimpl.repository.safe.access", false); // NOI18N

    // see IZ#101952 and IZ#101953
    public static final boolean SAFE_UID_ACCESS = DebugUtils.getBoolean("cnd.modelimpl.safe.uid", true); // NOI18N
    
    public static final boolean TRACE_CANONICAL_FIND_FILE = DebugUtils.getBoolean("cnd.modelimpl.trace.canonical.find", false); // NOI18N

    public static final boolean NEED_TO_TRACE_UNRESOLVED_INCLUDE = DebugUtils.getBoolean("cnd.modelimpl.trace.failed.include", false); // NOI18N
    public static final boolean TRACE_VALIDATION = DebugUtils.getBoolean("cnd.modelimpl.trace.validation", false); // NOI18N

    public static final boolean TRACE_XREF_REPOSITORY = DebugUtils.getBoolean("cnd.modelimpl.trace.xref.repository", false); // NOI18N

    public static final boolean TRACE_REPOSITORY_LISTENER = DebugUtils.getBoolean("cnd.repository.listener.trace", false); // NOI18N
    public static final boolean TRACE_UP_TO_DATE_PROVIDER = DebugUtils.getBoolean("cnd.uptodate.trace", false); // NOI18N
    public static final boolean TRACE_PROJECT_COMPONENT_RW = DebugUtils.getBoolean("cnd.project.compoment.rw.trace", false); // NOI18N

    public static final boolean TRACE_RESOLVED_LIBRARY = DebugUtils.getBoolean("cnd.project.trace.resolved.library", false); // NOI18N
    
    public static final boolean TRACE_EXTERNAL_CHANGES = DebugUtils.getBoolean("cnd.modelimpl.trace.external.changes", false); // NOI18N
    
    public static final boolean TRACE_ERROR_PROVIDER = DebugUtils.getBoolean("cnd.modelimpl.trace.error.provider", false); // NOI18N
    public static final boolean PARSE_STATISTICS = DebugUtils.getBoolean("cnd.parse.statistics", false); // NOI18N
    public static final boolean TRACE_PC_STATE = DebugUtils.getBoolean("cnd.pp.condition.state.trace", false); // NOI18N
    public static final boolean TRACE_PC_STATE_COMPARISION = DebugUtils.getBoolean("cnd.pp.condition.comparision.trace", false); // NOI18N

    public static final int REPARSE_DELAY = DebugUtils.getInt("cnd.reparse.delay", 1001); // NOI18N
    public static final boolean REPARSE_ON_DOCUMENT_CHANGED = DebugUtils.getBoolean("cnd.reparse.on.document.changed", true); // NOI18N
    
    public static final boolean DYNAMIC_TESTS_TRACE = DebugUtils.getBoolean("cnd.modelimpl.dynamic.tests.trace", false); // NOI18N
    
    // experimental expression evaluator for template instantiations
    public static final boolean EXPRESSION_EVALUATOR = DebugUtils.getBoolean("cnd.modelimpl.expression.evaluator", true); // NOI18N
    public static final boolean EXPRESSION_EVALUATOR_DEEP_VARIABLE_PROVIDER = DebugUtils.getBoolean("cnd.modelimpl.expression.evaluator.deep.variable.provider", true); // NOI18N
    public static final boolean EXPRESSION_EVALUATOR_RECURSIVE_CALC = DebugUtils.getBoolean("cnd.modelimpl.expression.evaluator.recursive.calc", true); // NOI18N
    public static final boolean EXPRESSION_EVALUATOR_EXTRA_SPEC_PARAMS_MATCHING = DebugUtils.getBoolean("cnd.modelimpl.expression.evaluator.extra.spec.params.matching", true); // NOI18N
    public static final boolean COMPLETE_EXPRESSION_EVALUATOR = EXPRESSION_EVALUATOR &&
                                                                EXPRESSION_EVALUATOR_DEEP_VARIABLE_PROVIDER && 
                                                                EXPRESSION_EVALUATOR_RECURSIVE_CALC && 
                                                                EXPRESSION_EVALUATOR_EXTRA_SPEC_PARAMS_MATCHING;
    
    public static final boolean INSTANTIATION_FULL_FORWARDS_SEARCH = DebugUtils.getBoolean(
            "cnd.modelimpl.instantiation.full_forwards_search",  // NOI18N
            false
    );

    /** 
     * swithces off parsing function bodies
     */
    private static final String CND_MODELIMPL_EXCL_COMPOUND = "cnd.modelimpl.excl.compound"; // NOI18N
    public static boolean EXCLUDE_COMPOUND = DebugUtils.getBoolean(CND_MODELIMPL_EXCL_COMPOUND, true); // NOI18N
    
    private static final String CND_MODELIMPL_CPP_PARSER_ACTION = "cnd.modelimpl.cpp.parser.action"; // NOI18N
    public static boolean CPP_PARSER_ACTION = DebugUtils.getBoolean(CND_MODELIMPL_CPP_PARSER_ACTION, false);
    private static final String CND_MODELIMPL_CPP_PARSER_ACTION_TRACE = "cnd.modelimpl.cpp.parser.action.trace"; // NOI18N
    public static boolean TRACE_CPP_PARSER_ACTION = DebugUtils.getBoolean(CND_MODELIMPL_CPP_PARSER_ACTION_TRACE, false);
    private static final String CND_MODELIMPL_CPP_PARSER_NEW_GRAMMAR = "cnd.modelimpl.cpp.parser.new.grammar"; // NOI18N
    public static boolean CPP_PARSER_NEW_GRAMMAR = DebugUtils.getBoolean(CND_MODELIMPL_CPP_PARSER_NEW_GRAMMAR, false);
    private static final String CND_MODELIMPL_PARSE_HEADERS_WITH_SOURCES = "cnd.modelimpl.parse.headers.with.sources"; // NOI18N
    public static boolean PARSE_HEADERS_WITH_SOURCES = DebugUtils.getBoolean(CND_MODELIMPL_PARSE_HEADERS_WITH_SOURCES, false);
    private static final String CND_MODELIMPL_CPP_PARSER_RULES_TRACE = "cnd.modelimpl.cpp.parser.rules.trace"; // NOI18N
    public static boolean TRACE_CPP_PARSER_RULES = DebugUtils.getBoolean(CND_MODELIMPL_CPP_PARSER_RULES_TRACE, false);
    private static final String CND_MODELIMPL_CPP_PARSER_SHOW_AST = "cnd.modelimpl.cpp.parser.show.AST"; // NOI18N
    public static boolean TRACE_CPP_PARSER_SHOW_AST = DebugUtils.getBoolean(CND_MODELIMPL_CPP_PARSER_SHOW_AST, false);
    
    public static void validate(String flag, boolean value) {
        if (CND_MODELIMPL_CPP_PARSER_ACTION.equals(flag)) {
            System.setProperty(CND_MODELIMPL_CPP_PARSER_ACTION, Boolean.toString(value));
            CPP_PARSER_ACTION = value;
        } else if (CND_MODELIMPL_CPP_PARSER_ACTION_TRACE.equals(flag)) {
            System.setProperty(CND_MODELIMPL_CPP_PARSER_ACTION_TRACE, Boolean.toString(value));
            TRACE_CPP_PARSER_ACTION = value;
        } else if (CND_MODELIMPL_CPP_PARSER_NEW_GRAMMAR.equals(flag)) {
            System.setProperty(CND_MODELIMPL_CPP_PARSER_NEW_GRAMMAR, Boolean.toString(value));
            CPP_PARSER_NEW_GRAMMAR = value;
        } else if (CND_MODELIMPL_PARSE_HEADERS_WITH_SOURCES.equals(flag)) {
            System.setProperty(CND_MODELIMPL_PARSE_HEADERS_WITH_SOURCES, Boolean.toString(value));
            PARSE_HEADERS_WITH_SOURCES = value;
        } else if (CND_MODELIMPL_CPP_PARSER_RULES_TRACE.equals(flag)) {
            System.setProperty(CND_MODELIMPL_CPP_PARSER_RULES_TRACE, Boolean.toString(value));
            TRACE_CPP_PARSER_RULES = value;
        } else if (CND_MODELIMPL_EXCL_COMPOUND.equals(flag)) {
            System.setProperty(CND_MODELIMPL_EXCL_COMPOUND, Boolean.toString(value));
            EXCLUDE_COMPOUND = value;
        } else if (CND_MODELIMPL_CPP_PARSER_SHOW_AST.equals(flag)) {
            System.setProperty(CND_MODELIMPL_CPP_PARSER_SHOW_AST, Boolean.toString(value));
            TRACE_CPP_PARSER_SHOW_AST = value;
        } 
    }

    public static final boolean CACHE_IN_PROJECT = DebugUtils.getBoolean("cnd.cache.in.project", true); // NOI18N
    public static final boolean USE_CURR_PARSE_TIME = DebugUtils.getBoolean("cnd.use.curr.parse.time", false); // NOI18N
    
    public static final List<String> logMacros;
    static {
         String text = System.getProperty("parser.log.macro"); //NOI18N
         if (text != null && text.length() > 0) {
             List<String> l = new ArrayList<>();
             for (StringTokenizer stringTokenizer = new StringTokenizer(text, ","); stringTokenizer.hasMoreTokens();) { //NOI18N
                 l.add(stringTokenizer.nextToken());
             }
             logMacros = Collections.unmodifiableList(l);
         } else {
             logMacros = null;
         }
    }

    private TraceFlags() {
    }
}
