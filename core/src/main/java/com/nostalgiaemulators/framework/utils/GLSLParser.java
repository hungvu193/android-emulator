package com.nostalgiaemulators.framework.utils;

import java.io.BufferedReader;
import java.io.StringReader;

public class GLSLParser {
	public GLSLParser(String glsl) {
		glsl = glsl.trim();
		BufferedReader reader = new BufferedReader(new StringReader(glsl));
		StringBuilder vertex = new StringBuilder();
		StringBuilder fragment = new StringBuilder();
		int state = 0;
		String line;
		boolean wasEndif = false;
		try {
			while ((line = reader.readLine()) != null) {
				if (state == 0) {
					if (line.equals("#elif defined(FRAGMENT)")) {
						state = 1;
					} else if (!line.equals("#if defined(VERTEX)")) {
						vertex.append(line).append('\n');
					}
				} else if (state == 1) {
					// tohle dela jen to, ze uplne posledni #endif se vynecha
					if (wasEndif) {
						fragment.append("#endif").append('\n');
						wasEndif = false;
					}
					if (line.equals("#endif")) {
						wasEndif = true;
					} else {
						fragment.append(line).append('\n');
					}
				}
			}
		} catch (Exception e) {
			Log.e("GLSL", e.toString());
		}
		this.vertex = vertex.toString();
		this.fragment = fragment.toString();
	}

	public String getVertexShader() {
		return vertex;
	}

	public String getFragmentShader() {
		return fragment;
	}

	String vertex;
	String fragment;
}
