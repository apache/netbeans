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
package org.netbeans.modules.profiler.heapwalk.details.jdk.ui;

import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.GapContent;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.InsetsBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.ComponentBuilders.JComponentBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.Utils.InstanceBuilder;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;

/**
 *
 * @author Jiri Sedlacek
 */
final class TextComponentBuilders {
    
    // Make sure subclasses are listed before base class if using isSubclassOf
    static ComponentBuilders.ComponentBuilder getBuilder(Instance instance, Heap heap) {
        if (DetailsUtils.isSubclassOf(instance, JTextField.class.getName())) {
            return new JTextFieldBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JTextArea.class.getName())) {
            return new JTextAreaBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JEditorPane.class.getName())) {
            return new JEditorPaneBuilder(instance, heap);
        }
        return null;
    }
    
    
    private static class ContentTextBuilder extends InstanceBuilder<String> {
        
        private final char[] array;
        
        ContentTextBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            List<String> values = DetailsUtils.getPrimitiveArrayFieldValues(instance, "array");
            array = values != null ? DetailsUtils.getCharArray(values) : null;
        }
        
        static ContentTextBuilder fromField(Instance instance, String field, Heap heap) {
            Object model = instance.getValueOfField(field);
            if (!(model instanceof Instance)) return null;
            if (!DetailsUtils.isSubclassOf((Instance)model, GapContent.class.getName())) return null;
            return new ContentTextBuilder((Instance)model, heap);
        }
        
        protected String createInstanceImpl() {
            return array != null ? new String(array).trim() : "";
        }
        
    }
    
    private static class DocumentTextBuilder extends InstanceBuilder<String> {
        
        private final ContentTextBuilder data;
        
        DocumentTextBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            data = ContentTextBuilder.fromField(instance, "data", heap);
        }
        
        static DocumentTextBuilder fromField(Instance instance, String field, Heap heap) {
            Object model = instance.getValueOfField(field);
            if (!(model instanceof Instance)) return null;
            if (!DetailsUtils.isSubclassOf((Instance)model, PlainDocument.class.getName())) return null;
            return new DocumentTextBuilder((Instance)model, heap);
        }
        
        protected String createInstanceImpl() {
            return data != null ? data.createInstance() : "";
        }
        
    }
    
    private abstract static class JTextComponentBuilder<T extends JTextComponent> extends JComponentBuilder<T> {
        
        private final DocumentTextBuilder model;
        private final boolean isEditable;
        private final InsetsBuilder margin;
        
        JTextComponentBuilder(Instance instance, Heap heap) {
            super(instance, heap, false);
            
            model = DocumentTextBuilder.fromField(instance, "model", heap);
            
            isEditable = DetailsUtils.getBooleanFieldValue(instance, "editable", false);
            
            margin = InsetsBuilder.fromField(instance, "margin", heap);
        }
        
        protected void setupInstance(T instance) {
            super.setupInstance(instance);
            
            if (model != null) instance.setText(model.createInstance());
            
            instance.setEditable(isEditable);
            
            if (margin != null) instance.setMargin(margin.createInstance());
        }
        
    }
    
    private static class JTextFieldBuilder extends JTextComponentBuilder<JTextField> {
        
        JTextFieldBuilder(Instance instance, Heap heap) {
            super(instance, heap);
        }
        
        protected JTextField createInstanceImpl() {
            return new JTextField();
        }
        
    }
    
    private static class JTextAreaBuilder extends JTextComponentBuilder<JTextArea> {
        
        JTextAreaBuilder(Instance instance, Heap heap) {
            super(instance, heap);
        }
        
        protected JTextArea createInstanceImpl() {
            return new JTextArea();
        }
        
    }
    
    private static class JEditorPaneBuilder extends JTextComponentBuilder<JEditorPane> {
        
        JEditorPaneBuilder(Instance instance, Heap heap) {
            super(instance, heap);
        }
        
        protected JEditorPane createInstanceImpl() {
            return new JEditorPane();
        }
        
    }
    
}
