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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.explorer.*;

/**
* xxx
*
* @author Slavek Psenicka
*/

class TypeElement
{
    private String tstr, tname;

    public TypeElement(String typestr, String name)
    {
        tstr = typestr;
        tname = name;
    }

    public String getType()
    {
        return tstr;
    }

    public String getName()
    {
        return tname;
    }

    public String toString()
    {
        return tname;
    }

    @Override
    public boolean equals(Object anObject) {
        if (anObject instanceof TypeElement) {
            return tstr.equals(((TypeElement) anObject).tstr);
        }
        return false;
    }
}
