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

package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.antlr.Token;
import java.io.Serializable;

/**
 * interface for APT tokens
 */
public interface APTToken extends Token, Serializable {    
    public int getOffset();
    public void setOffset(int o);
    
    public int getEndOffset();
    public void setEndOffset(int o);    
    
    public int getEndColumn();
    public void setEndColumn(int c);
    
    public int getEndLine();
    public void setEndLine(int l);
    
    @Override
    public String getText();
    public CharSequence getTextID();
    public void setTextID(CharSequence id);    
    
    public Object getProperty(Object key);
}
