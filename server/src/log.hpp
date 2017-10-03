#ifndef		_PROJECT_TIME_WALK_LOG_HPP_
#define		_PROJECT_TIME_WALK_LOG_HPP_

#include <tuple>
#include <utility>
#include <iostream>


#include <mutex>
#include <boost/filesystem.hpp>

class Log
{
	static constexpr std::size_t DEFAULT_LOGS_PER_FILE = 500;

	static std::mutex & get_mutex(void)
	{
		static std::mutex mutex;
		return mutex;
	}

	static const std::size_t new_id(void)
	{
		static std::size_t id = 0;
		return id++;
	}

	std::size_t _logs_per_file = DEFAULT_LOGS_PER_FILE;
	const std::size_t _id = new_id();

public:

	enum LogType
	{
		DEBUG,
		INFO,
		ERROR
	};

	std::size_t & logs_per_file(void) { return Log::_logs_per_file; }

	template <typename... Types_>
	void operator() (const LogType & log_type, const Types_ ... data)
	{		
		// TODO: _io_service.post( ... 
		std::lock_guard<std::mutex> lock(get_mutex());
		
		switch (log_type)
		{
		case DEBUG:
			std::cout << "[DEBUG] ";
			break;
		case INFO:
			std::cout << "[INFO]";
			break;
		case ERROR:
			std::cout << "[ERROR]";
			break;
		}

		std::cout << " [ID = " << std::to_string(Log::_id) << "] ";
		print(std::cout, std::move(data) ...);
	}

private:

	template <typename First_, typename... Trail_>
	void print(std::ostream & out, const First_ & first, const Trail_ & ... trail)
	{
		out << first;
		print(out, trail...);
	}

	void print(std::ostream & out)
	{
		out << "\n";
	}
};

#endif	//	_PROJECT_TIME_WALK_LOG_HPP_