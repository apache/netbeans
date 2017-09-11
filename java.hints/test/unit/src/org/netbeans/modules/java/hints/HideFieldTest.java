/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
