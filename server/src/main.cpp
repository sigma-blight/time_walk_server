#include "threads.hpp"
#include "server.hpp"

int main(void)
{
	Threads threads;
	Server server{ threads };

	server.start();
	threads.join();
}
