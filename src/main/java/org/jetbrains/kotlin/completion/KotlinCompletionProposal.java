/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.completion;

import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
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
import org.jetbrains.kotlin.utils.KotlinImageProvider;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinCompletionProposal extends DefaultCompletionProposal {

    private final String text, proposal;
    private final String type, name;
    private final int caretOffset, idenStartOffset;
    private final ImageIcon FIELD_ICON;
    private final DeclarationDescriptor descriptor;
    private final StyledDocument doc;
    
    public KotlinCompletionProposal(int idenStartOffset, int caretOffset, 
            DeclarationDescriptor descriptor, StyledDocument doc) {
        this.text = descriptor.getName().getIdentifier();
        this.idenStartOffset = idenStartOffset;
        this.caretOffset = caretOffset;
        this.proposal = DescriptorRenderer.ONLY_NAMES_WITH_SHORT_TYPES.render(descriptor);
        this.descriptor = descriptor;
        this.FIELD_ICON = KotlinImageProvider.INSTANCE.getImage(descriptor);
        this.doc = doc;
        String[] splitted = proposal.split(":");
        name = splitted[0];
        if (splitted.length > 1) {
            type = splitted[1];
        } else {
            type = "";
        }
    }
    
    @Override
    public ElementHandle getElement() {
        return null;
    }
    
    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        return name;
    }
    
    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return type;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getInsertPrefix() {
        return proposal;
    }
    
    @Override
    public String getSortText() {
        return name;
    }
    
    @Override
    public int getAnchorOffset() {
        return idenStartOffset;
    }
    
    @Override
    public int getSortPrioOverride() {
        if (descriptor instanceof VariableDescriptor) {
            return 20;
        } else if (descriptor instanceof FunctionDescriptor) {
            return 30;
        } else if (descriptor instanceof ClassDescriptor) {
            return 40;
        } else if (descriptor instanceof PackageFragmentDescriptor
                || descriptor instanceof PackageViewDescriptor) {
            return 10;
        } else {
            return 150;
        }
    }
    
    @Override
    public ImageIcon getIcon() {
        return FIELD_ICON;
    }
    
    public DeclarationDescriptor getDeclarationDescriptor() {
        return descriptor;
    }
    
    public void doInsert() {
        try {
            doc.remove(idenStartOffset, caretOffset - idenStartOffset);
            if (descriptor instanceof FunctionDescriptor) {
                functionAction(doc);
            } else {
                doc.insertString(idenStartOffset, text, null);
            }
            
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void functionAction(StyledDocument doc) throws BadLocationException {
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        List<ValueParameterDescriptor> params = functionDescriptor.getValueParameters();
        
        if (params.size() == 1) {
            if (name.contains("->")) {
                doc.insertString(idenStartOffset, text + "{  }", null);
                return;
            }
        }
        
        StringBuilder functionParams = new StringBuilder();
        functionParams.append("(");
        for (ValueParameterDescriptor desc : params) {
            functionParams.append(getValueParameter(desc));
            functionParams.append(",");
        }
        if (params.size() > 0) {
            functionParams.deleteCharAt(functionParams.length() - 1);
        }
        functionParams.append(")");
        doc.insertString(idenStartOffset, text + functionParams.toString(), null);
    }
    
    private String getValueParameter(ValueParameterDescriptor desc) {
        KotlinType kotlinType = desc.getType();
        ClassifierDescriptor classifierDescriptor = kotlinType.getConstructor().getDeclarationDescriptor();
        if (classifierDescriptor == null) {
            return desc.getName().asString();
        }

        String typeName = classifierDescriptor.getName().asString();
        String value = KotlinCompletionUtils.INSTANCE.getValueForType(typeName);
        if (value == null) {
            value = desc.getName().asString();
        }

        return value;
    }
    
}
