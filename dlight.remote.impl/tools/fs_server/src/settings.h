/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 */

#ifndef SETTINGS_H
#define SETTINGS_H

#ifdef __cplusplus
extern "C" {
#endif

/// To lower concurrency between threads
/// settings use copy-on-write pattern.
/// To change settings:
///     create a new instance on the heap, fill it, then
///     under settings_mutex:
///         store previous instance in prev field of new one
///         change settings variable
/// To use settings:
///     under settings_mutex make a shallow copy,
///     use without mutex
/// Settings are freed only on shutdown.
/// settings pointer is never null, set it to default_settings instead of null

typedef struct {
    /// directories that are forbidden to stat; the last element is null
    const char** dirs_forbidden_to_stat;
    /// true if full access check via access() function is needed
    bool full_access_check;
    /// points to previous instance
    void* prev;
} settings_str;

void clone_global_settings(settings_str* dst);
void change_settings(const char** dirs_forbidden_to_stat, bool *full_access_check);
void free_settings();
bool is_dir_forbidden_to_stat(const char* path2check, const settings_str* settings);
void set_dirs_forbidden_to_stat(const char* dir_list);

#ifdef __cplusplus
}
#endif

#endif /* SETTINGS_H */

