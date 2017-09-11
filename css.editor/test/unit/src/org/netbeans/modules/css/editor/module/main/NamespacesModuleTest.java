/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
