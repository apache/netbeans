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

namespace TodoList\Flash;

use \Exception;

/**
 * Class managing flash messages.
 * <p>
 * (Flash messages are positive messages that are displayed exactly once,
 * on the next page; typically after form submitting.)
 */
final class Flash {

    const FLASHES_KEY = '_flashes';

    private static $flashes = null;


    private function __construct() {
    }

    public static function hasFlashes() {
        self::initFlashes();
        return count(self::$flashes) > 0;
    }

    public static function addFlash($message) {
        if (!strlen(trim($message))) {
            throw new Exception('Cannot insert empty flash message.');
        }
        self::initFlashes();
        self::$flashes[] = $message;
    }

    /**
     * Get flash messages and clear them.
     * @return array flash messages
     */
    public static function getFlashes() {
        self::initFlashes();
        $copy = self::$flashes;
        self::$flashes = [];
        return $copy;
    }

    private static function initFlashes() {
        if (self::$flashes !== null) {
            return;
        }
        if (!array_key_exists(self::FLASHES_KEY, $_SESSION)) {
            $_SESSION[self::FLASHES_KEY] = [];
        }
        self::$flashes = &$_SESSION[self::FLASHES_KEY];
    }

}
