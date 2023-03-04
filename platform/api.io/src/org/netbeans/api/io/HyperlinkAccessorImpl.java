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
package org.netbeans.api.io;

import org.netbeans.api.intent.Intent;
import org.netbeans.modules.io.HyperlinkAccessor;
import org.netbeans.spi.io.support.HyperlinkType;

/**
 * Implementation of accessor that enables retrieving information about
 * hyperlinks in SPI.
 *
 * @author jhavlin
 */
class HyperlinkAccessorImpl extends HyperlinkAccessor {

    @Override
    public HyperlinkType getType(Hyperlink hyperlink) {
        if (hyperlink instanceof Hyperlink.OnClickHyperlink) {
            return HyperlinkType.FROM_RUNNABLE;
        } else if (hyperlink instanceof Hyperlink.IntentHyperlink) {
            return HyperlinkType.FROM_INTENT;
        } else {
            throw new IllegalArgumentException("Unknown hyperlink.");   //NOI18N
        }
    }

    @Override
    public boolean isImportant(Hyperlink hyperlink) {
        return hyperlink.isImportant();
    }

    @Override
    public Runnable getRunnable(Hyperlink hyperlink) {
        if (hyperlink instanceof Hyperlink.OnClickHyperlink) {
            return ((Hyperlink.OnClickHyperlink) hyperlink).getRunnable();
        } else {
            throw new IllegalArgumentException(
                    "Not an FROM_RUNNABLE link.");                      //NOI18N
        }
    }

    @Override
    public Intent getIntent(Hyperlink hyperlink) {
        if (hyperlink instanceof Hyperlink.IntentHyperlink) {
            return ((Hyperlink.IntentHyperlink) hyperlink).getIntent();
        } else {
            throw new IllegalArgumentException(
                    "Not a FROM_INTENT link");                          //NOI18N
        }
    }
}
