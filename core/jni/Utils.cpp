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

#include "Utils.h"
#include <stdio.h>
#include <stdlib.h>

namespace emudroid {
	char* Utils::loadFile(const char *filename, int *size) {
		FILE *fp;
		long len;
		fp = fopen(filename, "rb");
		fseek(fp, 0, SEEK_END);
		len = ftell(fp);
		*size = len;
		fseek(fp, 0, SEEK_SET);
		char *buf = (char *) malloc(len);
		fread(buf, len, 1, fp);
		fclose(fp);
		return buf;
	}
} 
