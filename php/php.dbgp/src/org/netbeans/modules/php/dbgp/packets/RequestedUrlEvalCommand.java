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
package org.netbeans.modules.php.dbgp.packets;

/**
 *
 * @author Ondrej Brejla
 */
public class RequestedUrlEvalCommand extends EvalCommand {
    private static final String REQUEST_URI = "(isset($_SERVER['SSL']) ? 'https' : 'http').'://'.$_SERVER['SERVER_NAME'].':'.$_SERVER['SERVER_PORT'].$_SERVER['REQUEST_URI']"; // NOI18N
    private static final LastUsedTransactionIdHolder LAST_USED_TRANSACTION_ID_HOLDER = LastUsedTransactionIdHolder.getInstance();

    public RequestedUrlEvalCommand(String transactionId) {
        super(transactionId);
        LAST_USED_TRANSACTION_ID_HOLDER.setLastUsedTransactionId(transactionId);
    }

    @Override
    protected String getData() {
        return REQUEST_URI;
    }

    public static String getLastUsedTransactionId() {
        return LAST_USED_TRANSACTION_ID_HOLDER.getLastUsedTransactionId();
    }

    private static final class LastUsedTransactionIdHolder {
        private static final LastUsedTransactionIdHolder INSTANCE = new LastUsedTransactionIdHolder();
        private String lastUsedTransactionId;

        private LastUsedTransactionIdHolder() {
        }

        public static LastUsedTransactionIdHolder getInstance() {
            return INSTANCE;
        }

        public synchronized String getLastUsedTransactionId() {
            return lastUsedTransactionId;
        }

        public synchronized void setLastUsedTransactionId(final String lastUsedTransactionId) {
            this.lastUsedTransactionId = lastUsedTransactionId;
        }

    }

}
