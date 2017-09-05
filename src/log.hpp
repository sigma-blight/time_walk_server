#ifndef		_PROJECT_TIME_WALK_LOG_HPP_
#define		_PROJECT_TIME_WALK_LOG_HPP_

#include <iostream>
#include <mutex>

class Log
{
	static std::mutex & create_mutex(void)
	{
		static std::mutex mutex;
		return mutex;
	}

	std::mutex & _mutex = create_mutex();

public:

	// pass by value for thread safety
	template <typename... Types_>
	void operator() (Types_ ... data)
	{
		std::lock_guard<std::mutex> lock{ Log::_mutex };
		print(data...);
	}

private:

	void print(void)
	{
		std::cout << "\n";
	}

	template <typename First_, typename... Trail_>
	void print(const First_ & first, const Trail_ & ... trail)
	{
		std::cout << first;
		print(trail...);
	}
};

#endif	//	_PROJECT_TIME_WALK_LOG_HPP_