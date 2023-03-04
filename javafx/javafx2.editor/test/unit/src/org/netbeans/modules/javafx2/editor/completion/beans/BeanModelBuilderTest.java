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
