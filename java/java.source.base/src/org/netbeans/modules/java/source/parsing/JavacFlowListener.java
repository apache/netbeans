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

package org.netbeans.modules.java.source.parsing;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTaskImpl;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public class JavacFlowListener {
        
    protected static final Context.Key<JavacFlowListener> flowListenerKey =
        new Context.Key<JavacFlowListener>();
    
    public static JavacFlowListener instance (final Context context) {
        final JavacFlowListener flowListener = context.get(flowListenerKey);
        return flowListener != null ? flowListener : null;
    }

    static void preRegister(final Context context, JavacTaskImpl jti) {
        context.put(flowListenerKey, new JavacFlowListener(context, jti));
    }

    private final Set<URI> flowCompleted = new HashSet<URI>();

    private JavacFlowListener(Context context, JavacTaskImpl jti) {
        //TODO: should probably use MultiTaskListener when awailable:
        //XXX: only one listener can be set through setTaskListener!
        jti.setTaskListener(new TaskListenerImpl());
    }

    public final boolean hasFlowCompleted (final FileObject fo) {
        if (fo == null) {
            return false;
        }
        else {
            try {
                return this.flowCompleted.contains(fo.toURL().toURI());
            } catch (Exception e) {
                return false;
            }
        }
    }

    private class TaskListenerImpl implements TaskListener {
        public TaskListenerImpl() {
        }
        @Override
        public void started(TaskEvent e) {
        }

        @Override
        public void finished(TaskEvent e) {
            if (e.getKind() == Kind.ANALYZE) {
                JCCompilationUnit toplevel = (JCCompilationUnit) e.getCompilationUnit();
                if (toplevel != null && toplevel.sourcefile != null) {
                    flowCompleted.add(toplevel.sourcefile.toUri());
                }
            }
        }
    }
}
