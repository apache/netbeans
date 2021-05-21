<?php
$data = [
    ["id" => 1, "name" => 'Tom'],
    ["id" => 2, "name" => 'Fred'],
];

list("id" => $id1, "name" => $name1) = $data[0];

["id" => $id1, "name" => $name1] = $data[0];

foreach ($data as list("id" => $id, "name" => $name)) {
}

foreach ($data as ["id" => $id, "name" => $name]) {
}
