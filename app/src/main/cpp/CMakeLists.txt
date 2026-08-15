# ThinkForge AI - Native Engine
# Based on DeepSeek V4 Pro 0813 architecture
# Uses llama.cpp for GGUF inference on Android

cmake_minimum_required(VERSION 3.22)
project("thinkforge_engine")

# Enable C++17
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Define source files
set(ENGINE_SOURCES
    thinkforge_engine.cpp
    ${CMAKE_SOURCE_DIR}/../src/ChatEngine.cpp
    ${CMAKE_SOURCE_DIR}/../src/ContextTrim.cpp
    ${CMAKE_SOURCE_DIR}/../src/ModelLoader.cpp
)

# Create library
add_library(thinkforge_engine SHARED ${ENGINE_SOURCES})

# Include headers
target_include_directories(thinkforge_engine PRIVATE
    ${CMAKE_SOURCE_DIR}/../include
    ${CMAKE_SOURCE_DIR}/../../../include
)

# Android logging
target_link_libraries(thinkforge_engine
    log
    android
)

# Optimization flags
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    target_compile_options(thinkforge_engine PRIVATE -O3 -DNDEBUG -march=armv8.2a+dotprod)
endif()