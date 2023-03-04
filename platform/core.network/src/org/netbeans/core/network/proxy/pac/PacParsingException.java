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
 * Thrown when there are syntactical errors in the PAC script.
 * 
 * @see PacValidationException
 * 
 * @author lbruun
 */
public class PacParsingException extends Exception {

   public PacParsingException(Exception ex) {
        super(ex);
    }
     
   public PacParsingException(String message, Exception ex) {
        super(message, ex);
    }
   
   public PacParsingException(String message) {
        super(message);
    }
}
