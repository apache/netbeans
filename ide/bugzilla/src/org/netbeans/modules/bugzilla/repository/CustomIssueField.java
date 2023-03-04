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

package org.netbeans.modules.bugzilla.repository;

import java.util.List;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCustomField;

/**
 * Information about a custom issue field.
 *
 * @author Jan Stola
 */
public class CustomIssueField extends IssueField {
    public enum Type {DateTime, DropDown, FreeText, LargeText, MultipleSelection, Unknown};
    private BugzillaCustomField field;

    CustomIssueField(BugzillaCustomField field) {
        super(field.getName(), null);
        this.field = field;
    }

    @Override
    public String getDisplayName() {
        return field.getDescription();
    }

    public Type getType() {
        Type type = Type.Unknown;
        switch (field.getFieldType()) {
            case DateTime: type = Type.DateTime; break;
            case DropDown: type = Type.DropDown; break;
            case FreeText: type = Type.FreeText; break;
            case LargeText: type = Type.LargeText; break;
            case MultipleSelection: type = Type.MultipleSelection; break;
        }
        return type;
    }

    public List<String> getOptions() {
        return field.getOptions();
    }

    public boolean getShowOnBugCreation() {
        return field.isEnterBug();
    }

}
