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
package org.netbeans.modules.web.jsf.editor;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.jsf.api.palette.PaletteItem;
import org.netbeans.modules.web.jsf.api.palette.PaletteItemsProvider;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Lookup;

public class JsfCodeGenerator {

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {

            JTextComponent component = context.lookup(JTextComponent.class);
            List<CodeGenerator> generators = new ArrayList<>();
            generators.add(new InjectCompositeComponent.InjectCCCodeGen());

            //add palette items
            for (PaletteItem item : PaletteItemsProvider.getPaletteItems()) {
                generators.add(new PaletteCodeGenerator(component, item));
            }
            return generators;
        }
    }

    private static class PaletteCodeGenerator implements CodeGenerator {

        private JTextComponent component;
        private PaletteItem item;

        public PaletteCodeGenerator(JTextComponent component, PaletteItem item) {
            this.component = component;
            this.item = item;
        }

        @Override
        public String getDisplayName() {
            return item.getDisplayName();
        }

        @Override
        public void invoke() {
            item.insert(component);
        }
    }
}
