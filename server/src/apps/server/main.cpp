#include "server.hpp"

int main(void)
{
    static constexpr std::uint16_t client_port = 5001;
    static constexpr std::uint16_t access_port = 5002;
    static constexpr std::uint16_t archive_port = 5003;
    static constexpr std::size_t thread_count = 8;

    Server server {
        Port{ client_port, access_port, archive_port },
        thread_count 
    };
    server.start();
}