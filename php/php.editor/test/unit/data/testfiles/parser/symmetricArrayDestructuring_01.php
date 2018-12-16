<?php
$data = [
    [1, 'Tom'],
    [2, 'Fred'],
];

list($id1, $name1) = $data[0];

[$id1, $name1] = $data[0];

foreach ($data as list($id, $name)) {
}

foreach ($data as [$id, $name]) {
}
