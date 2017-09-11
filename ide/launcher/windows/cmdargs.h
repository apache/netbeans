/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2016 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
 /*
 * Author: Tomas Holy
 */

#ifndef _CMDARGS_H
#define	_CMDARGS_H

class CmdArgs {
public:

    CmdArgs(int _count) {
        used = 0;
        size = _count;
        args = new char*[size];
        memset(args, 0, size * sizeof (char*));
    }

    ~CmdArgs() {
        if (args) {
            for (int i = 0; i < size; i++) {
                delete[] args[i];
            }
            delete[] args;
        }
    }

    void add(const char *arg) {
        if (used + 1 > size) {
            int newSize = size + size / 2 + 1;
            char **newArgs = new char*[newSize];
            memcpy(newArgs, args, size * sizeof (char*));
            memset(newArgs + size, 0, (newSize - size) * sizeof (char*));
            delete[] args;
            args = newArgs;
            size = newSize;
        }
        args[used] = new char[strlen(arg) + 1];
        strcpy(args[used++], arg);
    }

    void addCmdLine(const char *cmdLine) {
        char arg[1024] = "";
        bool inQuotes = false;
        bool inText = false;
        int i = 0;
        int j = 0;
        char c;
        while (c = cmdLine[i]) {
            if (inQuotes) {
                if (c == '\"') {
                    inQuotes = false;
                } else {
                    arg[j++] = c;
                }
            } else {
                switch (c) {
                    case '\"':
                        inQuotes = true;
                        inText = true;
                        break;
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        if (inText) {
                            arg[j] = '\0';
                            add(arg);
                            j = 0;
                        }
                        inText = false;
                        break;
                    default:
                        inText = true;
                        arg[j++] = c;
                        break;
                }
            }
            i++;
        }
        if (j > 0) {
            arg[j] = '\0';
            add(arg);
        }
    }

    int getCount() {
        return used;
    }

    char **getArgs() {
        return args;
    }

private:
    int used;
    int size;
    char **args;
};

#endif	/* _CMDARGS_H */

