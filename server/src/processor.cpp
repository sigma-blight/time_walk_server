#include "processor.hpp"
#include "cache.hpp"
#include "log.hpp"
#include <string>
#include <sstream>
#include <fstream>
#include <boost/filesystem.hpp>

namespace fs = boost::filesystem;

enum class Command
{
	LIST_REGIONS,
	LIST_LANDMARKS,
	LIST_IMAGES,
	LIST_POPULAR,

	GET_THEMES,
	GET_REGION_IMG,
	GET_LANDMARK_IMG,
	GET_IMAGE,
	GET_GPS,
	GET_TEXT
};

enum class Response
{
	VALID_REQUEST,
	INVALID_REQUEST,
	
	INVALID_DIR,
	INVALID_FILE
};

static constexpr const char INVALID_REQUEST[] = "invalid request";
static constexpr const char WEB_ROOT[] = "http://deco3801-chronos.uqcloud.net/deps/time_walk_images/";
static constexpr const char ARCHIVES_ROOT[] = "../../archives/project_time_walk/";
static constexpr const char FN_MAIN_PHOTO[] = "main_photo";
static constexpr const char FN_GPS[] = "gps";
static constexpr const char FN_TEXT[] = "text";
static constexpr const char FN_THEMES[] = "themes";
static constexpr const char SEPERATOR = '|';

template <typename Return_>
bool get_from_stream(Return_ &, std::istringstream &);
void get_response(std::string &, const std::string &, Log &);
void invalid_request(std::string &, const Response &);
void get_list(std::string &, const fs::path &, Log &);
void get_file(std::string &, const fs::path &, Log &);
void get_image(std::string &, const fs::path &, Log &);
static LocalCache & get_count_cache(void);

std::string process(const std::string & request, Log & log)
{
	if (Cache::perform([&request](const Cache::cache_t & cache)
		{
			return cache.find(request) != cache.end();
		}) &&
		request.size() != 0 && // ensure next check won't seg fault
		request.front() != '3')// don't cache the popular command
	{
		return Cache::perform([&request](Cache::cache_t & cache)
		{
			return cache[request];
		});
	}

	std::string response;
	get_response(response, request, log);

	Cache::perform([&request, &response](Cache::cache_t & cache)
	{
		// check for double writing
		if (cache.find(request) != cache.end())
			cache[request] = response;
	});

	return response;
}

void get_response(std::string & response, const std::string & request, Log & log)
{
	std::istringstream stream{ request };
	std::size_t command;

	if (get_from_stream(command, stream))
	{
		log(Log::ERROR, "Invalid Command - ", command);
		invalid_request(response, Response::INVALID_REQUEST);
		return;
	}

	switch (static_cast<Command>(command))
	{
	case Command::LIST_REGIONS:
		log(Log::INFO, "Listing Regions");
		get_list(response, ARCHIVES_ROOT, log);
		break;
	case Command::LIST_LANDMARKS:
	{
		std::string region;
		if (get_from_stream(region, stream))
		{
			log(Log::ERROR, "Invalid Args - ", region);
			invalid_request(response, Response::INVALID_REQUEST);
		}
		else
		{
			fs::path path{ ARCHIVES_ROOT };
			path.append(region);
			log(Log::INFO, "Listing Landmarks of ", region);
			get_list(response, path, log);
		}
		break;
	}
	case Command::LIST_IMAGES:
	{
		std::string region;
		std::string landmark;
		if (get_from_stream(region, stream) ||
			get_from_stream(landmark, stream))
		{
			log(Log::ERROR, "Invalid Args - ", region, " or ", landmark);
			invalid_request(response, Response::INVALID_REQUEST);
		}
		else
		{
			fs::path path{ ARCHIVES_ROOT };
			path.append(region);
			path.append(landmark);
			log(Log::INFO, "Listing Images of ", landmark, " of ", region);
			get_list(response, path, log);

			// update cache count
			get_count_cache().perform([&region, &landmark](LocalCache::cache_t & cache)
			{
				++cache[region][landmark];
			});
		}
		break;
	}
	case Command::LIST_POPULAR:
	{
		std::string region;
		if (get_from_stream(region, stream))
		{
			log(Log::ERROR, "Invalid Args - ", region);
			invalid_request(response, Response::INVALID_REQUEST);
		}

		log(Log::INFO, "Listing Popular Landmarks of ", region);

		auto popular = get_count_cache().get_popular(region);
		for (auto it = popular.begin(); it != popular.end(); ++it)
		{
			it->second.push_back(SEPERATOR);
			response = it->second.append(response);
		}
	}
		break;
	case Command::GET_THEMES:
	{
		std::string region;
		if (get_from_stream(region, stream))
		{
			log(Log::ERROR, "Invalid Args - ", region);
			invalid_request(response, Response::INVALID_REQUEST);
		}
		else
		{
			fs::path path{ ARCHIVES_ROOT };
			path.append(region);
			path.append(FN_THEMES);
			log(Log::INFO, "Getting Themes of ", region);
			get_file(response, path, log);
		}
		break;
	}
	case Command::GET_GPS:
	{
		std::string region;
		std::string landmark;
		if (get_from_stream(region, stream) ||
			get_from_stream(landmark, stream))
		{
			log(Log::ERROR, "Invalid Args - ", region, " or ", landmark);
			invalid_request(response, Response::INVALID_REQUEST);
		}
		else
		{
			fs::path path{ ARCHIVES_ROOT };
			path.append(region);
			path.append(landmark);
			path.append(FN_GPS);
			log(Log::INFO, "Getting GPS of ", landmark, " of ", region);
			get_file(response, path, log);
		}
		break;
	}
	case Command::GET_TEXT:
	{
		std::string region;
		std::string landmark;
		std::string image;
		if (get_from_stream(region, stream) ||
			get_from_stream(landmark, stream) ||
			get_from_stream(image, stream))
		{
			log(Log::ERROR, "Invalid Args - ", region, 
				" or ", landmark, " or ", image);
			invalid_request(response, Response::INVALID_REQUEST);
		}
		else
		{
			fs::path path{ ARCHIVES_ROOT };
			path.append(region);
			path.append(landmark);
			path.append(image);
			path.append(FN_TEXT);
			log(Log::INFO, "Getting Text of ", image, " of ", 
				landmark, " of ", region);
			get_file(response, path, log);
		}
		break;
	}
	case Command::GET_LANDMARK_IMG:
	{
		std::string region;
		std::string landmark;
		if (get_from_stream(region, stream) ||
			get_from_stream(landmark, stream))
		{
			log(Log::ERROR, "Invalid Args - ", region, " or ", landmark);
			invalid_request(response, Response::INVALID_REQUEST);
		}
		else
		{
			fs::path path{ ARCHIVES_ROOT };
			path.append(region);
			path.append(landmark);
			log(Log::INFO, "Getting Main Photo of ", landmark, " of ", region);
			get_image(response, path, log);
		}
		break;
	}
	case Command::GET_IMAGE:
	{
		std::string region;
		std::string landmark;
		std::string image;
		if (get_from_stream(region, stream) ||
			get_from_stream(landmark, stream) ||
			get_from_stream(image, stream))
		{
			log(Log::ERROR, "Invalid Args - ", region, 
				" or ", landmark, " or ", image);
			invalid_request(response, Response::INVALID_REQUEST);
		}
		else
		{
			// don't start with ARCHIVES_ROOT
			fs::path path{ region };
			path.append(landmark);
			path.append(image);
			log(Log::INFO, "Getting Image of ", image, " of ", 
				landmark, " of ", region);
			get_image(response, path, log);
		}
		break;
	}
	default:
		invalid_request(response, Response::INVALID_REQUEST);
		break;
	}
}

template <typename Return_>
bool get_from_stream(Return_ & result, std::istringstream & stream)
{
	if (stream.eof()) return true;
	stream >> result;	
	return stream.fail();
}

void invalid_request(std::string & response, const Response & cmd)
{
	response.append(std::to_string(static_cast<size_t>(cmd)));
	response.push_back(SEPERATOR);
	response.append(INVALID_REQUEST);
}

void get_list(std::string & response, const fs::path & path, Log & log)
{
	if (!fs::exists(path) || !fs::is_directory(path))
	{
		log(Log::ERROR, "Invalid Directory");
		invalid_request(response, Response::INVALID_DIR);
	}
	else
	{
		response.append(std::to_string(
			static_cast<size_t>(Response::VALID_REQUEST)));
		response.push_back(SEPERATOR);

		for (auto && item : fs::directory_iterator(path))
			if (item.path().filename().string() != FN_MAIN_PHOTO ||
				item.path().filename().string() != FN_GPS ||
				item.path().filename().string() != FN_TEXT ||
				item.path().filename().string() != FN_THEMES)
			{
				response.append(item.path().filename().string());
				response.push_back(SEPERATOR);
			}
	}
}

void get_file(std::string & response, const fs::path & path, Log & log)
{
	std::fstream file{ path.string() };

	if (!file)
	{
		log(Log::ERROR, "Invalid File");
		invalid_request(response, Response::INVALID_FILE);
	}
	else
	{
		response.append(std::to_string(
			static_cast<size_t>(Response::VALID_REQUEST)));
		response.push_back(SEPERATOR);

		std::string line;
		while (std::getline(file, line))
			response.append(line);
	}
}

void get_image(std::string & response, const fs::path & path, Log & log)
{
	fs::path validate{ ARCHIVES_ROOT };
	validate.append(path.string());

	if (!fs::exists(validate) || !fs::is_regular_file(validate))
	{
		log(Log::ERROR, "Invalid Image");
		invalid_request(response, Response::INVALID_FILE);
	}
	else
	{
		response = WEB_ROOT;
		response.append(path.string());
	}
}

static LocalCache & get_count_cache(void)
{
	static LocalCache cache;
	return cache;
}