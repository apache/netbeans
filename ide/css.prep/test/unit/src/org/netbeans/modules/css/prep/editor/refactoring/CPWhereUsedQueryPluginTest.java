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
package org.netbeans.modules.css.prep.editor.refactoring;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.ProjectTestBase;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
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