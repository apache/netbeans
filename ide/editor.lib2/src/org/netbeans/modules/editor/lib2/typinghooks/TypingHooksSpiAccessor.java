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

package org.netbeans.modules.editor.lib2.typinghooks;

import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author vita
 */
public abstract class TypingHooksSpiAccessor {

    private static TypingHooksSpiAccessor ACCESSOR = null;

    public static synchronized void register(TypingHooksSpiAccessor accessor) {
        assert ACCESSOR == null : "Can't register two package accessors!"; //NOI18N
        ACCESSOR = accessor;
    }

    public static synchronized TypingHooksSpiAccessor get() {
        // Trying to wake up HighlightsLayer ...
        try {
            Class<?> clazz = Class.forName(TypedTextInterceptor.MutableContext.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }

        assert ACCESSOR != null : "There is no package accessor available!"; //NOI18N
        return ACCESSOR;
    }

    /** Creates a new instance of HighlightingSpiPackageAccessor */
    protected TypingHooksSpiAccessor() {
    }

    public abstract TypedTextInterceptor.MutableContext createTtiContext(JTextComponent c, Position offset, String typedText, String replacedText);
    public abstract Object [] getTtiContextData(TypedTextInterceptor.MutableContext context);
    public abstract void resetTtiContextData(TypedTextInterceptor.MutableContext context);
    
    public abstract DeletedTextInterceptor.Context createDtiContext(JTextComponent c, int offset, String removedText, boolean backwardDelete);
    public abstract Object [] getDwiContextData(CamelCaseInterceptor.MutableContext context);
    public abstract CamelCaseInterceptor.MutableContext createDwiContext(JTextComponent c, int offset, boolean backwardDelete);
    
    public abstract TypedBreakInterceptor.MutableContext createTbiContext(JTextComponent c, int caretOffset, int insertBreakOffset);
    public abstract Object [] getTbiContextData(TypedBreakInterceptor.MutableContext context);
    public abstract void resetTbiContextData(TypedBreakInterceptor.MutableContext context);
}
