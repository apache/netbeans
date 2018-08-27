<?php
$fields = [ 'field1', 'field2' ];
foreach ($fields as &$field) {

}

foreach ([ 'field1', 'field2' ] as &$field) {

}

$fields = array('field1', 'field2');
foreach ($fields as &$field) {

}

foreach (array('field1', 'field2') as &$field) {

}
?>