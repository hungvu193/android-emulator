# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

set(EMUDROID_PATH ../../../../core/jni)


find_library( # Defines the name of the path variable that stores the
        # location of the NDK library.
        log-lib

        # Specifies the name of the NDK library that
        # CMake needs to locate.
        log)

include_directories(${EMUDROID_PATH})

include_directories(.)

set(LOCAL_PATH "${CMAKE_CURRENT_SOURCE_DIR}")


set(FS1 "sms_plus/z80.c")
set(FS1 ${FS1} "sms_plus/sms.c")
set(FS1 ${FS1} "sms_plus/pio.c")
set(FS1 ${FS1} "sms_plus/memz80.c")
set(FS1 ${FS1}	"sms_plus/render.c")
set(FS1 ${FS1}	"sms_plus/tms.c")
set(FS1 ${FS1}	"sms_plus/vdp.c")
set(FS1 ${FS1}	"sms_plus/system.c")
set(FS1 ${FS1}	"sms_plus/error.c")

set(FS1 ${FS1}	"sms_plus/fileio.c")
set(FS1 ${FS1}	"sms_plus/state.c")
set(FS1 ${FS1}	"sms_plus/loadrom.c")


set(FS1 ${FS1}	"sms_plus/ioapi.c")
set(FS1 ${FS1}	"sms_plus/unzip.c")






set(FS1 ${FS1}	"sms_plus/sound.c")
set(FS1 ${FS1}	"sms_plus/sn76489.c")
set(FS1 ${FS1}	"sms_plus/emu2413.c")
set(FS1 ${FS1}	"sms_plus/ym2413.c")
set(FS1 ${FS1}	"sms_plus/fmintf.c")
set(FS1 ${FS1}	"sms_plus/stream.c")



set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DPSS_STYLE=1 -DHAVE_ASPRINTF  -DLSB_FIRST -DFRAMESKIP -D_STLP_HAS_WCHAR_T -D_GLIBCXX_USE_WCHAR_T -Wno-write-strings -DANDROID")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Ofast -ffast-math")  # -fno-tree-vectorize ZPOMALUJE
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions -frtti")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-builtin-sin -fno-builtin-cos")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fmerge-all-constants")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-psabi  -Wa,--noexecstack") # obrovske zrychleni
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fsingle-precision-constant")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden -funroll-loops")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DLSB_FIRST=1")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DLSB_FIRST=1")



# SANITIZER **************************
# set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fsanitize=address -fno-omit-frame-pointer")
# SET (CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -fsanitize=address")
####################



set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99")


find_library(
        log-lib
        log)


find_library(m-lib
        m)

find_library(gles-lib
        GLESv1_CM
        )





#set(CMAKE_ANDROID_ARCH_ABI " armeabi-v7a x86_64 arm64-v8a ")


# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
        nostalgia

        SHARED

        # Provides a relative path to your source file(s).
        Sms.cpp ${FS1} ${EMUDROID_PATH}/Emulator.cpp ${EMUDROID_PATH}/Bridge.cpp)


target_link_libraries(nostalgia
        -ljnigraphics
        -landroid
        -lz
        ${log-lib}
        ${m-lib}
        ${gles-lib}
        )

