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

package org.netbeans.modules.hibernate.completion;

import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 * Provides the code completion for Hibernate Reverse Engineering file
 * 
 * @author gowri
 */
public class HibernateRevengCompletionProvider implements CompletionProvider {

    public CompletionTask createTask(int queryType, JTextComponent component) {
        if( queryType != CompletionProvider.COMPLETION_QUERY_TYPE ) 
            return null;
        
            return new AsyncCompletionTask(new HibernateRevengCompletionQuery(queryType,
                    component.getSelectionStart()), component);
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
         // return 0 here, meaning no query should be automatically invoked.
        // If we want some query to be invoked automatically, then a combination of the 
        // COMPLETION_QUERY_TYPE, 
        // COMPLETION_ALL_QUERY_TYPE, 
        // DOCUMENTATION_QUERY_TYPE, 
        // and TOOLTIP_QUERY_TYPE  values should be returned
        return 0;
    }
    
}
