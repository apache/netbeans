/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.core.network.proxy.pac;



/**
 * Stub for PAC Helpers. 
 * 
 * <p>
 * The 'Helpers' are utility functions that the PAC script can make use of.
 * 
 * @author lbruun
 */
public abstract class PacHelperMethods implements PacHelperMethodsNetscape, PacHelperMethodsMicrosoft {
 
    
    /**
     * Writes something to log or elsewhere. This allows the PAC script to do
     * simple logging for debugging purpose. It overwrites the standard
     * JavaScript {@code alert()} function.
     *
     * <p>
     * Note that the JavaScript {@code alert()} function should not be used in
     * a production PAC script. It is only intended for debugging and
     * unit test purpose.
     *
     * <p>
     * The default implementation simply writes to stderr.
     *
     * @param message text to log 
     */
    public void alert(String message) {
        System.err.println(message);
    }

}
