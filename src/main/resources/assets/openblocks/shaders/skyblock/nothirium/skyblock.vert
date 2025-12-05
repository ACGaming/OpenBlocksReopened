#version 110

uniform mat4 u_ModelViewProjectionMatrix;

attribute vec3 a_Pos;
attribute vec3 a_Offset;
attribute float shaderType;

varying vec3 position;
varying float vShaderType;

void main() {
    gl_Position = u_ModelViewProjectionMatrix * vec4(a_Pos + a_Offset, 1.0);
    position = gl_Position.xyz;
    vShaderType = shaderType;
}
