<?php

use Foo\Arrays;

/**
 * Homepage presenter.
 *
 * @author     John Doe
 * @package    MyApplication
 */
class HomepagePresenter extends BasePresenter {

    public function renderDefault() {
        $this->template->anyVariable = 'any value';
        \Nette\Utils\Strings::capitalize();
        Arrays::get($arr, $key);
        Html::el();
        new Request();
    }

}

?>