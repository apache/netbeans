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

package org.netbeans.modules.java.guards;

import java.util.List;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;

/**
 *
 * @author Jan Pokorsky
 */
public final class JavaGuardedSectionsProvider extends AbstractGuardedSectionsProvider {
    
    public JavaGuardedSectionsProvider(GuardedEditorSupport editor) {
        super(editor, true);
    }

    public char[] writeSections(List<GuardedSection> sections, char[] buff) {
        JavaGuardedWriter writer = new JavaGuardedWriter();
        writer.setGuardedSection(sections);
        return writer.translate(buff);
    }

    public AbstractGuardedSectionsProvider.Result readSections(char[] buff) {
        JavaGuardedReader reader = new JavaGuardedReader(this);
        return new AbstractGuardedSectionsProvider.Result(
                reader.translateToCharBuff(buff),
                reader.getGuardedSections()
                );
    }
    
    
}
