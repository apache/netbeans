<?php
class User {
    private $name;

    public function getName() {
        return $this->name;
    }
}

$user1 = new User("Pepa");
echo "Name1: ".$user1->;

$user2 = &new User("Pavel");
echo "Name2: ".$user2->;
?>