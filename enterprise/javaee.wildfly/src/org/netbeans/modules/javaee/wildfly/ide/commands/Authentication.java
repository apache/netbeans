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
package org.netbeans.modules.javaee.wildfly.ide.commands;

import java.io.IOException;
import java.util.Arrays;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

/**
 *
 * @author ehugonnet
 */
public class Authentication {

    private final String username;
    private final char[] password;
    private CallbackHandler callBackHandler;

    public Authentication() {
        this.username = "";
        this.password = new char[0];
        this.callBackHandler = createCallBackHandler();
    }

    public Authentication(String username, char[] password) {
        this.username = username;
        this.password = Arrays.copyOf(password, password.length);
        this.callBackHandler = createCallBackHandler();
    }

    private CallbackHandler createCallBackHandler(){
        return new CallbackHandler() {

            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback current : callbacks) {
                    if (current instanceof NameCallback) {
                        NameCallback nameCallback = (NameCallback) current;
                        nameCallback.setName(username);
                    } else if (current instanceof PasswordCallback) {
                        PasswordCallback pwdCallback = (PasswordCallback) current;
                        pwdCallback.setPassword(password);
                    } else if (current instanceof RealmCallback) {
                        RealmCallback realmCallback = (RealmCallback) current;
                        realmCallback.setText(realmCallback.getDefaultText());
                    } else {
                        throw new UnsupportedCallbackException(current);
                    }
                }
            }
        };
    }

    public CallbackHandler getCallbackHandler() {
        return this.callBackHandler;
    }

}
