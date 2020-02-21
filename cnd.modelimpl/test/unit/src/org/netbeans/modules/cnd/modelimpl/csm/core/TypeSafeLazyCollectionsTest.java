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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 * 
 */
public class TypeSafeLazyCollectionsTest extends ModelImplBaseTestCase {

    public TypeSafeLazyCollectionsTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {

        File workDir = getWorkDir();
        File sourceFile = new File(workDir, "test1.cc");

        // 
        // create a file that uses macros;
        // parse this file with these macros set to class start/end
        //
        writeFile(sourceFile, "START\nvoid foo();\nEND\n");
        
	final TraceModelBase traceModel = new  TraceModelBase(true);
	
        String className = "MyClass";
	traceModel.processArguments(sourceFile.getAbsolutePath(), "-DSTART=class " + className + "{", "-DEND=};");
	ModelImpl model = traceModel.getModel();
	//ModelSupport.instance().setModel(model);
	final CsmProject project = traceModel.getProject();

        project.waitParse();
        
        //new CsmTracer(System.err).dumpModel(project);
                
        CsmClass cls = (CsmClass) findDeclaration(className, project);
        assertNotNull(className + " can not be found", cls);
        
        //
        // Remember the members collection
        //
        Collection<CsmMember> members = cls.getMembers();
        
        
        //
        // Parse it once more with macros unset
        //
        NativeProject nativeProject = (NativeProject)project.getPlatformProject();
        List<String> macros = new ArrayList<>();
        macros.add("START=");
        macros.add("END=");
        NativeProjectProvider.setUserMacros(nativeProject, macros /*Collections.<String>emptyList()*/);
        ((FileImpl) cls.getContainingFile()).markReparseNeeded(true);
        cls.getContainingFile().scheduleParsing(true);
        
        project.waitParse();
        
        //new CsmTracer(System.err).dumpModel(project);
        
        //
        // Make sure no class cast exception happens
        //
        CharSequence tmp;
        try {
            for( CsmMember member : members ) {
                tmp = member.getName();
            }
        } catch( Exception e) {
            DiagnosticExceptoins.register(e);
        }
        assertNoExceptions();
    }
   
}
