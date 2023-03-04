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

package org.netbeans.api.languages;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;

/**
 * Represents context for methods called from nbs files.
 *
 * @author Jan Jancura
 */
public abstract class Context { 

    
    /**
     * Returns instance of editor.
     * 
     * @return instance of editor
     */
    public abstract JTextComponent getJTextComponent ();
    
    /**
     * Returns instance of {@link javax.swing.text.Document}.
     * 
     * @return instance of {@link javax.swing.text.Document}
     */
    public abstract Document getDocument ();
    
    public abstract int getOffset ();
    
    
    /**
     * Creates a new Context.
     * 
     * @return a new Context
     */
    public static Context create (Document doc, int offset) {
        return new CookieImpl (doc, offset);
    }
    
    private static class CookieImpl extends Context {
        
        private Document        doc;
        private JTextComponent  component;
        private int             offset;
        
        CookieImpl (
            Document        doc,
            int             offset
        ) {
            this.doc = doc;
            this.offset = offset;
        }
        
        public JTextComponent getJTextComponent () {
            if (component == null) {
                DataObject dob = NbEditorUtilities.getDataObject (doc);
                EditorCookie ec = dob.getLookup ().lookup (EditorCookie.class);
                if (ec.getOpenedPanes ().length > 0)
                    component = ec.getOpenedPanes () [0];
            }
            return component;
        }
        
        public Document getDocument () {
            return doc;
        }
        
        public int getOffset () {
            return offset;
        }
    }
}


