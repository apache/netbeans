/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.jetbrains.kotlin.utils.KotlinImageProvider;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.FunctionDescriptor;
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor;
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor;
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor;
import org.jetbrains.kotlin.descriptors.VariableDescriptor;
import org.jetbrains.kotlin.renderer.DescriptorRenderer;
import org.jetbrains.kotlin.types.KotlinType;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinCompletionItem implements CompletionItem {

    private final String text, proposal;
    private final String type, name;
    private final ImageIcon FIELD_ICON; 
    private static final Color FIELD_COLOR = Color.decode("0x0000B2"); 
    private final int caretOffset, idenStartOffset; 
    private final DeclarationDescriptor descriptor;
    
    public KotlinCompletionItem(int idenStartOffset, int caretOffset, DeclarationDescriptor descriptor) { 
        this.text = descriptor.getName().getIdentifier(); 
        this.idenStartOffset = idenStartOffset;
        this.caretOffset = caretOffset; 
        this.proposal = DescriptorRenderer.ONLY_NAMES_WITH_SHORT_TYPES.render(descriptor);
        this.FIELD_ICON = KotlinImageProvider.INSTANCE.getImage(descriptor);
        this.descriptor = descriptor;
        String[] splitted = proposal.split(":");
        name = splitted[0];
        if (splitted.length > 1){
            type = splitted[1];
        } else {
            type = "";
        }
    }
    
    private String getValueParameter(ValueParameterDescriptor desc) {
        KotlinType kotlinType = desc.getType();
        ClassifierDescriptor classifierDescriptor = kotlinType.getConstructor().getDeclarationDescriptor();
        if (classifierDescriptor == null) {
            return desc.getName().asString();
        }
        
        String typeName = classifierDescriptor.getName().asString();
        
        if (typeName.equals("Int")  || typeName.equals("Long") || typeName.equals("Short")) {
            return "0";
        } else if (typeName.equals("Double") || typeName.equals("Float")) {
            return "0.0";
        } else if (typeName.equals("String")) {
            return "\"" + desc.getName().asString() + "\"";
        } else if (typeName.equals("Char")) {
            return "\"\"";
        }
        else if (typeName.equals("Boolean")) {
            return "true";
        } else return desc.getName().asString();
    }
    
    @Override
    public void defaultAction(JTextComponent jtc) {
        try {
            StyledDocument doc = (StyledDocument) jtc.getDocument();
            doc.remove(idenStartOffset, caretOffset - idenStartOffset);
            if (descriptor instanceof FunctionDescriptor){
                List<ValueParameterDescriptor> params = ((FunctionDescriptor) descriptor).getValueParameters();
                StringBuilder functionParams = new StringBuilder();
                functionParams.append("(");
                for (ValueParameterDescriptor desc : params) {
                    functionParams.append(getValueParameter(desc));
                    functionParams.append(",");
                }
                if (params.size() > 0) {
                    functionParams.deleteCharAt(functionParams.length()-1);
                }
                functionParams.append(")");
                doc.insertString(idenStartOffset, text + functionParams.toString(), null);
            } else{
                doc.insertString(idenStartOffset, text, null);
            }
            Completion.get().hideAll();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent ke) {
    }

    @Override
    public int getPreferredWidth(Graphics graphics, Font font) {
        return CompletionUtilities.getPreferredWidth(proposal, null, graphics, font);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(FIELD_ICON, name, type, g, defaultFont, 
                (selected ? Color.white : FIELD_COLOR), width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent jtc) {
        return false;
    }

    @Override
    public int getSortPriority() {
        if (descriptor instanceof VariableDescriptor){
            return 20;
        } else if (descriptor instanceof FunctionDescriptor){
            return 30;
        } else if (descriptor instanceof ClassDescriptor){
            return 40;
        } else if (descriptor instanceof PackageFragmentDescriptor 
                || descriptor instanceof PackageViewDescriptor){
            return 10;
        } else {
            return 150;
        }
    }

    @Override
    public CharSequence getSortText() {
        return name;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return proposal;
    }
    
}
