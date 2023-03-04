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
package org.netbeans.modules.html.angular.navigate;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.html.angular.GeneralAngular;

/**
 *
 * @author vriha
 */
public class BindOnceNavTest extends GeneralAngular {

    static final String[] tests = new String[]{
        "openProject",
        "testExpression21"
    };

    public BindOnceNavTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(BindOnceNavTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("asctrlmodule");
        evt.waitNoEvent(2000);
        openFile("partials|bindonce.html", "asctrlmodule");
        BindOnceNavTest.originalContent = new EditorOperator("bindonce.html").getText();
        endTest();
    }

    public void testExpression21() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("bindonce.html"), 18);
        endTest();
    }
}
