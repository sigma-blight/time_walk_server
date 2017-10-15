#include "threads.hpp"

boost::asio::io_service & Threads::io_service(void)
{
	return Threads::_io_service;
}

void Threads::join(void)
{
	for (size_t t = 0; t != thread_count; ++t)
		Threads::_group.create_thread([&] { Threads::_io_service.run(); });
	Threads::_group.join_all();
}