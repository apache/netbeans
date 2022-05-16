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
package org.netbeans.modules.cpplite.debugger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIConst;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITListItem;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;
import org.netbeans.modules.nativeimage.api.debug.EvaluateException;
import org.netbeans.modules.nativeimage.api.debug.NIFrame;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;
import org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer;
import org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer.DisplayedFrame;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFrame;

import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.text.Line;
import org.openide.util.Pair;

public final class CPPFrame implements DVFrame {

    private static final Logger LOGGER = Logger.getLogger(CPPFrame.class.getName());

    private final CPPThread thread;
    private final DisplayedFrame displayedFrame;
    private final NIFrame niFrame;
    public final int level;

    private volatile Map<String, NIVariable> variables;

    private CPPFrame(CPPThread thread, DisplayedFrame displayedFrame, NIFrame niFrame) {
        Objects.requireNonNull(thread);
        Objects.requireNonNull(displayedFrame);
        Objects.requireNonNull(niFrame);
        this.thread = thread;
        this.displayedFrame = displayedFrame;
        this.niFrame = niFrame;
        this.level = niFrame.getLevel();
    }

    static CPPFrame create(CPPThread thread, MITList frame) {
        NIFrame niFrame = new NIFrameImpl(thread.getId(), frame);
        FrameDisplayer frameDisplayer = thread.getDebugger().getContextProvider().lookupFirst(null, FrameDisplayer.class);
        DisplayedFrame displayedFrame = frameDisplayer != null ? frameDisplayer.displayed(niFrame) : createDisplayedFrame(niFrame);
        if (displayedFrame == null) {
            return null; // Not to be displayed
        }
        return new CPPFrame(thread, displayedFrame, niFrame);
    }

    NIFrame getFrame() {
        return niFrame;
    }

    @Override
    public String getName() {
        return displayedFrame.getDisplayName();
    }

    public String getDescription() {
        return displayedFrame.getDescription();
    }

    @CheckForNull
    public Line location() {
        URI sourceURI = displayedFrame.getSourceURI();
        if (sourceURI == null) {
            return null;
        }
        FileObject file;
        try {
            file = URLMapper.findFileObject(sourceURI.toURL());
        } catch (MalformedURLException ex) {
            return null;
        }
        if (file == null) {
            return null;
        }
        LineCookie lc = file.getLookup().lookup(LineCookie.class);
        return lc.getLineSet().getOriginal(displayedFrame.getLine() - 1);
    }

    @Override
    public CPPThread getThread() {
        return thread;
    }

    @Override
    public void makeCurrent() {
        thread.getDebugger().setCurrentStackFrame(this);
    }

    @Override
    public URI getSourceURI() {
        return displayedFrame.getSourceURI();
    }

    @Override
    public int getLine() {
        return displayedFrame.getLine();
    }

    @Override
    public int getColumn() {
        return -1;
    }

    public Map<String, NIVariable> getVariables() {
        Map<String, NIVariable> vars = variables;
        if (vars == null) {
            synchronized (this) {
                vars = variables;
                if (vars == null) {
                    variables = vars = retrieveVariables(this, null);
                }
            }
        }
        return vars;
    }

    static Map<String, NIVariable> retrieveVariables(CPPFrame frame, CPPVariable parentVar) {
        MIRecord record;
        try {
            if (parentVar == null) {
                record = frame.thread.getDebugger().sendAndGet("-stack-list-variables --thread " + frame.thread.getId() + " --frame " + frame.level + " --no-frame-filters 2");
            } else {
                // from to
                record = frame.thread.getDebugger().sendAndGet("-var-list-children --thread " + frame.thread.getId() + " --frame " + frame.level + " --all-values " + "\"" + parentVar.getUniqueName() + "\"");
            }
        } catch (InterruptedException ex) {
            return Collections.emptyMap();
        }
        if (record.isError()) {
            return Collections.singletonMap(record.error(), null);
        }
        MITList results = record.results();
        if (results.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, NIVariable> map = new LinkedHashMap<>(results.size());
        MIValue children = results.valueOf("children");
        if (children != null) {
            for (MITListItem item : children.asList()) {
                MITList child = ((MIResult) item).value().asTList();
                String uniqueName = child.getConstValue("name");
                String name = child.getConstValue("exp");
                int numChildren = Integer.parseInt(child.getConstValue("numchild"));
                String type = child.getConstValue("type");
                MIValue value = child.valueOf("value");
                map.put(name, new CPPVariable(frame, parentVar, uniqueName, name, type, value, numChildren));
            }
        } else {
            MIValue resultValue = results.valueOf("variables");
            if (resultValue == null) {
                return Collections.emptyMap();
            } else if (resultValue.isConst()) {
                return Collections.singletonMap(((MIConst) resultValue).value(), null);
            }
            results = (MITList) resultValue;
            LOGGER.log(Level.FINE, "retrieveVariables: have {0} variables:", results.size());
            for (MITListItem item : results) {
                MITList varList = (MITList) item;
                String name = varList.getConstValue("name");
                String type = varList.getConstValue("type");
                MIValue value = varList.valueOf("value");
                Pair<String, Integer> uniqueVar = createVariable(frame, name);
                String uniqueName = uniqueVar.first();
                if (uniqueName != null) {
                    int numChildren = uniqueVar.second();
                    LOGGER.log(Level.FINE, "  {0} = ({1}) {2} ; [{3}]", new Object[]{name, type, value, numChildren});
                    map.put(name, new CPPVariable(frame, parentVar, uniqueName, name, type, value, numChildren));
                }
            }
        }
        return map;
    }

    private static Pair<String, Integer> createVariable(CPPFrame frame, String variableName) {
        String uniqueName = null;
        int numChildren = 0;
        MIRecord record;
        try {
            record = frame.thread.getDebugger().sendAndGet("-var-create --thread " + frame.thread.getId() + " --frame " + frame.level + " - " + "*" + " " + variableName);
            if (!record.isError() && !record.isEmpty()) {
                String name = record.results().getConstValue("name");
                if (!name.isEmpty()) {
                    uniqueName = name;
                    String numchild = record.results().getConstValue("numchild");
                    numChildren = Integer.parseInt(numchild);
                }
            }
        } catch (InterruptedException ex) {
        }
        return Pair.of(uniqueName, numChildren);
    }

    private static int retrieveNumChildren(CPPFrame frame, String variableName) {
        int numChildren = 0;
        MIRecord record;
        try {
            record = frame.thread.getDebugger().sendAndGet("-var-info-num-children --thread " + frame.thread.getId() + " --frame " + frame.level + " " + variableName);
            if (!record.isError() && !record.isEmpty()) {
                String numchild = record.results().getConstValue("numchild");
                if (!numchild.isEmpty()) {
                    numChildren = Integer.parseInt(numchild);
                }
            }
        } catch (InterruptedException ex) {
        }
        return numChildren;
    }

    private static final String MI_ERROR = "MI parse error: ";

    public CompletableFuture<NIVariable> evaluateAsync(String expression) {
        return evaluateAsync(expression, null);
    }

    public CompletableFuture<NIVariable> evaluateAsync(String expression, String resultName) {
        String resultVarName = (resultName != null) ? resultName : expression;
        CompletableFuture<NIVariable> result = new CompletableFuture<>();
        NIVariable value = getVariables().get(expression);
        if (value != null) {
            result.complete(value);
            return result;
        }
        thread.getDebugger().send(new Command("-var-create --thread " + thread.getId() + " --frame " + level + " - * \"" + expression + "\"") {
            @Override
            protected void onDone(MIRecord record) {
                MITList results = record.results();
                String varName = results.valueOf("name").asConst().value();
                MIValue typeValue = results.valueOf("type");
                String type = typeValue != null ? typeValue.asConst().value() : null;
                MIValue resultValue = results.valueOf("value");
                int numChildren;
                MIValue numchildValue = results.valueOf("numchild");
                if (numchildValue != null) {
                    numChildren = Integer.parseInt(numchildValue.asConst().value());
                } else {
                    numChildren = retrieveNumChildren(CPPFrame.this, varName);
                }
                result.complete(new CPPVariable(CPPFrame.this, null, varName, resultVarName, type, resultValue, numChildren));
                //thread.getDebugger().send(new Command("-var-delete " + varName));
            }
            @Override
            protected void onError(MIRecord record) {
                String error = record.error();
                if (error.startsWith(MI_ERROR)) {
                    error = error.substring(MI_ERROR.length());
                    result.completeExceptionally(new EvaluateException(error));
                }
            }
        });
        return result;
    }

    private static DisplayedFrame createDisplayedFrame(NIFrame frame) {
        return DisplayedFrame.newBuilder(getDisplayName(frame))
                .description(getDescription(frame))
                .line(frame.getLine())
                .sourceURISupplier(() -> getSourceURI(frame))
                .build();
    }

    private static String getDisplayName(NIFrame frame) {
        StringBuilder builder = new StringBuilder(frame.getFunctionName());
        String shortName = frame.getShortFileName();
        if (shortName != null) {
            builder.append("; ");
            builder.append(shortName);
        }
        int line = frame.getLine();
        if (line > 0) {
            builder.append(':');
            builder.append(line);
        }
        return builder.toString();
    }

    private static String getDescription(NIFrame frame) {
        StringBuilder builder = new StringBuilder(frame.getFunctionName());
        String fullName = frame.getFullFileName();
        if (fullName != null) {
            builder.append("; ");
            builder.append(fullName);
        }
        int line = frame.getLine();
        if (line > 0) {
            builder.append(':');
            builder.append(line);
        }
        return builder.toString();
    }

    private static URI getSourceURI(NIFrame frame) {
        String fullFileName = frame.getFullFileName();
        if (fullFileName != null && !fullFileName.isEmpty()) {
            FileObject file = FileUtil.toFileObject(FileUtil.normalizeFile(new File(fullFileName)));
            if (file != null) {
                return file.toURI();
            }
        }
        return null;
    }

    private static final class NIFrameImpl implements NIFrame {

        private final String threadId;
        private final int level;
        private final String address;
        private final String shortFileName;
        private final String fullFileName;
        private final String functionName;
        private final int line;

        NIFrameImpl(String threadId, MITList frame) {
            this.threadId = threadId;
            this.address = frame.getConstValue("addr");
            this.shortFileName = frame.valueOf("file") != null ? frame.valueOf("file").asConst().value() : null;
            this.functionName = frame.valueOf("func").asConst().value();
            this.fullFileName = frame.valueOf("fullname") != null ? frame.valueOf("fullname").asConst().value() : null;
            this.line = frame.valueOf("line") != null ? Integer.parseInt(frame.valueOf("line").asConst().value()) : -1;
            if (frame.valueOf("level") != null) {
                this.level = Integer.parseInt(frame.valueOf("level").asConst().value());
            } else {
                this.level = 0;
            }
        }

        @Override
        public String getAddress() {
            return address;
        }

        @Override
        public String getShortFileName() {
            return shortFileName;
        }

        @Override
        public String getFullFileName() {
            return fullFileName;
        }

        @Override
        public String getFunctionName() {
            return functionName;
        }

        @Override
        public int getLine() {
            return line;
        }

        @Override
        public String getThreadId() {
            return threadId;
        }

        @Override
        public int getLevel() {
            return level;
        }
    }

}
