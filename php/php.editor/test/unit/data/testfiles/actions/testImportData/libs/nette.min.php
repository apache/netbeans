<?php //netteloader=Nette\Framework
/*
 * New BSD License
 *
 * Copyright (c) 2004, 2014 David Grudl (https://davidgrudl.com)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 * 
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 * 
 * 	* Neither the name of "Nette Framework" nor the names of its contributors
 * 	may be used to endorse or promote products derived from this software
 * 	without specific prior written permission.
 * 
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright owner or contributors be liable for
 * any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused and on
 * any theory of liability, whether in contract, strict liability, or tort
 * (including negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage.
 */
namespace   {

/**
 * Nette Framework (version 2.0-dev released on 2011-07-02, http://nette.org)
 *
 * Copyright (c) 2004, 2011 David Grudl (http://davidgrudl.com)
 *
 * For the full copyright and license information, please view
 * the file license.txt that was distributed with this source code.
 */

error_reporting(E_ALL | E_STRICT);
@set_magic_quotes_runtime(FALSE);
iconv_set_encoding('internal_encoding', 'UTF-8');
extension_loaded('mbstring') && mb_internal_encoding('UTF-8');
@header('X-Powered-By: Nette Framework');
@header('Content-Type: text/html; charset=utf-8');

define('NETTE', TRUE);
define('NETTE_DIR', __DIR__);
define('NETTE_VERSION_ID', 20000);
define('NETTE_PACKAGE', '5.3');

}

namespace Nette\Diagnostics {

use Nette;

interface IBarPanel
{

	function getTab();

	function getPanel();

}

}

namespace Nette\Application {

use Nette;

interface IPresenter
{

	function run(Request $request);

}

interface IPresenterFactory
{

	function getPresenterClass(& $name);

	function createPresenter($name);

}

interface IResponse
{

	function send(Nette\Http\IRequest $httpRequest, Nette\Http\IResponse $httpResponse);

}

interface IRouter
{

	const ONE_WAY = 1;

	const SECURED = 2;

	function match(Nette\Http\IRequest $httpRequest);

	function constructUrl(Request $appRequest, Nette\Http\Url $refUrl);

}

}

namespace Nette {

use Nette;

interface IFreezable
{

	function freeze();

	function isFrozen();

}

}

namespace Nette\ComponentModel {

use Nette;

interface IComponent
{

	const NAME_SEPARATOR = '-';

	function getName();

	function getParent();

	function setParent(IContainer $parent = NULL, $name = NULL);

}

interface IContainer extends IComponent
{

	function addComponent(IComponent $component, $name);

	function removeComponent(IComponent $component);

	function getComponent($name);

	function getComponents($deep = FALSE, $filterType = NULL);

}

}

namespace Nette\Application\UI {

use Nette;

interface ISignalReceiver
{

	function signalReceived($signal);

}

interface IStatePersistent
{

	function loadState(array $params);

	function saveState(array & $params);

}

interface IPartiallyRenderable extends IRenderable
{

}

interface IRenderable
{

	function invalidateControl();

	function isControlInvalid();

}

}

namespace Nette\Caching {

use Nette;

interface IStorage
{

	function read($key);

	function write($key, $data, array $dependencies);

	function remove($key);

	function clean(array $conds);

}

}

namespace Nette\Caching\Storages {

use Nette;

interface IJournal
{

	function write($key, array $dependencies);

	function clean(array $conditions);

}

}

namespace Nette\Config {

use Nette;

interface IAdapter
{

	static function load($file);

	static function save($config, $file);

}

}

namespace Nette\Database {

use Nette;

interface ISupplementalDriver
{

	function delimite($name);

	function formatDateTime(\DateTime $value);

	function formatLike($value, $pos);

	function applyLimit(&$sql, $limit, $offset);

	function normalizeRow($row, $statement);

}

}

namespace Nette\DI {

use Nette;

interface IContainer
{

	function addService($name, $service);

	function getService($name);

	function removeService($name);

	function hasService($name);

}

interface IServiceBuilder
{

	function createService(IContainer $container);

}

}

namespace Nette\Forms {

use Nette;

interface IControl
{

	function loadHttpData();

	function setValue($value);

	function getValue();

	function getRules();

	function getErrors();

	function isDisabled();

	function translate($s, $count = NULL);

}

interface ISubmitterControl extends IControl
{

	function isSubmittedBy();

	function getValidationScope();

}

interface IFormRenderer
{

	function render(Form $form);

}

}

namespace Nette\Http {

use Nette;

interface IRequest
{

	const
		GET = 'GET',
		POST = 'POST',
		HEAD = 'HEAD',
		PUT = 'PUT',
		DELETE = 'DELETE';

	function getUrl();

	function getQuery($key = NULL, $default = NULL);

	function getPost($key = NULL, $default = NULL);

	function getFile($key);

	function getFiles();

	function getCookie($key, $default = NULL);

	function getCookies();

	function getMethod();

	function isMethod($method);

	function getHeader($header, $default = NULL);

	function getHeaders();

	function isSecured();

	function isAjax();

	function getRemoteAddress();

	function getRemoteHost();

}

interface IResponse
{

	const PERMANENT = 2116333333;

	const BROWSER = 0;

	const
		S200_OK = 200,
		S204_NO_CONTENT = 204,
		S300_MULTIPLE_CHOICES = 300,
		S301_MOVED_PERMANENTLY = 301,
		S302_FOUND = 302,
		S303_SEE_OTHER = 303,
		S303_POST_GET = 303,
		S304_NOT_MODIFIED = 304,
		S307_TEMPORARY_REDIRECT= 307,
		S400_BAD_REQUEST = 400,
		S401_UNAUTHORIZED = 401,
		S403_FORBIDDEN = 403,
		S404_NOT_FOUND = 404,
		S405_METHOD_NOT_ALLOWED = 405,
		S410_GONE = 410,
		S500_INTERNAL_SERVER_ERROR = 500,
		S501_NOT_IMPLEMENTED = 501,
		S503_SERVICE_UNAVAILABLE = 503;

	function setCode($code);

	function getCode();

	function setHeader($name, $value);

	function addHeader($name, $value);

	function setContentType($type, $charset = NULL);

	function redirect($url, $code = self::S302_FOUND);

	function setExpiration($seconds);

	function isSent();

	function getHeaders();

	function setCookie($name, $value, $expire, $path = NULL, $domain = NULL, $secure = NULL, $httpOnly = NULL);

	function deleteCookie($name, $path = NULL, $domain = NULL, $secure = NULL);

}

interface ISessionStorage
{

	function open($savePath, $sessionName);

	function close();

	function read($id);

	function write($id, $data);

	function remove($id);

	function clean($maxlifetime);

}

interface IUser
{

	function login();

	function logout($clearIdentity = FALSE);

	function isLoggedIn();

	function getIdentity();

	function setAuthenticator(Nette\Security\IAuthenticator $handler);

	function getAuthenticator();

	function setNamespace($namespace);

	function getNamespace();

	function getRoles();

	function isInRole($role);

	function isAllowed();

	function setAuthorizator(Nette\Security\IAuthorizator $handler);

	function getAuthorizator();

}

}

namespace Nette\Latte {

use Nette;

interface IMacro
{

	function initialize();

	function finalize();

	function nodeOpened(MacroNode $node);

	function nodeClosed(MacroNode $node);

}

}

namespace Nette\Localization {

use Nette;

interface ITranslator
{

	function translate($message, $count = NULL);

}

}

namespace Nette\Mail {

use Nette;

interface IMailer
{

	function send(Message $mail);

}

}

namespace Nette\Reflection {

use Nette;

interface IAnnotation
{

	function __construct(array $values);

}

}

namespace Nette\Security {

use Nette;

interface IAuthenticator
{

	const USERNAME = 0,
		PASSWORD = 1;

	const IDENTITY_NOT_FOUND = 1,
		INVALID_CREDENTIAL = 2,
		FAILURE = 3,
		NOT_APPROVED = 4;

	function authenticate(array $credentials);

}

interface IAuthorizator
{

	const ALL = NULL;

	const ALLOW = TRUE;

	const DENY = FALSE;

	function isAllowed($role, $resource, $privilege);

}

interface IIdentity
{

	function getId();

	function getRoles();

}

interface IResource
{

	function getResourceId();

}

interface IRole
{

	function getRoleId();

}

}

namespace Nette\Templating {

use Nette;

interface ITemplate
{

	function render();

}

interface IFileTemplate extends ITemplate
{

	function setFile($file);

	function getFile();

}

}

namespace Nette {

use Nette;

class ArgumentOutOfRangeException extends \InvalidArgumentException
{
}

class InvalidStateException extends \RuntimeException
{
	}

class NotImplementedException extends \LogicException
{
}

class NotSupportedException extends \LogicException
{
}

class DeprecatedException extends NotSupportedException
{
}

class MemberAccessException extends \LogicException
{
}

class IOException extends \RuntimeException
{
}

class FileNotFoundException extends IOException
{
}

class DirectoryNotFoundException extends IOException
{
}

class InvalidArgumentException extends \InvalidArgumentException
{
}

class OutOfRangeException extends \OutOfRangeException
{
}

class UnexpectedValueException extends \UnexpectedValueException
{
}

class StaticClassException extends \LogicException
{
}

class FatalErrorException extends \ErrorException
{

	function __construct($message, $code, $severity, $file, $line, $context)
	{
		parent::__construct($message, $code, $severity, $file, $line);
		$this->context = $context;
	}

}

abstract class Object
{

	static function getReflection()
	{
		return new Reflection\ClassType(get_called_class());
	}

	function __call($name, $args)
	{
		return ObjectMixin::call($this, $name, $args);
	}

	static function __callStatic($name, $args)
	{
		return ObjectMixin::callStatic(get_called_class(), $name, $args);
	}

	static function extensionMethod($name, $callback = NULL)
	{
		if (strpos($name, '::') === FALSE) {
			$class = get_called_class();
		} else {
			list($class, $name) = explode('::', $name);
		}
		$class = new Reflection\ClassType($class);
		if ($callback === NULL) {
			return $class->getExtensionMethod($name);
		} else {
			$class->setExtensionMethod($name, $callback);
		}
	}

	function &__get($name)
	{
		return ObjectMixin::get($this, $name);
	}

	function __set($name, $value)
	{
		return ObjectMixin::set($this, $name, $value);
	}

	function __isset($name)
	{
		return ObjectMixin::has($this, $name);
	}

	function __unset($name)
	{
		ObjectMixin::remove($this, $name);
	}

}

}

namespace Nette\Utils {

use Nette;

final class LimitedScope
{
	private static $vars;

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function evaluate()
	{
		if (func_num_args() > 1) {
			self::$vars = func_get_arg(1);
			extract(self::$vars);
		}
		$res = eval('?>' . func_get_arg(0));
		if ($res === FALSE && ($error = error_get_last()) && $error['type'] === E_PARSE) {
			throw new Nette\FatalErrorException($error['message'], 0, $error['type'], $error['file'], $error['line'], NULL);
		}
		return $res;
	}

	static function load()
	{
		if (func_num_args() > 1) {
			self::$vars = func_get_arg(1);
			extract(self::$vars);
		}
		return  include func_get_arg(0);
	}

}

}

namespace Nette\Loaders {

use Nette;

abstract class AutoLoader extends Nette\Object
{

	static private $loaders = array();

	public static $count = 0;

	final static function load($type)
	{
		foreach (func_get_args() as $type) {
			if (!class_exists($type)) {
				throw new Nette\InvalidStateException("Unable to load class or interface '$type'.");
			}
		}
	}

	final static function getLoaders()
	{
		return array_values(self::$loaders);
	}

	function register()
	{
		if (!function_exists('spl_autoload_register')) {
			throw new Nette\NotSupportedException('spl_autoload does not exist in this PHP installation.');
		}

		spl_autoload_register(array($this, 'tryLoad'));
		self::$loaders[spl_object_hash($this)] = $this;
	}

	function unregister()
	{
		unset(self::$loaders[spl_object_hash($this)]);
		return spl_autoload_unregister(array($this, 'tryLoad'));
	}

	abstract function tryLoad($type);

}

}

namespace Nette\Diagnostics {

use Nette;

final class Helpers
{

	static function editorLink($file, $line)
	{
		if (Debugger::$editor && is_file($file)) {
			$dir = dirname(strtr($file, '/', DIRECTORY_SEPARATOR));
			$base = isset($_SERVER['SCRIPT_FILENAME']) ? dirname(dirname(strtr($_SERVER['SCRIPT_FILENAME'], '/', DIRECTORY_SEPARATOR))) : dirname($dir);
			if (substr($dir, 0, strlen($base)) === $base) {
				$dir = '...' . substr($dir, strlen($base));
			}
			return Nette\Utils\Html::el('a')
				->href(strtr(Debugger::$editor, array('%file' => rawurlencode($file), '%line' => $line)))
				->title("$file:$line")
				->setHtml(htmlSpecialChars(rtrim($dir, DIRECTORY_SEPARATOR)) . DIRECTORY_SEPARATOR . '<b>' . htmlSpecialChars(basename($file)) . '</b>');
		} else {
			return Nette\Utils\Html::el('span')->setText($file);
		}
	}

	static function htmlDump(&$var, $level = 0)
	{
		static $tableUtf, $tableBin, $reBinary = '#[^\x09\x0A\x0D\x20-\x7E\xA0-\x{10FFFF}]#u';
		if ($tableUtf === NULL) {
			foreach (range("\x00", "\xFF") as $ch) {
				if (ord($ch) < 32 && strpos("\r\n\t", $ch) === FALSE) {
					$tableUtf[$ch] = $tableBin[$ch] = '\\x' . str_pad(dechex(ord($ch)), 2, '0', STR_PAD_LEFT);
				} elseif (ord($ch) < 127) {
					$tableUtf[$ch] = $tableBin[$ch] = $ch;
				} else {
					$tableUtf[$ch] = $ch; $tableBin[$ch] = '\\x' . dechex(ord($ch));
				}
			}
			$tableBin["\\"] = '\\\\';
			$tableBin["\r"] = '\\r';
			$tableBin["\n"] = '\\n';
			$tableBin["\t"] = '\\t';
			$tableUtf['\\x'] = $tableBin['\\x'] = '\\\\x';
		}

		if (is_bool($var)) {
			return '<span class="php-bool">' . ($var ? 'TRUE' : 'FALSE') . "</span>\n";

		} elseif ($var === NULL) {
			return "<span class=\"php-null\">NULL</span>\n";

		} elseif (is_int($var)) {
			return "<span class=\"php-int\">$var</span>\n";

		} elseif (is_float($var)) {
			$var = var_export($var, TRUE);
			if (strpos($var, '.') === FALSE) {
				$var .= '.0';
			}
			return "<span class=\"php-float\">$var</span>\n";

		} elseif (is_string($var)) {
			if (Debugger::$maxLen && strlen($var) > Debugger::$maxLen) {
				$s = htmlSpecialChars(substr($var, 0, Debugger::$maxLen), ENT_NOQUOTES) . ' ... ';
			} else {
				$s = htmlSpecialChars($var, ENT_NOQUOTES);
			}
			$s = strtr($s, preg_match($reBinary, $s) || preg_last_error() ? $tableBin : $tableUtf);
			$len = strlen($var);
			return "<span class=\"php-string\">\"$s\"</span>" . ($len > 1 ? " ($len)" : "") . "\n";

		} elseif (is_array($var)) {
			$s = '<span class="php-array">array</span>(' . count($var) . ") ";
			$space = str_repeat($space1 = '   ', $level);
			$brackets = range(0, count($var) - 1) === array_keys($var) ? "[]" : "{}";

			static $marker;
			if ($marker === NULL) {
				$marker = uniqid("\x00", TRUE);
			}
			if (empty($var)) {

			} elseif (isset($var[$marker])) {
				$brackets = $var[$marker];
				$s .= "$brackets[0] *RECURSION* $brackets[1]";

			} elseif ($level < Debugger::$maxDepth || !Debugger::$maxDepth) {
				$s .= "<code>$brackets[0]\n";
				$var[$marker] = $brackets;
				foreach ($var as $k => &$v) {
					if ($k === $marker) {
						continue;
					}
					$k = strtr($k, preg_match($reBinary, $k) || preg_last_error() ? $tableBin : $tableUtf);
					$k = htmlSpecialChars(preg_match('#^\w+$#', $k) ? $k : "\"$k\"");
					$s .= "$space$space1<span class=\"php-key\">$k</span> => " . self::htmlDump($v, $level + 1);
				}
				unset($var[$marker]);
				$s .= "$space$brackets[1]</code>";

			} else {
				$s .= "$brackets[0] ... $brackets[1]";
			}
			return $s . "\n";

		} elseif (is_object($var)) {
			if ($var instanceof \Closure) {
				$rc = new \ReflectionFunction($var);
				$arr = array();
				foreach ($rc->getParameters() as $param) {
					$arr[] = '$' . $param->getName();
				}
				$arr = array('file' => $rc->getFileName(), 'line' => $rc->getStartLine(), 'parameters' => implode(', ', $arr));
			} else {
				$arr = (array) $var;
			}
			$s = '<span class="php-object">' . get_class($var) . "</span>(" . count($arr) . ") ";
			$space = str_repeat($space1 = '   ', $level);

			static $list = array();
			if (empty($arr)) {

			} elseif (in_array($var, $list, TRUE)) {
				$s .= "{ *RECURSION* }";

			} elseif ($level < Debugger::$maxDepth || !Debugger::$maxDepth || $var instanceof \Closure) {
				$s .= "<code>{\n";
				$list[] = $var;
				foreach ($arr as $k => &$v) {
					$m = '';
					if ($k[0] === "\x00") {
						$m = ' <span class="php-visibility">' . ($k[1] === '*' ? 'protected' : 'private') . '</span>';
						$k = substr($k, strrpos($k, "\x00") + 1);
					}
					$k = strtr($k, preg_match($reBinary, $k) || preg_last_error() ? $tableBin : $tableUtf);
					$k = htmlSpecialChars(preg_match('#^\w+$#', $k) ? $k : "\"$k\"");
					$s .= "$space$space1<span class=\"php-key\">$k</span>$m => " . self::htmlDump($v, $level + 1);
				}
				array_pop($list);
				$s .= "$space}</code>";

			} else {
				$s .= "{ ... }";
			}
			return $s . "\n";

		} elseif (is_resource($var)) {
			return '<span class="php-resource">' . htmlSpecialChars(get_resource_type($var)) . " resource</span>\n";

		} else {
			return "<span>unknown type</span>\n";
		}
	}

	static function clickableDump($dump)
	{
		return '<pre class="nette-dump">' . preg_replace_callback(
			'#^( *)((?>[^(]{1,200}))\((\d+)\) <code>#m',
			function ($m) {
				return "$m[1]<a href='#' rel='next'>$m[2]($m[3]) "
					. (trim($m[1]) || $m[3] < 7
					? '<abbr>&#x25bc;</abbr> </a><code>'
					: '<abbr>&#x25ba;</abbr> </a><code class="nette-collapsed">');
			},
			self::htmlDump($dump)
		) . '</pre>';
	}

}

}

namespace Nette\Utils {

use Nette;

class Html extends Nette\Object implements \ArrayAccess, \Countable, \IteratorAggregate
{

	private $name;

	private $isEmpty;

	public $attrs = array();

	protected $children = array();

	public static $xhtml = TRUE;

	public static $emptyElements = array('img'=>1,'hr'=>1,'br'=>1,'input'=>1,'meta'=>1,'area'=>1,'embed'=>1,'keygen'=>1,
		'source'=>1,'base'=>1,'col'=>1,'link'=>1,'param'=>1,'basefont'=>1,'frame'=>1,'isindex'=>1,'wbr'=>1,'command'=>1);

	static function el($name = NULL, $attrs = NULL)
	{
		$el = new static;
		$parts = explode(' ', $name, 2);
		$el->setName($parts[0]);

		if (is_array($attrs)) {
			$el->attrs = $attrs;

		} elseif ($attrs !== NULL) {
			$el->setText($attrs);
		}

		if (isset($parts[1])) {
			foreach (Strings::matchAll($parts[1] . ' ', '#([a-z0-9:-]+)(?:=(["\'])?(.*?)(?(2)\\2|\s))?#i') as $m) {
				$el->attrs[$m[1]] = isset($m[3]) ? $m[3] : TRUE;
			}
		}

		return $el;
	}

	final function setName($name, $isEmpty = NULL)
	{
		if ($name !== NULL && !is_string($name)) {
			throw new Nette\InvalidArgumentException("Name must be string or NULL, " . gettype($name) ." given.");
		}

		$this->name = $name;
		$this->isEmpty = $isEmpty === NULL ? isset(self::$emptyElements[$name]) : (bool) $isEmpty;
		return $this;
	}

	final function getName()
	{
		return $this->name;
	}

	final function isEmpty()
	{
		return $this->isEmpty;
	}

	function addAttributes(array $attrs)
	{
		$this->attrs = $attrs + $this->attrs;
		return $this;
	}

	final function __set($name, $value)
	{
		$this->attrs[$name] = $value;
	}

	final function &__get($name)
	{
		return $this->attrs[$name];
	}

	final function __unset($name)
	{
		unset($this->attrs[$name]);
	}

	final function __call($m, $args)
	{
		$p = substr($m, 0, 3);
		if ($p === 'get' || $p === 'set' || $p === 'add') {
			$m = substr($m, 3);
			$m[0] = $m[0] | "\x20";
			if ($p === 'get') {
				return isset($this->attrs[$m]) ? $this->attrs[$m] : NULL;

			} elseif ($p === 'add') {
				$args[] = TRUE;
			}
		}

		if (count($args) === 0) {

		} elseif (count($args) === 1) {
			$this->attrs[$m] = $args[0];

		} elseif ((string) $args[0] === '') {
			$tmp = & $this->attrs[$m];

		} elseif (!isset($this->attrs[$m]) || is_array($this->attrs[$m])) {
			$this->attrs[$m][$args[0]] = $args[1];

		} else {
			$this->attrs[$m] = array($this->attrs[$m], $args[0] => $args[1]);
		}

		return $this;
	}

	final function href($path, $query = NULL)
	{
		if ($query) {
			$query = http_build_query($query, NULL, '&');
			if ($query !== '') {
				$path .= '?' . $query;
			}
		}
		$this->attrs['href'] = $path;
		return $this;
	}

	final function setHtml($html)
	{
		if ($html === NULL) {
			$html = '';

		} elseif (is_array($html)) {
			throw new Nette\InvalidArgumentException("Textual content must be a scalar, " . gettype($html) ." given.");

		} else {
			$html = (string) $html;
		}

		$this->removeChildren();
		$this->children[] = $html;
		return $this;
	}

	final function getHtml()
	{
		$s = '';
		foreach ($this->children as $child) {
			if (is_object($child)) {
				$s .= $child->render();
			} else {
				$s .= $child;
			}
		}
		return $s;
	}

	final function setText($text)
	{
		if (!is_array($text)) {
			$text = htmlspecialchars((string) $text, ENT_NOQUOTES);
		}
		return $this->setHtml($text);
	}

	final function getText()
	{
		return html_entity_decode(strip_tags($this->getHtml()), ENT_QUOTES, 'UTF-8');
	}

	final function add($child)
	{
		return $this->insert(NULL, $child);
	}

	final function create($name, $attrs = NULL)
	{
		$this->insert(NULL, $child = static::el($name, $attrs));
		return $child;
	}

	function insert($index, $child, $replace = FALSE)
	{
		if ($child instanceof Html || is_scalar($child)) {
			if ($index === NULL) {
				$this->children[] = $child;

			} else {
				array_splice($this->children, (int) $index, $replace ? 1 : 0, array($child));
			}

		} else {
			throw new Nette\InvalidArgumentException("Child node must be scalar or Html object, " . (is_object($child) ? get_class($child) : gettype($child)) ." given.");
		}

		return $this;
	}

	final function offsetSet($index, $child)
	{
		$this->insert($index, $child, TRUE);
	}

	final function offsetGet($index)
	{
		return $this->children[$index];
	}

	final function offsetExists($index)
	{
		return isset($this->children[$index]);
	}

	function offsetUnset($index)
	{
		if (isset($this->children[$index])) {
			array_splice($this->children, (int) $index, 1);
		}
	}

	final function count()
	{
		return count($this->children);
	}

	function removeChildren()
	{
		$this->children = array();
	}

	final function getIterator($deep = FALSE)
	{
		if ($deep) {
			$deep = $deep > 0 ? \RecursiveIteratorIterator::SELF_FIRST : \RecursiveIteratorIterator::CHILD_FIRST;
			return new \RecursiveIteratorIterator(new Nette\Iterators\Recursor(new \ArrayIterator($this->children)), $deep);

		} else {
			return new Nette\Iterators\Recursor(new \ArrayIterator($this->children));
		}
	}

	final function getChildren()
	{
		return $this->children;
	}

	final function render($indent = NULL)
	{
		$s = $this->startTag();

		if (!$this->isEmpty) {

			if ($indent !== NULL) {
				$indent++;
			}
			foreach ($this->children as $child) {
				if (is_object($child)) {
					$s .= $child->render($indent);
				} else {
					$s .= $child;
				}
			}

			$s .= $this->endTag();
		}

		if ($indent !== NULL) {
			return "\n" . str_repeat("\t", $indent - 1) . $s . "\n" . str_repeat("\t", max(0, $indent - 2));
		}
		return $s;
	}

	final function __toString()
	{
		return $this->render();
	}

	final function startTag()
	{
		if ($this->name) {
			return '<' . $this->name . $this->attributes() . (self::$xhtml && $this->isEmpty ? ' />' : '>');

		} else {
			return '';
		}
	}

	final function endTag()
	{
		return $this->name && !$this->isEmpty ? '</' . $this->name . '>' : '';
	}

	final function attributes()
	{
		if (!is_array($this->attrs)) {
			return '';
		}

		$s = '';
		foreach ($this->attrs as $key => $value) {
			if ($value === NULL || $value === FALSE) {
				continue;

			} elseif ($value === TRUE) {
				if (self::$xhtml) {
					$s .= ' ' . $key . '="' . $key . '"';
				} else {
					$s .= ' ' . $key;
				}
				continue;

			} elseif (is_array($value)) {
				if ($key === 'data') {
					foreach ($value as $k => $v) {
						if ($v !== NULL && $v !== FALSE) {
							$s .= ' data-' . $k . '="' . htmlspecialchars((string) $v) . '"';
						}
					}
					continue;
				}

				$tmp = NULL;
				foreach ($value as $k => $v) {
					if ($v != NULL) {

						$tmp[] = $v === TRUE ? $k : (is_string($k) ? $k . ':' . $v : $v);
					}
				}
				if ($tmp === NULL) {
					continue;
				}

				$value = implode($key === 'style' || !strncmp($key, 'on', 2) ? ';' : ' ', $tmp);

			} else {
				$value = (string) $value;
			}

			$s .= ' ' . $key . '="' . htmlspecialchars($value) . '"';
		}

		$s = str_replace('@', '&#64;', $s);
		return $s;
	}

	function __clone()
	{
		foreach ($this->children as $key => $value) {
			if (is_object($value)) {
				$this->children[$key] = clone $value;
			}
		}
	}

}

}

namespace Nette\Diagnostics {

use Nette;

final class Debugger
{

	public static $productionMode;

	public static $consoleMode;

	public static $time;

	private static $ajaxDetected;

	public static $source;

	public static $editor = 'editor://open/?file=%file&line=%line';

	public static $maxDepth = 3;

	public static $maxLen = 150;

	public static $showLocation = FALSE;

	const DEVELOPMENT = FALSE,
		PRODUCTION = TRUE,
		DETECT = NULL;

	public static $blueScreen;

	public static $strictMode = FALSE;

	public static $scream = FALSE;

	public static $onFatalError = array();

	private static $enabled = FALSE;

	private static $lastError = FALSE;

	public static $logger;

	public static $fireLogger;

	public static $logDirectory;

	public static $email;

	public static $mailer;

	public static $emailSnooze;

	public static $bar;

	private static $errorPanel;

	private static $dumpPanel;

	const DEBUG = 'debug',
		INFO = 'info',
		WARNING = 'warning',
		ERROR = 'error',
		CRITICAL = 'critical';

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function _init()
	{
		self::$time = microtime(TRUE);
		self::$consoleMode = PHP_SAPI === 'cli';
		self::$productionMode = self::DETECT;
		if (self::$consoleMode) {
			self::$source = empty($_SERVER['argv']) ? 'cli' : 'cli: ' . implode(' ', $_SERVER['argv']);
		} else {
			self::$ajaxDetected = isset($_SERVER['HTTP_X_REQUESTED_WITH']) && $_SERVER['HTTP_X_REQUESTED_WITH'] === 'XMLHttpRequest';
			if (isset($_SERVER['REQUEST_URI'])) {
				self::$source = (isset($_SERVER['HTTPS']) && strcasecmp($_SERVER['HTTPS'], 'off') ? 'https://' : 'http://')
					. (isset($_SERVER['HTTP_HOST']) ? $_SERVER['HTTP_HOST'] : (isset($_SERVER['SERVER_NAME']) ? $_SERVER['SERVER_NAME'] : ''))
					. $_SERVER['REQUEST_URI'];
			}
		}

		self::$logger = new Logger;
		self::$logDirectory = & self::$logger->directory;
		self::$email = & self::$logger->email;
		self::$mailer = & self::$logger->mailer;
		self::$emailSnooze = & Logger::$emailSnooze;

		self::$fireLogger = new FireLogger;

		self::$blueScreen = new BlueScreen;
		self::$blueScreen->addPanel(function($e) {
			if ($e instanceof Nette\Templating\FilterException) {
				return array(
					'tab' => 'Template',
					'panel' => '<p><b>File:</b> ' . Helpers::editorLink($e->sourceFile, $e->sourceLine)
					. '&nbsp; <b>Line:</b> ' . ($e->sourceLine ? $e->sourceLine : 'n/a') . '</p>'
					. ($e->sourceLine ? '<pre>' . BlueScreen::highlightFile($e->sourceFile, $e->sourceLine) . '</pre>' : '')
				);
			}
		});

		self::$bar = new Bar;
		self::$bar->addPanel(new DefaultBarPanel('time'));
		self::$bar->addPanel(new DefaultBarPanel('memory'));
		self::$bar->addPanel(self::$errorPanel = new DefaultBarPanel('errors'));
		self::$bar->addPanel(self::$dumpPanel = new DefaultBarPanel('dumps'));
	}

	static function enable($mode = NULL, $logDirectory = NULL, $email = NULL)
	{
		error_reporting(E_ALL | E_STRICT);

		if (is_bool($mode)) {
			self::$productionMode = $mode;

		} elseif (is_string($mode)) {
			$mode = preg_split('#[,\s]+#', "$mode 127.0.0.1 ::1");
		}

		if (is_array($mode)) {
			self::$productionMode = !isset($_SERVER['REMOTE_ADDR']) || !in_array($_SERVER['REMOTE_ADDR'], $mode, TRUE);
		}

		if (self::$productionMode === self::DETECT) {
			if (isset($_SERVER['SERVER_ADDR']) || isset($_SERVER['LOCAL_ADDR'])) {
				$addrs = array();
				if (isset($_SERVER['HTTP_X_FORWARDED_FOR'])) {
					$addrs = preg_split('#,\s*#', $_SERVER['HTTP_X_FORWARDED_FOR']);
				}
				if (isset($_SERVER['REMOTE_ADDR'])) {
					$addrs[] = $_SERVER['REMOTE_ADDR'];
				}
				$addrs[] = isset($_SERVER['SERVER_ADDR']) ? $_SERVER['SERVER_ADDR'] : $_SERVER['LOCAL_ADDR'];
				self::$productionMode = FALSE;
				foreach ($addrs as $addr) {
					$oct = explode('.', $addr);
					if ($addr !== '::1' && (count($oct) !== 4 || ($oct[0] !== '10' && $oct[0] !== '127' && ($oct[0] !== '172' || $oct[1] < 16 || $oct[1] > 31)
						&& ($oct[0] !== '169' || $oct[1] !== '254') && ($oct[0] !== '192' || $oct[1] !== '168')))
					) {
						self::$productionMode = TRUE;
						break;
					}
				}

			} else {
				self::$productionMode = !self::$consoleMode;
			}
		}

		if (is_string($logDirectory)) {
			self::$logDirectory = realpath($logDirectory);
			if (self::$logDirectory === FALSE) {
				throw new Nette\DirectoryNotFoundException("Directory '$logDirectory' is not found.");
			}
		} elseif ($logDirectory === FALSE) {
			self::$logDirectory = FALSE;

		} elseif (self::$logDirectory === NULL) {
			self::$logDirectory = defined('APP_DIR') ? APP_DIR . '/../log' : getcwd() . '/log';
		}
		if (self::$logDirectory) {
			ini_set('error_log', self::$logDirectory . '/php_error.log');
		}

		if (function_exists('ini_set')) {
			ini_set('display_errors', !self::$productionMode);
			ini_set('html_errors', FALSE);
			ini_set('log_errors', FALSE);

		} elseif (ini_get('display_errors') != !self::$productionMode && ini_get('display_errors') !== (self::$productionMode ? 'stderr' : 'stdout')) {
			throw new Nette\NotSupportedException('Function ini_set() must be enabled.');
		}

		if ($email) {
			if (!is_string($email)) {
				throw new Nette\InvalidArgumentException('Email address must be a string.');
			}
			self::$email = $email;
		}

		if (!defined('E_DEPRECATED')) {
			define('E_DEPRECATED', 8192);
		}

		if (!defined('E_USER_DEPRECATED')) {
			define('E_USER_DEPRECATED', 16384);
		}

		if (!self::$enabled) {
			register_shutdown_function(array(__CLASS__, '_shutdownHandler'));
			set_exception_handler(array(__CLASS__, '_exceptionHandler'));
			set_error_handler(array(__CLASS__, '_errorHandler'));
			self::$enabled = TRUE;
		}
	}

	static function isEnabled()
	{
		return self::$enabled;
	}

	static function log($message, $priority = self::INFO)
	{
		if (self::$logDirectory === FALSE) {
			return;

		} elseif (!self::$logDirectory) {
			throw new Nette\InvalidStateException('Logging directory is not specified in Nette\Diagnostics\Debugger::$logDirectory.');
		}

		if ($message instanceof \Exception) {
			$exception = $message;
			$message = "PHP Fatal error: "
				. ($message instanceof Nette\FatalErrorException
					? $exception->getMessage()
					: "Uncaught exception " . get_class($exception) . " with message '" . $exception->getMessage() . "'")
				. " in " . $exception->getFile() . ":" . $exception->getLine();

			$hash = md5($exception );
			$exceptionFilename = "exception " . @date('Y-m-d H-i-s') . " $hash.html";
			foreach (new \DirectoryIterator(self::$logDirectory) as $entry) {
				if (strpos($entry, $hash)) {
					$exceptionFilename = NULL; break;
				}
			}
		}

		self::$logger->log(array(
			@date('[Y-m-d H-i-s]'),
			$message,
			self::$source ? ' @  ' . self::$source : NULL,
			!empty($exceptionFilename) ? ' @@  ' . $exceptionFilename : NULL
		), $priority);

		if (!empty($exceptionFilename) && $logHandle = @fopen(self::$logDirectory . '/'. $exceptionFilename, 'w')) {
			ob_start();
			ob_start(function($buffer) use($logHandle) { fwrite($logHandle, $buffer); }, 1);
			self::$blueScreen->render($exception);
			ob_end_flush();
			ob_end_clean();
			fclose($logHandle);
		}
	}

	static function _shutdownHandler()
	{
		if (!self::$enabled) {
			return;
		}

		static $types = array(
			E_ERROR => 1,
			E_CORE_ERROR => 1,
			E_COMPILE_ERROR => 1,
			E_PARSE => 1,
		);
		$error = error_get_last();
		if (isset($types[$error['type']])) {
			self::_exceptionHandler(new Nette\FatalErrorException($error['message'], 0, $error['type'], $error['file'], $error['line'], NULL));
		}

		if (self::$bar && !self::$productionMode && self::isHtmlMode()) {
			self::$bar->render();
		}
	}

	static function _exceptionHandler(\Exception $exception)
	{
		if (!headers_sent()) {
			header('HTTP/1.1 500 Internal Server Error');
		}

		try {
			if (self::$productionMode) {
				self::log($exception, self::ERROR);

				if (self::$consoleMode) {
					echo "ERROR: the server encountered an internal error and was unable to complete your request.\n";

				} elseif (self::isHtmlMode()) {

?>
<!DOCTYPE html>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name=robots content=noindex><meta name=generator content="Nette Framework">
<style>body{color:#333;background:white;width:500px;margin:100px auto}h1{font:bold 47px/1.5 sans-serif;margin:.6em 0}p{font:21px/1.5 Georgia,serif;margin:1.5em 0}small{font-size:70%;color:gray}</style>

<title>Server Error</title>

<h1>Server Error</h1>

<p>We're sorry! The server encountered an internal error and was unable to complete your request. Please try again later.</p>

<p><small>error 500</small></p>
<?php
				}

			} else {
				if (self::$consoleMode) {
					echo "$exception\n";

				} elseif (self::isHtmlMode()) {
					self::$blueScreen->render($exception);
					if (self::$bar) {
						self::$bar->render();
					}

				} elseif (!self::fireLog($exception, self::ERROR)) {
					self::log($exception);
				}
			}

			foreach (self::$onFatalError as $handler) {
				call_user_func($handler, $exception);
			}
		} catch (\Exception $e) {
			echo "\nNette\\Debug FATAL ERROR: thrown ", get_class($e), ': ', $e->getMessage(),
				"\nwhile processing ", get_class($exception), ': ', $exception->getMessage(), "\n";
		}
		self::$enabled = FALSE;
		exit(255);
	}

	static function _errorHandler($severity, $message, $file, $line, $context)
	{
		if (self::$scream) {
			error_reporting(E_ALL | E_STRICT);
		}

		if (self::$lastError !== FALSE && ($severity & error_reporting()) === $severity) {
			self::$lastError = new \ErrorException($message, 0, $severity, $file, $line);
			return NULL;
		}

		if ($severity === E_RECOVERABLE_ERROR || $severity === E_USER_ERROR) {
			throw new Nette\FatalErrorException($message, 0, $severity, $file, $line, $context);

		} elseif (($severity & error_reporting()) !== $severity) {
			return FALSE;

		} elseif (self::$strictMode && !self::$productionMode) {
			self::_exceptionHandler(new Nette\FatalErrorException($message, 0, $severity, $file, $line, $context));
		}

		static $types = array(
			E_WARNING => 'Warning',
			E_COMPILE_WARNING => 'Warning',
			E_USER_WARNING => 'Warning',
			E_NOTICE => 'Notice',
			E_USER_NOTICE => 'Notice',
			E_STRICT => 'Strict standards',
			E_DEPRECATED => 'Deprecated',
			E_USER_DEPRECATED => 'Deprecated',
		);

		$message = 'PHP ' . (isset($types[$severity]) ? $types[$severity] : 'Unknown error') . ": $message";
		$count = & self::$errorPanel->data["$message|$file|$line"];

		if ($count++) {
			return NULL;

		} elseif (self::$productionMode) {
			self::log("$message in $file:$line", self::ERROR);
			return NULL;

		} else {
			$ok = self::fireLog(new \ErrorException($message, 0, $severity, $file, $line), self::WARNING);
			return !self::isHtmlMode() || (!self::$bar && !$ok) ? FALSE : NULL;
		}

		return FALSE;
	}

	static function toStringException(\Exception $exception)
	{
		if (self::$enabled) {
			self::_exceptionHandler($exception);
		} else {
			trigger_error($exception->getMessage(), E_USER_ERROR);
		}
	}

	static function tryError()
	{
		if (!self::$enabled && self::$lastError === FALSE) {
			set_error_handler(array(__CLASS__, '_errorHandler'));
		}
		self::$lastError = NULL;
	}

	static function catchError(& $error)
	{
		if (!self::$enabled && self::$lastError !== FALSE) {
			restore_error_handler();
		}
		$error = self::$lastError;
		self::$lastError = FALSE;
		return (bool) $error;
	}

	static function dump($var, $return = FALSE)
	{
		if (!$return && self::$productionMode) {
			return $var;
		}

		$output = "<pre class=\"nette-dump\">" . Helpers::htmlDump($var) . "</pre>\n";

		if (!$return) {
			$trace = debug_backtrace();
			$i = !isset($trace[1]['class']) && isset($trace[1]['function']) && $trace[1]['function'] === 'dump' ? 1 : 0;
			if (isset($trace[$i]['file'], $trace[$i]['line']) && is_file($trace[$i]['file'])) {
				$lines = file($trace[$i]['file']);
				preg_match('#dump\((.*)\)#', $lines[$trace[$i]['line'] - 1], $m);
				$output = substr_replace(
					$output,
					' title="' . htmlspecialchars((isset($m[0]) ? "$m[0] \n" : '') . "in file {$trace[$i]['file']} on line {$trace[$i]['line']}") . '"',
					4, 0);

				if (self::$showLocation) {
					$output = substr_replace(
						$output,
						' <small>in ' . Helpers::editorLink($trace[$i]['file'], $trace[$i]['line']) . ":{$trace[$i]['line']}</small>",
						-8, 0);
				}
			}
		}

		if (self::$consoleMode) {
			$output = htmlspecialchars_decode(strip_tags($output), ENT_NOQUOTES);
		}

		if ($return) {
			return $output;

		} else {
			echo $output;
			return $var;
		}
	}

	static function timer($name = NULL)
	{
		static $time = array();
		$now = microtime(TRUE);
		$delta = isset($time[$name]) ? $now - $time[$name] : 0;
		$time[$name] = $now;
		return $delta;
	}

	static function barDump($var, $title = NULL)
	{
		if (!self::$productionMode) {
			$dump = array();
			foreach ((is_array($var) ? $var : array('' => $var)) as $key => $val) {
				$dump[$key] = Helpers::clickableDump($val);
			}
			self::$dumpPanel->data[] = array('title' => $title, 'dump' => $dump);
		}
		return $var;
	}

	static function fireLog($message)
	{
		if (!self::$productionMode) {
			return self::$fireLogger->log($message);
		}
	}

	private static function isHtmlMode()
	{
		return !self::$ajaxDetected && !self::$consoleMode
			&& !preg_match('#^Content-Type: (?!text/html)#im', implode("\n", headers_list()));
	}

	static function addPanel(IBarPanel $panel, $id = NULL)
	{
		self::$bar->addPanel($panel, $id);
	}

}

class Logger extends Nette\Object
{
	const DEBUG = 'debug',
		INFO = 'info',
		WARNING = 'warning',
		ERROR = 'error',
		CRITICAL = 'critical';

	public static $emailSnooze = 172800;

	public $mailer = array(__CLASS__, 'defaultMailer');

	public $directory;

	public $email;

	function log($message, $priority = self::INFO)
	{
		if (!is_dir($this->directory)) {
			throw new Nette\DirectoryNotFoundException("Directory '$this->directory' is not found or is not directory.");
		}

		if (is_array($message)) {
			$message = implode(' ', $message);
		}
		$res = error_log(trim($message) . PHP_EOL, 3, $this->directory . '/' . strtolower($priority) . '.log');

		if (($priority === self::ERROR || $priority === self::CRITICAL) && $this->email && $this->mailer
			&& @filemtime($this->directory . '/email-sent') + self::$emailSnooze < time()
			&& @file_put_contents($this->directory . '/email-sent', 'sent')
		) {
			call_user_func($this->mailer, $message, $this->email);
		}
		return $res;
	}

	private static function defaultMailer($message, $email)
	{
		$host = isset($_SERVER['HTTP_HOST']) ? $_SERVER['HTTP_HOST'] :
				(isset($_SERVER['SERVER_NAME']) ? $_SERVER['SERVER_NAME'] : '');

		$parts = str_replace(
			array("\r\n", "\n"),
			array("\n", PHP_EOL),
			array(
				'headers' => "From: noreply@$host\nX-Mailer: Nette Framework\n",
				'subject' => "PHP: An error occurred on the server $host",
				'body' => "[" . @date('Y-m-d H:i:s') . "] $message",
			)
		);

		mail($email, $parts['subject'], $parts['body'], $parts['headers']);
	}

}

class FireLogger extends Nette\Object
{
	const DEBUG = 'debug',
		INFO = 'info',
		WARNING = 'warning',
		ERROR = 'error',
		CRITICAL = 'critical';

	private static $payload = array('logs' => array());

	static function log($message, $priority = self::DEBUG)
	{
		if (!isset($_SERVER['HTTP_X_FIRELOGGER']) || headers_sent()) {
			return FALSE;
		}

		$item = array(
			'name' => 'PHP',
			'level' => $priority,
			'order' => count(self::$payload['logs']),
			'time' => str_pad(number_format((microtime(TRUE) - Debugger::$time) * 1000, 1, '.', ' '), 8, '0', STR_PAD_LEFT) . ' ms',
			'template' => '',
			'message' => '',
			'style' => 'background:#767ab6',
		);

		$args = func_get_args();
		if (isset($args[0]) && is_string($args[0])) {
			$item['template'] = array_shift($args);
		}

		if (isset($args[0]) && $args[0] instanceof \Exception) {
			$e = array_shift($args);
			$trace = $e->getTrace();
			if (isset($trace[0]['class']) && $trace[0]['class'] === 'Nette\Diagnostics\Debugger'
				&& ($trace[0]['function'] === '_shutdownHandler' || $trace[0]['function'] === '_errorHandler')
			) {
				unset($trace[0]);
			}

			$file = str_replace(dirname(dirname(dirname($e->getFile()))), "\xE2\x80\xA6", $e->getFile());
			$item['template'] = ($e instanceof \ErrorException ? '' : get_class($e) . ': ')
				. $e->getMessage() . ($e->getCode() ? ' #' . $e->getCode() : '') . ' in ' . $file . ':' . $e->getLine();
			$item['pathname'] = $e->getFile();
			$item['lineno'] = $e->getLine();

		} else {
			$trace = debug_backtrace();
			if (isset($trace[1]['class']) && $trace[1]['class'] === 'Nette\Diagnostics\Debugger'
				&& ($trace[1]['function'] === 'fireLog')
			) {
				unset($trace[0]);
			}

			foreach ($trace as $frame) {
				if (isset($frame['file']) && is_file($frame['file'])) {
					$item['pathname'] = $frame['file'];
					$item['lineno'] = $frame['line'];
					break;
				}
			}
		}

		$item['exc_info'] = array('', '', array());
		$item['exc_frames'] = array();

		foreach ($trace as $frame) {
			$frame += array('file' => NULL, 'line' => NULL, 'class' => NULL, 'type' => NULL, 'function' => NULL, 'object' => NULL, 'args' => NULL);
			$item['exc_info'][2][] = array($frame['file'], $frame['line'], "$frame[class]$frame[type]$frame[function]", $frame['object']);
			$item['exc_frames'][] = $frame['args'];
		}

		if (isset($args[0]) && in_array($args[0], array(self::DEBUG, self::INFO, self::WARNING, self::ERROR, self::CRITICAL), TRUE)) {
			$item['level'] = array_shift($args);
		}

		$item['args'] = $args;

		self::$payload['logs'][] = self::jsonDump($item, -1);
		foreach (str_split(base64_encode(@json_encode(self::$payload)), 4990) as $k => $v) {
			header("FireLogger-de11e-$k:$v");
		}
		return TRUE;
	}

	private static function jsonDump(&$var, $level = 0)
	{
		if (is_bool($var) || is_null($var) || is_int($var) || is_float($var)) {
			return $var;

		} elseif (is_string($var)) {
			if (Debugger::$maxLen && strlen($var) > Debugger::$maxLen) {
				$var = substr($var, 0, Debugger::$maxLen) . " \xE2\x80\xA6 ";
			}
			return Nette\Utils\Strings::fixEncoding($var);

		} elseif (is_array($var)) {
			static $marker;
			if ($marker === NULL) {
				$marker = uniqid("\x00", TRUE);
			}
			if (isset($var[$marker])) {
				return "\xE2\x80\xA6RECURSION\xE2\x80\xA6";

			} elseif ($level < Debugger::$maxDepth || !Debugger::$maxDepth) {
				$var[$marker] = TRUE;
				$res = array();
				foreach ($var as $k => &$v) {
					if ($k !== $marker) {
						$res[self::jsonDump($k)] = self::jsonDump($v, $level + 1);
					}
			}
				unset($var[$marker]);
				return $res;

			} else {
				return " \xE2\x80\xA6 ";
		}

		} elseif (is_object($var)) {
			$arr = (array) $var;
			static $list = array();
			if (in_array($var, $list, TRUE)) {
				return "\xE2\x80\xA6RECURSION\xE2\x80\xA6";

			} elseif ($level < Debugger::$maxDepth || !Debugger::$maxDepth) {
				$list[] = $var;
				$res = array("\x00" => '(object) ' . get_class($var));
				foreach ($arr as $k => &$v) {
					if ($k[0] === "\x00") {
						$k = substr($k, strrpos($k, "\x00") + 1);
					}
					$res[self::jsonDump($k)] = self::jsonDump($v, $level + 1);
				}
				array_pop($list);
				return $res;

			} else {
				return " \xE2\x80\xA6 ";
			}

		} elseif (is_resource($var)) {
			return "resource " . get_resource_type($var);

		} else {
			return "unknown type";
		}
	}

}

class BlueScreen extends Nette\Object
{

	private $panels = array();

	function addPanel($panel, $id = NULL)
	{
		if ($id === NULL) {
			$this->panels[] = $panel;
		} else {
			$this->panels[$id] = $panel;
		}
	}

	function render(\Exception $exception)
	{
		$panels = $this->panels;

static $errorTypes = array(
	E_ERROR => 'Fatal Error',
	E_USER_ERROR => 'User Error',
	E_RECOVERABLE_ERROR => 'Recoverable Error',
	E_CORE_ERROR => 'Core Error',
	E_COMPILE_ERROR => 'Compile Error',
	E_PARSE => 'Parse Error',
	E_WARNING => 'Warning',
	E_CORE_WARNING => 'Core Warning',
	E_COMPILE_WARNING => 'Compile Warning',
	E_USER_WARNING => 'User Warning',
	E_NOTICE => 'Notice',
	E_USER_NOTICE => 'User Notice',
	E_STRICT => 'Strict',
	E_DEPRECATED => 'Deprecated',
	E_USER_DEPRECATED => 'User Deprecated',
);

$title = ($exception instanceof Nette\FatalErrorException && isset($errorTypes[$exception->getSeverity()])) ? $errorTypes[$exception->getSeverity()] : get_class($exception);

$expandPath = NETTE_DIR . DIRECTORY_SEPARATOR;
$counter = 0;

?><!-- "' --></script></style></pre></xmp></table>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="robots" content="noindex,noarchive">
	<meta name="generator" content="Nette Framework">

	<title><?php echo htmlspecialchars($title) ?></title><!-- <?php
		$ex = $exception; echo $ex->getMessage(), ($ex->getCode() ? ' #' . $ex->getCode() : '');
		while ((method_exists($ex, 'getPrevious') && $ex = $ex->getPrevious()) || (isset($ex->previous) && $ex = $ex->previous)) echo '; caused by ', get_class($ex), ' ', $ex->getMessage(), ($ex->getCode() ? ' #' . $ex->getCode() : '');
	?> -->

	<style type="text/css" class="nette">html{overflow-y:scroll}body{margin:0 0 2em;padding:0}#netteBluescreen{font:9pt/1.5 Verdana,sans-serif;background:white;color:#333;position:absolute;left:0;top:0;width:100%;z-index:23178;text-align:left}#netteBluescreen *{font:inherit;color:inherit;background:transparent;border:none;margin:0;padding:0;text-align:inherit;text-indent:0}#netteBluescreen b{font-weight:bold}#netteBluescreen i{font-style:italic}#netteBluescreen a{text-decoration:none;color:#328ADC;padding:2px 4px;margin:-2px -4px}#netteBluescreen a:hover,#netteBluescreen a:active,#netteBluescreen a:focus{color:#085AA3}#netteBluescreen a abbr{font-family:sans-serif;color:#BBB}#netteBluescreenIcon{position:absolute;right:.5em;top:.5em;z-index:23179;text-decoration:none;background:#CD1818;padding:3px}#netteBluescreenError{background:#CD1818;color:white;font:13pt/1.5 Verdana,sans-serif!important;display:block}#netteBluescreenError #netteBsSearch{color:#CD1818;font-size:.7em}#netteBluescreenError:hover #netteBsSearch{color:#ED8383}#netteBluescreen h1{font-size:18pt;font-weight:normal;text-shadow:1px 1px 0 rgba(0,0,0,.4);margin:.7em 0}#netteBluescreen h2{font:14pt/1.5 sans-serif!important;color:#888;margin:.6em 0}#netteBluescreen h3{font:bold 10pt/1.5 Verdana,sans-serif!important;margin:1em 0;padding:0}#netteBluescreen p,#netteBluescreen pre{margin:.8em 0}#netteBluescreen pre,#netteBluescreen code,#netteBluescreen table{font:9pt/1.5 Consolas,monospace!important}#netteBluescreen pre,#netteBluescreen table{background:#FDF5CE;padding:.4em .7em;border:1px dotted silver;overflow:auto}#netteBluescreen table pre{padding:0;margin:0;border:none}#netteBluescreen pre .php-array,#netteBluescreen pre .php-object{color:#C22}#netteBluescreen pre .php-string{color:#080}#netteBluescreen pre .php-int,#netteBluescreen pre .php-float,#netteBluescreen pre .php-null,#netteBluescreen pre .php-bool{color:#328ADC}#netteBluescreen pre .php-visibility{font-size:85%;color:#998}#netteBluescreen pre.nette-dump a{color:#333}#netteBluescreen div.panel{padding:1px 25px}#netteBluescreen div.inner{background:#F4F3F1;padding:.1em 1em 1em;border-radius:8px;-moz-border-radius:8px;-webkit-border-radius:8px}#netteBluescreen table{border-collapse:collapse;width:100%}#netteBluescreen .outer{overflow:auto}#netteBluescreen td,#netteBluescreen th{vertical-align:top;text-align:left;padding:2px 6px;border:1px solid #e6dfbf}#netteBluescreen th{width:10%;font-weight:bold}#netteBluescreen tr:nth-child(2n),#netteBluescreen tr:nth-child(2n) pre{background-color:#F7F0CB}#netteBluescreen ol{margin:1em 0;padding-left:2.5em}#netteBluescreen ul{font:7pt/1.5 Verdana,sans-serif!important;padding:2em 4em;margin:1em 0 0;color:#777;background:#F6F5F3 url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFEAAAAjCAMAAADbuxbOAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAADBQTFRF/fz24d7Y7Onj5uLd9vPu3drUzMvG09LN39zW8e7o2NbQ3NnT29jS0M7J1tXQAAAApvmsFgAAABB0Uk5T////////////////////AOAjXRkAAAKlSURBVHja7FbbsqQgDAwENEgc//9vN+SCWDtbtXPmZR/Wc6o02mlC58LA9ckFAOszvMV8xNgyUjyXhojfMVKvRL0ZHavxXYy5JrmchMdzou8YlTClxajtK8ZGGpWRoBr1+gFjKfHkJPbizabLgzE3pH7Iu4K980xgFvlrVzMZoVBWhtvouCDdcTDmTgMCJdVxJ9MKO6XxnliM7hxi5lbj2ZVM4l8DqYyKoNLYcfqBB1/LpHYxEcfVG6ZpMDgyFUVWY/Q1sSYPpIdSAKWqLWL0XqWiMWc4hpH0OQOMOAgdycY4N9Sb7wWANQs3rsDSdLAYiuxi5siVfOhBWIrtH0G3kNaF/8Q4kCPE1kMucG/ZMUBUCOgiKJkPuWWTLGVgLGpwns1DraUayCtoBqERyaYtVsm85NActRooezvSLO/sKZP/nq8n4+xcyjNsRu8zW6KWpdb7wjiQd4WrtFZYFiKHENSmWp6xshh96c2RQ+c7Lt+qbijyEjHWUJ/pZsy8MGIUuzNiPySK2Gqoh6ZTRF6ko6q3nVTkaA//itIrDpW6l3SLo8juOmqMXkYknu5FdQxWbhCfKHEGDhxxyTVaXJF3ZjSl3jMksjSOOKmne9pI+mcG5QvaUJhI9HpkmRo2NpCrDJvsktRhRE2MM6F2n7dt4OaMUq8bCctk0+PoMRzL+1l5PZ2eyM/Owr86gf8z/tOM53lom5+nVcFuB+eJVzlXwAYy9TZ9s537tfqcsJWbEU4nBngZo6FfO9T9CdhfBtmk2dLiAy8uS4zwOpMx2HqYbTC+amNeAYTpsP4SIgvWfUBWXxn3CMHW3ffd7k3+YIkx7w0t/CVGvcPejoeOlzOWzeGbawOHqXQGUTMZRcfj4XPCgW9y/fuvVn8zD9P1QHzv80uAAQA0i3Jer7Jr7gAAAABJRU5ErkJggg==') 99% 10px no-repeat;border-top:1px solid #DDD}#netteBluescreen .highlight{background:#CD1818;color:white;font-weight:bold;font-style:normal;display:block;padding:0 .4em;margin:0 -.4em}#netteBluescreen .line{color:#9F9C7F;font-weight:normal;font-style:normal}#netteBluescreen a[href^=editor\:]{color:inherit;border-bottom:1px dotted #C1D2E1}</style>
</head>

<body>
<div id="netteBluescreen">
	<a id="netteBluescreenIcon" href="#" rel="next"><abbr>&#x25bc;</abbr></a

	><div>
		<div id="netteBluescreenError" class="panel">
			<h1><?php echo htmlspecialchars($title), ($exception->getCode() ? ' #' . $exception->getCode() : '') ?></h1>

			<p><?php echo htmlspecialchars($exception->getMessage()) ?> <a href="http://www.google.cz/search?sourceid=nette&amp;q=<?php echo urlencode($title . ' ' . preg_replace('#\'.*\'|".*"#Us', '', $exception->getMessage())) ?>" id="netteBsSearch">search&#x25ba;</a></p>
		</div>

		<?php $ex = $exception; $level = 0; ?>
		<?php do { ?>

			<?php if ($level++): ?>
			<div class="panel">
			<h2><a href="#" rel="netteBsPnl<?php echo ++$counter ?>">Caused by <abbr><?php echo ($collapsed = $level > 2) ? '&#x25ba;' : '&#x25bc;' ?></abbr></a></h2>

			<div id="netteBsPnl<?php echo $counter ?>" class="<?php echo $collapsed ? 'nette-collapsed ' : '' ?>inner">
				<div class="panel">
					<h1><?php echo htmlspecialchars(get_class($ex)), ($ex->getCode() ? ' #' . $ex->getCode() : '') ?></h1>

					<p><b><?php echo htmlspecialchars($ex->getMessage()) ?></b></p>
				</div>
			<?php endif ?>

			<?php foreach ($panels as $panel): ?>
			<?php $panel = call_user_func($panel, $ex); if (empty($panel['tab']) || empty($panel['panel'])) continue; ?>
			<div class="panel">
				<h2><a href="#" rel="netteBsPnl<?php echo ++$counter ?>"><?php echo htmlSpecialChars($panel['tab']) ?> <abbr>&#x25bc;</abbr></a></h2>

				<div id="netteBsPnl<?php echo $counter ?>" class="inner">
				<?php echo $panel['panel'] ?>
			</div></div>
			<?php endforeach ?>

			<?php $stack = $ex->getTrace(); $expanded = NULL ?>
			<?php if (strpos($ex->getFile(), $expandPath) === 0) {
				foreach ($stack as $key => $row) {
					if (isset($row['file']) && strpos($row['file'], $expandPath) !== 0) { $expanded = $key; break; }
				}
			} ?>

			<div class="panel">
			<h2><a href="#" rel="netteBsPnl<?php echo ++$counter ?>">Source file <abbr><?php echo ($collapsed = $expanded !== NULL) ? '&#x25ba;' : '&#x25bc;' ?></abbr></a></h2>

			<div id="netteBsPnl<?php echo $counter ?>" class="<?php echo $collapsed ? 'nette-collapsed ' : '' ?>inner">
				<p><b>File:</b> <?php echo Helpers::editorLink($ex->getFile(), $ex->getLine()) ?> &nbsp; <b>Line:</b> <?php echo $ex->getLine() ?></p>
				<?php if (is_file($ex->getFile())): ?><pre><?php echo self::highlightFile($ex->getFile(), $ex->getLine(), 15, isset($ex->context) ? $ex->context : NULL) ?></pre><?php endif ?>
			</div></div>

			<?php if (isset($stack[0]['class']) && $stack[0]['class'] === 'Nette\Diagnostics\Debugger' && ($stack[0]['function'] === '_shutdownHandler' || $stack[0]['function'] === '_errorHandler')) unset($stack[0]) ?>
			<?php if ($stack): ?>
			<div class="panel">
				<h2><a href="#" rel="netteBsPnl<?php echo ++$counter ?>">Call stack <abbr>&#x25bc;</abbr></a></h2>

				<div id="netteBsPnl<?php echo $counter ?>" class="inner">
				<ol>
					<?php foreach ($stack as $key => $row): ?>
					<li><p>

					<?php if (isset($row['file']) && is_file($row['file'])): ?>
						<?php echo Helpers::editorLink($row['file'], $row['line']), ':', $row['line'] ?>
					<?php else: ?>
						<i>inner-code</i><?php if (isset($row['line'])) echo ':', $row['line'] ?>
					<?php endif ?>

					<?php if (isset($row['file']) && is_file($row['file'])): ?><a href="#" rel="netteBsSrc<?php echo "$level-$key" ?>">source <abbr>&#x25ba;</abbr></a>&nbsp; <?php endif ?>

					<?php if (isset($row['class'])) echo $row['class'] . $row['type'] ?>
					<?php echo $row['function'] ?>

					(<?php if (!empty($row['args'])): ?><a href="#" rel="netteBsArgs<?php echo "$level-$key" ?>">arguments <abbr>&#x25ba;</abbr></a><?php endif ?>)
					</p>

					<?php if (!empty($row['args'])): ?>
						<div class="nette-collapsed outer" id="netteBsArgs<?php echo "$level-$key" ?>">
						<table>
						<?php
						try {
							$r = isset($row['class']) ? new \ReflectionMethod($row['class'], $row['function']) : new \ReflectionFunction($row['function']);
							$params = $r->getParameters();
						} catch (\Exception $e) {
							$params = array();
						}
						foreach ($row['args'] as $k => $v) {
							echo '<tr><th>', (isset($params[$k]) ? '$' . $params[$k]->name : "#$k"), '</th><td>';
							echo Helpers::clickableDump($v);
							echo "</td></tr>\n";
						}
						?>
						</table>
						</div>
					<?php endif ?>

					<?php if (isset($row['file']) && is_file($row['file'])): ?>
						<pre <?php if ($expanded !== $key) echo 'class="nette-collapsed"'; ?> id="netteBsSrc<?php echo "$level-$key" ?>"><?php echo self::highlightFile($row['file'], $row['line']) ?></pre>
					<?php endif ?>

					</li>
					<?php endforeach ?>
				</ol>
			</div></div>
			<?php endif ?>

			<?php if (isset($ex->context) && is_array($ex->context)):?>
			<div class="panel">
			<h2><a href="#" rel="netteBsPnl<?php echo ++$counter ?>">Variables <abbr>&#x25ba;</abbr></a></h2>

			<div id="netteBsPnl<?php echo $counter ?>" class="nette-collapsed inner">
			<div class="outer">
			<table>
			<?php
			foreach ($ex->context as $k => $v) {
					echo '<tr><th>$', htmlspecialchars($k), '</th><td>', Helpers::clickableDump($v), "</td></tr>\n";
			}
			?>
			</table>
			</div>
			</div></div>
			<?php endif ?>

		<?php } while ((method_exists($ex, 'getPrevious') && $ex = $ex->getPrevious()) || (isset($ex->previous) && $ex = $ex->previous)); ?>
		<?php while (--$level) echo '</div></div>' ?>

		<?php foreach ($panels as $panel): ?>
		<?php $panel = call_user_func($panel, NULL); if (empty($panel['tab']) || empty($panel['panel'])) continue; ?>
		<div class="panel">
			<h2><a href="#" rel="netteBsPnl<?php echo ++$counter ?>"><?php echo htmlSpecialChars($panel['tab']) ?> <abbr>&#x25ba;</abbr></a></h2>

			<div id="netteBsPnl<?php echo $counter ?>" class="nette-collapsed inner">
			<?php echo $panel['panel'] ?>
		</div></div>
		<?php endforeach ?>

		<div class="panel">
		<h2><a href="#" rel="netteBsPnl<?php echo ++$counter ?>">Environment <abbr>&#x25ba;</abbr></a></h2>

		<div id="netteBsPnl<?php echo $counter ?>" class="nette-collapsed inner">
			<?php
			$list = get_defined_constants(TRUE);
			if (!empty($list['user'])):?>
			<h3><a href="#" rel="netteBsPnl-env-const">Constants <abbr>&#x25bc;</abbr></a></h3>
			<div class="outer">
			<table id="netteBsPnl-env-const">
			<?php
			foreach ($list['user'] as $k => $v) {
				echo '<tr><th>', htmlspecialchars($k), '</th>';
				echo '<td>', Helpers::clickableDump($v), "</td></tr>\n";
			}
			?>
			</table>
			</div>
			<?php endif ?>

			<h3><a href="#" rel="netteBsPnl-env-files">Included files <abbr>&#x25ba;</abbr></a> (<?php echo count(get_included_files()) ?>)</h3>
			<div class="outer">
			<table id="netteBsPnl-env-files" class="nette-collapsed">
			<?php
			foreach (get_included_files() as $v) {
				echo '<tr><td>', htmlspecialchars($v), "</td></tr>\n";
			}
			?>
			</table>
			</div>

			<h3>$_SERVER</h3>
			<?php if (empty($_SERVER)):?>
			<p><i>empty</i></p>
			<?php else: ?>
			<div class="outer">
			<table>
			<?php
			foreach ($_SERVER as $k => $v) echo '<tr><th>', htmlspecialchars($k), '</th><td>', Helpers::clickableDump($v), "</td></tr>\n";
			?>
			</table>
			</div>
			<?php endif ?>
		</div></div>

		<div class="panel">
		<h2><a href="#" rel="netteBsPnl<?php echo ++$counter ?>">HTTP request <abbr>&#x25ba;</abbr></a></h2>

		<div id="netteBsPnl<?php echo $counter ?>" class="nette-collapsed inner">
			<?php if (function_exists('apache_request_headers')): ?>
			<h3>Headers</h3>
			<div class="outer">
			<table>
			<?php
			foreach (apache_request_headers() as $k => $v) echo '<tr><th>', htmlspecialchars($k), '</th><td>', htmlspecialchars($v), "</td></tr>\n";
			?>
			</table>
			</div>
			<?php endif ?>

			<?php foreach (array('_GET', '_POST', '_COOKIE') as $name): ?>
			<h3>$<?php echo $name ?></h3>
			<?php if (empty($GLOBALS[$name])):?>
			<p><i>empty</i></p>
			<?php else: ?>
			<div class="outer">
			<table>
			<?php
			foreach ($GLOBALS[$name] as $k => $v) echo '<tr><th>', htmlspecialchars($k), '</th><td>', Helpers::clickableDump($v), "</td></tr>\n";
			?>
			</table>
			</div>
			<?php endif ?>
			<?php endforeach ?>
		</div></div>

		<div class="panel">
		<h2><a href="#" rel="netteBsPnl<?php echo ++$counter ?>">HTTP response <abbr>&#x25ba;</abbr></a></h2>

		<div id="netteBsPnl<?php echo $counter ?>" class="nette-collapsed inner">
			<h3>Headers</h3>
			<?php if (headers_list()): ?>
			<pre><?php
			foreach (headers_list() as $s) echo htmlspecialchars($s), '<br>';
			?></pre>
			<?php else: ?>
			<p><i>no headers</i></p>
			<?php endif ?>
		</div></div>

		<ul>
			<li>Report generated at <?php echo @date('Y/m/d H:i:s', Debugger::$time) ?></li>
			<?php if (preg_match('#^https?://#', Debugger::$source)): ?>
				<li><a href="<?php echo htmlSpecialChars(Debugger::$source) ?>"><?php echo htmlSpecialChars(Debugger::$source) ?></a></li>
			<?php elseif (Debugger::$source): ?>
				<li><?php echo htmlSpecialChars(Debugger::$source) ?></li>
			<?php endif ?>
			<li>PHP <?php echo htmlSpecialChars(PHP_VERSION) ?></li>
			<?php if (isset($_SERVER['SERVER_SOFTWARE'])): ?><li><?php echo htmlSpecialChars($_SERVER['SERVER_SOFTWARE']) ?></li><?php endif ?>
			<?php if (class_exists('Nette\Framework')): ?><li><?php echo htmlSpecialChars('Nette Framework ' . Nette\Framework::VERSION) ?> <i>(revision <?php echo htmlSpecialChars(Nette\Framework::REVISION) ?>)</i></li><?php endif ?>
		</ul>
	</div>
</div>

</body>
</html>
<?php
	}

	static function highlightFile($file, $line, $count = 15, $vars = array())
	{
		if (function_exists('ini_set')) {
			ini_set('highlight.comment', '#998; font-style: italic');
			ini_set('highlight.default', '#000');
			ini_set('highlight.html', '#06B');
			ini_set('highlight.keyword', '#D24; font-weight: bold');
			ini_set('highlight.string', '#080');
		}

		$start = max(1, $line - floor($count / 2));

		$source = @file_get_contents($file);
		if (!$source) {
			return;
		}
		$source = explode("\n", highlight_string($source, TRUE));
		$spans = 1;
		$out = $source[0];
		$source = explode('<br />', $source[1]);
		array_unshift($source, NULL);

		$i = $start;
		while (--$i >= 1) {
			if (preg_match('#.*(</?span[^>]*>)#', $source[$i], $m)) {
				if ($m[1] !== '</span>') {
					$spans++; $out .= $m[1];
				}
				break;
			}
		}

		$source = array_slice($source, $start, $count, TRUE);
		end($source);
		$numWidth = strlen((string) key($source));

		foreach ($source as $n => $s) {
			$spans += substr_count($s, '<span') - substr_count($s, '</span');
			$s = str_replace(array("\r", "\n"), array('', ''), $s);
			preg_match_all('#<[^>]+>#', $s, $tags);
			if ($n === $line) {
				$out .= sprintf(
					"<span class='highlight'>%{$numWidth}s:    %s\n</span>%s",
					$n,
					strip_tags($s),
					implode('', $tags[0])
				);
			} else {
				$out .= sprintf("<span class='line'>%{$numWidth}s:</span>    %s\n", $n, $s);
			}
		}
		$out .= str_repeat('</span>', $spans) . '</code>';

		$out = preg_replace_callback('#">\$(\w+)(&nbsp;)?</span>#', function($m) use($vars) {
			return isset($vars[$m[1]])
				? '" title="' . str_replace('"', '&quot;', strip_tags(Helpers::htmlDump($vars[$m[1]]))) . $m[0]
				: $m[0];
		}, $out);

		return $out;
	}

}

class Bar extends Nette\Object
{

	private $panels = array();

	function addPanel(IBarPanel $panel, $id = NULL)
	{
		if ($id === NULL) {
			$c = 0;
			do {
				$id = get_class($panel) . ($c++ ? "-$c" : '');
			} while (isset($this->panels[$id]));
		}
		$this->panels[$id] = $panel;
	}

	function render()
	{
		$panels = array();
		foreach ($this->panels as $id => $panel) {
			try {
				$panels[] = array(
					'id' => preg_replace('#[^a-z0-9]+#i', '-', $id),
					'tab' => $tab = (string) $panel->getTab(),
					'panel' => $tab ? (string) $panel->getPanel() : NULL,
				);
			} catch (\Exception $e) {
				$panels[] = array(
					'id' => "error-$id",
					'tab' => "Error: $id",
					'panel' => nl2br(htmlSpecialChars((string) $e)),
				);
			}
		}

?>

<!-- Nette Debug Bar -->

<?php ob_start() ?>
&nbsp;

<style id="nette-debug-style" class="nette">#nette-debug{display:none;position:fixed}body#nette-debug{margin:5px 5px 0;display:block}#nette-debug *{font:inherit;color:inherit;background:transparent;margin:0;padding:0;border:none;text-align:inherit;list-style:inherit}#nette-debug .nette-fixed-coords{position:fixed;_position:absolute;right:0;bottom:0;max-width:100%}#nette-debug a{color:#125EAE;text-decoration:none}#nette-debug .nette-panel a{color:#125EAE;text-decoration:none}#nette-debug a:hover,#nette-debug a:active,#nette-debug a:focus{background-color:#125EAE;color:white}#nette-debug .nette-panel h2,#nette-debug .nette-panel h3,#nette-debug .nette-panel p{margin:.4em 0}#nette-debug .nette-panel table{border-collapse:collapse;background:#FDF5CE}#nette-debug .nette-panel tr:nth-child(2n) td{background:#F7F0CB}#nette-debug .nette-panel td,#nette-debug .nette-panel th{border:1px solid #E6DFBF;padding:2px 5px;vertical-align:top;text-align:left}#nette-debug .nette-panel th{background:#F4F3F1;color:#655E5E;font-size:90%;font-weight:bold}#nette-debug .nette-panel pre,#nette-debug .nette-panel code{font:9pt/1.5 Consolas,monospace}#nette-debug table .nette-right{text-align:right}.nette-hidden,.nette-collapsed{display:none}#nette-debug-bar{font:normal normal 12px/21px Tahoma,sans-serif;color:#333;border:1px solid #c9c9c9;background:#EDEAE0 url('data:image/png;base64,R0lGODlhAQAVALMAAOTh1/Px6eHe1fHv5e/s4vLw6Ofk2u3q4PPw6PPx6PDt5PLw5+Dd1OXi2Ojm3Orn3iH5BAAAAAAALAAAAAABABUAAAQPMISEyhpYkfOcaQAgCEwEADs=') top;position:relative;overflow:auto;min-height:21px;_float:left;min-width:50px;white-space:nowrap;z-index:23181;opacity:.9;border-radius:3px;-moz-border-radius:3px;box-shadow:1px 1px 10px rgba(0,0,0,.15);-moz-box-shadow:1px 1px 10px rgba(0,0,0,.15);-webkit-box-shadow:1px 1px 10px rgba(0,0,0,.15)}#nette-debug-bar:hover{opacity:1}#nette-debug-bar ul{list-style:none none;margin-left:4px}#nette-debug-bar li{float:left}#nette-debug-bar img{vertical-align:middle;position:relative;top:-1px;margin-right:3px}#nette-debug-bar li a{color:#000;display:block;padding:0 4px}#nette-debug-bar li a:hover{color:black;background:#c3c1b8}#nette-debug-bar li .nette-warning{color:#D32B2B;font-weight:bold}#nette-debug-bar li>span{padding:0 4px}#nette-debug-logo{background:url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAC0AAAAPCAYAAABwfkanAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAABiFJREFUSMe1VglPlGcQ5i+1xjZNqxREtGq8ahCPWsVGvEDBA1BBRQFBDjkE5BYUzwpovRBUREBEBbl3OVaWPfj2vi82eTrvbFHamLRJ4yYTvm+u95mZZ96PoKAv+LOatXBYZ+Bx6uFy6DGnt1m0EOKwSmQzwmHTgX5B/1W+yM9GYJ02CX6/B/5ZF+w2A4x6FYGTYDVp4PdY2Tbrs5N+mnRa2Km4/wV6rhPzQQj5fDc1mJM5nd0iYdZtQWtrCxobGnDpUiledTynbuvg99mgUMhw924Trl2rR01NNSTNJE9iDpTV8innv4K2kZPLroPXbYLHZeSu2K1aeF0muJ2GvwGzmNSwU2E+svm8ZrgdBliMaha/34Vx+RAKCgpwpa4OdbW1UE/L2cc/68WtWzdRVlaG6uoqtD1/BA/pA1MIxLvtes7pc5vhoDOE/rOgbVSdf9aJWa8dBp0Kyg+jdLiTx2vQKWEyqGmcNkqg4iTC1+dzQatWkK+cJqPD7KyFaKEjvRuNjY24fLkGdXW1ePjwAeX4QHonDNI0A75+/RpqqqshH+6F2UAUMaupYXouykV0mp6SQ60coxgL8Z4aMg/4x675/V60v3jKB+Xl5WJibIC4KPEIS0qKqWv5GOh7BZ/HSIk9kA33o7y8DOfPZ6GQOipkXDZAHXKxr4ipqqpkKS6+iIrycgz2dyMnJxtVlZUsotNZWZmor79KBbvgpdjm5sfIzc1hv4L8fKJPDTfJZZc+gRYKr8sAEy2DcBRdEEk62ltx9uwZ5qNILoDU1l6mbrvx5EkzUlKSuTiR7PHjR3x4fv4FyIbeIic7G5WVFUyN+qtX+Lnt2SPcvn2LfURjhF7kE4WPDr+Bx+NEUVEhkpNPoImm5CSOl5aUIC3tLOMR59gtAY4HidGIzj14cB8ZGRkM8kJeHk6cOI4xWR8vSl5uLlJTT6O74xnT5lB8PM6cSYXVqILb5UBWZiYSExMYkE4zzjqX00QHG+h9AjPqMei0k3ywy2khMdNiq6BVCf04T6ekuBgJCUdRUVHOBQwPvkNSUiLjaGi4Q/5qFgYtHgTXRJdTT59GenoaA5gY64deq0Bc3EGuNj4+DnppEheLijhZRkY6SktLsGPHdi6irOwSFTRAgO04deokTSIFsbExuHfvLnFSx8DevelAfFwcA0lJTqZi5PDS9aci/sbE7Oe4wsICbtD27b/ye1NTI3FeSX4W2gdFALRD3A4eM44ePcKViuD79/8gnZP5Kg4+cCAW2dnnqUM2Lujw4UM4ePAA2ztfPsHIYA/sdOt43A50d7UFCjkUj+joXVBMDJDeDhcVk08cjd61C3v37uFYp8PKXX3X8xJRUTtw7FgSn3Xzxg10d7ZCqRjkM+02C7pettDNogqAFjzxuI3YHR2Nffv2coXy0V44HGZERm7kJNu2/cK8bW9rwbp1axnMnj27uUijQQOb1QyTcYZ3YMOGn/Hbzp1crAAvaDfY38O5hW3//n0ce+TIYWiUcub1xo0R2Lp1y8cYsUMWM125VhPe93Zj7do1vEPi26GfUdBFbhK8tGHrli1YsWwpgoOD0dXRQqAtXMCy8DBs3rwJoSGLsWrVclylBdoUGYlVK1dg9eqVCFsSSs8/4btvvmUwEnE0KTERISE/IiIiAsGLF2HhwgU8qbc97QgPX8qFr1mzGgu+/opzdL5o5l1aEhqC9evXYWlYKFYsD6e/YVj0w/dMGZVyBDMqeaDTRuKpkxYjIz2dOyeup6H3r2kkOuJ1H3N5Z1QUzp3LQF9vJ4xGLQYHXiM9LY0pEhsTg+PHj9HNcJu4OcL3uaQZY86LiZw8mcJTkmhBTUYJbU8fcoygobgWR4Z6iKtTPLE7d35HYkICT1dIZuY59HQ9412StBPQTMvw8Z6WaMNFxy3Gab4TeQT0M9IHwUT/G0i0MGIJ9CTiJjBIH+iQaQbC7+QnfEXiQL6xgF09TjETHCt8RbeMuil+D8RNsV1LHdQoZfR/iJJzCZuYmEE/Bd3MJNs/+0UURgFWJJ//aQ8k+CsxVTqnVytHObkQrUoG8T4/bs4u4ubbxLPwFzYNPc8HI2zijLm84l39Dx8hfwJenFezFBKKQwAAAABJRU5ErkJggg==') 0 50% no-repeat;min-width:45px;cursor:move}#nette-debug-logo span{display:none}#nette-debug-bar-bgl,#nette-debug-bar-bgx,#nette-debug-bar-bgr{position:absolute;z-index:-1;top:-7px;height:37px}#nette-debug .nette-panel{font:normal normal 12px/1.5 sans-serif;background:white;color:#333}#nette-debug h1{font:normal normal 23px/1.4 Tahoma,sans-serif;color:#575753;background:#EDEAE0;margin:-5px -5px 5px;padding:0 25px 5px 5px}#nette-debug .nette-mode-peek .nette-inner,#nette-debug .nette-mode-float .nette-inner{max-width:700px;max-height:500px;overflow:auto}#nette-debug .nette-panel .nette-icons{display:none}#nette-debug .nette-mode-peek{display:none;position:relative;z-index:23180;padding:5px;min-width:150px;min-height:50px;border:5px solid #EDEAE0;border-radius:5px;-moz-border-radius:5px}#nette-debug .nette-mode-peek h1{cursor:move}#nette-debug .nette-mode-float{position:relative;z-index:23179;padding:5px;min-width:150px;min-height:50px;border:5px solid #EDEAE0;border-radius:5px;-moz-border-radius:5px;opacity:.9;box-shadow:1px 1px 6px #666;-moz-box-shadow:1px 1px 6px rgba(0,0,0,.45);-webkit-box-shadow:1px 1px 6px #666}#nette-debug .nette-focused{z-index:23180;opacity:1}#nette-debug .nette-mode-float h1{cursor:move}#nette-debug .nette-mode-float .nette-icons{display:block;position:absolute;top:0;right:0;font-size:18px}#nette-debug .nette-icons a{color:#575753}#nette-debug .nette-icons a:hover{color:white}pre.nette-dump{color:#444;background:white}pre.nette-dump .php-array,pre.nette-dump .php-object{color:#C22}pre.nette-dump .php-string{color:#080}pre.nette-dump .php-int,pre.nette-dump .php-float{color:#37D}pre.nette-dump .php-null,pre.nette-dump .php-bool{color:black}pre.nette-dump .php-visibility{font-size:85%;color:#999}</style>

<!--[if lt IE 8]><style class="nette">#nette-debug-bar img{display:none}#nette-debug-bar li{border-left:1px solid #DCD7C8;padding:0 3px}#nette-debug-logo span{background:#edeae0;display:inline}</style><![endif]-->

<?php foreach ($panels as $id => $panel): if (!$panel['panel']) continue; ?>
<div class="nette-fixed-coords">
	<div class="nette-panel" id="nette-debug-panel-<?php echo $panel['id'] ?>">
		<?php echo $panel['panel'] ?>
		<div class="nette-icons">
			<a href="#" title="open in window">&curren;</a>
			<a href="#" rel="close" title="close window">&times;</a>
		</div>
	</div>
</div>
<?php endforeach ?>

<div class="nette-fixed-coords">
	<div id="nette-debug-bar">
		<ul>
			<li id="nette-debug-logo" title="PHP <?php echo htmlSpecialChars(PHP_VERSION . " |\n"
			. (isset($_SERVER['SERVER_SOFTWARE']) ? $_SERVER['SERVER_SOFTWARE'] . " |\n" : '')
			. (class_exists('Nette\Framework') ? 'Nette Framework ' . Nette\Framework::VERSION . ' (' . substr(Nette\Framework::REVISION, 8) . ')' : '')) ?>">&nbsp;<span>Nette Framework</span></li>
			<?php foreach ($panels as $panel): if (!$panel['tab']) continue; ?>
			<li><?php if ($panel['panel']): ?><a href="#" rel="<?php echo $panel['id'] ?>"><?php echo trim($panel['tab']) ?></a><?php else: echo '<span>', trim($panel['tab']), '</span>'; endif ?></li>
			<?php endforeach ?>
			<li><a href="#" rel="close" title="close debug bar">&times;</a></li>
		</ul>
	</div>
</div>
<?php $output = ob_get_clean(); ?>

<div id="nette-debug"></div>

<!-- /Nette Debug Bar -->
<?php
	}

}

final class DefaultBarPanel extends Nette\Object implements IBarPanel
{
	private $id;

	public $data;

	function __construct($id)
	{
		$this->id = $id;
	}

	function getTab()
	{
		ob_start();
		$data = $this->data;
		if ($this->id === 'time') {

?>
<span title="Execution time"><img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAJ6SURBVDjLjZO7T1NhGMY7Mji6uJgYt8bElTjof6CDg4sMSqIxJsRGB5F4TwQSIg1QKC0KWmkZEEsKtEcSxF5ohV5pKSicXqX3aqGn957z+PUEGopiGJ583/A+v3znvPkJAAjWR0VNJG0kGhKahCFhXcN3YBFfx8Kry6ym4xIzce88/fbWGY2k5WRb77UTTbWuYA9gDGg7EVmSIOF4g5T7HZKuMcSW5djWDyL0uRf0dCc8inYYxTcw9fAiCMBYB3gVj1z7gLhNTjKCqHkYP79KENC9Bq3uxrrqORzy+9D3tPAAccspVx1gWg0KbaZFbGllWFM+xrKkFQudV0CeDfJsjN4+C2nracjunoPq5VXIBrowMK4V1gG1LGyWdbZwCalsBYUyh2KFQzpXxVqkAGswD3+qBDpZwow9iYE5v26/VwfUQnnznyhvjguQYabIIpKpYD1ahI8UTT92MUSFuP5Z/9TBTgOgFrVjp3nakaG/0VmEfpX58pwzjUEquNk362s+PP8XYD/KpYTBHmRg9Wch0QX1R80dCZhYipudYQY2Auib8RmODVCa4hfUK4ngaiiLNFNFdKeCWWscXZMbWy9Unv9/gsIQU09a4pwvUeA3Uapy2C2wCKXL0DqTePLexbWPOv79E8f0UWrencZ2poxciUWZlKssB4bcHeE83NsFuMgpo2iIpMuNa1TNu4XjhggWvb+R2K3wZdLlAZl8Fd9jRb5sD+Xx0RJBx5gdom6VsMEFDyWF0WyCeSOFcDKPnRxZYTQL5Rc/nn1w4oFsBaIhC3r6FRh5erPRhYMyHdeFw4C6zkRhmijM7CnMu0AUZonCDCnRJBqSus5/ABD6Ba5CkQS8AAAAAElFTkSuQmCC"
/><?php echo number_format((microtime(TRUE) - Debugger::$time) * 1000, 1, '.', ' ') ?> ms</span>
<?php
		} elseif ($this->id === 'memory') {

?>
<span title="The peak of allocated memory"><img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAGvSURBVDjLpZO7alZREEbXiSdqJJDKYJNCkPBXYq12prHwBezSCpaidnY+graCYO0DpLRTQcR3EFLl8p+9525xgkRIJJApB2bN+gZmqCouU+NZzVef9isyUYeIRD0RTz482xouBBBNHi5u4JlkgUfx+evhxQ2aJRrJ/oFjUWysXeG45cUBy+aoJ90Sj0LGFY6anw2o1y/mK2ZS5pQ50+2XiBbdCvPk+mpw2OM/Bo92IJMhgiGCox+JeNEksIC11eLwvAhlzuAO37+BG9y9x3FTuiWTzhH61QFvdg5AdAZIB3Mw50AKsaRJYlGsX0tymTzf2y1TR9WwbogYY3ZhxR26gBmocrxMuhZNE435FtmSx1tP8QgiHEvj45d3jNlONouAKrjjzWaDv4CkmmNu/Pz9CzVh++Yd2rIz5tTnwdZmAzNymXT9F5AtMFeaTogJYkJfdsaaGpyO4E62pJ0yUCtKQFxo0hAT1JU2CWNOJ5vvP4AIcKeao17c2ljFE8SKEkVdWWxu42GYK9KE4c3O20pzSpyyoCx4v/6ECkCTCqccKorNxR5uSXgQnmQkw2Xf+Q+0iqQ9Ap64TwAAAABJRU5ErkJggg=="
/><?php echo function_exists('memory_get_peak_usage') ? number_format(memory_get_peak_usage() / 1000000, 2, '.', ' ') : 'n/a'; ?> MB</span>
<?php
		} elseif ($this->id === 'dumps' && $this->data) {

?>
<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAIASURBVDjLpVPPaxNREJ6Vt01caH4oWk1T0ZKlGIo9RG+BUsEK4kEP/Q8qPXnpqRdPBf8A8Wahhx7FQ0GF9FJ6UksqwfTSBDGyB5HkkphC9tfb7jfbtyQQTx142byZ75v5ZnZWC4KALmICPy+2DkvKIX2f/POz83LxCL7nrz+WPNcll49DrhM9v7xdO9JW330DuXrrqkFSgig5iR2Cfv3t3gNxOnv5BwU+eZ5HuON5/PMPJZKJ+yKQfpW0S7TxdC6WJaWkyvff1LDaFRAeLZj05MHsiPTS6hua0PUqtwC5sHq9zv9RYWl+nu5cETcnJ1M0M5WlWq3GsX6/T+VymRzHDluZiGYAAsw0TQahV8uyyGq1qFgskm0bHIO/1+sx1rFtchJhArwEyIQ1Gg2WD2A6nWawHQJVDIWgIJfLhQowTIeE9D0mKAU8qPC0220afsWFQoH93W6X7yCDJ+DEBeBmsxnPIJVKxWQVUwry+XyUwBlKMKwA8jqdDhOVCqVAzQDVvXAXhOdGBFgymYwrGoZBmUyGjxCCdF0fSahaFdgoTHRxfTveMCXvWfkuE3Y+f40qhgT/nMitupzApdvT18bu+YeDQwY9Xl4aG9/d/URiMBhQq/dvZMeVghtT17lSZW9/rAKsvPa/r9Fc2dw+Pe0/xI6kM9mT5vtXy+Nw2kU/5zOGRpvuMIu0YAAAAABJRU5ErkJggg==" />variables
<?php
		} elseif ($this->id === 'errors' && $this->data) {

?>
<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAIsSURBVDjLpVNLSJQBEP7+h6uu62vLVAJDW1KQTMrINQ1vPQzq1GOpa9EppGOHLh0kCEKL7JBEhVCHihAsESyJiE4FWShGRmauu7KYiv6Pma+DGoFrBQ7MzGFmPr5vmDFIYj1mr1WYfrHPovA9VVOqbC7e/1rS9ZlrAVDYHig5WB0oPtBI0TNrUiC5yhP9jeF4X8NPcWfopoY48XT39PjjXeF0vWkZqOjd7LJYrmGasHPCCJbHwhS9/F8M4s8baid764Xi0Ilfp5voorpJfn2wwx/r3l77TwZUvR+qajXVn8PnvocYfXYH6k2ioOaCpaIdf11ivDcayyiMVudsOYqFb60gARJYHG9DbqQFmSVNjaO3K2NpAeK90ZCqtgcrjkP9aUCXp0moetDFEeRXnYCKXhm+uTW0CkBFu4JlxzZkFlbASz4CQGQVBFeEwZm8geyiMuRVntzsL3oXV+YMkvjRsydC1U+lhwZsWXgHb+oWVAEzIwvzyVlk5igsi7DymmHlHsFQR50rjl+981Jy1Fw6Gu0ObTtnU+cgs28AKgDiy+Awpj5OACBAhZ/qh2HOo6i+NeA73jUAML4/qWux8mt6NjW1w599CS9xb0mSEqQBEDAtwqALUmBaG5FV3oYPnTHMjAwetlWksyByaukxQg2wQ9FlccaK/OXA3/uAEUDp3rNIDQ1ctSk6kHh1/jRFoaL4M4snEMeD73gQx4M4PsT1IZ5AfYH68tZY7zv/ApRMY9mnuVMvAAAAAElFTkSuQmCC"
/><span class="nette-warning"><?php echo array_sum($data) ?> errors</span>
<?php
		}
		return ob_get_clean();
	}

	function getPanel()
	{
		ob_start();
		$data = $this->data;
		if ($this->id === 'dumps') {

?>
<style>#nette-debug .nette-DumpPanel h2{font:11pt/1.5 sans-serif;margin:0;padding:2px 8px;background:#3484d2;color:white}#nette-debug .nette-DumpPanel table{width:100%}#nette-debug .nette-DumpPanel a{color:#333;background:transparent}#nette-debug .nette-DumpPanel a abbr{font-family:sans-serif;color:#999}#nette-debug .nette-DumpPanel pre .php-array,#nette-debug .nette-DumpPanel pre .php-object{color:#c16549}</style>

<h1>Dumped variables</h1>

<div class="nette-inner nette-DumpPanel">
<?php foreach ($data as $item): ?>
	<?php if ($item['title']):?>
	<h2><?php echo htmlspecialchars($item['title']) ?></h2>
	<?php endif ?>

	<table>
	<?php $i = 0 ?>
	<?php foreach ($item['dump'] as $key => $dump): ?>
	<tr class="<?php echo $i++ % 2 ? 'nette-alt' : '' ?>">
		<th><?php echo htmlspecialchars($key) ?></th>
		<td><?php echo $dump ?></td>
	</tr>
	<?php endforeach ?>
	</table>
<?php endforeach ?>
</div>
<?php
		} elseif ($this->id === 'errors') {

?>
<h1>Errors</h1>

<div class="nette-inner">
<table>
<?php $i = 0 ?>
<?php foreach ($data as $item => $count): list($message, $file, $line) = explode('|', $item) ?>
<tr class="<?php echo $i++ % 2 ? 'nette-alt' : '' ?>">
	<td class="nette-right"><?php echo $count ? "$count\xC3\x97" : '' ?></td>
	<td><pre><?php echo htmlspecialchars($message), ' in ', Helpers::editorLink($file, $line), ':', $line ?></pre></td>
</tr>
<?php endforeach ?>
</table>
</div>
<?php
		}
		return ob_get_clean();
	}

}

}

namespace Nette\Utils {

use Nette;

final class SafeStream
{

	const PROTOCOL = 'safe';

	private $handle;

	private $tempHandle;

	private $file;

	private $tempFile;

	private $deleteFile;

	private $writeError = FALSE;

	static function register()
	{
		return stream_wrapper_register(self::PROTOCOL, __CLASS__);
	}

	function stream_open($path, $mode, $options, &$opened_path)
	{
		$path = substr($path, strlen(self::PROTOCOL)+3);

		$flag = trim($mode, 'rwax+');
		$mode = trim($mode, 'tb');
		$use_path = (bool) (STREAM_USE_PATH & $options);

		if ($mode === 'r') {
			return $this->checkAndLock($this->tempHandle = fopen($path, 'r'.$flag, $use_path), LOCK_SH);

		} elseif ($mode === 'r+') {
			if (!$this->checkAndLock($this->handle = fopen($path, 'r'.$flag, $use_path), LOCK_EX)) {
				return FALSE;
			}

		} elseif ($mode[0] === 'x') {
			if (!$this->checkAndLock($this->handle = fopen($path, 'x'.$flag, $use_path), LOCK_EX)) {
				return FALSE;
			}
			$this->deleteFile = TRUE;

		} elseif ($mode[0] === 'w' || $mode[0] === 'a') {
			if ($this->checkAndLock($this->handle = @fopen($path, 'x'.$flag, $use_path), LOCK_EX)) {
				$this->deleteFile = TRUE;

			} elseif (!$this->checkAndLock($this->handle = fopen($path, 'a+'.$flag, $use_path), LOCK_EX)) {
				return FALSE;
			}

		} else {
			trigger_error("Unknown mode $mode", E_USER_WARNING);
			return FALSE;
		}

		$tmp = '~~' . lcg_value() . '.tmp';
		if (!$this->tempHandle = fopen($path . $tmp, (strpos($mode, '+') ? 'x+' : 'x').$flag, $use_path)) {
			$this->clean();
			return FALSE;
		}
		$this->tempFile = realpath($path . $tmp);
		$this->file = substr($this->tempFile, 0, -strlen($tmp));

		if ($mode === 'r+' || $mode[0] === 'a') {
			$stat = fstat($this->handle);
			fseek($this->handle, 0);
			if (stream_copy_to_stream($this->handle, $this->tempHandle) !== $stat['size']) {
				$this->clean();
				return FALSE;
			}

			if ($mode[0] === 'a') {
				fseek($this->tempHandle, 0, SEEK_END);
			}
		}

		return TRUE;
	}

	private function checkAndLock($handle, $lock)
	{
		if (!$handle) {
			return FALSE;

		} elseif (!flock($handle, $lock)) {
			fclose($handle);
			return FALSE;
		}

		return TRUE;
	}

	private function clean()
	{
		flock($this->handle, LOCK_UN);
		fclose($this->handle);
		if ($this->deleteFile) {
			unlink($this->file);
		}
		if ($this->tempHandle) {
			fclose($this->tempHandle);
			unlink($this->tempFile);
		}
	}

	function stream_close()
	{
		if (!$this->tempFile) {
			flock($this->tempHandle, LOCK_UN);
			fclose($this->tempHandle);
			return;
		}

		flock($this->handle, LOCK_UN);
		fclose($this->handle);
		fclose($this->tempHandle);

		if ($this->writeError || !rename($this->tempFile, $this->file)
		) {
			unlink($this->tempFile);
			if ($this->deleteFile) {
				unlink($this->file);
			}
		}
	}

	function stream_read($length)
	{
		return fread($this->tempHandle, $length);
	}

	function stream_write($data)
	{
		$len = strlen($data);
		$res = fwrite($this->tempHandle, $data, $len);

		if ($res !== $len) {
			$this->writeError = TRUE;
		}

		return $res;
	}

	function stream_tell()
	{
		return ftell($this->tempHandle);
	}

	function stream_eof()
	{
		return feof($this->tempHandle);
	}

	function stream_seek($offset, $whence)
	{
		return fseek($this->tempHandle, $offset, $whence) === 0;
	}

	function stream_stat()
	{
		return fstat($this->tempHandle);
	}

	function url_stat($path, $flags)
	{

		$path = substr($path, strlen(self::PROTOCOL)+3);
		return ($flags & STREAM_URL_STAT_LINK) ? @lstat($path) : @stat($path);
	}

	function unlink($path)
	{
		$path = substr($path, strlen(self::PROTOCOL)+3);
		return unlink($path);
	}

}

}

namespace Nette\Application {

use Nette;

class Application extends Nette\Object
{

	public static $maxLoop = 20;

	public $catchExceptions;

	public $errorPresenter;

	public $onStartup;

	public $onShutdown;

	public $onRequest;

	public $onResponse;

	public $onError;

	public $allowedMethods = array('GET', 'POST', 'HEAD', 'PUT', 'DELETE');

	private $requests = array();

	private $presenter;

	private $context;

	function __construct(Nette\DI\IContainer $context)
	{
		$this->context = $context;
	}

	function run()
	{
		$httpRequest = $this->context->httpRequest;
		$httpResponse = $this->context->httpResponse;

		if ($this->allowedMethods) {
			$method = $httpRequest->getMethod();
			if (!in_array($method, $this->allowedMethods, TRUE)) {
				$httpResponse->setCode(Nette\Http\IResponse::S501_NOT_IMPLEMENTED);
				$httpResponse->setHeader('Allow', implode(',', $this->allowedMethods));
				echo '<h1>Method ' . htmlSpecialChars($method) . ' is not implemented</h1>';
				return;
			}
		}

		$request = NULL;
		$repeatedError = FALSE;
		do {
			try {
				if (count($this->requests) > self::$maxLoop) {
					throw new ApplicationException('Too many loops detected in application life cycle.');
				}

				if (!$request) {
					$this->onStartup($this);

					$router = $this->getRouter();

					Diagnostics\RoutingPanel::initialize($this, $httpRequest);

					$request = $router->match($httpRequest);
					if (!$request instanceof Request) {
						$request = NULL;
						throw new BadRequestException('No route for HTTP request.');
					}

					if (strcasecmp($request->getPresenterName(), $this->errorPresenter) === 0) {
						throw new BadRequestException('Invalid request. Presenter is not achievable.');
					}
				}

				$this->requests[] = $request;
				$this->onRequest($this, $request);

				$presenterName = $request->getPresenterName();
				try {
					$this->presenter = $this->getPresenterFactory()->createPresenter($presenterName);
				} catch (InvalidPresenterException $e) {
					throw new BadRequestException($e->getMessage(), 404, $e);
				}

				$this->getPresenterFactory()->getPresenterClass($presenterName);
				$request->setPresenterName($presenterName);
				$request->freeze();

				$response = $this->presenter->run($request);
				$this->onResponse($this, $response);

				if ($response instanceof Responses\ForwardResponse) {
					$request = $response->getRequest();
					continue;

				} elseif ($response instanceof IResponse) {
					$response->send($httpRequest, $httpResponse);
				}
				break;

			} catch (\Exception $e) {

				$this->onError($this, $e);

				if (!$this->catchExceptions) {
					$this->onShutdown($this, $e);
					throw $e;
				}

				if ($repeatedError) {
					$e = new ApplicationException('An error occurred while executing error-presenter', 0, $e);
				}

				if (!$httpResponse->isSent()) {
					$httpResponse->setCode($e instanceof BadRequestException ? $e->getCode() : 500);
				}

				if (!$repeatedError && $this->errorPresenter) {
					$repeatedError = TRUE;
					if ($this->presenter instanceof UI\Presenter) {
						try {
							$this->presenter->forward(":$this->errorPresenter:", array('exception' => $e));
						} catch (AbortException $foo) {
							$request = $this->presenter->getLastCreatedRequest();
						}
					} else {
						$request = new Request(
							$this->errorPresenter,
							Request::FORWARD,
							array('exception' => $e)
						);
					}

				} else {
					if ($e instanceof BadRequestException) {
						$code = $e->getCode();
					} else {
						$code = 500;
						Nette\Diagnostics\Debugger::log($e, Nette\Diagnostics\Debugger::ERROR);
					}

$messages = array(
	0 => array('Oops...', 'Your browser sent a request that this server could not understand or process.'),
	403 => array('Access Denied', 'You do not have permission to view this page. Please try contact the web site administrator if you believe you should be able to view this page.'),
	404 => array('Page Not Found', 'The page you requested could not be found. It is possible that the address is incorrect, or that the page no longer exists. Please use a search engine to find what you are looking for.'),
	405 => array('Method Not Allowed', 'The requested method is not allowed for the URL.'),
	410 => array('Page Not Found', 'The page you requested has been taken off the site. We apologize for the inconvenience.'),
	500 => array('Server Error', 'We\'re sorry! The server encountered an internal error and was unable to complete your request. Please try again later.'),
);
$message = isset($messages[$code]) ? $messages[$code] : $messages[0];

?>
<!DOCTYPE html>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name=robots content=noindex><meta name=generator content="Nette Framework">
<style>body{color:#333;background:white;width:500px;margin:100px auto}h1{font:bold 47px/1.5 sans-serif;margin:.6em 0}p{font:21px/1.5 Georgia,serif;margin:1.5em 0}small{font-size:70%;color:gray}</style>

<title><?php echo $message[0] ?></title>

<h1><?php echo $message[0] ?></h1>

<p><?php echo $message[1] ?></p>

<?php if ($code): ?><p><small>error <?php echo $code ?></small></p><?php endif ?>
<?php
					break;
				}
			}
		} while (1);

		$this->onShutdown($this, isset($e) ? $e : NULL);
	}

	final function getRequests()
	{
		return $this->requests;
	}

	final function getPresenter()
	{
		return $this->presenter;
	}

	protected function getContext()
	{
		return $this->context;
	}

	function getRouter()
	{
		return $this->context->router;
	}

	function getPresenterFactory()
	{
		return $this->context->presenterFactory;
	}

	function storeRequest($expiration = '+ 10 minutes')
	{
		$session = $this->context->session->getSection('Nette.Application/requests');
		do {
			$key = Nette\Utils\Strings::random(5);
		} while (isset($session[$key]));

		$session[$key] = end($this->requests);
		$session->setExpiration($expiration, $key);
		return $key;
	}

	function restoreRequest($key)
	{
		$session = $this->context->session->getSection('Nette.Application/requests');
		if (isset($session[$key])) {
			$request = clone $session[$key];
			unset($session[$key]);
			$request->setFlag(Request::RESTORED, TRUE);
			$this->presenter->sendResponse(new Responses\ForwardResponse($request));
		}
	}

}

}

namespace Nette\Application\Diagnostics {

use Nette;use Nette\Application\Routers;use Nette\Application\UI\Presenter;
use Nette\Diagnostics\Debugger;

class RoutingPanel extends Nette\Object implements Nette\Diagnostics\IBarPanel
{

	private $router;

	private $httpRequest;

	private $routers = array();

	private $request;

	static function initialize(Nette\Application\Application $application, Nette\Http\IRequest $httpRequest)
	{
		Debugger::$bar->addPanel(new self($application->getRouter(), $httpRequest));
		Debugger::$blueScreen->addPanel(function($e) use($application) {
			if ($e === NULL) {
				return array(
					'tab' => 'Nette Application',
					'panel' => '<h3>Requests</h3>' . Nette\Diagnostics\Helpers::clickableDump($application->getRequests())
						. '<h3>Presenter</h3>' . Nette\Diagnostics\Helpers::clickableDump($application->getPresenter())
				);
			}
		});
	}

	function __construct(Nette\Application\IRouter $router, Nette\Http\IRequest $httpRequest)
	{
		$this->router = $router;
		$this->httpRequest = $httpRequest;
	}

	function getTab()
	{
		$this->analyse($this->router);
		ob_start();

?>
<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAJHSURBVDjLlZPNi81hFMc/z7137p1mTCFvNZfGSzLIWNjZKRvFRoqNhRCSYm8xS3+AxRRZ2JAFJWJHSQqTQkbEzYwIM+6Yid/znJfH4prLXShOnb6r8/nWOd8Tcs78bz0/f+KMu50y05nK/wy+uHDylbutqS5extvGcxaWqtoGDA8PZ3dnrs2srQc2Zko41UXLmLdyDW5OfvsUkUgbYGbU63UAQggdmvMzFmzZCgTi7CQmkZwdEaX0JwDgTnGbTCaE0G4zw80omhPI92lcEtkNkdgJCCHwJX7mZvNaB0A14SaYJlwTrpHsTkoFlV1nt2c3x5YYo1/vM9A/gKpxdfwyu/v3teCayKq4JEwT5EB2R6WgYmrs2bYbcUNNUVfEhIfFYy69uci+1fuRX84mkawFSxd/4nVWUopUVIykwlQxRTJBTIDA4Pp1jBZPuNW4wUAPmCqWIn29X1k4f5Ku8g9mpKCkakRLVEs1auVuauVuyqHMo8ejNCe+sWPVTkQKXCMmkeZUmUZjETF1tc6ooly+fgUVw9So1/tRN6YnZji46QghBFKKuAouERNhMlbAHZFE6e7pB+He8MMw+GGI4xtOMf1+lsl3TQ4NHf19BSlaO1DB9BfMHdX0O0iqSgiBbJkjm491hClJbA1LxCURgpPzXwAHhg63necAIi3XngXLcRU0fof8ETMljIyM5LGxMcbHxzvy/6fuXdWgt6+PWncv1e4euqo1ZmabvHs5+jn8yzufO7hiiZmuNpNBM13rbvVSpbrXJE7/BMkHtU9jFIC/AAAAAElFTkSuQmCC"
/><?php if (empty($this->request)): ?>no route<?php else: echo $this->request->getPresenterName() . ':' . (isset($this->request->params[Presenter::ACTION_KEY]) ? $this->request->params[Presenter::ACTION_KEY] : Presenter::DEFAULT_ACTION) . (isset($this->request->params[Presenter::SIGNAL_KEY]) ? " {$this->request->params[Presenter::SIGNAL_KEY]}!" : ''); endif ?>
<?php
		return ob_get_clean();
	}

	function getPanel()
	{
		ob_start();

?>
<style>#nette-debug .nette-RoutingPanel table{font:9pt/1.5 Consolas,monospace}#nette-debug .nette-RoutingPanel .yes td{color:green}#nette-debug .nette-RoutingPanel .may td{color:#67F}#nette-debug .nette-RoutingPanel pre,#nette-debug .nette-RoutingPanel code{display:inline}</style>

<div class="nette-RoutingPanel">
<h1>
<?php if (empty($this->request)): ?>
	no route
<?php else: ?>
	<?php echo $this->request->getPresenterName() . ':' . (isset($this->request->params[Presenter::ACTION_KEY]) ? $this->request->params[Presenter::ACTION_KEY] : Presenter::DEFAULT_ACTION) . (isset($this->request->params[Presenter::SIGNAL_KEY]) ? " {$this->request->params[Presenter::SIGNAL_KEY]}!" : '') ?>
<?php endif ?>
</h1>

<?php if (!empty($this->request)): ?>
	<?php $params = $this->request->getParams() ?>
	<?php if (empty($params)): ?>
		<p>No parameters.</p>

	<?php else: ?>
		<table>
		<thead>
		<tr>
			<th>Parameter</th>
			<th>Value</th>
		</tr>
		</thead>
		<tbody>
		<?php unset($params[Presenter::ACTION_KEY], $params[Presenter::SIGNAL_KEY]) ?>
		<?php foreach ($params as $key => $value): ?>
		<tr>
			<td><code><?php echo htmlSpecialChars($key) ?></code></td>
			<td><?php if (is_string($value)):?><code><?php echo htmlSpecialChars($value) ?></code><?php else: echo Debugger::dump($value, TRUE); endif ?></td>
		</tr>
		<?php endforeach ?>
		</tbody>
		</table>
	<?php endif ?>
<?php endif ?>

<h2>Routers</h2>

<?php if (empty($this->routers)): ?>
	<p>No routers defined.</p>

<?php else: ?>
	<div class="nette-inner">
	<table>
	<thead>
	<tr>
		<th>Matched?</th>
		<th>Class</th>
		<th>Mask</th>
		<th>Defaults</th>
		<th>Request</th>
	</tr>
	</thead>

	<tbody>
	<?php foreach ($this->routers as $router): ?>
	<tr class="<?php echo $router['matched'] ?>">
		<td><?php echo $router['matched'] ?></td>

		<td><code title="<?php echo htmlSpecialChars($router['class']) ?>"><?php echo preg_replace('#.+\\\\#', '', htmlSpecialChars($router['class'])) ?></code></td>

		<td><code><strong><?php echo htmlSpecialChars($router['mask']) ?></strong></code></td>

		<td><code>
		<?php foreach ($router['defaults'] as $key => $value): ?>
			<?php echo htmlSpecialChars($key), "&nbsp;=&nbsp;", is_string($value) ? htmlSpecialChars($value) : str_replace("\n</pre", '</pre', Debugger::dump($value, TRUE)) ?><br />
		<?php endforeach ?>
		</code></td>

		<td><?php if ($router['request']): ?><code>
		<?php $params = $router['request']->getParams(); ?>
		<strong><?php echo htmlSpecialChars($router['request']->getPresenterName() . ':' . (isset($params[Presenter::ACTION_KEY]) ? $params[Presenter::ACTION_KEY] : Presenter::DEFAULT_ACTION)) ?></strong><br />
		<?php unset($params[Presenter::ACTION_KEY]) ?>
		<?php foreach ($params as $key => $value): ?>
			<?php echo htmlSpecialChars($key), "&nbsp;=&nbsp;", is_string($value) ? htmlSpecialChars($value) : str_replace("\n</pre", '</pre', Debugger::dump($value, TRUE)) ?><br />
		<?php endforeach ?>
		</code><?php endif ?></td>
	</tr>
	<?php endforeach ?>
	</tbody>
	</table>
	</div>
<?php endif ?>
</div>
<?php
		return ob_get_clean();
	}

	private function analyse($router)
	{
		if ($router instanceof Routers\RouteList) {
			foreach ($router as $subRouter) {
				$this->analyse($subRouter);
			}
			return;
		}

		$request = $router->match($this->httpRequest);
		$matched = $request === NULL ? 'no' : 'may';
		if ($request !== NULL && empty($this->request)) {
			$this->request = $request;
			$matched = 'yes';
		}

		$this->routers[] = array(
			'matched' => $matched,
			'class' => get_class($router),
			'defaults' => $router instanceof Routers\Route || $router instanceof Routers\SimpleRouter ? $router->getDefaults() : array(),
			'mask' => $router instanceof Routers\Route ? $router->getMask() : NULL,
			'request' => $request,
		);
	}

}

}

namespace Nette\Application {

use Nette;

class AbortException extends \Exception
{
}

class ApplicationException extends \Exception
{
	}

class InvalidPresenterException extends \Exception
{
}

class BadRequestException extends \Exception
{

	protected $defaultCode = 404;

	function __construct($message = '', $code = 0, \Exception $previous = NULL)
	{
		if ($code < 200 || $code > 504) {
			$code = $this->defaultCode;
		}

		{
			parent::__construct($message, $code, $previous);
		}
	}

}

class ForbiddenRequestException extends BadRequestException
{

	protected $defaultCode = 403;

}

}

namespace NetteModule {

use Nette;use Nette\Application;use Nette\Application\Responses;use Nette\Http;

class MicroPresenter extends Nette\Object implements Application\IPresenter
{

	private $context;

	private $request;

	function run(Application\Request $request)
	{
		$this->request = $request;

		$httpRequest = $this->context->httpRequest;
		if (!$httpRequest->isAjax() && ($request->isMethod('get') || $request->isMethod('head'))) {
			$refUrl = clone $httpRequest->getUrl();
			$url = $this->context->router->constructUrl($request, $refUrl->setPath($refUrl->getScriptPath()));
			if ($url !== NULL && !$httpRequest->getUrl()->isEqual($url)) {
				return new Responses\RedirectResponse($url, Http\IResponse::S301_MOVED_PERMANENTLY);
			}
		}

		$params = $request->getParams();
		if (!isset($params['callback'])) {
			return;
		}
		$params['presenter'] = $this;
		$response = callback($params['callback'])->invokeNamedArgs($params);

		if (is_string($response)) {
			$response = array($response, array());
		}
		if (is_array($response)) {
			if ($response instanceof \SplFileInfo) {
				$response = $this->createTemplate('Nette\Templating\FileTemplate')
					->setParams($response[1])->setFile($response[0]);
			} else {
				$response = $this->createTemplate('Nette\Templating\Template')
					->setParams($response[1])->setSource($response[0]);
			}
		}
		if ($response instanceof Nette\Templating\ITemplate) {
			return new Responses\TextResponse($response);
		} else {
			return $response;
		}
	}

	function createTemplate($class = NULL, $latteFactory = NULL)
	{
		$template = $class ? new $class : new Nette\Templating\FileTemplate;

		$template->setParams($this->request->getParams());
		$template->presenter = $this;
		$template->context = $context = $this->context;
		$url = $context->httpRequest->getUrl();
		$template->baseUrl = rtrim($url->getBaseUrl(), '/');
		$template->basePath = rtrim($url->getBasePath(), '/');

		$template->registerHelperLoader('Nette\Templating\DefaultHelpers::loader');
		$template->setCacheStorage($context->templateCacheStorage);
		$template->onPrepareFilters[] = function($template) use($latteFactory, $context) {
			$template->registerFilter($latteFactory ? $latteFactory() : new Nette\Latte\Engine);
		};
		return $template;
	}

	function redirectUrl($url, $code = Http\IResponse::S302_FOUND)
	{
		return new Responses\RedirectResponse($url, $code);
	}

	function error($code, $message = NULL)
	{
		throw new Application\BadRequestException($message, $code);
	}

	function getRequest()
	{
		return $this->request;
	}

	function setContext(Nette\DI\IContainer $context)
	{
		$this->context = $context;
		return $this;
	}

	final function getContext()
	{
		return $this->context;
	}

}

}

namespace Nette\Application {

use Nette;

class PresenterFactory implements IPresenterFactory
{

	public $caseSensitive = FALSE;

	private $baseDir;

	private $cache = array();

	private $context;

	function __construct($baseDir, Nette\DI\IContainer $context)
	{
		$this->baseDir = $baseDir;
		$this->context = $context;
	}

	function createPresenter($name)
	{
		$class = $this->getPresenterClass($name);
		$presenter = new $class;
		$presenter->setContext($this->context);
		return $presenter;
	}

	function getPresenterClass(& $name)
	{
		if (isset($this->cache[$name])) {
			list($class, $name) = $this->cache[$name];
			return $class;
		}

		if (!is_string($name) || !Nette\Utils\Strings::match($name, "#^[a-zA-Z\x7f-\xff][a-zA-Z0-9\x7f-\xff:]*$#")) {
			throw new InvalidPresenterException("Presenter name must be alphanumeric string, '$name' is invalid.");
		}

		$class = $this->formatPresenterClass($name);

		if (!class_exists($class)) {

			$file = $this->formatPresenterFile($name);
			if (is_file($file) && is_readable($file)) {
				Nette\Utils\LimitedScope::load($file);
			}

			if (!class_exists($class)) {
				throw new InvalidPresenterException("Cannot load presenter '$name', class '$class' was not found in '$file'.");
			}
		}

		$reflection = new Nette\Reflection\ClassType($class);
		$class = $reflection->getName();

		if (!$reflection->implementsInterface('Nette\Application\IPresenter')) {
			throw new InvalidPresenterException("Cannot load presenter '$name', class '$class' is not Nette\\Application\\IPresenter implementor.");
		}

		if ($reflection->isAbstract()) {
			throw new InvalidPresenterException("Cannot load presenter '$name', class '$class' is abstract.");
		}

		$realName = $this->unformatPresenterClass($class);
		if ($name !== $realName) {
			if ($this->caseSensitive) {
				throw new InvalidPresenterException("Cannot load presenter '$name', case mismatch. Real name is '$realName'.");
			} else {
				$this->cache[$name] = array($class, $realName);
				$name = $realName;
			}
		} else {
			$this->cache[$name] = array($class, $realName);
		}

		return $class;
	}

	function formatPresenterClass($presenter)
	{
		return str_replace(':', 'Module\\', $presenter) . 'Presenter';
	}

	function unformatPresenterClass($class)
	{
		return str_replace('Module\\', ':', substr($class, 0, -9));
	}

	function formatPresenterFile($presenter)
	{
		$path = '/' . str_replace(':', 'Module/', $presenter);
		return $this->baseDir . substr_replace($path, '/presenters', strrpos($path, '/'), 0) . 'Presenter.php';
	}

}

}

namespace Nette {

use Nette;

abstract class FreezableObject extends Object implements IFreezable
{

	private $frozen = FALSE;

	function freeze()
	{
		$this->frozen = TRUE;
	}

	final function isFrozen()
	{
		return $this->frozen;
	}

	function __clone()
	{
		$this->frozen = FALSE;
	}

	protected function updating()
	{
		if ($this->frozen) {
			$class = get_class($this);
			throw new InvalidStateException("Cannot modify a frozen object $class.");
		}
	}

}

}

namespace Nette\Application {

use Nette;

final class Request extends Nette\FreezableObject
{

	const FORWARD = 'FORWARD';

	const SECURED = 'secured';

	const RESTORED = 'restored';

	private $method;

	private $flags = array();

	private $name;

	private $params;

	private $post;

	private $files;

	function __construct($name, $method, array $params, array $post = array(), array $files = array(), array $flags = array())
	{
		$this->name = $name;
		$this->method = $method;
		$this->params = $params;
		$this->post = $post;
		$this->files = $files;
		$this->flags = $flags;
	}

	function setPresenterName($name)
	{
		$this->updating();
		$this->name = $name;
		return $this;
	}

	function getPresenterName()
	{
		return $this->name;
	}

	function setParams(array $params)
	{
		$this->updating();
		$this->params = $params;
		return $this;
	}

	function getParams()
	{
		return $this->params;
	}

	function setPost(array $params)
	{
		$this->updating();
		$this->post = $params;
		return $this;
	}

	function getPost()
	{
		return $this->post;
	}

	function setFiles(array $files)
	{
		$this->updating();
		$this->files = $files;
		return $this;
	}

	function getFiles()
	{
		return $this->files;
	}

	function setMethod($method)
	{
		$this->method = $method;
		return $this;
	}

	function getMethod()
	{
		return $this->method;
	}

	function isMethod($method)
	{
		return strcasecmp($this->method, $method) === 0;
	}

	function isPost()
	{
		return strcasecmp($this->method, 'post') === 0;
	}

	function setFlag($flag, $value = TRUE)
	{
		$this->updating();
		$this->flags[$flag] = (bool) $value;
		return $this;
	}

	function hasFlag($flag)
	{
		return !empty($this->flags[$flag]);
	}

}

}

namespace Nette\Application\Responses {

use Nette;

class FileResponse extends Nette\Object implements Nette\Application\IResponse
{

	private $file;

	private $contentType;

	private $name;

	public $resuming = TRUE;

	function __construct($file, $name = NULL, $contentType = NULL)
	{
		if (!is_file($file)) {
			throw new Nette\Application\BadRequestException("File '$file' doesn't exist.");
		}

		$this->file = $file;
		$this->name = $name ? $name : basename($file);
		$this->contentType = $contentType ? $contentType : 'application/octet-stream';
	}

	final function getFile()
	{
		return $this->file;
	}

	final function getName()
	{
		return $this->name;
	}

	final function getContentType()
	{
		return $this->contentType;
	}

	function send(Nette\Http\IRequest $httpRequest, Nette\Http\IResponse $httpResponse)
	{
		$httpResponse->setContentType($this->contentType);
		$httpResponse->setHeader('Content-Disposition', 'attachment; filename="' . $this->name . '"');

		$filesize = $length = filesize($this->file);
		$handle = fopen($this->file, 'r');

		if ($this->resuming) {
			$httpResponse->setHeader('Accept-Ranges', 'bytes');
			$range = $httpRequest->getHeader('Range');
			if ($range !== NULL) {
				$range = substr($range, 6);
				list($start, $end) = explode('-', $range);
				if ($start == NULL) {
					$start = 0;
				}
				if ($end == NULL) {
					$end = $filesize - 1;
				}

				if ($start < 0 || $end <= $start || $end > $filesize -1) {
					$httpResponse->setCode(416);
					return;
				}

				$httpResponse->setCode(206);
				$httpResponse->setHeader('Content-Range', 'bytes ' . $start . '-' . $end . '/' . $filesize);
				$length = $end - $start + 1;
				fseek($handle, $start);

			} else {
				$httpResponse->setHeader('Content-Range', 'bytes 0-' . ($filesize - 1) . '/' . $filesize);
			}
		}

		$httpResponse->setHeader('Content-Length', $length);
		while (!feof($handle)) {
			echo fread($handle, 4e6);
		}
		fclose($handle);
	}

}

class ForwardResponse extends Nette\Object implements Nette\Application\IResponse
{

	private $request;

	function __construct(Nette\Application\Request $request)
	{
		$this->request = $request;
	}

	final function getRequest()
	{
		return $this->request;
	}

	function send(Nette\Http\IRequest $httpRequest, Nette\Http\IResponse $httpResponse)
	{
	}

}

class JsonResponse extends Nette\Object implements Nette\Application\IResponse
{

	private $payload;

	private $contentType;

	function __construct($payload, $contentType = NULL)
	{
		if (!is_array($payload) && !is_object($payload)) {
			throw new Nette\InvalidArgumentException("Payload must be array or object class, " . gettype($payload) . " given.");
		}
		$this->payload = $payload;
		$this->contentType = $contentType ? $contentType : 'application/json';
	}

	final function getPayload()
	{
		return $this->payload;
	}

	final function getContentType()
	{
		return $this->contentType;
	}

	function send(Nette\Http\IRequest $httpRequest, Nette\Http\IResponse $httpResponse)
	{
		$httpResponse->setContentType($this->contentType);
		$httpResponse->setExpiration(FALSE);
		echo Nette\Utils\Json::encode($this->payload);
	}

}

use Nette\Http;

class RedirectResponse extends Nette\Object implements Nette\Application\IResponse
{

	private $url;

	private $code;

	function __construct($url, $code = Http\IResponse::S302_FOUND)
	{
		$this->url = (string) $url;
		$this->code = (int) $code;
	}

	final function getUrl()
	{
		return $this->url;
	}

	final function getCode()
	{
		return $this->code;
	}

	function send(Http\IRequest $httpRequest, Http\IResponse $httpResponse)
	{
		$httpResponse->redirect($this->url, $this->code);
	}

}

class TextResponse extends Nette\Object implements Nette\Application\IResponse
{

	private $source;

	function __construct($source)
	{
		$this->source = $source;
	}

	final function getSource()
	{
		return $this->source;
	}

	function send(Nette\Http\IRequest $httpRequest, Nette\Http\IResponse $httpResponse)
	{
		if ($this->source instanceof Nette\Templating\ITemplate) {
			$this->source->render();

		} else {
			echo $this->source;
		}
	}

}

}

namespace Nette\Application\Routers {

use Nette;use Nette\Application;

class CliRouter extends Nette\Object implements Application\IRouter
{
	const PRESENTER_KEY = 'action';

	private $defaults;

	function __construct($defaults = array())
	{
		$this->defaults = $defaults;
	}

	function match(Nette\Http\IRequest $httpRequest)
	{
		if (empty($_SERVER['argv']) || !is_array($_SERVER['argv'])) {
			return NULL;
		}

		$names = array(self::PRESENTER_KEY);
		$params = $this->defaults;
		$args = $_SERVER['argv'];
		array_shift($args);
		$args[] = '--';

		foreach ($args as $arg) {
			$opt = preg_replace('#/|-+#A', '', $arg);
			if ($opt === $arg) {
				if (isset($flag) || $flag = array_shift($names)) {
					$params[$flag] = $arg;
				} else {
					$params[] = $arg;
				}
				$flag = NULL;
				continue;
			}

			if (isset($flag)) {
				$params[$flag] = TRUE;
				$flag = NULL;
			}

			if ($opt !== '') {
				$pair = explode('=', $opt, 2);
				if (isset($pair[1])) {
					$params[$pair[0]] = $pair[1];
				} else {
					$flag = $pair[0];
				}
			}
		}

		if (!isset($params[self::PRESENTER_KEY])) {
			throw new Nette\InvalidStateException('Missing presenter & action in route definition.');
		}
		$presenter = $params[self::PRESENTER_KEY];
		if ($a = strrpos($presenter, ':')) {
			$params[self::PRESENTER_KEY] = substr($presenter, $a + 1);
			$presenter = substr($presenter, 0, $a);
		}

		return new Application\Request(
			$presenter,
			'CLI',
			$params
		);
	}

	function constructUrl(Application\Request $appRequest, Nette\Http\Url $refUrl)
	{
		return NULL;
	}

	function getDefaults()
	{
		return $this->defaults;
	}

}

use Nette\Utils\Strings;

class Route extends Nette\Object implements Application\IRouter
{
	const PRESENTER_KEY = 'presenter';
	const MODULE_KEY = 'module';

	const CASE_SENSITIVE = 256;

	const HOST = 1,
		PATH = 2,
		RELATIVE = 3;

	const VALUE = 'value';
	const PATTERN = 'pattern';
	const FILTER_IN = 'filterIn';
	const FILTER_OUT = 'filterOut';
	const FILTER_TABLE = 'filterTable';

	const OPTIONAL = 0,
		PATH_OPTIONAL = 1,
		CONSTANT = 2;

	public static $defaultFlags = 0;

	public static $styles = array(
		'#' => array(
			self::PATTERN => '[^/]+',
			self::FILTER_IN => 'rawurldecode',
			self::FILTER_OUT => 'rawurlencode',
		),
		'?#' => array(
		),
		'module' => array(
			self::PATTERN => '[a-z][a-z0-9.-]*',
			self::FILTER_IN => array(__CLASS__, 'path2presenter'),
			self::FILTER_OUT => array(__CLASS__, 'presenter2path'),
		),
		'presenter' => array(
			self::PATTERN => '[a-z][a-z0-9.-]*',
			self::FILTER_IN => array(__CLASS__, 'path2presenter'),
			self::FILTER_OUT => array(__CLASS__, 'presenter2path'),
		),
		'action' => array(
			self::PATTERN => '[a-z][a-z0-9-]*',
			self::FILTER_IN => array(__CLASS__, 'path2action'),
			self::FILTER_OUT => array(__CLASS__, 'action2path'),
		),
		'?module' => array(
		),
		'?presenter' => array(
		),
		'?action' => array(
		),
	);

	private $mask;

	private $sequence;

	private $re;

	private $metadata = array();

	private $xlat;

	private $type;

	private $flags;

	function __construct($mask, $metadata = array(), $flags = 0)
	{
		if (is_string($metadata)) {
			$a = strrpos($metadata, ':');
			if (!$a) {
				throw new Nette\InvalidArgumentException("Second argument must be array or string in format Presenter:action, '$metadata' given.");
			}
			$metadata = array(
				self::PRESENTER_KEY => substr($metadata, 0, $a),
				'action' => $a === strlen($metadata) - 1 ? Application\UI\Presenter::DEFAULT_ACTION : substr($metadata, $a + 1),
			);
		} elseif ($metadata instanceof \Closure || $metadata instanceof Nette\Callback) {
			$metadata = array(
				self::PRESENTER_KEY => 'Nette:Micro',
				'callback' => $metadata,
			);
		}

		$this->flags = $flags | self::$defaultFlags;
		$this->setMask($mask, $metadata);
	}

	function match(Nette\Http\IRequest $httpRequest)
	{

		$url = $httpRequest->getUrl();

		if ($this->type === self::HOST) {
			$path = '//' . $url->getHost() . $url->getPath();

		} elseif ($this->type === self::RELATIVE) {
			$basePath = $url->getBasePath();
			if (strncmp($url->getPath(), $basePath, strlen($basePath)) !== 0) {
				return NULL;
			}
			$path = (string) substr($url->getPath(), strlen($basePath));

		} else {
			$path = $url->getPath();
		}

		if ($path !== '') {
			$path = rtrim($path, '/') . '/';
		}

		if (!$matches = Strings::match($path, $this->re)) {

			return NULL;
		}

		$params = array();
		foreach ($matches as $k => $v) {
			if (is_string($k) && $v !== '') {
				$params[str_replace('___', '-', $k)] = $v;
			}
		}

		foreach ($this->metadata as $name => $meta) {
			if (isset($params[$name])) {

			} elseif (isset($meta['fixity']) && $meta['fixity'] !== self::OPTIONAL) {
				$params[$name] = NULL;
			}
		}

		if ($this->xlat) {
			$params += self::renameKeys($httpRequest->getQuery(), array_flip($this->xlat));
		} else {
			$params += $httpRequest->getQuery();
		}

		foreach ($this->metadata as $name => $meta) {
			if (isset($params[$name])) {
				if (!is_scalar($params[$name])) {

				} elseif (isset($meta[self::FILTER_TABLE][$params[$name]])) {
					$params[$name] = $meta[self::FILTER_TABLE][$params[$name]];

				} elseif (isset($meta[self::FILTER_IN])) {
					$params[$name] = call_user_func($meta[self::FILTER_IN], (string) $params[$name]);
					if ($params[$name] === NULL && !isset($meta['fixity'])) {
						return NULL;
					}
				}

			} elseif (isset($meta['fixity'])) {
				$params[$name] = $meta[self::VALUE];
			}
		}

		if (!isset($params[self::PRESENTER_KEY])) {
			throw new Nette\InvalidStateException('Missing presenter in route definition.');
		}
		if (isset($this->metadata[self::MODULE_KEY])) {
			if (!isset($params[self::MODULE_KEY])) {
				throw new Nette\InvalidStateException('Missing module in route definition.');
			}
			$presenter = $params[self::MODULE_KEY] . ':' . $params[self::PRESENTER_KEY];
			unset($params[self::MODULE_KEY], $params[self::PRESENTER_KEY]);

		} else {
			$presenter = $params[self::PRESENTER_KEY];
			unset($params[self::PRESENTER_KEY]);
		}

		return new Application\Request(
			$presenter,
			$httpRequest->getMethod(),
			$params,
			$httpRequest->getPost(),
			$httpRequest->getFiles(),
			array(Application\Request::SECURED => $httpRequest->isSecured())
		);
	}

	function constructUrl(Application\Request $appRequest, Nette\Http\Url $refUrl)
	{
		if ($this->flags & self::ONE_WAY) {
			return NULL;
		}

		$params = $appRequest->getParams();
		$metadata = $this->metadata;

		$presenter = $appRequest->getPresenterName();
		$params[self::PRESENTER_KEY] = $presenter;

		if (isset($metadata[self::MODULE_KEY])) {
			$module = $metadata[self::MODULE_KEY];
			if (isset($module['fixity']) && strncasecmp($presenter, $module[self::VALUE] . ':', strlen($module[self::VALUE]) + 1) === 0) {
				$a = strlen($module[self::VALUE]);
			} else {
				$a = strrpos($presenter, ':');
			}
			if ($a === FALSE) {
				$params[self::MODULE_KEY] = '';
			} else {
				$params[self::MODULE_KEY] = substr($presenter, 0, $a);
				$params[self::PRESENTER_KEY] = substr($presenter, $a + 1);
			}
		}

		foreach ($metadata as $name => $meta) {
			if (!isset($params[$name])) {
				continue;
			}

			if (isset($meta['fixity'])) {
				if (is_scalar($params[$name]) ? strcasecmp($params[$name], $meta[self::VALUE]) === 0
					: $params[$name] === $meta[self::VALUE]
				) {
					unset($params[$name]);
					continue;

				} elseif ($meta['fixity'] === self::CONSTANT) {
					return NULL;
				}
			}

			if (!is_scalar($params[$name])) {

			} elseif (isset($meta['filterTable2'][$params[$name]])) {
				$params[$name] = $meta['filterTable2'][$params[$name]];

			} elseif (isset($meta[self::FILTER_OUT])) {
				$params[$name] = call_user_func($meta[self::FILTER_OUT], $params[$name]);
			}

			if (isset($meta[self::PATTERN]) && !preg_match($meta[self::PATTERN], rawurldecode($params[$name]))) {
				return NULL;
			}
		}

		$sequence = $this->sequence;
		$brackets = array();
		$required = 0;
		$url = '';
		$i = count($sequence) - 1;
		do {
			$url = $sequence[$i] . $url;
			if ($i === 0) {
				break;
			}
			$i--;

			$name = $sequence[$i]; $i--;

			if ($name === ']') {
				$brackets[] = $url;

			} elseif ($name[0] === '[') {
				$tmp = array_pop($brackets);
				if ($required < count($brackets) + 1) {
					if ($name !== '[!') {
						$url = $tmp;
					}
				} else {
					$required = count($brackets);
				}

			} elseif ($name[0] === '?') {
				continue;

			} elseif (isset($params[$name]) && $params[$name] != '') {
				$required = count($brackets);
				$url = $params[$name] . $url;
				unset($params[$name]);

			} elseif (isset($metadata[$name]['fixity'])) {
				$url = $metadata[$name]['defOut'] . $url;

			} else {
				return NULL;
			}
		} while (TRUE);

		if ($this->xlat) {
			$params = self::renameKeys($params, $this->xlat);
		}

		$sep = ini_get('arg_separator.input');
		$query = http_build_query($params, '', $sep ? $sep[0] : '&');
		if ($query != '') {
			$url .= '?' . $query;
		}

		if ($this->type === self::RELATIVE) {
			$url = '//' . $refUrl->getAuthority() . $refUrl->getBasePath() . $url;

		} elseif ($this->type === self::PATH) {
			$url = '//' . $refUrl->getAuthority() . $url;
		}

		if (strpos($url, '//', 2) !== FALSE) {
			return NULL;
		}

		$url = ($this->flags & self::SECURED ? 'https:' : 'http:') . $url;

		return $url;
	}

	private function setMask($mask, array $metadata)
	{
		$this->mask = $mask;

		if (substr($mask, 0, 2) === '//') {
			$this->type = self::HOST;

		} elseif (substr($mask, 0, 1) === '/') {
			$this->type = self::PATH;

		} else {
			$this->type = self::RELATIVE;
		}

		foreach ($metadata as $name => $meta) {
			if (!is_array($meta)) {
				$metadata[$name] = array(self::VALUE => $meta, 'fixity' => self::CONSTANT);

			} elseif (array_key_exists(self::VALUE, $meta)) {
				$metadata[$name]['fixity'] = self::CONSTANT;
			}
		}

		$parts = Strings::split($mask, '/<([^>#= ]+)(=[^># ]*)? *([^>#]*)(#?[^>\[\]]*)>|(\[!?|\]|\s*\?.*)/');

		$this->xlat = array();
		$i = count($parts) - 1;

		if (isset($parts[$i - 1]) && substr(ltrim($parts[$i - 1]), 0, 1) === '?') {

			$matches = Strings::matchAll($parts[$i - 1], '/(?:([a-zA-Z0-9_.-]+)=)?<([^># ]+) *([^>#]*)(#?[^>]*)>/');

			foreach ($matches as $match) {
				list(, $param, $name, $pattern, $class) = $match;

				if ($class !== '') {
					if (!isset(self::$styles[$class])) {
						throw new Nette\InvalidStateException("Parameter '$name' has '$class' flag, but Route::\$styles['$class'] is not set.");
					}
					$meta = self::$styles[$class];

				} elseif (isset(self::$styles['?' . $name])) {
					$meta = self::$styles['?' . $name];

				} else {
					$meta = self::$styles['?#'];
				}

				if (isset($metadata[$name])) {
					$meta = $metadata[$name] + $meta;
				}

				if (array_key_exists(self::VALUE, $meta)) {
					$meta['fixity'] = self::OPTIONAL;
				}

				unset($meta['pattern']);
				$meta['filterTable2'] = empty($meta[self::FILTER_TABLE]) ? NULL : array_flip($meta[self::FILTER_TABLE]);

				$metadata[$name] = $meta;
				if ($param !== '') {
					$this->xlat[$name] = $param;
				}
			}
			$i -= 6;
		}

		$brackets = 0;
		$re = '';
		$sequence = array();
		$autoOptional = array(0, 0);
		do {
			array_unshift($sequence, $parts[$i]);
			$re = preg_quote($parts[$i], '#') . $re;
			if ($i === 0) {
				break;
			}
			$i--;

			$part = $parts[$i];
			if ($part === '[' || $part === ']' || $part === '[!') {
				$brackets += $part[0] === '[' ? -1 : 1;
				if ($brackets < 0) {
					throw new Nette\InvalidArgumentException("Unexpected '$part' in mask '$mask'.");
				}
				array_unshift($sequence, $part);
				$re = ($part[0] === '[' ? '(?:' : ')?') . $re;
				$i -= 5;
				continue;
			}

			$class = $parts[$i]; $i--;
			$pattern = trim($parts[$i]); $i--;
			$default = $parts[$i]; $i--;
			$name = $parts[$i]; $i--;
			array_unshift($sequence, $name);

			if ($name[0] === '?') {
				$re = '(?:' . preg_quote(substr($name, 1), '#') . '|' . $pattern . ')' . $re;
				$sequence[1] = substr($name, 1) . $sequence[1];
				continue;
			}

			if (preg_match('#[^a-z0-9_-]#i', $name)) {
				throw new Nette\InvalidArgumentException("Parameter name must be alphanumeric string due to limitations of PCRE, '$name' given.");
			}

			if ($class !== '') {
				if (!isset(self::$styles[$class])) {
					throw new Nette\InvalidStateException("Parameter '$name' has '$class' flag, but Route::\$styles['$class'] is not set.");
				}
				$meta = self::$styles[$class];

			} elseif (isset(self::$styles[$name])) {
				$meta = self::$styles[$name];

			} else {
				$meta = self::$styles['#'];
			}

			if (isset($metadata[$name])) {
				$meta = $metadata[$name] + $meta;
			}

			if ($pattern == '' && isset($meta[self::PATTERN])) {
				$pattern = $meta[self::PATTERN];
			}

			if ($default !== '') {
				$meta[self::VALUE] = (string) substr($default, 1);
				$meta['fixity'] = self::PATH_OPTIONAL;
			}

			$meta['filterTable2'] = empty($meta[self::FILTER_TABLE]) ? NULL : array_flip($meta[self::FILTER_TABLE]);
			if (array_key_exists(self::VALUE, $meta)) {
				if (isset($meta['filterTable2'][$meta[self::VALUE]])) {
					$meta['defOut'] = $meta['filterTable2'][$meta[self::VALUE]];

				} elseif (isset($meta[self::FILTER_OUT])) {
					$meta['defOut'] = call_user_func($meta[self::FILTER_OUT], $meta[self::VALUE]);

				} else {
					$meta['defOut'] = $meta[self::VALUE];
				}
			}
			$meta[self::PATTERN] = "#(?:$pattern)$#A" . ($this->flags & self::CASE_SENSITIVE ? '' : 'iu');

			$re = '(?P<' . str_replace('-', '___', $name) . '>(?U)' . $pattern . ')' . $re;
			if ($brackets) {
				if (!isset($meta[self::VALUE])) {
					$meta[self::VALUE] = $meta['defOut'] = NULL;
				}
				$meta['fixity'] = self::PATH_OPTIONAL;

			} elseif (isset($meta['fixity'])) {
				$re = '(?:' . substr_replace($re, ')?', strlen($re) - $autoOptional[0], 0);
				array_splice($sequence, count($sequence) - $autoOptional[1], 0, array(']', ''));
				array_unshift($sequence, '[', '');
				$meta['fixity'] = self::PATH_OPTIONAL;

			} else {
				$autoOptional = array(strlen($re), count($sequence));
			}

			$metadata[$name] = $meta;
		} while (TRUE);

		if ($brackets) {
			throw new Nette\InvalidArgumentException("Missing closing ']' in mask '$mask'.");
		}

		$this->re = '#' . $re . '/?$#A' . ($this->flags & self::CASE_SENSITIVE ? '' : 'iu');
		$this->metadata = $metadata;
		$this->sequence = $sequence;
	}

	function getMask()
	{
		return $this->mask;
	}

	function getDefaults()
	{
		$defaults = array();
		foreach ($this->metadata as $name => $meta) {
			if (isset($meta['fixity'])) {
				$defaults[$name] = $meta[self::VALUE];
			}
		}
		return $defaults;
	}

	function getTargetPresenter()
	{
		if ($this->flags & self::ONE_WAY) {
			return FALSE;
		}

		$m = $this->metadata;
		$module = '';

		if (isset($m[self::MODULE_KEY])) {
			if (isset($m[self::MODULE_KEY]['fixity']) && $m[self::MODULE_KEY]['fixity'] === self::CONSTANT) {
				$module = $m[self::MODULE_KEY][self::VALUE] . ':';
			} else {
				return NULL;
			}
		}

		if (isset($m[self::PRESENTER_KEY]['fixity']) && $m[self::PRESENTER_KEY]['fixity'] === self::CONSTANT) {
			return $module . $m[self::PRESENTER_KEY][self::VALUE];
		}
		return NULL;
	}

	private static function renameKeys($arr, $xlat)
	{
		if (empty($xlat)) {
			return $arr;
		}

		$res = array();
		$occupied = array_flip($xlat);
		foreach ($arr as $k => $v) {
			if (isset($xlat[$k])) {
				$res[$xlat[$k]] = $v;

			} elseif (!isset($occupied[$k])) {
				$res[$k] = $v;
			}
		}
		return $res;
	}

	private static function action2path($s)
	{
		$s = preg_replace('#(.)(?=[A-Z])#', '$1-', $s);
		$s = strtolower($s);
		$s = rawurlencode($s);
		return $s;
	}

	private static function path2action($s)
	{
		$s = strtolower($s);
		$s = preg_replace('#-(?=[a-z])#', ' ', $s);
		$s = substr(ucwords('x' . $s), 1);

		$s = str_replace(' ', '', $s);
		return $s;
	}

	private static function presenter2path($s)
	{
		$s = strtr($s, ':', '.');
		$s = preg_replace('#([^.])(?=[A-Z])#', '$1-', $s);
		$s = strtolower($s);
		$s = rawurlencode($s);
		return $s;
	}

	private static function path2presenter($s)
	{
		$s = strtolower($s);
		$s = preg_replace('#([.-])(?=[a-z])#', '$1 ', $s);
		$s = ucwords($s);
		$s = str_replace('. ', ':', $s);
		$s = str_replace('- ', '', $s);
		return $s;
	}

	static function addStyle($style, $parent = '#')
	{
		if (isset(self::$styles[$style])) {
			throw new Nette\InvalidArgumentException("Style '$style' already exists.");
		}

		if ($parent !== NULL) {
			if (!isset(self::$styles[$parent])) {
				throw new Nette\InvalidArgumentException("Parent style '$parent' doesn't exist.");
			}
			self::$styles[$style] = self::$styles[$parent];

		} else {
			self::$styles[$style] = array();
		}
	}

	static function setStyleProperty($style, $key, $value)
	{
		if (!isset(self::$styles[$style])) {
			throw new Nette\InvalidArgumentException("Style '$style' doesn't exist.");
		}
		self::$styles[$style][$key] = $value;
	}

}

}

namespace Nette {

use Nette;

class ArrayList extends Object implements \ArrayAccess, \Countable, \IteratorAggregate
{
	private $list = array();

	function getIterator()
	{
		return new \ArrayIterator($this->list);
	}

	function count()
	{
		return count($this->list);
	}

	function offsetSet($index, $value)
	{
		if ($index === NULL) {
			$this->list[] = $value;

		} elseif ($index < 0 || $index >= count($this->list)) {
			throw new OutOfRangeException("Offset invalid or out of range");

		} else {
			$this->list[(int) $index] = $value;
		}
	}

	function offsetGet($index)
	{
		if ($index < 0 || $index >= count($this->list)) {
			throw new OutOfRangeException("Offset invalid or out of range");
		}
		return $this->list[(int) $index];
	}

	function offsetExists($index)
	{
		return $index >= 0 && $index < count($this->list);
	}

	function offsetUnset($index)
	{
		if ($index < 0 || $index >= count($this->list)) {
			throw new OutOfRangeException("Offset invalid or out of range");
		}
		array_splice($this->list, (int) $index, 1);
	}

}

}

namespace Nette\Application\Routers {

use Nette;

class RouteList extends Nette\ArrayList implements Nette\Application\IRouter
{

	private $cachedRoutes;

	private $module;

	function __construct($module = NULL)
	{
		$this->module = $module ? $module . ':' : '';
	}

	function match(Nette\Http\IRequest $httpRequest)
	{
		foreach ($this as $route) {
			$appRequest = $route->match($httpRequest);
			if ($appRequest !== NULL) {
				$appRequest->setPresenterName($this->module . $appRequest->getPresenterName());
				return $appRequest;
			}
		}
		return NULL;
	}

	function constructUrl(Nette\Application\Request $appRequest, Nette\Http\Url $refUrl)
	{
		if ($this->cachedRoutes === NULL) {
			$routes = array();
			$routes['*'] = array();

			foreach ($this as $route) {
				$presenter = $route instanceof Route ? $route->getTargetPresenter() : NULL;

				if ($presenter === FALSE) {
					continue;
				}

				if (is_string($presenter)) {
					$presenter = strtolower($presenter);
					if (!isset($routes[$presenter])) {
						$routes[$presenter] = $routes['*'];
					}
					$routes[$presenter][] = $route;

				} else {
					foreach ($routes as $id => $foo) {
						$routes[$id][] = $route;
					}
				}
			}

			$this->cachedRoutes = $routes;
		}

		if ($this->module) {
			if (strncasecmp($tmp = $appRequest->getPresenterName(), $this->module, strlen($this->module)) === 0) {
				$appRequest = clone $appRequest;
				$appRequest->setPresenterName(substr($tmp, strlen($this->module)));
			} else {
				return NULL;
			}
		}

		$presenter = strtolower($appRequest->getPresenterName());
		if (!isset($this->cachedRoutes[$presenter])) {
			$presenter = '*';
		}

		foreach ($this->cachedRoutes[$presenter] as $route) {
			$url = $route->constructUrl($appRequest, $refUrl);
			if ($url !== NULL) {
				return $url;
			}
		}

		return NULL;
	}

	function offsetSet($index, $route)
	{
		if (!$route instanceof Nette\Application\IRouter) {
			throw new Nette\InvalidArgumentException("Argument must be IRouter descendant.");
		}
		parent::offsetSet($index, $route);
	}

}

use Nette\Application;

class SimpleRouter extends Nette\Object implements Application\IRouter
{
	const PRESENTER_KEY = 'presenter';
	const MODULE_KEY = 'module';

	private $module = '';

	private $defaults;

	private $flags;

	function __construct($defaults = array(), $flags = 0)
	{
		if (is_string($defaults)) {
			$a = strrpos($defaults, ':');
			if (!$a) {
				throw new Nette\InvalidArgumentException("Argument must be array or string in format Presenter:action, '$defaults' given.");
			}
			$defaults = array(
				self::PRESENTER_KEY => substr($defaults, 0, $a),
				'action' => $a === strlen($defaults) - 1 ? Application\UI\Presenter::DEFAULT_ACTION : substr($defaults, $a + 1),
			);
		}

		if (isset($defaults[self::MODULE_KEY])) {
			$this->module = $defaults[self::MODULE_KEY] . ':';
			unset($defaults[self::MODULE_KEY]);
		}

		$this->defaults = $defaults;
		$this->flags = $flags;
	}

	function match(Nette\Http\IRequest $httpRequest)
	{
		if ($httpRequest->getUrl()->getPathInfo() !== '') {
			return NULL;
		}

		$params = $httpRequest->getQuery();
		$params += $this->defaults;

		if (!isset($params[self::PRESENTER_KEY])) {
			throw new Nette\InvalidStateException('Missing presenter.');
		}

		$presenter = $this->module . $params[self::PRESENTER_KEY];
		unset($params[self::PRESENTER_KEY]);

		return new Application\Request(
			$presenter,
			$httpRequest->getMethod(),
			$params,
			$httpRequest->getPost(),
			$httpRequest->getFiles(),
			array(Application\Request::SECURED => $httpRequest->isSecured())
		);
	}

	function constructUrl(Application\Request $appRequest, Nette\Http\Url $refUrl)
	{
		$params = $appRequest->getParams();

		$presenter = $appRequest->getPresenterName();
		if (strncasecmp($presenter, $this->module, strlen($this->module)) === 0) {
			$params[self::PRESENTER_KEY] = substr($presenter, strlen($this->module));
		} else {
			return NULL;
		}

		foreach ($this->defaults as $key => $value) {
			if (isset($params[$key]) && $params[$key] == $value) {
				unset($params[$key]);
			}
		}

		$url = ($this->flags & self::SECURED ? 'https://' : 'http://') . $refUrl->getAuthority() . $refUrl->getPath();
		$sep = ini_get('arg_separator.input');
		$query = http_build_query($params, '', $sep ? $sep[0] : '&');
		if ($query != '') {
			$url .= '?' . $query;
		}
		return $url;
	}

	function getDefaults()
	{
		return $this->defaults;
	}

}

}

namespace Nette\Application\UI {

use Nette;

class BadSignalException extends Nette\Application\BadRequestException
{

	protected $defaultCode = 403;

}

}

namespace Nette\ComponentModel {

use Nette;

abstract class Component extends Nette\Object implements IComponent
{

	private $parent;

	private $name;

	private $monitors = array();

	function __construct(IContainer $parent = NULL, $name = NULL)
	{
		if ($parent !== NULL) {
			$parent->addComponent($this, $name);

		} elseif (is_string($name)) {
			$this->name = $name;
		}
	}

	function lookup($type, $need = TRUE)
	{
		if (!isset($this->monitors[$type])) {
			$obj = $this->parent;
			$path = self::NAME_SEPARATOR . $this->name;
			$depth = 1;
			while ($obj !== NULL) {
				if ($obj instanceof $type) {
					break;
				}
				$path = self::NAME_SEPARATOR . $obj->getName() . $path;
				$depth++;
				$obj = $obj->getParent();
				if ($obj === $this) {
					$obj = NULL;
				}
			}

			if ($obj) {
				$this->monitors[$type] = array($obj, $depth, substr($path, 1), FALSE);

			} else {
				$this->monitors[$type] = array(NULL, NULL, NULL, FALSE);
			}
		}

		if ($need && $this->monitors[$type][0] === NULL) {
			throw new Nette\InvalidStateException("Component '$this->name' is not attached to '$type'.");
		}

		return $this->monitors[$type][0];
	}

	function lookupPath($type, $need = TRUE)
	{
		$this->lookup($type, $need);
		return $this->monitors[$type][2];
	}

	function monitor($type)
	{
		if (empty($this->monitors[$type][3])) {
			if ($obj = $this->lookup($type, FALSE)) {
				$this->attached($obj);
			}
			$this->monitors[$type][3] = TRUE;
		}
	}

	function unmonitor($type)
	{
		unset($this->monitors[$type]);
	}

	protected function attached($obj)
	{
	}

	protected function detached($obj)
	{
	}

	final function getName()
	{
		return $this->name;
	}

	final function getParent()
	{
		return $this->parent;
	}

	function setParent(IContainer $parent = NULL, $name = NULL)
	{
		if ($parent === NULL && $this->parent === NULL && $name !== NULL) {
			$this->name = $name;
			return $this;

		} elseif ($parent === $this->parent && $name === NULL) {
			return $this;
		}

		if ($this->parent !== NULL && $parent !== NULL) {
			throw new Nette\InvalidStateException("Component '$this->name' already has a parent.");
		}

		if ($parent === NULL) {
			$this->refreshMonitors(0);
			$this->parent = NULL;

		} else {
			$this->validateParent($parent);
			$this->parent = $parent;
			if ($name !== NULL) {
				$this->name = $name;
			}

			$tmp = array();
			$this->refreshMonitors(0, $tmp);
		}
		return $this;
	}

	protected function validateParent(IContainer $parent)
	{
	}

	private function refreshMonitors($depth, & $missing = NULL, & $listeners = array())
	{
		if ($this instanceof IContainer) {
			foreach ($this->getComponents() as $component) {
				if ($component instanceof Component) {
					$component->refreshMonitors($depth + 1, $missing, $listeners);
				}
			}
		}

		if ($missing === NULL) {
			foreach ($this->monitors as $type => $rec) {
				if (isset($rec[1]) && $rec[1] > $depth) {
					if ($rec[3]) {
						$this->monitors[$type] = array(NULL, NULL, NULL, TRUE);
						$listeners[] = array($this, $rec[0]);
					} else {
						unset($this->monitors[$type]);
					}
				}
			}

		} else {
			foreach ($this->monitors as $type => $rec) {
				if (isset($rec[0])) {
					continue;

				} elseif (!$rec[3]) {
					unset($this->monitors[$type]);

				} elseif (isset($missing[$type])) {
					$this->monitors[$type] = array(NULL, NULL, NULL, TRUE);

				} else {
					$this->monitors[$type] = NULL;
					if ($obj = $this->lookup($type, FALSE)) {
						$listeners[] = array($this, $obj);
					} else {
						$missing[$type] = TRUE;
					}
					$this->monitors[$type][3] = TRUE;
				}
			}
		}

		if ($depth === 0) {
			$method = $missing === NULL ? 'detached' : 'attached';
			foreach ($listeners as $item) {
				$item[0]->$method($item[1]);
			}
		}
	}

	function __clone()
	{
		if ($this->parent === NULL) {
			return;

		} elseif ($this->parent instanceof Container) {
			$this->parent = $this->parent->_isCloning();
			if ($this->parent === NULL) {
				$this->refreshMonitors(0);
			}

		} else {
			$this->parent = NULL;
			$this->refreshMonitors(0);
		}
	}

	final function __wakeup()
	{
		throw new Nette\NotImplementedException;
	}

}

class Container extends Component implements IContainer
{

	private $components = array();

	private $cloning;

	function addComponent(IComponent $component, $name, $insertBefore = NULL)
	{
		if ($name === NULL) {
			$name = $component->getName();
		}

		if (is_int($name)) {
			$name = (string) $name;

		} elseif (!is_string($name)) {
			throw new Nette\InvalidArgumentException("Component name must be integer or string, " . gettype($name) . " given.");

		} elseif (!preg_match('#^[a-zA-Z0-9_]+$#', $name)) {
			throw new Nette\InvalidArgumentException("Component name must be non-empty alphanumeric string, '$name' given.");
		}

		if (isset($this->components[$name])) {
			throw new Nette\InvalidStateException("Component with name '$name' already exists.");
		}

		$obj = $this;
		do {
			if ($obj === $component) {
				throw new Nette\InvalidStateException("Circular reference detected while adding component '$name'.");
			}
			$obj = $obj->getParent();
		} while ($obj !== NULL);

		$this->validateChildComponent($component);

		try {
			if (isset($this->components[$insertBefore])) {
				$tmp = array();
				foreach ($this->components as $k => $v) {
					if ($k === $insertBefore) {
						$tmp[$name] = $component;
					}
					$tmp[$k] = $v;
				}
				$this->components = $tmp;
			} else {
				$this->components[$name] = $component;
			}
			$component->setParent($this, $name);

		} catch (\Exception $e) {
			unset($this->components[$name]);
			throw $e;
		}
	}

	function removeComponent(IComponent $component)
	{
		$name = $component->getName();
		if (!isset($this->components[$name]) || $this->components[$name] !== $component) {
			throw new Nette\InvalidArgumentException("Component named '$name' is not located in this container.");
		}

		unset($this->components[$name]);
		$component->setParent(NULL);
	}

	final function getComponent($name, $need = TRUE)
	{
		if (is_int($name)) {
			$name = (string) $name;

		} elseif (!is_string($name)) {
			throw new Nette\InvalidArgumentException("Component name must be integer or string, " . gettype($name) . " given.");

		} else {
			$a = strpos($name, self::NAME_SEPARATOR);
			if ($a !== FALSE) {
				$ext = (string) substr($name, $a + 1);
				$name = substr($name, 0, $a);
			}

			if ($name === '') {
				throw new Nette\InvalidArgumentException("Component or subcomponent name must not be empty string.");
			}
		}

		if (!isset($this->components[$name])) {
			$component = $this->createComponent($name);
			if ($component instanceof IComponent && $component->getParent() === NULL) {
				$this->addComponent($component, $name);
			}
		}

		if (isset($this->components[$name])) {
			if (!isset($ext)) {
				return $this->components[$name];

			} elseif ($this->components[$name] instanceof IContainer) {
				return $this->components[$name]->getComponent($ext, $need);

			} elseif ($need) {
				throw new Nette\InvalidArgumentException("Component with name '$name' is not container and cannot have '$ext' component.");
			}

		} elseif ($need) {
			throw new Nette\InvalidArgumentException("Component with name '$name' does not exist.");
		}
	}

	protected function createComponent($name)
	{
		$ucname = ucfirst($name);
		$method = 'createComponent' . $ucname;
		if ($ucname !== $name && method_exists($this, $method) && $this->getReflection()->getMethod($method)->getName() === $method) {
			$component = $this->$method($name);
			if (!$component instanceof IComponent && !isset($this->components[$name])) {
				$class = get_class($this);
				throw new Nette\UnexpectedValueException("Method $class::$method() did not return or create the desired component.");
			}
			return $component;
		}
	}

	final function getComponents($deep = FALSE, $filterType = NULL)
	{
		$iterator = new RecursiveComponentIterator($this->components);
		if ($deep) {
			$deep = $deep > 0 ? \RecursiveIteratorIterator::SELF_FIRST : \RecursiveIteratorIterator::CHILD_FIRST;
			$iterator = new \RecursiveIteratorIterator($iterator, $deep);
		}
		if ($filterType) {
			$iterator = new Nette\Iterators\InstanceFilter($iterator, $filterType);
		}
		return $iterator;
	}

	protected function validateChildComponent(IComponent $child)
	{
	}

	function __clone()
	{
		if ($this->components) {
			$oldMyself = reset($this->components)->getParent();
			$oldMyself->cloning = $this;
			foreach ($this->components as $name => $component) {
				$this->components[$name] = clone $component;
			}
			$oldMyself->cloning = NULL;
		}
		parent::__clone();
	}

	function _isCloning()
	{
		return $this->cloning;
	}

}

}

namespace Nette\Application\UI {

use Nette;

abstract class PresenterComponent extends Nette\ComponentModel\Container implements ISignalReceiver, IStatePersistent, \ArrayAccess
{

	protected $params = array();

	function __construct(Nette\ComponentModel\IContainer $parent = NULL, $name = NULL)
	{
		$this->monitor('Nette\Application\UI\Presenter');
		parent::__construct($parent, $name);
	}

	function getPresenter($need = TRUE)
	{
		return $this->lookup('Nette\Application\UI\Presenter', $need);
	}

	function getUniqueId()
	{
		return $this->lookupPath('Nette\Application\UI\Presenter', TRUE);
	}

	protected function attached($presenter)
	{
		if ($presenter instanceof Presenter) {
			$this->loadState($presenter->popGlobalParams($this->getUniqueId()));
		}
	}

	protected function tryCall($method, array $params)
	{
		$rc = $this->getReflection();
		if ($rc->hasMethod($method)) {
			$rm = $rc->getMethod($method);
			if ($rm->isPublic() && !$rm->isAbstract() && !$rm->isStatic()) {
				$this->checkRequirements($rm);
				$rm->invokeNamedArgs($this, $params);
				return TRUE;
			}
		}
		return FALSE;
	}

	function checkRequirements($element)
	{
	}

	static function getReflection()
	{
		return new PresenterComponentReflection(get_called_class());
	}

	function loadState(array $params)
	{
		foreach ($this->getReflection()->getPersistentParams() as $nm => $meta) {
			if (isset($params[$nm])) {
				if (isset($meta['def'])) {
					if (is_array($params[$nm]) && !is_array($meta['def'])) {
						$params[$nm] = $meta['def'];
					} else {
						settype($params[$nm], gettype($meta['def']));
					}
				}
				$this->$nm = & $params[$nm];
			}
		}
		$this->params = $params;
	}

	function saveState(array & $params, $reflection = NULL)
	{
		$reflection = $reflection === NULL ? $this->getReflection() : $reflection;
		foreach ($reflection->getPersistentParams() as $nm => $meta) {

			if (isset($params[$nm])) {
				$val = $params[$nm];

			} elseif (array_key_exists($nm, $params)) {
				continue;

			} elseif (!isset($meta['since']) || $this instanceof $meta['since']) {
				$val = $this->$nm;

			} else {
				continue;
			}

			if (is_object($val)) {
				$class = get_class($this);
				throw new Nette\InvalidStateException("Persistent parameter must be scalar or array, $class::\$$nm is " . gettype($val));

			} else {
				if (isset($meta['def'])) {
					settype($val, gettype($meta['def']));
					if ($val === $meta['def']) {
						$val = NULL;
					}
				} else {
					if ((string) $val === '') {
						$val = NULL;
					}
				}
				$params[$nm] = $val;
			}
		}
	}

	final function getParam($name = NULL, $default = NULL)
	{
		if (func_num_args() === 0) {
			return $this->params;

		} elseif (isset($this->params[$name])) {
			return $this->params[$name];

		} else {
			return $default;
		}
	}

	final function getParamId($name)
	{
		$uid = $this->getUniqueId();
		return $uid === '' ? $name : $uid . self::NAME_SEPARATOR . $name;
	}

	static function getPersistentParams()
	{
		$rc = new Nette\Reflection\ClassType(get_called_class());
		$params = array();
		foreach ($rc->getProperties(\ReflectionProperty::IS_PUBLIC) as $rp) {
			if (!$rp->isStatic() && $rp->hasAnnotation('persistent')) {
				$params[] = $rp->getName();
			}
		}
		return $params;
	}

	function signalReceived($signal)
	{
		if (!$this->tryCall($this->formatSignalMethod($signal), $this->params)) {
			$class = get_class($this);
			throw new BadSignalException("There is no handler for signal '$signal' in class $class.");
		}
	}

	function formatSignalMethod($signal)
	{
		return $signal == NULL ? NULL : 'handle' . $signal;
	}

	function link($destination, $args = array())
	{
		if (!is_array($args)) {
			$args = func_get_args();
			array_shift($args);
		}

		try {
			return $this->getPresenter()->createRequest($this, $destination, $args, 'link');

		} catch (InvalidLinkException $e) {
			return $this->getPresenter()->handleInvalidLink($e);
		}
	}

	function lazyLink($destination, $args = array())
	{
		if (!is_array($args)) {
			$args = func_get_args();
			array_shift($args);
		}

		return new Link($this, $destination, $args);
	}

	function isLinkCurrent($destination = NULL, $args = array())
	{
		if ($destination !== NULL) {
			if (!is_array($args)) {
				$args = func_get_args();
				array_shift($args);
			}
			$this->link($destination, $args);
		}
		return $this->getPresenter()->getLastCreatedRequestFlag('current');
	}

	function redirect($code, $destination = NULL, $args = array())
	{
		if (!is_numeric($code)) {
			$args = $destination;
			$destination = $code;
			$code = NULL;
		}

		if (!is_array($args)) {
			$args = func_get_args();
			if (is_numeric(array_shift($args))) {
				array_shift($args);
			}
		}

		$presenter = $this->getPresenter();
		$presenter->redirectUrl($presenter->createRequest($this, $destination, $args, 'redirect'), $code);
	}

	final function offsetSet($name, $component)
	{
		$this->addComponent($component, $name);
	}

	final function offsetGet($name)
	{
		return $this->getComponent($name, TRUE);
	}

	final function offsetExists($name)
	{
		return $this->getComponent($name, FALSE) !== NULL;
	}

	final function offsetUnset($name)
	{
		$component = $this->getComponent($name, FALSE);
		if ($component !== NULL) {
			$this->removeComponent($component);
		}
	}

}

abstract class Control extends PresenterComponent implements IPartiallyRenderable
{

	private $template;

	private $invalidSnippets = array();

	public $snippetMode;

	final function getTemplate()
	{
		if ($this->template === NULL) {
			$value = $this->createTemplate();
			if (!$value instanceof Nette\Templating\ITemplate && $value !== NULL) {
				$class2 = get_class($value); $class = get_class($this);
				throw new Nette\UnexpectedValueException("Object returned by $class::createTemplate() must be instance of Nette\\Templating\\ITemplate, '$class2' given.");
			}
			$this->template = $value;
		}
		return $this->template;
	}

	protected function createTemplate($class = NULL)
	{
		$template = $class ? new $class : new Nette\Templating\FileTemplate;
		$presenter = $this->getPresenter(FALSE);
		$template->onPrepareFilters[] = callback($this, 'templatePrepareFilters');
		$template->registerHelperLoader('Nette\Templating\DefaultHelpers::loader');

		$template->control = $this;
		$template->presenter = $presenter;
		if ($presenter instanceof Presenter) {
			$template->setCacheStorage($presenter->getContext()->templateCacheStorage);
			$template->user = $presenter->getUser();
			$template->netteHttpResponse = $presenter->getHttpResponse();
			$template->netteCacheStorage = $presenter->getContext()->cacheStorage;
			$template->baseUri = $template->baseUrl = rtrim($presenter->getHttpRequest()->getUrl()->getBaseUrl(), '/');
			$template->basePath = preg_replace('#https?://[^/]+#A', '', $template->baseUrl);

			if ($presenter->hasFlashSession()) {
				$id = $this->getParamId('flash');
				$template->flashes = $presenter->getFlashSession()->$id;
			}
		}
		if (!isset($template->flashes) || !is_array($template->flashes)) {
			$template->flashes = array();
		}

		return $template;
	}

	function templatePrepareFilters($template)
	{
		$template->registerFilter(new Nette\Latte\Engine);
	}

	function getWidget($name)
	{
		return $this->getComponent($name);
	}

	function flashMessage($message, $type = 'info')
	{
		$id = $this->getParamId('flash');
		$messages = $this->getPresenter()->getFlashSession()->$id;
		$messages[] = $flash = (object) array(
			'message' => $message,
			'type' => $type,
		);
		$this->getTemplate()->flashes = $messages;
		$this->getPresenter()->getFlashSession()->$id = $messages;
		return $flash;
	}

	function invalidateControl($snippet = NULL)
	{
		$this->invalidSnippets[$snippet] = TRUE;
	}

	function validateControl($snippet = NULL)
	{
		if ($snippet === NULL) {
			$this->invalidSnippets = array();

		} else {
			unset($this->invalidSnippets[$snippet]);
		}
	}

	function isControlInvalid($snippet = NULL)
	{
		if ($snippet === NULL) {
			if (count($this->invalidSnippets) > 0) {
				return TRUE;

			} else {
				foreach ($this->getComponents() as $component) {
					if ($component instanceof IRenderable && $component->isControlInvalid()) {

						return TRUE;
					}
				}
				return FALSE;
			}

		} else {
			return isset($this->invalidSnippets[NULL]) || isset($this->invalidSnippets[$snippet]);
		}
	}

	function getSnippetId($name = NULL)
	{

		return 'snippet-' . $this->getUniqueId() . '-' . $name;
	}

}

}

namespace Nette\Forms {

use Nette;

class Container extends Nette\ComponentModel\Container implements \ArrayAccess
{

	public $onValidate;

	protected $currentGroup;

	protected $valid;

	function setDefaults($values, $erase = FALSE)
	{
		$form = $this->getForm(FALSE);
		if (!$form || !$form->isAnchored() || !$form->isSubmitted()) {
			$this->setValues($values, $erase);
		}
		return $this;
	}

	function setValues($values, $erase = FALSE)
	{
		if ($values instanceof \Traversable) {
			$values = iterator_to_array($values);

		} elseif (!is_array($values)) {
			throw new Nette\InvalidArgumentException("First parameter must be an array, " . gettype($values) ." given.");
		}

		foreach ($this->getComponents() as $name => $control) {
			if ($control instanceof IControl) {
				if (array_key_exists($name, $values)) {
					$control->setValue($values[$name]);

				} elseif ($erase) {
					$control->setValue(NULL);
				}

			} elseif ($control instanceof Container) {
				if (array_key_exists($name, $values)) {
					$control->setValues($values[$name], $erase);

				} elseif ($erase) {
					$control->setValues(array(), $erase);
				}
			}
		}
		return $this;
	}

	function getValues()
	{
		$values = new Nette\ArrayHash;
		foreach ($this->getComponents() as $name => $control) {
			if ($control instanceof IControl && !$control->isDisabled() && !$control instanceof ISubmitterControl) {
				$values->$name = $control->getValue();

			} elseif ($control instanceof Container) {
				$values->$name = $control->getValues();
			}
		}
		return $values;
	}

	function isValid()
	{
		if ($this->valid === NULL) {
			$this->validate();
		}
		return $this->valid;
	}

	function validate()
	{
		$this->valid = TRUE;
		$this->onValidate($this);
		foreach ($this->getControls() as $control) {
			if (!$control->getRules()->validate()) {
				$this->valid = FALSE;
			}
		}
	}

	function setCurrentGroup(ControlGroup $group = NULL)
	{
		$this->currentGroup = $group;
		return $this;
	}

	function getCurrentGroup()
	{
		return $this->currentGroup;
	}

	function addComponent(Nette\ComponentModel\IComponent $component, $name, $insertBefore = NULL)
	{
		parent::addComponent($component, $name, $insertBefore);
		if ($this->currentGroup !== NULL && $component instanceof IControl) {
			$this->currentGroup->add($component);
		}
	}

	function getControls()
	{
		return $this->getComponents(TRUE, 'Nette\Forms\IControl');
	}

	function getForm($need = TRUE)
	{
		return $this->lookup('Nette\Forms\Form', $need);
	}

	function addText($name, $label = NULL, $cols = NULL, $maxLength = NULL)
	{
		return $this[$name] = new Controls\TextInput($label, $cols, $maxLength);
	}

	function addPassword($name, $label = NULL, $cols = NULL, $maxLength = NULL)
	{
		$control = new Controls\TextInput($label, $cols, $maxLength);
		$control->setType('password');
		return $this[$name] = $control;
	}

	function addTextArea($name, $label = NULL, $cols = 40, $rows = 10)
	{
		return $this[$name] = new Controls\TextArea($label, $cols, $rows);
	}

	function addUpload($name, $label = NULL)
	{
		return $this[$name] = new Controls\UploadControl($label);
	}

	function addHidden($name, $default = NULL)
	{
		$control = new Controls\HiddenField;
		$control->setDefaultValue($default);
		return $this[$name] = $control;
	}

	function addCheckbox($name, $caption = NULL)
	{
		return $this[$name] = new Controls\Checkbox($caption);
	}

	function addRadioList($name, $label = NULL, array $items = NULL)
	{
		return $this[$name] = new Controls\RadioList($label, $items);
	}

	function addSelect($name, $label = NULL, array $items = NULL, $size = NULL)
	{
		return $this[$name] = new Controls\SelectBox($label, $items, $size);
	}

	function addMultiSelect($name, $label = NULL, array $items = NULL, $size = NULL)
	{
		return $this[$name] = new Controls\MultiSelectBox($label, $items, $size);
	}

	function addSubmit($name, $caption = NULL)
	{
		return $this[$name] = new Controls\SubmitButton($caption);
	}

	function addButton($name, $caption)
	{
		return $this[$name] = new Controls\Button($caption);
	}

	function addImage($name, $src = NULL, $alt = NULL)
	{
		return $this[$name] = new Controls\ImageButton($src, $alt);
	}

	function addContainer($name)
	{
		$control = new Container;
		$control->currentGroup = $this->currentGroup;
		return $this[$name] = $control;
	}

	final function offsetSet($name, $component)
	{
		$this->addComponent($component, $name);
	}

	final function offsetGet($name)
	{
		return $this->getComponent($name, TRUE);
	}

	final function offsetExists($name)
	{
		return $this->getComponent($name, FALSE) !== NULL;
	}

	final function offsetUnset($name)
	{
		$component = $this->getComponent($name, FALSE);
		if ($component !== NULL) {
			$this->removeComponent($component);
		}
	}

	final function __clone()
	{
		throw new Nette\NotImplementedException('Form cloning is not supported yet.');
	}

	function addFile($name, $label = NULL)
	{
		trigger_error(__METHOD__ . '() is deprecated; use addUpload() instead.', E_USER_WARNING);
		return $this->addUpload($name, $label);
	}

}

class Form extends Container
{

	const EQUAL = ':equal',
		IS_IN = ':equal',
		FILLED = ':filled',
		VALID = ':valid';

	const PROTECTION = 'Nette\Forms\Controls\HiddenField::validateEqual';

	const SUBMITTED = ':submitted';

	const MIN_LENGTH = ':minLength',
		MAX_LENGTH = ':maxLength',
		LENGTH = ':length',
		EMAIL = ':email',
		URL = ':url',
		REGEXP = ':regexp',
		PATTERN = ':pattern',
		INTEGER = ':integer',
		NUMERIC = ':integer',
		FLOAT = ':float',
		RANGE = ':range';

	const MAX_FILE_SIZE = ':fileSize',
		MIME_TYPE = ':mimeType',
		IMAGE = ':image';

	const GET = 'get',
		POST = 'post';

	const TRACKER_ID = '_form_';

	const PROTECTOR_ID = '_token_';

	public $onSuccess;

	public $onError;

	public $onSubmit;

	public $onInvalidSubmit;

	private $submittedBy;

	private $httpData;

	private $element;

	private $renderer;

	private $translator;

	private $groups = array();

	private $errors = array();

	function __construct($name = NULL)
	{
		$this->element = Nette\Utils\Html::el('form');
		$this->element->action = '';
		$this->element->method = self::POST;
		$this->element->id = 'frm-' . $name;

		$this->monitor(__CLASS__);
		if ($name !== NULL) {
			$tracker = new Controls\HiddenField($name);
			$tracker->unmonitor(__CLASS__);
			$this[self::TRACKER_ID] = $tracker;
		}
		parent::__construct(NULL, $name);
	}

	protected function attached($obj)
	{
		if ($obj instanceof self) {
			throw new Nette\InvalidStateException('Nested forms are forbidden.');
		}
	}

	final function getForm($need = TRUE)
	{
		return $this;
	}

	function setAction($url)
	{
		$this->element->action = $url;
		return $this;
	}

	function getAction()
	{
		return $this->element->action;
	}

	function setMethod($method)
	{
		if ($this->httpData !== NULL) {
			throw new Nette\InvalidStateException(__METHOD__ . '() must be called until the form is empty.');
		}
		$this->element->method = strtolower($method);
		return $this;
	}

	function getMethod()
	{
		return $this->element->method;
	}

	function addProtection($message = NULL, $timeout = NULL)
	{
		$session = $this->getSession()->getSection('Nette.Forms.Form/CSRF');
		$key = "key$timeout";
		if (isset($session->$key)) {
			$token = $session->$key;
		} else {
			$session->$key = $token = Nette\Utils\Strings::random();
		}
		$session->setExpiration($timeout, $key);
		$this[self::PROTECTOR_ID] = new Controls\HiddenField($token);
		$this[self::PROTECTOR_ID]->addRule(self::PROTECTION, $message, $token);
	}

	function addGroup($caption = NULL, $setAsCurrent = TRUE)
	{
		$group = new ControlGroup;
		$group->setOption('label', $caption);
		$group->setOption('visual', TRUE);

		if ($setAsCurrent) {
			$this->setCurrentGroup($group);
		}

		if (isset($this->groups[$caption])) {
			return $this->groups[] = $group;
		} else {
			return $this->groups[$caption] = $group;
		}
	}

	function removeGroup($name)
	{
		if (is_string($name) && isset($this->groups[$name])) {
			$group = $this->groups[$name];

		} elseif ($name instanceof ControlGroup && in_array($name, $this->groups, TRUE)) {
			$group = $name;
			$name = array_search($group, $this->groups, TRUE);

		} else {
			throw new Nette\InvalidArgumentException("Group not found in form '$this->name'");
		}

		foreach ($group->getControls() as $control) {
			$this->removeComponent($control);
		}

		unset($this->groups[$name]);
	}

	function getGroups()
	{
		return $this->groups;
	}

	function getGroup($name)
	{
		return isset($this->groups[$name]) ? $this->groups[$name] : NULL;
	}

	function setTranslator(Nette\Localization\ITranslator $translator = NULL)
	{
		$this->translator = $translator;
		return $this;
	}

	final function getTranslator()
	{
		return $this->translator;
	}

	function isAnchored()
	{
		return TRUE;
	}

	final function isSubmitted()
	{
		if ($this->submittedBy === NULL) {
			$this->getHttpData();
			$this->submittedBy = !empty($this->httpData);
		}
		return $this->submittedBy;
	}

	function setSubmittedBy(ISubmitterControl $by = NULL)
	{
		$this->submittedBy = $by === NULL ? FALSE : $by;
		return $this;
	}

	final function getHttpData()
	{
		if ($this->httpData === NULL) {
			if (!$this->isAnchored()) {
				throw new Nette\InvalidStateException('Form is not anchored and therefore can not determine whether it was submitted.');
			}
			$this->httpData = (array) $this->receiveHttpData();
		}
		return $this->httpData;
	}

	function fireEvents()
	{
		if (!$this->isSubmitted()) {
			return;

		} elseif ($this->submittedBy instanceof ISubmitterControl) {
			if (!$this->submittedBy->getValidationScope() || $this->isValid()) {
				$this->submittedBy->click();
				$valid = TRUE;
			} else {
				$this->submittedBy->onInvalidClick($this->submittedBy);
			}
		}

		if (isset($valid) || $this->isValid()) {
			$this->onSuccess($this);
		} else {
			$this->onError($this);
			if ($this->onInvalidSubmit) {
				trigger_error(__CLASS__ . '->onInvalidSubmit is deprecated; use onError instead.', E_USER_WARNING);
				$this->onInvalidSubmit($this);
			}
		}

		if ($this->onSuccess) {
			$this->onSubmit($this);
		} elseif ($this->onSubmit) {
			trigger_error(__CLASS__ . '->onSubmit changed its behavior; use onSuccess instead.', E_USER_WARNING);
			if (isset($valid) || $this->isValid()) {
				$this->onSubmit($this);
			}
		}
	}

	protected function receiveHttpData()
	{
		$httpRequest = $this->getHttpRequest();
		if (strcasecmp($this->getMethod(), $httpRequest->getMethod())) {
			return;
		}

		if ($httpRequest->isMethod('post')) {
			$data = Nette\Utils\Arrays::mergeTree($httpRequest->getPost(), $httpRequest->getFiles());
		} else {
			$data = $httpRequest->getQuery();
		}

		if ($tracker = $this->getComponent(self::TRACKER_ID, FALSE)) {
			if (!isset($data[self::TRACKER_ID]) || $data[self::TRACKER_ID] !== $tracker->getValue()) {
				return;
			}
		}

		return $data;
	}

	function getValues()
	{
		$values = parent::getValues();
		unset($values[self::TRACKER_ID], $values[self::PROTECTOR_ID]);
		return $values;
	}

	function addError($message)
	{
		$this->valid = FALSE;
		if ($message !== NULL && !in_array($message, $this->errors, TRUE)) {
			$this->errors[] = $message;
		}
	}

	function getErrors()
	{
		return $this->errors;
	}

	function hasErrors()
	{
		return (bool) $this->getErrors();
	}

	function cleanErrors()
	{
		$this->errors = array();
		$this->valid = NULL;
	}

	function getElementPrototype()
	{
		return $this->element;
	}

	function setRenderer(IFormRenderer $renderer)
	{
		$this->renderer = $renderer;
		return $this;
	}

	final function getRenderer()
	{
		if ($this->renderer === NULL) {
			$this->renderer = new Rendering\DefaultFormRenderer;
		}
		return $this->renderer;
	}

	function render()
	{
		$args = func_get_args();
		array_unshift($args, $this);
		echo call_user_func_array(array($this->getRenderer(), 'render'), $args);
	}

	function __toString()
	{
		try {
			return $this->getRenderer()->render($this);

		} catch (\Exception $e) {
			if (func_get_args() && func_get_arg(0)) {
				throw $e;
			} else {
				Nette\Diagnostics\Debugger::toStringException($e);
			}
		}
	}

	protected function getHttpRequest()
	{
		return Nette\Environment::getHttpRequest();
	}

	protected function getSession()
	{
		return Nette\Environment::getSession();
	}

}

}

namespace Nette\Application\UI {

use Nette;

class Form extends Nette\Forms\Form implements ISignalReceiver
{

	function __construct(Nette\ComponentModel\IContainer $parent = NULL, $name = NULL)
	{
		parent::__construct();
		$this->monitor('Nette\Application\UI\Presenter');
		if ($parent !== NULL) {
			$parent->addComponent($this, $name);
		}
	}

	function getPresenter($need = TRUE)
	{
		return $this->lookup('Nette\Application\UI\Presenter', $need);
	}

	protected function attached($presenter)
	{
		if ($presenter instanceof Presenter) {
			$name = $this->lookupPath('Nette\Application\UI\Presenter');

			if (!isset($this->getElementPrototype()->id)) {
				$this->getElementPrototype()->id = 'frm-' . $name;
			}

			$this->setAction(new Link(
				$presenter,
				$name . self::NAME_SEPARATOR . 'submit!',
				array()
			));

			if ($this->isSubmitted()) {
				foreach ($this->getControls() as $control) {
					$control->loadHttpData();
				}
			}
		}
		parent::attached($presenter);
	}

	function isAnchored()
	{
		return (bool) $this->getPresenter(FALSE);
	}

	protected function receiveHttpData()
	{
		$presenter = $this->getPresenter();
		if (!$presenter->isSignalReceiver($this, 'submit')) {
			return;
		}

		$isPost = $this->getMethod() === self::POST;
		$request = $presenter->getRequest();
		if ($request->isMethod('forward') || $request->isMethod('post') !== $isPost) {
			return;
		}

		if ($isPost) {
			return Nette\Utils\Arrays::mergeTree($request->getPost(), $request->getFiles());
		} else {
			return $request->getParams();
		}
	}

	function signalReceived($signal)
	{
		if ($signal === 'submit') {
			$this->fireEvents();
		} else {
			$class = get_class($this);
			throw new BadSignalException("Missing handler for signal '$signal' in $class.");
		}
	}

}

class InvalidLinkException extends \Exception
{
}

class Link extends Nette\Object
{

	private $component;

	private $destination;

	private $params;

	function __construct(PresenterComponent $component, $destination, array $params)
	{
		$this->component = $component;
		$this->destination = $destination;
		$this->params = $params;
	}

	function getDestination()
	{
		return $this->destination;
	}

	function setParam($key, $value)
	{
		$this->params[$key] = $value;
		return $this;
	}

	function getParam($key)
	{
		return isset($this->params[$key]) ? $this->params[$key] : NULL;
	}

	function getParams()
	{
		return $this->params;
	}

	function __toString()
	{
		try {
			return $this->component->link($this->destination, $this->params);

		} catch (\Exception $e) {
			Nette\Diagnostics\Debugger::toStringException($e);
		}
	}

}

use Nette\Application;use Nette\Application\Responses;use Nette\Http;use Nette\Reflection;

abstract class Presenter extends Control implements Application\IPresenter
{

	const INVALID_LINK_SILENT = 1,
		INVALID_LINK_WARNING = 2,
		INVALID_LINK_EXCEPTION = 3;

	const SIGNAL_KEY = 'do',
		ACTION_KEY = 'action',
		FLASH_KEY = '_fid',
		DEFAULT_ACTION = 'default';

	public static $invalidLinkMode;

	public $onShutdown;

	private $request;

	private $response;

	public $autoCanonicalize = TRUE;

	public $absoluteUrls = FALSE;

	private $globalParams;

	private $globalState;

	private $globalStateSinces;

	private $action;

	private $view;

	private $layout;

	private $payload;

	private $signalReceiver;

	private $signal;

	private $ajaxMode;

	private $startupCheck;

	private $lastCreatedRequest;

	private $lastCreatedRequestFlag;

	private $context;

	final function getRequest()
	{
		return $this->request;
	}

	final function getPresenter($need = TRUE)
	{
		return $this;
	}

	final function getUniqueId()
	{
		return '';
	}

	function run(Application\Request $request)
	{
		try {

			$this->request = $request;
			$this->payload = (object) NULL;
			$this->setParent($this->getParent(), $request->getPresenterName());

			$this->initGlobalParams();
			$this->checkRequirements($this->getReflection());
			$this->startup();
			if (!$this->startupCheck) {
				$class = $this->getReflection()->getMethod('startup')->getDeclaringClass()->getName();
				throw new Nette\InvalidStateException("Method $class::startup() or its descendant doesn't call parent::startup().");
			}

			$this->tryCall($this->formatActionMethod($this->getAction()), $this->params);

			if ($this->autoCanonicalize) {
				$this->canonicalize();
			}
			if ($this->getHttpRequest()->isMethod('head')) {
				$this->terminate();
			}

			$this->processSignal();

			$this->beforeRender();

			$this->tryCall($this->formatRenderMethod($this->getView()), $this->params);
			$this->afterRender();

			$this->saveGlobalState();
			if ($this->isAjax()) {
				$this->payload->state = $this->getGlobalState();
			}

			$this->sendTemplate();

		} catch (Application\AbortException $e) {

			if ($this->isAjax()) try {
				$hasPayload = (array) $this->payload; unset($hasPayload['state']);
				if ($this->response instanceof Responses\TextResponse && $this->isControlInvalid()) {
					$this->snippetMode = TRUE;
					$this->response->send($this->getHttpRequest(), $this->getHttpResponse());
					$this->sendPayload();

				} elseif (!$this->response && $hasPayload) {
					$this->sendPayload();
				}
			} catch (Application\AbortException $e) { }

			if ($this->hasFlashSession()) {
				$this->getFlashSession()->setExpiration($this->response instanceof Responses\RedirectResponse ? '+ 30 seconds': '+ 3 seconds');
			}

			$this->onShutdown($this, $this->response);
			$this->shutdown($this->response);

			return $this->response;
		}
	}

	protected function startup()
	{
		$this->startupCheck = TRUE;
	}

	protected function beforeRender()
	{
	}

	protected function afterRender()
	{
	}

	protected function shutdown($response)
	{
	}

	function checkRequirements($element)
	{
		$user = (array) $element->getAnnotation('User');
		if (in_array('loggedIn', $user) && !$this->getUser()->isLoggedIn()) {
			throw new Application\ForbiddenRequestException;
		}
	}

	function processSignal()
	{
		if ($this->signal === NULL) {
			return;
		}

		$component = $this->signalReceiver === '' ? $this : $this->getComponent($this->signalReceiver, FALSE);
		if ($component === NULL) {
			throw new BadSignalException("The signal receiver component '$this->signalReceiver' is not found.");

		} elseif (!$component instanceof ISignalReceiver) {
			throw new BadSignalException("The signal receiver component '$this->signalReceiver' is not ISignalReceiver implementor.");
		}

		$component->signalReceived($this->signal);
		$this->signal = NULL;
	}

	final function getSignal()
	{
		return $this->signal === NULL ? NULL : array($this->signalReceiver, $this->signal);
	}

	final function isSignalReceiver($component, $signal = NULL)
	{
		if ($component instanceof Nette\ComponentModel\Component) {
			$component = $component === $this ? '' : $component->lookupPath(__CLASS__, TRUE);
		}

		if ($this->signal === NULL) {
			return FALSE;

		} elseif ($signal === TRUE) {
			return $component === ''
				|| strncmp($this->signalReceiver . '-', $component . '-', strlen($component) + 1) === 0;

		} elseif ($signal === NULL) {
			return $this->signalReceiver === $component;

		} else {
			return $this->signalReceiver === $component && strcasecmp($signal, $this->signal) === 0;
		}
	}

	final function getAction($fullyQualified = FALSE)
	{
		return $fullyQualified ? ':' . $this->getName() . ':' . $this->action : $this->action;
	}

	function changeAction($action)
	{
		if (Nette\Utils\Strings::match($action, "#^[a-zA-Z0-9][a-zA-Z0-9_\x7f-\xff]*$#")) {
			$this->action = $action;
			$this->view = $action;

		} else {
			throw new Application\BadRequestException("Action name '$action' is not alphanumeric string.");
		}
	}

	final function getView()
	{
		return $this->view;
	}

	function setView($view)
	{
		$this->view = (string) $view;
		return $this;
	}

	final function getLayout()
	{
		return $this->layout;
	}

	function setLayout($layout)
	{
		$this->layout = $layout === FALSE ? FALSE : (string) $layout;
		return $this;
	}

	function sendTemplate()
	{
		$template = $this->getTemplate();
		if (!$template) {
			return;
		}

		if ($template instanceof Nette\Templating\IFileTemplate && !$template->getFile()) {
			$files = $this->formatTemplateFiles();
			foreach ($files as $file) {
				if (is_file($file)) {
					$template->setFile($file);
					break;
				}
			}

			if (!$template->getFile()) {
				$file = preg_replace('#^.*([/\\\\].{1,70})$#U', "\xE2\x80\xA6\$1", reset($files));
				$file = strtr($file, '/', DIRECTORY_SEPARATOR);
				throw new Application\BadRequestException("Page not found. Missing template '$file'.");
			}
		}

		if ($this->layout !== FALSE) {
			$files = $this->formatLayoutTemplateFiles();
			foreach ($files as $file) {
				if (is_file($file)) {
					$template->layout = $file;
					$template->_extends = $file;
					break;
				}
			}

			if (empty($template->layout) && $this->layout !== NULL) {
				$file = preg_replace('#^.*([/\\\\].{1,70})$#U', "\xE2\x80\xA6\$1", reset($files));
				$file = strtr($file, '/', DIRECTORY_SEPARATOR);
				throw new Nette\FileNotFoundException("Layout not found. Missing template '$file'.");
			}
		}

		$this->sendResponse(new Responses\TextResponse($template));
	}

	function formatLayoutTemplateFiles()
	{
		$name = $this->getName();
		$presenter = substr($name, strrpos(':' . $name, ':'));
		$layout = $this->layout ? $this->layout : 'layout';
		$dir = dirname(dirname($this->getReflection()->getFileName()));
		$list = array(
			"$dir/templates/$presenter/@$layout.latte",
			"$dir/templates/$presenter.@$layout.latte",
			"$dir/templates/$presenter/@$layout.phtml",
			"$dir/templates/$presenter.@$layout.phtml",
		);
		do {
			$list[] = "$dir/templates/@$layout.latte";
			$list[] = "$dir/templates/@$layout.phtml";
			$dir = dirname($dir);
		} while ($dir && ($name = substr($name, 0, strrpos($name, ':'))));
		return $list;
	}

	function formatTemplateFiles()
	{
		$name = $this->getName();
		$presenter = substr($name, strrpos(':' . $name, ':'));
		$dir = dirname(dirname($this->getReflection()->getFileName()));
		return array(
			"$dir/templates/$presenter/$this->view.latte",
			"$dir/templates/$presenter.$this->view.latte",
			"$dir/templates/$presenter/$this->view.phtml",
			"$dir/templates/$presenter.$this->view.phtml",
		);
	}

	protected static function formatActionMethod($action)
	{
		return 'action' . $action;
	}

	protected static function formatRenderMethod($view)
	{
		return 'render' . $view;
	}

	final function getPayload()
	{
		return $this->payload;
	}

	function isAjax()
	{
		if ($this->ajaxMode === NULL) {
			$this->ajaxMode = $this->getHttpRequest()->isAjax();
		}
		return $this->ajaxMode;
	}

	function sendPayload()
	{
		$this->sendResponse(new Responses\JsonResponse($this->payload));
	}

	function sendResponse(Application\IResponse $response)
	{
		$this->response = $response;
		$this->terminate();
	}

	function terminate()
	{
		if (func_num_args() !== 0) {
			trigger_error(__METHOD__ . ' is not intended to send a Application\Response; use sendResponse() instead.', E_USER_WARNING);
			$this->sendResponse(func_get_arg(0));
		}
		throw new Application\AbortException();
	}

	function forward($destination, $args = array())
	{
		if ($destination instanceof Application\Request) {
			$this->sendResponse(new Responses\ForwardResponse($destination));

		} elseif (!is_array($args)) {
			$args = func_get_args();
			array_shift($args);
		}

		$this->createRequest($this, $destination, $args, 'forward');
		$this->sendResponse(new Responses\ForwardResponse($this->lastCreatedRequest));
	}

	function redirectUrl($url, $code = NULL)
	{
		if ($this->isAjax()) {
			$this->payload->redirect = (string) $url;
			$this->sendPayload();

		} elseif (!$code) {
			$code = $this->getHttpRequest()->isMethod('post')
				? Http\IResponse::S303_POST_GET
				: Http\IResponse::S302_FOUND;
		}
		$this->sendResponse(new Responses\RedirectResponse($url, $code));
	}

	function redirectUri($url, $code = NULL)
	{
		trigger_error(__METHOD__ . '() is deprecated; use ' . __CLASS__ . '::redirectUrl() instead.', E_USER_WARNING);
		$this->redirectUrl($url, $code);
	}

	function backlink()
	{
		return $this->getAction(TRUE);
	}

	function getLastCreatedRequest()
	{
		return $this->lastCreatedRequest;
	}

	function getLastCreatedRequestFlag($flag)
	{
		return !empty($this->lastCreatedRequestFlag[$flag]);
	}

	function canonicalize()
	{
		if (!$this->isAjax() && ($this->request->isMethod('get') || $this->request->isMethod('head'))) {
			$url = $this->createRequest($this, $this->action, $this->getGlobalState() + $this->request->params, 'redirectX');
			if ($url !== NULL && !$this->getHttpRequest()->getUrl()->isEqual($url)) {
				$this->sendResponse(new Responses\RedirectResponse($url, Http\IResponse::S301_MOVED_PERMANENTLY));
			}
		}
	}

	function lastModified($lastModified, $etag = NULL, $expire = NULL)
	{
		if ($expire !== NULL) {
			$this->getHttpResponse()->setExpiration($expire);
		}

		if (!$this->getHttpContext()->isModified($lastModified, $etag)) {
			$this->terminate();
		}
	}

	final protected function createRequest($component, $destination, array $args, $mode)
	{

		static $presenterFactory, $router, $refUrl;
		if ($presenterFactory === NULL) {
			$presenterFactory = $this->getApplication()->getPresenterFactory();
			$router = $this->getApplication()->getRouter();
			$refUrl = new Http\Url($this->getHttpRequest()->getUrl());
			$refUrl->setPath($this->getHttpRequest()->getUrl()->getScriptPath());
		}

		$this->lastCreatedRequest = $this->lastCreatedRequestFlag = NULL;

		$a = strpos($destination, '#');
		if ($a === FALSE) {
			$fragment = '';
		} else {
			$fragment = substr($destination, $a);
			$destination = substr($destination, 0, $a);
		}

		$a = strpos($destination, '?');
		if ($a !== FALSE) {
			parse_str(substr($destination, $a + 1), $args);
			$destination = substr($destination, 0, $a);
		}

		$a = strpos($destination, '//');
		if ($a === FALSE) {
			$scheme = FALSE;
		} else {
			$scheme = substr($destination, 0, $a);
			$destination = substr($destination, $a + 2);
		}

		if (!$component instanceof Presenter || substr($destination, -1) === '!') {
			$signal = rtrim($destination, '!');
			$a = strrpos($signal, ':');
			if ($a !== FALSE) {
				$component = $component->getComponent(strtr(substr($signal, 0, $a), ':', '-'));
				$signal = (string) substr($signal, $a + 1);
			}
			if ($signal == NULL) {
				throw new InvalidLinkException("Signal must be non-empty string.");
			}
			$destination = 'this';
		}

		if ($destination == NULL) {
			throw new InvalidLinkException("Destination must be non-empty string.");
		}

		$current = FALSE;
		$a = strrpos($destination, ':');
		if ($a === FALSE) {
			$action = $destination === 'this' ? $this->action : $destination;
			$presenter = $this->getName();
			$presenterClass = get_class($this);

		} else {
			$action = (string) substr($destination, $a + 1);
			if ($destination[0] === ':') {
				if ($a < 2) {
					throw new InvalidLinkException("Missing presenter name in '$destination'.");
				}
				$presenter = substr($destination, 1, $a - 1);

			} else {
				$presenter = $this->getName();
				$b = strrpos($presenter, ':');
				if ($b === FALSE) {
					$presenter = substr($destination, 0, $a);
				} else {
					$presenter = substr($presenter, 0, $b + 1) . substr($destination, 0, $a);
				}
			}
			try {
				$presenterClass = $presenterFactory->getPresenterClass($presenter);
			} catch (Application\InvalidPresenterException $e) {
				throw new InvalidLinkException($e->getMessage(), NULL, $e);
			}
		}

		if (isset($signal)) {
			$reflection = new PresenterComponentReflection(get_class($component));
			if ($signal === 'this') {
				$signal = '';
				if (array_key_exists(0, $args)) {
					throw new InvalidLinkException("Unable to pass parameters to 'this!' signal.");
				}

			} elseif (strpos($signal, self::NAME_SEPARATOR) === FALSE) {

				$method = $component->formatSignalMethod($signal);
				if (!$reflection->hasCallableMethod($method)) {
					throw new InvalidLinkException("Unknown signal '$signal', missing handler {$reflection->name}::$method()");
				}
				if ($args) {
					self::argsToParams(get_class($component), $method, $args);
				}
			}

			if ($args && array_intersect_key($args, $reflection->getPersistentParams())) {
				$component->saveState($args);
			}

			if ($args && $component !== $this) {
				$prefix = $component->getUniqueId() . self::NAME_SEPARATOR;
				foreach ($args as $key => $val) {
					unset($args[$key]);
					$args[$prefix . $key] = $val;
				}
			}
		}

		if (is_subclass_of($presenterClass, __CLASS__)) {
			if ($action === '') {
				$action = self::DEFAULT_ACTION;
			}

			$current = ($action === '*' || $action === $this->action) && $presenterClass === get_class($this);

			$reflection = new PresenterComponentReflection($presenterClass);
			if ($args || $destination === 'this') {

				$method = $presenterClass::formatActionMethod($action);
				if (!$reflection->hasCallableMethod($method)) {
					$method = $presenterClass::formatRenderMethod($action);
					if (!$reflection->hasCallableMethod($method)) {
						$method = NULL;
					}
				}

				if ($method === NULL) {
					if (array_key_exists(0, $args)) {
						throw new InvalidLinkException("Unable to pass parameters to action '$presenter:$action', missing corresponding method.");
					}

				} elseif ($destination === 'this') {
					self::argsToParams($presenterClass, $method, $args, $this->params);

				} else {
					self::argsToParams($presenterClass, $method, $args);
				}
			}

			if ($args && array_intersect_key($args, $reflection->getPersistentParams())) {
				$this->saveState($args, $reflection);
			}

			$globalState = $this->getGlobalState($destination === 'this' ? NULL : $presenterClass);
			if ($current && $args) {
				$tmp = $globalState + $this->params;
				foreach ($args as $key => $val) {
					if ((string) $val !== (isset($tmp[$key]) ? (string) $tmp[$key] : '')) {
						$current = FALSE;
						break;
					}
				}
			}
			$args += $globalState;
		}

		$args[self::ACTION_KEY] = $action;
		if (!empty($signal)) {
			$args[self::SIGNAL_KEY] = $component->getParamId($signal);
			$current = $current && $args[self::SIGNAL_KEY] === $this->getParam(self::SIGNAL_KEY);
		}
		if (($mode === 'redirect' || $mode === 'forward') && $this->hasFlashSession()) {
			$args[self::FLASH_KEY] = $this->getParam(self::FLASH_KEY);
		}

		$this->lastCreatedRequest = new Application\Request(
			$presenter,
			Application\Request::FORWARD,
			$args,
			array(),
			array()
		);
		$this->lastCreatedRequestFlag = array('current' => $current);

		if ($mode === 'forward') {
			return;
		}

		$url = $router->constructUrl($this->lastCreatedRequest, $refUrl);
		if ($url === NULL) {
			unset($args[self::ACTION_KEY]);
			$params = urldecode(http_build_query($args, NULL, ', '));
			throw new InvalidLinkException("No route for $presenter:$action($params)");
		}

		if ($mode === 'link' && $scheme === FALSE && !$this->absoluteUrls) {
			$hostUrl = $refUrl->getHostUrl();
			if (strncmp($url, $hostUrl, strlen($hostUrl)) === 0) {
				$url = substr($url, strlen($hostUrl));
			}
		}

		return $url . $fragment;
	}

	private static function argsToParams($class, $method, & $args, $supplemental = array())
	{
		static $cache;
		$params = & $cache[strtolower($class . ':' . $method)];
		if ($params === NULL) {
			$params = Reflection\Method::from($class, $method)->getDefaultParameters();
		}
		$i = 0;
		foreach ($params as $name => $def) {
			if (array_key_exists($i, $args)) {
				$args[$name] = $args[$i];
				unset($args[$i]);
				$i++;

			} elseif (array_key_exists($name, $args)) {

			} elseif (array_key_exists($name, $supplemental)) {
				$args[$name] = $supplemental[$name];

			} else {
				continue;
			}

			if ($def === NULL) {
				if ((string) $args[$name] === '') {
					$args[$name] = NULL;
				}
			} else {
				settype($args[$name], gettype($def));
				if ($args[$name] === $def) {
					$args[$name] = NULL;
				}
			}
		}

		if (array_key_exists($i, $args)) {
			$method = Reflection\Method::from($class, $method)->getName();
			throw new InvalidLinkException("Passed more parameters than method $class::$method() expects.");
		}
	}

	protected function handleInvalidLink($e)
	{
		if (self::$invalidLinkMode === self::INVALID_LINK_SILENT) {
			return '#';

		} elseif (self::$invalidLinkMode === self::INVALID_LINK_WARNING) {
			return 'error: ' . $e->getMessage();

		} else {
			throw $e;
		}
	}

	static function getPersistentComponents()
	{
		return (array) Reflection\ClassType::from(get_called_class())
			->getAnnotation('persistent');
	}

	private function getGlobalState($forClass = NULL)
	{
		$sinces = & $this->globalStateSinces;

		if ($this->globalState === NULL) {
			$state = array();
			foreach ($this->globalParams as $id => $params) {
				$prefix = $id . self::NAME_SEPARATOR;
				foreach ($params as $key => $val) {
					$state[$prefix . $key] = $val;
				}
			}
			$this->saveState($state, $forClass ? new PresenterComponentReflection($forClass) : NULL);

			if ($sinces === NULL) {
				$sinces = array();
				foreach ($this->getReflection()->getPersistentParams() as $nm => $meta) {
					$sinces[$nm] = $meta['since'];
				}
			}

			$components = $this->getReflection()->getPersistentComponents();
			$iterator = $this->getComponents(TRUE, 'Nette\Application\UI\IStatePersistent');

			foreach ($iterator as $name => $component) {
				if ($iterator->getDepth() === 0) {

					$since = isset($components[$name]['since']) ? $components[$name]['since'] : FALSE;
				}
				$prefix = $component->getUniqueId() . self::NAME_SEPARATOR;
				$params = array();
				$component->saveState($params);
				foreach ($params as $key => $val) {
					$state[$prefix . $key] = $val;
					$sinces[$prefix . $key] = $since;
				}
			}

		} else {
			$state = $this->globalState;
		}

		if ($forClass !== NULL) {
			$since = NULL;
			foreach ($state as $key => $foo) {
				if (!isset($sinces[$key])) {
					$x = strpos($key, self::NAME_SEPARATOR);
					$x = $x === FALSE ? $key : substr($key, 0, $x);
					$sinces[$key] = isset($sinces[$x]) ? $sinces[$x] : FALSE;
				}
				if ($since !== $sinces[$key]) {
					$since = $sinces[$key];
					$ok = $since && (is_subclass_of($forClass, $since) || $forClass === $since);
				}
				if (!$ok) {
					unset($state[$key]);
				}
			}
		}

		return $state;
	}

	protected function saveGlobalState()
	{

		foreach ($this->globalParams as $id => $foo) {
			$this->getComponent($id, FALSE);
		}

		$this->globalParams = array();
		$this->globalState = $this->getGlobalState();
	}

	private function initGlobalParams()
	{

		$this->globalParams = array();
		$selfParams = array();

		$params = $this->request->getParams();
		if ($this->isAjax()) {
			$params = $this->request->getPost() + $params;
		}

		foreach ($params as $key => $value) {
			$a = strlen($key) > 2 ? strrpos($key, self::NAME_SEPARATOR, -2) : FALSE;
			if ($a === FALSE) {
				$selfParams[$key] = $value;
			} else {
				$this->globalParams[substr($key, 0, $a)][substr($key, $a + 1)] = $value;
			}
		}

		$this->changeAction(isset($selfParams[self::ACTION_KEY]) ? $selfParams[self::ACTION_KEY] : self::DEFAULT_ACTION);

		$this->signalReceiver = $this->getUniqueId();
		if (!empty($selfParams[self::SIGNAL_KEY])) {
			$param = $selfParams[self::SIGNAL_KEY];
			$pos = strrpos($param, '-');
			if ($pos) {
				$this->signalReceiver = substr($param, 0, $pos);
				$this->signal = substr($param, $pos + 1);
			} else {
				$this->signalReceiver = $this->getUniqueId();
				$this->signal = $param;
			}
			if ($this->signal == NULL) {
				$this->signal = NULL;
			}
		}

		$this->loadState($selfParams);
	}

	final function popGlobalParams($id)
	{
		if (isset($this->globalParams[$id])) {
			$res = $this->globalParams[$id];
			unset($this->globalParams[$id]);
			return $res;

		} else {
			return array();
		}
	}

	function hasFlashSession()
	{
		return !empty($this->params[self::FLASH_KEY])
			&& $this->getSession()->hasSection('Nette.Application.Flash/' . $this->params[self::FLASH_KEY]);
	}

	function getFlashSession()
	{
		if (empty($this->params[self::FLASH_KEY])) {
			$this->params[self::FLASH_KEY] = Nette\Utils\Strings::random(4);
		}
		return $this->getSession('Nette.Application.Flash/' . $this->params[self::FLASH_KEY]);
	}

	function setContext(Nette\DI\IContainer $context)
	{
		$this->context = $context;
		return $this;
	}

	final function getContext()
	{
		return $this->context;
	}

	final function getService($name)
	{
		return $this->context->getService($name);
	}

	protected function getHttpRequest()
	{
		return $this->context->httpRequest;
	}

	protected function getHttpResponse()
	{
		return $this->context->httpResponse;
	}

	protected function getHttpContext()
	{
		return $this->context->httpContext;
	}

	function getApplication()
	{
		return $this->context->application;
	}

	function getSession($namespace = NULL)
	{
		$handler = $this->context->session;
		return $namespace === NULL ? $handler : $handler->getSection($namespace);
	}

	function getUser()
	{
		return $this->context->user;
	}

}

}

namespace Nette\Reflection {

use Nette;use Nette\ObjectMixin;

class ClassType extends \ReflectionClass
{

	private static $extMethods;

	static function from($class)
	{
		return new static($class);
	}

	function __toString()
	{
		return 'Class ' . $this->getName();
	}

	function hasEventProperty($name)
	{
		if (preg_match('#^on[A-Z]#', $name) && $this->hasProperty($name)) {
			$rp = $this->getProperty($name);
			return $rp->isPublic() && !$rp->isStatic();
		}
		return FALSE;
	}

	function setExtensionMethod($name, $callback)
	{
		$l = & self::$extMethods[strtolower($name)];
		$l[strtolower($this->getName())] = callback($callback);
		$l[''] = NULL;
		return $this;
	}

	function getExtensionMethod($name)
	{
		$class = strtolower($this->getName());
		$l = & self::$extMethods[strtolower($name)];

		if (empty($l)) {
			return FALSE;

		} elseif (isset($l[''][$class])) {
			return $l[''][$class];
		}

		$cl = $class;
		do {
			if (isset($l[$cl])) {
				return $l[''][$class] = $l[$cl];
			}
		} while (($cl = strtolower(get_parent_class($cl))) !== '');

		foreach (class_implements($class) as $cl) {
			$cl = strtolower($cl);
			if (isset($l[$cl])) {
				return $l[''][$class] = $l[$cl];
			}
		}
		return $l[''][$class] = FALSE;
	}

	function getConstructor()
	{
		return ($ref = parent::getConstructor()) ? Method::from($this->getName(), $ref->getName()) : NULL;
	}

	function getExtension()
	{
		return ($name = $this->getExtensionName()) ? new Extension($name) : NULL;
	}

	function getInterfaces()
	{
		$res = array();
		foreach (parent::getInterfaceNames() as $val) {
			$res[$val] = new static($val);
		}
		return $res;
	}

	function getMethod($name)
	{
		return new Method($this->getName(), $name);
	}

	function getMethods($filter = -1)
	{
		foreach ($res = parent::getMethods($filter) as $key => $val) {
			$res[$key] = new Method($this->getName(), $val->getName());
		}
		return $res;
	}

	function getParentClass()
	{
		return ($ref = parent::getParentClass()) ? new static($ref->getName()) : NULL;
	}

	function getProperties($filter = -1)
	{
		foreach ($res = parent::getProperties($filter) as $key => $val) {
			$res[$key] = new Property($this->getName(), $val->getName());
		}
		return $res;
	}

	function getProperty($name)
	{
		return new Property($this->getName(), $name);
	}

	function hasAnnotation($name)
	{
		$res = AnnotationsParser::getAll($this);
		return !empty($res[$name]);
	}

	function getAnnotation($name)
	{
		$res = AnnotationsParser::getAll($this);
		return isset($res[$name]) ? end($res[$name]) : NULL;
	}

	function getAnnotations()
	{
		return AnnotationsParser::getAll($this);
	}

	function getDescription()
	{
		return $this->getAnnotation('description');
	}

	static function getReflection()
	{
		return new ClassType(get_called_class());
	}

	function __call($name, $args)
	{
		return ObjectMixin::call($this, $name, $args);
	}

	function &__get($name)
	{
		return ObjectMixin::get($this, $name);
	}

	function __set($name, $value)
	{
		return ObjectMixin::set($this, $name, $value);
	}

	function __isset($name)
	{
		return ObjectMixin::has($this, $name);
	}

	function __unset($name)
	{
		ObjectMixin::remove($this, $name);
	}

}

}

namespace Nette\Application\UI {

use Nette;

class PresenterComponentReflection extends Nette\Reflection\ClassType
{

	private static $ppCache = array();

	private static $pcCache = array();

	private static $mcCache = array();

	function getPersistentParams($class = NULL)
	{
		$class = $class === NULL ? $this->getName() : $class;
		$params = & self::$ppCache[$class];
		if ($params !== NULL) {
			return $params;
		}
		$params = array();
		if (is_subclass_of($class, 'Nette\Application\UI\PresenterComponent')) {

			$defaults = get_class_vars($class);
			foreach (call_user_func(array($class, 'getPersistentParams'), $class) as $name => $meta) {
				if (is_string($meta)) {
					$name = $meta;
				}
				$params[$name] = array(
					'def' => $defaults[$name],
					'since' => $class,
				);
			}
			foreach ($this->getPersistentParams(get_parent_class($class)) as $name => $param) {
				if (isset($params[$name])) {
					$params[$name]['since'] = $param['since'];
					continue;
				}

				$params[$name] = $param;
			}
		}
		return $params;
	}

	function getPersistentComponents()
	{
		$class = $this->getName();
		$components = & self::$pcCache[$class];
		if ($components !== NULL) {
			return $components;
		}
		$components = array();
		if (is_subclass_of($class, 'Nette\Application\UI\Presenter')) {

			foreach (call_user_func(array($class, 'getPersistentComponents'), $class) as $name => $meta) {
				if (is_string($meta)) {
					$name = $meta;
				}
				$components[$name] = array('since' => $class);
			}
			$components = self::getPersistentComponents(get_parent_class($class)) + $components;
		}
		return $components;
	}

	function hasCallableMethod($method)
	{
		$class = $this->getName();
		$cache = & self::$mcCache[strtolower($class . ':' . $method)];
		if ($cache === NULL) try {
			$cache = FALSE;
			$rm = Nette\Reflection\Method::from($class, $method);
			$cache = $this->isInstantiable() && $rm->isPublic() && !$rm->isAbstract() && !$rm->isStatic();
		} catch (\ReflectionException $e) {
		}
		return $cache;
	}

}

}

namespace Nette\Caching {

use Nette;

class Cache extends Nette\Object implements \ArrayAccess
{

	const PRIORITY = 'priority',
		EXPIRATION = 'expire',
		EXPIRE = 'expire',
		SLIDING = 'sliding',
		TAGS = 'tags',
		FILES = 'files',
		ITEMS = 'items',
		CONSTS = 'consts',
		CALLBACKS = 'callbacks',
		ALL = 'all';

	const NAMESPACE_SEPARATOR = "\x00";

	private $storage;

	private $namespace;

	private $key;

	private $data;

	function __construct(IStorage $storage, $namespace = NULL)
	{
		$this->storage = $storage;
		$this->namespace = $namespace . self::NAMESPACE_SEPARATOR;
	}

	function getStorage()
	{
		return $this->storage;
	}

	function getNamespace()
	{
		return (string) substr($this->namespace, 0, -1);
	}

	function derive($namespace)
	{
		$derived = new static($this->storage, $this->namespace . $namespace);
		return $derived;
	}

	function release()
	{
		$this->key = $this->data = NULL;
	}

	function load($key)
	{
		$key = is_scalar($key) ? (string) $key : serialize($key);
		if ($this->key === $key) {
			return $this->data;
		}
		$this->key = $key;
		$this->data = $this->storage->read($this->namespace . md5($key));
		return $this->data;
	}

	function save($key, $data, array $dp = NULL)
	{
		$this->key = is_scalar($key) ? (string) $key : serialize($key);
		$key = $this->namespace . md5($this->key);

		if (isset($dp[Cache::EXPIRATION])) {
			$dp[Cache::EXPIRATION] = Nette\DateTime::from($dp[Cache::EXPIRATION])->format('U') - time();
		}

		if (isset($dp[self::FILES])) {

			foreach ((array) $dp[self::FILES] as $item) {
				$dp[self::CALLBACKS][] = array(array(__CLASS__, 'checkFile'), $item, @filemtime($item));
			}
			unset($dp[self::FILES]);
		}

		if (isset($dp[self::ITEMS])) {
			$dp[self::ITEMS] = (array) $dp[self::ITEMS];
			foreach ($dp[self::ITEMS] as $k => $item) {
				$dp[self::ITEMS][$k] = $this->namespace . md5(is_scalar($item) ? $item : serialize($item));
			}
		}

		if (isset($dp[self::CONSTS])) {
			foreach ((array) $dp[self::CONSTS] as $item) {
				$dp[self::CALLBACKS][] = array(array(__CLASS__, 'checkConst'), $item, constant($item));
			}
			unset($dp[self::CONSTS]);
		}

		if ($data instanceof Nette\Callback || $data instanceof \Closure) {
			Nette\Utils\CriticalSection::enter();
			$data = $data->__invoke();
			Nette\Utils\CriticalSection::leave();
		}

		if (is_object($data)) {
			$dp[self::CALLBACKS][] = array(array(__CLASS__, 'checkSerializationVersion'), get_class($data),
				Nette\Reflection\ClassType::from($data)->getAnnotation('serializationVersion'));
		}

		$this->data = $data;
		if ($data === NULL) {
			$this->storage->remove($key);
		} else {
			$this->storage->write($key, $data, (array) $dp);
		}
		return $data;
	}

	function clean(array $conds = NULL)
	{
		$this->release();
		$this->storage->clean((array) $conds);
	}

	function call($function)
	{
		$key = func_get_args();
		if ($this->load($key) === NULL) {
			array_shift($key);
			return $this->save($this->key, call_user_func_array($function, $key));
		} else {
			return $this->data;
		}
	}

	function start($key)
	{
		if ($this->offsetGet($key) === NULL) {
			return new OutputHelper($this, $key);
		} else {
			echo $this->data;
		}
	}

	function offsetSet($key, $data)
	{
		$this->save($key, $data);
	}

	function offsetGet($key)
	{
		return $this->load($key);
	}

	function offsetExists($key)
	{
		return $this->load($key) !== NULL;
	}

	function offsetUnset($key)
	{
		$this->save($key, NULL);
	}

	static function checkCallbacks($callbacks)
	{
		foreach ($callbacks as $callback) {
			$func = array_shift($callback);
			if (!call_user_func_array($func, $callback)) {
				return FALSE;
			}
		}
		return TRUE;
	}

	private static function checkConst($const, $value)
	{
		return defined($const) && constant($const) === $value;
	}

	private static function checkFile($file, $time)
	{
		return @filemtime($file) == $time;
	}

	private static function checkSerializationVersion($class, $value)
	{
		return Nette\Reflection\ClassType::from($class)->getAnnotation('serializationVersion') === $value;
	}

}

class OutputHelper extends Nette\Object
{

	public $dependencies;

	private $cache;

	private $key;

	function __construct(Cache $cache, $key)
	{
		$this->cache = $cache;
		$this->key = $key;
		ob_start();
	}

	function end(array $dp = NULL)
	{
		if ($this->cache === NULL) {
			throw new Nette\InvalidStateException('Output cache has already been saved.');
		}
		$this->cache->save($this->key, ob_get_flush(), (array) $dp + (array) $this->dependencies);
		$this->cache = NULL;
	}

}

}

namespace Nette\Caching\Storages {

use Nette;

class DevNullStorage extends Nette\Object implements Nette\Caching\IStorage
{

	function read($key)
	{
	}

	function write($key, $data, array $dp)
	{
	}

	function remove($key)
	{
	}

	function clean(array $conds)
	{
	}

}

use Nette\Caching\Cache;

class FileJournal extends Nette\Object implements IJournal
{

	const FILE = 'btfj.dat';

	const FILE_MAGIC  = 0x6274666A;

	const INDEX_MAGIC = 0x696E6465;

	const DATA_MAGIC  = 0x64617461;

	const NODE_SIZE = 4096;

	const BITROT = 12;

	const HEADER_SIZE = 4096;

	const INT32_SIZE  = 4;

	const INFO = 'i',
		TYPE = 't',
		IS_LEAF = 'il',
		PREV_NODE = 'p',
		END = 'e',
		MAX = 'm',
		INDEX_DATA = 'id',
		LAST_INDEX = 'l';

	const TAGS = 't',
		PRIORITY = 'p',
		ENTRIES = 'e';

	const DATA = 'd',
		KEY = 'k',
		DELETED = 'd';

	public static $debug = FALSE;

	private $file;

	private $handle;

	private $lastNode = 2;

	private $lastModTime = NULL;

	private $nodeCache = array();

	private $nodeChanged = array();

	private $toCommit = array();

	private $deletedLinks = array();

	private $dataNodeFreeSpace = array();

	private static $startNode = array(
		self::TAGS     => 0,
		self::PRIORITY => 1,
		self::ENTRIES  => 2,
		self::DATA     => 3,
	);

	function __construct($dir)
	{
		$this->file = $dir . '/' . self::FILE;

		if (!file_exists($this->file)) {
			$init = @fopen($this->file, 'xb');
			if (!$init) {
				clearstatcache();
				if (!file_exists($this->file)) {
					throw new Nette\InvalidStateException("Cannot create journal file $this->file.");
				}
			} else {
				$writen = fwrite($init, pack('N2', self::FILE_MAGIC, $this->lastNode));
				fclose($init);
				if ($writen !== self::INT32_SIZE * 2) {
					throw new Nette\InvalidStateException("Cannot write journal header.");
				}
			}
		}

		$this->handle = fopen($this->file, 'r+b');

		if (!$this->handle) {
			throw new Nette\InvalidStateException("Cannot open journal file '$this->file'.");
		}

		if (!flock($this->handle, LOCK_SH)) {
			throw new Nette\InvalidStateException('Cannot acquire shared lock on journal.');
		}

		$header = stream_get_contents($this->handle, 2 * self::INT32_SIZE, 0);

		flock($this->handle, LOCK_UN);

		list(, $fileMagic, $this->lastNode) = unpack('N2', $header);

		if ($fileMagic !== self::FILE_MAGIC) {
			fclose($this->handle);
			$this->handle = false;
			throw new Nette\InvalidStateException("Malformed journal file '$this->file'.");
		}
	}

	function __destruct()
	{
		if ($this->handle) {
			$this->headerCommit();
			flock($this->handle, LOCK_UN);
			fclose($this->handle);
			$this->handle = false;
		}
	}

	function write($key, array $dependencies)
	{
		$this->lock();

		$priority = !isset($dependencies[Cache::PRIORITY]) ? FALSE : (int) $dependencies[Cache::PRIORITY];
		$tags = empty($dependencies[Cache::TAGS]) ? FALSE : (array) $dependencies[Cache::TAGS];

		$exists = FALSE;
		$keyHash = crc32($key);
		list($entriesNodeId, $entriesNode) = $this->findIndexNode(self::ENTRIES, $keyHash);

		if (isset($entriesNode[$keyHash])) {
			$entries = $this->mergeIndexData($entriesNode[$keyHash]);
			foreach ($entries as $link => $foo) {
				$dataNode = $this->getNode($link >> self::BITROT);
				if ($dataNode[$link][self::KEY] === $key) {
					if ($dataNode[$link][self::TAGS] == $tags && $dataNode[$link][self::PRIORITY] === $priority) {
						if ($dataNode[$link][self::DELETED]) {
							$dataNode[$link][self::DELETED] = FALSE;
							$this->saveNode($link >> self::BITROT, $dataNode);
						}
						$exists = TRUE;
					} else {
						$toDelete = array();
						foreach ($dataNode[$link][self::TAGS] as $tag) {
							$toDelete[self::TAGS][$tag][$link] = TRUE;
						}
						if ($dataNode[$link][self::PRIORITY] !== FALSE) {
							$toDelete[self::PRIORITY][$dataNode[$link][self::PRIORITY]][$link] = TRUE;
						}
						$toDelete[self::ENTRIES][$keyHash][$link] = TRUE;
						$this->cleanFromIndex($toDelete);
						$entriesNode = $this->getNode($entriesNodeId);
						unset($dataNode[$link]);
						$this->saveNode($link >> self::BITROT, $dataNode);
					}
					break;
				}
			}
		}

		if ($exists === FALSE) {

			$requiredSize = strlen($key) + 75;
			if ($tags) {
				foreach ($tags as $tag) {
					$requiredSize += strlen($tag) + 13;
				}
			}
			$requiredSize += $priority ? 10 : 1;

			$freeDataNode = $this->findFreeDataNode($requiredSize);
			$data = $this->getNode($freeDataNode);

			if ($data === FALSE) {
				$data = array(
					self::INFO => array(
						self::LAST_INDEX => ($freeDataNode << self::BITROT),
						self::TYPE => self::DATA,
					)
				);
			}

			$dataNodeKey = ++$data[self::INFO][self::LAST_INDEX];
			$data[$dataNodeKey] = array(
				self::KEY => $key,
				self::TAGS => $tags ? $tags : array(),
				self::PRIORITY => $priority,
				self::DELETED => FALSE,
			);

			$this->saveNode($freeDataNode, $data);

			$entriesNode[$keyHash][$dataNodeKey] = 1;
			$this->saveNode($entriesNodeId, $entriesNode);

			if ($tags) {
				foreach ($tags as $tag) {
					list($nodeId, $node) = $this->findIndexNode(self::TAGS, $tag);
					$node[$tag][$dataNodeKey] = 1;
					$this->saveNode($nodeId, $node);
				}
			}

			if ($priority) {
				list($nodeId, $node) = $this->findIndexNode(self::PRIORITY, $priority);
				$node[$priority][$dataNodeKey] = 1;
				$this->saveNode($nodeId, $node);
			}
		}

		$this->commit();
		$this->unlock();
	}

	function clean(array $conditions)
	{
		$this->lock();

		if (!empty($conditions[Cache::ALL])) {
			$this->nodeCache = $this->nodeChanged = $this->dataNodeFreeSpace = array();
			$this->deleteAll();
			$this->unlock();
			return;
		}

		$toDelete = array(
			self::TAGS => array(),
			self::PRIORITY => array(),
			self::ENTRIES => array()
		);

		$entries = array();

		if (!empty($conditions[Cache::TAGS])) {
			$entries = $this->cleanTags((array) $conditions[Cache::TAGS], $toDelete);
		}

		if (isset($conditions[Cache::PRIORITY])) {
			$this->arrayAppend($entries, $this->cleanPriority((int) $conditions[Cache::PRIORITY], $toDelete));
		}

		$this->deletedLinks = array();
		$this->cleanFromIndex($toDelete);

		$this->commit();
		$this->unlock();

		return $entries;
	}

	private function cleanTags(array $tags, array &$toDelete)
	{
		$entries = array();

		foreach ($tags as $tag) {
			list($nodeId, $node) = $this->findIndexNode(self::TAGS, $tag);

			if (isset($node[$tag])) {
				$ent = $this->cleanLinks($this->mergeIndexData($node[$tag]), $toDelete);
				$this->arrayAppend($entries, $ent);
			}
		}

		return $entries;
	}

	private function cleanPriority($priority, array &$toDelete)
	{
		list($nodeId, $node) = $this->findIndexNode(self::PRIORITY, $priority);

		ksort($node);

		$allData = array();

		foreach ($node as $prior => $data) {
			if ($prior === self::INFO) {
				continue;
			} elseif ($prior > $priority) {
				break;
			}

			$this->arrayAppendKeys($allData, $this->mergeIndexData($data));
		}

		$nodeInfo = $node[self::INFO];
		while ($nodeInfo[self::PREV_NODE] !== -1) {
			$nodeId = $nodeInfo[self::PREV_NODE];
			$node = $this->getNode($nodeId);

			if ($node === FALSE) {
				if (self::$debug) {
					throw new Nette\InvalidStateException("Cannot load node number $nodeId.");
				}
				break;
			}

			$nodeInfo = $node[self::INFO];
			unset($node[self::INFO]);

			foreach ($node as $prior => $data) {
				$this->arrayAppendKeys($allData, $this->mergeIndexData($data));
			}
		}

		return $this->cleanLinks($allData, $toDelete);
	}

	private function cleanLinks(array $data, array &$toDelete)
	{
		$return = array();

		$data = array_keys($data);
		sort($data);
		$max = count($data);
		$data[] = 0;
		$i = 0;

		while ($i < $max) {
			$searchLink = $data[$i];

			if (isset($this->deletedLinks[$searchLink])) {
				++$i;
				continue;
			}

			$nodeId = $searchLink >> self::BITROT;
			$node = $this->getNode($nodeId);

			if ($node === FALSE) {
				if (self::$debug) {
					throw new Nette\InvalidStateException('Cannot load node number ' . ($nodeId) . '.');
				}
				++$i;
				continue;
			}

			do {
				$link = $data[$i];

				if (!isset($node[$link])) {
					if (self::$debug) {
						throw new Nette\InvalidStateException("Link with ID $searchLink is not in node ". ($nodeId) . '.');
					}
					continue;
				} elseif (isset($this->deletedLinks[$link])) {
					continue;
				}

				$nodeLink = &$node[$link];
				if (!$nodeLink[self::DELETED]) {
					$nodeLink[self::DELETED] = TRUE;
					$return[] = $nodeLink[self::KEY];
				} else {
					foreach ($nodeLink[self::TAGS] as $tag) {
						$toDelete[self::TAGS][$tag][$link] = TRUE;
					}
					if ($nodeLink[self::PRIORITY] !== FALSE) {
						$toDelete[self::PRIORITY][$nodeLink[self::PRIORITY]][$link] = TRUE;
					}
					$toDelete[self::ENTRIES][crc32($nodeLink[self::KEY])][$link] = TRUE;
					unset($node[$link]);
					$this->deletedLinks[$link] = TRUE;
				}
			} while (($data[++$i] >> self::BITROT) === $nodeId);

			$this->saveNode($nodeId, $node);
		}

		return $return;
	}

	private function cleanFromIndex(array $toDeleteFromIndex)
	{
		foreach ($toDeleteFromIndex as $type => $toDelete) {
			ksort($toDelete);

			while (!empty($toDelete)) {
				reset($toDelete);
				$searchKey = key($toDelete);
				list($masterNodeId, $masterNode) = $this->findIndexNode($type, $searchKey);

				if (!isset($masterNode[$searchKey])) {
					if (self::$debug) {
						throw new Nette\InvalidStateException('Bad index.');
					}
					unset($toDelete[$searchKey]);
					continue;
				}

				foreach ($toDelete as $key => $links) {
					if (isset($masterNode[$key])) {
						foreach ($links as $link => $foo) {
							if (isset($masterNode[$key][$link])) {
								unset($masterNode[$key][$link], $links[$link]);
							}
						}

						if (!empty($links) && isset($masterNode[$key][self::INDEX_DATA])) {
							$this->cleanIndexData($masterNode[$key][self::INDEX_DATA], $links, $masterNode[$key]);
						}

						if (empty($masterNode[$key])) {
							unset($masterNode[$key]);
						}
						unset($toDelete[$key]);
					} else {
						break;
					}
				}
				$this->saveNode($masterNodeId, $masterNode);
			}
		}
	}

	private function mergeIndexData(array $data)
	{
		while (isset($data[self::INDEX_DATA])) {
			$id = $data[self::INDEX_DATA];
			unset($data[self::INDEX_DATA]);
			$childNode = $this->getNode($id);

			if ($childNode === FALSE) {
				if (self::$debug) {
					throw new Nette\InvalidStateException("Cannot load node number $id.");
				}
				break;
			}

			$this->arrayAppendKeys($data, $childNode[self::INDEX_DATA]);
		}

		return $data;
	}

	private function cleanIndexData($nextNodeId, array $links, &$masterNodeLink)
	{
		$prev = -1;

		while ($nextNodeId && !empty($links)) {
			$nodeId = $nextNodeId;
			$node = $this->getNode($nodeId);

			if ($node === FALSE) {
				if (self::$debug) {
					throw new Nette\InvalidStateException("Cannot load node number $nodeId.");
				}
				break;
			}

			foreach ($links as $link => $foo) {
				if (isset($node[self::INDEX_DATA][$link])) {
					unset($node[self::INDEX_DATA][$link], $links[$link]);
				}
			}

			if (isset($node[self::INDEX_DATA][self::INDEX_DATA])) {
				$nextNodeId = $node[self::INDEX_DATA][self::INDEX_DATA];
			} else {
				$nextNodeId = FALSE;
			}

			if (empty($node[self::INDEX_DATA]) || (count($node[self::INDEX_DATA]) === 1 && $nextNodeId)) {
				if ($prev === -1) {
					if ($nextNodeId === FALSE) {
						unset($masterNodeLink[self::INDEX_DATA]);
					} else {
						$masterNodeLink[self::INDEX_DATA] = $nextNodeId;
					}
				} else {
					$prevNode = $this->getNode($prev);
					if ($prevNode === FALSE) {
						if (self::$debug) {
							throw new Nette\InvalidStateException("Cannot load node number $prev.");
						}
					} else {
						if ($nextNodeId === FALSE) {
							unset($prevNode[self::INDEX_DATA][self::INDEX_DATA]);
							if (empty($prevNode[self::INDEX_DATA])) {
								unset($prevNode[self::INDEX_DATA]);
							}
						} else {
							$prevNode[self::INDEX_DATA][self::INDEX_DATA] = $nextNodeId;
						}

						$this->saveNode($prev, $prevNode);
					}
				}
				unset($node[self::INDEX_DATA]);
			} else {
				$prev = $nodeId;
			}

			$this->saveNode($nodeId, $node);
		}
	}

	private function getNode($id)
	{

		if (isset($this->nodeCache[$id])) {
			return $this->nodeCache[$id];
		}

		$binary = stream_get_contents($this->handle, self::NODE_SIZE, self::HEADER_SIZE + self::NODE_SIZE * $id);

		if (empty($binary)) {

			return FALSE;
		}

		list(, $magic, $lenght) = unpack('N2', $binary);
		if ($magic !== self::INDEX_MAGIC && $magic !== self::DATA_MAGIC) {
			if (!empty($magic)) {
				if (self::$debug) {
					throw new Nette\InvalidStateException("Node $id has malformed header.");
				}
				$this->deleteNode($id);
			}
			return FALSE;
		}

		$data = substr($binary, 2 * self::INT32_SIZE, $lenght - 2 * self::INT32_SIZE);

		$node = @unserialize($data);
		if ($node === FALSE) {
			$this->deleteNode($id);
			if (self::$debug) {
				throw new Nette\InvalidStateException("Cannot deserialize node number $id.");
			}
			return FALSE;
		}

		return $this->nodeCache[$id] = $node;
	}

	private function saveNode($id, array $node)
	{
		if (count($node) === 1) {
			$nodeInfo = $node[self::INFO];
			if ($nodeInfo[self::TYPE] !== self::DATA) {

				if ($nodeInfo[self::END] !== -1) {
					$this->nodeCache[$id] = $node;
					$this->nodeChanged[$id] = TRUE;
					return;
				}

				if ($nodeInfo[self::MAX] === -1) {
					$max = PHP_INT_MAX;
				} else {
					$max = $nodeInfo[self::MAX];
				}

				list(, , $parentId) = $this->findIndexNode($nodeInfo[self::TYPE], $max, $id);
				if ($parentId !== -1 && $parentId !== $id) {
					$parentNode = $this->getNode($parentId);
					if ($parentNode === FALSE) {
						if (self::$debug) {
							throw new Nette\InvalidStateException("Cannot load node number $parentId.");
						}
					} else {
						if ($parentNode[self::INFO][self::END] === $id) {
							if (count($parentNode) === 1) {
								$parentNode[self::INFO][self::END] = -1;
							} else {
								end($parentNode);
								$lastKey = key($parentNode);
								$parentNode[self::INFO][self::END] = $parentNode[$lastKey];
								unset($parentNode[$lastKey]);
							}
						} else {
							unset($parentNode[$nodeInfo[self::MAX]]);
						}

						$this->saveNode($parentId, $parentNode);
					}
				}

				if ($nodeInfo[self::TYPE] === self::PRIORITY) {
					if ($nodeInfo[self::MAX] === -1) {
						if ($nodeInfo[self::PREV_NODE] !== -1) {
							$prevNode = $this->getNode($nodeInfo[self::PREV_NODE]);
							if ($prevNode === FALSE) {
								if (self::$debug) {
									throw new Nette\InvalidStateException('Cannot load node number ' . $nodeInfo[self::PREV_NODE] . '.');
								}
							} else {
								$prevNode[self::INFO][self::MAX] = -1;
								$this->saveNode($nodeInfo[self::PREV_NODE], $prevNode);
							}
						}
					} else {
						list($nextId, $nextNode) = $this->findIndexNode($nodeInfo[self::TYPE], $nodeInfo[self::MAX] + 1, NULL, $id);
						if ($nextId !== -1 && $nextId !== $id) {
							$nextNode[self::INFO][self::PREV_NODE] = $nodeInfo[self::PREV_NODE];
							$this->saveNode($nextId, $nextNode);
						}
					}
				}
			}
			$this->nodeCache[$id] = FALSE;
		} else {
			$this->nodeCache[$id] = $node;
		}
		$this->nodeChanged[$id] = TRUE;
	}

	private function commit()
	{
		do {
			foreach ($this->nodeChanged as $id => $foo) {
				if ($this->prepareNode($id, $this->nodeCache[$id])) {
					unset($this->nodeChanged[$id]);
				}
			}
		} while (!empty($this->nodeChanged));

		foreach ($this->toCommit as $node => $str) {
			$this->commitNode($node, $str);
		}
		$this->toCommit = array();
	}

	private function prepareNode($id, $node)
	{
		if ($node === FALSE) {
			if ($id < $this->lastNode) {
				$this->lastNode = $id;
			}
			unset($this->nodeCache[$id]);
			unset($this->dataNodeFreeSpace[$id]);
			$this->deleteNode($id);
			return TRUE;
		}

		$data = serialize($node);
		$dataSize = strlen($data) + 2 * self::INT32_SIZE;

		$isData = $node[self::INFO][self::TYPE] === self::DATA;
		if ($dataSize > self::NODE_SIZE) {
			if ($isData) {
				throw new Nette\InvalidStateException('Saving node is bigger than maximum node size.');
			} else {
				$this->bisectNode($id, $node);
				return FALSE;
			}
		}

		$this->toCommit[$id] = pack('N2', $isData ? self::DATA_MAGIC : self::INDEX_MAGIC, $dataSize) . $data;

		if ($this->lastNode < $id) {
			$this->lastNode = $id;
		}
		if ($isData) {
			$this->dataNodeFreeSpace[$id] = self::NODE_SIZE - $dataSize;
		}

		return TRUE;
	}

	private function commitNode($id, $str)
	{
		fseek($this->handle, self::HEADER_SIZE + self::NODE_SIZE * $id);
		$writen = fwrite($this->handle, $str);
		if ($writen === FALSE) {
			throw new Nette\InvalidStateException("Cannot write node number $id to journal.");
		}
	}

	private function findIndexNode($type, $search, $childId = NULL, $prevId = NULL)
	{
		$nodeId = self::$startNode[$type];

		$parentId = -1;
		while (TRUE) {
			$node = $this->getNode($nodeId);

			if ($node === FALSE) {
				return array(
					$nodeId,
					array(
						self::INFO => array(
							self::TYPE => $type,
							self::IS_LEAF => TRUE,
							self::PREV_NODE => -1,
							self::END => -1,
							self::MAX => -1,
						)
					),
					$parentId,
				);
			}

			if ($node[self::INFO][self::IS_LEAF] || $nodeId === $childId || $node[self::INFO][self::PREV_NODE] === $prevId) {
				return array($nodeId, $node, $parentId);
			}

			$parentId = $nodeId;

			if (isset($node[$search])) {
				$nodeId = $node[$search];
			} else {
				foreach ($node as $key => $childNode) {
					if ($key > $search and $key !== self::INFO) {
						$nodeId = $childNode;
						continue 2;
					}
				}

				$nodeId = $node[self::INFO][self::END];
			}
		}
	}

	private function findFreeNode($count = 1)
	{
		$id = $this->lastNode;
		$nodesId = array();

		do {
			if (isset($this->nodeCache[$id])) {
				++$id;
				continue;
			}

			$offset = self::HEADER_SIZE + self::NODE_SIZE * $id;

			$binary = stream_get_contents($this->handle, self::INT32_SIZE, $offset);

			if (empty($binary)) {
				$nodesId[] = $id;
			} else {
				list(, $magic) = unpack('N', $binary);
				if ($magic !== self::INDEX_MAGIC && $magic !== self::DATA_MAGIC) {
					$nodesId[] = $id;
				}
			}

			++$id;
		} while (count($nodesId) !== $count);

		if ($count === 1) {
			return $nodesId[0];
		} else {
			return $nodesId;
		}
	}

	private function findFreeDataNode($size)
	{
		foreach ($this->dataNodeFreeSpace as $id => $freeSpace) {
			if ($freeSpace > $size) {
				return $id;
			}
		}

		$id = self::$startNode[self::DATA];
		while (TRUE) {
			if (isset($this->dataNodeFreeSpace[$id]) || isset($this->nodeCache[$id])) {
				++$id;
				continue;
			}

			$offset = self::HEADER_SIZE + self::NODE_SIZE * $id;
			$binary = stream_get_contents($this->handle, 2 * self::INT32_SIZE, $offset);

			if (empty($binary)) {
				$this->dataNodeFreeSpace[$id] = self::NODE_SIZE;
				return $id;
			}

			list(, $magic, $nodeSize) = unpack('N2', $binary);
			if (empty($magic)) {
				$this->dataNodeFreeSpace[$id] = self::NODE_SIZE;
				return $id;
			} elseif ($magic === self::DATA_MAGIC) {
				$freeSpace = self::NODE_SIZE - $nodeSize;
				$this->dataNodeFreeSpace[$id] = $freeSpace;

				if ($freeSpace > $size) {
					return $id;
				}
			}

			++$id;
		}
	}

	private function bisectNode($id, array $node)
	{
		$nodeInfo = $node[self::INFO];
		unset($node[self::INFO]);

		if (count($node) === 1) {
			$key = key($node);

			$dataId = $this->findFreeDataNode(self::NODE_SIZE);
			$this->saveNode($dataId, array(
				self::INDEX_DATA => $node[$key],
				self::INFO => array(
					self::TYPE => self::DATA,
					self::LAST_INDEX => ($dataId << self::BITROT),
			)));

			unset($node[$key]);
			$node[$key][self::INDEX_DATA] = $dataId;
			$node[self::INFO] = $nodeInfo;

			$this->saveNode($id, $node);
			return;
		}

		ksort($node);
		$halfCount = ceil(count($node) / 2);

		list($first, $second) = array_chunk($node, $halfCount, TRUE);

		end($first);
		$halfKey = key($first);

		if ($id <= 2) {
			list($firstId, $secondId) = $this->findFreeNode(2);

			$first[self::INFO] = array(
				self::TYPE => $nodeInfo[self::TYPE],
				self::IS_LEAF => $nodeInfo[self::IS_LEAF],
				self::PREV_NODE => -1,
				self::END => -1,
				self::MAX => $halfKey,
			);
			$this->saveNode($firstId, $first);

			$second[self::INFO] = array(
				self::TYPE => $nodeInfo[self::TYPE],
				self::IS_LEAF => $nodeInfo[self::IS_LEAF],
				self::PREV_NODE => $firstId,
				self::END => $nodeInfo[self::END],
				self::MAX => -1,
			);
			$this->saveNode($secondId, $second);

			$parentNode = array(
				self::INFO => array(
					self::TYPE => $nodeInfo[self::TYPE],
					self::IS_LEAF => FALSE,
					self::PREV_NODE => -1,
					self::END => $secondId,
					self::MAX => -1,
				),
				$halfKey => $firstId,
			);
			$this->saveNode($id, $parentNode);
		} else {
			$firstId = $this->findFreeNode();

			$first[self::INFO] = array(
				self::TYPE => $nodeInfo[self::TYPE],
				self::IS_LEAF => $nodeInfo[self::IS_LEAF],
				self::PREV_NODE => $nodeInfo[self::PREV_NODE],
				self::END => -1,
				self::MAX => $halfKey,
			);
			$this->saveNode($firstId, $first);

			$second[self::INFO] = array(
				self::TYPE => $nodeInfo[self::TYPE],
				self::IS_LEAF => $nodeInfo[self::IS_LEAF],
				self::PREV_NODE => $firstId,
				self::END => $nodeInfo[self::END],
				self::MAX => $nodeInfo[self::MAX],
			);
			$this->saveNode($id, $second);

			list(,, $parent) = $this->findIndexNode($nodeInfo[self::TYPE], $halfKey);
			$parentNode = $this->getNode($parent);
			if ($parentNode === FALSE) {
				if (self::$debug) {
					throw new Nette\InvalidStateException("Cannot load node number $parent.");
				}
			} else {
				$parentNode[$halfKey] = $firstId;
				ksort($parentNode);
				$this->saveNode($parent, $parentNode);
			}
		}
	}

	private function headerCommit()
	{
		fseek($this->handle, self::INT32_SIZE);
		@fwrite($this->handle, pack('N', $this->lastNode));
	}

	private function deleteNode($id)
	{
		fseek($this->handle, 0, SEEK_END);
		$end = ftell($this->handle);

		if ($end <= (self::HEADER_SIZE + self::NODE_SIZE * ($id + 1))) {
			$packedNull = pack('N', 0);

			do {
				$binary = stream_get_contents($this->handle, self::INT32_SIZE, (self::HEADER_SIZE + self::NODE_SIZE * --$id));
			} while (empty($binary) || $binary === $packedNull);

			if (!ftruncate($this->handle, self::HEADER_SIZE + self::NODE_SIZE * ($id + 1))) {
				throw new Nette\InvalidStateException('Cannot truncate journal file.');
			}
		} else {
			fseek($this->handle, self::HEADER_SIZE + self::NODE_SIZE * $id);
			$writen = fwrite($this->handle, pack('N', 0));
			if ($writen !== self::INT32_SIZE) {
				throw new Nette\InvalidStateException("Cannot delete node number $id from journal.");
			}
		}
	}

	private function deleteAll()
	{
		if (!ftruncate($this->handle, self::HEADER_SIZE)) {
			throw new Nette\InvalidStateException('Cannot truncate journal file.');
		}
	}

	private function lock()
	{
		if (!$this->handle) {
			throw new Nette\InvalidStateException('File journal file is not opened');
		}

		if (!flock($this->handle, LOCK_EX)) {
			throw new Nette\InvalidStateException('Cannot acquire exclusive lock on journal.');
		}

		if ($this->lastModTime !== NULL) {
			clearstatcache();
			if ($this->lastModTime < @filemtime($this->file)) {
				$this->nodeCache = $this->dataNodeFreeSpace = array();
			}
		}
	}

	private function unlock()
	{
		if ($this->handle) {
			fflush($this->handle);
			flock($this->handle, LOCK_UN);
			clearstatcache();
			$this->lastModTime = @filemtime($this->file);
		}
	}

	private function arrayAppend(array &$array, array $append)
	{
		foreach ($append as $value) {
			$array[] = $value;
		}
	}

	private function arrayAppendKeys(array &$array, array $append)
	{
		foreach ($append as $key => $value) {
			$array[$key] = $value;
		}
	}

}

class FileStorage extends Nette\Object implements Nette\Caching\IStorage
{

	const META_HEADER_LEN = 28,

		META_TIME = 'time',
		META_SERIALIZED = 'serialized',
		META_EXPIRE = 'expire',
		META_DELTA = 'delta',
		META_ITEMS = 'di',
		META_CALLBACKS = 'callbacks';

	const FILE = 'file',
		HANDLE = 'handle';

	public static $gcProbability = 0.001;

	public static $useDirectories = TRUE;

	private $dir;

	private $useDirs;

	private $journal;

	function __construct($dir, IJournal $journal = NULL)
	{
		$this->dir = realpath($dir);
		if ($this->dir === FALSE) {
			throw new Nette\DirectoryNotFoundException("Directory '$dir' not found.");
		}

		$this->useDirs = (bool) self::$useDirectories;
		$this->journal = $journal;

		if (mt_rand() / mt_getrandmax() < self::$gcProbability) {
			$this->clean(array());
		}
	}

	function read($key)
	{
		$meta = $this->readMetaAndLock($this->getCacheFile($key), LOCK_SH);
		if ($meta && $this->verify($meta)) {
			return $this->readData($meta);

		} else {
			return NULL;
		}
	}

	private function verify($meta)
	{
		do {
			if (!empty($meta[self::META_DELTA])) {

				if (filemtime($meta[self::FILE]) + $meta[self::META_DELTA] < time()) {
					break;
				}
				touch($meta[self::FILE]);

			} elseif (!empty($meta[self::META_EXPIRE]) && $meta[self::META_EXPIRE] < time()) {
				break;
			}

			if (!empty($meta[self::META_CALLBACKS]) && !Cache::checkCallbacks($meta[self::META_CALLBACKS])) {
				break;
			}

			if (!empty($meta[self::META_ITEMS])) {
				foreach ($meta[self::META_ITEMS] as $depFile => $time) {
					$m = $this->readMetaAndLock($depFile, LOCK_SH);
					if ($m[self::META_TIME] !== $time || ($m && !$this->verify($m))) {
						break 2;
					}
				}
			}

			return TRUE;
		} while (FALSE);

		$this->delete($meta[self::FILE], $meta[self::HANDLE]);
		return FALSE;
	}

	function write($key, $data, array $dp)
	{
		$meta = array(
			self::META_TIME => microtime(),
		);

		if (isset($dp[Cache::EXPIRATION])) {
			if (empty($dp[Cache::SLIDING])) {
				$meta[self::META_EXPIRE] = $dp[Cache::EXPIRATION] + time();
			} else {
				$meta[self::META_DELTA] = (int) $dp[Cache::EXPIRATION];
			}
		}

		if (isset($dp[Cache::ITEMS])) {
			foreach ((array) $dp[Cache::ITEMS] as $item) {
				$depFile = $this->getCacheFile($item);
				$m = $this->readMetaAndLock($depFile, LOCK_SH);
				$meta[self::META_ITEMS][$depFile] = $m[self::META_TIME];
				unset($m);
			}
		}

		if (isset($dp[Cache::CALLBACKS])) {
			$meta[self::META_CALLBACKS] = $dp[Cache::CALLBACKS];
		}

		$cacheFile = $this->getCacheFile($key);
		if ($this->useDirs && !is_dir($dir = dirname($cacheFile))) {
			umask(0000);
			if (!mkdir($dir, 0777)) {
				return;
			}
		}
		$handle = @fopen($cacheFile, 'r+b');
		if (!$handle) {
			$handle = fopen($cacheFile, 'wb');
			if (!$handle) {
				return;
			}
		}

		if (isset($dp[Cache::TAGS]) || isset($dp[Cache::PRIORITY])) {
			if (!$this->journal) {
				throw new Nette\InvalidStateException('CacheJournal has not been provided.');
			}
			$this->journal->write($cacheFile, $dp);
		}

		flock($handle, LOCK_EX);
		ftruncate($handle, 0);

		if (!is_string($data)) {
			$data = serialize($data);
			$meta[self::META_SERIALIZED] = TRUE;
		}

		$head = serialize($meta) . '?>';
		$head = '<?php //netteCache[01]' . str_pad((string) strlen($head), 6, '0', STR_PAD_LEFT) . $head;
		$headLen = strlen($head);
		$dataLen = strlen($data);

		do {
			if (fwrite($handle, str_repeat("\x00", $headLen), $headLen) !== $headLen) {
				break;
			}

			if (fwrite($handle, $data, $dataLen) !== $dataLen) {
				break;
			}

			fseek($handle, 0);
			if (fwrite($handle, $head, $headLen) !== $headLen) {
				break;
			}

			flock($handle, LOCK_UN);
			fclose($handle);
			return TRUE;
		} while (FALSE);

		$this->delete($cacheFile, $handle);
	}

	function remove($key)
	{
		$this->delete($this->getCacheFile($key));
	}

	function clean(array $conds)
	{
		$all = !empty($conds[Cache::ALL]);
		$collector = empty($conds);

		if ($all || $collector) {
			$now = time();
			foreach (Nette\Utils\Finder::find('*')->from($this->dir)->childFirst() as $entry) {
				$path = (string) $entry;
				if ($entry->isDir()) {
					@rmdir($path);
					continue;
				}
				if ($all) {
					$this->delete($path);

				} else {
					$meta = $this->readMetaAndLock($path, LOCK_SH);
					if (!$meta) {
						continue;
					}

					if ((!empty($meta[self::META_DELTA]) && filemtime($meta[self::FILE]) + $meta[self::META_DELTA] < $now)
						|| (!empty($meta[self::META_EXPIRE]) && $meta[self::META_EXPIRE] < $now)
					) {
						$this->delete($path, $meta[self::HANDLE]);
						continue;
					}

					flock($meta[self::HANDLE], LOCK_UN);
					fclose($meta[self::HANDLE]);
				}
			}

			if ($this->journal) {
				$this->journal->clean($conds);
			}
			return;
		}

		if ($this->journal) {
			foreach ($this->journal->clean($conds) as $file) {
				$this->delete($file);
			}
		}
	}

	protected function readMetaAndLock($file, $lock)
	{
		$handle = @fopen($file, 'r+b');
		if (!$handle) {
			return NULL;
		}

		flock($handle, $lock);

		$head = stream_get_contents($handle, self::META_HEADER_LEN);
		if ($head && strlen($head) === self::META_HEADER_LEN) {
			$size = (int) substr($head, -6);
			$meta = stream_get_contents($handle, $size, self::META_HEADER_LEN);
			$meta = @unserialize($meta);
			if (is_array($meta)) {
				fseek($handle, $size + self::META_HEADER_LEN);
				$meta[self::FILE] = $file;
				$meta[self::HANDLE] = $handle;
				return $meta;
			}
		}

		flock($handle, LOCK_UN);
		fclose($handle);
		return NULL;
	}

	protected function readData($meta)
	{
		$data = stream_get_contents($meta[self::HANDLE]);
		flock($meta[self::HANDLE], LOCK_UN);
		fclose($meta[self::HANDLE]);

		if (empty($meta[self::META_SERIALIZED])) {
			return $data;
		} else {
			return @unserialize($data);
		}
	}

	protected function getCacheFile($key)
	{
		$file = urlencode($key);
		if ($this->useDirs && $a = strrpos($file, '%00')) {
			$file = substr_replace($file, '/_', $a, 3);
		}
		return $this->dir . '/_' . $file;
	}

	private static function delete($file, $handle = NULL)
	{
		if (@unlink($file)) {
			if ($handle) {
				flock($handle, LOCK_UN);
				fclose($handle);
			}
			return;
		}

		if (!$handle) {
			$handle = @fopen($file, 'r+');
		}
		if ($handle) {
			flock($handle, LOCK_EX);
			ftruncate($handle, 0);
			flock($handle, LOCK_UN);
			fclose($handle);
			@unlink($file);
		}
	}

}

class MemcachedStorage extends Nette\Object implements Nette\Caching\IStorage
{

	const META_CALLBACKS = 'callbacks',
		META_DATA = 'data',
		META_DELTA = 'delta';

	private $memcache;

	private $prefix;

	private $journal;

	static function isAvailable()
	{
		return extension_loaded('memcache');
	}

	function __construct($host = 'localhost', $port = 11211, $prefix = '', IJournal $journal = NULL)
	{
		if (!self::isAvailable()) {
			throw new Nette\NotSupportedException("PHP extension 'memcache' is not loaded.");
		}

		$this->prefix = $prefix;
		$this->journal = $journal;
		$this->memcache = new \Memcache;
		Nette\Diagnostics\Debugger::tryError();
		$this->memcache->connect($host, $port);
		if (Nette\Diagnostics\Debugger::catchError($e)) {
			throw new Nette\InvalidStateException('Memcache::connect(): ' . $e->getMessage(), 0, $e);
		}
	}

	function read($key)
	{
		$key = $this->prefix . $key;
		$meta = $this->memcache->get($key);
		if (!$meta) {
			return NULL;
		}

		if (!empty($meta[self::META_CALLBACKS]) && !Cache::checkCallbacks($meta[self::META_CALLBACKS])) {
			$this->memcache->delete($key, 0);
			return NULL;
		}

		if (!empty($meta[self::META_DELTA])) {
			$this->memcache->replace($key, $meta, 0, $meta[self::META_DELTA] + time());
		}

		return $meta[self::META_DATA];
	}

	function write($key, $data, array $dp)
	{
		if (isset($dp[Cache::ITEMS])) {
			throw new Nette\NotSupportedException('Dependent items are not supported by MemcachedStorage.');
		}

		$key = $this->prefix . $key;
		$meta = array(
			self::META_DATA => $data,
		);

		$expire = 0;
		if (isset($dp[Cache::EXPIRATION])) {
			$expire = (int) $dp[Cache::EXPIRATION];
			if (!empty($dp[Cache::SLIDING])) {
				$meta[self::META_DELTA] = $expire;
			}
		}

		if (isset($dp[Cache::CALLBACKS])) {
			$meta[self::META_CALLBACKS] = $dp[Cache::CALLBACKS];
		}

		if (isset($dp[Cache::TAGS]) || isset($dp[Cache::PRIORITY])) {
			if (!$this->journal) {
				throw new Nette\InvalidStateException('CacheJournal has not been provided.');
			}
			$this->journal->write($key, $dp);
		}

		$this->memcache->set($key, $meta, 0, $expire);
	}

	function remove($key)
	{
		$this->memcache->delete($this->prefix . $key, 0);
	}

	function clean(array $conds)
	{
		if (!empty($conds[Cache::ALL])) {
			$this->memcache->flush();

		} elseif ($this->journal) {
			foreach ($this->journal->clean($conds) as $entry) {
				$this->memcache->delete($entry, 0);
			}
		}
	}

}

class MemoryStorage extends Nette\Object implements Nette\Caching\IStorage
{

	private $data = array();

	function read($key)
	{
		return isset($this->data[$key]) ? $this->data[$key] : NULL;
	}

	function write($key, $data, array $dp)
	{
		$this->data[$key] = $data;
	}

	function remove($key)
	{
		unset($this->data[$key]);
	}

	function clean(array $conds)
	{
		if (!empty($conds[Nette\Caching\Cache::ALL])) {
			$this->data = array();
		}
	}

}

class PhpFileStorage extends FileStorage
{

	public $hint;

	protected function readData($meta)
	{
		return array(
			'file' => $meta[self::FILE],
			'handle' => $meta[self::HANDLE],
		);
	}

	protected function getCacheFile($key)
	{
		return parent::getCacheFile(substr_replace(
			$key,
			trim(strtr($this->hint, '\\/@', '.._'), '.') . '-',
			strpos($key, Nette\Caching\Cache::NAMESPACE_SEPARATOR) + 1,
			0
		)) . '.php';
	}

}

}

namespace Nette {

use Nette;

class ArrayHash extends \stdClass implements \ArrayAccess, \Countable, \IteratorAggregate
{

	static function from($arr, $recursive = TRUE)
	{
		$obj = new static;
		foreach ($arr as $key => $value) {
			if ($recursive && is_array($value)) {
				$obj->$key = static::from($value, TRUE);
			} else {
				$obj->$key = $value;
			}
		}
		return $obj;
	}

	function getIterator()
	{
		return new \RecursiveArrayIterator($this);
	}

	function count()
	{
		return count((array) $this);
	}

	function offsetSet($key, $value)
	{
		if (!is_scalar($key)) {
			throw new InvalidArgumentException("Key must be either a string or an integer, " . gettype($key) ." given.");
		}
		$this->$key = $value;
	}

	function offsetGet($key)
	{
		return $this->$key;
	}

	function offsetExists($key)
	{
		return isset($this->$key);
	}

	function offsetUnset($key)
	{
		unset($this->$key);
	}

}

final class Callback extends Object
{

	private $cb;

	function __construct($t, $m = NULL)
	{
		if ($m === NULL) {
			if (is_string($t)) {
				$t = explode('::', $t, 2);
				$this->cb = isset($t[1]) ? $t : $t[0];
			} elseif (is_object($t)) {
				$this->cb = $t instanceof \Closure ? $t : array($t, '__invoke');
			} else {
				$this->cb = $t;
			}

		} else {
			$this->cb = array($t, $m);
		}

		if (!is_callable($this->cb, TRUE)) {
			throw new InvalidArgumentException("Invalid callback.");
		}
	}

	function __invoke()
	{
		if (!is_callable($this->cb)) {
			throw new InvalidStateException("Callback '$this' is not callable.");
		}
		$args = func_get_args();
		return call_user_func_array($this->cb, $args);
	}

	function invoke()
	{
		if (!is_callable($this->cb)) {
			throw new InvalidStateException("Callback '$this' is not callable.");
		}
		$args = func_get_args();
		return call_user_func_array($this->cb, $args);
	}

	function invokeArgs(array $args)
	{
		if (!is_callable($this->cb)) {
			throw new InvalidStateException("Callback '$this' is not callable.");
		}
		return call_user_func_array($this->cb, $args);
	}

	function invokeNamedArgs(array $args)
	{
		$ref = $this->toReflection();
		if (is_array($this->cb)) {
			return $ref->invokeNamedArgs(is_object($this->cb[0]) ? $this->cb[0] : NULL, $args);
		} else {
			return $ref->invokeNamedArgs($args);
		}
	}

	function isCallable()
	{
		return is_callable($this->cb);
	}

	function getNative()
	{
		return $this->cb;
	}

	function toReflection()
	{
		if (is_array($this->cb)) {
			return new Nette\Reflection\Method($this->cb[0], $this->cb[1]);
		} else {
			return new Nette\Reflection\GlobalFunction($this->cb);
		}
	}

	function isStatic()
	{
		return is_array($this->cb) ? is_string($this->cb[0]) : is_string($this->cb);
	}

	function __toString()
	{
		if ($this->cb instanceof \Closure) {
			return '{closure}';
		} elseif (is_string($this->cb) && $this->cb[0] === "\0") {
			return '{lambda}';
		} else {
			is_callable($this->cb, TRUE, $textual);
			return $textual;
		}
	}

}

use Nette\Caching\Cache;use Nette\DI;

class Configurator extends Object
{
	public static $instance;

	public $defaultConfigFile = '%appDir%/config.neon';

	private $container;

	function __construct($containerClass = 'Nette\DI\Container')
	{
		self::$instance = $this;
		$this->container = new $containerClass;
		$this->container->addService('container', $this->container);

		foreach (get_class_methods($this) as $name) {
			if (substr($name, 0, 13) === 'createService' ) {
				$this->container->addService(strtolower($name[13]) . substr($name, 14), array(get_called_class(), $name));
			}
		}

		defined('WWW_DIR') && $this->container->params['wwwDir'] = realpath(WWW_DIR);
		defined('APP_DIR') && $this->container->params['appDir'] = realpath(APP_DIR);
		defined('LIBS_DIR') && $this->container->params['libsDir'] = realpath(LIBS_DIR);
		defined('TEMP_DIR') && $this->container->params['tempDir'] = realpath(TEMP_DIR);
		$this->container->params['productionMode'] = self::detectProductionMode();
		$this->container->params['consoleMode'] = PHP_SAPI === 'cli';
	}

	function getContainer()
	{
		return $this->container;
	}

	function loadConfig($file, $section = NULL)
	{
		if ($file === NULL) {
			$file = $this->defaultConfigFile;
		}
		$container = $this->container;
		$file = $container->expand($file);
		if (!is_file($file)) {
			$file = preg_replace('#\.neon$#', '.ini', $file);
		}
		if ($section === NULL) {
			if (PHP_SAPI === 'cli') {
				$section = Environment::CONSOLE;
			} else {
				$section = $container->params['productionMode'] ? Environment::PRODUCTION : Environment::DEVELOPMENT;
			}
		}

		$cache = new Cache($container->templateCacheStorage, 'Nette.Configurator');
		$cacheKey = array((array) $container->params, $file, $section);
		$cached = $cache->load($cacheKey);
		if ($cached) {
			 require $cached['file'];
			fclose($cached['handle']);
			return $this->container;
		}

		$config = Nette\Config\Config::fromFile($file, $section);
		$code = "<?php\n// source file $file\n\n";

		foreach (array('service', 'variable') as $item) {
			if (isset($config[$item])) {
				trigger_error(basename($file) . ": Section '$item' is deprecated; use plural form '{$item}s' instead.", E_USER_WARNING);
				$config[$item . 's'] = $config[$item];
				unset($config[$item]);
			}
		}

		if (isset($config['services'])) {
			foreach ($config['services'] as $key => & $def) {
				if (preg_match('#^Nette\\\\.*\\\\I?([a-zA-Z]+)$#', strtr($key, '-', '\\'), $m)) {
					$m[1][0] = strtolower($m[1][0]);
					trigger_error(basename($file) . ": service name '$key' has been renamed to '$m[1]'", E_USER_WARNING);
					$key = $m[1];
				}

				if (is_array($def)) {
					if (method_exists(get_called_class(), "createService$key") && !isset($def['factory']) && !isset($def['class'])) {
						$def['factory'] = array(get_called_class(), "createService$key");
					}

					if (isset($def['option'])) {
						$def['arguments'][] = $def['option'];
					}

					if (!empty($def['run'])) {
						$def['tags'] = array('run');
					}
				}
			}
			$builder = new DI\ContainerBuilder;
			$code .= $builder->generateCode($config['services']);
			unset($config['services']);
		}

		if (!isset($config['variables'])) {
			$config['variables'] = array();
		}
		foreach ($config as $key => $value) {
			if (!in_array($key, array('variables', 'services', 'php', 'const', 'mode'))) {
				$config['variables'][$key] = $value;
			}
		}

		$variables = $config['variables'];
		array_walk_recursive($config, function(&$val) use($variables) {
			$val = Configurator::preExpand($val, $variables);
		});

		foreach ($config['variables'] as $key => $value) {
			$code .= $this->generateCode('$container->params[?] = ?', $key, $value);
		}

		if (isset($config['php'])) {
			foreach ($config['php'] as $key => $value) {
				if (is_array($value)) {
					foreach ($value as $k => $v) {
						$code .= $this->configurePhp("$key.$k", $v);
					}
				} else {
					$code .= $this->configurePhp($key, $value);
				}
			}
		}

		if (isset($config['const'])) {
			foreach ($config['const'] as $key => $value) {
				$code .= $this->generateCode('define', $key, $value);
			}
		}

		if (isset($config['mode'])) {
			foreach ($config['mode'] as $mode => $state) {
				trigger_error(basename($file) . ": Section 'mode' is deprecated; use '{$mode}Mode' in section 'variables' instead.", E_USER_WARNING);
				$code .= $this->generateCode('$container->params[?] = ?', $mode . 'Mode', (bool) $state);
			}
		}

		$code .= self::preloadEnvironment($container);

		$code .= 'foreach ($container->getServiceNamesByTag("run") as $name => $foo) { $container->getService($name); }' . "\n";

		$cache->save($cacheKey, $code, array(
			Cache::FILES => $file,
		));

		Nette\Utils\LimitedScope::evaluate($code, array('container' => $container));
		return $this->container;
	}

	static function detectProductionMode()
	{
		$addrs = array();
		if (PHP_SAPI === 'cli') {
			$addrs[] = getHostByName(php_uname('n'));
		}
		else {
			if (!isset($_SERVER['SERVER_ADDR']) && !isset($_SERVER['LOCAL_ADDR'])) {
				return TRUE;
			}
			if (isset($_SERVER['HTTP_X_FORWARDED_FOR'])) {
				$addrs = preg_split('#,\s*#', $_SERVER['HTTP_X_FORWARDED_FOR']);
			}
			if (isset($_SERVER['REMOTE_ADDR'])) {
				$addrs[] = $_SERVER['REMOTE_ADDR'];
			}
			$addrs[] = isset($_SERVER['SERVER_ADDR']) ? $_SERVER['SERVER_ADDR'] : $_SERVER['LOCAL_ADDR'];
		}
		foreach ($addrs as $addr) {
			$oct = explode('.', $addr);

			if ($addr !== '::1' && (count($oct) !== 4 || ($oct[0] !== '10' && $oct[0] !== '127' && ($oct[0] !== '172' || $oct[1] < 16 || $oct[1] > 31)
				&& ($oct[0] !== '169' || $oct[1] !== '254') && ($oct[0] !== '192' || $oct[1] !== '168')))
			) {
				return TRUE;
			}
		}
		return FALSE;
	}

	function configurePhp($name, $value)
	{
		if (!is_scalar($value)) {
			throw new Nette\InvalidStateException("Configuration value for directive '$name' is not scalar.");
		}

		switch ($name) {
		case 'include_path':
			return $this->generateCode('set_include_path', str_replace(';', PATH_SEPARATOR, $value));
		case 'ignore_user_abort':
			return $this->generateCode('ignore_user_abort', $value);
		case 'max_execution_time':
			return $this->generateCode('set_time_limit', $value);
		case 'date.timezone':
			return $this->generateCode('date_default_timezone_set', $value);
		}

		if (function_exists('ini_set')) {
			return $this->generateCode('ini_set', $name, $value);
		} elseif (ini_get($name) != $value && !Framework::$iAmUsingBadHost) {
			throw new Nette\NotSupportedException('Required function ini_set() is disabled.');
		}
	}

	private static function generateCode($statement)
	{
		$args = func_get_args();
		unset($args[0]);
		foreach ($args as &$arg) {
			$arg = var_export($arg, TRUE);
			$arg = preg_replace("#(?<!\\\)'%([\w-]+)%'#", '\$container->params[\'$1\']', $arg);
			$arg = preg_replace("#(?<!\\\)'(?:[^'\\\]|\\\.)*%(?:[^'\\\]|\\\.)*'#", '\$container->expand($0)', $arg);
		}
		if (strpos($statement, '?') === FALSE) {
			return $statement .= '(' . implode(', ', $args) . ");\n\n";
		}
		$a = strpos($statement, '?');
		$i = 1;
		while ($a !== FALSE) {
			$statement = substr_replace($statement, $args[$i], $a, 1);
			$a = strpos($statement, '?', $a + strlen($args[$i]));
			$i++;
		}
		return $statement . ";\n\n";
	}

	static function preExpand($s, array $params, $check = array())
	{
		if (!is_string($s)) {
			return $s;
		}

		$parts = preg_split('#%([\w.-]*)%#i', $s, -1, PREG_SPLIT_DELIM_CAPTURE);
		$res = '';
		foreach ($parts as $n => $part) {
			if ($n % 2 === 0) {
				$res .= str_replace('%', '%%', $part);

			} elseif ($part === '') {
				$res .= '%%';

			} elseif (isset($check[$part])) {
				throw new Nette\InvalidArgumentException('Circular reference detected for variables: ' . implode(', ', array_keys($check)) . '.');

			} else {
				try {
					$val = Nette\Utils\Arrays::get($params, explode('.', $part));
				} catch (Nette\InvalidArgumentException $e) {
					$res .= "%$part%";
					continue;
				}
				$val = self::preExpand($val, $params, $check + array($part => 1));
				if (strlen($part) + 2 === strlen($s)) {
					if (is_array($val)) {
						array_walk_recursive($val, function(&$val) use($params, $check, $part) {
							$val = Configurator::preExpand($val, $params, $check + array($part => 1));
						});
					}
					return $val;
				}
				if (!is_scalar($val)) {
					throw new Nette\InvalidArgumentException("Unable to concatenate non-scalar parameter '$part' into '$s'.");
				}
				$res .= $val;
			}
		}
		return $res;
	}

	static function createServiceApplication(DI\Container $container, array $options = NULL)
	{
		$context = new DI\Container;
		$context->addService('httpRequest', $container->httpRequest);
		$context->addService('httpResponse', $container->httpResponse);
		$context->addService('session', $container->session);
		$context->addService('presenterFactory', $container->presenterFactory);
		$context->addService('router', $container->router);

		Nette\Application\UI\Presenter::$invalidLinkMode = $container->params['productionMode']
			? Nette\Application\UI\Presenter::INVALID_LINK_SILENT
			: Nette\Application\UI\Presenter::INVALID_LINK_WARNING;

		$class = isset($options['class']) ? $options['class'] : 'Nette\Application\Application';
		$application = new $class($context);
		$application->catchExceptions = $container->params['productionMode'];
		if ($container->session->exists()) {
		$application->onStartup[] = function() use($container) {
				$container->session->start();
			};
		}
		return $application;
	}

	static function createServicePresenterFactory(DI\Container $container)
	{
		return new Nette\Application\PresenterFactory(
			isset($container->params['appDir']) ? $container->params['appDir'] : NULL,
			$container
		);
	}

	static function createServiceRouter(DI\Container $container)
	{
		return new Nette\Application\Routers\RouteList;
	}

	static function createServiceHttpRequest()
	{
		$factory = new Nette\Http\RequestFactory;
		$factory->setEncoding('UTF-8');
		return $factory->createHttpRequest();
	}

	static function createServiceHttpResponse()
	{
		return new Nette\Http\Response;
	}

	static function createServiceHttpContext(DI\Container $container)
	{
		return new Nette\Http\Context($container->httpRequest, $container->httpResponse);
	}

	static function createServiceSession(DI\Container $container, array $options = NULL)
	{
		$session = new Nette\Http\Session($container->httpRequest, $container->httpResponse);
		$session->setOptions((array) $options);
		if (isset($options['expiration'])) {
			$session->setExpiration($options['expiration']);
		}
		return $session;
	}

	static function createServiceUser(DI\Container $container)
	{
		$context = new DI\Container;

		$context->addService('authenticator', function() use($container) {
			return $container->authenticator;
		});
		$context->addService('authorizator', function() use($container) {
			return $container->authorizator;
		});
		$context->addService('session', $container->session);
		return new Nette\Http\User($context);
	}

	static function createServiceCacheStorage(DI\Container $container)
	{
		if (!isset($container->params['tempDir'])) {
			throw new Nette\InvalidStateException("Service cacheStorage requires that parameter 'tempDir' contains path to temporary directory.");
		}
		$dir = $container->expand('%tempDir%/cache');
		umask(0000);
		@mkdir($dir, 0777);
		return new Nette\Caching\Storages\FileStorage($dir, $container->cacheJournal);
	}

	static function createServiceTemplateCacheStorage(DI\Container $container)
	{
		if (!isset($container->params['tempDir'])) {
			throw new Nette\InvalidStateException("Service templateCacheStorage requires that parameter 'tempDir' contains path to temporary directory.");
		}
		$dir = $container->expand('%tempDir%/cache');
		umask(0000);
		@mkdir($dir, 0777);
		return new Nette\Caching\Storages\PhpFileStorage($dir);
	}

	static function createServiceCacheJournal(DI\Container $container)
	{
		return new Nette\Caching\Storages\FileJournal($container->params['tempDir']);
	}

	static function createServiceMailer(DI\Container $container, array $options = NULL)
	{
		if (empty($options['smtp'])) {
			return new Nette\Mail\SendmailMailer;
		} else {
			return new Nette\Mail\SmtpMailer($options);
		}
	}

	static function createServiceRobotLoader(DI\Container $container, array $options = NULL)
	{
		$loader = new Nette\Loaders\RobotLoader;
		$loader->autoRebuild = isset($options['autoRebuild']) ? $options['autoRebuild'] : !$container->params['productionMode'];
		$loader->setCacheStorage($container->cacheStorage);
		if (isset($options['directory'])) {
			$loader->addDirectory($options['directory']);
		} else {
			foreach (array('appDir', 'libsDir') as $var) {
				if (isset($container->params[$var])) {
					$loader->addDirectory($container->params[$var]);
				}
			}
		}
		$loader->register();
		return $loader;
	}

	static function preloadEnvironment(DI\Container $container)
	{
		$code = '';
		$dir = $container->expand('%tempDir%/cache');
		umask(0000);
		@mkdir($dir, 0777);

		$uniq = uniqid('_', TRUE);
		umask(0000);
		if (!@mkdir("$dir/$uniq", 0777)) {
			throw new Nette\InvalidStateException("Unable to write to directory '$dir'. Make this directory writable.");
		}

		$useDirs = @file_put_contents("$dir/$uniq/_", '') !== FALSE;
		@unlink("$dir/$uniq/_");
		@rmdir("$dir/$uniq");

		$code .= self::generateCode('Nette\Caching\Storages\FileStorage::$useDirectories = ?', $useDirs);
		return $code;
	}

}

class DateTime extends \DateTime
{

	const MINUTE = 60;

	const HOUR = 3600;

	const DAY = 86400;

	const WEEK = 604800;

	const MONTH = 2629800;

	const YEAR = 31557600;

	static function from($time)
	{
		if ($time instanceof \DateTime) {
			return clone $time;

		} elseif (is_numeric($time)) {
			if ($time <= self::YEAR) {
				$time += time();
			}
			return new static(date('Y-m-d H:i:s', $time));

		} else {
			return new static($time);
		}
	}

	}

final class Environment
{

	const DEVELOPMENT = 'development',
		PRODUCTION = 'production',
		CONSOLE = 'console';

	private static $configurator;

	private static $context;

	final function __construct()
	{
		throw new StaticClassException;
	}

	static function setConfigurator(Configurator $configurator)
	{
		self::$configurator = $configurator;
	}

	static function getConfigurator()
	{
		if (self::$configurator === NULL) {
			self::$configurator = Configurator::$instance ?: new Configurator;
		}
		return self::$configurator;
	}

	static function isConsole()
	{
		return self::getContext()->params['consoleMode'];
	}

	static function isProduction()
	{
		return self::getContext()->params['productionMode'];
	}

	static function setProductionMode($value = TRUE)
	{
		self::getContext()->params['productionMode'] = (bool) $value;
	}

	static function setVariable($name, $value, $expand = TRUE)
	{
		if ($expand && is_string($value)) {
			$value = self::getContext()->expand($value);
		}
		self::getContext()->params[$name] = $value;
	}

	static function getVariable($name, $default = NULL)
	{
		if (isset(self::getContext()->params[$name])) {
			return self::getContext()->params[$name];
		} elseif (func_num_args() > 1) {
			return $default;
		} else {
			throw new InvalidStateException("Unknown environment variable '$name'.");
		}
	}

	static function getVariables()
	{
		return self::getContext()->params;
	}

	static function expand($s)
	{
		return self::getContext()->expand($s);
	}

	static function setContext(DI\IContainer $context)
	{
		self::$context = $context;
	}

	static function getContext()
	{
		if (self::$context === NULL) {
			self::$context = self::getConfigurator()->getContainer();
		}
		return self::$context;
	}

	static function getService($name)
	{
		return self::getContext()->getService($name);
	}

	static function __callStatic($name, $args)
	{
		if (!$args && strncasecmp($name, 'get', 3) === 0) {
			return self::getContext()->getService(lcfirst(substr($name, 3)));
		} else {
			throw new MemberAccessException("Call to undefined static method Nette\\Environment::$name().");
		}
	}

	static function getHttpRequest()
	{
		return self::getContext()->httpRequest;
	}

	static function getHttpContext()
	{
		return self::getContext()->httpContext;
	}

	static function getHttpResponse()
	{
		return self::getContext()->httpResponse;
	}

	static function getApplication()
	{
		return self::getContext()->application;
	}

	static function getUser()
	{
		return self::getContext()->user;
	}

	static function getRobotLoader()
	{
		return self::getContext()->robotLoader;
	}

	static function getCache($namespace = '')
	{
		return new Caching\Cache(self::getContext()->cacheStorage, $namespace);
	}

	static function getSession($namespace = NULL)
	{
		return $namespace === NULL
			? self::getContext()->session
			: self::getContext()->session->getSection($namespace);
	}

	static function loadConfig($file = NULL, $section = NULL)
	{
		self::getConfigurator()->loadConfig($file, $section);
		return self::getConfig();
	}

	static function getConfig($key = NULL, $default = NULL)
	{
		$params = Nette\ArrayHash::from(self::getContext()->params);
		if (func_num_args()) {
			return isset($params[$key]) ? $params[$key] : $default;
		} else {
			return $params;
		}
	}

}

final class Framework
{

	const NAME = 'Nette Framework',
		VERSION = '2.0-beta',
		REVISION = 'd2c39f5 released on 2011-07-02';

	public static $iAmUsingBadHost = FALSE;

	final function __construct()
	{
		throw new StaticClassException;
	}

}

class Image extends Object
{

	const ENLARGE = 1;

	const STRETCH = 2;

	const FIT = 0;

	const FILL = 4;

	const JPEG = IMAGETYPE_JPEG,
		PNG = IMAGETYPE_PNG,
		GIF = IMAGETYPE_GIF;

	const EMPTY_GIF = "GIF89a\x01\x00\x01\x00\x80\x00\x00\x00\x00\x00\x00\x00\x00!\xf9\x04\x01\x00\x00\x00\x00,\x00\x00\x00\x00\x01\x00\x01\x00\x00\x02\x02D\x01\x00;";

	private $image;

	static function rgb($red, $green, $blue, $transparency = 0)
	{
		return array(
			'red' => max(0, min(255, (int) $red)),
			'green' => max(0, min(255, (int) $green)),
			'blue' => max(0, min(255, (int) $blue)),
			'alpha' => max(0, min(127, (int) $transparency)),
		);
	}

	static function fromFile($file, & $format = NULL)
	{
		if (!extension_loaded('gd')) {
			throw new NotSupportedException("PHP extension GD is not loaded.");
		}

		$info = @getimagesize($file);

		switch ($format = $info[2]) {
		case self::JPEG:
			return new static(imagecreatefromjpeg($file));

		case self::PNG:
			return new static(imagecreatefrompng($file));

		case self::GIF:
			return new static(imagecreatefromgif($file));

		default:
			throw new UnknownImageFileException("Unknown image type or file '$file' not found.");
		}
	}

	static function getFormatFromString($s)
	{
		$types = array('image/jpeg' => self::JPEG, 'image/gif' => self::GIF, 'image/png' => self::PNG);
		$type = Utils\MimeTypeDetector::fromString($s);
		return isset($types[$type]) ? $types[$type] : NULL;
	}

	static function fromString($s, & $format = NULL)
	{
		if (!extension_loaded('gd')) {
			throw new NotSupportedException("PHP extension GD is not loaded.");
		}

		$format = static::getFormatFromString($s);

		return new static(imagecreatefromstring($s));
	}

	static function fromBlank($width, $height, $color = NULL)
	{
		if (!extension_loaded('gd')) {
			throw new NotSupportedException("PHP extension GD is not loaded.");
		}

		$width = (int) $width;
		$height = (int) $height;
		if ($width < 1 || $height < 1) {
			throw new InvalidArgumentException('Image width and height must be greater than zero.');
		}

		$image = imagecreatetruecolor($width, $height);
		if (is_array($color)) {
			$color += array('alpha' => 0);
			$color = imagecolorallocatealpha($image, $color['red'], $color['green'], $color['blue'], $color['alpha']);
			imagealphablending($image, FALSE);
			imagefilledrectangle($image, 0, 0, $width - 1, $height - 1, $color);
			imagealphablending($image, TRUE);
		}
		return new static($image);
	}

	function __construct($image)
	{
		$this->setImageResource($image);
		imagesavealpha($image, TRUE);
	}

	function getWidth()
	{
		return imagesx($this->image);
	}

	function getHeight()
	{
		return imagesy($this->image);
	}

	protected function setImageResource($image)
	{
		if (!is_resource($image) || get_resource_type($image) !== 'gd') {
			throw new InvalidArgumentException('Image is not valid.');
		}
		$this->image = $image;
		return $this;
	}

	function getImageResource()
	{
		return $this->image;
	}

	function resize($width, $height, $flags = self::FIT)
	{
		list($newWidth, $newHeight) = self::calculateSize($this->getWidth(), $this->getHeight(), $width, $height, $flags);

		if ($newWidth !== $this->getWidth() || $newHeight !== $this->getHeight()) {
			$newImage = self::fromBlank($newWidth, $newHeight, self::RGB(0, 0, 0, 127))->getImageResource();
			imagecopyresampled(
				$newImage, $this->getImageResource(),
				0, 0, 0, 0,
				$newWidth, $newHeight, $this->getWidth(), $this->getHeight()
			);
			$this->image = $newImage;
		}

		if ($width < 0 || $height < 0) {
			$newImage = self::fromBlank($newWidth, $newHeight, self::RGB(0, 0, 0, 127))->getImageResource();
			imagecopyresampled(
				$newImage, $this->getImageResource(),
				0, 0, $width < 0 ? $newWidth - 1 : 0, $height < 0 ? $newHeight - 1 : 0,
				$newWidth, $newHeight, $width < 0 ? -$newWidth : $newWidth, $height < 0 ? -$newHeight : $newHeight
			);
			$this->image = $newImage;
		}
		return $this;
	}

	static function calculateSize($srcWidth, $srcHeight, $newWidth, $newHeight, $flags = self::FIT)
	{
		if (substr($newWidth, -1) === '%') {
			$newWidth = round($srcWidth / 100 * abs($newWidth));
			$flags |= self::ENLARGE;
			$percents = TRUE;
		} else {
			$newWidth = (int) abs($newWidth);
		}

		if (substr($newHeight, -1) === '%') {
			$newHeight = round($srcHeight / 100 * abs($newHeight));
			$flags |= empty($percents) ? self::ENLARGE : self::STRETCH;
		} else {
			$newHeight = (int) abs($newHeight);
		}

		if ($flags & self::STRETCH) {
			if (empty($newWidth) || empty($newHeight)) {
				throw new InvalidArgumentException('For stretching must be both width and height specified.');
			}

			if (($flags & self::ENLARGE) === 0) {
				$newWidth = round($srcWidth * min(1, $newWidth / $srcWidth));
				$newHeight = round($srcHeight * min(1, $newHeight / $srcHeight));
			}

		} else {
			if (empty($newWidth) && empty($newHeight)) {
				throw new InvalidArgumentException('At least width or height must be specified.');
			}

			$scale = array();
			if ($newWidth > 0) {
				$scale[] = $newWidth / $srcWidth;
			}

			if ($newHeight > 0) {
				$scale[] = $newHeight / $srcHeight;
			}

			if ($flags & self::FILL) {
				$scale = array(max($scale));
			}

			if (($flags & self::ENLARGE) === 0) {
				$scale[] = 1;
			}

			$scale = min($scale);
			$newWidth = round($srcWidth * $scale);
			$newHeight = round($srcHeight * $scale);
		}

		return array(max((int) $newWidth, 1), max((int) $newHeight, 1));
	}

	function crop($left, $top, $width, $height)
	{
		list($left, $top, $width, $height) = self::calculateCutout($this->getWidth(), $this->getHeight(), $left, $top, $width, $height);
		$newImage = self::fromBlank($width, $height, self::RGB(0, 0, 0, 127))->getImageResource();
		imagecopy($newImage, $this->getImageResource(), 0, 0, $left, $top, $width, $height);
		$this->image = $newImage;
		return $this;
	}

	static function calculateCutout($srcWidth, $srcHeight, $left, $top, $newWidth, $newHeight)
	{
		if (substr($newWidth, -1) === '%') {
			$newWidth = round($srcWidth / 100 * $newWidth);
		}
		if (substr($newHeight, -1) === '%') {
			$newHeight = round($srcHeight / 100 * $newHeight);
		}
		if (substr($left, -1) === '%') {
			$left = round(($srcWidth - $newWidth) / 100 * $left);
		}
		if (substr($top, -1) === '%') {
			$top = round(($srcHeight - $newHeight) / 100 * $top);
		}
		if ($left < 0) {
			$newWidth += $left; $left = 0;
		}
		if ($top < 0) {
			$newHeight += $top; $top = 0;
		}
		$newWidth = min((int) $newWidth, $srcWidth - $left);
		$newHeight = min((int) $newHeight, $srcHeight - $top);
		return array($left, $top, $newWidth, $newHeight);
	}

	function sharpen()
	{
		imageconvolution($this->getImageResource(), array(
			array( -1, -1, -1 ),
			array( -1, 24, -1 ),
			array( -1, -1, -1 ),
		), 16, 0);
		return $this;
	}

	function place(Image $image, $left = 0, $top = 0, $opacity = 100)
	{
		$opacity = max(0, min(100, (int) $opacity));

		if (substr($left, -1) === '%') {
			$left = round(($this->getWidth() - $image->getWidth()) / 100 * $left);
		}

		if (substr($top, -1) === '%') {
			$top = round(($this->getHeight() - $image->getHeight()) / 100 * $top);
		}

		if ($opacity === 100) {
			imagecopy(
				$this->getImageResource(), $image->getImageResource(),
				$left, $top, 0, 0, $image->getWidth(), $image->getHeight()
			);

		} elseif ($opacity <> 0) {
			imagecopymerge(
				$this->getImageResource(), $image->getImageResource(),
				$left, $top, 0, 0, $image->getWidth(), $image->getHeight(),
				$opacity
			);
		}
		return $this;
	}

	function save($file = NULL, $quality = NULL, $type = NULL)
	{
		if ($type === NULL) {
			switch (strtolower(pathinfo($file, PATHINFO_EXTENSION))) {
			case 'jpg':
			case 'jpeg':
				$type = self::JPEG;
				break;
			case 'png':
				$type = self::PNG;
				break;
			case 'gif':
				$type = self::GIF;
			}
		}

		switch ($type) {
		case self::JPEG:
			$quality = $quality === NULL ? 85 : max(0, min(100, (int) $quality));
			return imagejpeg($this->getImageResource(), $file, $quality);

		case self::PNG:
			$quality = $quality === NULL ? 9 : max(0, min(9, (int) $quality));
			return imagepng($this->getImageResource(), $file, $quality);

		case self::GIF:
			return $file === NULL ? imagegif($this->getImageResource()) : imagegif($this->getImageResource(), $file);

		default:
			throw new InvalidArgumentException("Unsupported image type.");
		}
	}

	function toString($type = self::JPEG, $quality = NULL)
	{
		ob_start();
		$this->save(NULL, $quality, $type);
		return ob_get_clean();
	}

	function __toString()
	{
		try {
			return $this->toString();

		} catch (\Exception $e) {
			Diagnostics\Debugger::toStringException($e);
		}
	}

	function send($type = self::JPEG, $quality = NULL)
	{
		if ($type !== self::GIF && $type !== self::PNG && $type !== self::JPEG) {
			throw new InvalidArgumentException("Unsupported image type.");
		}
		header('Content-Type: ' . image_type_to_mime_type($type));
		return $this->save(NULL, $quality, $type);
	}

	function __call($name, $args)
	{
		$function = 'image' . $name;
		if (function_exists($function)) {
			foreach ($args as $key => $value) {
				if ($value instanceof self) {
					$args[$key] = $value->getImageResource();

				} elseif (is_array($value) && isset($value['red'])) {
					$args[$key] = imagecolorallocatealpha(
						$this->getImageResource(),
						$value['red'], $value['green'], $value['blue'], $value['alpha']
					);
				}
			}
			array_unshift($args, $this->getImageResource());

			$res = call_user_func_array($function, $args);
			return is_resource($res) && get_resource_type($res) === 'gd' ? $this->setImageResource($res) : $res;
		}

		return parent::__call($name, $args);
	}

}

class UnknownImageFileException extends \Exception
{
}

final class ObjectMixin
{

	private static $methods;

	final function __construct()
	{
		throw new StaticClassException;
	}

	static function call($_this, $name, $args)
	{
		$class = new Reflection\ClassType($_this);

		if ($name === '') {
			throw new MemberAccessException("Call to class '$class->name' method without name.");
		}

		if ($class->hasEventProperty($name)) {
			if (is_array($list = $_this->$name) || $list instanceof \Traversable) {
				foreach ($list as $handler) {
					callback($handler)->invokeArgs($args);
				}
			}
			return NULL;
		}

		if ($cb = $class->getExtensionMethod($name)) {
			array_unshift($args, $_this);
			return $cb->invokeArgs($args);
		}

		throw new MemberAccessException("Call to undefined method $class->name::$name().");
	}

	static function callStatic($class, $name, $args)
	{
		throw new MemberAccessException("Call to undefined static method $class::$name().");
	}

	static function & get($_this, $name)
	{
		$class = get_class($_this);

		if ($name === '') {
			throw new MemberAccessException("Cannot read a class '$class' property without name.");
		}

		if (!isset(self::$methods[$class])) {

			self::$methods[$class] = array_flip(get_class_methods($class));
		}

		$name[0] = $name[0] & "\xDF";
		$m = 'get' . $name;
		if (isset(self::$methods[$class][$m])) {

			$val = $_this->$m();
			return $val;
		}

		$m = 'is' . $name;
		if (isset(self::$methods[$class][$m])) {
			$val = $_this->$m();
			return $val;
		}

		$type = isset(self::$methods[$class]['set' . $name]) ? 'a write-only' : 'an undeclared';
		$name = func_get_arg(1);
		throw new MemberAccessException("Cannot read $type property $class::\$$name.");
	}

	static function set($_this, $name, $value)
	{
		$class = get_class($_this);

		if ($name === '') {
			throw new MemberAccessException("Cannot write to a class '$class' property without name.");
		}

		if (!isset(self::$methods[$class])) {
			self::$methods[$class] = array_flip(get_class_methods($class));
		}

		$name[0] = $name[0] & "\xDF";

		$m = 'set' . $name;
		if (isset(self::$methods[$class][$m])) {
			$_this->$m($value);
			return;
		}

		$type = isset(self::$methods[$class]['get' . $name]) || isset(self::$methods[$class]['is' . $name])
			? 'a read-only' : 'an undeclared';
		$name = func_get_arg(1);
		throw new MemberAccessException("Cannot write to $type property $class::\$$name.");
	}

	static function remove($_this, $name)
	{
		$class = get_class($_this);
		throw new MemberAccessException("Cannot unset the property $class::\$$name.");
	}

	static function has($_this, $name)
	{
		if ($name === '') {
			return FALSE;
		}

		$class = get_class($_this);
		if (!isset(self::$methods[$class])) {
			self::$methods[$class] = array_flip(get_class_methods($class));
		}

		$name[0] = $name[0] & "\xDF";
		return isset(self::$methods[$class]['get' . $name]) || isset(self::$methods[$class]['is' . $name]);
	}

}

}

namespace Nette\ComponentModel {

use Nette;

class RecursiveComponentIterator extends \RecursiveArrayIterator implements \Countable
{

	function hasChildren()
	{
		return $this->current() instanceof IContainer;
	}

	function getChildren()
	{
		return $this->current()->getComponents();
	}

	function count()
	{
		return iterator_count($this);
	}

}

}

namespace Nette\Config {

use Nette;

class Config
{

	private static $extensions = array(
		'ini' => 'Nette\Config\IniAdapter',
		'neon' => 'Nette\Config\NeonAdapter',
	);

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function registerExtension($extension, $class)
	{
		if (!class_exists($class)) {
			throw new Nette\InvalidArgumentException("Class '$class' was not found.");
		}

		if (!Nette\Reflection\ClassType::from($class)->implementsInterface('Nette\Config\IAdapter')) {
			throw new Nette\InvalidArgumentException("Configuration adapter '$class' is not Nette\\Config\\IAdapter implementor.");
		}

		self::$extensions[strtolower($extension)] = $class;
	}

	static function fromFile($file, $section = NULL)
	{
		$extension = strtolower(pathinfo($file, PATHINFO_EXTENSION));
		if (!isset(self::$extensions[$extension])) {
			throw new Nette\InvalidArgumentException("Unknown file extension '$file'.");
		}

		$data = call_user_func(array(self::$extensions[$extension], 'load'), $file, $section);
		if ($section) {
			if (!isset($data[$section]) || !is_array($data[$section])) {
				throw new Nette\InvalidStateException("There is not section [$section] in file '$file'.");
			}
			$data = $data[$section];
		}
		return $data;
	}

	static function save($config, $file)
	{
		$extension = strtolower(pathinfo($file, PATHINFO_EXTENSION));
		if (!isset(self::$extensions[$extension])) {
			throw new Nette\InvalidArgumentException("Unknown file extension '$file'.");
		}
		return call_user_func(array(self::$extensions[$extension], 'save'), $config, $file);
	}

}

final class IniAdapter implements IAdapter
{

	public static $keySeparator = '.';

	public static $sectionSeparator = ' < ';

	public static $rawSection = '!';

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function load($file)
	{
		if (!is_file($file) || !is_readable($file)) {
			throw new Nette\FileNotFoundException("File '$file' is missing or is not readable.");
		}

		Nette\Diagnostics\Debugger::tryError();
		$ini = parse_ini_file($file, TRUE);
		if (Nette\Diagnostics\Debugger::catchError($e)) {
			throw new Nette\InvalidStateException('parse_ini_file(): ' . $e->getMessage(), 0, $e);
		}

		$separator = trim(self::$sectionSeparator);
		$data = array();
		foreach ($ini as $secName => $secData) {

			if (is_array($secData)) {
				if (substr($secName, -1) === self::$rawSection) {
					$secName = substr($secName, 0, -1);

				} elseif (self::$keySeparator) {

					$tmp = array();
					foreach ($secData as $key => $val) {
						$cursor = & $tmp;
						foreach (explode(self::$keySeparator, $key) as $part) {
							if (!isset($cursor[$part]) || is_array($cursor[$part])) {
								$cursor = & $cursor[$part];
							} else {
								throw new Nette\InvalidStateException("Invalid key '$key' in section [$secName] in file '$file'.");
							}
						}
						$cursor = $val;
					}
					$secData = $tmp;
				}

				$parts = $separator ? explode($separator, strtr($secName, ':', $separator)) : array($secName);
				if (count($parts) > 1) {
					$parent = trim($parts[1]);
					if (!isset($data[$parent]) || !is_array($data[$parent])) {
						throw new Nette\InvalidStateException("Missing parent section [$parent] in file '$file'.");
					}
					$secData = array_reverse(Nette\Utils\Arrays::mergeTree(array_reverse($secData, TRUE), array_reverse($data[$parent], TRUE)), TRUE);
					$secName = trim($parts[0]);
					if ($secName === '') {
						throw new Nette\InvalidStateException("Invalid empty section name in file '$file'.");
					}
				}
			}

			if (self::$keySeparator) {
				$cursor = & $data;
				foreach (explode(self::$keySeparator, $secName) as $part) {
					if (!isset($cursor[$part]) || is_array($cursor[$part])) {
						$cursor = & $cursor[$part];
					} else {
						throw new Nette\InvalidStateException("Invalid section [$secName] in file '$file'.");
					}
				}
			} else {
				$cursor = & $data[$secName];
			}

			if (is_array($secData) && is_array($cursor)) {
				$secData = Nette\Utils\Arrays::mergeTree($secData, $cursor);
			}

			$cursor = $secData;
		}

		return $data;
	}

	static function save($config, $file)
	{
		$output = array();
		$output[] = '; generated by Nette';
		$output[] = '';

		foreach ($config as $secName => $secData) {
			if (!(is_array($secData) || $secData instanceof \Traversable)) {
				throw new Nette\InvalidStateException("Invalid section '$secName'.");
			}

			$output[] = "[$secName]";
			self::build($secData, $output, '');
			$output[] = '';
		}

		if (!file_put_contents($file, implode(PHP_EOL, $output))) {
			throw new Nette\IOException("Cannot write file '$file'.");
		}
	}

	private static function build($input, & $output, $prefix)
	{
		foreach ($input as $key => $val) {
			if (is_array($val) || $val instanceof \Traversable) {
				self::build($val, $output, $prefix . $key . self::$keySeparator);

			} elseif (is_bool($val)) {
				$output[] = "$prefix$key = " . ($val ? 'true' : 'false');

			} elseif (is_numeric($val)) {
				$output[] = "$prefix$key = $val";

			} elseif (is_string($val)) {
				$output[] = "$prefix$key = \"$val\"";

			} else {
				throw new Nette\InvalidArgumentException("The '$prefix$key' item must be scalar or array, " . gettype($val) ." given.");
			}
		}
	}

}

use Nette\Utils\Neon;

final class NeonAdapter implements IAdapter
{

	public static $sectionSeparator = ' < ';

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function load($file)
	{
		if (!is_file($file) || !is_readable($file)) {
			throw new Nette\FileNotFoundException("File '$file' is missing or is not readable.");
		}

		$neon = Neon::decode(file_get_contents($file));

		$separator = trim(self::$sectionSeparator);
		$data = array();
		foreach ($neon as $secName => $secData) {
			if ($secData === NULL) {
				$secData = array();
			}

			if (is_array($secData)) {

				$parts = $separator ? explode($separator, $secName) : array($secName);
				if (count($parts) > 1) {
					$parent = trim($parts[1]);
					if (!isset($data[$parent]) || !is_array($data[$parent])) {
						throw new Nette\InvalidStateException("Missing parent section '$parent' in file '$file'.");
					}
					$secData = array_reverse(Nette\Utils\Arrays::mergeTree(array_reverse($secData, TRUE), array_reverse($data[$parent], TRUE)), TRUE);
					$secName = trim($parts[0]);
					if ($secName === '') {
						throw new Nette\InvalidStateException("Invalid empty section name in file '$file'.");
					}
				}
			}

			$data[$secName] = $secData;
		}

		return $data;
	}

	static function save($config, $file)
	{
		if (!file_put_contents($file, "# generated by Nette\n\n" . Neon::encode($config, Neon::BLOCK))) {
			throw new Nette\IOException("Cannot write file '$file'.");
		}
	}

}

}

namespace Nette\Database {

use Nette;use Nette\ObjectMixin;use PDO;

if (class_exists('PDO')){ class Connection extends PDO
{

	private $dsn;

	private $driver;

	private $preprocessor;

	public $databaseReflection;

	private $cache;

	public $substitutions = array();

	public $onQuery;

	function __construct($dsn, $username = NULL, $password  = NULL, array $options = NULL, Reflection\DatabaseReflection $databaseReflection = NULL)
	{
		parent::__construct($this->dsn = $dsn, $username, $password, $options);
		$this->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		$this->setAttribute(PDO::ATTR_STATEMENT_CLASS, array('Nette\Database\Statement', array($this)));

		$class = 'Nette\Database\Drivers\\' . $this->getAttribute(PDO::ATTR_DRIVER_NAME) . 'Driver';
		if (class_exists($class)) {
			$this->driver = new $class($this, (array) $options);
		}

		$this->preprocessor = new SqlPreprocessor($this);
		$this->databaseReflection = $databaseReflection ?: new Reflection\DatabaseReflection;

		Diagnostics\ConnectionPanel::initialize($this);
	}

	function getSupplementalDriver()
	{
		return $this->driver;
	}

	function setCacheStorage(Nette\Caching\IStorage $storage = NULL)
	{
		$this->cache = $storage ? new Nette\Caching\Cache($storage, "Nette.Database/$this->dsn") : NULL;
	}

	function getCache()
	{
		return $this->cache;
	}

	function query($statement)
	{
		$args = func_get_args();
		return $this->queryArgs(array_shift($args), $args);
	}

	function exec($statement)
	{
		$args = func_get_args();
		return $this->queryArgs(array_shift($args), $args)->rowCount();
	}

	function queryArgs($statement, $params)
	{
		foreach ($params as $value) {
			if (is_array($value) || is_object($value)) {
				$need = TRUE; break;
			}
		}
		if (isset($need) || strpos($statement, ':') !== FALSE && $this->preprocessor !== NULL) {
			list($statement, $params) = $this->preprocessor->process($statement, $params);
		}

		return $this->prepare($statement)->execute($params);
	}

	function fetch($args)
	{
		$args = func_get_args();
		return $this->queryArgs(array_shift($args), $args)->fetch();
	}

	function fetchColumn($args)
	{
		$args = func_get_args();
		return $this->queryArgs(array_shift($args), $args)->fetchColumn();
	}

	function fetchPairs($args)
	{
		$args = func_get_args();
		return $this->queryArgs(array_shift($args), $args)->fetchPairs();
	}

	function fetchAll($args)
	{
		$args = func_get_args();
		return $this->queryArgs(array_shift($args), $args)->fetchAll();
	}

	function table($table)
	{
		return new Table\Selection($table, $this);
	}

	function loadFile($file)
	{
		@set_time_limit(0);

		$handle = @fopen($file, 'r');
		if (!$handle) {
			throw new Nette\FileNotFoundException("Cannot open file '$file'.");
		}

		$count = 0;
		$sql = '';
		while (!feof($handle)) {
			$s = fgets($handle);
			$sql .= $s;
			if (substr(rtrim($s), -1) === ';') {
				parent::exec($sql);
				$sql = '';
				$count++;
			}
		}
		fclose($handle);
		return $count;
	}

	static function highlightSql($sql)
	{
		static $keywords1 = 'SELECT|UPDATE|INSERT(?:\s+INTO)?|REPLACE(?:\s+INTO)?|DELETE|FROM|WHERE|HAVING|GROUP\s+BY|ORDER\s+BY|LIMIT|OFFSET|SET|VALUES|LEFT\s+JOIN|INNER\s+JOIN|TRUNCATE';
		static $keywords2 = 'ALL|DISTINCT|DISTINCTROW|AS|USING|ON|AND|OR|IN|IS|NOT|NULL|LIKE|TRUE|FALSE';

		$sql = " $sql ";
		$sql = preg_replace("#(?<=[\\s,(])($keywords1)(?=[\\s,)])#i", "\n\$1", $sql);

		$sql = preg_replace('#[ \t]{2,}#', " ", $sql);

		$sql = wordwrap($sql, 100);
		$sql = preg_replace("#([ \t]*\r?\n){2,}#", "\n", $sql);

		$sql = htmlSpecialChars($sql);
		$sql = preg_replace_callback("#(/\\*.+?\\*/)|(\\*\\*.+?\\*\\*)|(?<=[\\s,(])($keywords1)(?=[\\s,)])|(?<=[\\s,(=])($keywords2)(?=[\\s,)=])#is", function($matches) {
			if (!empty($matches[1]))
				return '<em style="color:gray">' . $matches[1] . '</em>';

			if (!empty($matches[2]))
				return '<strong style="color:red">' . $matches[2] . '</strong>';

			if (!empty($matches[3]))
				return '<strong style="color:blue">' . $matches[3] . '</strong>';

			if (!empty($matches[4]))
				return '<strong style="color:green">' . $matches[4] . '</strong>';
		}, $sql);

		return '<pre class="dump">' . trim($sql) . "</pre>\n";
	}

	static function getReflection()
	{
		return new Nette\Reflection\ClassType(get_called_class());
	}

	function __call($name, $args)
	{
		return ObjectMixin::call($this, $name, $args);
	}

	function &__get($name)
	{
		return ObjectMixin::get($this, $name);
	}

	function __set($name, $value)
	{
		return ObjectMixin::set($this, $name, $value);
	}

	function __isset($name)
	{
		return ObjectMixin::has($this, $name);
	}

	function __unset($name)
	{
		ObjectMixin::remove($this, $name);
	}

}
 }

}

namespace Nette\Database\Diagnostics {

use Nette;use Nette\Database\Connection;use Nette\Diagnostics\Debugger;

class ConnectionPanel extends Nette\Object implements Nette\Diagnostics\IBarPanel
{

	static public $maxLength = 1000;

	public $totalTime = 0;

	public $queries = array();

	public $name;

	public $explain = TRUE;

	public $disabled = FALSE;

	static function initialize(Connection $connection)
	{
		$panel = new static;
		Debugger::$blueScreen->addPanel(array($panel, 'renderException'), __CLASS__);
		if (!Debugger::$productionMode) {
			$connection->onQuery[] = callback($panel, 'logQuery');
			Debugger::$bar->addPanel($panel);
		}
	}

	function logQuery(Nette\Database\Statement $result, array $params = NULL)
	{
		if ($this->disabled) {
			return;
		}
		$source = NULL;
		foreach (debug_backtrace(FALSE) as $row) {
			if (isset($row['file']) && is_file($row['file']) && strpos($row['file'], NETTE_DIR . DIRECTORY_SEPARATOR) !== 0) {
				$source = array($row['file'], (int) $row['line']);
				break;
			}
		}
		$this->totalTime += $result->time;
		$this->queries[] = array($result->queryString, $params, $result->time, $result->rowCount(), $result->getConnection(), $source);
	}

	function renderException($e)
	{
		if ($e instanceof \PDOException && isset($e->queryString)) {
			return array(
				'tab' => 'SQL',
				'panel' => Connection::highlightSql($e->queryString),
			);
		}
	}

	function getTab()
	{
		return '<span title="Nette\\Database ' . htmlSpecialChars($this->name) . '">'
			. '<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAEYSURBVBgZBcHPio5hGAfg6/2+R980k6wmJgsJ5U/ZOAqbSc2GnXOwUg7BESgLUeIQ1GSjLFnMwsKGGg1qxJRmPM97/1zXFAAAAEADdlfZzr26miup2svnelq7d2aYgt3rebl585wN6+K3I1/9fJe7O/uIePP2SypJkiRJ0vMhr55FLCA3zgIAOK9uQ4MS361ZOSX+OrTvkgINSjS/HIvhjxNNFGgQsbSmabohKDNoUGLohsls6BaiQIMSs2FYmnXdUsygQYmumy3Nhi6igwalDEOJEjPKP7CA2aFNK8Bkyy3fdNCg7r9/fW3jgpVJbDmy5+PB2IYp4MXFelQ7izPrhkPHB+P5/PjhD5gCgCenx+VR/dODEwD+A3T7nqbxwf1HAAAAAElFTkSuQmCC" />'
			. count($this->queries) . ' queries'
			. ($this->totalTime ? ' / ' . sprintf('%0.1f', $this->totalTime * 1000) . 'ms' : '')
			. '</span>';
	}

	function getPanel()
	{
		$this->disabled = TRUE;
		$s = '';
		$h = 'htmlSpecialChars';
		foreach ($this->queries as $i => $query) {
			list($sql, $params, $time, $rows, $connection, $source) = $query;

			$explain = NULL;
			if ($this->explain && preg_match('#\s*SELECT\s#iA', $sql)) {
				try {
					$explain = $connection->queryArgs('EXPLAIN ' . $sql, $params)->fetchAll();
				} catch (\PDOException $e) {}
			}

			$s .= '<tr><td>' . sprintf('%0.3f', $time * 1000);
			if ($explain) {
				static $counter;
				$counter++;
				$s .= "<br /><a href='#' class='nette-toggler' rel='#nette-DbConnectionPanel-row-$counter'>explain&nbsp;&#x25ba;</a>";
			}

			$s .= '</td><td class="nette-DbConnectionPanel-sql">' . Connection::highlightSql(Nette\Utils\Strings::truncate($sql, self::$maxLength));
			if ($explain) {
				$s .= "<table id='nette-DbConnectionPanel-row-$counter' class='nette-collapsed'><tr>";
				foreach ($explain[0] as $col => $foo) {
					$s .= "<th>{$h($col)}</th>";
				}
				$s .= "</tr>";
				foreach ($explain as $row) {
					$s .= "<tr>";
					foreach ($row as $col) {
						$s .= "<td>{$h($col)}</td>";
					}
					$s .= "</tr>";
				}
				$s .= "</table>";
			}
			if ($source) {
				$s .= Nette\Diagnostics\Helpers::editorLink($source[0], $source[1])->class('nette-DbConnectionPanel-source');
			}

			$s .= '</td><td>';
			foreach ($params as $param) {
				$s .= Debugger::dump($param, TRUE);
			}

			$s .= '</td><td>' . $rows . '</td></tr>';
		}

		return empty($this->queries) ? '' :
			'<style> #nette-debug td.nette-DbConnectionPanel-sql { background: white !important }
			#nette-debug .nette-DbConnectionPanel-source { color: #BBB !important }
			#nette-debug nette-DbConnectionPanel tr table { margin: 8px 0; max-height: 150px; overflow:auto } </style>
			<h1>Queries: ' . count($this->queries) . ($this->totalTime ? ', time: ' . sprintf('%0.3f', $this->totalTime * 1000) . ' ms' : '') . '</h1>
			<div class="nette-inner nette-DbConnectionPanel">
			<table>
				<tr><th>Time&nbsp;ms</th><th>SQL Statement</th><th>Params</th><th>Rows</th></tr>' . $s . '
			</table>
			</div>';
	}

}

}

namespace Nette\Database\Drivers {

use Nette;

class MsSqlDriver extends Nette\Object implements Nette\Database\ISupplementalDriver
{

	public $supports = array('meta' => TRUE);

	private $connection;

	function __construct(Nette\Database\Connection $connection, array $options)
	{
		$this->connection = $connection;
	}

	function delimite($name)
	{

		return '[' . str_replace(array('[', ']'), array('[[', ']]'), $name) . ']';
	}

	function formatDateTime(\DateTime $value)
	{
		return $value->format("'Y-m-d H:i:s'");
	}

	function formatLike($value, $pos)
	{
		$value = strtr($value, array("'" => "''", '%' => '[%]', '_' => '[_]', '[' => '[[]'));
		return ($pos <= 0 ? "'%" : "'") . $value . ($pos >= 0 ? "%'" : "'");
	}

	function applyLimit(&$sql, $limit, $offset)
	{

		if ($limit >= 0) {
			$sql = 'SELECT TOP ' . (int) $limit . ' * FROM (' . $sql . ') t';
		}

		if ($offset) {
			throw new Nette\NotImplementedException('Offset is not implemented.');
		}
	}

	function normalizeRow($row, $statement)
	{
		return $row;
	}

}

class MySqlDriver extends Nette\Object implements Nette\Database\ISupplementalDriver
{

	public $supports = array('meta' => TRUE);

	private $connection;

	function __construct(Nette\Database\Connection $connection, array $options)
	{
		$this->connection = $connection;
		$charset = isset($options['charset']) ? $options['charset'] : 'utf8';
		if ($charset) {
			$connection->exec("SET NAMES '$charset'");
		}
		if (isset($options['sqlmode'])) {
			$connection->exec("SET sql_mode='$options[sqlmode]'");
		}
		$connection->exec("SET time_zone='" . date('P') . "'");
	}

	function delimite($name)
	{

		return '`' . str_replace('`', '``', $name) . '`';
	}

	function formatDateTime(\DateTime $value)
	{
		return $value->format("'Y-m-d H:i:s'");
	}

	function formatLike($value, $pos)
	{
		$value = addcslashes(str_replace('\\', '\\\\', $value), "\x00\n\r\\'%_");
		return ($pos <= 0 ? "'%" : "'") . $value . ($pos >= 0 ? "%'" : "'");
	}

	function applyLimit(&$sql, $limit, $offset)
	{
		if ($limit >= 0 || $offset > 0) {

			$sql .= ' LIMIT ' . ($limit < 0 ? '18446744073709551615' : (int) $limit)
				. ($offset > 0 ? ' OFFSET ' . (int) $offset : '');
		}
	}

	function normalizeRow($row, $statement)
	{
		return $row;
	}

}

class OciDriver extends Nette\Object implements Nette\Database\ISupplementalDriver
{

	public $supports = array('meta' => TRUE);

	private $connection;

	private $fmtDateTime;

	function __construct(Nette\Database\Connection $connection, array $options)
	{
		$this->connection = $connection;
		$this->fmtDateTime = isset($options['formatDateTime']) ? $options['formatDateTime'] : 'U';
	}

	function delimite($name)
	{

		return '"' . str_replace('"', '""', $name) . '"';
	}

	function formatDateTime(\DateTime $value)
	{
		return $value->format($this->fmtDateTime);
	}

	function formatLike($value, $pos)
	{
		throw new Nette\NotImplementedException;
	}

	function applyLimit(&$sql, $limit, $offset)
	{
		if ($offset > 0) {

			$sql = 'SELECT * FROM (SELECT t.*, ROWNUM AS "__rnum" FROM (' . $sql . ') t '
				. ($limit >= 0 ? 'WHERE ROWNUM <= ' . ((int) $offset + (int) $limit) : '')
				. ') WHERE "__rnum" > '. (int) $offset;

		} elseif ($limit >= 0) {
			$sql = 'SELECT * FROM (' . $sql . ') WHERE ROWNUM <= ' . (int) $limit;
		}
	}

	function normalizeRow($row, $statement)
	{
		return $row;
	}

}

class OdbcDriver extends Nette\Object implements Nette\Database\ISupplementalDriver
{

	public $supports = array('meta' => TRUE);

	private $connection;

	function __construct(Nette\Database\Connection $connection, array $options)
	{
		$this->connection = $connection;
	}

	function delimite($name)
	{
		return '[' . str_replace(array('[', ']'), array('[[', ']]'), $name) . ']';
	}

	function formatDateTime(\DateTime $value)
	{
		return $value->format("#m/d/Y H:i:s#");
	}

	function formatLike($value, $pos)
	{
		$value = strtr($value, array("'" => "''", '%' => '[%]', '_' => '[_]', '[' => '[[]'));
		return ($pos <= 0 ? "'%" : "'") . $value . ($pos >= 0 ? "%'" : "'");
	}

	function applyLimit(&$sql, $limit, $offset)
	{

		if ($limit >= 0) {
			$sql = 'SELECT TOP ' . (int) $limit . ' * FROM (' . $sql . ')';
		}

		if ($offset) {
			throw new Nette\InvalidArgumentException('Offset is not implemented in driver odbc.');
		}
	}

	function normalizeRow($row, $statement)
	{
		return $row;
	}

}

class PgSqlDriver extends Nette\Object implements Nette\Database\ISupplementalDriver
{

	public $supports = array('meta' => TRUE);

	private $connection;

	function __construct(Nette\Database\Connection $connection, array $options)
	{
		$this->connection = $connection;
	}

	function delimite($name)
	{

		return '"' . str_replace('"', '""', $name) . '"';
	}

	function formatDateTime(\DateTime $value)
	{
		return $value->format("'Y-m-d H:i:s'");
	}

	function formatLike($value, $pos)
	{
		throw new Nette\NotImplementedException;
	}

	function applyLimit(&$sql, $limit, $offset)
	{
		if ($limit >= 0)
			$sql .= ' LIMIT ' . (int) $limit;

		if ($offset > 0)
			$sql .= ' OFFSET ' . (int) $offset;
	}

	function normalizeRow($row, $statement)
	{
		return $row;
	}

}

class SqliteDriver extends Nette\Object implements Nette\Database\ISupplementalDriver
{

	public $supports = array('meta' => FALSE);

	private $connection;

	private $fmtDateTime;

	function __construct(Nette\Database\Connection $connection, array $options)
	{
		$this->connection = $connection;
		$this->fmtDateTime = isset($options['formatDateTime']) ? $options['formatDateTime'] : 'U';
	}

	function delimite($name)
	{
		return '[' . strtr($name, '[]', '  ') . ']';
	}

	function formatDateTime(\DateTime $value)
	{
		return $value->format($this->fmtDateTime);
	}

	function formatLike($value, $pos)
	{
		$value = addcslashes(substr($this->connection->quote($value), 1, -1), '%_\\');
		return ($pos <= 0 ? "'%" : "'") . $value . ($pos >= 0 ? "%'" : "'") . " ESCAPE '\\'";
	}

	function applyLimit(&$sql, $limit, $offset)
	{
		if ($limit >= 0 || $offset > 0) {
			$sql .= ' LIMIT ' . $limit . ($offset > 0 ? ' OFFSET ' . (int) $offset : '');
		}
	}

	function normalizeRow($row, $statement)
	{
		return $row;
	}

}

class Sqlite2Driver extends SqliteDriver
{

	function formatLike($value, $pos)
	{
		throw new Nette\NotSupportedException;
	}

	function normalizeRow($row, $statement)
	{
		if (!is_object($row)) {
			$iterator = $row;
		} elseif ($row instanceof \Traversable) {
			$iterator = iterator_to_array($row);
		} else {
			$iterator = (array) $row;
		}
		foreach ($iterator as $key => $value) {
			unset($row[$key]);
			if ($key[0] === '[' || $key[0] === '"') {
				$key = substr($key, 1, -1);
			}
			$row[$key] = $value;
		}
		return $row;
	}

}

}

namespace Nette\Database\Reflection {

use Nette;

class DatabaseReflection extends Nette\Object
{
	const FIELD_TEXT = 'string',
		FIELD_BINARY = 'bin',
		FIELD_BOOL = 'bool',
		FIELD_INTEGER = 'int',
		FIELD_FLOAT = 'float',
		FIELD_DATETIME = 'datetime';

	private $primary;

	private $foreign;

	private $table;

	function __construct($primary = 'id', $foreign = '%s_id', $table = '%s')
	{
		$this->primary = $primary;
		$this->foreign = $foreign;
		$this->table = $table;
	}

	function getPrimary($table)
	{
		return sprintf($this->primary, $table);
	}

	function getReferencingColumn($name, $table)
	{
		return $this->getReferencedColumn($table, $name);
	}

	function getReferencedColumn($name, $table)
	{
		if ($this->table !== '%s' && preg_match('(^' . str_replace('%s', '(.*)', preg_quote($this->table)) . '$)', $name, $match)) {
			$name = $match[1];
		}
		return sprintf($this->foreign, $name, $table);
	}

	function getReferencedTable($name, $table)
	{
		return sprintf($this->table, $name, $table);
	}

	static function detectType($type)
	{
		static $types, $patterns = array(
			'BYTEA|BLOB|BIN' => self::FIELD_BINARY,
			'TEXT|CHAR' => self::FIELD_TEXT,
			'YEAR|BYTE|COUNTER|SERIAL|INT|LONG' => self::FIELD_INTEGER,
			'CURRENCY|REAL|MONEY|FLOAT|DOUBLE|DECIMAL|NUMERIC|NUMBER' => self::FIELD_FLOAT,
			'TIME|DATE' => self::FIELD_DATETIME,
			'BOOL|BIT' => self::FIELD_BOOL,
		);

		if (!isset($types[$type])) {
			$types[$type] = 'string';
			foreach ($patterns as $s => $val) {
				if (preg_match("#$s#i", $type)) {
					return $types[$type] = $val;
				}
			}
		}
		return $types[$type];
	}

}

}

namespace Nette\Database {

use Nette;

class Row extends Nette\ArrayHash
{

	function __construct($statement)
	{
		$statement->normalizeRow($this);
	}

	function offsetGet($key)
	{
		if (is_int($key)) {
			$arr = array_values((array) $this);
			return $arr[$key];
		}
		return $this->$key;
	}

}

class SqlLiteral
{

	public $value = '';

	function __construct($value)
	{
		$this->value = (string) $value;
	}

}

class SqlPreprocessor extends Nette\Object
{

	private $connection;

	private $driver;

	private $params;

	private $remaining;

	private $counter;

	private $arrayMode;

	function __construct(Connection $connection)
	{
		$this->connection = $connection;
		$this->driver = $connection->getSupplementalDriver();
	}

	function process($sql, $params)
	{
		$this->params = $params;
		$this->counter = 0;
		$this->remaining = array();

		$cmd = strtoupper(substr(ltrim($sql), 0, 6));
		$this->arrayMode = $cmd === 'INSERT' || $cmd === 'REPLAC' ? 'values' : 'assoc';

		$sql = Nette\Utils\Strings::replace($sql, '~\'.*?\'|".*?"|:[a-zA-Z0-9_]+:|\?~s', array($this, 'callback'));

		while ($this->counter < count($params)) {
			$sql .= ' ' . $this->formatValue($params[$this->counter++]);
		}

		return array($sql, $this->remaining);
	}

	function callback($m)
	{
		$m = $m[0];
		if ($m[0] === "'" || $m[0] === '"') {
			return $m;

		} elseif ($m[0] === '?') {
			return $this->formatValue($this->params[$this->counter++]);

		} elseif ($m[0] === ':') {
			$s = substr($m, 1, -1);
			return isset($this->connection->substitutions[$s]) ? $this->connection->substitutions[$s] : $m;
		}
	}

	private function formatValue($value)
	{
		if (is_string($value)) {
			if (strlen($value) > 20) {
				$this->remaining[] = $value;
				return '?';

			} else {
				return $this->connection->quote($value);
			}

		} elseif (is_int($value)) {
			return (string) $value;

		} elseif (is_float($value)) {
			return rtrim(rtrim(number_format($value, 10, '.', ''), '0'), '.');

		} elseif (is_bool($value)) {
			$this->remaining[] = $value;
			return '?';

		} elseif ($value === NULL) {
			return 'NULL';

		} elseif (is_array($value) || $value instanceof \Traversable) {
			$vx = $kx = array();

			if (isset($value[0])) {
				foreach ($value as $v) {
					$vx[] = $this->formatValue($v);
				}
				return implode(', ', $vx);

			} elseif ($this->arrayMode === 'values') {
				$this->arrayMode = 'multi';
				foreach ($value as $k => $v) {
					$kx[] = $this->driver->delimite($k);
					$vx[] = $this->formatValue($v);
				}
				return '(' . implode(', ', $kx) . ') VALUES (' . implode(', ', $vx) . ')';

			} elseif ($this->arrayMode === 'assoc') {
				foreach ($value as $k => $v) {
					$vx[] = $this->driver->delimite($k) . '=' . $this->formatValue($v);
				}
				return implode(', ', $vx);

			} elseif ($this->arrayMode === 'multi') {
				foreach ($value as $k => $v) {
					$vx[] = $this->formatValue($v);
				}
				return ', (' . implode(', ', $vx) . ')';
			}

		} elseif ($value instanceof \DateTime) {
			return $this->driver->formatDateTime($value);

		} elseif ($value instanceof SqlLiteral) {
			return $value->value;

		} else {
			$this->remaining[] = $value;
			return '?';
		}
	}

}

use PDO;use Nette\ObjectMixin;

if (class_exists('PDO')){ class Statement extends \PDOStatement
{

	private $connection;

	public $time;

	private $types;

	protected function __construct(Connection $connection)
	{
		$this->connection = $connection;
		$this->setFetchMode(PDO::FETCH_CLASS, 'Nette\Database\Row', array($this));
	}

	function getConnection()
	{
		return $this->connection;
	}

	function execute($params = array())
	{
		static $types = array('boolean' => PDO::PARAM_BOOL, 'integer' => PDO::PARAM_INT,
			'resource' => PDO::PARAM_LOB, 'NULL' => PDO::PARAM_NULL);

		foreach ($params as $key => $value) {
			$type = gettype($value);
			$this->bindValue(is_int($key) ? $key + 1 : $key, $value, isset($types[$type]) ? $types[$type] : PDO::PARAM_STR);
		}

		$time = microtime(TRUE);
		try {
			parent::execute();
		} catch (\PDOException $e) {
			$e->queryString = $this->queryString;
			throw $e;
		}
		$this->time = microtime(TRUE) - $time;
		$this->connection->__call('onQuery', array($this, $params));

		return $this;
	}

	function fetchPairs()
	{
		return $this->fetchAll(PDO::FETCH_KEY_PAIR);
	}

	function normalizeRow($row)
	{
		if ($this->types === NULL) {
			$this->types = array();
			if ($this->connection->getSupplementalDriver()->supports['meta']) {
				$col = 0;
				foreach ($row as $key => $foo) {
					$type = $this->getColumnMeta($col++);
					if (isset($type['native_type'])) {
						$this->types[$key] = Reflection\DatabaseReflection::detectType($type['native_type']);
					}
				}
			}
		}

		foreach ($this->types as $key => $type) {
			$value = $row[$key];
			if ($value === NULL || $value === FALSE || $type === Reflection\DatabaseReflection::FIELD_TEXT) {

			} elseif ($type === Reflection\DatabaseReflection::FIELD_INTEGER) {
				$row[$key] = is_float($tmp = $value * 1) ? $value : $tmp;

			} elseif ($type === Reflection\DatabaseReflection::FIELD_FLOAT) {
				$row[$key] = (string) ($tmp = (float) $value) === $value ? $tmp : $value;

			} elseif ($type === Reflection\DatabaseReflection::FIELD_BOOL) {
				$row[$key] = ((bool) $value) && $value !== 'f' && $value !== 'F';
			}
		}

		return $this->connection->getSupplementalDriver()->normalizeRow($row, $this);
	}

	function dump()
	{
		echo "\n<table class=\"dump\">\n<caption>" . htmlSpecialChars($this->queryString) . "</caption>\n";
		if (!$this->columnCount()) {
			echo "\t<tr>\n\t\t<th>Affected rows:</th>\n\t\t<td>", $this->rowCount(), "</td>\n\t</tr>\n</table>\n";
			return;
		}
		$i = 0;
		foreach ($this as $row) {
			if ($i === 0) {
				echo "<thead>\n\t<tr>\n\t\t<th>#row</th>\n";
				foreach ($row as $col => $foo) {
					echo "\t\t<th>" . htmlSpecialChars($col) . "</th>\n";
				}
				echo "\t</tr>\n</thead>\n<tbody>\n";
			}
			echo "\t<tr>\n\t\t<th>", $i, "</th>\n";
			foreach ($row as $col) {

				echo "\t\t<td>", htmlSpecialChars($col), "</td>\n";
			}
			echo "\t</tr>\n";
			$i++;
		}

		if ($i === 0) {
			echo "\t<tr>\n\t\t<td><em>empty result set</em></td>\n\t</tr>\n</table>\n";
		} else {
			echo "</tbody>\n</table>\n";
		}
	}

	static function getReflection()
	{
		return new Nette\Reflection\ClassType(get_called_class());
	}

	function __call($name, $args)
	{
		return ObjectMixin::call($this, $name, $args);
	}

	function &__get($name)
	{
		return ObjectMixin::get($this, $name);
	}

	function __set($name, $value)
	{
		return ObjectMixin::set($this, $name, $value);
	}

	function __isset($name)
	{
		return ObjectMixin::has($this, $name);
	}

	function __unset($name)
	{
		ObjectMixin::remove($this, $name);
	}

}
 }

}

namespace Nette\Database\Table {

use Nette;

class ActiveRow extends Nette\Object implements \IteratorAggregate, \ArrayAccess
{

	protected $table;

	protected $data;

	private $modified = array();

	function __construct(array $data, Selection $table)
	{
		$this->data = $data;
		$this->table = $table;
	}

	function __toString()
	{
		return (string) $this[$this->table->primary];
	}

	function toArray()
	{
		$this->access(NULL);
		return $this->data;
	}

	function ref($name)
	{
		$referenced = $this->table->getReferencedTable($name, $column);
		if (isset($referenced[$this[$column]])) {
			$res = $referenced[$this[$column]];
			return $res;
		}
	}

	function related($table)
	{
		$referencing = $this->table->getReferencingTable($table);
		$referencing->active = $this[$this->table->primary];
		return $referencing;
	}

	function update($data = NULL)
	{
		if ($data === NULL) {
			$data = $this->modified;
		}
		return $this->table->connection->table($this->table->name)
			->where($this->table->primary, $this[$this->table->primary])
			->update($data);
	}

	function delete()
	{
		return $this->table->connection->table($this->table->name)
			->where($this->table->primary, $this[$this->table->primary])
			->delete();
	}

	function getIterator()
	{
		$this->access(NULL);
		return new \ArrayIterator($this->data);
	}

	function offsetSet($key, $value)
	{
		$this->__set($key, $value);
	}

	function offsetGet($key)
	{
		return $this->__get($key);
	}

	function offsetExists($key)
	{
		return $this->__isset($key);
	}

	function offsetUnset($key)
	{
		$this->__unset($key);
	}

	function __set($key, $value)
	{
		$this->data[$key] = $value;
		$this->modified[$key] = $value;
	}

	function &__get($key)
	{
		if (array_key_exists($key, $this->data)) {
			$this->access($key);
			return $this->data[$key];
		}

		$column = $this->table->connection->databaseReflection->getReferencedColumn($key, $this->table->name);
		if (array_key_exists($column, $this->data)) {
			$value = $this->data[$column];
			$referenced = $this->table->getReferencedTable($key);
			$ret = isset($referenced[$value]) ? $referenced[$value] : NULL;
			return $ret;
		}

		$this->access($key);
		if (array_key_exists($key, $this->data)) {
			return $this->data[$key];

		} else {
			$this->access($key, TRUE);

			$this->access($column);
			if (array_key_exists($column, $this->data)) {
				$value = $this->data[$column];
				$referenced = $this->table->getReferencedTable($key);
				$ret = isset($referenced[$value]) ? $referenced[$value] : NULL;

			} else {
				$this->access($column, TRUE);
				trigger_error("Unknown column $key", E_USER_WARNING);
				$ret = NULL;
			}
			return $ret;
		}
	}

	function __isset($key)
	{
		$this->access($key);
		$return = array_key_exists($key, $this->data);
		if (!$return) {
			$this->access($key, TRUE);
		}
		return $return;
	}

	function __unset($key)
	{
		unset($this->data[$key]);
		unset($this->modified[$key]);
	}

	function access($key, $delete = FALSE)
	{
		if ($this->table->connection->getCache() && !isset($this->modified[$key]) && $this->table->access($key, $delete)) {
			$id = (isset($this->data[$this->table->primary]) ? $this->data[$this->table->primary] : $this->data);
			$this->data = $this->table[$id]->data;
		}
	}

}

use PDO;

class Selection extends Nette\Object implements \Iterator, \ArrayAccess, \Countable
{

	public $connection;

	public $name;

	public $primary;

	protected $rows;

	protected $data;

	protected $select = array();

	protected $where = array();

	protected $conditions = array();

	protected $parameters = array();

	protected $order = array();

	protected $limit = NULL;

	protected $offset = NULL;

	protected $group = '';

	protected $having = '';

	protected $referenced = array();

	protected $referencing = array();

	protected $aggregation = array();

	protected $accessed;

	protected $prevAccessed;

	protected $keys = array();

	protected $delimitedName;

	protected $delimitedPrimary;

	function __construct($table, Nette\Database\Connection $connection)
	{
		$this->name = $table;
		$this->connection = $connection;
		$this->primary = $this->getPrimary($table);
		$this->delimitedName = $connection->getSupplementalDriver()->delimite($this->name);
		$this->delimitedPrimary = $connection->getSupplementalDriver()->delimite($this->primary);
	}

	function __destruct()
	{
		$cache = $this->connection->getCache();
		if ($cache && !$this->select && $this->rows !== NULL) {
			$accessed = $this->accessed;
			if (is_array($accessed)) {
				$accessed = array_filter($accessed);
			}
			$cache->save(array(__CLASS__, $this->name, $this->conditions), $accessed);
		}
		$this->rows = NULL;
		$this->data = NULL;
	}

	function get($key)
	{

		$clone = clone $this;
		$clone->where($this->delimitedPrimary, $key);
		return $clone->fetch();
	}

	function select($columns)
	{
		$this->__destruct();
		$this->select[] = $this->tryDelimite($columns);
		return $this;
	}

	function find($key)
	{
		return $this->where($this->delimitedPrimary, $key);
	}

	function where($condition, $parameters = array())
	{
		if (is_array($condition)) {
			foreach ($condition as $key => $val) {
				$this->where($key, $val);
			}
			return $this;
		}

		$this->__destruct();

		$this->conditions[] = $condition;
		$condition = $this->tryDelimite($condition);

		$args = func_num_args();
		if ($args !== 2 || strpbrk($condition, '?:')) {
			if ($args !== 2 || !is_array($parameters)) {
				$parameters = func_get_args();
				array_shift($parameters);
			}
			$this->parameters = array_merge($this->parameters, $parameters);

		} elseif ($parameters === NULL) {
			$condition .= ' IS NULL';

		} elseif ($parameters instanceof Selection) {
			$clone = clone $parameters;
			if (!$clone->select) {
				$clone->select = array($this->getPrimary($clone->name));
			}
			if ($this->connection->getAttribute(PDO::ATTR_DRIVER_NAME) !== 'mysql') {
				$condition .= " IN ($clone)";
			} else {
				$in = array();
				foreach ($clone as $row) {
					$this->parameters[] = array_values(iterator_to_array($row));
					$in[] = (count($row) === 1 ? '?' : '(?)');
				}
				$condition .= ' IN (' . ($in ? implode(', ', $in) : 'NULL') . ')';
			}

		} elseif (!is_array($parameters)) {
			$condition .= ' = ?';
			$this->parameters[] = $parameters;

		} else {
			if ($parameters) {
				$condition .= " IN (?)";
				$this->parameters[] = $parameters;
			} else {
				$condition .= " IN (NULL)";
			}
		}

		$this->where[] = $condition;
		return $this;
	}

	function order($columns)
	{
		$this->rows = NULL;
		$this->order[] = $this->tryDelimite($columns);
		return $this;
	}

	function limit($limit, $offset = NULL)
	{
		$this->rows = NULL;
		$this->limit = $limit;
		$this->offset = $offset;
		return $this;
	}

	function page($page, $itemsPerPage)
	{
		$this->rows = NULL;
		$this->limit = $itemsPerPage;
		$this->offset = ($page - 1) * $itemsPerPage;
		return $this;
	}

	function group($columns, $having = '')
	{
		$this->__destruct();
		$this->group = $columns;
		$this->having = $having;
		return $this;
	}

	function aggregation($function)
	{
		$join = $this->createJoins(implode(',', $this->conditions), TRUE) + $this->createJoins($function);
		$query = "SELECT $function FROM $this->delimitedName" . implode($join);
		if ($this->where) {
			$query .= ' WHERE (' . implode(') AND (', $this->where) . ')';
		}
		foreach ($this->query($query)->fetch() as $val) {
			return $val;
		}
	}

	function count($column = '')
	{
		if (!$column) {
			$this->execute();
			return count($this->data);
		}
		return $this->aggregation("COUNT({$this->tryDelimite($column)})");
	}

	function min($column)
	{
		return $this->aggregation("MIN({$this->tryDelimite($column)})");
	}

	function max($column)
	{
		return $this->aggregation("MAX({$this->tryDelimite($column)})");
	}

	function sum($column)
	{
		return $this->aggregation("SUM({$this->tryDelimite($column)})");
	}

	function getSql()
	{
		$join = $this->createJoins(implode(',', $this->conditions), TRUE)
			+ $this->createJoins(implode(',', $this->select) . ",$this->group,$this->having," . implode(',', $this->order));

		$cache = $this->connection->getCache();
		if ($this->rows === NULL && $cache && !is_string($this->prevAccessed)) {
			$this->accessed = $this->prevAccessed = $cache->load(array(__CLASS__, $this->name, $this->conditions));
		}

		$prefix = $join ? "$this->delimitedName." : '';
		if ($this->select) {
			$cols = implode(', ', $this->select);

		} elseif ($this->prevAccessed) {
			$cols = $prefix . implode(', ' . $prefix, array_map(array($this->connection->getSupplementalDriver(), 'delimite'), array_keys($this->prevAccessed)));

		} else {
			$cols = $prefix . '*';
		}

		return "SELECT{$this->topString()} $cols FROM $this->delimitedName" . implode($join) . $this->whereString();
	}

	protected function createJoins($val, $inner = FALSE)
	{
		$supplementalDriver = $this->connection->getSupplementalDriver();
		$joins = array();
		preg_match_all('~\\b([a-z][\\w.]*)\\.([a-z]\\w*)(\\s+IS\\b|\\s*<=>)?~i', $val, $matches, PREG_SET_ORDER);
		foreach ($matches as $match) {
			if ($match[1] !== $this->name) {
				foreach (explode('.', $match[1]) as $name) {
					$table = $this->connection->databaseReflection->getReferencedTable($name, $this->name);
					$column = $this->connection->databaseReflection->getReferencedColumn($name, $this->name);
					$primary = $this->getPrimary($table);
					$joins[$name] = ' ' . (!isset($joins[$name]) && $inner && !isset($match[3]) ? 'INNER' : 'LEFT')
						. ' JOIN ' . $supplementalDriver->delimite($table)
						. ($table !== $name ? ' AS ' . $supplementalDriver->delimite($name) : '')
						. " ON $this->delimitedName." . $supplementalDriver->delimite($column)
						. ' = ' . $supplementalDriver->delimite($name) . '.' . $supplementalDriver->delimite($primary);
				}
			}
		}
		return $joins;
	}

	protected function execute()
	{
		if ($this->rows !== NULL) {
			return;
		}

		try {
			$result = $this->query($this->getSql());

		} catch (\PDOException $exception) {
			if (!$this->select && $this->prevAccessed) {
				$this->prevAccessed = '';
				$this->accessed = array();
				$result = $this->query($this->getSql());
			} else {
				throw $exception;
			}
		}

		$this->rows = array();
		$result->setFetchMode(PDO::FETCH_ASSOC);
		foreach ($result as $key => $row) {
			$row = $result->normalizeRow($row);
			$this->rows[isset($row[$this->primary]) ? $row[$this->primary] : $key] = new ActiveRow($row, $this);
		}
		$this->data = $this->rows;

		if (isset($row[$this->primary]) && !is_string($this->accessed)) {
			$this->accessed[$this->primary] = TRUE;
		}
	}

	protected function whereString()
	{
		$return = '';
		$driver = $this->connection->getAttribute(PDO::ATTR_DRIVER_NAME);
		$where = $this->where;
		if ($this->limit !== NULL && $driver === 'oci') {
			$where[] = ($this->offset ? "rownum > $this->offset AND " : '') . 'rownum <= ' . ($this->limit + $this->offset);
		}
		if ($where) {
			$return .= ' WHERE (' . implode(') AND (', $where) . ')';
		}
		if ($this->group) {
			$return .= ' GROUP BY '. $this->tryDelimite($this->group);
		}
		if ($this->having) {
			$return .= ' HAVING '. $this->tryDelimite($this->having);
		}
		if ($this->order) {
			$return .= ' ORDER BY ' . implode(', ', $this->order);
		}
		if ($this->limit !== NULL && $driver !== 'oci' && $driver !== 'dblib') {
			$return .= " LIMIT $this->limit";
			if ($this->offset !== NULL) {
				$return .= " OFFSET $this->offset";
			}
		}
		return $return;
	}

	protected function topString()
	{
		if ($this->limit !== NULL && $this->connection->getAttribute(PDO::ATTR_DRIVER_NAME) === 'dblib') {
			return " TOP ($this->limit)";
		}
		return '';
	}

	protected function tryDelimite($s)
	{
		return preg_match('#^[a-z_][a-z0-9_.]*$#i', $s)
			? implode('.', array_map(array($this->connection->getSupplementalDriver(), 'delimite'), explode('.', $s)))
			: $s;
	}

	protected function query($query)
	{
		return $this->connection->queryArgs($query, $this->parameters);
	}

	function access($key, $delete = FALSE)
	{
		if ($delete) {
			if (is_array($this->accessed)) {
				$this->accessed[$key] = FALSE;
			}
			return FALSE;
		}

		if ($key === NULL) {
			$this->accessed = '';

		} elseif (!is_string($this->accessed)) {
			$this->accessed[$key] = TRUE;
		}

		if (!$this->select && $this->prevAccessed && ($key === NULL || !isset($this->prevAccessed[$key]))) {
			$this->prevAccessed = '';
			$this->rows = NULL;
			return TRUE;
		}
		return FALSE;
	}

	function insert($data)
	{
		if ($data instanceof Selection) {
			$data = $data->getSql();

		} elseif ($data instanceof \Traversable) {
			$data = iterator_to_array($data);
		}

		$return = $this->connection->query("INSERT INTO $this->delimitedName", $data);

		$this->rows = NULL;
		if (!is_array($data)) {
			return $return->rowCount();
		}

		if (!isset($data[$this->primary]) && ($id = $this->connection->lastInsertId())) {
			$data[$this->primary] = $id;
		}
		return new ActiveRow($data, $this);
	}

	function update($data)
	{
		if ($data instanceof \Traversable) {
			$data = iterator_to_array($data);

		} elseif (!is_array($data)) {
			throw new Nette\InvalidArgumentException;
		}

		if (!$data) {
			return 0;
		}

		return $this->connection->queryArgs(
			'UPDATE' . $this->topString() . " $this->delimitedName SET ?" . $this->whereString(),
			array_merge(array($data), $this->parameters)
		)->rowCount();
	}

	function delete()
	{
		return $this->query(
			'DELETE' . $this->topString() . " FROM $this->delimitedName" . $this->whereString()
		)->rowCount();
	}

	function getReferencedTable($name, & $column = NULL)
	{
		$column = $this->connection->databaseReflection->getReferencedColumn($name, $this->name);
		$referenced = & $this->referenced[$name];
		if ($referenced === NULL) {
			$keys = array();
			foreach ($this->rows as $row) {
				if ($row[$column] !== NULL) {
					$keys[$row[$column]] = NULL;
				}
			}
			if ($keys) {
				$table = $this->connection->databaseReflection->getReferencedTable($name, $this->name);
				$referenced = new Selection($table, $this->connection);
				$referenced->where($table . '.' . $this->getPrimary($table), array_keys($keys));
			} else {
				$referenced = array();
			}
		}
		return $referenced;
	}

	function getReferencingTable($table)
	{
		$column = $this->connection->databaseReflection->getReferencingColumn($table, $this->name);
		$referencing = new GroupedSelection($table, $this, $column);
		$referencing->where("$table.$column", array_keys((array) $this->rows));
		return $referencing;
	}

	private function getPrimary($table)
	{
		return $this->connection->databaseReflection->getPrimary($table);
	}

	function rewind()
	{
		$this->execute();
		$this->keys = array_keys($this->data);
		reset($this->keys);
	}

	function current()
	{
		return $this->data[current($this->keys)];
	}

	function key()
	{
		return current($this->keys);
	}

	function next()
	{
		next($this->keys);
	}

	function valid()
	{
		return current($this->keys) !== FALSE;
	}

	function offsetSet($key, $value)
	{
		$this->execute();
		$this->data[$key] = $value;
	}

	function offsetGet($key)
	{
		$this->execute();
		return $this->data[$key];
	}

	function offsetExists($key)
	{
		$this->execute();
		return isset($this->data[$key]);
	}

	function offsetUnset($key)
	{
		$this->execute();
		unset($this->data[$key]);
	}

	function fetch()
	{
		$this->execute();
		$return = current($this->data);
		next($this->data);
		return $return;
	}

	function fetchPairs($key, $value = '')
	{
		$return = array();

		foreach ($this as $row) {
			$return[$row[$key]] = ($value !== '' ? $row[$value] : $row);
		}
		return $return;
	}

}

class GroupedSelection extends Selection
{

	private $refTable;

	private $column;

	private $delimitedColumn;

	public $active;

	function __construct($name, Selection $refTable, $column)
	{
		parent::__construct($name, $refTable->connection);
		$this->refTable = $refTable;
		$this->through($column);
	}

	function through($column)
	{
		$this->column = $column;
		$this->delimitedColumn = $this->refTable->connection->getSupplementalDriver()->delimite($this->column);
		return $this;
	}

	function select($columns)
	{
		if (!$this->select) {
			$this->select[] = "$this->delimitedName.$this->delimitedColumn";
		}
		return parent::select($columns);
	}

	function order($columns)
	{
		if (!$this->order) {
			$this->order[] = "$this->delimitedName.$this->delimitedColumn"
				. (preg_match('~\\bDESC$~i', $columns) ? ' DESC' : '');
		}
		return parent::order($columns);
	}

	function aggregation($function)
	{
		$join = $this->createJoins(implode(',', $this->conditions), TRUE) + $this->createJoins($function);
		$column = ($join ? "$this->delimitedName." : '') . $this->delimitedColumn;
		$query = "SELECT $function, $column FROM $this->delimitedName" . implode($join);
		if ($this->where) {
			$query .= ' WHERE (' . implode(') AND (', $this->where) . ')';
		}
		$query .= " GROUP BY $column";
		$aggregation = & $this->refTable->aggregation[$query];
		if ($aggregation === NULL) {
			$aggregation = array();
			foreach ($this->query($query, $this->parameters) as $row) {
				$aggregation[$row[$this->column]] = $row;
			}
		}

		if (isset($aggregation[$this->active])) {
			foreach ($aggregation[$this->active] as $val) {
				return $val;
			}
		}
	}

	function count($column = '')
	{
		$return = parent::count($column);
		return isset($return) ? $return : 0;
	}

	function insert($data)
	{
		if ($data instanceof \Traversable && !$data instanceof Selection) {
			$data = iterator_to_array($data);
		}
		if (is_array($data)) {
			$data[$this->column] = $this->active;
		}
		return parent::insert($data);
	}

	function update($data)
	{
		$where = $this->where;
		$this->where[0] = "$this->delimitedColumn = " . $this->connection->quote($this->active);
		$return = parent::update($data);
		$this->where = $where;
		return $return;
	}

	function delete()
	{
		$where = $this->where;
		$this->where[0] = "$this->delimitedColumn = " . $this->connection->quote($this->active);
		$return = parent::delete();
		$this->where = $where;
		return $return;
	}

	protected function execute()
	{
		if ($this->rows !== NULL) {
			return;
		}

		$referencing = & $this->refTable->referencing[$this->getSql()];
		if ($referencing === NULL) {
			$limit = $this->limit;
			$rows = count($this->refTable->rows);
			if ($this->limit && $rows > 1) {
				$this->limit = NULL;
			}
			parent::execute();
			$this->limit = $limit;
			$referencing = array();
			$offset = array();
			foreach ($this->rows as $key => $row) {
				$ref = & $referencing[$row[$this->column]];
				$skip = & $offset[$row[$this->column]];
				if ($limit === NULL || $rows <= 1 || (count($ref) < $limit && $skip >= $this->offset)) {
					$ref[$key] = $row;
				} else {
					unset($this->rows[$key]);
				}
				$skip++;
				unset($ref, $skip);
			}
		}

		$this->data = & $referencing[$this->active];
		if ($this->data === NULL) {
			$this->data = array();
		}
	}

}

}

namespace Nette\DI {

use Nette;

class Container extends Nette\FreezableObject implements IContainer
{
	const TAG_TYPEHINT = 'typeHint';

	public $params = array();

	private $registry = array();

	private $factories = array();

	private $tags = array();

	private $creating;

	function addService($name, $service, $tags = NULL)
	{
		$this->updating();
		if (!is_string($name) || $name === '') {
			throw new Nette\InvalidArgumentException("Service name must be a non-empty string, " . gettype($name) . " given.");
		}

		if (isset($this->registry[$name]) || method_exists($this, "createService$name")) {
			throw new Nette\InvalidStateException("Service '$name' has already been registered.");
		}

		if (is_string($tags)) {
			$tags = array(self::TAG_TYPEHINT => array($tags));
		} elseif (is_array($tags)) {
			foreach ($tags as $id => $attrs) {
				if (is_int($id) && is_string($attrs)) {
					$tags[$attrs] = array();
					unset($tags[$id]);
				} elseif (!is_array($attrs)) {
					$tags[$id] = (array) $attrs;
				}
			}
		}

		if (is_string($service) && strpos($service, ':') === FALSE) {
			if (!isset($tags[self::TAG_TYPEHINT][0])) {
				$tags[self::TAG_TYPEHINT][0] = $service;
			}
			$service = new ServiceBuilder($service);
		}

		if ($service instanceof IServiceBuilder) {
			$factory = array($service, 'createService');

		} elseif (is_object($service) && !$service instanceof \Closure && !$service instanceof Nette\Callback) {
			$this->registry[$name] = $service;
			$this->tags[$name] = $tags;
			return $this;

		} else {
			$factory = $service;
		}

		$this->factories[$name] = array(callback($factory));
		$this->tags[$name] = $tags;
		$this->registry[$name] = & $this->factories[$name][1];
		return $service;
	}

	function removeService($name)
	{
		$this->updating();
		unset($this->registry[$name], $this->factories[$name]);
	}

	function getService($name)
	{
		if (isset($this->registry[$name])) {
			return $this->registry[$name];

		} elseif (isset($this->creating[$name])) {
			throw new Nette\InvalidStateException("Circular reference detected for services: "
				. implode(', ', array_keys($this->creating)) . ".");
		}

		if (isset($this->factories[$name])) {
			list($factory) = $this->factories[$name];
			if (!$factory->isCallable()) {
				throw new Nette\InvalidStateException("Unable to create service '$name', factory '$factory' is not callable.");
			}

			$this->creating[$name] = TRUE;
			try {
				$service = $factory($this);
			} catch (\Exception $e) {}

		} elseif (method_exists($this, $factory = 'createService' . ucfirst($name))) {
			$this->creating[$name] = TRUE;
			try {
				$service = $this->$factory();
			} catch (\Exception $e) {}

		} else {
			throw new MissingServiceException("Service '$name' not found.");
		}

		unset($this->creating[$name]);

		if (isset($e)) {
			throw $e;

		} elseif (!is_object($service)) {
			throw new Nette\UnexpectedValueException("Unable to create service '$name', value returned by factory '$factory' is not object.");

		} elseif (isset($this->tags[$name][self::TAG_TYPEHINT][0]) && !$service instanceof $this->tags[$name][self::TAG_TYPEHINT][0]) {
			throw new Nette\UnexpectedValueException("Unable to create service '$name', value returned by factory '$factory' is not '{$this->tags[$name][self::TAG_TYPEHINT][0]}' type.");
		}

		unset($this->factories[$name]);
		return $this->registry[$name] = $service;
	}

	function getServiceByType($type)
	{
		foreach ($this->registry as $name => $service) {
			if (isset($this->tags[$name][self::TAG_TYPEHINT][0]) ? !strcasecmp($this->tags[$name][self::TAG_TYPEHINT][0], $type) : $service instanceof $type) {
				$found[] = $name;
			}
		}
		if (!isset($found)) {
			throw new MissingServiceException("Service matching '$type' type not found.");

		} elseif (count($found) > 1) {
			throw new AmbiguousServiceException("Found more than one service ('" . implode("', '", $found) . "') matching '$type' type.");
		}
		return $this->getService($found[0]);
	}

	function getServiceNamesByTag($tag)
	{
		$found = array();
		foreach ($this->registry as $name => $service) {
			if (isset($this->tags[$name][$tag])) {
				$found[$name] = $this->tags[$name][$tag];
			}
		}
		return $found;
	}

	function hasService($name)
	{
		return isset($this->registry[$name])
			|| isset($this->factories[$name])
			|| method_exists($this, "createService$name");
	}

	function checkServiceType($name, $type)
	{
		return isset($this->tags[$name][self::TAG_TYPEHINT][0])
			? !strcasecmp($this->tags[$name][self::TAG_TYPEHINT][0], $type)
			: (isset($this->registry[$name]) && $this->registry[$name] instanceof $type);
	}

	function expand($s)
	{
		return is_string($s) ? Nette\Utils\Strings::expand($s, $this->params) : $s;
	}

	function &__get($name)
	{
		if (!isset($this->registry[$name])) {
			$this->getService($name);
		}
		return $this->registry[$name];
	}

	function __set($name, $service)
	{
		$this->updating();
		if (!is_string($name) || $name === '') {
			throw new Nette\InvalidArgumentException("Service name must be a non-empty string, " . gettype($name) . " given.");

		} elseif (isset($this->registry[$name]) || method_exists($this, "createService$name")) {
			throw new Nette\InvalidStateException("Service '$name' has already been registered.");

		} elseif (!is_object($service)) {
			throw new Nette\InvalidArgumentException("Service must be a object, " . gettype($service) . " given.");
		}
		$this->registry[$name] = $service;
	}

	function __isset($name)
	{
		return $this->hasService($name);
	}

	function __unset($name)
	{
		$this->removeService($name);
	}

}

class ContainerBuilder extends Nette\Object
{

	function addDefinitions(IContainer $container, array $definitions)
	{
		foreach ($definitions as $name => $definition) {
			if (is_scalar($definition)) {
				if (substr($definition, 0, 1) === '@') {
					$definition = array('alias' => substr($definition, 1));
				} else {
					$definition = array('class' => $definition);
				}
			}

			$arguments = isset($definition['arguments']) ? $definition['arguments'] : array();
			$expander = function(&$val) use($container) {
				if (substr($val, 0, 1) === '@') {
					$val = $container->getService(substr($val, 1));
				} elseif (is_string($val)) {
					$val = Nette\Utils\Strings::expand($val, $container->params);
				}
			};

			if (isset($definition['class']) || isset($definition['factory'])) {
				if (isset($definition['class'])) {
					$class = $definition['class'];
				} else {
					$class = NULL;
					array_unshift($arguments, $definition['factory']);
				}
				$methods = isset($definition['methods']) ? $definition['methods'] : array();
				$factory = function($container) use($class, $arguments, $methods, $expander) {
					array_walk_recursive($arguments, $expander);
					if ($class) {
						$class = Nette\Utils\Strings::expand($class, $container->params);
						if ($arguments) {
							$service = Nette\Reflection\ClassType::from($class)->newInstanceArgs($arguments);
						} else {
							$service = new $class;
						}
					} else {
						$factory = $arguments[0]; $arguments[0] = $container;
						$service = call_user_func_array($factory, $arguments);
					}

					array_walk_recursive($methods, $expander);
					foreach ($methods as $method) {
						call_user_func_array(array($service, $method[0]), isset($method[1]) ? $method[1] : array());
					}

					return $service;
				};

			} elseif (isset($definition['alias'])) {
				$factory = function($container) use($definition) {
					return $container->getService($definition['alias']);
				};
			} else {
				throw new Nette\InvalidStateException("The definition of service '$name' is missing factory method.");
			}

			if (isset($definition['tags'])) {
				$tags = (array) $definition['tags'];
				array_walk_recursive($tags, $expander);
			} else {
				$tags = NULL;
			}
			$container->addService($name, $factory, $tags);
		}
	}

	function generateCode(array $definitions)
	{
		$code = '';
		foreach ($definitions as $name => $definition) {
			$name = $this->varExport($name);
			if (is_scalar($definition)) {
				if (substr($definition, 0, 1) === '@') {
					$definition = array('alias' => substr($definition, 1));
				} else {
					$factory = $this->varExport($definition);
					$code .= "\$container->addService($name, $factory);\n\n";
					continue;
				}
			}

			if (isset($definition['class']) || isset($definition['factory'])) {
				$arguments = $this->argsExport(isset($definition['arguments']) ? $definition['arguments'] : array());
				$factory = "function(\$container) {\n\t";
				$factory .= isset($definition['class'])
					? '$class = ' . $this->argsExport(array($definition['class'])) . '; $service = new $class(' . $arguments . ");\n"
					: "\$service = call_user_func(\n\t\t" . $this->argsExport(array($definition['factory']))
						. ",\n\t\t\$container" . ($arguments ? ",\n\t\t$arguments" : '') . "\n\t);\n";

				if (isset($definition['methods'])) {
					foreach ($definition['methods'] as $method) {
						$args = isset($method[1]) ? $this->argsExport($method[1]) : '';
						$factory .= "\t\$service->$method[0]($args);\n";
					}
				}
				$factory .= "\treturn \$service;\n}";

			} elseif (isset($definition['alias'])) {
				$factory = $this->varExport($definition['alias']);
				$factory = "function(\$container) {\n\treturn \$container->getService($factory);\n}";
			} else {
				throw new Nette\InvalidStateException("The definition of service '$name' is missing factory method.");
			}

			$tags = isset($definition['tags']) ? $this->argsExport(array($definition['tags'])) : 'NULL';
			$code .= "\$container->addService($name, $factory, $tags);\n\n";
		}
		return $code;
	}

	private function argsExport($args)
	{
		$args = implode(', ', array_map(array($this, 'varExport'), $args));
		$args = preg_replace("#(?<!\\\)'@(\w+)'#", '\$container->getService(\'$1\')', $args);
		$args = preg_replace("#(?<!\\\)'%([\w-]+)%'#", '\$container->params[\'$1\']', $args);
		$args = preg_replace("#(?<!\\\)'(?:[^'\\\]|\\\.)*%(?:[^'\\\]|\\\.)*'#", 'Nette\Utils\Strings::expand($0, \$container->params)', $args);
		return $args;
	}

	private function varExport($arg)
	{
		return preg_replace('#\n *#', ' ', var_export($arg, TRUE));
	}

}

class MissingServiceException extends Nette\InvalidStateException
{
}

class AmbiguousServiceException extends Nette\InvalidStateException
{
}

class ServiceBuilder extends Nette\Object implements IServiceBuilder
{

	private $class;

	function __construct($class)
	{
		$this->class = $class;
	}

	function getClass()
	{
		return $this->class;
	}

	function createService(Nette\DI\IContainer $container)
	{
		if (!class_exists($this->class)) {
			throw new Nette\InvalidStateException("Cannot instantiate service, class '$this->class' not found.");
		}
		return new $this->class;
	}

}

}

namespace Nette\Forms {

use Nette;

class ControlGroup extends Nette\Object
{

	protected $controls;

	private $options = array();

	function __construct()
	{
		$this->controls = new \SplObjectStorage;
	}

	function add()
	{
		foreach (func_get_args() as $num => $item) {
			if ($item instanceof IControl) {
				$this->controls->attach($item);

			} elseif ($item instanceof \Traversable || is_array($item)) {
				foreach ($item as $control) {
					$this->controls->attach($control);
				}

			} else {
				throw new Nette\InvalidArgumentException("Only IFormControl items are allowed, the #$num parameter is invalid.");
			}
		}
		return $this;
	}

	function getControls()
	{
		return iterator_to_array($this->controls);
	}

	function setOption($key, $value)
	{
		if ($value === NULL) {
			unset($this->options[$key]);

		} else {
			$this->options[$key] = $value;
		}
		return $this;
	}

	final function getOption($key, $default = NULL)
	{
		return isset($this->options[$key]) ? $this->options[$key] : $default;
	}

	final function getOptions()
	{
		return $this->options;
	}

}

}

namespace Nette\Forms\Controls {

use Nette;use Nette\Forms\IControl;use Nette\Utils\Html;use Nette\Forms\Form;use Nette\Forms\Rule;

abstract class BaseControl extends Nette\ComponentModel\Component implements IControl
{

	public static $idMask = 'frm%s-%s';

	public $caption;

	protected $value;

	protected $control;

	protected $label;

	private $errors = array();

	private $disabled = FALSE;

	private $htmlId;

	private $htmlName;

	private $rules;

	private $translator = TRUE;

	private $options = array();

	function __construct($caption = NULL)
	{
		$this->monitor('Nette\Forms\Form');
		parent::__construct();
		$this->control = Html::el('input');
		$this->label = Html::el('label');
		$this->caption = $caption;
		$this->rules = new Nette\Forms\Rules($this);
	}

	protected function attached($form)
	{
		if (!$this->disabled && $form instanceof Form && $form->isAnchored() && $form->isSubmitted()) {
			$this->htmlName = NULL;
			$this->loadHttpData();
		}
	}

	function getForm($need = TRUE)
	{
		return $this->lookup('Nette\Forms\Form', $need);
	}

	function getHtmlName()
	{
		if ($this->htmlName === NULL) {
			$name = str_replace(self::NAME_SEPARATOR, '][', $this->lookupPath('Nette\Forms\Form'), $count);
			if ($count) {
				$name = substr_replace($name, '', strpos($name, ']'), 1) . ']';
			}
			if (is_numeric($name) || in_array($name, array('attributes','children','elements','focus','length','reset','style','submit','onsubmit'))) {
				$name .= '_';
			}
			$this->htmlName = $name;
		}
		return $this->htmlName;
	}

	function setHtmlId($id)
	{
		$this->htmlId = $id;
		return $this;
	}

	function getHtmlId()
	{
		if ($this->htmlId === FALSE) {
			return NULL;

		} elseif ($this->htmlId === NULL) {
			$this->htmlId = sprintf(self::$idMask, $this->getForm()->getName(), $this->lookupPath('Nette\Forms\Form'));
		}
		return $this->htmlId;
	}

	function setAttribute($name, $value = TRUE)
	{
		$this->control->$name = $value;
		return $this;
	}

	function setOption($key, $value)
	{
		if ($value === NULL) {
			unset($this->options[$key]);

		} else {
			$this->options[$key] = $value;
		}
		return $this;
	}

	final function getOption($key, $default = NULL)
	{
		return isset($this->options[$key]) ? $this->options[$key] : $default;
	}

	final function getOptions()
	{
		return $this->options;
	}

	function setTranslator(Nette\Localization\ITranslator $translator = NULL)
	{
		$this->translator = $translator;
		return $this;
	}

	final function getTranslator()
	{
		if ($this->translator === TRUE) {
			return $this->getForm(FALSE) ? $this->getForm()->getTranslator() : NULL;
		}
		return $this->translator;
	}

	function translate($s, $count = NULL)
	{
		$translator = $this->getTranslator();
		return $translator === NULL || $s == NULL ? $s : $translator->translate($s, $count);
	}

	function setValue($value)
	{
		$this->value = $value;
		return $this;
	}

	function getValue()
	{
		return $this->value;
	}

	function isFilled()
	{
		return (string) $this->getValue() !== '';
	}

	function setDefaultValue($value)
	{
		$form = $this->getForm(FALSE);
		if (!$form || !$form->isAnchored() || !$form->isSubmitted()) {
			$this->setValue($value);
		}
		return $this;
	}

	function loadHttpData()
	{
		$path = explode('[', strtr(str_replace(array('[]', ']'), '', $this->getHtmlName()), '.', '_'));
		$this->setValue(Nette\Utils\Arrays::get($this->getForm()->getHttpData(), $path, NULL));
	}

	function setDisabled($value = TRUE)
	{
		$this->disabled = (bool) $value;
		return $this;
	}

	function isDisabled()
	{
		return $this->disabled;
	}

	function getControl()
	{
		$this->setOption('rendered', TRUE);

		$control = clone $this->control;
		$control->name = $this->getHtmlName();
		$control->disabled = $this->disabled;
		$control->id = $this->getHtmlId();
		$control->required = $this->isRequired();

		$rules = self::exportRules($this->rules);
		$rules = substr(json_encode($rules), 1, -1);
		$rules = preg_replace('#"([a-z0-9]+)":#i', '$1:', $rules);
		$rules = preg_replace('#(?<!\\\\)"([^\\\\\',]*)"#i', "'$1'", $rules);
		$control->data('nette-rules', $rules ? $rules : NULL);

		return $control;
	}

	function getLabel($caption = NULL)
	{
		$label = clone $this->label;
		$label->for = $this->getHtmlId();
		if ($caption !== NULL) {
			$label->setText($this->translate($caption));

		} elseif ($this->caption instanceof Html) {
			$label->add($this->caption);

		} else {
			$label->setText($this->translate($this->caption));
		}
		return $label;
	}

	final function getControlPrototype()
	{
		return $this->control;
	}

	final function getLabelPrototype()
	{
		return $this->label;
	}

	function addRule($operation, $message = NULL, $arg = NULL)
	{
		$this->rules->addRule($operation, $message, $arg);
		return $this;
	}

	function addCondition($operation, $value = NULL)
	{
		return $this->rules->addCondition($operation, $value);
	}

	function addConditionOn(IControl $control, $operation, $value = NULL)
	{
		return $this->rules->addConditionOn($control, $operation, $value);
	}

	final function getRules()
	{
		return $this->rules;
	}

	final function setRequired($message = NULL)
	{
		return $this->addRule(Form::FILLED, $message);
	}

	final function isRequired()
	{
		foreach ($this->rules as $rule) {
			if ($rule->type === Rule::VALIDATOR && !$rule->isNegative && $rule->operation === Form::FILLED) {
				return TRUE;
			}
		}
		return FALSE;
	}

	private static function exportRules($rules)
	{
		$payload = array();
		foreach ($rules as $rule) {
			if (!is_string($op = $rule->operation)) {
				$op = callback($op);
				if (!$op->isStatic()) {
					continue;
				}
			}
			if ($rule->type === Rule::VALIDATOR) {
				$item = array('op' => ($rule->isNegative ? '~' : '') . $op, 'msg' => $rules->formatMessage($rule, FALSE));

			} elseif ($rule->type === Rule::CONDITION) {
				$item = array(
					'op' => ($rule->isNegative ? '~' : '') . $op,
					'rules' => self::exportRules($rule->subRules),
					'control' => $rule->control->getHtmlName()
				);
				if ($rule->subRules->getToggles()) {
					$item['toggle'] = $rule->subRules->getToggles();
				}
			}

			if (is_array($rule->arg)) {
				foreach ($rule->arg as $key => $value) {
					$item['arg'][$key] = $value instanceof IControl ? (object) array('control' => $value->getHtmlName()) : $value;
				}
			} elseif ($rule->arg !== NULL) {
				$item['arg'] = $rule->arg instanceof IControl ? (object) array('control' => $rule->arg->getHtmlName()) : $rule->arg;
			}

			$payload[] = $item;
		}
		return $payload;
	}

	static function validateEqual(IControl $control, $arg)
	{
		$value = $control->getValue();
		foreach ((is_array($value) ? $value : array($value)) as $val) {
			foreach ((is_array($arg) ? $arg : array($arg)) as $item) {
				if ((string) $val === (string) ($item instanceof IControl ? $item->value : $item)) {
					return TRUE;
				}
			}
		}
		return FALSE;
	}

	static function validateFilled(IControl $control)
	{
		return $control->isFilled();
	}

	static function validateValid(IControl $control)
	{
		return $control->rules->validate(TRUE);
	}

	function addError($message)
	{
		if (!in_array($message, $this->errors, TRUE)) {
			$this->errors[] = $message;
		}
		$this->getForm()->addError($message);
	}

	function getErrors()
	{
		return $this->errors;
	}

	function hasErrors()
	{
		return (bool) $this->errors;
	}

	function cleanErrors()
	{
		$this->errors = array();
	}

}

class Button extends BaseControl
{

	function __construct($caption = NULL)
	{
		parent::__construct($caption);
		$this->control->type = 'button';
	}

	function getLabel($caption = NULL)
	{
		return NULL;
	}

	function getControl($caption = NULL)
	{
		$control = parent::getControl();
		$control->value = $this->translate($caption === NULL ? $this->caption : $caption);
		return $control;
	}

}

class Checkbox extends BaseControl
{

	function __construct($label = NULL)
	{
		parent::__construct($label);
		$this->control->type = 'checkbox';
		$this->value = FALSE;
	}

	function setValue($value)
	{
		$this->value = is_scalar($value) ? (bool) $value : FALSE;
		return $this;
	}

	function getControl()
	{
		return parent::getControl()->checked($this->value);
	}

}

class HiddenField extends BaseControl
{

	private $forcedValue;

	function __construct($forcedValue = NULL)
	{
		parent::__construct();
		$this->control->type = 'hidden';
		$this->value = (string) $forcedValue;
		$this->forcedValue = $forcedValue;
	}

	function getLabel($caption = NULL)
	{
		return NULL;
	}

	function setValue($value)
	{
		$this->value = is_scalar($value) ? (string) $value : '';
		return $this;
	}

	function getControl()
	{
		return parent::getControl()
			->value($this->forcedValue === NULL ? $this->value : $this->forcedValue)
			->data('nette-rules', NULL);
	}

}

class SubmitButton extends Button implements Nette\Forms\ISubmitterControl
{

	public $onClick;

	public $onInvalidClick;

	private $validationScope = TRUE;

	function __construct($caption = NULL)
	{
		parent::__construct($caption);
		$this->control->type = 'submit';
	}

	function setValue($value)
	{
		if ($this->value = is_scalar($value) && (bool) $value) {
			$this->getForm()->setSubmittedBy($this);
		}
		return $this;
	}

	function isSubmittedBy()
	{
		return $this->getForm()->isSubmitted() === $this;
	}

	function setValidationScope($scope)
	{

		$this->validationScope = (bool) $scope;
		$this->control->formnovalidate = !$this->validationScope;
		return $this;
	}

	final function getValidationScope()
	{
		return $this->validationScope;
	}

	function click()
	{
		$this->onClick($this);
	}

	static function validateSubmitted(Nette\Forms\ISubmitterControl $control)
	{
		return $control->isSubmittedBy();
	}

}

class ImageButton extends SubmitButton
{

	function __construct($src = NULL, $alt = NULL)
	{
		parent::__construct();
		$this->control->type = 'image';
		$this->control->src = $src;
		$this->control->alt = $alt;
	}

	function getHtmlName()
	{
		$name = parent::getHtmlName();
		return strpos($name, '[') === FALSE ? $name : $name . '[]';
	}

	function loadHttpData()
	{
		$path = $this->getHtmlName();
		$path = explode('[', strtr(str_replace(']', '', strpos($path, '[') === FALSE ? $path . '.x' : substr($path, 0, -2)), '.', '_'));
		$this->setValue(Nette\Utils\Arrays::get($this->getForm()->getHttpData(), $path, NULL) !== NULL);
	}

}

class SelectBox extends BaseControl
{

	private $items = array();

	protected $allowed = array();

	private $prompt = FALSE;

	private $useKeys = TRUE;

	function __construct($label = NULL, array $items = NULL, $size = NULL)
	{
		parent::__construct($label);
		$this->control->setName('select');
		$this->control->size = $size > 1 ? (int) $size : NULL;
		if ($items !== NULL) {
			$this->setItems($items);
		}
	}

	function getValue()
	{
		$allowed = $this->allowed;
		if ($this->prompt) {
			$allowed = array_slice($allowed, 1, count($allowed), TRUE);
		}

		return is_scalar($this->value) && isset($allowed[$this->value]) ? $this->value : NULL;
	}

	function getRawValue()
	{
		return is_scalar($this->value) ? $this->value : NULL;
	}

	function isFilled()
	{
		$value = $this->getValue();
		return is_array($value) ? count($value) > 0 : $value !== NULL;
	}

	function setPrompt($prompt)
	{
		if (is_bool($prompt)) {
			$this->prompt = $prompt;
		} else {
			$this->prompt = TRUE;
			if ($prompt !== NULL) {
				$this->items = array('' => $prompt) + $this->items;
				$this->allowed = array('' => '') + $this->allowed;
			}
		}
		return $this;
	}

	function skipFirst($v = NULL)
	{
		trigger_error(__METHOD__ . '() is deprecated; use setPrompt() instead.', E_USER_WARNING);
		return $this->setPrompt($v);
	}

	final function getPrompt()
	{
		return $this->prompt;
	}

	final function areKeysUsed()
	{
		return $this->useKeys;
	}

	function setItems(array $items, $useKeys = TRUE)
	{
		$this->items = $items;
		$this->allowed = array();
		$this->useKeys = (bool) $useKeys;

		foreach ($items as $key => $value) {
			if (!is_array($value)) {
				$value = array($key => $value);
			}

			foreach ($value as $key2 => $value2) {
				if (!$this->useKeys) {
					if (!is_scalar($value2)) {
						throw new Nette\InvalidArgumentException("All items must be scalar.");
					}
					$key2 = $value2;
				}

				if (isset($this->allowed[$key2])) {
					throw new Nette\InvalidArgumentException("Items contain duplication for key '$key2'.");
				}

				$this->allowed[$key2] = $value2;
			}
		}
		return $this;
	}

	final function getItems()
	{
		return $this->items;
	}

	function getSelectedItem()
	{
		if (!$this->useKeys) {
			return $this->getValue();

		} else {
			$value = $this->getValue();
			return $value === NULL ? NULL : $this->allowed[$value];
		}
	}

	function getControl()
	{
		$control = parent::getControl();
		if ($this->prompt) {
			reset($this->items);
			$control->data('nette-empty-value', $this->useKeys ? key($this->items) : current($this->items));
		}
		$selected = $this->getValue();
		$selected = is_array($selected) ? array_flip($selected) : array($selected => TRUE);
		$option = Nette\Utils\Html::el('option');

		foreach ($this->items as $key => $value) {
			if (!is_array($value)) {
				$value = array($key => $value);
				$dest = $control;

			} else {
				$dest = $control->create('optgroup')->label($this->translate($key));
			}

			foreach ($value as $key2 => $value2) {
				if ($value2 instanceof Nette\Utils\Html) {
					$dest->add((string) $value2->selected(isset($selected[$key2])));

				} else {
					$key2 = $this->useKeys ? $key2 : $value2;
					$value2 = $this->translate((string) $value2);
					$dest->add((string) $option->value($key2 === $value2 ? NULL : $key2)
						->selected(isset($selected[$key2]))
						->setText($value2));
				}
			}
		}
		return $control;
	}

}

class MultiSelectBox extends SelectBox
{

	function getValue()
	{
		$allowed = array_keys($this->allowed);
		if ($this->getPrompt()) {
			unset($allowed[0]);
		}
		return array_intersect($this->getRawValue(), $allowed);
	}

	function getRawValue()
	{
		if (is_scalar($this->value)) {
			$value = array($this->value);

		} elseif (!is_array($this->value)) {
			$value = array();

		} else {
			$value = $this->value;
		}

		$res = array();
		foreach ($value as $val) {
			if (is_scalar($val)) {
				$res[] = $val;
			}
		}
		return $res;
	}

	function getSelectedItem()
	{
		if (!$this->areKeysUsed()) {
			return $this->getValue();

		} else {
			$res = array();
			foreach ($this->getValue() as $value) {
				$res[$value] = $this->allowed[$value];
			}
			return $res;
		}
	}

	function getHtmlName()
	{
		return parent::getHtmlName() . '[]';
	}

	function getControl()
	{
		$control = parent::getControl();
		$control->multiple = TRUE;
		return $control;
	}

}

class RadioList extends BaseControl
{

	protected $separator;

	protected $container;

	protected $items = array();

	function __construct($label = NULL, array $items = NULL)
	{
		parent::__construct($label);
		$this->control->type = 'radio';
		$this->container = Html::el();
		$this->separator = Html::el('br');
		if ($items !== NULL) {
			$this->setItems($items);
		}
	}

	function getValue($raw = FALSE)
	{
		return is_scalar($this->value) && ($raw || isset($this->items[$this->value])) ? $this->value : NULL;
	}

	function isFilled()
	{
		return $this->getValue() !== NULL;
	}

	function setItems(array $items)
	{
		$this->items = $items;
		return $this;
	}

	final function getItems()
	{
		return $this->items;
	}

	final function getSeparatorPrototype()
	{
		return $this->separator;
	}

	final function getContainerPrototype()
	{
		return $this->container;
	}

	function getControl($key = NULL)
	{
		if ($key === NULL) {
			$container = clone $this->container;
			$separator = (string) $this->separator;

		} elseif (!isset($this->items[$key])) {
			return NULL;
		}

		$control = parent::getControl();
		$id = $control->id;
		$counter = -1;
		$value = $this->value === NULL ? NULL : (string) $this->getValue();
		$label = Html::el('label');

		foreach ($this->items as $k => $val) {
			$counter++;
			if ($key !== NULL && (string) $key !== (string) $k) {
				continue;
			}

			$control->id = $label->for = $id . '-' . $counter;
			$control->checked = (string) $k === $value;
			$control->value = $k;

			if ($val instanceof Html) {
				$label->setHtml($val);
			} else {
				$label->setText($this->translate((string) $val));
			}

			if ($key !== NULL) {
				return Html::el()->add($control)->add($label);
			}

			$container->add((string) $control . (string) $label . $separator);
			$control->data('nette-rules', NULL);

		}

		return $container;
	}

	function getLabel($caption = NULL)
	{
		$label = parent::getLabel($caption);
		$label->for = NULL;
		return $label;
	}

}

use Nette\Utils\Strings;

abstract class TextBase extends BaseControl
{

	protected $emptyValue = '';

	protected $filters = array();

	function setValue($value)
	{
		$this->value = is_scalar($value) ? (string) $value : '';
		return $this;
	}

	function getValue()
	{
		$value = $this->value;
		foreach ($this->filters as $filter) {
			$value = (string) $filter($value);
		}
		return $value === $this->translate($this->emptyValue) ? '' : $value;
	}

	function setEmptyValue($value)
	{
		$this->emptyValue = (string) $value;
		return $this;
	}

	final function getEmptyValue()
	{
		return $this->emptyValue;
	}

	function addFilter($filter)
	{
		$this->filters[] = callback($filter);
		return $this;
	}

	function getControl()
	{
		$control = parent::getControl();
		foreach ($this->getRules() as $rule) {
			if ($rule->type === Nette\Forms\Rule::VALIDATOR && !$rule->isNegative
				&& ($rule->operation === Form::LENGTH || $rule->operation === Form::MAX_LENGTH)
			) {
				$control->maxlength = is_array($rule->arg) ? $rule->arg[1] : $rule->arg;
			}
		}
		if ($this->emptyValue !== '') {
			$control->data('nette-empty-value', $this->translate($this->emptyValue));
		}
		return $control;
	}

	function addRule($operation, $message = NULL, $arg = NULL)
	{
		if ($operation === Form::FLOAT) {
			$this->addFilter(callback(__CLASS__, 'filterFloat'));
		}
		return parent::addRule($operation, $message, $arg);
	}

	static function validateMinLength(TextBase $control, $length)
	{
		return Strings::length($control->getValue()) >= $length;
	}

	static function validateMaxLength(TextBase $control, $length)
	{
		return Strings::length($control->getValue()) <= $length;
	}

	static function validateLength(TextBase $control, $range)
	{
		if (!is_array($range)) {
			$range = array($range, $range);
		}
		$len = Strings::length($control->getValue());
		return ($range[0] === NULL || $len >= $range[0]) && ($range[1] === NULL || $len <= $range[1]);
	}

	static function validateEmail(TextBase $control)
	{
		$atom = "[-a-z0-9!#$%&'*+/=?^_`{|}~]";
		$localPart = "(?:\"(?:[ !\\x23-\\x5B\\x5D-\\x7E]*|\\\\[ -~])+\"|$atom+(?:\\.$atom+)*)";
		$chars = "a-z0-9\x80-\xFF";
		$domain = "[$chars](?:[-$chars]{0,61}[$chars])";
		return (bool) Strings::match($control->getValue(), "(^$localPart@(?:$domain?\\.)+[-$chars]{2,19}\\z)i");
	}

	static function validateUrl(TextBase $control)
	{
		$chars = "a-z0-9\x80-\xFF";
		return (bool) Strings::match(
			$control->getValue(),
			"#^(?:https?://|)(?:[$chars](?:[-$chars]{0,61}[$chars])?\\.)+[-$chars]{2,19}(/\S*)?$#i"
		);
	}

	static function validateRegexp(TextBase $control, $regexp)
	{
		return (bool) Strings::match($control->getValue(), $regexp);
	}

	static function validatePattern(TextBase $control, $pattern)
	{
		return (bool) Strings::match($control->getValue(), "\x01^($pattern)$\x01u");
	}

	static function validateInteger(TextBase $control)
	{
		return (bool) Strings::match($control->getValue(), '/^-?[0-9]+$/');
	}

	static function validateFloat(TextBase $control)
	{
		return (bool) Strings::match($control->getValue(), '/^-?[0-9]*[.,]?[0-9]+$/');
	}

	static function validateRange(TextBase $control, $range)
	{
		return ($range[0] === NULL || $control->getValue() >= $range[0])
			&& ($range[1] === NULL || $control->getValue() <= $range[1]);
	}

	static function filterFloat($s)
	{
		return str_replace(array(' ', ','), array('', '.'), $s);
	}

}

class TextArea extends TextBase
{

	function __construct($label = NULL, $cols = NULL, $rows = NULL)
	{
		parent::__construct($label);
		$this->control->setName('textarea');
		$this->control->cols = $cols;
		$this->control->rows = $rows;
		$this->value = '';
	}

	function getControl()
	{
		$control = parent::getControl();
		$control->setText($this->getValue() === '' ? $this->translate($this->emptyValue) : $this->value);
		return $control;
	}

}

class TextInput extends TextBase
{

	function __construct($label = NULL, $cols = NULL, $maxLength = NULL)
	{
		parent::__construct($label);
		$this->control->type = 'text';
		$this->control->size = $cols;
		$this->control->maxlength = $maxLength;
		$this->filters[] = callback($this, 'sanitize');
		$this->value = '';
	}

	function sanitize($value)
	{
		if ($this->control->maxlength && Nette\Utils\Strings::length($value) > $this->control->maxlength) {
			$value = iconv_substr($value, 0, $this->control->maxlength, 'UTF-8');
		}
		return Nette\Utils\Strings::trim(strtr($value, "\r\n", '  '));
	}

	function setType($type)
	{
		$this->control->type = $type;
		return $this;
	}

	function setPasswordMode($mode = TRUE)
	{
		$this->control->type = $mode ? 'password' : 'text';
		return $this;
	}

	function getControl()
	{
		$control = parent::getControl();
		foreach ($this->getRules() as $rule) {
			if ($rule->isNegative || $rule->type !== Nette\Forms\Rule::VALIDATOR) {

			} elseif ($rule->operation === Nette\Forms\Form::RANGE && $control->type !== 'text') {
				list($control->min, $control->max) = $rule->arg;

			} elseif ($rule->operation === Nette\Forms\Form::PATTERN) {
				$control->pattern = $rule->arg;
			}
		}
		if ($control->type !== 'password') {
			$control->value = $this->getValue() === '' ? $this->translate($this->emptyValue) : $this->value;
		}
		return $control;
	}

}

use Nette\Http;

class UploadControl extends BaseControl
{

	function __construct($label = NULL)
	{
		parent::__construct($label);
		$this->control->type = 'file';
	}

	protected function attached($form)
	{
		if ($form instanceof Nette\Forms\Form) {
			if ($form->getMethod() !== Nette\Forms\Form::POST) {
				throw new Nette\InvalidStateException('File upload requires method POST.');
			}
			$form->getElementPrototype()->enctype = 'multipart/form-data';
		}
		parent::attached($form);
	}

	function setValue($value)
	{
		if (is_array($value)) {
			$this->value = new Http\FileUpload($value);

		} elseif ($value instanceof Http\FileUpload) {
			$this->value = $value;

		} else {
			$this->value = new Http\FileUpload(NULL);
		}
		return $this;
	}

	function isFilled()
	{
		return $this->value instanceof Http\FileUpload && $this->value->isOK();
	}

	static function validateFileSize(UploadControl $control, $limit)
	{
		$file = $control->getValue();
		return $file instanceof Http\FileUpload && $file->getSize() <= $limit;
	}

	static function validateMimeType(UploadControl $control, $mimeType)
	{
		$file = $control->getValue();
		if ($file instanceof Http\FileUpload) {
			$type = strtolower($file->getContentType());
			$mimeTypes = is_array($mimeType) ? $mimeType : explode(',', $mimeType);
			if (in_array($type, $mimeTypes, TRUE)) {
				return TRUE;
			}
			if (in_array(preg_replace('#/.*#', '/*', $type), $mimeTypes, TRUE)) {
				return TRUE;
			}
		}
		return FALSE;
	}

	static function validateImage(UploadControl $control)
	{
		$file = $control->getValue();
		return $file instanceof Http\FileUpload && $file->isImage();
	}

}

}

namespace Nette\Forms\Rendering {

use Nette;use Nette\Utils\Html;

class DefaultFormRenderer extends Nette\Object implements Nette\Forms\IFormRenderer
{

	public $wrappers = array(
		'form' => array(
			'container' => NULL,
			'errors' => TRUE,
		),

		'error' => array(
			'container' => 'ul class=error',
			'item' => 'li',
		),

		'group' => array(
			'container' => 'fieldset',
			'label' => 'legend',
			'description' => 'p',
		),

		'controls' => array(
			'container' => 'table',
		),

		'pair' => array(
			'container' => 'tr',
			'.required' => 'required',
			'.optional' => NULL,
			'.odd' => NULL,
		),

		'control' => array(
			'container' => 'td',
			'.odd' => NULL,

			'errors' => FALSE,
			'description' => 'small',
			'requiredsuffix' => '',

			'.required' => 'required',
			'.text' => 'text',
			'.password' => 'text',
			'.file' => 'text',
			'.submit' => 'button',
			'.image' => 'imagebutton',
			'.button' => 'button',
		),

		'label' => array(
			'container' => 'th',
			'suffix' => NULL,
			'requiredsuffix' => '',
		),

		'hidden' => array(
			'container' => 'div',
		),
	);

	protected $form;

	protected $counter;

	function render(Nette\Forms\Form $form, $mode = NULL)
	{
		if ($this->form !== $form) {
			$this->form = $form;
			$this->init();
		}

		$s = '';
		if (!$mode || $mode === 'begin') {
			$s .= $this->renderBegin();
		}
		if ((!$mode && $this->getValue('form errors')) || $mode === 'errors') {
			$s .= $this->renderErrors();
		}
		if (!$mode || $mode === 'body') {
			$s .= $this->renderBody();
		}
		if (!$mode || $mode === 'end') {
			$s .= $this->renderEnd();
		}
		return $s;
	}

	function setClientScript()
	{
		trigger_error(__METHOD__ . '() is deprecated; use unobstructive JavaScript instead.', E_USER_WARNING);
		return $this;
	}

	protected function init()
	{

		$wrapper = & $this->wrappers['control'];
		foreach ($this->form->getControls() as $control) {
			if ($control->isRequired() && isset($wrapper['.required'])) {
				$control->getLabelPrototype()->class($wrapper['.required'], TRUE);
			}

			$el = $control->getControlPrototype();
			if ($el->getName() === 'input' && isset($wrapper['.' . $el->type])) {
				$el->class($wrapper['.' . $el->type], TRUE);
			}
		}
	}

	function renderBegin()
	{
		$this->counter = 0;

		foreach ($this->form->getControls() as $control) {
			$control->setOption('rendered', FALSE);
		}

		if (strcasecmp($this->form->getMethod(), 'get') === 0) {
			$el = clone $this->form->getElementPrototype();
			$url = explode('?', (string) $el->action, 2);
			$el->action = $url[0];
			$s = '';
			if (isset($url[1])) {
				foreach (preg_split('#[;&]#', $url[1]) as $param) {
					$parts = explode('=', $param, 2);
					$name = urldecode($parts[0]);
					if (!isset($this->form[$name])) {
						$s .= Html::el('input', array('type' => 'hidden', 'name' => $name, 'value' => urldecode($parts[1])));
					}
				}
				$s = "\n\t" . $this->getWrapper('hidden container')->setHtml($s);
			}
			return $el->startTag() . $s;

		} else {
			return $this->form->getElementPrototype()->startTag();
		}
	}

	function renderEnd()
	{
		$s = '';
		foreach ($this->form->getControls() as $control) {
			if ($control instanceof Nette\Forms\Controls\HiddenField && !$control->getOption('rendered')) {
				$s .= (string) $control->getControl();
			}
		}
		if (iterator_count($this->form->getComponents(TRUE, 'Nette\Forms\Controls\TextInput')) < 2) {
			$s .= '<!--[if IE]><input type=IEbug disabled style="display:none"><![endif]-->';
		}
		if ($s) {
			$s = $this->getWrapper('hidden container')->setHtml($s) . "\n";
		}

		return $s . $this->form->getElementPrototype()->endTag() . "\n";
	}

	function renderErrors(Nette\Forms\IControl $control = NULL)
	{
		$errors = $control === NULL ? $this->form->getErrors() : $control->getErrors();
		if (count($errors)) {
			$ul = $this->getWrapper('error container');
			$li = $this->getWrapper('error item');

			foreach ($errors as $error) {
				$item = clone $li;
				if ($error instanceof Html) {
					$item->add($error);
				} else {
					$item->setText($error);
				}
				$ul->add($item);
			}
			return "\n" . $ul->render(0);
		}
	}

	function renderBody()
	{
		$s = $remains = '';

		$defaultContainer = $this->getWrapper('group container');
		$translator = $this->form->getTranslator();

		foreach ($this->form->getGroups() as $group) {
			if (!$group->getControls() || !$group->getOption('visual')) {
				continue;
			}

			$container = $group->getOption('container', $defaultContainer);
			$container = $container instanceof Html ? clone $container : Html::el($container);

			$s .= "\n" . $container->startTag();

			$text = $group->getOption('label');
			if ($text instanceof Html) {
				$s .= $text;

			} elseif (is_string($text)) {
				if ($translator !== NULL) {
					$text = $translator->translate($text);
				}
				$s .= "\n" . $this->getWrapper('group label')->setText($text) . "\n";
			}

			$text = $group->getOption('description');
			if ($text instanceof Html) {
				$s .= $text;

			} elseif (is_string($text)) {
				if ($translator !== NULL) {
					$text = $translator->translate($text);
				}
				$s .= $this->getWrapper('group description')->setText($text) . "\n";
			}

			$s .= $this->renderControls($group);

			$remains = $container->endTag() . "\n" . $remains;
			if (!$group->getOption('embedNext')) {
				$s .= $remains;
				$remains = '';
			}
		}

		$s .= $remains . $this->renderControls($this->form);

		$container = $this->getWrapper('form container');
		$container->setHtml($s);
		return $container->render(0);
	}

	function renderControls($parent)
	{
		if (!($parent instanceof Nette\Forms\Container || $parent instanceof Nette\Forms\ControlGroup)) {
			throw new Nette\InvalidArgumentException("Argument must be FormContainer or FormGroup instance.");
		}

		$container = $this->getWrapper('controls container');

		$buttons = NULL;
		foreach ($parent->getControls() as $control) {
			if ($control->getOption('rendered') || $control instanceof Nette\Forms\Controls\HiddenField || $control->getForm(FALSE) !== $this->form) {

			} elseif ($control instanceof Nette\Forms\Controls\Button) {
				$buttons[] = $control;

			} else {
				if ($buttons) {
					$container->add($this->renderPairMulti($buttons));
					$buttons = NULL;
				}
				$container->add($this->renderPair($control));
			}
		}

		if ($buttons) {
			$container->add($this->renderPairMulti($buttons));
		}

		$s = '';
		if (count($container)) {
			$s .= "\n" . $container . "\n";
		}

		return $s;
	}

	function renderPair(Nette\Forms\IControl $control)
	{
		$pair = $this->getWrapper('pair container');
		$pair->add($this->renderLabel($control));
		$pair->add($this->renderControl($control));
		$pair->class($this->getValue($control->isRequired() ? 'pair .required' : 'pair .optional'), TRUE);
		$pair->class($control->getOption('class'), TRUE);
		if (++$this->counter % 2) {
			$pair->class($this->getValue('pair .odd'), TRUE);
		}
		$pair->id = $control->getOption('id');
		return $pair->render(0);
	}

	function renderPairMulti(array $controls)
	{
		$s = array();
		foreach ($controls as $control) {
			if (!$control instanceof Nette\Forms\IControl) {
				throw new Nette\InvalidArgumentException("Argument must be array of IFormControl instances.");
			}
			$s[] = (string) $control->getControl();
		}
		$pair = $this->getWrapper('pair container');
		$pair->add($this->renderLabel($control));
		$pair->add($this->getWrapper('control container')->setHtml(implode(" ", $s)));
		return $pair->render(0);
	}

	function renderLabel(Nette\Forms\IControl $control)
	{
		$head = $this->getWrapper('label container');

		if ($control instanceof Nette\Forms\Controls\Checkbox || $control instanceof Nette\Forms\Controls\Button) {
			return $head->setHtml(($head->getName() === 'td' || $head->getName() === 'th') ? '&nbsp;' : '');

		} else {
			$label = $control->getLabel();
			$suffix = $this->getValue('label suffix') . ($control->isRequired() ? $this->getValue('label requiredsuffix') : '');
			if ($label instanceof Html) {
				$label->setHtml($label->getHtml() . $suffix);
				$suffix = '';
			}
			return $head->setHtml((string) $label . $suffix);
		}
	}

	function renderControl(Nette\Forms\IControl $control)
	{
		$body = $this->getWrapper('control container');
		if ($this->counter % 2) {
			$body->class($this->getValue('control .odd'), TRUE);
		}

		$description = $control->getOption('description');
		if ($description instanceof Html) {
			$description = ' ' . $control->getOption('description');

		} elseif (is_string($description)) {
			$description = ' ' . $this->getWrapper('control description')->setText($control->translate($description));

		} else {
			$description = '';
		}

		if ($control->isRequired()) {
			$description = $this->getValue('control requiredsuffix') . $description;
		}

		if ($this->getValue('control errors')) {
			$description .= $this->renderErrors($control);
		}

		if ($control instanceof Nette\Forms\Controls\Checkbox || $control instanceof Nette\Forms\Controls\Button) {
			return $body->setHtml((string) $control->getControl() . (string) $control->getLabel() . $description);

		} else {
			return $body->setHtml((string) $control->getControl() . $description);
		}
	}

	protected function getWrapper($name)
	{
		$data = $this->getValue($name);
		return $data instanceof Html ? clone $data : Html::el($data);
	}

	protected function getValue($name)
	{
		$name = explode(' ', $name);
		$data = & $this->wrappers[$name[0]][$name[1]];
		return $data;
	}

}

}

namespace Nette\Forms {

use Nette;

final class Rule extends Nette\Object
{

	const CONDITION = 1;

	const VALIDATOR = 2;

	const FILTER = 3;

	public $control;

	public $operation;

	public $arg;

	public $type;

	public $isNegative = FALSE;

	public $message;

	public $subRules;

}

final class Rules extends Nette\Object implements \IteratorAggregate
{

	const VALIDATE_PREFIX = 'validate';

	public static $defaultMessages = array(
		Form::PROTECTION => 'Security token did not match. Possible CSRF attack.',
		Form::EQUAL => 'Please enter %s.',
		Form::FILLED => 'Please complete mandatory field.',
		Form::MIN_LENGTH => 'Please enter a value of at least %d characters.',
		Form::MAX_LENGTH => 'Please enter a value no longer than %d characters.',
		Form::LENGTH => 'Please enter a value between %d and %d characters long.',
		Form::EMAIL => 'Please enter a valid email address.',
		Form::URL => 'Please enter a valid URL.',
		Form::INTEGER => 'Please enter a numeric value.',
		Form::FLOAT => 'Please enter a numeric value.',
		Form::RANGE => 'Please enter a value between %d and %d.',
		Form::MAX_FILE_SIZE => 'The size of the uploaded file can be up to %d bytes.',
		Form::IMAGE => 'The uploaded file must be image in format JPEG, GIF or PNG.',
	);

	private $rules = array();

	private $parent;

	private $toggles = array();

	private $control;

	function __construct(IControl $control)
	{
		$this->control = $control;
	}

	function addRule($operation, $message = NULL, $arg = NULL)
	{
		$rule = new Rule;
		$rule->control = $this->control;
		$rule->operation = $operation;
		$this->adjustOperation($rule);
		$rule->arg = $arg;
		$rule->type = Rule::VALIDATOR;
		if ($message === NULL && is_string($rule->operation) && isset(self::$defaultMessages[$rule->operation])) {
			$rule->message = self::$defaultMessages[$rule->operation];
		} else {
			$rule->message = $message;
		}
		$this->rules[] = $rule;
		return $this;
	}

	function addCondition($operation, $arg = NULL)
	{
		return $this->addConditionOn($this->control, $operation, $arg);
	}

	function addConditionOn(IControl $control, $operation, $arg = NULL)
	{
		$rule = new Rule;
		$rule->control = $control;
		$rule->operation = $operation;
		$this->adjustOperation($rule);
		$rule->arg = $arg;
		$rule->type = Rule::CONDITION;
		$rule->subRules = new static($this->control);
		$rule->subRules->parent = $this;

		$this->rules[] = $rule;
		return $rule->subRules;
	}

	function elseCondition()
	{
		$rule = clone end($this->parent->rules);
		$rule->isNegative = !$rule->isNegative;
		$rule->subRules = new static($this->parent->control);
		$rule->subRules->parent = $this->parent;
		$this->parent->rules[] = $rule;
		return $rule->subRules;
	}

	function endCondition()
	{
		return $this->parent;
	}

	function toggle($id, $hide = TRUE)
	{
		$this->toggles[$id] = $hide;
		return $this;
	}

	function validate($onlyCheck = FALSE)
	{
		foreach ($this->rules as $rule) {
			if ($rule->control->isDisabled()) {
				continue;
			}

			$success = ($rule->isNegative xor $this->getCallback($rule)->invoke($rule->control, $rule->arg));

			if ($rule->type === Rule::CONDITION && $success) {
				if (!$rule->subRules->validate($onlyCheck)) {
					return FALSE;
				}

			} elseif ($rule->type === Rule::VALIDATOR && !$success) {
				if (!$onlyCheck) {
					$rule->control->addError(self::formatMessage($rule, TRUE));
				}
				return FALSE;
			}
		}
		return TRUE;
	}

	final function getIterator()
	{
		return new \ArrayIterator($this->rules);
	}

	final function getToggles()
	{
		return $this->toggles;
	}

	private function adjustOperation($rule)
	{
		if (is_string($rule->operation) && ord($rule->operation[0]) > 127) {
			$rule->isNegative = TRUE;
			$rule->operation = ~$rule->operation;
		}

		if (!$this->getCallback($rule)->isCallable()) {
			$operation = is_scalar($rule->operation) ? " '$rule->operation'" : '';
			throw new Nette\InvalidArgumentException("Unknown operation$operation for control '{$rule->control->name}'.");
		}
	}

	private function getCallback($rule)
	{
		$op = $rule->operation;
		if (is_string($op) && strncmp($op, ':', 1) === 0) {
			return callback(get_class($rule->control), self::VALIDATE_PREFIX . ltrim($op, ':'));
		} else {
			return callback($op);
		}
	}

	static function formatMessage($rule, $withValue)
	{
		$message = $rule->message;
		if (!isset($message)) {
			$message = self::$defaultMessages[$rule->operation];
		}
		if ($translator = $rule->control->getForm()->getTranslator()) {
			$message = $translator->translate($message, is_int($rule->arg) ? $rule->arg : NULL);
		}
		$message = vsprintf(preg_replace('#%(name|label|value)#', '%$0', $message), (array) $rule->arg);
		$message = str_replace('%name', $rule->control->getName(), $message);
		$message = str_replace('%label', $rule->control->translate($rule->control->caption), $message);
		if ($withValue && strpos($message, '%value') !== FALSE) {
			$message = str_replace('%value', $rule->control->getValue(), $message);
		}
		return $message;
	}

}

}

namespace Nette\Http {

use Nette;

class Context extends Nette\Object
{

	private $request;

	private $response;

	function __construct(IRequest $request, IResponse $response)
	{
		$this->request = $request;
		$this->response = $response;
	}

	function isModified($lastModified = NULL, $etag = NULL)
	{
		if ($lastModified) {
			$this->response->setHeader('Last-Modified', $this->response->date($lastModified));
		}
		if ($etag) {
			$this->response->setHeader('ETag', '"' . addslashes($etag) . '"');
		}

		$ifNoneMatch = $this->request->getHeader('If-None-Match');
		if ($ifNoneMatch === '*') {
			$match = TRUE;

		} elseif ($ifNoneMatch !== NULL) {
			$etag = $this->response->getHeader('ETag');

			if ($etag == NULL || strpos(' ' . strtr($ifNoneMatch, ",\t", '  '), ' ' . $etag) === FALSE) {
				return TRUE;

			} else {
				$match = TRUE;
			}
		}

		$ifModifiedSince = $this->request->getHeader('If-Modified-Since');
		if ($ifModifiedSince !== NULL) {
			$lastModified = $this->response->getHeader('Last-Modified');
			if ($lastModified != NULL && strtotime($lastModified) <= strtotime($ifModifiedSince)) {
				$match = TRUE;

			} else {
				return TRUE;
			}
		}

		if (empty($match)) {
			return TRUE;
		}

		$this->response->setCode(IResponse::S304_NOT_MODIFIED);
		return FALSE;
	}

	function getRequest()
	{
		return $this->request;
	}

	function getResponse()
	{
		return $this->response;
	}

}

class FileUpload extends Nette\Object
{

	private $name;

	private $type;

	private $size;

	private $tmpName;

	private $error;

	function __construct($value)
	{
		foreach (array('name', 'type', 'size', 'tmp_name', 'error') as $key) {
			if (!isset($value[$key]) || !is_scalar($value[$key])) {
				$this->error = UPLOAD_ERR_NO_FILE;
				return;
			}
		}
		$this->name = $value['name'];
		$this->size = $value['size'];
		$this->tmpName = $value['tmp_name'];
		$this->error = $value['error'];
	}

	function getName()
	{
		return $this->name;
	}

	function getSanitizedName()
	{
		return trim(Nette\Utils\Strings::webalize($this->name, '.', FALSE), '.-');
	}

	function getContentType()
	{
		if ($this->isOk() && $this->type === NULL) {
			$this->type = Nette\Utils\MimeTypeDetector::fromFile($this->tmpName);
		}
		return $this->type;
	}

	function getSize()
	{
		return $this->size;
	}

	function getTemporaryFile()
	{
		return $this->tmpName;
	}

	function __toString()
	{
		return $this->tmpName;
	}

	function getError()
	{
		return $this->error;
	}

	function isOk()
	{
		return $this->error === UPLOAD_ERR_OK;
	}

	function move($dest)
	{
		$dir = dirname($dest);
		if (@mkdir($dir, 0755, TRUE)) {
			chmod($dir, 0755);
		}
		$func = is_uploaded_file($this->tmpName) ? 'move_uploaded_file' : 'rename';
		if (!$func($this->tmpName, $dest)) {
			throw new Nette\InvalidStateException("Unable to move uploaded file '$this->tmpName' to '$dest'.");
		}
		chmod($dest, 0644);
		$this->tmpName = $dest;
		return $this;
	}

	function isImage()
	{
		return in_array($this->getContentType(), array('image/gif', 'image/png', 'image/jpeg'), TRUE);
	}

	function toImage()
	{
		return Nette\Image::fromFile($this->tmpName);
	}

	function getImageSize()
	{
		return $this->isOk() ? @getimagesize($this->tmpName) : NULL;
	}

	function getContents()
	{

		return $this->isOk() ? file_get_contents($this->tmpName) : NULL;
	}

}

class Request extends Nette\Object implements IRequest
{

	private $method;

	private $url;

	private $query;

	private $post;

	private $files;

	private $cookies;

	private $headers;

	private $remoteAddress;

	private $remoteHost;

	function __construct(UrlScript $url, $query = NULL, $post = NULL, $files = NULL, $cookies = NULL,
		$headers = NULL, $method = NULL, $remoteAddress = NULL, $remoteHost = NULL)
	{
		$this->url = $url;
		$this->url->freeze();
		if ($query === NULL) {
			parse_str($url->query, $this->query);
		} else {
			$this->query = (array) $query;
		}
		$this->post = (array) $post;
		$this->files = (array) $files;
		$this->cookies = (array) $cookies;
		$this->headers = (array) $headers;
		$this->method = $method;
		$this->remoteAddress = $remoteAddress;
		$this->remoteHost = $remoteHost;
	}

	final function getUrl()
	{
		return $this->url;
	}

	function getUri()
	{
		trigger_error(__METHOD__ . '() is deprecated; use ' . __CLASS__ . '::getUrl() instead.', E_USER_WARNING);
		return $this->getUrl();
	}

	final function getQuery($key = NULL, $default = NULL)
	{
		if (func_num_args() === 0) {
			return $this->query;

		} elseif (isset($this->query[$key])) {
			return $this->query[$key];

		} else {
			return $default;
		}
	}

	final function getPost($key = NULL, $default = NULL)
	{
		if (func_num_args() === 0) {
			return $this->post;

		} elseif (isset($this->post[$key])) {
			return $this->post[$key];

		} else {
			return $default;
		}
	}

	final function getFile($key)
	{
		$args = func_get_args();
		return Nette\Utils\Arrays::get($this->files, $args, NULL);
	}

	final function getFiles()
	{
		return $this->files;
	}

	final function getCookie($key, $default = NULL)
	{
		if (func_num_args() === 0) {
			return $this->cookies;

		} elseif (isset($this->cookies[$key])) {
			return $this->cookies[$key];

		} else {
			return $default;
		}
	}

	final function getCookies()
	{
		return $this->cookies;
	}

	function getMethod()
	{
		return $this->method;
	}

	function isMethod($method)
	{
		return strcasecmp($this->method, $method) === 0;
	}

	function isPost()
	{
		return $this->isMethod('POST');
	}

	final function getHeader($header, $default = NULL)
	{
		$header = strtolower($header);
		if (isset($this->headers[$header])) {
			return $this->headers[$header];
		} else {
			return $default;
		}
	}

	function getHeaders()
	{
		return $this->headers;
	}

	final function getReferer()
	{
		return isset($this->headers['referer']) ? new Url($this->headers['referer']) : NULL;
	}

	function isSecured()
	{
		return $this->url->scheme === 'https';
	}

	function isAjax()
	{
		return $this->getHeader('X-Requested-With') === 'XMLHttpRequest';
	}

	function getRemoteAddress()
	{
		return $this->remoteAddress;
	}

	function getRemoteHost()
	{
		if (!$this->remoteHost) {
			$this->remoteHost = $this->remoteAddress ? getHostByAddr($this->remoteAddress) : NULL;
		}
		return $this->remoteHost;
	}

	function detectLanguage(array $langs)
	{
		$header = $this->getHeader('Accept-Language');
		if (!$header) {
			return NULL;
		}

		$s = strtolower($header);
		$s = strtr($s, '_', '-');
		rsort($langs);
		preg_match_all('#(' . implode('|', $langs) . ')(?:-[^\s,;=]+)?\s*(?:;\s*q=([0-9.]+))?#', $s, $matches);

		if (!$matches[0]) {
			return NULL;
		}

		$max = 0;
		$lang = NULL;
		foreach ($matches[1] as $key => $value) {
			$q = $matches[2][$key] === '' ? 1.0 : (float) $matches[2][$key];
			if ($q > $max) {
				$max = $q; $lang = $value;
			}
		}

		return $lang;
	}

}

use Nette\Utils\Strings;

class RequestFactory extends Nette\Object
{

	const NONCHARS = '#[^\x09\x0A\x0D\x20-\x7E\xA0-\x{10FFFF}]#u';

	public $urlFilters = array(
		'path' => array('#/{2,}#' => '/'),
		'url' => array(),
	);

	private $encoding;

	function setEncoding($encoding)
	{
		$this->encoding = $encoding;
		return $this;
	}

	function createHttpRequest()
	{

		$url = new UrlScript;
		$url->scheme = isset($_SERVER['HTTPS']) && strcasecmp($_SERVER['HTTPS'], 'off') ? 'https' : 'http';
		$url->user = isset($_SERVER['PHP_AUTH_USER']) ? $_SERVER['PHP_AUTH_USER'] : '';
		$url->password = isset($_SERVER['PHP_AUTH_PW']) ? $_SERVER['PHP_AUTH_PW'] : '';

		if (isset($_SERVER['HTTP_HOST'])) {
			$pair = explode(':', $_SERVER['HTTP_HOST']);

		} elseif (isset($_SERVER['SERVER_NAME'])) {
			$pair = explode(':', $_SERVER['SERVER_NAME']);

		} else {
			$pair = array('');
		}

		$url->host = preg_match('#^[-._a-z0-9]+$#', $pair[0]) ? $pair[0] : '';

		if (isset($pair[1])) {
			$url->port = (int) $pair[1];

		} elseif (isset($_SERVER['SERVER_PORT'])) {
			$url->port = (int) $_SERVER['SERVER_PORT'];
		}

		if (isset($_SERVER['REQUEST_URI'])) {
			$requestUrl = $_SERVER['REQUEST_URI'];

		} elseif (isset($_SERVER['ORIG_PATH_INFO'])) {
			$requestUrl = $_SERVER['ORIG_PATH_INFO'];
			if (isset($_SERVER['QUERY_STRING']) && $_SERVER['QUERY_STRING'] != '') {
				$requestUrl .= '?' . $_SERVER['QUERY_STRING'];
			}
		} else {
			$requestUrl = '';
		}

		$requestUrl = Strings::replace($requestUrl, $this->urlFilters['url']);
		$tmp = explode('?', $requestUrl, 2);
		$url->path = Strings::replace($tmp[0], $this->urlFilters['path']);
		$url->query = isset($tmp[1]) ? $tmp[1] : '';

		$url->canonicalize();
		$url->path = Strings::fixEncoding($url->path);

		if (isset($_SERVER['DOCUMENT_ROOT'], $_SERVER['SCRIPT_FILENAME'])
			&& strncmp($_SERVER['DOCUMENT_ROOT'], $_SERVER['SCRIPT_FILENAME'], strlen($_SERVER['DOCUMENT_ROOT'])) === 0
		) {
			$script = '/' . ltrim(strtr(substr($_SERVER['SCRIPT_FILENAME'], strlen($_SERVER['DOCUMENT_ROOT'])), '\\', '/'), '/');
		} elseif (isset($_SERVER['SCRIPT_NAME'])) {
			$script = $_SERVER['SCRIPT_NAME'];
		} else {
			$script = '/';
		}

		if (strncasecmp($url->path . '/', $script . '/', strlen($script) + 1) === 0) {
			$url->scriptPath = substr($url->path, 0, strlen($script));

		} elseif (strncasecmp($url->path, $script, strrpos($script, '/') + 1) === 0) {
			$url->scriptPath = substr($url->path, 0, strrpos($script, '/') + 1);

		} else {
			$url->scriptPath = '/';
		}

		$useFilter = (!in_array(ini_get('filter.default'), array('', 'unsafe_raw')) || ini_get('filter.default_flags'));

		parse_str($url->query, $query);
		if (!$query) {
			$query = $useFilter ? filter_input_array(INPUT_GET, FILTER_UNSAFE_RAW) : (empty($_GET) ? array() : $_GET);
		}
		$post = $useFilter ? filter_input_array(INPUT_POST, FILTER_UNSAFE_RAW) : (empty($_POST) ? array() : $_POST);
		$cookies = $useFilter ? filter_input_array(INPUT_COOKIE, FILTER_UNSAFE_RAW) : (empty($_COOKIE) ? array() : $_COOKIE);

		$gpc = (bool) get_magic_quotes_gpc();
		$old = error_reporting(error_reporting() ^ E_NOTICE);

		if ($gpc || $this->encoding) {
			$utf = strcasecmp($this->encoding, 'UTF-8') === 0;
			$list = array(& $query, & $post, & $cookies);
			while (list($key, $val) = each($list)) {
				foreach ($val as $k => $v) {
					unset($list[$key][$k]);

					if ($gpc) {
						$k = stripslashes($k);
					}

					if ($this->encoding && is_string($k) && (preg_match(self::NONCHARS, $k) || preg_last_error())) {

					} elseif (is_array($v)) {
						$list[$key][$k] = $v;
						$list[] = & $list[$key][$k];

					} else {
						if ($gpc && !$useFilter) {
							$v = stripSlashes($v);
						}
						if ($this->encoding) {
							if ($utf) {
								$v = Strings::fixEncoding($v);

							} else {
								if (!Strings::checkEncoding($v)) {
									$v = iconv($this->encoding, 'UTF-8//IGNORE', $v);
								}
								$v = html_entity_decode($v, ENT_QUOTES, 'UTF-8');
							}
							$v = preg_replace(self::NONCHARS, '', $v);
						}
						$list[$key][$k] = $v;
					}
				}
			}
			unset($list, $key, $val, $k, $v);
		}

		$files = array();
		$list = array();
		if (!empty($_FILES)) {
			foreach ($_FILES as $k => $v) {
				if ($this->encoding && is_string($k) && (preg_match(self::NONCHARS, $k) || preg_last_error())) {
					continue;
				}
				$v['@'] = & $files[$k];
				$list[] = $v;
			}
		}

		while (list(, $v) = each($list)) {
			if (!isset($v['name'])) {
				continue;

			} elseif (!is_array($v['name'])) {
				if ($gpc) {
					$v['name'] = stripSlashes($v['name']);
				}
				if ($this->encoding) {
					$v['name'] = preg_replace(self::NONCHARS, '', Strings::fixEncoding($v['name']));
				}
				$v['@'] = new FileUpload($v);
				continue;
			}

			foreach ($v['name'] as $k => $foo) {
				if ($this->encoding && is_string($k) && (preg_match(self::NONCHARS, $k) || preg_last_error())) {
					continue;
				}
				$list[] = array(
					'name' => $v['name'][$k],
					'type' => $v['type'][$k],
					'size' => $v['size'][$k],
					'tmp_name' => $v['tmp_name'][$k],
					'error' => $v['error'][$k],
					'@' => & $v['@'][$k],
				);
			}
		}

		error_reporting($old);

		if (function_exists('apache_request_headers')) {
			$headers = array_change_key_case(apache_request_headers(), CASE_LOWER);
		} else {
			$headers = array();
			foreach ($_SERVER as $k => $v) {
				if (strncmp($k, 'HTTP_', 5) == 0) {
					$k = substr($k, 5);
				} elseif (strncmp($k, 'CONTENT_', 8)) {
					continue;
				}
				$headers[ strtr(strtolower($k), '_', '-') ] = $v;
			}
		}

		return new Request($url, $query, $post, $files, $cookies, $headers,
			isset($_SERVER['REQUEST_METHOD']) ? $_SERVER['REQUEST_METHOD'] : NULL,
			isset($_SERVER['REMOTE_ADDR']) ? $_SERVER['REMOTE_ADDR'] : NULL,
			isset($_SERVER['REMOTE_HOST']) ? $_SERVER['REMOTE_HOST'] : NULL
		);
	}

}

final class Response extends Nette\Object implements IResponse
{

	private static $fixIE = TRUE;

	public $cookieDomain = '';

	public $cookiePath = '/';

	public $cookieSecure = FALSE;

	public $cookieHttpOnly = TRUE;

	private $code = self::S200_OK;

	function setCode($code)
	{
		$code = (int) $code;

		static $allowed = array(
			200=>1, 201=>1, 202=>1, 203=>1, 204=>1, 205=>1, 206=>1,
			300=>1, 301=>1, 302=>1, 303=>1, 304=>1, 307=>1,
			400=>1, 401=>1, 403=>1, 404=>1, 405=>1, 406=>1, 408=>1, 410=>1, 412=>1, 415=>1, 416=>1,
			500=>1, 501=>1, 503=>1, 505=>1
		);

		if (!isset($allowed[$code])) {
			throw new Nette\InvalidArgumentException("Bad HTTP response '$code'.");

		} elseif (headers_sent($file, $line)) {
			throw new Nette\InvalidStateException("Cannot set HTTP code after HTTP headers have been sent" . ($file ? " (output started at $file:$line)." : "."));

		} else {
			$this->code = $code;
			$protocol = isset($_SERVER['SERVER_PROTOCOL']) ? $_SERVER['SERVER_PROTOCOL'] : 'HTTP/1.1';
			header($protocol . ' ' . $code, TRUE, $code);
		}
		return $this;
	}

	function getCode()
	{
		return $this->code;
	}

	function setHeader($name, $value)
	{
		if (headers_sent($file, $line)) {
			throw new Nette\InvalidStateException("Cannot send header after HTTP headers have been sent" . ($file ? " (output started at $file:$line)." : "."));
		}

		if ($value === NULL && function_exists('header_remove')) {
			header_remove($name);
		} else {
			header($name . ': ' . $value, TRUE, $this->code);
		}
		return $this;
	}

	function addHeader($name, $value)
	{
		if (headers_sent($file, $line)) {
			throw new Nette\InvalidStateException("Cannot send header after HTTP headers have been sent" . ($file ? " (output started at $file:$line)." : "."));
		}

		header($name . ': ' . $value, FALSE, $this->code);
	}

	function setContentType($type, $charset = NULL)
	{
		$this->setHeader('Content-Type', $type . ($charset ? '; charset=' . $charset : ''));
		return $this;
	}

	function redirect($url, $code = self::S302_FOUND)
	{
		if (isset($_SERVER['SERVER_SOFTWARE']) && preg_match('#^Microsoft-IIS/[1-5]#', $_SERVER['SERVER_SOFTWARE'])
			&& $this->getHeader('Set-Cookie') !== NULL
		) {
			$this->setHeader('Refresh', "0;url=$url");
			return;
		}

		$this->setCode($code);
		$this->setHeader('Location', $url);
		echo "<h1>Redirect</h1>\n\n<p><a href=\"" . htmlSpecialChars($url) . "\">Please click here to continue</a>.</p>";
	}

	function setExpiration($time)
	{
		if (!$time) {
			$this->setHeader('Cache-Control', 's-maxage=0, max-age=0, must-revalidate');
			$this->setHeader('Expires', 'Mon, 23 Jan 1978 10:00:00 GMT');
			return $this;
		}

		$time = Nette\DateTime::from($time);
		$this->setHeader('Cache-Control', 'max-age=' . ($time->format('U') - time()));
		$this->setHeader('Expires', self::date($time));
		return $this;
	}

	function isSent()
	{
		return headers_sent();
	}

	function getHeader($header, $default = NULL)
	{
		$header .= ':';
		$len = strlen($header);
		foreach (headers_list() as $item) {
			if (strncasecmp($item, $header, $len) === 0) {
				return ltrim(substr($item, $len));
			}
		}
		return $default;
	}

	function getHeaders()
	{
		$headers = array();
		foreach (headers_list() as $header) {
			$a = strpos($header, ':');
			$headers[substr($header, 0, $a)] = (string) substr($header, $a + 2);
		}
		return $headers;
	}

	static function date($time = NULL)
	{
		$time = Nette\DateTime::from($time);
		$time->setTimezone(new \DateTimeZone('GMT'));
		return $time->format('D, d M Y H:i:s \G\M\T');
	}

	function __destruct()
	{
		if (self::$fixIE && isset($_SERVER['HTTP_USER_AGENT']) && strpos($_SERVER['HTTP_USER_AGENT'], 'MSIE ') !== FALSE
			&& in_array($this->code, array(400, 403, 404, 405, 406, 408, 409, 410, 500, 501, 505), TRUE)
			&& $this->getHeader('Content-Type', 'text/html') === 'text/html'
		) {
			echo Nette\Utils\Strings::random(2e3, " \t\r\n");
			self::$fixIE = FALSE;
		}
	}

	function setCookie($name, $value, $time, $path = NULL, $domain = NULL, $secure = NULL, $httpOnly = NULL)
	{
		if (headers_sent($file, $line)) {
			throw new Nette\InvalidStateException("Cannot set cookie after HTTP headers have been sent" . ($file ? " (output started at $file:$line)." : "."));
		}

		setcookie(
			$name,
			$value,
			$time ? Nette\DateTime::from($time)->format('U') : 0,
			$path === NULL ? $this->cookiePath : (string) $path,
			$domain === NULL ? $this->cookieDomain : (string) $domain,
			$secure === NULL ? $this->cookieSecure : (bool) $secure,
			$httpOnly === NULL ? $this->cookieHttpOnly : (bool) $httpOnly
		);
		return $this;
	}

	function deleteCookie($name, $path = NULL, $domain = NULL, $secure = NULL)
	{
		if (headers_sent($file, $line)) {
			throw new Nette\InvalidStateException("Cannot delete cookie after HTTP headers have been sent" . ($file ? " (output started at $file:$line)." : "."));
		}

		setcookie(
			$name,
			FALSE,
			254400000,
			$path === NULL ? $this->cookiePath : (string) $path,
			$domain === NULL ? $this->cookieDomain : (string) $domain,
			$secure === NULL ? $this->cookieSecure : (bool) $secure,
			TRUE
		);
	}

}

class Session extends Nette\Object
{

	const DEFAULT_FILE_LIFETIME = 10800;

	private $regenerationNeeded;

	private static $started;

	private $options = array(

		'referer_check' => '',
		'use_cookies' => 1,
		'use_only_cookies' => 1,
		'use_trans_sid' => 0,

		'cookie_lifetime' => 0,
		'cookie_path' => '/',
		'cookie_domain' => '',
		'cookie_secure' => FALSE,
		'cookie_httponly' => TRUE,

		'gc_maxlifetime' => self::DEFAULT_FILE_LIFETIME,
		'cache_limiter' => NULL,
		'cache_expire' => NULL,
		'hash_function' => NULL,
		'hash_bits_per_character' => NULL,
	);

	private $request;

	private $response;

	function __construct(IRequest $request, IResponse $response)
	{
		$this->request = $request;
		$this->response = $response;
	}

	function start()
	{
		if (self::$started) {
			return;

		} elseif (self::$started === NULL && defined('SID')) {
			throw new Nette\InvalidStateException('A session had already been started by session.auto_start or session_start().');
		}

		$this->configure($this->options);

		Nette\Diagnostics\Debugger::tryError();
		session_start();
		if (Nette\Diagnostics\Debugger::catchError($e)) {
			@session_write_close();
			throw new Nette\InvalidStateException('session_start(): ' . $e->getMessage(), 0, $e);
		}

		self::$started = TRUE;
		if ($this->regenerationNeeded) {
			session_regenerate_id(TRUE);
			$this->regenerationNeeded = FALSE;
		}

		unset($_SESSION['__NT'], $_SESSION['__NS'], $_SESSION['__NM']);

		$nf = & $_SESSION['__NF'];
		if (empty($nf)) {
			$nf = array('C' => 0);
		} else {
			$nf['C']++;
		}

		$browserKey = $this->request->getCookie('nette-browser');
		if (!$browserKey) {
			$browserKey = Nette\Utils\Strings::random();
		}
		$browserClosed = !isset($nf['B']) || $nf['B'] !== $browserKey;
		$nf['B'] = $browserKey;

		$this->sendCookie();

		if (isset($nf['META'])) {
			$now = time();

			foreach ($nf['META'] as $section => $metadata) {
				if (is_array($metadata)) {
					foreach ($metadata as $variable => $value) {
						if ((!empty($value['B']) && $browserClosed) || (!empty($value['T']) && $now > $value['T'])
							|| ($variable !== '' && is_object($nf['DATA'][$section][$variable]) && (isset($value['V']) ? $value['V'] : NULL)
								!== Nette\Reflection\ClassType::from($nf['DATA'][$section][$variable])->getAnnotation('serializationVersion'))
						) {
							if ($variable === '') {
								unset($nf['META'][$section], $nf['DATA'][$section]);
								continue 2;
							}
							unset($nf['META'][$section][$variable], $nf['DATA'][$section][$variable]);
						}
					}
				}
			}
		}

		register_shutdown_function(array($this, 'clean'));
	}

	function isStarted()
	{
		return (bool) self::$started;
	}

	function close()
	{
		if (self::$started) {
			$this->clean();
			session_write_close();
			self::$started = FALSE;
		}
	}

	function destroy()
	{
		if (!self::$started) {
			throw new Nette\InvalidStateException('Session is not started.');
		}

		session_destroy();
		$_SESSION = NULL;
		self::$started = FALSE;
		if (!$this->response->isSent()) {
			$params = session_get_cookie_params();
			$this->response->deleteCookie(session_name(), $params['path'], $params['domain'], $params['secure']);
		}
	}

	function exists()
	{
		return self::$started || $this->request->getCookie(session_name()) !== NULL;
	}

	function regenerateId()
	{
		if (self::$started) {
			if (headers_sent($file, $line)) {
				throw new Nette\InvalidStateException("Cannot regenerate session ID after HTTP headers have been sent" . ($file ? " (output started at $file:$line)." : "."));
			}
			session_regenerate_id(TRUE);

		} else {
			$this->regenerationNeeded = TRUE;
		}
	}

	function getId()
	{
		return session_id();
	}

	function setName($name)
	{
		if (!is_string($name) || !preg_match('#[^0-9.][^.]*$#A', $name)) {
			throw new Nette\InvalidArgumentException('Session name must be a string and cannot contain dot.');
		}

		session_name($name);
		return $this->setOptions(array(
			'name' => $name,
		));
	}

	function getName()
	{
		return session_name();
	}

	function getSection($section, $class = 'Nette\Http\SessionSection')
	{
		return new $class($this, $section);
	}

	function getNamespace($section)
	{
		trigger_error(__METHOD__ . '() is deprecated; use getSection() instead.', E_USER_WARNING);
		return $this->getSection($section);
	}

	function hasSection($section)
	{
		if ($this->exists() && !self::$started) {
			$this->start();
		}

		return !empty($_SESSION['__NF']['DATA'][$section]);
	}

	function getIterator()
	{
		if ($this->exists() && !self::$started) {
			$this->start();
		}

		if (isset($_SESSION['__NF']['DATA'])) {
			return new \ArrayIterator(array_keys($_SESSION['__NF']['DATA']));

		} else {
			return new \ArrayIterator;
		}
	}

	function clean()
	{
		if (!self::$started || empty($_SESSION)) {
			return;
		}

		$nf = & $_SESSION['__NF'];
		if (isset($nf['META']) && is_array($nf['META'])) {
			foreach ($nf['META'] as $name => $foo) {
				if (empty($nf['META'][$name])) {
					unset($nf['META'][$name]);
				}
			}
		}

		if (empty($nf['META'])) {
			unset($nf['META']);
		}

		if (empty($nf['DATA'])) {
			unset($nf['DATA']);
		}

		if (empty($_SESSION)) {

		}
	}

	function setOptions(array $options)
	{
		if (self::$started) {
			$this->configure($options);
		}
		$this->options = $options + $this->options;
		if (!empty($options['auto_start'])) {
			$this->start();
		}
		return $this;
	}

	function getOptions()
	{
		return $this->options;
	}

	private function configure(array $config)
	{
		$special = array('cache_expire' => 1, 'cache_limiter' => 1, 'save_path' => 1, 'name' => 1);

		foreach ($config as $key => $value) {
			if (!strncmp($key, 'session.', 8)) {
				$key = substr($key, 8);
			}

			if ($value === NULL) {
				continue;

			} elseif (isset($special[$key])) {
				if (self::$started) {
					throw new Nette\InvalidStateException("Unable to set '$key' when session has been started.");
				}
				$key = "session_$key";
				$key($value);

			} elseif (strncmp($key, 'cookie_', 7) === 0) {
				if (!isset($cookie)) {
					$cookie = session_get_cookie_params();
				}
				$cookie[substr($key, 7)] = $value;

			} elseif (!function_exists('ini_set')) {
				if (ini_get($key) != $value && !Nette\Framework::$iAmUsingBadHost) {
					throw new Nette\NotSupportedException('Required function ini_set() is disabled.');
				}

			} else {
				if (self::$started) {
					throw new Nette\InvalidStateException("Unable to set '$key' when session has been started.");
				}
				ini_set("session.$key", $value);
			}
		}

		if (isset($cookie)) {
			session_set_cookie_params(
				$cookie['lifetime'], $cookie['path'], $cookie['domain'],
				$cookie['secure'], $cookie['httponly']
			);
			if (self::$started) {
				$this->sendCookie();
			}
		}
	}

	function setExpiration($time)
	{
		if (empty($time)) {
			return $this->setOptions(array(
				'gc_maxlifetime' => self::DEFAULT_FILE_LIFETIME,
				'cookie_lifetime' => 0,
			));

		} else {
			$time = Nette\DateTime::from($time)->format('U') - time();
			return $this->setOptions(array(
				'gc_maxlifetime' => $time,
				'cookie_lifetime' => $time,
			));
		}
	}

	function setCookieParams($path, $domain = NULL, $secure = NULL)
	{
		return $this->setOptions(array(
			'cookie_path' => $path,
			'cookie_domain' => $domain,
			'cookie_secure' => $secure
		));
	}

	function getCookieParams()
	{
		return session_get_cookie_params();
	}

	function setSavePath($path)
	{
		return $this->setOptions(array(
			'save_path' => $path,
		));
	}

	function setStorage(ISessionStorage $storage)
	{
		if (self::$started) {
			throw new Nette\InvalidStateException("Unable to set storage when session has been started.");
		}
		session_set_save_handler(
			array($storage, 'open'), array($storage, 'close'), array($storage, 'read'),
			array($storage, 'write'), array($storage, 'remove'), array($storage, 'clean')
		);
	}

	private function sendCookie()
	{
		$cookie = $this->getCookieParams();
		$this->response->setCookie(
			session_name(), session_id(),
			$cookie['lifetime'] ? $cookie['lifetime'] + time() : 0,
			$cookie['path'], $cookie['domain'], $cookie['secure'], $cookie['httponly']

		)->setCookie(
			'nette-browser', $_SESSION['__NF']['B'],
			Response::BROWSER, $cookie['path'], $cookie['domain']
		);
	}

}

final class SessionSection extends Nette\Object implements \IteratorAggregate, \ArrayAccess
{

	private $session;

	private $name;

	private $data;

	private $meta = FALSE;

	public $warnOnUndefined = FALSE;

	function __construct(Session $session, $name)
	{
		if (!is_string($name)) {
			throw new Nette\InvalidArgumentException("Session namespace must be a string, " . gettype($name) ." given.");
		}

		$this->session = $session;
		$this->name = $name;
	}

	private function start()
	{
		if ($this->meta === FALSE) {
			$this->session->start();
			$this->data = & $_SESSION['__NF']['DATA'][$this->name];
			$this->meta = & $_SESSION['__NF']['META'][$this->name];
		}
	}

	function getIterator()
	{
		$this->start();
		if (isset($this->data)) {
			return new \ArrayIterator($this->data);
		} else {
			return new \ArrayIterator;
		}
	}

	function __set($name, $value)
	{
		$this->start();
		$this->data[$name] = $value;
		if (is_object($value)) {
			$this->meta[$name]['V'] = Nette\Reflection\ClassType::from($value)->getAnnotation('serializationVersion');
		}
	}

	function &__get($name)
	{
		$this->start();
		if ($this->warnOnUndefined && !array_key_exists($name, $this->data)) {
			trigger_error("The variable '$name' does not exist in session section", E_USER_NOTICE);
		}

		return $this->data[$name];
	}

	function __isset($name)
	{
		if ($this->session->exists()) {
			$this->start();
		}
		return isset($this->data[$name]);
	}

	function __unset($name)
	{
		$this->start();
		unset($this->data[$name], $this->meta[$name]);
	}

	function offsetSet($name, $value)
	{
		$this->__set($name, $value);
	}

	function offsetGet($name)
	{
		return $this->__get($name);
	}

	function offsetExists($name)
	{
		return $this->__isset($name);
	}

	function offsetUnset($name)
	{
		$this->__unset($name);
	}

	function setExpiration($time, $variables = NULL)
	{
		$this->start();
		if (empty($time)) {
			$time = NULL;
			$whenBrowserIsClosed = TRUE;
		} else {
			$time = Nette\DateTime::from($time)->format('U');
			$max = ini_get('session.gc_maxlifetime');
			if ($time - time() > $max + 3) {
				trigger_error("The expiration time is greater than the session expiration $max seconds", E_USER_NOTICE);
			}
			$whenBrowserIsClosed = FALSE;
		}

		if ($variables === NULL) {
			$this->meta['']['T'] = $time;
			$this->meta['']['B'] = $whenBrowserIsClosed;

		} elseif (is_array($variables)) {
			foreach ($variables as $variable) {
				$this->meta[$variable]['T'] = $time;
				$this->meta[$variable]['B'] = $whenBrowserIsClosed;
			}

		} else {
			$this->meta[$variables]['T'] = $time;
			$this->meta[$variables]['B'] = $whenBrowserIsClosed;
		}
		return $this;
	}

	function removeExpiration($variables = NULL)
	{
		$this->start();
		if ($variables === NULL) {

			unset($this->meta['']['T'], $this->meta['']['B']);

		} elseif (is_array($variables)) {

			foreach ($variables as $variable) {
				unset($this->meta[$variable]['T'], $this->meta[$variable]['B']);
			}
		} else {
			unset($this->meta[$variables]['T'], $this->meta[$variable]['B']);
		}
	}

	function remove()
	{
		$this->start();
		$this->data = NULL;
		$this->meta = NULL;
	}

}

class Url extends Nette\FreezableObject
{

	public static $defaultPorts = array(
		'http' => 80,
		'https' => 443,
		'ftp' => 21,
		'news' => 119,
		'nntp' => 119,
	);

	private $scheme = '';

	private $user = '';

	private $pass = '';

	private $host = '';

	private $port = NULL;

	private $path = '';

	private $query = '';

	private $fragment = '';

	function __construct($url = NULL)
	{
		if (is_string($url)) {
			$parts = @parse_url($url);
			if ($parts === FALSE) {
				throw new Nette\InvalidArgumentException("Malformed or unsupported URI '$url'.");
			}

			foreach ($parts as $key => $val) {
				$this->$key = $val;
			}

			if (!$this->port && isset(self::$defaultPorts[$this->scheme])) {
				$this->port = self::$defaultPorts[$this->scheme];
			}

			if ($this->path === '' && ($this->scheme === 'http' || $this->scheme === 'https')) {
				$this->path = '/';
			}

		} elseif ($url instanceof self) {
			foreach ($this as $key => $val) {
				$this->$key = $url->$key;
			}
		}
	}

	function setScheme($value)
	{
		$this->updating();
		$this->scheme = (string) $value;
		return $this;
	}

	function getScheme()
	{
		return $this->scheme;
	}

	function setUser($value)
	{
		$this->updating();
		$this->user = (string) $value;
		return $this;
	}

	function getUser()
	{
		return $this->user;
	}

	function setPassword($value)
	{
		$this->updating();
		$this->pass = (string) $value;
		return $this;
	}

	function getPassword()
	{
		return $this->pass;
	}

	function setHost($value)
	{
		$this->updating();
		$this->host = (string) $value;
		return $this;
	}

	function getHost()
	{
		return $this->host;
	}

	function setPort($value)
	{
		$this->updating();
		$this->port = (int) $value;
		return $this;
	}

	function getPort()
	{
		return $this->port;
	}

	function setPath($value)
	{
		$this->updating();
		$this->path = (string) $value;
		return $this;
	}

	function getPath()
	{
		return $this->path;
	}

	function setQuery($value)
	{
		$this->updating();
		$this->query = (string) (is_array($value) ? http_build_query($value, '', '&') : $value);
		return $this;
	}

	function appendQuery($value)
	{
		$this->updating();
		$value = (string) (is_array($value) ? http_build_query($value, '', '&') : $value);
		$this->query .= ($this->query === '' || $value === '') ? $value : '&' . $value;
	}

	function getQuery()
	{
		return $this->query;
	}

	function setFragment($value)
	{
		$this->updating();
		$this->fragment = (string) $value;
		return $this;
	}

	function getFragment()
	{
		return $this->fragment;
	}

	function getAbsoluteUrl()
	{
		return $this->scheme . '://' . $this->getAuthority() . $this->path
			. ($this->query === '' ? '' : '?' . $this->query)
			. ($this->fragment === '' ? '' : '#' . $this->fragment);
	}

	function getAuthority()
	{
		$authority = $this->host;
		if ($this->port && isset(self::$defaultPorts[$this->scheme]) && $this->port !== self::$defaultPorts[$this->scheme]) {
			$authority .= ':' . $this->port;
		}

		if ($this->user !== '' && $this->scheme !== 'http' && $this->scheme !== 'https') {
			$authority = $this->user . ($this->pass === '' ? '' : ':' . $this->pass) . '@' . $authority;
		}

		return $authority;
	}

	function getHostUrl()
	{
		return $this->scheme . '://' . $this->getAuthority();
	}

	function getBasePath()
	{
		$pos = strrpos($this->path, '/');
		return $pos === FALSE ? '' : substr($this->path, 0, $pos + 1);
	}

	function getBaseUrl()
	{
		return $this->scheme . '://' . $this->getAuthority() . $this->getBasePath();
	}

	function getRelativeUrl()
	{
		return (string) substr($this->getAbsoluteUrl(), strlen($this->getBaseUrl()));
	}

	function isEqual($url)
	{

		$part = self::unescape(strtok($url, '?#'), '%/');
		if (strncmp($part, '//', 2) === 0) {
			if ($part !== '//' . $this->getAuthority() . $this->path) {
				return FALSE;
			}

		} elseif (strncmp($part, '/', 1) === 0) {
			if ($part !== $this->path) {
				return FALSE;
			}

		} else {
			if ($part !== $this->scheme . '://' . $this->getAuthority() . $this->path) {
				return FALSE;
			}
		}

		$part = preg_split('#[&;]#', self::unescape(strtr((string) strtok('?#'), '+', ' '), '%&;=+'));
		sort($part);
		$query = preg_split('#[&;]#', $this->query);
		sort($query);
		return $part === $query;
	}

	function canonicalize()
	{
		$this->updating();
		$this->path = $this->path === '' ? '/' : self::unescape($this->path, '%/');
		$this->host = strtolower(rawurldecode($this->host));
		$this->query = self::unescape(strtr($this->query, '+', ' '), '%&;=+');
	}

	function __toString()
	{
		return $this->getAbsoluteUrl();
	}

	static function unescape($s, $reserved = '%;/?:@&=+$,')
	{

		preg_match_all('#(?<=%)[a-f0-9][a-f0-9]#i', $s, $matches, PREG_OFFSET_CAPTURE | PREG_SET_ORDER);
		foreach (array_reverse($matches) as $match) {
			$ch = chr(hexdec($match[0][0]));
			if (strpos($reserved, $ch) === FALSE) {
				$s = substr_replace($s, $ch, $match[0][1] - 1, 3);
			}
		}
		return $s;
	}

	function getRelativeUri()
	{
		trigger_error(__METHOD__ . '() is deprecated; use ' . __CLASS__ . '::getRelativeUrl() instead.', E_USER_WARNING);
		return $this->getRelativeUrl();
	}

	function getAbsoluteUri()
	{
		trigger_error(__METHOD__ . '() is deprecated; use ' . __CLASS__ . '::getAbsoluteUrl() instead.', E_USER_WARNING);
		return $this->getAbsoluteUrl();
	}

	function getHostUri()
	{
		trigger_error(__METHOD__ . '() is deprecated; use ' . __CLASS__ . '::getHostUrl() instead.', E_USER_WARNING);
		return $this->getHostUrl();
	}

	function getBaseUri()
	{
		trigger_error(__METHOD__ . '() is deprecated; use ' . __CLASS__ . '::getBaseUrl() instead.', E_USER_WARNING);
		return $this->getBaseUrl();
	}

}

class UrlScript extends Url
{

	private $scriptPath = '/';

	function setScriptPath($value)
	{
		$this->updating();
		$this->scriptPath = (string) $value;
		return $this;
	}

	function getScriptPath()
	{
		return $this->scriptPath;
	}

	function getBasePath()
	{
		$pos = strrpos($this->scriptPath, '/');
		return $pos === FALSE ? '' : substr($this->path, 0, $pos + 1);
	}

	function getPathInfo()
	{
		return (string) substr($this->path, strlen($this->scriptPath));
	}

}

use Nette\Security\IAuthenticator;use Nette\Security\IAuthorizator;use Nette\Security\IIdentity;

class User extends Nette\Object implements IUser
{

	const MANUAL = 1,
		INACTIVITY = 2,
		BROWSER_CLOSED = 3;

	public $guestRole = 'guest';

	public $authenticatedRole = 'authenticated';

	public $onLoggedIn;

	public $onLoggedOut;

	private $namespace = '';

	private $session;

	private $context;

	function __construct(Nette\DI\IContainer $context)
	{
		$this->context = $context;
	}

	function login($username = NULL, $password = NULL)
	{
		$this->logout(TRUE);
		$credentials = func_get_args();
		$this->setIdentity($this->context->authenticator->authenticate($credentials));
		$this->setAuthenticated(TRUE);
		$this->onLoggedIn($this);
	}

	final function logout($clearIdentity = FALSE)
	{
		if ($this->isLoggedIn()) {
			$this->setAuthenticated(FALSE);
			$this->onLoggedOut($this);
		}

		if ($clearIdentity) {
			$this->setIdentity(NULL);
		}
	}

	final function isLoggedIn()
	{
		$session = $this->getSessionSection(FALSE);
		return $session && $session->authenticated;
	}

	final function getIdentity()
	{
		$session = $this->getSessionSection(FALSE);
		return $session ? $session->identity : NULL;
	}

	function getId()
	{
		$identity = $this->getIdentity();
		return $identity ? $identity->getId() : NULL;
	}

	function setAuthenticator(IAuthenticator $handler)
	{
		$this->context->removeService('authenticator');
		$this->context->authenticator = $handler;
		return $this;
	}

	final function getAuthenticator()
	{
		return $this->context->authenticator;
	}

	function setNamespace($namespace)
	{
		if ($this->namespace !== $namespace) {
			$this->namespace = (string) $namespace;
			$this->session = NULL;
		}
		return $this;
	}

	final function getNamespace()
	{
		return $this->namespace;
	}

	function setExpiration($time, $whenBrowserIsClosed = TRUE, $clearIdentity = FALSE)
	{
		$session = $this->getSessionSection(TRUE);
		if ($time) {
			$time = Nette\DateTime::from($time)->format('U');
			$session->expireTime = $time;
			$session->expireDelta = $time - time();

		} else {
			unset($session->expireTime, $session->expireDelta);
		}

		$session->expireIdentity = (bool) $clearIdentity;
		$session->expireBrowser = (bool) $whenBrowserIsClosed;
		$session->browserCheck = TRUE;
		$session->setExpiration(0, 'browserCheck');
		return $this;
	}

	final function getLogoutReason()
	{
		$session = $this->getSessionSection(FALSE);
		return $session ? $session->reason : NULL;
	}

	protected function getSessionSection($need)
	{
		if ($this->session !== NULL) {
			return $this->session;
		}

		if (!$need && !$this->context->session->exists()) {
			return NULL;
		}

		$this->session = $session = $this->context->session->getSection('Nette.Web.User/' . $this->namespace);

		if (!$session->identity instanceof IIdentity || !is_bool($session->authenticated)) {
			$session->remove();
		}

		if ($session->authenticated && $session->expireBrowser && !$session->browserCheck) {
			$session->reason = self::BROWSER_CLOSED;
			$session->authenticated = FALSE;
			$this->onLoggedOut($this);
			if ($session->expireIdentity) {
				unset($session->identity);
			}
		}

		if ($session->authenticated && $session->expireDelta > 0) {
			if ($session->expireTime < time()) {
				$session->reason = self::INACTIVITY;
				$session->authenticated = FALSE;
				$this->onLoggedOut($this);
				if ($session->expireIdentity) {
					unset($session->identity);
				}
			}
			$session->expireTime = time() + $session->expireDelta;
		}

		if (!$session->authenticated) {
			unset($session->expireTime, $session->expireDelta, $session->expireIdentity,
				$session->expireBrowser, $session->browserCheck, $session->authTime);
		}

		return $this->session;
	}

	protected function setAuthenticated($state)
	{
		$session = $this->getSessionSection(TRUE);
		$session->authenticated = (bool) $state;

		$this->context->session->regenerateId();

		if ($state) {
			$session->reason = NULL;
			$session->authTime = time();

		} else {
			$session->reason = self::MANUAL;
			$session->authTime = NULL;
		}
		return $this;
	}

	protected function setIdentity(IIdentity $identity = NULL)
	{
		$this->getSessionSection(TRUE)->identity = $identity;
		return $this;
	}

	function getRoles()
	{
		if (!$this->isLoggedIn()) {
			return array($this->guestRole);
		}

		$identity = $this->getIdentity();
		return $identity ? $identity->getRoles() : array($this->authenticatedRole);
	}

	final function isInRole($role)
	{
		return in_array($role, $this->getRoles(), TRUE);
	}

	function isAllowed($resource = IAuthorizator::ALL, $privilege = IAuthorizator::ALL)
	{
		$authorizator = $this->context->authorizator;
		foreach ($this->getRoles() as $role) {
			if ($authorizator->isAllowed($role, $resource, $privilege)) {
				return TRUE;
			}
		}

		return FALSE;
	}

	function setAuthorizator(IAuthorizator $handler)
	{
		$this->context->removeService('authorizator');
		$this->context->authorizator = $handler;
		return $this;
	}

	final function getAuthorizator()
	{
		return $this->context->authorizator;
	}

	function setAuthenticationHandler($v)
	{
		trigger_error(__METHOD__ . '() is deprecated; use setAuthenticator() instead.', E_USER_WARNING);
		return $this->setAuthenticator($v);
	}

	function setAuthorizationHandler($v)
	{
		trigger_error(__METHOD__ . '() is deprecated; use setAuthorizator() instead.', E_USER_WARNING);
		return $this->setAuthorizator($v);
	}

}

}

namespace Nette\Iterators {

use Nette;

class CachingIterator extends \CachingIterator implements \Countable
{

	private $counter = 0;

	function __construct($iterator)
	{
		if (is_array($iterator) || $iterator instanceof \stdClass) {
			$iterator = new \ArrayIterator($iterator);

		} elseif ($iterator instanceof \Traversable) {
			if ($iterator instanceof \IteratorAggregate) {
				$iterator = $iterator->getIterator();

			} elseif (!$iterator instanceof \Iterator) {
				$iterator = new \IteratorIterator($iterator);
			}

		} else {
			throw new Nette\InvalidArgumentException("Invalid argument passed to foreach resp. " . __CLASS__ . "; array or Traversable expected, " . (is_object($iterator) ? get_class($iterator) : gettype($iterator)) ." given.");
		}

		parent::__construct($iterator, 0);
	}

	function isFirst($width = NULL)
	{
		return $this->counter === 1 || ($width && $this->counter !== 0 && (($this->counter - 1) % $width) === 0);
	}

	function isLast($width = NULL)
	{
		return !$this->hasNext() || ($width && ($this->counter % $width) === 0);
	}

	function isEmpty()
	{
		return $this->counter === 0;
	}

	function isOdd()
	{
		return $this->counter % 2 === 1;
	}

	function isEven()
	{
		return $this->counter % 2 === 0;
	}

	function getCounter()
	{
		return $this->counter;
	}

	function count()
	{
		$inner = $this->getInnerIterator();
		if ($inner instanceof \Countable) {
			return $inner->count();

		} else {
			throw new Nette\NotSupportedException('Iterator is not countable.');
		}
	}

	function next()
	{
		parent::next();
		if (parent::valid()) {
			$this->counter++;
		}
	}

	function rewind()
	{
		parent::rewind();
		$this->counter = parent::valid() ? 1 : 0;
	}

	function getNextKey()
	{
		return $this->getInnerIterator()->key();
	}

	function getNextValue()
	{
		return $this->getInnerIterator()->current();
	}

	function __call($name, $args)
	{
		return Nette\ObjectMixin::call($this, $name, $args);
	}

	function &__get($name)
	{
		return Nette\ObjectMixin::get($this, $name);
	}

	function __set($name, $value)
	{
		return Nette\ObjectMixin::set($this, $name, $value);
	}

	function __isset($name)
	{
		return Nette\ObjectMixin::has($this, $name);
	}

	function __unset($name)
	{
		Nette\ObjectMixin::remove($this, $name);
	}

}

class Filter extends \FilterIterator
{

	private $callback;

	function __construct(\Iterator $iterator, $callback)
	{
		parent::__construct($iterator);
		$this->callback = $callback;
	}

	function accept()
	{
		return call_user_func($this->callback, $this);
	}

}

class InstanceFilter extends \FilterIterator implements \Countable
{

	private $type;

	function __construct(\Iterator $iterator, $type)
	{
		$this->type = $type;
		parent::__construct($iterator);
	}

	function accept()
	{
		return $this->current() instanceof $this->type;
	}

	function count()
	{
		return iterator_count($this);
	}

}

class Mapper extends \IteratorIterator
{

	private $callback;

	function __construct(\Traversable $iterator, $callback)
	{
		parent::__construct($iterator);
		$this->callback = $callback;
	}

	function current()
	{
		return call_user_func($this->callback, parent::current(), parent::key());
	}

}

class RecursiveFilter extends \FilterIterator implements \RecursiveIterator
{

	private $callback;

	private $childrenCallback;

	function __construct(\RecursiveIterator $iterator, $callback, $childrenCallback = NULL)
	{
		parent::__construct($iterator);
		$this->callback = $callback;
		$this->childrenCallback = $childrenCallback;
	}

	function accept()
	{
		return $this->callback === NULL || call_user_func($this->callback, $this);
	}

	function hasChildren()
	{
		return $this->getInnerIterator()->hasChildren()
			&& ($this->childrenCallback === NULL || call_user_func($this->childrenCallback, $this));
	}

	function getChildren()
	{
		return new static($this->getInnerIterator()->getChildren(), $this->callback, $this->childrenCallback);
	}

}

class Recursor extends \IteratorIterator implements \RecursiveIterator, \Countable
{

	function hasChildren()
	{
		$obj = $this->current();
		return ($obj instanceof \IteratorAggregate && $obj->getIterator() instanceof \RecursiveIterator)
			|| $obj instanceof \RecursiveIterator;
	}

	function getChildren()
	{
		$obj = $this->current();
		return $obj instanceof \IteratorAggregate ? $obj->getIterator() : $obj;
	}

	function count()
	{
		return iterator_count($this);
	}

}

}

namespace Nette\Latte {

use Nette;

class Engine extends Nette\Object
{

	public $parser;

	function __construct()
	{
		$this->parser = new Parser;
		Macros\CoreMacros::install($this->parser);
		$this->parser->addMacro('cache', new Macros\CacheMacro($this->parser));
		Macros\UIMacros::install($this->parser);
		Macros\FormMacros::install($this->parser);
	}

	function __invoke($s)
	{
		$this->parser->context = array(Parser::CONTEXT_TEXT);
		$this->parser->setDelimiters('\\{(?![\\s\'"{}])', '\\}');
		return $this->parser->parse($s);
	}

}

class HtmlNode extends Nette\Object
{

	public $name;

	public $isEmpty = FALSE;

	public $attrs = array();

	public $closing = FALSE;

	public $offset;

	function __construct($name)
	{
		$this->name = $name;
		$this->isEmpty = isset(Nette\Utils\Html::$emptyElements[strtolower($this->name)]);
	}

}

class MacroNode extends Nette\Object
{

	public $macro;

	public $name;

	public $isEmpty = FALSE;

	public $args;

	public $modifiers;

	public $closing = FALSE;

	public $tokenizer;

	public $offset;

	public $parentNode;

	public $content;

	public $data;

	function __construct(IMacro $macro, $name, $args = NULL, $modifiers = NULL, MacroNode $parentNode = NULL)
	{
		$this->macro = $macro;
		$this->name = (string) $name;
		$this->modifiers = (string) $modifiers;
		$this->parentNode = $parentNode;
		$this->tokenizer = new MacroTokenizer($this->args);
		$this->data = new \stdClass;
		$this->setArgs($args);
	}

	function setArgs($args)
	{
		$this->args = (string) $args;
		$this->tokenizer->tokenize($this->args);
	}

	function close($content)
	{
		$this->closing = TRUE;
		$this->content = $content;
		return $this->macro->nodeClosed($this);
	}

}

}

namespace Nette\Latte\Macros {

use Nette;use Nette\Latte;

class CacheMacro extends Nette\Object implements Latte\IMacro
{

	private $used;

	function initialize()
	{
		$this->used = FALSE;
	}

	function finalize()
	{
		if ($this->used) {
			return array('Nette\Latte\Macros\CacheMacro::initRuntime($template, $_g);');
		}
	}

	function nodeOpened(Latte\MacroNode $node)
	{
		$this->used = TRUE;
		$node->isEmpty = FALSE;
		return Latte\PhpWriter::using($node)
			->write('<?php if (Nette\Latte\Macros\CacheMacro::createCache($netteCacheStorage, %var, $_g->caches, %node.array?)) { ?>',
				Nette\Utils\Strings::random()
			);
	}

	function nodeClosed(Latte\MacroNode $node)
	{
		return '<?php $_l->tmp = array_pop($_g->caches); if (!$_l->tmp instanceof stdClass) $_l->tmp->end(); } ?>';
	}

	static function initRuntime($template, $global)
	{
		if (!empty($global->caches)) {
			end($global->caches)->dependencies[Nette\Caching\Cache::FILES][] = $template->getFile();
		}
	}

	static function createCache(Nette\Caching\IStorage $cacheStorage, $key, & $parents, $args = NULL)
	{
		if ($args) {
			if (array_key_exists('if', $args) && !$args['if']) {
				return $parents[] = (object) NULL;
			}
			$key = array_merge(array($key), array_intersect_key($args, range(0, count($args))));
		}
		if ($parents) {
			end($parents)->dependencies[Nette\Caching\Cache::ITEMS][] = $key;
		}

		$cache = new Nette\Caching\Cache($cacheStorage, 'Nette.Templating.Cache');
		if ($helper = $cache->start($key)) {
			$helper->dependencies = array(
				Nette\Caching\Cache::TAGS => isset($args['tags']) ? $args['tags'] : NULL,
				Nette\Caching\Cache::EXPIRATION => isset($args['expire']) ? $args['expire'] : '+ 7 days',
			);
			$parents[] = $helper;
		}
		return $helper;
	}

}

use Nette\Latte\MacroNode;

class MacroSet extends Nette\Object implements Latte\IMacro
{

	public $parser;

	private $macros;

	function __construct(Latte\Parser $parser)
	{
		$this->parser = $parser;
	}

	function addMacro($name, $begin, $end = NULL)
	{
		$this->macros[$name] = array($begin, $end);
		$this->parser->addMacro($name, $this);
	}

	static function install(Latte\Parser $parser)
	{
		return new static($parser);
	}

	function initialize()
	{
	}

	function finalize()
	{
	}

	function nodeOpened(MacroNode $node)
	{
		$node->isEmpty = !isset($this->macros[$node->name][1]);
		return $this->compile($node, $this->macros[$node->name][0]);
	}

	function nodeClosed(MacroNode $node)
	{
		return $this->compile($node, $this->macros[$node->name][1]);
	}

	private function compile(MacroNode $node, $def)
	{
		$writer = Latte\PhpWriter::using($node, $this->parser->context);
		if (is_string($def)) {
			$code = $writer->write($def);
		} else {
			$code = callback($def)->invoke($node, $writer);
			if ($code === FALSE) {
				return FALSE;
			}
		}
		return "<?php $code ?>";
	}

}

use Nette\Latte\ParseException;

class CoreMacros extends MacroSet
{

	static function install(Latte\Parser $parser)
	{
		$me = new static($parser);

		$me->addMacro('if', array($me, 'macroIf'), array($me, 'macroEndIf'));
		$me->addMacro('elseif', 'elseif (%node.args):');
		$me->addMacro('else', 'else:');
		$me->addMacro('ifset', 'if (isset(%node.args)):', 'endif');
		$me->addMacro('elseifset', 'elseif (isset(%node.args)):');

		$me->addMacro('foreach', array($me, 'macroForeach'), '$iterations++; endforeach; array_pop($_l->its); $iterator = end($_l->its)');
		$me->addMacro('for', 'for (%node.args):', 'endfor');
		$me->addMacro('while', 'while (%node.args):', 'endwhile');
		$me->addMacro('continueIf', 'if (%node.args) continue');
		$me->addMacro('breakIf', 'if (%node.args) break');
		$me->addMacro('first', 'if ($iterator->isFirst(%node.args)):', 'endif');
		$me->addMacro('last', 'if ($iterator->isLast(%node.args)):', 'endif');
		$me->addMacro('sep', 'if (!$iterator->isLast(%node.args)):', 'endif');

		$me->addMacro('var', array($me, 'macroVar'));
		$me->addMacro('assign', array($me, 'macroVar'));
		$me->addMacro('default', array($me, 'macroVar'));
		$me->addMacro('dump', array($me, 'macroDump'));
		$me->addMacro('debugbreak', array($me, 'macroDebugbreak'));
		$me->addMacro('l', '?>{<?php');
		$me->addMacro('r', '?>}<?php');

		$me->addMacro('_', array($me, 'macroTranslate'), array($me, 'macroTranslate'));
		$me->addMacro('=', array($me, 'macroExpr'));
		$me->addMacro('?', array($me, 'macroExpr'));

		$me->addMacro('syntax', array($me, 'macroSyntax'), array($me, 'macroSyntax'));
		$me->addMacro('capture', array($me, 'macroCapture'), array($me, 'macroCaptureEnd'));
		$me->addMacro('include', array($me, 'macroInclude'));
		$me->addMacro('use', array($me, 'macroUse'));

		$me->addMacro('@href', NULL, NULL);
		$me->addMacro('@class', array($me, 'macroClass'));
		$me->addMacro('@attr', array($me, 'macroAttr'));
		$me->addMacro('attr', array($me, 'macroOldAttr'));
	}

	function finalize()
	{
		return array('list($_l, $_g) = Nette\Latte\Macros\CoreMacros::initRuntime($template, '
			. var_export($this->parser->templateId, TRUE) . ')');
	}

	function macroIf(MacroNode $node, $writer)
	{
		if ($node->data->capture = ($node->args === '')) {
			return 'ob_start()';
		}
		return $writer->write('if (%node.args):');
	}

	function macroEndIf(MacroNode $node, $writer)
	{
		if ($node->data->capture) {
			if ($node->args === '') {
				throw new ParseException('Missing condition in {if} macro.');
			}
			return $writer->write('if (%node.args) ob_end_flush(); else ob_end_clean()');
		}
		return 'endif';
	}

	function macroTranslate(MacroNode $node, $writer)
	{
		if ($node->closing) {
			return $writer->write('echo %modify($template->translate(ob_get_clean()))');

		} elseif ($node->isEmpty = ($node->args !== '')) {
			return $writer->write('echo %modify($template->translate(%node.args))');

		} else {
			return 'ob_start()';
		}
	}

	function macroSyntax(MacroNode $node)
	{
		if ($node->closing) {
			$node->args = 'latte';
		}
		switch ($node->args) {
		case '':
		case 'latte':
			$this->parser->setDelimiters('\\{(?![\\s\'"{}])', '\\}');
			break;

		case 'double':
			$this->parser->setDelimiters('\\{\\{(?![\\s\'"{}])', '\\}\\}');
			break;

		case 'asp':
			$this->parser->setDelimiters('<%\s*', '\s*%>');
			break;

		case 'python':
			$this->parser->setDelimiters('\\{[{%]\s*', '\s*[%}]\\}');
			break;

		case 'off':
			$this->parser->setDelimiters('[^\x00-\xFF]', '');
			break;

		default:
			throw new ParseException("Unknown syntax '$node->args'");
		}
	}

	function macroInclude(MacroNode $node, $writer)
	{
		$code = $writer->write('Nette\Latte\Macros\CoreMacros::includeTemplate(%node.word, %node.array? + $template->getParams(), $_l->templates[%var])',
			$this->parser->templateId);

		if ($node->modifiers) {
			return $writer->write('echo %modify(%raw->__toString(TRUE))', $code);
		} else {
			return $code . '->render()';
		}
	}

	function macroUse(MacroNode $node, $writer)
	{
		call_user_func(array($node->tokenizer->fetchWord(), 'install'), $this->parser)
			->initialize();
	}

	function macroCapture(MacroNode $node, $writer)
	{
		$variable = $node->tokenizer->fetchWord();
		if (substr($variable, 0, 1) !== '$') {
			throw new ParseException("Invalid capture block variable '$variable'");
		}
		$node->data->variable = $variable;
		return 'ob_start()';
	}

	function macroCaptureEnd(MacroNode $node, $writer)
	{
		return $writer->write("{$node->data->variable} = %modify(ob_get_clean())");
	}

	function macroForeach(MacroNode $node, $writer)
	{
		return '$iterations = 0; foreach ($iterator = $_l->its[] = new Nette\Iterators\CachingIterator('
			. preg_replace('#(.*)\s+as\s+#i', '$1) as ', $writer->formatArgs(), 1) . '):';
	}

	function macroClass(MacroNode $node, $writer)
	{
		return $writer->write('if ($_l->tmp = trim(implode(" ", array_unique(%node.array)))) echo \' class="\' . %escape($_l->tmp) . \'"\'');
	}

	function macroAttr(MacroNode $node, $writer)
	{
		return $writer->write('echo Nette\Utils\Html::el(NULL, %node.array)->attributes()');
	}

	function macroOldAttr(MacroNode $node)
	{
		return Nette\Utils\Strings::replace($node->args . ' ', '#\)\s+#', ')->');
	}

	function macroDump(MacroNode $node, $writer)
	{
		$args = $writer->formatArgs();
		return $writer->write('Nette\Diagnostics\Debugger::barDump(' . ($node->args ? "array(%var => $args)" : 'get_defined_vars()')
			. ', "Template " . str_replace(dirname(dirname($template->getFile())), "\xE2\x80\xA6", $template->getFile()))', $args);
	}

	function macroDebugbreak(MacroNode $node, $writer)
	{
		return $writer->write(($node->args == NULL ? '' : 'if (!(%node.args)); else')
			. 'if (function_exists("debugbreak")) debugbreak(); elseif (function_exists("xdebug_break")) xdebug_break()');
	}

	function macroVar(MacroNode $node, $writer)
	{
		$out = '';
		$var = TRUE;
		$tokenizer = $writer->preprocess();
		while ($token = $tokenizer->fetchToken()) {
			if ($var && ($token['type'] === Latte\MacroTokenizer::T_SYMBOL || $token['type'] === Latte\MacroTokenizer::T_VARIABLE)) {
				if ($node->name === 'default') {
					$out .= "'" . ltrim($token['value'], "$") . "'";
				} else {
					$out .= '$' . ltrim($token['value'], "$");
				}
				$var = NULL;

			} elseif (($token['value'] === '=' || $token['value'] === '=>') && $token['depth'] === 0) {
				$out .= $node->name === 'default' ? '=>' : '=';
				$var = FALSE;

			} elseif ($token['value'] === ',' && $token['depth'] === 0) {
				$out .= $node->name === 'default' ? ',' : ';';
				$var = TRUE;

			} elseif ($var === NULL && $node->name === 'default' && $token['type'] !== Latte\MacroTokenizer::T_WHITESPACE) {
				throw new ParseException("Unexpected '$token[value]' in {default $node->args}");

			} else {
				$out .= $writer->canQuote($tokenizer) ? "'$token[value]'" : $token['value'];
			}
		}
		return $node->name === 'default' ? "extract(array($out), EXTR_SKIP)" : $out;
	}

	function macroExpr(MacroNode $node, $writer)
	{
		return $writer->write(($node->name === '?' ? '' : 'echo ') . '%modify(%node.args)');
	}

	static function includeTemplate($destination, $params, $template)
	{
		if ($destination instanceof Nette\Templating\ITemplate) {
			$tpl = $destination;

		} elseif ($destination == NULL) {
			throw new Nette\InvalidArgumentException("Template file name was not specified.");

		} else {
			$tpl = clone $template;
			if ($template instanceof Nette\Templating\IFileTemplate) {
				if (substr($destination, 0, 1) !== '/' && substr($destination, 1, 1) !== ':') {
					$destination = dirname($template->getFile()) . '/' . $destination;
				}
				$tpl->setFile($destination);
			}
		}

		$tpl->setParams($params);
		return $tpl;
	}

	static function initRuntime($template, $templateId)
	{

		if (isset($template->_l)) {
			$local = $template->_l;
			unset($template->_l);
		} else {
			$local = (object) NULL;
		}
		$local->templates[$templateId] = $template;

		if (!isset($template->_g)) {
			$template->_g = (object) NULL;
		}

		return array($local, $template->_g);
	}

}

use Nette\Utils\Strings;

class FormMacros extends MacroSet
{

	static function install(Latte\Parser $parser)
	{
		$me = new static($parser);
		$me->addMacro('form',
			'$form = $control[%node.word]; echo $form->getElementPrototype()->addAttributes(%node.array)->startTag()',
			'?><div><?php
foreach ($form->getComponents(TRUE, \'Nette\Forms\Controls\HiddenField\') as $_tmp) echo $_tmp->getControl();
if (iterator_count($form->getComponents(TRUE, \'Nette\Forms\Controls\TextInput\')) < 2) echo "<!--[if IE]><input type=IEbug disabled style=\"display:none\"><![endif]-->";
?></div>
<?php echo $form->getElementPrototype()->endTag()');
		$me->addMacro('label', array($me, 'macroLabel'), '?></label><?php');
		$me->addMacro('input', 'echo $form[%node.word]->getControl()->addAttributes(%node.array)');
	}

	function macroLabel(MacroNode $node, $writer)
	{
		$cmd = 'if ($_label = $form[%node.word]->getLabel()) echo $_label->addAttributes(%node.array)';
		if ($node->isEmpty = (substr($node->args, -1) === '/')) {
			$node->setArgs(substr($node->args, 0, -1));
			return $writer->write($cmd);
		} else {
			return $writer->write($cmd . '->startTag()');
		}
	}

}

class UIMacros extends MacroSet
{

	const RE_IDENTIFIER = '[_a-zA-Z\x7F-\xFF][_a-zA-Z0-9\x7F-\xFF]*';

	private $namedBlocks = array();

	private $extends;

	static function install(Latte\Parser $parser)
	{
		$me = new static($parser);
		$me->addMacro('include', array($me, 'macroInclude'));
		$me->addMacro('includeblock', array($me, 'macroIncludeBlock'));
		$me->addMacro('extends', array($me, 'macroExtends'));
		$me->addMacro('layout', array($me, 'macroExtends'));
		$me->addMacro('block', array($me, 'macroBlock'), array($me, 'macroBlockEnd'));
		$me->addMacro('define', array($me, 'macroBlock'), array($me, 'macroBlockEnd'));
		$me->addMacro('snippet', array($me, 'macroBlock'), array($me, 'macroBlockEnd'));
		$me->addMacro('ifset', array($me, 'macroIfset'), 'endif');

		$me->addMacro('widget', array($me, 'macroControl'));
		$me->addMacro('control', array($me, 'macroControl'));

		$me->addMacro('@href', function(MacroNode $node, $writer) use($me) {
			return ' ?> href="<?php ' . $me->macroLink($node, $writer) . ' ?>"<?php ';
		});
		$me->addMacro('plink', array($me, 'macroLink'));
		$me->addMacro('link', array($me, 'macroLink'));
		$me->addMacro('ifCurrent', array($me, 'macroIfCurrent'), 'endif');

		$me->addMacro('contentType', array($me, 'macroContentType'));
		$me->addMacro('status', array($me, 'macroStatus'));
	}

	function initialize()
	{
		$this->namedBlocks = array();
		$this->extends = NULL;
	}

	function finalize()
	{

		try {
			$this->parser->writeMacro('/block');
		} catch (ParseException $e) {
		}

		$epilog = $prolog = array();

		if ($this->namedBlocks) {
			foreach ($this->namedBlocks as $name => $code) {
				$func = '_lb' . substr(md5($this->parser->templateId . $name), 0, 10) . '_' . preg_replace('#[^a-z0-9_]#i', '_', $name);
				$snippet = $name[0] === '_';
				$prolog[] = "//\n// block $name\n//\n"
					. "if (!function_exists(\$_l->blocks[" . var_export($name, TRUE) . "][] = '$func')) { "
					. "function $func(\$_l, \$_args) { "
					. (PHP_VERSION_ID > 50208 ? 'extract($_args)' : 'foreach ($_args as $__k => $__v) $$__k = $__v')
					. ($snippet ? '; $control->validateControl(' . var_export(substr($name, 1), TRUE) . ')' : '')
					. "\n?>$code<?php\n}}";
			}
			$prolog[] = "//\n// end of blocks\n//";
		}

		if ($this->namedBlocks || $this->extends) {
			$prolog[] = "// template extending and snippets support";

			if (is_bool($this->extends)) {
				$prolog[] = '$_l->extends = ' . var_export($this->extends, TRUE) . '; unset($_extends, $template->_extends);';
			} else {
				$prolog[] = '$_l->extends = empty($template->_extends) ? FALSE : $template->_extends; unset($_extends, $template->_extends);';
			}

			$prolog[] = '
if ($_l->extends) {
	ob_start();
} elseif (!empty($control->snippetMode)) {
	return Nette\Latte\Macros\UIMacros::renderSnippets($control, $_l, get_defined_vars());
}';
			$epilog[] = '
// template extending support
if ($_l->extends) {
	ob_end_clean();
	Nette\Latte\Macros\CoreMacros::includeTemplate($_l->extends, get_defined_vars(), $template)->render();
}';
		} else {
			$prolog[] = '
// snippets support
if (!empty($control->snippetMode)) {
	return Nette\Latte\Macros\UIMacros::renderSnippets($control, $_l, get_defined_vars());
}';
		}

		return array(implode("\n\n", $prolog), implode("\n", $epilog));
	}

	function macroInclude(MacroNode $node, $writer)
	{
		$destination = $node->tokenizer->fetchWord();
		if (substr($destination, 0, 1) !== '#') {
			return FALSE;
		}

		$destination = ltrim($destination, '#');
		if (!Strings::match($destination, '#^\$?' . self::RE_IDENTIFIER . '$#')) {
			throw new ParseException("Included block name must be alphanumeric string, '$destination' given.");
		}

		$parent = $destination === 'parent';
		if ($destination === 'parent' || $destination === 'this') {
			$item = $node->parentNode;
			while ($item && $item->name !== 'block' && !isset($item->data->name)) $item = $item->parentNode;
			if (!$item) {
				throw new ParseException("Cannot include $destination block outside of any block.");
			}
			$destination = $item->data->name;
		}

		$name = $destination[0] === '$' ? $destination : var_export($destination, TRUE);
		if (isset($this->namedBlocks[$destination]) && !$parent) {
			$cmd = "call_user_func(reset(\$_l->blocks[$name]), \$_l, %node.array? + \$template->getParams())";
		} else {
			$cmd = 'Nette\Latte\Macros\UIMacros::callBlock' . ($parent ? 'Parent' : '') . "(\$_l, $name, %node.array? + \$template->getParams())";
		}

		if ($node->modifiers) {
			return $writer->write("ob_start(); $cmd; echo %modify(ob_get_clean())");
		} else {
			return $writer->write($cmd);
		}
	}

	function macroIncludeBlock(MacroNode $node, $writer)
	{
		return $writer->write('Nette\Latte\Macros\CoreMacros::includeTemplate(%node.word, %node.array? + get_defined_vars(), $_l->templates[%var])->render()',
			$this->parser->templateId);
	}

	function macroExtends(MacroNode $node, $writer)
	{
		if (!$node->args) {
			throw new ParseException("Missing destination in {extends}");
		}
		if (!empty($node->parentNode)) {
			throw new ParseException("{extends} must be placed outside any macro.");
		}
		if ($this->extends !== NULL) {
			throw new ParseException("Multiple {extends} declarations are not allowed.");
		}
		$this->extends = $node->args !== 'none';
		return $this->extends ? '$_l->extends = ' . ($node->args === 'auto' ? '$layout' : $writer->formatArgs()) : '';
	}

	function macroBlock(MacroNode $node, $writer)
	{
		$name = $node->tokenizer->fetchWord();

		if ($node->name === 'block' && $name === FALSE) {
			return $node->modifiers === '' ? '' : 'ob_start()';
		}

		$node->data->name = $name = ltrim($name, '#');
		$node->data->end = '';
		if ($name == NULL) {
			if ($node->name !== 'snippet') {
				throw new ParseException("Missing block name.");
			}

		} elseif (!Strings::match($name, '#^' . self::RE_IDENTIFIER . '$#')) {
			if ($node->name === 'snippet') {
				$parent = $node->parentNode;
				while ($parent && $parent->name !== 'snippet') $parent = $parent->parentNode;
				if (!$parent) {
					throw new ParseException("Dynamic snippets are allowed only inside static snippet.");
				}
				$parent->data->dynamic = TRUE;

				$tag = trim($node->tokenizer->fetchWord(), '<>');
				$tag = $tag ? $tag : 'div';
				$node->data->leave = TRUE;
				$node->data->end = "\$_dynSnippets[\$_dynSnippetId] = ob_get_flush() ?>\n</$tag><?php";
				return $writer->write("?>\n<$tag id=\"<?php echo \$_dynSnippetId = \$control->getSnippetId({$writer->formatWord($name)}) ?>\"><?php ob_start()");

			} else {
				$node->data->leave = TRUE;
				$fname = $writer->formatWord($name);
				$node->data->end = "}} call_user_func(reset(\$_l->blocks[$fname]), \$_l, get_defined_vars())";
				$func = '_lb' . substr(md5($this->parser->templateId . $name), 0, 10) . '_' . preg_replace('#[^a-z0-9_]#i', '_', $name);
				return "//\n// block $name\n//\n"
					. "if (!function_exists(\$_l->blocks[$fname][] = '$func')) { "
					. "function $func(\$_l, \$_args) { "
					. (PHP_VERSION_ID > 50208 ? 'extract($_args)' : 'foreach ($_args as $__k => $__v) $$__k = $__v');
			}
		}

		if ($node->name === 'snippet') {
			$node->data->name = $name = '_' . $name;
		}
		if (isset($this->namedBlocks[$name])) {
			throw new ParseException("Cannot redeclare static block '$name'");
		}
		$top = empty($node->parentNode);
		$this->namedBlocks[$name] = TRUE;

		$include = 'call_user_func(reset($_l->blocks[%var]), $_l, ' . ($node->name === 'snippet' ? '$template->getParams()' : 'get_defined_vars()') . ')';
		if ($node->modifiers) {
			$include = "ob_start(); $include; echo %modify(ob_get_clean())";
		}

		if ($node->name === 'snippet') {
			$tag = trim($node->tokenizer->fetchWord(), '<>');
			$tag = $tag ? $tag : 'div';
			return $writer->write("?>\n<$tag id=\"<?php echo \$control->getSnippetId(%var) ?>\"><?php $include ?>\n</$tag><?php ",
				(string) substr($name, 1), $name
			);

		} elseif ($node->name === 'define') {
			return '';

		} elseif (!$top) {
			return $writer->write($include, $name);

		} elseif ($this->extends) {
			return '';

		} else {
			return $writer->write("if (!\$_l->extends) { $include; }", $name);
		}
	}

	function macroBlockEnd(MacroNode $node, $writer)
	{
		if (isset($node->data->name)) {
			if (empty($node->data->leave)) {
				if (!empty($node->data->dynamic)) {
					$node->content .= '<?php if (isset($_dynSnippets)) return $_dynSnippets; ?>';
				}
				$this->namedBlocks[$node->data->name] = $node->content;
				$node->content = '';
			}
			return $node->data->end;

		} elseif ($node->modifiers) {
			return $writer->write('echo %modify(ob_get_clean())');
		}
	}

	function macroIfset(MacroNode $node, $writer)
	{
		if (strpos($node->args, '#') === FALSE) {
			return FALSE;
		}
		$list = array();
		while (($name = $node->tokenizer->fetchWord()) !== FALSE) {
			$list[] = $name[0] === '#' ? '$_l->blocks["' . substr($name, 1) . '"]' : $name;
		}
		return 'if (isset(' . implode(', ', $list) . ')):';
	}

	function macroControl(MacroNode $node, $writer)
	{
		$pair = $node->tokenizer->fetchWord();
		if ($pair === FALSE) {
			throw new ParseException("Missing control name in {control}");
		}
		$pair = explode(':', $pair, 2);
		$name = $writer->formatWord($pair[0]);
		$method = isset($pair[1]) ? ucfirst($pair[1]) : '';
		$method = Strings::match($method, '#^(' . self::RE_IDENTIFIER . '|)$#') ? "render$method" : "{\"render$method\"}";
		$param = $writer->formatArray();
		if (strpos($node->args, '=>') === FALSE) {
			$param = substr($param, 6, -1);
		}
		return ($name[0] === '$' ? "if (is_object($name)) \$_ctrl = $name; else " : '')
			. '$_ctrl = $control->getWidget(' . $name . '); '
			. 'if ($_ctrl instanceof Nette\Application\UI\IPartiallyRenderable) $_ctrl->validateControl(); '
			. "\$_ctrl->$method($param)";
	}

	function macroLink(MacroNode $node, $writer)
	{
		return $writer->write('echo %escape(' . ($node->name === 'plink' ? '$presenter' : '$control') . '->link(%node.word, %node.array?))');
	}

	function macroIfCurrent(MacroNode $node, $writer)
	{
		return $writer->write(($node->args ? 'try { $presenter->link(%node.word, %node.array?); } catch (Nette\Application\UI\InvalidLinkException $e) {}' : '')
			. '; if ($presenter->getLastCreatedRequestFlag("current")):');
	}

	function macroContentType(MacroNode $node, $writer)
	{
		if (strpos($node->args, 'html') !== FALSE) {
			$this->parser->context = array(Latte\Parser::CONTEXT_TEXT);

		} elseif (strpos($node->args, 'xml') !== FALSE) {
			$this->parser->context = array(Latte\Parser::CONTEXT_NONE, 'xml');

		} elseif (strpos($node->args, 'javascript') !== FALSE) {
			$this->parser->context = array(Latte\Parser::CONTEXT_NONE, 'js');

		} elseif (strpos($node->args, 'css') !== FALSE) {
			$this->parser->context = array(Latte\Parser::CONTEXT_NONE, 'css');

		} elseif (strpos($node->args, 'plain') !== FALSE) {
			$this->parser->context = array(Latte\Parser::CONTEXT_NONE, 'text');

		} else {
			$this->parser->context = array(Latte\Parser::CONTEXT_NONE);
		}

		if (strpos($node->args, '/')) {
			return $writer->write('$netteHttpResponse->setHeader("Content-Type", %var)', $node->args);
		}
	}

	function macroStatus(MacroNode $node, $writer)
	{
		return $writer->write((substr($node->args, -1) === '?' ? 'if (!$netteHttpResponse->isSent()) ' : '') .
			'$netteHttpResponse->setCode(%var)', (int) $node->args
		);
	}

	static function callBlock($context, $name, $params)
	{
		if (empty($context->blocks[$name])) {
			throw new Nette\InvalidStateException("Cannot include undefined block '$name'.");
		}
		$block = reset($context->blocks[$name]);
		$block($context, $params);
	}

	static function callBlockParent($context, $name, $params)
	{
		if (empty($context->blocks[$name]) || ($block = next($context->blocks[$name])) === FALSE) {
			throw new Nette\InvalidStateException("Cannot include undefined parent block '$name'.");
		}
		$block($context, $params);
	}

	static function renderSnippets($control, $local, $params)
	{
		$control->snippetMode = FALSE;
		$payload = $control->getPresenter()->getPayload();
		if (isset($local->blocks)) {
			foreach ($local->blocks as $name => $function) {
				if ($name[0] !== '_' || !$control->isControlInvalid(substr($name, 1))) {
					continue;
				}
				ob_start();
				$function = reset($function);
				$snippets = $function($local, $params);
				$payload->snippets[$id = $control->getSnippetId(substr($name, 1))] = ob_get_clean();
				if ($snippets) {
					$payload->snippets += $snippets;
					unset($payload->snippets[$id]);
			}
		}
		}
		if ($control instanceof Nette\Application\UI\Control) {
			foreach ($control->getComponents(FALSE, 'Nette\Application\UI\Control') as $child) {
				if ($child->isControlInvalid()) {
					$child->snippetMode = TRUE;
					$child->render();
					$child->snippetMode = FALSE;
				}
			}
		}
	}

}

}

namespace Nette\Utils {

use Nette;use Nette\Utils\Strings;

class Tokenizer extends Nette\Object
{

	public $tokens;

	public $position = 0;

	public $ignored = array();

	private $input;

	private $re;

	private $types;

	public $current;

	function __construct(array $patterns, $flags = '')
	{
		$this->re = '~(' . implode(')|(', $patterns) . ')~A' . $flags;
		$keys = array_keys($patterns);
		$this->types = $keys === range(0, count($patterns) - 1) ? FALSE : $keys;
	}

	function tokenize($input)
	{
		$this->input = $input;
		if ($this->types) {
			$this->tokens = Strings::matchAll($input, $this->re);
			$len = 0;
			$count = count($this->types);
			$line = 1;
			foreach ($this->tokens as & $match) {
				$type = NULL;
				for ($i = 1; $i <= $count; $i++) {
					if (!isset($match[$i])) {
						break;
					} elseif ($match[$i] != NULL) {
						$type = $this->types[$i - 1]; break;
					}
				}
				$match = self::createToken($match[0], $type, $line);
				$len += strlen($match['value']);
				$line += substr_count($match['value'], "\n");
			}
			if ($len !== strlen($input)) {
				$errorOffset = $len;
			}

		} else {
			$this->tokens = Strings::split($input, $this->re, PREG_SPLIT_NO_EMPTY);
			if ($this->tokens && !Strings::match(end($this->tokens), $this->re)) {
				$tmp = Strings::split($this->input, $this->re, PREG_SPLIT_NO_EMPTY | PREG_SPLIT_OFFSET_CAPTURE);
				list(, $errorOffset) = end($tmp);
			}
		}

		if (isset($errorOffset)) {
			$line = $errorOffset ? substr_count($this->input, "\n", 0, $errorOffset) + 1 : 1;
			$col = $errorOffset - strrpos(substr($this->input, 0, $errorOffset), "\n") + 1;
			$token = str_replace("\n", '\n', substr($input, $errorOffset, 10));
			throw new TokenizerException("Unexpected '$token' on line $line, column $col.");
		}
		return $this->tokens;
	}

	static function createToken($value, $type = NULL, $line = NULL)
	{
		return array('value' => $value, 'type' => $type, 'line' => $line);
	}

	function getOffset($i)
	{
		$tokens = Strings::split($this->input, $this->re, PREG_SPLIT_NO_EMPTY | PREG_SPLIT_OFFSET_CAPTURE);
		$offset = isset($tokens[$i]) ? $tokens[$i][1] : strlen($this->input);
		return array(
			$offset,
			($offset ? substr_count($this->input, "\n", 0, $offset) + 1 : 1),
			$offset - strrpos(substr($this->input, 0, $offset), "\n"),
		);
	}

	function fetch()
	{
		$args = func_get_args();
		return $this->scan($args, TRUE);
	}

	function fetchToken()
	{ 
		$args = func_get_args();
		return $this->scan($args, TRUE) === FALSE ? FALSE : $this->current;
	}

	function fetchAll()
	{
		$args = func_get_args();
		return $this->scan($args, FALSE);
	}

	function fetchUntil($arg)
	{
		$args = func_get_args();
		return $this->scan($args, FALSE, TRUE, TRUE);
	}

	function isNext($arg)
	{
		$args = func_get_args();
		return (bool) $this->scan($args, TRUE, FALSE);
	}

	function isPrev($arg)
	{
		$args = func_get_args();
		return (bool) $this->scan($args, TRUE, FALSE, FALSE, TRUE);
	}

	function hasNext()
	{
		return isset($this->tokens[$this->position]);
	}

	function hasPrev()
	{
		return $this->position > 1;
	}

	function isCurrent($arg)
	{
		$args = func_get_args();
		if (is_array($this->current)) {
			return in_array($this->current['value'], $args, TRUE)
				|| in_array($this->current['type'], $args, TRUE);
		} else {
			return in_array($this->current, $args, TRUE);
		}
	}

	private function scan($wanted, $first, $advance = TRUE, $neg = FALSE, $prev = FALSE)
	{
		$res = FALSE;
		$pos = $this->position + ($prev ? -2 : 0);
		while (isset($this->tokens[$pos])) {
			$token = $this->tokens[$pos];
			$pos += $prev ? -1 : 1;
			$value = is_array($token) ? $token['value'] : $token;
			$type = is_array($token) ? $token['type'] : $token;
			if (!$wanted || (in_array($value, $wanted, TRUE) || in_array($type, $wanted, TRUE)) ^ $neg) {
				if ($advance) {
					$this->position = $pos;
					$this->current = $token;
				}
				$res .= $value;
				if ($first) {
					break;
				}

			} elseif ($neg || !in_array($type, $this->ignored, TRUE)) {
				break;
			}
		}
		return $res;
	}

}

class TokenizerException extends \Exception
{
}

}

namespace Nette\Latte {

use Nette;

class MacroTokenizer extends Nette\Utils\Tokenizer
{
	const T_WHITESPACE = 1,
		T_COMMENT = 2,
		T_SYMBOL = 3,
		T_NUMBER = 4,
		T_VARIABLE = 5,
		T_STRING = 6,
		T_CAST = 7,
		T_KEYWORD = 8,
		T_CHAR = 9;

	function __construct($input)
	{
		parent::__construct(array(
			self::T_WHITESPACE => '\s+',
			self::T_COMMENT => '(?s)/\*.*?\*/',
			self::T_STRING => Parser::RE_STRING,
			self::T_KEYWORD => '(?:true|false|null|and|or|xor|clone|new|instanceof|return|continue|break|[A-Z_][A-Z0-9_]{2,})(?![\w\pL_])',
			self::T_CAST => '\([a-z]+\)',
			self::T_VARIABLE => '\$[\w\pL_]+',
			self::T_NUMBER => '[+-]?[0-9]+(?:\.[0-9]+)?(?:e[0-9]+)?',
			self::T_SYMBOL => '[\w\pL_]+(?:-[\w\pL_]+)*',
			self::T_CHAR => '::|=>|[^"\']',
		), 'u');
		$this->ignored = array(self::T_COMMENT, self::T_WHITESPACE);
		$this->tokenize($input);
	}

	function fetchWord()
	{
		$word = $this->fetchUntil(self::T_WHITESPACE, ',');
		$this->fetch(',');
		$this->fetchAll(self::T_WHITESPACE, self::T_COMMENT);
		return $word;
	}

}

}

namespace Nette\Templating {

use Nette;

class FilterException extends Nette\InvalidStateException
{

	public $sourceFile;

	public $sourceLine;

	function __construct($message, $code = 0, $sourceLine = 0)
	{
		$this->sourceLine = (int) $sourceLine;
		parent::__construct($message, $code);
	}

	function setSourceFile($file)
	{
		$this->sourceFile = (string) $file;
		$this->message = rtrim($this->message, '.') . " in " . str_replace(dirname(dirname($file)), '...', $file)
			. ($this->sourceLine ? ":$this->sourceLine" : '');
	}

}

}

namespace Nette\Latte {

use Nette;

class ParseException extends Nette\Templating\FilterException
{
}

use Nette\Utils\Strings;

class Parser extends Nette\Object
{

	const RE_STRING = '\'(?:\\\\.|[^\'\\\\])*\'|"(?:\\\\.|[^"\\\\])*"';

	const N_PREFIX = 'n:';

	private $macroRe;

	private $input;

	private $output;

	private $offset;

	private $macros;

	private $macroHandlers;

	private $htmlNodes = array();

	private $macroNodes = array();

	public $context;

	public $templateId;

	const CONTEXT_TEXT = 'text',
		CONTEXT_CDATA = 'cdata',
		CONTEXT_TAG = 'tag',
		CONTEXT_ATTRIBUTE = 'attribute',
		CONTEXT_NONE = 'none',
		CONTEXT_COMMENT = 'comment';

	function __construct()
	{
		$this->macroHandlers = new \SplObjectStorage;
		$this->setDelimiters('\\{(?![\\s\'"{}])', '\\}');
		$this->context = array(self::CONTEXT_NONE, 'text');
	}

	function addMacro($name, IMacro $macro)
	{
		$this->macros[$name][] = $macro;
		$this->macroHandlers->attach($macro);
		return $this;
	}

	function parse($s)
	{
		if (!Strings::checkEncoding($s)) {
			throw new ParseException('Template is not valid UTF-8 stream.');
		}
		$s = str_replace("\r\n", "\n", $s);

		$this->templateId = Strings::random();
		$this->input = & $s;
		$this->offset = 0;
		$this->output = '';
		$this->htmlNodes = $this->macroNodes = array();

		foreach ($this->macroHandlers as $handler) {
			$handler->initialize($this);
		}

		$len = strlen($s);

		try {
			while ($this->offset < $len) {
				$matches = $this->{"context".$this->context[0]}();

				if (!$matches) {
					break;

				} elseif (!empty($matches['comment'])) {

				} elseif (!empty($matches['macro'])) {
					list($macroName, $macroArgs, $macroModifiers) = $this->parseMacro($matches['macro']);
					$isRightmost = $this->offset >= $len || $this->input[$this->offset] === "\n";
					$this->writeMacro($macroName, $macroArgs, $macroModifiers, $isRightmost);

				} else {
					$this->output .= $matches[0];
				}
			}
		} catch (ParseException $e) {
			if (!$e->sourceLine) {
				$e->sourceLine = $this->getLine();
			}
			throw $e;
		}

		$this->output .= substr($this->input, $this->offset);

		foreach ($this->htmlNodes as $node) {
			if (!empty($node->attrs)) {
				throw new ParseException("Missing end tag </$node->name> for macro-attribute " . self::N_PREFIX
					. implode(' and ' . self::N_PREFIX, array_keys($node->attrs)) . ".", 0, $this->getLine());
			}
		}

		$prologs = $epilogs = '';
		foreach ($this->macroHandlers as $handler) {
			$res = $handler->finalize();
			$prologs .= isset($res[0]) ? "<?php $res[0]\n?>" : '';
			$epilogs .= isset($res[1]) ? "<?php $res[1]\n?>" : '';
		}
		$this->output = ($prologs ? $prologs . "<?php\n//\n// main template\n//\n?>\n" : '') . $this->output . $epilogs;

		if ($this->macroNodes) {
			throw new ParseException("There are unclosed macros.", 0, $this->getLine());
		}

		return $this->output;
	}

	private function contextText()
	{
		$matches = $this->match('~
			(?:(?<=\n|^)[ \t]*)?<(?P<closing>/?)(?P<tag>[a-z0-9:]+)|  ##  begin of HTML tag <tag </tag - ignores <!DOCTYPE
			<(?P<htmlcomment>!--)|           ##  begin of HTML comment <!--
			'.$this->macroRe.'           ##  curly tag
		~xsi');

		if (!$matches || !empty($matches['macro']) || !empty($matches['comment'])) {

		} elseif (!empty($matches['htmlcomment'])) {
			$this->context = array(self::CONTEXT_COMMENT);

		} elseif (empty($matches['closing'])) {
			$this->htmlNodes[] = $node = new HtmlNode($matches['tag']);
			$node->offset = strlen($this->output);
			$this->context = array(self::CONTEXT_TAG);

		} else {
			do {
				$node = array_pop($this->htmlNodes);
				if (!$node) {
					$node = new HtmlNode($matches['tag']);
				}
			} while (strcasecmp($node->name, $matches['tag']));
			$this->htmlNodes[] = $node;
			$node->closing = TRUE;
			$node->offset = strlen($this->output);
			$this->context = array(self::CONTEXT_TAG);
		}
		return $matches;
	}

	private function contextCData()
	{
		$node = end($this->htmlNodes);
		$matches = $this->match('~
			</'.$node->name.'(?![a-z0-9:])| ##  end HTML tag </tag
			'.$this->macroRe.'           ##  curly tag
		~xsi');

		if ($matches && empty($matches['macro']) && empty($matches['comment'])) {
			$node->closing = TRUE;
			$node->offset = strlen($this->output);
			$this->context = array(self::CONTEXT_TAG);
		}
		return $matches;
	}

	private function contextTag()
	{
		$matches = $this->match('~
			(?P<end>\ ?/?>)(?P<tagnewline>[ \t]*\n)?|  ##  end of HTML tag
			'.$this->macroRe.'|          ##  curly tag
			\s*(?P<attr>[^\s/>={]+)(?:\s*=\s*(?P<value>["\']|[^\s/>{]+))? ## begin of HTML attribute
		~xsi');

		if (!$matches || !empty($matches['macro']) || !empty($matches['comment'])) {

		} elseif (!empty($matches['end'])) {
			$node = end($this->htmlNodes);
			$isEmpty = !$node->closing && (strpos($matches['end'], '/') !== FALSE || $node->isEmpty);

			if ($isEmpty) {
				$matches[0] = (Nette\Utils\Html::$xhtml ? ' />' : '>')
					. (isset($matches['tagnewline']) ? $matches['tagnewline'] : '');
			}

			if (!empty($node->attrs)) {
				$code = substr($this->output, $node->offset) . $matches[0];
				$this->output = substr($this->output, 0, $node->offset);
				$this->writeAttrsMacro($code, $node->attrs, $node->closing);
				if ($isEmpty) {
					$this->writeAttrsMacro('', $node->attrs, TRUE);
				}
				$matches[0] = '';
			}

			if ($isEmpty) {
				$node->closing = TRUE;
			}

			if (!$node->closing && (strcasecmp($node->name, 'script') === 0 || strcasecmp($node->name, 'style') === 0)) {
				$this->context = array(self::CONTEXT_CDATA, strcasecmp($node->name, 'style') ? 'js' : 'css');
			} else {
				$this->context = array(self::CONTEXT_TEXT);
				if ($node->closing) {
					array_pop($this->htmlNodes);
				}
			}

		} else {
			$name = $matches['attr'];
			$value = isset($matches['value']) ? $matches['value'] : '';
			$node = end($this->htmlNodes);

			if (Strings::startsWith($name, self::N_PREFIX)) {
				$name = substr($name, strlen(self::N_PREFIX));
				if ($value === '"' || $value === "'") {
					if ($matches = $this->match('~(.*?)' . $value . '~xsi')) {
						$value = $matches[1];
					}
				}
				$node->attrs[$name] = $value;
				$matches[0] = '';

			} elseif ($value === '"' || $value === "'") {
				$this->context = array(self::CONTEXT_ATTRIBUTE, $name, $value);
			}
		}
		return $matches;
	}

	private function contextAttribute()
	{
		$matches = $this->match('~
			(' . $this->context[2] . ')|      ##  1) end of HTML attribute
			'.$this->macroRe.'                ##  curly tag
		~xsi');

		if ($matches && empty($matches['macro']) && empty($matches['comment'])) {
			$this->context = array(self::CONTEXT_TAG);
		}
		return $matches;
	}

	private function contextComment()
	{
		$matches = $this->match('~
			(--\s*>)|                    ##  1) end of HTML comment
			'.$this->macroRe.'           ##  curly tag
		~xsi');

		if ($matches && empty($matches['macro']) && empty($matches['comment'])) {
			$this->context = array(self::CONTEXT_TEXT);
		}
		return $matches;
	}

	private function contextNone()
	{
		$matches = $this->match('~
			'.$this->macroRe.'           ##  curly tag
		~xsi');
		return $matches;
	}

	private function match($re)
	{
		if ($matches = Strings::match($this->input, $re, PREG_OFFSET_CAPTURE, $this->offset)) {
			$this->output .= substr($this->input, $this->offset, $matches[0][1] - $this->offset);
			$this->offset = $matches[0][1] + strlen($matches[0][0]);
			foreach ($matches as $k => $v) $matches[$k] = $v[0];
		}
		return $matches;
	}

	function getLine()
	{
		return $this->input && $this->offset ? substr_count($this->input, "\n", 0, $this->offset - 1) + 1 : NULL;
	}

	function setDelimiters($left, $right)
	{
		$this->macroRe = '
			(?P<comment>' . $left . '\\*.*?\\*' . $right . '\n{0,2})|
			' . $left . '
				(?P<macro>(?:' . self::RE_STRING . '|[^\'"]+?)*?)
			' . $right . '
			(?P<rmargin>[ \t]*(?=\n))?
		';
		return $this;
	}

	function writeMacro($name, $args = NULL, $modifiers = NULL, $isRightmost = FALSE)
	{
		$isLeftmost = trim(substr($this->output, $leftOfs = strrpos("\n$this->output", "\n"))) === '';

		if ($name[0] === '/') {
			$node = end($this->macroNodes);

			if (!$node || ("/$node->name" !== $name && '/' !== $name) || $modifiers
				|| ($args && $node->args && !Strings::startsWith("$node->args ", "$args "))
			) {
				$name .= $args ? ' ' : '';
				throw new ParseException("Unexpected macro {{$name}{$args}{$modifiers}}"
					. ($node ? ", expecting {/$node->name}" . ($args && $node->args ? " or eventually {/$node->name $node->args}" : '') : ''),
					0, $this->getLine());
			}

			array_pop($this->macroNodes);
			if (!$node->args) {
				$node->setArgs($args);
			}
			if ($isLeftmost && $isRightmost) {
				$this->output = substr($this->output, 0, $leftOfs);
			}

			$code = $node->close(substr($this->output, $node->offset));

			if (!$isLeftmost && $isRightmost && substr($code, -2) === '?>') {
				$code .= "\n";
			}
			$this->output = substr($this->output, 0, $node->offset) . $node->content. $code;

		} else {
			list($node, $code) = $this->expandMacro($name, $args, $modifiers);
			if (!$node->isEmpty) {
				$this->macroNodes[] = $node;
			}

			if ($isRightmost) {
				if ($isLeftmost && substr($code, 0, 11) !== '<?php echo ') {
					$this->output = substr($this->output, 0, $leftOfs);
				} elseif (substr($code, -2) === '?>') {
					$code .= "\n";
				}
			}

			$this->output .= $code;
			$node->offset = strlen($this->output);
		}
	}

	function writeAttrsMacro($code, $attrs, $closing)
	{
		$left = $right = array();
		foreach ($this->macros as $name => $foo) {
			if ($name[0] === '@') {
				$name = substr($name, 1);
				if (isset($attrs[$name])) {
					if (!$closing) {
						$pos = strrpos($code, '>');
						if ($code[$pos-1] === '/') {
							$pos--;
						}
						list(, $macroCode) = $this->expandMacro("@$name", $attrs[$name]);
						$code = substr_replace($code, $macroCode, $pos, 0);
					}
					unset($attrs[$name]);
				}
			}

			$macro = $closing ? "/$name" : $name;
			if (isset($attrs[$name])) {
				if ($closing) {
					$right[] = array($macro, '');
				} else {
					array_unshift($left, array($macro, $attrs[$name]));
				}
			}

			$innerName = "inner-$name";
			if (isset($attrs[$innerName])) {
				if ($closing) {
					$left[] = array($macro, '');
				} else {
					array_unshift($right, array($macro, $attrs[$innerName]));
				}
			}

			$tagName = "tag-$name";
			if (isset($attrs[$tagName])) {
				array_unshift($left, array($name, $attrs[$tagName]));
				$right[] = array("/$name", '');
			}

			unset($attrs[$name], $attrs[$innerName], $attrs[$tagName]);
		}

		if ($attrs) {
			throw new ParseException("Unknown macro-attribute " . self::N_PREFIX
				. implode(' and ' . self::N_PREFIX, array_keys($attrs)), 0, $this->getLine());
		}

		foreach ($left as $item) {
			$this->writeMacro($item[0], $item[1]);
			if (substr($this->output, -2) === '?>') {
				$this->output .= "\n";
			}
		}
		$this->output .= $code;

		foreach ($right as $item) {
			$this->writeMacro($item[0], $item[1]);
			if (substr($this->output, -2) === '?>') {
				$this->output .= "\n";
			}
		}
	}

	function expandMacro($name, $args, $modifiers = NULL)
	{
		if (empty($this->macros[$name])) {
			throw new ParseException("Unknown macro {{$name}}", 0, $this->getLine());
		}
		foreach (array_reverse($this->macros[$name]) as $macro) {
			$node = new MacroNode($macro, $name, $args, $modifiers, $this->macroNodes ? end($this->macroNodes) : NULL);
			$code = $macro->nodeOpened($node);
			if ($code !== FALSE) {
				return array($node, $code);
			}
		}
		throw new ParseException("Unhandled macro {{$name}}", 0, $this->getLine());
	}

	function parseMacro($macro)
	{
		$match = Strings::match($macro, '~^
			(
				(?P<name>\?|/?[a-z]\w*+(?:[.:]\w+)*+(?!::|\())|   ## ?, name, /name, but not function( or class::
				(?P<noescape>!?)(?P<shortname>/?[=\~#%^&_]?)      ## [!] [=] expression to print
			)(?P<args>.*?)
			(?P<modifiers>\|[a-z](?:'.Parser::RE_STRING.'|[^\'"]+)*)?
		()$~isx');

		if (!$match) {
			return FALSE;
		}
		if ($match['name'] === '') {
			$match['name'] = $match['shortname'] ?: '=';
			if (!$match['noescape'] && substr($match['shortname'], 0, 1) !== '/') {
				$match['modifiers'] .= '|escape';
			}
		}
		return array($match['name'], trim($match['args']), $match['modifiers']);
	}

}

class PhpWriter extends Nette\Object
{

	private $argsTokenizer;

	private $modifiers;

	private $context;

	static function using(MacroNode $node, $context = NULL)
	{
		return new static($node->tokenizer, $node->modifiers, $context);
	}

	function __construct(MacroTokenizer $argsTokenizer, $modifiers = NULL, $context = NULL)
	{
		$this->argsTokenizer = $argsTokenizer;
		$this->modifiers = $modifiers;
		$this->context = $context;
	}

	function write($mask)
	{
		$args = func_get_args();
		array_shift($args);
		$word = strpos($mask, '%node.word') === FALSE ? NULL : $this->argsTokenizer->fetchWord();
		$me = $this;
		$mask = Nette\Utils\Strings::replace($mask, '#%escape(\(([^()]*+|(?1))+\))#', function($m) use($me) {
			return $me->escape(substr($m[1], 1, -1));
		});
		$mask = Nette\Utils\Strings::replace($mask, '#%modify(\(([^()]*+|(?1))+\))#', function($m) use($me) {
			return $me->formatModifiers(substr($m[1], 1, -1));
		});

		return Nette\Utils\Strings::replace($mask, '#([,+]\s*)?%(node\.word|node\.array|node\.args|var|raw)(\?)?(\s*\+\s*)?()#',
			function($m) use($me, $word, & $args) {
			list(, $l, $macro, $cond, $r) = $m;

			switch ($macro) {
			case 'node.word':
				$code = $me->formatWord($word); break;
			case 'node.args':
				$code = $me->formatArgs(); break;
			case 'node.array':
				$code = $me->formatArray();
				$code = $cond && $code === 'array()' ? '' : $code; break;
			case 'var':
				$code = var_export(array_shift($args), TRUE); break;
			case 'raw':
				$code = (string) array_shift($args); break;
			}

			if ($cond && $code === '') {
				return $r ? $l : $r;
			} else {
				return $l . $code . $r;
			}
		});
	}

	function formatModifiers($var)
	{
		$modifiers = ltrim($this->modifiers, '|');
		if (!$modifiers) {
			return $var;
		}

		$tokenizer = $this->preprocess(new MacroTokenizer($modifiers));
		$inside = FALSE;
		while ($token = $tokenizer->fetchToken()) {
			if ($token['type'] === MacroTokenizer::T_WHITESPACE) {
				$var = rtrim($var) . ' ';

			} elseif (!$inside) {
				if ($token['type'] === MacroTokenizer::T_SYMBOL) {
					if ($this->context && $token['value'] === 'escape') {
						$var = $this->escape($var);
						$tokenizer->fetch('|');
					} else {
						$var = "\$template->" . $token['value'] . "($var";
						$inside = TRUE;
					}
				} else {
					throw new ParseException("Modifier name must be alphanumeric string, '$token[value]' given.");
				}
			} else {
				if ($token['value'] === ':' || $token['value'] === ',') {
					$var = $var . ', ';

				} elseif ($token['value'] === '|') {
					$var = $var . ')';
					$inside = FALSE;

				} else {
					$var .= $this->canQuote($tokenizer) ? "'$token[value]'" : $token['value'];
				}
			}
		}
		return $inside ? "$var)" : $var;
	}

	function formatArgs()
	{
		$out = '';
		$tokenizer = $this->preprocess();
		while ($token = $tokenizer->fetchToken()) {
			$out .= $this->canQuote($tokenizer) ? "'$token[value]'" : $token['value'];
		}
		return $out;
	}

	function formatArray()
	{
		$out = '';
		$expand = NULL;
		$tokenizer = $this->preprocess();
		while ($token = $tokenizer->fetchToken()) {
			if ($token['value'] === '(expand)' && $token['depth'] === 0) {
				$expand = TRUE;
				$out .= '),';

			} elseif ($expand && ($token['value'] === ',') && !$token['depth']) {
				$expand = FALSE;
				$out .= ', array(';
			} else {
				$out .= $this->canQuote($tokenizer) ? "'$token[value]'" : $token['value'];
			}
		}
		if ($expand === NULL) {
			return "array($out)";
		} else {
			return "array_merge(array($out" . ($expand ? ', array(' : '') ."))";
		}
	}

	function formatWord($s)
	{
		return (is_numeric($s) || strspn($s, '\'"$') || in_array(strtolower($s), array('true', 'false', 'null')))
			? $s : '"' . $s . '"';
	}

	function canQuote($tokenizer)
	{
		return $tokenizer->isCurrent(MacroTokenizer::T_SYMBOL)
			&& (!$tokenizer->hasPrev() || $tokenizer->isPrev(',', '(', '[', '=', '=>', ':', '?'))
			&& (!$tokenizer->hasNext() || $tokenizer->isNext(',', ')', ']', '=', '=>', ':', '|'));
	}

	function preprocess(MacroTokenizer $tokenizer = NULL)
	{
		$tokenizer = $tokenizer === NULL ? $this->argsTokenizer : $tokenizer;
		$inTernary = $prev = NULL;
		$tokens = $arrays = array();
		while ($token = $tokenizer->fetchToken()) {
			$token['depth'] = $depth = count($arrays);

			if ($token['type'] === MacroTokenizer::T_COMMENT) {
				continue;

			} elseif ($token['type'] === MacroTokenizer::T_WHITESPACE) {
				$tokens[] = $token;
				continue;
			}

			if ($token['value'] === '?') {
				$inTernary = $depth;

			} elseif ($token['value'] === ':') {
				$inTernary = NULL;

			} elseif ($inTernary === $depth && ($token['value'] === ',' || $token['value'] === ')' || $token['value'] === ']')) {
				$tokens[] = MacroTokenizer::createToken(':') + array('depth' => $depth);
				$tokens[] = MacroTokenizer::createToken('null') + array('depth' => $depth);
				$inTernary = NULL;
			}

			if ($token['value'] === '[') {
				if ($arrays[] = $prev['value'] !== ']' && $prev['type'] !== MacroTokenizer::T_SYMBOL && $prev['type'] !== MacroTokenizer::T_VARIABLE) {
					$tokens[] = MacroTokenizer::createToken('array') + array('depth' => $depth);
					$token = MacroTokenizer::createToken('(');
				}
			} elseif ($token['value'] === ']') {
				if (array_pop($arrays) === TRUE) {
					$token = MacroTokenizer::createToken(')');
				}
			} elseif ($token['value'] === '(') {
				$arrays[] = '(';

			} elseif ($token['value'] === ')') {
				array_pop($arrays);
			}

			$tokens[] = $prev = $token;
		}

		if ($inTernary !== NULL) {
			$tokens[] = MacroTokenizer::createToken(':') + array('depth' => count($arrays));
			$tokens[] = MacroTokenizer::createToken('null') + array('depth' => count($arrays));
		}

		$tokenizer = clone $tokenizer;
		$tokenizer->position = 0;
		$tokenizer->tokens = $tokens;
		return $tokenizer;
	}

	function escape($s)
	{
		switch ($this->context[0]) {
		case Parser::CONTEXT_TEXT:
			return "Nette\\Templating\\DefaultHelpers::escapeHtml($s, ENT_NOQUOTES)";
		case Parser::CONTEXT_TAG:
			return "Nette\\Templating\\DefaultHelpers::escapeHtml($s)";
		case Parser::CONTEXT_ATTRIBUTE:
			list(, $name, $quote) = $this->context;
			$quote = $quote === '"' ? '' : ', ENT_QUOTES';
			if (strncasecmp($name, 'on', 2) === 0) {
				return "htmlSpecialChars(Nette\\Templating\\DefaultHelpers::escapeJs($s)$quote)";
			} elseif ($name === 'style') {
				return "htmlSpecialChars(Nette\\Templating\\DefaultHelpers::escapeCss($s)$quote)";
			} else {
				return "htmlSpecialChars($s$quote)";
			}
		case Parser::CONTEXT_COMMENT:
			return "Nette\\Templating\\DefaultHelpers::escapeHtmlComment($s)";
		case Parser::CONTEXT_CDATA;
			return 'Nette\Templating\DefaultHelpers::escape' . ucfirst($this->context[1]) . "($s)";
		case Parser::CONTEXT_NONE:
			switch (isset($this->context[1]) ? $this->context[1] : NULL) {
			case 'xml':
			case 'js':
			case 'css':
				return 'Nette\Templating\DefaultHelpers::escape' . ucfirst($this->context[1]) . "($s)";
			case 'text':
				return $s;
			default:
				return "\$template->escape($s)";
			}
		}
	}

}

}

namespace Nette\Loaders {

use Nette;use Nette\Utils\Strings;use Nette\Caching\Cache;

class RobotLoader extends AutoLoader
{

	public $scanDirs;

	public $ignoreDirs = '.*, *.old, *.bak, *.tmp, temp';

	public $acceptFiles = '*.php, *.php5';

	public $autoRebuild = TRUE;

	private $list = array();

	private $files;

	private $rebuilt = FALSE;

	private $cacheStorage;

	function __construct()
	{
		if (!extension_loaded('tokenizer')) {
			throw new Nette\NotSupportedException("PHP extension Tokenizer is not loaded.");
		}
	}

	function register()
	{
		$cache = $this->getCache();
		$key = $this->getKey();
		if (isset($cache[$key])) {
			$this->list = $cache[$key];
		} else {
			$this->rebuild();
		}

		if (isset($this->list[strtolower(__CLASS__)]) && class_exists('Nette\Loaders\NetteLoader', FALSE)) {
			NetteLoader::getInstance()->unregister();
		}

		parent::register();
	}

	function tryLoad($type)
	{
		$type = ltrim(strtolower($type), '\\');

		if (isset($this->list[$type][0]) && !is_file($this->list[$type][0])) {
			unset($this->list[$type]);
		}

		if (!isset($this->list[$type])) {
			$trace = debug_backtrace();
			$initiator = & $trace[2]['function'];
			if ($initiator === 'class_exists' || $initiator === 'interface_exists') {
				$this->list[$type] = FALSE;
				if ($this->autoRebuild && $this->rebuilt) {
					$this->getCache()->save($this->getKey(), $this->list, array(
						Cache::CONSTS => 'Nette\Framework::REVISION',
					));
				}
			}

			if ($this->autoRebuild && !$this->rebuilt) {
				$this->rebuild();
			}
		}

		if (isset($this->list[$type][0])) {
			Nette\Utils\LimitedScope::load($this->list[$type][0]);
			self::$count++;
		}
	}

	function rebuild()
	{
		$this->getCache()->save($this->getKey(), callback($this, '_rebuildCallback'), array(
			Cache::CONSTS => 'Nette\Framework::REVISION',
		));
		$this->rebuilt = TRUE;
	}

	function _rebuildCallback()
	{
		foreach ($this->list as $pair) {
			if ($pair) {
				$this->files[$pair[0]] = $pair[1];
			}
		}
		foreach (array_unique($this->scanDirs) as $dir) {
			$this->scanDirectory($dir);
		}
		$this->files = NULL;
		return $this->list;
	}

	function getIndexedClasses()
	{
		$res = array();
		foreach ($this->list as $class => $pair) {
			if ($pair) {
				$res[$pair[2]] = $pair[0];
			} 
		}
		return $res;
	}

	function addDirectory($path)
	{
		foreach ((array) $path as $val) {
			$real = realpath($val);
			if ($real === FALSE) {
				throw new Nette\DirectoryNotFoundException("Directory '$val' not found.");
			}
			$this->scanDirs[] = $real;
		}
		return $this;
	}

	private function addClass($class, $file, $time)
	{
		$lClass = strtolower($class);
		if (isset($this->list[$lClass][0]) && ($file2 = $this->list[$lClass][0]) !== $file && is_file($file2)) {
			if ($this->files[$file2] !== filemtime($file2)) {
				$this->scanScript($file2);
				return $this->addClass($class, $file, $time);
			}
			$e = new Nette\InvalidStateException("Ambiguous class '$class' resolution; defined in $file and in " . $this->list[$lClass][0] . ".");
			{
				throw $e;
			}
		}
		$this->list[$lClass] = array($file, $time, $class);
		$this->files[$file] = $time;
	}

	private function scanDirectory($dir)
	{
		if (is_dir($dir)) {
			$ignoreDirs = is_array($this->ignoreDirs) ? $this->ignoreDirs : Strings::split($this->ignoreDirs, '#[,\s]+#');
			$disallow = array();
			foreach ($ignoreDirs as $item) {
				if ($item = realpath($item)) {
					$disallow[$item] = TRUE;
				}
			}
			$iterator = Nette\Utils\Finder::findFiles(is_array($this->acceptFiles) ? $this->acceptFiles : Strings::split($this->acceptFiles, '#[,\s]+#'))
				->filter(function($file) use(&$disallow){
					return !isset($disallow[$file->getPathname()]);
				})
				->from($dir)
				->exclude($ignoreDirs)
				->filter($filter = function($dir) use(&$disallow){
					$path = $dir->getPathname();
					if (is_file("$path/netterobots.txt")) {
						foreach (file("$path/netterobots.txt") as $s) {
							if ($matches = Strings::match($s, '#^disallow\\s*:\\s*(\\S+)#i')) {
								$disallow[$path . str_replace('/', DIRECTORY_SEPARATOR, rtrim('/' . ltrim($matches[1], '/'), '/'))] = TRUE;
							}
						}
					}
					return !isset($disallow[$path]);
				});
			$filter(new \SplFileInfo($dir));
		} else {
			$iterator = new \ArrayIterator(array(new \SplFileInfo($dir)));
		}

		foreach ($iterator as $entry) {
			$path = $entry->getPathname();
			if (!isset($this->files[$path]) || $this->files[$path] !== $entry->getMTime()) {
				$this->scanScript($path);
			}
		}
	}

	private function scanScript($file)
	{
		$T_NAMESPACE = PHP_VERSION_ID < 50300 ? -1 : T_NAMESPACE;
		$T_NS_SEPARATOR = PHP_VERSION_ID < 50300 ? -1 : T_NS_SEPARATOR;

		$expected = FALSE;
		$namespace = '';
		$level = $minLevel = 0;
		$time = filemtime($file);
		$s = file_get_contents($file);

		foreach ($this->list as $class => $pair) {
			if ($pair && $pair[0] === $file) {
				unset($this->list[$class]);
			}
		}

		if ($matches = Strings::match($s, '#//nette'.'loader=(\S*)#')) {
			foreach (explode(',', $matches[1]) as $name) {
				$this->addClass($name, $file, $time);
			}
			return;
		}

		foreach (token_get_all($s) as $token) {
			if (is_array($token)) {
				switch ($token[0]) {
				case T_COMMENT:
				case T_DOC_COMMENT:
				case T_WHITESPACE:
					continue 2;

				case $T_NS_SEPARATOR:
				case T_STRING:
					if ($expected) {
						$name .= $token[1];
					}
					continue 2;

				case $T_NAMESPACE:
				case T_CLASS:
				case T_INTERFACE:
					$expected = $token[0];
					$name = '';
					continue 2;
				case T_CURLY_OPEN:
				case T_DOLLAR_OPEN_CURLY_BRACES:
					$level++;
				}
			}

			if ($expected) {
				switch ($expected) {
				case T_CLASS:
				case T_INTERFACE:
					if ($level === $minLevel) {
						$this->addClass($namespace . $name, $file, $time);
					}
					break;

				case $T_NAMESPACE:
					$namespace = $name ? $name . '\\' : '';
					$minLevel = $token === '{' ? 1 : 0;
				}

				$expected = NULL;
			}

			if ($token === '{') {
				$level++;
			} elseif ($token === '}') {
				$level--;
			}
		}
	}

	function setCacheStorage(Nette\Caching\IStorage $storage)
	{
		$this->cacheStorage = $storage;
		return $this;
	}

	function getCacheStorage()
	{
		return $this->cacheStorage;
	}

	protected function getCache()
	{
		if (!$this->cacheStorage) {
			trigger_error('Missing cache storage.', E_USER_WARNING);
			$this->cacheStorage = new Nette\Caching\Storages\DevNullStorage;
		}
		return new Cache($this->cacheStorage, 'Nette.RobotLoader');
	}

	protected function getKey()
	{
		return array($this->ignoreDirs, $this->acceptFiles, $this->scanDirs);
	}

}

}

namespace Nette\Mail {

use Nette;use Nette\Utils\Strings;

class MimePart extends Nette\Object
{

	const ENCODING_BASE64 = 'base64',
		ENCODING_7BIT = '7bit',
		ENCODING_8BIT = '8bit',
		ENCODING_QUOTED_PRINTABLE = 'quoted-printable';

	const EOL = "\r\n";
	const LINE_LENGTH = 76;

	private $headers = array();

	private $parts = array();

	private $body;

	function setHeader($name, $value, $append = FALSE)
	{
		if (!$name || preg_match('#[^a-z0-9-]#i', $name)) {
			throw new Nette\InvalidArgumentException("Header name must be non-empty alphanumeric string, '$name' given.");
		}

		if ($value == NULL) {
			if (!$append) {
				unset($this->headers[$name]);
			}

		} elseif (is_array($value)) {
			$tmp = & $this->headers[$name];
			if (!$append || !is_array($tmp)) {
				$tmp = array();
			}

			foreach ($value as $email => $name) {
				if ($name !== NULL && !Strings::checkEncoding($name)) {
					throw new Nette\InvalidArgumentException("Name is not valid UTF-8 string.");
				}

				if (!preg_match('#^[^@",\s]+@[^@",\s]+\.[a-z]{2,10}$#i', $email)) {
					throw new Nette\InvalidArgumentException("Email address '$email' is not valid.");
				}

				if (preg_match('#[\r\n]#', $name)) {
					throw new Nette\InvalidArgumentException("Name must not contain line separator.");
				}
				$tmp[$email] = $name;
			}

		} else {
			$value = (string) $value;
			if (!Strings::checkEncoding($value)) {
				throw new Nette\InvalidArgumentException("Header is not valid UTF-8 string.");
			}
			$this->headers[$name] = preg_replace('#[\r\n]+#', ' ', $value);
		}
		return $this;
	}

	function getHeader($name)
	{
		return isset($this->headers[$name]) ? $this->headers[$name] : NULL;
	}

	function clearHeader($name)
	{
		unset($this->headers[$name]);
		return $this;
	}

	function getEncodedHeader($name)
	{
		$offset = strlen($name) + 2;

		if (!isset($this->headers[$name])) {
			return NULL;

		} elseif (is_array($this->headers[$name])) {
			$s = '';
			foreach ($this->headers[$name] as $email => $name) {
				if ($name != NULL) {
					$s .= self::encodeHeader(
						strpbrk($name, '.,;<@>()[]"=?') ? '"' . addcslashes($name, '"\\') . '"' : $name,
						$offset
					);
					$email = " <$email>";
				}
				$email .= ',';
				if ($s !== '' && $offset + strlen($email) > self::LINE_LENGTH) {
					$s .= self::EOL . "\t";
					$offset = 1;
				}
				$s .= $email;
				$offset += strlen($email);
			}
			return substr($s, 0, -1);

		} else {
			return self::encodeHeader($this->headers[$name], $offset);
		}
	}

	function getHeaders()
	{
		return $this->headers;
	}

	function setContentType($contentType, $charset = NULL)
	{
		$this->setHeader('Content-Type', $contentType . ($charset ? "; charset=$charset" : ''));
		return $this;
	}

	function setEncoding($encoding)
	{
		$this->setHeader('Content-Transfer-Encoding', $encoding);
		return $this;
	}

	function getEncoding()
	{
		return $this->getHeader('Content-Transfer-Encoding');
	}

	function addPart(MimePart $part = NULL)
	{
		return $this->parts[] = $part === NULL ? new self : $part;
	}

	function setBody($body)
	{
		$this->body = $body;
		return $this;
	}

	function getBody()
	{
		return $this->body;
	}

	function generateMessage()
	{
		$output = '';
		$boundary = '--------' . Strings::random();

		foreach ($this->headers as $name => $value) {
			$output .= $name . ': ' . $this->getEncodedHeader($name);
			if ($this->parts && $name === 'Content-Type') {
				$output .= ';' . self::EOL . "\tboundary=\"$boundary\"";
			}
			$output .= self::EOL;
		}
		$output .= self::EOL;

		$body = (string) $this->body;
		if ($body !== '') {
			switch ($this->getEncoding()) {
			case self::ENCODING_QUOTED_PRINTABLE:
				$output .= function_exists('quoted_printable_encode') ? quoted_printable_encode($body) : self::encodeQuotedPrintable($body);
				break;

			case self::ENCODING_BASE64:
				$output .= rtrim(chunk_split(base64_encode($body), self::LINE_LENGTH, self::EOL));
				break;

			case self::ENCODING_7BIT:
				$body = preg_replace('#[\x80-\xFF]+#', '', $body);

			case self::ENCODING_8BIT:
				$body = str_replace(array("\x00", "\r"), '', $body);
				$body = str_replace("\n", self::EOL, $body);
				$output .= $body;
				break;

			default:
				throw new Nette\InvalidStateException('Unknown encoding.');
			}
		}

		if ($this->parts) {
			if (substr($output, -strlen(self::EOL)) !== self::EOL) {
				$output .= self::EOL;
			}
			foreach ($this->parts as $part) {
				$output .= '--' . $boundary . self::EOL . $part->generateMessage() . self::EOL;
			}
			$output .= '--' . $boundary.'--';
		}

		return $output;
	}

	private static function encodeHeader($s, & $offset = 0)
	{
		$o = '';
		if ($offset >= 55) {
			$o = self::EOL . "\t";
			$offset = 1;
		}

		if (strspn($s, "!\"#$%&\'()*+,-./0123456789:;<>@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^`abcdefghijklmnopqrstuvwxyz{|}=? _\r\n\t") === strlen($s)
			&& ($offset + strlen($s) <= self::LINE_LENGTH)
		) {
			$offset += strlen($s);
			return $o . $s;
		}

		$o .= str_replace("\n ", "\n\t", substr(iconv_mime_encode(str_repeat(' ', $offset), $s, array(
			'scheme' => 'B',
			'input-charset' => 'UTF-8',
			'output-charset' => 'UTF-8',
		)), $offset + 2));

		$offset = strlen($o) - strrpos($o, "\n");
		return $o;
	}

	}

class Message extends MimePart
{

	const HIGH = 1,
		NORMAL = 3,
		LOW = 5;

	public static $defaultMailer = 'Nette\Mail\SendmailMailer';

	public static $defaultHeaders = array(
		'MIME-Version' => '1.0',
		'X-Mailer' => 'Nette Framework',
	);

	private $mailer;

	private $attachments = array();

	private $inlines = array();

	private $html;

	private $basePath;

	function __construct()
	{
		foreach (self::$defaultHeaders as $name => $value) {
			$this->setHeader($name, $value);
		}
		$this->setHeader('Date', date('r'));
	}

	function setFrom($email, $name = NULL)
	{
		$this->setHeader('From', $this->formatEmail($email, $name));
		return $this;
	}

	function getFrom()
	{
		return $this->getHeader('From');
	}

	function addReplyTo($email, $name = NULL)
	{
		$this->setHeader('Reply-To', $this->formatEmail($email, $name), TRUE);
		return $this;
	}

	function setSubject($subject)
	{
		$this->setHeader('Subject', $subject);
		return $this;
	}

	function getSubject()
	{
		return $this->getHeader('Subject');
	}

	function addTo($email, $name = NULL)
	{
		$this->setHeader('To', $this->formatEmail($email, $name), TRUE);
		return $this;
	}

	function addCc($email, $name = NULL)
	{
		$this->setHeader('Cc', $this->formatEmail($email, $name), TRUE);
		return $this;
	}

	function addBcc($email, $name = NULL)
	{
		$this->setHeader('Bcc', $this->formatEmail($email, $name), TRUE);
		return $this;
	}

	private function formatEmail($email, $name)
	{
		if (!$name && preg_match('#^(.+) +<(.*)>$#', $email, $matches)) {
			return array($matches[2] => $matches[1]);
		} else {
			return array($email => $name);
		}
	}

	function setReturnPath($email)
	{
		$this->setHeader('Return-Path', $email);
		return $this;
	}

	function getReturnPath()
	{
		return $this->getHeader('From');
	}

	function setPriority($priority)
	{
		$this->setHeader('X-Priority', (int) $priority);
		return $this;
	}

	function getPriority()
	{
		return $this->getHeader('X-Priority');
	}

	function setHtmlBody($html, $basePath = NULL)
	{
		$this->html = $html;
		$this->basePath = $basePath;
		return $this;
	}

	function getHtmlBody()
	{
		return $this->html;
	}

	function addEmbeddedFile($file, $content = NULL, $contentType = NULL)
	{
		return $this->inlines[$file] = $this->createAttachment($file, $content, $contentType, 'inline')
			->setHeader('Content-ID', $this->getRandomId());
	}

	function addAttachment($file, $content = NULL, $contentType = NULL)
	{
		return $this->attachments[] = $this->createAttachment($file, $content, $contentType, 'attachment');
	}

	private function createAttachment($file, $content, $contentType, $disposition)
	{
		$part = new MimePart;
		if ($content === NULL) {
			$content = file_get_contents($file);
			if ($content === FALSE) {
				throw new Nette\FileNotFoundException("Unable to read file '$file'.");
			}
		} else {
			$content = (string) $content;
		}
		$part->setBody($content);
		$part->setContentType($contentType ? $contentType : Nette\Utils\MimeTypeDetector::fromString($content));
		$part->setEncoding(preg_match('#(multipart|message)/#A', $contentType) ? self::ENCODING_8BIT : self::ENCODING_BASE64);
		$part->setHeader('Content-Disposition', $disposition . '; filename="' . Strings::fixEncoding(basename($file)) . '"');
		return $part;
	}

	function send()
	{
		$this->getMailer()->send($this->build());
	}

	function setMailer(IMailer $mailer)
	{
		$this->mailer = $mailer;
		return $this;
	}

	function getMailer()
	{
		if ($this->mailer === NULL) {
			$this->mailer = is_object(self::$defaultMailer) ? self::$defaultMailer : new static::$defaultMailer;
		}
		return $this->mailer;
	}

	function generateMessage()
	{
		if ($this->getHeader('Message-ID')) {
			return parent::generateMessage();
		} else {
			return $this->build()->generateMessage();
		}
	}

	protected function build()
	{
		$mail = clone $this;
		$mail->setHeader('Message-ID', $this->getRandomId());

		$mail->buildHtml();
		$mail->buildText();

		$cursor = $mail;
		if ($mail->attachments) {
			$tmp = $cursor->setContentType('multipart/mixed');
			$cursor = $cursor->addPart();
			foreach ($mail->attachments as $value) {
				$tmp->addPart($value);
			}
		}

		if ($mail->html != NULL) {
			$tmp = $cursor->setContentType('multipart/alternative');
			$cursor = $cursor->addPart();
			$alt = $tmp->addPart();
			if ($mail->inlines) {
				$tmp = $alt->setContentType('multipart/related');
				$alt = $alt->addPart();
				foreach ($mail->inlines as $name => $value) {
					$tmp->addPart($value);
				}
			}
			$alt->setContentType('text/html', 'UTF-8')
				->setEncoding(preg_match('#\S{990}#', $mail->html)
					? self::ENCODING_QUOTED_PRINTABLE
					: (preg_match('#[\x80-\xFF]#', $mail->html) ? self::ENCODING_8BIT : self::ENCODING_7BIT))
				->setBody($mail->html);
		}

		$text = $mail->getBody();
		$mail->setBody(NULL);
		$cursor->setContentType('text/plain', 'UTF-8')
			->setEncoding(preg_match('#\S{990}#', $text)
				? self::ENCODING_QUOTED_PRINTABLE
				: (preg_match('#[\x80-\xFF]#', $text) ? self::ENCODING_8BIT : self::ENCODING_7BIT))
			->setBody($text);

		return $mail;
	}

	protected function buildHtml()
	{
		if ($this->html instanceof Nette\Templating\ITemplate) {
			$this->html->mail = $this;
			if ($this->basePath === NULL && $this->html instanceof Nette\Templating\IFileTemplate) {
				$this->basePath = dirname($this->html->getFile());
			}
			$this->html = $this->html->__toString(TRUE);
		}

		if ($this->basePath !== FALSE) {
			$cids = array();
			$matches = Strings::matchAll(
				$this->html,
				'#(src\s*=\s*|background\s*=\s*|url\()(["\'])(?![a-z]+:|[/\\#])(.+?)\\2#i',
				PREG_OFFSET_CAPTURE
			);
			foreach (array_reverse($matches) as $m) {
				$file = rtrim($this->basePath, '/\\') . '/' . $m[3][0];
				if (!isset($cids[$file])) {
					$cids[$file] = substr($this->addEmbeddedFile($file)->getHeader("Content-ID"), 1, -1);
				}
				$this->html = substr_replace($this->html,
					"{$m[1][0]}{$m[2][0]}cid:{$cids[$file]}{$m[2][0]}",
					$m[0][1], strlen($m[0][0])
				);
			}
		}

		if (!$this->getSubject() && $matches = Strings::match($this->html, '#<title>(.+?)</title>#is')) {
			$this->setSubject(html_entity_decode($matches[1], ENT_QUOTES, 'UTF-8'));
		}
	}

	protected function buildText()
	{
		$text = $this->getBody();
		if ($text instanceof Nette\Templating\ITemplate) {
			$text->mail = $this;
			$this->setBody($text->__toString(TRUE));

		} elseif ($text == NULL && $this->html != NULL) {
			$text = Strings::replace($this->html, array(
				'#<(style|script|head).*</\\1>#Uis' => '',
				'#<t[dh][ >]#i' => " $0",
				'#[ \t\r\n]+#' => ' ',
				'#<(/?p|/?h\d|li|br|/tr)[ >/]#i' => "\n$0",
			));
			$text = html_entity_decode(strip_tags($text), ENT_QUOTES, 'UTF-8');
			$this->setBody(trim($text));
		}
	}

	private function getRandomId()
	{
		return '<' . Strings::random() . '@' . (isset($_SERVER['HTTP_HOST']) ? $_SERVER['HTTP_HOST']
			: (isset($_SERVER['SERVER_NAME']) ? $_SERVER['SERVER_NAME'] : 'localhost'))
			. '>';
	}

}

class SendmailMailer extends Nette\Object implements IMailer
{

	public $commandArgs;

	function send(Message $mail)
	{
		$tmp = clone $mail;
		$tmp->setHeader('Subject', NULL);
		$tmp->setHeader('To', NULL);

		$parts = explode(Message::EOL . Message::EOL, $tmp->generateMessage(), 2);

		Nette\Diagnostics\Debugger::tryError();
		$res = mail(
			str_replace(Message::EOL, PHP_EOL, $mail->getEncodedHeader('To')),
			str_replace(Message::EOL, PHP_EOL, $mail->getEncodedHeader('Subject')),
			str_replace(Message::EOL, PHP_EOL, $parts[1]),
			str_replace(Message::EOL, PHP_EOL, $parts[0]),
			(string) $this->commandArgs
		);

		if (Nette\Diagnostics\Debugger::catchError($e)) {
			throw new Nette\InvalidStateException('mail(): ' . $e->getMessage(), 0, $e);

		} elseif (!$res) {
			throw new Nette\InvalidStateException('Unable to send email.');
		}
	}

}

class SmtpMailer extends Nette\Object implements IMailer
{

	private $connection;

	private $host;

	private $port;

	private $username;

	private $password;

	private $secure;

	private $timeout;

	function __construct(array $options = array())
	{
		if (isset($options['host'])) {
			$this->host = $options['host'];
			$this->port = isset($options['port']) ? (int) $options['port'] : NULL;
		} else {
			$this->host = ini_get('SMTP');
			$this->port = (int) ini_get('smtp_port');
		}
		$this->username = isset($options['username']) ? $options['username'] : '';
		$this->password = isset($options['password']) ? $options['password'] : '';
		$this->secure = isset($options['secure']) ? $options['secure'] : '';
		$this->timeout = isset($options['timeout']) ? (int) $options['timeout'] : 20;
		if (!$this->port) {
			$this->port = $this->secure === 'ssl' ? 465 : 25;
		}
	}

	function send(Message $mail)
	{
		$data = $mail->generateMessage();

		$this->connect();

		$from = $mail->getHeader('From');
		if ($from) {
			$from = array_keys($from);
			$this->write("MAIL FROM:<$from[0]>", 250);
		}

		foreach (array_merge(
			(array) $mail->getHeader('To'),
			(array) $mail->getHeader('Cc'),
			(array) $mail->getHeader('Bcc')
		) as $email => $name) {
			$this->write("RCPT TO:<$email>", array(250, 251));
		}

		$this->write('DATA', 354);
		$data = preg_replace('#^\.#m', '..', $data);
		$this->write($data);
		$this->write('.', 250);

		$this->write('QUIT', 221);

		$this->disconnect();
	}

	private function connect()
	{
		$this->connection = @fsockopen(
			($this->secure === 'ssl' ? 'ssl://' : '') . $this->host,
			$this->port, $errno, $error, $this->timeout
		);
		if (!$this->connection) {
			throw new SmtpException($error, $errno);
		}
		stream_set_timeout($this->connection, $this->timeout, 0);
		$this->read();

		$self = isset($_SERVER['SERVER_NAME']) ? $_SERVER['SERVER_NAME'] : 'localhost';
		$this->write("EHLO $self");
		if ((int) $this->read() !== 250) {
			$this->write("HELO $self", 250);
		}

		if ($this->secure === 'tls') {
			$this->write('STARTTLS', 220);
			if (!stream_socket_enable_crypto($this->connection, TRUE, STREAM_CRYPTO_METHOD_TLS_CLIENT)) {
				throw new SmtpException('Unable to connect via TLS.');
			}
		}

		if ($this->username != NULL && $this->password != NULL) {
			$this->write('AUTH LOGIN', 334);
			$this->write(base64_encode($this->username), 334, 'username');
			$this->write(base64_encode($this->password), 235, 'password');
		}
	}

	private function disconnect()
	{
		fclose($this->connection);
		$this->connection = NULL;
	}

	private function write($line, $expectedCode = NULL, $message = NULL)
	{
		fwrite($this->connection, $line . Message::EOL);
		if ($expectedCode && !in_array((int) $this->read(), (array) $expectedCode)) {
			throw new SmtpException('SMTP server did not accept ' . ($message ? $message : $line));
		}
	}

	private function read()
	{
		$s = '';
		while (($line = fgets($this->connection, 1e3)) != NULL) {
			$s .= $line;
			if (substr($line, 3, 1) === ' ') {
				break;
			}
		}
		return $s;
	}

}

class SmtpException extends \Exception
{
}

}

namespace Nette\Reflection {

use Nette;

class Annotation extends Nette\Object implements IAnnotation
{

	function __construct(array $values)
	{
		foreach ($values as $k => $v) {
			$this->$k = $v;
		}
	}

	function __toString()
	{
		return $this->value;
	}

}

use Nette\Utils\Strings;

/**
 * Annotations support for PHP.
 *
 * @author     David Grudl
 * @Annotation
 */
final class AnnotationsParser
{

	const RE_STRING = '\'(?:\\\\.|[^\'\\\\])*\'|"(?:\\\\.|[^"\\\\])*"';

	const RE_IDENTIFIER = '[_a-zA-Z\x7F-\xFF][_a-zA-Z0-9\x7F-\xFF-]*';

	public static $useReflection;

	public static $inherited = array('description', 'param', 'return');

	private static $cache;

	private static $timestamps;

	private static $cacheStorage;

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function getAll(\Reflector $r)
	{
		if ($r instanceof \ReflectionClass) {
			$type = $r->getName();
			$member = '';

		} elseif ($r instanceof \ReflectionMethod) {
			$type = $r->getDeclaringClass()->getName();
			$member = $r->getName();

		} else {
			$type = $r->getDeclaringClass()->getName();
			$member = '$' . $r->getName();
		}

		if (!self::$useReflection) {
			$file = $r instanceof \ReflectionClass ? $r->getFileName() : $r->getDeclaringClass()->getFileName();
			if ($file && isset(self::$timestamps[$file]) && self::$timestamps[$file] !== filemtime($file)) {
				unset(self::$cache[$type]);
			}
			unset(self::$timestamps[$file]);
		}

		if (isset(self::$cache[$type][$member])) {
			return self::$cache[$type][$member];
		}

		if (self::$useReflection === NULL) {
			self::$useReflection = (bool) ClassType::from(__CLASS__)->getDocComment();
		}

		if (self::$useReflection) {
			$annotations = self::parseComment($r->getDocComment());

		} else {
			if (!self::$cacheStorage) {

				self::$cacheStorage = new Nette\Caching\Storages\DevNullStorage;
			}
			$outerCache = new Nette\Caching\Cache(self::$cacheStorage, 'Nette.Reflection.Annotations');

			if (self::$cache === NULL) {
				self::$cache = (array) $outerCache->offsetGet('list');
				self::$timestamps = isset(self::$cache['*']) ? self::$cache['*'] : array();
			}

			if (!isset(self::$cache[$type]) && $file) {
				self::$cache['*'][$file] = filemtime($file);
				self::parseScript($file);
				$outerCache->save('list', self::$cache);
			}

			if (isset(self::$cache[$type][$member])) {
				$annotations = self::$cache[$type][$member];
			} else {
				$annotations = array();
			}
		}

		if ($r instanceof \ReflectionMethod && !$r->isPrivate()
			&& (!$r->isConstructor() || !empty($annotations['inheritdoc'][0])))
		{
			try {
				$inherited = self::getAll(new \ReflectionMethod(get_parent_class($type), $member));
			} catch (\ReflectionException $e) {
				try {
					$inherited = self::getAll($r->getPrototype());
				} catch (\ReflectionException $e) {
					$inherited = array();
				}
			}
			$annotations += array_intersect_key($inherited, array_flip(self::$inherited));
		}

		return self::$cache[$type][$member] = $annotations;
	}

	private static function parseComment($comment)
	{
		static $tokens = array('true' => TRUE, 'false' => FALSE, 'null' => NULL, '' => TRUE);

		$res = array();
		$comment = preg_replace('#^\s*\*\s?#ms', '', trim($comment, '/*'));
		$parts = preg_split('#^\s*(?=@'.self::RE_IDENTIFIER.')#m', $comment, 2);

		$description = trim($parts[0]);
		if ($description !== '') {
			$res['description'] = array($description);
		}

		$matches = Strings::matchAll(
			isset($parts[1]) ? $parts[1] : '',
			'~
				(?<=\s|^)@('.self::RE_IDENTIFIER.')[ \t]*      ##  annotation
				(
					\((?>'.self::RE_STRING.'|[^\'")@]+)+\)|  ##  (value)
					[^(@\r\n][^@\r\n]*|)                     ##  value
			~xi'
		);

		foreach ($matches as $match) {
			list(, $name, $value) = $match;

			if (substr($value, 0, 1) === '(') {
				$items = array();
				$key = '';
				$val = TRUE;
				$value[0] = ',';
				while ($m = Strings::match(
					$value,
					'#\s*,\s*(?>(' . self::RE_IDENTIFIER . ')\s*=\s*)?(' . self::RE_STRING . '|[^\'"),\s][^\'"),]*)#A')
				) {
					$value = substr($value, strlen($m[0]));
					list(, $key, $val) = $m;
					if ($val[0] === "'" || $val[0] === '"') {
						$val = substr($val, 1, -1);

					} elseif (is_numeric($val)) {
						$val = 1 * $val;

					} else {
						$lval = strtolower($val);
						$val = array_key_exists($lval, $tokens) ? $tokens[$lval] : $val;
					}

					if ($key === '') {
						$items[] = $val;

					} else {
						$items[$key] = $val;
					}
				}

				$value = count($items) < 2 && $key === '' ? $val : $items;

			} else {
				$value = trim($value);
				if (is_numeric($value)) {
					$value = 1 * $value;

				} else {
					$lval = strtolower($value);
					$value = array_key_exists($lval, $tokens) ? $tokens[$lval] : $value;
				}
			}

			$class = $name . 'Annotation';
			if (class_exists($class)) {
				$res[$name][] = new $class(is_array($value) ? $value : array('value' => $value));

			} else {
				$res[$name][] = is_array($value) ? new \ArrayObject($value, \ArrayObject::ARRAY_AS_PROPS) : $value;
			}
		}

		return $res;
	}

	private static function parseScript($file)
	{
		$T_NAMESPACE = PHP_VERSION_ID < 50300 ? -1 : T_NAMESPACE;
		$T_NS_SEPARATOR = PHP_VERSION_ID < 50300 ? -1 : T_NS_SEPARATOR;

		$s = file_get_contents($file);

		if (Strings::match($s, '#//nette'.'loader=(\S*)#')) {
			return;
		}

		$expected = $namespace = $class = $docComment = NULL;
		$level = $classLevel = 0;

		foreach (token_get_all($s) as $token) {

			if (is_array($token)) {
				switch ($token[0]) {
				case T_DOC_COMMENT:
					$docComment = $token[1];
				case T_WHITESPACE:
				case T_COMMENT:
					continue 2;

				case T_STRING:
				case $T_NS_SEPARATOR:
				case T_VARIABLE:
					if ($expected) {
						$name .= $token[1];
					}
					continue 2;

				case T_FUNCTION:
				case T_VAR:
				case T_PUBLIC:
				case T_PROTECTED:
				case $T_NAMESPACE:
				case T_CLASS:
				case T_INTERFACE:
					$expected = $token[0];
					$name = NULL;
					continue 2;

				case T_STATIC:
				case T_ABSTRACT:
				case T_FINAL:
					continue 2;

				case T_CURLY_OPEN:
				case T_DOLLAR_OPEN_CURLY_BRACES:
					$level++;
				}
			}

			if ($expected) {
				switch ($expected) {
				case T_CLASS:
				case T_INTERFACE:
					$class = $namespace . $name;
					$classLevel = $level;
					$name = '';

				case T_FUNCTION:
					if ($token === '&') {
						continue 2;
					}
				case T_VAR:
				case T_PUBLIC:
				case T_PROTECTED:
					if ($class && $name !== NULL && $docComment) {
						self::$cache[$class][$name] = self::parseComment($docComment);
					}
					break;

				case $T_NAMESPACE:
					$namespace = $name . '\\';
				}

				$expected = $docComment = NULL;
			}

			if ($token === ';') {
				$docComment = NULL;
			} elseif ($token === '{') {
				$docComment = NULL;
				$level++;
			} elseif ($token === '}') {
				$level--;
				if ($level === $classLevel) {
					$class = NULL;
				}
			}
		}
	}

	static function setCacheStorage(Nette\Caching\IStorage $storage)
	{
		self::$cacheStorage = $storage;
	}

	static function getCacheStorage()
	{
		return self::$cacheStorage;
	}

}

use Nette\ObjectMixin;

class Extension extends \ReflectionExtension
{

	function __toString()
	{
		return 'Extension ' . $this->getName();
	}

	function getClasses()
	{
		$res = array();
		foreach (parent::getClassNames() as $val) {
			$res[$val] = new ClassType($val);
		}
		return $res;
	}

	function getFunctions()
	{
		foreach ($res = parent::getFunctions() as $key => $val) {
			$res[$key] = new GlobalFunction($key);
		}
		return $res;
	}

	static function getReflection()
	{
		return new ClassType(get_called_class());
	}

	function __call($name, $args)
	{
		return ObjectMixin::call($this, $name, $args);
	}

	function &__get($name)
	{
		return ObjectMixin::get($this, $name);
	}

	function __set($name, $value)
	{
		return ObjectMixin::set($this, $name, $value);
	}

	function __isset($name)
	{
		return ObjectMixin::has($this, $name);
	}

	function __unset($name)
	{
		ObjectMixin::remove($this, $name);
	}

}

class GlobalFunction extends \ReflectionFunction
{

	private $value;

	function __construct($name)
	{
		parent::__construct($this->value = $name);
	}

	function getDefaultParameters()
	{
		return Method::buildDefaultParameters(parent::getParameters());
	}

	function invokeNamedArgs($args)
	{
		return $this->invokeArgs(Method::combineArgs($this->getDefaultParameters(), $args));
	}

	function toCallback()
	{
		return new Nette\Callback($this->value);
	}

	function __toString()
	{
		return 'Function ' . $this->getName() . '()';
	}

	function getClosure()
	{
		return $this->isClosure() ? $this->value : NULL;
	}

	function getExtension()
	{
		return ($name = $this->getExtensionName()) ? new Extension($name) : NULL;
	}

	function getParameters()
	{
		foreach ($res = parent::getParameters() as $key => $val) {
			$res[$key] = new Parameter($this->value, $val->getName());
		}
		return $res;
	}

	static function getReflection()
	{
		return new ClassType(get_called_class());
	}

	function __call($name, $args)
	{
		return ObjectMixin::call($this, $name, $args);
	}

	function &__get($name)
	{
		return ObjectMixin::get($this, $name);
	}

	function __set($name, $value)
	{
		return ObjectMixin::set($this, $name, $value);
	}

	function __isset($name)
	{
		return ObjectMixin::has($this, $name);
	}

	function __unset($name)
	{
		ObjectMixin::remove($this, $name);
	}

}

class Method extends \ReflectionMethod
{

	static function from($class, $method)
	{
		return new static(is_object($class) ? get_class($class) : $class, $method);
	}

	function getDefaultParameters()
	{
		return self::buildDefaultParameters(parent::getParameters());
	}

	function invokeNamedArgs($object, $args)
	{
		return $this->invokeArgs($object, self::combineArgs($this->getDefaultParameters(), $args));
	}

	function toCallback()
	{
		return new Nette\Callback(parent::getDeclaringClass()->getName(), $this->getName());
	}

	function __toString()
	{
		return 'Method ' . parent::getDeclaringClass()->getName() . '::' . $this->getName() . '()';
	}

	function getDeclaringClass()
	{
		return new ClassType(parent::getDeclaringClass()->getName());
	}

	function getPrototype()
	{
		$prototype = parent::getPrototype();
		return new Method($prototype->getDeclaringClass()->getName(), $prototype->getName());
	}

	function getExtension()
	{
		return ($name = $this->getExtensionName()) ? new Extension($name) : NULL;
	}

	function getParameters()
	{
		$me = array(parent::getDeclaringClass()->getName(), $this->getName());
		foreach ($res = parent::getParameters() as $key => $val) {
			$res[$key] = new Parameter($me, $val->getName());
		}
		return $res;
	}

	function hasAnnotation($name)
	{
		$res = AnnotationsParser::getAll($this);
		return !empty($res[$name]);
	}

	function getAnnotation($name)
	{
		$res = AnnotationsParser::getAll($this);
		return isset($res[$name]) ? end($res[$name]) : NULL;
	}

	function getAnnotations()
	{
		return AnnotationsParser::getAll($this);
	}

	function getDescription()
	{
		return $this->getAnnotation('description');
	}

	static function getReflection()
	{
		return new ClassType(get_called_class());
	}

	function __call($name, $args)
	{
		return ObjectMixin::call($this, $name, $args);
	}

	function &__get($name)
	{
		return ObjectMixin::get($this, $name);
	}

	function __set($name, $value)
	{
		return ObjectMixin::set($this, $name, $value);
	}

	function __isset($name)
	{
		return ObjectMixin::has($this, $name);
	}

	function __unset($name)
	{
		ObjectMixin::remove($this, $name);
	}

	static function buildDefaultParameters($params)
	{
		$res = array();
		foreach ($params as $param) {
			$res[$param->getName()] = $param->isDefaultValueAvailable()
				? $param->getDefaultValue()
				: NULL;

			if ($param->isArray()) {
				settype($res[$param->getName()], 'array');
			}
		}
		return $res;
	}

	static function combineArgs($params, $args)
	{
		$res = array();
		$i = 0;
		foreach ($params as $name => $def) {
			if (isset($args[$name])) {
				$val = $args[$name];
				if ($def !== NULL) {
					settype($val, gettype($def));
				}
				$res[$i++] = $val;
			} else {
				$res[$i++] = $def;
			}
		}
		return $res;
	}

}

class Parameter extends \ReflectionParameter
{

	private $function;

	function __construct($function, $parameter)
	{
		parent::__construct($this->function = $function, $parameter);
	}

	function getClass()
	{
		return ($ref = parent::getClass()) ? new ClassType($ref->getName()) : NULL;
	}

	function getClassName()
	{
		return ($tmp = Nette\Utils\Strings::match($this, '#>\s+([a-z0-9_\\\\]+)#i')) ? $tmp[1] : NULL;
	}

	function getDeclaringClass()
	{
		return ($ref = parent::getDeclaringClass()) ? new ClassType($ref->getName()) : NULL;
	}

	function getDeclaringFunction()
	{
		return is_array($this->function)
			? new Method($this->function[0], $this->function[1])
			: new GlobalFunction($this->function);
	}

	static function getReflection()
	{
		return new ClassType(get_called_class());
	}

	function __call($name, $args)
	{
		return ObjectMixin::call($this, $name, $args);
	}

	function &__get($name)
	{
		return ObjectMixin::get($this, $name);
	}

	function __set($name, $value)
	{
		return ObjectMixin::set($this, $name, $value);
	}

	function __isset($name)
	{
		return ObjectMixin::has($this, $name);
	}

	function __unset($name)
	{
		ObjectMixin::remove($this, $name);
	}

}

class Property extends \ReflectionProperty
{

	function __toString()
	{
		return 'Property ' . parent::getDeclaringClass()->getName() . '::$' . $this->getName();
	}

	function getDeclaringClass()
	{
		return new ClassType(parent::getDeclaringClass()->getName());
	}

	function hasAnnotation($name)
	{
		$res = AnnotationsParser::getAll($this);
		return !empty($res[$name]);
	}

	function getAnnotation($name)
	{
		$res = AnnotationsParser::getAll($this);
		return isset($res[$name]) ? end($res[$name]) : NULL;
	}

	function getAnnotations()
	{
		return AnnotationsParser::getAll($this);
	}

	function getDescription()
	{
		return $this->getAnnotation('description');
	}

	static function getReflection()
	{
		return new ClassType(get_called_class());
	}

	function __call($name, $args)
	{
		return ObjectMixin::call($this, $name, $args);
	}

	function &__get($name)
	{
		return ObjectMixin::get($this, $name);
	}

	function __set($name, $value)
	{
		return ObjectMixin::set($this, $name, $value);
	}

	function __isset($name)
	{
		return ObjectMixin::has($this, $name);
	}

	function __unset($name)
	{
		ObjectMixin::remove($this, $name);
	}

}

}

namespace Nette\Security {

use Nette;

class AuthenticationException extends \Exception
{
}

class Identity extends Nette\FreezableObject implements IIdentity
{

	private $id;

	private $roles;

	private $data;

	function __construct($id, $roles = NULL, $data = NULL)
	{
		$this->setId($id);
		$this->setRoles((array) $roles);
		$this->data = $data instanceof \Traversable ? iterator_to_array($data) : (array) $data;
	}

	function setId($id)
	{
		$this->updating();
		$this->id = is_numeric($id) ? 1 * $id : $id;
		return $this;
	}

	function getId()
	{
		return $this->id;
	}

	function setRoles(array $roles)
	{
		$this->updating();
		$this->roles = $roles;
		return $this;
	}

	function getRoles()
	{
		return $this->roles;
	}

	function getData()
	{
		return $this->data;
	}

	function __set($key, $value)
	{
		$this->updating();
		if (parent::__isset($key)) {
			parent::__set($key, $value);

		} else {
			$this->data[$key] = $value;
		}
	}

	function &__get($key)
	{
		if (parent::__isset($key)) {
			return parent::__get($key);

		} else {
			return $this->data[$key];
		}
	}

	function __isset($key)
	{
		return isset($this->data[$key]) || parent::__isset($key);
	}

	function __unset($name)
	{
		ObjectMixin::remove($this, $name);
	}

}

class Permission extends Nette\Object implements IAuthorizator
{

	private $roles = array();

	private $resources = array();

	private $rules = array(
		'allResources' => array(
			'allRoles' => array(
				'allPrivileges' => array(
					'type' => self::DENY,
					'assert' => NULL,
				),
				'byPrivilege' => array(),
			),
			'byRole' => array(),
		),
		'byResource' => array(),
	);

	private $queriedRole, $queriedResource;

	function addRole($role, $parents = NULL)
	{
		$this->checkRole($role, FALSE);
		if (isset($this->roles[$role])) {
			throw new Nette\InvalidStateException("Role '$role' already exists in the list.");
		}

		$roleParents = array();

		if ($parents !== NULL) {
			if (!is_array($parents)) {
				$parents = array($parents);
			}

			foreach ($parents as $parent) {
				$this->checkRole($parent);
				$roleParents[$parent] = TRUE;
				$this->roles[$parent]['children'][$role] = TRUE;
			}
		}

		$this->roles[$role] = array(
			'parents'  => $roleParents,
			'children' => array(),
		);

		return $this;
	}

	function hasRole($role)
	{
		$this->checkRole($role, FALSE);
		return isset($this->roles[$role]);
	}

	private function checkRole($role, $need = TRUE)
	{
		if (!is_string($role) || $role === '') {
			throw new Nette\InvalidArgumentException("Role must be a non-empty string.");

		} elseif ($need && !isset($this->roles[$role])) {
			throw new Nette\InvalidStateException("Role '$role' does not exist.");
		}
	}

	function getRoleParents($role)
	{
		$this->checkRole($role);
		return array_keys($this->roles[$role]['parents']);
	}

	function roleInheritsFrom($role, $inherit, $onlyParents = FALSE)
	{
		$this->checkRole($role);
		$this->checkRole($inherit);

		$inherits = isset($this->roles[$role]['parents'][$inherit]);

		if ($inherits || $onlyParents) {
			return $inherits;
		}

		foreach ($this->roles[$role]['parents'] as $parent => $foo) {
			if ($this->roleInheritsFrom($parent, $inherit)) {
				return TRUE;
			}
		}

		return FALSE;
	}

	function removeRole($role)
	{
		$this->checkRole($role);

		foreach ($this->roles[$role]['children'] as $child => $foo) {
			unset($this->roles[$child]['parents'][$role]);
		}

		foreach ($this->roles[$role]['parents'] as $parent => $foo) {
			unset($this->roles[$parent]['children'][$role]);
		}

		unset($this->roles[$role]);

		foreach ($this->rules['allResources']['byRole'] as $roleCurrent => $rules) {
			if ($role === $roleCurrent) {
				unset($this->rules['allResources']['byRole'][$roleCurrent]);
			}
		}

		foreach ($this->rules['byResource'] as $resourceCurrent => $visitor) {
			if (isset($visitor['byRole'])) {
				foreach ($visitor['byRole'] as $roleCurrent => $rules) {
					if ($role === $roleCurrent) {
						unset($this->rules['byResource'][$resourceCurrent]['byRole'][$roleCurrent]);
					}
				}
			}
		}

		return $this;
	}

	function removeAllRoles()
	{
		$this->roles = array();

		foreach ($this->rules['allResources']['byRole'] as $roleCurrent => $rules) {
			unset($this->rules['allResources']['byRole'][$roleCurrent]);
		}

		foreach ($this->rules['byResource'] as $resourceCurrent => $visitor) {
			foreach ($visitor['byRole'] as $roleCurrent => $rules) {
				unset($this->rules['byResource'][$resourceCurrent]['byRole'][$roleCurrent]);
			}
		}

		return $this;
	}

	function addResource($resource, $parent = NULL)
	{
		$this->checkResource($resource, FALSE);

		if (isset($this->resources[$resource])) {
			throw new Nette\InvalidStateException("Resource '$resource' already exists in the list.");
		}

		if ($parent !== NULL) {
			$this->checkResource($parent);
			$this->resources[$parent]['children'][$resource] = TRUE;
		}

		$this->resources[$resource] = array(
			'parent'   => $parent,
			'children' => array()
		);

		return $this;
	}

	function hasResource($resource)
	{
		$this->checkResource($resource, FALSE);
		return isset($this->resources[$resource]);
	}

	private function checkResource($resource, $need = TRUE)
	{
		if (!is_string($resource) || $resource === '') {
			throw new Nette\InvalidArgumentException("Resource must be a non-empty string.");

		} elseif ($need && !isset($this->resources[$resource])) {
			throw new Nette\InvalidStateException("Resource '$resource' does not exist.");
		}
	}

	function resourceInheritsFrom($resource, $inherit, $onlyParent = FALSE)
	{
		$this->checkResource($resource);
		$this->checkResource($inherit);

		if ($this->resources[$resource]['parent'] === NULL) {
			return FALSE;
		}

		$parent = $this->resources[$resource]['parent'];
		if ($inherit === $parent) {
			return TRUE;

		} elseif ($onlyParent) {
			return FALSE;
		}

		while ($this->resources[$parent]['parent'] !== NULL) {
			$parent = $this->resources[$parent]['parent'];
			if ($inherit === $parent) {
				return TRUE;
			}
		}

		return FALSE;
	}

	function removeResource($resource)
	{
		$this->checkResource($resource);

		$parent = $this->resources[$resource]['parent'];
		if ($parent !== NULL) {
			unset($this->resources[$parent]['children'][$resource]);
		}

		$removed = array($resource);
		foreach ($this->resources[$resource]['children'] as $child => $foo) {
			$this->removeResource($child);
			$removed[] = $child;
		}

		foreach ($removed as $resourceRemoved) {
			foreach ($this->rules['byResource'] as $resourceCurrent => $rules) {
				if ($resourceRemoved === $resourceCurrent) {
					unset($this->rules['byResource'][$resourceCurrent]);
				}
			}
		}

		unset($this->resources[$resource]);
		return $this;
	}

	function removeAllResources()
	{
		foreach ($this->resources as $resource => $foo) {
			foreach ($this->rules['byResource'] as $resourceCurrent => $rules) {
				if ($resource === $resourceCurrent) {
					unset($this->rules['byResource'][$resourceCurrent]);
				}
			}
		}

		$this->resources = array();
		return $this;
	}

	function allow($roles = self::ALL, $resources = self::ALL, $privileges = self::ALL, $assertion = NULL)
	{
		$this->setRule(TRUE, self::ALLOW, $roles, $resources, $privileges, $assertion);
		return $this;
	}

	function deny($roles = self::ALL, $resources = self::ALL, $privileges = self::ALL, $assertion = NULL)
	{
		$this->setRule(TRUE, self::DENY, $roles, $resources, $privileges, $assertion);
		return $this;
	}

	function removeAllow($roles = self::ALL, $resources = self::ALL, $privileges = self::ALL)
	{
		$this->setRule(FALSE, self::ALLOW, $roles, $resources, $privileges);
		return $this;
	}

	function removeDeny($roles = self::ALL, $resources = self::ALL, $privileges = self::ALL)
	{
		$this->setRule(FALSE, self::DENY, $roles, $resources, $privileges);
		return $this;
	}

	protected function setRule($toAdd, $type, $roles, $resources, $privileges, $assertion = NULL)
	{

		if ($roles === self::ALL) {
			$roles = array(self::ALL);

		} else {
			if (!is_array($roles)) {
				$roles = array($roles);
			}

			foreach ($roles as $role) {
				$this->checkRole($role);
			}
		}

		if ($resources === self::ALL) {
			$resources = array(self::ALL);

		} else {
			if (!is_array($resources)) {
				$resources = array($resources);
			}

			foreach ($resources as $resource) {
				$this->checkResource($resource);
			}
		}

		if ($privileges === self::ALL) {
			$privileges = array();

		} elseif (!is_array($privileges)) {
			$privileges = array($privileges);
		}

		$assertion = $assertion ? callback($assertion) : NULL;

		if ($toAdd) {
			foreach ($resources as $resource) {
				foreach ($roles as $role) {
					$rules = & $this->getRules($resource, $role, TRUE);
					if (count($privileges) === 0) {
						$rules['allPrivileges']['type'] = $type;
						$rules['allPrivileges']['assert'] = $assertion;
						if (!isset($rules['byPrivilege'])) {
							$rules['byPrivilege'] = array();
						}
					} else {
						foreach ($privileges as $privilege) {
							$rules['byPrivilege'][$privilege]['type'] = $type;
							$rules['byPrivilege'][$privilege]['assert'] = $assertion;
						}
					}
				}
			}

		} else {
			foreach ($resources as $resource) {
				foreach ($roles as $role) {
					$rules = & $this->getRules($resource, $role);
					if ($rules === NULL) {
						continue;
					}
					if (count($privileges) === 0) {
						if ($resource === self::ALL && $role === self::ALL) {
							if ($type === $rules['allPrivileges']['type']) {
								$rules = array(
									'allPrivileges' => array(
										'type' => self::DENY,
										'assert' => NULL
										),
									'byPrivilege' => array()
									);
							}
							continue;
						}
						if ($type === $rules['allPrivileges']['type']) {
							unset($rules['allPrivileges']);
						}
					} else {
						foreach ($privileges as $privilege) {
							if (isset($rules['byPrivilege'][$privilege]) &&
								$type === $rules['byPrivilege'][$privilege]['type']) {
								unset($rules['byPrivilege'][$privilege]);
							}
						}
					}
				}
			}
		}
		return $this;
	}

	function isAllowed($role = self::ALL, $resource = self::ALL, $privilege = self::ALL)
	{
		$this->queriedRole = $role;
		if ($role !== self::ALL) {
			if ($role instanceof IRole) {
				$role = $role->getRoleId();
			}
			$this->checkRole($role);
		}

		$this->queriedResource = $resource;
		if ($resource !== self::ALL) {
			if ($resource instanceof IResource) {
				$resource = $resource->getResourceId();
			}
			$this->checkResource($resource);
		}

		do {

			if ($role !== NULL && NULL !== ($result = $this->searchRolePrivileges($privilege === self::ALL, $role, $resource, $privilege))) {
				break;
			}

			if ($privilege === self::ALL) {
				if ($rules = $this->getRules($resource, self::ALL)) {
					foreach ($rules['byPrivilege'] as $privilege => $rule) {
						if (self::DENY === ($result = $this->getRuleType($resource, NULL, $privilege))) {
							break 2;
						}
					}
					if (NULL !== ($result = $this->getRuleType($resource, NULL, NULL))) {
						break;
					}
				}
			} else {
				if (NULL !== ($result = $this->getRuleType($resource, NULL, $privilege))) {
					break;

				} elseif (NULL !== ($result = $this->getRuleType($resource, NULL, NULL))) {
					break;
				}
			}

			$resource = $this->resources[$resource]['parent'];
		} while (TRUE);

		$this->queriedRole = $this->queriedResource = NULL;
		return $result;
	}

	function getQueriedRole()
	{
		return $this->queriedRole;
	}

	function getQueriedResource()
	{
		return $this->queriedResource;
	}

	private function searchRolePrivileges($all, $role, $resource, $privilege)
	{
		$dfs = array(
			'visited' => array(),
			'stack' => array($role),
		);

		while (NULL !== ($role = array_pop($dfs['stack']))) {
			if (isset($dfs['visited'][$role])) {
				continue;
			}
			if ($all) {
				if ($rules = $this->getRules($resource, $role)) {
					foreach ($rules['byPrivilege'] as $privilege2 => $rule) {
						if (self::DENY === $this->getRuleType($resource, $role, $privilege2)) {
							return self::DENY;
						}
					}
					if (NULL !== ($type = $this->getRuleType($resource, $role, NULL))) {
						return $type;
					}
				}
			} else {
				if (NULL !== ($type = $this->getRuleType($resource, $role, $privilege))) {
					return $type;

				} elseif (NULL !== ($type = $this->getRuleType($resource, $role, NULL))) {
					return $type;
				}
			}

			$dfs['visited'][$role] = TRUE;
			foreach ($this->roles[$role]['parents'] as $roleParent => $foo) {
				$dfs['stack'][] = $roleParent;
			}
		}
		return NULL;
	}

	private function getRuleType($resource, $role, $privilege)
	{
		if (!$rules = $this->getRules($resource, $role)) {
			return NULL;
		}

		if ($privilege === self::ALL) {
			if (isset($rules['allPrivileges'])) {
				$rule = $rules['allPrivileges'];
			} else {
				return NULL;
			}
		} elseif (!isset($rules['byPrivilege'][$privilege])) {
			return NULL;

		} else {
			$rule = $rules['byPrivilege'][$privilege];
		}

		if ($rule['assert'] === NULL || $rule['assert']->__invoke($this, $role, $resource, $privilege)) {
			return $rule['type'];

		} elseif ($resource !== self::ALL || $role !== self::ALL || $privilege !== self::ALL) {
			return NULL;

		} elseif (self::ALLOW === $rule['type']) {
			return self::DENY;

		} else {
			return self::ALLOW;
		}
	}

	private function & getRules($resource, $role, $create = FALSE)
	{
		$null = NULL;
		if ($resource === self::ALL) {
			$visitor = & $this->rules['allResources'];
		} else {
			if (!isset($this->rules['byResource'][$resource])) {
				if (!$create) {
					return $null;
				}
				$this->rules['byResource'][$resource] = array();
			}
			$visitor = & $this->rules['byResource'][$resource];
		}

		if ($role === self::ALL) {
			if (!isset($visitor['allRoles'])) {
				if (!$create) {
					return $null;
				}
				$visitor['allRoles']['byPrivilege'] = array();
			}
			return $visitor['allRoles'];
		}

		if (!isset($visitor['byRole'][$role])) {
			if (!$create) {
				return $null;
			}
			$visitor['byRole'][$role]['byPrivilege'] = array();
		}

		return $visitor['byRole'][$role];
	}

}

class SimpleAuthenticator extends Nette\Object implements IAuthenticator
{

	private $userlist;

	function __construct(array $userlist)
	{
		$this->userlist = $userlist;
	}

	function authenticate(array $credentials)
	{
		list($username, $password) = $credentials;
		foreach ($this->userlist as $name => $pass) {
			if (strcasecmp($name, $username) === 0) {
				if ((string) $pass === (string) $password) {
					return new Identity($name);
				} else {
					throw new AuthenticationException("Invalid password.", self::INVALID_CREDENTIAL);
				}
			}
		}
		throw new AuthenticationException("User '$username' not found.", self::IDENTITY_NOT_FOUND);
	}

}

}

namespace Nette\Templating {

use Nette;use Nette\Utils\Strings;use Nette\Forms\Form;use Nette\Utils\Html;

final class DefaultHelpers
{
	private static $helpers = array(
		'normalize' => 'Nette\Utils\Strings::normalize',
		'toascii' => 'Nette\Utils\Strings::toAscii',
		'webalize' => 'Nette\Utils\Strings::webalize',
		'truncate' => 'Nette\Utils\Strings::truncate',
		'lower' => 'Nette\Utils\Strings::lower',
		'upper' => 'Nette\Utils\Strings::upper',
		'firstupper' => 'Nette\Utils\Strings::firstUpper',
		'capitalize' => 'Nette\Utils\Strings::capitalize',
		'trim' => 'Nette\Utils\Strings::trim',
		'padleft' => 'Nette\Utils\Strings::padLeft',
		'padright' => 'Nette\Utils\Strings::padRight',
		'replacere' => 'Nette\Utils\Strings::replace',
		'url' => 'rawurlencode',
		'striptags' => 'strip_tags',
		'nl2br' => 'nl2br',
		'substr' => 'iconv_substr',
		'repeat' => 'str_repeat',
		'implode' => 'implode',
		'number' => 'number_format',
	);

	public static $dateFormat = '%x';

	static function loader($helper)
	{
		if (method_exists(__CLASS__, $helper)) {
			return callback(__CLASS__, $helper);
		} elseif (isset(self::$helpers[$helper])) {
			return self::$helpers[$helper];
		}
	}

	static function escapeHtml($s, $quotes = ENT_QUOTES)
	{
		if (is_object($s) && ($s instanceof ITemplate || $s instanceof Html || $s instanceof Form)) {
			return $s->__toString(TRUE);
		}
		return htmlSpecialChars($s, $quotes);
	}

	static function escapeHtmlComment($s)
	{

		return str_replace('--', '--><!-- ', $s);
	}

	static function escapeXML($s)
	{

		return htmlSpecialChars(preg_replace('#[\x00-\x08\x0B\x0C\x0E-\x1F]+#', '', $s), ENT_QUOTES);
	}

	static function escapeCss($s)
	{

		return addcslashes($s, "\x00..\x1F!\"#$%&'()*+,./:;<=>?@[\\]^`{|}~");
	}

	static function escapeJs($s)
	{
		if (is_object($s) && ($s instanceof ITemplate || $s instanceof Html || $s instanceof Form)) {
			$s = $s->__toString(TRUE);
		}
		return str_replace(']]>', ']]\x3E', Nette\Utils\Json::encode($s));
	}

	static function escapeICal($s)
	{

		return addcslashes(preg_replace('#[\x00-\x08\x0B\x0C-\x1F]+#', '', $s), "\";\\,:\n");
	}

	static function strip($s)
	{
		return Strings::replace(
			$s,
			'#(</textarea|</pre|</script|^).*?(?=<textarea|<pre|<script|$)#si',
			function($m) {
				return trim(preg_replace("#[ \t\r\n]+#", " ", $m[0]));
			});
	}

	static function indent($s, $level = 1, $chars = "\t")
	{
		if ($level >= 1) {
			$s = Strings::replace($s, '#<(textarea|pre).*?</\\1#si', function($m) {
				return strtr($m[0], " \t\r\n", "\x1F\x1E\x1D\x1A");
			});
			$s = Strings::indent($s, $level, $chars);
			$s = strtr($s, "\x1F\x1E\x1D\x1A", " \t\r\n");
		}
		return $s;
	}

	static function date($time, $format = NULL)
	{
		if ($time == NULL) {
			return NULL;
		}

		if (!isset($format)) {
			$format = self::$dateFormat;
		}

		$time = Nette\DateTime::from($time);
		return strpos($format, '%') === FALSE
			? $time->format($format)
			: strftime($format, $time->format('U'));
	}

	static function bytes($bytes, $precision = 2)
	{
		$bytes = round($bytes);
		$units = array('B', 'kB', 'MB', 'GB', 'TB', 'PB');
		foreach ($units as $unit) {
			if (abs($bytes) < 1024 || $unit === end($units)) {
				break;
			}
			$bytes = $bytes / 1024;
		}
		return round($bytes, $precision) . ' ' . $unit;
	}

	static function length($var)
	{
		return is_string($var) ? Strings::length($var) : count($var);
	}

	static function replace($subject, $search, $replacement = '')
	{
		return str_replace($search, $replacement, $subject);
	}

	static function dataStream($data, $type = NULL)
	{
		if ($type === NULL) {
			$type = Nette\Utils\MimeTypeDetector::fromString($data, NULL);
		}
		return 'data:' . ($type ? "$type;" : '') . 'base64,' . base64_encode($data);
	}

	static function null($value)
	{
		return '';
	}

}

use Nette\Caching;

class Template extends Nette\Object implements ITemplate
{

	public $warnOnUndefined = TRUE;

	public $onPrepareFilters = array();

	private $source;

	private $params = array();

	private $filters = array();

	private $helpers = array();

	private $helperLoaders = array();

	private $cacheStorage;

	function setSource($source)
	{
		$this->source = $source;
		return $this;
	}

	function getSource()
	{
		return $this->source;
	}

	function render()
	{
		$cache = new Caching\Cache($storage = $this->getCacheStorage(), 'Nette.Template');
		$cached = $compiled = $cache->load($this->source);

		if ($compiled === NULL) {
			$compiled = $this->compile();
			$cache->save($this->source, $compiled, array(Caching\Cache::CONSTS => 'Nette\Framework::REVISION'));
			$cache->release();
			$cached = $cache->load($this->source);
		}

		if ($cached !== NULL && $storage instanceof Caching\Storages\PhpFileStorage) {
			Nette\Utils\LimitedScope::load($cached['file'], $this->getParams());
		} else {
			Nette\Utils\LimitedScope::evaluate($compiled, $this->getParams());
		}
	}

	function save($file)
	{
		if (file_put_contents($file, $this->__toString(TRUE)) === FALSE) {
			throw new Nette\IOException("Unable to save file '$file'.");
		}
	}

	function __toString()
	{
		$args = func_get_args();
		ob_start();
		try {
			$this->render();
			return ob_get_clean();

		} catch (\Exception $e) {
			ob_end_clean();
			if ($args && $args[0]) {
				throw $e;
			} else {
				Nette\Diagnostics\Debugger::toStringException($e);
			}
		}
	}

	function compile()
	{
		if (!$this->filters) {
			$this->onPrepareFilters($this);
		}

		$code = $this->getSource();
		foreach ($this->filters as $filter) {
			$code = self::extractPhp($code, $blocks);
			$code = $filter($code);
			$code = strtr($code, $blocks);
		}

		return self::optimizePhp($code);
	}

	function registerFilter($callback)
	{
		$callback = callback($callback);
		if (in_array($callback, $this->filters)) {
			throw new Nette\InvalidStateException("Filter '$callback' was registered twice.");
		}
		$this->filters[] = $callback;
	}

	final function getFilters()
	{
		return $this->filters;
	}

	function registerHelper($name, $callback)
	{
		$this->helpers[strtolower($name)] = callback($callback);
	}

	function registerHelperLoader($callback)
	{
		$this->helperLoaders[] = callback($callback);
	}

	final function getHelpers()
	{
		return $this->helpers;
	}

	function __call($name, $args)
	{
		$lname = strtolower($name);
		if (!isset($this->helpers[$lname])) {
			foreach ($this->helperLoaders as $loader) {
				$helper = $loader($lname);
				if ($helper) {
					$this->registerHelper($lname, $helper);
					return $this->helpers[$lname]->invokeArgs($args);
				}
			}
			return parent::__call($name, $args);
		}

		return $this->helpers[$lname]->invokeArgs($args);
	}

	function setTranslator(Nette\Localization\ITranslator $translator = NULL)
	{
		$this->registerHelper('translate', $translator === NULL ? NULL : array($translator, 'translate'));
		return $this;
	}

	function add($name, $value)
	{
		if (array_key_exists($name, $this->params)) {
			throw new Nette\InvalidStateException("The variable '$name' already exists.");
		}

		$this->params[$name] = $value;
	}

	function setParams(array $params)
	{
		$this->params = $params + $this->params;
		return $this;
	}

	function getParams()
	{
		$this->params['template'] = $this;
		return $this->params;
	}

	function __set($name, $value)
	{
		$this->params[$name] = $value;
	}

	function &__get($name)
	{
		if ($this->warnOnUndefined && !array_key_exists($name, $this->params)) {
			trigger_error("The variable '$name' does not exist in template.", E_USER_NOTICE);
		}

		return $this->params[$name];
	}

	function __isset($name)
	{
		return isset($this->params[$name]);
	}

	function __unset($name)
	{
		unset($this->params[$name]);
	}

	function setCacheStorage(Caching\IStorage $storage)
	{
		$this->cacheStorage = $storage;
	}

	function getCacheStorage()
	{
		if ($this->cacheStorage === NULL) {
			return new Caching\Storages\DevNullStorage;
		}
		return $this->cacheStorage;
	}

	private static function extractPhp($source, & $blocks)
	{
		$res = '';
		$blocks = array();
		$tokens = token_get_all($source);
		foreach ($tokens as $n => $token) {
			if (is_array($token)) {
				if ($token[0] === T_INLINE_HTML || $token[0] === T_CLOSE_TAG) {
					$res .= $token[1];
					continue;

				} elseif ($token[0] === T_OPEN_TAG && $token[1] === '<?' && isset($tokens[$n+1][1]) && $tokens[$n+1][1] === 'xml') {
					$php = & $res;
					$token[1] = '<<?php ?>?';

				} elseif ($token[0] === T_OPEN_TAG || $token[0] === T_OPEN_TAG_WITH_ECHO) {
					$res .= $id = "<?php \x01@php:p" . count($blocks) . "@\x02";
					$php = & $blocks[$id];
				}
				$php .= $token[1];

			} else {
				$php .= $token;
			}
		}
		return $res;
	}

	static function optimizePhp($source, $lineLength = 80, $existenceOfThisParameterSolvesDamnBugInPHP535 = NULL)
	{
		$res = $php = '';
		$lastChar = ';';
		$tokens = new \ArrayIterator(token_get_all($source));
		foreach ($tokens as $key => $token) {
			if (is_array($token)) {
				if ($token[0] === T_INLINE_HTML) {
					$lastChar = '';
					$res .= $token[1];

				} elseif ($token[0] === T_CLOSE_TAG) {
					$next = isset($tokens[$key + 1]) ? $tokens[$key + 1] : NULL;
					if (substr($res, -1) !== '<' && preg_match('#^<\?php\s*$#', $php)) {
						$php = '';

					} elseif (is_array($next) && $next[0] === T_OPEN_TAG) {
						if (!strspn($lastChar, ';{}:/')) {
							$php .= $lastChar = ';';
						}
						if (substr($next[1], -1) === "\n") {
							$php .= "\n";
						}
						$tokens->next();

					} elseif ($next) {
						$res .= preg_replace('#;?(\s)*$#', '$1', $php) . $token[1];
						if (strlen($res) - strrpos($res, "\n") > $lineLength
							&& (!is_array($next) || strpos($next[1], "\n") === FALSE)
						) {
							$res .= "\n";
						}
						$php = '';

					} else {
						if (!strspn($lastChar, '};')) {
							$php .= ';';
						}
					}

				} elseif ($token[0] === T_ELSE || $token[0] === T_ELSEIF) {
					if ($tokens[$key + 1] === ':' && $lastChar === '}') {
						$php .= ';';
					}
					$lastChar = '';
					$php .= $token[1];

				} else {
					if (!in_array($token[0], array(T_WHITESPACE, T_COMMENT, T_DOC_COMMENT, T_OPEN_TAG))) {
						$lastChar = '';
					}
					$php .= $token[1];
				}
			} else {
				$php .= $lastChar = $token;
			}
		}
		return $res . $php;
	}

}

class FileTemplate extends Template implements IFileTemplate
{

	private $file;

	function __construct($file = NULL)
	{
		if ($file !== NULL) {
			$this->setFile($file);
		}
	}

	function setFile($file)
	{
		$this->file = realpath($file);
		if (!$this->file) {
			throw new Nette\FileNotFoundException("Missing template file '$file'.");
		}
		return $this;
	}

	function getFile()
	{
		return $this->file;
	}

	function getSource()
	{
		return file_get_contents($this->file);
	}

	function render()
	{
		if ($this->file == NULL) {
			throw new Nette\InvalidStateException("Template file name was not specified.");
		}

		$cache = new Caching\Cache($storage = $this->getCacheStorage(), 'Nette.FileTemplate');
		if ($storage instanceof Caching\Storages\PhpFileStorage) {
			$storage->hint = str_replace(dirname(dirname($this->file)), '', $this->file);
		}
		$cached = $compiled = $cache->load($this->file);

		if ($compiled === NULL) {
			try {
				$compiled = "<?php\n\n// source file: $this->file\n\n?>" . $this->compile();

			} catch (FilterException $e) {
				$e->setSourceFile($this->file);
				throw $e;
			}

			$cache->save($this->file, $compiled, array(
				Caching\Cache::FILES => $this->file,
				Caching\Cache::CONSTS => 'Nette\Framework::REVISION',
			));
			$cache->release();
			$cached = $cache->load($this->file);
		}

		if ($cached !== NULL && $storage instanceof Caching\Storages\PhpFileStorage) {
			Nette\Utils\LimitedScope::load($cached['file'], $this->getParams());
		} else {
			Nette\Utils\LimitedScope::evaluate($compiled, $this->getParams());
		}
	}

}

}

namespace Nette\Utils {

use Nette;

final class Arrays
{

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function get(array $arr, $key, $default = NULL)
	{
		foreach (is_array($key) ? $key : array($key) as $k) {
			if (is_array($arr) && array_key_exists($k, $arr)) {
				$arr = $arr[$k];
			} else {
				if (func_num_args() < 3) {
					throw new Nette\InvalidArgumentException("Missing item '$k'.");
				}
				return $default;
			}
		}
		return $arr;
	}

	static function & getRef(& $arr, $key)
	{
		foreach (is_array($key) ? $key : array($key) as $k) {
			if (is_array($arr) || $arr === NULL) {
				$arr = & $arr[$k];
			} else {
				throw new Nette\InvalidArgumentException('Traversed item is not an array.');
			}
		}
		return $arr;
	}

	static function mergeTree($arr1, $arr2)
	{
		$res = $arr1 + $arr2;
		foreach (array_intersect_key($arr1, $arr2) as $k => $v) {
			if (is_array($v) && is_array($arr2[$k])) {
				$res[$k] = self::mergeTree($v, $arr2[$k]);
			}
		}
		return $res;
	}

	static function searchKey($arr, $key)
	{
		$foo = array($key => NULL);
		return array_search(key($foo), array_keys($arr), TRUE);
	}

	static function insertBefore(array &$arr, $key, array $inserted)
	{
		$offset = self::searchKey($arr, $key);
		$arr = array_slice($arr, 0, $offset, TRUE) + $inserted + array_slice($arr, $offset, count($arr), TRUE);
	}

	static function insertAfter(array &$arr, $key, array $inserted)
	{
		$offset = self::searchKey($arr, $key);
		$offset = $offset === FALSE ? count($arr) : $offset + 1;
		$arr = array_slice($arr, 0, $offset, TRUE) + $inserted + array_slice($arr, $offset, count($arr), TRUE);
	}

	static function renameKey(array &$arr, $oldKey, $newKey)
	{
		$offset = self::searchKey($arr, $oldKey);
		if ($offset !== FALSE) {
			$keys = array_keys($arr);
			$keys[$offset] = $newKey;
			$arr = array_combine($keys, $arr);
		}
	}

	static function grep(array $arr, $pattern, $flags = 0)
	{
		Nette\Diagnostics\Debugger::tryError();
		$res = preg_grep($pattern, $arr, $flags);
		Strings::catchPregError($pattern);
		return $res;
	}

}

final class CriticalSection
{

	private static $criticalSections;

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function enter()
	{
		if (self::$criticalSections) {
			throw new Nette\InvalidStateException('Critical section has already been entered.');
		}

		$handle = substr(PHP_OS, 0, 3) === 'WIN'
			? @fopen(NETTE_DIR . '/lockfile', 'w')
			: @fopen(__FILE__, 'r');

		if (!$handle) {
			throw new Nette\InvalidStateException("Unable initialize critical section.");
		}
		flock(self::$criticalSections = $handle, LOCK_EX);
	}

	static function leave()
	{
		if (!self::$criticalSections) {
			throw new Nette\InvalidStateException('Critical section has not been initialized.');
		}
		flock(self::$criticalSections, LOCK_UN);
		fclose(self::$criticalSections);
		self::$criticalSections = NULL;
	}

}

use RecursiveIteratorIterator;

class Finder extends Nette\Object implements \IteratorAggregate
{

	private $paths = array();

	private $groups;

	private $exclude = array();

	private $order = RecursiveIteratorIterator::SELF_FIRST;

	private $maxDepth = -1;

	private $cursor;

	static function find($mask)
	{
		if (!is_array($mask)) {
			$mask = func_get_args();
		}
		$finder = new static;
		return $finder->select(array(), 'isDir')->select($mask, 'isFile');
	}

	static function findFiles($mask)
	{
		if (!is_array($mask)) {
			$mask = func_get_args();
		}
		$finder = new static;
		return $finder->select($mask, 'isFile');
	}

	static function findDirectories($mask)
	{
		if (!is_array($mask)) {
			$mask = func_get_args();
		}
		$finder = new static;
		return $finder->select($mask, 'isDir');
	}

	private function select($masks, $type)
	{
		$this->cursor = & $this->groups[];
		$pattern = self::buildPattern($masks);
		if ($type || $pattern) {
			$this->filter(function($file) use($type, $pattern) {
				return !$file->isDot()
					&& (!$type || $file->$type())
					&& (!$pattern || preg_match($pattern, '/' . strtr($file->getSubPathName(), '\\', '/')));
			});
		}
		return $this;
	}

	function in($path)
	{
		if (!is_array($path)) {
			$path = func_get_args();
		}
		$this->maxDepth = 0;
		return $this->from($path);
	}

	function from($path)
	{
		if ($this->paths) {
			throw new Nette\InvalidStateException('Directory to search has already been specified.');
		}
		if (!is_array($path)) {
			$path = func_get_args();
		}
		$this->paths = $path;
		$this->cursor = & $this->exclude;
		return $this;
	}

	function childFirst()
	{
		$this->order = RecursiveIteratorIterator::CHILD_FIRST;
		return $this;
	}

	private static function buildPattern($masks)
	{
		$pattern = array();

		foreach ($masks as $mask) {
			$mask = rtrim(strtr($mask, '\\', '/'), '/');
			$prefix = '';
			if ($mask === '') {
				continue;

			} elseif ($mask === '*') {
				return NULL;

			} elseif ($mask[0] === '/') {
				$mask = ltrim($mask, '/');
				$prefix = '(?<=^/)';
			}
			$pattern[] = $prefix . strtr(preg_quote($mask, '#'),
				array('\*\*' => '.*', '\*' => '[^/]*', '\?' => '[^/]', '\[\!' => '[^', '\[' => '[', '\]' => ']', '\-' => '-'));
		}
		return $pattern ? '#/(' . implode('|', $pattern) . ')$#i' : NULL;
	}

	function getIterator()
	{
		if (!$this->paths) {
			throw new Nette\InvalidStateException('Call in() or from() to specify directory to search.');

		} elseif (count($this->paths) === 1) {
			return $this->buildIterator($this->paths[0]);

		} else {
			$iterator = new \AppendIterator();
			foreach ($this->paths as $path) {
				$iterator->append($this->buildIterator($path));
			}
			return $iterator;
		}
	}

	private function buildIterator($path)
	{
		if (PHP_VERSION_ID < 50301) {
			$iterator = new Nette\Utils\RecursiveDirectoryIteratorFixed($path);
		} else {
			$iterator = new \RecursiveDirectoryIterator($path, \RecursiveDirectoryIterator::FOLLOW_SYMLINKS);
		}

		if ($this->exclude) {
			$filters = $this->exclude;
			$iterator = new Nette\Iterators\RecursiveFilter($iterator, function($file) use($filters) {
				if (!$file->isFile()) {
					foreach ($filters as $filter) {
						if (!call_user_func($filter, $file)) {
							return FALSE;
						}
					}
				}
				return TRUE;
			});
		}

		if ($this->maxDepth !== 0) {
			$iterator = new RecursiveIteratorIterator($iterator, $this->order);
			$iterator->setMaxDepth($this->maxDepth);
		}

		if ($this->groups) {
			$groups = $this->groups;
			$iterator = new Nette\Iterators\Filter($iterator, function($file) use($groups) {
				foreach ($groups as $filters) {
					foreach ($filters as $filter) {
						if (!call_user_func($filter, $file)) {
							continue 2;
						}
					}
					return TRUE;
				}
				return FALSE;
			});
		}

		return $iterator;
	}

	function exclude($masks)
	{
		if (!is_array($masks)) {
			$masks = func_get_args();
		}
		$pattern = self::buildPattern($masks);
		if ($pattern) {
			$this->filter(function($file) use($pattern) {
				return !preg_match($pattern, '/' . strtr($file->getSubPathName(), '\\', '/'));
			});
		}
		return $this;
	}

	function filter($callback)
	{
		$this->cursor[] = $callback;
		return $this;
	}

	function limitDepth($depth)
	{
		$this->maxDepth = $depth;
		return $this;
	}

	function size($operator, $size = NULL)
	{
		if (func_num_args() === 1) {
			if (!preg_match('#^(?:([=<>!]=?|<>)\s*)?((?:\d*\.)?\d+)\s*(K|M|G|)B?$#i', $operator, $matches)) {
				throw new Nette\InvalidArgumentException('Invalid size predicate format.');
			}
			list(, $operator, $size, $unit) = $matches;
			static $units = array('' => 1, 'k' => 1e3, 'm' => 1e6, 'g' => 1e9);
			$size *= $units[strtolower($unit)];
			$operator = $operator ? $operator : '=';
		}
		return $this->filter(function($file) use($operator, $size) {
			return Finder::compare($file->getSize(), $operator, $size);
		});
	}

	function date($operator, $date = NULL)
	{
		if (func_num_args() === 1) {
			if (!preg_match('#^(?:([=<>!]=?|<>)\s*)?(.+)$#i', $operator, $matches)) {
				throw new Nette\InvalidArgumentException('Invalid date predicate format.');
			}
			list(, $operator, $date) = $matches;
			$operator = $operator ? $operator : '=';
		}
		$date = Nette\DateTime::from($date)->format('U');
		return $this->filter(function($file) use($operator, $date) {
			return Finder::compare($file->getMTime(), $operator, $date);
		});
	}

	static function compare($l, $operator, $r)
	{
		switch ($operator) {
		case '>':
			return $l > $r;
		case '>=':
			return $l >= $r;
		case '<':
			return $l < $r;
		case '<=':
			return $l <= $r;
		case '=':
		case '==':
			return $l == $r;
		case '!':
		case '!=':
		case '<>':
			return $l != $r;
		}
		throw new Nette\InvalidArgumentException("Unknown operator $operator.");
	}

}

if (PHP_VERSION_ID < 50301) {

	class RecursiveDirectoryIteratorFixed extends \RecursiveDirectoryIterator
	{
		function hasChildren()
		{
			return parent::hasChildren(TRUE);
		}
	}
}

final class Json
{
	const FORCE_ARRAY = 1;

	private static $messages = array(
		JSON_ERROR_DEPTH => 'The maximum stack depth has been exceeded',
		JSON_ERROR_STATE_MISMATCH => 'Syntax error, malformed JSON',
		JSON_ERROR_CTRL_CHAR => 'Unexpected control character found',
		JSON_ERROR_SYNTAX => 'Syntax error, malformed JSON',
	);

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function encode($value)
	{
		Nette\Diagnostics\Debugger::tryError();
		if (function_exists('ini_set')) {
			$old = ini_set('display_errors', 0);
			$json = json_encode($value);
			ini_set('display_errors', $old);
		} else {
			$json = json_encode($value);
		}
		if (Nette\Diagnostics\Debugger::catchError($e)) {
			throw new JsonException($e->getMessage());
		}
		return $json;
	}

	static function decode($json, $options = 0)
	{
		$json = (string) $json;
		$value = json_decode($json, (bool) ($options & self::FORCE_ARRAY));
		if ($value === NULL && $json !== '' && strcasecmp($json, 'null')) {
			$error = PHP_VERSION_ID >= 50300 ? json_last_error() : 0;
			throw new JsonException(isset(self::$messages[$error]) ? self::$messages[$error] : 'Unknown error', $error);
		}
		return $value;
	}

}

class JsonException extends \Exception
{
}

final class MimeTypeDetector
{

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function fromFile($file)
	{
		if (!is_file($file)) {
			throw new Nette\FileNotFoundException("File '$file' not found.");
		}

		$info = @getimagesize($file);
		if (isset($info['mime'])) {
			return $info['mime'];

		} elseif (extension_loaded('fileinfo')) {
			$type = preg_replace('#[\s;].*$#', '', finfo_file(finfo_open(FILEINFO_MIME), $file));

		} elseif (function_exists('mime_content_type')) {
			$type = mime_content_type($file);
		}

		return isset($type) && preg_match('#^\S+/\S+$#', $type) ? $type : 'application/octet-stream';
	}

	static function fromString($data)
	{
		if (extension_loaded('fileinfo') && preg_match('#^(\S+/[^\s;]+)#', finfo_buffer(finfo_open(FILEINFO_MIME), $data), $m)) {
			return $m[1];

		} elseif (strncmp($data, "\xff\xd8", 2) === 0) {
			return 'image/jpeg';

		} elseif (strncmp($data, "\x89PNG", 4) === 0) {
			return 'image/png';

		} elseif (strncmp($data, "GIF", 3) === 0) {
			return 'image/gif';

		} else {
			return 'application/octet-stream';
		}
	}

}

class Neon extends Nette\Object
{
	const BLOCK = 1;

	private static $patterns = array(
		'\'[^\'\n]*\'|"(?:\\\\.|[^"\\\\\n])*"',
		'@[a-zA-Z_0-9\\\\]+',
		'[:-](?=\s|$)|[,=[\]{}()]',
		'?:#.*',
		'\n[\t ]*',
		'[^#"\',:=@[\]{}()<>\x00-\x20!`](?:[^#,:=\]})>\x00-\x1F]+|:(?!\s|$)|(?<!\s)#)*(?<!\s)',
		'?:[\t ]+',
	);

	private static $tokenizer;

	private static $brackets = array(
		'[' => ']',
		'{' => '}',
		'(' => ')',
	);

	private $n = 0;

	private $indentTabs;

	static function encode($var, $options = NULL)
	{
		if ($var instanceof \DateTime) {
			return $var->format('Y-m-d H:i:s O');
		}
		if (is_object($var)) {
			$obj = $var; $var = array();
			foreach ($obj as $k => $v) {
				$var[$k] = $v;
			}
		}
		if (is_array($var)) {
			$isArray = array_keys($var) === range(0, count($var) - 1);
			$s = '';
			if ($options & self::BLOCK) {
				foreach ($var as $k => $v) {
					$v = self::encode($v, self::BLOCK);
					$s .= ($isArray ? '-' : self::encode($k) . ':')
						. (strpos($v, "\n") === FALSE ? ' ' . $v : "\n\t" . str_replace("\n", "\n\t", $v))
						. "\n";
					continue;
				}
				return $s;

			} else {
				foreach ($var as $k => $v) {
					$s .= ($isArray ? '' : self::encode($k) . ': ') . self::encode($v) . ', ';
				}
				return ($isArray ? '[' : '{') . substr($s, 0, -2) . ($isArray ? ']' : '}');
			}

		} elseif (is_string($var) && !is_numeric($var)
			&& !preg_match('~[\x00-\x1F]|^\d{4}|^(true|false|yes|no|on|off|null)$~i', $var)
			&& preg_match('~^' . self::$patterns[5] . '$~', $var)
		) {
			return $var;

		} else {
			return json_encode($var);
		}
	}

	static function decode($input)
	{
		if (!is_string($input)) {
			throw new Nette\InvalidArgumentException("Argument must be a string, " . gettype($input) . " given.");
		}
		if (!self::$tokenizer) {
			self::$tokenizer = new Tokenizer(self::$patterns, 'mi');
		}

		$input = str_replace("\r", '', $input);
		self::$tokenizer->tokenize($input);

		$parser = new static;
		$res = $parser->parse(0);

		while (isset(self::$tokenizer->tokens[$parser->n])) {
			if (self::$tokenizer->tokens[$parser->n][0] === "\n") {
				$parser->n++;
			} else {
				$parser->error();
			}
		}
		return $res;
	}

	private function parse($indent = NULL, $result = NULL)
	{
		$inlineParser = $indent === NULL;
		$value = $key = $object = NULL;
		$hasValue = $hasKey = FALSE;
		$tokens = self::$tokenizer->tokens;
		$n = & $this->n;
		$count = count($tokens);

		for (; $n < $count; $n++) {
			$t = $tokens[$n];

			if ($t === ',') {
				if (!$hasValue || !$inlineParser) {
					$this->error();
				}
				if ($hasKey) {
					$result[$key] = $value;
				} else {
					$result[] = $value;
				}
				$hasKey = $hasValue = FALSE;

			} elseif ($t === ':' || $t === '=') {
				if ($hasKey || !$hasValue) {
					$this->error();
				}
				if (is_array($value) || (is_object($value) && !method_exists($value, '__toString'))) {
					$this->error('Unacceptable key');
				} else {
					$key = (string) $value;
				}
				$hasKey = TRUE;
				$hasValue = FALSE;

			} elseif ($t === '-') {
				if ($hasKey || $hasValue || $inlineParser) {
					$this->error();
				}
				$key = NULL;
				$hasKey = TRUE;

			} elseif (isset(self::$brackets[$t])) {
				if ($hasValue) {
					if ($value[0] === '@' && $t === '(') {
						$n++;
						$value = $this->parse(NULL, array('@' => substr($value, 1)));
					} else {
						$this->error();
					}
				} else {
					$n++;
					$value = $this->parse(NULL, array());
				}
				$hasValue = TRUE;
				if (!isset($tokens[$n]) || $tokens[$n] !== self::$brackets[$t]) {
					$this->error();
				}

			} elseif ($t === ']' || $t === '}' || $t === ')') {
				if (!$inlineParser) {
					$this->error();
				}
				break;

			} elseif ($t[0] === "\n") {
				if ($inlineParser) {
					if ($hasValue) {
						if ($hasKey) {
							$result[$key] = $value;
						} else {
							$result[] = $value;
						}
						$hasKey = $hasValue = FALSE;
					}

				} else {
					while (isset($tokens[$n+1]) && $tokens[$n+1][0] === "\n") $n++;
					if (!isset($tokens[$n+1])) {
						break;
					}

					$newIndent = strlen($tokens[$n]) - 1;
					if ($indent === NULL) {
						$indent = $newIndent;
					}
					if ($newIndent) {
						if ($this->indentTabs === NULL) {
							$this->indentTabs = $tokens[$n][1] === "\t";
						}
						if (strpos($tokens[$n], $this->indentTabs ? ' ' : "\t")) {
							$n++;
							$this->error('Either tabs or spaces may be used as indenting chars, but not both.');
						}
					}

					if ($newIndent > $indent) {
						if ($hasValue || !$hasKey) {
							$n++;
							$this->error('Unexpected indentation.');
						} elseif ($key === NULL) {
							$result[] = $this->parse($newIndent);
						} else {
							$result[$key] = $this->parse($newIndent);
						}
						$newIndent = isset($tokens[$n]) ? strlen($tokens[$n]) - 1 : 0;
						$hasKey = FALSE;

					} else {
						if ($hasValue && !$hasKey) {
							break;

						} elseif ($hasKey) {
							$value = $hasValue ? $value : NULL;
							if ($key === NULL) {
								$result[] = $value;
							} else {
								$result[$key] = $value;
							}
							$hasKey = $hasValue = FALSE;
						}
					}

					if ($newIndent < $indent) {
						return $result;
					}
				}

			} else {
				if ($hasValue) {
					$this->error();
				}
				static $consts = array(
					'true' => TRUE, 'True' => TRUE, 'TRUE' => TRUE, 'yes' => TRUE, 'Yes' => TRUE, 'YES' => TRUE, 'on' => TRUE, 'On' => TRUE, 'ON' => TRUE,
					'false' => FALSE, 'False' => FALSE, 'FALSE' => FALSE, 'no' => FALSE, 'No' => FALSE, 'NO' => FALSE, 'off' => FALSE, 'Off' => FALSE, 'OFF' => FALSE,
				);
				if ($t[0] === '"') {
					$value = preg_replace_callback('#\\\\(?:u[0-9a-f]{4}|x[0-9a-f]{2}|.)#i', array($this, 'cbString'), substr($t, 1, -1));
				} elseif ($t[0] === "'") {
					$value = substr($t, 1, -1);
				} elseif (isset($consts[$t])) {
					$value = $consts[$t];
				} elseif ($t === 'null' || $t === 'Null' || $t === 'NULL') {
					$value = NULL;
				} elseif (is_numeric($t)) {
					$value = $t * 1;
				} elseif (preg_match('#\d\d\d\d-\d\d?-\d\d?(?:(?:[Tt]| +)\d\d?:\d\d:\d\d(?:\.\d*)? *(?:Z|[-+]\d\d?(?::\d\d)?)?)?$#A', $t)) {
					$value = new Nette\DateTime($t);
				} else {
					$value = $t;
				}
				$hasValue = TRUE;
			}
		}

		if ($inlineParser) {
			if ($hasValue) {
				if ($hasKey) {
					$result[$key] = $value;
				} else {
					$result[] = $value;
				}
			} elseif ($hasKey) {
				$this->error();
			}
		} else {
			if ($hasValue && !$hasKey) {
				if ($result === NULL) {
					$result = $value;
				} else {
					$this->error();
				}
			} elseif ($hasKey) {
				$value = $hasValue ? $value : NULL;
				if ($key === NULL) {
					$result[] = $value;
				} else {
					$result[$key] = $value;
				}
			}
		}
		return $result;
	}

	private function cbString($m)
	{
		static $mapping = array('t' => "\t", 'n' => "\n", '"' => '"', '\\' => '\\',  '/' => '/', '_' => "\xc2\xa0");
		$sq = $m[0];
		if (isset($mapping[$sq[1]])) {
			return $mapping[$sq[1]];
		} elseif ($sq[1] === 'u' && strlen($sq) === 6) {
			return Strings::chr(hexdec(substr($sq, 2)));
		} elseif ($sq[1] === 'x' && strlen($sq) === 4) {
			return chr(hexdec(substr($sq, 2)));
		} else {
			$this->error("Invalid escaping sequence $sq");
		}
	}

	private function error($message = "Unexpected '%s'")
	{
		list(, $line, $col) = self::$tokenizer->getOffset($this->n);
		$token = isset(self::$tokenizer->tokens[$this->n])
			? str_replace("\n", '<new line>', Strings::truncate(self::$tokenizer->tokens[$this->n], 40))
			: 'end';
		throw new NeonException(str_replace('%s', $token, $message) . " on line $line, column $col.");
	}

}

class NeonException extends \Exception
{
}

class Paginator extends Nette\Object
{

	private $base = 1;

	private $itemsPerPage = 1;

	private $page;

	private $itemCount;

	function setPage($page)
	{
		$this->page = (int) $page;
		return $this;
	}

	function getPage()
	{
		return $this->base + $this->getPageIndex();
	}

	function getFirstPage()
	{
		return $this->base;
	}

	function getLastPage()
	{
		return $this->itemCount === NULL ? NULL : $this->base + max(0, $this->getPageCount() - 1);
	}

	function setBase($base)
	{
		$this->base = (int) $base;
		return $this;
	}

	function getBase()
	{
		return $this->base;
	}

	protected function getPageIndex()
	{
		$index = max(0, $this->page - $this->base);
		return $this->itemCount === NULL ? $index : min($index, max(0, $this->getPageCount() - 1));
	}

	function isFirst()
	{
		return $this->getPageIndex() === 0;
	}

	function isLast()
	{
		return $this->itemCount === NULL ? FALSE : $this->getPageIndex() >= $this->getPageCount() - 1;
	}

	function getPageCount()
	{
		return $this->itemCount === NULL ? NULL : (int) ceil($this->itemCount / $this->itemsPerPage);
	}

	function setItemsPerPage($itemsPerPage)
	{
		$this->itemsPerPage = max(1, (int) $itemsPerPage);
		return $this;
	}

	function getItemsPerPage()
	{
		return $this->itemsPerPage;
	}

	function setItemCount($itemCount)
	{
		$this->itemCount = ($itemCount === FALSE || $itemCount === NULL) ? NULL : max(0, (int) $itemCount);
		return $this;
	}

	function getItemCount()
	{
		return $this->itemCount;
	}

	function getOffset()
	{
		return $this->getPageIndex() * $this->itemsPerPage;
	}

	function getCountdownOffset()
	{
		return $this->itemCount === NULL
			? NULL
			: max(0, $this->itemCount - ($this->getPageIndex() + 1) * $this->itemsPerPage);
	}

	function getLength()
	{
		return $this->itemCount === NULL
			? $this->itemsPerPage
			: min($this->itemsPerPage, $this->itemCount - $this->getPageIndex() * $this->itemsPerPage);
	}

}

class Strings
{

	final function __construct()
	{
		throw new Nette\StaticClassException;
	}

	static function checkEncoding($s, $encoding = 'UTF-8')
	{
		return $s === self::fixEncoding($s, $encoding);
	}

	static function fixEncoding($s, $encoding = 'UTF-8')
	{

		$s = @iconv('UTF-16', $encoding . '//IGNORE', iconv($encoding, 'UTF-16//IGNORE', $s));
		return str_replace("\xEF\xBB\xBF", '', $s);
	}

	static function chr($code, $encoding = 'UTF-8')
	{
		return iconv('UTF-32BE', $encoding . '//IGNORE', pack('N', $code));
	}

	static function startsWith($haystack, $needle)
	{
		return strncmp($haystack, $needle, strlen($needle)) === 0;
	}

	static function endsWith($haystack, $needle)
	{
		return strlen($needle) === 0 || substr($haystack, -strlen($needle)) === $needle;
	}

	static function normalize($s)
	{

		$s = str_replace("\r\n", "\n", $s);
		$s = strtr($s, "\r", "\n");

		$s = preg_replace('#[\x00-\x08\x0B-\x1F\x7F]+#', '', $s);

		$s = preg_replace("#[\t ]+$#m", '', $s);

		$s = trim($s, "\n");

		return $s;
	}

	static function toAscii($s)
	{
		$s = preg_replace('#[^\x09\x0A\x0D\x20-\x7E\xA0-\x{10FFFF}]#u', '', $s);
		$s = strtr($s, '`\'"^~', "\x01\x02\x03\x04\x05");
		if (ICONV_IMPL === 'glibc') {
			$s = @iconv('UTF-8', 'WINDOWS-1250//TRANSLIT', $s);
			$s = strtr($s, "\xa5\xa3\xbc\x8c\xa7\x8a\xaa\x8d\x8f\x8e\xaf\xb9\xb3\xbe\x9c\x9a\xba\x9d\x9f\x9e"
				. "\xbf\xc0\xc1\xc2\xc3\xc4\xc5\xc6\xc7\xc8\xc9\xca\xcb\xcc\xcd\xce\xcf\xd0\xd1\xd2\xd3"
				. "\xd4\xd5\xd6\xd7\xd8\xd9\xda\xdb\xdc\xdd\xde\xdf\xe0\xe1\xe2\xe3\xe4\xe5\xe6\xe7\xe8"
				. "\xe9\xea\xeb\xec\xed\xee\xef\xf0\xf1\xf2\xf3\xf4\xf5\xf6\xf8\xf9\xfa\xfb\xfc\xfd\xfe",
				"ALLSSSSTZZZallssstzzzRAAAALCCCEEEEIIDDNNOOOOxRUUUUYTsraaaalccceeeeiiddnnooooruuuuyt");
		} else {
			$s = @iconv('UTF-8', 'ASCII//TRANSLIT', $s);
		}
		$s = str_replace(array('`', "'", '"', '^', '~'), '', $s);
		return strtr($s, "\x01\x02\x03\x04\x05", '`\'"^~');
	}

	static function webalize($s, $charlist = NULL, $lower = TRUE)
	{
		$s = self::toAscii($s);
		if ($lower) {
			$s = strtolower($s);
		}
		$s = preg_replace('#[^a-z0-9' . preg_quote($charlist, '#') . ']+#i', '-', $s);
		$s = trim($s, '-');
		return $s;
	}

	static function truncate($s, $maxLen, $append = "\xE2\x80\xA6")
	{
		if (self::length($s) > $maxLen) {
			$maxLen = $maxLen - self::length($append);
			if ($maxLen < 1) {
				return $append;

			} elseif ($matches = self::match($s, '#^.{1,'.$maxLen.'}(?=[\s\x00-/:-@\[-`{-~])#us')) {
				return $matches[0] . $append;

			} else {
				return iconv_substr($s, 0, $maxLen, 'UTF-8') . $append;
			}
		}
		return $s;
	}

	static function indent($s, $level = 1, $chars = "\t")
	{
		return $level < 1 ? $s : self::replace($s, '#(?:^|[\r\n]+)(?=[^\r\n])#', '$0' . str_repeat($chars, $level));
	}

	static function lower($s)
	{
		return mb_strtolower($s, 'UTF-8');
	}

	static function upper($s)
	{
		return mb_strtoupper($s, 'UTF-8');
	}

	static function firstUpper($s)
	{
		return self::upper(mb_substr($s, 0, 1, 'UTF-8')) . mb_substr($s, 1, self::length($s), 'UTF-8');
	}

	static function capitalize($s)
	{
		return mb_convert_case($s, MB_CASE_TITLE, 'UTF-8');
	}

	static function compare($left, $right, $len = NULL)
	{
		if ($len < 0) {
			$left = iconv_substr($left, $len, -$len, 'UTF-8');
			$right = iconv_substr($right, $len, -$len, 'UTF-8');
		} elseif ($len !== NULL) {
			$left = iconv_substr($left, 0, $len, 'UTF-8');
			$right = iconv_substr($right, 0, $len, 'UTF-8');
		}
		return self::lower($left) === self::lower($right);
	}

	static function length($s)
	{
		return function_exists('mb_strlen') ? mb_strlen($s, 'UTF-8') : strlen(utf8_decode($s));
	}

	static function trim($s, $charlist = " \t\n\r\0\x0B\xC2\xA0")
	{
		$charlist = preg_quote($charlist, '#');
		return self::replace($s, '#^['.$charlist.']+|['.$charlist.']+$#u', '');
	}

	static function padLeft($s, $length, $pad = ' ')
	{
		$length = max(0, $length - self::length($s));
		$padLen = self::length($pad);
		return str_repeat($pad, $length / $padLen) . iconv_substr($pad, 0, $length % $padLen, 'UTF-8') . $s;
	}

	static function padRight($s, $length, $pad = ' ')
	{
		$length = max(0, $length - self::length($s));
		$padLen = self::length($pad);
		return $s . str_repeat($pad, $length / $padLen) . iconv_substr($pad, 0, $length % $padLen, 'UTF-8');
	}

	static function random($length = 10, $charlist = '0-9a-z')
	{
		$charlist = str_shuffle(preg_replace_callback('#.-.#', function($m) {
			return implode('', range($m[0][0], $m[0][2]));
		}, $charlist));
		$chLen = strlen($charlist);

		$s = '';
		for ($i = 0; $i < $length; $i++) {
			if ($i % 5 === 0) {
				$rand = lcg_value();
				$rand2 = microtime(TRUE);
			}
			$rand *= $chLen;
			$s .= $charlist[($rand + $rand2) % $chLen];
			$rand -= (int) $rand;
		}
		return $s;
	}

	static function split($subject, $pattern, $flags = 0)
	{
		Nette\Diagnostics\Debugger::tryError();
		$res = preg_split($pattern, $subject, -1, $flags | PREG_SPLIT_DELIM_CAPTURE);
		self::catchPregError($pattern);
		return $res;
	}

	static function match($subject, $pattern, $flags = 0, $offset = 0)
	{
		Nette\Diagnostics\Debugger::tryError();
		$res = preg_match($pattern, $subject, $m, $flags, $offset);
		self::catchPregError($pattern);
		if ($res) {
			return $m;
		}
	}

	static function matchAll($subject, $pattern, $flags = 0, $offset = 0)
	{
		Nette\Diagnostics\Debugger::tryError();
		$res = preg_match_all(
			$pattern, $subject, $m,
			($flags & PREG_PATTERN_ORDER) ? $flags : ($flags | PREG_SET_ORDER),
			$offset
		);
		self::catchPregError($pattern);
		return $m;
	}

	static function replace($subject, $pattern, $replacement = NULL, $limit = -1)
	{
		Nette\Diagnostics\Debugger::tryError();
		if (is_object($replacement) || is_array($replacement)) {
			if ($replacement instanceof Nette\Callback) {
				$replacement = $replacement->getNative();
			}
			if (!is_callable($replacement, FALSE, $textual)) {
				Nette\Diagnostics\Debugger::catchError($foo);
				throw new Nette\InvalidStateException("Callback '$textual' is not callable.");
			}
			$res = preg_replace_callback($pattern, $replacement, $subject, $limit);

			if (Nette\Diagnostics\Debugger::catchError($e)) {
				$trace = $e->getTrace();
				if (isset($trace[2]['class']) && $trace[2]['class'] === __CLASS__) {
					throw new RegexpException($e->getMessage() . " in pattern: $pattern");
				}
			}

		} elseif (is_array($pattern)) {
			$res = preg_replace(array_keys($pattern), array_values($pattern), $subject, $limit);

		} else {
			$res = preg_replace($pattern, $replacement, $subject, $limit);
		}
		self::catchPregError($pattern);
		return $res;
	}

	static function catchPregError($pattern)
	{
		if (Nette\Diagnostics\Debugger::catchError($e)) {
			throw new RegexpException($e->getMessage() . " in pattern: $pattern");

		} elseif (preg_last_error()) {
			static $messages = array(
				PREG_INTERNAL_ERROR => 'Internal error',
				PREG_BACKTRACK_LIMIT_ERROR => 'Backtrack limit was exhausted',
				PREG_RECURSION_LIMIT_ERROR => 'Recursion limit was exhausted',
				PREG_BAD_UTF8_ERROR => 'Malformed UTF-8 data',
				5 => 'Offset didn\'t correspond to the begin of a valid UTF-8 code point',
			);
			$code = preg_last_error();
			throw new RegexpException((isset($messages[$code]) ? $messages[$code] : 'Unknown error') . " (pattern: $pattern)", $code);
		}
	}

	static function expand($s, array $params, $recursive = FALSE)
	{
		$parts = preg_split('#%([\w.-]*)%#i', $s, -1, PREG_SPLIT_DELIM_CAPTURE);
		$res = '';
		foreach ($parts as $n => $part) {
			if ($n % 2 === 0) {
				$res .= $part;

			} elseif ($part === '') {
				$res .= '%';

			} elseif (isset($recursive[$part])) {
				throw new Nette\InvalidArgumentException('Circular reference detected for variables: ' . implode(', ', array_keys($recursive)) . '.');

			} else {
				$val = Arrays::get($params, explode('.', $part));
				if ($recursive && is_string($val)) {
					$val = self::expand($val, $params, (is_array($recursive) ? $recursive : array()) + array($part => 1));
				}
				if (strlen($part) + 2 === strlen($s)) {
					return $val;
				}
				if (!is_scalar($val)) {
					throw new Nette\InvalidArgumentException("Unable to concatenate non-scalar parameter '$part' into '$s'.");
				}
				$res .= $val;
			}
		}
		return $res;
	}

}

class RegexpException extends \Exception
{
}
}

namespace  {

 Nette\Diagnostics\Debugger::_init();

Nette\Utils\SafeStream::register();

function callback($callback, $m = NULL)
{
	return ($m === NULL && $callback instanceof Nette\Callback) ? $callback : new Nette\Callback($callback, $m);
}

function dump($var)
{
	foreach (func_get_args() as $arg) Nette\Diagnostics\Debugger::dump($arg);
	return $var;
}
}
