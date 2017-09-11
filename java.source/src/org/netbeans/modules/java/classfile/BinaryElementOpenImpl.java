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
 * Portions Copyrighted 2007-2010 Sun Microsystems, Inc.
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
        FileObject source = CodeGenerator.generateCode(cpInfo, toOpen);
        if (source != null) {
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
