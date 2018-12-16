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

namespace TodoList\Util;

use DateTime;
use Exception;
use TodoList\Dao\TodoDao;
use TodoList\Exception\NotFoundException;
use TodoList\Model\Todo;
use TodoList\Validation\TodoValidator;

/**
 * Miscellaneous utility methods.
 */
final class Utils {
    
    private static $STATUS_ICONS = [
        Todo::STATUS_PENDING => 'event_note',
        Todo::STATUS_DONE => 'event_available',
        Todo::STATUS_VOIDED => 'event_busy',
    ];

    private function __construct() {
    }

    /**
     * Generate link.
     * @param string $page target page
     * @param array $params page parameters
     */
    public static function createLink($page, array $params = []) {
        unset($params['page']);
        return 'index.php?' .http_build_query(array_merge(['page' => $page], $params));
    }

    /**
     * Format date.
     * @param DateTime $date date to be formatted
     * @return string formatted date
     */
    public static function formatDate(DateTime $date = null) {
        if ($date === null) {
            return '';
        }
        return $date->format('m/d/Y');
    }

    /**
     * Format date and time.
     * @param DateTime $date date to be formatted
     * @return string formatted date and time
     */
    public static function formatDateTime(DateTime $date = null) {
        if ($date === null) {
            return '';
        }
        return $date->format('m/d/Y H:i');
    }

    /**
     * Returns icon for status.
     * @param int $status status
     * @param boolean $disabled whether to disable (change color)
     * @param boolean $tooltip whether to show tooltip
     * @return string icon for status
     */
    public static function iconStatus($status, $disabled = false, $tooltip = true) {
        TodoValidator::validateStatus($status);
        $title = $tooltip ?  : '';
        $icon = '<i class="material-icons ' . ($disabled ? 'disabled' : strtolower($status)) . '"';
        if ($tooltip) {
            $icon .= ' title="' . self::capitalize($status) . '"';
        }
        $icon .= '>' . self::$STATUS_ICONS[$status] . '</i>';
        return $icon;
    }

    /**
     * Returns icon for priority.
     * @param int $priority priority
     * @return string icon for priority
     */
    public static function iconPriority($priority) {
        return str_repeat(
                '<i class="material-icons multi priority" title="Priority ' . $priority . '">star</i>',
                4 - $priority);
    }
    
    /**
     * Redirect to the given page.
     * @param type $page target page
     * @param array $params page parameters
     */
    public static function redirect($page, array $params = []) {
        header('Location: ' . self::createLink($page, $params));
        die();
    }

    /**
     * Get value of the URL param.
     * @return string parameter value
     * @throws NotFoundException if the param is not found in the URL
     */
    public static function getUrlParam($name) {
        if (!array_key_exists($name, $_GET)) {
            throw new NotFoundException('URL parameter "' . $name . '" not found.');
        }
        return $_GET[$name];
    }

    /**
     * Get {@link Todo} by the identifier 'id' found in the URL.
     * @return Todo {@link Todo} instance
     * @throws NotFoundException if the param or {@link Todo} instance is not found
     */
    public static function getTodoByGetId() {
        $id = null;
        try {
            $id = self::getUrlParam('id');
        } catch (Exception $ex) {
            throw new NotFoundException('No TODO identifier provided.');
        }
        if (!is_numeric($id)) {
            throw new NotFoundException('Invalid TODO identifier provided.');
        }
        $dao = new TodoDao();
        $todo = $dao->findById($id);
        if ($todo === null) {
            throw new NotFoundException('Unknown TODO identifier provided.');
        }
        return $todo;
    }

    /**
     * Capitalize the first letter of the given string
     * @param string $string string to be capitalized
     * @return string capitalized string
     */
    public static function capitalize($string) {
        return ucfirst(mb_strtolower($string));
    }

    /**
     * Escape the given string
     * @param string $string string to be escaped
     * @return string escaped string
     */
    public static function escape($string) {
        return htmlspecialchars($string, ENT_QUOTES);
    }

}
