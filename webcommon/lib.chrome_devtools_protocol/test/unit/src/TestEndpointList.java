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

import java.net.URI;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.chrome_devtools_protocol.ChromeDevToolsClient;
import org.netbeans.lib.chrome_devtools_protocol.DebuggerDomain;
import org.netbeans.lib.chrome_devtools_protocol.RuntimeDomain;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.lib.chrome_devtools_protocol.debugger.DisableRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.EnableRequest;
import org.netbeans.lib.chrome_devtools_protocol.json.Endpoint;

import static org.netbeans.lib.chrome_devtools_protocol.CDTUtil.toNodeUrl;
/**
 *
 * @author matthias
 */
public class TestEndpointList {

    public static void main(String[] args) throws Exception {
        for(Handler h: Logger.getLogger("").getHandlers()) {
            h.setLevel(Level.ALL);
        }
        Logger.getLogger(ChromeDevToolsClient.class.getName()).setLevel(Level.FINE);
        Endpoint[] eps = ChromeDevToolsClient.listEndpoints("127.0.0.1", 9292);
        try(ChromeDevToolsClient cdtc = new ChromeDevToolsClient(eps[0].getWebSocketDebuggerUrl())) {
        cdtc.connect();
        DebuggerDomain dbg = cdtc.getDebugger();
        RuntimeDomain rt = cdtc.getRuntime();
//        dbg.onScriptParsed(sp -> {
//            if (sp.getUrl().getPath().endsWith("/main.js")) {
//                GetScriptSourceRequest req = new GetScriptSourceRequest();
//                req.setScriptId(sp.getScriptId());
//                dbg.getScriptSource(req).handle((res, thr) -> {
//                    System.out.printf("############# %s\t%s%n", sp.getScriptId(), sp.getUrl());
//                    if (thr != null) {
//                        System.out.println("FAILED: " + thr.getMessage());
//                    } else {
//                        System.out.println(res.getScriptSource());
//                    }
//                    return null;
//                });
//            }
//        });
//        dbg.onPaused(p -> {
//            System.out.printf("+ %s%n", p.getReason());
//        });

        List<CallFrame>[] callFrames = new List[1];
        dbg.onPaused(p -> {
            callFrames[0] = p.getCallFrames();
        });

        File file = new File("/home/matthias/tmp/NodeJsApplication/main.js");
        URI fileUri = toNodeUrl(file.toURI());

        CountDownLatch cdl = new CountDownLatch(1);

        CompletableFuture.completedStage(null)
                .thenCompose((x) -> dbg.enable(new EnableRequest()))
//                .thenCompose((er) -> {
//                    SetBreakpointByUrlRequest bbur = new SetBreakpointByUrlRequest();
//                    bbur.setUrl(fileUri);
//                    bbur.setLineNumber(15);
//                    bbur.setColumnNumber(0);
//                    return dbg.setBreakpointByUrl(bbur);
//                })
                .thenCompose((bbur) -> cdtc.getRuntime().runIfWaitingForDebugger())
                .thenCompose((bbur) -> delay(5, TimeUnit.SECONDS))
                .thenCompose((bbur) -> cdtc.getDebugger().resume(null))
                .thenCompose((bbur) -> delay(5, TimeUnit.SECONDS))
                .thenCompose((bbur) -> cdtc.getDebugger().disable(new DisableRequest()))
//                .thenCompose((res) -> {
//                    return cdtc.getDebugger().evaluateOnCallFrame(new EvaluateOnCallFrameRequest(callFrames[0].get(0).getCallFrameId(), "a"));
//                })
//                .thenCompose((res) -> {
//                    return cdtc.getDebugger().evaluateOnCallFrame(new EvaluateOnCallFrameRequest(callFrames[0].get(0).getCallFrameId(), "b"));
//                })
//                .thenCompose((res) -> {
//                    return cdtc.getDebugger().evaluateOnCallFrame(new EvaluateOnCallFrameRequest(callFrames[0].get(0).getCallFrameId(), "c"));
//                })
//                .thenCompose((res) -> {
//                    return cdtc.getDebugger().evaluateOnCallFrame(new EvaluateOnCallFrameRequest(callFrames[0].get(0).getCallFrameId(), "d"));
//                })
//                .thenCompose((res) -> {
//                    return cdtc.getDebugger().evaluateOnCallFrame(new EvaluateOnCallFrameRequest(callFrames[0].get(0).getCallFrameId(), "e"));
//                })
//                .thenCompose((res) -> {
//                    return cdtc.getDebugger().evaluateOnCallFrame(new EvaluateOnCallFrameRequest(callFrames[0].get(0).getCallFrameId(), "f"));
//                })
//                .thenCompose((res) -> {
//                    return cdtc.getDebugger().evaluateOnCallFrame(new EvaluateOnCallFrameRequest(callFrames[0].get(0).getCallFrameId(), "g"));
//                })
//                .thenCompose((res) -> {
//                    return cdtc.getDebugger().evaluateOnCallFrame(new EvaluateOnCallFrameRequest(callFrames[0].get(0).getCallFrameId(), "h"));
//                })
//                .thenCompose((res) -> {
//                    return cdtc.getDebugger().evaluateOnCallFrame(new EvaluateOnCallFrameRequest(callFrames[0].get(0).getCallFrameId(), "i"));
//                })
//                .thenCompose((res) -> {
//                    return CompletableFuture.completedFuture(null);
//                })
//                .thenCompose((bbur) -> {
//                    return cdtc.getDebugger().resume(null);
//                })
//                .thenCompose((res) -> {
//                    return delay(5, TimeUnit.SECONDS);
//                })
//                .thenCompose((bbur) -> {
//                    return cdtc.getDebugger().resume(null);
//                })
                //                .thenCompose((res) -> {
                //                    return delay(5, TimeUnit.SECONDS);
                //                })
                .handle((res, ex) -> {
                    if (ex != null) {
                        ex.printStackTrace();
                    };
                    try {
                        cdtc.close();
                    } catch (IOException ex1) {
                        Logger.getLogger(TestEndpointList.class.getName()).log(Level.SEVERE, null, ex1);
                    }
//                    cdl.countDown();
                    return null;
                });

            cdl.await();
        }
    }

    private static CompletableFuture<?> delay(int amount, TimeUnit unit) {
        CompletableFuture<?> result = new CompletableFuture<>();
        result.completeAsync(() -> null, CompletableFuture.delayedExecutor(amount, unit));
        return result;
    }
}
