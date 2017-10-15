#ifndef		_PROJECT_TIME_WALK_SERVER_HPP_
#define		_PROJECT_TIME_WALK_SERVER_HPP_

#include <boost/asio.hpp>
#include <memory>
#include "connection.hpp"
#include "threads.hpp"
#include "log.hpp"

class Server
{
	using tcp_t = boost::asio::ip::tcp;

	static constexpr std::uint16_t PORT = 5001;

	boost::asio::io_service &	_io_service;
	tcp_t::acceptor				_acceptor;
	tcp_t::endpoint				_endpoint;
	Log							_log;

public:

	explicit Server(Threads &);
	void start(void);

private:

	void accept(std::shared_ptr<Connection>);
};

#endif	//	_PROJECT_TIME_WALK_SERVER_HPP_