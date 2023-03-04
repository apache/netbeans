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
package org.netbeans.jellytools.modules.editor;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author tester
 */
class ManageProfilesDialogOperator extends NbDialogOperator {

    public ManageProfilesDialogOperator() {
        super("Manage Keymap Profiles");
    }

    public void checkProfileListContent(String... items) {
        for (String string : items) {
            new JListOperator(this).selectItem(string);
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                // ...
            }
        }
    }

    public void Restore(String profileName) {
        System.out.println("[TEST_DEBUG] Attempting to restore profile: " + profileName);
        new JListOperator(this).selectItem(profileName);
        new JButtonOperator(this, "Restore Defaults").push();
        System.out.println("[TEST_DEBUG] Profile restored: " + profileName);
    }

    public void Delete(String profileName) {
        System.out.println("[TEST_DEBUG] Attempting to delete profile: " + profileName);
        new JListOperator(this).selectItem(profileName);
        new JButtonOperator(this, "Delete").push();
        System.out.println("[TEST_DEBUG] Profile deleted: " + profileName);
    }

    public void Duplicate(String profileNameOrig, String profileNameNew) {
            System.out.println("[TEST_DEBUG] Attempting to duplicate profile: " + profileNameOrig);
            new JListOperator(this).selectItem(profileNameOrig);
            new JButtonOperator(this, "Duplicate").push();
            NbDialogOperator confirmDupl = new NbDialogOperator("Create New Profile Dialog");
            JTextFieldOperator newName = new JTextFieldOperator(confirmDupl);
            newName.setText(profileNameNew);
            new JButtonOperator(confirmDupl, "OK").push();
            System.out.println("[TEST_DEBUG] Profile duplicated: " + profileNameOrig);
    }

    @Override
    public void ok() {
        new JButtonOperator(this, "OK").push();
    }
    
    public void close() {
      new JButtonOperator(this, "Close").push();
    }

    @Override
    public void cancel() {
        new JButtonOperator(this, "Cancel").push();
    }
}
