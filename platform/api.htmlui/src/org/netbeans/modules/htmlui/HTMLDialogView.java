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
package org.netbeans.modules.htmlui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import javax.swing.JButton;
import org.openide.util.Exceptions;

final class HTMLDialogView extends HTMLDialogBase {
    private final HtmlPair<?> view;

    public HTMLDialogView(String url, HtmlPair<?> view) {
        super(url);
        this.view = view;
    }

    @Override
    public String showAndWait() {
        view.makeVisible(() -> {
            try {
                view.load(getClass().getClassLoader(), new URL(url), () -> {
                    onPageLoad.run();
                    JButton[] b = Buttons.buttons();
                    System.err.println("butt: " + Arrays.toString(b));
                    return null;
                }, this.techIds.toArray(new String[0]));
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        return null;
    }

    @Override
    public <C> C component(Class<C> type) {
        return null;
    }
    
}
