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

namespace TodoList\Validation;

use \TodoList\Exception\NotFoundException;
use \TodoList\Model\Todo;

/**
 * Validator for {@link \TodoList\Model\Todo}.
 * @see \TodoList\Mapping\TodoMapper
 */
final class TodoValidator {

    private function __construct() {
    }

    /**
     * Validate the given {@link Todo} instance.
     * @param Todo $todo {@link Todo} instance to be validated
     * @return array array of {@link Error} s
     */
    public static function validate(Todo $todo) {
        $errors = [];
        if (!$todo->getCreatedOn()) {
            $errors[] = new \TodoList\Validation\ValidationError('createdOn', 'Empty or invalid Created On.');
        }
        if (!$todo->getLastModifiedOn()) {
            $errors[] = new \TodoList\Validation\ValidationError('lastModifiedOn', 'Empty or invalid Last Modified On.');
        }
        if (!trim($todo->getTitle())) {
            $errors[] = new \TodoList\Validation\ValidationError('title', 'Title cannot be empty.');
        }
        if (!$todo->getDueOn()) {
            $errors[] = new \TodoList\Validation\ValidationError('dueOn', 'Empty or invalid Due On.');
        }
        if (!trim($todo->getPriority())) {
            $errors[] = new \TodoList\Validation\ValidationError('priority', 'Priority cannot be empty.');
        } elseif (!self::isValidPriority($todo->getPriority())) {
            $errors[] = new \TodoList\Validation\ValidationError('priority', 'Invalid Priority set.');
        }
        if (!trim($todo->getStatus())) {
            $errors[] = new \TodoList\Validation\ValidationError('status', 'Status cannot be empty.');
        } elseif (!self::isValidStatus($todo->getStatus())) {
            $errors[] = new \TodoList\Validation\ValidationError('status', 'Invalid Status set.');
        }
        return $errors;
    }

    /**
     * Validate the given status.
     * @param string $status status to be validated
     * @throws NotFoundException if the status is not known
     */
    public static function validateStatus($status) {
        if (!self::isValidStatus($status)) {
            throw new NotFoundException('Unknown status: ' . $status);
        }
    }

    /**
     * Validate the given priority.
     * @param int $priority priority to be validated
     * @throws NotFoundException if the priority is not known
     */
    public static function validatePriority($priority) {
        if (!self::isValidPriority($priority)) {
            throw new NotFoundException('Unknown priority: ' . $priority);
        }
    }

    private static function isValidStatus($status) {
        return in_array($status, Todo::allStatuses());
    }

    private static function isValidPriority($priority) {
        return in_array($priority, Todo::allPriorities());
    }

}
