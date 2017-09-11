/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

#include "ProcessUtils.h"
#include "StringUtils.h"
#include "FileUtils.h"


const DWORD DEFAULT_PROCESS_TIMEOUT = 30000; //30 sec

DWORD readBuf(HANDLE hRead, WCHAR * buf, DWORD * bytesRead, HANDLE hWrite) {
    ReadFile(hRead, buf, STREAM_BUF_LENGTH - 1, bytesRead, NULL);
    
    if((*bytesRead)>0 && hWrite!=INVALID_HANDLE_VALUE) {
        DWORD bytesWritten = 0;
        WriteFile(hWrite, buf, (*bytesRead), &bytesWritten, 0);
    }
    ZERO(buf, sizeof(buf));
    return 0;
}

DWORD readNextData(HANDLE hRead, WCHAR * buf, HANDLE hWrite) {
    DWORD bytesRead;
    DWORD bytesAvailable;
    ZERO(buf, sizeof(buf));
    
    PeekNamedPipe(hRead, buf, STREAM_BUF_LENGTH - 1, &bytesRead, &bytesAvailable, NULL);
    if (bytesRead != 0) {
        ZERO(buf, sizeof(buf));
        if (bytesAvailable >= STREAM_BUF_LENGTH) {
            while (bytesRead >= STREAM_BUF_LENGTH-1) {
                readBuf(hRead, buf, &bytesRead, hWrite);
            }
        }
        else {
            readBuf(hRead, buf, &bytesRead, hWrite);
        }
        return bytesRead;
    }
    return 0;
}

// get already running process stdout
DWORD readProcessStream(PROCESS_INFORMATION pi, HANDLE currentProcessStdin, HANDLE currentProcessStdout, HANDLE currentProcessStderr, DWORD timeOut, HANDLE hWriteInput, HANDLE hWriteOutput, HANDLE hWriteError) {
    DWORD started = GetTickCount();
    WCHAR buf[STREAM_BUF_LENGTH];
    DWORD exitCode=0;    
    DWORD outRead =0;
    DWORD errRead =0;
    DWORD inRead =0;
    while(1) {
        outRead = readNextData(currentProcessStdout, buf, hWriteOutput);
        errRead = readNextData(currentProcessStderr, buf, hWriteError);
        inRead  = readNextData(hWriteInput, buf, currentProcessStdin);
        GetExitCodeProcess(pi.hProcess, &exitCode);
        if (exitCode != STILL_ACTIVE) break;
        
        if(outRead == 0 && errRead==0 && inRead==0 && timeOut!=INFINITE) {
            if((GetTickCount() - started) > timeOut) break;
        }
        //avoid extra using of CPU resources
        Sleep(1);
    }
    return exitCode;
}
char * readHandle(HANDLE hRead) {
    char * output = NULL;
    char * buf = newpChar(STREAM_BUF_LENGTH);
    DWORD total = 0;
    DWORD read;
    DWORD bytesRead;
    DWORD bytesAvailable;
    
    while(1) {
        PeekNamedPipe(hRead, buf, STREAM_BUF_LENGTH - 1, &bytesRead, &bytesAvailable, NULL);
        if(bytesAvailable==0) break;
        ReadFile(hRead, buf, STREAM_BUF_LENGTH - 1, &read, NULL);
        if(read==0) break;
        output = appendStringN(output, total, buf, read);
        total+=read;
    }
    FREE(buf);
    return output;
}




// run process and get its standart output
// command - executing command
// timeLimitMillis - timeout of the process running without any output
// dir - working directory
// return ERROR_ON_EXECUTE_PROCESS for serios error
// return ERROR_PROCESS_TIMEOUT for timeout

void executeCommand(LauncherProperties * props, WCHAR * command, WCHAR * dir, DWORD timeLimitMillis, HANDLE hWriteOutput, HANDLE hWriteError, DWORD priority) {
    STARTUPINFOW si;
    SECURITY_ATTRIBUTES sa;
    SECURITY_DESCRIPTOR sd;
    PROCESS_INFORMATION pi;
    
    HANDLE newProcessInput;
    HANDLE newProcessOutput;
    HANDLE newProcessError;
    
    HANDLE currentProcessStdout;
    HANDLE currentProcessStdin;
    HANDLE currentProcessStderr;
    
    WCHAR * directory;
    
    InitializeSecurityDescriptor(&sd, SECURITY_DESCRIPTOR_REVISION);
    SetSecurityDescriptorDacl(&sd, TRUE, NULL, FALSE);
    sa.lpSecurityDescriptor = &sd;
    sa.nLength = sizeof(SECURITY_ATTRIBUTES);
    sa.bInheritHandle = TRUE;
    
    
    if (!CreatePipe(&newProcessInput, &currentProcessStdin, &sa, 0)) {
        writeErrorA(props, OUTPUT_LEVEL_NORMAL, 1, "Can`t create pipe for input. ", NULL , GetLastError());
        props->status = ERROR_ON_EXECUTE_PROCESS;
        return;
    }
    
    if (!CreatePipe(&currentProcessStdout, &newProcessOutput, &sa, 0)) {
        writeErrorA(props, OUTPUT_LEVEL_NORMAL, 1, "Can`t create pipe for output. ", NULL , GetLastError());
        CloseHandle(newProcessInput);
        CloseHandle(currentProcessStdin);
        props->status = ERROR_ON_EXECUTE_PROCESS;
        return;
    }
    
    if (!CreatePipe(&currentProcessStderr, &newProcessError, &sa, 0)) {
        writeErrorA(props, OUTPUT_LEVEL_NORMAL, 1, "Can`t create pipe for error. ", NULL , GetLastError());
        CloseHandle(newProcessInput);
        CloseHandle(currentProcessStdin);
        CloseHandle(newProcessOutput);
        CloseHandle(currentProcessStdout);
        props->status = ERROR_ON_EXECUTE_PROCESS;
        return;
    }
    
    
    GetStartupInfoW(&si);
    
    si.dwFlags = STARTF_USESTDHANDLES|STARTF_USESHOWWINDOW;
    si.wShowWindow = SW_HIDE;
    si.hStdOutput = newProcessOutput;
    si.hStdError = newProcessError;
    si.hStdInput = newProcessInput;
    
    directory = (dir!=NULL) ? dir : getCurrentDirectory();
    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "Create new process: ", 1);
    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "          command : ", 0);
    writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, command, 1);
    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "        directory : ", 0);
    writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, directory, 1);
    
    props->exitCode = ERROR_OK;
    if (CreateProcessW(NULL, command, NULL, NULL, TRUE,
    CREATE_NEW_CONSOLE | CREATE_NO_WINDOW | CREATE_DEFAULT_ERROR_MODE | priority,
    NULL, directory, &si, &pi)) {        
        // TODO
        // Check whether volder virtualization can brake things and provide method to disable it if necessary 
        // I am not sure whether we need it off or on.
        // http://www.netbeans.org/issues/show_bug.cgi?id=122186
        DWORD timeOut = ((timeLimitMillis<=0) ? DEFAULT_PROCESS_TIMEOUT: timeLimitMillis);
        props->status = ERROR_OK;
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... process created", 1);
        
        props->exitCode = readProcessStream(pi, currentProcessStdin, currentProcessStdout, currentProcessStderr, timeOut, newProcessInput, hWriteOutput, hWriteError);
        
        if(props->exitCode==STILL_ACTIVE) {
            //actually we have reached the timeout of the process and need to terminate it
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "... process is timeouted", 1);
            GetExitCodeProcess(pi.hProcess, & (props->exitCode));
            
            if(props->exitCode==STILL_ACTIVE) {
                TerminateProcess(pi.hProcess, 0);
                writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "... terminate process", 1);
                //Terminating process...It worked too much without any stdout/stdin/stderr
                props->status = ERROR_PROCESS_TIMEOUT;//terminated by timeout                
            }
        } else {
            //application finished its work... succesfully or not - it doesn`t matter            
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... process finished his work", 1);
        }
        CloseHandle(pi.hThread);
        CloseHandle(pi.hProcess);
    }  else {
        writeErrorA(props, OUTPUT_LEVEL_DEBUG, 1, "... can`t create process.", NULL, GetLastError());
        props->status = ERROR_ON_EXECUTE_PROCESS;        
    }
    
    
    CloseHandle(newProcessInput);
    CloseHandle(newProcessOutput);
    CloseHandle(newProcessError);
    CloseHandle(currentProcessStdin);
    CloseHandle(currentProcessStdout);
    CloseHandle(currentProcessStderr);    
}



