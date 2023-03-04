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
package org.openide.awt;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import static javax.swing.Action.ACTION_COMMAND_KEY;
import org.netbeans.api.actions.Closable;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.actions.Printable;
import org.netbeans.api.actions.Viewable;
import org.openide.util.Lookup.Provider;

final class ActionDefaultPerfomer extends ContextAction.Performer<Object> {

    final int type;

    public ActionDefaultPerfomer(int type) {
        super(Collections.emptyMap());
        this.type = type;
    }

    @Override
    public void actionPerformed(ActionEvent ev, List<? extends Object> data, Provider everything) {
        for (Object o : data) {
            switch (type) {
                case 0:
                    ((Openable) o).open();
                    break;
                case 1:
                    ((Viewable) o).view();
                    break;
                case 2:
                    ((Editable) o).edit();
                    break;
                case 3:
                    ((Closable) o).close();
                    break;
                case 4:
                    ((Printable) o).print();
                    break;
                default:
                    assert false : "Wrong type: " + type;
            }
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Object o = delegate.get("key"); // NOI18N
        if (o == null) {
            o = delegate.get(ACTION_COMMAND_KEY);
        }
        Object d= instDelegate == null ? null : instDelegate.get();
        sb.append("PerformerDefault{id = ").append(Objects.toString(o))
                .append(", type = ").append(type)
                .append("}");
        return sb.toString();
    }
}
