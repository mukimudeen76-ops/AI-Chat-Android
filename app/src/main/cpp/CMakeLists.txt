# ThinkForge AI - Native JNI Engine
# GGUF inference bridge for Android

cmake_minimum_required(VERSION 3.22)
project("thinkforge_engine")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# JNI bridge only (llama.cpp GGUF engine can be linked separately)
add_library(thinkforge_engine SHARED
    thinkforge_engine.cpp
)

# Android logging
target_link_libraries(thinkforge_engine
    log
    android
)

if(CMAKE_BUILD_TYPE STREQUAL "Release")
    target_compile_options(thinkforge_engine PRIVATE -O3 -DNDEBUG)
endif()