/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git;

/**
 * Used as a callback to acquire user's credentials and ask caller about 
 * different questions during an inter-repository commands, e.g. fetch, push, clone.
 * If an API client runs a git command that accesses a remote repository and the repository
 * requires authentication then this is the class it should use to pass the credentials.
 * <h5>How to use this class</h5>
 * <ol>
 * <li>Get an instance of {@link GitClient} you want to run the fetch command with, see {@link GitRepository}</li>
 * <li>Extend this class and implement all abstract methods</li>
 * <li>Pass the instance of the class to the git client, see {@link GitClient#setCallback(org.netbeans.libs.git.GitClientCallback) }</li>
 * <li>Run a remote command, see e.g. {@link GitClient#fetch(java.lang.String, org.netbeans.libs.git.progress.ProgressMonitor) }</li>
 * <li>While the fetch command is running, methods <code>getUsername</code> and <code>getPassword</code> will be called from within the client
 * so make sure they return the correct credentials</li>
 * </ol>
 * Let's assume we want to run a fetch command on a remote repository at http://myrepositoryhost/path 
 * that requires credentials username/password:
 * <pre>
 * GitClient client = {@link GitRepository#getInstance(java.io.File) GitRepository.getInstance(myLocalReposiry)}.{@link GitRepository#createClient() createClient()};
 * GitClientCallback myCallback = {@link GitClientCallback#GitClientCallback() new GitClientCallback ()} {
 *     public String askQuestion (String uri, String prompt) { return null; }
 *     
 *     public String getUsername (String uri, String prompt) {
 *         return "username";
 *     }
 *     
 *     public char[] getPassword (String uri, String prompt) {
 *         return "password".toCharArray();
 *     }
 *         
 *     public char[] getPassphrase (String uri, String prompt) { return null; }
 * 
 *     public String getIdentityFile (String uri, String prompt);
 *     public Boolean askYesNoQuestion (String uri, String prompt);
 * };
 * client.{@link GitClient#setCallback(org.netbeans.libs.git.GitClientCallback) setCallback(myCallback)};
 * List<String> refspecs = Arrays.asList("refs/heads/*:refs/remotes/origin/*");
 * client.fetch("http://myrepositoryhost/path", refspecs, pm);
 * </pre>
 * 
 * <p>Also note that returning <code>null</code> from the implemented methods means that you want to cancel the authentication attempt.</p>
 * @author Ondra Vrabec
 */
public abstract class GitClientCallback {
    
    /**
     * Through this method you are asked a question you should answer.
     * You can implement this by raising a dialog asking a user a question and return his answer.
     * @param uri URI of a host you are trying to connect to.
     * @param prompt a question asked by the system that needs answering.
     * @return an answer to the given prompt or <code>null</code> if the authentication attempt should be halted.
     */
    public abstract String askQuestion (String uri, String prompt);
    
    /**
     * You should implement this to pass a username required by the authentication process.
     * @param uri URI of a host you are trying to connect to.
     * @param prompt explanation of what is expected as the return value
     * @return username or <code>null</code> if the authentication attempt should be halted.
     */
    public abstract String getUsername (String uri, String prompt);
    
    /**
     * Implement this to pass the user's password to the authentication process.
     * @param uri URI of a host you are trying to connect to.
     * @param prompt explanation of what is expected as the return value
     * @return password or <code>null</code> if the authentication attempt should be halted.
     */
    public abstract char[] getPassword (String uri, String prompt);
    
    /**
     * Implement this to pass the passphrase to unlock the private key.
     * @param uri URI of a host you are trying to connect to.
     * @param prompt explanation of what is expected as the return value
     * @return passphrase to unlock the private key or <code>null</code> if the authentication attempt should be halted.
     * @see #getIdentityFile(java.lang.String, java.lang.String) 
     */
    public abstract char[] getPassphrase (String uri, String prompt);
    
    /**
     * If the authentication should be done via a private/public key pair instead of usual username/password,
     * implement this method and return the absolute path to the file with the private key.
     * @param uri URI of a host you are trying to connect to.
     * @param prompt explanation of what is expected as the return value
     * @return absolute path to the identity file with the private key 
     *         or <code>null</code> if the authentication attempt should be halted.
     */
    public abstract String getIdentityFile (String uri, String prompt);
    
    /**
     * Through this method you are asked a question you should answer Yes or No.
     * You can implement this by raising a dialog asking a user a question and return his answer.
     * @param uri URI of a host you are trying to connect to.
     * @param prompt explanation of what is expected as the return value
     * @return reply to the answer or <code>null</code> if the authentication attempt should be halted.
     */
    public abstract Boolean askYesNoQuestion (String uri, String prompt);

}
