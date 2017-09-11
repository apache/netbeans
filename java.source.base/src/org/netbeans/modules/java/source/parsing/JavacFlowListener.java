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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
class JavacFlowListener {
        
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

    final boolean hasFlowCompleted (final FileObject fo) {
        if (fo == null) {
            return false;
        }
        else {
            try {
                return this.flowCompleted.contains(fo.getURL().toURI());
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
