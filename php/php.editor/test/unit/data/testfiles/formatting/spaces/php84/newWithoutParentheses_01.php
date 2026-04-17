<?php
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

new         Example($param) ->           method($param,   1,    2);
new Example(        )?->method(              );
                    new Example( )->method(  )::CONSTANT;
                new Example()->method(  )::$staticField;
                    new Example()->method()->field;
                new Example(  )->method( )->method( );
                new Example( )->method()?->method();
$example =  new Example()->     method( )::   staticMethod1( );

          $example = new           $class()->method();
new $class()?->method();
new $class(             )->method()::CONSTANT;
new $class            ()->method()::$staticField;
new $class(  )->method(   )->field;
new                   $class()->method()->method();
                     new $class()->method()?->method();
new $class($param, 1 , 2     , "test")->method(  )::staticMethod1();

new (   trim(' Example ')   )()          ->method();
new (trim(' Example '))(   )?->       method(   );
new (trim(' Example '))()->method()::CONSTANT;
          $example = new (trim(' Example '))()->method(   $a, 1, "test")::$staticField;
                 new (trim(' Example '))()->method(  test: "test"   )->field;
new (trim(' Example '))()-> method ()->method();
new (trim(' Example '))(   )  ->method()?->method();
 new  (   trim   (   ' Example '   )    )(  test: "test"   )->method()::staticMethod1();

 new  Example( )::staticMethod1( );
new Example(  test: "test"   ):: staticMethod1 ( )::CONSTANT;
new Example()  ::staticMethod1()::$staticField;
new Example()::staticMethod1()-> field;
$example = new Example()::staticMethod1()->method();
new Example():: staticMethod1 (   )?->method();
new Example()  ::staticMethod1(   )::staticMethod2();

new    $class()::staticMethod1();
new $class()::staticMethod1()::CONSTANT;
new $class()::        staticMethod1()::$staticField;
            new $class()::staticMethod1()->field;
new $class()::staticMethod1()->method();
          echo new $class()::staticMethod1()?->method();
new        $class()::staticMethod1()              ::staticMethod2();

new (trim(' Example '))()::staticMethod1();
new (trim(' Example '))()::staticMethod1()::CONSTANT;
new (trim(' Example '))()::staticMethod1()::$staticField;
new (trim(' Example '))()::staticMethod1()->field;
new (trim(' Example '))(    )::staticMethod1(   )->method();
new (trim(' Example '))()::staticMethod1()?->method();
$example = new (trim(' Example '))(   )::staticMethod1(   )::staticMethod2(    );

new Example()::           CONSTANT1;
new Example()::CONSTANT1::CONSTANT2;
        new Example()::CONSTANT1::$staticField;
new          Example()::CONSTANT1->field;
new Example()::CONSTANT1->            method();
new Example()         ::CONSTANT1           ?->method();
          echo         new Example()::CONSTANT1::staticMethod();

new Example()::{'CONSTANT'};
new Example()::{'CONSTANT'}::CONSTANT;
new Example()::{'CONSTANT'}::$staticField;
new Example()         ::{'CONSTANT'}->field;
new Example()::{'CONSTANT'}->method(  );
new Example()::               {'CONSTANT'}?->method();
new Example()::{'CONSTANT'}::staticMethod();

new $class()::          CONSTANT;
new $class()::CONSTANT::CONSTANT;
new $class()::CONSTANT::$staticField;
          new $class()::CONSTANT->field;
new $class()::CONSTANT->method();
                  new $class()           ::CONSTANT?->              method();
new        $class()::CONSTANT::                staticMethod();

new      $class()::{'CONSTANT'};
new $class()::{'CONSTANT'}            ::CONSTANT;
         new $class()::          {'CONSTANT'}::               $staticField;
new $class()::{'CONSTANT'}->field;
            new $class()::{'CONSTANT'}->method();
new $class()              ::{'CONSTANT'}?->method();
new $class()::{'CONSTANT'}::staticMethod();

new (trim(' Example '))()::CONSTANT1;
new (trim(' Example '))()::CONSTANT1::CONSTANT2;
new (trim(' Example '))()::CONSTANT1::$staticField;
new (             trim(' Example '))(            )::               CONSTANT1-> field;
new (trim(' Example '))()::CONSTANT1->method();
new       (trim(' Example '))()::        CONSTANT1?->method();
new (trim(' Example '))()::CONSTANT1           ::staticMethod();

new (trim(' Example '))()::{'CONSTANT'};
              new (trim(' Example ')         )()::              {'CONSTANT'}::CONSTANT;
new (trim(' Example '))()::{'CONSTANT'}             ::$staticField;
          echo new (trim(' Example ')              )(             )::{'CONSTANT'}->field;
$example = new (trim(' Example '))()::{'CONSTANT'}->method();
              new (trim(' Example '))()::{'CONSTANT'}?->method();
new (trim(' Example '))()::{'CONSTANT'}::staticMethod();

new Example()->field1;
new Example()?->             field1;
          new Example()->field1::CONSTANT;
                new Example()->field1::$staticField;
                new Example()-> field1->field2;
                new     Example()->field1->method();
                new Example()->field1?-> method();
                new Example()->field1 :: staticMethod1();

new $class()->field1;
new $class()    ?->field1;
new $class()->field1::CONSTANT;
               new $class()->field1::$staticField;
new $class()->field1->field2;
new $class()                      ->field1->method();
              $example             =          new $class()->          field1 ?->method();
new $class()->field1::staticMethod1();

new     (trim     (' Example '))()->field1;
             new (trim(' Example '))()?->field1;
new (trim(' Example '))()->field1                ::CONSTANT;
new             (trim(' Example '))()   ->field1            ::$staticField;
new (trim(' Example '))()->field1->field2;
new (trim(' Example '))()->field1->method();
$example = new (trim(' Example '))()->field1?->method();
new (trim(' Example '))()->field1::staticMethod1();

new Example($a, 1, "test")::$staticField;
           new Example()::$staticField->field1::CONSTANT;
new Example()::           $staticField->field1::$staticField2;
new Example()::$staticField              ->field1->field2;
new Example()::$staticField->field1->method();
           new Example()              ::$staticField->field1?->method();
new Example()::$staticField->          field1::staticMethod1();

new $class()::$staticField;
new $class()::$staticField->field1::CONSTANT;
new              $class()::$staticField->field1::$staticField2;
new $class()::$staticField->field1->field2;
              new $class()::$staticField->                 field1->method();
new $class()::              $staticField->field1                ?->method();
             $example              = new $class()::$staticField->field1::staticMethod1();

new (trim(' Example '))()::$staticField;
                  new (trim(' Example '))()::             $staticField->field1 :: CONSTANT;
$example = new (trim(' Example '))()::$staticField->field1::$staticField2;
                   new (trim(' Example '))( ):: $staticField->    field1->field2;
new (trim(' Example '))()::$staticField->field1->method();
new ( trim(' Example '))  ()::$staticField->field1?->method();
            new ( trim(' Example '))()::$staticField->field1::   staticMethod1();

       $example =        new Example()      ();
                 new  $class()();
new          (trim(' Example '))( )( );

               echo (new Example())['key'];
( new $class( ) )         ['key'];
         ( new (trim ( ' Example ' ))(  ))[ 'key' ];

new Example()['key'];
$example = new $class()['key'];
new (trim(' Example '))()['key'];

    isset(   new Example(   )['key'   ]);
isset(   new $class()['key'   ]);
isset(new (trim(' Example '))   ()['key']   );

var_dump(new Something());
var_dump(new (Something()));

var_dump(new A::$staticField);
