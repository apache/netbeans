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
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 *
 * @author lahvac
 */
public class Utilities {
    
    public static JavacTaskImpl createJavac(JavaFileManager fm, JavaFileObject... sources) {
        final String version = System.getProperty("java.vm.specification.version"); //NOI18N
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        assert tool != null;
        Context context = new Context();
        //need to preregister the Messages here, because the getTask below requires Log instance:
        Log.preRegister(context, DEV_NULL);
        JavacTaskImpl task = (JavacTaskImpl)JavacTool.create().getTask(null, 
                fm,
                null, Arrays.asList("-source", version, "-target", version, "-Xjcov", "-XDshouldStopPolicy=GENERATE"), null, Arrays.asList(sources),
                context);
        NBParserFactory.preRegister(context);
        NBTreeMaker.preRegister(context);
        NBEnter.preRegister(context);
        return task;
    }
    
    public static JavaFileObject fileObjectFor(String code) {
        return new MyFileObject(code);
    }
    
    private static class MyFileObject extends SimpleJavaFileObject {
        private String text;
        public MyFileObject(String text) {
            super(URI.create("myfo:/Test.java"), JavaFileObject.Kind.SOURCE);
            this.text = text;
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return text;
        }
    }

    public static final PrintWriter DEV_NULL = new PrintWriter(new NullWriter(), false);

    private static class NullWriter extends Writer {

        public void write(char[] cbuf, int off, int len) throws IOException {
        }

        public void flush() throws IOException {
        }

        public void close() throws IOException {
        }
    }
}
