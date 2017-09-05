#ifndef		_PROJECT_TIME_WALK_CONNECTION_HPP_
#define		_PROJECT_TIME_WALK_CONNECTION_HPP_

#include "log.hpp"
#include "request.hpp"

#include <memory>
#include <string>
#include <sstream>
#include <boost/asio.hpp>

class Connection : public std::enable_shared_from_this<Connection>
{
	using io_service_t = boost::asio::io_service;
	using socket_t = boost::asio::ip::tcp::socket;
	using stream_buf_t = boost::asio::streambuf;

	static constexpr const char READ_UNTIL = ';';

	io_service_t &	_io_service;
	socket_t		_socket;
	stream_buf_t	_read_buffer;
	Log				_log;
	Request			_request;

public:

	Connection(io_service_t & io_service) :
		_io_service{ io_service },
		_socket{ io_service },
		_request{ _log }
	{}

	~Connection(void)
	{
		Connection::disconnect();
	}

	socket_t & socket(void) { return Connection::_socket; }
	const socket_t & socket(void) const { return Connection::_socket; }

	void start(void)
	{
		Connection::_log("Starting new connection");
		Connection::start_read();
	}

private:

	void start_read(void)
	{
		auto self = shared_from_this();
		boost::asio::async_read_until(self->_socket,
			self->_read_buffer, Connection::READ_UNTIL, 
			[self](boost::system::error_code error, std::size_t bytes)
		{
			self->on_read(error, bytes);
		});
	}

	void on_read(boost::system::error_code error, std::size_t bytes)
	{
		if (error)
		{
			Connection::_log("Fatal read - ", error.message());
			return;
		}
		
		std::string read_data;
		std::istream stream{ &(Connection::_read_buffer) };
		std::getline(stream, read_data);
		
		
		auto process = Connection::_request.process(read_data);

		// informs client if transfer is successful and file or text
		Connection::start_write(std::to_string(
			static_cast<std::size_t>(process.transfer_code)));

		// write related error message to client
		if (process.is_invalid)
			Connection::start_write(process.data); // data -> error message

		// write simple text line to client
		else if (process.is_text)
			Connection::start_write(process.data); // data -> text to send

		// transfer file to client
		else if (process.is_file)
		{
			// file transfer
		}

		// start another read after processing request
		Connection::start_read();
	}

	// pass by value to avoid thread issues
	void start_write(std::string data)
	{

	}

	void disconnect(void)
	{
		Connection::_log("Shutting down connection");
		Connection::_socket.shutdown(socket_t::shutdown_both);
		Connection::_socket.close();
	}
};

#endif	//	_PROJECT_TIME_WALK_CONNECTION_HPP_