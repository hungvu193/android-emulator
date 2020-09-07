#ifndef SETTINGS_H_
#define SETTINGS_H_

#define BRIDGE_PACKAGE(x) Java_com_nostalgiaemulators_framework_base_JniBridge_ ## x
#define BUFFER_TYPE unsigned int
#define PALETTE_TYPE unsigned int
#define GET_PIXEL(buf, idx) buf[idx]
#endif
