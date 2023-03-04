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
package org.netbeans.modules.css.model.impl;

import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.model.api.MediaExpression;
import org.netbeans.modules.css.model.api.MediaFeature;
import org.netbeans.modules.css.model.api.MediaFeatureValue;
import org.netbeans.modules.css.model.api.Model;

/**
 *
 * @author marekfukala
 */
public class MediaExpressionI extends ModelElement implements MediaExpression {

    private MediaFeature mediaFeature;
    private MediaFeatureValue mediaFeatureValue;
    
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(MediaFeature value) {
            mediaFeature = value;
        }

        @Override
        public void elementAdded(MediaFeatureValue value) {
            mediaFeatureValue = value;
        }      
        
    };
    
    public MediaExpressionI(Model model) {
        super(model);
        
        addTextElement("(");
        addTextElement(" ");
        addEmptyElement(MediaFeature.class);
        addEmptyElement(MediaFeatureValue.class);
        addTextElement(" ");
        addTextElement(")");
        addTextElement(" ");
    }

    public MediaExpressionI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    protected Class getModelClass() {
        return MediaExpression.class;
    }
    
    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    public MediaFeature getMediaFeature() {
        return mediaFeature;
    }

    @Override
    public void setMediaFeature(MediaFeature mediaFeature) {
        setElement(mediaFeature);
    }

    @Override
    public MediaFeatureValue getMediaFeatureValue() {
        return mediaFeatureValue;
    }

    @Override
    public void setMediaFeatureValue(MediaFeatureValue mediaFeatureValue) {
        setElement(mediaFeatureValue);
    }

}
