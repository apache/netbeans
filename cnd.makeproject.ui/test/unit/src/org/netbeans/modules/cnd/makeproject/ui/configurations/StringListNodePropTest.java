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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.beans.PropertyEditor;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.utils.TokenizerFactory;

public class StringListNodePropTest {    
  
    private final VectorConfiguration<String> fakeConfiguration = new VectorConfiguration<>(null);
    private StringListNodeProp instance = null;
    
    @Before
    public void setUp() throws Exception {
        instance = new StringListNodeProp(fakeConfiguration, null, new String[]{"preprocessor-definitions", "Macroses", "Macroses", "Macroses", ""}, true, null) {
            @Override
            protected List<String> convertToList(String text) {
                return TokenizerFactory.MACRO_CONVERTER.convertToList(text);
            }

            @Override
            protected String convertToString(List<String> list) {
                return TokenizerFactory.MACRO_CONVERTER.convertToString(list);
            }
            
        };
    }

    private void testEditor(String initial, String converted, String[] tokens) {
        PropertyEditor editor = instance.getPropertyEditor();
        editor.setAsText(initial);
        instance.setValue((List) editor.getValue());
        assertEquals("Unneeded spaces should be removed",  converted, instance.getPropertyEditor().getAsText());
        assertArrayEquals("Items should be parsed properly", tokens, ((List) editor.getValue()).toArray());
        instance.restoreDefaultValue();
    }
    
    @Test
    public void testGetPropertyEditor() {
        testEditor(" 111   222  333=444       555 -g", 
                   "111 222 333=444 555", 
                   new String[] {"111", "222", "333=444", "555"});
        testEditor("111 \"222 333\" \"44 4=555\" \"666=777 888\" 999=000 \"a\"", 
                   "111 \"222 333\" \"44 4=555\" \"666=777 888\" 999=000 a", 
                   new String[] {"111", "222 333", "44 4=555", "666=777 888", "999=000", "a"});
        testEditor("111    \"222 333\"     \"44 4=555\" \"666=777 888\"   999=000 \"a\"  b", 
                   "111 \"222 333\" \"44 4=555\" \"666=777 888\" 999=000 a b", 
                   new String[] {"111", "222 333", "44 4=555", "666=777 888", "999=000", "a", "b"});
        testEditor("m1=v1   m2=\"v2\"    m3=\"v 3\" ", 
                   "m1=v1 m2=\"v2\" \"m3=\\\"v 3\\\"\"", 
                   new String[] {"m1=v1", "m2=\"v2\"", "m3=\"v 3\""});     
        testEditor("  -DMMM=\"qq\"  -DXXX -DHHH=xxx ", 
                   "MMM=\"qq\" XXX HHH=xxx", 
                   new String[] {"MMM=\"qq\"", "XXX", "HHH=xxx"});
        testEditor("\"AD_VER=char *szVer=\\\"biswdas_linux\\\"\" EXT=\"ExternalClass.h\" \"FOO=foo()\" IMPL=ImplClass MA=main \"QQ=namespace qq { namespace in {\" QQ_CLOSE=}} \"QUOTE(name, extension)=<name.extension>\" \"RET(index)=ret[index]\" USE=qq::in", 
                   "\"AD_VER=char *szVer=\\\"biswdas_linux\\\"\" EXT=\"ExternalClass.h\" \"FOO=foo()\" IMPL=ImplClass MA=main \"QQ=namespace qq { namespace in {\" QQ_CLOSE=}} \"QUOTE(name, extension)=<name.extension>\" \"RET(index)=ret[index]\" USE=qq::in", 
                   new String[] {
                    "AD_VER=char *szVer=\"biswdas_linux\"",
                    "EXT=\"ExternalClass.h\"",
                    "FOO=foo()",
                    "IMPL=ImplClass",
                    "MA=main",
                    "QQ=namespace qq { namespace in {",
                    "QQ_CLOSE=}}",
                    "QUOTE(name, extension)=<name.extension>",
                    "RET(index)=ret[index]",
                    "USE=qq::in"
                   });
        testEditor("\"-DRET(index)=ret[index]\" \"-DFOO=foo()\" -DUSE=qq::in -DIMPL=ImplClass \"-DQUOTE(name, extension)=<name.extension>\" -DMA=\"main\" -DEXT=\\\"ExternalClass.h\\\" \"-DQQ=namespace qq { namespace in {\" -DQQ_CLOSE=\"}}\" -DAD_VER='char *szVer=\"biswdas_linux\"' -I../src/impl -I../incl -I../external", 
                   "\"RET(index)=ret[index]\" \"FOO=foo()\" USE=qq::in IMPL=ImplClass \"QUOTE(name, extension)=<name.extension>\" MA=\"main\" EXT=\"ExternalClass.h\" \"QQ=namespace qq { namespace in {\" QQ_CLOSE=\"}}\" \"AD_VER='char *szVer=\\\"biswdas_linux\\\"'\"", 
                   new String[] {
                    "RET(index)=ret[index]",
                    "FOO=foo()",
                    "USE=qq::in",
                    "IMPL=ImplClass",
                    "QUOTE(name, extension)=<name.extension>",
                    "MA=\"main\"",
                    "EXT=\"ExternalClass.h\"",
                    "QQ=namespace qq { namespace in {",
                    "QQ_CLOSE=\"}}\"",
                    "AD_VER='char *szVer=\"biswdas_linux\"'",
                   });
        
//        
    }

}
