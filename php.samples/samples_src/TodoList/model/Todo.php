<?php
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
