#include "connection.hpp"
#include "log.hpp"
#include <memory>
#include <boost/asio.hpp>
#include <boost/thread.hpp>

using namespace boost;
using namespace boost::asio::ip;

void on_accept(system::error_code, std::shared_ptr<Connection>, tcp::acceptor &, Log &);

int main(void)
{
	constexpr std::uint16_t		server_port		= 5001;
	constexpr std::size_t		thread_count	= 8;

	//	** Initialise Server **

	asio::io_service	io_service;
	tcp::acceptor		acceptor{ io_service };
	boost::thread_group thread_pool;
	tcp::endpoint		endpoint{ tcp::v4(), server_port };
	Log					log;

	//	** Setup Acceptor Endpoint **

	acceptor.open(endpoint.protocol());
	acceptor.set_option(tcp::acceptor::reuse_address(true));
	acceptor.bind(endpoint);
	acceptor.listen();

	//	** First Accept **

	{
		auto conn = std::make_shared<Connection>(io_service);
		acceptor.async_accept(conn->socket(),
			[conn, &acceptor, &log](system::error_code error) 
		{
			on_accept(error, conn, acceptor, log);
		});
	}

	//	** Setup Thread Pool **

	for (std::size_t i = 0; i != thread_count; ++i)
		thread_pool.create_thread([&io_service] { io_service.run(); });
	thread_pool.join_all();
}

void on_accept(system::error_code error, std::shared_ptr<Connection> conn,
	tcp::acceptor & acceptor, Log & log)
{
	// log error or start the new connection
	if (error) log("Fatal accept - ", error.message());
	else conn->start();

	// start a new connection

	auto new_conn = std::make_shared<Connection>(acceptor.get_io_service());
	acceptor.async_accept(new_conn->socket(),
		[new_conn, &acceptor, &log](system::error_code error)
	{
		on_accept(error, new_conn, acceptor, log);
	});
}