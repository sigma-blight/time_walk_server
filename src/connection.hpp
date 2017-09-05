#ifndef		_PROJECT_TIME_WALK_CONNECTION_HPP_
#define		_PROJECT_TIME_WALK_CONNECTION_HPP_

#include "log.hpp"
#include <memory>
#include <boost/asio.hpp>

class Connection : public std::enable_shared_from_this<Connection>
{
	using io_service_t = boost::asio::io_service;
	using socket_t = boost::asio::ip::tcp::socket;

	io_service_t &	_io_service;
	socket_t		_socket;
	Log				_log;

public:

	Connection(io_service_t & io_service) :
		_io_service{ io_service },
		_socket{ io_service }
	{}

	socket_t & socket(void) { return Connection::_socket; }
	const socket_t & socket(void) const { return Connection::_socket; }

	void start(void)
	{
		Connection::_log("Starting new connection");
	}
};

#endif	//	_PROJECT_TIME_WALK_CONNECTION_HPP_