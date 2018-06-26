<?php
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

namespace TodoList;

use \DateTime;
use \TodoList\Dao\TodoDao;
use \TodoList\Flash\Flash;
use \TodoList\Mapping\TodoMapper;
use \TodoList\Model\Todo;
use \TodoList\Util\Utils;
use \TodoList\Validation\TodoValidator;

$errors = [];
$todo = null;
$edit = array_key_exists('id', $_GET);
if ($edit) {
    $todo = Utils::getTodoByGetId();
} else {
    // set defaults
    $todo = new Todo();
    $todo->setPriority(Todo::PRIORITY_MEDIUM);
    $dueOn = new DateTime("+1 day");
    $dueOn->setTime(0, 0, 0);
    $todo->setDueOn($dueOn);
}

if (array_key_exists('cancel', $_POST)) {
    // redirect
    Utils::redirect('detail', ['id' => $todo->getId()]);
} elseif (array_key_exists('save', $_POST)) {
    // for security reasons, do not map the whole $_POST['todo']
    $data = [
        'title' => $_POST['todo']['title'],
        'due_on' => $_POST['todo']['due_on_date'] . ' ' . $_POST['todo']['due_on_hour'] . ':' . $_POST['todo']['due_on_minute'] . ':00',
        'priority' => $_POST['todo']['priority'],
        'description' => $_POST['todo']['description'],
        'comment' => $_POST['todo']['comment'],
    ];
        ;
    // map
    TodoMapper::map($todo, $data);
    // validate
    $errors = TodoValidator::validate($todo);
    // validate
    if (empty($errors)) {
        // save
        $dao = new TodoDao();
        $todo = $dao->save($todo);
        Flash::addFlash('TODO saved successfully.');
        // redirect
        Utils::redirect('detail', ['id' => $todo->getId()]);
    }
}
