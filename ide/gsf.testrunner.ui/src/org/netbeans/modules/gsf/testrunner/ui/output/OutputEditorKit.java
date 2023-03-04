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

package org.netbeans.modules.gsf.testrunner.ui.output;

import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * 
 * @author  Marian Petras
 */
public final class OutputEditorKit extends DefaultEditorKit
                            implements ViewFactory {

    public OutputEditorKit() {
        super();
    }

    @Override
    public ViewFactory getViewFactory() {
        return this;
    }

    public View create(Element element) {
        return (element instanceof OutputDocument.RootElement)
               ? new OutputView(element)
               : new PlainView(element);
    }

    @Override
    public Document createDefaultDocument() {
        return new OutputDocument();
    }

}
