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
package org.netbeans.modules.templatesui;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 */
public final class HTMLWizard extends AbstractWizard {
    private static final Logger LOG = Logger.getLogger(HTMLWizard.class.getName());
    /** publicly known factory method */
    public static WizardDescriptor.InstantiatingIterator<?> create(FileObject data) {
        return new HTMLWizard(data);
    }
    
    private final FileObject def;
    final CountDownLatch initializationDone = new CountDownLatch(1);

    private HTMLWizard(FileObject definition) {
        this.def = definition;
    }
    
    @Override
    protected Object initSequence(ClassLoader l) throws Exception {
        String clazz = (String) def.getAttribute("class");
        String method = (String) def.getAttribute("method");
        Method m = Class.forName(clazz, true, l).getDeclaredMethod(method);
        m.setAccessible(true);
        Object ret = m.invoke(null);
        return ret;
    }
    @Override
    protected URL initPage(ClassLoader l) {
        String page = (String) def.getAttribute("page");
        return l.getResource(page);
    }

    @Override
    protected void initializationDone(Throwable t) {
        if (t != null) {
            LOG.log(Level.SEVERE, "Problems initializing HTML wizard", t);
        }
        initializationDone.countDown();
    }

    @Override
    protected String[] getTechIds() {
        List<String> techIds = new ArrayList<>();
        for (int i = 0;; i++) {
            Object val = def.getAttribute("techId." + i);
            if (val instanceof String) {
                techIds.add((String) val);
            } else {
                break;
            }
        }
        return techIds.toArray(new String[0]);
    }
}
