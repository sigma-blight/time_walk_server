#ifndef		_PROJECT_TIME_WALK_REQUEST_HPP_
#define		_PROJECT_TIME_WALK_REQUEST_HPP_

#include "log.hpp"
#include <string>

enum class TransferCode
{
	INVALID_REQUEST,
	EMPTY_DIRECTORY,
	INVALID_FILE,
	SUCCESS
};

struct Process
{
	bool			is_invalid;
	bool			is_text;
	bool			is_file;
	TransferCode	transfer_code;
	std::string		data;
};

class Request
{
	Log & _log;

public:

	Request(Log & log) :
		_log{ log }
	{}

	Process process(const std::string & request)
	{
		Request::_log("Processing request - ", request);
		Process process;
		process.is_invalid = false;
		process.is_text = false;
		process.is_file = false;
		return process;
	}
};

#endif	//	_PROJECT_TIME_WALK_REQUEST_HPP_