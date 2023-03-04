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

package org.netbeans.modules.parsing.impl;

import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class ParserAccessor {
    
    private static volatile ParserAccessor instance;
    
    public static synchronized ParserAccessor getINSTANCE () {
        if (instance == null) {
            try {
                Class.forName(Parser.class.getName(),true,ParserAccessor.class.getClassLoader());
                assert instance != null;
            } catch (final ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return instance;
    }
    
    public static void setINSTANCE (final ParserAccessor _instance) {
        assert _instance != null;
        instance = _instance;
    }
    
    public abstract void invalidate (Parser.Result result);    
    public abstract boolean processingFinished (Parser.Result result);

}
