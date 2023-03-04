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
package org.netbeans.modules.autoupdate.ui;

import java.awt.Image;
import javax.swing.text.html.*;
import javax.swing.text.*;

/**
 * @author Radek Matous
 */
public class HTMLEditorKitEx extends HTMLEditorKit {

    @Override
    public ViewFactory getViewFactory() {
        return new HTMLFactory() {
            @Override
            public View create(Element elem) {
                Object obj = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
                return (obj == HTML.Tag.IMG) ? new CachedImageView(elem) : super.create(elem);
            }
        };
    }

    private static class CachedImageView extends ImageView {
        CachedImageView(Element el) {
            super(el);
        }

        @Override
        public Image getImage() {
            return super.getImage();
        }
    }
}
