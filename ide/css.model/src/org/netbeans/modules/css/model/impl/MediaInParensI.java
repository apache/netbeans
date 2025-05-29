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

import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.model.api.MediaExpression;
import org.netbeans.modules.css.model.api.MediaInParens;
import org.netbeans.modules.css.model.api.Model;

public class MediaInParensI extends ModelElement implements MediaInParens {

    private MediaExpression mediaExpression;
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {
        @Override
        public void elementAdded(MediaExpression value) {
            mediaExpression = value;
        }
    };

    public MediaInParensI(Model model) {
        super(model);
    }

    public MediaInParensI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    protected Class getModelClass() {
        return MediaInParens.class;
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    public MediaExpression getMediaExpression() {
        return mediaExpression;
    }

    @Override
    public void setMediaExpression(MediaExpression mediaExpression) {
        setElement(mediaExpression);
    }

}
