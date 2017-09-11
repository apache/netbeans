/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.debug;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.LookupBasedJavaSourceTaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
public final class TreeNavigatorJavaSourceFactory extends LookupBasedJavaSourceTaskFactory {
    
    private CancellableTask<CompilationInfo> task;
    
    static TreeNavigatorJavaSourceFactory getInstance() {
        for (JavaSourceTaskFactory f :  Lookup.getDefault().lookupAll(JavaSourceTaskFactory.class)) {
            if (f instanceof TreeNavigatorJavaSourceFactory) {
                return (TreeNavigatorJavaSourceFactory) f;
            }
        }
        return null;
    }
    
    public TreeNavigatorJavaSourceFactory() {
        super(Phase.UP_TO_DATE, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    public synchronized CancellableTask<CompilationInfo> createTask(FileObject file) {
        //XXX: should not be necessary to do the wrapper task, but for some reason it is necessary:
        return new WrapperTask(task);
    }

    @Override
    public List<FileObject> getFileObjects() {
        List<FileObject> result = super.getFileObjects();

        if (result.size() == 1)
            return result;

        return Collections.emptyList();
    }

    public FileObject getFile() {
        List<FileObject> result = super.getFileObjects();
        
        if (result.size() == 1)
            return result.get(0);
        
        return null;
    }

    public synchronized void setLookup(Lookup l, CancellableTask<CompilationInfo> task) {
        this.task = task;
        super.setLookup(l);
    }

    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class CaretAwareFactoryImpl extends CaretAwareJavaSourceTaskFactory {

        static CaretAwareFactoryImpl getInstance() {
            return Lookup.getDefault().lookup(CaretAwareFactoryImpl.class);
        }

        private CancellableTask<CompilationInfo> task;

        public CaretAwareFactoryImpl() {
            super(Phase.UP_TO_DATE, Priority.LOW, TaskIndexingMode.ALLOWED_DURING_SCAN);
        }

        @Override
        protected synchronized CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new WrapperTask(task);
        }

        @Override
        public List<FileObject> getFileObjects() {
            List<FileObject> result = super.getFileObjects();

            if (result.size() == 1)
                return result;

            return Collections.emptyList();
        }

        public FileObject getFile() {
            List<FileObject> result = super.getFileObjects();

            if (result.size() == 1)
                return result.get(0);

            return null;
        }

        public synchronized void setTask(CancellableTask<CompilationInfo> task) {
            this.task = task;
            FileObject file = getFile();
            if (file != null) {
                reschedule(file);
            }
        }
    }

    static class WrapperTask implements CancellableTask<CompilationInfo> {
        
        private final CancellableTask<CompilationInfo> delegate;
        
        public WrapperTask(CancellableTask<CompilationInfo> delegate) {
            this.delegate = delegate;
        }

        public void cancel() {
            if (delegate != null)
                delegate.cancel();
        }

        public void run(CompilationInfo parameter) throws Exception {
            if (delegate != null)
                delegate.run(parameter);
        }
        
    }
}
