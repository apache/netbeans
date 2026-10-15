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
package org.netbeans.lib.chrome_devtools_protocol;

import com.google.gson.JsonElement;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointByUrlRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointByUrlResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.BreakpointResolved;
import org.netbeans.lib.chrome_devtools_protocol.debugger.ContinueToLocationRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.ContinueToLocationResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.DisableRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.DisableResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.EnableRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.EnableResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.GetPossibleBreakpointsRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.GetPossibleBreakpointsResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.GetScriptSourceRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.GetScriptSourceResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.PauseRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.PauseResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.Paused;
import org.netbeans.lib.chrome_devtools_protocol.debugger.RemoveBreakpointRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.RemoveBreakpointResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.ResumeRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.ResumeResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.Resumed;
import org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptFailedToParse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptParsed;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SearchInContentRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SearchInContentResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetAsyncCallStackDepthRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetAsyncCallStackDepthResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointsActiveRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointsActiveResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetInstrumentationBreakpointRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetInstrumentationBreakpointResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetPauseOnExceptionsRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetPauseOnExceptionsResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetScriptSourceRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetScriptSourceResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetSkipAllPausesRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetSkipAllPausesResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetVariableValueRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetVariableValueResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.StepIntoRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.StepIntoResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.StepOutRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.StepOutResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.StepOverRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.StepOverResponse;

public final class DebuggerDomain {
    private final ChromeDevToolsClient cdtc;

    DebuggerDomain(ChromeDevToolsClient cdtc) {
        this.cdtc = cdtc;
    }

    /**
     * Continues execution until specific location is reached.
     */
    public CompletionStage<ContinueToLocationResponse> continueToLocation(ContinueToLocationRequest req) {
        return cdtc.methodCall("Debugger.continueToLocation", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, ContinueToLocationResponse.class));
    }

    /**
     * Disables debugger for given page.
     */
    public CompletionStage<DisableResponse> disable(DisableRequest req) {
        return cdtc.methodCall("Debugger.disable", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, DisableResponse.class));
    }

    /**
     * Enables debugger for the given page. Clients should not assume that the
     * debugging has been enabled until the result for this command is received.
     */
    public CompletionStage<EnableResponse> enable(EnableRequest req) {
        return cdtc.methodCall("Debugger.enable", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, EnableResponse.class));
    }

    /**
     * Evaluates expression on a given call frame.
     */
    public CompletionStage<EvaluateOnCallFrameResponse> evaluateOnCallFrame(EvaluateOnCallFrameRequest req) {
        return cdtc.methodCall("Debugger.evaluateOnCallFrame", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, EvaluateOnCallFrameResponse.class));
    }

    /**
     * Returns possible locations for breakpoint. scriptId in start and end range locations should be the same.
     */
    public CompletionStage<GetPossibleBreakpointsResponse> getPossibleBreakpoints(GetPossibleBreakpointsRequest req) {
        return cdtc.methodCall("Debugger.getPossibleBreakpoints", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, GetPossibleBreakpointsResponse.class));
    }

    /**
     * Returns source for the script with given id.
     */
    public CompletionStage<GetScriptSourceResponse> getScriptSource(GetScriptSourceRequest req) {
        return cdtc.methodCall("Debugger.getScriptSource", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, GetScriptSourceResponse.class));
    }

    /**
     * Stops on the next JavaScript statement.
     */
    public CompletionStage<PauseResponse> pause(PauseRequest req) {
        return cdtc.methodCall("Debugger.pause", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, PauseResponse.class));
    }

    /**
     * Removes JavaScript breakpoint.
     */
    public CompletionStage<RemoveBreakpointResponse> removeBreakpoint(RemoveBreakpointRequest req) {
        return cdtc.methodCall("Debugger.removeBreakpoint", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, RemoveBreakpointResponse.class));
    }

    /**
     * Resumes JavaScript execution.
     */
    public CompletionStage<ResumeResponse> resume(ResumeRequest req) {
        return cdtc.methodCall("Debugger.resume", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, ResumeResponse.class));
    }

    /**
     *
     */
    public CompletionStage<SearchInContentRequest> searchInContent(SearchInContentResponse req) {
        return cdtc.methodCall("Debugger.searchInContent", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, SearchInContentRequest.class));
    }

    /**
     * Enables or disables async call stacks tracking.
     */
    public CompletionStage<SetAsyncCallStackDepthResponse> setAsyncCallStackDepth(SetAsyncCallStackDepthRequest req) {
        return cdtc.methodCall("Debugger.setAsyncCallStackDepth", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, SetAsyncCallStackDepthResponse.class));
    }

    /**
     * Sets JavaScript breakpoint at a given location.
     */
    public CompletionStage<SetBreakpointResponse> setBreakpoint(SetBreakpointRequest req) {
        return cdtc.methodCall("Debugger.setBreakpoint", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, SetBreakpointResponse.class));
    }

    /**
     * Sets JavaScript breakpoint at given location specified either by URL or
     * URL regex. Once this command is issued, all existing parsed scripts will
     * have breakpoints resolved and returned in {@code locations} property.
     * Further matching script parsing will result in subsequent
     * {@code breakpointResolved} events issued. This logical breakpoint will
     * survive page reloads.
     */
    public CompletionStage<SetBreakpointByUrlResponse> setBreakpointByUrl(SetBreakpointByUrlRequest bbur) {
        return cdtc.methodCall("Debugger.setBreakpointByUrl", bbur)
                .thenApply(s -> cdtc.getGson().fromJson(s, SetBreakpointByUrlResponse.class));
    }

    /**
     * Activates / deactivates all breakpoints on the page.
     */
    public CompletionStage<SetBreakpointsActiveResponse> setBreakpointsActive(SetBreakpointsActiveRequest req) {
        return cdtc.methodCall("Debugger.setBreakpointsActive", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, SetBreakpointsActiveResponse.class));
    }

    /**
     * Sets instrumentation breakpoint.
     */
    public CompletionStage<SetInstrumentationBreakpointResponse> setInstrumentationBreakpoint(SetInstrumentationBreakpointRequest req) {
        return cdtc.methodCall("Debugger.setInstrumentationBreakpoint", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, SetInstrumentationBreakpointResponse.class));
    }

    /**
     * Defines pause on exceptions state. Can be set to stop on all exceptions,
     * uncaught exceptions or no exceptions. Initial pause on exceptions state
     * is {@code none}.
     */
    public CompletionStage<SetPauseOnExceptionsResponse> setPauseOnExceptions(SetPauseOnExceptionsRequest req) {
        return cdtc.methodCall("Debugger.setPauseOnExceptions", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, SetPauseOnExceptionsResponse.class));
    }

    /**
     * Edits JavaScript source live.
     */
    public CompletionStage<SetScriptSourceResponse> setScriptSource(SetScriptSourceRequest req) {
        return cdtc.methodCall("Debugger.setScriptSource", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, SetScriptSourceResponse.class));
    }

    /**
     * Makes page not interrupt on any pauses (breakpoint, exception, dom exception etc).
     */
    public CompletionStage<SetSkipAllPausesResponse> setSkipAllPauses(SetSkipAllPausesRequest req) {
        return cdtc.methodCall("Debugger.setSkipAllPauses", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, SetSkipAllPausesResponse.class));
    }

    /**
     * Changes value of variable in a callframe. Object-based scopes are not
     * supported and must be mutated manually.
     */
    public CompletionStage<SetVariableValueResponse> setVariableValue(SetVariableValueRequest req) {
        return cdtc.methodCall("Debugger.setVariableValue", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, SetVariableValueResponse.class));
    }

    /**
     * Steps into the function call.
     */
    public CompletionStage<StepIntoResponse> stepInto(StepIntoRequest req) {
        return cdtc.methodCall("Debugger.stepInto", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, StepIntoResponse.class));
    }

    /**
     * Steps out of the function call.
     */
    public CompletionStage<StepOutResponse> stepOut(StepOutRequest req) {
        return cdtc.methodCall("Debugger.stepOut", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, StepOutResponse.class));
    }

    /**
     * Steps over the statement.
     */
    public CompletionStage<StepOverResponse> stepOver(StepOverRequest req) {
        return cdtc.methodCall("Debugger.stepOver", req)
                .thenApply(s -> cdtc.getGson().fromJson(s, StepOverResponse.class));
    }

    public Unregisterer onBreakpointResolved(Consumer<BreakpointResolved> handler) {
        Consumer<JsonElement> adapter = (je) -> {
            BreakpointResolved parsed = cdtc.getGson().fromJson(je, BreakpointResolved.class);
            handler.accept(parsed);
        };
        cdtc.registerEventHandler("Debugger.breakpointResolved", adapter);
        return () -> cdtc.unregisterEventHandler("Debugger.breakpointResolved", adapter);
    }

    public Unregisterer onPaused(Consumer<Paused> handler) {
        Consumer<JsonElement> adapter = (je) -> {
            Paused parsed = cdtc.getGson().fromJson(je, Paused.class);
            handler.accept(parsed);
        };
        cdtc.registerEventHandler("Debugger.paused", adapter);
        return () -> cdtc.unregisterEventHandler("Debugger.paused", adapter);
    }
    public Unregisterer onResumed(Consumer<Resumed> handler) {
        Consumer<JsonElement> adapter = (je) -> {
            Resumed parsed = cdtc.getGson().fromJson(je, Resumed.class);
            handler.accept(parsed);
        };
        cdtc.registerEventHandler("Debugger.resumed", adapter);
        return () -> cdtc.unregisterEventHandler("Debugger.resumed", adapter);
    }

    public Unregisterer onScriptParsed(Consumer<ScriptParsed> scriptParsedHandler) {
        Consumer<JsonElement> adapter = (je) -> {
            ScriptParsed parsed = cdtc.getGson().fromJson(je, ScriptParsed.class);
            scriptParsedHandler.accept(parsed);
        };
        cdtc.registerEventHandler("Debugger.scriptParsed", adapter);
        return () -> cdtc.unregisterEventHandler("Debugger.scriptParsed", adapter);
    }

    public Unregisterer onScriptFailedToParse(Consumer<ScriptFailedToParse> handler) {
        Consumer<JsonElement> adapter = (je) -> {
            ScriptFailedToParse parsed = cdtc.getGson().fromJson(je, ScriptFailedToParse.class);
            handler.accept(parsed);
        };
        cdtc.registerEventHandler("Debugger.scriptFailedToParse", adapter);
        return () -> cdtc.unregisterEventHandler("Debugger.scriptFailedToParse", adapter);
    }
}
