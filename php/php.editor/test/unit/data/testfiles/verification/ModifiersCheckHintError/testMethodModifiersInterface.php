<?php
interface InvalidMethodModifiersInterface {
    // Valid
    function implicitPublicMethod(): void;
    static function implicitPublicStaticMethod(): void;
    public function publicMethod(): void;
    public static function publicStaticMethod(): void;
    
    // Invalid abstract
    abstract function abstractImplicitPublicMethodError(): void;
//  function abstractImplicitPublicMethod(): void;
    abstract static function abstractImplicitPublicStaticMethodError(): void;
//  static function abstractImplicitPublicStaticMethod(): void;
    abstract public function abstractPublicMethodError(): void;
//  public function abstractPublicMethod(): void;
    abstract public static function abstractPublicStaticMethodError(): void;
//  public static function abstractPublicStaticMethod(): void;
    
    // Invalid visibility
    private function privateMethodError(): void;
//  function privateMethodError(): void;
    protected function protectedMethodError(): void;
//  function protectedMethodError(): void;
    private static function privateStaticMethodError(): void;
//  static function privateStaticMethodError(): void;
    protected static function protectedStaticMethodError(): void;
//  static function protectedStaticMethodError(): void;

    // Invalid final modifier
    final function finalImplicitPublicMethodError(): void;
//  function finalImplicitPublicMethod(): void;
    final static function finalImplicitPublicStaticMethodError(): void;
//  static function finalImplicitPublicStaticMethod(): void;
    final public function finalPublicMethodError(): void;
//  public function finalPublicMethod(): void;
    final public static function finalPublicStaticMethodError(): void;
//  public static function finalPublicStaticMethod(): void;

    // Invalid readonly modifier
    readonly function readonlyImplicitPublicMethodError(): void;
//  function readonlyImplicitPublicMethodError(): void;
    readonly static function readonlyImplicitPublicStaticMethodError(): void;
//  static function readonlyImplicitPublicStaticMethod(): void;
    readonly public function readonlyPublicMethodError(): void;
//  public function readonlyPublicMethod(): void;
    readonly public static function readonlyPublicStaticMethodError(): void;
//  public static function readonlyPublicStaticMethod(): void;

    // Invalid set visibility
    private(set) function privateSetImplicitPublicMethodError(): void;
//  function privateSetImplicitPublicMethodError(): void;
    protected(set) static function protectedSetImplicitPublicStaticMethodError(): void;
//  static function protectedSetImplicitPublicStaticMethod(): void;
    public(set) public function publicSetPublicMethodError(): void;
//  public function publicSetPublicMethod(): void;
    private(set) public static function privateSetPublicStaticMethodError(): void;
//  public static function privateSetPublicStaticMethod(): void;
    
    // Invalid multiple access type modifiers
    public public function publicPublicMethodError(): void;
//  public function publicPublicMethodError(): void;
    public private function publicPrivateMethodError(): void;
//  public function publicPrivateMethodError(): void;
    protected private function protectedPrivateMethodError(): void;
//  protected function protectedPrivateMethodError(): void; 
}
