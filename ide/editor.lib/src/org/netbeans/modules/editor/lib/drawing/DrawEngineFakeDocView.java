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

package org.netbeans.modules.editor.lib.drawing;

import javax.swing.text.Element;

/**
 *  Fake view of the whole document supporting the code folding, operating from given startOffset
 *  to endOffset
 *
 * @author Martin Roskanin
 */
/* package */ class DrawEngineFakeDocView extends DrawEngineDocView{

        private boolean useCollapsing = true;
        private int fakeStartOffset;
        private int fakeEndOffset;
        
        DrawEngineFakeDocView(Element elem, int startOffset, int endOffset, boolean useCollapsing){
            this(elem, startOffset, endOffset, useCollapsing, false);
        }

        DrawEngineFakeDocView(Element elem, int startOffset, int endOffset, boolean useCollapsing, boolean hideBottomPadding){
            super(elem, hideBottomPadding);

            this.useCollapsing = useCollapsing;
            this.fakeStartOffset = startOffset;
            this.fakeEndOffset = endOffset;
            setEstimatedSpan(false);
        }
        
        @Override
        public int getStartOffset(){
            return fakeStartOffset;
        }
        
        @Override
        public int getEndOffset(){
            return fakeEndOffset;
        }
        
        @Override
        protected void attachListeners(){
        }
    
}
