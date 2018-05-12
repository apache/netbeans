/**
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
package org.netbeans.modules.java.source.remote.api;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.java.source.remoteapi.SourceLevelQueryImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**TODO: relies on get/putCachedValue for correctness (error fixes, etc.)
 *
 * @author lahvac
 */
public class Parser {

    private static int lastConfig;
    private static JavaSource source;
    private static Document lastDoc;

    public static <T> T runTask(Config config, ParserTask<T, CompilationInfo> task) throws IOException {
        return runControllerTask(config, cc -> {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            return task.run(cc);
        });
    }

    public static <T> T runControllerTask(Config config, ParserTask<T, CompilationController> task) throws IOException {
        JavaSource source;
        
        if (lastConfig == config.id && Parser.source != null) {
            source = Parser.source;
        } else {
            FileObject root = FileUtil.createMemoryFileSystem().getRoot();
            FileObject file = FileUtil.createData(root, config.fileName);

            file.setAttribute(SourceLevelQueryImpl.KEY_SOURCE_LEVEL, config.sourceLevel);

            try (OutputStream out = file.getOutputStream();
                 Writer w = new OutputStreamWriter(out)) {
                w.append(config.fileContent);
            }

            ClasspathInfo cpInfo = SourceUtils.deserializeClasspathInfo(config.cpInfo);

            lastConfig = config.id;
            Parser.source = source = JavaSource.create(cpInfo, file);
        }

        Object[] result = new Object[1];
        source.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                result[0] = task.run(cc);
            }
        }, true);
        
        return (T) result[0];
    }
    
    public interface ParserTask<T, CI extends CompilationInfo> {
        public T run(CI info) throws Exception;
    }

    public static final class Config {
        private static int nextId = 0; //XXX: synchronization

        public static Config create(CompilationInfo info) {
            Object conf = info.getCachedValue(Config.class);
            if (conf == null) {
                conf = new Config(nextId++, info.getFileObject().getNameExt(), info.getText(), info.getClasspathInfo(), SourceLevelQuery.getSourceLevel(info.getFileObject()));
                info.putCachedValue(Config.class, conf, CompilationInfo.CacheClearPolicy.ON_CHANGE);
            }
            return (Config) conf;
        }

        public static Config create(FileObject file) {
            //caching...
            try {
                return new Config(nextId++, file.getNameExt(), file.asText(), ClasspathInfo.create(file), SourceLevelQuery.getSourceLevel(file));
            } catch (IOException ex) {
                throw new IllegalStateException(ex); //XXX: error handling
            }
        }

        private int id;
        private String fileName;
        private String fileContent;
        private Map<String, Object> cpInfo;
        private String sourceLevel;

        public Config(int id, String fileName, String fileContent,
                      ClasspathInfo cpInfo, String sourceLevel) {
            this.id = id;
            this.fileName = fileName;
            this.fileContent = fileContent;
            this.cpInfo = SourceUtils.serializeClasspathInfo(cpInfo);
            this.sourceLevel = sourceLevel;
        }

    }
    
}
