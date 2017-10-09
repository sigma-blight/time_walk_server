#ifndef     _PROJECT_TIME_WALK_CONNECTION_CONNECTION_HPP_
#define     _PROJECT_TIME_WALK_CONNECTION_CONNECTION_HPP_

#include <boost/asio.hpp>
#include <memory>
#include "data/data_in.hpp"
#include "data/data_out.hpp"

class Connection : public std::enable_shared_from_this<Connection>
{
private:

    using tcp_t = boost::asio::ip::tcp;

    tcp_t::socket _socket;

public:

    tcp_t::socket & socket(void) { return Connection::_socket; }
    const tcp_t::socket & socket(void) const { return Connection::_socket; }
};

#endif  //  _PROJECT_TIME_WALK_CONNECTION_CONNECTION_HPP_