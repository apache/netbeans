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

package org.netbeans.modules.diff;

import java.awt.Component;
import java.io.IOException;
import java.io.Reader;
import javax.swing.JPanel;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;

/**
 *
 * @author Martin Entlicher
 */
public class FallbackDiffViewTest extends DiffViewAbstract {

    /** Creates a new instance of DefaultDiffViewTest */
    public FallbackDiffViewTest(String name) {
        super(name);
    }

    protected DiffView createDiffView(StreamSource ss1, StreamSource ss2) throws IOException {
        return new DiffImpl().createDiff(ss1, ss2);
    }
    
    private static class DiffImpl extends Diff {
        public Component createDiff(String name1, String title1,
                                    Reader r1, String name2, String title2,
                                    Reader r2, String MIMEType) throws IOException  {
            return new JPanel();
        }
        
    }
}
