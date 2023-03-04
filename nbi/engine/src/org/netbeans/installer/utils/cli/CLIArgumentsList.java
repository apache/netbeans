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

package org.netbeans.installer.utils.cli;

import java.util.Iterator;
import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Dmitry Lipin
 */
public class CLIArgumentsList implements Iterable <String>,Iterator <String> {
    private String[] arguments ;
    private int index ;
    
    public CLIArgumentsList(String [] args) {
        this.arguments = args;
        index = -1;
    }
    
    public String next() {
        index++;
        return arguments[index];
    }
    
    public boolean hasNext() {
        return (index + 1 < arguments.length);        
    }
    public int length() {
        return arguments.length;
    }
    
    public int getIndex() {
        return index;
    }
    public Iterator<String> iterator() {
        return this;
    }

    public void remove() {
        //do nothing
    }
    @Override
    public String toString() {
        return (arguments.length==0) ? StringUtils.EMPTY_STRING : 
            "[" + StringUtils.asString(arguments,"], [") + "]";//NOI18N
    }
}
