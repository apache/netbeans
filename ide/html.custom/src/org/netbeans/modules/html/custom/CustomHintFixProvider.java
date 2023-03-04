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
package org.netbeans.modules.html.custom;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.html.custom.hints.AddAttributeFix;
import org.netbeans.modules.html.custom.hints.AddElementFix;
import org.netbeans.modules.html.custom.hints.EditProjectsConfFix;
import org.netbeans.modules.html.editor.spi.HintFixProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marek
 */
@ServiceProvider(service = HintFixProvider.class)
public class CustomHintFixProvider extends HintFixProvider {

    @Override
    public List<HintFix> getHintFixes(Context context) {
        List<HintFix> fixes = new ArrayList<>();
        String elementName = (String)context.getMetadata().get(UNKNOWN_ELEMENT_FOUND);
        String attributeName = (String)context.getMetadata().get(UNKNOWN_ATTRIBUTE_FOUND);
        String contextElementName = (String)context.getMetadata().get(UNKNOWN_ELEMENT_CONTEXT);
        
        assert contextElementName != null;
        
        if(elementName != null) {
            //unknown element found
            fixes.add(new AddElementFix(elementName, contextElementName, context.getSnapshot()));
            fixes.add(new EditProjectsConfFix(context.getSnapshot()));
            
        } else if(attributeName != null) {
            //unknown attribute found
            fixes.add(new AddAttributeFix(attributeName, contextElementName, context.getSnapshot()));
            fixes.add(new AddAttributeFix(attributeName, null, context.getSnapshot()));
            fixes.add(new EditProjectsConfFix(context.getSnapshot()));
        } else {
            throw new IllegalStateException();
        }
        
        return fixes;
    }
}
