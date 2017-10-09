#ifndef		_PROJECT_TIME_WALK_APPS_SERVER_SERVER_HPP_
#define		_PROJECT_TIME_WALK_APPS_SERVER_SERVER_HPP_

#include <boost/asio.hpp>
#include <boost/thread.hpp>

#include <cstdint>
#include <memory>
#include <type_traits>

#include "connection/access_connection.hpp"
#include "connection/client_connection.hpp"

struct Port
{
    const std::uint16_t _client_port;
    const std::uint16_t _access_port;
    const std::uint16_t _archive_port;

    Port(
        const std::uint16_t & client_port,
        const std::uint16_t & access_port,
        const std::uint16_t & archive_port) :
        _client_port{ client_port },
        _access_port{ access_port },
        _archive_port{ archive_port }
    {}

    Port(void) = default;
    Port(const Port &) = default;
    Port(Port &&) = default;
};

class Server
{
    using tcp_t = boost::asio::ip::tcp;

    boost::asio::io_service _io_service;
    boost::thread_group _threads;

    tcp_t::acceptor _client_acceptor;
    tcp_t::acceptor _access_acceptor;
    tcp_t::acceptor _archive_acceptor;
    
    const Port _port;
    const std::size_t _thread_count;

public:

    Server(const Port & port, const std::size_t & thread_count) :        
        _client_acceptor{ _io_service },
        _access_acceptor{ _io_service },
        _archive_acceptor{ _io_service },
        _port{ port },
        _thread_count{ thread_count }
    {}

    ~Server(void)
    {
        for (std::size_t i = 0; i!= Server::_thread_count; ++i)
            Server::_threads.create_thread(
                [&] { Server::_io_service.run(); }
            );
    }

    void start(void)
    {
        setup_acceptor(
            Server::_client_acceptor, 
            Server::_port._client_port);

        setup_acceptor(
            Server::_access_acceptor,
            Server::_port._access_port);

        setup_acceptor(
            Server::_archive_acceptor,
            Server::_port._archive_port);

        accept(std::make_shared<ClientConnection>());
        accept(std::make_shared<AccessConnection>());
        
    }

private:

    void setup_acceptor(tcp_t::acceptor & acceptor, const std::uint16_t & port)
    {
        tcp_t::endpoint endpoint{ tcp_t::v4(), port };
        acceptor.open(endpoint.protocol());
        acceptor.set_option(tcp_t::acceptor::reuse_address(true));
        acceptor.bind(endpoint);
        acceptor.listen();
    }

    template <typename Type_>
    void accept(tcp_t::acceptor & acceptor,
        std::shared_ptr<Type_> connection)
    {
        static_assert(std::is_base_of<Connection, Type_>::value,
            "Invalid connection type, requires `Connection` Base");

        acceptor.async_accept(connection->socket(),
            [&, connection](const boost::system::error_code error)
        {
            if (error)
        });
    }
};

#endif	//	_PROJECT_TIME_WALK_APPS_SERVER_SERVER_HPP_