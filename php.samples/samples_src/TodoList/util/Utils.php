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
