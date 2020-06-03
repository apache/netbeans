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
package org.netbeans.modules.cnd.spi.utils;

import java.util.Collection;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Lookup;

/**
 * Implement this to notify about the errors
 */
public abstract class  CndNotifier {
    
    public static enum Priority {
        HIGH,
        NORMAL,
        LOW,
        SILENT
    }
    
    public static enum Category {
        INFO,
        WARNING,
        ERROR
    }
    private static final CndNotifier DEFAULT = new CndErrorNotifierDefault();
    
    
    public static CndNotifier getDefault() {
        if (CndUtils.isStandalone()) {
            return DEFAULT;
        }
        Collection<? extends CndNotifier> notifiers = Lookup.getDefault().lookupAll(CndNotifier.class);
        if (notifiers.isEmpty()) {
            return DEFAULT;
        }
       return notifiers.iterator().next();
    }
    
    /**
     * 
     * @param title
     * @param msg
     */
    abstract public void notifyError(String msg);
    
    /**
     * 
     * @param title
     * @param msg
     */
    abstract public void notifyInfo(String msg);

    /**
     * 
     * @param title
     * @param msg
     */
    abstract public void notifyErrorLater(String msg);   
    
    
    abstract public boolean notifyAndIgnore(String title, String msg);
    
    abstract public void notifyStatus(String text);
    
    
    private static class CndErrorNotifierDefault extends CndNotifier {

        @Override
        public void notifyErrorLater(String msg) {
            notifyError(msg);
        }
        

        @Override
        public void notifyError(String msg) {
            System.err.println(msg);//NOI18N
        }

        @Override
        public void notifyInfo(String msg) {
            System.out.println(msg);//NOI18N
        }

        @Override
        public boolean notifyAndIgnore(String title, String msg) {
           notifyError(msg);
           return true;
        }

        @Override
        public void notifyStatus(String text) {
            System.out.println(text);
        }

    }
    
}
