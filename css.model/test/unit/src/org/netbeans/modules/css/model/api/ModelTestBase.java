/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
