/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep.editor.refactoring;

import org.netbeans.modules.css.prep.editor.refactoring.RefactoringElement;
import org.netbeans.modules.css.prep.editor.refactoring.CPWhereUsedQueryPlugin;
import org.netbeans.modules.css.prep.editor.refactoring.RefactoringElementContext;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.prep.editor.ProjectTestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class CPWhereUsedQueryPluginTest extends ProjectTestBase {

    public CPWhereUsedQueryPluginTest(String name) {
        super(name, "testProject");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        setScssSource();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown(); 
        setPlainSource();
    }
    
    /*
     * Tests FU on global variable declaration and usage in two linked files.
     */
    public void testGlobalVariables() throws ParseException, BadLocationException, IOException {
        FileObject libfile = getTestFile(getSourcesFolderName() + "/lib1.scss");
        FileObject clientfile = getTestFile(getSourcesFolderName() + "/client1.scss");
        
        CssParserResult result = parse(libfile);
        TestUtil.dumpResult(result);
        //line:1, offset:5 - global variable declaration $glob|al_var_lib1: 1;
        RefactoringElementContext context = new RefactoringElementContext(result, 5);
        Collection<RefactoringElement> vars = CPWhereUsedQueryPlugin.findVariables(context);
        assertNotNull(vars);
//        for(RefactoringElement e : vars) {
//            System.out.println(e.toString());
//        }
        assertEquals(3, vars.size());
        
        Iterator<RefactoringElement> iterator = vars.iterator();
        
        //the declaration itself
        RefactoringElement re = iterator.next();
        assertEquals("$global_var_lib1", re.getName());
        assertEquals(libfile, re.getFile());
        OffsetRange range = re.getRange();
        assertEquals(0, range.getStart());
        assertEquals(16, range.getEnd());
                
        //the usage in mixin
        re = iterator.next();
        assertEquals("$global_var_lib1", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(144, range.getStart());
        assertEquals(160, range.getEnd());
                
        //the usage in client1.scss
        re = iterator.next();
        assertEquals("$global_var_lib1", re.getName());
        assertEquals(clientfile, re.getFile());
        range = re.getRange();
        assertEquals(35, range.getStart());
        assertEquals(51, range.getEnd());
        
        //now try to find usages when caret in on the usage
        context = new RefactoringElementContext(result, 150);
        vars = CPWhereUsedQueryPlugin.findVariables(context);
        assertNotNull(vars);
        assertEquals(3, vars.size());
        
        iterator = vars.iterator();
        
        //the declaration
        re = iterator.next();
        assertEquals("$global_var_lib1", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(0, range.getStart());
        assertEquals(16, range.getEnd());
        
        //the usage in mixin
        re = iterator.next();
        assertEquals("$global_var_lib1", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(144, range.getStart());
        assertEquals(160, range.getEnd());
        
        //the usage in client file
        re = iterator.next();
        assertEquals("$global_var_lib1", re.getName());
        assertEquals(clientfile, re.getFile());
        range = re.getRange();
        assertEquals(35, range.getStart());
        assertEquals(51, range.getEnd());
        
    }
    
    /*
     * Tests FU on variable declared as mixin argument.
     */
     public void testLocalFUOnMixinArgVariable() throws ParseException, BadLocationException, IOException {
        FileObject libfile = getTestFile(getSourcesFolderName() + "/lib1.scss");
        
        CssParserResult result = parse(libfile);
        
        //pos 9:22 - in the $arg: @mixin mixin2_lib1($arg) 
        RefactoringElementContext context = new RefactoringElementContext(result, 88);
        Collection<RefactoringElement> vars = CPWhereUsedQueryPlugin.findVariables(context);
        assertNotNull(vars);
//        for(RefactoringElement e : vars) {
//            System.out.println(e.toString());
//        }
        assertEquals(2, vars.size());
        
        Iterator<RefactoringElement> iterator = vars.iterator();
        
        //the declaration itself
        RefactoringElement re = iterator.next();
        assertEquals("$arg", re.getName());
        assertEquals(libfile, re.getFile());
        OffsetRange range = re.getRange();
        assertEquals(86, range.getStart());
        assertEquals(90, range.getEnd());
                
        //the usage in mixin
        re = iterator.next();
        assertEquals("$arg", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(105, range.getStart());
        assertEquals(109, range.getEnd());
        
        //and vice versa
        context = new RefactoringElementContext(result, 107);
        vars = CPWhereUsedQueryPlugin.findVariables(context);
        assertNotNull(vars);
//        for(RefactoringElement e : vars) {
//            System.out.println(e.toString());
//        }
        assertEquals(2, vars.size());
        
        iterator = vars.iterator();
        
        //the declaration itself
        re = iterator.next();
        assertEquals("$arg", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(86, range.getStart());
        assertEquals(90, range.getEnd());
                
        //the usage in mixin
        re = iterator.next();
        assertEquals("$arg", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(105, range.getStart());
        assertEquals(109, range.getEnd());
        
     }
    
      /*
     * Tests FU on mixin calls in one file.
     */
     public void testFUOfMixinsCalls() throws ParseException, BadLocationException, IOException {
        FileObject libfile = getTestFile(getSourcesFolderName() + "/test1.scss");
        
        CssParserResult result = parse(libfile);
        
        //pos 7:24 - at the "incr" in @include incr(20px);
        RefactoringElementContext context = new RefactoringElementContext(result, 143);
        Collection<RefactoringElement> mixins = CPWhereUsedQueryPlugin.findMixins(context);
        assertNotNull(mixins);
//        for(RefactoringElement e : vars) {
//            System.out.println(e.toString());
//        }
        assertEquals(2, mixins.size());
        
        Iterator<RefactoringElement> iterator = mixins.iterator();
        
        //the declaration itself
        RefactoringElement re = iterator.next();
        assertEquals("incr", re.getName());
        assertEquals(libfile, re.getFile());
        OffsetRange range = re.getRange();
        assertEquals(56, range.getStart());
        assertEquals(60, range.getEnd());
                
        //the usage of the mixin
        re = iterator.next();
        assertEquals("incr", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(141, range.getStart());
        assertEquals(145, range.getEnd());
        
        //and vice versa
        context = new RefactoringElementContext(result, 57);
        mixins = CPWhereUsedQueryPlugin.findMixins(context);
        assertNotNull(mixins);
//        for(RefactoringElement e : vars) {
//            System.out.println(e.toString());
//        }
        assertEquals(2, mixins.size());
        
        iterator = mixins.iterator();
        
        //the declaration itself
        re = iterator.next();
        assertEquals("incr", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(56, range.getStart());
        assertEquals(60, range.getEnd());
                
        //the usage in mixin
        re = iterator.next();
        assertEquals("incr", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(141, range.getStart());
        assertEquals(145, range.getEnd());
        
     }
     
     /*
     * Tests FU on local variable declared in sass @for statement
     */
     public void testFUOfVarDeclaredInForBlock() throws ParseException, BadLocationException, IOException {
        FileObject libfile = getTestFile(getSourcesFolderName() + "/test2.scss");
        
        CssParserResult result = parse(libfile);
        
        //pos 1:8 - in the $prom in: @for $prom from 1 to 10 {
        RefactoringElementContext context = new RefactoringElementContext(result, 8);
        Collection<RefactoringElement> vars = CPWhereUsedQueryPlugin.findVariables(context);
        assertNotNull(vars);
        assertEquals(2, vars.size());
        
        Iterator<RefactoringElement> iterator = vars.iterator();
        
        //the declaration itself
        RefactoringElement re = iterator.next();
        assertEquals("$prom", re.getName());
        assertEquals(libfile, re.getFile());
        OffsetRange range = re.getRange();
        assertEquals(5, range.getStart());
        assertEquals(10, range.getEnd());
                
        //the usage of the mixin
        re = iterator.next();
        assertEquals("$prom", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(37, range.getStart());
        assertEquals(42, range.getEnd());
        
        //and vice versa
        context = new RefactoringElementContext(result, 40);
        vars = CPWhereUsedQueryPlugin.findVariables(context);
        assertNotNull(vars);
        assertEquals(2, vars.size());
        
        iterator = vars.iterator();
        
        //the declaration itself
        re = iterator.next();
        assertEquals("$prom", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(5, range.getStart());
        assertEquals(10, range.getEnd());
                
        //the usage of the mixin
        re = iterator.next();
        assertEquals("$prom", re.getName());
        assertEquals(libfile, re.getFile());
        range = re.getRange();
        assertEquals(37, range.getStart());
        assertEquals(42, range.getEnd());
     }
    
     
    /**
     * Parse using standard mechanism in contrary to {@link TestUtil#parse(org.openide.filesystems.FileObject)}.
     */
    private CssParserResult parse(FileObject file) throws ParseException {
        Source source = Source.create(file);
        final AtomicReference<CssParserResult> resultRef = new AtomicReference<>();
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                resultRef.set((CssParserResult)WebUtils.getResultIterator(resultIterator, "text/css").getParserResult());
            }
        });
        
        return resultRef.get();
    }
    
    
}