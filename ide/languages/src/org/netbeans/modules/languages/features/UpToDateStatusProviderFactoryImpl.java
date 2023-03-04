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

package org.netbeans.modules.languages.features;

import javax.swing.text.Document;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;


/**
 *
 * @author Jan Jancura
 */
public class UpToDateStatusProviderFactoryImpl implements UpToDateStatusProviderFactory {
    
    /** Creates a new instance of UpToDateStatusProvider */
    public UpToDateStatusProviderFactoryImpl () {
    }

    public UpToDateStatusProvider createUpToDateStatusProvider (Document document) {
        return new UpToDateStatusProviderImpl ((NbEditorDocument) document);
    }
    
    private static class UpToDateStatusProviderImpl extends UpToDateStatusProvider {
        
        private ParserManager editorParser;
        
        
        private UpToDateStatusProviderImpl (NbEditorDocument doc) {
            editorParser = ParserManager.get (doc);
            editorParser.addListener (new ParserManagerListener () {
                public void parsed (State state, ASTNode ast) {
                    firePropertyChange (PROP_UP_TO_DATE, null, null);
                }
            });
        }
        
        public UpToDateStatus getUpToDate () {
            switch (editorParser.getState ()) {
                case ERROR:
                    return UpToDateStatus.UP_TO_DATE_DIRTY;
                case OK:
                    return UpToDateStatus.UP_TO_DATE_OK;
                case PARSING:
                    return UpToDateStatus.UP_TO_DATE_PROCESSING;
            }
            return UpToDateStatus.UP_TO_DATE_PROCESSING;
        }
    }
}


