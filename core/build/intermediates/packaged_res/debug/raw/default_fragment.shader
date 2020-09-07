precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D rubyTexture;

void main()                                            
{
	   gl_FragColor = vec4(texture2D(rubyTexture, vTexCoord));
}