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

package org.netbeans.modules.editor.errorstripe.privatespi;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * A factory for <code>MarkProvider</code>s. Implementations of this interface should
 * be registered in module layers under the <code>Editors/&lt;mime-type&gt;/UpToDateStatusProvider</code>
 * folder.
 *
 * @author Jan Lahoda
 */
@MimeLocation(subfolderName="UpToDateStatusProvider")
public interface MarkProviderCreator {

    /**
     * Create an instance of {@link MarkProvider} for the given {@link Document}.
     *
     * @param document The document to create a <code>MarkProvider</code> for.
     * 
     * @return The requested {@link MarkProvider}.
     */
    public MarkProvider createMarkProvider(JTextComponent document);
    
}
