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
package org.netbeans.modules.css.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.model.api.MediaCondition;
import org.netbeans.modules.css.model.api.MediaExpression;
import org.netbeans.modules.css.model.api.MediaQuery;
import org.netbeans.modules.css.model.api.MediaQueryOperator;
import org.netbeans.modules.css.model.api.MediaType;
import org.netbeans.modules.css.model.api.Model;

/**
 *
 * @author marekfukala
 */
public class MediaQueryI extends ModelElement implements MediaQuery {

    private MediaQueryOperator mediaQueryOperator;
    private MediaType mediaType;
    private Collection<MediaExpression> mediaExpressions = new ArrayList<>();
    
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(MediaQueryOperator value) {
            mediaQueryOperator = value;
        }

        @Override
        public void elementAdded(MediaType value) {
            mediaType = value;
        }

        @Override
        public void elementAdded(MediaExpression value) {
            mediaExpressions.add(value);
        }
      
        @Override
        public void elementAdded(MediaCondition value) {
            mediaExpressions.addAll(value.getMediaExpressions());
        }
        
    };
    
    public MediaQueryI(Model model) {
        super(model);
        
        addEmptyElement(MediaQueryOperator.class);
        addTextElement(" ");
        addEmptyElement(MediaType.class);
        addTextElement(" ");
    }

    public MediaQueryI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    protected Class getModelClass() {
        return MediaQuery.class;
    }

    @Override
    public MediaQueryOperator getMediaQueryOperator() {
        return mediaQueryOperator;
    }

    @Override
    public void setMediaQueryOperator(MediaQueryOperator mediaQueryOperator) {
        setElement(mediaQueryOperator);
    }

    @Override
    public MediaType getMediaType() {
        return mediaType;
    }

    @Override
    public void setMediaType(MediaType mediaType) {
        setElement(mediaType);
    }

    @Override
    public Collection<MediaExpression> getMediaExpressions() {
        return mediaExpressions;
    }

    @Override
    public void addMediaExpression(MediaExpression mediaExpression) {
        addTextElement("AND");
        addTextElement(" ");
        addElement(mediaExpression);
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

}
