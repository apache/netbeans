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
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class NamespacesModuleTest extends CssModuleTestBase {
    
    public NamespacesModuleTest(String name) {
        super(name);
    }

    public void testNamespaceKeywordCompletion() throws ParseException  {
        checkCC("|", arr("@namespace"), Match.CONTAINS);
        checkCC("@|", arr("@namespace"), Match.CONTAINS);
        checkCC("@name|", arr("@namespace"), Match.EXACT);
    }
    
    public void testNamespaceKeywordCompletionInContext() throws ParseException  {
        checkCC("|  .clz {}", arr("@namespace"), Match.CONTAINS);
        checkCC("@|  .clz {}", arr("@namespace"), Match.CONTAINS);

        checkCC("|  #id {}", arr("@namespace"), Match.CONTAINS);
        checkCC("@|  #id {}", arr("@namespace"), Match.CONTAINS);
        
        checkCC("|  div {}", arr("@namespace"), Match.CONTAINS);
        checkCC("@|  div {}", arr("@namespace"), Match.CONTAINS);

        checkCC("|  \n.clz {}", arr("@namespace"), Match.CONTAINS);
        checkCC("@|  \n.clz {}", arr("@namespace"), Match.CONTAINS);

        checkCC("|  \n#id {}", arr("@namespace"), Match.CONTAINS);
        checkCC("@|  \n#id {}", arr("@namespace"), Match.CONTAINS);

        checkCC("|  \ndiv {}", arr("@namespace"), Match.CONTAINS);
        checkCC("@|  \ndiv {}", arr("@namespace"), Match.CONTAINS);
        
        checkCC("@name|  #id {}", arr("@namespace"), Match.EXACT);
        checkCC("@name|  \n#id {}", arr("@namespace"), Match.EXACT);
        
        checkCC("@name|  .clz {}", arr("@namespace"), Match.EXACT);
        checkCC("@name|  \n.clz {}", arr("@namespace"), Match.EXACT);
        
        checkCC("@name|  div {}", arr("@namespace"), Match.EXACT);
        checkCC("@name|  \ndiv {}", arr("@namespace"), Match.EXACT);
        
    }
    
    
    public void testNamespacePrefixesCompletion() throws ParseException  {
        String nsdecl = "@namespace foo \"http://foo.org\";\n ";
        
        checkCC(nsdecl + "| ", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "f| ", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "foo| ", arr("foo"), Match.CONTAINS);
        
        checkCC(nsdecl + "div {} | ", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "div {} f| ", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "div {} foo| ", arr("foo"), Match.CONTAINS);

        checkCC(nsdecl + "h1 | ", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "h1 f| ", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "h1 foo| ", arr("foo"), Match.CONTAINS);
        
        //ugly - first pipe is caret, second the normal text pipe char
        checkCC(nsdecl + "h1 ||h2 ", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "h1 f||h2 ", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "h1 foo||h2 ", arr("foo"), Match.CONTAINS);
    }
    
    public void testNamespacePrefixesInAttributeCompletion() throws ParseException  {
        String nsdecl = "@namespace foo \"http://foo.org\";\n ";
        checkCC(nsdecl + "h1[|", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "h1[ |", arr("foo"), Match.CONTAINS);
        
        checkCC(nsdecl + "h1[|]", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "h1[ |]", arr("foo"), Match.CONTAINS);
        
        checkCC(nsdecl + "h1[f|", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "h1[foo|]", arr("foo"), Match.CONTAINS);
        
        checkCC(nsdecl + "h1[ f|", arr("foo"), Match.CONTAINS);
        checkCC(nsdecl + "h1[ foo|]", arr("foo"), Match.CONTAINS);
        
        checkCC(nsdecl + "h1[*|attr=val]", arr("foo"), Match.CONTAINS, '*');
        checkCC(nsdecl + "h1[f*|attr=val]", arr("foo"), Match.CONTAINS, '*');
        checkCC(nsdecl + "h1[foo*|attr=val]", arr("foo"), Match.CONTAINS, '*');
        
    }
   
}
