#ifndef		_PROJECT_TIME_WALK_REQUEST_HPP_
#define		_PROJECT_TIME_WALK_REQUEST_HPP_

#include "log.hpp"
#include <string>
#include <sstream>
#include <boost/filesystem.hpp>

enum class TransferCode
{
	TEXT					= 00,
	IMAGE					= 01,
	EMPTY_DIRECTORY			= 02,
	INVALID_REQUEST			= 10,
	INVALID_DIRECTORY		= 11,
	INVALID_FILE			= 12
};

enum class RequestCode
{
	LIST_REGIONS			= 00,
	LIST_LANDMARKS			= 01,
	LIST_IMAGES				= 02,
	GET_GPS					= 10,
	GET_TEXT				= 11,
	TRANSFER_MAIN_REGION	= 20,
	TRANSFER_MAIN_LANDMARK	= 21,
	TRANSFER_IMAGE			= 22
};

struct Process
{
	bool			is_invalid;
	bool			is_text;
	bool			is_file;
	TransferCode	transfer_code;
	std::string		data;
};

static constexpr const char ROOT_DIR[] = "../../archives/time_walk/";
static constexpr const char MAIN_PHOTO_NAME[] = "main_photo";
static constexpr const char GPS_NAME[] = "gps";
static constexpr const char SEPERATOR = ' ';

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

		if (request.empty())
		{
			process.is_invalid = true;
			process.data = "empty request";
			process.transfer_code = TransferCode::INVALID_REQUEST;
		}
		else
		{
			std::size_t code;
			std::istringstream stream{ request };
			stream >> code;

			if (stream.fail())
			{
				process.is_invalid = true;
				process.data = "invalid request";
				process.transfer_code = TransferCode::INVALID_REQUEST;
			}
			else
			{
				auto rc = static_cast<RequestCode>(code);
				switch (rc)
				{
				case RequestCode::LIST_REGIONS:
				case RequestCode::LIST_LANDMARKS:
				case RequestCode::LIST_IMAGES:
					list_process(rc, stream, process);
					break;
				case RequestCode::GET_GPS:
				case RequestCode::GET_TEXT: 
					get_process(rc, stream, process);
					break;
				case RequestCode::TRANSFER_MAIN_REGION:
				case RequestCode::TRANSFER_MAIN_LANDMARK:
				case RequestCode::TRANSFER_IMAGE: 
					transfer_process(rc, stream, process);
					break;
				default:
					process.is_invalid = true;
					process.data = "invalid request code";
					process.transfer_code = TransferCode::INVALID_REQUEST;
					break;
				}
			}
		}	

		return process;
	}

private:

	void list_process(RequestCode code, std::istringstream & stream, Process & process)
	{
		using namespace boost::filesystem;
		Request::_log("Processing list command");		

		path path{ ROOT_DIR };

		auto stream_gob = [&path, &process, &stream](std::size_t args, 
			const std::string & arg_fault = "invalid arguments")
			-> bool
		{
			for (std::size_t i = 0; i != args; ++i)
			{
				if (stream.eof())
				{
					process.is_invalid = true;
					process.data = arg_fault;
					process.transfer_code = TransferCode::INVALID_REQUEST;
					return false;
				}

				std::string dir;
				stream >> dir;
				if (stream.fail())
				{
					process.is_invalid = true;
					process.data = "invalid request";
					process.transfer_code = TransferCode::INVALID_REQUEST;
					return false;
				}

				path.append(dir);
			}
			return true;
		};

		switch (code)
		{			
		case RequestCode::LIST_REGIONS: break; // root is regions dir
		case RequestCode::LIST_LANDMARKS:
			if (!stream_gob(1, "command requires <region> argument"))
				return;
			break;

		case RequestCode::LIST_IMAGES:
			if (!stream_gob(2, "command requiest <region> and <landmark> arguments"))
				return;
			break;
		}

		Request::_log("Getting contents of - ", path.string());

		if (!exists(path))
		{
			process.is_invalid = true;
			process.data = "invalid directory";
			process.transfer_code = TransferCode::INVALID_DIRECTORY;
			return;
		}

		for (auto && item : directory_iterator(path))
			if (item.path().filename().string() != MAIN_PHOTO_NAME ||
				item.path().filename().string() != GPS_NAME)
				process.data += item.path().filename().string() + SEPERATOR;

		if (process.data.empty())
		{
			Request::_log("Empty directory");
			process.transfer_code = TransferCode::EMPTY_DIRECTORY;
		}
		else
		{
			process.is_text = true;
			process.transfer_code = TransferCode::TEXT;
		}
	}

	void get_process(RequestCode code, std::istringstream & stream, Process & process)
	{
		Request::_log("Processing get command");
	}

	void transfer_process(RequestCode code, std::istringstream & stream, Process & process)
	{
		Request::_log("Processing transfer command");
	}
};

#endif	//	_PROJECT_TIME_WALK_REQUEST_HPP_