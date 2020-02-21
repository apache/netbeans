/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
