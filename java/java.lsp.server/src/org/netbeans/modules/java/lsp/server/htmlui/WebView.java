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
package org.netbeans.modules.java.lsp.server.htmlui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.presenters.spi.ProtoPresenter;
import org.netbeans.html.presenters.spi.ProtoPresenterBuilder;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.HtmlPageParams;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.spi.htmlui.HTMLViewerSpi;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

public class WebView implements Closeable {

    private static final Logger LOG = Logger.getLogger(WebView.class.getName());

    private final Consumer<Page> viewer;
    private final String app;
    private Runnable onPageLoad;
    private String id;

    public WebView(Consumer<Page> viewer) {
        this.viewer = viewer;
        this.app = findCalleeClassName();
    }

    public void displayPage(HTMLViewerSpi.Context ctx) {
        try {
            this.onPageLoad = () -> {
                Buttons.registerCloseWindow();
                try {
                    ctx.onPageLoad();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            };
            this.id = UUID.randomUUID().toString();
            Server.SESSIONS.put(this.id, new Command(this));
            this.viewer.accept(new Page(this.id, getText(ctx.getPage(), this.id), getResources(ctx.getPage(), ctx.getResources())));
        } catch (IOException ex) {
            Logger.getLogger(WebView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close() throws IOException {
        Server.SESSIONS.remove(this.id);
    }

    private String getText(URL page, String id) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Reader is = new InputStreamReader(page.openStream())) {
            int state = id != null ? 0 : 1000;
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                char lower = Character.toLowerCase((char)ch);
                switch (state) {
                    case 1000: break;
                    case 0: if (lower == '<') state = 1; break;
                    case 1: if (lower == 'b') state = 2;
                        else if (lower != ' ' && lower != '\n') state = 0;
                        break;
                    case 2: if (lower == 'o') state = 3; else state = 0; break;
                    case 3: if (lower == 'd') state = 4; else state = 0; break;
                    case 4: if (lower == 'y') state = 5; else state = 0; break;
                    case 5: if (lower == '>') state = 500;
                        else if (lower != ' ' && lower != '\n') state = 0;
                        break;
                }
                sb.append((char)ch);
                if (state == 500) {
                    emitScript(sb, id);
                    state = 1000;
                }
            }
            if (state != 1000) {
                emitScript(sb, id);
            }
        }
        return sb.toString();
    }

    private void emitScript(StringBuilder sb, String id) throws IOException {
        
        sb.append("<script id='exec' type='text/javascript'>\n"
                + "const vscode = acquireVsCodeApi();\n"
                + "(function () {\n"
                + "  window.addEventListener('message', new Function('event', `\n"
                + "    const message = event.data;\n"
                + "    if (message.pause) {\n"
                + "      debugger;\n"
                + "    }\n"
                + "    if (message.execScript) {\n"
                + "      try {\n"
                + "        (0 || eval)(message.execScript);\n"
                + "      } catch (e) {\n"
                + "        console.warn(e); \n"
                + "      }\n"
                + "    }\n"
                + "  `));\n"
                + "  vscode.postMessage({\n"
                + "    command: 'command',\n"
                + "    data: {\n"
                + "      id: '" + id + "'\n"
                + "    }\n"
                + "  });\n"
                + "}());\n"
                + "</script>\n");
    }

    private Map<String, String> getResources(URL page, String[] resources) throws IOException {
        Map<String, String> ret = new HashMap<>();
        for (String resource : resources) {
            ret.put(resource, getText(new URL(page, resource), null));
        }
        return ret;
    }

    private String createCallbackFn(String id) {
        return "this.toBrwsrSrvr = function(name, a1, a2, a3, a4) {\n"
             + "  vscode.postMessage({\n"
             + "    command: 'command',\n"
             + "    data: {\n"
             + "      id: '" + id + "',\n"
             + "      name,\n"
             + "      p0: encodeURIComponent(a1),\n"
             + "      p1: encodeURIComponent(a2),\n"
             + "      p2: encodeURIComponent(a3),\n"
             + "      p3: encodeURIComponent(a4)\n"
             + "    }"
             + "  });\n"
             + "  return '';\n"
             + "};\n";
    }

    private static String findCalleeClassName() {
        StackTraceElement[] frames = new Exception().getStackTrace();
        for (StackTraceElement e : frames) {
            String cn = e.getClassName();
            if (cn.startsWith("com.dukescript.presenters.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("org.netbeans.html.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("net.java.html.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("java.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("javafx.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("com.sun.")) { // NOI18N
                continue;
            }
            return cn;
        }
        return "org.netbeans.html"; // NOI18N
    }

    public static final class Page {
        private final String id;
        private final String text;
        private final Map<String, String> resources;

        private Page(String id, String text, Map<String, String> resources) {
            this.id = id;
            this.text = text;
            this.resources = resources;
        }

        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public Map<String, String> getResources() {
            return resources;
        }
    }

    private static final class Command implements Executor, ThreadFactory {
        private final WebView webView;
        private final String id;
        private final Executor RUN;
        private Thread RUNNER;
        private NbCodeLanguageClient client;
        private final ProtoPresenter presenter;

        private Command(WebView webView) {
            this.webView = webView;
            this.id = this.webView.id;
            this.RUN = Executors.newSingleThreadExecutor(this);
            this.presenter = ProtoPresenterBuilder.newBuilder().
                preparator(this::callbackFn, true).
                loadJavaScript(this::loadJS, false).
                app(webView.app).
                dispatcher(this, true).
                displayer(this::displayPage).
                logger(this::log).
                type("Browser").
                register(this).
                build();
        }

        @Override
        public void execute(final Runnable r) {
            runSafe(r, this.presenter);
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Processor for " + id);
            RUNNER = t;
            return t;
        }

        private void execScript(String obj) {
            client.execInHtmlPage(new HtmlPageParams(id, obj));
        }

        private void initialize(NbCodeLanguageClient client) {
            if (this.client == null) {
                this.client = client;
                execute(webView.onPageLoad);
            }
        }

        private CompletableFuture<Object> service(NbCodeLanguageClient client, Map params) {
            CompletableFuture future = new CompletableFuture();
            final String methodName = (String) params.get("name");
            if (methodName == null) {
                initialize(client);
                future.complete(null);
            } else {
                try {
                    final String result = presenter.js2java(methodName,
                            URLDecoder.decode((String) params.get("p0"), "UTF-8"),
                            URLDecoder.decode((String) params.get("p1"), "UTF-8"),
                            URLDecoder.decode((String) params.get("p2"), "UTF-8"),
                            URLDecoder.decode((String) params.get("p3"), "UTF-8"));
                    future.complete(result);
                } catch (Exception ex) {
                    future.complete("error:" + ex.getMessage());
                }
            }
            return future;
        }

        private void callbackFn(ProtoPresenterBuilder.OnPrepared onReady) {
            String sb = this.webView.createCallbackFn(id);
            execScript(sb);
            onReady.callbackIsPrepared("toBrwsrSrvr");
        }

        private void log(int priority, String msg, Object... args) {
            Level level = findLevel(priority);

            if (args.length == 1 && args[0] instanceof Throwable) {
                LOG.log(level, msg, (Throwable) args[0]);
            } else {
                LOG.log(level, msg, args);
            }
        }

        private void loadJS(String js) {
            execScript(js);
        }

        private void displayPage(URL url, Runnable r) {
            throw new UnsupportedOperationException(url.toString());
        }

        private void runSafe(Runnable r, Fn.Presenter p) {
            class Wrap implements Runnable {

                @Override
                public void run() {
                    if (p != null) {
                        try ( Closeable c = Fn.activate(p)) {
                            r.run();
                        } catch (IOException ex) {
                            // go on
                        }
                    } else {
                        r.run();
                    }
                }
            }
            if (RUNNER == Thread.currentThread()) {
                if (p != null) {
                    Runnable w = new Wrap();
                    w.run();
                } else {
                    r.run();
                }
            } else {
                Runnable w = new Wrap();
                RUN.execute(w);
            }
        }

        private static Level findLevel(int priority) {
            if (priority >= Level.SEVERE.intValue()) {
                return Level.SEVERE;
            }
            if (priority >= Level.WARNING.intValue()) {
                return Level.WARNING;
            }
            if (priority >= Level.INFO.intValue()) {
                return Level.INFO;
            }
            return Level.FINE;
        }
    } // end of Command

    @ServiceProvider(service = CodeActionsProvider.class)
    public static final class Server extends CodeActionsProvider {

        private static final Map<String, WebView.Command> SESSIONS = new HashMap<>();
        private static final String PROCESS_COMMAND = "nbls.htmlui.process.command"; // NOI18N
        private static final String ID = "id"; // NOI18N
        private final Gson gson = new Gson();

        @Override
        public Set<String> getCommands() {
            return Collections.singleton(PROCESS_COMMAND);
        }

        @Override
        public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
            if (arguments != null && !arguments.isEmpty()) {
                final Map m = gson.fromJson((JsonObject) arguments.get(0), Map.class);
                final String id = (String) m.get(ID);
                if (id != null) {
                    WebView.Command c = SESSIONS.get(id);
                    if (c != null) {
                        return c.service(client, m).thenApply(res -> res != null ? res : "null");
                    }
                }
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
            return Collections.emptyList();
        }
    } // end of Server
}
