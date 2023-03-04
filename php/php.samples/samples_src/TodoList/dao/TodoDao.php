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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

namespace TodoList\Dao;

use \DateTime;
use \Exception;
use \PDO;
use \PDOStatement;
use \TodoList\Config\Config;
use \TodoList\Exception\NotFoundException;
use \TodoList\Mapping\TodoMapper;
use \TodoList\Model\Todo;

/**
 * DAO for {@link \TodoList\Model\Todo}.
 * <p>
 * It is also a service, ideally, this class should be divided into DAO and Service.
 */
final class TodoDao {

    /** @var PDO */
    private $db = null;


    public function __destruct() {
        // close db connection
        $this->db = null;
    }

    /**
     * Find all {@link Todo}s by search criteria.
     * @return array array of {@link Todo}s
     */
    public function find(TodoSearchCriteria $search = null) {
        $result = [];
        foreach ($this->query($this->getFindSql($search)) as $row) {
            $todo = new Todo();
            TodoMapper::map($todo, $row);
            $result[$todo->getId()] = $todo;
        }
        return $result;
    }

    /**
     * Find {@link Todo} by identifier.
     * @return Todo Todo or <i>null</i> if not found
     */
    public function findById($id) {
        $row = $this->query('SELECT * FROM todo WHERE deleted = 0 and id = ' . (int) $id)->fetch();
        if (!$row) {
            return null;
        }
        $todo = new Todo();
        TodoMapper::map($todo, $row);
        return $todo;
    }

    /**
     * Save {@link Todo}.
     * @param Todo $todo {@link Todo} to be saved
     * @return Todo saved {@link Todo} instance
     */
    public function save(Todo $todo) {
        if ($todo->getId() === null) {
            return $this->insert($todo);
        }
        return $this->update($todo);
    }

    /**
     * Delete {@link Todo} by identifier.
     * @param int $id {@link Todo} identifier
     * @return bool <i>true</i> on success, <i>false</i> otherwise
     */
    public function delete($id) {
        $sql = '
            UPDATE todo SET
                last_modified_on = :last_modified_on,
                deleted = :deleted
            WHERE
                id = :id';
        $statement = $this->getDb()->prepare($sql);
        $this->executeStatement($statement, [
            ':last_modified_on' => self::formatDateTime(new DateTime()),
            ':deleted' => true,
            ':id' => $id,
        ]);
        return $statement->rowCount() == 1;
    }

    /**
     * @return PDO
     */
    private function getDb() {
        if ($this->db !== null) {
            return $this->db;
        }
        $config = Config::getConfig('db');
        try {
            $this->db = new PDO($config['dsn'], $config['username'], $config['password']);
        } catch (Exception $ex) {
            throw new Exception('DB connection error: ' . $ex->getMessage());
        }
        return $this->db;
    }

    private function getFindSql(TodoSearchCriteria $search = null) {
        $sql = 'SELECT * FROM todo WHERE deleted = 0 ';
        $orderBy = ' priority, due_on';
        if ($search !== null) {
            if ($search->getStatus() !== null) {
                $sql .= 'AND status = ' . $this->getDb()->quote($search->getStatus());
                switch ($search->getStatus()) {
                    case Todo::STATUS_PENDING:
                        $orderBy = 'due_on, priority';
                        break;
                    case Todo::STATUS_DONE:
                    case Todo::STATUS_VOIDED:
                        $orderBy = 'due_on DESC, priority';
                        break;
                    default:
                        throw new Exception('No order for status: ' . $search->getStatus());
                }
            }
        }
        $sql .= ' ORDER BY ' . $orderBy;
        return $sql;
    }

    /**
     * @return Todo
     * @throws Exception
     */
    private function insert(Todo $todo) {
        $now = new DateTime();
        $todo->setId(null);
        $todo->setCreatedOn($now);
        $todo->setLastModifiedOn($now);
        $todo->setStatus(Todo::STATUS_PENDING);
        $sql = '
            INSERT INTO todo (id, priority, created_on, last_modified_on, due_on, title, description, comment, status, deleted)
                VALUES (:id, :priority, :created_on, :last_modified_on, :due_on, :title, :description, :comment, :status, :deleted)';
        return $this->execute($sql, $todo);
    }

    /**
     * @return Todo
     * @throws Exception
     */
    private function update(Todo $todo) {
        $todo->setLastModifiedOn(new DateTime());
        $sql = '
            UPDATE todo SET
                priority = :priority,
                last_modified_on = :last_modified_on,
                due_on = :due_on,
                title = :title,
                description = :description,
                comment = :comment,
                status = :status,
                deleted = :deleted
            WHERE
                id = :id';
        return $this->execute($sql, $todo);
    }

    /**
     * @return Todo
     * @throws Exception
     */
    private function execute($sql, Todo $todo) {
        $statement = $this->getDb()->prepare($sql);
        $this->executeStatement($statement, $this->getParams($todo));
        if (!$todo->getId()) {
            return $this->findById($this->getDb()->lastInsertId());
        }
        if (!$statement->rowCount()) {
            throw new NotFoundException('TODO with ID "' . $todo->getId() . '" does not exist.');
        }
        return $todo;
    }

    private function getParams(Todo $todo) {
        $params = [
            ':id' => $todo->getId(),
            ':priority' => $todo->getPriority(),
            ':created_on' => self::formatDateTime($todo->getCreatedOn()),
            ':last_modified_on' => self::formatDateTime($todo->getLastModifiedOn()),
            ':due_on' => self::formatDateTime($todo->getDueOn()),
            ':title' => $todo->getTitle(),
            ':description' => $todo->getDescription(),
            ':comment' => $todo->getComment(),
            ':status' => $todo->getStatus(),
            ':deleted' => self::formatBoolean($todo->getDeleted()),
        ];
        if ($todo->getId()) {
            // unset created date, this one is never updated
            unset($params[':created_on']);
        }
        return $params;
    }

    private function executeStatement(PDOStatement $statement, array $params) {
        // XXX
        //echo str_replace(array_keys($params), $params, $statement->queryString) . PHP_EOL;
        if ($statement->execute($params) === false) {
            self::throwDbError($this->getDb()->errorInfo());
        }
    }

    /**
     * @return PDOStatement
     */
    private function query($sql) {
        $statement = $this->getDb()->query($sql, PDO::FETCH_ASSOC);
        if ($statement === false) {
            self::throwDbError($this->getDb()->errorInfo());
        }
        return $statement;
    }

    private static function throwDbError(array $errorInfo) {
        // TODO log error, send email, etc.
        throw new Exception('DB error [' . $errorInfo[0] . ', ' . $errorInfo[1] . ']: ' . $errorInfo[2]);
    }

    private static function formatDateTime(DateTime $date) {
        return $date->format('Y-m-d H:i:s');
    }

    private static function formatBoolean($bool) {
        return $bool ? 1 : 0;
    }

}
