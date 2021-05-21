<?php

    $parser->RegisterKeys(array(
        'ID'        => $key,
        'PARENT_ID' => $val['parent_id'],
        'NAME'      => /*FORMAT_START*/str_replace($search, $replace, $subject)/*FORMAT_END*/
        'IS_OPEN'   => $val['level'] < 1 ? "true" : "false",
    ));

?>
