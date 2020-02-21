/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#define MY_MAX_PATH (1024)
#ifdef MINGW
#include <process.h>
#else
#include <spawn.h>
#include <sys/wait.h>
#endif
#include <unistd.h>
#include <limits.h>
#ifdef WINDOWS
#define PATH_SEPARATOR ";"
#define FILE_SEPARATOR_CHAR '\\'
#define FILE_SEPARATOR_STRING "\\"
#else
#define PATH_SEPARATOR ":"
#define FILE_SEPARATOR_CHAR '/'
#define FILE_SEPARATOR_STRING "/"
#endif
#define MAGIC "echo magic"
extern char **environ;
#define COPY(x) x x x x x x x x x x
char *real_binary = COPY(COPY(MAGIC));

void prependPath(char* path) {
    char** e = environ;
    while (e) {
        char *buf = malloc(strlen(*e) + 1);
        strcpy(buf, *e);
        char* eq = strchr(buf, '=');
        if (eq != NULL) {
            *eq = 0;
            if (strcasecmp("PATH", buf) == 0) {
                char* old_path = getenv(buf);
                char *tool_path = malloc(strlen(path) + 1);
                strcpy(tool_path, path);
                char* key;
                key = strrchr(tool_path, FILE_SEPARATOR_CHAR);
                if (key != NULL) {
                    *key = 0;
                }
                char *new_path = malloc(strlen(tool_path) + 1 + strlen(old_path) + 1);
#ifdef WINDOWS
                sprintf(new_path, "%s;%s", tool_path, old_path); 
#else
                sprintf(new_path, "%s:%s", tool_path, old_path); 
#endif
#ifdef MINGW
                char *path_macro = malloc(strlen(buf) + 1 + strlen(new_path) + 1);
                sprintf(path_macro, "%s=%s", buf, new_path); 
                putenv(path_macro);
                free(path_macro);
#else
                setenv(buf, new_path, 1);
#endif
                free(new_path);
                free(tool_path);
                free(buf);
                break;
            }
        }
        free(buf);
        e++;
    }
}

int main(int argc, char**argv) {
    char *pattern = MAGIC;
    char *place = real_binary;
    int changed = 0;
    while(*pattern) {
        if (*place != *pattern) {
            changed = 1;
            break;
        }
        pattern++;
        place++;
    }
    if (!changed) {
        printf("Real compiler is not set\n");
        return -1;
    }

    argv[0] = real_binary;
    char* log = getenv("__CND_BUILD_LOG__");
    if (log != NULL) {
        FILE* flog = fopen(log, "a");
        if (flog != NULL) {
            fprintf(flog, "called: %s\n", real_binary);
            char *buf = malloc(MY_MAX_PATH + 1);
            getcwd(buf, MY_MAX_PATH);
            fprintf(flog, "\t%s\n", buf);
            char** par = (char**) argv;
            for (; *par != 0; par++) {
                fprintf(flog, "\t%s\n", *par);
            }
            fprintf(flog, "\n");
            fflush(flog);
            fclose(flog);
        }
    }
    prependPath(real_binary);
#ifdef MINGW
    // It seems MinGW merge arguments in command line
    // Wrap each argument in quote.
    char** par = (char**) argv;
    int len = 0;
    for (; *par != 0; par++) {
        len += 3;
        char *i = *par;
        while(*i) {
            if (*i == '"') {
                len++;
            }
            len++;
            i++;
        }
    }
    char *newpar = malloc(len);
    char *line = newpar;
    par = (char**) argv;
    for (; *par != 0; par++) {
        *newpar++ = '"';
        char *i = *par;
        while(*i) {
            if (*i == '"') {
                *newpar++ = '\\';
            }
            *newpar++ = *i++;
        }
        *newpar++ = '"';
        *newpar++ = ' ';
    }
    *--newpar = 0;
    argv[0] = line;
    argv[1] = 0;
    
    return spawnv(P_WAIT, real_binary, argv);
#else
    pid_t pid;
    int status;
    status = posix_spawn(&pid, tool, NULL, NULL, argv, environ);
    if (status == 0) {
        if (waitpid(pid, &status, 0) != -1) {
            return status;
        } else {
            return -1;
        }
    } else {
        return status;
    }
#endif
}
