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

package org.openide.loaders;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author  pzajac
 */
public class SortModeEditorTest extends NbTestCase {

    /** Creates a new instance of SortModeEditorTest */
    public SortModeEditorTest(String name) {
        super(name);
    }


    public void testSortModeEditor () {

        SortModeEditor editor = new SortModeEditor();
        String values[] = editor.getTags();
        for (int i = 0 ; i < values.length ; i++ ) {
            editor.setAsText(values[i]);
            assertEquals (values[i],editor.getAsText());
        }
        
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
