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
package org.netbeans.libs.git.jgit;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitClientCallback;

/**
 *
 * @author kevin
 */
public class JGitCommitCredentialsProvider extends CredentialsProvider {
    private final String gpgPrivateKeyPassphase;
    private static final Logger LOG = Logger.getLogger(JGitCredentialsProvider.class.getName());

    public JGitCommitCredentialsProvider (String gpgPrivateKeyPassphase) {
        this.gpgPrivateKeyPassphase = gpgPrivateKeyPassphase;
    }
    
    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public boolean supports(CredentialItem... items) {
        for (CredentialItem i : items) {
            if (!(i instanceof CredentialItem.Username
                    || i instanceof CredentialItem.Password || i instanceof CredentialItem.InformationalMessage || i instanceof CredentialItem.CharArrayType)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean get(URIish uriish, CredentialItem... items) throws UnsupportedCredentialItem {
        String uri = uriish.toString();
        String user = uriish.getUser();
        if (user == null) {
            user = "";
        }
        String password = uriish.getPass();
        if (password == null) {
            password = "";
        }
        for (CredentialItem item : items) {
            if (item instanceof CredentialItem.InformationalMessage) {
                continue;
            }
            if (item instanceof CredentialItem.CharArrayType) {
                ((CredentialItem.CharArrayType) item).setValue(gpgPrivateKeyPassphase.toCharArray());
                continue;
            }
            
            LOG.log(Level.WARNING, "Unknown credential item: {0} - {1}:{2}", new Object[] { uri, item.getClass().getName(), item.getPromptText() });
        }
        return true;
    }
}
