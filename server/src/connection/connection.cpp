#include "connection.hpp"

Connection::tcp_t::socket & Connection::socket(void) 
{ 
    return Connection::_socket; 
}

const Connection::tcp_t::socket & Connection::socket(void) const 
{ 
    return Connection::_socket;
}