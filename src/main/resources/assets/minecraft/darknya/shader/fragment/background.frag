//10,8,15,9

#ifdef GL_ES
precision mediump float;
#endif
//from novoline

// glslsandbox uniforms
uniform float iTime;
uniform vec2 iResolution;

// shadertoy emulation
#define iiTime iTime
#define iiResolution vec3(iResolution,1.)

void mainImage(out vec4 O, vec2 I)
{
	float time = iTime;
	vec2 uv = (I*2.-iResolution.xy)/iResolution.y;

	float s = 0.;
	for (float p = 0.; p < 100.; p++) {
		#define R3P 1.22074408460575947536
        vec3 q = fract(.5+p*vec3(1./R3P, 1./(R3P*R3P), 1./(R3P*R3P*R3P)));
		float a = p*.001+time*(.02+q.z*.1);
		vec2 x = q.xy*mat2(sin(a*2.1), sin(a*4.13), sin(a*8.13), sin(a*4.18));
		float l = length(x-uv.xy);
		s += sin((l-q.z)*10.)/(1.+max(0., l-.01)*200.);
	}
	O = mix(vec4(.0, .08, .1, 1), vec4(0.015, 0.156, 0.788, 1), max(0., (s*.9)+0.1));
}

void main(void)
{
	mainImage(gl_FragColor, gl_FragCoord.xy);
}