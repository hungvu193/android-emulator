LOCAL_PATH := $(call my-dir)

EMUDROID_PATH := $(LOCAL_PATH)/../../Framework/jni

include $(CLEAR_VARS)

FCEUX_SRC += fceux/cart.cpp fceux/cheat.cpp fceux/emufile.cpp fceux/fceu.cpp fceux/file.cpp fceux/filter.cpp
FCEUX_SRC += fceux/ines.cpp fceux/input.cpp fceux/palette.cpp fceux/ppu.cpp fceux/sound.cpp fceux/state.cpp fceux/unif.cpp fceux/video.cpp fceux/vsuni.cpp
FCEUX_SRC += fceux/x6502.cpp fceux/movie.cpp fceux/fds.cpp
FCEUX_SRC += fceux/input/mouse.cpp fceux/input/oekakids.cpp fceux/input/powerpad.cpp fceux/input/quiz.cpp
FCEUX_SRC +=  fceux/input/shadow.cpp fceux/input/suborkb.cpp fceux/input/toprider.cpp fceux/input/zapper.cpp

FCEUX_SRC += $(wildcard FS1 += $(LOCAL_PATH)/fceux/boards/*.cpp)


FCEUX_SRC += fceux/input/arkanoid.cpp fceux/input/bworld.cpp fceux/input/cursor.cpp fceux/input/fkb.cpp fceux/input/ftrainer.cpp fceux/input/hypershot.cpp fceux/input/mahjong.cpp
FCEUX_SRC += fceux/utils/endian.cpp fceux/utils/memory.cpp fceux/utils/crc32.cpp fceux/utils/general.cpp
FCEUX_SRC += fceux/utils/guid.cpp fceux/utils/xstring.cpp fceux/utils/md5.cpp

#F := $(wildcard FS1 += $(LOCAL_PATH)/fceux/*.cpp)
#F += $(wildcard FS1 += $(LOCAL_PATH)/fceux/boards/*.cpp)
#F += $(wildcard FS1 += $(LOCAL_PATH)/fceux/drivers/common/*.cpp)
#F += $(wildcard FS1 += $(LOCAL_PATH)/fceux/input/*.cpp)
#F += $(wildcard FS1 += $(LOCAL_PATH)/fceux/utils/*.cpp)


FS1 += $(LOCAL_PATH)/fceux/ppu.cpp
FS1 += $(LOCAL_PATH)/fceux/drawing.cpp
FS1 += $(LOCAL_PATH)/fceux/input.cpp
FS1 += $(LOCAL_PATH)/fceux/debug.cpp
FS1 += $(LOCAL_PATH)/fceux/vsuni.cpp
FS1 += $(LOCAL_PATH)/fceux/state.cpp
FS1 += $(LOCAL_PATH)/fceux/palette.cpp
FS1 += $(LOCAL_PATH)/fceux/ines.cpp
FS1 += $(LOCAL_PATH)/fceux/x6502.cpp
FS1 += $(LOCAL_PATH)/fceux/file.cpp
FS1 += $(LOCAL_PATH)/fceux/video.cpp
FS1 += $(LOCAL_PATH)/fceux/oldmovie.cpp
FS1 += $(LOCAL_PATH)/fceux/movie.cpp
FS1 += $(LOCAL_PATH)/fceux/conddebug.cpp
FS1 += $(LOCAL_PATH)/fceux/asm.cpp
FS1 += $(LOCAL_PATH)/fceux/sound.cpp
FS1 += $(LOCAL_PATH)/fceux/config.cpp
FS1 += $(LOCAL_PATH)/fceux/cart.cpp
FS1 += $(LOCAL_PATH)/fceux/fds.cpp
FS1 += $(LOCAL_PATH)/fceux/fceu.cpp
FS1 += $(LOCAL_PATH)/fceux/unif.cpp
FS1 += $(LOCAL_PATH)/fceux/wave.cpp
FS1 += $(LOCAL_PATH)/fceux/netplay.cpp
FS1 += $(LOCAL_PATH)/fceux/nsf.cpp
FS1 += $(LOCAL_PATH)/fceux/cheat.cpp

FS1 += $(LOCAL_PATH)/fceux/drivers/common/nes_ntsc.c

FS1 += $(LOCAL_PATH)/fceux/filter.cpp
FS1 += $(LOCAL_PATH)/fceux/emufile.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/15.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ks7013.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/235.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/112.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/n106.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/96.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/dream.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/bandai.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/kof97.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/vrc2and4.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/68.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/le05.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/vrc7.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/222.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/176.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/117.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/43.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/vrc1.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/t-262.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/sa-9602b.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/sl1632.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ghostbusters63in1.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/t-227-1.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/mmc1.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/gs-2013.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/vrc3.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/sheroes.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/164.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/51.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/bonza.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/246.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/33.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/156.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/71.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/40.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/41.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/mmc2and4.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/dance2000.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ks7057.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/225.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/cityfighter.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/183.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/09-034a.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/famicombox.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ax5705.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/onebus.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/151.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/116.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/addrlatch.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/lh53.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/411120-c.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/vrc6.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/65.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/bmc70in1.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/103.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/230.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/bb.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/67.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/175.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/77.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/121.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/50.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/fk23c.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/gs-2004.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/lh32.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/bmc42in1r.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/99.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/189.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/bs-5.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/79.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/36.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/bmc64in1nr.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/sc-127.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/42.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/datalatch.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ks7017.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ks7032.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ks7012.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/supervision.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/232.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/108.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/transformer.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/__dummy_mapper.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/186.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/8237.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/208.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/228.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/57.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/subor.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/253.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/185.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/mmc3.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/170.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ffe.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/tf-1201.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ks7037.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/28.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/177.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/mmc5.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/n625092.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/46.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/12in1.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/malee.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/193.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/super24.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/187.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/tengen.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/199.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/pec-586.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/h2288.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/88.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/konami-qtai.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/80.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ac-08.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ks7031.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/bmc13in1jy110.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/244.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/edu2000.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/34.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/sachen.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/18.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/ks7030.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/106.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/01-222.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/vrc7p.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/168.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/178.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/82.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/62.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/69.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/252.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/3d-block.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/91.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/234.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/yoko.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/a9746.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/90.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/karaoke.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/72.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/603-5052.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/206.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/32.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/120.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/8157.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/830118C.cpp
FS1 += $(LOCAL_PATH)/fceux/boards/novel.cpp
FS1 += $(LOCAL_PATH)/fceux/drivers/common/scale2x.cpp

FS1 += $(LOCAL_PATH)/fceux/boards/emu2413.c

FS1 += $(LOCAL_PATH)/fceux/drivers/common/args.cpp
FS1 += $(LOCAL_PATH)/fceux/drivers/common/hq2x.cpp
FS1 += $(LOCAL_PATH)/fceux/drivers/common/scalebit.cpp
FS1 += $(LOCAL_PATH)/fceux/drivers/common/configSys.cpp
FS1 += $(LOCAL_PATH)/fceux/drivers/common/hq3x.cpp
FS1 += $(LOCAL_PATH)/fceux/drivers/common/scale3x.cpp
FS1 += $(LOCAL_PATH)/fceux/drivers/common/config.cpp
FS1 += $(LOCAL_PATH)/fceux/drivers/common/vidblit.cpp
FS1 += $(LOCAL_PATH)/fceux/drivers/common/cheat.cpp
FS1 += $(LOCAL_PATH)/fceux/input/bworld.cpp
FS1 += $(LOCAL_PATH)/fceux/input/mahjong.cpp
FS1 += $(LOCAL_PATH)/fceux/input/suborkb.cpp
FS1 += $(LOCAL_PATH)/fceux/input/hypershot.cpp
FS1 += $(LOCAL_PATH)/fceux/input/quiz.cpp
FS1 += $(LOCAL_PATH)/fceux/input/arkanoid.cpp
FS1 += $(LOCAL_PATH)/fceux/input/zapper.cpp
FS1 += $(LOCAL_PATH)/fceux/input/powerpad.cpp
FS1 += $(LOCAL_PATH)/fceux/input/shadow.cpp
FS1 += $(LOCAL_PATH)/fceux/input/mouse.cpp
FS1 += $(LOCAL_PATH)/fceux/input/ftrainer.cpp
FS1 += $(LOCAL_PATH)/fceux/input/toprider.cpp
FS1 += $(LOCAL_PATH)/fceux/input/cursor.cpp
FS1 += $(LOCAL_PATH)/fceux/input/oekakids.cpp
FS1 += $(LOCAL_PATH)/fceux/input/fkb.cpp
FS1 += $(LOCAL_PATH)/fceux/utils/md5.cpp
FS1 += $(LOCAL_PATH)/fceux/utils/xstring.cpp
FS1 += $(LOCAL_PATH)/fceux/utils/memory.cpp
FS1 += $(LOCAL_PATH)/fceux/utils/unzip.cpp
FS1 += $(LOCAL_PATH)/fceux/utils/general.cpp
FS1 += $(LOCAL_PATH)/fceux/utils/crc32.cpp
FS1 += $(LOCAL_PATH)/fceux/utils/endian.cpp
FS1 += $(LOCAL_PATH)/fceux/utils/guid.cpp


FILES :=  Nes.cpp $(FS1) $(EMUDROID_PATH)/Emulator.cpp $(EMUDROID_PATH)/Bridge.cpp
FILES := $(FILES:$(LOCAL_PATH)/%=%)
FILES := $(FILES:fceux/lua-engine.cpp= )

include $(CLEAR_VARS)
LOCAL_ARM_MODE := arm
LOCAL_SRC_FILES :=  $(FILES) 
LOCAL_C_INCLUDES := $(EMUDROID_PATH)
LOCAL_MODULE    := nostalgia
LOCAL_CPPFLAGS += -DPSS_STYLE=1 -DHAVE_ASPRINTF  -DLSB_FIRST -DFRAMESKIP -D_STLP_HAS_WCHAR_T -D_GLIBCXX_USE_WCHAR_T -Wno-write-strings -DANDROID
LOCAL_CPPFLAGS +=  -Ofast -ffast-math  # -fno-tree-vectorize ZPOMALUJE
LOCAL_CPPFLAGS += -fexceptions -frtti
LOCAL_CPPFLAGS += -fno-builtin-sin -fno-builtin-cos 
# LOCAL_CPPFLAGS += -ffunction-sections -fdata-sections # - ZPOMALUJE
# LOCAL_CPPFLAGS += -mfloat-abi=softfp -mfpu=vfp - pro ARM7 automaticky a jine nepodporuji
LOCAL_CPPFLAGS += -fmerge-all-constants
LOCAL_CPPFLAGS += -Wno-psabi  -Wa,--noexecstack # obrovske zrychleni
LOCAL_CPPFLAGS += -fsingle-precision-constant
LOCAL_CPPFLAGS += -fvisibility=hidden -fvisibility-inlines-hidden -funroll-loops 
# LOCAL_CPPFLAGS += -flto - silene zpomaleni 
# LOCAL_CPPFLAGS += -fomit-frame-pointer # pada


LOCAL_CFLAGS += -std=c99

LOCAL_LDLIBS := -lz -llog -ljnigraphics -lm -lGLESv1_CM -flto
include $(BUILD_SHARED_LIBRARY)

  