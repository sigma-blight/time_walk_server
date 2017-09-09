#ifndef		_PROJECT_TIME_WALK_REQUEST_HPP_
#define		_PROJECT_TIME_WALK_REQUEST_HPP_

#include "log.hpp"
#include <string>
#include <sstream>
#include <fstream>
#include <boost/filesystem.hpp>

enum class TransferCode
{
	SUCCESS					= 0,
	EMPTY_REQUEST			= 1,
	EMPTY_DIRECTORY			= 2,
	EMPTY_FILE				= 3,
	INVALID_REQUEST			= 10,
	INVALID_REQUEST_CODE	= 11,
	INVALID_ARGUMENT_COUNT	= 12,
	INVALID_ARGUMENTS		= 13,
	INVALID_DIRECTORY		= 14,
	INVALID_FILE			= 15
};

enum class RequestCode
{
	LIST_REGIONS				= 0,
	LIST_LANDMARKS				= 1,
	LIST_IMAGES					= 2,
	GET_REGION_GPS				= 10,
	GET_LANDMARK_GPS			= 11,
	GET_TEXT					= 20,
	GET_IMAGE					= 21,
	GET_POSTCARD_IMAGE_LANDMARK	= 30,
	GET_POSTCARD_IMAGE_REGION	= 31
};

struct Process
{
	bool			is_valid = true;
	bool			write_size_required = false;
	std::string		data;
	TransferCode	transfer_code = TransferCode::SUCCESS;
};

static constexpr const char ROOT_DIR[] = "../../archives/time_walk/";
static constexpr const char SEPERATOR = '|';
static constexpr const char GPS_FILENAME[] = "gps";
static constexpr const char POSTCARD_IMAGE_FILENAME[] = "postcard_image";
static constexpr const char TEXT_FILENAME[] = "text";
static constexpr const char IMAGE_FILENAME[] = "image";

class Request
{
	Log & _log;

public:

	Request(Log & log) :
		_log{ log }
	{}

	Process process(const std::string & data)
	{
		Request::_log("Process Request - ", data);
		Process result;

		if (data.empty())
		{
			result.data = "empty request";
			result.is_valid = false;
			result.transfer_code = TransferCode::EMPTY_REQUEST;
		}
		else
		{
			std::istringstream stream{ data };
			std::size_t request_code;
			stream >> request_code;

			if (stream.fail())
			{
				result.data = "invalid request";
				result.is_valid = false;
				result.transfer_code = TransferCode::INVALID_REQUEST;
			}
			else
			{
				auto code = static_cast<RequestCode>(request_code);
				switch (code)
				{
				case RequestCode::LIST_REGIONS:
				case RequestCode::LIST_LANDMARKS:
				case RequestCode::LIST_IMAGES:
					list_request(result, stream, code);
					break;

				case RequestCode::GET_REGION_GPS:
				case RequestCode::GET_LANDMARK_GPS:
				case RequestCode::GET_TEXT:
				case RequestCode::GET_IMAGE:
				case RequestCode::GET_POSTCARD_IMAGE_LANDMARK:
				case RequestCode::GET_POSTCARD_IMAGE_REGION:
					get_request(result, stream, code);
					break;

				default:
					result.data = "invalid request code";
					result.is_valid = false;
					result.transfer_code = TransferCode::INVALID_REQUEST_CODE;
					break;
				}
			}
		}

		return result;
	}

private:

	void list_request(Process & process, std::istringstream & stream, 
		const RequestCode & code)
	{
		using namespace boost::filesystem;
		path dir{ ROOT_DIR };

		switch (code)
		{
		case RequestCode::LIST_REGIONS: break;
		case RequestCode::LIST_LANDMARKS:
			// expects <region>
			append_path(dir, process, stream, 1);
			break;
		case RequestCode::LIST_IMAGES:
			// expects <region> <landmark>
			append_path(dir, process, stream, 2);
			break;
		}

		// exit if append_path detects error
		if (!process.is_valid) return;

		Request::_log("Listing contents of - ", dir.string());

		// send error if invalid directory
		if (!exists(dir) || !is_directory(dir))
		{
			process.is_valid = false;
			process.data = "invalid directory";
			process.transfer_code = TransferCode::INVALID_DIRECTORY;
			return;
		}

		// get all filenames in directory
		for (auto && item : directory_iterator(dir))
			if (item.path().filename().string() != GPS_FILENAME &&
				item.path().filename().string() != POSTCARD_IMAGE_FILENAME)
			{
				process.data.append(item.path().filename().string());
				process.data.push_back(SEPERATOR);
			}

		// error if nothing in directory
		if (process.data.empty())
		{
			process.data = "empty directory";
			process.is_valid = false;
			process.transfer_code = TransferCode::EMPTY_DIRECTORY;
		}
		else
			process.data.pop_back(); // remove last seperator
	}

	void get_request(Process & process, std::istringstream & stream,
		const RequestCode & code)
	{
		using namespace boost::filesystem;
		path file{ ROOT_DIR };

		switch (code)
		{
		case RequestCode::GET_REGION_GPS:
			// expects <region>
			append_path(file, process, stream, 1);
			file.append(GPS_FILENAME);
			break;
		case RequestCode::GET_LANDMARK_GPS:
			// expects <region> <landmark>
			append_path(file, process, stream, 2);
			file.append(GPS_FILENAME);
			break;
		case RequestCode::GET_TEXT:
			// expects <region> <landmark> <image_name>
			append_path(file, process, stream, 3);
			file.append(TEXT_FILENAME);
			process.write_size_required = true;
			break;
		case RequestCode::GET_IMAGE:
			// expects <region> <landmark> <image_name> <size>
			append_path(file, process, stream, 3); // first 3
			file.append(IMAGE_FILENAME);
			append_path(file, process, stream, 1); // size
			break;
		case RequestCode::GET_POSTCARD_IMAGE_REGION:
			// expects <region>
			append_path(file, process, stream, 1);
			file.append(POSTCARD_IMAGE_FILENAME);
			break;
		case RequestCode::GET_POSTCARD_IMAGE_LANDMARK:
			// expects <region> <landmark>
			append_path(file, process, stream, 2);
			file.append(POSTCARD_IMAGE_FILENAME);
			break;
		}

		// exit if append_path detects error
		if (!process.is_valid) return;

		Request::_log("Getting contents of - ", file.string());

		if (!exists(file) || !is_regular_file(file))
		{
			process.data = "invalid file";
			process.is_valid = false;
			process.transfer_code = TransferCode::INVALID_FILE;
			return;
		}

		std::ifstream file_stream{ file.string() };
		std::string line;
		while (std::getline(file_stream, line))
			process.data.append(line + "\n");

		if (process.data.empty())
		{
			process.data = "empty file";
			process.is_valid = false;
			process.transfer_code = TransferCode::EMPTY_FILE;
		}
		else
			process.data.pop_back(); // remove last newline
	}

	void append_path(boost::filesystem::path & path, Process & process,
		std::istringstream & stream, std::size_t args)
	{
		using namespace boost::filesystem;
	
		for (std::size_t i = 0; i != args; ++i)
		{
			// expected more arguments
			if (stream.eof())
			{
				process.is_valid = false;
				process.data = "invalid arguments";
				process.transfer_code = TransferCode::INVALID_ARGUMENT_COUNT;
				return;
			}
	
			std::string dir;
			stream >> dir;

			// invalid string
			if (stream.fail())
			{
				process.is_valid = false;
				process.data = "invalid request";
				process.transfer_code = TransferCode::INVALID_ARGUMENTS;
				return;
			}
	
			path.append(dir);
		}
	}
};

#endif	//	_PROJECT_TIME_WALK_REQUEST_HPP_