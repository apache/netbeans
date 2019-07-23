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
package org.netbeans.modules.nativeexecution.spi.support;

import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.openide.util.Lookup;

/**
 *
 * @author masha
 */
abstract public class NativeExecutionUserNotification {

    private static NativeExecutionUserNotification INSTANCE = null;

    public static enum Descriptor {
        WARNING,
        ERROR
    }

    public static synchronized NativeExecutionUserNotification getDefault() {
        if (INSTANCE == null) {
            INSTANCE = Lookup.getDefault().lookup(NativeExecutionUserNotification.class);

            if (INSTANCE == null) {
                INSTANCE = new Trivial();
            }
        }

        return INSTANCE;
    }

    abstract public void notify(String message, Descriptor type);

    abstract public void notify(String message);
    
    abstract public void notifyStatus(String message);

    abstract public void showErrorNotification(String title, String shortMessage, String longMesage);

    abstract public void showInfoNotification(String title, String shortMessage, String longMesage);

    abstract public boolean confirmShellStatusValiation(String title, final String header, final String footer, final Shell shell);
    
    abstract public boolean showYesNoQuestion(String title, String text);
    

    private static class Trivial extends NativeExecutionUserNotification {

        public Trivial() {
        }

        @Override
        public void notify(String message, Descriptor type) {
            System.err.println(message);
        }

        @Override
        public void notify(String message) {
            System.err.println(message);
        }

        @Override
        public void showErrorNotification(String title, String shortMessage, String longMesage) {
            System.err.println(shortMessage);
        }

        @Override
        public void showInfoNotification(String title, String shortMessage, String longMesage) {
            System.err.println(shortMessage);
        }

        @Override
        public boolean confirmShellStatusValiation(String title, String header, String footer, Shell shell) {
            System.err.println(title);
            System.err.println(header);
            for (String error : shell.getValidationStatus().getErrors()) {
                System.err.println(error);
            }
            System.err.println(footer);
            return true;
        }

        @Override
        public boolean showYesNoQuestion(String title, String text) {
            System.err.println(text);
            return true;
        }

        @Override
        public void notifyStatus(String message) {
            System.err.println(message);
        }
                
    }

}
