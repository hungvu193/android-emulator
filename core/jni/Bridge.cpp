// Copyright (c) 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 Radek Lzicar & Ales Lanik
//
// This file is part of Nostalgia Emulator Framework.
//
// Nostalgia Emulator Framework is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nostalgia Emulator Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Nostalgia Emulator Framework. If not, see <http://www.gnu.org/licenses/>.

#include <jni.h>
#include "Emulator.h"
#include "Bridge.h"
#include "settings.h"

extern "C" {
using namespace emudroid;

Emulator *emu;

#ifndef BRIDGE_PACKAGE
#define BRIDGE_PACKAGE :-)
#endif

Bridge::Bridge(Emulator *emulator) {
	emu = emulator;
}


JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(start)(JNIEnv *env, jobject obj, jint gfx, jint sfx, jint general) {
	return emu->start(gfx, sfx, general);
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(processCommand)(JNIEnv *env, jobject obj, jstring command) {
	jboolean isCopy;
	const char *cmd = env->GetStringUTFChars(command, &isCopy);
	bool success = emu->processCommand(cmd);
	env->ReleaseStringUTFChars(command, cmd);
	return success;
}



JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(readPalette)(JNIEnv *env, jobject obj, jintArray result) {
	return emu->readPalette(env, result);
}


JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(loadGame)(JNIEnv *env, jobject obj, jstring path, jstring batteryPath, jstring batteryFullPath) {
	jboolean isCopy;
	jboolean isCopy2;
	jboolean isCopy3;
	const char *fname = env->GetStringUTFChars(path, &isCopy);
	const char *fbattery = env->GetStringUTFChars(batteryPath, &isCopy2);
	const char *fbatteryFullPath = env->GetStringUTFChars(batteryFullPath, &isCopy3);
	bool success = emu->loadGame(fname, fbattery, fbatteryFullPath);

	env->ReleaseStringUTFChars(path, fname);
	env->ReleaseStringUTFChars(batteryPath, fbattery);
	env->ReleaseStringUTFChars(batteryFullPath, fbatteryFullPath);
	return success;
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(setBaseDir)(JNIEnv *env, jobject obj, jstring path) {
	jboolean isCopy;
	const char *fname = env->GetStringUTFChars(path, &isCopy);
	bool success = emu->setBaseDir(fname);
	env->ReleaseStringUTFChars(path, fname);
	return success;
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(enableCheat)(JNIEnv *env, jobject obj, jstring gg, jint type) {
	jboolean isCopy;
	const char *cheat = env->GetStringUTFChars(gg, &isCopy);
	bool success = emu->enableCheat(cheat, type);
	env->ReleaseStringUTFChars(gg, cheat);
	return success;
}


JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(enableRawCheat)(JNIEnv *env, jobject obj, jint addr, jint val, jint comp) {
	jboolean isCopy;
	bool success = emu->enableRawCheat(addr, val, comp);
	return success;
}


JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(emulate)(JNIEnv *env, jobject obj, jint keys, jint turbos, jint numFramesToSkip) {
	int res = emu->emulateFrame(keys, turbos, numFramesToSkip);
	return res;
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(render)(JNIEnv *env, jobject obj, jobject bitmap) {
	return emu->render(env, bitmap, -1, -1, NULL);
}


JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(renderVP)(JNIEnv *env, jobject obj, jobject bitmap, jint w, jint h) {
	return emu->render(env, bitmap, w, h, NULL);
}


JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(renderGL)(JNIEnv *env, jobject obj) {
	return emu->renderGL();
}

JNIEXPORT jint JNICALL BRIDGE_PACKAGE(getHistoryItemCount)(JNIEnv *env, jobject obj) {
	return emu->getHistoryItemCount();
}

JNIEXPORT jint JNICALL BRIDGE_PACKAGE(getInt)(JNIEnv *env, jobject obj, jstring name) {
	jboolean isCopy;
	const char *fname = env->GetStringUTFChars(name, &isCopy);
	int res = emu->getInt(fname);
	env->ReleaseStringUTFChars(name, fname);
	return res;
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(loadHistoryState)(JNIEnv *env, jobject obj, jint pos) {
	return emu->loadHistoryState(pos);
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(renderHistory)(JNIEnv *env, jobject obj, jobject bmp, jint pos, jint w, jint h) {
	return emu->renderHistory(env, bmp, pos, w, h);
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(setViewPortSize)(JNIEnv *env, jobject obj, jint w,
		jint h) {
	return emu->setViewPortSize(w, h);
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(fireZapper)(JNIEnv *env, jobject obj, jint x, jint y) {
	return emu->fireZapper(x, y);
}


JNIEXPORT jint JNICALL BRIDGE_PACKAGE(readSfxBuffer)(JNIEnv *env, jobject obj,
		jshortArray data) {
	return emu->readSfxBuffer(env, obj, data);
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(loadState)(JNIEnv *env, jobject obj,
		jstring path, jint slot) {
	jboolean isCopy;
	const char *fname = env->GetStringUTFChars(path, &isCopy);
	bool success = emu->loadState(fname, slot);
	env->ReleaseStringUTFChars(path, fname);
	return success;
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(saveState)(JNIEnv *env, jobject obj,
		jstring path, jint slot) {
	jboolean isCopy;
	const char *fname = env->GetStringUTFChars(path, &isCopy);
	bool success = emu->saveState(fname, slot);
	env->ReleaseStringUTFChars(path, fname);
	return success;

}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(reset)(JNIEnv *env, jobject obj,
		jstring path) {
	return emu->reset();
}

JNIEXPORT jboolean JNICALL BRIDGE_PACKAGE(stop)(JNIEnv *env, jobject obj) {
	return emu->stop();
}


}
