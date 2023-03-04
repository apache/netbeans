/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.lib.v8debug;

/**
 *
 * @author Martin Entlicher
 */
class JSONConstants {
    
    public static final String SEQ = "seq";
    public static final String TYPE = "type";
    public static final String COMMAND = "command";
    public static final String ARGUMENTS = "arguments";
    public static final String SEQ_REQUEST = "request_seq";
    public static final String BODY = "body";
    public static final String RUNNING = "running";
    public static final String SUCCESS = "success";
    public static final String MESSAGE = "message";
    public static final String EVENT = "event";
    
    public static final String HANDLE = "handle";
    public static final String HANDLES = "handles";
    public static final String NAME = "name";
    public static final String TARGET = "target";
    public static final String NUMBER = "number";
    public static final String POSITION = "position";
    public static final String LINE = "line";
    public static final String COLUMN = "column";
    public static final String ID = "id";
    public static final String DATA = "data";
    public static final String SOURCE = "source";
    public static final String SOURCE_START = "sourceStart";
    public static final String SOURCE_LENGTH = "sourceLength";
    public static final String COMPILATION_TYPE = "compilationType";
    public static final String STRING_DESCRIPTION = "stringDescription";
    
    public static final String ARGS_STEP_ACTION = "stepaction";
    public static final String ARGS_STEP_COUNT = "stepcount";
    
    public static final String FRAME = "frame";
    public static final String FROM_FRAME = "fromFrame";
    public static final String TO_FRAME = "toFrame";
    public static final String TOTAL_FRAMES = "totalFrames";
    public static final String FRAMES = "frames";
    public static final String BOTTOM = "bottom";
    
    public static final String INDEX = "index";
    public static final String FRAME_INDEX = "frameIndex";
    public static final String FRAME_RECEIVER = "receiver";
    public static final String FRAME_FUNC = "func";
    public static final String FRAME_CONSTRUCT_CALL = "constructCall";
    public static final String FRAME_AT_RETURN = "atReturn";
    public static final String FRAME_DEBUGGER = "debuggerFrame";
    public static final String FRAME_ARGUMENTS = "arguments";
    public static final String FRAME_LOCALS = "locals";
    public static final String FRAME_NUMBER = "frameNumber";
    public static final String RESULT = "result";
    
    public static final String REF = "ref";
    public static final String REFS = "refs";
    public static final String VALUE = "value";
    public static final String VALUE_CLASS_NAME = "className";
    public static final String VALUE_CONSTRUCTOR_FUNCTION = "constructorFunction";
    public static final String VALUE_PROTO_OBJECT = "protoObject";
    public static final String VALUE_PROTOTYPE_OBJECT = "prototypeObject";
    public static final String VALUE_PROPERTIES = "properties";
    public static final String FUNCTION_INFERRED_NAME = "inferredName";
    public static final String FUNCTION_RESOLVED = "resolved";
    public static final String INLINE_REFS = "inlineRefs";
    public static final String OBJECT = "object";
    public static final String LENGTH = "length";
    
    public static final String ATTRIBUTES = "attributes";
    public static final String PROPERTY_TYPE = "propertyType";
    
    public static final String BODY_VERSION = "V8Version";
    
    public static final String EVT_INVOCATION_TEXT = "invocationText";
    public static final String EVT_SOURCE_LINE = "sourceLine";
    public static final String EVT_SOURCE_COLUMN = "sourceColumn";
    public static final String EVT_SOURCE_LINE_TEXT = "sourceLineText";
    public static final String EVT_SCRIPT = "script";
    public static final String EVT_BREAKPOINTS = "breakpoints";
    public static final String EVT_UNCAUGHT = "uncaught";
    public static final String EVT_EXCEPTION = "exception";
    
    public static final String SCRIPT = "script";
    public static final String SCRIPT_ID = "script_id";
    public static final String SCRIPTID = "scriptId";
    public static final String SCRIPT_NAME = "script_name";
    public static final String SCRIPT_LINE_OFFSET = "lineOffset";
    public static final String SCRIPT_COLUMN_OFFSET = "columnOffset";
    public static final String SCRIPT_LINE_COUNT = "lineCount";
    public static final String SCRIPT_TYPE = "scriptType";
    public static final String TYPES = "types";
    public static final String IDs = "ids";
    public static final String CONTEXT = "context";
    
    public static final String PREVIEW_ONLY = "preview_only";
    public static final String NEW_SOURCE = "new_source";
    public static final String STEP_IN_RECOMMENDED = "stepin_recommended";
    public static final String CHANGE_TREE = "change_tree";
    public static final String TEXTUAL_DIFF = "textual_diff";
    public static final String OLD_LEN = "old_len";
    public static final String NEW_LEN = "new_len";
    public static final String CHUNKS = "chunks";
    public static final String UPDATED = "updated";
    public static final String STACK_MODIFIED = "stack_modified";
    public static final String STACK_UPDATE_NEEDS_STEP_IN = "stack_update_needs_step_in";
    public static final String CREATED_SCRIPT_NAME = "created_script_name";
    public static final String STATUS = "status";
    public static final String STATUS_EXPLANATION = "status_explanation";
    public static final String POSITIONS = "positions";
    public static final String OLD_POSITIONS = "old_positions";
    public static final String NEW_POSITIONS = "new_positions";
    public static final String START_POSITION = "start_position";
    public static final String END_POSITION = "end_position";
    public static final String CHILDREN = "children";
    public static final String NEW_CHILDREN = "new_children";
    public static final String CHANGE_LOG = "change_log";
    public static final String BREAK_POINTS_UPDATE = "break_points_update";
    public static final String LINKED_TO_OLD_SCRIPT = "linked_to_old_script";
    public static final String DROPPED_FROM_STACK = "dropped_from_stack";
    public static final String FUNCTION_PATCHED = "function_patched";
    public static final String FUNCTION_INFO_NOT_FOUND = "function_info_not_found";
    public static final String POSITION_PATCHED = "position_patched";
    public static final String INFO_NOT_FOUND = "info_not_found";
    
    public static final String INCLUDE_SOURCE = "includeSource";
    public static final String FILTER = "filter";
    public static final String FROM_LINE = "fromLine";
    public static final String TO_LINE = "toLine";
    public static final String FROM_POSITION = "fromPosition";
    public static final String TO_POSITION = "toPosition";
    public static final String TOTAL_LINES = "totalLines";
    
    public static final String EVAL_FROM_SCRIPT = "evalFromScript";
    public static final String EVAL_FROM_LOCATION = "evalFromLocation";
    public static final String EVAL_EXPRESSION = "expression";
    public static final String EVAL_GLOBAL = "global";
    public static final String EVAL_DISABLE_BREAK = "disable_break";
    public static final String EVAL_ADDITIONAL_CONTEXT = "additional_context";
    
    public static final String BREAK_POINT = "breakpoint";
    public static final String BREAK_POINTS = "breakpoints";
    public static final String BREAK_ON_EXCEPTIONS = "breakOnExceptions";
    public static final String BREAK_ON_UNCAUGHT_EXCEPTIONS = "breakOnUncaughtExceptions";
    public static final String BREAK_GROUP_ID = "groupId";
    public static final String BREAK_HIT_COUNT = "hit_count";
    public static final String BREAK_ACTIVE = "active";
    public static final String BREAK_ENABLED = "enabled";
    public static final String BREAK_CONDITION = "condition";
    public static final String BREAK_IGNORE_COUNT = "ignoreCount";
    public static final String BREAK_ACTUAL_LOCATIONS = "actual_locations";
    
    public static final String NEW_ID = "new_id";
    
    public static final String SCOPES = "scopes";
    public static final String SCOPE = "scope";
    public static final String SCOPE_INDEX = "index";
    public static final String FROM_SCOPE = "fromScope";
    public static final String TO_SCOPE = "toScope";
    public static final String TOTAL_SCOPES = "totalScopes";
    
    public static final String NEW_VALUE = "newValue";
    
    public static final String GC_BEFORE = "before";
    public static final String GC_AFTER = "after";
    
    public static final String TOTAL_THREADS = "totalThreads";
    public static final String THREADS = "threads";
    public static final String CURRENT = "current";
    
    public static final String TEXT = "text";
    
    public static final String FLAGS = "flags";
    
    public static final String INFINITY = "Infinity";
    public static final String NaN = "NaN";
    
    private JSONConstants() {}
    
}
