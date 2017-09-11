/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
