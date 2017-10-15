#ifndef		_PROJECT_TIME_WALK_INTERFACE_HPP_
#define		_PROJECT_TIME_WALK_INTERFACE_HPP_

#include <boost/asio/io_service.hpp>
#include "threads.hpp"
#include "server.hpp"
#include "log.hpp"

// for .cpp
#include <string>
#include <iostream>

class Interface
{
	boost::asio::io_service & _io_service;
	Server & _server;
	Log _log;

public:

	explicit Interface(Threads & threads, Server & server) :
		_io_service{ threads.io_service() },
		_server{ server }
	{}

	void start(void)
	{
		Interface::_io_service.post(
			[&](void)
		{
			//while (true)
			//{
			//	// get commands
			//}
		});
	}
};

#endif	//	_PROJECT_TIME_WALK_INTERFACE_HPP_