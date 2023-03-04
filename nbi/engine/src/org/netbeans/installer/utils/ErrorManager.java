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

package org.netbeans.installer.utils;

import java.lang.Thread.UncaughtExceptionHandler;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.FinishHandler;

/**
 *
 * @author Kirill Sorokin
 */
public final class ErrorManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static UncaughtExceptionHandler exceptionHandler;
    private static FinishHandler finishHandler;
    
    public static synchronized void notifyDebug(String message) {
        notify(ErrorLevel.DEBUG, message);
    }
    
    public static synchronized void notifyDebug(String message, Throwable e) {
        notify(ErrorLevel.DEBUG, message, e);
    }
    
    public static synchronized void notify(String message) {
        notify(ErrorLevel.MESSAGE, message);
    }
    
    public static synchronized void notify(String message, Throwable e) {
        notify(ErrorLevel.MESSAGE, message, e);
    }
    
    public static synchronized void notifyWarning(String message) {
        notify(ErrorLevel.WARNING, message);
    }
    
    public static synchronized void notifyWarning(String message, Throwable e) {
        notify(ErrorLevel.WARNING, message, e);
    }
    
    public static synchronized void notifyError(String message) {
        notify(ErrorLevel.ERROR, message);
    }
    
    public static synchronized void notifyError(String message, Throwable e) {
        notify(ErrorLevel.ERROR, message, e);
    }
    
    public static synchronized void notifyCritical(String message) {
        notify(ErrorLevel.CRITICAL, message);
    }
    
    public static synchronized void notifyCritical(String message, Throwable e) {
        notify(ErrorLevel.CRITICAL, message, e);
    }
    
    public static synchronized void notify(int level, String message) {
        notify(level, message, null);
    }
    
    public static synchronized void notify(int level, Throwable exception) {
        notify(level, null, exception);
    }
    
    public static synchronized void notify(int level, String message, Throwable exception) {
        // parameters validation
        assert (message != null) || (exception != null);
        
        String dialogText = StringUtils.EMPTY_STRING;
        
        if (message != null) {
            LogManager.log(level, message);
            dialogText = message;
        } else {
            dialogText = ResourceUtils.getString(ErrorManager.class, 
                ERROR_UNEXPECTED_ERROR_KEY);
        }
        if (exception != null) {
            LogManager.log(level, exception);
            dialogText += ResourceUtils.getString(ErrorManager.class, 
                    ERROR_EXCEPTION_MESSAGE_KEY,
                    exception.getClass().getName(),
                    exception.getMessage());
        }
        if(LogManager.getLogFile()!=null) {
            dialogText += ResourceUtils.getString(ErrorManager.class,
                    ERROR_LOGFILE_INFO_KEY,
                    LogManager.getLogFile().getAbsolutePath());
        }
        String titleKey = null;
        UiUtils.MessageType type;

        switch (level) {
            case ErrorLevel.MESSAGE:
                titleKey = ERROR_MESSAGE_KEY;
                type = UiUtils.MessageType.INFORMATION;
                break;
            case ErrorLevel.WARNING:
                titleKey = ERROR_WARNING_KEY;
                type = UiUtils.MessageType.WARNING;
                break;
            case ErrorLevel.ERROR:
                dialogText += ResourceUtils.getString(ErrorManager.class, ERROR_EXCEPTION_CONTINUE_QUESTION_KEY);
                titleKey = ERROR_ERROR_KEY;
                type = UiUtils.MessageType.ERROR;
                break;
            case ErrorLevel.CRITICAL:
                titleKey = ERROR_CRITICAL_KEY;
                type = UiUtils.MessageType.CRITICAL;
                break;
            default:
                return;
        }
        String title = ResourceUtils.getString(ErrorManager.class, titleKey); 
        if(title == null) {
            title = type.toString();
        }
        
        boolean exitInstaller = UiUtils.showMessageDialog(dialogText, title, type);
                
        if(exitInstaller || type.equals(UiUtils.MessageType.CRITICAL)) {
            finishHandler.criticalExit();
        }
    }
    
    public static UncaughtExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }
    
    public static void setExceptionHandler(final UncaughtExceptionHandler exceptionHandler) {
        ErrorManager.exceptionHandler = exceptionHandler;
    }
    
    public static FinishHandler getFinishHandler() {
        return finishHandler;
    }
    
    public static void setFinishHandler(final FinishHandler finishHandler) {
        ErrorManager.finishHandler = finishHandler;
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private ErrorManager() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class ExceptionHandler implements UncaughtExceptionHandler {
        public void uncaughtException(
                final Thread thread,
                final Throwable exception) {
            ErrorManager.notifyCritical(
                    ResourceUtils.getString(ErrorManager.class,
                    ERROR_UNEXPECTED_EXCEPTION_KEY,thread.getName()),
                    exception);
        }
    }
    private static final String ERROR_UNEXPECTED_EXCEPTION_KEY =
            "EM.ununexpected.exception";//NOI18N
    private static final String ERROR_CRITICAL_KEY =
            "EM.errortype.critical";//NOI18N
    private static final String ERROR_WARNING_KEY =
            "EM.errortype.warning";//NOI18N
    private static final String ERROR_ERROR_KEY =
            "EM.errortype.error";//NOI18N
    private static final String ERROR_MESSAGE_KEY =
            "EM.errortype.message";//NOI18N
    private static final String ERROR_UNEXPECTED_ERROR_KEY = 
            "EM.unexpected.error";//NOI18N
    private static final String ERROR_EXCEPTION_MESSAGE_KEY =
            "EM.exception.message";//NOI18N
    private static final String ERROR_LOGFILE_INFO_KEY =
            "EM.exception.logfile.info";//NOI18N
    private static final String ERROR_EXCEPTION_CONTINUE_QUESTION_KEY =
            "EM.exception.continue.question";//NOI18N
}
