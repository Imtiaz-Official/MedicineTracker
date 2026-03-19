cmake_minimum_required(VERSION 3.22.1)

project("medicinetracker")

add_library(
        medicinetracker
        SHARED
        native-lib.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        medicinetracker
        ${log-lib})
