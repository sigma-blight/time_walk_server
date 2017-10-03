#include "server.hpp"

Server::Server(Threads & threads) :
	_io_service{ threads.io_service() },
	_acceptor{ threads.io_service() },
	_endpoint{ tcp_t::v4(), PORT }
{}

void Server::start(void)
{
	Server::_acceptor.open(Server::_endpoint.protocol());
	Server::_acceptor.set_option(tcp_t::acceptor::reuse_address(true));
	Server::_acceptor.bind(Server::_endpoint);
	Server::_acceptor.listen();

	accept(std::make_shared<Connection>(Server::_io_service));
}

void Server::accept(std::shared_ptr<Connection> connection)
{
	Server::_acceptor.async_accept(connection->shared_from_this()->socket(),
		[&, connection](const boost::system::error_code error)
	{
		if (error) Server::_log(Log::ERROR, "Fatal Accept - ", error.message());
		else connection->start();
		Server::accept(std::make_shared<Connection>(connection));
	});
}