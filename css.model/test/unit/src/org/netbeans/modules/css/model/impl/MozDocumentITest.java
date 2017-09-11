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
package org.netbeans.modules.css.model.impl;

import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.MozDocument;
import org.netbeans.modules.css.model.api.MozDocumentFunction;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class MozDocumentITest extends ModelTestBase {

    public MozDocumentITest(String name) {
        super(name);
    }

    public void testBasic() throws BadLocationException, ParseException {
        String code = "@-moz-document domain(mozilla.org),  regexp(\"https:.*\")  { body { color: purple } }";

        StyleSheet styleSheet = createStyleSheet(code);

//        TestUtil.dumpResult(TestUtil.parse(code));

        List<MozDocument> mozdocs = styleSheet.getBody().getMozDocuments();
        assertNotNull(mozdocs);
        assertEquals(1, mozdocs.size());

        MozDocument mozdoc = mozdocs.get(0);
        assertNotNull(mozdoc);

        Body body = mozdoc.getBody();
        assertNotNull(body);
        
        Declarations ds = body.getRules().get(0).getDeclarations();
        assertNotNull(ds);
        
        List<MozDocumentFunction> restrictions = mozdoc.getRestrictions();
        assertEquals(2, restrictions.size());
        
        MozDocumentFunction r1 = restrictions.get(0);
        assertNotNull(r1);
        
        assertEquals("domain(mozilla.org)", r1.getContent().toString());
        
        MozDocumentFunction r2 = restrictions.get(1);
        assertNotNull(r2);
        
        assertEquals("regexp(\"https:.*\")", r2.getContent().toString());

    }

    //XXX: MozDocument is not mutable
//    public void testAddToEmptyStyleSheet() {
//        Model model = createModel();
//        StyleSheet styleSheet = getStyleSheet(model);
//        ElementFactory f = model.getElementFactory();
//        
//        Body body = f.createBody();
//        styleSheet.setBody(body);
//
//        MozDocument mozd = f.createMozDocument();
//        MozDocumentFunction mozdf1 = f.createMozDocumentFunction();
//        mozdf1.setContent("domain(mozilla.org)");
//        MozDocumentFunction mozdf2 = f.createMozDocumentFunction();
//        mozdf2.setContent("regexp(\"https:.*\")");
//    }
    
}
