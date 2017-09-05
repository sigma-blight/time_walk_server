#ifndef		_PROJECT_TIME_WALK_LOG_HPP_
#define		_PROJECT_TIME_WALK_LOG_HPP_

#include <iostream>
#include <chrono>
#include <ctime>
#include <mutex>

class Log
{
	static std::mutex & create_mutex(void)
	{
		static std::mutex mutex;
		return mutex;
	}

	static const std::size_t new_id(void)
	{
		static std::size_t id = 0;
		return id++;
	}

	std::mutex &		_mutex	= create_mutex();
	const std::size_t	_id		= new_id();

public:

	// pass by value for thread safety
	template <typename... Types_>
	void operator() (Types_ ... data)
	{
		std::lock_guard<std::mutex> lock{ Log::_mutex };
		std::time_t timestamp = std::chrono::system_clock::to_time_t(std::chrono::system_clock::now());
		std::cout 
			// ID
			<< "[" << Log::_id << "] - "
			// Time stamp (prints newline)
			<< std::ctime(&timestamp)
			<< "\t";
		print(data...);
	}

private:

	void print(void)
	{
		std::cout << "\n\n";
	}

	template <typename First_, typename... Trail_>
	void print(const First_ & first, const Trail_ & ... trail)
	{
		std::cout << first;
		print(trail...);
	}
};

#endif	//	_PROJECT_TIME_WALK_LOG_HPP_