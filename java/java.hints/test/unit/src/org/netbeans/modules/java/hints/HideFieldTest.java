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
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.modules.java.hints.legacy.spi.RulesManager;
import org.netbeans.modules.java.hints.legacy.spi.RulesManager.LegacyHintConfiguration;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Jaroslav Tulach
 */
public class HideFieldTest extends TreeRuleTestBase {
    private String sourceLevel = "1.5";
    
    public HideFieldTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Locale.setDefault(Locale.US);
    }
    
    
    
    public void testClassWithOnlyStaticMethods() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int value;" +
            "}" +
            "class Test2 extends Test {" +
            "  private float val";
        String after =         "ue;" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:92-0:97:verifier:Field hides another field"
        );
    }
    public void testNoHintWithMethod() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int value() { return 1 };" +
            "}" +
            "class Test2 extends Test {" +
            "  private float val";
        String after =         "ue;" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    public void testDisabled() throws Exception {
        LegacyHintConfiguration conf = RulesManager.currentHintPreferences.get();
        Preferences prefs = conf.preferences;
        String origSetting = prefs.get(HideField.KEY_WARN_HIDDEN_STATIC_FIELDS, null);
        try {
            prefs.putBoolean(HideField.KEY_WARN_HIDDEN_STATIC_FIELDS, false);
            
            String code = "package test; class Test {" +
                "  public static int HH;" +
                "}" +
                "class Test2 extends Test {" +
                "  public static int H|H;" +
                "}";

            performAnalysisTest("test/Test.java", code);
        } finally {
            if (origSetting == null) {
                prefs.remove(HideField.KEY_WARN_HIDDEN_STATIC_FIELDS);
            } else {
                prefs.put(HideField.KEY_WARN_HIDDEN_STATIC_FIELDS, origSetting);
            }
        }
    }

    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new HideField().run(info, path);
    }
    
    
}
