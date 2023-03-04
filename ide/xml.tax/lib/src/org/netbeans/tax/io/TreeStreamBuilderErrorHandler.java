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
package org.netbeans.tax.io;

/**
 * All errors spotted during building a Tree should be reported
 * by this interface.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public interface TreeStreamBuilderErrorHandler {

    // message types
    public static final int ERROR_WARNING     = 0;
    public static final int ERROR_ERROR       = 1;
    public static final int ERROR_FATAL_ERROR = 2;

    public static final String [] ERROR_NAME  = new String [] {
        "Warning",      // NOI18N
        "Error",        // NOI18N
        "Fatal error"   // NOI18N
    };
    
    public void message (int type, org.xml.sax.SAXParseException e);
    
}
