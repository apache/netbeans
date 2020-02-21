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

package org.netbeans.modules.cnd.spi.toolchain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.windows.IOPosition;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;

/**
 *
 */
public abstract class ErrorParserProvider {
    // id of error parser provider which handles any output
    public static final String UNIVERSAL_PROVIDER_ID = "universal"; //NOI18N
    public static final Result NO_RESULT = new NoResult();
    public static final Result REMOVE_LINE = new RemoveLine();

    private static final DefaultErrorParserProvider DEFAULT = new DefaultErrorParserProvider();
    
    public static ErrorParserProvider getDefault() {
	return DEFAULT;
    }

    public static List<ErrorParser> getUniversalErorParsers(Project project, ExecutionEnvironment execEnv, FileObject relativeTo) {
        return DEFAULT.getUniversalErorParsersImpl(project, execEnv, relativeTo);
    }

    public abstract ErrorParser getErorParser(Project project,CompilerFlavor flavor, ExecutionEnvironment execEnv, FileObject relativeTo);
    
    public abstract String getID();

    public final static class OutputListenerRegistry {
        private final Map<FileObject,Set<OutputListener>> storage = new HashMap<FileObject,Set<OutputListener>>();
        private final Project project;
        private final InputOutput io;
        
        private boolean scrollingSupported = false;
        
        protected OutputListenerRegistry(Project project, InputOutput io) {
            this.project = project;
            this.io = io;
            
            scrollingSupported = IOPosition.isSupported(io);
        }

        public OutputListener register(FileObject file, int line, boolean isError, String description) {
            IOPosition.Position ioPos = IOPosition.currentPosition(io); // null is ok

            OutputListener res = OutputListenerProvider.getInstance().get(this, file, line, isError, description, ioPos);
            synchronized(storage) {
                Set<OutputListener> list = storage.get(file);
                if (list == null) {
                    list = new HashSet<OutputListener>();
                    storage.put(file, list);
                }
                list.add(res);
            }
            return res;
        }

        Project getProject() {
            return project;
        }

        public InputOutput getIO() {
            return io;
        }

        public Set<OutputListener> getFileListeners(FileObject file){
            Set<OutputListener> res = null;
            if (file.isData()) {
                synchronized(storage) {
                    res = storage.get(file);
                    if (res != null) {
                        res = new HashSet<OutputListener>(res);
                    }
                }
            } else {
                synchronized(storage) {
                    for(Map.Entry<FileObject,Set<OutputListener>> entry : storage.entrySet()) {
                        if (FileUtil.isParentOf(file, entry.getKey())) {
                            if (res == null) {
                                res = new HashSet<OutputListener>();
                            }
                            res.addAll(entry.getValue());
                        }
                    }
                }
            }
            return res;
        }
    }
    
    public interface ErrorParser {
	Result handleLine(String line);
        void setOutputListenerRegistry(OutputListenerRegistry regestry);
    }
    public interface Result {
        public abstract boolean result();
        public abstract List<ConvertedLine> converted();
    }

    public static final class Results implements Result {
        private final List<ConvertedLine> result = new ArrayList<ConvertedLine>(1);
        public Results(){
        }
        public Results(String line, OutputListener listener){
            result.add(ConvertedLine.forText(line, listener));
        }
        public void add(String line, OutputListener listener) {
            result.add(ConvertedLine.forText(line, listener));
        }
        @Override
        public boolean result() {
            return true;
        }
        @Override
        public List<ConvertedLine> converted() {
            return result;
        }
    }

    private static final class NoResult implements Result {
        @Override
        public boolean result() {
            return false;
        }
        @Override
        public List<ConvertedLine> converted() {
            return Collections.<ConvertedLine>emptyList();
        }
    }

    private static final class RemoveLine implements Result {
        @Override
        public boolean result() {
            return true;
        }
        @Override
        public List<ConvertedLine> converted() {
            return Collections.<ConvertedLine>emptyList();
        }
    }

    private static final class DefaultErrorParserProvider extends ErrorParserProvider {
        private final Lookup.Result<ErrorParserProvider> res;
        DefaultErrorParserProvider() {
            res = Lookup.getDefault().lookupResult(ErrorParserProvider.class);
        }

        private ErrorParserProvider getService(String id){
	    for (ErrorParserProvider service : res.allInstances()) {
		if (service.getID().equals(id)) {
		    return service;
		}
	    }
            return null;
        }

	@Override
	public ErrorParser getErorParser(Project project, CompilerFlavor flavor, ExecutionEnvironment execEnv, FileObject relativeTo) {
	    ErrorParserProvider provider = getService(flavor.getToolchainDescriptor().getScanner().getID());
	    if (provider != null) {
		return provider.getErorParser(project, flavor, execEnv, relativeTo);
	    }
	    return null;
	}

	@Override
	public String getID() {
	    throw new UnsupportedOperationException();
	}

        public List<ErrorParser> getUniversalErorParsersImpl(Project project, ExecutionEnvironment execEnv, FileObject relativeTo) {
            List<ErrorParser> parsers = new ArrayList<ErrorParser>();
            for (ErrorParserProvider service : res.allInstances()) {
                if (ErrorParserProvider.UNIVERSAL_PROVIDER_ID.equals(service.getID())) {
                    ErrorParser erorParser = service.getErorParser(project, null, execEnv, relativeTo);
                    if (erorParser != null) {
                        parsers.add(erorParser);
                    }
                }
            }
            return parsers;
        }
    }
}
