<?php
use Nette\Application\UI;

class TaskList extends UI\Control {

    public function handleMarkDone($taskId) {
        // no CC after $this->presenter-> 
        $this->presenter->redirect('this');
    }

}