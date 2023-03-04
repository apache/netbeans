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
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.MediaBody;
import org.netbeans.modules.css.model.api.MediaQueryList;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.PlainElement;
import org.openide.util.CharSequences;

/**
 *
 * @author marekfukala
 */
public class MediaI extends ModelElement implements Media {

    private MediaQueryList mediaQueryList;
    private MediaBody mediaBody;
    
    
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(MediaQueryList value) {
            mediaQueryList = value;
        }

        @Override
        public void elementAdded(MediaBody value) {
            mediaBody = value;
        }

    };

    public MediaI(Model model) {
        super(model);
        
        addTextElement("@media");
        addTextElement(" ");
        addEmptyElement(MediaQueryList.class);
        addTextElement(" ");
        addTextElement("{");
        addEmptyElement(MediaBody.class);
        addTextElement("}");
    }

    public MediaI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    protected Class getModelClass() {
        return Media.class;
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    public MediaQueryList getMediaQueryList() {
        return mediaQueryList;
    }

    @Override
    public void setMediaQueryList(MediaQueryList mediaQueryList) {
        setElement(mediaQueryList);
    }

    @Override
    public MediaBody getMediaBody() {
        return mediaBody;
    }

    @Override
    public void setMediaBody(MediaBody mediaBody) {
        //find the existing curly braces and put the element between them
        for(int i = getElementsCount() - 1; i >= 0; i--) {
            PlainElement e = getElementAt(i, PlainElement.class);
            if(e != null && CharSequences.indexOf(e.getContent(), "}") != -1) {
                //found the closing curly brace, put the mediaBody before it
                insertElement(i, mediaBody);
                return ;
            }
        }
        throw new IllegalStateException("Can't found curly braces in Media element!"); //NOI18N
    }

}
