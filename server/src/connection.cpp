#include "connection.hpp"
#include "processor.hpp"
#include <string>
#include <sstream>

static constexpr const char DELIMITER = ';';

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
	// TODO: shutdown - don't print this twice (destructor)
	Connection::_log(Log::INFO, "Stopping Connection");	
	boost::system::error_code code;
        Connection::_socket.shutdown(tcp_t::socket::shutdown_both, code);
	if (!code)
		Connection::_socket.close();
}

void Connection::start_read(void)
{
	auto conn = shared_from_this();
	boost::asio::async_read_until(
		conn->_socket,
		conn->_request,
		DELIMITER,
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
			request.pop_back(); // remove new_line from getline
			request.pop_back(); // remove the trailing delimiter

			conn->_log(Log::INFO, "Successful Read of ", bytes, " bytes - ", request);
			
			conn->start_write(process(request, conn->_log));
			conn->start_read();
		}
	});
}

/*

DATA:		abcdefg
SEND:		to_string(size of DATA) + new_line
SEND:		DATA + new_line

*/

void Connection::start_write(const std::string data)
{
	auto conn = shared_from_this();
	auto data_ptr = std::make_shared<std::string>(data);
	auto size_str_ptr = std::make_shared<std::string>(
		std::to_string(data.size()).append("\n"));
	data_ptr->append("\n");

	boost::asio::async_write(
		conn->_socket,
		boost::asio::buffer(*size_str_ptr),
		[conn, data_ptr, size_str_ptr]
		(const boost::system::error_code error, const std::size_t bytes)
	{
		if (error)
		{
			conn->_log(Log::ERROR, "Fatal Size Write - ", error.message());
			conn->stop();
		}
		else
		{

			size_str_ptr->pop_back(); // remove newline for logging
			conn->_log(Log::INFO, "Successful Size Write of ", bytes,
				" bytes - ", *size_str_ptr);

			conn->continue_write(data_ptr);
		}
	});
}

void Connection::continue_write(std::shared_ptr<std::string> data_ptr)
{
	auto conn = shared_from_this();
	boost::asio::async_write(
		conn->_socket,
		boost::asio::buffer(*data_ptr),
		[conn, data_ptr](const boost::system::error_code error,
			const std::size_t bytes)
	{
		if (error)
		{
			conn->_log(Log::ERROR, "Fatal Write - ", error.message());
			conn->stop();
		}
		else
		{
			data_ptr->pop_back(); // remove newline for logging
			conn->_log(Log::INFO, "Successful Write of ", bytes, " bytes - ", *data_ptr);
		}
	});
}
