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
package org.netbeans.modules.intent;

import java.util.concurrent.CountDownLatch;
import org.netbeans.spi.intent.Result;
import org.openide.util.Parameters;

/**
 *
 * @author jhavlin
 */
public final class SettableResult implements Result {

    private final CountDownLatch latch = new CountDownLatch(1);

    private Object result = null;
    private Exception exception = null;

    public Object getResult() {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            setException(ex);
            latch.countDown();
        }
        return result;
    }

    public synchronized Exception getException() {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            setException(ex);
            latch.countDown();
        }
        return exception;
    }

    @Override
    public synchronized void setResult(Object result) {
        this.result = result;
        latch.countDown();
    }

    @Override
    public synchronized void setException(Exception exception) {
        Parameters.notNull("exception", exception);                     //NOI18N
        this.exception = exception;
        latch.countDown();
    }
}
