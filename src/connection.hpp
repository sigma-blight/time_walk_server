#ifndef		_PROJECT_TIME_WALK_CONNECTION_HPP_
#define		_PROJECT_TIME_WALK_CONNECTION_HPP_

#include "log.hpp"
#include "request.hpp"

#include <memory>
#include <vector>
#include <string>
#include <sstream>
#include <fstream>
#include <boost/asio.hpp>

class Connection : public std::enable_shared_from_this<Connection>
{
	using io_service_t = boost::asio::io_service;
	using socket_t = boost::asio::ip::tcp::socket;
	using stream_buf_t = boost::asio::streambuf;

	static constexpr const char			READ_UNTIL = ';';
	static constexpr const std::size_t	CHUNK_SIZE = 10240; // 10KB

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

		// delete the READ_UNTIL character
		auto pos = read_data.find_last_of(READ_UNTIL);
		if (pos != std::string::npos) read_data.erase(pos);
		
		
		auto process = Connection::_request.process(read_data);

		// informs client if transfer is successful and file or text
		std::size_t code = static_cast<std::size_t>(process.transfer_code);
		Connection::_log("TransferCode - ", code);
		Connection::start_write(code);

		// write related error message to client
		if (process.is_invalid)
		{
			Connection::_log("Invalid request - ", process.data);
			Connection::start_write(process.data); // data -> error message
		}

		// write simple text line to client
		else if (process.is_text)
		{
			// write size first for the text files
			if (process.request_code == RequestCode::GET_TEXT)
			{
				Connection::_log("Writing Size - ", process.data.size());
				Connection::start_write(process.data.size());
			}

			Connection::_log("Sending text - ", process.data);	
			Connection::start_write(process.data); // data -> text to send
		}

		// transfer file to client
		else if (process.is_file)
		{
			Connection::_log("Sending file - ", process.data);
			
			std::ifstream file;
			file.open(process.data, std::ios::binary | std::ios::ate);
			start_write(file.tellg()); // write file size
			file.close();
			// start writing
			auto self = shared_from_this();
			self->start_file_write(std::make_shared<std::ifstream>(process.data, std::ios::binary));
		}

		// start another read after processing request
		Connection::start_read();
	}

	void start_write(std::size_t code)
	{
		auto self = shared_from_this();
		boost::system::error_code error;
		boost::asio::write(self->socket(),
			boost::asio::buffer(*std::make_shared<std::string>(std::to_string(code) + "\n")),
			error);

		if (error)
			self->_log("Fatal Write - ", error.message());
	}

	// pass by value to avoid thread issues
	void start_write(std::string data)
	{
		auto self = shared_from_this();
		data.push_back('\n');
		boost::asio::async_write(self->socket(),
			boost::asio::buffer(*std::make_shared<std::string>(data)),
			boost::asio::transfer_all(),
			[self](boost::system::error_code error, std::size_t bytes)
		{
			if (error)
				self->_log("Fatal Write - ", error.message());
			else
				self->_log("Wrote ", bytes, " bytes");
		});
	}

	void start_file_write(std::shared_ptr<std::ifstream> file,
		boost::system::error_code error = boost::system::error_code{},
		std::size_t bytes = 0, std::size_t total_bytes = 0)
	{
		total_bytes += bytes;

		if (error)
			return Connection::_log("Fatal write from file transfer - ", error.message());
		if (file->eof())
			return Connection::_log("File transfer complete of ", total_bytes, " bytes");

		Connection::_log("Transfered ", total_bytes, " so far");

		std::vector<char> data(CHUNK_SIZE);
		file->read(&data[0], CHUNK_SIZE);

		auto self = shared_from_this();
		boost::asio::async_write(self->socket(),
			boost::asio::buffer(*std::make_shared<std::vector<char>>(data)),
			boost::asio::transfer_all(),
			[self, file, total_bytes](boost::system::error_code error, std::size_t bytes)
		{
			self->start_file_write(file, error, bytes, total_bytes);
		});
	}

	void disconnect(void)
	{
		Connection::_log("Shutting down connection");
		Connection::_socket.shutdown(socket_t::shutdown_both);
		Connection::_socket.close();
	}
};

#endif	//	_PROJECT_TIME_WALK_CONNECTION_HPP_