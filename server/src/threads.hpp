#ifndef		_PROJECT_TIME_WALK_THREADS_HPP_
#define		_PROJECT_TIME_WALK_THREADS_HPP_

#include <boost/thread.hpp>
#include <boost/asio/io_service.hpp>

class Threads
{
	static constexpr std::size_t thread_count = 8;

	boost::thread_group			_group;
	boost::asio::io_service		_io_service;

public:

	boost::asio::io_service & io_service(void);
	void join(void);
};

#endif	//	_PROJECT_TIME_WALK_THREADS_HPP_