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
import org.netbeans.modules.css.model.api.MediaInParens;
import org.netbeans.modules.css.model.api.Model;

public class MediaConditionI extends ModelElement implements MediaCondition {

    private Collection<MediaExpression> mediaExpressions = new ArrayList<>();
    private Collection<MediaInParens> mediaInParens = new ArrayList<>();
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(MediaExpression value) {
            mediaExpressions.add(value);
        }

        @Override
        public void elementAdded(MediaInParens value) {
            mediaInParens.add(value);
            mediaExpressions.add(value.getMediaExpression());
        }
    };

    public MediaConditionI(Model model) {
        super(model);
    }

    public MediaConditionI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    protected Class getModelClass() {
        return MediaCondition.class;
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    public Collection<MediaExpression> getMediaExpressions() {
        return mediaExpressions;
    }

    @Override
    public void addMediaExpression(MediaExpression mediaExpression) {
        addElement(mediaExpression);
    }

    @Override
    public Collection<MediaInParens> getMediaInParens() {
        return mediaInParens;
    }

    @Override
    public void addMediaInParens(MediaInParens mediaExpression) {
        addElement(mediaExpression);
    }
}
