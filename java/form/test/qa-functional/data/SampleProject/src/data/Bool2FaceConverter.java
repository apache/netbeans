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

package data;

/**
 * Simple binding convertor from boolean values to smilies and back
 *
 * @author Jiri Vagner
 */
public class Bool2FaceConverter extends org.jdesktop.beansbinding.Converter {
    private static String TRUE_FACE = ":)";  // NOI18N
    private static String FALSE_FACE = ":(";  // NOI18N
    
    public Object convertForward(Object arg) {
        return ((Boolean) arg) ? TRUE_FACE : FALSE_FACE; 
    }

    public Object convertReverse(Object arg) {
        return ((String) arg).equals(TRUE_FACE);
    }
}
