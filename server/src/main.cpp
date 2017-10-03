#include "threads.hpp"
#include "server.hpp"
#include "interface.hpp"

int main(void)
{
	Threads threads;
	Server server{ threads };
	Interface interface{ threads, server };

	server.start();
	interface.start();
	threads.join();
}