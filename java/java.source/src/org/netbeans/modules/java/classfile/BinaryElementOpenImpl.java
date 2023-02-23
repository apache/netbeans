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

package org.netbeans.modules.java.classfile;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.java.BinaryElementOpen;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.java.BinaryElementOpen.class)
public class BinaryElementOpenImpl implements BinaryElementOpen {

    @Override
    public boolean open(ClasspathInfo cpInfo, final ElementHandle<? extends Element> toOpen, final AtomicBoolean cancel) {
        boolean[] sourceAttrApplied = { false };
        FileObject source = CodeGenerator.generateCode(cpInfo, toOpen, sourceAttrApplied);
        if (source != null) {
            if (sourceAttrApplied[0]) {
              return open(source, 0);
            }

            final int[] pos = new int[] {-1};

            try {
                JavaSource.create(cpInfo, source).runUserActionTask(new Task<CompilationController>() {
                    @Override public void run(CompilationController parameter) throws Exception {
                        if (cancel.get()) return ;
                        parameter.toPhase(JavaSource.Phase.RESOLVED);

                        Element el = toOpen.resolve(parameter);

                        if (el == null) return ;

                        TreePath p = parameter.getTrees().getPath(el);

                        if (p == null) return ;

                        pos[0] = (int) parameter.getTrees().getSourcePositions().getStartPosition(p.getCompilationUnit(), p.getLeaf());
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (pos[0] != (-1) && !cancel.get()) {
                return open(source, pos[0]);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private boolean open(FileObject source, int pos) {
        return org.netbeans.api.java.source.UiUtils.open(source, pos);
    }

}
