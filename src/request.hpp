#ifndef		_PROJECT_TIME_WALK_REQUEST_HPP_
#define		_PROJECT_TIME_WALK_REQUEST_HPP_

#include "log.hpp"
#include <string>
#include <sstream>
#include <fstream>
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
static constexpr const char TEXT_NAME[] = "text";
static constexpr const char IMAGE_NAME[] = "image";
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

	bool stream_gob(boost::filesystem::path & path, Process & process,
		std::istringstream & stream, std::size_t args, 
		const std::string & arg_fault = "invalid arguments")
	{
		using namespace boost::filesystem;

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
	}

	void list_process(RequestCode code, std::istringstream & stream, Process & process)
	{
		using namespace boost::filesystem;
		Request::_log("Processing list command");		

		path path{ ROOT_DIR };

		switch (code)
		{			
		case RequestCode::LIST_REGIONS: break; // root is regions dir
		case RequestCode::LIST_LANDMARKS:
			if (!stream_gob(path, process, stream, 1, 
				"command requires <region> argument"))
				return;
			break;

		case RequestCode::LIST_IMAGES:
			if (!stream_gob(path, process, stream, 2, 
				"command requiest <region> and <landmark> arguments"))
				return;
			break;
		}

		Request::_log("Getting contents of - ", path.string());

		if (!exists(path) || !is_directory(path))
		{
			process.is_invalid = true;
			process.data = "invalid directory";
			process.transfer_code = TransferCode::INVALID_DIRECTORY;
			return;
		}

		for (auto && item : directory_iterator(path))
			if (item.path().filename().string() != MAIN_PHOTO_NAME &&
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
		using namespace boost::filesystem;
		Request::_log("Processing get command");

		path path{ ROOT_DIR };

		switch (code)
		{
		case RequestCode::GET_GPS:
			if (!stream_gob(path, process, stream, 2, 
				"command requiest <region> and <landmark> arguments"))
				return;
			path.append(GPS_NAME);
			break;
		case RequestCode::GET_TEXT:
			if (!stream_gob(path, process, stream, 3, 
				"command requiest <region>, <landmark> and <image_name> arguments"))
				return;
			path.append(TEXT_NAME);
			break;
		}

		if (!exists(path) || !is_regular_file(path))
		{
			process.is_invalid = true;
			process.data = "invalid file";
			process.transfer_code = TransferCode::INVALID_FILE;
			return;
		}

		std::ifstream file{ path.string() };
		std::string line;
		process.data = "";
		while (std::getline(file, line))
			process.data.append(line + "\n");
		process.data.pop_back(); // remove last "\n"
		process.is_text = true;
		process.transfer_code = TransferCode::TEXT;
	}

	void transfer_process(RequestCode code, std::istringstream & stream, Process & process)
	{
		using namespace boost::filesystem;
		Request::_log("Processing transfer command");

		path path{ ROOT_DIR };

		switch (code)
		{
		case RequestCode::TRANSFER_MAIN_REGION:
			if (!stream_gob(path, process, stream, 1,
				"command requires <region> argument"))
				return;
			path.append(MAIN_PHOTO_NAME);
			break;
		case RequestCode::TRANSFER_MAIN_LANDMARK:
			if (!stream_gob(path, process, stream, 2,
				"command requires <region> and <landmark> arguments"))
				return;
			path.append(MAIN_PHOTO_NAME);
			break;
		case RequestCode::TRANSFER_IMAGE:
			if (!stream_gob(path, process, stream, 3,
				"command requires <region>, <landmark> and <image_name> arguments"))
				return;
			path.append(IMAGE_NAME);
			break;
		}

		if (!exists(path) || !is_regular_file(path))
		{
			process.is_invalid = true;
			process.data = "invalid file";
			process.transfer_code = TransferCode::INVALID_FILE;
			return;
		}
		
		process.is_file = true;
		process.data = path.string();
		process.transfer_code = TransferCode::IMAGE;
	}
};

#endif	//	_PROJECT_TIME_WALK_REQUEST_HPP_