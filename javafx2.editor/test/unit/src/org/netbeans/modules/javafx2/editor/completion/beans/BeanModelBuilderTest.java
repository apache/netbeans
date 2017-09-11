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
package org.netbeans.modules.javafx2.editor.completion.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TypeUtilities.TypeNameOptions;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.java.source.parsing.JavacParserResult;
import org.netbeans.modules.javafx2.editor.GoldenFileTestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;

/**
 *
 * @author sdedic
 */
public class BeanModelBuilderTest extends GoldenFileTestBase {

    public BeanModelBuilderTest(String testName) {
        super(testName);
    }

    class UT extends UserTask implements ClasspathInfoProvider {
        private StringBuilder content;
        
        private String className;

        public UT(String className) {
            this.className = className;
        }
        
        public void run(ResultIterator resultIterator) throws Exception {
            JavacParserResult res = (JavacParserResult)resultIterator.getParserResult();
            CompilationInfo ci = res.get(CompilationInfo.class);

            FxBean bi = FxBean.getBeanProvider(ci).getBeanInfo(className);
            StringBuilder sb = new StringBuilder();
            printBeanInfo(sb, bi, ci);
            content = sb;
        }            

        @Override
        public ClasspathInfo getClasspathInfo() {
            return cpInfo;
        }
    };
    
    public void testAnchorPane() throws Exception {
        UT ut = new UT("javafx.scene.layout.AnchorPane");
        ParserManager.parse("text/x-java", ut);
        assertContents(ut.content);
    }
    
    private static void printType(StringBuilder sb, TypeMirror tm, CompilationInfo ci) {
        sb.append(ci.getTypeUtilities().getTypeName(tm, TypeNameOptions.PRINT_FQN, TypeNameOptions.PRINT_AS_VARARG));
    }
    
    private static void printProperty(StringBuilder sb, FxProperty pi, CompilationInfo ci) {
        if (pi == null) {
            sb.append("<null>");
            return;
        }
        sb.append("Property[");
        sb.append("name: ").append(pi.getName()).
                append("; kind: ").append(pi.getKind()).
                append("; simple: ").append(pi.isSimple()).
                append("; type: ");
        printType(sb, pi.getType().resolve(ci), ci);
        sb.append("; target: ");
        if (pi.getObjectType() != null) {
            sb.append(pi.getObjectType().resolve(ci));
        } else {
            sb.append("<null>");
        }
        sb.append("; accessor: ").append(pi.getAccessor());
        sb.append("]");
    }
    
    private static void printEvent(StringBuilder sb, FxEvent ei, CompilationInfo ci) {
        sb.append("Event[");
        sb.append("name: ").append(ei.getName()).
                append("; type: ").append(ei.getEventType().resolve(ci));
        sb.append("]");
    }
    
    private static void printProperties(StringBuilder sb, Map m, CompilationInfo ci) {
        ArrayList al = new ArrayList(m.keySet());
        Collections.sort(al);
        
        for (Object o : al) {
            FxProperty v = (FxProperty)m.get(o);
            sb.append("    ");
            printProperty(sb, v, ci);
            sb.append("\n");
        }
    }
    
    private static void printEvents(StringBuilder sb, Map m, CompilationInfo ci) {
        ArrayList al = new ArrayList(m.keySet());
        Collections.sort(al);
        
        for (Object o : al) {
            FxEvent v = (FxEvent)m.get(o);
            sb.append("    ");
            printEvent(sb, v, ci);
            sb.append("\n");
        }
    }

    private static void printBeanInfo(StringBuilder sb, FxBean bi, CompilationInfo ci) {
        sb.append("BeanInfo[");
        sb.append("\n  className: ").append(bi.getClassName()).
                append("; default: ");
        printProperty(sb, bi.getDefaultProperty(), ci);
        sb.append("; value: ").append(bi.hasValueOf()).
                append("\n factories: ").append(bi.getFactoryNames());
        sb.append("\n properties: ").append("\n");
        printProperties(sb, bi.getProperties(), ci);
        sb.append("\n attached properties: ").append("\n");
        printProperties(sb, bi.getAttachedProperties(), ci);

        sb.append("\n events: ").append("\n");
        printEvents(sb, bi.getEvents(), ci);
    }
}
