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
package org.netbeans.modules.xml.text.syntax.javacc.lib;

/**
 * //constants used by JJ bridge
 *
 * @author  Petr Kuze;
 * @version 1.0
 */
public interface JJConstants {

    /** jj reached end of buffer. */
    public static final int JJ_EOF = -10;

    /** jj reached NL */
    public static final int JJ_EOL = -11;

    /** jj reached an internal error. e.g. invalid regexp. */
    public static final int JJ_ERR = -13;

}
