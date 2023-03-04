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

package org.netbeans.modules.editor.errorstripe;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator;

/**
 *
 * @author Jan Lahoda
 */
public class TestMarkProviderCreator implements MarkProviderCreator {

    private MarkProvider provider;

    /** Creates a new instance of TestMarkProviderCreator */
    private TestMarkProviderCreator() {
    }

    public void setProvider(MarkProvider provider) {
        this.provider = provider;
    }

    public MarkProvider createMarkProvider(JTextComponent document) {
        return provider;
    }
    
    private static final TestMarkProviderCreator INSTANCE = new TestMarkProviderCreator();
    
    public static final TestMarkProviderCreator getDefault() {
        return INSTANCE;
    }
    
}
