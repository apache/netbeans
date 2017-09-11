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
package org.netbeans.modules.java.hints.declarative.idebinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser;
import org.netbeans.modules.java.hints.spiimpl.TestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class HintsTaskTest extends TestBase {
    
    public HintsTaskTest(String name) {
        super(name);
    }
    
    public void test206116WarnAboutRemovingNonVoidExpression() throws Exception {
        prepareTest("test/Test.java", "");
        
        FileObject hint = sourceRoot.createData("test.hint");
        String code = "Math.min(0, 1) => ;;";
        
        TestUtilities.copyStringToFile(hint, code);
        
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        DeclarativeHintsParser.Result res = new DeclarativeHintsParser().parse(hint, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<ErrorDescription> errorInstances = HintsTask.computeErrors(res, code, hint);
        List<String> errors = new ArrayList<String>();
        
        for (ErrorDescription ed : errorInstances) {
            errors.add(ed.toString());
        }
        
        assertEquals(Arrays.asList("0:0-0:15:warning:ERR_RemoveExpression"), errors);
    }
    
    public void test206116NoWarningAboutRemovingVoidExpression() throws Exception {
        prepareTest("test/Test.java", "");
        
        FileObject hint = sourceRoot.createData("test.hint");
        String code = "System.err.println() => ;;";
        
        TestUtilities.copyStringToFile(hint, code);
        
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        DeclarativeHintsParser.Result res = new DeclarativeHintsParser().parse(hint, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<ErrorDescription> errorInstances = HintsTask.computeErrors(res, code, hint);
        List<String> errors = new ArrayList<String>();
        
        for (ErrorDescription ed : errorInstances) {
            errors.add(ed.toString());
        }
        
        assertEquals(Arrays.asList(), errors);
    }
    
    public void test206116NoWarningAboutRemovingNonExpressions() throws Exception {
        prepareTest("test/Test.java", "");
        
        FileObject hint = sourceRoot.createData("test.hint");
        String code = "Math.min(0, 1); => ;;";
        
        TestUtilities.copyStringToFile(hint, code);
        
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        DeclarativeHintsParser.Result res = new DeclarativeHintsParser().parse(hint, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<ErrorDescription> errorInstances = HintsTask.computeErrors(res, code, hint);
        List<String> errors = new ArrayList<String>();
        
        for (ErrorDescription ed : errorInstances) {
            errors.add(ed.toString());
        }
        
        assertEquals(Arrays.asList(), errors);
    }
    
    public void test206116NoWarningAboutRemovingUnattributable() throws Exception {
        prepareTest("test/Test.java", "");
        
        FileObject hint = sourceRoot.createData("test.hint");
        String code = "Math.doesNotExist(0, 1) => ;;";
        
        TestUtilities.copyStringToFile(hint, code);
        
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        DeclarativeHintsParser.Result res = new DeclarativeHintsParser().parse(hint, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<ErrorDescription> errorInstances = HintsTask.computeErrors(res, code, hint);
        List<String> errors = new ArrayList<String>();
        
        for (ErrorDescription ed : errorInstances) {
            errors.add(ed.toString());
        }
        
        assertFalse(new HashSet<String>(errors).contains("ERR_RemoveExpression"));
    }
    
    static {
        NbBundle.setBranding("test");
    }
}
