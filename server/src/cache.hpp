#ifndef		_PROJECT_TIME_WALK_CACHE_HPP_
#define		_PROJECT_TIME_WALK_CACHE_HPP_

#include <map>
#include <unordered_map>
#include <mutex>
#include <algorithm>

class Cache
{
public:

	using cache_t = std::unordered_map<std::string, std::string>;

private:

	static cache_t & get_cache(void)
	{
		static cache_t cache;
		return cache;
	}

	static std::mutex & get_mutex(void)
	{
		static std::mutex mutex;
		return mutex;
	}

public:

	template <typename Function_>
	static auto perform(Function_ && func)
		-> decltype(func(get_cache()))
	{
		std::lock_guard<std::mutex> lock{ get_mutex() };
		return func(get_cache());
	}
};

class LocalCache
{
public:
	
	// Map<region, Map<landmark, count>>
	using cache_t = std::unordered_map<std::string,
		std::unordered_map<std::string, std::size_t>>;

private:
	
	std::mutex _mutex;
	cache_t _cache;

public:

	template <typename Function_>
	auto perform(Function_ && func)
		-> decltype(func(_cache))
	{
		std::lock_guard<std::mutex> lock{ LocalCache::_mutex };
		return func(LocalCache::_cache);
	}

	std::multimap<size_t, std::string> get_popular(const std::string & region)
	{
		std::lock_guard<std::mutex> lock{ LocalCache::_mutex };
		std::multimap<size_t, std::string> popular;

		std::transform(
			LocalCache::_cache[region].begin(),
			LocalCache::_cache[region].end(),
			std::inserter(popular, popular.begin()),
			[](const std::pair<std::string, std::size_t> & p)
		{
			return std::pair<std::size_t, std::string>{ p.second, p.first };
		});

		return popular;
	}
};

#endif	//	_PROJECT_TIME_WALK_CACHE_HPP_