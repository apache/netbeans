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
package org.netbeans.modules.cnd.highlight.semantic;

import org.netbeans.modules.cnd.modelutil.CsmFontColorManager;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;

/**
 *
 */
public abstract class HighlighterBase extends IndexingAwareParserResultTask<Parser.Result> implements CsmFontColorManager.FontColorChangeListener {

    /*package*/ static final boolean MINIMAL = Boolean.getBoolean("cnd.highlighting.minimal");
    
    public HighlighterBase() {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    protected void init(String mimeType){
        CsmFontColorManager.instance().addListener(mimeType, this);
    }
    
    // ChangeListener
    @Override
    public void stateChanged(FontColorProvider provider) {
        updateFontColors(provider);
        //run(PhaseRunner.Phase.INIT);
    }
    
    protected abstract void updateFontColors(FontColorProvider provider);

    protected boolean isCancelled() {
        return Thread.currentThread().isInterrupted();
    }
}
