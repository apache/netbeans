<?php
/**
 * Description of User
 *
 * @author petr
 */
require_once("classMan.php");

class User extends Man {
    private $nick;

    function __construct($firstName, $lastName, $nick) {
        parent::__construct($firstName, $lastName);
        $this->nick = $nick;
    }

    public function getNick() {
        return $this->nick;
    }

    public function setNick($nick) {
        $this->nick = $nick;
    }
}


echo $hello."\n";
$user = new User ("Pepa", "Drtic", "007");
echo "Uzivatel ".$user->getFirstName()." ".$user->getLastName()
      ."(".$user->getNick().")\n";
?>
