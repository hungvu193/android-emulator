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

set(FS1 "fceux/ppu.cpp")
set(FS1 ${FS1} "fceux/drawing.cpp")
set(FS1 ${FS1} "fceux/input.cpp")
set(FS1 ${FS1} "fceux/debug.cpp")
set(FS1 ${FS1} "fceux/vsuni.cpp")
set(FS1 ${FS1} "fceux/state.cpp")
set(FS1 ${FS1} "fceux/palette.cpp")
set(FS1 ${FS1} "fceux/ines.cpp")
set(FS1 ${FS1} "fceux/x6502.cpp")
set(FS1 ${FS1} "fceux/file.cpp")
set(FS1 ${FS1} "fceux/video.cpp")
set(FS1 ${FS1} "fceux/oldmovie.cpp")
set(FS1 ${FS1} "fceux/movie.cpp")
set(FS1 ${FS1} "fceux/conddebug.cpp")
set(FS1 ${FS1} "fceux/asm.cpp")
set(FS1 ${FS1} "fceux/sound.cpp")
set(FS1 ${FS1} "fceux/config.cpp")
set(FS1 ${FS1} "fceux/cart.cpp")
set(FS1 ${FS1} "fceux/fds.cpp")
set(FS1 ${FS1} "fceux/fceu.cpp")
set(FS1 ${FS1} "fceux/unif.cpp")
set(FS1 ${FS1} "fceux/wave.cpp")
set(FS1 ${FS1} "fceux/netplay.cpp")
set(FS1 ${FS1} "fceux/nsf.cpp")
set(FS1 ${FS1} "fceux/cheat.cpp")
#
set(FS1 ${FS1} "fceux/drivers/common/nes_ntsc.c")
#
set(FS1 ${FS1} "fceux/filter.cpp")
set(FS1 ${FS1} "fceux/emufile.cpp")
set(FS1 ${FS1} "fceux/boards/15.cpp")
set(FS1 ${FS1} "fceux/boards/ks7013.cpp")
set(FS1 ${FS1} "fceux/boards/235.cpp")
set(FS1 ${FS1} "fceux/boards/112.cpp")
set(FS1 ${FS1} "fceux/boards/n106.cpp")
set(FS1 ${FS1} "fceux/boards/96.cpp")
set(FS1 ${FS1} "fceux/boards/dream.cpp")
set(FS1 ${FS1} "fceux/boards/bandai.cpp")
set(FS1 ${FS1} "fceux/boards/kof97.cpp")
set(FS1 ${FS1} "fceux/boards/vrc2and4.cpp")
set(FS1 ${FS1} "fceux/boards/68.cpp")
set(FS1 ${FS1} "fceux/boards/le05.cpp")
set(FS1 ${FS1} "fceux/boards/vrc7.cpp")
set(FS1 ${FS1} "fceux/boards/222.cpp")
set(FS1 ${FS1} "fceux/boards/176.cpp")
set(FS1 ${FS1} "fceux/boards/117.cpp")
set(FS1 ${FS1} "fceux/boards/43.cpp")
set(FS1 ${FS1} "fceux/boards/vrc1.cpp")
set(FS1 ${FS1} "fceux/boards/t-262.cpp")
set(FS1 ${FS1} "fceux/boards/sa-9602b.cpp")
set(FS1 ${FS1} "fceux/boards/sl1632.cpp")
set(FS1 ${FS1} "fceux/boards/ghostbusters63in1.cpp")
set(FS1 ${FS1} "fceux/boards/t-227-1.cpp")
set(FS1 ${FS1} "fceux/boards/mmc1.cpp")
set(FS1 ${FS1} "fceux/boards/gs-2013.cpp")
set(FS1 ${FS1} "fceux/boards/vrc3.cpp")
set(FS1 ${FS1} "fceux/boards/sheroes.cpp")
set(FS1 ${FS1} "fceux/boards/164.cpp")
set(FS1 ${FS1} "fceux/boards/51.cpp")
set(FS1 ${FS1} "fceux/boards/bonza.cpp")
set(FS1 ${FS1} "fceux/boards/246.cpp")
set(FS1 ${FS1} "fceux/boards/33.cpp")
set(FS1 ${FS1} "fceux/boards/156.cpp")
set(FS1 ${FS1} "fceux/boards/71.cpp")
set(FS1 ${FS1} "fceux/boards/40.cpp")
set(FS1 ${FS1} "fceux/boards/41.cpp")
set(FS1 ${FS1} "fceux/boards/mmc2and4.cpp")
set(FS1 ${FS1} "fceux/boards/dance2000.cpp")
set(FS1 ${FS1} "fceux/boards/ks7057.cpp")
set(FS1 ${FS1} "fceux/boards/225.cpp")
set(FS1 ${FS1} "fceux/boards/cityfighter.cpp")
set(FS1 ${FS1} "fceux/boards/183.cpp")
set(FS1 ${FS1} "fceux/boards/09-034a.cpp")
set(FS1 ${FS1} "fceux/boards/famicombox.cpp")
set(FS1 ${FS1} "fceux/boards/ax5705.cpp")
set(FS1 ${FS1} "fceux/boards/onebus.cpp")
set(FS1 ${FS1} "fceux/boards/151.cpp")
set(FS1 ${FS1} "fceux/boards/116.cpp")
set(FS1 ${FS1} "fceux/boards/addrlatch.cpp")
set(FS1 ${FS1} "fceux/boards/lh53.cpp")
set(FS1 ${FS1} "fceux/boards/411120-c.cpp")
set(FS1 ${FS1} "fceux/boards/vrc6.cpp")
set(FS1 ${FS1} "fceux/boards/65.cpp")
set(FS1 ${FS1} "fceux/boards/bmc70in1.cpp")
set(FS1 ${FS1} "fceux/boards/103.cpp")
set(FS1 ${FS1} "fceux/boards/230.cpp")
set(FS1 ${FS1} "fceux/boards/bb.cpp")
set(FS1 ${FS1} "fceux/boards/67.cpp")
set(FS1 ${FS1} "fceux/boards/175.cpp")
set(FS1 ${FS1} "fceux/boards/77.cpp")
set(FS1 ${FS1} "fceux/boards/121.cpp")
set(FS1 ${FS1} "fceux/boards/50.cpp")
set(FS1 ${FS1} "fceux/boards/fk23c.cpp")
set(FS1 ${FS1} "fceux/boards/gs-2004.cpp")
set(FS1 ${FS1} "fceux/boards/lh32.cpp")
set(FS1 ${FS1} "fceux/boards/bmc42in1r.cpp")
set(FS1 ${FS1} "fceux/boards/99.cpp")
set(FS1 ${FS1} "fceux/boards/189.cpp")
set(FS1 ${FS1} "fceux/boards/bs-5.cpp")
set(FS1 ${FS1} "fceux/boards/79.cpp")
set(FS1 ${FS1} "fceux/boards/36.cpp")
set(FS1 ${FS1} "fceux/boards/bmc64in1nr.cpp")
set(FS1 ${FS1} "fceux/boards/sc-127.cpp")
set(FS1 ${FS1} "fceux/boards/42.cpp")
set(FS1 ${FS1} "fceux/boards/datalatch.cpp")
set(FS1 ${FS1} "fceux/boards/ks7017.cpp")
set(FS1 ${FS1} "fceux/boards/ks7032.cpp")
set(FS1 ${FS1} "fceux/boards/ks7012.cpp")
set(FS1 ${FS1} "fceux/boards/supervision.cpp")
set(FS1 ${FS1} "fceux/boards/232.cpp")
set(FS1 ${FS1} "fceux/boards/108.cpp")
set(FS1 ${FS1} "fceux/boards/transformer.cpp")
set(FS1 ${FS1} "fceux/boards/__dummy_mapper.cpp")
set(FS1 ${FS1} "fceux/boards/186.cpp")
set(FS1 ${FS1} "fceux/boards/8237.cpp")
set(FS1 ${FS1} "fceux/boards/208.cpp")
set(FS1 ${FS1} "fceux/boards/228.cpp")
set(FS1 ${FS1} "fceux/boards/57.cpp")
set(FS1 ${FS1} "fceux/boards/subor.cpp")
set(FS1 ${FS1} "fceux/boards/253.cpp")
set(FS1 ${FS1} "fceux/boards/185.cpp")
set(FS1 ${FS1} "fceux/boards/mmc3.cpp")
set(FS1 ${FS1} "fceux/boards/170.cpp")
set(FS1 ${FS1} "fceux/boards/ffe.cpp")
set(FS1 ${FS1} "fceux/boards/tf-1201.cpp")
set(FS1 ${FS1} "fceux/boards/ks7037.cpp")
set(FS1 ${FS1} "fceux/boards/28.cpp")
set(FS1 ${FS1} "fceux/boards/177.cpp")
set(FS1 ${FS1} "fceux/boards/mmc5.cpp")
set(FS1 ${FS1} "fceux/boards/n625092.cpp")
set(FS1 ${FS1} "fceux/boards/46.cpp")
set(FS1 ${FS1} "fceux/boards/12in1.cpp")
set(FS1 ${FS1} "fceux/boards/malee.cpp")
set(FS1 ${FS1} "fceux/boards/193.cpp")
set(FS1 ${FS1} "fceux/boards/super24.cpp")
set(FS1 ${FS1} "fceux/boards/187.cpp")
set(FS1 ${FS1} "fceux/boards/tengen.cpp")
set(FS1 ${FS1} "fceux/boards/199.cpp")
set(FS1 ${FS1} "fceux/boards/pec-586.cpp")
set(FS1 ${FS1} "fceux/boards/h2288.cpp")
set(FS1 ${FS1} "fceux/boards/88.cpp")
set(FS1 ${FS1} "fceux/boards/konami-qtai.cpp")
set(FS1 ${FS1} "fceux/boards/80.cpp")
set(FS1 ${FS1} "fceux/boards/ac-08.cpp")
set(FS1 ${FS1} "fceux/boards/ks7031.cpp")
set(FS1 ${FS1} "fceux/boards/bmc13in1jy110.cpp")
set(FS1 ${FS1} "fceux/boards/244.cpp")
set(FS1 ${FS1} "fceux/boards/edu2000.cpp")
set(FS1 ${FS1} "fceux/boards/34.cpp")
set(FS1 ${FS1} "fceux/boards/sachen.cpp")
set(FS1 ${FS1} "fceux/boards/18.cpp")
set(FS1 ${FS1} "fceux/boards/ks7030.cpp")
set(FS1 ${FS1} "fceux/boards/106.cpp")
set(FS1 ${FS1} "fceux/boards/01-222.cpp")
set(FS1 ${FS1} "fceux/boards/vrc7p.cpp")
set(FS1 ${FS1} "fceux/boards/168.cpp")
set(FS1 ${FS1} "fceux/boards/178.cpp")
set(FS1 ${FS1} "fceux/boards/82.cpp")
set(FS1 ${FS1} "fceux/boards/62.cpp")
set(FS1 ${FS1} "fceux/boards/69.cpp")
set(FS1 ${FS1} "fceux/boards/252.cpp")
set(FS1 ${FS1} "fceux/boards/3d-block.cpp")
set(FS1 ${FS1} "fceux/boards/91.cpp")
set(FS1 ${FS1} "fceux/boards/234.cpp")
set(FS1 ${FS1} "fceux/boards/yoko.cpp")
set(FS1 ${FS1} "fceux/boards/a9746.cpp")
set(FS1 ${FS1} "fceux/boards/90.cpp")
set(FS1 ${FS1} "fceux/boards/karaoke.cpp")
set(FS1 ${FS1} "fceux/boards/72.cpp")
set(FS1 ${FS1} "fceux/boards/603-5052.cpp")
set(FS1 ${FS1} "fceux/boards/206.cpp")
set(FS1 ${FS1} "fceux/boards/32.cpp")
set(FS1 ${FS1} "fceux/boards/120.cpp")
set(FS1 ${FS1} "fceux/boards/8157.cpp")
set(FS1 ${FS1} "fceux/boards/830118C.cpp")
set(FS1 ${FS1} "fceux/boards/novel.cpp")
set(FS1 ${FS1} "fceux/drivers/common/scale2x.cpp")

set(FS1 ${FS1} "fceux/boards/emu2413.c")

set(FS1 ${FS1} "fceux/drivers/common/args.cpp")
set(FS1 ${FS1} "fceux/drivers/common/hq2x.cpp")
set(FS1 ${FS1} "fceux/drivers/common/scalebit.cpp")
set(FS1 ${FS1} "fceux/drivers/common/configSys.cpp")
set(FS1 ${FS1} "fceux/drivers/common/hq3x.cpp")
set(FS1 ${FS1} "fceux/drivers/common/scale3x.cpp")
set(FS1 ${FS1} "fceux/drivers/common/config.cpp")
set(FS1 ${FS1} "fceux/drivers/common/vidblit.cpp")
set(FS1 ${FS1} "fceux/drivers/common/cheat.cpp")
set(FS1 ${FS1} "fceux/input/bworld.cpp")
set(FS1 ${FS1} "fceux/input/mahjong.cpp")
set(FS1 ${FS1} "fceux/input/suborkb.cpp")
set(FS1 ${FS1} "fceux/input/hypershot.cpp")
set(FS1 ${FS1} "fceux/input/quiz.cpp")
set(FS1 ${FS1} "fceux/input/arkanoid.cpp")
set(FS1 ${FS1} "fceux/input/zapper.cpp")
set(FS1 ${FS1} "fceux/input/powerpad.cpp")
set(FS1 ${FS1} "fceux/input/shadow.cpp")
set(FS1 ${FS1} "fceux/input/mouse.cpp")
set(FS1 ${FS1} "fceux/input/ftrainer.cpp")
set(FS1 ${FS1} "fceux/input/toprider.cpp")
set(FS1 ${FS1} "fceux/input/cursor.cpp")
set(FS1 ${FS1} "fceux/input/oekakids.cpp")
set(FS1 ${FS1} "fceux/input/fkb.cpp")
set(FS1 ${FS1} "fceux/utils/md5.cpp")
set(FS1 ${FS1} "fceux/utils/xstring.cpp")
set(FS1 ${FS1} "fceux/utils/memory.cpp")
set(FS1 ${FS1} "fceux/utils/unzip.cpp")
set(FS1 ${FS1} "fceux/utils/general.cpp")
set(FS1 ${FS1} "fceux/utils/crc32.cpp")
set(FS1 ${FS1} "fceux/utils/endian.cpp")
set(FS1 ${FS1} "fceux/utils/guid.cpp")


set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DPSS_STYLE=1 -DHAVE_ASPRINTF -DLSB_FIRST -DFRAMESKIP -D_STLP_HAS_WCHAR_T -D_GLIBCXX_USE_WCHAR_T -Wno-write-strings -DANDROID")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Oz -ffast-math")  # -fno-tree-vectorize ZPOMALUJE
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions -frtti")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-builtin-sin -fno-builtin-cos")
# LOCAL_CPPFLAGS += -ffunction-sections -fdata-sections # - ZPOMALUJE
# LOCAL_CPPFLAGS += -mfloat-abi=softfp -mfpu=vfp - pro ARM7 automaticky a jine nepodporuji

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-function-sections -fno-data-sections")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fmerge-all-constants")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-stack-protector")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wa,--noexecstack") # obrovske zrychleni # -Wno-psabi chybi v CLANG
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wc++11-extensions -Wc++11-long-long -Wnewline-eof -Wc++11-narrowing -Wvariadic-macros") # -fsingle-precision-constant chybi v CLANG   -pedantic-errors
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden -funroll-loops -Wreserved-user-defined-literal")




# SANITIZER **************************
# set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fsanitize=address -fno-omit-frame-pointer")
# SET (CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -fsanitize=address")
####################



# LOCAL_CPPFLAGS += -flto - silene zpomaleni
# LOCAL_CPPFLAGS += -fomit-frame-pointer # pada


set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++03")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99")

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
        Nes.cpp ${FS1} ${EMUDROID_PATH}/Emulator.cpp ${EMUDROID_PATH}/Bridge.cpp)


target_link_libraries(nostalgia
        -ljnigraphics
        -landroid
        -lz
        ${log-lib}
        ${m-lib}
        ${gles-lib}
        )

