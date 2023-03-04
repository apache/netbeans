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
package org.netbeans.lib.nbjshell;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SourceCodeAnalysis.SnippetWrapper;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControl.ExecutionControlException;
import jdk.jshell.spi.ExecutionControl.InternalException;
/**
 *
 * @author sdedic
 */
public class JShellAccessor {
    /**
     * Resets the compile classpath: set it to the desired strng.
     * @param instance
     * @param classpath 
     */
    public static void resetCompileClasspath(JShell instance, String classpath) throws ExecutionControlException {
        try {
            Field factory = JShell.class.getDeclaredField("taskFactory");       // NOI18N
            factory.setAccessible(true);
            Class taskFactoryClazz = Class.forName("jdk.jshell.TaskFactory");       // NOI18N
            Field f = taskFactoryClazz.getDeclaredField("classpath");       // NOI18N
            f.setAccessible(true);
            Object factoryInstance = factory.get(instance);
            f.set(factoryInstance, "");
            
            Method m = instance.getClass().getDeclaredMethod("executionControl");
            m.setAccessible(true);
            ExecutionControl ctrl = (ExecutionControl)m.invoke(instance);
            RemoteJShellService rjs = (RemoteJShellService)ctrl;
            rjs.suppressClasspathChanges(true);
            try {
                instance.addToClasspath(classpath);
            } finally {
                rjs.suppressClasspathChanges(false);
            }
        } catch (InvocationTargetException ex) {
            Throwable t = ex.getCause();
            if (t instanceof ExecutionControlException) {
                throw (ExecutionControlException)t;
            }
            InternalException x = new InternalException("Error during setting classpath");
            x.initCause(t);
            throw x;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | 
                NoSuchFieldException | ClassNotFoundException ex) {
            Logger.getLogger(JShellAccessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Converts position in the original source into position in the wrapped text
     * @param snip snippet which contains the source
     * @param snippetPos index in the original rouce
     * @return index in wrapped snippet code
     */
    public static int getWrappedPosition(JShell state, Snippet snip, int snippetPos) {
        return state.sourceCodeAnalysis().wrapper(snip).sourceToWrappedPosition(snippetPos);
    }
    
    /**
     * Returns text corresponding to a snippet
     * @param state
     * @param s
     * @return 
     */
    public static SnippetWrapping snippetWrap(JShell state, Snippet s) {
        SnippetWrapper wrp = state.sourceCodeAnalysis().wrapper(s);
        return new WrappedWrapper(s, wrp, state);
    }
    
    /**
     * Generates a wrapping for a text without declaring a new Snippet
     * @param state JShell instance
     * @param input source text
     * @return wrapped source
     */
    public static SnippetWrapping wrapInput(JShell state, String input) {
        if (input.trim().isEmpty()) {
            input = input + ";"; // NOI18N
        }
        List<SnippetWrapper> wraps = state.sourceCodeAnalysis().wrappers(input);
        if (wraps.size() != 1) {
            return null;
        }
        return new WrappedWrapper(null, wraps.get(0), state);
    }
    
    private static class WrappedWrapper implements SnippetWrapping {
        private final Snippet snippet;
        private final SnippetWrapper wrapper;
        private final JShell jshell;

        public WrappedWrapper(Snippet snippet, SnippetWrapper wrapper, JShell jshell) {
            this.snippet = snippet;
            this.wrapper = wrapper;
            this.jshell = jshell;
        }

        @Override
        public Snippet.Kind getSnippetKind() {
            return wrapper.kind();
        }

        @Override
        public Status getStatus() {
            return snippet == null ? Status.NONEXISTENT : jshell.status(snippet); 
        }

        @Override
        public Snippet getSnippet() {
            return snippet;
        }

        @Override
        public String getCode() {
            return wrapper.wrapped();
        }

        @Override
        public String getSource() {
            return wrapper.source();
        }

        @Override
        public int getWrappedPosition(int pos) {
            return wrapper.sourceToWrappedPosition(pos);
        }

        @Override
        public String getClassName() {
            return wrapper.fullClassName();
        }
        
        public String toString() {
            return "Wrapper(snippet = " + (snippet == null ? "none" : snippet.id()) + ", status = " + getStatus() + ")";
        }
    }

    /**
     * Finds dependent snippets. Only persistent snippets are returned.
     * 
     * @param state shell instance
     * @param snip dependency target
     * @return persistent snippets which depend on 'snip'.
     */
    public static Collection<Snippet> getDependents(JShell state, Snippet snip) {
        return state.sourceCodeAnalysis().dependents(snip);
    }
}
