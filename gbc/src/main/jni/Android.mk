LOCAL_PATH := $(call my-dir)
EMUDROID_PATH := $(LOCAL_PATH)/../../Framework/jni
include $(CLEAR_VARS)


RESAMPLER_DIR := $(LOCAL_PATH)/common/resample/src

FILES := $(wildcard $(LOCAL_PATH)/libgambatte/src/file/unzip/*.c) \
$(wildcard $(LOCAL_PATH)/libgambatte/src/file/*.cpp) \
$(wildcard $(LOCAL_PATH)/libgambatte/src/sound/*.cpp) \
$(wildcard $(LOCAL_PATH)/libgambatte/src/video/*.cpp) \
$(wildcard $(LOCAL_PATH)/libgambatte/src/mem/*.cpp)  $(wildcard $(LOCAL_PATH)/libgambatte/src/*.cpp) \
$(RESAMPLER_DIR)/chainresampler.cpp $(RESAMPLER_DIR)/i0.cpp $(RESAMPLER_DIR)/makesinckernel.cpp $(RESAMPLER_DIR)/resamplerinfo.cpp $(RESAMPLER_DIR)/u48div.cpp \
$(EMUDROID_PATH)/Emulator.cpp $(EMUDROID_PATH)/Bridge.cpp 
				
FILES := $(FILES:$(LOCAL_PATH)/%=%) Gbc.cpp   	


include $(CLEAR_VARS)
#$(warning $(FILES))
LOCAL_C_INCLUDES := $(LOCAL_PATH) $(EMUDROID_PATH) $(LOCAL_PATH)/common $(LOCAL_PATH)/common/resample $(LOCAL_PATH)/include  $(LOCAL_PATH)/libgambatte/include $(LOCAL_PATH)/libgambatte/src $(LOCAL_PATH)/src/file $(LOCAL_PATH)/src/mem $(LOCAL_PATH)/common $(LOCAL_PATH)/libgambatte/src/file $(LOCAL_PATH)/src/file/unzip
LOCAL_SRC_FILES :=   $(FILES) 
LOCAL_MODULE := nostalgia

#LOCAL_CFLAGS = -Wall -Wextra -O2 -fomit-frame-pointer
LOCAL_CPPFLAGS = -fno-exceptions -fno-rtti -DINLINE=inline -DHAVE_STDINT_H -DINT_LEAST_32 -DHAVE_INTTYPES_H -DLSB_FIRST
LOCAL_CPPFLAGS +=  -Ofast -ffast-math  # -fno-tree-vectorize ZPOMALUJE
#LOCAL_CPPFLAGS += -fexceptions -frtti
LOCAL_CPPFLAGS += -fno-builtin-sin -fno-builtin-cos 
# LOCAL_CPPFLAGS += -ffunction-sections -fdata-sections # - ZPOMALUJE
# LOCAL_CPPFLAGS += -mfloat-abi=softfp -mfpu=vfp - pro ARM7 automaticky a jine nepodporuji
LOCAL_CPPFLAGS += -fmerge-all-constants
LOCAL_CPPFLAGS += -Wno-psabi  -Wa,--noexecstack # obrovske zrychleni
LOCAL_CPPFLAGS += -fsingle-precision-constant
LOCAL_CPPFLAGS += -fvisibility=hidden -fvisibility-inlines-hidden -funroll-loops 
# LOCAL_CPPFLAGS += -flto - silene zpomaleni 
# LOCAL_CPPFLAGS += -fomit-frame-pointer # pada

LOCAL_LDLIBS := -lz -llog -ljnigraphics -lm -lGLESv1_CM
include $(BUILD_SHARED_LIBRARY)




	 