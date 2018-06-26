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

namespace TodoList\Model;

use \DateTime;
use \Exception;
use \TodoList\Validation\TodoValidator;

/**
 * Model class representing one TODO item.
 */
final class Todo {

    // priority
    const PRIORITY_HIGH = 1;
    const PRIORITY_MEDIUM = 2;
    const PRIORITY_LOW = 3;
    // status
    const STATUS_PENDING = "PENDING";
    const STATUS_DONE = "DONE";
    const STATUS_VOIDED = "VOIDED";

    /** @var int */
    private $id;
    /** @var string */
    private $priority;
    /** @var DateTime */
    private $createdOn;
    /** @var DateTime */
    private $dueOn;
    /** @var DateTime */
    private $lastModifiedOn;
    /** @var string */
    private $title;
    /** @var string */
    private $description;
    /** @var string */
    private $comment;
    /** @var string one of PENDING/COMPLETED/VOIDED */
    private $status;
    /** @var boolean */
    private $deleted;


    /**
     * Create new {@link Todo} with default properties set.
     */
    public function __construct() {
        $now = new DateTime();
        $this->setCreatedOn($now);
        $this->setLastModifiedOn($now);
        $this->setStatus(self::STATUS_PENDING);
        $this->setDeleted(false);
    }

    public static function allStatuses() {
        return [
            self::STATUS_PENDING,
            self::STATUS_DONE,
            self::STATUS_VOIDED,
        ];
    }

    public static function allPriorities() {
        return [
            self::PRIORITY_HIGH,
            self::PRIORITY_MEDIUM,
            self::PRIORITY_LOW,
        ];
    }

    //~ Getters & setters

    /**
     * @return int <i>null</i> if not persistent
     */
    public function getId() {
        return $this->id;
    }

    public function setId($id) {
        if ($this->id !== null
                && $this->id != $id) {
            throw new Exception('Cannot change identifier to ' . $id . ', already set to ' . $this->id);
        }
        if ($id === null) {
            $this->id = null;
        } else {
            $this->id = (int) $id;
        }
    }

    /**
     * @return int one of 1/2/3
     */
    public function getPriority() {
        return $this->priority;
    }

    public function setPriority($priority) {
        TodoValidator::validatePriority($priority);
        $this->priority = (int) $priority;
    }

    /**
     * @return DateTime
     */
    public function getCreatedOn() {
        return $this->createdOn;
    }

    public function setCreatedOn(DateTime $createdOn) {
        $this->createdOn = $createdOn;
    }

    /**
     * @return DateTime
     */
    public function getDueOn() {
        return $this->dueOn;
    }

    public function setDueOn(DateTime $dueOn) {
        $this->dueOn = $dueOn;
    }

    /**
     * @return DateTime
     */
    public function getLastModifiedOn() {
        return $this->lastModifiedOn;
    }

    public function setLastModifiedOn(DateTime $lastModifiedOn) {
        $this->lastModifiedOn = $lastModifiedOn;
    }

    /**
     * @return string
     */
    public function getTitle() {
        return $this->title;
    }

    public function setTitle($title) {
        $this->title = $title;
    }

    /**
     * @return string
     */
    public function getDescription() {
        return $this->description;
    }

    public function setDescription($description) {
        $this->description = $description;
    }

    /**
     * @return string
     */
    public function getComment() {
        return $this->comment;
    }

    public function setComment($comment) {
        $this->comment = $comment;
    }

    /**
     * @return string one of PENDING/DONE/VOIDED
     */
    public function getStatus() {
        return $this->status;
    }

    public function setStatus($status) {
        TodoValidator::validateStatus($status);
        $this->status = $status;
    }

    /**
     * @return boolean
     */
    public function getDeleted() {
        return $this->deleted;
    }

    public function setDeleted($deleted) {
        $this->deleted = (bool) $deleted;
    }

}
