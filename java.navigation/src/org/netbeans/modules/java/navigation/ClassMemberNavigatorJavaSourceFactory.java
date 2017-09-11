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
package org.netbeans.modules.java.navigation;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.LookupBasedJavaSourceTaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda, Petr Hrebejk
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
public final class ClassMemberNavigatorJavaSourceFactory extends LookupBasedJavaSourceTaskFactory {
            
    private ClassMemberPanelUI ui;
    private static final CancellableTask<CompilationInfo> EMPTY_TASK = new CancellableTask<CompilationInfo>() {

        public void cancel() {}

        public void run(CompilationInfo parameter) throws Exception {}
    };
    
    static ClassMemberNavigatorJavaSourceFactory getInstance() {
        for(JavaSourceTaskFactory t : Lookup.getDefault().lookupAll(JavaSourceTaskFactory.class)) {
            if (t instanceof ClassMemberNavigatorJavaSourceFactory) {
                return (ClassMemberNavigatorJavaSourceFactory) t;
            }
        }
        return null;
    }
    
    public ClassMemberNavigatorJavaSourceFactory() {        
        super(
            Phase.ELEMENTS_RESOLVED,
            Priority.NORMAL,
            TaskIndexingMode.ALLOWED_DURING_SCAN,
            "text/x-java",
            "application/x-class-file");
    }

    public synchronized CancellableTask<CompilationInfo> createTask(FileObject file) {
        // System.out.println("CREATE TASK FOR " + file.getNameExt() );
        if ( ui == null) {
            return EMPTY_TASK;
        }
        else {
            ui.showWaitNode();
            return ui.getTask();
        }
    }

    public List<FileObject> getFileObjects() {
        List<FileObject> result = super.getFileObjects();

        if (result.size() == 1)
            return result;

        // System.out.println("Nothing to show");
        return Collections.emptyList();
    }

    public synchronized void setLookup(Lookup l, ClassMemberPanelUI ui) {
        this.ui = ui;
        super.setLookup(l);
    }            
}
