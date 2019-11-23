<?php
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
//START

/**
 * This file is part of the Nette Framework (http://nette.org)
 *
 * Copyright (c) 2004, 2011 David Grudl (http://davidgrudl.com)
 *
 * For the full copyright and license information, please view
 * the file license.txt that was distributed with this source code.
 */

namespace Nette\Application;

use Nette;



/**
 * Front Controller.
 *
 * @author     David Grudl
 */
class Application extends Nette\Object
{
	/** @var int */
	public static $maxLoop = 20;

	/** @var bool enable fault barrier? */
	public $catchExceptions;

	/** @var string */
	public $errorPresenter;

	/** @var array of function(Application $sender); Occurs before the application loads presenter */
	public $onStartup;

	/** @var array of function(Application $sender, \Exception $e = NULL); Occurs before the application shuts down */
	public $onShutdown;

	/** @var array of function(Application $sender, Request $request); Occurs when a new request is ready for dispatch */
	public $onRequest;

	/** @var array of function(Application $sender, IResponse $response); Occurs when a new response is received */
	public $onResponse;

	/** @var array of function(Application $sender, \Exception $e); Occurs when an unhandled exception occurs in the application */
	public $onError;

	/** @var array of string */
	public $allowedMethods = array('GET', 'POST', 'HEAD', 'PUT', 'DELETE');

	/** @var array of Request */
	private $requests = array();

	/** @var IPresenter */
	private $presenter;

	/** @var Nette\DI\IContainer */
	private $context;



	public function __construct(Nette\DI\IContainer $context)
	{
		$this->context = $context;
	}



	/**
	 * Dispatch a HTTP request to a front controller.
	 * @return void
	 */
	public function run()
	{
		$httpRequest = $this->context->httpRequest;
		$httpResponse = $this->context->httpResponse;

		// check HTTP method
		if ($this->allowedMethods) {
			$method = $httpRequest->getMethod();
			if (!in_array($method, $this->allowedMethods, TRUE)) {
				$httpResponse->setCode(Nette\Http\IResponse::S501_NOT_IMPLEMENTED);
				$httpResponse->setHeader('Allow', implode(',', $this->allowedMethods));
				echo '<h1>Method ' . htmlSpecialChars($method) . ' is not implemented</h1>';
				return;
			}
		}

		// dispatching
		$request = NULL;
		$repeatedError = FALSE;
		do {
			try {
				if (count($this->requests) > self::$maxLoop) {
					throw new ApplicationException('Too many loops detected in application life cycle.');
				}

				if (!$request) {
					$this->onStartup($this);

					// routing
					$router = $this->getRouter();

					// enable routing debugger
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

				// Instantiate presenter
				$presenterName = $request->getPresenterName();
				try {
					$this->presenter = $this->getPresenterFactory()->createPresenter($presenterName);
				} catch (InvalidPresenterException $e) {
					throw new BadRequestException($e->getMessage(), 404, $e);
				}

				$this->getPresenterFactory()->getPresenterClass($presenterName);
				$request->setPresenterName($presenterName);
				$request->freeze();

				// Execute presenter
				$response = $this->presenter->run($request);
				$this->onResponse($this, $response);

				// Send response
				if ($response instanceof Responses\ForwardResponse) {
					$request = $response->getRequest();
					continue;

				} elseif ($response instanceof IResponse) {
					$response->send($httpRequest, $httpResponse);
				}
				break;
                hereIsEmptyStatement();;
			} catch (\Exception $e) {
				// fault barrier
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
					// continue

				} else { // default error handler
					if ($e instanceof BadRequestException) {
						$code = $e->getCode();
					} else {
						$code = 500;
						Nette\Diagnostics\Debugger::log($e, Nette\Diagnostics\Debugger::ERROR);
					}
					require __DIR__ . '/templates/error.phtml';
					break;
				}
			}
		} while (1);

		$this->onShutdown($this, isset($e) ? $e : NULL);
	}



	/**
	 * Returns all processed requests.
	 * @return array of Request
	 */
	final public function getRequests()
	{
		return $this->requests;
	}



	/**
	 * Returns current presenter.
	 * @return IPresenter
	 */
	final public function getPresenter()
	{
		return $this->presenter;
	}



	/********************* services ****************d*g**/



	/**
	 * Gets the context.
	 * @return Nette\DI\IContainer
	 */
	final public function getContext()
	{
		return $this->context;
	}



	/**
	 * Returns router.
	 * @return IRouter
	 */
	public function getRouter()
	{
		return $this->context->router;
	}



	/**
	 * Returns presenter factory.
	 * @return IPresenterFactory
	 */
	public function getPresenterFactory()
	{
		return $this->context->presenterFactory;
	}



	/********************* request serialization ****************d*g**/



	/**
	 * Stores current request to session.
	 * @param  mixed  optional expiration time
	 * @return string key
	 */
	public function storeRequest($expiration = '+ 10 minutes')
	{
		$session = $this->context->session->getNamespace('Nette.Application/requests');
		do {
			$key = Nette\Utils\Strings::random(5);
		} while (isset($session[$key]));

		$session[$key] = end($this->requests);
		$session->setExpiration($expiration, $key);
		return $key;
	}



	/**
	 * Restores current request to session.
	 * @param  string key
	 * @return void
	 */
	public function restoreRequest($key)
	{
		$session = $this->context->session->getNamespace('Nette.Application/requests');
		if (isset($session[$key])) {
			$request = clone $session[$key];
			unset($session[$key]);
			$request->setFlag(Request::RESTORED, TRUE);
			$this->presenter->sendResponse(new Responses\ForwardResponse($request));
		}
	}

}

//END
?>
