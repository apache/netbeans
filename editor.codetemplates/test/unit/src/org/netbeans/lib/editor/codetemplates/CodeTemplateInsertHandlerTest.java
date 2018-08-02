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
package org.netbeans.lib.editor.codetemplates;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;

/**
 *
 * @author markiewb
 */
public class CodeTemplateInsertHandlerTest {

    @Test
    public void testPrioritizeParameters() {
        CodeTemplateSpiPackageAccessor get = CodeTemplateSpiPackageAccessor.get();
        CodeTemplateParameter paramA = get.createParameter(new CodeTemplateParameterImpl(null, "${paramA}", 0));
        CodeTemplateParameter paramB = get.createParameter(new CodeTemplateParameterImpl(null, "${paramB ordering=\"2\"}", 0));
        CodeTemplateParameter paramC = get.createParameter(new CodeTemplateParameterImpl(null, "${paramC ordering=\"1\"}", 0));

        List<CodeTemplateParameter> prioritizeParameters = CodeTemplateInsertHandler.prioritizeParameters(Arrays.asList(paramA, paramB, paramC));
        assertEquals(Arrays.asList(paramC, paramB, paramA), prioritizeParameters);
    }

}
