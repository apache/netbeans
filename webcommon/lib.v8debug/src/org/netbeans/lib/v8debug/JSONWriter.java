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

import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
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
import org.netbeans.lib.v8debug.connection.LinkedJSONObject;
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
 * Translator of Java API classes into the corresponding JSON objects.
 * 
 * @author Martin Entlicher
 */
@SuppressWarnings("unchecked")
public class JSONWriter {
    
    private JSONWriter() {}
    
    public static JSONObject store(V8Request request) {
        JSONObject obj = newJSONObject();
        obj.put(SEQ, request.getSequence());
        obj.put(TYPE, V8Type.request.toString());
        V8Command command = request.getCommand();
        obj.put(COMMAND, command.toString());
        V8Arguments arguments = request.getArguments();
        if (arguments != null) {
            obj.put(ARGUMENTS, store(command, arguments));
        }
        return obj;
    }
    
    public static JSONObject store(V8Response response) {
        JSONObject obj = newJSONObject();
        obj.put(SEQ, response.getSequence());
        obj.put(SEQ_REQUEST, response.getRequestSequence());
        obj.put(TYPE, V8Type.response.toString());
        V8Command command = response.getCommand();
        obj.put(COMMAND, command.toString());
        boolean success = response.isSuccess();
        obj.put(SUCCESS, success);
        if (success) {
            V8Body body = response.getBody();
            if (body != null) {
                obj.put(BODY, store(command, body));
            }
        } else {
            obj.put(MESSAGE, response.getErrorMessage());
        }
        ReferencedValue[] referencedValues = response.getReferencedValues();
        if (referencedValues != null) {
            obj.put(REFS, store(referencedValues));
        }
        obj.put(RUNNING, response.isRunning());
        return obj;
    }
    
    public static JSONObject store(V8Event event) {
        JSONObject obj = newJSONObject();
        obj.put(SEQ, event.getSequence());
        obj.put(TYPE, V8Type.event.toString());
        V8Event.Kind eventKind = event.getKind();
        obj.put(EVENT, eventKind.toString());
        V8Body body = event.getBody();
        JSONObject bodyObj = newJSONObject();
        switch (eventKind) {
            case AfterCompile:
                AfterCompileEventBody aceb = (AfterCompileEventBody) body;
                bodyObj.put(EVT_SCRIPT, store(aceb.getScript()));
                break;
            case CompileError:
                CompileErrorEventBody ceeb = (CompileErrorEventBody) body;
                bodyObj.put(EVT_SCRIPT, store(ceeb.getScript()));
                break;
            case ScriptCollected:
                ScriptCollectedEventBody sceb = (ScriptCollectedEventBody) body;
                bodyObj.put(EVT_SCRIPT, sceb.getScriptId());
                break;
            case Break:
                BreakEventBody beb = (BreakEventBody) body;
                storeIf(beb.getInvocationText(), bodyObj, EVT_INVOCATION_TEXT);
                bodyObj.put(EVT_SOURCE_LINE, beb.getSourceLine());
                bodyObj.put(EVT_SOURCE_COLUMN, beb.getSourceColumn());
                storeIf(beb.getSourceLineText(), bodyObj, EVT_SOURCE_LINE_TEXT);
                V8ScriptLocation scriptLocation = beb.getScript();
                if (scriptLocation != null) {
                    bodyObj.put(EVT_SCRIPT, store(scriptLocation));
                }
                long[] breakpoints = beb.getBreakpoints();
                if (breakpoints != null) {
                    bodyObj.put(EVT_BREAKPOINTS, array(breakpoints));
                }
                break;
            case Exception:
                ExceptionEventBody eeb = (ExceptionEventBody) body;
                bodyObj.put(EVT_UNCAUGHT, eeb.isUncaught());
                bodyObj.put(EVT_EXCEPTION, store(eeb.getException()));
                bodyObj.put(EVT_SOURCE_LINE, eeb.getSourceLine());
                bodyObj.put(EVT_SOURCE_COLUMN, eeb.getSourceColumn());
                storeIf(eeb.getSourceLineText(), bodyObj, EVT_SOURCE_LINE_TEXT);
                bodyObj.put(EVT_SCRIPT, store(eeb.getScript(), false));
                break;
            default:
                throw new IllegalArgumentException("Unknown event kind: "+eventKind);
        }
        if (event.getSuccess().hasValue()) {
            obj.put(SUCCESS, event.getSuccess().getValue());
        }
        obj.put(BODY, bodyObj);
        ReferencedValue[] referencedValues = event.getReferencedValues();
        if (referencedValues != null) {
            obj.put(REFS, store(referencedValues));
        }
        PropertyBoolean running = event.isRunning();
        if (running.hasValue()) {
            obj.put(RUNNING, running.getValue());
        }
        return obj;
    }
    
    private static JSONObject newJSONObject() {
        return new LinkedJSONObject();
    }
    
    private static Object store(V8Command command, V8Arguments arguments) {
        JSONObject obj = newJSONObject();
        switch(command) {
            case Backtrace:
                Backtrace.Arguments btargs = (Backtrace.Arguments) arguments;
                storeIf(btargs.getFromFrame(), obj, FROM_FRAME);
                storeIf(btargs.getToFrame(), obj, TO_FRAME);
                storeIf(btargs.isBottom(), obj, BOTTOM);
                storeIf(btargs.isInlineRefs(), obj, INLINE_REFS);
                return obj;
            case Continue:
                Continue.Arguments cargs = (Continue.Arguments) arguments;
                obj.put(ARGS_STEP_ACTION, cargs.getStepAction().toString());
                storeIf(cargs.getStepCount(), obj, ARGS_STEP_COUNT);
                return obj;
            case Setbreakpoint:
                SetBreakpoint.Arguments sbargs = (SetBreakpoint.Arguments) arguments;
                String bpType;
                if (V8Breakpoint.Type.scriptName.equals(sbargs.getType())) {
                    bpType = "script";
                } else {
                    bpType = sbargs.getType().toString();
                }
                obj.put(TYPE, bpType);
                obj.put(TARGET, sbargs.getTarget());
                storeIf(sbargs.getLine(), obj, LINE);
                storeIf(sbargs.getColumn(), obj, COLUMN);
                storeIf(sbargs.isEnabled(), obj, BREAK_ENABLED);
                storeIf(sbargs.getCondition(), obj, BREAK_CONDITION);
                storeIf(sbargs.getIgnoreCount(), obj, BREAK_IGNORE_COUNT);
                storeIf(sbargs.getGroupId(), obj, BREAK_GROUP_ID);
                return obj;
            case Changebreakpoint:
                ChangeBreakpoint.Arguments chbargs = (ChangeBreakpoint.Arguments) arguments;
                obj.put(BREAK_POINT, chbargs.getBreakpoint());
                storeIf(chbargs.isEnabled(), obj, BREAK_ENABLED);
                obj.put(BREAK_CONDITION, chbargs.getCondition());
                storeIf(chbargs.getIgnoreCount(), obj, BREAK_IGNORE_COUNT);
                return obj;
            case Clearbreakpoint:
                ClearBreakpoint.Arguments cbargs = (ClearBreakpoint.Arguments) arguments;
                obj.put(BREAK_POINT, cbargs.getBreakpoint());
                return obj;
            case Clearbreakpointgroup:
                ClearBreakpointGroup.Arguments cbgargs = (ClearBreakpointGroup.Arguments) arguments;
                obj.put(BREAK_GROUP_ID, cbgargs.getGroupId());
                return obj;
            case Setexceptionbreak:
                SetExceptionBreak.Arguments sebargs = (SetExceptionBreak.Arguments) arguments;
                obj.put(TYPE, sebargs.getType().toString());
                obj.put(BREAK_ENABLED, sebargs.isEnabled());
                return obj;
            case Evaluate:
                Evaluate.Arguments eargs = (Evaluate.Arguments) arguments;
                obj.put(EVAL_EXPRESSION, eargs.getExpression());
                storeIf(eargs.getFrame(), obj, FRAME);
                storeIf(eargs.isGlobal(), obj, EVAL_GLOBAL);
                storeIf(eargs.isDisableBreak(), obj, EVAL_DISABLE_BREAK);
                JSONArray additionalContexts = store(eargs.getAdditionalContext());
                if (additionalContexts != null) {
                    obj.put(EVAL_ADDITIONAL_CONTEXT, additionalContexts);
                }
                return obj;
            case Frame:
                Frame.Arguments fargs = (Frame.Arguments) arguments;
                storeIf(fargs.getFrameNumber(), obj, NUMBER);
                return obj;
            case Restartframe:
                RestartFrame.Arguments ra = (RestartFrame.Arguments) arguments;
                PropertyLong frame = ra.getFrame();
                if (frame.hasValue()) {
                    obj.put(FRAME, frame.getValue());
                }
                return obj;
            case Lookup:
                Lookup.Arguments largs = (Lookup.Arguments) arguments;
                obj.put(HANDLES, array(largs.getHandles()));
                storeIf(largs.isIncludeSource(), obj, INCLUDE_SOURCE);
                return obj;
            case References:
                References.Arguments rargs = (References.Arguments) arguments;
                obj.put(TYPE, rargs.getType().name());
                obj.put(HANDLE, rargs.getHandle());
                return obj;
            case Scope:
                Scope.Arguments sargs = (Scope.Arguments) arguments;
                obj.put(NUMBER, sargs.getScopeNumber());
                storeIf(sargs.getFrameNumber(), obj, FRAME_NUMBER);
                return obj;
            case Scopes:
                Scopes.Arguments ssargs = (Scopes.Arguments) arguments;
                storeIf(ssargs.getFrameNumber(), obj, FRAME_NUMBER);
                return obj;
            case Scripts:
                Scripts.Arguments scargs = (Scripts.Arguments) arguments;
                if (scargs.getTypes() != null) {
                    obj.put(TYPES, scargs.getTypes().getIntTypes());
                }
                if (scargs.getIds() != null) {
                    obj.put(IDs, array(scargs.getIds()));
                }
                storeIf(scargs.isIncludeSource(), obj, INCLUDE_SOURCE);
                storeIf(scargs.getNameFilter(), obj, FILTER);
                storeIf(scargs.getIdFilter(), obj, FILTER);
                return obj;
            case Source:
                Source.Arguments srcargs = (Source.Arguments) arguments;
                storeIf(srcargs.getFrame(), obj, FRAME);
                storeIf(srcargs.getFromLine(), obj, FROM_LINE);
                storeIf(srcargs.getToLine(), obj, TO_LINE);
                return obj;
            case SetVariableValue:
                SetVariableValue.Arguments svargs = (SetVariableValue.Arguments) arguments;
                obj.put(NAME, svargs.getName());
                obj.put(NEW_VALUE, store(svargs.getNewValue()));
                JSONObject scope = newJSONObject();
                scope.put(NUMBER, svargs.getScopeNumber());
                storeIf(svargs.getScopeFrameNumber(), scope, FRAME_NUMBER);
                obj.put(SCOPE, scope);
                return obj;
            case Changelive:
                ChangeLive.Arguments chlargs = (ChangeLive.Arguments) arguments;
                obj.put(SCRIPT_ID, chlargs.getScriptId());
                storeIf(chlargs.isPreviewOnly(), obj, PREVIEW_ONLY);
                obj.put(NEW_SOURCE, chlargs.getNewSource());
                return obj;
            case Gc:
                GC.Arguments gcargs = (GC.Arguments) arguments;
                obj.put(TYPE, gcargs.getType());
                return obj;
            case V8flags:
                V8Flags.Arguments v8flargs = (V8Flags.Arguments) arguments;
                obj.put(FLAGS, v8flargs.getFlags());
                return obj;
            case Flags:
                Flags.Arguments flargs = (Flags.Arguments) arguments;
                Map<String, Boolean> flags = flargs.getFlags();
                if (flags != null) {
                    JSONArray arr = new JSONArray();
                    for (Map.Entry<String, Boolean> flagEntry : flags.entrySet()) {
                        JSONObject f = newJSONObject();
                        f.put(NAME, flagEntry.getKey());
                        f.put(VALUE, flagEntry.getValue());
                        arr.add(f);
                    }
                    obj.put(FLAGS, arr);
                }
                return obj;
            default:
                return null;
        }
    }
    
    private static JSONAware store(V8Command command, V8Body body) {
        switch (command) {
            case Listbreakpoints:
                JSONObject obj = newJSONObject();
                ListBreakpoints.ResponseBody lbrb = (ListBreakpoints.ResponseBody) body;
                obj.put(BREAK_POINTS, store(lbrb.getBreakpoints()));
                obj.put(BREAK_ON_EXCEPTIONS, lbrb.isBreakOnExceptions());
                obj.put(BREAK_ON_UNCAUGHT_EXCEPTIONS, lbrb.isBreakOnUncaughtExceptions());
                return obj;
            case Setbreakpoint:
                obj = newJSONObject();
                SetBreakpoint.ResponseBody sbrb = (SetBreakpoint.ResponseBody) body;
                obj.put(TYPE, sbrb.getType().toString());
                obj.put(BREAK_POINT, sbrb.getBreakpoint());
                if (V8Breakpoint.Type.scriptId.equals(sbrb.getType())) {
                    storeIf(sbrb.getScriptName(), obj, SCRIPT_ID);
                } else {
                    storeIf(sbrb.getScriptName(), obj, SCRIPT_NAME);
                }
                obj.put(LINE, getLongOrNull(sbrb.getLine()));
                obj.put(COLUMN, getLongOrNull(sbrb.getColumn()));
                obj.put(BREAK_ACTUAL_LOCATIONS, store(sbrb.getActualLocations()));
                return obj;
            case Setexceptionbreak:
                obj = newJSONObject();
                SetExceptionBreak.ResponseBody sebrb = (SetExceptionBreak.ResponseBody) body;
                obj.put(TYPE, sebrb.getType().toString());
                obj.put(BREAK_ENABLED, sebrb.isEnabled());
                return obj;
            case Clearbreakpoint:
                obj = newJSONObject();
                ClearBreakpoint.ResponseBody cbrb = (ClearBreakpoint.ResponseBody) body;
                obj.put(BREAK_POINT, cbrb.getBreakpoint());
                return obj;
            case Clearbreakpointgroup:
                obj = newJSONObject();
                ClearBreakpointGroup.ResponseBody cbgrb = (ClearBreakpointGroup.ResponseBody) body;
                obj.put(BREAK_POINTS, array(cbgrb.getBreakpointsCleared()));
                return obj;
            case Backtrace:
                obj = newJSONObject();
                Backtrace.ResponseBody brb = (Backtrace.ResponseBody) body;
                obj.put(FROM_FRAME, brb.getFromFrame());
                obj.put(TO_FRAME, brb.getToFrame());
                obj.put(TOTAL_FRAMES, brb.getTotalFrames());
                if (brb.getFrames() != null) {
                    obj.put(FRAMES, store(brb.getFrames()));
                }
                return obj;
            case Frame:
                Frame.ResponseBody frb = (Frame.ResponseBody) body;
                obj = store(frb.getFrame());
                return obj;
            case Restartframe:
                obj = newJSONObject();
                RestartFrame.ResponseBody rfrb = (RestartFrame.ResponseBody) body;
                obj.put(RESULT, store(rfrb.getResult()));
                return obj;
            case Changelive:
                obj = newJSONObject();
                ChangeLive.ResponseBody clrb = (ChangeLive.ResponseBody) body;
                obj.put(CHANGE_LOG, store(clrb.getChangeLog()));
                obj.put(RESULT, store(clrb.getResult()));
                storeIf(clrb.getStepInRecommended(), obj, STEP_IN_RECOMMENDED);
                return obj;
            case Lookup:
                Lookup.ResponseBody lrb = (Lookup.ResponseBody) body;
                obj = storeValues(lrb.getValuesByHandle());
                return obj;
            case Evaluate:
                Evaluate.ResponseBody erb = (Evaluate.ResponseBody) body;
                obj = store(erb.getValue());
                return obj;
            case References:
                References.ResponseBody rrb = (References.ResponseBody) body;
                JSONArray arrayRefs = new JSONArray();
                for (V8Value vr : rrb.getReferences()) {
                    arrayRefs.add(store(vr));
                }
                return arrayRefs;
            case Scripts:
                Scripts.ResponseBody scrrb = (Scripts.ResponseBody) body;
                return store(scrrb.getScripts());
            case SetVariableValue:
                obj = newJSONObject();
                SetVariableValue.ResponseBody svvrb = (SetVariableValue.ResponseBody) body;
                obj.put(NEW_VALUE, store(svvrb.getNewValue()));
                return obj;
            case Scope:
                Scope.ResponseBody srb = (Scope.ResponseBody) body;
                obj = store(srb.getScope());
                return obj;
            case Scopes:
                obj = newJSONObject();
                Scopes.ResponseBody ssrb = (Scopes.ResponseBody) body;
                obj.put(FROM_SCOPE, ssrb.getFromScope());
                obj.put(TO_SCOPE, ssrb.getToScope());
                obj.put(TOTAL_SCOPES, ssrb.getTotalScopes());
                obj.put(SCOPES, store(ssrb.getScopes()));
                return obj;
            case Source:
                obj = newJSONObject();
                Source.ResponseBody srcrb = (Source.ResponseBody) body;
                storeIf(srcrb.getSource(), obj, SOURCE);
                obj.put(FROM_LINE, srcrb.getFromLine());
                obj.put(TO_LINE, srcrb.getToLine());
                obj.put(FROM_POSITION, srcrb.getFromPosition());
                obj.put(TO_POSITION, srcrb.getToPosition());
                obj.put(TOTAL_LINES, srcrb.getTotalLines());
                return obj;
            case Threads:
                obj = newJSONObject();
                Threads.ResponseBody trb = (Threads.ResponseBody) body;
                obj.put(TOTAL_THREADS, trb.getNumThreads());
                obj.put(THREADS, storeThreads(trb.getIds()));
                return obj;
            case Gc:
                obj = newJSONObject();
                GC.ResponseBody gcrb = (GC.ResponseBody) body;
                obj.put(GC_BEFORE, gcrb.getBefore());
                obj.put(GC_AFTER, gcrb.getAfter());
                return obj;
            case Version:
                obj = newJSONObject();
                Version.ResponseBody vrb = (Version.ResponseBody) body;
                obj.put(BODY_VERSION, vrb.getVersion());
                return obj;
            case Flags:
                obj = newJSONObject();
                Flags.ResponseBody flrb = (Flags.ResponseBody) body;
                obj.put(FLAGS, storeFlags(flrb.getFlags()));
                return obj;
        }
        return null;
    }
    
    private static Object getLongOrNull(PropertyLong pl) {
        if (pl.hasValue()) {
            return pl.getValue();
        } else {
            return null;
        }
    }
    
    private static JSONArray store(ReferencedValue[] rvs) {
        JSONArray array = new JSONArray();
        for (ReferencedValue rv : rvs) {
            array.add(store(rv, false, false));
        }
        return array;
    }
    
    private static JSONObject store(ReferencedValue rv, boolean storeRefNotHandle, boolean noTextAndLength) {
        JSONObject obj = newJSONObject();
        //obj.put(REF, rv.getReference());
        if (rv.hasValue()) {
            if (storeRefNotHandle) {
                obj.put(REF, rv.getReference());
                store(rv.getValue(), obj, false, noTextAndLength);
            } else {
                store(rv.getValue(), obj, true, noTextAndLength);
            }
        } else {
            obj.put(REF, rv.getReference());
        }
        return obj;
    }
    
    private static JSONArray store(V8Script[] scripts) {
        JSONArray array = new JSONArray();
        for (V8Script scr : scripts) {
            array.add(store(scr));
        }
        return array;
    }
    
    private static JSONObject store(V8Script script) {
        return store(script, true);
    }
    
    private static JSONObject store(V8Script script, boolean storeHandle) {
        JSONObject obj = newJSONObject();
        storeTo(script, obj, storeHandle);
        return obj;
    }
    
    private static void storeTo(V8Script script, JSONObject obj, boolean storeHandle) {
        if (storeHandle) {
            obj.put(HANDLE, script.getHandle());
            obj.put(TYPE, script.getType().toString());
        }
        if (storeHandle) {
            if (script.getName() != null) {
                obj.put(NAME, script.getName());
            }
            obj.put(ID, script.getId());
        } else { // Just different order for easy comparison with V8 debug protocol.
            obj.put(ID, script.getId());
            if (script.getName() != null) {
                obj.put(NAME, script.getName());
            }
        }
        obj.put(SCRIPT_LINE_OFFSET, script.getLineOffset());
        obj.put(SCRIPT_COLUMN_OFFSET, script.getColumnOffset());
        obj.put(SCRIPT_LINE_COUNT, script.getLineCount());
        if (script.getData() != null) {
            obj.put(DATA, script.getData());
        }
        storeIf(script.getSource(), obj, SOURCE);
        storeIf(script.getSourceStart(), obj, SOURCE_START);
        storeIf(script.getSourceLength(), obj, SOURCE_LENGTH);
        V8Script.Type scriptType = script.getScriptType();
        if (scriptType != null) {
            obj.put(SCRIPT_TYPE, scriptType.ordinal());
        }
        V8Script.CompilationType compilationType = script.getCompilationType();
        if (compilationType != null) {
            obj.put(COMPILATION_TYPE, compilationType.ordinal());
        }
        storeIf(script.getContext(), obj, CONTEXT);
        if (script.getText() != null) {
            obj.put(TEXT, script.getText());
        }
        storeIf(script.getEvalFromScript(), obj, EVAL_FROM_SCRIPT);
        if (V8Script.CompilationType.EVAL.equals(compilationType) && script.getEvalFromLocation() != null) {
            obj.put(LINE, script.getEvalFromLocation().getLine());
            obj.put(COLUMN, script.getEvalFromLocation().getColumn());
        }
    }
    
    private static JSONObject store(V8ScriptLocation scriptLocation) {
        JSONObject obj = newJSONObject();
        obj.put(ID, scriptLocation.getId());
        obj.put(NAME, scriptLocation.getName());
        obj.put(SCRIPT_LINE_OFFSET, scriptLocation.getLine());
        obj.put(SCRIPT_COLUMN_OFFSET, scriptLocation.getColumn());
        obj.put(SCRIPT_LINE_COUNT, scriptLocation.getLineCount());
        return obj;
    }
    
    private static JSONArray store(V8Breakpoint[] breakpoints) {
        JSONArray array = new JSONArray();
        for (V8Breakpoint bp : breakpoints) {
            array.add(store(bp));
        }
        return array;
    }
    
    private static JSONObject store(V8Breakpoint bp) {
        JSONObject obj = newJSONObject();
        obj.put(NUMBER, bp.getNumber());
        storeIf(bp.getLine(), obj, LINE);
        //storeIf(bp.getColumn(), obj, COLUMN);
        obj.put(COLUMN, getLongOrNull(bp.getColumn()));
        PropertyLong groupId = bp.getGroupId();
        obj.put(BREAK_GROUP_ID, getLongOrNull(groupId));
        if (bp.getHitCount() != 0) {
            obj.put(BREAK_HIT_COUNT, bp.getHitCount());
        }
        obj.put(BREAK_ACTIVE, bp.isActive());
        obj.put(BREAK_CONDITION, bp.getCondition());
        if (bp.getIgnoreCount() != 0) {
            obj.put(BREAK_IGNORE_COUNT, bp.getIgnoreCount());
        }
        obj.put(BREAK_ACTUAL_LOCATIONS, store(bp.getActualLocations()));
        obj.put(TYPE, bp.getType().toString());
        storeIf(bp.getScriptId(), obj, SCRIPT_ID);
        storeIf(bp.getScriptName(), obj, SCRIPT_NAME);
        return obj;
    }
    
    private static JSONArray store(V8Breakpoint.ActualLocation[] actualLocations) {
        JSONArray array = new JSONArray();
        if (actualLocations != null) {
            for (V8Breakpoint.ActualLocation al : actualLocations) {
                JSONObject obj = newJSONObject();
                obj.put(LINE, al.getLine());
                obj.put(COLUMN, al.getColumn());
                storeIf(al.getScriptId(), obj, SCRIPT_ID);
                storeIf(al.getScriptName(), obj, SCRIPT_NAME);
                array.add(obj);
            }
        }
        return array;
    }
    
    private static JSONArray store(V8Frame[] frames) {
        JSONArray array = new JSONArray();
        for (V8Frame frame : frames) {
            array.add(store(frame));
        }
        return array;
    }
    
    private static JSONObject store(V8Frame frame) {
        JSONObject obj = newJSONObject();
        obj.put(TYPE, V8Value.Type.Frame.toString());
        PropertyLong index = frame.getIndex();
        obj.put(INDEX, getLongOrNull(index));
        obj.put(FRAME_RECEIVER, store(frame.getReceiver(), true, false));
        obj.put(FRAME_FUNC, store(frame.getFunction(), true, false));
        storeReference(frame.getScriptRef(), obj, SCRIPT);
        obj.put(FRAME_CONSTRUCT_CALL, frame.isConstructCall());
        obj.put(FRAME_AT_RETURN, frame.isAtReturn());
        obj.put(FRAME_DEBUGGER, frame.isDebuggerFrame());
        storeReferences(frame.getArgumentRefs(), obj, FRAME_ARGUMENTS, true);
        storeReferences(frame.getLocalRefs(), obj, FRAME_LOCALS, true);
        obj.put(POSITION, frame.getPosition());
        obj.put(LINE, frame.getLine());
        obj.put(COLUMN, frame.getColumn());
        obj.put(EVT_SOURCE_LINE_TEXT, frame.getSourceLineText());
        obj.put(SCOPES, store(frame.getScopes()));
        obj.put(TEXT, frame.getText());
        return obj;
    }
    
    private static JSONArray store(ChangeLive.ChangeLog changeLog) {
        JSONArray array = new JSONArray();
        if (changeLog != null) {
            ChangeLive.ChangeLog.BreakpointUpdate[] breakpointsUpdate = changeLog.getBreakpointsUpdate();
            if (breakpointsUpdate != null) {
                JSONObject obj = newJSONObject();
                obj.put(BREAK_POINTS_UPDATE, store(breakpointsUpdate));
                array.add(obj);
            }
            String[] droppedFrames = changeLog.getDroppedFrames();
            if (droppedFrames != null) {
                JSONObject obj = newJSONObject();
                obj.put(DROPPED_FROM_STACK, storeStringValues(droppedFrames, NAME));
                array.add(obj);
            }
            String[] namesLinkedToOldScript = changeLog.getNamesLinkedToOldScript();
            if (namesLinkedToOldScript != null) {
                JSONObject obj = newJSONObject();
                obj.put(LINKED_TO_OLD_SCRIPT, storeStringValues(namesLinkedToOldScript, NAME));
                array.add(obj);
            }
            ChangeLive.ChangeLog.FunctionPatched functionPatched = changeLog.getFunctionPatched();
            if (functionPatched != null) {
                JSONObject obj = newJSONObject();
                obj.put(FUNCTION_PATCHED, functionPatched.getFunction());
                storeIf(functionPatched.getFunctionInfoNotFound(), obj, FUNCTION_INFO_NOT_FOUND);
                array.add(obj);
            }
            ChangeLive.ChangeLog.PositionPatched[] patchedPositions = changeLog.getPatchedPositions();
            if (patchedPositions != null) {
                JSONObject obj = newJSONObject();
                obj.put(POSITION_PATCHED, store(patchedPositions));
                array.add(obj);
            }
        }
        return array;
    }
    
    private static JSONArray store(ChangeLive.ChangeLog.BreakpointUpdate[] breakpointsUpdate) {
        JSONArray array = new JSONArray();
        for (ChangeLive.ChangeLog.BreakpointUpdate bpu : breakpointsUpdate) {
            JSONObject obj = newJSONObject();
            obj.put(ID, bpu.getId());
            storeIf(bpu.getNewId(), obj, NEW_ID);
            storeIf(bpu.getNewPositions(), obj, NEW_POSITIONS);
            storeIf(bpu.getOldPositions(), obj, POSITIONS);
            obj.put(TYPE, bpu.getType().toString());
            array.add(obj);
        }
        return array;
    }
    
    private static JSONArray store(ChangeLive.ChangeLog.PositionPatched[] patchedPositions) {
        JSONArray array = new JSONArray();
        for (ChangeLive.ChangeLog.PositionPatched pp : patchedPositions) {
            JSONObject obj = newJSONObject();
            storeIf(pp.getName(), obj, NAME);
            storeIf(pp.getInfoNotFound(), obj, INFO_NOT_FOUND);
            array.add(obj);
        }
        return array;
    }
    
    private static JSONObject store(ChangeLive.Result result) {
        JSONObject obj = newJSONObject();
        obj.put(CHANGE_TREE, store(result.getChangeTree()));
        obj.put(TEXTUAL_DIFF, store(result.getDiff()));
        obj.put(UPDATED, result.isUpdated());
        storeIf(result.getStackModified(), obj, STACK_MODIFIED);
        storeIf(result.getStackUpdateNeedsStepIn(), obj, STACK_UPDATE_NEEDS_STEP_IN);
        storeIf(result.getCreatedScriptName(), obj, CREATED_SCRIPT_NAME);
        return obj;
    }
    
    private static JSONObject store(ChangeLive.Result.ChangeTree changeTree) {
        JSONObject obj = newJSONObject();
        obj.put(NAME, changeTree.getName());
        storeIf(changeTree.getPositions(), obj, POSITIONS);
        if (changeTree.getStatus() != null) {
            obj.put(STATUS, changeTree.getStatus().toString());
        }
        obj.put(CHILDREN, store(changeTree.getChildren()));
        if (changeTree.getNewChildren() != null) {
            obj.put(NEW_CHILDREN, store(changeTree.getNewChildren()));
        }
        storeIf(changeTree.getStatusExplanation(), obj, STATUS_EXPLANATION);
        storeIf(changeTree.getNewPositions(), obj, NEW_POSITIONS);
        return obj;
    }
    
    private static JSONObject store(ChangeLive.Result.TextualDiff diff) {
        JSONObject obj = newJSONObject();
        obj.put(OLD_LEN, diff.getOldLength());
        obj.put(NEW_LEN, diff.getNewLength());
        obj.put(CHUNKS, array(diff.getChunks()));
        return obj;
    }
    
    private static void storeIf(ChangeLive.ChangeLog.BreakpointUpdate.Position position, JSONObject obj, String propName) {
        if (position != null) {
            JSONObject pos = newJSONObject();
            pos.put(POSITION, position.getPosition());
            pos.put(LINE, position.getLine());
            pos.put(COLUMN, position.getColumn());
            obj.put(propName, pos);
        }
    }
    
    private static JSONArray store(ChangeLive.Result.ChangeTree[] children) {
        JSONArray array = new JSONArray();
        if (children != null) {
            for (ChangeLive.Result.ChangeTree ct : children) {
                array.add(store(ct));
            }
        }
        return array;
    }
    
    private static void storeIf(ChangeLive.Result.ChangeTree.Positions positions, JSONObject obj, String propName) {
        if (positions != null) {
            JSONObject pos = newJSONObject();
            pos.put(START_POSITION, positions.getStartPosition());
            pos.put(END_POSITION, positions.getEndPosition());
            obj.put(propName, pos);
        }
    }
    
    private static JSONObject storeValues(Map<Long, V8Value> values) {
        JSONObject obj = newJSONObject();
        for (V8Value vv : values.values()) {
            obj.put(Long.toString(vv.getHandle()), store(vv));
        }
        return obj;
    }
    
    private static JSONArray store(V8Scope[] scopes) {
        JSONArray array = new JSONArray();
        for (V8Scope scope : scopes) {
            array.add(store(scope));
        }
        return array;
    }
    
    private static JSONObject store(V8Scope scope) {
        JSONObject obj = newJSONObject();
        obj.put(TYPE, scope.getType().ordinal());
        obj.put(SCOPE_INDEX, scope.getIndex());
        storeIf(scope.getFrameIndex(), obj, FRAME_INDEX);
        if (scope.getObject() != null) {
            obj.put(OBJECT, store(scope.getObject(), false, true));
        }
        storeIf(scope.getText(), obj, TEXT);
        return obj;
    }
    
    private static JSONArray storeThreads(Map<Long, Boolean> threads) {
        JSONArray array = new JSONArray();
        for (Map.Entry<Long, Boolean> thread : threads.entrySet()) {
            JSONObject obj = newJSONObject();
            obj.put(CURRENT, thread.getValue());
            obj.put(ID, thread.getKey());
            array.add(obj);
        }
        return array;
    }
    
    private static JSONArray storeFlags(Map<String, Boolean> flags) {
        JSONArray array = new JSONArray();
        for (Map.Entry<String, Boolean> flag : flags.entrySet()) {
            JSONObject obj = newJSONObject();
            obj.put(NAME, flag.getKey());
            obj.put(VALUE, flag.getValue());
            array.add(obj);
        }
        return array;
    }
    
    private static JSONObject store(Map<String, Object> map) {
        JSONObject obj = newJSONObject();
        obj.putAll(map);
        return obj;
    }
    
    private static JSONArray array(long[] array) {
        JSONArray jsArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsArray.add(array[i]);
        }
        return jsArray;
    }
    
    private static JSONArray storeStringValues(String[] ss, String propName) {
        JSONArray array = new JSONArray();
        for (String s : ss) {
            JSONObject obj = newJSONObject();
            obj.put(NAME, s);
            array.add(obj);
        }
        return array;
    }
    
    private static void storeIf(PropertyBoolean prop, JSONObject obj, String propertyName) {
        if (prop.hasValue()) {
            obj.put(propertyName, prop.getValue());
        }
    }
    
    private static void storeIf(PropertyLong prop, JSONObject obj, String propertyName) {
        if (prop.hasValue()) {
            obj.put(propertyName, prop.getValue());
        }
    }
    
    private static void storeIf(String prop, JSONObject obj, String propertyName) {
        if (prop != null) {
            obj.put(propertyName, prop);
        }
    }
    
    private static void storeIf(ReferencedValue rv, JSONObject obj, String propertyName) {
        if (rv == null) {
            return ;
        }
        JSONObject ref = newJSONObject();
        ref.put(REF, rv.getReference());
        if (rv.hasValue()) {
            store(rv.getValue(), ref, false, false);
        }
        obj.put(propertyName, ref);
    }
    
    private static JSONArray store(Evaluate.Arguments.Context[] contexts) {
        if (contexts == null) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (Evaluate.Arguments.Context c : contexts) {
            array.add(store(c));
        }
        return array;
    }
    
    private static JSONObject store(Evaluate.Arguments.Context context) {
        JSONObject obj = newJSONObject();
        obj.put(NAME, context.getName());
        obj.put(HANDLE, context.getHandle());
        return obj;
    }
    
    private static JSONObject store(NewValue value) {
        JSONObject obj = newJSONObject();
        if (value.getHandle().hasValue()) {
            obj.put(HANDLE, value.getHandle().getValue());
        } else {
            obj.put(TYPE, value.getType().toString());
            storeIf(value.getDescription(), obj, STRING_DESCRIPTION);
        }
        return obj;
    }
    
    private static JSONObject store(V8Value value) {
        JSONObject obj = newJSONObject();
        store(value, obj, true, false);
        return obj;
    }
    
    private static void store(V8Value value, JSONObject obj, boolean storeHandle, boolean noTextAndLength) {
        if (storeHandle) {
            obj.put(HANDLE, value.getHandle());
        }
        V8Value.Type type = value.getType();
        obj.put(TYPE, type.toString());
        switch (type) {
            case Boolean:
                V8Boolean vb = (V8Boolean) value;
                obj.put(VALUE, vb.getValue());
                break;
            case Number:
                V8Number vn = (V8Number) value;
                V8Number.Kind kind = vn.getKind();
                switch (kind) {
                    case Double:
                        double dv = vn.getDoubleValue();
                        if (dv == Double.POSITIVE_INFINITY) {
                            obj.put(VALUE, INFINITY);
                        } else if (dv == Double.NEGATIVE_INFINITY) {
                            obj.put(VALUE, "-"+INFINITY);
                        } else if (Double.isNaN(dv)) {
                            obj.put(VALUE, NaN);
                        } else {
                            obj.put(VALUE, dv);
                        }
                        break;
                    case Long:
                        obj.put(VALUE, vn.getLongValue());
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled number kind: "+kind);
                }
                break;
            case String:
                V8String vs = (V8String) value;
                String vss = vs.getValue();
                obj.put(VALUE, vss);
                if (!noTextAndLength) {
                    obj.put(LENGTH, vss.length());
                }
                break;
            case Function:
                V8Function vf = (V8Function) value;
                if (vf.isResolved().hasValue()) { // Hack to be compliant with original V8 protocol
                    obj.put(VALUE_CLASS_NAME, vf.getClassName());
                }
                storeReferenceIf(vf.getConstructorFunctionHandle(), obj, VALUE_CONSTRUCTOR_FUNCTION);
                storeReferenceIf(vf.getProtoObjectHandle(), obj, VALUE_PROTO_OBJECT);
                storeReferenceIf(vf.getPrototypeObjectHandle(), obj, VALUE_PROTOTYPE_OBJECT);
                storeIf(vf.getName(), obj, NAME);
                storeIf(vf.getInferredName(), obj, FUNCTION_INFERRED_NAME);
                storeIf(vf.isResolved(), obj, FUNCTION_RESOLVED);
                storeIf(vf.getSource(), obj, SOURCE);
                storeReferenceIf(vf.getScriptRef(), obj, SCRIPT);
                storeIf(vf.getScriptId(), obj, SCRIPTID);
                storeIf(vf.getPosition(), obj, POSITION);
                storeIf(vf.getLine(), obj, LINE);
                storeIf(vf.getColumn(), obj, COLUMN);
                V8Scope[] scopes = vf.getScopes();
                if (scopes != null) {
                    obj.put(SCOPES, store(scopes));
                }
                if (vf.getProperties() != null || vf.getArray() != null && vf.getArray().getLength() > 0) {
                    obj.put(VALUE_PROPERTIES, storeProperties(vf.getProperties(), vf.getArray()));
                }
                break;
            case Generator:
                V8Generator gf = (V8Generator) value;
                obj.put(VALUE_CLASS_NAME, gf.getClassName());
                storeReferenceIf(gf.getConstructorFunctionHandle(), obj, VALUE_CONSTRUCTOR_FUNCTION);
                storeReferenceIf(gf.getProtoObjectHandle(), obj, VALUE_PROTO_OBJECT);
                storeReferenceIf(gf.getPrototypeObjectHandle(), obj, VALUE_PROTOTYPE_OBJECT);
                storeReferenceIf(gf.getFunctionHandle(), obj, FRAME_FUNC);
                storeReferenceIf(gf.getReceiverHandle(), obj, FRAME_RECEIVER);
                if (gf.getProperties() != null) {
                    obj.put(VALUE_PROPERTIES, storeProperties(gf.getProperties(), null));
                }
                break;
            case Object:
            case Error:
            case Regexp:
                V8Object vo = (V8Object) value;
                obj.put(VALUE_CLASS_NAME, vo.getClassName());
                storeReferenceIf(vo.getConstructorFunctionHandle(), obj, VALUE_CONSTRUCTOR_FUNCTION);
                storeReferenceIf(vo.getProtoObjectHandle(), obj, VALUE_PROTO_OBJECT);
                storeReferenceIf(vo.getPrototypeObjectHandle(), obj, VALUE_PROTOTYPE_OBJECT);
                if (vo.getProperties() != null || vo.getArray() != null && vo.getArray().getLength() > 0) {
                    obj.put(VALUE_PROPERTIES, storeProperties(vo.getProperties(), vo.getArray()));
                }
                break;
            case Frame:
                // ? TODO
                break;
            case Script:
                V8ScriptValue vsv = (V8ScriptValue) value;
                storeTo(vsv.getScript(), obj, true);
                break;
            case Null:
                if (noTextAndLength) {
                    obj.put(VALUE, null);
                }
                break;
        }
        if (value.getText() != null && !noTextAndLength) {
            obj.put(TEXT, value.getText());
        }
    }
    
    private static void storeReferenceIf(PropertyLong ref, JSONObject obj, String propertyName) {
        if (ref.hasValue()) {
            storeReference(ref.getValue(), obj, propertyName);
        }
    }
    
    private static void storeReference(long ref, JSONObject obj, String propertyName) {
        JSONObject refObj = newJSONObject();
        refObj.put(REF, ref);
        obj.put(propertyName, refObj);
    }
    
    private static void storeReferences(Map<String, ReferencedValue> rvals,
                                        JSONObject obj, String propertyName,
                                        boolean storeRefNotHandle) {
        if (rvals == null) {
            return ;
        }
        JSONArray array = new JSONArray();
        for (Map.Entry<String, ReferencedValue> rval : rvals.entrySet()) {
            JSONObject elm = newJSONObject();
            if (rval.getKey() != null) {
                elm.put(NAME, rval.getKey());
            }
            elm.put(VALUE, store(rval.getValue(), storeRefNotHandle, true));
            array.add(elm);
        }
        obj.put(propertyName, array);
    }
    
    private static JSONArray storeProperties(Map<String, V8Object.Property> properties, V8Object.Array array) {
        JSONArray arrObj = new JSONArray();
        if (properties != null) {
            for (Map.Entry<String, V8Object.Property> entry : properties.entrySet()) {
                String propName = entry.getKey();
                V8Object.Property prop = entry.getValue();
                JSONObject propObj = newJSONObject();
                propObj.put(NAME, propName);
                if (prop.getAttributes() != 0) {
                    propObj.put(ATTRIBUTES, prop.getAttributes());
                }
                V8Object.Property.Type type = prop.getType();
                if (type != null) {
                    propObj.put(PROPERTY_TYPE, type.ordinal());
                } else {
                    propObj.put(PROPERTY_TYPE, 0);
                }
                propObj.put(REF, prop.getReference());
                arrObj.add(propObj);
            }
        }
        if (array != null) {
            V8Object.IndexIterator indexIterator = array.getIndexIterator();
            while (indexIterator.hasNextIndex()) {
                JSONObject arrElm = newJSONObject();
                long index = indexIterator.nextIndex();
                arrElm.put(NAME, index);
                arrElm.put(PROPERTY_TYPE, 0);
                arrElm.put(REF, array.getReferenceAt(index));
                arrObj.add(arrElm);
            }
        }
        return arrObj;
    }
}
