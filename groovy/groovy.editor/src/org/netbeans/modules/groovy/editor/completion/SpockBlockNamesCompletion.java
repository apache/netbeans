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
package org.netbeans.modules.groovy.editor.completion;

import java.util.Map;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.api.elements.KeywordElement;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public class SpockBlockNamesCompletion extends BaseCompletion {

    enum SpockBlock {
        SPOCK_and("and"),
        SPOCK_cleanup("cleanup"),
        SPOCK_expect("expect"),
        SPOCK_given("given"),
        SPOCK_setup("setup"),
        SPOCK_then("then"),
        SPOCK_when("when"),
        SPOCK_where("where");
        
        private final String name;

        private SpockBlock(String name) {
            this.name = name;
        }   
    }
    
    static class SpockBlockItem extends CompletionItem {
        private static final String ICON_PATH = "org/netbeans/modules/groovy/editor/resources/spock-16x16.png"; //NOI18N
        private final String name;
        private static volatile ImageIcon spockIcon;

        public SpockBlockItem(String name, int anchorOffset) {
            super(new KeywordElement(name), anchorOffset);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }
        
        @Override
        public ImageIcon getIcon() {
            if (spockIcon == null) {
                spockIcon = ImageUtilities.loadImageIcon(ICON_PATH, false);
            }
            return spockIcon;
        }

        @Override
        public String getInsertPrefix() {
            return getName() + ": ";
        }
        
    }
    
    @Override
    public boolean complete(Map<Object, CompletionProposal> proposals, CompletionContext request, int anchor) {
        if (request.location == CaretLocation.INSIDE_METHOD 
                && SpockUtils.isInSpecificationClass(request)) {
            for (SpockBlock block: SpockBlock.values()) {
                if (block.name.startsWith(request.getPrefix())) {
                    proposals.put("spockblock:" + block.name, new SpockBlockItem(block.name, anchor));
                }
            }
        }
        
        return true;
    }
    
}
