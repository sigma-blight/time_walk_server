#ifndef     _PROJECT_TIME_WALK_CONNECTION_CLIENT_CONNECTION_HPP_
#define     _PROJECT_TIME_WALK_CONNECTION_CLIENT_CONNECTION_HPP_

#include "connection/connection.hpp"

class ClientConnection : public Connection
{
public:

    using Connection::Connection;

    void start() 
    {
    }
};

#endif  //  _PROJECT_TIME_WALK_CONNECTION_CLIENT_CONNECTION_HPP_