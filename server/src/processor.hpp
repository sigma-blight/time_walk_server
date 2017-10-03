#ifndef		_PROJECT_TIME_WALK_PROCESSOR_HPP_
#define		_PROJECT_TIME_WALK_PROCESSOR_HPP_

#include <string>

class Processor
{
public:

	std::string operator() (const std::string & str) { return str; } // TODO
};

#endif	//	_PROJECT_TIME_WALK_PROCESSOR_HPP_