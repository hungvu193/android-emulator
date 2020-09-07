

LOCAL_PATH := $(call my-dir)


EMUDROID_PATH := $(LOCAL_PATH)/../../Framework/jni

include $(CLEAR_VARS)



SMS_SRC	=  sms_plus/z80.c	\
		sms_plus/sms.c	\
		sms_plus/pio.c	\
		sms_plus/memz80.c	\
		sms_plus/render.c	\
		sms_plus/tms.c	\
		sms_plus/vdp.c	\
		sms_plus/system.c \
		sms_plus/error.c
			        
SMS_SRC	+=	sms_plus/fileio.c	\
		sms_plus/state.c	\
		sms_plus/loadrom.c
	        

SMS_SRC	+=	sms_plus/ioapi.c	\
		sms_plus/unzip.c


 



SMS_SRC	+=      sms_plus/sound.c	\
		sms_plus/sn76489.c	\
		sms_plus/emu2413.c	\
		sms_plus/ym2413.c	\
		sms_plus/fmintf.c	\
		sms_plus/stream.c






FILES :=   $(SMS_SRC) $(EMUDROID_PATH)/Emulator.cpp $(EMUDROID_PATH)/Bridge.cpp 
FILES := $(FILES:$(LOCAL_PATH)/%=%)  Sms.cpp

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

LOCAL_CPPFLAGS += -DLSB_FIRST=1  
LOCAL_CFLAGS += -DLSB_FIRST=1


LOCAL_LDLIBS := -lz -llog -ljnigraphics -lm -lGLESv1_CM -flto
include $(BUILD_SHARED_LIBRARY)

  