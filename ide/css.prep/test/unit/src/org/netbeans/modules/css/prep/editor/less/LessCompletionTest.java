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
package org.netbeans.modules.css.prep.editor.less;

import javax.swing.text.Document;
import org.netbeans.modules.css.prep.editor.scss.*;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.css.editor.module.main.CssModuleTestBase;
import org.netbeans.modules.css.prep.editor.model.CPModel;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class LessCompletionTest extends CssModuleTestBase {

    public LessCompletionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CPModel.topLevelSnapshotMimetype = getTopLevelSnapshotMimetype();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        CPModel.topLevelSnapshotMimetype = null;
    }

    @Override
    protected String getTopLevelSnapshotMimetype() {
        return "text/less";
    }

    @Override
    protected String getCompletionItemText(CompletionProposal cp) {
        return cp.getInsertPrefix();
    }

    public void testMixinsCompletion() throws ParseException {
        checkCC(".mixin() {}\n"
                + "div { .| }", arr("mixin"), Match.CONTAINS);

    }

    public void testVarCompletionWithoutPrefix() throws ParseException {
        checkCC("@oops:1;\n"
                + ".mixin(@oops2) {\n"
                + "    color: @|\n"
                + "}", arr("@oops", "@oops2"), Match.CONTAINS);
    }

    public void testVarCompletionWithPrefix() throws ParseException {
        //at line end
        checkCC("@oops:1;\n"
                + ".mixin(@oops2) {\n"
                + "    color: @oo|\n"
                + "}", arr("@oops", "@oops2"), Match.CONTAINS);

        //before semi
        checkCC("@oops:1;\n"
                + ".mixin(@oops2) {\n"
                + "    color: @oo|;\n"
                + "}", arr("@oops", "@oops2"), Match.CONTAINS);

        //before ws and semi
        checkCC("@oops:1;\n"
                + ".mixin(@oops2) {\n"
                + "    color: @oo| ;\n"
                + "}", arr("@oops", "@oops2"), Match.CONTAINS);

    }

    public void testMixinCompletionOutsideOfAnyRule() throws ParseException {
        checkCC(".myMixin(@c){\n"
                + "    div{\n"
                + "    color:red   \n"
                + "    }\n"
                + "}\n"
                + ".| ", arr("myMixin"), Match.CONTAINS);
        
        checkCC(".myMixin(@c){\n"
                + "    div{\n"
                + "    color:red   \n"
                + "    }\n"
                + "}\n"
                + ".| \n"
                + ".clz {}", arr("myMixin"), Match.CONTAINS);
        
        checkCC(".myMixin(@c){\n"
                + "    div{\n"
                + "    color:red   \n"
                + "    }\n"
                + "}\n"
                + ".my| ", arr("myMixin"), Match.EXACT);
        
        checkCC(".myMixin(@c){\n"
                + "    div{\n"
                + "    color:red   \n"
                + "    }\n"
                + "}\n"
                + ".my| \n"
                + ".clz {}", arr("myMixin"), Match.EXACT);
    }
}
