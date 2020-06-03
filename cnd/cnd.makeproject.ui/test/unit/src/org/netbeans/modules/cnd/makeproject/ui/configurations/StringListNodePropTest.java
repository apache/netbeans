/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
