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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.netbeans.lib.v8debug.JSONConstants.*;
import org.netbeans.lib.v8debug.commands.Backtrace;
import org.netbeans.lib.v8debug.commands.ChangeBreakpoint;
import org.netbeans.lib.v8debug.commands.ChangeLive;
import org.netbeans.lib.v8debug.commands.ClearBreakpoint;
import org.netbeans.lib.v8debug.commands.ClearBreakpointGroup;
import org.netbeans.lib.v8debug.commands.Continue;
import org.netbeans.lib.v8debug.commands.Evaluate;
import org.netbeans.lib.v8debug.commands.Flags;
import org.netbeans.lib.v8debug.commands.Frame;
import org.netbeans.lib.v8debug.commands.GC;
import org.netbeans.lib.v8debug.commands.ListBreakpoints;
import org.netbeans.lib.v8debug.commands.Lookup;
import org.netbeans.lib.v8debug.commands.References;
import org.netbeans.lib.v8debug.commands.RestartFrame;
import org.netbeans.lib.v8debug.commands.Scope;
import org.netbeans.lib.v8debug.commands.Scopes;
import org.netbeans.lib.v8debug.commands.Scripts;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;
import org.netbeans.lib.v8debug.commands.SetExceptionBreak;
import org.netbeans.lib.v8debug.commands.SetVariableValue;
import org.netbeans.lib.v8debug.commands.Source;
import org.netbeans.lib.v8debug.commands.Threads;
import org.netbeans.lib.v8debug.commands.V8Flags;
import org.netbeans.lib.v8debug.commands.Version;
import org.netbeans.lib.v8debug.events.AfterCompileEventBody;
import org.netbeans.lib.v8debug.events.BreakEventBody;
import org.netbeans.lib.v8debug.events.CompileErrorEventBody;
import org.netbeans.lib.v8debug.events.ExceptionEventBody;
import org.netbeans.lib.v8debug.events.ScriptCollectedEventBody;
import org.netbeans.lib.v8debug.vars.NewValue;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Boolean;
import org.netbeans.lib.v8debug.vars.V8Function;
import org.netbeans.lib.v8debug.vars.V8Generator;
import org.netbeans.lib.v8debug.vars.V8Number;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8ScriptValue;
import org.netbeans.lib.v8debug.vars.V8String;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 * Translator of JSON objects into the corresponding Java API classes.
 * 
 * @author Martin Entlicher
 */
public class JSONReader {

    private JSONReader() {}
    
    public static V8Type getType(JSONObject obj) throws IllegalArgumentException {
        String type = (String) obj.get(TYPE);
        if (type == null) {
            throw new IllegalArgumentException("No type in "+obj.toJSONString());
        }
        return V8Type.valueOf(type);
    }
    
    public static V8Response getResponse(JSONObject obj) throws IllegalArgumentException {
        long sequence = (Long) obj.get(SEQ);
        V8Type type = getType(obj);
        long requestSequence = (Long) obj.get(SEQ_REQUEST);
        String commandName = (String) obj.get(COMMAND);
        V8Command command = (commandName != null) ? V8Command.fromString(commandName) : null;
        Boolean runningObj = (Boolean) obj.get(RUNNING);
        boolean running = (runningObj != null) ? runningObj : false;
        boolean success = (Boolean) obj.get(SUCCESS);
        V8Body body = null;
        String errorMessage = null;
        if (success) {
            Object bodyObj = obj.get(BODY);
            if (bodyObj instanceof JSONObject) {
                body = getBody(command, (JSONObject) bodyObj);
            } else if (bodyObj instanceof JSONArray) {
                body = getBody(command, (JSONArray) bodyObj);
            } else if (body != null) {
                throw new IllegalArgumentException("Unknown body "+bodyObj+" in "+obj.toJSONString());
            }
        } else {
            errorMessage = (String) obj.get(MESSAGE);
        }
        ReferencedValue[] refs = getRefs((JSONArray) obj.get(REFS));
        return new V8Response(sequence, requestSequence, command, body, refs, running, success, errorMessage);
    }
    
    public static V8Event getEvent(JSONObject obj) throws IllegalArgumentException {
        long sequence = (Long) obj.get(SEQ);
        String eventName = (String) obj.get(EVENT);
        V8Event.Kind eventKind;
        V8Body body = null;
        if (eventName != null) {
            eventKind = V8Event.Kind.fromString(eventName);
            JSONObject bodyObj = (JSONObject) obj.get(BODY);
            switch (eventKind) {
                case AfterCompile:
                    V8Script script = getScript((JSONObject) bodyObj.get(EVT_SCRIPT));
                    body = new AfterCompileEventBody(script);
                    break;
                case CompileError:
                    script = getScript((JSONObject) bodyObj.get(EVT_SCRIPT));
                    body = new CompileErrorEventBody(script);
                    break;
                case ScriptCollected:
                    long scriptId = getLong((JSONObject) bodyObj.get(EVT_SCRIPT), ID);
                    body = new ScriptCollectedEventBody(scriptId);
                    break;
                case Break:
                    String invocationText = (String) bodyObj.get(EVT_INVOCATION_TEXT);
                    long sourceLine = getLong(bodyObj, EVT_SOURCE_LINE);
                    long sourceColumn = getLong(bodyObj, EVT_SOURCE_COLUMN);
                    String sourceLineText = (String) bodyObj.get(EVT_SOURCE_LINE_TEXT);
                    V8ScriptLocation scriptLocation = getScriptLocation((JSONObject) bodyObj.get(EVT_SCRIPT));
                    long[] breakpoints = getLongArray((JSONArray) bodyObj.get(EVT_BREAKPOINTS));
                    body = new BreakEventBody(invocationText, sourceLine, sourceColumn, sourceLineText, scriptLocation, breakpoints);
                    break;
                case Exception:
                    boolean uncaught = (boolean) bodyObj.get(EVT_UNCAUGHT);
                    V8Value exception = getValue((JSONObject) bodyObj.get(EVT_EXCEPTION));
                    sourceLine = getLong(bodyObj, EVT_SOURCE_LINE);
                    sourceColumn = getLong(bodyObj, EVT_SOURCE_COLUMN);
                    sourceLineText = (String) bodyObj.get(EVT_SOURCE_LINE_TEXT);
                    script = getScript((JSONObject) bodyObj.get(EVT_SCRIPT));
                    body = new ExceptionEventBody(uncaught, exception, sourceLine, sourceColumn, sourceLineText, script);
                    break;
                default:
                    new IllegalArgumentException("Unknown event "+eventName+" in "+obj.toJSONString()).printStackTrace();
            }
        } else {
            // Handle events like: {"seq":218,"type":"event","success":false,"message":"SyntaxError: Unexpected token C","running":false}
            eventKind = null;
        }
        ReferencedValue[] refs = getRefs((JSONArray) obj.get(REFS));
        Boolean running = (Boolean) obj.get(RUNNING);
        Boolean success = (Boolean) obj.get(SUCCESS);
        String errorMessage = (String) obj.get(MESSAGE);
        return new V8Event(sequence, eventKind, body, refs, running, success, errorMessage);
    }

    public static V8Request getRequest(JSONObject obj) {
        long sequence = (Long) obj.get(SEQ);
        V8Type type = getType(obj);
        if (V8Type.request != type) {
            throw new IllegalArgumentException("Expecting request type. Actual type = "+type);
        }
        String commandName = (String) obj.get(COMMAND);
        V8Command command = V8Command.fromString(commandName);
        JSONObject argsObj = (JSONObject) obj.get(ARGUMENTS);
        V8Arguments args;
        if (argsObj != null) {
            args = getArguments(command, argsObj);
        } else {
            args = getSpecialArguments(command, obj);
        }
        return new V8Request(sequence, command, args);
    }

    private static V8Body getBody(V8Command command, JSONObject obj) {
        switch (command) {
            case Listbreakpoints:
                V8Breakpoint[] breakpoints = getBreakpoints((JSONArray) obj.get(BREAK_POINTS));
                boolean breakOnExceptions = getBoolean(obj, BREAK_ON_EXCEPTIONS);
                boolean breakOnUncaughtExceptions = getBoolean(obj, BREAK_ON_UNCAUGHT_EXCEPTIONS);
                return new ListBreakpoints.ResponseBody(breakpoints, breakOnExceptions, breakOnUncaughtExceptions);
            case Setbreakpoint:
                String type = getString(obj, TYPE);
                long bpId = getLong(obj, BREAK_POINT);
                String scriptName = null;
                if ("scriptName".equals(type)) {
                    scriptName = getString(obj, SCRIPT_NAME);
                }
                Long line = getLongOrNull(obj, LINE);
                Long column = getLongOrNull(obj, COLUMN);
                V8Breakpoint.ActualLocation[] actualLocations = getActualLocations((JSONArray) obj.get(BREAK_ACTUAL_LOCATIONS));
                return new SetBreakpoint.ResponseBody(V8Breakpoint.Type.valueOf(type), bpId,
                                                      scriptName, line, column, actualLocations);
            case Setexceptionbreak:
                String typeName = getString(obj, TYPE);
                V8ExceptionBreakType extype = V8ExceptionBreakType.valueOf(typeName);
                if (extype == null) {
                    throw new IllegalArgumentException("Unknown exception breakpoint type: '"+typeName+"'.");
                }
                boolean enabled = getBoolean(obj, BREAK_ENABLED);
                return new SetExceptionBreak.ResponseBody(extype, enabled);
            case Clearbreakpoint:
                bpId = getLong(obj, BREAK_POINT);
                return new ClearBreakpoint.ResponseBody(bpId);
            case Clearbreakpointgroup:
                long[] bpIds = getLongArray((JSONArray) obj.get(BREAK_POINTS));
                return new ClearBreakpointGroup.ResponseBody(bpIds);
            case Backtrace:
                long fromFrame = getLong(obj, FROM_FRAME);
                long toFrame = getLong(obj, TO_FRAME);
                long totalFrames = getLong(obj, TOTAL_FRAMES);
                V8Frame[] frames = getFrames((JSONArray) obj.get(FRAMES));
                return new Backtrace.ResponseBody(fromFrame, toFrame, totalFrames, frames);
            case Frame:
                V8Frame frame = getFrame(obj);
                return new Frame.ResponseBody(frame);
            case Restartframe:
                JSONObject resultObj = (JSONObject) obj.get(RESULT);
                if (resultObj == null) {
                    return null;
                }
                return new RestartFrame.ResponseBody(resultObj);
            case Changelive:
                resultObj = (JSONObject) obj.get(RESULT);
                if (resultObj == null) {
                    return null;
                }
                ChangeLive.ChangeLog changeLog = getChangeLog((JSONArray) obj.get(CHANGE_LOG));
                ChangeLive.Result result = getChangeLiveResult(resultObj);
                Boolean stepInRecommended = getBooleanOrNull(obj, STEP_IN_RECOMMENDED);
                return new ChangeLive.ResponseBody(changeLog, result, stepInRecommended);
            case Lookup:
                Map<Long, V8Value> valuesByHandle = new LinkedHashMap<>();
                for (Object element : obj.values()) {
                    V8Value value = getValue((JSONObject) element);
                    valuesByHandle.put(value.getHandle(), value);
                }
                return new Lookup.ResponseBody(valuesByHandle);
            case Evaluate:
                V8Value value = getValue(obj);
                return new Evaluate.ResponseBody(value);
            case SetVariableValue:
                value = getValue((JSONObject) obj.get(NEW_VALUE));
                return new SetVariableValue.ResponseBody(value);
            case Scope:
                V8Scope scope = getScope(obj, null);
                return new Scope.ResponseBody(scope);
            case Scopes:
                long fromScope = getLong(obj, FROM_SCOPE);
                long toScope = getLong(obj, TO_SCOPE);
                long totalScopes = getLong(obj, TOTAL_SCOPES);
                V8Scope[] scopes = getScopes((JSONArray) obj.get(SCOPES), null);
                return new Scopes.ResponseBody(fromScope, toScope, totalScopes, scopes);
            case Source:
                String source = getString(obj, SOURCE);
                long fromLine = getLong(obj, FROM_LINE);
                long toLine = getLong(obj, TO_LINE);
                long fromPosition = getLong(obj, FROM_POSITION);
                long toPosition = getLong(obj, TO_POSITION);
                long totalLines = getLong(obj, TOTAL_LINES);
                return new Source.ResponseBody(source, fromLine, toLine, fromPosition, toPosition, totalLines);
            case Threads:
                long numThreads = getLong(obj, TOTAL_THREADS);
                Map<Long, Boolean> threads = getThreads((JSONArray) obj.get(THREADS));
                return new Threads.ResponseBody(numThreads, threads);
            case Gc:
                long before = getLong(obj, GC_BEFORE);
                long after = getLong(obj, GC_AFTER);
                return new GC.ResponseBody(before, after);
            case Version:
                String version = getString(obj, BODY_VERSION);
                return new Version.ResponseBody(version);
            case Flags:
                JSONArray flagsArray = (JSONArray) obj.get(FLAGS);
                Map<String, Boolean> flags = new LinkedHashMap<>();
                for (Object fObj : flagsArray) {
                    JSONObject flag = (JSONObject) fObj;
                    flags.put(getString(flag, NAME), getBoolean(flag, VALUE));
                }
                return new Flags.ResponseBody(flags);
            default:
                return null;
        }
    }

    private static V8Body getBody(V8Command command, JSONArray array) {
        switch (command) {
            case Scripts:
                int n = array.size();
                V8Script[] scripts = new V8Script[n];
                for (int i = 0; i < n; i++) {
                    scripts[i] = getScript((JSONObject) array.get(i));
                }
                return new Scripts.ResponseBody(scripts);
            case References:
                n = array.size();
                V8Value[] refs = new V8Value[n];
                for (int i = 0; i < n; i++) {
                    refs[i] = getValue((JSONObject) array.get(i));
                }
                return new References.ResponseBody(refs);
            default:
                return null;
        }
    }
    
    private static V8Arguments getArguments(V8Command command, JSONObject obj) {
        switch (command) {
            case Backtrace:
                return new Backtrace.Arguments(
                        getLongOrNull(obj, FROM_FRAME), getLongOrNull(obj, TO_FRAME),
                        getBooleanOrNull(obj, BOTTOM), getBooleanOrNull(obj, INLINE_REFS));
            case Changebreakpoint:
                return new ChangeBreakpoint.Arguments(
                        getLong(obj, BREAK_POINT), getBooleanOrNull(obj, BREAK_ENABLED),
                        getString(obj, BREAK_CONDITION), getLongOrNull(obj, BREAK_IGNORE_COUNT));
            case Changelive:
                return new ChangeLive.Arguments(
                        getLong(obj, SCRIPT_ID), getString(obj, NEW_SOURCE),
                        getBooleanOrNull(obj, PREVIEW_ONLY));
            case Clearbreakpoint:
                return new ClearBreakpoint.Arguments(getLong(obj, BREAK_POINT));
            case Clearbreakpointgroup:
                return new ClearBreakpointGroup.Arguments(getLong(obj, BREAK_GROUP_ID));
            case Continue:
                String step = getString(obj, ARGS_STEP_ACTION);
                if (step == null) {
                    return null;
                }
                return new Continue.Arguments(
                        V8StepAction.valueOf(step),
                        getLongOrNull(obj, ARGS_STEP_COUNT));
            case Evaluate:
                return new Evaluate.Arguments(
                        getString(obj, EVAL_EXPRESSION),
                        getLongOrNull(obj, FRAME),
                        getBooleanOrNull(obj, EVAL_GLOBAL),
                        getBooleanOrNull(obj, EVAL_DISABLE_BREAK),
                        getAdditionalContext(obj.get(EVAL_ADDITIONAL_CONTEXT)));
            case Flags:
                Object fobj = obj.get(FLAGS);
                if (!(fobj instanceof JSONArray)) {
                    return null;
                }
                JSONArray farray = (JSONArray) fobj;
                Map<String, Boolean> flags = new LinkedHashMap<>();
                for (int i = 0; i < farray.size(); i++) {
                    Object felm = farray.get(i);
                    if (felm instanceof JSONObject) {
                        JSONObject fo = (JSONObject) felm;
                        flags.put(getString(fo, NAME), getBooleanOrNull(fo, VALUE));
                    }
                }
                return new Flags.Arguments(flags);
            case Frame:
                return new Frame.Arguments(getLongOrNull(obj, NUMBER));
            case Gc:
                return new GC.Arguments(getString(obj, TYPE));
            case Lookup:
                long[] handles = null;
                Object hObj = obj.get(HANDLES);
                if (hObj instanceof JSONArray) {
                    handles = getLongArray((JSONArray) hObj);
                }
                return new Lookup.Arguments(handles, getBooleanOrNull(obj, INCLUDE_SOURCE));
            case References:
                References.Type rType;
                String rTypeStr = getString(obj, TYPE);
                if (rTypeStr != null) {
                    rType = References.Type.valueOf(rTypeStr);
                } else {
                    rType = null;
                }
                return new References.Arguments(rType, getLong(obj, HANDLE));
            case Restartframe:
                return new RestartFrame.Arguments(getLongOrNull(obj, FRAME));
            case Scope:
                return new Scope.Arguments(getLong(obj, NUMBER), getLongOrNull(obj, FRAME_NUMBER));
            case Scopes:
                return new Scopes.Arguments(getLongOrNull(obj, FRAME_NUMBER));
            case Scripts:
                V8Script.Types scrTypes = null;
                long types = getLong(obj, TYPES);
                if (types >= 0) {
                    scrTypes = new V8Script.Types((int) types);
                }
                long[] ids = null;
                Object idsObj = obj.get(IDs);
                if (idsObj instanceof JSONArray) {
                    ids = getLongArray((JSONArray) idsObj);
                }
                Boolean includeSource = getBooleanOrNull(obj, INCLUDE_SOURCE);
                Object filter = obj.get(FILTER);
                if (filter instanceof String) {
                    return new Scripts.Arguments(scrTypes, ids, includeSource, (String) filter);
                } else {
                    if (!(filter instanceof Long)) {
                        return null;
                    }
                    return new Scripts.Arguments(scrTypes, ids, includeSource, (Long) filter);
                }
            case SetVariableValue:
                Object nvObj = obj.get(NEW_VALUE);
                if (!(nvObj instanceof JSONObject)) {
                    return null;
                }
                NewValue nv;
                Long handle = getLongOrNull((JSONObject) nvObj, HANDLE);
                if (handle != null) {
                    nv = new NewValue(handle);
                } else {
                    Object typeObj = ((JSONObject) nvObj).get(TYPE);
                    if (!(typeObj instanceof String)) {
                        return null;
                    }
                    V8Value.Type type = V8Value.Type.fromString((String) typeObj);
                    nv = new NewValue(type, STRING_DESCRIPTION);
                }
                Object scopeObj = obj.get(SCOPE);
                if (!(scopeObj instanceof JSONObject)) {
                    return null;
                }
                return new SetVariableValue.Arguments(
                        getString(obj, NAME), nv,
                        getLong((JSONObject) scopeObj, NUMBER),
                        getLongOrNull((JSONObject) scopeObj, FRAME_NUMBER));
            case Setbreakpoint:
                Object bpTypeObj = obj.get(TYPE);
                if (!(bpTypeObj instanceof String)) {
                    return null;
                }
                String bpType = (String) bpTypeObj;
                if ("script".equals(bpType)) {
                    bpType = V8Breakpoint.Type.scriptName.toString();
                }
                return new SetBreakpoint.Arguments(
                        V8Breakpoint.Type.valueOf(bpType),
                        getString(obj, TARGET),
                        getLongOrNull(obj, LINE), getLongOrNull(obj, COLUMN),
                        getBooleanOrNull(obj, BREAK_ENABLED),
                        getString(obj, BREAK_CONDITION),
                        getLongOrNull(obj, BREAK_IGNORE_COUNT),
                        getLongOrNull(obj, BREAK_GROUP_ID));
            case Setexceptionbreak:
                bpTypeObj = obj.get(TYPE);
                if (!(bpTypeObj instanceof String)) {
                    return null;
                }
                bpType = (String) bpTypeObj;
                return new SetExceptionBreak.Arguments(
                        V8ExceptionBreakType.valueOf(bpType),
                        getBoolean(obj, BREAK_ENABLED));
            case Source:
                return new Source.Arguments(
                        getLongOrNull(obj, FRAME),
                        getLongOrNull(obj, FROM_LINE),
                        getLongOrNull(obj, TO_LINE));
            case V8flags:
                return new V8Flags.Arguments(getString(obj, FLAGS));
            default:
                return null;
        }
    }
    
    private static V8Arguments getSpecialArguments(V8Command command, JSONObject obj) {
        // Arguments that are directly on the command request:
        switch (command) {
            case Source:
                return new Source.Arguments(
                        getLongOrNull(obj, FRAME),
                        getLongOrNull(obj, FROM_LINE),
                        getLongOrNull(obj, TO_LINE));
            default:
                return null;
        }
    }
    
    private static Evaluate.Arguments.Context[] getAdditionalContext(Object obj) {
        if (!(obj instanceof JSONArray)) {
            return null;
        }
        JSONArray array = (JSONArray) obj;
        int n = array.size();
        Evaluate.Arguments.Context[] context = new Evaluate.Arguments.Context[n];
        for (int i = 0; i < n; i++) {
            context[i] = getContext(array.get(i));
        }
        return context;
    }
    
    private static Evaluate.Arguments.Context getContext(Object obj) {
        if (!(obj instanceof JSONObject)) {
            return null;
        }
        JSONObject jobj = (JSONObject) obj;
        return new Evaluate.Arguments.Context(getString(jobj, NAME),
                                              getLong(jobj, HANDLE));
    }
    
    /**
     * @return the String property value, or <code>null</code> when not defined.
     */
    private static String getString(JSONObject obj, String propertyName) {
        return (String) obj.get(propertyName);
    }
    
    private static String[] getStringValuesFromArray(JSONArray array, String propertyName) {
        int l = array.size();
        String[] strings = new String[l];
        for (int i = 0; i < l; i++) {
            strings[i] = getString((JSONObject) array.get(i), propertyName);
        }
        return strings;
    }
    
    /**
     * @return the long property value, or <code>-1</code> when not defined.
     */
    private static long getLong(JSONObject obj, String propertyName) {
        return getLong(obj, propertyName, -1);
    }
    
    /**
     * @return the long property value, or the defaultValue when not defined.
     */
    private static long getLong(JSONObject obj, String propertyName, long defaultValue) {
        Object prop = obj.get(propertyName);
        if (prop == null) {
            return defaultValue;
        }
        if (prop instanceof Long) {
            return (Long) prop;
        } else {
            String str = (String) prop;
            return Long.parseLong(str);
        }
    }
    
    private static Long getLongOrNull(JSONObject obj, String propertyName) {
        Object prop = obj.get(propertyName);
        if (prop == null) {
            return null;
        }
        return (Long) prop;
    }
    
    private static PropertyLong getLongProperty(JSONObject obj, String propertyName) {
        Object prop = obj.get(propertyName);
        if (prop == null) {
            return new PropertyLong(null);
        }
        if (prop instanceof Long) {
            return new PropertyLong((Long) prop);
        } else {
            String str = (String) prop;
            return new PropertyLong(Long.parseLong(str));
        }
    }
    
    /**
     * @return the boolean property value, or <code>false</code> when not defined.
     */
    private static boolean getBoolean(JSONObject obj, String propertyName) {
        Object prop = obj.get(propertyName);
        if (prop == null) {
            return false;
        }
        return (Boolean) prop;
    }
    
    private static Boolean getBooleanOrNull(JSONObject obj, String propertyName) {
        Object prop = obj.get(propertyName);
        if (prop == null) {
            return null;
        }
        return (Boolean) prop;
    }
    
    private static V8ScriptLocation getScriptLocation(JSONObject obj) {
        long id = getLong(obj, ID);
        String name = getString(obj, NAME);
        long line = getLong(obj, SCRIPT_LINE_OFFSET);
        long column = getLong(obj, SCRIPT_COLUMN_OFFSET);
        long lineCount = getLong(obj, SCRIPT_LINE_COUNT);
        return new V8ScriptLocation(id, name, line, column, lineCount);
    }
    
    private static V8Script getScript(JSONObject obj) {
        long handle = getLong(obj, HANDLE);
        String name = getString(obj, NAME);
        long id = getLong(obj, ID);
        long lineOffset = getLong(obj, SCRIPT_LINE_OFFSET);
        long columnOffset = getLong(obj, SCRIPT_COLUMN_OFFSET);
        long lineCount = getLong(obj, SCRIPT_LINE_COUNT);
        Object data = obj.get(DATA);
        String source = getString(obj, SOURCE);
        String sourceStart = getString(obj, SOURCE_START);
        Long sourceLength = getLongOrNull(obj, SOURCE_LENGTH);
        ReferencedValue context = getReferencedValue(obj, CONTEXT);
        String text = getString(obj, TEXT);
        long scriptTypeNum = getLong(obj, SCRIPT_TYPE);
        V8Script.Type scriptType = (scriptTypeNum >= 0) ? V8Script.Type.valueOf((int) scriptTypeNum) : null;
        long compilationTypeNum = getLong(obj, COMPILATION_TYPE);
        V8Script.CompilationType compilationType = (compilationTypeNum >= 0) ? V8Script.CompilationType.valueOf((int) compilationTypeNum) : null;
        ReferencedValue evalFromScript = getReferencedValue(obj, EVAL_FROM_SCRIPT);
        V8Script.EvalFromLocation evalFromLocation;
        if (V8Script.CompilationType.EVAL.equals(compilationType)) {
            evalFromLocation = new V8Script.EvalFromLocation(getLong(obj, LINE), getLong(obj, COLUMN));
        } else {
            evalFromLocation = null;
        }
        return new V8Script(handle, name, id, lineOffset, columnOffset, lineCount, data, source, sourceStart, sourceLength, context, text, scriptType, compilationType, evalFromScript, evalFromLocation);
    }

    private static V8Value getValue(JSONObject obj) {
        return getValue(obj, -1);
    }
    
    private static V8Value getValue(JSONObject obj, long handle) {
        if (handle < 0) {
            handle = getLong(obj, HANDLE);
        }
        V8Value.Type type = V8Value.Type.fromString(getString(obj, TYPE));
        String text = getString(obj, TEXT);
        switch (type) {
            case Boolean:
                return new V8Boolean(handle, getBoolean(obj, VALUE), text);
            case Number:
                Object nVal = obj.get(VALUE);
                if (nVal instanceof Long) {
                    return new V8Number(handle, (Long) nVal, text);
                }
                if (nVal instanceof Double) {
                    return new V8Number(handle, (Double) nVal, text);
                }
                if (nVal == null) {
                    return new V8Number(handle, -1l, text);
                }
                if (INFINITY.equals(nVal)) {
                    return new V8Number(handle, Double.POSITIVE_INFINITY, text);
                }
                if (("-"+INFINITY).equals(nVal)) {
                    return new V8Number(handle, Double.NEGATIVE_INFINITY, text);
                }
                if (NaN.equals(nVal)) {
                    return new V8Number(handle, Double.NaN, text);
                }
                throw new IllegalArgumentException("Unknown variable value type: "+nVal);
            case String:
                return new V8String(handle, getString(obj, VALUE), text);
            case Function:
                String name = getString(obj, NAME);
                String inferredName = getString(obj, FUNCTION_INFERRED_NAME);
                Boolean resolved = getBooleanOrNull(obj, FUNCTION_RESOLVED);
                String source = getString(obj, SOURCE);
                PropertyLong scriptRef = getReferenceProperty(obj, SCRIPT);
                Long scriptId = getLongOrNull(obj, SCRIPTID);
                PropertyLong position = getLongProperty(obj, POSITION);
                PropertyLong line = getLongProperty(obj, LINE);
                PropertyLong column = getLongProperty(obj, COLUMN);
                PropertyLong constructorFunctionHandle = getReferenceProperty(obj, VALUE_CONSTRUCTOR_FUNCTION);
                PropertyLong protoObject = getReferenceProperty(obj, VALUE_PROTO_OBJECT);
                PropertyLong prototypeObject = getReferenceProperty(obj, VALUE_PROTOTYPE_OBJECT);
                V8Scope[] scopes = null;
                if (obj.get(SCOPES) instanceof JSONArray) {
                    scopes = getScopes((JSONArray) obj.get(SCOPES), null);
                }
                Map<String, V8Object.Property> properties = getProperties((JSONArray) obj.get(VALUE_PROPERTIES), null);
                return new V8Function(handle, constructorFunctionHandle,
                                      protoObject, prototypeObject,
                                      name, inferredName, resolved,
                                      source, scriptRef, scriptId,
                                      position, line, column, scopes, properties, text);
            case Generator:
                String className = getString(obj, VALUE_CLASS_NAME);
                constructorFunctionHandle = getReferenceProperty(obj, VALUE_CONSTRUCTOR_FUNCTION);
                protoObject = getReferenceProperty(obj, VALUE_PROTO_OBJECT);
                prototypeObject = getReferenceProperty(obj, VALUE_PROTOTYPE_OBJECT);
                PropertyLong function = getReferenceProperty(obj, FRAME_FUNC);
                PropertyLong receiver = getReferenceProperty(obj, FRAME_RECEIVER);
                properties = getProperties((JSONArray) obj.get(VALUE_PROPERTIES), null);
                return new V8Generator(handle, className, constructorFunctionHandle, protoObject, prototypeObject, function, receiver, properties, text);
            case Object:
            case Error:
            case Regexp:
                className = getString(obj, VALUE_CLASS_NAME);
                constructorFunctionHandle = getReferenceProperty(obj, VALUE_CONSTRUCTOR_FUNCTION);
                protoObject = getReferenceProperty(obj, VALUE_PROTO_OBJECT);
                prototypeObject = getReferenceProperty(obj, VALUE_PROTOTYPE_OBJECT);
                V8Object.Array[] arrayRef;
                boolean isArray = "Array".equals(className);
                if (isArray) {
                    arrayRef = new V8Object.Array[] { null };
                } else {
                    arrayRef = null;
                }
                properties = getProperties((JSONArray) obj.get(VALUE_PROPERTIES), arrayRef);
                if (isArray && arrayRef[0] == null) {
                    arrayRef[0] = new V8Object.DefaultArray();
                }
                V8Object.Array array = (arrayRef != null) ? arrayRef[0] : null;
                return new V8Object(handle, type, className,
                                    constructorFunctionHandle,
                                    protoObject, prototypeObject,
                                    properties, array, text);
            case Frame:
                // ? TODO
                return new V8Value(handle, type, text);
            case Script:
                V8Script script = getScript(obj);
                return new V8ScriptValue(handle, script, text);
            default: // null, undefined
                return new V8Value(handle, type, text);
        }
    }

    private static long[] getLongArray(JSONArray array) {
        if (array == null) {
            return null;
        }
        int n = array.size();
        long[] iarr = new long[n];
        for (int i = 0; i < n; i++) {
            iarr[i] = (Long) array.get(i);
        }
        return iarr;
    }
    
    private static V8Breakpoint[] getBreakpoints(JSONArray array) {
        int n = array.size();
        V8Breakpoint[] breakpoints = new V8Breakpoint[n];
        for (int i = 0; i < n; i++) {
            breakpoints[i] = getBreakpoint((JSONObject) array.get(i));
        }
        return breakpoints;
    }

    private static V8Breakpoint getBreakpoint(JSONObject obj) {
        String typeStr = (String) obj.get(TYPE);
        /*if ("scriptName".equals(typeStr)) {
            typeStr = V8Breakpoint.Type.script.name();
        }*/
        V8Breakpoint.Type type = V8Breakpoint.Type.valueOf(typeStr);
        PropertyLong scriptId;
        String scriptName;
        if (V8Breakpoint.Type.scriptId.equals(type)) {
            scriptId = new PropertyLong(getLong(obj, SCRIPT_ID));
            scriptName = null;
        } else {
            scriptId = new PropertyLong(null);
            scriptName = getString(obj, SCRIPT_NAME);
        }
        long number = getLong(obj, NUMBER);
        PropertyLong line = getLongProperty(obj, LINE);
        PropertyLong column = getLongProperty(obj, COLUMN);
        PropertyLong groupId = getLongProperty(obj, BREAK_GROUP_ID);
        long hitCount = getLong(obj, BREAK_HIT_COUNT, 0);
        boolean active = getBoolean(obj, BREAK_ACTIVE);
        Object conditionObject = obj.get(BREAK_CONDITION);
        String condition;
        if (conditionObject instanceof String) {
            condition = (String) conditionObject;
        } else {
            condition = null;
        }
        long ignoreCount = getLong(obj, BREAK_IGNORE_COUNT, 0);
        V8Breakpoint.ActualLocation[] actualLocations = getActualLocations((JSONArray) obj.get(BREAK_ACTUAL_LOCATIONS));
        return new V8Breakpoint(type, scriptId, scriptName, number, line, column, groupId, hitCount, active, condition, ignoreCount, actualLocations);
    }
    
    private static V8Breakpoint.ActualLocation[] getActualLocations(JSONArray array) {
        int n = array.size();
        V8Breakpoint.ActualLocation[] locations = new V8Breakpoint.ActualLocation[n];
        for (int i = 0; i < n; i++) {
            JSONObject location = (JSONObject) array.get(i);
            long line = getLong(location, LINE);
            long column = getLong(location, COLUMN);
            String scriptName = getString(location, SCRIPT_NAME);
            if (scriptName != null) {
                locations[i] = new V8Breakpoint.ActualLocation(line, column, scriptName);
            } else {
                long scriptId = getLong(location, SCRIPT_ID);
                locations[i] = new V8Breakpoint.ActualLocation(line, column, scriptId);
            }
        }
        return locations;
    }
    
    private static V8Frame[] getFrames(JSONArray array) {
        if (array == null) {
            return new V8Frame[]{};
        }
        int n = array.size();
        V8Frame[] frames = new V8Frame[n];
        for (int i = 0; i < n; i++) {
            frames[i] = getFrame((JSONObject) array.get(i));
        }
        return frames;
    }
    
    private static V8Frame getFrame(JSONObject obj) {
        Long index = getLongOrNull(obj, INDEX);
        ReferencedValue receiver = getReferencedValue(obj, FRAME_RECEIVER);
        ReferencedValue func = getReferencedValue(obj, FRAME_FUNC);
        long scriptRef = getReference(obj, SCRIPT);
        boolean constructCall = getBoolean(obj, FRAME_CONSTRUCT_CALL);
        boolean atReturn = getBoolean(obj, FRAME_AT_RETURN);
        boolean debuggerFrame = getBoolean(obj, FRAME_DEBUGGER);
        Map<String, ReferencedValue> arguments = getReferences((JSONArray) obj.get(FRAME_ARGUMENTS));
        Map<String, ReferencedValue> locals = getReferences((JSONArray) obj.get(FRAME_LOCALS));
        long position = getLong(obj, POSITION);
        long line = getLong(obj, LINE);
        long column = getLong(obj, COLUMN);
        String sourceLineText = getString(obj, EVT_SOURCE_LINE_TEXT);
        V8Scope[] scopes = getScopes((JSONArray) obj.get(SCOPES), null);
        String text = getString(obj, TEXT);
        return new V8Frame(index, receiver, func, scriptRef, constructCall, atReturn,
                           debuggerFrame, arguments, locals, position, line, column,
                           sourceLineText, scopes, text);
    }
    
    private static Map<String, ReferencedValue> getReferences(JSONArray array) {
        if (array == null) {
            return null;
        }
        Map<String, ReferencedValue> references = new LinkedHashMap<>();
        for (Object obj : array) {
            String name = getString((JSONObject) obj, NAME);
            ReferencedValue ref = getReferencedValue((JSONObject) obj, VALUE);
            references.put(name, ref);
        }
        return references;
    }
    
    private static Map<String, V8Object.Property> getProperties(JSONArray array, V8Object.Array[] oArrayRef) {
        if (array == null) {
            return null;
        }
        if (array.isEmpty()) {
            return Collections.<String, V8Object.Property>emptyMap();
        }
        Map<String, V8Object.Property> properties = new LinkedHashMap<>();
        V8Object.Property.Type[] types = V8Object.Property.Type.values();
        V8Object.DefaultArray oArray = null;
        for (Object obj : array) {
            JSONObject prop = (JSONObject) obj;
            Object nameObj = prop.get(NAME);
            if (nameObj == null) {
                continue;
            }
            //String name = getString(prop, NAME);
            long ref = getReference(prop);
            if (oArrayRef != null && nameObj instanceof Long) {
                // An array
                if (oArray == null) {
                    oArray = new V8Object.DefaultArray();
                }
                long index = (Long) nameObj;
                oArray.putReferenceAt(index, ref);
            } else {
                String name = nameObj.toString();
                long attributes = getLong(prop, ATTRIBUTES, 0);
                long propertyTypeNum = getLong(prop, PROPERTY_TYPE);
                V8Object.Property.Type type;
                if (propertyTypeNum < 0) {
                    type = null;
                } else if (propertyTypeNum < types.length) {
                    type = types[(int) propertyTypeNum];
                } else {
                    throw new IllegalArgumentException("Unknown property type: "+propertyTypeNum);
                }
                V8Object.Property property = new V8Object.Property(name, type, (int) attributes, ref);
                properties.put(name, property);
            }
        }
        if (oArrayRef != null) {
            oArrayRef[0] = oArray;
        }
        return properties;
    }
    
    private static ReferencedValue[] getRefs(JSONArray array) {
        if (array == null) {
            return null;
        }
        int n = array.size();
        ReferencedValue[] refs = new ReferencedValue[n];
        for (int i = 0; i < n; i++) {
            JSONObject obj = (JSONObject) array.get(i);
            long ref = getReference(obj);
            V8Value value = getValue(obj, ref);
            if (value != null) {
                refs[i] = new ReferencedValue(value.getHandle(), value);
            } else {
                refs[i] = new ReferencedValue(ref, value);
            }
        }
        return refs;
    }

    private static long getReference(JSONObject obj) {
        return getLong(obj, REF);
    }
    
    private static Long getReference(JSONObject obj, String propertyName) {
        JSONObject ref = (JSONObject) obj.get(propertyName);
        if (ref == null) {
            return null;
        }
        return getReference(ref);
    }
    
    private static PropertyLong getReferenceProperty(JSONObject obj, String propertyName) {
        JSONObject ref = (JSONObject) obj.get(propertyName);
        if (ref == null) {
            return new PropertyLong(null);
        }
        return getLongProperty(ref, REF);
    }
    
    private static ReferencedValue getReferencedValue(JSONObject obj, String propertyName) {
        JSONObject ref = (JSONObject) obj.get(propertyName);
        if (ref == null) {
            return null;
        }
        long reference = getReference(ref);
        V8Value value = null;
        if (getString(ref, TYPE) != null) {
            value = getValue(ref, reference);
        }
        return new ReferencedValue(reference, value);
    }
    
    private static V8Scope[] getScopes(JSONArray array, Long frameIndex) {
        int n = array.size();
        V8Scope[] scopes = new V8Scope[n];
        for (int i = 0; i < n; i++) {
            scopes[i] = getScope((JSONObject) array.get(i), frameIndex);
        }
        return scopes;
    }

    private static V8Scope getScope(JSONObject scope, Long frameIndex) {
        V8Scope.Type type = V8Scope.Type.valueOf((int) getLong(scope, TYPE));
        long index = getLong(scope, SCOPE_INDEX);
        PropertyLong scopeFrameIndex = getLongProperty(scope, FRAME_INDEX);
        if (!scopeFrameIndex.hasValue() && frameIndex != null) {
            scopeFrameIndex = new PropertyLong(frameIndex);
        }
        ReferencedValue referencedValue = getReferencedValue(scope, OBJECT);
        ReferencedValue<V8Object> referencedObject;
        if (referencedValue != null) {
            V8Object object = referencedValue.hasValue() ? (V8Object) referencedValue.getValue() : null;
            referencedObject = new ReferencedValue<>(referencedValue.getReference(), object);
        } else {
            referencedObject = null;
        }
        String text = getString(scope, TEXT);
        return new V8Scope(index, scopeFrameIndex, type, referencedObject, text);
    }
    
    private static Map<Long, Boolean> getThreads(JSONArray array) {
        Map<Long, Boolean> threads = new LinkedHashMap<>();
        for (Object o : array) {
            JSONObject obj = (JSONObject) o;
            threads.put(getLong(obj, ID), getBoolean(obj, CURRENT));
        }
        return threads;
    }
    
    private static ChangeLive.Result getChangeLiveResult(JSONObject resultObj) {
        ChangeLive.Result.ChangeTree changeTree = getChangeTree((JSONObject) resultObj.get(CHANGE_TREE));
        ChangeLive.Result.TextualDiff diff = getTextualDiff((JSONObject) resultObj.get(TEXTUAL_DIFF));
        boolean updated = getBoolean(resultObj, UPDATED);
        Boolean stackModified = getBooleanOrNull(resultObj, STACK_MODIFIED);
        Boolean stackUpdateNeedsStepIn = getBooleanOrNull(resultObj, STACK_UPDATE_NEEDS_STEP_IN);
        String createdScriptName = getString(resultObj, CREATED_SCRIPT_NAME);
        return new ChangeLive.Result(changeTree, diff, updated, stackModified, stackUpdateNeedsStepIn, createdScriptName);
    }

    private static ChangeLive.Result.ChangeTree getChangeTree(JSONObject obj) {
        String name = getString(obj, NAME);
        ChangeLive.Result.ChangeTree.Positions positions = getChangeTreePositions((JSONObject) obj.get(POSITIONS));
        ChangeLive.Result.ChangeTree.Positions newPositions = getChangeTreePositions((JSONObject) obj.get(NEW_POSITIONS));
        ChangeLive.Result.ChangeTree.FunctionStatus status;
        String statusStr = getString(obj, STATUS);
        if (statusStr != null) {
            status = ChangeLive.Result.ChangeTree.FunctionStatus.fromString(statusStr);
        } else {
            status = null;
        }
        String statusExplanation = getString(obj, STATUS_EXPLANATION);
        ChangeLive.Result.ChangeTree[] children = getChangeTreeChildren((JSONArray) obj.get(CHILDREN));
        ChangeLive.Result.ChangeTree[] newChildren = getChangeTreeChildren((JSONArray) obj.get(NEW_CHILDREN));
        return new ChangeLive.Result.ChangeTree(name, positions, newPositions, status, statusExplanation, children, newChildren);
    }
    
    private static ChangeLive.Result.ChangeTree[] getChangeTreeChildren(JSONArray array) {
        if (array == null) {
            return null;
        }
        int n = array.size();
        ChangeLive.Result.ChangeTree[] ch = new ChangeLive.Result.ChangeTree[n];
        for (int i = 0; i < n; i++) {
            ch[i] = getChangeTree((JSONObject) array.get(i));
        }
        return ch;
    }

    private static ChangeLive.Result.TextualDiff getTextualDiff(JSONObject obj) {
        long oldLen = getLong(obj, OLD_LEN);
        long newLen = getLong(obj, NEW_LEN);
        long[] chunks = getLongArray((JSONArray) obj.get(CHUNKS));
        return new ChangeLive.Result.TextualDiff(oldLen, newLen, chunks);
    }
    
    private static ChangeLive.Result.ChangeTree.Positions getChangeTreePositions(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        long startPosition = getLong(obj, START_POSITION);
        long endPosition = getLong(obj, END_POSITION);
        return new ChangeLive.Result.ChangeTree.Positions(startPosition, endPosition);
    }

    private static ChangeLive.ChangeLog getChangeLog(JSONArray array) {
        int n = array.size();
        if (n == 0) {
            return null;
        }
        ChangeLive.ChangeLog.BreakpointUpdate[] breakpointsUpdate = null;
        String[] namesLinkedToOldScript = null;
        String[] droppedFrames = null;
        ChangeLive.ChangeLog.FunctionPatched functionPatched = null;
        ChangeLive.ChangeLog.PositionPatched[] patchedPositions = null;
        for (Object aelem : array) {
            if (!(aelem instanceof JSONObject)) {
                continue;
            }
            JSONObject obj = (JSONObject) aelem;
            JSONArray breakpointsUpdateArr = (JSONArray) obj.get(BREAK_POINTS_UPDATE);
            if (breakpointsUpdateArr != null) {
                breakpointsUpdate = getChangeLogBreakpointsUpdate(breakpointsUpdateArr);
            }
            JSONArray linkedToOldScriptArr = (JSONArray) obj.get(LINKED_TO_OLD_SCRIPT);
            if (linkedToOldScriptArr != null) {
                namesLinkedToOldScript = getStringValuesFromArray(linkedToOldScriptArr, NAME);
            }
            JSONArray droppedFromStack = (JSONArray) obj.get(DROPPED_FROM_STACK);
            if (droppedFromStack != null) {
                droppedFrames = getStringValuesFromArray(droppedFromStack, NAME);
            }
            String fp = (String) obj.get(FUNCTION_PATCHED);
            if (fp != null) {
                Boolean finf = getBooleanOrNull(obj, FUNCTION_INFO_NOT_FOUND);
                functionPatched = new ChangeLive.ChangeLog.FunctionPatched(fp, new PropertyBoolean(finf));
            }
            JSONArray positionPatchedArr = (JSONArray) obj.get(POSITION_PATCHED);
            if (positionPatchedArr != null) {
                patchedPositions = getPatchedPositions(positionPatchedArr);
            }
        }
        return new ChangeLive.ChangeLog(breakpointsUpdate, namesLinkedToOldScript,
                                        droppedFrames, functionPatched, patchedPositions);
    }
    
    private static ChangeLive.ChangeLog.BreakpointUpdate[] getChangeLogBreakpointsUpdate(JSONArray array) {
        int l = array.size();
        ChangeLive.ChangeLog.BreakpointUpdate[] bpus = new ChangeLive.ChangeLog.BreakpointUpdate[l];
        for (int i = 0; i < l; i++) {
            JSONObject bpu = (JSONObject) array.get(i);
            String typeRaw = getString(bpu, TYPE);
            ChangeLive.ChangeLog.BreakpointUpdate.Type type =
                    ChangeLive.ChangeLog.BreakpointUpdate.Type.fromString(typeRaw);
            long id = getLong(bpu, ID);
            PropertyLong newId = getLongProperty(bpu, NEW_ID);
            ChangeLive.ChangeLog.BreakpointUpdate.Position oldPositions = null;
            ChangeLive.ChangeLog.BreakpointUpdate.Position newPositions = null;
            JSONObject positionsObj = (JSONObject) bpu.get(OLD_POSITIONS);
            if (positionsObj != null) {
                oldPositions = getPositions(positionsObj);
            }
            positionsObj = (JSONObject) bpu.get(POSITIONS);
            if (positionsObj == null) {
                positionsObj = (JSONObject) bpu.get(NEW_POSITIONS);
            }
            if (positionsObj != null) {
                newPositions = getPositions(positionsObj);
            }
            bpus[i] = new ChangeLive.ChangeLog.BreakpointUpdate(type, id, newId,
                                                                oldPositions, newPositions);
        }
        return bpus;
    }
    
    private static ChangeLive.ChangeLog.BreakpointUpdate.Position getPositions(JSONObject positionObj) {
        long position = getLong(positionObj, POSITION);
        long line = getLong(positionObj, LINE);
        long column = getLong(positionObj, COLUMN);
        return new ChangeLive.ChangeLog.BreakpointUpdate.Position(position, line, column);
    }
    
    private static ChangeLive.ChangeLog.PositionPatched[] getPatchedPositions(JSONArray array) {
        int l = array.size();
        ChangeLive.ChangeLog.PositionPatched[] pps = new ChangeLive.ChangeLog.PositionPatched[l];
        for (int i = 0; i < l; i++) {
            JSONObject pp = (JSONObject) array.get(i);
            String name = getString(pp, NAME);
            Boolean infoNF = getBooleanOrNull(pp, INFO_NOT_FOUND);
            pps[i] = new ChangeLive.ChangeLog.PositionPatched(name, new PropertyBoolean(infoNF));
        }
        return pps;
    }

}
