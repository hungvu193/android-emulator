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
include_directories(./libgambatte/src)
include_directories(./libgambatte/include)
include_directories(./libgambatte/file)
include_directories(./libgambatte/mem)
include_directories(./libgambatte/sound)
include_directories(./libgambatte/video)
include_directories(./common/resample)
include_directories(./common/resample/src)
include_directories(./common)

set(LOCAL_PATH "${CMAKE_CURRENT_SOURCE_DIR}")

set(FS1 ${FS1} "libgambatte/src/file/unzip/ioapi.c")
set(FS1 ${FS1} "libgambatte/src/file/unzip/unzip.c")
set(FS1 ${FS1} "libgambatte/src/file/file.cpp")
set(FS1 ${FS1} "libgambatte/src/sound/channel1.cpp")
set(FS1 ${FS1} "libgambatte/src/sound/channel2.cpp")
set(FS1 ${FS1} "libgambatte/src/sound/channel3.cpp")
set(FS1 ${FS1} "libgambatte/src/sound/channel4.cpp")
set(FS1 ${FS1} "libgambatte/src/sound/duty_unit.cpp")
set(FS1 ${FS1} "libgambatte/src/sound/envelope_unit.cpp")
set(FS1 ${FS1} "libgambatte/src/sound/length_counter.cpp")
set(FS1 ${FS1} "libgambatte/src/video/lyc_irq.cpp")
set(FS1 ${FS1} "libgambatte/src/video/ly_counter.cpp")
set(FS1 ${FS1} "libgambatte/src/video/next_m0_time.cpp")
set(FS1 ${FS1} "libgambatte/src/video/ppu.cpp")
set(FS1 ${FS1} "libgambatte/src/video/sprite_mapper.cpp")
set(FS1 ${FS1} "libgambatte/src/mem/cartridge.cpp")
set(FS1 ${FS1} "libgambatte/src/mem/memptrs.cpp")
set(FS1 ${FS1} "libgambatte/src/mem/rtc.cpp")
set(FS1 ${FS1} "libgambatte/src/bitmap_font.cpp")
set(FS1 ${FS1} "libgambatte/src/cpu.cpp")
set(FS1 ${FS1} "libgambatte/src/gambatte.cpp")
set(FS1 ${FS1} "libgambatte/src/initstate.cpp")
set(FS1 ${FS1} "libgambatte/src/interrupter.cpp")
set(FS1 ${FS1} "libgambatte/src/interruptrequester.cpp")
set(FS1 ${FS1} "libgambatte/src/memory.cpp")
set(FS1 ${FS1} "libgambatte/src/sound.cpp")
set(FS1 ${FS1} "libgambatte/src/statesaver.cpp")
set(FS1 ${FS1} "libgambatte/src/state_osd_elements.cpp")
set(FS1 ${FS1} "libgambatte/src/tima.cpp")
set(FS1 ${FS1} "libgambatte/src/video.cpp")
set(FS1 ${FS1} "common/resample/src/chainresampler.cpp")
set(FS1 ${FS1} "common/resample/src/i0.cpp")
set(FS1 ${FS1} "common/resample/src/makesinckernel.cpp")
set(FS1 ${FS1} "common/resample/src/resamplerinfo.cpp")
set(FS1 ${FS1} "common/resample/src/u48div.cpp")



set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-exceptions -fno-rtti -DINLINE=inline -DHAVE_STDINT_H -DINT_LEAST_32 -DHAVE_INTTYPES_H -DLSB_FIRST")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Ofast -ffast-math ")  # -fno-tree-vectorize ZPOMALUJE
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions -frtti")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}  -fno-builtin-sin -fno-builtin-cos ")
# LOCAL_CPPFLAGS += -ffunction-sections -fdata-sections # - ZPOMALUJE
# LOCAL_CPPFLAGS += -mfloat-abi=softfp -mfpu=vfp - pro ARM7 automaticky a jine nepodporuji

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fmerge-all-constants")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wa,--noexecstack ")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fsingle-precision-constant")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden -funroll-loops ")



# SANITIZER **************************
# set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fsanitize=address -fno-omit-frame-pointer")
# SET (CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -fsanitize=address")
####################



# LOCAL_CPPFLAGS += -flto - silene zpomaleni
# LOCAL_CPPFLAGS += -fomit-frame-pointer # pada


set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++03")


#set(CMAKE_ANDROID_STL_TYPE "c++_shared")


# -lz -llog -ljnigraphics -lm -lGLESv1_CM -flto



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
        Gbc.cpp ${FS1} ${EMUDROID_PATH}/Emulator.cpp ${EMUDROID_PATH}/Bridge.cpp)


target_link_libraries(nostalgia
        -ljnigraphics
        -landroid
        -lz
        ${log-lib}
        ${m-lib}
        ${gles-lib}
        )

