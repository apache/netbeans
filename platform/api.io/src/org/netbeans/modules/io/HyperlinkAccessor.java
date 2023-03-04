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
package org.netbeans.modules.io;

import org.netbeans.api.intent.Intent;
import org.netbeans.spi.io.support.HyperlinkType;
import org.netbeans.api.io.Hyperlink;

/**
 *
 * @author jhavlin
 */
public abstract class HyperlinkAccessor {

    /**
     * The default implementation is set in static initializer of
     * {@link Hyperlink}.
     */
    private static HyperlinkAccessor DEFAULT;

    public static void setDefault(HyperlinkAccessor def) {
        HyperlinkAccessor.DEFAULT = def;
    }

    public static HyperlinkAccessor getDefault() {
        if (DEFAULT != null) {
            return DEFAULT;
        }

        // invokes static initializer of Item.class
        // that will assign value to the DEFAULT field above
        Class<Hyperlink> c = Hyperlink.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        assert DEFAULT != null :
                "The DEFAULT field must be initialized";                //NOI18N
        return DEFAULT;
    }

    public abstract HyperlinkType getType(Hyperlink hyperlink);

    public abstract boolean isImportant(Hyperlink hyperlink);

    public abstract Runnable getRunnable(Hyperlink hyperlink);

    public abstract Intent getIntent(Hyperlink hyperlink);
}
