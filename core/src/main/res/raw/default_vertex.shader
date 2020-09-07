attribute vec4 aPosition; 
attribute vec2 aTexCoord;
varying vec2 vTexCoord;
uniform mat4 uMVPMatrix;
			 
void main() {
	vTexCoord = aTexCoord;
	gl_Position = uMVPMatrix * vec4(aPosition.xyz, 1.0);
}