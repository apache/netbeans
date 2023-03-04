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

package org.netbeans.modules.parsing.spi;

import java.util.EventObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Source;
import org.openide.util.Parameters;

/**
 *
 * @author hanz
 */
public class SourceModificationEvent extends EventObject {
    
    private final boolean sourceChanged;
    
    /**
     * Creates a new {@link SourceModificationEvent}
     * @param source the {@link Source} in which the event occurred
     * @deprecated use {@link SourceModificationEvent#SourceModificationEvent(java.lang.Object, boolean)}
     */
    @Deprecated
    protected SourceModificationEvent (
        Object              source
    ) {
        this(source, true);
    }
    
    /**
     * Creates a new {@link SourceModificationEvent}
     * @param source the {@link Source} in which the event occurred
     * @param sourceChanged true if the change caused a modification of the text being parsed.
     * @since 1.36
     */
    protected SourceModificationEvent (
        Object              source,
        boolean             sourceChanged
    ) {
        super (source);
        this.sourceChanged = sourceChanged;
    }

    public Source getModifiedSource () {
        return (Source) getSource ();
    }
    
    /**
     * Returns true when the change causing this event affected the source.
     * @return true if the source was changed
     * @since 1.36
     */
    public boolean sourceChanged() {
        return sourceChanged;
    }
    
    /**
     * Returns start offset of the change that affected the source.
     * @return offset or -1 if the source was not affected
     * @since 9.1
     */
    public int getAffectedStartOffset() {
        return -1;
    }

    /**
     * Returns end offset of the change that affected the source.
     * @return offset or -1 if the source was not affected
     * @since 9.1
     */
    public int getAffectedEndOffset() {
        return -1;
    }
    
    @Override
    public String toString () {
        return "SourceModificationEvent " + hashCode () + "(source: " + source + ")";
    }

    /**
     * @since 9.8.0
     */
    public static class Composite extends SourceModificationEvent {
        private final SourceModificationEvent read;
        private final SourceModificationEvent write;

        public Composite(
                @NonNull final SourceModificationEvent read,
                @NonNull SourceModificationEvent write) {
            super(read.getSource(), true);
            Parameters.notNull("read", read);   //NOI18N
            Parameters.notNull("write", write);   //NOI18N
            this.read = read;
            this.write = write;
        }

        public SourceModificationEvent getWriteEvent() {
            return write;
        }

        public SourceModificationEvent getReadEvent() {
            return read;
        }
    }
}
