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
package org.netbeans.modules.cloud.oracle.actions;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;

/**
 *
 * @author Jan Horvath
 */
public abstract class AbstractPasswordPanel extends JPanel {
    private static final String SPECIAL_CHARACTERS = "/!#$^?:.(){}[]~-_."; // NOI18N
    protected DialogDescriptor descriptor;
    
    protected final void setDescriptor(DialogDescriptor descriptor) {
        if (this.descriptor != null) {
            throw new IllegalStateException(
                    "DialogDescriptor has been already set."); //NOI18N
        }
        this.descriptor = descriptor;
        descriptor.setValid(false);
    }
    
    static boolean checkPasswordLogic(char[] passwd1, char[] passwd2, Consumer<String> message) {
        boolean result = false;
        if (passwd1.length < 8) {
            message.accept(Bundle.Lenght());
            return result;
        } 
        int nSpecialCharacters = 0;
        int nLetters = 0;
        int nDigits = 0;
        for (int i = 0; i < passwd1.length; i++) {
            if (Character.isLetter(passwd1[i])) {
                nLetters++;
            } else if (Character.isDigit(passwd1[i])) {
                nDigits++;
            } else if (SPECIAL_CHARACTERS.indexOf(passwd1[i]) >= 0) {
                nSpecialCharacters++;
            }
        }
        if (nLetters < 1) {
            message.accept(Bundle.OneLetter());
        } else if (nSpecialCharacters < 1) {
            message.accept(Bundle.OneSpecial());
        } else if (nDigits < 1) {
            message.accept(Bundle.OneNumber());
        } else if (!Arrays.equals(passwd1, passwd2)) {
            message.accept(Bundle.Match());
        } else {
            message.accept(null);
            result = true;
        }
        return result;
    }
    
    static char[] generatePassword() {
        Random rnd = new Random();
        char[] password = new char[12];
        for (int i = 0; i < 4; i++) {
            password[i] = (char) (65 + rnd.nextInt(25));
        }
        password[4] = SPECIAL_CHARACTERS.charAt(rnd.nextInt(SPECIAL_CHARACTERS.length()));
        for (int i = 5; i < password.length - 1; i++) {
            password[i] = (char) (97 + rnd.nextInt(25));
        }
        password[password.length - 1] = (char) (48 + rnd.nextInt(9));
        return password;
    }
    
    protected void errorMessage(String message) {
        if (message == null) {
            descriptor.getNotificationLineSupport().clearMessages();
            descriptor.setValid(true);
        } else {
            descriptor.setValid(false);
            descriptor.getNotificationLineSupport().setErrorMessage(message);
        }
    }
    
    protected abstract void checkPassword();
            
    protected class PasswordListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            checkPassword();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkPassword();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkPassword();
        }
        
    }
    
}
