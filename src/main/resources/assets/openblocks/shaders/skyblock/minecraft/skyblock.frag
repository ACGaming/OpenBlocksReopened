#version 120

uniform sampler2D skyboxTexture;
uniform vec2 screenSize;

void main() {
    vec2 uv = gl_FragCoord.xy / screenSize;
    vec4 color = texture2D(skyboxTexture, uv);
    gl_FragColor = vec4(color.xyz, 1.0);
}
