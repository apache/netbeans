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
package org.netbeans.modules.css.prep.util;

import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.CssPreprocessorImplementation;
import org.netbeans.modules.web.common.spi.CssPreprocessorImplementationListener;

public abstract class BaseCssPreprocessor implements CssPreprocessorImplementation {

    protected final CssPreprocessorImplementationListener.Support listenersSupport = new CssPreprocessorImplementationListener.Support();


    @Override
    public void addCssPreprocessorListener(CssPreprocessorImplementationListener listener) {
        listenersSupport.addCssPreprocessorListener(listener);
    }

    @Override
    public void removeCssPreprocessorListener(CssPreprocessorImplementationListener listener) {
        listenersSupport.removeCssPreprocessorListener(listener);
    }

    public void fireOptionsChanged() {
        listenersSupport.fireOptionsChanged(this);
    }

    public void fireCustomizerChanged(Project project) {
        listenersSupport.fireCustomizerChanged(project, this);
    }

    public void fireProcessingErrorOccured(Project project, String error) {
        listenersSupport.fireProcessingErrorOccured(project, this, error);
    }

}
