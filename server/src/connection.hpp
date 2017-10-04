#ifndef		_PROJECT_TIME_WALK_CONNECTION_HPP_
#define		_PROJECT_TIME_WALK_CONNECTION_HPP_

#include <boost/asio.hpp>
#include <memory>
#include "log.hpp"

class Connection : public std::enable_shared_from_this<Connection>
{
	using tcp_t = boost::asio::ip::tcp;

	boost::asio::io_service &	_io_service;
	tcp_t::socket				_socket;
	Log							_log;
	boost::asio::streambuf		_request;

public:

	Connection(boost::asio::io_service & io_service);
	Connection(std::shared_ptr<Connection> connection);
	~Connection(void);
	tcp_t::socket & socket(void);
	void start(void);

private:

	void stop(void);
	void start_read(void);
	void start_write(const std::string);
	void continue_write(std::shared_ptr<std::string>);
};

#endif	//	_PROJECT_TIME_WALK_CONNECTION_HPP_