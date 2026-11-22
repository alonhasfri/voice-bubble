# The speech engine for Voice Bubble on Windows: whisper.cpp (MIT licence) + the same small bridge the phone uses,
# as voicebubble_base.dll. Built by CI (.github/workflows/build.yml, "windows" job).
cmake_minimum_required(VERSION 3.22)
project(voicebubble_desktop C CXX)
set(CMAKE_CXX_STANDARD 17)
# No separate Visual C++ runtime to install.
set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded")
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
# Works on any PC from about 2013 on (AVX2), fast on new ones.
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_AVX OFF CACHE BOOL "" FORCE)
set(GGML_AVX2 ON CACHE BOOL "" FORCE)
set(GGML_FMA ON CACHE BOOL "" FORCE)
set(GGML_F16C ON CACHE BOOL "" FORCE)

include(FetchContent)
FetchContent_Declare(whisper
    GIT_REPOSITORY https://github.com/ggml-org/whisper.cpp.git
    GIT_TAG        v1.7.6
    GIT_SHALLOW    TRUE)
FetchContent_MakeAvailable(whisper)

find_package(JNI REQUIRED)
add_library(voicebubble_base SHARED ../../app/src/main/cpp/whisper_jni.cpp)
target_include_directories(voicebubble_base PRIVATE ${JNI_INCLUDE_DIRS})
target_link_libraries(voicebubble_base PRIVATE whisper)
