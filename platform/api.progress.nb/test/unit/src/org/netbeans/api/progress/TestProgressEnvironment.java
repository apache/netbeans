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
package org.netbeans.api.progress;

import java.util.concurrent.Callable;
import junit.framework.AssertionFailedError;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.ProgressEnvironment;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = ProgressEnvironment.class, position = 0)
public class TestProgressEnvironment implements ProgressEnvironment {
    public volatile ProgressEnvironment delegate;

    public TestProgressEnvironment() {
    }
    
    public ProgressEnvironment delegate() {
        if (delegate != null) {
            return delegate;
        }
        return Lookup.getDefault().lookupAll(ProgressEnvironment.class).stream().
                filter(i -> i != this).findFirst().orElseThrow(() -> new AssertionFailedError());
    }

    @Override
    public ProgressHandle createHandle(String displayname, Cancellable c, boolean userInit) {
        return delegate().createHandle(displayname, c, userInit);
    }

    @Override
    public Controller getController() {
        return delegate().getController();
    }
    
    public static void withEnvironment(ProgressEnvironment instance, Callable<Void> c) throws Exception {
        // will fail on CCE if this is not the 1st.
        TestProgressEnvironment e = (TestProgressEnvironment)Lookup.getDefault().lookup(TestProgressEnvironment.class);
        try {
            e.delegate = instance;
            c.call();
        } finally {
            e.delegate = null;
        }
    }
}
