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
package org.netbeans.modules.css.model.api;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.ModelAccess;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author marekfukala
 */
public class ModelTestBase extends NbTestCase {

    public ModelTestBase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockServices.setServices(BuiltInDiffProvider.class);

        //disable checking for model access so tests doesn't have to use 
        //the Model.runRead/WriteModel(...) methods
        ModelAccess.checkModelAccess = false;
    }
    
    protected FileObject getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }
    
    //for testing only - leaking model!
    protected StyleSheet getStyleSheet(Model model) {
        final AtomicReference<StyleSheet> ssref = new AtomicReference<>();
        model.runReadTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {
                ssref.set(styleSheet);
            }
        });
        return ssref.get();        
    }
    
    protected StyleSheet createStyleSheet(String source) {
        return getStyleSheet(createModel(source));
    }
    
    protected Model createModel() {
        return new Model();
    }
    
    protected Model createModel(String source) {
        return createModel(TestUtil.parse(source));
    }
    
    protected Model createModel(CssParserResult result) {
        return new Model(result);
    }


    
    
    protected void dumpTree(org.netbeans.modules.css.lib.api.properties.Node node) {
        PrintWriter pw = new PrintWriter(System.out);
        dump(node, 0, pw);
        pw.flush();
    }

    private void dump(org.netbeans.modules.css.lib.api.properties.Node tree, int level, PrintWriter pw) {
        for (int i = 0; i < level; i++) {
            pw.print("    ");
        }
        pw.print(tree.toString());
        pw.println();
        for (org.netbeans.modules.css.lib.api.properties.Node c : tree.children()) {
            dump(c, level + 1, pw);
        }
    }
    
    
    
}
