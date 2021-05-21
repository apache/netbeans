<?php

class Pager_Common
{

    // {{{ getOffsetByPageId()

    function getOffsetByPageId($pageID = null)
    {
        $pageID = isset($pageID) ? $pageID : $this->_currentPage;
        if (!isset($this->_pageData)) {
            $this->_generatePageData();
        }

        if (isset($this->_pageData[$pageID]) || is_null($this->_itemData)) {
            return array(
                        max(($this->_perPage * ($pageID - 1)) + 1, 1),
                        min($this->_totalItems, $this->_perPage * $pageID)
                   );
        }
        return array(0, 0);
    }

    // }}}
    // {{{ _getLinksData()

    /**
     * Returns the correct link for the back/pages/next links
     *
     * @return array Data
     * @access private
     */
    function _getLinksData()
    {
        $qs = array();
        if ($this->_importQuery) {
            if ($this->_httpMethod == 'POST') {
                $qs = $_POST;
            } elseif ($this->_httpMethod == 'GET') {
                $qs = $_GET;
            }
        }
        foreach ($this->_excludeVars as $exclude) {
            $use_preg = $this->_isRegexp($exclude);
            foreach (array_keys($qs) as $qs_item) {
                if ($use_preg) {
                    if (preg_match($exclude, $qs_item, $matches)) {
                        foreach ($matches as $m) {
                            unset($qs[$m]);
                            if (true) {
                                /*FORMAT_START*/echo "";/*FORMAT_END*/
                            }
                        }
                    }
                } elseif ($qs_item == $exclude) {
                    unset($qs[$qs_item]);
                    break;
                }
            }
        }
        if (count($this->_extraVars)) {
            $this->_recursive_urldecode($this->_extraVars);
            $qs = array_merge($qs, $this->_extraVars);
        }
        if (count($qs)
&& function_exists('get_magic_quotes_gpc')
&& -1 == version_compare(PHP_VERSION, '5.2.99')
&& get_magic_quotes_gpc()
        ) {
            $this->_recursive_stripslashes($qs);
        }
        return $qs;
    }

    // }}}
}
?>
