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

package org.netbeans.modules.profiler.ui;

import org.netbeans.lib.profiler.ui.components.HTMLLabel;
import org.openide.awt.HtmlBrowser;
import java.net.URL;
import javax.swing.BorderFactory;


public final class NBHTMLLabel extends HTMLLabel {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public NBHTMLLabel(final String text) {
        super(text);
        setBorder(BorderFactory.createEmptyBorder(5, 3, 3, 5));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected void showURL(final URL url) {
        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
    }
}
