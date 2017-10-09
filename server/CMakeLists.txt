#		Setup

CMAKE_MINIMUM_REQUIRED(VERSION 3.0 FATAL_ERROR)

project(static_libs)
project(server_app)
project(access_app)
project(archive_app)

IF (NOT ${SOLARIS})
	SET(SOLARIS FALSE)
ENDIF()

SET(CMAKE_ARCHIVE_OUTPUT_DRIECTORY ${CMAKE_PROJECT_DIR}/lib)
SET(CMAKE_LIBRARY_OUTPUT_DRIECTORY ${CMAKE_PROJECT_DIR}/lib)
SET(CMAKE_RUNTIME_OUTPUT_DRIECTORY ${CMAKE_PROJECT_DIR}/bin)

SET(CMAKE_BUILD_TYPE Release)
SET(CMAKE_CXX_STANDARD 11)
SET(CMAKE_CXX_STANDARD_REQUIRED)

SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} \
	-O3 \
	-fno-rtti \
	-Wall \
	-pedantic \
	-I /mnt/d/projects/code/time_walk_server/server/src/")

IF(SOLARIS)
	SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} \
		-pthreads \
		-lsocket")
ENDIF()

FIND_PACKAGE(Threads REQUIRED)
FIND_PACKAGE(Boost COMPONENTS system thread filesystem REQUIRED)

INCLUDE_DIRECTORIES(${Boost_INCLUDE_DIRS})
LINK_DIRECTORIES(${Boost_LIBRARY_DIRS})

FUNCTION(LINK_BOOST project_name)
	TARGET_LINK_LIBRARIES(${project_name} ${CMAKE_THREAD_LIBS_INIT})
	TARGET_LINK_LIBRARIES(${project_name} ${Boost_LIBRARIES})
ENDFUNCTION(LINK_BOOST)

FUNCTION(LINK_APP project_name)
	TARGET_LINK_LIBRARIES(${project_name} static_libs)
	LINK_BOOST(${project_name})
ENDFUNCTION(LINK_APP)

##		Libraries

FILE(GLOB static_libs_src_files
	"src/connection/*.cpp"
	"src/data/*.cpp")

ADD_LIBRARY(static_libs STATIC ${static_libs_src_files})
LINK_BOOST(static_libs)

##		Server App

FILE(GLOB server_app_src_files
	"src/apps/server/*.cpp")

ADD_EXECUTABLE(server_app ${server_app_src_files})
LINK_APP(server_app)

#		Access App

#FILE(GLOB access_app_src_files
	#"src/apps/access/*.cpp")
#
#ADD_EXECUTABLE(access_app ${access_app_src_files})
#LINK_APP(access_app)