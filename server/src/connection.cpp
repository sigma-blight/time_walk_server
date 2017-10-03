#include "connection.hpp"
#include <string>
#include <stringstream>

Connection::Connection(boost::asio::io_service & io_service) :
	_io_service{ io_service },
	_socket{ io_service }
{}

Connection::Connection(std::shared_ptr<Connection> connection) :
	_io_service{ connection->_io_service },
	_socket{ connection->_io_service }
{}

Connection::~Connection(void)
{
	Connection::stop();
}

Connection::tcp_t::socket & Connection::socket(void)
{
	return Connection::_socket;
}

void Connection::start(void)
{
	Connection::_log(Log::INFO, "Starting New Connection");
	Connection::start_read();
}

void Connection::stop(void)
{
	Connection::_log(Log::INFO, "Stopping Connection");
	// TODO: shutdown
}

void Connection::start_read(void)
{
	auto conn = shared_from_this();
	boost::asio::async_read_until(
		conn->_socket,
		conn->_request,
		';',
		[conn](const boost::system::error_code error,
			const std::size_t bytes)
	{
		// TODO: distinguish fatal read and error
		if (error)
		{
			conn->_log(Log::ERROR, "Fatal Read - ", error.message());
			conn->stop();
		}
		else
		{
			std::string request;
			std::istream stream(&conn->_request);
			std::getline(stream, request);

			conn->_log(Log::INFO, "Successful Read [", bytes, " bytes] - ", request);
		}
	});
}